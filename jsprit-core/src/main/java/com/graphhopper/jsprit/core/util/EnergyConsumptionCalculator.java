package com.graphhopper.jsprit.core.util;

public class EnergyConsumptionCalculator {

    public static double calculateConsumption(Coordinate coord1, Coordinate coord2) {
        double xDiff = coord1.getX() - coord2.getX();
        double yDiff = coord1.getY() - coord2.getY();
        return Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
    }
}
