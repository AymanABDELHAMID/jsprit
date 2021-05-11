package com.graphhopper.jsprit.core.util;


import java.util.*;

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
    private List<VehicleProfile> vehicleProfiles = new ArrayList<>();
    /*
    As far as I understood, the following error;
    java.lang.NumberFormatException: For input string: "carIcev"
    is maybe because there are repeated characters, I am not sure.
    https://stackoverflow.com/questions/32503555/iterating-through-linkedhashset-with-switch-case-cause-a-matching-parameter-to-g
    I will try again but with using a List instead of a Set.
     */
    //private Set<VehicleProfile> vehicleProfiles = new LinkedHashSet<>();

    private Set<String> addedVehicleProfilesNames = new LinkedHashSet<>();
    // TODO: add default parameter maybe with a build factory

    public static class Builder {

        public static VehicleProfiles.Builder newInstance() {
            return new VehicleProfiles.Builder();
        }

        private List<VehicleProfile> vehicleProfiles = new ArrayList<>();

        private Set<String> addedVehicleProfilesNames = new LinkedHashSet<>();

        private  Map<String, VehicleProfile> vehicleProfilesMap = new HashMap<>();


        public Builder addProfile(VehicleProfile profile){
            if(addedVehicleProfilesNames.contains(profile.getName())){
                throw new IllegalArgumentException("The vehicle Profile already has a similar profile for " + profile.getName() + ".");
            }
            else addedVehicleProfilesNames.add(profile.getName());
            vehicleProfiles.add(profile);
            vehicleProfilesMap.put(profile.getName(), profile);
            return this;
        }

        /**
         * builds vehicle profiles
         */
        public VehicleProfiles build() {
            return new VehicleProfiles(vehicleProfiles, addedVehicleProfilesNames, vehicleProfilesMap);
        }

    }
    public void addProfile(VehicleProfile profile){
        // TODO: add in the builder the list.
        if(addedVehicleProfilesNames.contains(profile.getName())){
            throw new IllegalArgumentException("The vehicle Profile already has a similar profile for " + profile.getName() + ".");
        }
        else addedVehicleProfilesNames.add(profile.getName());
        vehicleProfiles.add(profile);
        vehicleProfilesMap.put(profile.getName(), profile);
    }

    public VehicleProfiles(List<VehicleProfile> vehicleProfiles,  Set<String> addedVehicleProfilesNames,  Map<String, VehicleProfile> vehicleProfilesMap) {
        this.vehicleProfiles = vehicleProfiles;
        this.addedVehicleProfilesNames = addedVehicleProfilesNames;
        this.vehicleProfilesMap = vehicleProfilesMap;
    }

    public List<VehicleProfile> getVehicleProfiles() {
        return vehicleProfiles;
    }

    public Set<String> getAddedVehicleProfilesNames() {
        return addedVehicleProfilesNames;
    }

    public Map<String, VehicleProfile> getVehicleProfilesMap() {
        return vehicleProfilesMap;
    }

    public  Map<String, VehicleProfile> vehicleProfilesMap = new HashMap<>();

    @Override
    public String toString() {
        return "VehicleProfiles{" +
            "vehicleProfiles=" + vehicleProfiles +
            ", addedVehicleProfilesNames=" + addedVehicleProfilesNames +
            '}';
    }

    // TODO: use same equals and hashCode approaches used by Schroder
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleProfiles)) return false;
        VehicleProfiles that = (VehicleProfiles) o;
        return getVehicleProfiles().equals(that.getVehicleProfiles()) &&
            getAddedVehicleProfilesNames().equals(that.getAddedVehicleProfilesNames());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVehicleProfiles(), getAddedVehicleProfilesNames());
    }
}
