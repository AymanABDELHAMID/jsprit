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
    public static VehicleProfile newInstance(String profile_name) {
        return new VehicleProfile(profile_name);
    }
    /*
    The profile name should be one of the profiles recognized, if not, the vehicle profile is set to default values of a car profile
    To add your own profile, just add a file containing the profile data in the IO
     */

    private final String profile_name;


    private VehicleProfile(String profile_name) {
        super();
        this.profile_name = profile_name;
    }

    /**
     * Vehicle profile XML configuration
     */
    public class VehicleProfileConfig {

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

        private String profile_name;
        private Set<String> addedProfileNames = new LinkedHashSet<>();
        private List<VehicleProfile> vehicleProfiles = new ArrayList<>();

        /**
         * 29.04.21 Ayman M. Adding default profile values
         */
        private double frontalArea = 1.81;// in m^2

        private Builder(String profile_name) {
            this.profile_name = profile_name;
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
         * @author Ayman M.
         *
         * Setting profile only requires the name, the user has no access to the parameters of the profile
         * The profile name is then used to build an object that can be used to build an object (@link VehicleProfile)
         * @param profile_name
         * @return
         */

        public VehicleProfile.Builder setProfile(String profile_name) {
            this.profile_name = profile_name;
            return this;
        }

        /**
         * Adds a vehicle.
         *
         * @param profile vehicle to be added
         * @return this builder
         */
        public Builder addProfile(VehicleProfile profile) {
            if(addedProfileNames.contains(profile.getName())){
                throw new IllegalArgumentException("The vehicle Profile already has a similar profile for " + profile.getName() + ".");
            }
            else addedProfileNames.add(profile.getName());
            vehicleProfiles.add(profile);

            return this;
        }

        /**
         * builds vehicle profiles
         */
        public VehicleProfile build() {

            return new VehicleProfile(profile_name);
        }

    }

    private final Set<String> addedProfileNames = new LinkedHashSet<>();


    private VehicleProfile(VehicleProfile.Builder builder) {
       this.profile_name = builder.profile_name;
    }

    public String getName() {
        return profile_name;
    }

    @Override
    public String toString() {
        return "[Name=" + profile_name + "]";
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
