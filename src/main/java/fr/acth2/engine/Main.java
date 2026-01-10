package fr.acth2.engine;

import de.matthiasmann.twl.utils.PNGDecoder;
import fr.acth2.engine.engine.Renderer;
import fr.acth2.engine.engine.ShaderProgram;
import fr.acth2.engine.engine.camera.Camera;
import fr.acth2.engine.engine.models.Item;
import fr.acth2.engine.engine.models.Mesh;
import fr.acth2.engine.inputs.KeyManager;
import fr.acth2.engine.inputs.MouseInput;
import fr.acth2.engine.utils.loader.Loader;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static fr.acth2.engine.utils.Refs.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main implements Runnable {
    public long id;
    public int textureId;
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
    private static Item item;

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

        glfwSetInputMode(getWindowID(), GLFW_CURSOR, GRABBED_CURSOR ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);

        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Loader.loadResource("/shaders/vertex.glsl"));
        shaderProgram.createFragmentShader(Loader.loadResource("/shaders/fragment.glsl"));
        shaderProgram.link();

        renderer.init();
        Mesh mesh = Loader.loadMesh("/models/test.obj");
        this.item = new Item(mesh);

        PNGDecoder decoder = renderer.loadTexture("v2.png");
        ByteBuffer buf = ByteBuffer.allocateDirect(
                4 * decoder.getWidth() * decoder.getHeight());
        try {
            decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
        } catch (IOException e) {
            System.err.println("ERROR during decoding of the texture: w" + decoder.getWidth() + ", h" + decoder.getHeight());
            e.printStackTrace();
            System.exit(1);
        }
        buf.flip();
        int textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(),
                decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
        glGenerateMipmap(GL_TEXTURE_2D);
        this.textureId = textureId;

        System.out.println("GLFW Window ID: " + getWindowID());
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
    float tempAmount = 0.0f;
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

        temp += deltaTime;
        tempAmount = (float) Math.sin(temp);

        float rotation = item.getRotation().x + 1.5f;
        if (rotation > 360) {
            rotation = 0;
        }
        //item.setPosition(tempAmount, item.getPosition().y, -2F);
        item.setScale(12);
        item.getMesh().setTextured(false);

        renderer.render(item);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        shaderProgram.unbind();
        glfwSwapBuffers(getWindowID());
        glfwPollEvents();
    }

    public void inputs(long window, MouseInput mouseInput) {
        KeyManager.update();
        mouseInput.input(window);

        if (KeyManager.getKeyPress(GLFW_KEY_W)) {
            camera.movePosition(0.0F, 0.0F, -0.05F);
        }

        if (KeyManager.getKeyPress(GLFW_KEY_A)) {
            camera.movePosition(-0.05F, 0.0F, 0.0F);
        }

        if (KeyManager.getKeyPress(GLFW_KEY_S)) {
            camera.movePosition(0.0F, 0.0F, 0.05F);
        }

        if (KeyManager.getKeyPress(GLFW_KEY_D)) {
            camera.movePosition(0.05F, 0.0F, 0.0F);
        }

        if (KeyManager.getKeyPress(GLFW_KEY_LEFT_SHIFT)) {
            camera.movePosition(0.0F, -0.05F, 0.0F);
        }

        if (KeyManager.getKeyPress(GLFW_KEY_SPACE)) {
            camera.movePosition(0.0F, 0.05F, 0.0F);
        }

        if (KeyManager.getKeyJustPressed(GLFW_KEY_ESCAPE)) {
            System.exit(0);
        }

        Vector2f rotVec = mouseInput.getDisplVec();
        camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
    }

    public void cleanUp() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
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
