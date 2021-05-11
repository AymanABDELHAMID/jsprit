/*
 * Ayman Mahmoud - Example 1 - Simple VRP
 */

package com.graphhopper.jsprit.examples;

import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.StateUpdater;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.*;
import com.graphhopper.jsprit.io.problem.VrpXMLReader;


import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

/*
 * The following import is inspired from the one presented by the following article:
 * https://reader.elsevier.com/reader/sd/pii/S2352146521001022?token=6D4E21F130E2AD9715700E4D04C2C2BE6049A12DC1E8901D92E62CC88B1C94586CDEC7D523FE8E4039BE1B2F89EDFA65
 */

/**
 * @author Ayman (Not the correct approach)
 *
 *         !! No recharging or refueling is integrated !!
 *         Vehicles are considered totally full at the beginning.
 *
 *         Creates the distance constraint.
 */

public class VRP_Range_Constraint  {

// TODO:  the goal is to add the distance constraint here (then understand how)
// TODO: create an example with vehicle attribute call it battery
// TODO: try to print remaining battery percentage in the solution analyzer
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

// Reminder: There is a maximum distance constraint that we can use.
    static class RangeConstraint implements HardActivityConstraint {

        private final StateManager stateManager;

        private final VehicleRoutingTransportCostsMatrix costsMatrix;
        private final VehicleRoutingEnergyCostMatrix energyCostMatrix;

        private final double maxDistance;

        private final StateId distanceStateId;

        RangeConstraint(double maxDistance, StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix transportCosts, VehicleRoutingEnergyCostMatrix energyCostMatrix) { //get maximum distance from range of vehicle
            this.costsMatrix = transportCosts;
            this.maxDistance = maxDistance;
            this.stateManager = stateManager;
            this.distanceStateId = distanceStateId;
            this.energyCostMatrix = energyCostMatrix;
        }

        @Override
        public ConstraintsStatus fulfilled(JobInsertionContext context, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double v) {
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



