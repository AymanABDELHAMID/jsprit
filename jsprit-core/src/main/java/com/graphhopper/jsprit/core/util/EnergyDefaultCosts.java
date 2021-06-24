package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleEnergyTransportCost;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

public class EnergyDefaultCosts extends AbstractForwardVehicleEnergyTransportCost {
    public double speed = 1;

    private Locations locations;

    public EnergyDefaultCosts(Locations locations) {
        super();
        this.locations = locations;
    }

    public EnergyDefaultCosts() {
    }

    @Override
    public double getEnergyConsumption(Location from, Location to, Vehicle vehicle) {
        // TODO : use the vehicle parameter
        return EnergyConsumptionCalculator.calculateConsumptionDefault(from.getCoordinate(), to.getCoordinate());
    }

    @Override
    public double getEnergyConsumption(Location from, Location to) {
        // TODO : use the vehicle parameter
        return EnergyConsumptionCalculator.calculateConsumptionDefault(from.getCoordinate(), to.getCoordinate());
    }

    @Override
    public double getEnergyCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
        return EnergyConsumptionCalculator.calculateConsumptionDefault(from.getCoordinate(), to.getCoordinate());
    }
}
