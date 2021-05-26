package com.graphhopper.jsprit.core.problem.job;

public class Recharge extends Service {

    public static class Builder extends Service.Builder<Recharge> {

        /**
         * Returns a new instance of builder that builds a recharge.
         *
         * @param id the id of the pickup
         * @return the builder
         */
        public static Recharge.Builder newInstance(String id) {
            return new Recharge.Builder(id);
        }

        Builder(String id) {
            super(id);
        }

        public Recharge.Builder setMaxTimeInVehicle(double maxTimeInVehicle){
            throw new UnsupportedOperationException("maxTimeInVehicle is not yet supported for Pickups and Services (only for Deliveries and Shipments)");
//            if(maxTimeInVehicle < 0) throw new IllegalArgumentException("maxTimeInVehicle should be positive");
//            this.maxTimeInVehicle = maxTimeInVehicle;
//            return this;
        }

        /**
         * Builds recharge.
         * <p>
         * <p> Recharge type is "recharge"
         *
         * @return recharge
         * @throws IllegalArgumentException if neither locationId nor coordinate has been set
         */
        public Recharge build() {
            if (location == null) throw new IllegalArgumentException("location is missing");
            this.setType("recharge");
            super.battery = super.batteryBuilder.build();
            this.setPriority(2); // TODO: priority cannot be changed
            // TODO : make sure the vehicle type is not required here
            super.activity = new Activity.Builder(location, Activity.Type.RECHARGE).setTimeWindows(timeWindows.getTimeWindows()).setServiceTime(serviceTime).build();
            return new Recharge(this);
        }

    }

    Recharge(Recharge.Builder builder) {
        super(builder);
    }
}
