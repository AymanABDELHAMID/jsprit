package com.graphhopper.jsprit.core.problem;


import java.util.Arrays;

/**
 * @author Ayman
 * Introducing the Battery Class to the vehicle.
 * The battery has the following dimensions:
 * (1) - SoC (State of charge)
 * (2) - Range (Maximum distance)
 * (3) - The SoC can either increase in the case of RECHARGING
 *                          decrease in the case of DRIVING
 */

public class BatteryAM {

    /**
     * Adds up two ranges, i.e. sums up each and every range, and returns the resulting new Range.
     *
     *
     * @param range1 existing battery range
     * @param range2 new range to be added up
     * @return new BatteryAM
     * @throws NullPointerException if one of the args is null
     */

    public static BatteryAM addRange(BatteryAM range1, BatteryAM range2) {
        if (range1 == null || range2 == null) throw new NullPointerException("arguments must not be null");
        BatteryAM.Builder BatteryAMBuilder = BatteryAM.Builder.newInstance();
        for (int i = 0; i < Math.max(range1.getNuOfDimensions(), range2.getNuOfDimensions()); i++) { // for now the dimension should be equal to one
            BatteryAMBuilder.addDimension(i, range1.get(i) + range2.get(i));
        }
        return BatteryAMBuilder.build();
    }

    /**
     * Subtracts range2Subtract from range and returns the resulting new range.
     *
     * @param range          range to be subtracted from
     * @param range2subtract range to subtract
     * @return new range
     * @throws NullPointerException  if one of the args is null
     * @throws IllegalStateException if number of RangeDimensions are different (i.e. <code>cap1.getNuOfDimension() != cap2.getNuOfDimension()</code>).
     * @throws Exception if range2Subtract is larger than current range.
     */
    public static BatteryAM subtractRange(BatteryAM range, BatteryAM range2subtract) {
        if (range == null || range2subtract == null) throw new NullPointerException("arguments must not be null");
        // TODO : modify here if the range you need to modify will be negative before inserting the charging station.
        if (range.isLessOrEqual(range2subtract)) throw new IllegalStateException("The battery range will not fulfill the trip");
        BatteryAM.Builder BatteryAMBuilder = BatteryAM.Builder.newInstance();
        for (int i = 0; i < Math.max(range.getNuOfDimensions(), range2subtract.getNuOfDimensions()); i++) {
            double dimValue = range.get(i) - range2subtract.get(i);
            BatteryAMBuilder.addDimension(i, dimValue);
        }
        return BatteryAMBuilder.build();
    }


    /**
     * Makes a deep copy of Capacity.
     *
     * @param batteryAM range to be copied
     * @return copy
     */
    public static BatteryAM copyOf(BatteryAM batteryAM) {
        if (batteryAM == null) return null;
        return new BatteryAM(batteryAM);
    }

/**
 * Builder that builds Battery
 *
 * @author Ayman
 */
public static class Builder {

    /**
     * default is 1 dimension with size of zero
     */
    private double[] dimensions = new double[1];

    /**
     * Returns a new instance of BatteryAM with one dimension and a value/size of 0
     *
     * @return this builder
     */
    public static BatteryAM.Builder newInstance() {
        return new BatteryAM.Builder();
    }

    Builder() {

    }

    /**
     * add BatteryAM dimension
     * <p>
     * <p>Note that it automatically resizes dimensions according to index, i.e. if index=7 there are 8 dimensions.
     * New dimensions then are initialized with 0
     *
     * @param index    dimensionIndex
     * @param dimValue dimensionValue
     * @return this builder
     */
    public BatteryAM.Builder addDimension(int index, double dimValue) {
        if (index < dimensions.length) {
            dimensions[index] = dimValue;
        } else {
            int requiredSize = index + 1;
            double[] newDimensions = new double[requiredSize];
            copy(dimensions, newDimensions);
            newDimensions[index] = dimValue;
            this.dimensions = newDimensions;
        }
        return this;
    }

    private void copy(double[] from, double[] to) {
        System.arraycopy(from, 0, to, 0, dimensions.length);
    }

