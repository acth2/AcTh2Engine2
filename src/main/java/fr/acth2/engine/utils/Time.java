package fr.acth2.engine.utils;

public class Time {

    private static boolean isTimeStopped = false;

    /**
     * Toggles the state of time between stopped and running.
     */
    public static void stopTime() {
        isTimeStopped = !isTimeStopped;
    }

    /**
     * Checks if the time is currently stopped.
     * @return true if time is stopped, false otherwise.
     */
    public static boolean isTimeStopped() {
        return isTimeStopped;
    }
}
