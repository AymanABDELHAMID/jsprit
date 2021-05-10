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
package com.graphhopper.jsprit.core.problem.vehicle;


import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.BatteryAM;
import com.graphhopper.jsprit.core.util.VehicleProfile;
import com.graphhopper.jsprit.core.util.VehicleProfiles;

import java.io.InputStream;

/**
 * Implementation of {@link VehicleType}.
 * <p>
 * <p>Two vehicle-types are equal if they have the same typeId.
 *
 * @author schroeder
 */
public class VehicleTypeImpl implements VehicleType {

    /**
     * CostParameter consisting of fixed cost parameter, time-based cost parameter and distance-based cost parameter.
     * Ayman M.
     * Adding Cost parameters related to energy consumption
     *
     * @author schroeder
     */
    public static class VehicleCostParams {


        public static VehicleTypeImpl.VehicleCostParams newInstance(double fix, double perTimeUnit, double perDistanceUnit) {
            return new VehicleCostParams(fix, perTimeUnit, perDistanceUnit);
        }

        public final double fix;

        public final double perTransportTimeUnit;
        public final double perDistanceUnit;
        public final double perWaitingTimeUnit;
        public final double perServiceTimeUnit;
        /*
        Energy consumption cost parameters:
         */
        public final double perEnergyUnit_Battery;
        public final double perEnergyUnit_Fuel;

        private VehicleCostParams(double fix, double perTimeUnit, double perDistanceUnit) {
            super();
            this.fix = fix;
            this.perTransportTimeUnit = perTimeUnit;
            this.perDistanceUnit = perDistanceUnit;
            this.perWaitingTimeUnit = 0.;
            this.perServiceTimeUnit = 0.;
            this.perEnergyUnit_Battery = 1.;
            this.perEnergyUnit_Fuel = 1.;
        }

        public VehicleCostParams(double fix, double perTimeUnit, double perDistanceUnit, double perWaitingTimeUnit) {
            this.fix = fix;
            this.perTransportTimeUnit = perTimeUnit;
            this.perDistanceUnit = perDistanceUnit;
            this.perWaitingTimeUnit = perWaitingTimeUnit;
            this.perServiceTimeUnit = 0.;
            this.perEnergyUnit_Battery = 1.;
            this.perEnergyUnit_Fuel = 1.;
        }

        public VehicleCostParams(double fix, double perTimeUnit, double perDistanceUnit, double perWaitingTimeUnit, double perServiceTimeUnit) {
            this.fix = fix;
            this.perTransportTimeUnit = perTimeUnit;
            this.perDistanceUnit = perDistanceUnit;
            this.perWaitingTimeUnit = perWaitingTimeUnit;
            this.perServiceTimeUnit = perServiceTimeUnit;
            this.perEnergyUnit_Battery = 1.;
            this.perEnergyUnit_Fuel = 1.;
        }

        public VehicleCostParams(double fix, double perTimeUnit, double perDistanceUnit, double perWaitingTimeUnit, double perServiceTimeUnit, double perEnergyUnit_Battery) {
            this.fix = fix;
            this.perTransportTimeUnit = perTimeUnit;
            this.perDistanceUnit = perDistanceUnit;
            this.perWaitingTimeUnit = perWaitingTimeUnit;
            this.perServiceTimeUnit = perServiceTimeUnit;
            this.perEnergyUnit_Battery = perEnergyUnit_Battery;
            this.perEnergyUnit_Fuel = 1.;
        }

        public VehicleCostParams(double fix, double perTimeUnit, double perDistanceUnit, double perWaitingTimeUnit, double perServiceTimeUnit, double perEnergyUnit_Battery, double perEnergyUnit_Fuel) {
            this.fix = fix;
            this.perTransportTimeUnit = perTimeUnit;
            this.perDistanceUnit = perDistanceUnit;
            this.perWaitingTimeUnit = perWaitingTimeUnit;
            this.perServiceTimeUnit = perServiceTimeUnit;
            this.perEnergyUnit_Battery = perEnergyUnit_Battery;
            this.perEnergyUnit_Fuel = perEnergyUnit_Fuel;
        }

