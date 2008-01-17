package edu.sdsc.nbcr.opal.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *
 * Utility class for undeployment of Opal services
 *
 * @author Sriram Krishnan
 */
public class Undeploy {
    private static Logger logger = Logger.getLogger(Undeploy.class.getName());

    /**
     * Default constructor
     */
    public Undeploy() {
    }

    /**
     * The main method which uses a WSDD template for Opal services, and replaces
     * the dummy variables with application specific parameters, which is 
     * then used by the Ant target to undeploy an existing Opal service.
     *
     * <p>The following system properties are required -
     * <br><i>serviceName</i>: the name of the target service to undeploy
     * <br><i>wsddTemplate</i>: the location of the WSDD template 
     * <br><i>wsddFinal</i>: the target location for the generated WSDD
     */

    public static void main(String[] args) throws Exception {
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

	FileOutputStream fOut = new FileOutputStream(wsddFinal);
	fOut.write(finalData.getBytes());
	fOut.close();
    }
}
