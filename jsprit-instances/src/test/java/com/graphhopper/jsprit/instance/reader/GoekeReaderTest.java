package com.graphhopper.jsprit.instance.reader;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import org.junit.Test;

import java.net.URL;

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
}