        @Override
        public String toString() {
            return "[fixed=" + fix + "][perTime=" + perTransportTimeUnit + "][perDistance=" + perDistanceUnit + "][perWaitingTimeUnit=" + perWaitingTimeUnit + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VehicleCostParams)) return false;

            VehicleCostParams that = (VehicleCostParams) o;

            if (Double.compare(that.fix, fix) != 0) return false;
            if (Double.compare(that.perTransportTimeUnit, perTransportTimeUnit) != 0) return false;
            if (Double.compare(that.perDistanceUnit, perDistanceUnit) != 0) return false;
            if (Double.compare(that.perWaitingTimeUnit, perWaitingTimeUnit) != 0) return false;
            return Double.compare(that.perServiceTimeUnit, perServiceTimeUnit) == 0;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(fix);
            result = (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(perTransportTimeUnit);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(perDistanceUnit);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(perWaitingTimeUnit);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(perServiceTimeUnit);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(perEnergyUnit_Battery);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(perEnergyUnit_Fuel);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }

    /**
     * Builder that builds the vehicle-type.
     *
     * @author schroeder
     */
    public static class Builder {


        public static VehicleTypeImpl.Builder newInstance(String id) {
            if (id == null) throw new IllegalArgumentException();
            return new Builder(id);
        }

        private String id;
        /**
         * 29.04.21 Ayman M. Maximum Velocity value changed to a logical value. 90km/h = 25 m/s
         */
        private double maxVelo = 25;// Double.MAX_VALUE;
        /*
        Setting average velocity to 50 km/h ~= 13 m/s
         */
        private double avgVelo = 13;
        /**
         * default cost values for default vehicle type
         */
        private double fixedCost = 0.0;
        private double perDistance = 1.0;
        private double perTime = 0.0;
        private double perWaitingTime = 0.0;
        private double perServiceTime = 0.0;
        /**
         * @author: Ayman M.
         * Energy Specific cost parameters
         */
        private double perEnergyUnit_Battery = 0.0;
        private double perEnergyUnit_Fuel = 0.0;

        /**
         * @author Ayman M.
         * Adding energy_type
         * 1: Fuel - 2: Battery Electric - 3: Hybrid (TODO: add mode selection attribute)
         */
        private int energy_type = 1;

        /**
         * @author: profile is transformed into an object
         *
         */
        private String profile_name = "car";

        private VehicleProfile profile = VehicleProfile.Builder.newInstance(profile_name).build(); //VehicleProfile.newInstance(profile_name); // TODO: fix the error

        private Capacity.Builder capacityBuilder = Capacity.Builder.newInstance();

        private Capacity capacityDimensions = null;


        /**
         * Ayman Mahmoud 06/02/2021
         * Integrating Battery into vehicle type
         */
        private BatteryAM.Builder batteryBuilder = BatteryAM.Builder.newInstance();

        private BatteryAM BatteryDimensions = null;

        private boolean dimensionAdded = false;

        private Object userData;

        private Builder(String id) {
            this.id = id;
        }


        /**
         * Sets user specific domain data associated with the object.
         *
         * <p>
         * The user data is a black box for the framework, it only stores it,
         * but never interacts with it in any way.
         * </p>
         *
         * @param userData
         *            any object holding the domain specific user data
         *            associated with the object.
         * @return builder
         */
        public Builder setUserData(Object userData) {
            this.userData = userData;
            return this;
        }

        /**
         * Sets the maximum velocity this vehicle-type can go [in meter per
         * seconds].
         *
         * @param inMeterPerSeconds in m/s
         * @return this builder
         * @throws IllegalArgumentException
         *             if velocity is smaller than zero
         */
        public VehicleTypeImpl.Builder setMaxVelocity(double inMeterPerSeconds) {
            if (inMeterPerSeconds < 0.0)
                throw new IllegalArgumentException("The velocity of a vehicle (type) cannot be smaller than zero.");
            this.maxVelo = inMeterPerSeconds;
            return this;
        }

        /**
         * Sets the maximum velocity this vehicle-type can go [in meter per
         * seconds].
         *
         * @author Ayman M.
         *
         * @param inMeterPerSeconds in m/s
         * @return this builder
         * @throws IllegalArgumentException
         *             if velocity is smaller than zero
         */
        public VehicleTypeImpl.Builder setAverageVelocity(double inMeterPerSeconds) {
            if (inMeterPerSeconds < 0.0)
                throw new IllegalArgumentException("The velocity of a vehicle (type) cannot be smaller than zero.");
            this.avgVelo = inMeterPerSeconds;
            return this;
        }

