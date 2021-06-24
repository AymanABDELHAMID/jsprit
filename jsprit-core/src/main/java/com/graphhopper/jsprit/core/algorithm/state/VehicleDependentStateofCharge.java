package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import com.graphhopper.jsprit.core.problem.BatteryAM;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.TransportConsumption;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeKey;
import com.graphhopper.jsprit.core.util.EnergyConsumptionCalculator;

import java.util.*;

/**
 * @author Ayman
 * Vehicle dependent state of charge in case of multiple routes
 */

public class VehicleDependentStateOfCharge  implements StateUpdater, ActivityVisitor, InsertionStartsListener, JobInsertedListener {


    static class State {

        Location prevLocation;

        double consumption;

        public State(Location prevLocation, double consumption) {
            this.prevLocation = prevLocation;
        }

        public Location getPrevLocation() {
            return prevLocation;
        }

        public double getConsumption() {
            return consumption;
        }
    }

    private final TransportConsumption transportConsumption;

    private final StateManager stateManager;

    private final StateId consumptionId;

    private VehicleRoute route;

    private BatteryAM defaultValue;

    private List<Vehicle> uniqueVehicles;

    private Map<VehicleTypeKey, State> states;

    public VehicleDependentStateOfCharge(TransportConsumption transportConsumptionMatrices, StateManager stateManager, StateId consumptionInRouteId, Collection<Vehicle> vehicles) {
        this.transportConsumption = transportConsumptionMatrices;
        this.stateManager = stateManager;
        this.consumptionId = consumptionInRouteId;
        this.defaultValue = BatteryAM.Builder.newInstance().build();
        uniqueVehicles = getUniqueVehicles(vehicles);
    }

    private List<Vehicle> getUniqueVehicles(Collection<Vehicle> vehicles) {
        Set<VehicleTypeKey> types = new HashSet<>();
        List<Vehicle> uniqueVehicles = new ArrayList<>();
        for (Vehicle v : vehicles) {
            if (!types.contains(v.getVehicleTypeIdentifier())) {
                types.add(v.getVehicleTypeIdentifier());
                uniqueVehicles.add(v);
            }
        }
        return uniqueVehicles;
    }

    @Override
    public void begin(VehicleRoute route) {
        // What to do in Begin?
        this.route = route;
    }

    @Override
    public void visit(TourActivity activity) {
        for (Vehicle v : uniqueVehicles) {
            VehicleDependentStateOfCharge.State old = states.get(v.getVehicleTypeIdentifier());
            double consumption = old.getConsumption();
            consumption += transportConsumption.getEnergyConsumption(old.getPrevLocation(), activity.getLocation(), v);
            //         stateManager.putActivityState(route.getActivities().get(2), vehicle, stateOfChargeId, 50d);
            stateManager.putActivityState(activity, v, consumptionId, consumption);
            states.put(v.getVehicleTypeIdentifier(), new VehicleDependentStateOfCharge.State(activity.getLocation(), consumption));
        }
    }

    @Override
    public void finish() {
        for (Vehicle v : uniqueVehicles) {
            VehicleDependentStateOfCharge.State old = states.get(v.getVehicleTypeIdentifier());
            double consumption = old.getConsumption();
            if (v.isReturnToDepot()) {
                consumption += transportConsumption.getEnergyConsumption(old.getPrevLocation(), v.getEndLocation(),v);
            }
            stateManager.putRouteState(route, v, consumptionId, consumption);
        }
    }

    void insertionStarts(VehicleRoute route) {
        BatteryAM energyCostAtDepot = BatteryAM.Builder.newInstance().build();
        BatteryAM energyCostAtEnd = BatteryAM.Builder.newInstance().build();
        for (Job j : route.getTourActivities().getJobs()) {
            double energyConsumption;
            VehicleDependentStateOfCharge.State old = states.get(route);
            energyConsumption = EnergyConsumptionCalculator.calculateConsumption(old.getPrevLocation().getCoordinate(), j.getActivities().get(0).getLocation().getCoordinate(),
                route.getVehicle().getType(), j.getActivities().get(0).getLocation().getLoad());
            BatteryAM consumptionCost = BatteryAM.Builder.newInstance().addDimension(0,energyConsumption).build();
            if (j instanceof Delivery) {
                energyCostAtDepot = BatteryAM.subtractRange(energyCostAtDepot, consumptionCost);
            } else if (j instanceof Pickup || j instanceof Service) {
                energyCostAtEnd = BatteryAM.subtractRange(energyCostAtEnd, consumptionCost);
            }
        }
        // TODO : add vehicle dependent internal state for the state of charge
        stateManager.putTypedInternalRouteState(route, InternalStates.STATE_OF_CHARGE_AT_BEGINNING, energyCostAtDepot);
        stateManager.putTypedInternalRouteState(route, InternalStates.STATE_OF_CHARGE_AT_END, energyCostAtEnd);
    }


    @Override
    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        /**
         * 22.06
         * Insertion Starts: initialize state
         * Insertion Starts is called before Begin, should initialize State here then use it in begin.
         * warning : risk of reinitializing states everytime the
         */
        states = new HashMap<>();
        for (Vehicle v : uniqueVehicles) {
            VehicleDependentStateOfCharge.State state = new VehicleDependentStateOfCharge.State(v.getStartLocation(), 0);
            states.put(v.getVehicleTypeIdentifier(), state);
        }
        for (VehicleRoute route : vehicleRoutes) {
            insertionStarts(route);
        }
    }

    @Override
    public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
        VehicleDependentStateOfCharge.State old = states.get(route);
        if (job2insert instanceof Delivery) {
            BatteryAM loadAtDepot = stateManager.getRouteState(inRoute, InternalStates.STATE_OF_CHARGE_AT_BEGINNING, BatteryAM.class);
            if (loadAtDepot == null) loadAtDepot = defaultValue;
            double energyConsumption = EnergyConsumptionCalculator.calculateConsumption(old.getPrevLocation().getCoordinate(), job2insert.getActivities().get(0).getLocation().getCoordinate(),
                route.getVehicle().getType(), job2insert.getActivities().get(0).getLocation().getLoad());
            BatteryAM consumptionCost = BatteryAM.Builder.newInstance().addDimension(0,energyConsumption).build();
            stateManager.putTypedInternalRouteState(inRoute, InternalStates.STATE_OF_CHARGE_AT_BEGINNING, BatteryAM.addRange(loadAtDepot, consumptionCost));
        } else if (job2insert instanceof Pickup || job2insert instanceof Service) {
            BatteryAM loadAtEnd = stateManager.getRouteState(inRoute, InternalStates.STATE_OF_CHARGE_AT_END, BatteryAM.class);
            double energyConsumption = EnergyConsumptionCalculator.calculateConsumption(old.getPrevLocation().getCoordinate(), job2insert.getActivities().get(0).getLocation().getCoordinate(),
                route.getVehicle().getType(), job2insert.getActivities().get(0).getLocation().getLoad());
            BatteryAM consumptionCost = BatteryAM.Builder.newInstance().addDimension(0,energyConsumption).build();
            if (loadAtEnd == null) loadAtEnd = defaultValue;
            stateManager.putTypedInternalRouteState(inRoute, InternalStates.STATE_OF_CHARGE_AT_END, BatteryAM.addRange(loadAtEnd, consumptionCost));
        }
    }

    public void informRouteChanged(VehicleRoute route){
        insertionStarts(route);
    }
}
