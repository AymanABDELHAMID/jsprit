package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.SoftActivityConstraint;
import com.graphhopper.jsprit.core.problem.constraint.SoftRouteConstraint;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingEnergyCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Recharge;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RechargeServiceInsertionCalculator extends AbstractInsertionCalculator {

    /**
     * @author Ayman M.
     * Calculator that calculates the best insertion position for a {@link Recharge}
     */

    private static final Logger logger = LoggerFactory.getLogger(ServiceInsertionCalculator.class);

    private final SoftRouteConstraint softRouteConstraint;

    private final SoftActivityConstraint softActivityConstraint;

    private final VehicleRoutingTransportCosts transportCosts;

    private final VehicleRoutingEnergyCosts energyCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    private final ActivityInsertionCostsCalculator activityInsertionCostsCalculator;

    private final JobActivityFactory activityFactory;

    private final AdditionalAccessEgressCalculator additionalAccessEgressCalculator;

    private final ConstraintManager constraintManager;

    public RechargeServiceInsertionCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts,  VehicleRoutingEnergyCosts energyCosts, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, ConstraintManager constraintManager, JobActivityFactory activityFactory) {
        super();
        // TODO: ensure there aren't more attributes needed
        this.transportCosts = routingCosts;
        this.energyCosts = energyCosts;
        this.activityCosts = activityCosts;
        this.constraintManager = constraintManager;
        softActivityConstraint = constraintManager;
        softRouteConstraint = constraintManager;
        this.activityInsertionCostsCalculator = activityInsertionCostsCalculator;
        additionalAccessEgressCalculator = new AdditionalAccessEgressCalculator(routingCosts);
        this.activityFactory = activityFactory;
        logger.debug("initialise {}", this);
    }

    @Override
    public String toString() {
        return "[name=calculatesRechargeStationInsertion]";
    }

    /**
     * Calculates the marginal cost of inserting job i locally. This is based on the
     * assumption that cost changes can entirely covered by only looking at the predecessor i-1 and its successor i+1.
     */

    @Override
    public InsertionData getInsertionData(VehicleRoute currentRoute, final Job jobToInsert, final Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownCosts) {
        JobInsertionContext insertionContext = new JobInsertionContext(currentRoute, jobToInsert, newVehicle, newDriver, newVehicleDepartureTime);
        Recharge recharge = (Recharge) jobToInsert;
        int insertionIndex = InsertionData.NO_INDEX;

        /*
        check hard constraints at route level
         */

        /*
        check soft constraints at route level
         */
        // TODO: add soft route constraint to prevent vehicles from visiting recharge stations if not needed
        /*
        generate new start and end for new vehicle
         */


        return null;
    }
}
