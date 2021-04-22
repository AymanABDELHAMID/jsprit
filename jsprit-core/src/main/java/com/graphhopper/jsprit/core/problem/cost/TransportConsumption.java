package com.graphhopper.jsprit.core.problem.cost;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

/**
 * Interface for transport energy consumption
 * @author Ayman
 */
public interface TransportConsumption extends ForwardTransportCost, BackwardTransportCost {
    public double getEnergyConsumption(Location from, Location to, double departureTime, Vehicle vehicle);
}
