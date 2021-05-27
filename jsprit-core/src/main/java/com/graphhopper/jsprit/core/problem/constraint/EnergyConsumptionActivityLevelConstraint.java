package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.StateUpdater;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.VehicleRoutingEnergyCostMatrix;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import java.util.Collection;
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

    private final Map<String, VehicleTypeImpl> vehicleTypes;
    // check : https://github.com/matsim-org/matsim-libs/blob/c8cb872afa98de1f72e87cb35b199db2ab0ddeca/contribs/freight/src/main/java/org/matsim/contrib/freight/carrier/CarrierVehicleTypes.java#L15
    private final VehicleRoutingEnergyCostMatrix energyCostMatrix;

    public EnergyConsumptionActivityLevelConstraint(Map<String, VehicleTypeImpl> vehicleTypes, RouteAndActivityStateGetter states, VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts, StateManager stateManager, VehicleRoutingTransportCostsMatrix costsMatrix, VehicleRoutingEnergyCostMatrix energyCostMatrix, double maxDistance, StateId distanceStateId) {
        this.vehicleTypes = vehicleTypes;
        this.states = states;
        this.routingCosts = routingCosts;
        this.activityCosts = activityCosts;
        this.stateManager = stateManager;
        this.costsMatrix = costsMatrix;
        this.energyCostMatrix = energyCostMatrix;
        this.maxDistance = maxDistance;
        this.distanceStateId = distanceStateId;
    }

    static class DistanceUpdater implements StateUpdater, ActivityVisitor {

        private final StateManager stateManager;

        private final VehicleRoutingTransportCostsMatrix costMatrix;
        private final VehicleRoutingEnergyCostMatrix energyCostMatrix;


        //        private final StateFactory.StateId distanceStateId;    //v1.3.1
        private final StateId distanceStateId; //head of development - upcoming release

        private VehicleRoute vehicleRoute;

        private double distance = 0.;

        private TourActivity prevAct;


        public DistanceUpdater(StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix costMatrix, VehicleRoutingEnergyCostMatrix energyCosts) {
            this.costMatrix = costMatrix;
            this.energyCostMatrix = energyCosts;
            this.stateManager = stateManager;
            this.distanceStateId = distanceStateId;
        }

        @Override
        public void begin(VehicleRoute vehicleRoute) {
            distance = 0.;
            prevAct = vehicleRoute.getStart();
            this.vehicleRoute = vehicleRoute;
        }

        @Override
        public void visit(TourActivity tourActivity) {
            distance += getConsumption(prevAct, tourActivity);
            prevAct = tourActivity;
        }

        @Override
        public void finish() {
            distance += getConsumption(prevAct, vehicleRoute.getEnd());
            //            stateManager.putTypedRouteState(vehicleRoute,distanceStateId,Double.class,distance); //v1.3.1
            stateManager.putRouteState(vehicleRoute, distanceStateId, distance); //head of development - upcoming release (v1.4)
        }

        double getDistance(TourActivity from, TourActivity to) {
            return costMatrix.getDistance(from.getLocation().getId(), to.getLocation().getId());
        }

        double getConsumption(TourActivity from, TourActivity to) {
            return energyCostMatrix.getConsumption(from.getLocation().getId(), to.getLocation().getId());
        }
    }

    private RouteAndActivityStateGetter states;

    private VehicleRoutingTransportCosts routingCosts;

    private VehicleRoutingActivityCosts activityCosts;


    private final StateManager stateManager;

    private final VehicleRoutingTransportCostsMatrix costsMatrix;

    private final double maxDistance;

    private final StateId distanceStateId;

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


    /**
     *
     * @param from
     * @param to
     * @param costsMatrix
     * @return Distance from point one to point 2
     */
    double getDistance(TourActivity from, TourActivity to, VehicleRoutingTransportCostsMatrix costsMatrix) {
        return costsMatrix.getDistance(from.getLocation().getId(), to.getLocation().getId());
    }

}
