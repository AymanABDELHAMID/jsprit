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
import com.graphhopper.jsprit.core.util.VehicleProfiles;
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
        // instantiate VehicleProfiles
        VehicleProfiles profiles = VehicleProfiles.Builder.newInstance().build();
        for (HierarchicalConfiguration vehicleProfileConfig : vehicleProfileConfigs) {
            String profileId = vehicleProfileConfig.getString("id");
            if (profileId == null) throw new IllegalArgumentException("profileId is missing.");
            VehicleProfile.Builder builder = VehicleProfile.Builder.newInstance(profileId); // profileID = profile_name

            //read Frontal Area
            String frontalArea = vehicleProfileConfig.getString("dimensions.frontalArea");
            if (frontalArea == null) {
                throw new IllegalArgumentException("frontalArea is missing.");
            }
            builder.setFrontalArea(Double.parseDouble(frontalArea));

            //read vehicle curb mass
            String weight = vehicleProfileConfig.getString("dimensions.weight");
            if (weight == null) {
                throw new IllegalArgumentException("Vehicle weight is missing.");
            }
            builder.setVehicleWeight(Double.parseDouble(weight));

            //read vehicle maximum speed
            String speed = vehicleProfileConfig.getString("speed.maximum");
            if (speed == null) {
                throw new IllegalArgumentException("Vehicle max. speed is missing.");
            }
            builder.setVehicleMaxSpeed(Double.parseDouble(speed));

            //read vehicle average speed
            String avgSpeed = vehicleProfileConfig.getString("speed.average");
            if (avgSpeed == null) {
                throw new IllegalArgumentException("Vehicle average speed is missing.");
            }
            builder.setVehicleAvgSpeed(Double.parseDouble(avgSpeed));

            //read vehicle rolling resistance coefficient
            String crr = vehicleProfileConfig.getString("crr");
            if (crr == null) {
                throw new IllegalArgumentException("Vehicle rolling resistance coefficient is missing.");
            }
            builder.setVehicleCRR(Double.parseDouble(crr));

            //read vehicle air drag coefficient
            String cw = vehicleProfileConfig.getString("cw");
            if (cw == null) {
                throw new IllegalArgumentException("Vehicle air drag coefficient is missing.");
            }
            builder.setVehicleCW(Double.parseDouble(cw));

            //read vehicle mechanical efficiency
            String nm = vehicleProfileConfig.getString("nm");
            if (nm == null) {
                throw new IllegalArgumentException("Vehicle mechanical efficiency is missing.");
            }
            builder.setVehicleNM(Double.parseDouble(crr));

            //read vehicle gain efficiency
            String ng = vehicleProfileConfig.getString("ng");
            if (ng == null) {
                throw new IllegalArgumentException("Vehicle gain efficiency is missing.");
            }
            builder.setVehicleNG(Double.parseDouble(crr));

            // Build vehicle profile
            VehicleProfile profile = builder.build();

            // you can also put a map
            profiles.addProfile(profile);
        }
    }

    public void read(String filename) {
        log.debug("read vehicle parameters from file " + filename);
        URL url = Resource.getAsURL(filename);
        readVehicleParameters(this.vehicleProfileConfig.getXMLConfiguration());
        read(url);
    }
}
