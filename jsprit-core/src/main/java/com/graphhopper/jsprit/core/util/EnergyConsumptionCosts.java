package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleEnergyTransportCost;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.EuclideanCosts;

public class EnergyConsumptionCosts extends AbstractForwardVehicleEnergyTransportCost {

    public int speed = 1;

    public double detourFactor = 1.0;

    @Override
    public String toString() {
        return "[name=constantConsumptionCost]";
    }

    @Override
    public double getEnergyConsumption(Location from, Location to, double departureTime, Vehicle vehicle){
        double energy = calculateConsumption(from, to, vehicle.getType(), from.getLoad());
        if (vehicle != null && vehicle.getType() != null) {
            return energy * vehicle.getType().getVehicleCostParams().perDistanceUnit; // TODO: change to perEnergyUnit
        }
        return energy;
    }

    double calculateConsumption(Location fromLocation, Location toLocation, Vehicle vehicle) {
        return calculateConsumption(fromLocation.getCoordinate(), toLocation.getCoordinate(), vehicle.getType());
    }

    double calculateConsumption(Coordinate from, Coordinate to, String type) {
        try {
            return EnergyConsumptionCalculator.calculateConsumption(from, to, type, load) * detourFactor;
        } catch (NullPointerException e) {
            throw new NullPointerException("Coordinates are Missing");
            // TODO: vehicle.getType().getVehicledDescription.frontal_area
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
