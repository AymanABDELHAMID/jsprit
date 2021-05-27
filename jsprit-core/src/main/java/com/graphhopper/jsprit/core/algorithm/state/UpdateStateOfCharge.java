package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import com.graphhopper.jsprit.core.problem.BatteryAM;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.EnergyConsumptionCalculator;

import java.util.Collection;
import java.util.Map;

/**
 * @author Ayman
 * Updates vehicle state of charge at start and end of route as well as at each activity.
 * Update is triggered when either activityVisitor has been started, the insertion process has been started
 * or a job has been inserted.
 */

public class UpdateStateOfCharge implements ActivityVisitor, StateUpdater, InsertionStartsListener, JobInsertedListener {


    private StateManager stateManager;

    /*
     * default has one dimension with a value of zero
     */
    private BatteryAM currentStateOfCharge;

    private BatteryAM defaultValue;

    private VehicleRoute route;

    static class State {

        Location prevLocation;

        public State(Location prevLocation) {
            this.prevLocation = prevLocation;
        }

        public Location getPrevLocation() {
            return prevLocation;
        }
    }


    private Map<VehicleRoute, State> states;

    public UpdateStateOfCharge(StateManager stateManager){
        // I have seen VRP used everywhere in the state manager.
        this.stateManager = stateManager;
        this.defaultValue = BatteryAM.Builder.newInstance().build(); // in this builder the state of charge should be full by default.
    }

    void insertionStarts(VehicleRoute route) {
        BatteryAM energyCostAtDepot = BatteryAM.Builder.newInstance().build();
        BatteryAM energyCostAtEnd = BatteryAM.Builder.newInstance().build();
        for (Job j : route.getTourActivities().getJobs()) {
            /**
             * Calculating the consumption at the job
             */
            double energyConsumption;
            UpdateStateOfCharge.State old = states.get(route);
            energyConsumption = EnergyConsumptionCalculator.calculateConsumption(old.getPrevLocation().getCoordinate(), j.getActivities().get(0).getLocation().getCoordinate(),
                route.getVehicle().getType(), j.getActivities().get(0).getLocation().getLoad());
            // TODO : ask why there is a list of activities for every job and how to get around that
            // TODO : getLoad from Job
            BatteryAM consumptionCost = BatteryAM.Builder.newInstance().addDimension(0,energyConsumption).build();
            if (j instanceof Delivery) {
                energyCostAtDepot = BatteryAM.subtractRange(energyCostAtDepot, consumptionCost);
            } else if (j instanceof Pickup || j instanceof Service) {
                energyCostAtEnd = BatteryAM.subtractRange(energyCostAtEnd, consumptionCost);
            }
        }
        stateManager.putTypedInternalRouteState(route, InternalStates.STATE_OF_CHARGE_AT_BEGINNING, energyCostAtDepot);
        stateManager.putTypedInternalRouteState(route, InternalStates.STATE_OF_CHARGE_AT_END, energyCostAtEnd);
    }

    @Override
    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {

    }

    @Override
    public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {

    }

    @Override
    public void begin(VehicleRoute route) {
        currentStateOfCharge = stateManager.getRouteState(route, InternalStates.STATE_OF_CHARGE_AT_BEGINNING, BatteryAM.class);
        if (currentStateOfCharge == null) currentStateOfCharge = defaultValue;
        this.route = route;
    }

    @Override
    public void visit(TourActivity activity) {
        //State old = states.get(v.getVehicleTypeIdentifier());
        //currentStateOfCharge = BatteryAM.subtractRange(currentStateOfCharge);
        //stateManager.putInternalTypedActivityState(act, InternalStates.LOAD, currentLoad);
    }

    @Override
    public void finish() {

    }
}
