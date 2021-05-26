package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.BatteryAM;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;

/**
 * @author Ayman M.
 * A class to calculate recharging at recharging stations
 * Note that the consumption calculator allows for negative values, but recuperation in driving mode (route level)
 * is only possible if vehicle speed is defined as a decision variable, hence recuperation occurs when v < Vmin.
 */

public class EnergyRecuperationCalculator {

    /**
     *
     * @param type get the type of the vehicle to choose the corresponding recharge profile
     * @param soc the vehicle state of charge in KWh
     * @return
     */
    public static double calculateRecuperationDuration(VehicleType type, double soc) {
        return 0;
    }

    /**
     *
     * @param type get the type of the vehicle to choose the corresponding recharge profile
     * @param battery the vehicle's battery
     * @return
     */
    public static double calculateRecuperationDuration(VehicleType type, BatteryAM battery) {
        return 0;
    }

}
