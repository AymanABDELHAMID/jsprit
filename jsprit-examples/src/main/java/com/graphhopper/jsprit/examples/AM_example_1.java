/*
 * Ayman Mahmoud - Example 1 - Simple VRP
 */

package com.graphhopper.jsprit.examples;

/*
    To add custom constraints use core.algorithm.VehicleRoutingAlgorithmBuilder
    to build an algorithm instead of core.algorithm.io.VehicleRoutingAlgorithms.
 */

import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer.Label;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.io.problem.VrpXMLWriter;

import java.io.File;
import java.util.Collection;

public class AM_example_1 {

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
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(WEIGHT_INDEX, 2).setEnergyType(2);
        VehicleType vehicleType = vehicleTypeBuilder.build();

        /*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
         */
        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.newInstance(10, 10));
        vehicleBuilder.setType(vehicleType);
        VehicleImpl vehicle = vehicleBuilder.build();

        /*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
         */
        VehicleImpl.Builder vehicleBuilder2 = VehicleImpl.Builder.newInstance("vehicle2");
        vehicleBuilder2.setStartLocation(Location.newInstance(10, 10));
        vehicleBuilder2.setType(vehicleType);
        VehicleImpl vehicle2 = vehicleBuilder2.build();

        /*
         * build services with id 1...4 at the required locations, each with a capacity-demand of 1.
         * Note, that the builder allows chaining which makes building quite handy
         */
        //Service service1 = Service.Builder.newInstance("Fathy").addSizeDimension(WEIGHT_INDEX,1).setLocation(Location.newInstance(0, 7)).build();
        //  Factory method (and shortcut) for creating a location object just with x and y coordinates.
        // Instead we can use this:
        Service service1 = Service.Builder.newInstance("Fathy").addSizeDimension(WEIGHT_INDEX,1).
            setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 7)).setLoad(15).build()).build();
        // TODO: check out why this is wrong.
        Service service2 = Service.Builder.newInstance("Ziad").addSizeDimension(WEIGHT_INDEX,1).setLocation(Location.newInstance(5, 13)).build();
        Service service3 = Service.Builder.newInstance("Quentin").addSizeDimension(WEIGHT_INDEX,1).setLocation(Location.newInstance(10, 7)).build();
        Service service4 = Service.Builder.newInstance("Amina").addSizeDimension(WEIGHT_INDEX,1).setLocation(Location.newInstance(15, 13)).build();

        /*
         * again define a builder to build the VehicleRoutingProblem I think this is the important part to dig deeper
         */
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle).addVehicle(vehicle2);
        vrpBuilder.addJob(service1).addJob(service2).addJob(service3).addJob(service4); // This is dumb, there must be an efficient way
        /*
         * build the problem
         * by default, the problem is specified such that FleetSize is INFINITE, i.e. an infinite number of
         * the defined vehicles can be used to solve the problem
         * by default, transport costs are computed as Euclidean distances
         */

        VehicleRoutingProblem problem = vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();
        // vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        /*
         * get the algorithm out-of-the-box.
         */
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);

        /*
         * and search a solution which returns a collection of solutions (here only one solution is constructed)
         */
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        /*
         * use the static helper-method in the utility class Solutions to get the best solution (in terms of least costs)
         */
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);


        new VrpXMLWriter(problem, solutions).write("output/problem-with-solution.xml");

        //SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);
        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.CONCISE);


        /*
         * plot
         */
        new Plotter(problem,bestSolution).plot("output/plot.png","simple example");

        /*
        render problem and solution with GraphStream
         */
        new GraphStreamViewer(problem, bestSolution).labelWith(Label.ID).setRenderDelay(200).display();

    }
}
