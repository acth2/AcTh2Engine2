package fr.acth2.engine;

import fr.acth2.engine.engine.Renderer;
import fr.acth2.engine.engine.ShaderProgram;
import fr.acth2.engine.engine.Texture;
import fr.acth2.engine.engine.camera.Camera;
import fr.acth2.engine.engine.light.DirectionalLight;
import fr.acth2.engine.engine.light.PointLight;
import fr.acth2.engine.engine.light.SpotLight;
import fr.acth2.engine.engine.models.items.Item;
import fr.acth2.engine.engine.models.Material;
import fr.acth2.engine.engine.models.Mesh;
import fr.acth2.engine.inputs.KeyManager;
import fr.acth2.engine.inputs.MouseInput;
import fr.acth2.engine.utils.Time;
import fr.acth2.engine.utils.hud.Hud;
import fr.acth2.engine.utils.loader.Loader;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static fr.acth2.engine.utils.Refs.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main implements Runnable {
    public long id;
    public Camera camera;
    public MouseInput mouseInput;

    private static final float FOV = PROJECTION_FOV;
    private static final float Z_NEAR = PROJECTION_Z_NEAR;
    private static final float Z_FAR = PROJECTION_Z_FAR;
    private Matrix4f projectionMatrix;

    private static Thread loopThread;
    private static Main main;
    private static Renderer renderer;
    public ShaderProgram shaderProgram;
    public ShaderProgram hudShaderProgram;
    private Item[] items;
    private Vector3f ambientLight;
    private PointLight[] pointLights;
    private SpotLight[] spotLights;
    private DirectionalLight directionalLight;
    private Item spotLightItem;
    public Hud hud;


    public Main() {
        this.renderer = new Renderer();
        this.camera = new Camera();
        this.mouseInput = new MouseInput();
    }

    public static Main getInstance() {
        return main;
    }

    public static void setInstance(Main instance) {
        main = instance;
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        Main.setInstance(new Main());
        Main.getInstance().start();
    }

    private void load() throws Exception {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        System.out.println("GLFW initialized");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, RESIZABLE ? GLFW_TRUE : GLFW_FALSE);

        this.id = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_TITLE, NULL, NULL);
        if (this.id == NULL) throw new RuntimeException("Failed to create window");
        this.mouseInput.init(this.id);

        glfwSetFramebufferSizeCallback(this.id, (window, width, height) -> {
            glViewport(0, 0, width, height);
            hud.updateSize(this.id);
        });

        glfwMakeContextCurrent(this.id);
        GL.createCapabilities();
        if (DEBUG_BACKGROUND)
            glClearColor(0.2f, 0.3f, 0.4f, 1.0f);

        glfwSwapInterval(1);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glfwShowWindow(this.id);
        glfwFocusWindow(this.id);
        glfwSetInputMode(this.id, GLFW_CURSOR, GRABBED_CURSOR ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
        
        hud = new Hud();
        hud.updateSize(this.id);

        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Loader.loadResource("/shaders/vertex.glsl"));
        shaderProgram.createFragmentShader(Loader.loadResource("/shaders/fragment.glsl"));
        shaderProgram.link();

        hudShaderProgram = new ShaderProgram();
        hudShaderProgram.createVertexShader(Loader.loadResource("/shaders/hud_vertex.glsl"));
        hudShaderProgram.createFragmentShader(Loader.loadResource("/shaders/hud_fragment.glsl"));
        hudShaderProgram.link();

        renderer.init(shaderProgram, hudShaderProgram);

        List<Item> allItems = new ArrayList<>();

        float reflectance = 1f;
        Mesh cubeMesh = Loader.loadMesh("/models/cuboid.obj");
        Material cubeMaterial = new Material(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), reflectance);
        cubeMesh.setMaterial(cubeMaterial);
        cubeMesh.attachTexture(new Texture("/textures/v2.png"));

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Item cubeItem = new Item(cubeMesh);
                cubeItem.setPosition(i * 2, 0, j * 2 - 2);
                allItems.add(cubeItem);
            }
        }

        ambientLight = new Vector3f(0.1f, 0.1f, 0.1f);

        pointLights = new PointLight[0];

        spotLights = new SpotLight[1];
        Mesh spotLightMesh = Loader.loadMesh("/models/light.obj");
        Material spotLightMaterial = new Material(new Vector4f(1f, 1f, 1f, 1.0f), 0f, true);
        spotLightMesh.setMaterial(spotLightMaterial);
        spotLightItem = new Item(spotLightMesh);
        spotLightItem.setScale(1f);
        allItems.add(spotLightItem);
        PointLight spotPointLight = new PointLight(new Vector3f(1,1,1), new Vector3f(0,5,0), 1.0f);
        spotLights[0] = new SpotLight(spotPointLight, new Vector3f(0,-1,0), (float)Math.cos(Math.toRadians(30)));

        directionalLight = new DirectionalLight(new Vector3f(0,0,0), new Vector3f(0,0,0), 0);

        items = allItems.toArray(new Item[0]);

        System.out.println("GLFW Window ID: " + this.id + "\n");

        System.out.println("Controls: ");
        System.out.println(" - X = STOP TIME");
        System.out.println(" - TAB        = FOCUS AND TAKE CAMERA");
        System.out.println(" - CONTROLS   = BOOST");
        System.out.println(" - SHIFT      = HEAD DOWN");
        System.out.println(" - SPACE      = HEAD UP");
        System.out.println(" - WASD       = MOVEMENT");
        System.out.println(" - ESC        = EXIT");

        hud.setPersistentText("tabTxt", "PRESS TAB TO TAKE THE CAMERA");
    }

    private static void loop() {
        Main instance = Main.getInstance();

        double lastFpsTime = getTime();
        int frames = 0;

        while (!glfwWindowShouldClose(instance.id)) {

            instance.inputs(instance.id, instance.mouseInput);
            instance.render();

            frames++;

            double now = getTime();
            if (now - lastFpsTime >= 1000) {
                glfwSetWindowTitle(instance.id, WINDOW_TITLE + " | FPS: " + frames);
                frames = 0;
                lastFpsTime = now;
            }
        }
    }

    public void start() {
        loopThread = new Thread(this, "GAME_LOOP_THREAD");
        loopThread.start();
    }

    float temp = 0.0F;
    long lastTime = System.currentTimeMillis();

    public void render() {
        renderer.clear();

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastTime) / 1000.0f;
        lastTime = currentTime;

        if (!Time.isTimeStopped()) {
            temp += deltaTime;
        }

        hud.update();

        float spotLightY = (float) (3.0f + (Math.sin(temp) * 2.0f));
        spotLights[0].getPointLight().setPosition(new Vector3f(0, spotLightY, 0));
        spotLightItem.setPosition(0, spotLightY, 0);

        renderer.render(this.id, this.camera, this.shaderProgram, items, ambientLight, pointLights, spotLights, directionalLight);
        renderer.renderHud(this.id, this.hudShaderProgram, hud);

        glfwSwapBuffers(this.id);
        glfwPollEvents();
    }

    public void inputs(long window, MouseInput mouseInput) {
        KeyManager.update();
        mouseInput.input(window);

        if (KeyManager.getKeyJustPressed(GLFW_KEY_X)) {
            Time.stopTime();
            hud.showInformation("TIME ALTERED", 1000);
        }

        float speed = 0.05f;

        if (KeyManager.getKeyJustPressed(GLFW_KEY_LEFT_CONTROL)) {
            speed = 15f;
        }

        if (KeyManager.getKeyPress(GLFW_KEY_W)) {
             camera.movePosition(0.0F, 0.0F, -speed);
        }

        if (KeyManager.getKeyPress(GLFW_KEY_A)) {
             camera.movePosition(-speed, 0.0F, 0.0F);
        }

        if (KeyManager.getKeyPress(GLFW_KEY_S)) {
             camera.movePosition(0.0F, 0.0F, speed);
        }

        if (KeyManager.getKeyPress(GLFW_KEY_D)) {
             camera.movePosition(speed, 0.0F, 0.0F);
        }

        if (KeyManager.getKeyPress(GLFW_KEY_LEFT_SHIFT)) {
             camera.movePosition(0.0F, -speed, 0.0F);
        }

        if (KeyManager.getKeyPress(GLFW_KEY_SPACE)) {
             camera.movePosition(0.0F, speed, 0.0F);
        }

        if (KeyManager.getKeyPress(GLFW_KEY_ESCAPE)) {
            System.exit(0);
        }

        if (KeyManager.getKeyJustPressed(GLFW_KEY_TAB)) {
            GRABBED_CURSOR = !GRABBED_CURSOR;
            glfwSetInputMode(this.id, GLFW_CURSOR, GRABBED_CURSOR ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);

            if (GRABBED_CURSOR) {
                hud.showInformation("CAMERA TAKEN", 1000);
                hud.removePersistentText("tabTxt");
            } else
                hud.showInformation("CAMERA RELEASED", 1000);
        }

        if (GRABBED_CURSOR) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }
    }

    public void cleanUp() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
        if (hudShaderProgram != null) {
            hudShaderProgram.cleanup();
        }
        if (hud != null) {
            hud.cleanUp();
        }
        for (Item item : items) {
            item.getMesh().cleanUp();
        }
    }

    @Override
    public void run() {
        try {
            load();
            loop();
        } catch (Exception excp) {
            excp.printStackTrace();
        } finally {
            cleanUp();
        }
    }
}
