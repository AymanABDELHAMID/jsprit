package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.StateUpdater;
import com.graphhopper.jsprit.core.algorithm.state.VehicleDependentStateOfCharge;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.TransportConsumption;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.VehicleRoutingEnergyCostMatrix;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ayman M.
 * Checkout : https://github.com/matsim-org/matsim-libs/tree/master/contribs/freight/src/main/java/org/matsim/contrib/freight/jsprit
 *
 * This is an implementation of the methods for the distance constraint.
 * The base for calculating the consumption is the energyConsumptionCalculator
 * // TODO: in vrp create an enum that chooses which consumption model is used
 *
 * Recharging is considered.
 */


public class EnergyConsumptionActivityLevelConstraint implements HardActivityConstraint {

    private RouteAndActivityStateGetter stateManager; // TODO: Make sure you don't need the actual state manager

    //private StateId stateOfChargeId;

    private TransportConsumption consumptionCalculator;

    private Double[] maxConsumptions;

    private VehicleRoutingProblem vrp;

    /**
     * This replaces the DistanceUpdater
     */
    //VehicleDependentStateOfCharge stateOfCharge =
     //   new VehicleDependentStateOfCharge(vrp.getEnergyConsumption(), stateManager, stateOfChargeId, vrp.getVehicles());

    public EnergyConsumptionActivityLevelConstraint(RouteAndActivityStateGetter stateManager, VehicleRoutingProblem vrp) { // Why did we use RouteAndActivityStateGetter??
        this.stateManager = stateManager;
        this.consumptionCalculator = vrp.getEnergyConsumption();
        Map<Vehicle, Double> consumptionMap = new HashMap<>();
        makeArray(consumptionMap);
        this.vrp = vrp; // in testing I found out this is null..
    }

    private void makeArray(Map<Vehicle, Double> maxConsumptions) {
        int maxIndex = getMaxIndex(maxConsumptions.keySet());
        this.maxConsumptions = new Double[maxIndex + 1];
        for (Vehicle v :  vrp.getVehicles()) {
            this.maxConsumptions[v.getIndex()] = v.getType().getBatteryDimensions().getRange(0);} // Assuming the vehicle has one battery for now
            //this.maxConsumptions[v.getIndex()] = maxConsumptions.get(v);
        // TODO: change to get state of charge in the updater. This means the range should be updated
    }

    private int getMaxIndex(Collection<Vehicle> vehicles) {
        int index = 0;
        for (Vehicle v : vehicles) {
            if (v.getIndex() > index) index = v.getIndex();
        }
        return index;
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        return null;
    }

        /*
    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext context, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double departureTime) {
        double additionalDistance = getConsumption(prevAct, newAct) + getConsumption(newAct, nextAct) - getConsumption(prevAct, nextAct);
        Double routeDistance = stateManager.getRouteState(context.getRoute(), distanceStateId, Double.class);

        if (routeDistance == null) routeDistance = 0.;
        double newRouteDistance = routeDistance + additionalDistance;
        if (newRouteDistance > maxDistance) {
            return ConstraintsStatus.NOT_FULFILLED;
        } else return ConstraintsStatus.FULFILLED;
    }

    double getDistance(TourActivity from, TourActivity to) {
        return costsMatrix.getDistance(from.getLocation().getId(), to.getLocation().getId());
    }

    double getConsumption(TourActivity from, TourActivity to) {
        return energyCostMatrix.getConsumption(from.getLocation().getId(), to.getLocation().getId());
    }
    /*

    /**
     *
     * @param from
     * @param to
     * @param costsMatrix
     * @return Distance from point one to point 2
     */
        /*
    double getDistance(TourActivity from, TourActivity to, VehicleRoutingTransportCostsMatrix costsMatrix) {
        return costsMatrix.getDistance(from.getLocation().getId(), to.getLocation().getId());
    }
    */

}
