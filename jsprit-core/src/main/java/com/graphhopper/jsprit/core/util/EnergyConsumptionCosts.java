package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleEnergyTransportCost;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.util.EuclideanCosts;

public class EnergyConsumptionCosts extends AbstractForwardVehicleEnergyTransportCost {

    public int speed = 1;

    public double detourFactor = 1.0;

    @Override
    public String toString() {
        return "[name=constantConsumptionCost]";
    }

    @Override
    public double getEnergyConsumption(Location from, Location to, Vehicle vehicle){
        double energy = calculateConsumption(from, to, vehicle.getType(), from.getLoad());
        if (vehicle != null && vehicle.getType() != null) { // TODO: add energy_type
            if (vehicle.getType().getEnergyType() == 1) {
                return energy * vehicle.getType().getVehicleCostParams().perEnergyUnit_Fuel;}
            else if (vehicle.getType().getEnergyType() == 2) {
                return energy * vehicle.getType().getVehicleCostParams().perEnergyUnit_Battery;
            }
            else { return energy * vehicle.getType().getVehicleCostParams().perEnergyUnit_Fuel;
            // TODO: deal with hybrid setting
            }
        }
        return energy;
    }

    double calculateConsumption(Location from, Location to, VehicleType type, double load) {
        return calculateConsumption(from.getCoordinate(), to.getCoordinate(), type, load);
    }

    double calculateConsumption(Coordinate coordinate, Coordinate coordinate1, VehicleType type, double load) {
        try {
            return EnergyConsumptionCalculator.calculateConsumption(coordinate, coordinate1, type, load) * detourFactor;
        } catch (NullPointerException e) {
            throw new NullPointerException("Coordinates are Missing");
            // TODO: vehicle.getProfile().getVehicleDescription.frontal_area
            // TODO: Driver.getType().getDriverDescription.aux_power_coefficient
        }
    }


    double calculateConsumptionConstant(Location fromLocation, Location toLocation) {
        return calculateConsumptionConstant(fromLocation.getCoordinate(), toLocation.getCoordinate());
    }


    double calculateConsumptionConstant(Coordinate from, Coordinate to) {
        try {
            return EnergyConsumptionCalculator.calculateConsumptionConstant(from, to) * detourFactor;
        } catch (NullPointerException e) {
            throw new NullPointerException("Coordinates are Missing");
        }
    }

    double calculateDistance(Location fromLocation, Location toLocation) {
        return calculateDistance(fromLocation.getCoordinate(), toLocation.getCoordinate());
    }

    double calculateDistance(Coordinate from, Coordinate to) {
        try {
            return EuclideanDistanceCalculator.calculateDistance(from, to) * detourFactor;
        } catch (NullPointerException e) {
            throw new NullPointerException("cannot calculate euclidean distance. coordinates are missing. either add coordinates or use another transport-cost-calculator.");
        }
    }
}
