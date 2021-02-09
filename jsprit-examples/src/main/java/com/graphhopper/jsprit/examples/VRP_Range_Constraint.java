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
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.EuclideanDistanceCalculator;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.jsprit.io.problem.VrpXMLReader;
import org.apache.logging.log4j.Logger;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;

import java.util.Collection;
import java.io.File;
// import the library that modifies vehicle type (BEV)

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

public class VRP_Range_Constraint  {

// TODO:  the goal is to add the distance constraint here (then understand how)
// TODO: create an example with vehicle attribute call it battery
// TODO: try to print remaining battery percentage in the solution analyzer
    static class DistanceUpdater implements StateUpdater, ActivityVisitor {

        private final StateManager stateManager;

        private final VehicleRoutingTransportCostsMatrix costMatrix;

        //        private final StateFactory.StateId distanceStateId;    //v1.3.1
        private final StateId distanceStateId; //head of development - upcoming release

        private VehicleRoute vehicleRoute;

        private double distance = 0.;

        private TourActivity prevAct;

        public DistanceUpdater(StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix transportCosts) {
            this.costMatrix = transportCosts;
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
            distance += getDistance(prevAct, tourActivity);
            prevAct = tourActivity;
        }

        @Override
        public void finish() {
            distance += getDistance(prevAct, vehicleRoute.getEnd());
    //            stateManager.putTypedRouteState(vehicleRoute,distanceStateId,Double.class,distance); //v1.3.1
            stateManager.putRouteState(vehicleRoute, distanceStateId, distance); //head of development - upcoming release (v1.4)
        }

        double getDistance(TourActivity from, TourActivity to) {
            return costMatrix.getDistance(from.getLocation().getId(), to.getLocation().getId());
        }
    }

// Reminder: There is a maximum distance constraint that we can use.
    static class RangeConstraint implements HardActivityConstraint {

        private final StateManager stateManager;

        private final VehicleRoutingTransportCostsMatrix costsMatrix;

        private final double maxDistance;

        private final StateId distanceStateId;

        RangeConstraint(double maxDistance, StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix transportCosts) { //get maximum distance from range of vehicle
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
    public static void main(String[] args) {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("C:\\Users\\AYMAN\\OneDrive - CentraleSupelec\\CentraleSupelec\\M2_MACLO\\Recherche\\Mémoire Thématique\\co"+
            "de\\jsprit\\jsprit-examples\\input\\pickups_and_deliveries_withBEV_withoutTWs_AM.xml");
        //builds a matrix based on euclidean distances; t_ij = euclidean(i,j) / 2; d_ij = euclidean(i,j)
        VehicleRoutingTransportCostsMatrix costMatrix = createMatrix(vrpBuilder);
        vrpBuilder.setRoutingCost(costMatrix);
        VehicleRoutingProblem vrp = vrpBuilder.build();


        StateManager stateManager = new StateManager(vrp); //head of development - upcoming release (v1.4)

        StateId distanceStateId = stateManager.createStateId("distance"); //head of development - upcoming release (v1.4)
        stateManager.addStateUpdater(new DistanceUpdater(distanceStateId, stateManager, costMatrix));

        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        /**
         * Adding maximum distance from maximum battery range
         */
        //vrp.getBatteryDimensions();
        final double MAX_Range = 500.;
        constraintManager.addConstraint(new RangeConstraint(MAX_Range, distanceStateId, stateManager, costMatrix), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager)
            .buildAlgorithm();
        vra.setMaxIterations(250);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);

        new Plotter(vrp, Solutions.bestOf(solutions)).plot("output/plot", "plot");
    }

    private static VehicleRoutingTransportCostsMatrix createMatrix(VehicleRoutingProblem.Builder vrpBuilder) {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        for (String from : vrpBuilder.getLocationMap().keySet()) {
            for (String to : vrpBuilder.getLocationMap().keySet()) {
                Coordinate fromCoord = vrpBuilder.getLocationMap().get(from);
                Coordinate toCoord = vrpBuilder.getLocationMap().get(to);
                double distance = EuclideanDistanceCalculator.calculateDistance(fromCoord, toCoord);
                matrixBuilder.addTransportDistance(from, to, distance);
                matrixBuilder.addTransportTime(from, to, (distance / 2.));
            }
        }
        return matrixBuilder.build();
    }

//TODO: Add solution analyzer, check example in VRPWithBackhaulsExample2
}

