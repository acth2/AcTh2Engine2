package fr.acth2.engine.engine;

import fr.acth2.engine.Main;
import fr.acth2.engine.engine.light.DirectionalLight;
import fr.acth2.engine.engine.light.PointLight;
import fr.acth2.engine.engine.light.SpotLight;
import fr.acth2.engine.engine.models.Item;
import fr.acth2.engine.engine.models.Mesh;
import fr.acth2.engine.engine.models.Transformation;
import fr.acth2.engine.utils.Refs;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.opengl.GL11.*;
import static fr.acth2.engine.Main.shaderProgram;

public class Renderer {

    private static final int MAX_POINT_LIGHTS = 5;
    private static final int MAX_SPOT_LIGHTS = 5;
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
        shaderProgram.createMaterialUniform("material");
        shaderProgram.createUniform("specularPower");
        shaderProgram.createUniform("ambientLight");
        shaderProgram.createDirectionalLightUniform("directionalLight");
        shaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
        shaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        shaderProgram.createUniform("pointLightCount");
        shaderProgram.createUniform("spotLightCount");
    }

    public void render(Item[] items, Vector3f ambientLight, PointLight[] pointLights, SpotLight[] spotLights, DirectionalLight directionalLight) {
        shaderProgram.bind();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetWindowSize(Main.getInstance().id, width, height);
            Matrix4f projectionMatrix = transformation.getProjectionMatrix(
                    Refs.PROJECTION_FOV, width.get(0), height.get(0), Refs.PROJECTION_Z_NEAR, Refs.PROJECTION_Z_FAR);
            shaderProgram.setUniform("projectionMatrix", projectionMatrix);
        }

        Matrix4f viewMatrix = transformation.getViewMatrix(Main.getInstance().camera);

        int numLights = pointLights != null ? pointLights.length : 0;
        PointLight[] viewPointLights = new PointLight[numLights];
        for (int i = 0; i < numLights; i++) {
            PointLight currPointLight = new PointLight(pointLights[i]);
            Vector3f lightPos = currPointLight.getPosition();
            Vector4f aux = new Vector4f(lightPos, 1);
            aux.mul(viewMatrix);
            lightPos.x = aux.x;
            lightPos.y = aux.y;
            lightPos.z = aux.z;
            viewPointLights[i] = currPointLight;
        }

        numLights = spotLights != null ? spotLights.length : 0;
        SpotLight[] viewSpotLights = new SpotLight[numLights];
        for (int i = 0; i < numLights; i++) {
            SpotLight currSpotLight = new SpotLight(spotLights[i]);
            Vector4f lightPos = new Vector4f(currSpotLight.getPointLight().getPosition(), 1);
            lightPos.mul(viewMatrix);
            currSpotLight.getPointLight().setPosition(new Vector3f(lightPos.x, lightPos.y, lightPos.z));

            Vector4f coneDir = new Vector4f(currSpotLight.getConeDirection(), 0);
            coneDir.mul(viewMatrix);
            currSpotLight.setConeDirection(new Vector3f(coneDir.x, coneDir.y, coneDir.z));
            viewSpotLights[i] = currSpotLight;
        }

        DirectionalLight currDirLight = new DirectionalLight(directionalLight);
        Vector4f dir = new Vector4f(currDirLight.getPosition(), 0);
        dir.mul(viewMatrix);
        currDirLight.setPosition(new Vector3f(dir.x, dir.y, dir.z));

        shaderProgram.setUniform("ambientLight", ambientLight);
        shaderProgram.setUniform("specularPower", 10f);
        shaderProgram.setUniform("pointLightCount", viewPointLights.length);
        shaderProgram.setUniform("pointLights", viewPointLights);
        shaderProgram.setUniform("spotLightCount", viewSpotLights.length);
        shaderProgram.setUniform("spotLights", viewSpotLights);
        shaderProgram.setUniform("directionalLight", currDirLight);

        shaderProgram.setUniform("texture_sampler", 0);

        for (Item gameItem : items) {
            Mesh mesh = gameItem.getMesh();
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(gameItem, viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            shaderProgram.setUniform("material", mesh.getMaterial());
            mesh.render();
        }

        shaderProgram.unbind();
    }
}
