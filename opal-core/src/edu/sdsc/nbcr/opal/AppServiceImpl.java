/**
 *
 * Implementation of the Application Wrapper Web service. 
 *
 * @author: Sriram Krishnan [mailto:sriram@sdsc.edu]
 */

package edu.sdsc.nbcr.opal;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import java.util.Properties;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.description.ServiceDesc;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import org.globus.gram.GramJob;

import edu.sdsc.nbcr.common.TypeDeserializer;

public class AppServiceImpl 
    implements AppServicePortType {

    // get an instance of the log4j Logger
    private static Logger logger = 
	Logger.getLogger(AppServiceImpl.class.getName());

    public static String catalinaHome; // location of tomcat installation
    private static String outputPrefix; // the location of the webapps/ROOT
    public static String mpiRun; // path to the mpirun executable
    public static int numProcs; // number of available processors
    public static String tomcatURL; // URL for tomcat installation

    // the hash table that stores the job status
    // this is static so that it can be shared across multiple instances
    public static Hashtable statusTable = new Hashtable();

    // the hash table that stores the locations of the outputs
    public static Hashtable outputTable = new Hashtable();

    // the hash table that stores the references to the jobLaunchUtils
    public static Hashtable jobTable = new Hashtable();

    // following values are set if a database is being used
    public static boolean dbInUse; // if a database is used or not
    public static String dbUrl; // jdbc url for the database
    public static String dbUser; // user name for the database
    public static String dbPasswd; // password for the user

    // following values are set for using globus correctly
    public static boolean globusInUse; // if globus is being used or not
    public static String gatekeeperContact; // url for the globus gatekeeper
    public static String serviceCertPath; // location of the cert of service
    public static String serviceKeyPath; // location of the private key of service

    // following values are set for using DRMAA for job submission
    public static boolean drmaaInUse; // if DRMAA is being used or not
    public static String drmaaPE;  // the name of the parallel environment

    // the configuration information for the application
    private AppConfigType appConfig;
    private File appConfigFile;
    private long lastModified;

    // initialize the properties statically (only once)
    static {
	Properties props = new Properties();
	String propsFileName = "opal.properties";
	try {
	    props.load(AppServiceImpl.class.getClassLoader().
		       getResourceAsStream(propsFileName));
	} catch (IOException ioe) {
	    logger.fatal("Failed to load opal.properties");
	}

	mpiRun = props.getProperty("mpi.run");
	String numProcsString = props.getProperty("num.procs");
	if (numProcsString == null)
	    numProcs = 0;
	else
	    numProcs = Integer.parseInt(numProcsString);

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

	// database setup
	dbInUse = 
	    Boolean.valueOf(props.getProperty("database.use")).booleanValue();
	if (dbInUse) {
	    try {
		Class.forName("org.postgresql.Driver");
	    } catch (ClassNotFoundException cnfe) {
		logger.fatal(cnfe);
	    }

	    logger.info("Database being used to store state");
	    dbUrl = props.getProperty("database.url");
	    if (dbUrl == null) {
		logger.fatal("Can't find property: database.url");
	    }
		
	    dbUser = props.getProperty("database.user");
	    if (dbUser == null) {
		logger.fatal("Can't find property: database.user");
	    }
		
	    dbPasswd = props.getProperty("database.passwd");
	    if (dbPasswd == null) {
		logger.fatal("Can't find property: database.passwd");
	    }		
	}

	// DRMAA setup
	drmaaInUse =
	    Boolean.valueOf(props.getProperty("drmaa.use")).booleanValue();
	if (drmaaInUse) {
	    logger.info("DRMAA being used for launching Opal jobs");

	    drmaaPE = props.getProperty("drmaa.pe");
	}

	// Globus setup
	globusInUse = 
	    Boolean.valueOf(props.getProperty("globus.use")).booleanValue();
	if (globusInUse && !drmaaInUse) {
	    logger.info("Globus being used for launching Opal jobs");

	    gatekeeperContact = props.getProperty("globus.gatekeeper");
	    if (gatekeeperContact == null) {
		logger.fatal("Can't find property: globus.gatekeeper");
	    }

	    serviceCertPath = props.getProperty("globus.service_cert");
	    if (serviceCertPath == null) {
		logger.fatal("Can't find property: globus.service_cert");
	    }

	    serviceKeyPath = props.getProperty("globus.service_privkey");
	    if (serviceKeyPath == null) {
		logger.fatal("Can't find property: globus.service_privkey");
	    }
	}
    }

    /**
     * Default constructor
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
	if (dbInUse) {
	    if (dbUrl == null) {
		logger.fatal("Can't find property: database.url");
		throw new FaultType("Can't find property: database.url");
	    }

	    if (dbUser == null) {
		logger.fatal("Can't find property: database.user");
		throw new FaultType("Can't find property: database.user");
	    }

	    if (dbPasswd == null) {
		logger.fatal("Can't find property: database.passwd");
		throw new FaultType("Can't find property: database.passwd");
	    }		
	}
	if (globusInUse && !drmaaInUse) {
	    if (gatekeeperContact == null) {
		logger.fatal("Can't find property: globus.gatekeeper");
		throw new FaultType("Can't find property: globus.gatekeeper");
	    }

	    if (serviceCertPath == null) {
		logger.fatal("Can't find property: globus.service_cert");
		throw new FaultType("Can't find property: globus.service_cert");
	    }

	    if (serviceKeyPath == null) {
		logger.fatal("Can't find property: globus.service_privkey");
		throw new FaultType("Can't find property: globus.service_privkey");
	    }
	}
    }

    //-------------------------------------------------------------//
    // Implementation of the methods inside the AppServicePortType //
    //-------------------------------------------------------------//
    
    public AppMetadataType getAppMetadata(AppMetadataInputType in) 
	throws FaultType {
	logger.info("called");

	// make sure that the appConfig has been retrieved
	retrieveAppConfig();

	// return the metadata
	return appConfig.getMetadata();
    }

    public AppConfigType getAppConfig(AppConfigInputType in)
	throws FaultType {
	logger.info("called");

	// make sure that the appConfig has been retrieved
	retrieveAppConfig();

	// return the appConfig
	return appConfig;
    }

    public JobSubOutputType launchJob(JobInputType in) 
	throws FaultType {
	long t0 = System.currentTimeMillis();
	logger.info("called");

	// make sure that the appConfig has been retrieved
	retrieveAppConfig();

	// write the input files, and launch the job
	JobSubOutputType output = launchApp(in);

	long t1 = System.currentTimeMillis();
	logger.debug("Server execution time: " + (t1-t0) + " ms");
	return output;
    }

    public BlockingOutputType launchJobBlocking(JobInputType in) 
	throws FaultType {
	long t0 = System.currentTimeMillis();
	logger.info("called");

	// make sure that the appConfig has been retrieved
	retrieveAppConfig();

	// write the input files, and launch the job
	BlockingOutputType output = launchAppBlocking(in);

	long t1 = System.currentTimeMillis();
	logger.debug("Server execution time: " + (t1-t0) + " ms");
	return output;
    }

    public StatusOutputType queryStatus(String in) 
	throws FaultType {
	long t0 = System.currentTimeMillis();
	logger.info("called for job: " + in);
	
	// make sure that the appConfig has been retrieved
	retrieveAppConfig();

	// retrieve the status
	StatusOutputType status = null;
	if (!dbInUse) {
	    // use an in memory hash table
	    if (statusTable.containsKey(in)) {
		status =
		    (StatusOutputType) statusTable.get(in);
	    } else {
		logger.error("Unknown job id: " + in);
		throw new FaultType("Unknown job id: " + in);
	    }
	} else {
	    // use a database
	    Connection conn = null;
	    try {
		conn = DriverManager.getConnection(dbUrl,
						   dbUser,
						   dbPasswd);
	    } catch (SQLException e) {
		logger.error("Can't connect to database: " + e.getMessage());
		throw new FaultType("Can't connect to database: " + e.getMessage());
	    }

	    String sqlStmt = 
		"select * from job_status " +
		"where job_ID = '" + in + "';";
	    try {
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sqlStmt);
		boolean notEmpty = rs.next();
		if (!notEmpty) {
		    logger.error("Unknown job id: " + in);
		    throw new FaultType("Unknown job id: " + in);
		}

		status = new StatusOutputType();
		status.setCode(rs.getInt("code"));
		status.setMessage(rs.getString("message"));
		status.setBaseURL(new URI(rs.getString("base_url")));

		conn.close();
	    } catch (MalformedURIException mue) {
		logger.error("Can't convert base_url string to URI: " +
			     mue.getMessage());
		throw new FaultType("Can't convert base_url string to URI: " +
				    mue.getMessage());
	    } catch (SQLException e) {
		logger.error("Can't retrieve job status from database: " + e.getMessage());
		throw new FaultType("Can't retrieve job status from database: " + 
				    e.getMessage());
	    }
	}

	long t1 = System.currentTimeMillis();
	logger.debug("Query execution time: " + (t1-t0) + " ms");
	return status;
    }

    public JobOutputType getOutputs(String in) 
	throws FaultType {
	long t0 = System.currentTimeMillis();
	logger.info("called for job: " + in);
	
	// make sure that the appConfig has been retrieved
	retrieveAppConfig();

	// retrieve the outputs
	JobOutputType outputs = null;
	if (!dbInUse) {
	    // use an in memory hash table
	    if (outputTable.containsKey(in)) {
		outputs =
		    (JobOutputType) outputTable.get(in);
	    } else {
		logger.error("Output unavailable for job: " + in);
		throw new FaultType("Output unavailable for job: " + in);
	    }
	} else {
	    // use a database
	    Connection conn = null;
	    try {
		conn = DriverManager.getConnection(dbUrl,
						   dbUser,
						   dbPasswd);
	    } catch (SQLException e) {
		logger.error("Can't connect to database: " + e.getMessage());
		throw new FaultType("Can't connect to database: " + e.getMessage());
	    }

	    try {
		outputs = new JobOutputType();

		String sqlStmt = 
		    "select * from job_output " +
		    "where job_id = '" + in + "';";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(sqlStmt);
		boolean notEmpty = rs.next();
		if (!notEmpty) {
		    logger.error("Output unavailable for job: " + in);
		    throw new FaultType("Output unavailable for job: " + in);
		}

		// Read outputs from database, and return them
 		outputs.setStdOut(new URI(rs.getString("std_out")));
 		outputs.setStdErr(new URI(rs.getString("std_err")));

		sqlStmt =
		    "select * from output_file " +
		    "where job_id = '" + in + "';";
		stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					    ResultSet.CONCUR_UPDATABLE);
		rs = stmt.executeQuery(sqlStmt);
		rs.last();
		int length = rs.getRow();

		OutputFileType[] outputFile = new OutputFileType[length];
		rs.beforeFirst();
		for (int i = 0; i < length; i++) {
		    rs.next();
		    OutputFileType next = new OutputFileType();
		    next.setName(rs.getString("name"));
		    next.setUrl(new URI(rs.getString("url")));
		    outputFile[i] = next;
		}
		outputs.setOutputFile(outputFile);

		conn.close();
	    } catch (MalformedURIException mue) {
		logger.error("Can't convert url string to URI: " +
			     mue.getMessage());
		throw new FaultType("Can't convert url string to URI: " +
				    mue.getMessage());
	    } catch (SQLException e) {
		logger.error("Can't retrieve outputs from database: " + e.getMessage());
		throw new FaultType("Can't retrieve outputs from database: " + 
				    e.getMessage());
	    }
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

    public StatusOutputType destroy(String in) 
	throws FaultType {
	long t0 = System.currentTimeMillis();
	logger.info("called for job: " + in);

	// initialize output
	StatusOutputType status = null;

	// check to see if it is still running
	if (jobTable.containsKey(in)) {
	    // retrieve the jobLaunchUtil from the jobTable
	    AppJobLaunchUtil jobLaunchUtil = 
		(AppJobLaunchUtil) jobTable.get(in);

	    // destroy the job, and wait until it is done
	    jobLaunchUtil.destroy();
	    jobLaunchUtil.waitFor();

	    // get the job status - ok to retrieve from memory this time 
	    // because only the former call is made to an in-memory store anyways
	    status = (StatusOutputType) statusTable.get(in);
	} else {
	    // check if the job has finished executing, or if it didn't exist at all
	    if (!dbInUse) {
		// use an in memory hash table
		if (statusTable.containsKey(in)) {
		    status =
			(StatusOutputType) statusTable.get(in);
		} else {
		    logger.error("Unknown job id: " + in);
		    throw new FaultType("Unknown job id: " + in);
		}
	    } else {
		// use a database
		Connection conn = null;
		try {
		    conn = DriverManager.getConnection(dbUrl,
						       dbUser,
						       dbPasswd);
		} catch (SQLException e) {
		    logger.error("Can't connect to database: " + e.getMessage());
		    throw new FaultType("Can't connect to database: " + e.getMessage());
		}

		String sqlStmt = 
		    "select * from job_status " +
		    "where job_ID = '" + in + "';";
		try {
		    Statement stmt = conn.createStatement();
		    ResultSet rs = stmt.executeQuery(sqlStmt);
		    boolean notEmpty = rs.next();
		    if (!notEmpty) {
			logger.error("Unknown job id: " + in);
			throw new FaultType("Unknown job id: " + in);
		    }
		    
		    status = new StatusOutputType();
		    status.setCode(rs.getInt("code"));
		    status.setMessage(rs.getString("message"));
		    status.setBaseURL(new URI(rs.getString("base_url")));
		    
		    conn.close();
		} catch (MalformedURIException mue) {
		    logger.error("Can't convert base_url string to URI: " +
				 mue.getMessage());
		    throw new FaultType("Can't convert base_url string to URI: " +
					mue.getMessage());
		} catch (SQLException e) {
		    logger.error("Can't retrieve job status from database: " + e.getMessage());
		    throw new FaultType("Can't retrieve job status from database: " + 
					e.getMessage());
		}
	    }
	}

	long t1 = System.currentTimeMillis();
	logger.debug("Destruction time: " + (t1 - t0) + " ms");
	return status;
    }

    //--------------------------------------------------------------------//
    //    Private helper methods used by the above public impl methods    //
    //--------------------------------------------------------------------//

    private JobSubOutputType launchApp(JobInputType in)
	throws FaultType {

	// create a working directory where it can be accessible
	final String jobID = "app" + System.currentTimeMillis();
	String outputDirName = 
	    outputPrefix + File.separator + jobID + File.separator;
	final File outputDir = new File(outputDirName);
	if (!outputDir.mkdir()) {
	    logger.error("Can't create new directory to run application in");
	    throw new FaultType("Can't create new directory to run application in");
	}

	// create the application input files there 
	writeAppInput(in, outputDirName);

	// create a new status object, and initialize AppJobLaunchUtil
	StatusOutputType status = new StatusOutputType();
	JobOutputType outputs = new JobOutputType();
	AppJobLaunchUtil jobLaunchUtil = new AppJobLaunchUtil(jobID,
							      in,
							      status,
							      outputs);

	// add this jobLaunchUtil into the jobTable
	jobTable.put(jobID, jobLaunchUtil);

	// launch the job asynchronously
	jobLaunchUtil.launchJob(outputDirName,
				appConfig);

	// return the jobID, and preliminary status
	JobSubOutputType out = new JobSubOutputType();
	out.setJobID(jobID);
	out.setStatus(status);
	return out;
    }

    private BlockingOutputType launchAppBlocking(JobInputType in)
	throws FaultType {

	// create a working directory where it can be accessible
	final String jobID = "app" + System.currentTimeMillis();
	String outputDirName = 
	    outputPrefix + File.separator + jobID + File.separator;
	final File outputDir = new File(outputDirName);
	if (!outputDir.mkdir()) {
	    logger.error("Can't create new directory to run application in");
	    throw new FaultType("Can't create new directory to run application in");
	}

	// create the application input files there 
	writeAppInput(in, outputDirName);

	// create a new status object, and initialize AppJobLaunchUtil
	StatusOutputType status = new StatusOutputType();
	JobOutputType outputs = new JobOutputType();
	AppJobLaunchUtil jobLaunchUtil = new AppJobLaunchUtil(jobID,
							      in,
							      status,
							      outputs);

	// add this jobLaunchUtil into the jobTable
	jobTable.put(jobID, jobLaunchUtil);

	// launch the job asynchronously
	jobLaunchUtil.launchJob(outputDirName,
				appConfig);

	// wait for the job to finish
	jobLaunchUtil.waitFor();

	// return the status and the job outputs
	BlockingOutputType out = new BlockingOutputType();
	out.setStatus(status);
	out.setJobOut(outputs);
	return out;
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
	    try {
		File f = new File(outputDirName + File.separator + 
				  inputFiles[i].getName());
		FileOutputStream out = new FileOutputStream(f);
		out.write(inputFiles[i].getContents());
		out.close();
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

	// read location of appConfig file
	MessageContext mc = MessageContext.getCurrentContext();
	SOAPService service = mc.getService();
	String appConfigFileName = (String) service.getOption("appConfig");
	if (appConfigFileName == null) {
	    logger.error("Required parameter appConfig not found in WSDD");
	    throw new FaultType("Required parameter appConfig not found in WSDD");
	}
	    
	// read the config file if it is not set, or if has been modified
	boolean reconfigure = false;
	if (appConfigFile == null) {
	    appConfigFile = new File(appConfigFileName);
	    lastModified = appConfigFile.lastModified();
	}
	long newLastModified = appConfigFile.lastModified();
	if (newLastModified > lastModified) {
	    reconfigure = true;
	    lastModified = newLastModified;
	    logger.info("Application config modified recently -- reconfiguring");
	}
	if (appConfig == null) {
	    reconfigure = true;
	    logger.info("Configuring service for the first time");
	}

	if (reconfigure) {
	    logger.info("Reading application config: " + appConfigFileName);

	    try {
		appConfig = 
		    (AppConfigType) TypeDeserializer.getValue(appConfigFileName,
							      new AppConfigType());;
	    } catch (Exception e) {
		logger.error("Can't read application configuration from XML: " +
			     e.getMessage());
		throw new FaultType("Can't read application configuration from XML: " +
				    e.getMessage());
	    }
	}
    }
}
