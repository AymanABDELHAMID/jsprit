package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.TransportConsumption;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeKey;
import jdk.internal.net.http.websocket.Transport;

import java.util.*;

/**
 * @author Ayman
 * Vehicle dependent state of charge in case of multiple routes
 */

public class VehicleDependentStateOfCharge  implements StateUpdater, ActivityVisitor {
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

    private final StateId consumptionId;

    private VehicleRoute route;

    private List<Vehicle> uniqueVehicles;

    private Map<VehicleTypeKey, VehicleDependentStateOfCharge.State> states;

    public VehicleDependentStateOfCharge(TransportConsumption transportConsumptionMatrices, StateManager stateManager, StateId consumptionInRouteId, Collection<Vehicle> vehicles) {
        this.transportConsumption = transportConsumptionMatrices;
        this.stateManager = stateManager;
        this.consumptionId = consumptionInRouteId;
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
        this.route = route;
        states = new HashMap<>();
        for (Vehicle v : uniqueVehicles) {
            VehicleDependentStateOfCharge.State state = new VehicleDependentStateOfCharge.State(v.getStartLocation(), 0);
            states.put(v.getVehicleTypeIdentifier(), state);
        }
    }

    @Override
    public void visit(TourActivity activity) {
        for (Vehicle v : uniqueVehicles) {
            VehicleDependentStateOfCharge.State old = states.get(v.getVehicleTypeIdentifier());
            double consumption = old.getConsumption();
            consumption += transportConsumption.getEnergyConsumption(old.getPrevLocation(), activity.getLocation(), v);
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
}
