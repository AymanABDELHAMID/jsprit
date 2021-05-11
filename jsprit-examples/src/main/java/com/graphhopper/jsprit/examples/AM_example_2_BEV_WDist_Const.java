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
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.*;
import com.graphhopper.jsprit.io.problem.VehicleParameterReader;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AM_example_2_BEV_WDist_Const {

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

    public static void main(String[] args) {
        /*
         * some preparation - create output folder
         */
        File dir = new File("output");
        // if the directory does not exist, create it
        if (!dir.exists()) {
            System.out.println("creating directory ./output");
            boolean result = dir.mkdir();
            if (result) System.out.println("./output created");
        }
        // .addCapacityDimension(dimensionIndex,dimensionValue)
        final int WEIGHT_INDEX = 0;
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("BEV").addCapacityDimension(WEIGHT_INDEX, 2);

        // .addBatteryDimension(dimensionIndex,range)
        final int BATTERY_INDEX = 0;
        vehicleTypeBuilder.addBatteryDimension(BATTERY_INDEX, 3000); // Ayman: tested with small range and found no solution

        /**
         * setting the profile
         */
        vehicleTypeBuilder.setProfile("carBe");
        // building the profile
        VehicleProfiles.Builder vpBuilder = VehicleProfiles.Builder.newInstance();
        String path = "vehicleParameters.xml"; // what if the xml file has a different name...
        new VehicleParameterReader(vpBuilder).read(path);
        VehicleProfiles vehicleProfiles = vpBuilder.build();
        List<VehicleProfile> profiles = vehicleProfiles.getVehicleProfiles();
        Map<String, VehicleProfile> profilesMap = vehicleProfiles.getVehicleProfilesMap();
        VehicleProfile profile = profilesMap.get("carBe"); // can be replaced by getProfile_name.
        vehicleTypeBuilder.buildProfile(profile);

        // Building the Vehicle
        VehicleType vehicleType = vehicleTypeBuilder.build();

        /*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "BEV"
         */
        Builder vehicleBuilder = Builder.newInstance("vehicle1");
        vehicleBuilder.setStartLocation(Location.newInstance(10, 10));
        vehicleBuilder.setType(vehicleType);
        VehicleImpl vehicle = vehicleBuilder.build();

        /*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "BEV"
         */
        Builder vehicleBuilder2 = Builder.newInstance("vehicle2");
        vehicleBuilder2.setStartLocation(Location.newInstance(10, 10));
        vehicleBuilder2.setType(vehicleType);
        VehicleImpl vehicle2 = vehicleBuilder2.build();

        /*
         * build services with id 1...4 at the required locations, each with a capacity-demand of 1.
         * Note, that the builder allows chaining which makes building quite handy
         */
        //Service service1 = Service.Builder.newInstance("Jakob").addSizeDimension(WEIGHT_INDEX,1).setLocation(Location.newInstance(0, 7)).build();
        Service service1 = Service.Builder.newInstance("Jakob").addSizeDimension(WEIGHT_INDEX,1).
            setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 7)).setLoad(15).build()).build();
        Service service2 = Service.Builder.newInstance("Ayman").addSizeDimension(WEIGHT_INDEX,1).setLocation(Location.newInstance(5, 13)).build();
        Service service3 = Service.Builder.newInstance("Sebastian").addSizeDimension(WEIGHT_INDEX,1).setLocation(Location.newInstance(10, 7)).build();
        Service service4 = Service.Builder.newInstance("Tarek").addSizeDimension(WEIGHT_INDEX,1).setLocation(Location.newInstance(15, 13)).build();

        /*
         * again define a builder to build the VehicleRoutingProblem
         */
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle).addVehicle(vehicle2);
        vrpBuilder.addJob(service1).addJob(service2).addJob(service3).addJob(service4);

        /*
         * build the problem
         * by default, the problem is specified such that FleetSize is INFINITE, i.e. an infinite number of
         * the defined vehicles can be used to solve the problem
         * by default, transport costs are computed as Euclidean distances
         */
        VehicleRoutingTransportCostsMatrix costMatrix = createMatrix(vrpBuilder);
        /**
         * @author: Ayman
         * 10.05.21
         * testing the energy cost matrix, version one: only one vehicle type (homogenous fleet)
         */

        VehicleType type = vehicle.getType();
        VehicleRoutingEnergyCostMatrix energyCostMatrix = createEnergyMatrix(vrpBuilder, type);
        vrpBuilder.setRoutingCost(costMatrix);
        vrpBuilder.setEnergyCost(energyCostMatrix);

        VehicleRoutingProblem problem = vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();
        // vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        /*
         * get the algorithm out-of-the-box.
         */
        // vehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);

        /*
         * Adding the distance constraint
         */

        StateManager stateManager = new StateManager(problem); //head of development - upcoming release (v1.4)

        StateId distanceStateId = stateManager.createStateId("distance"); //head of development - upcoming release (v1.4)
        stateManager.addStateUpdater(new VRP_Range_Constraint.DistanceUpdater(distanceStateId, stateManager, costMatrix, energyCostMatrix));

        ConstraintManager constraintManager = new ConstraintManager(problem, stateManager);

        final double MAX_Range = vehicle.getType().getBatteryDimensions().getRange(0); // TODO: if we have more than one range we can try to include a list of ranges into the constraint.

        constraintManager.addConstraint(new VRP_Range_Constraint.RangeConstraint(MAX_Range, distanceStateId, stateManager, costMatrix, energyCostMatrix), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(problem).setStateAndConstraintManager(stateManager,constraintManager)
            .buildAlgorithm();
        algorithm.setMaxIterations(250);

        /*
         * and search a solution which returns a collection of solutions (here only one solution is constructed)
         */
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        /*
         * use the static helper-method in the utility class Solutions to get the best solution (in terms of least costs)
         */
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);


        // new VrpXMLWriter(problem, solutions).write("output/problem-with-solution.xml");

        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);
        //SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.CONCISE);


        /*
         * plot
         */
        new Plotter(problem,bestSolution).plot("output/plot_simple_BEV_with_range_const.png","simple example with distance constraint");

        /*
        render problem and solution with GraphStream
         */
        //new GraphStreamViewer(problem, bestSolution).labelWith(Label.ID).setRenderDelay(200).display();
    }

    private static VehicleRoutingTransportCostsMatrix createMatrix(VehicleRoutingProblem.Builder vrpBuilder) {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        for (String from : vrpBuilder.getLocationMap().keySet()) {
            for (String to : vrpBuilder.getLocationMap().keySet()) {
                Coordinate fromCoord = vrpBuilder.getLocationMap().get(from);
                Coordinate toCoord = vrpBuilder.getLocationMap().get(to);
                double distance = EuclideanDistanceCalculator.calculateDistance(fromCoord, toCoord);
                matrixBuilder.addTransportDistance(from, to, distance); // what is the distance unit
                matrixBuilder.addTransportTime(from, to, (distance / 2.));
            }
        }
        return matrixBuilder.build();
    }

    /**
     * Constructing an energy cost matrix
     * @param vrpBuilder
     * @param type
     * @return
     */

    private static VehicleRoutingEnergyCostMatrix createEnergyMatrix(VehicleRoutingProblem.Builder vrpBuilder, VehicleType type) {
        VehicleRoutingEnergyCostMatrix.Builder matrixBuilder = VehicleRoutingEnergyCostMatrix.Builder.newInstance(true);
        for (String from : vrpBuilder.getLocationMap().keySet()) {
            for (String to : vrpBuilder.getLocationMap().keySet()) {
                Coordinate fromCoord = vrpBuilder.getLocationMap().get(from);
                Coordinate toCoord = vrpBuilder.getLocationMap().get(to);
                double load = vrpBuilder.getLocationClassMap().get(fromCoord).getLoad();
                double consumption = EnergyConsumptionCalculator.calculateConsumption(fromCoord, toCoord, type, load);
                matrixBuilder.addTransportConsumption(from, to, consumption);
            }
        }
        return matrixBuilder.build();
    }
}
