package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingEnergyCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CostFactoryTest extends CostFactory {
    @Test
    public void whenCalculatingConstantEnergyConsumption_itShouldReturnConstant_EnergyValue() {
        VehicleRoutingEnergyCosts EnergyCost = CostFactory.createConstantEnergyCosts();
        // Locations has no field
        // TODO: Need to understand locations and create a more assertive test
        // Locations (1,2), (4,5)..
        // CostFactory(Locations) --> constant energy costs
        // assertion == 10?
        // public abstract double getEnergyConsumption(Location from, Location to, double departureTime, Vehicle vehicle);
        // EnergyCost.getEnergyConsumption();
    }

}
