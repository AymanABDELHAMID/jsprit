package com.graphhopper.jsprit.core.problem.cost;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

public abstract class AbstractForwardVehicleEnergyTransportCost  implements  VehicleRoutingEnergyCosts {

    @Override
    public abstract double getEnergyConsumption(Location from, Location to, double departureTime, Vehicle vehicle);

    @Override
    public abstract double getDistance(Location from, Location to, double departureTime, Vehicle vehicle); // Not needed but keep it for now

    @Override
    public abstract double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle);

    @Override
    public double getBackwardTransportTime(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
        return getTransportTime(from, to, arrivalTime, driver, vehicle);
    }

}
