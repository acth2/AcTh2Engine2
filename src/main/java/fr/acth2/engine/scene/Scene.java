package fr.acth2.engine.scene;

import fr.acth2.engine.engine.Texture;
import fr.acth2.engine.engine.items.Item;
import fr.acth2.engine.engine.light.DirectionalLight;
import fr.acth2.engine.engine.light.PointLight;
import fr.acth2.engine.engine.light.SpotLight;
import fr.acth2.engine.engine.models.Material;
import fr.acth2.engine.engine.models.Mesh;
import fr.acth2.engine.engine.models.heightmap.Terrain;
import fr.acth2.engine.engine.models.skybox.SkyBox;
import fr.acth2.engine.utils.Time;
import fr.acth2.engine.utils.loader.Loader;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scene {

    private List<Item> gameItems;
    private SkyBox skyBox;
    private SceneLight sceneLight;
    private Terrain terrain;
    private Item sun;

    public Scene() {
        gameItems = new ArrayList<>();
    }

    public void init() throws Exception {
        sceneLight = new SceneLight();
        sceneLight.setAmbientLight(new Vector3f(0.8f, 0.8f, 0.8f));
        sceneLight.setPointLights(new PointLight[0]);
        sceneLight.setSpotLights(new SpotLight[0]);
        sceneLight.setDirectionalLight(new DirectionalLight(new Vector3f(1,1,1), new Vector3f(-1, -1, -1), 0.8f));

        terrain = new Terrain(2, 32, 0.0F, 0.25F, "/textures/heightmap.png", "/textures/v2.png", 16);
        gameItems.addAll(Arrays.asList(terrain.getGameItems()));

        Mesh sunMesh = Loader.loadMesh("/models/light.obj");
        Material sunMaterial = new Material(new Vector4f(1f, 1f, 1f, 1.0f), 0f, true);
        sunMesh.setMaterial(sunMaterial);
        sun = new Item(sunMesh);
        sun.setScale(10f);
        gameItems.add(sun);
    }

    public void update() {
        Vector3f lightDirection = new Vector3f(sceneLight.getDirectionalLight().getPosition()).normalize().negate();
        sun.setPosition(lightDirection.x * 100, lightDirection.y * 100, lightDirection.z * 100);
    }
    
    public void cleanUp() {
        for (Item item : gameItems) {
            item.getMesh().cleanUp();
        }
    }

    public List<Item> getGameItems() {
        return gameItems;
    }

    public void setGameItems(List<Item> gameItems) {
        this.gameItems = gameItems;
    }

    public SkyBox getSkyBox() {
        return skyBox;
    }

    public void setSkyBox(SkyBox skyBox) {
        this.skyBox = skyBox;
    }

    public SceneLight getSceneLight() {
        return sceneLight;
    }

    public void setSceneLight(SceneLight sceneLight) {
        this.sceneLight = sceneLight;
    }
}
