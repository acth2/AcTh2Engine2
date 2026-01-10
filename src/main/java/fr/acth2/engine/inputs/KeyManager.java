package fr.acth2.engine.inputs;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static fr.acth2.engine.utils.Refs.*;

public class KeyManager {
    private static List<Integer> pressedKeysList = new ArrayList<>();
    private static List<Integer> releasedKeysList = new ArrayList<>();
    private static List<Integer> justReleasedKeysList = new ArrayList<>();
    private static List<Integer> justPressedKeysList = new ArrayList<>();

    public static void update() {
        justPressedKeysList.clear();
        justReleasedKeysList.clear();

        for (int i = pressedKeysList.size() - 1; i >= 0; i--) {
            int key = pressedKeysList.get(i);
            if (!glfwKeyPress(key)) {

                pressedKeysList.remove(i);
                releasedKeysList.add(key);
                justReleasedKeysList.add(key);
            }
        }

        checkKey(GLFW_KEY_W);
        checkKey(GLFW_KEY_A);
        checkKey(GLFW_KEY_S);
        checkKey(GLFW_KEY_D);

        checkKey(GLFW_KEY_SPACE);
        checkKey(GLFW_KEY_LEFT_SHIFT);

        checkKey(GLFW_KEY_X);
        checkKey(GLFW_KEY_ESCAPE);
        checkKey(GLFW_KEY_TAB);
        checkKey(GLFW_KEY_LEFT_CONTROL);
    }

    private static void checkKey(int keyCode) {
        boolean isPressed = glfwKeyPress(keyCode);
        boolean wasPressed = pressedKeysList.contains(keyCode);

        if (isPressed && !wasPressed) {
            pressedKeysList.add(keyCode);
            justPressedKeysList.add(keyCode);
        } else if (!isPressed && wasPressed) {
            justPressedKeysList.remove(keyCode);
            justReleasedKeysList.add(keyCode);
        }
    }

    public static boolean getKeyPress(int keyCode) {
        return pressedKeysList.contains(keyCode);
    }

    public static boolean getKeyJustPressed(int keyCode) {
        return justPressedKeysList.contains(keyCode);
    }

    public static boolean getKeyJustReleased(int keyCode) {
        return justReleasedKeysList.contains(keyCode);
    }

    private static boolean glfwKeyPress(int keyCode) {
        return glfwGetKey(getWindowID(), keyCode) == GLFW_PRESS;
    }
}