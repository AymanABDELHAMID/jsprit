package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.UpdateStateOfChargeBattery;
import com.graphhopper.jsprit.core.problem.BatteryAM;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.EnergyConsumptionCosts;
import com.graphhopper.jsprit.core.util.EnergyDefaultCosts;
import com.graphhopper.jsprit.core.util.ManhattanCosts;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class VehicleDependentStateOfChargeBatteryTest {
    StateManager stateManager;

    VehicleRoute route;

    StateId stateOfChargeId;

    Vehicle vehicle;

    Vehicle vehicle2;

    VehicleRoutingProblem vrp;

    Delivery d1, d2, newDelivery;

    Pickup pickup;

    Shipment s1;


    @Before
    public void doBefore() {

        // .addCapacityDimension(dimensionIndex,dimensionValue)
        final int WEIGHT_INDEX = 0;
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("BEV").addCapacityDimension(WEIGHT_INDEX, 2);

        // .addBatteryDimension(dimensionIndex,range)
        final int BATTERY_INDEX = 0;
        vehicleTypeBuilder.addBatteryDimension(BATTERY_INDEX, 30000); // Ayman: tested with small range and found no solution


        // Building the Vehicle
        VehicleType vehicleType = vehicleTypeBuilder.build();

        /*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "BEV"
         */
        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("v");
        vehicleBuilder.setStartLocation(Location.newInstance(0, 0));
        vehicleBuilder.setType(vehicleType);
        vehicle = vehicleBuilder.build();

        VehicleImpl.Builder vehicleBuilder2 = VehicleImpl.Builder.newInstance("v2");
        vehicleBuilder2.setStartLocation(Location.newInstance(10, 10));
        vehicleBuilder2.setType(vehicleType);
        vehicle2 = vehicleBuilder2.build();

        d1 = Delivery.Builder.newInstance("d1").setLocation(Location.newInstance(10, 10)).build();
        d2 = Delivery.Builder.newInstance("d2").setLocation(Location.newInstance(20, 15)).build();
        pickup = Pickup.Builder.newInstance("pickup").setLocation(Location.newInstance(50, 50)).build();
        s1 = Shipment.Builder.newInstance("s1").setPickupLocation(Location.newInstance(35, 30))
            .setDeliveryLocation(Location.newInstance(20, 25)).build();

        newDelivery = Delivery.Builder.newInstance("new").setLocation(Location.newInstance(-10, 10)).build();

        vrp = VehicleRoutingProblem.Builder.newInstance()
            .setRoutingCost(new ManhattanCosts()).setEnergyCost(new EnergyDefaultCosts()).addVehicle(vehicle).addVehicle(vehicle2)
            .addJob(d1).addJob(d2).addJob(s1).addJob(pickup).addJob(newDelivery).build();

        route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory())
            .addDelivery(d1).addDelivery(d2).addPickup(s1).addPickup(pickup).addDelivery(s1).build();

        stateManager = new StateManager(vrp);

        stateOfChargeId = stateManager.createStateId("stateOfChargeBattery");

        UpdateStateOfChargeBattery stateOfCharge =
            new UpdateStateOfChargeBattery(vrp.getEnergyConsumption(), stateManager, stateOfChargeId, Arrays.asList(vehicle, vehicle2));

        stateManager.addStateUpdater(stateOfCharge);
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
    }

    private TourActivity act(int i) {
        return route.getActivities().get(i);
    }

    private TourActivity newAct() {
        return vrp.getActivities(newDelivery).get(0);
    }

    @Test
    public void stateOfChargeShouldDecreaseInRoute() {
        BatteryAM stateOfChargeBeforePickup = stateManager.getActivityState(route.getActivities().get(2), vehicle, stateOfChargeId, BatteryAM.class);
        BatteryAM stateOfChargeBeforeDelivery = stateManager.getActivityState(route.getActivities().get(4), vehicle, stateOfChargeId, BatteryAM.class);
        Assert.assertTrue(stateOfChargeBeforeDelivery.getSoC(0) >= stateOfChargeBeforePickup.getSoC(0));
    }

    @Test
    public void energyConsumptionOfShipmentInRouteInternal() {
        BatteryAM stateOfChargeBeforePickup = stateManager.getActivityState(route.getActivities().get(2), vehicle, InternalStates.STATE_OF_CHARGE, BatteryAM.class);
        BatteryAM stateOfChargeBeforeDelivery = stateManager.getActivityState(route.getActivities().get(4), vehicle, InternalStates.STATE_OF_CHARGE, BatteryAM.class);
        Assert.assertTrue(stateOfChargeBeforeDelivery.getSoC(0) > stateOfChargeBeforePickup.getSoC(0));
    }

    //stateManager.getActivityState(serviceRoute.getActivities().get(0), vehicle, InternalStates.LOAD, BatteryAM.class);
}
