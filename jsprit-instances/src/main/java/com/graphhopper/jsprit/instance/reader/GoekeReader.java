package com.graphhopper.jsprit.instance.reader;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Recharge;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.VehicleProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ayman M.
 *
 * Instance Reader for Goeke et al. 2018
 * // TODO (1) : add link to the article
 * // TODO (2) : add link to download instances
 *
 * TODO : See how instances in utils can be used in benchmarking (check task D8.3 in the plan)
 */

public class GoekeReader implements evrpReader {

    // Load (positive at pickup, negative at delivery)

    private int fixedCosts;

    /**
     * @param costProjectionFactor the costProjectionFactor to set
     */
    public void setVariableCostProjectionFactor(double costProjectionFactor) {
        this.variableCostProjectionFactor = costProjectionFactor;
    }

    public void setCoordProjectionFactor(double coordProjectionFactor) {
        this.coordProjectionFactor = coordProjectionFactor;
    }

    private static Logger logger = LoggerFactory.getLogger(BelhaizaReader.class);

    private final VehicleRoutingProblem.Builder vrpBuilder;

    private double coordProjectionFactor = 1;

    private double timeProjectionFactor = 1;

    private double variableCostProjectionFactor = 1;

    private double fixedCostPerVehicle = 0.0;

    private double batteryConsumptionRate = 1.0;

    public GoekeReader(VehicleRoutingProblem.Builder vrpBuilder) {
        this.vrpBuilder = vrpBuilder;
    }

    public GoekeReader(VehicleRoutingProblem.Builder vrpBuilder, double fixedCostPerVehicle) {
        this.vrpBuilder = vrpBuilder;
        this.fixedCostPerVehicle = fixedCostPerVehicle;
    }

    // Transform methods in Read into the below 4 methods

    @Override
    public void addService(VehicleRoutingProblem vrp) {

    }

    @Override
    public void addPickup(VehicleRoutingProblem vrp) {

    }

    @Override
    public void addDelivery(VehicleRoutingProblem vrp) {

    }

    @Override
    public void addRechargeStation(VehicleRoutingProblem vrp) {

    }

