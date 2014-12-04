package me.yugy.github.folderlayout;

/**
 * Created by yugy on 14/12/2.
 */
public class MathUtils {

    public static int clamp(int value, int max, int min) {
        return Math.max(Math.min(value, min), max);
    }

    public static long clamp(long value, long max, long min) {
        return Math.max(Math.min(value, min), max);
    }

    public static float clamp(float value, float max, float min) {
        return Math.max(Math.min(value, min), max);
    }

    public static double clamp(double value, double max, double min) {
        return Math.max(Math.min(value, min), max);
    }
}
