package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleEnergyTransportCost;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

public class EnergyConsumptionCosts extends AbstractForwardVehicleEnergyTransportCost {

    @Override
    public double getEnergyConsumption(Location from, Location to, double departureTime, Vehicle vehicle){
        double energy = calculateConsumption(from, to);
        if (vehicle != null && vehicle.getType() != null) {
            return energy * vehicle.getType().getVehicleCostParams().perDistanceUnit; // TODO: change to perEnergyUnit
        }
        return energy;
    }

    double calculateConsumption(Coordinate from, Coordinate to) {
        try {
            return EnergyConsumptionCalculator.calculateConsumption(from, to) * detourFactor;
        } catch (NullPointerException e) {
            throw new NullPointerException("Coordinates are Missing");
            // TODO: vehicle.getType().getVehicledDescription.frontal_area
            // TODO: Driver.getType().getDriverDescription.aux_power_coefficient
        }
    }
}
