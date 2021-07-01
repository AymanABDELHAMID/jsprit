package com.graphhopper.jsprit.instance.reader;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;

public class GoekeReaderTest {

    @Test
    public void testGoekeReader() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new GoekeReader(vrpBuilder).read(getPath("c101C6.txt"));
        vrpBuilder.build();
    }

    private String getPath(String string) {
        // TODO : find a better fix
        URL resource = this.getClass().getClassLoader().getResource(string);
        if (resource == null) throw new IllegalStateException("resource " + string + " does not exist");
        return resource.getPath().replace("%20", " ");
    }

    @Test
    public void whenReadingInstance_fleetSizeIsFinite() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new GoekeReader(vrpBuilder).read(getPath("c101C6.txt"));
        VehicleRoutingProblem vrp = vrpBuilder.build();
        assertEquals(VehicleRoutingProblem.FleetSize.INFINITE, vrp.getFleetSize());
    }

    @Test
    public void whenReadingInstance_vehiclesHaveTheCorrectCapacity() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new GoekeReader(vrpBuilder).read(getPath("c101C6.txt"));
        VehicleRoutingProblem vrp = vrpBuilder.build();
        for (Vehicle v : vrp.getVehicles()) {
            assertEquals(200, v.getType().getCapacityDimensions().get(0));
        }
    }

    @Test
        public void whenReadingInstance_vehiclesHaveTheCorrectBatteryRange() {
            VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
            new GoekeReader(vrpBuilder).read(getPath("c101C6.txt"));
            VehicleRoutingProblem vrp = vrpBuilder.build();
            for (Vehicle v : vrp.getVehicles()) {
                assertEquals(77, v.getType().getBatteryDimensions().get(0), 0.1);
            }
        }

    @Test
    public void testNuOfShipments() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new GoekeReader(vrpBuilder).read(getPath("c101C6.txt"));
        VehicleRoutingProblem vrp = vrpBuilder.build();
        assertEquals(6, vrp.getJobs().values().size());
    }

}
