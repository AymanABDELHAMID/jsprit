package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeKey;

public class EnergyConsumptionCalculator {


    /**
     * Calculates energy consumption based on vehicle type, load on vehicle crossing the arc and distance.
     * Asamer et al. 2016 (LDM)
     * @param coord1
     * @param coord2
     * @param type
     * @param load
     * @return energy consumption needed
     */
    public static double calculateConsumption(Coordinate coord1, Coordinate coord2, VehicleType type, Double load) {
        double mass = type.getProfile().getWeight() + load; // TODO: reverifier les ordres des job
        double slope = 0; // TODO: road grade is considered flat for now
        double g = 9.81;
        double rho = 1.055;
        double grade = mass*g*Math.sin(slope);
        double rolling = mass*g*Math.cos(slope)*type.getProfile().getCrr();
        double air = 0.5*rho*type.getProfile().getCw()*type.getProfile().getFrontalArea()*Math.pow(type.getProfile().getAvgSpeed(), 2.);
        double Fr = grade + rolling + air;
        // acceleration is considered to be null
        double d = EuclideanDistanceCalculator.calculateDistance(coord1, coord2);
        double travelTime = d/type.getProfile().getAvgSpeed();
        // p_el out = P+Po // TODO: calculate auxiliary power based on the driver profile
        // for now the auxiliary power is assumed to be an additional 20% of energy consumption in the case of electric vehicles
        double P1 = (Fr*type.getProfile().getAvgSpeed())/type.getProfile().getNm();
        double P2 = (0.18/type.getProfile().getNg())*P1; // float MAX VALUE
        // the factor is divided by the Gain factor to make it ~ 0 in the case of non electric vehicles
        double P = P1 + P2;
        double E = P*travelTime;
        return E;
    }

    public static double calculateConsumptionGoekeSchneider(Coordinate coord1, Coordinate coord2, VehicleType type, Double load){
        return load;
    }

    public static double calculateConsumptionCatrina(Coordinate coord1, Coordinate coord2, VehicleType type, Double load){
        return load;
    }

    public static double calculateConsumptionHiermann(Coordinate coord1, Coordinate coord2, VehicleType type, Double load){
        return load;
    }

    public static double calculateConsumptionMontoya(Coordinate coord1, Coordinate coord2, VehicleType type, Double load){
        return load;
    }


    /**
     * calculates energy consumption based on distance only, assuming an average energy cost of 0.25kW/km
     * // https://enveurope.springeropen.com/articles/10.1186/s12302-020-00307-8
     * @param coord1
     * @param coord2
     * @return
     */
    public static double calculateConsumptionConstant(Coordinate coord1, Coordinate coord2) {
        double value = 0.25*1000*EuclideanDistanceCalculator.calculateDistance(coord1, coord2);
        return value;
    }

    /**
     * calculates energy consumption based on default vehicle profile values only,
     * @param coord1
     * @param coord2
     * @return
     */
    public static double calculateConsumptionDefault(Coordinate coord1, Coordinate coord2) {
        /*
         * Building a vehicle with default profile
         * TODO: change fixed values with profile default values
         */
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleType type = vehicle.getType();
        double mass = type.getProfile().getWeight();
        double slope = 0;
        double g = 9.81;
        double rho = 1.055;
        double grade = mass*g*Math.sin(slope);
        double rolling = mass*g*Math.cos(slope)*0.0105;
        double air = 0.5*rho*0.33*1.81*Math.pow(13, 2.);
        double Fr = grade + rolling + air;
        // acceleration is considered to be null
        double d = EuclideanDistanceCalculator.calculateDistance(coord1, coord2);
        double travelTime = d/13;
        // p_el out = P+Po // TODO: calculate auxiliary power based on the driver profile
        // for now the auxiliary power is assumed to be an additional 20% of energy consumption in the case of electric vehicles
        double P1 = (Fr*13)/0.85;
        double P2 = (0.18/0.64)*P1; // float MAX VALUE
        // the factor is divided by the Gain factor to make it ~ 0 in the case of non electric vehicles
        double P = P1 + P2;
        double E = P*travelTime;
        return E;
    }
}
