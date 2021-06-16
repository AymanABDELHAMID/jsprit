package com.graphhopper.jsprit.core.problem.constraint;


import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;


/**
 * @author Ayman M.
 * Checkout : https://github.com/matsim-org/matsim-libs/tree/master/contribs/freight/src/main/java/org/matsim/contrib/freight/jsprit
 */


public class EnergyConsumptionRouteLevelConstraint implements HardRouteConstraint {

    @Override
    public boolean fulfilled(JobInsertionContext insertionContext) {
        return true;
    }
}