        /**
         * Sets the fixed costs of the vehicle-type.
         * <p>
         * <p>by default it is 0.
         *
         * @param fixedCost fixed cost of vehicle type
         * @return this builder
         * @throws IllegalArgumentException if fixedCost is smaller than zero
         */
        public VehicleTypeImpl.Builder setFixedCost(double fixedCost) {
            if (fixedCost < 0.0) throw new IllegalArgumentException("Fixed costs must not be smaller than zero.");
            this.fixedCost = fixedCost;
            return this;
        }

        /**
         * Sets the cost per distance unit, for instance € per meter.
         * <p>
         * <p>by default it is 1.0
         *
         * @param perDistance cost per distance
         * @return this builder
         * @throws IllegalArgumentException if perDistance is smaller than zero
         */
        public VehicleTypeImpl.Builder setCostPerDistance(double perDistance) {
            if (perDistance < 0.0)
                throw new IllegalArgumentException("Cost per distance must not be smaller than zero.");
            this.perDistance = perDistance;
            return this;
        }

        /**
         * Sets cost per time unit, for instance € per second.
         * <p>
         * <p>by default it is 0.0
         *
         * @param perTime cost per time
         * @return this builder
         * @throws IllegalArgumentException if costPerTime is smaller than zero
         * @deprecated use .setCostPerTransportTime(..) instead
         */
        @Deprecated
        public VehicleTypeImpl.Builder setCostPerTime(double perTime) {
            if (perTime < 0.0) throw new IllegalArgumentException();
            this.perTime = perTime;
            return this;
        }

        /**
         * Sets cost per time unit, for instance € per second.
         * <p>
         * <p>by default it is 0.0
         *
         * @param perTime cost per time
         * @return this builder
         * @throws IllegalArgumentException if costPerTime is smaller than zero
         */
        public VehicleTypeImpl.Builder setCostPerTransportTime(double perTime) {
            if (perTime < 0.0) throw new IllegalArgumentException();
            this.perTime = perTime;
            return this;
        }

        /**
         * Sets cost per waiting time unit, for instance € per second.
         * <p>
         * <p>by default it is 0.0
         *
         * @param perWaitingTime cost per waiting time
         * @return this builder
         * @throws IllegalArgumentException if costPerTime is smaller than zero
         */
        public VehicleTypeImpl.Builder setCostPerWaitingTime(double perWaitingTime) {
            if (perWaitingTime < 0.0) throw new IllegalArgumentException();
            this.perWaitingTime = perWaitingTime;
            return this;
        }

        public VehicleTypeImpl.Builder setCostPerServiceTime(double perServiceTime) {
            this.perServiceTime = perServiceTime;
            return this;
        }

        /**
         * sets battery propulsion cost
         * // TODO: add a cost parameter for recuperation cost
         * <p>
         * <p>by default it is 1.0
         *
         * @param perEnergyUnit_Battery cost per energy unit
         * @return this builder
         * @throws IllegalArgumentException if costPerTime is smaller than zero
         */
        public VehicleTypeImpl.Builder setCostPerEnergyUnit_Battery(double perEnergyUnit_Battery) {
            if (perEnergyUnit_Battery < 0.0) throw new IllegalArgumentException();
            this.perEnergyUnit_Battery = perEnergyUnit_Battery;
            return this;
        }

        /**
         * sets fuel propulsion cost
         *
         * <p>
         * <p>by default it is 1.0
         *
         * @param perEnergyUnit_Fuel cost per energy unit
         * @return this builder
         * @throws IllegalArgumentException if costPerTime is smaller than zero
         */
        public VehicleTypeImpl.Builder setCostPerEnergyUnit_Fuel(double perEnergyUnit_Fuel) {
            if (perEnergyUnit_Fuel < 0.0) throw new IllegalArgumentException();
            this.perEnergyUnit_Fuel = perEnergyUnit_Fuel;
            return this;
        }

