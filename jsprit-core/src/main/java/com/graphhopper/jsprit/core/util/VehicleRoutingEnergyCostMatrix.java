package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleEnergyTransportCost;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl.VehicleCostParams;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * EnergyConsumptionMatrix that allows pre-compiled energy-matrices to be considered as
 * {@link com.graphhopper.jsprit.core.problem.cost.VehicleRoutingEnergyCosts}
 * in the {@link com.graphhopper.jsprit.core.problem.VehicleRoutingProblem}.
 *
 * @author Ayman M.
 */

public class VehicleRoutingEnergyCostMatrix extends AbstractForwardVehicleEnergyTransportCost {


    static class RelationKey {

        static VehicleRoutingEnergyCostMatrix.RelationKey newKey(String from, String to) {
            return new VehicleRoutingEnergyCostMatrix.RelationKey(from, to);
        }

        final String from;
        final String to;

        public RelationKey(String from, String to) {
            super();
            this.from = from;
            this.to = to;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() { //Proposition TC
            String result = ""; //"";
            result = result + ((from == null) ? "0" : from);
            result += "-";
            result = result + ((to == null) ? "0" : to);
            return Integer.parseInt(result);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            VehicleRoutingEnergyCostMatrix.RelationKey other = (VehicleRoutingEnergyCostMatrix.RelationKey) obj;
            if (from == null) {
                if (other.from != null)
                    return false;
            } else if (!from.equals(other.from))
                return false;
            if (to == null) {
                if (other.to != null)
                    return false;
            } else if (!to.equals(other.to))
                return false;
            return true;
        }
    }

    public static class Builder {
        private static Logger log = LoggerFactory.getLogger(VehicleRoutingEnergyCostMatrix.Builder.class); // TODO: Understand the logger functionality

        private boolean isSymmetric;

        private Map<VehicleRoutingEnergyCostMatrix.RelationKey, Double> consumptions = new HashMap<VehicleRoutingEnergyCostMatrix.RelationKey, Double>();

        private boolean consumptionsSet = false;

        /**
         * Creates a new builder returning the matrix-builder.
         * <p>If you want to consider symmetric matrices, set isSymmetric to true.
         *
         * @param isSymmetric true if matrix is symmetric, false otherwise
         * @return builder
         */
        public static VehicleRoutingEnergyCostMatrix.Builder newInstance(boolean isSymmetric) {
            return new VehicleRoutingEnergyCostMatrix.Builder(isSymmetric);
        }

        private Builder(boolean isSymmetric) {
            this.isSymmetric = isSymmetric;
        }

        /**
         * Adds a transport-consumption for a particular relation.
         *
         * @param from     from loactionId
         * @param to       to locationId
         * @param consumption the consumption to be added
         * @return builder
         */
        public VehicleRoutingEnergyCostMatrix.Builder addTransportConsumption(String from, String to, double consumption) {
            VehicleRoutingEnergyCostMatrix.RelationKey key = VehicleRoutingEnergyCostMatrix.RelationKey.newKey(from, to);;
            if (!consumptionsSet) consumptionsSet = true;
            if (consumptions.containsKey(key)) {
                log.warn("Consumption from " + from + " to " + to + " already exists. This overrides consumption.");
            }
            consumptions.put(key, consumption);
            if (isSymmetric) {
                VehicleRoutingEnergyCostMatrix.RelationKey revKey = VehicleRoutingEnergyCostMatrix.RelationKey.newKey(to, from);
                if (consumptions.containsKey(revKey)) consumptions.put(revKey, consumption);
            }
            return this;
        }
    }
    private Map<VehicleRoutingEnergyCostMatrix.RelationKey, Double> consumptions = new HashMap<VehicleRoutingEnergyCostMatrix.RelationKey, Double>();

    private boolean isSymmetric;

    private boolean consumptionsSet;


    private VehicleRoutingEnergyCostMatrix(VehicleRoutingEnergyCostMatrix.Builder builder) {
        this.isSymmetric = builder.isSymmetric;
        consumptions.putAll(builder.consumptions);
        consumptionsSet = builder.consumptionsSet;
    }



    @Override
    public double getEnergyConsumption(Location from, Location to, double departureTime, Vehicle vehicle){
        return getConsumption(from.getId(), to.getId());
    }

    public double getConsumption(String fromId, String toId) {
        if (fromId.equals(toId)) return 0.0;
        if (!consumptionsSet) return 0.0;
        VehicleRoutingTransportCostsMatrix.RelationKey key = VehicleRoutingTransportCostsMatrix.RelationKey.newKey(fromId, toId);
        if (!isSymmetric) {
            if (consumptions.containsKey(key)) return consumptions.get(key);
            else
                throw new IllegalStateException("consumption value for relation from " + fromId + " to " + toId + " does not exist");
        } else {
            Double time = consumptions.get(key);
            if (time == null) {
                time = consumptions.get(VehicleRoutingTransportCostsMatrix.RelationKey.newKey(toId, fromId));
            }
            if (time != null) return time;
            else
                throw new IllegalStateException("consumption value for relation from " + fromId + " to " + toId + " does not exist");
        }
    }

    @Override
    public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle){
        return getDistance(from.getId(), to.getId());
    }

    /**
     * Returns the distance fromId to toId.
     *
     * @param fromId from locationId
     * @param toId   to locationId
     * @return the distance from fromId to toId
     * @throws IllegalStateException if distance of fromId -> toId is not found
     */
    public double getDistance(String fromId, String toId) {
        if (fromId.equals(toId)) return 0.0;
        if (!distancesSet) return 0.0;
        VehicleRoutingTransportCostsMatrix.RelationKey key = VehicleRoutingTransportCostsMatrix.RelationKey.newKey(fromId, toId);
        if (!isSymmetric) {
            if (distances.containsKey(key)) return distances.get(key);
            else
                throw new IllegalStateException("distance value for relation from " + fromId + " to " + toId + " does not exist");
        } else {
            Double time = distances.get(key);
            if (time == null) {
                time = distances.get(VehicleRoutingTransportCostsMatrix.RelationKey.newKey(toId, fromId));
            }
            if (time != null) return time;
            else
                throw new IllegalStateException("distance value for relation from " + fromId + " to " + toId + " does not exist");
        }
    }

    @Override
    public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle){
        return getTime(from.getId(), to.getId());
    }

    private double getTime(String fromId, String toId) {
        if (fromId.equals(toId)) return 0.0;
        if (!timesSet) return 0.0;
        VehicleRoutingTransportCostsMatrix.RelationKey key = VehicleRoutingTransportCostsMatrix.RelationKey.newKey(fromId, toId);
        if (!isSymmetric) {
            if (times.containsKey(key)) return times.get(key);
            else
                throw new IllegalStateException("time value for relation from " + fromId + " to " + toId + " does not exist");
        } else {
            Double time = times.get(key);
            if (time == null) {
                time = times.get(VehicleRoutingTransportCostsMatrix.RelationKey.newKey(toId, fromId));
            }
            if (time != null) return time;
            else
                throw new IllegalStateException("time value for relation from " + fromId + " to " + toId + " does not exist");
        }
    }

}
