package edu.sdsc.nbcr.opal.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *
 * Utility class for deployment of Opal services
 *
 * @author Sriram Krishnan
 */
public class Deploy {
    private static Logger logger = Logger.getLogger(Deploy.class.getName());

    /**
     * Default constructor
     */
    public Deploy() {
    }

    /**
     * The main method which uses a WSDD template for Opal services, and replaces
     * the dummy variables with application specific parameters, which is 
     * then used by the Ant target to deploy a new Opal service.
     *
     * <p>The following system properties are required -
     * <br><i>appConfig</i>: the location of the application configuration file
     * <br><i>serviceName</i>: the name of the target service to deploy
     * <br><i>wsddTemplate</i>: the location of the WSDD template 
     * <br><i>wsddFinal</i>: the target location for the generated WSDD
     */
    public static void main(String[] args) throws Exception {
	String appConfig = System.getProperty("appConfig");
	if (appConfig == null) {
	    logger.error("System property appConfig not set!");
	    System.exit(1);
	} else {
	    logger.info("Property appConfig set to: " + appConfig);
	}

	String serviceName = System.getProperty("serviceName");
	if (serviceName == null) {
	    logger.error("System property serviceName not set!");
	    System.exit(1);
	} else {
	    logger.info("Property serviceName set to: " + serviceName);
	}

	String wsddTemplate = System.getProperty("wsddTemplate");
	if (wsddTemplate == null) {
	    logger.error("System property wsddTemplate not set!");
	    System.exit(1);
	} else {
	    logger.info("Property wsddTemplate set to: " + wsddTemplate);
	}

	String wsddFinal = System.getProperty("wsddFinal");
	if (wsddFinal == null) {
	    logger.error("System property wsddFinal not set!");
	    System.exit(1);
	} else {
	    logger.info("Property wsddFinal set to: " + wsddFinal);
	}

	File f = new File(wsddTemplate);
	if (!f.exists()) {
	    logger.error("WSDD template file " + wsddTemplate + " does not exist");
	    System.exit(1);
	}

	byte[] data = new byte[(int) f.length()];
	FileInputStream fIn = new FileInputStream(f);
	fIn.read(data);
	fIn.close();
	String templateData = new String(data);
	String finalData = templateData.replaceAll("@SERVICE_NAME@", 
						   serviceName);

	File configFile = new File(appConfig);
	if (!configFile.exists()) {
	    logger.error("Application configuration file " + 
			 appConfig + " does not exist");
	}

	String configLoc = configFile.getAbsolutePath().replace('\\', '/');
	finalData = finalData.replaceAll("@CONFIG_LOCATION@",
					 configLoc);
	logger.info("Using location of config file: " + configLoc);

	FileOutputStream fOut = new FileOutputStream(wsddFinal);
	fOut.write(finalData.getBytes());
	fOut.close();
    }
}
