package fr.acth2.engine;

import de.matthiasmann.twl.utils.PNGDecoder;
import fr.acth2.engine.engine.Renderer;
import fr.acth2.engine.engine.ShaderProgram;
import fr.acth2.engine.engine.camera.Camera;
import fr.acth2.engine.engine.models.Item;
import fr.acth2.engine.engine.models.Mesh;
import fr.acth2.engine.inputs.KeyManager;
import fr.acth2.engine.inputs.MouseInput;
import fr.acth2.engine.utils.Refs;
import fr.acth2.engine.utils.loader.Loader;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static fr.acth2.engine.utils.Refs.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.*;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.system.MemoryUtil.*;

public class Main implements Runnable {
    public long id;
    public int textureId;
    public Camera camera;
    public MouseInput mouseInput;
    float[] positions = {
            // Front
            -0.5f,  0.5f,  0.5f,
            -0.5f, -0.5f,  0.5f,
            0.5f, -0.5f,  0.5f,
            0.5f,  0.5f,  0.5f,

            // Back
            0.5f,  0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f,  0.5f, -0.5f,

            // Left
            -0.5f,  0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f,  0.5f,
            -0.5f,  0.5f,  0.5f,

            // Right
            0.5f,  0.5f,  0.5f,
            0.5f, -0.5f,  0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f,  0.5f, -0.5f,

            // Top
            -0.5f,  0.5f, -0.5f,
            -0.5f,  0.5f,  0.5f,
            0.5f,  0.5f,  0.5f,
            0.5f,  0.5f, -0.5f,

            // Bottom
            -0.5f, -0.5f,  0.5f,
            -0.5f, -0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,
            0.5f, -0.5f,  0.5f
    };


    int[] indices = {
            0,1,2, 2,3,0,
            4,5,6, 6,7,4,
            8,9,10, 10,11,8,
            12,13,14, 14,15,12,
            16,17,18, 18,19,16,
            20,21,22, 22,23,20
    };

    float[] texCoords = {
            0,0, 0,1, 1,1, 1,0,
            0,0, 0,1, 1,1, 1,0,
            0,0, 0,1, 1,1, 1,0,
            0,0, 0,1, 1,1, 1,0,
            0,0, 0,1, 1,1, 1,0,
            0,0, 0,1, 1,1, 1,0
    };


    private static final float FOV = PROJECTION_FOV;
    private static final float Z_NEAR = PROJECTION_Z_NEAR;
    private static final float Z_FAR = PROJECTION_Z_FAR;
    private Matrix4f projectionMatrix;

    private static Thread loopThread;
    private static Main main;
    private static Renderer renderer;
    private static FloatBuffer verticesBuffer;
    public static ShaderProgram shaderProgram;
    private static Mesh mesh;
    private static Item item;

    public static int vaoId;
    private static int vboId;

    static float[] vertices = new float[]{
            0.0f,  0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f
    };

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

    public static void main(String[] args) throws Exception {
        Main.setInstance(new Main());
        Main.getInstance().start();
    }

    private void load() throws Exception {
        GLFWErrorCallback errorCallback;
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (glfwInit() != true) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        System.out.println("GLFW initialized");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, RESIZABLE ? GLFW_TRUE : GLFW_FALSE);

        Main.getInstance().id = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_TITLE, NULL, NULL);
        if ( getWindowID() == NULL ) throw new RuntimeException("Failed to create window");
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
        glfwShowWindow(getWindowID());
        glfwSwapBuffers(getWindowID());

        glfwSetInputMode(getWindowID(), GLFW_CURSOR, GRABBED_CURSOR ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);

        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Loader.loadResource("/shaders/vertex.glsl"));
        shaderProgram.createFragmentShader(Loader.loadResource("/shaders/fragment.glsl"));
        shaderProgram.link();

        renderer.init();
        this.mesh = new Mesh(positions, texCoords, indices);
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


        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
        verticesBuffer.put(vertices).flip();

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        memFree(verticesBuffer);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

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
                glfwSetWindowTitle(getWindowID(),  WINDOW_TITLE + " | FPS: " + frames);
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
        tempAmount = (float)Math.sin(temp);

        float rotation = item.getRotation().x + 1.5f;
        if ( rotation > 360 ) {
            rotation = 0;
        }
        //item.setRotation(0.0F, 0.0F, 35.5F);
        item.setPosition(tempAmount, item.getPosition().y, -1F );

        renderer.render(item);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

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
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }

        glDisableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);

        if (verticesBuffer != null) {
            MemoryUtil.memFree(verticesBuffer);
        }
    }

    @Override
    public void run() {
        try {
            load();
            loop();
            cleanUp();
        } catch (Exception excp) {
            excp.printStackTrace();
        }
    }
}