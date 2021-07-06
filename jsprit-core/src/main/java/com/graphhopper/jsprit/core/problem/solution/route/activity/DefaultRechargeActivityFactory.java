package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.job.Recharge;
import com.graphhopper.jsprit.core.problem.job.Service;

public class DefaultRechargeActivityFactory  implements TourActivityFactory {
    @Override
    public AbstractActivity createActivity(Service service) {
        AbstractActivity act;
        if (service.getLocation() == null) {
            throw new IllegalArgumentException("Recharge station must have a location.");
        }
        act = new RechargeService((Recharge) service);
        return act;
    }
}
