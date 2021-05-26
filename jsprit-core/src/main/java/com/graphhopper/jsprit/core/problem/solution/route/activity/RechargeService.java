package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Recharge;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.util.EnergyRecuperationCalculator;

public class RechargeService extends AbstractActivity implements RechargeActivity {

    private Service recharge;

    private double arrTime;

    private double depTime;

    private double theoreticalEarliest = 0;

    private double theoreticalLatest = Double.MAX_VALUE;

    public RechargeService(Recharge recharge) {
        super();
        this.recharge = recharge;
    }

    public RechargeService(Service service) {
        this.recharge = service;
    }

    /*
     * The TW of a recharge station is by default all the time from t = 0 to t = max value
     * But this gives us the opportunity to define stations that are open some part of the operation.
     */
    public RechargeService(RechargeService rechargeActivity ) {
        this.recharge = rechargeActivity.getJob();
        this.arrTime = rechargeActivity.getArrTime();
        this.depTime = rechargeActivity.getEndTime();
        setIndex(rechargeActivity.getIndex());
        this.theoreticalEarliest = rechargeActivity.getTheoreticalEarliestOperationStartTime();
        this.theoreticalLatest = rechargeActivity.getTheoreticalEarliestOperationStartTime();
    }

    public double getRechargeDuration(VehicleType type) {
        // TODO : Compare with when the constraint of capacity checks the remaining capacity
        return EnergyRecuperationCalculator.calculateRecuperationDuration(type, type.getBatteryDimensions());
    }

    @Override
    public void setTheoreticalEarliestOperationStartTime(double earliest) {
        this.theoreticalEarliest = earliest;
    }

    @Override
    public void setTheoreticalLatestOperationStartTime(double latest) {
        this.theoreticalLatest = latest;
    }

    @Override
    public Service getJob() {
        return recharge;
    }

    @Override
    public String getName() {
        return recharge.getType();
    }

    @Override
    public Location getLocation() {
        return recharge.getLocation();
    }

    @Override
    public double getTheoreticalEarliestOperationStartTime() {
        return this.theoreticalEarliest; // this.theoreticalEarliest
    }

    @Override
    public double getTheoreticalLatestOperationStartTime() {
        return this.theoreticalLatest;
    }

    @Override
    public double getOperationTime() {
        return recharge.getServiceDuration();
        //return this.getRechargeDuration(); // TODO : figure out how to insert vehicle type
        // Idea 1: insert by default vehicle type electric
    }

    @Override
    public double getArrTime() {
        return this.arrTime;
    }

    @Override
    public double getEndTime() {
        return this.depTime;
    }

    @Override
    public void setArrTime(double arrTime) {
        this.arrTime = arrTime;
    }

    @Override
    public void setEndTime(double endTime) {
        this.depTime = endTime;
    }

    @Override
    public Capacity getSize() { // or return a Capacity object with size 0
        return null;
    }

    @Override
    public TourActivity duplicate() {
        return new RechargeService(this);
    }

    @Override
    public String toString() {
        return "RechargeService{" +
            "recharge=" + recharge +
            ", arrTime=" + arrTime +
            ", depTime=" + depTime +
            '}';
    }
}
