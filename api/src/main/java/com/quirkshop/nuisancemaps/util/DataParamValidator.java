package com.quirkshop.nuisancemaps.util;

public class DataParamValidator {

    private static final double MILES_PER_DEGREE_LAT = 69.0;

    public static void validateBoundingBox(double lat1, double lng1, double lat2, double lng2, double maxArea) {
        if (lat1 < -90 || lat1 > 90 || lat2 < -90 || lat2 > 90) {
            throw new IllegalArgumentException("Latitude values must be between -90 and 90.");
        }
        if (lng1 < -180 || lng1 > 180 || lng2 < -180 || lng2 > 180) {
            throw new IllegalArgumentException("Longitude values must be between -180 and 180.");
        }
        if (lat1 >= lat2 || lng1 >= lng2) {
            throw new IllegalArgumentException("Invalid bounding box: lat1/lng1 must be SW corner; lat2/lng2 must be NE corner.");
        }

        // TODO [deferred: limit by approximate area
        //
        // idea is to avoid potentially expensive and unecessary large area queries
        //
        // but there isn't really a hard rule for area limiting
        // different devices / viewport sizes / browser zoom can yield drastically different
        // area calculations. Most are capped by MAX_LIMIT anyway.
        //
        // I think this might be a usability problem given two nearby cities, (I
        // guess results would be ordered by date, and not geographically distributed)
        // defer until encountered.

        /*
         * double avgLat = (lat1 + lat2) / 2.0;
         * double milesPerDegreeLng = MILES_PER_DEGREE_LAT *
         * Math.cos(Math.toRadians(avgLat));
         * double nsDistance = Math.abs(lat2 - lat1) * MILES_PER_DEGREE_LAT;
         * double ewDistance = Math.abs(lng2 - lng1) * milesPerDegreeLng;
         * double area = nsDistance * ewDistance;
         *
         *
         * if (area > maxArea) {
         * throw new IllegalArgumentException(
         * String.format("Bounding box area exceeded: %.2f > (maxArea %.2f).", area,
         * maxArea));
         * }
         */
    }
}
