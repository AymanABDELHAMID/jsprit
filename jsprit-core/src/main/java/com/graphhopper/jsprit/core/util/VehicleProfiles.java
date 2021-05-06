package com.graphhopper.jsprit.core.util;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Ayman M.
 * A class container of vehicle profiles
 */
// TODO: check out abstract vehicle profile
public class VehicleProfiles {
    /*public static VehicleProfiles newInstance() {
        return new VehicleProfiles();
    }*/
    /**
     * Searching entire ArrayList every time you need to insert something will be performance killer.
     * Using Linkedhashset instead.
     */
    //private List<VehicleProfile> vehicleProfiles = new ArrayList<>();
    private Set<VehicleProfile> vehicleProfiles = new LinkedHashSet<>();

    private Set<String> addedVehicleProfilesNames = new LinkedHashSet<>();
    // TODO: add default parameter maybe with a build factory

    public static class Builder {

        public static VehicleProfiles.Builder newInstance() {
            return new VehicleProfiles.Builder();
        }

        private Set<VehicleProfile> vehicleProfiles = new LinkedHashSet<>();

        private Set<String> addedVehicleProfilesNames = new LinkedHashSet<>();

        public void addProfile(VehicleProfile profile){
            if(addedVehicleProfilesNames.contains(profile.getName())){
                throw new IllegalArgumentException("The vehicle Profile already has a similar profile for " + profile.getName() + ".");
            }
            vehicleProfiles.add(profile);
        }

        /**
         * builds vehicle profiles
         */
        public VehicleProfiles build() {
            return new VehicleProfiles(vehicleProfiles);
        }

    }
    public void addProfile(VehicleProfile profile){
        // TODO: add in the builder the list.
        if(addedVehicleProfilesNames.contains(profile.getName())){
            throw new IllegalArgumentException("The vehicle Profile already has a similar profile for " + profile.getName() + ".");
        }
        vehicleProfiles.add(profile);
    }

    public VehicleProfiles(Set<VehicleProfile> vehicleProfiles) {
        this.vehicleProfiles = vehicleProfiles;
    }
}
