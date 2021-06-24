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
        // this should be the getEnrgyCost and NOT Consumption
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

    @Override
    public double getEnergyConsumption(Location from, Location to) {
        return calculateConsumptionDefault(from.getCoordinate(), to.getCoordinate());

    }

    // TODO : Clear things between getting energy costs and getting energy consumption
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

    double calculateConsumptionDefault(Location fromLocation, Location toLocation) {
        return calculateConsumptionDefault(fromLocation.getCoordinate(), toLocation.getCoordinate());
    }


    double calculateConsumptionDefault(Coordinate from, Coordinate to) {
        try {
            return EnergyConsumptionCalculator.calculateConsumptionDefault(from, to) * detourFactor;
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

    @Override
    public double getBackwardEnergyCost(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
        return super.getBackwardEnergyCost(from, to, arrivalTime, driver, vehicle);
    }

    @Override
    public double getEnergyCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
        double consumption;
        try {
            consumption = calculateConsumptionDefault(from, to);
        } catch (NullPointerException e) {
            throw new NullPointerException("cannot calculate consumption. coordinates are missing. either add coordinates or use another transport-cost-calculator.");
        }
        double costs = consumption;
        if (vehicle != null) {
            if (vehicle.getType() != null) {
                costs = consumption * vehicle.getType().getVehicleCostParams().perEnergyUnit_Battery;
            }
        }
        return costs;
    }
}
