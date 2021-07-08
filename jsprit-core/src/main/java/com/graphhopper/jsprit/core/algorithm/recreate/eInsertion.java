package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Insertion based on Greedy Jobs and CS approach in Macrina et al. 2019
 *
 * Explain insertion strategy here
 *
 */

public class eInsertion extends AbstractInsertionStrategy  {

    private static Logger logger = LoggerFactory.getLogger(RegretInsertionFast.class);

    private ScoringFunction scoringFunction;

    private JobInsertionCostsCalculator insertionCostsCalculator;

    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        return null;
    }


}
