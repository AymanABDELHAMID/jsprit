package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;

import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class VehicleRoutingEnergyConsumptionMatrixTest {
    private InputStream inputStream;

    private static VehicleRoutingEnergyCostMatrix createEnergyMatrix(VehicleRoutingProblem.Builder vrpBuilder, VehicleType type) {
        VehicleRoutingEnergyCostMatrix.Builder matrixBuilder = VehicleRoutingEnergyCostMatrix.Builder.newInstance(true);
        //Object[] type = vrpBuilder.getAddedVehicleTypes().stream().toArray();

        for (String from : vrpBuilder.getLocationMap().keySet()) {
            for (String to : vrpBuilder.getLocationMap().keySet()) {
                Coordinate fromCoord = vrpBuilder.getLocationMap().get(from);
                Coordinate toCoord = vrpBuilder.getLocationMap().get(to);
                double load = vrpBuilder.getLocationClassMap().get(fromCoord).getLoad();
                double consumption = EnergyConsumptionCalculator.calculateConsumption(fromCoord, toCoord, type, load);
                matrixBuilder.addTransportConsumption(from, to, consumption);
            }
        }
        return matrixBuilder.build();
    }

    @Before
    public void doBefore() {
        inputStream = getClass().getResourceAsStream("testVehicleParameters.xml");
    }

/*    @Test
    public void whenAddingDistanceToSymmetricMatrix_itShouldReturnCorrectValues() {
        VehicleRoutingEnergyCostMatrix.Builder matrixBuilder = VehicleRoutingEnergyCostMatrix.Builder.newInstance(true);
        //Vehicle vehicle = Vehicle
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("BEV").addCapacityDimension(WEIGHT_INDEX, 2);
        vehicleTypeBuilder.buildProfile(profile);
        Builder vehicleBuilder = Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.newInstance(10, 10));
        vehicleBuilder.setType(new VehicleType());
        VehicleImpl vehicle = vehicleBuilder.build();
        matrixBuilder.addTransportConsumption("1", "2", 2.);
        VehicleRoutingEnergyCostMatrix matrix = matrixBuilder.build();
        assertEquals(2., matrix.getEnergyConsumption(loc("1"), loc("2")), 0.1);
        assertEquals(2., matrix.getEnergyConsumption(loc("2"), loc("1")), 0.1);
    }

    private Location loc(String s) {
        return Location.Builder.newInstance().setId(s).build();
    }
*/
}
