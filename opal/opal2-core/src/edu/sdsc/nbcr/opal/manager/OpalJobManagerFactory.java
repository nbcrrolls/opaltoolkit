package edu.sdsc.nbcr.opal.manager;

import edu.sdsc.nbcr.opal.JobManagerType;

import edu.sdsc.nbcr.opal.manager.ForkJobManager;
import edu.sdsc.nbcr.opal.manager.DRMAAJobManager;
import edu.sdsc.nbcr.opal.manager.GlobusJobManager;
import edu.sdsc.nbcr.opal.manager.RemoteGlobusJobManager;
import edu.sdsc.nbcr.opal.manager.JobManagerException;

/**
 * Factory class for creating instances of the Opal Job Manager
 */
public class OpalJobManagerFactory {

    public static OpalJobManager getOpalJobManager(JobManagerType jobManagerType) 
	throws JobManagerException {
	if (jobManagerType.equals(JobManagerType.drmaa)) {
	    return new DRMAAJobManager();
	} else if (jobManagerType.equals(JobManagerType.globusLocal)) {
	    return new GlobusJobManager();
	} else if (jobManagerType.equals(JobManagerType.globusRemote)) {
	    return new RemoteGlobusJobManager();
	} else if (jobManagerType.equals(JobManagerType.fork)) {
	    return new ForkJobManager();
	} else {
	    throw new JobManagerException("Unknown JobManagerType: " +
					  jobManagerType);
	}
    }
}