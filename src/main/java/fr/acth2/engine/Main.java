package fr.acth2.engine;

import fr.acth2.engine.engine.Renderer;
import fr.acth2.engine.engine.ShaderProgram;
import fr.acth2.engine.engine.Texture;
import fr.acth2.engine.engine.camera.Camera;
import fr.acth2.engine.engine.light.DirectionalLight;
import fr.acth2.engine.engine.light.PointLight;
import fr.acth2.engine.engine.models.Item;
import fr.acth2.engine.engine.models.Material;
import fr.acth2.engine.engine.models.Mesh;
import fr.acth2.engine.inputs.KeyManager;
import fr.acth2.engine.inputs.MouseInput;
import fr.acth2.engine.utils.Time;
import fr.acth2.engine.utils.loader.Loader;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

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
    public static ShaderProgram shaderProgram;
    private static Item[] items;
    private Vector3f ambientLight;
    private PointLight pointLight;
    private DirectionalLight directionalLight;


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

    public static void main(String[] args) {
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

        Main.getInstance().id = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_TITLE, NULL, NULL);
        if (getWindowID() == NULL) throw new RuntimeException("Failed to create window");
        this.mouseInput.init(getWindowID());

        glfwSetFramebufferSizeCallback(getWindowID(), (window, width, height) -> {
            glViewport(0, 0, width, height);
        });

        glfwMakeContextCurrent(getWindowID());
        GL.createCapabilities();
        if (DEBUG_BACKGROUND)
            glClearColor(0.2f, 0.3f, 0.4f, 1.0f);

        glfwSwapInterval(1);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glfwShowWindow(getWindowID());
        glfwFocusWindow(getWindowID());
        glfwSetInputMode(getWindowID(), GLFW_CURSOR, GRABBED_CURSOR ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);

        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Loader.loadResource("/shaders/vertex.glsl"));
        shaderProgram.createFragmentShader(Loader.loadResource("/shaders/fragment.glsl"));
        shaderProgram.link();

        renderer.init();

        float reflectance = 1f;
        Mesh mesh = Loader.loadMesh("/models/cuboid.obj");
        Material material = new Material(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), reflectance);
        mesh.setMaterial(material);
        mesh.attachTexture(new Texture("/textures/v2.png"));
        Item item = new Item(mesh);
        item.setPosition(0, 0, -2);

        Mesh lightMesh = Loader.loadMesh("/models/light.obj");
        Material lightMaterial = new Material(new Vector4f(1f, 1f, 1f, 1.0f), 0f, true);
        lightMesh.setMaterial(lightMaterial);
        Item lightItem = new Item(lightMesh);

        ambientLight = new Vector3f(0.2f, 0.2f, 0.2f);

        Vector3f lightPosition = new Vector3f(-1, 0, 0);
        Vector3f lightColor = new Vector3f(1, 1, 1);
        pointLight = new PointLight(lightColor, lightPosition, 1.0f);
        lightItem.setPosition(lightPosition.x, lightPosition.y, lightPosition.z);

        Vector3f directionalLightColor = new Vector3f(1.0f, 1.0f, 0.8f);
        Vector3f directionalLightDirection = new Vector3f(0, -1, -1);
        directionalLight = new DirectionalLight(directionalLightColor, directionalLightDirection, 0.7f);

        Mesh sunMesh = Loader.loadMesh("/models/sun.obj");
        Material sunMaterial = new Material(new Vector4f(1f, 1f, 1f, 1.0f), 0f, true);
        sunMesh.setMaterial(sunMaterial);
        Item sunItem = new Item(sunMesh);
        sunItem.setScale(0.1f);

        items = new Item[]{item, lightItem, sunItem};


        System.out.println("GLFW Window ID: " + getWindowID() + "\n");

        System.out.println("Controls: ");
        System.out.println(" - X = STOP TIME");
        System.out.println(" - TAB        = FOCUS AND TAKE CAMERA");
        System.out.println(" - CONTROLS   = BOOST");
        System.out.println(" - SHIFT      = HEAD DOWN");
        System.out.println(" - SPACE      = HEAD UP");
        System.out.println(" - WASD       = MOVEMENT");
        System.out.println(" - ESC        = EXIT");
    }

    private static void loop() {
        Main instance = Main.getInstance();

        double lastFpsTime = getTime();
        int frames = 0;

        while (!glfwWindowShouldClose(getWindowID())) {

            instance.inputs(instance.id, instance.mouseInput);
            instance.render();

            frames++;

            double now = getTime();
            if (now - lastFpsTime >= 1000) {
                glfwSetWindowTitle(getWindowID(), WINDOW_TITLE + " | FPS: " + frames);
                frames = 0;
                lastFpsTime = now;
            }
        }
    }

    public void start() {
        loopThread = new Thread(Main.getInstance(), "GAME_LOOP_THREAD");
        loopThread.start();
    }

    float temp = 0.0F;
    long lastTime = System.currentTimeMillis();

    public void render() {
        renderer.clear();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);

            glfwGetWindowSize(getInstance().id, width, height);

            int w = width.get(0);
            int h = height.get(0);
            float aspectRatio = (float) w / h;
            projectionMatrix = new Matrix4f().perspective(FOV, aspectRatio,
                    Z_NEAR, Z_FAR);
        } catch (Exception e) {
            float aspectRatio = (float) WINDOW_WIDTH / WINDOW_HEIGHT;
            projectionMatrix = new Matrix4f().perspective(FOV, aspectRatio,
                    Z_NEAR, Z_FAR);
        }

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastTime) / 1000.0f;
        lastTime = currentTime;

        if (!Time.isTimeStopped()) {
            temp += deltaTime;
        }

        float sunX = (float)Math.sin(temp) * 10.0f;
        float sunZ = (float)Math.cos(temp) * 10.0f - 2.0f;
        items[2].setPosition(sunX, 10.0f, sunZ);
        
        Vector3f sunDirection = new Vector3f(-sunX, -10.0f, -sunZ);
        sunDirection.normalize();
        directionalLight.setPosition(sunDirection);

        float pointLightY = (float)Math.sin(temp / 2) * 2.0f;
        float pointLightZ = (float)Math.cos(temp / 2) * 2.0f - 2.0f;
        pointLight.setPosition(new Vector3f(0, pointLightY, pointLightZ));
        items[1].setPosition(0, pointLightY, pointLightZ);


        renderer.render(items, ambientLight, pointLight, directionalLight);

        shaderProgram.unbind();
        glfwSwapBuffers(getWindowID());
        glfwPollEvents();
    }

    public void inputs(long window, MouseInput mouseInput) {
        KeyManager.update();
        mouseInput.input(window);

        if (KeyManager.getKeyJustPressed(GLFW_KEY_X)) {
            Time.stopTime();
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
            glfwSetInputMode(getWindowID(), GLFW_CURSOR, GRABBED_CURSOR ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
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

    private static long getWindowID() {
        return Main.getInstance().id;
    }

    private static double getTime() {
        return System.currentTimeMillis();
    }
}
