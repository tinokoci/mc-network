package net.exemine.api.util;

import java.util.concurrent.ThreadLocalRandom;

public class MathUtil {

    public static boolean tryChance(float percentage) {
        return ThreadLocalRandom.current().nextFloat() <= percentage / 100;
    }

    public static int getIntBetween(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    public static int getClosest(int toCompare, int number1, int number2) {
        int closest = number1;
        int distanceToPositiveBorder = MathUtil.getDistance(toCompare, number1);
        int distanceToNegativeBorder = MathUtil.getDistance(toCompare, number2);

        if (distanceToNegativeBorder < distanceToPositiveBorder) {
            closest = number2;
        }
        return closest;
    }

    public static int getClosest(int toCompare, int radius) {
        return getClosest(toCompare, radius, -radius);
    }

    public static int getDistance(int number1, int number2) {
        return Math.abs(number2 - number1);
    }

    public static String getGeoDistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double distance = Math.sin(degreesToRadians(lat1)) * Math.sin(degreesToRadians(lat2)) + Math.cos(degreesToRadians(lat1)) * Math.cos(degreesToRadians(lat2)) * Math.cos(degreesToRadians(theta));

        distance = radiansToDegrees(Math.acos(distance)) * 60;
        distance = distance * 1.1515; // miles
        distance = milesToKilometers(distance);

        return String.format("%.2f", distance);
    }

    public static double milesToKilometers(double miles) {
        return miles * 1.609344;
    }

    public static double degreesToRadians(double deg) {
        return (deg * Math.PI / 180.0);
    }

    public static double radiansToDegrees(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public static byte getCompressedAngle(float value) {
        return (byte) (int) (value * 256.0F / 360.0F);
    }
}
