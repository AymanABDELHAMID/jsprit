package com.graphhopper.jsprit.examples;

import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.Constraint;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.*;
import com.graphhopper.jsprit.instance.reader.GoekeReader;
import com.graphhopper.jsprit.io.problem.VrpXMLReader;

import java.io.File;
import java.util.Collection;

/**
 * @author Ayman M.
 *
 * This class reads and solves the instance used in Goeke et al. 2017
 */

public class PDTW_EV_GOEKE_2017 {
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

        /*
         * Build the problem.
         *
         * But define a problem-builder first.
         */
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();

        /*
         * A Goeke Reader reads Goeke-instance files, and stores the required information in the builder.
         */
        new GoekeReader(vrpBuilder).read("jsprit-instances/instances/e-vrp/Goeke_2018_PDPTW-EV/c101C6.txt");

        /*
         *  TransportCosts and EnergyCosts
         */
        Vehicle vehicle = vrpBuilder.getAddedVehicles().iterator().next();
        VehicleType type = vehicle.getType();
        // TODO: fix profile
        VehicleRoutingTransportCostsMatrix costMatrix = createMatrix(vrpBuilder);
        VehicleRoutingEnergyCostMatrix energyCostMatrix = createEnergyMatrix(vrpBuilder, type);
        vrpBuilder.setRoutingCost(costMatrix);
        vrpBuilder.setEnergyCost(energyCostMatrix);

        /*
         * Finally, the problem can be built.
         */
        VehicleRoutingProblem vrp = vrpBuilder.build();

        /*
         * Building the algorithm
         */
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(20000);

        /**
         * Solve the problem.
         * 01.07.2021
         * cannot find calculator for class com.graphhopper.jsprit.core.problem.job.Recharge
         * TODO: build Recharge calculator
         */
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        /*
         * Retrieve best solution.
         */
        VehicleRoutingProblemSolution solution = new SelectBest().selectSolution(solutions);

        /*
         * print solution
         */
        SolutionPrinter.print(solution);

        /*
         * Plot solution.
         */
        Plotter solPlotter = new Plotter(vrp, solution);
        solPlotter.plot("output/Goeke_.png", "Goeke_c101C6");


        new GraphStreamViewer(vrp, solution).setRenderDelay(50).labelWith(GraphStreamViewer.Label.ID).display();


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
