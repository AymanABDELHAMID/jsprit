package com.graphhopper.jsprit.io.problem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupService;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleProfile;
import com.graphhopper.jsprit.core.util.VehicleProfiles;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.*;

public class VehicleParameterReaderTest {
    private InputStream inputStream;

    @Before
    public void doBefore() {
        inputStream = getClass().getResourceAsStream("testVehicleParameters.xml");
    }

    // In the test, if I understood correctly, the reader should take as an argument the object,
    // the instantiation of the object happens before the call of the readFunction
    // This is more logical because you can then assert values added to the reader,
    // you need to make the necessary changes to the code (@link vrpXMLReader)
    @Test
    public void shouldReadVehicleProfileIDs() {
        VehicleProfiles.Builder builder = VehicleProfiles.Builder.newInstance();
        VehicleProfile.VehicleProfileConfig vehicleProfileConfig = new VehicleProfile.VehicleProfileConfig();
        new VehicleParameterReader(vehicleProfileConfig, builder).read(inputStream);
        VehicleProfiles vehicleProfiles = builder.build();
        Set<String> profileNames = vehicleProfiles.getAddedVehicleProfilesNames();
        String carIcev = "car_icev";
        //boolean verifier = profileNames.contains(car_icev);
        assertTrue(profileNames.contains(carIcev));
    }
}