    /**
     * Builds an immutable BatteryAM and returns it.
     *
     * @return BatteryAM
     */
    public BatteryAM build() {
        return new BatteryAM(this);
    }


}
    private double[] dimensions;
    private double[] maxRange;

    /**
     * copy constructor
     *
     * @param batteryAM capacity to be copied
     */
    private BatteryAM(BatteryAM batteryAM) {
        this.dimensions = new double[batteryAM.getNuOfDimensions()];
        this.maxRange = new double[batteryAM.getNuOfDimensions()];
        for (int i = 0; i < batteryAM.getNuOfDimensions(); i++) {
            this.dimensions[i] = batteryAM.get(i);
            this.maxRange[i] = this.dimensions[i];
        }
    }

    private BatteryAM(BatteryAM.Builder builder) {
        this.dimensions = builder.dimensions;
        this.maxRange = new double[this.dimensions.length];
        for (int i = 0; i < this.maxRange.length; i++) {
            this.maxRange[i] = this.dimensions[i];
        }
    }


    /**
     * Returns the number of specified battery dimensions. (what are the battery dimensions)
     *
     * @return noDimensions
     */
    public int getNuOfDimensions() {
        return dimensions.length;
    }

    /**
     * Returns value of range-dimension with specified index.
     * <p>
     * <p>If range dimension does not exist, it returns 0 (rather than IndexOutOfBoundsException).
     *
     * @param index dimension index of the range value to be retrieved
     * @return the according dimension value
     */
    public double get(int index) {
        if (index < dimensions.length) return dimensions[index];
        return 0;
    }

    /**
     * Returns value of range-dimension with specified index percentage.
     * <p>
     * <p>If range dimension does not exist, it returns 0 (rather than IndexOutOfBoundsException).
     *
     * @param index dimension index of the range value to be retrieved
     * @return the according dimension value
     */
    public double getSoCPercentage(int index) {
        if (index < dimensions.length) return (dimensions[index] / maxRange[index])*100;
        return 0;
    }

    /**
     * Returns value of range-dimension with specified index.
     * <p>
     * <p>If range dimension does not exist, it returns 0 (rather than IndexOutOfBoundsException).
     *
     * @param index dimension index of the range value to be retrieved
     * @return the according dimension value
     */
    public double getSoC(int index) {
        if (index < dimensions.length) return dimensions[index];
        return 0;
    }

    /**
     * Returns value of range-dimension with specified index.
     * <p>
     * <p>If range dimension does not exist, it returns 0 (rather than IndexOutOfBoundsException).
     *
     * @param index dimension index of the range value to be retrieved
     * @return the according dimension value
     */
    public double getRange(int index) {
        if (index < dimensions.length) return maxRange[index];
        return 0;
    }

    /**
     * Returns true if this range2subract is Greater or equal than the capacity toCompare.
     *
     * @param toCompare the capacity to compare
     * @return true if this capacity is less or equal than toCompare
     * @throws NullPointerException if one of the args is null
     */
    public boolean isGreaterOrEqual(BatteryAM toCompare) {
        if (toCompare == null) throw new NullPointerException();
        for (int i = 0; i < this.getNuOfDimensions(); i++) {
            if (this.get(i) < toCompare.get(i)) return false;
        }
        return true;
    }

    /**
     * Returns true if this range2subract is less or equal than the capacity toCompare
     */
    public boolean isLessOrEqual(BatteryAM toCompare) {
        if (toCompare == null) throw new NullPointerException();
        for (int i = 0; i < this.getNuOfDimensions(); i++) {
            if (this.get(i) > toCompare.get(i)) return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BatteryAM)) return false;

        BatteryAM batteryAM = (BatteryAM) o;

        return Arrays.equals(dimensions, batteryAM.dimensions);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(dimensions);
    }


    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("[noDimensions=" + getNuOfDimensions() + "]");
        for (int i = 0; i < getNuOfDimensions(); i++) {
            string.append("[[dimIndex=").append(i).append("][Max Range=").append(dimensions[i]).append(" kW]]");
        }
        return string.toString();
    }

}
