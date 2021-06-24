package com.graphhopper.jsprit.core.problem.cost;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

public abstract class AbstractForwardVehicleEnergyTransportCost  implements  VehicleRoutingEnergyCosts {

    @Override
    public abstract double getEnergyConsumption(Location from, Location to, Vehicle vehicle);

    @Override
    public abstract double getEnergyConsumption(Location from, Location to);

    @Override
    public double getBackwardEnergyCost(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
        return getEnergyCost(from, to, arrivalTime, driver, vehicle);
    }

    @Override
    public abstract double getEnergyCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle);
}
