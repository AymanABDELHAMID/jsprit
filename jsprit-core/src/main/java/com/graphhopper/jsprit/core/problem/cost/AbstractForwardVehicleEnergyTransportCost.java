package com.graphhopper.jsprit.core.problem.cost;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

public abstract class AbstractForwardVehicleEnergyTransportCost  implements  VehicleRoutingEnergyCosts {

    @Override
    public abstract double getEnergyConsumption(Location from, Location to, Vehicle vehicle);

}
