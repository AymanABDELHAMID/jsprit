package com.graphhopper.jsprit.core.util;

/**
 * @author Ayman M.
 * A class to calculate recharging at recharging stations
 * Note that the consumption calculator allows for negative values, but recuperation in driving mode (route level)
 * is only possible if vehicle speed is defined as a decision variable, hence recuperation occurs when v < Vmin.
 */

public class EnergyRecuperationCalculator {
}
