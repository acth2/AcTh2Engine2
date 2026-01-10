package fr.acth2.engine.engine;

import de.matthiasmann.twl.utils.PNGDecoder;
import fr.acth2.engine.Main;
import fr.acth2.engine.engine.models.Item;
import fr.acth2.engine.engine.models.Mesh;
import fr.acth2.engine.engine.models.Transformation;
import fr.acth2.engine.utils.Refs;
import org.joml.Matrix4f;
import org.lwjgl.openvr.Texture;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static fr.acth2.engine.Main.*;

public class Renderer {

    private static Transformation transformation;

    public Renderer() {
        transformation = new Transformation();
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void init() {
        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("texture_sampler");
        shaderProgram.createUniform("colour");
        shaderProgram.createUniform("useColour");
    }

    public void render(Item... items) {

        shaderProgram.bind();

        // --- SET GLOBAL UNIFORMS ---
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width  = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);

            glfwGetWindowSize(Main.getInstance().id, width, height);

            Matrix4f projectionMatrix =
                    transformation.getProjectionMatrix(
                            Refs.PROJECTION_FOV,
                            width.get(0),
                            height.get(0),
                            Refs.PROJECTION_Z_NEAR,
                            Refs.PROJECTION_Z_FAR);

            shaderProgram.setUniform("projectionMatrix", projectionMatrix);
        }

        Matrix4f viewMatrix = transformation.getViewMatrix(getInstance().camera);
        shaderProgram.setUniform("texture_sampler", 0);

        for(Item gameItem : items) {
            Mesh mesh = gameItem.getMesh();
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(gameItem, viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);

            shaderProgram.setUniform("colour", mesh.getColour());
            shaderProgram.setUniform("useColour", mesh.isTextured() ? 0 : 1);
            gameItem.getMesh().render();
        }

        shaderProgram.unbind();
    }

    public static PNGDecoder loadTexture(String texture) {
        try {
            return new PNGDecoder(
                    Texture.class.getResourceAsStream("/textures/" + texture));
        } catch (IOException e) {
            System.err.println("ERROR during loading of the texture: " + texture);
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
