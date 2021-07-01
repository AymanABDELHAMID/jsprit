package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Recharge;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

public class RechargeServiceInsertionCalculator extends AbstractInsertionCalculator {

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
