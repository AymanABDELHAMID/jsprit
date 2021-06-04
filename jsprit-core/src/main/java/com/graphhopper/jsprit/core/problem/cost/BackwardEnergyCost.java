package com.graphhopper.jsprit.core.problem.cost;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

public interface BackwardEnergyCost {
    public double getBackwardEnergyCost(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle);
}