        /**
         * Builds the vehicle-type.
         *
         * @return VehicleTypeImpl
         */
        public VehicleTypeImpl build() {
            if (capacityDimensions == null) {
                capacityDimensions = capacityBuilder.build();
            }
            /**
             * Ayman adding battery
             */
            if (BatteryDimensions == null) {
                BatteryDimensions = batteryBuilder.build();
            }
            return new VehicleTypeImpl(this);
        }

        /**
         * Adds a capacity dimension.
         *
         * @param dimIndex dimension index
         * @param dimVal dimension value
         * @return the builder
         * @throws IllegalArgumentException if dimVal < 0
         * @throws IllegalArgumentException    if capacity dimension is already set
         */
        public Builder addCapacityDimension(int dimIndex, int dimVal) {
            if (dimVal < 0) throw new IllegalArgumentException("The capacity value must not be negative.");
            if (capacityDimensions != null)
                throw new IllegalArgumentException("Either build your dimension with build your dimensions with " +
                    "addCapacityDimension(int dimIndex, int dimVal) or set the already built dimensions with .setCapacityDimensions(Capacity capacity)." +
                    "You used both methods.");
            dimensionAdded = true;
            capacityBuilder.addDimension(dimIndex, dimVal);
            return this;
        }

        /**
         * Sets capacity dimensions.
         * <p>
         * <p>Note if you use this you cannot use <code>addCapacityDimension(int dimIndex, int dimVal)</code> anymore. Thus either build
         * your dimensions with <code>addCapacityDimension(int dimIndex, int dimVal)</code> or set the already built dimensions with
         * this method.
         *
         * @param capacity capacity of vehicle type
         * @return this builder
         * @throws IllegalArgumentException if capacityDimension has already been added
         */
        public Builder setCapacityDimensions(Capacity capacity) {
            if (dimensionAdded)
                throw new IllegalArgumentException("Either build your dimension with build your dimensions with " +
                    "addCapacityDimension(int dimIndex, int dimVal) or set the already built dimensions with .setCapacityDimensions(Capacity capacity)." +
                    "You used both methods.");
            this.capacityDimensions = capacity;
            return this;
        }

        /**
         * Ayman 08/02
         * add and set battery dimensions
         */

        /**
         * Adds a Battery dimension.
         *
         * @param dimIndex dimension index
         * @param dimVal dimension value
         * @return the builder
         * @throws IllegalArgumentException if dimVal < 0
         * @throws IllegalArgumentException    if battery dimension is already set
         */
        public Builder addBatteryDimension(int dimIndex, int dimVal) {
            if (dimVal < 0) throw new IllegalArgumentException("The battery value must not be negative.");
            if (BatteryDimensions != null)
                throw new IllegalArgumentException("Either build your dimension with build your dimensions with " +
                    "addBatteryDimension(int dimIndex, int dimVal) or set the already built dimensions with .setBatteryDimensions(BatteryAM battery)." +
                    "You used both methods.");
            dimensionAdded = true;
            batteryBuilder.addDimension(dimIndex, dimVal);
            return this;
        }

        /**
         * Sets battery dimensions.
         * <p>
         * <p>Note if you use this you cannot use <code>addBatteryDimension(int dimIndex, int dimVal)</code> anymore. Thus either build
         * your dimensions with <code>addBatteryDimension(int dimIndex, int dimVal)</code> or set the already built dimensions with
         * this method.
         *
         * @param battery battery of vehicle type
         * @return this builder
         * @throws IllegalArgumentException if batteryDimension has already been added
         */
        public Builder setBatteryDimensions(BatteryAM battery) {
            if (dimensionAdded)
                throw new IllegalArgumentException("Either build your dimension with build your dimensions with " +
                    "addBatteryDimension(int dimIndex, int dimVal) or set the already built dimensions with .setBatteryDimensions(BatteryAM battery)." +
                    "You used both methods.");
            this.BatteryDimensions = battery;
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

        public Builder setProfile(String profile_name) {
            // TODO: Ensure that you don't need to instantiate the profile here
            this.profile_name = profile_name;
            return this;
        }

        public Builder buildProfile(VehicleProfile profile){
            this.profile = profile;
            return this;
        }

        public Builder setEnergyType(int energy_type) {
            if (energy_type < 0 || energy_type > 3) throw new IllegalArgumentException("The energy type value must be one of the following: 1 (Fuel), 2 (Battery Electric), 3 (Hybrid)");
            this.energy_type = energy_type;
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VehicleTypeImpl)) return false;

