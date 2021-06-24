package com.graphhopper.jsprit.core.algorithm.state;


import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.TransportConsumption;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeKey;

import java.util.*;

/**
 * @author Ayman
 * Updates vehicle state of charge at start and end of route as well as at each activity.
 * Update is triggered when either activityVisitor has been started, the insertion process has been started
 * or a job has been inserted.
 */

public class UpdateStateOfCharge implements ActivityVisitor, StateUpdater {


    static class State {

        Location prevLocation;

        double consumption;

        public State(Location prevLocation, double consumption) {
            this.prevLocation = prevLocation;

            this.consumption = consumption;
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

    private final StateId stateOfChargeId;

    private VehicleRoute route;

    private List<Vehicle> uniqueVehicles;

    private Map<VehicleTypeKey, State> states;

    public UpdateStateOfCharge(TransportConsumption transportConsumption, StateManager stateManager, StateId stateOfChargeId, Collection<Vehicle> vehicles) {
        this.transportConsumption = transportConsumption;
        this.stateManager = stateManager;
        this.stateOfChargeId = stateOfChargeId;
        this.uniqueVehicles = getUniqueVehicles(vehicles);;
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
        this.route = route;
        states = new HashMap<>();
        for (Vehicle v : uniqueVehicles) {
            // Start with zero consumption, it should start with vehicle fully charged
            State state = new State(v.getStartLocation(), 0);
            states.put(v.getVehicleTypeIdentifier(), state);
        }
    }

    @Override
    public void visit(TourActivity activity) {
        for (Vehicle v : uniqueVehicles) {
            State old = states.get(v.getVehicleTypeIdentifier());
            double consumption = old.getConsumption();
            consumption += transportConsumption.getEnergyConsumption(old.getPrevLocation(), activity.getLocation(), v);
            stateManager.putActivityState(activity, v, stateOfChargeId, consumption);
            states.put(v.getVehicleTypeIdentifier(), new State(activity.getLocation(), consumption));
        }
    }

    @Override
    public void finish() {
        for (Vehicle v : uniqueVehicles) {
            State old = states.get(v.getVehicleTypeIdentifier());
            double consumption = old.getConsumption();
            if (v.isReturnToDepot()) {
                consumption += transportConsumption.getEnergyConsumption(old.getPrevLocation(), v.getEndLocation(), v);
            }
            stateManager.putRouteState(route, v, stateOfChargeId, consumption);
        }
    }
}
