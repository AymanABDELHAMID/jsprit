package com.graphhopper.jsprit.io.problem;


import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.util.Resource;
import com.graphhopper.jsprit.core.util.VehicleProfile;
/**
 * XML Specific imports
 */
import com.graphhopper.jsprit.io.algorithm.AlgorithmConfig;
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
import java.net.URL;
import java.util.*;


public class VehicleParameterReader {

    private static Logger log = LoggerFactory.getLogger(VehicleParameterReader.class.getName());
    private VehicleProfile.VehicleProfileConfig vehicleProfileConfig;

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

    public VehicleParameterReader(VehicleProfile.VehicleProfileConfig vehicleProfileConfig) {
        this.vehicleProfileConfig = vehicleProfileConfig;
    }


    public void read(URL url) {
        log.debug("read vehicle parameters: " + url);
        vehicleProfileConfig.getXMLConfiguration().setURL(url);
        vehicleProfileConfig.getXMLConfiguration().setAttributeSplittingDisabled(true);
        vehicleProfileConfig.getXMLConfiguration().setDelimiterParsingDisabled(true);

        final InputStream resource = Resource.getAsInputStream("vehicleParameters_schema.xsd");
        if (resource != null) {
            EntityResolver resolver = new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    {
                        InputSource is = new InputSource(resource);
                        return is;
                    }
                }
            };
            vehicleProfileConfig.getXMLConfiguration().setEntityResolver(resolver);
            vehicleProfileConfig.getXMLConfiguration().setSchemaValidation(true);
        } else {
            log.warn("cannot find schema-xsd file (vehicleParameters_schema.xsd).");
        }
    }

    public void readVehicleParameters(XMLConfiguration vehicleProfile) {
        //read vehicle profiles
        // TODO: each profile must have an ID to link it with the vehicle, such as vehicle is linked to vehicleType with ID
        List<HierarchicalConfiguration> vehicleProfileConfigs = vehicleProfile.configurationsAt("profile");
        for (HierarchicalConfiguration vehicleProfileConfig : vehicleProfileConfigs) {
            String profileId = vehicleProfileConfig.getString("id");
            if (profileId == null) throw new IllegalArgumentException("profileId is missing.");
            //VehicleImpl.Builder builder = VehicleImpl.Builder.newInstance(vehicleId);
            VehicleProfile.Builder builder = VehicleProfile.Builder.newInstance(profileId); // profileID = profile_name
            //String profileType = vehicleProfileConfig.getString("[@type]");

            //read Frontal Area
            String frontalArea = vehicleProfileConfig.getString("dimensions.frontalArea");
            if (frontalArea == null) {
                //locationId = vehicleConfig.getString("startLocation.id"); // TODO: understand what this means
                throw new IllegalArgumentException("frontalArea is missing.");
                // or I can replace it with a default value using the builder
            }
            builder.setFrontalArea(Double.parseDouble(frontalArea));

            // TODO: instantiate VehicleProfiles

            //build vehicle
            VehicleProfile profile = builder.build();
            builder.addProfile(profile); // TODO: This is problematic, I think it is better to og back to the original design,
                                        // TODO: add vehicle parameters class, then add vehicleprofile that has a list of vehicleprofiles
                                        // TODO: or transform vehicleProfile into VehcileProfiles with an internal class: VehicleProfile
            // you can also put a map
        }
    }

    public void read(String filename) {
        log.debug("read vehicle parameters from file " + filename);
        URL url = Resource.getAsURL(filename);
        readVehicleParameters(this.vehicleProfileConfig.getXMLConfiguration()); // not sure this is correct
        read(url);
    }
}
