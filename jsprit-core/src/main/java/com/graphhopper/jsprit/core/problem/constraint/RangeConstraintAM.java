package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import java.util.Collection;
import java.util.Map;

/*
 * The following import is inspired from the one presented by the following article:
 * https://reader.elsevier.com/reader/sd/pii/S2352146521001022?token=6D4E21F130E2AD9715700E4D04C2C2BE6049A12DC1E8901D92E62CC88B1C94586CDEC7D523FE8E4039BE1B2F89EDFA65
 */

/**
 * @author Ayman
 *
 *         !! No recharging or refueling is integrated !!
 *         Vehicles are considered totally full at the beginning.
 *
 *         Creates the distance constraint.
 */

public class RangeConstraintAM {


    static class DistanceConstraint implements HardActivityConstraint {

        private final StateManager stateManager;

        private final VehicleRoutingTransportCostsMatrix costsMatrix;

        private final double maxDistance;

        //        private final StateFactory.StateId distanceStateId; //v1.3.1
        private final StateId distanceStateId; //head of development - upcoming release (v1.4)

        //        DistanceConstraint(double maxDistance, StateFactory.StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix costsMatrix) { //v1.3.1
        DistanceConstraint(double maxDistance, StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix transportCosts) { //head of development - upcoming release (v1.4)
            this.costsMatrix = transportCosts;
            this.maxDistance = maxDistance;
            this.stateManager = stateManager;
            this.distanceStateId = distanceStateId;
        }

        @Override
        public ConstraintsStatus fulfilled(JobInsertionContext context, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double v) {
            double additionalDistance = getDistance(prevAct, newAct) + getDistance(newAct, nextAct) - getDistance(prevAct, nextAct);
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

    }
}
