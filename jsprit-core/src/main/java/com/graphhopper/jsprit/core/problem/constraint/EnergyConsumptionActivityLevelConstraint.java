package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.StateUpdater;
import com.graphhopper.jsprit.core.algorithm.state.VehicleDependentStateOfCharge;
import com.graphhopper.jsprit.core.problem.BatteryAM;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.TransportConsumption;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.VehicleRoutingEnergyCostMatrix;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ayman M.
 * Checkout : https://github.com/matsim-org/matsim-libs/tree/master/contribs/freight/src/main/java/org/matsim/contrib/freight/jsprit
 *
 * This is an implementation of the methods for the distance constraint.
 * The base for calculating the consumption is the energyConsumptionCalculator
 * // TODO: in vrp create an enum that chooses which consumption model is used
 *
 * Recharging is considered.
 */


public class EnergyConsumptionActivityLevelConstraint implements HardActivityConstraint {

    // private RouteAndActivityStateGetter stateManager; // TODO: Make sure you don't need the actual state manager

    private StateManager stateManager;

    private StateId stateOfChargeId;

    private TransportConsumption consumptionCalculator;

    private Double[] maxConsumptions; // not needed

    private VehicleRoutingProblem vrp;

    /**
     * This replaces the DistanceUpdater
     */
    //VehicleDependentStateOfCharge stateOfCharge =
     //   new VehicleDependentStateOfCharge(vrp.getEnergyConsumption(), stateManager, stateOfChargeId, vrp.getVehicles());

    public EnergyConsumptionActivityLevelConstraint(StateManager stateManager, StateId stateOfChargeID,  Map<Vehicle, Double> consumptionMap, VehicleRoutingProblem vrp) { // Why did we use RouteAndActivityStateGetter??
        this.stateManager = stateManager;
        this.consumptionCalculator = vrp.getEnergyConsumption();
        this.stateOfChargeId = stateOfChargeID;
        makeArray(consumptionMap); // not needed
        this.vrp = vrp; // in testing I found out this is null..
    }

    private void makeArray(Map<Vehicle, Double> maxConsumptions) {
        int maxIndex = getMaxIndex(maxConsumptions.keySet());
        this.maxConsumptions = new Double[maxIndex + 1];
        for (Vehicle v :  maxConsumptions.keySet()) {
            this.maxConsumptions[v.getIndex()] = maxConsumptions.get(v);
        }
        // TODO: change to get state of charge in the updater. This means the range should be updated
    }

    private int getMaxIndex(Collection<Vehicle> vehicles) {
        int index = 0;
        for (Vehicle v : vehicles) {
            if (v.getIndex() > index) index = v.getIndex();
        }
        return index;
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        if (!isElectric(iFacts.getNewVehicle())) return ConstraintsStatus.FULFILLED;
        Double currentStateOfCharge = 0d;
        boolean routeIsEmpty = iFacts.getRoute().isEmpty();
        if(!routeIsEmpty){
            currentStateOfCharge = stateManager.getActivityState(prevAct, iFacts.getNewVehicle(), stateOfChargeId, BatteryAM.class).getSoC(0);
        }
        double maxRange = iFacts.getNewVehicle().getType().getBatteryDimensions().getRange(0);

        double consumptionPrevAct2NewAct = consumptionCalculator.getEnergyConsumption(prevAct.getLocation(), newAct.getLocation(), iFacts.getNewVehicle());
        double consumptionNewAct2nextAct = consumptionCalculator.getEnergyConsumption(newAct.getLocation(), nextAct.getLocation(), iFacts.getNewVehicle());
        double consumptionPrevAct2NextAct = consumptionCalculator.getEnergyConsumption(prevAct.getLocation(), nextAct.getLocation(), iFacts.getNewVehicle());
        /**
         * checking if the route consists of only 2 activities
         */
        if (prevAct instanceof Start && nextAct instanceof End) consumptionPrevAct2NextAct = 0;
        if (nextAct instanceof End && !iFacts.getNewVehicle().isReturnToDepot()) {
            consumptionNewAct2nextAct = 0;
            consumptionPrevAct2NextAct = 0;
        }
        double additionalConsumption = consumptionPrevAct2NewAct + consumptionNewAct2nextAct - consumptionPrevAct2NextAct;
        if ( additionalConsumption > currentStateOfCharge) return ConstraintsStatus.NOT_FULFILLED;
        if (additionalConsumption > maxRange) return ConstraintsStatus.NOT_FULFILLED_BREAK;

        /**
         * checking consumption constraint if activity type is {@link com.graphhopper.jsprit.core.problem.job.Shipment}
         */
        double additionalConsumptionOfPickup = 0d;
        if (newAct instanceof DeliverShipment) {
            int iIndexOfPickup = iFacts.getRelatedActivityContext().getInsertionIndex();
            TourActivity pickup = iFacts.getAssociatedActivities().get(0);
            TourActivity actBeforePickup;
            if (iIndexOfPickup > 0) actBeforePickup = iFacts.getRoute().getActivities().get(iIndexOfPickup - 1);
            else actBeforePickup = new Start(iFacts.getNewVehicle().getStartLocation(), 0, Double.MAX_VALUE);
            TourActivity actAfterPickup;
            if (iIndexOfPickup < iFacts.getRoute().getActivities().size())
                actAfterPickup = iFacts.getRoute().getActivities().get(iIndexOfPickup);
            else
                actAfterPickup = nextAct;
            double consumptionActBeforePickup2Pickup = consumptionCalculator.getEnergyConsumption(actBeforePickup.getLocation(), pickup.getLocation(), iFacts.getNewVehicle());
            double consumptionPickup2ActAfterPickup = consumptionCalculator.getEnergyConsumption(pickup.getLocation(), actAfterPickup.getLocation(), iFacts.getNewVehicle());
            double consumptionBeforePickup2AfterPickup = consumptionCalculator.getEnergyConsumption(actBeforePickup.getLocation(), actAfterPickup.getLocation(), iFacts.getNewVehicle());
            if (routeIsEmpty) consumptionBeforePickup2AfterPickup = 0;
            if (actAfterPickup instanceof End && !iFacts.getNewVehicle().isReturnToDepot()) {
                consumptionPickup2ActAfterPickup = 0;
                consumptionBeforePickup2AfterPickup = 0;
            }
            additionalConsumptionOfPickup = consumptionActBeforePickup2Pickup + consumptionPickup2ActAfterPickup - consumptionBeforePickup2AfterPickup;
        }
        if ( additionalConsumption + additionalConsumptionOfPickup > currentStateOfCharge) return ConstraintsStatus.NOT_FULFILLED; // Maybe here check the possibility of recharge stations

        return ConstraintsStatus.FULFILLED;
    }

    private boolean isElectric(Vehicle newVehicle) {
        // TODO read energy type in the problem.
        if (newVehicle.getType().getEnergyType() == 2 ) return true;
        else return false;
    }
}
