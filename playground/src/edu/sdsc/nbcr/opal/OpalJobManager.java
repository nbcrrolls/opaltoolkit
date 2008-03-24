package edu.sdsc.nbcr.opal;

import java.util.Properties;

/**
 * Job Manager interface for Opal 2.x
 * 
 * To keep the implementation of the interface simple, every Job Manager
 * instance is responsible for a single job. Otherwise, every JM impl
 * will have to implement a somewhat complicated state management to
 * keep track of jobs - this is best done just once by the service
 * implementation 
 */
public interface OpalJobManager {

    
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
	throws FaultType;
    
    /**
     * General clean up, if need be 
     *
     * @throws FaultType if there is an error during destruction
     */
    public String destroyJobManager()
	throws FaultType;
    
    /**
     * Launch a job with the given arguments. The input files are already staged in by
     * the service implementation, and the plug in can assume that they are already
     * there
     *
     * @param arglist a string containing the command line used to launch the application
     * @param numproc the number of processors requested. Null, if it is a serial job
     * @param wd String representing the working directory of this job on the local system
     * 
     * @return a plugin specific job handle to be persisted by the service implementation
     * @throws FaultType if there is an error during job launch
     */
    public String launchJob(String arglist, 
			    Integer numproc, 
			    String wd)
	throws FaultType;

    /**
     * Block until the job finishes executing
     *
     * @throws FaultType if there is an error while waiting for the job to finish
     */
    private StatusOutputType waitForCompletion() 
	throws FaultType;

    /**
     * Destroy this job
     * 
     * @return final job status
     * @throws FaultType if there is an error during job destruction
     */
    public StatusOutputType destroyJob()
	throws FaultType;

}
