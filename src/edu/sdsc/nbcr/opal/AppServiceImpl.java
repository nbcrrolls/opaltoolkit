package edu.sdsc.nbcr.opal;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;

import javax.activation.DataHandler;

import java.net.URL;
import java.net.URLConnection;

import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.description.ServiceDesc;

import java.sql.PreparedStatement;
import java.util.Date;
import java.util.List;

import org.globus.gram.GramJob;

import edu.sdsc.nbcr.common.TypeDeserializer;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.sdsc.nbcr.opal.manager.OpalJobManager;
import edu.sdsc.nbcr.opal.manager.OpalJobManagerFactory;
import edu.sdsc.nbcr.opal.manager.JobManagerException;

import edu.sdsc.nbcr.opal.state.JobInfo;
import edu.sdsc.nbcr.opal.state.JobOutput;
import edu.sdsc.nbcr.opal.state.OutputFile;
import edu.sdsc.nbcr.opal.state.HibernateUtil;
import edu.sdsc.nbcr.opal.state.StateManagerException;

import edu.sdsc.nbcr.opal.util.Util;
import edu.sdsc.nbcr.opal.util.ArgValidator;	

/**
 *
 * Implementation of the AppServicePortType, which represents every
 * Opal service
 *
 * @author Sriram Krishnan
 */

