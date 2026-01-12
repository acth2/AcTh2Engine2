package fr.acth2.engine.scene;

import fr.acth2.engine.engine.Texture;
import fr.acth2.engine.engine.items.Item;
import fr.acth2.engine.engine.light.DirectionalLight;
import fr.acth2.engine.engine.light.PointLight;
import fr.acth2.engine.engine.light.SpotLight;
import fr.acth2.engine.engine.models.Material;
import fr.acth2.engine.engine.models.Mesh;
import fr.acth2.engine.engine.models.skybox.SkyBox;
import fr.acth2.engine.utils.Time;
import fr.acth2.engine.utils.loader.Loader;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class Scene {

    private List<Item> gameItems;
    private SkyBox skyBox;
    private SceneLight sceneLight;
    private Item spotLightItem;
    private float temp = 0.0F;
    private long lastTime = System.currentTimeMillis();
    private Vector3f skyBoxRotation;

    public Scene() {
        gameItems = new ArrayList<>();
        skyBoxRotation = new Vector3f(0,0,0);
    }

    public void init() throws Exception {
        float reflectance = 1f;
        Mesh cubeMesh = Loader.loadMesh("/models/cuboid.obj");
        Material cubeMaterial = new Material(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), reflectance);
        cubeMesh.setMaterial(cubeMaterial);
        cubeMesh.attachTexture(new Texture("/textures/v2.png"));

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Item cubeItem = new Item(cubeMesh);
                cubeItem.setPosition(i * 2, 0, j * 2 - 2);
                gameItems.add(cubeItem);
            }
        }

        sceneLight = new SceneLight();
        sceneLight.setAmbientLight(new Vector3f(0.1f, 0.1f, 0.1f));
        sceneLight.setPointLights(new PointLight[0]);

        SpotLight[] spotLights = new SpotLight[0];
        sceneLight.setSpotLights(spotLights);

        Vector3f sunPosition = new Vector3f(1911, 1274, 1046);
        Vector3f lightDirection = new Vector3f(sunPosition).normalize().negate();

        sceneLight.setDirectionalLight(new DirectionalLight(new Vector3f(1,1,1), lightDirection, 1.0f));

        String[] textureFiles = new String[] {
            "/textures/skybox/hills_rt.png",
            "/textures/skybox/hills_lf.png",
            "/textures/skybox/hills_up.png",
            "/textures/skybox/hills_dn.png",
            "/textures/skybox/hills_bk.png",
            "/textures/skybox/hills_ft.png"
        };
        skyBox = new SkyBox(textureFiles);
    }

    public void update() {
        // disabled
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

    public Vector3f getSkyBoxRotation() {
        return skyBoxRotation;
    }

    public void rotateSkyBox(float x, float y, float z) {
        this.skyBoxRotation.x += x;
        this.skyBoxRotation.y += y;
        this.skyBoxRotation.z += z;
    }
}
