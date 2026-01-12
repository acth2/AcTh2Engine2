package fr.acth2.engine;

import fr.acth2.engine.engine.Renderer;
import fr.acth2.engine.engine.ShaderProgram;
import fr.acth2.engine.engine.camera.Camera;
import fr.acth2.engine.inputs.KeyManager;
import fr.acth2.engine.inputs.MouseInput;
import fr.acth2.engine.scene.Scene;
import fr.acth2.engine.utils.Time;
import fr.acth2.engine.utils.hud.Hud;
import fr.acth2.engine.utils.loader.Loader;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.awt.Font;

import static fr.acth2.engine.utils.Refs.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main implements Runnable {
    public long id;
    public Camera camera;
    public MouseInput mouseInput;

    private static Thread loopThread;
    private static Main main;
    private static Renderer renderer;
    public ShaderProgram shaderProgram;
    public ShaderProgram hudShaderProgram;
    public ShaderProgram skyboxShaderProgram;
    public Hud hud;
    private Scene scene;


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

        skyboxShaderProgram = new ShaderProgram();
        skyboxShaderProgram.createVertexShader(Loader.loadResource("/shaders/skybox_vertex.glsl"));
        skyboxShaderProgram.createFragmentShader(Loader.loadResource("/shaders/skybox_fragment.glsl"));
        skyboxShaderProgram.link();

        renderer.init(shaderProgram, hudShaderProgram, skyboxShaderProgram);

        scene = new Scene();
        scene.init();

        System.out.println("GLFW Window ID: " + this.id + "\n");

        System.out.println("Controls: ");
        System.out.println(" - X = STOP TIME");
        System.out.println(" - T = SHOW TEXT");
        System.out.println(" - E = SHOW ERROR");
        System.out.println(" - B = SHOW BOLD TEXT");
        System.out.println(" - I = SHOW ITALIC TEXT");
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

    public void render() {
        renderer.clear();
        hud.update();
        scene.update();

        renderer.render(this.id, this.camera, this.shaderProgram, scene);
        renderer.renderSkyBox(this.id, this.camera, this.skyboxShaderProgram, scene);
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

        if (KeyManager.getKeyJustPressed(GLFW_KEY_T)) {
            hud.showInformation("Normal Text", 2000);
        }
        
        if (KeyManager.getKeyJustPressed(GLFW_KEY_E)) {
            hud.showError("Error Text", 2000);
        }

        if (KeyManager.getKeyJustPressed(GLFW_KEY_B)) {
            hud.showInformation("Bold Text", 2000, Font.BOLD, 24);
        }

        if (KeyManager.getKeyJustPressed(GLFW_KEY_I)) {
            hud.showInformation("Italic Text", 2000, Font.ITALIC, 18);
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
        if (skyboxShaderProgram != null) {
            skyboxShaderProgram.cleanup();
        }
        if (hud != null) {
            hud.cleanUp();
        }
        scene.cleanUp();
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