        VehicleTypeImpl that = (VehicleTypeImpl) o;

        if (Double.compare(that.maxVelocity, maxVelocity) != 0) return false;
        if (Double.compare(that.avgVelocity, avgVelocity) != 0) return false;
        if (!typeId.equals(that.typeId)) return false;
        if (profile_name != null ? !profile_name.equals(that.profile_name) : that.profile_name != null) return false;
        if (!vehicleCostParams.equals(that.vehicleCostParams)) return false;
        /**
         * Ayman 09/02
         */
        if (!BatteryDimensions.equals(that.BatteryDimensions)) return false;
        return capacityDimensions.equals(that.capacityDimensions);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = typeId.hashCode();
        result = 31 * result + (profile_name != null ? profile_name.hashCode() : 0);
        result = 31 * result + vehicleCostParams.hashCode();
        result = 31 * result + capacityDimensions.hashCode();
        result = 31 * result + BatteryDimensions.hashCode();
        temp = Double.doubleToLongBits(maxVelocity);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(avgVelocity);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    private final String typeId;

    private final String profile_name;

    private final VehicleProfile profile;

    private final VehicleTypeImpl.VehicleCostParams vehicleCostParams;

    private final Capacity capacityDimensions;

    /**
     * Ayman 08/02
     */

    private final BatteryAM BatteryDimensions;

    private final double maxVelocity;
    private final double avgVelocity;

    private final int energy_type;

    private Object userData;

    /**
     * priv constructor constructing vehicle-type
     *
     * @param builder vehicle type builder
     */
    private VehicleTypeImpl(VehicleTypeImpl.Builder builder) {
        this.userData = builder.userData;
        typeId = builder.id;
        maxVelocity = builder.maxVelo;
        avgVelocity = builder.avgVelo;
        vehicleCostParams = new VehicleCostParams(builder.fixedCost, builder.perTime, builder.perDistance, builder.perWaitingTime, builder.perServiceTime);
        capacityDimensions = builder.capacityDimensions;
        /**
         * Ayman 08/02
         */
        BatteryDimensions = builder.BatteryDimensions;
        profile_name = builder.profile_name;
        profile = builder.profile;
        energy_type = builder.energy_type;
    }

    /**
     * @return User-specific domain data associated with the vehicle
     */
    @Override
    public Object getUserData() {
        return userData;
    }

    /* (non-Javadoc)
     * @see basics.route.VehicleType#getTypeId()
     */
    @Override
    public String getTypeId() {
        return typeId;
    }

    /* (non-Javadoc)
     * @see basics.route.VehicleType#getVehicleCostParams()
     */
    @Override
    public VehicleTypeImpl.VehicleCostParams getVehicleCostParams() {
        return vehicleCostParams;
    }

    @Override
    public String toString() {
        return "[typeId=" + typeId + "]" +
            "[capacity=" + capacityDimensions + "]" +
            "[battery=" + BatteryDimensions + "]" +
            "[costs=" + vehicleCostParams + "]";
    }

    @Override
    public double getMaxVelocity() {
        return maxVelocity;
    }

    @Override
    public double getAverageVelocity() {
        return avgVelocity;
    }

    @Override
    public Capacity getCapacityDimensions() {
        return capacityDimensions;
    }

    /**
     * Ayman 08/02
     * error: Method does not override method from its superclass
     */


    @Override
    public BatteryAM getBatteryDimensions() {
        return BatteryDimensions;
    }

    @Override
    public VehicleProfile getProfile() {
        return profile;
    }

    @Override
    public String getProfileName() {
        return profile_name;
    }

    @Override
    public int getEnergyType() {
        // getEnergyType().getCoefficient()
        // TODO:
        return energy_type;
    }

    public String getEnergyType_String() {
        String[] energy_types = {"FUEL", "BATTERY ELECTRIC", "HYBRID"};
        return energy_types[energy_type];
    }
}
