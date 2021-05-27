package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import com.graphhopper.jsprit.core.problem.BatteryAM;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeKey;

import java.util.Collection;
import java.util.List;
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

        double distance;

        public State(Location prevLocation, double distance) {
            this.prevLocation = prevLocation;
            this.distance = distance;
        }

        public Location getPrevLocation() {
            return prevLocation;
        }

        public double getDistance() {
            return distance;
        }
    }

    private Map<VehicleRoute, State> states;

    public UpdateStateOfCharge(StateManager stateManager){
        // I have seen VRP used everywhere in the state manager.
        this.stateManager = stateManager;
        this.defaultValue = BatteryAM.Builder.newInstance().build(); // in this builder the state of charge should be full by default.
    }

    void insertionStarts(VehicleRoute route) {
        double energyCostAtDepot;
        double energyCostAtEnd;
        for (Job j : route.getTourActivities().getJobs()) {
            if (j instanceof Delivery) {
                energyCostAtDepot += stateManager.getVrp().getEnergyConsumption().g
                    //Capacity.addup(loadAtDepot, j.getSize());
            } else if (j instanceof Pickup || j instanceof Service) {
                energyCostAtEnd +=
                    //Capacity.addup(loadAtEnd, j.getSize());
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

    }

    @Override
    public void visit(TourActivity activity) {

    }

    @Override
    public void finish() {

    }
}
