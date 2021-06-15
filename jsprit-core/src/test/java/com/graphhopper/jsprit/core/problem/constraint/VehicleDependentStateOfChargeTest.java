package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.VehicleDependentStateOfCharge;
import com.graphhopper.jsprit.core.algorithm.state.VehicleDependentTraveledDistance;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.TransportConsumption;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.ManhattanCosts;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VehicleDependentStateOfChargeTest {

    StateManager stateManager;

    VehicleRoute route;

    StateId stateOfChargeId;

    Vehicle vehicle;

    Vehicle vehicle2;

    VehicleRoutingProblem vrp;

    Delivery d1, d2, newDelivery;

    Pickup pickup;

    Shipment s1;

    Map<Vehicle, Double> maxDistanceMap;


    @Before
    public void doBefore() {
        vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance(10, 10)).build();

        maxDistanceMap = new HashMap<>();
        maxDistanceMap.put(vehicle, 200d);
        maxDistanceMap.put(vehicle2, 200d);

        d1 = Delivery.Builder.newInstance("d1").setLocation(Location.newInstance(10, 10)).build();
        d2 = Delivery.Builder.newInstance("d2").setLocation(Location.newInstance(20, 15)).build();
        pickup = Pickup.Builder.newInstance("pickup").setLocation(Location.newInstance(50, 50)).build();
        s1 = Shipment.Builder.newInstance("s1").setPickupLocation(Location.newInstance(35, 30))
            .setDeliveryLocation(Location.newInstance(20, 25)).build();

        newDelivery = Delivery.Builder.newInstance("new").setLocation(Location.newInstance(-10, 10)).build();

        vrp = VehicleRoutingProblem.Builder.newInstance()
            .setRoutingCost(new ManhattanCosts()).addVehicle(vehicle).addVehicle(vehicle2)
            .addJob(d1).addJob(d2).addJob(s1).addJob(pickup).addJob(newDelivery).build();

        route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory())
            .addDelivery(d1).addDelivery(d2).addPickup(s1).addPickup(pickup).addDelivery(s1).build();

        stateManager = new StateManager(vrp);

        //traveledDistanceId = stateManager.createStateId("traveledDistance");
        stateOfChargeId = stateManager.createStateId("stateOfCharge");

        VehicleDependentStateOfCharge stateOfCharge =
            new VehicleDependentStateOfCharge(vrp.getEnergyConsumption(), stateManager, stateOfChargeId, Arrays.asList(vehicle, vehicle2));

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
    public void whenCreatingAVehicleDependentActivityState_itShouldBeMemorized() {
        stateManager.putActivityState(route.getActivities().get(2), vehicle, stateOfChargeId, 50d);
        double getConsumption = stateManager.getActivityState(route.getActivities().get(2), vehicle, stateOfChargeId, Double.class);
        Assert.assertEquals(50d, getConsumption, 0.01);
    }

    @Test
    public void energyConsumptionOfShipmentInRoute() {
        double stateOfChargeBeforePickup = stateManager.getActivityState(route.getActivities().get(2), vehicle, stateOfChargeId, Double.class);
        double stateOfChargeBeforeDelivery = stateManager.getActivityState(route.getActivities().get(4), vehicle, stateOfChargeId, Double.class);
        double energyConsumptionPickupDelivery = stateOfChargeBeforeDelivery - stateOfChargeBeforePickup;
        Assert.assertEquals(90d, energyConsumptionPickupDelivery, 0.01);
    }

}
