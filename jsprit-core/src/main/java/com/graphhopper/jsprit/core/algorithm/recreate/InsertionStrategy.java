/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener;
import com.graphhopper.jsprit.core.problem.ChargingStation;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;


/**
 * Basic interface for insertion strategies
 *
 * @author stefan schroeder
 */

public interface InsertionStrategy {

    /**
     * Inserts unassigned jobs into vehicle routes.
     *
     * @param vehicleRoutes  existing vehicle routes
     * @param unassignedJobs jobs to be inserted
     */
    public Collection<Job> insertJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs);

    /* First solution proposed by Tarek
    If this approach is followed, insertion points for the charging stations can be computed separately or in combination with Jobs
    If separately, i'd advice to do it after the insertion of jobs
    There can also be two parts of charging station insertion,
    - one during the insertion of new jobs to make sure that such insertion do not violate the constraints regarding batteries
    - another phase at the end where we check if a charging station is still needed independently of the job inserted
    */
    /*
    default Collection<Job> insertJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs, Collection<ChargingStation> chargingStations) {
        return this.insertJobs(vehicleRoutes, unassignedJobs);
    }*/

    //Second solution proposed by Tarek
    /*
    default void insertChargingStations(Collection<VehicleRoute> vehicleRoutes, Collection<ChargingStation> chargingStations) {

    }*/




    public void addListener(InsertionListener insertionListener);

    public void removeListener(InsertionListener insertionListener);

    public Collection<InsertionListener> getListeners();

}
