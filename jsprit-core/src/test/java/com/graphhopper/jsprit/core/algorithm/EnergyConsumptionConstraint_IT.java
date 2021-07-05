package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.analysis.SolutionAnalyser;
import com.graphhopper.jsprit.core.problem.BatteryAM;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.EnergyDefaultCosts;
import com.graphhopper.jsprit.core.util.ManhattanCosts;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Assert;
import org.junit.Test;

public class EnergyConsumptionConstraint_IT {
    @Test
    public void rangeShouldNotBeExceeded() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type1")
            .addBatteryDimension(0,1000).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("type2")
            .addBatteryDimension(0,1000).build();
        VehicleTypeImpl type3 = VehicleTypeImpl.Builder.newInstance("type3")
            .addBatteryDimension(0,1000).build();
        VehicleTypeImpl type4 = VehicleTypeImpl.Builder.newInstance("type4")
            .addBatteryDimension(0,1000).build();
        VehicleTypeImpl type5 = VehicleTypeImpl.Builder.newInstance("type5")
            .addBatteryDimension(0,1000).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0,0)).setType(type1).setReturnToDepot(true).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance(0, 0)).setType(type2).setReturnToDepot(true).build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3").setStartLocation(Location.newInstance(0, 0)).setType(type3).setReturnToDepot(true).build();
        VehicleImpl v4 = VehicleImpl.Builder.newInstance("v4").setStartLocation(Location.newInstance(0, 0)).setType(type4).setReturnToDepot(true).build();
        VehicleImpl v5 = VehicleImpl.Builder.newInstance("v5").setStartLocation(Location.newInstance(0, 0)).setType(type5).setReturnToDepot(true).build();

        Delivery d1 = Delivery.Builder.newInstance("d1").setLocation(Location.newInstance(0,10))
            .addSizeDimension(2,1).build();
        Delivery d2 = Delivery.Builder.newInstance("d2").setLocation(Location.newInstance(0,12))
            .addSizeDimension(2,1).addSizeDimension(3,1).build();
        Delivery d3 = Delivery.Builder.newInstance("d3").setLocation(Location.newInstance(0,15))
            .addSizeDimension(0,1).addSizeDimension(4,1).build();
        Delivery d4 = Delivery.Builder.newInstance("d4").setLocation(Location.newInstance(0,20))
            .addSizeDimension(0,1).addSizeDimension(5,1).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .addJob(d1).addJob(d2).addJob(d3).addJob(d4)
            .addVehicle(v1).addVehicle(v2)
            .addVehicle(v3)
            .addVehicle(v4).addVehicle(v5);
        vrpBuilder.setRoutingCost(new ManhattanCosts());
        vrpBuilder.setEnergyCost(new EnergyDefaultCosts());

        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .setProperty(Jsprit.Parameter.VEHICLE_SWITCH, "true")
            .buildAlgorithm();
        // Activate Range constraints
        vra.setMaxIterations(2000);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

        SolutionAnalyser sa = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());

        for(VehicleRoute r : solution.getRoutes()){
            BatteryAM rangeAtBeginning = sa.getStateOfChargeAtBeginning(r);
            BatteryAM batteryDimensions = r.getVehicle().getType().getBatteryDimensions();
            System.out.println(r.getVehicle().getId() + " load@beginning: "  + rangeAtBeginning);
            System.out.println("cap: " + batteryDimensions);
            Assert.assertTrue("Battery range has been exceeded",
                rangeAtBeginning.isLessOrEqual(batteryDimensions));
        }

    }
}
