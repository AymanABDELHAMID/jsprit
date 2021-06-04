package com.graphhopper.jsprit.core.problem.cost;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

public interface ForwardEnergyCost {
    public double getEnergyCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle);
}
