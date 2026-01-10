package fr.acth2.engine.engine;

import fr.acth2.engine.Main;
import fr.acth2.engine.engine.models.Item;
import fr.acth2.engine.engine.models.Mesh;
import fr.acth2.engine.engine.models.Transformation;
import fr.acth2.engine.utils.Refs;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.opengl.GL11.*;
import static fr.acth2.engine.Main.shaderProgram;

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

        Matrix4f viewMatrix = transformation.getViewMatrix(Main.getInstance().camera);
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
}