    @Override
    public void read(String goekeFile) {
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE); // TODO: double check if there is a fleet size in the article
        BufferedReader reader = getReader(goekeFile);
        VehicleTypeImpl.Builder typeBuilder;
        typeBuilder = VehicleTypeImpl.Builder.newInstance("BEV");
        int demand;
        int nOfCustomers = 0;
        int nOfRechargeStations = 0;
        int nOfDepots = 0;
        int counter = 0;
        double start, end, serviceTime;
        Coordinate startLocation = new Coordinate(0,0); // TODO: test if the value changes
        Service service;
        Map<String, Service> serviceMap = new LinkedHashMap<String, Service>();
        Map<String, Shipment> shipmentMap =  new LinkedHashMap<String, Shipment>();
        Map<String, String> partnerMap =  new LinkedHashMap<String, String>();
        String line;
        List<List<VehicleImpl.Builder>> vehiclesAtDepot = new ArrayList<List<VehicleImpl.Builder>>();
        while (((line = readLine(reader)) != null) && counter <= 500) {
            line = line.replace("\r", "");
            line = line.trim();
            String[] tokens = line.split("\\s+"); // transforms terms in the lines into an array of strings
            if (counter == 0) {
                // Skipping first line
                counter++;
            } else if (counter >= 1) {
                String id = tokens[0].trim();
                switch (tokens[1]) {
                    case "d":
                        // Maybe this code is not perfect in case of multiple depots, but in case of multiple depots we
                        // can get inspired by (@link CordeauReader)
                        Coordinate depotCoord = makeCoord(tokens[2].trim(), tokens[3].trim());
                        startLocation = depotCoord;
                        nOfDepots++;
                        break;
                    case "f":
                        Coordinate rechargeStationCoord =  makeCoord(tokens[2].trim(), tokens[3].trim());
                        start = Double.parseDouble(tokens[4]) * timeProjectionFactor;
                        end = Double.parseDouble(tokens[5]) * timeProjectionFactor;
                        Recharge recharge = Recharge.Builder.newInstance(id).addTimeWindow(start, end).setServiceTime(0d)
                            .setLocation(Location.Builder.newInstance().setId(id).setCoordinate(rechargeStationCoord).build()).build();
                        vrpBuilder.addJob(recharge);
                        nOfRechargeStations++;
                        break;
                    case "cp":
                        Coordinate pickupCoord = makeCoord(tokens[2].trim(), tokens[3].trim());
                        start = Double.parseDouble(tokens[4]) * timeProjectionFactor;
                        end = Double.parseDouble(tokens[5]) * timeProjectionFactor;
                        serviceTime = Double.parseDouble(tokens[3].trim());
                        demand = Integer.parseInt(tokens[4].trim());
                        service = Service.Builder.newInstance(id).addSizeDimension(0, demand).setServiceTime(serviceTime)
                            .setLocation(Location.Builder.newInstance().setId(id).setCoordinate(pickupCoord).build()).build();
                        vrpBuilder.addJob(service);
                        serviceMap.put(id, service);
                        partnerMap.put(id, tokens[8]);
                        nOfCustomers++;
                        break;
                    case "cd":
                        Coordinate deliveryCoord = makeCoord(tokens[2].trim(), tokens[3].trim());
                        start = Double.parseDouble(tokens[4]) * timeProjectionFactor;
                        end = Double.parseDouble(tokens[5]) * timeProjectionFactor;
                        serviceTime = Double.parseDouble(tokens[7].trim());
                        demand = Integer.parseInt(tokens[4].trim());
                        service = Service.Builder.newInstance(id).addSizeDimension(0, demand).setServiceTime(serviceTime)
                            .setLocation(Location.Builder.newInstance().setId(id).setCoordinate(deliveryCoord).build()).build();
                        vrpBuilder.addJob(service);
                        serviceMap.put(id, service);
                        //partnerMap.put(id, tokens[8]); // not needed since every pickup must have a delivery
                        // TODO: make sure that there are no instances with multiple pickups and one delivery
                        nOfCustomers++;
                        break;
                }
            } else if (counter >= (nOfCustomers + nOfRechargeStations + nOfDepots + 1)) {
                counter++; // skipping the empty line in the middle
            }
            else if (counter >= (nOfCustomers + nOfRechargeStations + nOfDepots + 2)) {
                // TODO : choose consumption calculator based on consumption model in the article
                /*
                    Vehicle battery capacity : 77.75
                    Vehicle freight capacity : 200.0
                    battery consumption rate : 1.0
                    inverse recharging rate : 3.47
                    average velocity : 1.0
                 */
                typeBuilder.setEnergyType(2);
                VehicleProfile.Builder profileBuilder = VehicleProfile.Builder.newInstance("BEV");
                switch (counter - (nOfCustomers + nOfRechargeStations + nOfDepots + 2)){
                    case 1:
                        // Battery Range
                        // TODO: change battery dimensions from int to double
                        typeBuilder.addBatteryDimension(0, Integer.parseInt(tokens[5].trim()));
                        break;
                    case 2:
                        // Vehicle cargo capacity
                        typeBuilder.addCapacityDimension(0,Integer.parseInt(tokens[5].trim()));
                        break;
                    case 3:
                        // Vehicle consumption rate
                        // if the consumption rate is (1) then use the consumption model consumption rate
                        if (Integer.parseInt(tokens[5].trim()) != 1){
                            profileBuilder.setVehicleNM( Double.parseDouble(tokens[5].trim()));
                    }
                        break;
                    case 4:
                        // Vehicle recharge rate
                        profileBuilder.setVehicleNG( Double.parseDouble(tokens[5].trim()));
                        break;
                    case 5:
                        // Average velocity here has to do with time windows, it means each time
                        // unit is equivalent to one distance unit.
                        // TODO: ask whether I need to change it in the average velocity in the consumption model
                        typeBuilder.setAverageVelocity( Double.parseDouble(tokens[5].trim()) );
                        break;
                    case 6:
                        // Finished reading the file
                        counter = 500;
                        break;
                }
                VehicleProfile profile = profileBuilder.build();
                typeBuilder.setProfile("BEV").buildProfile(profile);
                    // breaking the while loop
                counter++;
            }
            counter++;
        }
        VehicleImpl.Builder vBuilder = VehicleImpl.Builder.newInstance("Goeke-vehicle");
        vBuilder.setStartLocation(Location.newInstance(startLocation.getX(), startLocation.getY()));
        VehicleImpl vehicle = vBuilder.build();
        vrpBuilder.addVehicle(vehicle);
        close(reader);
        // Building shipments

    }

    private String readLine(BufferedReader reader) {
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.toString());
            System.exit(1);
            return null;
        }
    }

    private BufferedReader getReader(String solomonFile) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(solomonFile));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            logger.error(e1.toString());
            System.exit(1);
        }
        return reader;
    }

    private void close(BufferedReader reader)  {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.toString());
            System.exit(1);
        }
    }

    private Coordinate makeCoord(String xString, String yString) {
        double x = Double.parseDouble(xString);
        double y = Double.parseDouble(yString);
        return new Coordinate(x*coordProjectionFactor,y*coordProjectionFactor);
    }
}
