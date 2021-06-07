package com.graphhopper.jsprit.instance.reader;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;

/**
 * @author Ayman
 *
 * Basic Interface for
 */
public interface evrpReader {

    /**
     *
     * @param vrp
     */
    public void addService(VehicleRoutingProblem vrp);

    /**
     *
     * @param vrp
     */
    public void addPickup(VehicleRoutingProblem vrp);

    /**
     *
     * @param vrp
     */
    public void addDelivery(VehicleRoutingProblem vrp);

    /**
     *
     * @param vrp
     */
    public void addRechargeStation(VehicleRoutingProblem vrp);

    /**
     *
     * @param file
     */
    public void read(String file);


}
