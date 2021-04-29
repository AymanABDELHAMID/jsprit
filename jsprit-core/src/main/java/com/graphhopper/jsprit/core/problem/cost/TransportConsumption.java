package com.graphhopper.jsprit.core.problem.cost;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.driver.Driver;

/**
 * Interface for transport energy consumption
 * @author Ayman M.
 */
public interface TransportConsumption extends ForwardEnergyCost, BackwardEnergyCost {

   public double getEnergyConsumption(Location from, Location to, Vehicle vehicle);
}
