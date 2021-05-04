package com.graphhopper.jsprit.io.problem;


import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.util.VehicleProfile;
/**
 * XML Specific imports
 */
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class VehicleParameterReader {
    // A Factory class to allow us create a profile builder object
    public interface VehicleProfileBuilderFactory {
        VehicleProfile.Builder createBuilder(String profileName); // TODO: test with vehicle parameters
    }
    static class DefaultVehicleProfileBuilderFactory implements VehicleParameterReader.VehicleProfileBuilderFactory {

        @Override
        public VehicleProfile.Builder createBuilder(String profileName) {
            // you can add if-else conditions here
            return VehicleProfile.Builder.newInstance(profileName);
        }
    }

}