public class AppServiceImpl 
    implements AppServicePortType {

    // get an instance of the log4j Logger
    private static Logger logger = 
	Logger.getLogger(AppServiceImpl.class.getName());

    /** Location of tomcat installation */
    private static String catalinaHome; 
    private static String outputPrefix; // the location of the webapps/ROOT

    /** URL for tomcat installation */
    private static String tomcatURL;

    /** Whether to zip up data after completion */
    private static boolean archiveData;

    /**
     * The hash table that stores the references to the job managers
     */
    private static Hashtable jobTable = new Hashtable();

    /** The fully qualified class name of the job manager */
    private static String jobManagerFQCN;

    // the configuration information for the application
    private String serviceName;
    private AppConfigType config;
    private File configFile;
    private long lastModified;

    // containier properties - initialize only once
    private static Properties props;
    static {
	props = new Properties();
	String propsFileName = "opal.properties";
	try {
	    props.load(AppServiceImpl.class.getClassLoader().
		       getResourceAsStream(propsFileName));
	} catch (IOException ioe) {
	    logger.fatal("Failed to load opal.properties");
	}

	tomcatURL = props.getProperty("tomcat.url") + "/";
	catalinaHome = System.getProperty("catalina.home");
	outputPrefix = 	    
	    catalinaHome + File.separator + "webapps" + File.separator;
	String workingDir = props.getProperty("working.dir");
	if (workingDir != null) {
	    outputPrefix += workingDir;
	    if (tomcatURL != null) {
		tomcatURL += workingDir + "/";
	    }
	} else {
	    outputPrefix += "ROOT";
	}

	// traverse symbolic links, if need be
	try {
	    File prefixDir = new File(outputPrefix);
	    outputPrefix = prefixDir.getCanonicalPath();
	} catch (IOException e) {
	    logger.fatal(e);
	}

	// whether to archive data
	archiveData =
	    Boolean.valueOf(props.getProperty("data.archive")).booleanValue();
	if (archiveData) {
	    logger.info("Data will be available as archive after job completion");
	}

	// clean up zombie jobs
	try {
	    logger.debug("Checking if there are any zombie jobs");

	    int numUpdates = HibernateUtil.markZombieJobs();

	    logger.debug("Number of DB entries for zombie jobs cleaned up: " + numUpdates);
	} catch (StateManagerException se) {
	    logger.fatal("Caught exception while trying to clean database", se);
	}

	// get the FQCN for the job manager
	jobManagerFQCN = props.getProperty("opal.jobmanager");
	if (jobManagerFQCN == null) {
	    logger.fatal("Required property not set - opal.jobmanager");
	}
    }

    
    /**
     * Method to access the tomcatURL from other classes...
     */
    static public String getTomcatURL(){
        return tomcatURL;
    }

    /**
     * Default constructor
     *
     * @throws FaultType if there is an error during initialization
     */
    public AppServiceImpl() 
	throws FaultType {
	logger.info("called");

	if (tomcatURL == null) {
	    logger.fatal("Can't find property: tomcatURL");
	    throw new FaultType("Can't find property: tomcatURL");
	}
	if (catalinaHome == null) {
	    logger.fatal("Can't find property: catalina.home");
	    throw new FaultType("Can't find property: catalina.home");
	}
    }

    //-------------------------------------------------------------//
    // Implementation of the methods inside the AppServicePortType //
    //-------------------------------------------------------------//

    /**
     * Get the metadata for this service
     * 
     * @param in dummy object representing doc-literal input parameter
     * @return application metadata, as specified by the WSDL
     * @throws FaultType if there is an error during retrieval of application metadata
     */    
    public AppMetadataType getAppMetadata(AppMetadataInputType in) 
	throws FaultType {
	logger.info("called");

	// make sure that the config has been retrieved
	retrieveAppConfig();

	// return the metadata
	return config.getMetadata();
    }

    /**
     * Get the application configuration for this service
     * 
     * @param in dummy object representing doc-literal input parameter
     * @return application configuration, as specified by the WSDL
     * @throws FaultType if there is an error during retrieval of application configuration
     */
    public AppConfigType getAppConfig(AppConfigInputType in)
	throws FaultType {
	logger.info("called");

	// make sure that the config has been retrieved
	retrieveAppConfig();

	// return the config
	return config;
    }

    /**
     * Launch a job on behalf of the user, using the given arguments
     *
     * @param in the input object, as defined by the WSDL, which contains the command-line
     * arguments, list of input files in Base64 encoded form, and a the number of processes
     * for parallel jobs
     * @param attch a list of file attachments
     * @return job submission output, as defined by the WSDL, which contains the <i>jobID</i>, 
     * and the initial job status
     * @throws FaultType if there is an error during job submission
     */
    public JobSubOutputType launchJob(JobInputType in)
	throws FaultType {
	long t0 = System.currentTimeMillis();
	logger.info("called");

	// make sure that the config has been retrieved
	retrieveAppConfig();

	// write the input files, and launch the job in a non-blocking fashion
	String jobID = launchApp(in, false);

	// create output object
	JobSubOutputType output = new JobSubOutputType();
	output.setJobID(jobID);
	StatusOutputType status = queryStatus(jobID);
	output.setStatus(status);

	long t1 = System.currentTimeMillis();
	logger.debug("Server execution time: " + (t1-t0) + " ms");
	return output;
    }

    /**
     * Launch a job on behalf of the user, using the given arguments, and block till it
     * finishes
     *
     * @param in the input object, as defined by the WSDL, which contains the command-line
     * arguments, list of input files in Base64 encoded form, and a the number of processes
     * for parallel jobs
     * @param attach the list of file attachments
     * @return job output, as defined by the WSDL, which contains the final job status
     * and output metadata
     * @throws FaultType if there is an error during job submission
     */
    public BlockingOutputType launchJobBlocking(JobInputType in)
	throws FaultType {
	long t0 = System.currentTimeMillis();
	logger.info("called");

	// make sure that the config has been retrieved
	retrieveAppConfig();

	// write the input files, and launch the job in a blocking fashion
	String jobID = launchApp(in, true);

	// create output object
	BlockingOutputType output = new BlockingOutputType();
	StatusOutputType status = queryStatus(jobID);
	output.setStatus(status);
	JobOutputType jobOut = getOutputs(jobID);
	output.setJobOut(jobOut);

	long t1 = System.currentTimeMillis();
	logger.debug("Server execution time: " + (t1-t0) + " ms");
	return output;
    }

    /** 
     * Query job status for the given jobID
     * 
     * @param in the <i>jobID</i> for which to query status
     * @return the status, as described by the WSDL
     * @throws FaultType if there is an error during the status query
     */
    public StatusOutputType queryStatus(String in) 
	throws FaultType {
	long t0 = System.currentTimeMillis();
	logger.info("called for job: " + in);
	
	// make sure that the config has been retrieved
	retrieveAppConfig();

	// retrieve the status
	StatusOutputType status = null;

	try {
	    status = HibernateUtil.getStatus(in);
	} catch (StateManagerException se) {
	    logger.error(se.getMessage());
	    throw new FaultType(se.getMessage());
	}

	long t1 = System.currentTimeMillis();
	logger.debug("Query execution time: " + (t1-t0) + " ms");
	return status;
    }

    /** 
     * Query job statistics for the given jobID
     * 
     * @param in the <i>jobID</i> for which to query status
     * @return the statistics, as described by the WSDL
     * @throws FaultType if there is an error during the status query
     */
    public JobStatisticsType getJobStatistics(String in) 
	throws FaultType {
	long t0 = System.currentTimeMillis();
	logger.info("called for job: " + in);
	
	// make sure that the config has been retrieved
	retrieveAppConfig();

	// retrieve the statistics
	JobStatisticsType stats = null;

	try {
	    stats = HibernateUtil.getStatistics(in);
	} catch (StateManagerException se) {
	    logger.error(se.getMessage());
	    throw new FaultType(se.getMessage());
	}

	long t1 = System.currentTimeMillis();
	logger.debug("Query execution time: " + (t1-t0) + " ms");
	return stats;
    }

    /**
     * Return output metadata for a particular job run
     * 
     * @param in <i>jobID</i> for a particular run
     * @return an object, as defined by the WSDL, which contains links to the 
     * standard output and error, and a list of output files
     * @throws FaultType if there is an error in output retrieval
     */
    public JobOutputType getOutputs(String in) 
	throws FaultType {
	long t0 = System.currentTimeMillis();
	logger.info("called for job: " + in);
	
	// make sure that the config has been retrieved
	retrieveAppConfig();

	// retrieve the outputs
	JobOutputType outputs = null;
	
	try {
	    outputs = HibernateUtil.getOutputs(in);
	} catch (StateManagerException se) {
	    logger.error(se.getMessage());
	    throw new FaultType(se.getMessage());
	}

	// make sure the outputs still exist
	File test = new File(outputPrefix + File.separator + in);
	if (!test.exists()) {
	    logger.error("Job outputs have been cleaned up");
	    throw new FaultType("Job outputs have been cleaned up");
	}

	long t1 = System.currentTimeMillis();
	logger.debug("Output retrieval time: " + (t1-t0) + " ms");
	return outputs;
    }

    /**
     * Return a Base64 encoded output file for a particular run
     * 
     * @param in input object, as defined by the WSDL, which contains a 
     * <i>jobID</i> and a <i>fileName</i>
     * @return output file in Base64 encoded binary form
     * @throws FaultType if there is an error in output generation
     */
    public byte[] getOutputAsBase64ByName(OutputsByNameInputType in) 
	throws FaultType {
	long t0 = System.currentTimeMillis();
	logger.info("called for job: " + in.getJobID() + 
		    " with file name: " + in.getFileName());

	byte[] data = null;
	String outputDirName = 
	    outputPrefix + File.separator + in.getJobID() + File.separator;
	String outputURL = outputDirName + in.getFileName();
	File f = new File(outputURL);
	if (f.exists()) {
	    try {
		data = new byte[(int) f.length()];
		FileInputStream fIn = new FileInputStream(f);
		fIn.read(data);
		fIn.close();
	    } catch (Exception e) {
		logger.error("Error while trying to read output: " + e.getMessage());
		throw new FaultType("Error while trying to read output: " + e.getMessage());
	    }

	} else {
	    logger.error("File " + in.getFileName() + " doesn't exist on server");
	    throw new FaultType("File " + in.getFileName() + " doesn't exist on server");
	}
	long t1 = System.currentTimeMillis();
	logger.debug("Output retrieval time: " + (t1-t0) + " ms");
	return data;
    }

    /**
     * Destroy job representing this jobID
     *
     * @param in the jobID for this job
     * @return the final status for this job
     * @throws FaultType if there is an error during job destruction
     */
    public StatusOutputType destroy(String in) 
	throws FaultType {
	long t0 = System.currentTimeMillis();
	logger.info("called for job: " + in);

	// initialize output
	StatusOutputType status = null;

	// check to see if it is still running
	if (jobTable.containsKey(in)) {
	    // retrieve the job manager from the jobTable
	    OpalJobManager jobManager =
		(OpalJobManager) jobTable.get(in);

	    // destroy the job, and wait until it is done
	    try {
		status = jobManager.destroyJob();
	    } catch (JobManagerException jme) {
		String msg = jme.getMessage();
		logger.error(msg);
		throw new FaultType(msg);
	    }
	} else {
	    // retrieve status for a possible finished job
	    status = queryStatus(in);
	}

	long t1 = System.currentTimeMillis();
	logger.debug("Destruction time: " + (t1 - t0) + " ms");
	return status;
    }

    //--------------------------------------------------------------------//
    //    Private helper methods used by the above public impl methods    //
    //--------------------------------------------------------------------//

    private String launchApp(JobInputType in,
			     boolean blocking)
	throws FaultType {

	// create a working directory where it can be accessible
	final String jobID = "app" + System.currentTimeMillis();
	final String outputDirName = 
	    outputPrefix + File.separator + jobID + File.separator;
	final File outputDir = new File(outputDirName);
	if (!outputDir.mkdir()) {
	    logger.error("Can't create new directory to run application in");
	    throw new FaultType("Can't create new directory to run application in");
	}

	// create the application input files there 
	writeAppInput(in, outputDirName);

	// make sure that the arguments don't refer to any parent directories
	String args = in.getArgList();
	if (args != null) {
	    StringTokenizer argTokens = new StringTokenizer(args);
	    while (argTokens.hasMoreTokens()) {
		String next = argTokens.nextToken();
		// must not begin with "/" or "~"
		if (next.startsWith(File.separator) ||
		    (next.startsWith("~"))) {
		    String msg = "Arguments are not allowed to begin with: " +
			File.separator + " or " + "~";
		    logger.error(msg);
		    throw new FaultType(msg);
		}

		// must not include ".."
		if (next.indexOf("..") != -1) {
		    String msg = "Arguments are not allowed to include: ..";
		    logger.error(msg);
		    throw new FaultType(msg);
		}
	    }
	}

	// validate command-line arguments, if need be
	if (config.getValidateArgs() != null) {
	    if (config.getValidateArgs().booleanValue()) {
		logger.info("Validating command-line arguments");
		
		ArgumentsType argsDesc = config.getMetadata().getTypes();
		if (argsDesc == null) {
		    String msg = "Validation of arguments requested - " +
			"but argument schema not provided within app config";
		    logger.error(msg);
		    throw new FaultType(msg);
		}
		
		ArgValidator av = new ArgValidator(argsDesc);
		
		boolean success = av.validateArgList(outputDirName, 
						     in.getArgList());
		if (success) {
		    logger.info("Argument validation successful");
		} else {
		    String msg = "Argument validation unsuccessful";
		    logger.error(msg);
		    throw new FaultType(msg);
		}
	    }
	}

	// create a new status object and save it
	StatusOutputType status = new StatusOutputType();
	status.setCode(GramJob.STATUS_PENDING);
	status.setMessage("Launching executable");
	final URI baseURL;
	try {
	    baseURL = new URI(tomcatURL + jobID);
	    status.setBaseURL(baseURL);
	} catch (Exception e) {
	    String message = "Exception while trying to construct base URL";
	    logger.error(message);
	    throw new FaultType(message);
	}

	// initialize the job information in database
	final JobInfo info = new JobInfo();
	info.setJobID(jobID);
	info.setCode(status.getCode());
	info.setMessage(status.getMessage());
	info.setBaseURL(status.getBaseURL().toString());
	info.setStartTime(new Date());
	info.setLastUpdate(new Date());
	info.setClientDN(Util.getRemoteDN());
	info.setClientIP(Util.getRemoteIP());
	info.setServiceName(serviceName);
	try {
	    HibernateUtil.saveJobInfoInDatabase(info);
	} catch (StateManagerException se) {
	    logger.error(se.getMessage());
	    throw new FaultType(se.getMessage());
	}

	// instantiate & initialize the job manager
	String jobManagerFQCNLocal = jobManagerFQCN;
	if (config.getJobManagerFQCN() != null) {
	    // if the app config has a job manager FQCN, use that
	    jobManagerFQCNLocal = config.getJobManagerFQCN();
	} 
	logger.info("Using job manager class: " + jobManagerFQCNLocal);

	final OpalJobManager jobManager;
	try {
	    jobManager = OpalJobManagerFactory.getOpalJobManager(jobManagerFQCNLocal);
	} catch (JobManagerException jme) {
	    logger.error(jme.getMessage());

	    // save status in database
	    try {
		HibernateUtil.updateJobInfoInDatabase(jobID,
						      GramJob.STATUS_FAILED,
						      jme.getMessage(),
						      info.getBaseURL(),
						      null);
	    } catch (StateManagerException se) {
		logger.error(se.getMessage());
		throw new FaultType(se.getMessage());
	    }

	    throw new FaultType(jme.getMessage());
	}

	try {
	    jobManager.initialize(props, config, null);
	} catch (JobManagerException jme) {
	    logger.error(jme.getMessage());

	    // save status in database
	    try {
		HibernateUtil.updateJobInfoInDatabase(jobID,
						      GramJob.STATUS_FAILED,
						      jme.getMessage(),
						      info.getBaseURL(),
						      null);
	    } catch (StateManagerException se) {
		logger.error(se.getMessage());
		throw new FaultType(se.getMessage());
	    }

	    throw new FaultType(jme.getMessage());
	}

	// launch job with the given arguments
	final String handle;
	try {
	    handle = jobManager.launchJob(in.getArgList(), 
					  in.getNumProcs(),
					  outputDirName);
	} catch (JobManagerException jme) {
	    logger.error(jme.getMessage());

	    // save status in database
	    try {
		HibernateUtil.updateJobInfoInDatabase(jobID,
						      GramJob.STATUS_FAILED,
						      jme.getMessage(),
						      info.getBaseURL(),
						      null);
	    } catch (StateManagerException se) {
		logger.error(se.getMessage());
		throw new FaultType(se.getMessage());
	    }

	    throw new FaultType(jme.getMessage());
	}

	// add this jobLaunchUtil into the jobTable
	jobTable.put(jobID, jobManager);

	if (!blocking) {
	    // launch thread to monitor status
	    new Thread() {
		public void run() {
		    try {
			manageJob(jobManager, jobID, outputDirName, baseURL, handle);
		    } catch (FaultType f) {
			// status is logged, not much else to do here
			logger.error(f);
		    }
		}
	    }.start();
	} else {
	    // monitor status in the same thread
	    manageJob(jobManager, jobID, outputDirName, baseURL, handle);
	}

	// return the jobID
	return jobID;
    }

    private void manageJob(OpalJobManager jobManager,
			   String jobID,
			   String workingDir,
			   URI baseURL,
			   String handle)
	throws FaultType {

	// wait for job activation
	StatusOutputType status = null;
	try {
	    status = jobManager.waitForActivation();
	} catch (JobManagerException jme) {
	    logger.error(jme.getMessage());

	    // save status in database
	    try {
		HibernateUtil.updateJobInfoInDatabase(jobID,
						      GramJob.STATUS_FAILED,
						      jme.getMessage(),
						      baseURL.toString(),
						      handle);
	    } catch (StateManagerException se) {
		logger.error(se.getMessage());
		throw new FaultType(se.getMessage());
	    }

	    throw new FaultType(jme.getMessage());
	}

	if (status.getBaseURL() == null)
	    status.setBaseURL(baseURL);

	// update status in database
	try {
	    HibernateUtil.updateJobInfoInDatabase(jobID, 
						  status.getCode(),
						  status.getMessage(),
						  status.getBaseURL().toString(),
						  new Date(), // activation time
						  null,
						  handle);
	} catch (StateManagerException se) {
	    logger.error(se.getMessage());
	    throw new FaultType(se.getMessage());
	}

	// if the job is still running, wait for it to finish
	try {
	    status = jobManager.waitForCompletion();
	} catch (JobManagerException jme) {
	    logger.error(jme.getMessage());
	    
	    // save status in database
	    try {
		HibernateUtil.updateJobInfoInDatabase(jobID,
						      GramJob.STATUS_FAILED,
						      jme.getMessage(),
						      status.getBaseURL().toString(),
						      handle);
	    } catch (StateManagerException se) {
		logger.error(se.getMessage());
		throw new FaultType(se.getMessage());
	    }
	    
	    throw new FaultType(jme.getMessage());
	}

	if (status.getBaseURL() == null)
	    status.setBaseURL(baseURL);
    
	// bit of a hack because execution completion does not
	// equal completion of Web service call
	int jobCode = status.getCode();
	String jobMessage = status.getMessage();
	status.setCode(GramJob.STATUS_STAGE_OUT);
	status.setMessage("Writing output metadata");

	// update status in database
	try {
	    HibernateUtil.updateJobInfoInDatabase(jobID, 
						  status.getCode(),
						  status.getMessage(),
						  status.getBaseURL().toString(),
						  handle);
	} catch (StateManagerException se) {
	    logger.error(se.getMessage());
	    throw new FaultType(se.getMessage());
	}

	// retrieve job outputs
	// make sure the stdout and stderr exist
	File stdOutFile = new File(workingDir + File.separator + "stdout.txt");
	if (!stdOutFile.exists()) {
	    String msg = "Standard output missing for execution";
	    logger.error(msg);
	    status.setCode(GramJob.STATUS_FAILED);
	    status.setMessage(msg);

	    // update status in database
	    try {
		HibernateUtil.updateJobInfoInDatabase(jobID, 
						      status.getCode(),
						      status.getMessage(),
						      status.getBaseURL().toString(),
						      handle);
	    } catch (StateManagerException se) {
		logger.error(se.getMessage());
		throw new FaultType(se.getMessage());
	    }

	    jobTable.remove(jobID);
	    return;
	}
	File stdErrFile = new File(workingDir + File.separator + "stderr.txt");
	if (!stdErrFile.exists()) {
	    String msg = "Standard error missing for execution";
	    logger.error(msg);
	    status.setCode(GramJob.STATUS_FAILED);
	    status.setMessage(msg);

	    // update status in database
	    try {
		HibernateUtil.updateJobInfoInDatabase(jobID, 
						      status.getCode(),
						      status.getMessage(),
						      status.getBaseURL().toString(),
						      handle);
	    } catch (StateManagerException se) {
		logger.error(se.getMessage());
		throw new FaultType(se.getMessage());
	    }

	    jobTable.remove(jobID);
	    return;
	}
	
	// archive the outputs, if need be
	if (archiveData) {
	    logger.debug("Archiving output files");

	    // get a list of files
	    File f = new File(workingDir);
	    File[] outputFiles = f.listFiles();
	    
	    // Create a buffer for reading the files
	    byte[] buf = new byte[1024];
	    
	    try {

		ZipOutputStream out = 
		    new ZipOutputStream(new FileOutputStream(workingDir + 
							     File.separator + 
							     jobID + 
							     ".zip"));
		
		// Compress the files
		for (int i = 0; i < outputFiles.length; i++) {
		    FileInputStream in = new FileInputStream(outputFiles[i]);
		    
		    // Add ZIP entry to output stream.
		    out.putNextEntry(new ZipEntry(outputFiles[i].getName()));
		    
		    // Transfer bytes from the file to the ZIP file
		    int len;
		    while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		    }
		    
		    // Complete the entry
		    out.closeEntry();
		    in.close();
		}
		
		// Complete the ZIP file
		out.close();
	    } catch (IOException e) {
		logger.error(e);
		logger.error("Error not fatal - moving on");
	    }
	}
	
	// at least 2 files exist now - stdout and stderr
	JobOutputType outputs = new JobOutputType();

	try {
	    File f = new File(workingDir);
	    File[] outputFiles = f.listFiles();
	
	    OutputFileType[] outputFileObj = new OutputFileType[outputFiles.length-2];
	    int j = 0;
	    for (int i = 0; i < outputFiles.length; i++) {
		if (outputFiles[i].getName().equals("stdout.txt")) {
		    outputs.setStdOut(new URI(tomcatURL +
					      jobID + 
					      "/stdout.txt"));
		} else if (outputFiles[i].getName().equals("stderr.txt")) {
		    outputs.setStdErr(new URI(tomcatURL +
					      jobID + 
					      "/stderr.txt"));
		} else {
		    // NOTE: all input files will also be duplicated here
		    OutputFileType next = new OutputFileType();
		    next.setName(outputFiles[i].getName());
		    next.setUrl(new URI(tomcatURL +
					jobID +
					"/" +
					outputFiles[i].getName()));
		    outputFileObj[j++] = next;
		}
	    }
	    outputs.setOutputFile(outputFileObj);

	    // update the outputs table
	    try {
		HibernateUtil.saveOutputsInDatabase(jobID, outputs);
	    } catch (StateManagerException se) {
		logger.error(se.getMessage());
		throw new FaultType(se.getMessage());
	    }
	} catch (IOException e) {
	    // log exception
	    logger.error(e);

	    // set status to FAILED
	    status.setCode(GramJob.STATUS_FAILED);
	    status.setMessage("Cannot retrieve outputs after finish - " +
			      e.getMessage());

	    // finish up

	    // update status in database
	    try {
		HibernateUtil.updateJobInfoInDatabase(jobID, 
						      status.getCode(),
						      status.getMessage(),
						      status.getBaseURL().toString(),
						      handle);
	    } catch (StateManagerException se) {
		logger.error(se.getMessage());
		throw new FaultType(se.getMessage());
	    }

	    jobTable.remove(jobID);

	    return;
	}

	// update final status
	status.setCode(jobCode);
	status.setMessage(jobMessage);
	
	// update status in database
	try {
	    HibernateUtil.updateJobInfoInDatabase(jobID, 
						  status.getCode(),
						  status.getMessage(),
						  status.getBaseURL().toString(),
						  null,
						  new Date(), // completion time
						  handle);
	} catch (StateManagerException se) {
	    logger.error(se.getMessage());
	    throw new FaultType(se.getMessage());
	}

	// get rid of the jobManager from the jobTable
	jobTable.remove(jobID);

	logger.info("Execution complete for job: " + jobID);
    }

    private void writeAppInput(JobInputType in,
			       String outputDirName) 
	throws FaultType {
	logger.info("called");

	// retrieve the list of files
	InputFileType[] inputFiles = in.getInputFile();
	if (inputFiles == null) {
	    // no files to be written
	    return;
	}

	// write the input files into the working directory
	for (int i = 0; i < inputFiles.length; i++) {
	    // make sure the contents are supplied
	    if ((inputFiles[i].getContents() == null) &&
		(inputFiles[i].getLocation() == null) &&
		(inputFiles[i].getAttachment() == null)) {
		String msg = "File contents, URL and attachment are missing - " +
		    "one of them should be provided";
		logger.error(msg);
		throw new FaultType(msg);
	    }

	    try {
		File f = new File(outputDirName + File.separator + 
				  inputFiles[i].getName());
                BufferedOutputStream out = null;
		if (inputFiles[i].getContents() != null) {
                    //it is a 'normal' file
                    out = new BufferedOutputStream(new FileOutputStream(f));
		    out.write(inputFiles[i].getContents());
		    out.close();
		} else if (inputFiles[i].getLocation() != null) {
                    //it is a URL
		    int index = inputFiles[i].getLocation().toString().indexOf(":");
		    if (index == -1) {
			String msg = "Can't find protocol for URL: " + 
			    inputFiles[i].getLocation();
			logger.error(msg);
			throw new FaultType(msg);
		    }
                    
                    out = new BufferedOutputStream(new FileOutputStream(f));
		    String protocol = inputFiles[i].getLocation().toString().substring(0, index);
		    logger.info("Using protocol: " + protocol);

		    if (protocol.equals("http") ||
			protocol.equals("https")) {
			// fetch the file from the URL
			URL url = new URL(inputFiles[i].getLocation().toString());
			URLConnection conn = url.openConnection();
			InputStream input = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while ((numRead = input.read(buffer)) != -1) {
			    out.write(buffer, 0, numRead);
			    numWritten += numRead;
			}
			logger.debug(numWritten + " bytes written from url: " +
				     inputFiles[i].getLocation());
			input.close();
			out.close();
		    } else {
			String msg = "Unsupported protocol: " + protocol;
			logger.error(msg);
			throw new FaultType(msg);
		    }
		} else { 
                    // it is an attachment
		    DataHandler dh = inputFiles[i].getAttachment();
		    logger.debug("Received attachment: " + dh.getName());
                    File attachFile = new File(dh.getName());
                    logger.debug("Source is " + attachFile.toString() + 
				" and dest is " + f.toString());
                    if (attachFile.renameTo(f) == false) {
			String msg = "Unable to copy attachment correctly: " +
			    dh.getName();
			logger.error(msg);
			throw new FaultType(msg);
		    }
		}
	    } catch (FaultType f) {
		// pass the exception along
		throw f;
	    } catch (IOException ioe) {
		logger.error("IOException while trying to write input file: " + 
			     ioe.getMessage());
		throw new FaultType("IOException while trying to write input file: " + 
				    ioe.getMessage());
	    }
	}
    }

    private void retrieveAppConfig()
	throws FaultType {
	logger.info("called");

	// read location of config file
	MessageContext mc = MessageContext.getCurrentContext();
	SOAPService service = mc.getService();
	serviceName = service.getName();
	if (serviceName == null) {
	    serviceName = "Unknown service";
	}

	String configFileName = (String) service.getOption("appConfig");
	if (configFileName == null) {
	    logger.error("Required parameter appConfig not found in WSDD");
	    throw new FaultType("Required parameter appConfig not found in WSDD");
	}
	    
	// read the config file if it is not set, or if has been modified
	boolean reconfigure = false;
	if (configFile == null) {
	    configFile = new File(configFileName);
	    lastModified = configFile.lastModified();
	}
	long newLastModified = configFile.lastModified();
	if (newLastModified > lastModified) {
	    reconfigure = true;
	    lastModified = newLastModified;
	    logger.info("Application config modified recently -- reconfiguring");
	}
	if (config == null) {
	    reconfigure = true;
	    logger.info("Configuring service for the first time");
	}

	if (reconfigure) {
	    logger.info("Reading application config: " + configFileName);

	    try {
		config = 
		    (AppConfigType) TypeDeserializer.getValue(configFileName,
							      new AppConfigType());;
	    } catch (Exception e) {
		logger.error(e);
		String msg = "Can't read application configuration from XML for service: " +
		    serviceName;
		logger.error(msg);
		throw new FaultType(msg);	    
	    }
	}
    }
}
