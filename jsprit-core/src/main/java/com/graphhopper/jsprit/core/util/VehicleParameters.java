package com.graphhopper.jsprit.core.util;

/*
    Note to self: maybe parameters are not needed
 */

public class VehicleParameters {

    public static VehicleParameters newInstance(String type) {
        return new VehicleParameters(type);
    } // Location.Builder.newInstance().setIndex(index).build();

    private final String type;
    //private final double weight;
    //private final double area;

    public VehicleParameters(String type) {
        super();
        this.type = type;
        //this.weight = weight;
        //this.area = area;
    }

    public String getType() {
        return type;
    }

    //public double getWeight() { return weight; }
    //public double getFrontalArea() { return area; }

    @Override
    public String toString() {
        return "[Vehicle Type=" + type + "][Vehicle Estimated Mass=" + type + "]";
    } // TODO: Change to profile

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Long.parseLong(type);
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
        VehicleParameters other = (VehicleParameters) obj;
        if (Double.doubleToLongBits(Long.parseLong(type)) != Double.doubleToLongBits(Long.parseLong(other.type)))
            return false;
        return true;
    }
}
