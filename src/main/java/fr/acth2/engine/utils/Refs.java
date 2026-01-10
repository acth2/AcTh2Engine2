package fr.acth2.engine.utils;

import fr.acth2.engine.Main;

public class Refs {
    public static String WINDOW_TITLE = "Test Engine";

    public static boolean RESIZABLE = true;
    public static boolean GRABBED_CURSOR = false;
    public static boolean DEBUG_BACKGROUND = false;

    public static int WINDOW_WIDTH = 1280;
    public static int WINDOW_HEIGHT = 720;

    public static final float PROJECTION_FOV = (float) Math.toRadians(120.0F);
    public static final float PROJECTION_Z_NEAR = 0.01f;
    public static final float PROJECTION_Z_FAR = 10000.f;

    public static final float MOUSE_SENSITIVITY = 1F;


    public static long getWindowID() {
        return Main.getInstance().id;
    }

    public static double getTime() {
        return System.currentTimeMillis();
    }
}
