package com.graphhopper.jsprit.core.algorithm.recreate;


import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListeners;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.constraint.HardRouteConstraint;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingEnergyCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.driver.DriverImpl;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.CostFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author: Ayman
 */


public class ServiceInsertionAndStateOfChargeTest {
    VehicleRoutingTransportCosts routingCosts;
    VehicleRoutingEnergyCosts energyCosts;

    VehicleRoutingActivityCosts activityCosts = new VehicleRoutingActivityCosts() {

        @Override
        public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
            return 0;
        }

        @Override
        public double getActivityDuration(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
            return tourAct.getOperationTime();
        }

    };

    HardActivityConstraint hardActivityLevelConstraint = new HardActivityConstraint() {

        @Override
        public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
            return ConstraintsStatus.FULFILLED;
        }
    };

    HardRouteConstraint hardRouteLevelConstraint = new HardRouteConstraint() {

        @Override
        public boolean fulfilled(JobInsertionContext insertionContext) {
            return true;
        }

    };

    ActivityInsertionCostsCalculator activityInsertionCostsCalculator;

//    ShipmentInsertionCalculator insertionCalculator;

    VehicleRoutingProblem vehicleRoutingProblem;

    Vehicle vehicle;

    @Before
    public void doBefore() {
        routingCosts = CostFactory.createManhattanCosts();
        energyCosts = CostFactory.createConstantEnergyCosts();
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").addCapacityDimension(0, 200).addBatteryDimension(0, 500).setCostPerDistance(1).build();
        vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("0,0")).setType(type).build();
        activityInsertionCostsCalculator = new LocalActivityInsertionCostsCalculator(routingCosts, activityCosts, mock(StateManager.class));
        createInsertionCalculator(hardRouteLevelConstraint);
        vehicleRoutingProblem = mock(VehicleRoutingProblem.class);
    }

    private void createInsertionCalculator(HardRouteConstraint hardRouteLevelConstraint) {
        ConstraintManager constraintManager = new ConstraintManager(mock(VehicleRoutingProblem.class), mock(RouteAndActivityStateGetter.class));
        constraintManager.addConstraint(hardRouteLevelConstraint);
//        insertionCalculator = new ShipmentInsertionCalculator(routingCosts, activityCosts, activityInsertionCostsCalculator, constraintManager, );
    }

    @Test
    public void whenInsertingServiceWhileNoRangeIsAvailable_itMustReturnTheCorrectInsertionIndex() {
        Delivery delivery = (Delivery) Delivery.Builder.newInstance("del").addSizeDimension(0, 41).setLocation(Location.newInstance("10,10")).build();
        Pickup pickup = (Pickup) Pickup.Builder.newInstance("pick").addSizeDimension(0, 15).setLocation(Location.newInstance("0,10")).build();

        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").addCapacityDimension(0, 50).addBatteryDimension(0, 500).setCostPerDistance(1).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("0,0")).setType(type).build();

        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(delivery).addJob(pickup).addVehicle(vehicle).build();

        VehicleRoute route = VehicleRoute.emptyRoute();
        route.setVehicleAndDepartureTime(vehicle, 0.0);

        Inserter inserter = new Inserter(new InsertionListeners(), vrp);
        inserter.insertJob(delivery, new InsertionData(0, 0, 0, vehicle, null), route);

        JobActivityFactory activityFactory = new JobActivityFactory() {
            @Override
            public List<AbstractActivity> createActivities(Job job) {
                return vrp.copyAndGetActivities(job);
            }
        };

        StateManager stateManager = new StateManager(vrp);
        stateManager.updateStateOfChargeStates();

        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        /**
         * @author: Ayman
         * Note: if I understand correctly, the consumption map should be constructed here.
         * The consumption map should then be updated with "update consumption map" - or with using
         */
        Map<Vehicle, Double> consumptionMap;
        consumptionMap = CreateMaxDistanceMap(vrp);
        constraintManager.addEnergyConsumptionConstraint(stateManager, consumptionMap);

        // Tested until here
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());

        JobCalculatorSwitcher switcher = new JobCalculatorSwitcher();
        ServiceInsertionCalculator serviceInsertionCalc = new ServiceInsertionCalculator(routingCosts, activityCosts, energyCosts, activityInsertionCostsCalculator, constraintManager, activityFactory);
        ShipmentInsertionCalculator insertionCalculator = new ShipmentInsertionCalculator(routingCosts, activityCosts, energyCosts, activityInsertionCostsCalculator, constraintManager, activityFactory);


        switcher.put(Pickup.class, serviceInsertionCalc);
        switcher.put(Delivery.class, serviceInsertionCalc);
        switcher.put(Shipment.class, insertionCalculator);

        InsertionData iData = switcher.getInsertionData(route, pickup, vehicle, 0, DriverImpl.noDriver(), Double.MAX_VALUE);

        assertEquals(1, iData.getDeliveryInsertionIndex());
    }

    /**
     * This method constructs the initial maximum consumption map
     * @param vrp
     * @return
     */
    public Map<Vehicle, Double> CreateMaxDistanceMap(VehicleRoutingProblem vrp){
        Collection<Vehicle> vehicles = vrp.getVehicles();
        Map<Vehicle, Double> consumptionMap = new HashMap<>();
        for (Vehicle v :  vehicles) {
            double stateOfCharge = v.getType().getBatteryDimensions().get(0); // Assuming vehicles have only one battery
            consumptionMap.put(v, stateOfCharge);
        }
        return consumptionMap;
    }

}
