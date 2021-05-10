package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeKey;

public class EnergyConsumptionCalculator {

    /**
     * Calculates energy consumption based on vehicle type, load on vehicle crossing the arc and distance.
     * @param coord1
     * @param coord2
     * @param type
     * @param load
     * @return energy consumption needed
     */
    public static double calculateConsumption(Coordinate coord1, Coordinate coord2, VehicleType type, Double load) {
        double mass = type.getProfile().getWeight() + load;
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
        double P2 = (0.18/type.getProfile().getNg())*P1;
        // the factor is divided by the Gain factor to make it ~ 0 in the case of non electric vehicles
        double P = P1 + P2;
        double E = P*travelTime;
        return E;
    }

    /**
     * calculates energy consumption based on distance only, assuming an average energy cost of 0.25kW/km
     * @param coord1
     * @param coord2
     * @return
     */
    public static double calculateConsumptionConstant(Coordinate coord1, Coordinate coord2) {
        double value = 0.25*EuclideanDistanceCalculator.calculateDistance(coord1, coord2);
        return value;
    }
}
