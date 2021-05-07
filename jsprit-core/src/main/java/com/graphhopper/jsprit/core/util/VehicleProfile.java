package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.*;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingEnergyCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.apache.commons.configuration.XMLConfiguration;

import java.util.*;

/**
 * @author Ayman M.
 * Class gets type, returns type parameters
 */

public class VehicleProfile {
    /*
    The profile name should be one of the profiles recognized, if not, the vehicle profile is set to default values of a car profile
    To add your own profile, just add a file containing the profile data in the IO
     */
    private final String profile_name;
    private double frontalArea;// in m^2
    private double weight;// in kg
    private double maxSpeed;// in m/s
    private double avgSpeed;// in m/s
    private double crr;
    private double cw;
    private double nm;
    private double ng;


    public VehicleProfile(String profile_name, double frontalArea, double weight, double maxSpeed, double avgSpeed, double crr, double cw, double nm, double ng) {
        super();
        this.profile_name = profile_name;
        this.frontalArea = frontalArea;
        this.weight = weight;
        this.maxSpeed = maxSpeed;
        this.avgSpeed = avgSpeed;
        this.crr = crr;
        this.cw = cw;
        this.nm = nm;
        this.ng = ng;
    }

    /**
     * Vehicle profile XML configuration
     */
    public static class VehicleProfileConfig {

        private XMLConfiguration xmlConfig;

        public VehicleProfileConfig() {
            xmlConfig = new XMLConfiguration();
        }

        public XMLConfiguration getXMLConfiguration() {
            return xmlConfig;
        }
    }

    /**
     * Builder that builds the vehicle-type.
     *
     * @author Ayman M.
     */
    public static class Builder {

        public static VehicleProfile.Builder newInstance(String profile_name) {
            if (profile_name == null) throw new IllegalArgumentException();
            return new VehicleProfile.Builder(profile_name);
        }

        private String profileName;
        private Set<String> addedProfileNames = new LinkedHashSet<>();
        private List<VehicleProfile> vehicleProfiles = new ArrayList<>();

        /**
         * 29.04.21 Ayman M. Adding default profile values
         */
        private double frontalArea;// in m^2
        private double weight;// in kg
        private double maxSpeed;// in m/s
        private double avgSpeed;// in m/s
        private double crr;
        private double cw;
        private double nm;
        private double ng;

        private Builder(String profile_name) {
            this.profileName = profile_name;
        }

        /**
         * @author Ayman M.
         *
         * Setting profile only requires the name, the user has no access to the parameters of the profile
         * The profile name is then used to build an object that can be used to build an object (@link VehicleProfile)
         * @param profile_name
         * @return
         */

        public VehicleProfile.Builder setProfile(String profile_name) {
            this.profileName = profile_name;
            return this;
        }


        /**
         *
         * @param frontalArea
         *
         * @return builder
         */
        public VehicleProfile.Builder setFrontalArea(double frontalArea) {
            this.frontalArea = frontalArea;
            return this;
        }

        /**
         *
         * @param weight
         *
         * @return builder
         */
        public VehicleProfile.Builder setVehicleWeight(double weight) {
            this.weight = weight;
            return this;
        }

        /**
         *
         * @param speed
         *
         * @return builder
         */
        public VehicleProfile.Builder setVehicleMaxSpeed(double speed) {
            this.maxSpeed = speed;
            return this;
        }

        /**
         *
         * @param speed
         *
         * @return builder
         */
        public VehicleProfile.Builder setVehicleAvgSpeed(double speed) {
            this.avgSpeed = speed;
            return this;
        }

        /**
         *
         * @param crr
         *
         * @return builder
         */
        public VehicleProfile.Builder setVehicleCRR(double crr) {
            this.crr = crr;
            return this;
        }

        /**
         *
         * @param cw
         *
         * @return builder
         */
        public VehicleProfile.Builder setVehicleCW(double cw) {
            this.cw = cw;
            return this;
        }

        /**
         *
         * @param nm
         *
         * @return builder
         */
        public VehicleProfile.Builder setVehicleNM(double nm) {
            this.nm = nm;
            return this;
        }

        /**
         *
         * @param ng
         *
         * @return builder
         */
        public VehicleProfile.Builder setVehicleNG(double ng) {
            this.ng = ng;
            return this;
        }

        /**
         * builds vehicle profile
         */
        public VehicleProfile build() {
            return new VehicleProfile(profileName, frontalArea, weight, maxSpeed, avgSpeed, crr, cw, nm, ng);
        }

    }

    public String getName() {
        return profile_name;
    }

    public double getFrontalArea() {
        return frontalArea;
    }

    public double getWeight() {
        return weight;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public double getCrr() {
        return crr;
    }

    public double getCw() {
        return cw;
    }

    public double getNm() {
        return nm;
    }

    public double getNg() {
        return ng;
    }

    @Override
    public String toString() {
        return "VehicleProfile{" +
            "profile_name ='" + profile_name + '\'' +
            ", frontalArea =" + frontalArea +
            ", weight =" + weight +
            ", Maximum Speed =" + maxSpeed +
            ", Average Speed =" + avgSpeed +
            ", Rolling Coefficient =" + crr +
            ", Air Drag Coefficient =" + cw +
            ", Mechanical Efficiency =" + nm +
            ", Gain Efficiency =" + ng +
            '}';
    }

    @Override
    public int hashCode() { // TODO: Add TC hashcode method
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Long.parseLong(profile_name);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VehicleProfile other = (VehicleProfile) obj;
        if (Double.doubleToLongBits(Long.parseLong(profile_name)) != Double.doubleToLongBits(Long.parseLong(other.profile_name)))
            return false;
        return true;
    }
}
