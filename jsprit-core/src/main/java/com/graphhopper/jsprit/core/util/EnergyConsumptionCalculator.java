package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeKey;

public class EnergyConsumptionCalculator {

    /**
     * Calculates energy consumption based on vehicle type, load on vehicle crossing the arc and distance.
     * @param coord1
     * @param coord2
     * @param type
     * @param load
     * @return energy consumption needed
     */
    public static double calculateConsumption(Coordinate coord1, Coordinate coord2, VehicleType type, Double load) {
        // Construct vehicle instance
        // I think you can use calculatedistance directly
        //Vehicle v = new Vehicle(type)
        double xDiff = coord1.getX() - coord2.getX();
        double yDiff = coord1.getY() - coord2.getY();
        return Math.sqrt((xDiff * xDiff) + (yDiff * yDiff));
    }

    public static double calculateConsumptionConstant(Coordinate coord1, Coordinate coord2) {
        double constant_value = 10;
        return constant_value;
    }
}
