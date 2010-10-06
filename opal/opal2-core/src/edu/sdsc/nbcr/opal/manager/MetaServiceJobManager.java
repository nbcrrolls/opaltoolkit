package edu.sdsc.nbcr.opal.manager;

import java.util.Properties;

import edu.sdsc.nbcr.opal.AppConfigType;
import edu.sdsc.nbcr.opal.StatusOutputType;

/**
 * Implementation of the Opal MetaService
 */
public class MetaServiceJobManager implements OpalJobManager {

    
    /**
     * Initialize the Job Manager for a particular job
     *
     * @param props the properties file containing the value to configure this plugin
     * @param config the opal configuration for this application
     * @param handle manager specific handle to bind to, if this is a resumption. 
     * NULL,if this manager is being initialized for the first time.
     * 
     * @throws JobManagerException if there is an error during initialization
     */
    public void initialize(Properties props,
			   AppConfigType config,
			   String handle)
	throws JobManagerException {
	// TODO: read the metaservice configuration as well
    }
    
    /**
     * General clean up, if need be 
     *
     * @throws JobManagerException if there is an error during destruction
     */
    public void destroyJobManager()
	throws JobManagerException {
    }
    
    /**
     * Launch a job with the given arguments. The input files are already staged in by
     * the service implementation, and the plug in can assume that they are already
     * there
     *
     * @param argList a string containing the command line used to launch the application
     * @param numproc the number of processors requested. Null, if it is a serial job
     * @param workingDir String representing the working directory of this job on the local system
     * 
     * @return a plugin specific job handle to be persisted by the service implementation
     * @throws JobManagerException if there is an error during job launch
     */
    public String launchJob(String argList, 
			    Integer numproc, 
			    String workingDir)
	throws JobManagerException {
	// TODO: pick one of the remote services and launch job
	return null;
    }

    /**
     * Block until the job state is GramJob.STATUS_ACTIVE
     *
     * @return status for this job after blocking
     * @throws JobManagerException if there is an error while waiting for the job to be ACTIVE
     */
    public StatusOutputType waitForActivation() 
	throws JobManagerException {
	// TODO: poll for activation
	return null;
    }

    /**
     * Block until the job finishes executing
     *
     * @return final job status
     * @throws JobManagerException if there is an error while waiting for the job to finish
     */
    public StatusOutputType waitForCompletion() 
	throws JobManagerException {
	// TODO: poll for completion and download results
	return null;
    }

    /**
     * Destroy this job
     * 
     * @return final job status
     * @throws JobManagerException if there is an error during job destruction
     */
    public StatusOutputType destroyJob()
	throws JobManagerException {
	// TODO: kill remote opal job
	return null;
    }
}
