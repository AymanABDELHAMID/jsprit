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

import java.util.*;

/**
 * @author Ayman
 *
 * Updates vehicle state of charge at start and end of route as well as at each activity.
 * Update is triggered when either activityVisitor has been started, the insertion process has been started
 * or a job has been inserted.
 *
 * // TODO: Look at battery dimensions of the vehicles and current state of charge
 */

public class UpdateStateOfChargeBattery implements ActivityVisitor, StateUpdater {

    private StateManager stateManager;

    /*
     * default has one dimension with a value of zero
     */
    private BatteryAM currentStateOfCharge;

    private final TransportConsumption transportConsumption;

    private BatteryAM defaultStateOfCharge;

    private final StateId stateOfChargeId;

    private VehicleRoute route;

    private List<Vehicle> uniqueVehicles;

    private Map<VehicleTypeKey, State> states;

    static class State {

        Location prevLocation;

        BatteryAM consumption;

        public State(Location prevLocation, BatteryAM consumption) {
            this.prevLocation = prevLocation;

            this.consumption = consumption;
        }

        public Location getPrevLocation() {
            return prevLocation;
        }

        public BatteryAM getConsumption() {
            return consumption;
        }
    }

    public UpdateStateOfChargeBattery(TransportConsumption transportConsumption, StateManager stateManager, StateId stateOfChargeId, Collection<Vehicle> vehicles) {
        super();
        this.transportConsumption = transportConsumption;
        this.stateManager = stateManager;
        this.uniqueVehicles = getUniqueVehicles(vehicles);
        this.stateOfChargeId = stateOfChargeId;
        this.defaultStateOfCharge = BatteryAM.Builder.newInstance().addDimension(0, 100000).build();
        //this.defaultStateOfCharge = BatteryAM.Builder.newInstance().addDimension(0,uniqueVehicles.get(0).getType().getBatteryDimensions().get(0)).build();
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
            BatteryAM consumption = BatteryAM.Builder.newInstance().addDimension(0,0).build();
            State state = new State(v.getStartLocation(), consumption);
            states.put(v.getVehicleTypeIdentifier(), state);
            currentStateOfCharge = v.getType().getBatteryDimensions();
            if (currentStateOfCharge.getSoC(0) == 0) currentStateOfCharge = defaultStateOfCharge;
        }
    }

    @Override
    public void visit(TourActivity activity) {
        for (Vehicle v : uniqueVehicles) {
            // TODO: if act is recharging ...
            State old = states.get(v.getVehicleTypeIdentifier());
            double energyConsumption = transportConsumption.getEnergyConsumption(old.getPrevLocation(), activity.getLocation(), v);
            BatteryAM batteryConsumption = BatteryAM.Builder.newInstance().addDimension(0,energyConsumption).build();
            currentStateOfCharge = BatteryAM.subtractRange(v.getType().getBatteryDimensions(), batteryConsumption);
            stateManager.putActivityState(activity, v, stateOfChargeId, currentStateOfCharge);
            states.put(v.getVehicleTypeIdentifier(), new State(activity.getLocation(), batteryConsumption));
            stateManager.putInternalTypedActivityState(activity, v, InternalStates.STATE_OF_CHARGE, currentStateOfCharge);
        }
    }

    @Override
    public void finish() {
        for (Vehicle v : uniqueVehicles) {
            State old = states.get(v.getVehicleTypeIdentifier());
            if (v.isReturnToDepot()) {
                double energyConsumption = transportConsumption.getEnergyConsumption(old.getPrevLocation(), v.getEndLocation(), v);
                BatteryAM batteryConsumption = BatteryAM.Builder.newInstance().addDimension(0,energyConsumption).build();
                currentStateOfCharge = BatteryAM.subtractRange(currentStateOfCharge, batteryConsumption);
            }
            stateManager.putRouteState(route, v, stateOfChargeId, currentStateOfCharge);
        }
    }
}
