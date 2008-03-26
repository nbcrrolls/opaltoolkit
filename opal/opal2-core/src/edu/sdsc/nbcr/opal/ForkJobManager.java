package edu.sdsc.nbcr.opal;

import java.util.Properties;

import org.apache.log4j.Logger;

/**
 *
 * Implementation of an Opal Job Manager using Process fork
 */
public class ForkJobManager implements OpalJobManager {

    // get an instance of a log4j logger
    private static Logger logger = Logger.getLogger(ForkJobManager.class.getName());

    private Properties props; // the container properties being passed
    private AppConfigType config; // the application configuration
    private Process proc; // the application process
    private StatusOutputType status; // current status
    private String handle; // the OS specific process id for this job
    private Thread stdoutThread; // the thread that writes stdout
    private Thread stderrThread; // the thread that writes stderr

    /**
     * Initialize the Job Manager for a particular job
     *
     * @param props the properties file containing the value to configure this plugin
     * @param config the opal configuration for this application
     * @param handle manager specific handle to bind to, if this is a resumption. 
     * NULL,if this manager is being initialized for the first time.
     * 
     * @throws FaultType if there is an error during initialization
     */
    public void initialize(Properties props,
			   AppConfigType config,
			   String handle)
	throws FaultType {
	this.props = props;
	this.config = config;
	this.handle = handle;

	// initialize status
	status = new StatusOutputType();
    }
    
    /**
     * General clean up, if need be 
     *
     * @throws FaultType if there is an error during destruction
     */
    public void destroyJobManager()
	throws FaultType {

	// TODO: not sure what needs to be done here
	return;
    }
    
    /**
     * Launch a job with the given arguments. The input files are already staged in by
     * the service implementation, and the plug in can assume that they are already
     * there
     *
     * @param argList a string containing the command line used to launch the application
     * @param numProcs the number of processors requested. Null, if it is a serial job
     * @param wd String representing the working directory of this job on the local system
     * 
     * @return a plugin specific job handle to be persisted by the service implementation
     * @throws FaultType if there is an error during job launch
     */
    public String launchJob(String argList, 
			    Integer numProcs, 
			    String wd)
	throws FaultType {

	// make sure we have all parameters we need
	if (config == null) {
	    String msg = "Can't find application configuration - "
		+ "Plugin not initialized correctly";
	    logger.error(msg);
	    throw new FaultType(msg);
	}

	// create list of arguments
	String args = config.getDefaultArgs();
	if (args == null) {
	    args = argList;
	} else {
	    String userArgs = argList;
	    if (userArgs != null)
		args += " " + userArgs;
	}
	if (args != null) {
	    args = args.trim();
	}
	logger.debug("Argument list: " + args);

	// get the number of processors available
	String systemProcsString = props.getProperty("num.procs");
	int systemProcs = 0;
	if (systemProcsString != null) {
	    systemProcs = Integer.parseInt(systemProcsString);
	}

	if (config.isParallel()) {
	    // make sure enough processors are present for the job
	    if (numProcs == null) {
		String msg = "Number of processes unspecified for parallel job";
		logger.error(msg);
		throw new FaultType(msg);
	    } else if (numProcs.intValue() > systemProcs) {
		String msg = "Processors required - " + numProcs +
		    ", available - " + systemProcs;
		logger.error(msg);
		throw new FaultType(msg);
	    }
	} else {
	}

	// TODO: launch serial/parallel jobs, spawn threads for stdout/stderr
	return null;
    }

    /**
     * Block until the job state is GramJob.STATUS_ACTIVE
     *
     * @return status for this job after blocking
     * @throws FaultType if there is an error while waiting for the job to be ACTIVE
     */
    public StatusOutputType waitForActivation() 
	throws FaultType {

	// TODO: poll till status is ACTIVE or ERROR
	return null;
    }

    /**
     * Block until the job finishes executing
     *
     * @return final job status
     * @throws FaultType if there is an error while waiting for the job to finish
     */
    public StatusOutputType waitForCompletion() 
	throws FaultType {

	// TODO: proc.waitFor()
	return null;
    }

    /**
     * Destroy this job
     * 
     * @return final job status
     * @throws FaultType if there is an error during job destruction
     */
    public StatusOutputType destroyJob()
	throws FaultType {

	// proc.destroy()
	return null;
    }
}
