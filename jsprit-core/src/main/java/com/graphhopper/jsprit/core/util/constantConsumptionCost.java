package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.Location;

/**
 * returns a constant energy consumption value for each arc
 * @author Ayman
 */
public class constantConsumptionCost extends EnergyConsumptionCosts {

    private Locations locations;

    public constantConsumptionCost(Locations locations) {
        this.locations = locations;
    }

    double calculateConsumptionConstant(Location fromLocation, Location toLocation) {
        Coordinate from = null;
        Coordinate to = null;
        if (fromLocation.getCoordinate() != null && toLocation.getCoordinate() != null) {
            from = fromLocation.getCoordinate();
            to = toLocation.getCoordinate();
        } else if (locations != null) {
            from = locations.getCoord(fromLocation.getId());
            to = locations.getCoord(toLocation.getId());
        }
        return calculateConsumptionConstant(from, to);
    }
}

