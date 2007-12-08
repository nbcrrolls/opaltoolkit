/**
 * Utility class to launch Globus jobs
 *
 * @author Sriram Krishnan [mailto:sriram@sdsc.edu]
 */
package edu.sdsc.nbcr.opal;

import org.globus.axis.gsi.GSIConstants;
import org.globus.gram.Gram;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.ietf.jgss.GSSCredential;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.axis.handlers.soap.SOAPService;

import javax.servlet.http.HttpServletRequest;

import java.security.cert.X509Certificate;

import java.util.Vector;

import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.DrmaaException;

public class AppJobLaunchUtil implements GramJobListener {

    // get an instance of a log4j logger
    private static Logger logger = Logger.getLogger(AppJobLaunchUtil.class.getName());

    private String jobID; // the primary key for this job
    private StatusOutputType status; // the status of the job thus far
    private JobOutputType outputs; // metadata about output of APBS job
    private JobInputType jobIn; // input calculation parameters

    private GramJob job;  // the application GramJob, if Globus is being used
    private Process proc; // the application process, if Globus is not being used
    private Thread stdoutThread; // the thread that writes stdout, if Globus is not used
    private Thread stderrThread; // the thread that writes stderr, if Globus is not used
    private volatile boolean done = false; // whether the execution is complete

    private static Session session = null; // drmaa session
    private String drmaaJobID; // job id returned by DRMAA job submit
    private JobInfo jobInfo; // job information returned after completion by DRMAA

    static {
	if (AppServiceImpl.drmaaInUse) {
	    SessionFactory factory = SessionFactory.getFactory();
	    session = factory.getSession();
	    try {
		session.init(null);
	    } catch (DrmaaException de) {
		logger.fatal("Can't initialize DRMAA session: " + de.getMessage());
	    }
	}
    }

    /**
     * Constructor
     *
     * @param jobID_ the ID for this job
     * @param jobIn_ the parameters for the job
     * @param status_ the status of this job
     * @param outputs_ the outputs for the job
     */
    public AppJobLaunchUtil(String jobID_,
			    JobInputType jobIn_,
			    StatusOutputType status_,
			    JobOutputType outputs_) {
	jobID = jobID_;
	jobIn = jobIn_;
	status = status_;
	outputs = outputs_;
    }

    /**
     * Launch job using properties set inside AppServiceImpl. The only
     * non-static parameter is the working directory which varies for
     * ever run
     *
     * @param workingDir the directory where the job should be launched
     * @param appConfig the configuration for this particular application
     */
    public void launchJob(final String workingDir,
			  final AppConfigType appConfig) 
	throws FaultType {
	logger.info("called for job: " + jobID);

	// get the DN of the client
        // extract the client DN and log it
        MessageContext mc = MessageContext.getCurrentContext();
        HttpServletRequest req = 
            (HttpServletRequest) mc.getProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST);
        String clientDN = (String) req.getAttribute(GSIConstants.GSI_USER_DN);
        if(clientDN != null) {
            logger.info("Client's DN: " + clientDN);
        } else {
	    clientDN = "Unknown client";
	}

	// get client IP
	String remoteIP = req.getRemoteAddr();

	// get the name of the service to log
	SOAPService service = mc.getService();
	String serviceName = service.getName();
	if (serviceName == null) {
	    serviceName = "Unknown service";
	}

	// make sure we have all parameters we need
	if (appConfig.isParallel()) {

	    // check to see if parallel jobs can be run
	    if (AppServiceImpl.drmaaInUse) {
		if (AppServiceImpl.drmaaPE == null) {
		    // if drmaa is being used, check to see if the pe is set up
		    logger.error("drmaa.pe property must be specified in opal.properties " +
				 "for parallel execution using DRMAA");
		    throw new FaultType("drmaa.pe property must be specified in opal.properties " +
					"for parallel execution using DRMAA");
		}

		if (AppServiceImpl.mpiRun == null) {
		    // need mpi.run if DRMAA is being used
		    logger.error("mpi.run property must be specified in opal.properties " +
				 "for parallel execution using DRMAA");
		    throw new FaultType("mpi.run property must be specified in " + 
					"opal.properties for parallel execution " + 
					"using DRMAA");
		}
	    } else if (!AppServiceImpl.globusInUse) {
		if (AppServiceImpl.mpiRun == null) {
		    // need mpi.run if process exec is being used
		    logger.error("mpi.run property must be specified in opal.properties " +
				 "for parallel execution without using Globus");
		    throw new FaultType("mpi.run property must be specified in " + 
					"opal.properties for parallel execution " + 
					"without using Globus");
		}
	    }

	    // make sure enough processors are present for the job
	    if (jobIn.getNumProcs() == null) {
		logger.error("Number of processes unspecified for parallel job");
		throw new FaultType("Number of processes unspecified for parallel job");
	    } else if (jobIn.getNumProcs().intValue() > AppServiceImpl.numProcs) {
		logger.error("Processors required - " + jobIn.getNumProcs() +
			     ", available - " + AppServiceImpl.numProcs);
		throw new FaultType("Processors required - " + jobIn.getNumProcs() +
				    ", available - " + AppServiceImpl.numProcs);
	    }
	}

	try {
	    // initialize status of job
	    status.setCode(GramJob.STATUS_PENDING);
	    status.setMessage("Launching executable");
	    status.setBaseURL(new URI(AppServiceImpl.tomcatURL + jobID));
	} catch (MalformedURIException mue) {
	    logger.error("Cannot convert base_url string to URI - " +
			 mue.getMessage());
	    throw new FaultType("Cannot convert base_url string to URI - " +
				mue.getMessage());
	}

	if (!AppServiceImpl.dbInUse) {
	    // use an in memory hash table
	   AppServiceImpl.statusTable.put(jobID, status);
	} else {
	    // use a database
	    Connection conn = null;
	    try {
		conn = DriverManager.getConnection(AppServiceImpl.dbUrl,
						   AppServiceImpl.dbUser,
						   AppServiceImpl.dbPasswd);
	    } catch (SQLException e) {
		logger.error("Cannot connect to database - " + e.getMessage());
		throw new FaultType("Cannot connect to database - " + e.getMessage());
	    }

	    String time = 
		new SimpleDateFormat("MMM d, yyyy h:mm:ss a").format(new Date());
	    String sqlStmt = 
		"insert into job_status(job_id, code, message, base_url, " + 
		"client_dn, client_ip, service_name, start_time, last_update) " +
		"values ('" + jobID + "', " +
		status.getCode() + ", " +
		"'" + status.getMessage() + "', " +
		"'" + status.getBaseURL() + "', " +
		"'" + clientDN + "', " +
		"'" + remoteIP + "', " +
		"'" + serviceName + "', " +
		"'" + time + "', " +
		"'" +  time + "');";
	    try {
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(sqlStmt);
		conn.close();
	    } catch (SQLException e) {
		logger.error("Cannot insert job status into database - " + e.getMessage());
		throw new FaultType("Cannot insert job status into database - " + 
				    e.getMessage());
	    }
	}

	// create list of arguments
	String args = appConfig.getDefaultArgs();
	if (args == null) {
	    args = jobIn.getArgList();
	} else {
	    String userArgs = jobIn.getArgList();
	    if (userArgs != null)
		args += " " + userArgs;
	}
	if (args != null) {
	    args = args.trim();
	}
	logger.debug("Argument list: " + args);

	// launch the executable
	if (AppServiceImpl.drmaaInUse) {
	    // launch executable using DRMAA
	    String cmd = null;
	    String[] argsArray = null;

	    if (appConfig.isParallel()) {
		// create command string and arguments for parallel run
		cmd = "/bin/sh";

		// append arguments - needs to be this way to locate machinefile
		String newArgs = AppServiceImpl.mpiRun + 
		    " -machinefile $TMPDIR/machines" +
		    " -np " + jobIn.getNumProcs() + " " +
		    appConfig.getBinaryLocation();
		if (args != null) {
		    args = newArgs + " " + args;
		} else {
		    args = newArgs;
		}
		
		// construct the args array
		argsArray = new String[] {"-c", args};
	    } else {
		// create command string and arguments for serial run
		cmd = appConfig.getBinaryLocation();
		if (args == null)
		    args = "";

		// construct the args array
		argsArray = args.split(" ");
	    }

	    // run executable in the working directory
	    try {
		JobTemplate jt = session.createJobTemplate();
		if (appConfig.isParallel()) 
		    jt.setNativeSpecification("-pe " + AppServiceImpl.drmaaPE + " " +
					      jobIn.getNumProcs());
		jt.setRemoteCommand(cmd);
		jt.setArgs(argsArray);
		jt.setJobName(jobID);
		jt.setWorkingDirectory(workingDir);
		jt.setErrorPath(":" + workingDir + "/stderr.txt");
		jt.setOutputPath(":" + workingDir + "/stdout.txt");
		drmaaJobID = session.runJob(jt);
		logger.info("DRMAA job has been submitted with id " + drmaaJobID);
		session.deleteJobTemplate(jt);
	    } catch (Exception ex) {
		logger.error(ex);
		status.setCode(GramJob.STATUS_FAILED);
		status.setMessage("Error while running executable via DRMAA - " +
				  ex.getMessage());
		if (AppServiceImpl.dbInUse) {
		    try {
			updateStatusInDatabase(jobID, status);
		    } catch (SQLException e) {
			logger.error(e);
			throw new FaultType("Cannot update status into database - " +
					    e.getMessage());
		    }
		}
		
		return;
	    }

	    // update status to active
	    status.setCode(GramJob.STATUS_ACTIVE);
	    status.setMessage("Execution in progress");
	    if (AppServiceImpl.dbInUse) {
		try {
		    updateStatusInDatabase(jobID, status);
		} catch (SQLException e) {
		    logger.error(e);
		    throw new FaultType("Cannot update status into database - " +
					e.getMessage());
		}
	    }
	} else if (AppServiceImpl.globusInUse) {
	    // launch executable using Globus
	    String rsl = null;

	    if (appConfig.isParallel()) {
		// create RSL for parallel run
		rsl =
		    "&(directory=" + workingDir + ")" + 
		    "(executable=" + appConfig.getBinaryLocation() + ")" + 
		    "(count=" + jobIn.getNumProcs() + ")" +
		    "(jobtype=mpi)" +
		    "(stdout=stdout.txt)" + 
		    "(stderr=stderr.txt)";
	    } else {
		// create RSL for serial run
		rsl = 
		    "&(directory=" + workingDir + ")" + 
		    "(executable=" + appConfig.getBinaryLocation() + ")" + 
		    "(stdout=stdout.txt)" + 
		    "(stderr=stderr.txt)";
	    }

	    // add arguments to the RSL
	    if (args != null) {
		// put every argument within quotes - needed by Globus if some of 
		// the arguments are of the form name=value
		args = "\"" + args + "\"";
		args = args.replaceAll("[\\s]+", "\" \"");
		rsl += "(arguments=" + args + ")";
	    }
	    logger.debug("RSL: " + rsl);

	    // execute the job using GRAM
	    try {
		job = new GramJob(rsl);

		// create credentials from service certificate/key
		GlobusCredential globusCred = 
		    new GlobusCredential(AppServiceImpl.serviceCertPath, 
					 AppServiceImpl.serviceKeyPath);
		GSSCredential gssCred = 
		    new GlobusGSSCredentialImpl(globusCred,
						GSSCredential.INITIATE_AND_ACCEPT);

		// set the credentials for the job
		job.setCredentials(gssCred);
		
		// add a listener for the job
		job.addListener(this);

		// execute the globus job
		job.request(AppServiceImpl.gatekeeperContact);
	    } catch (Exception ge) {
		logger.error(ge);
		status.setCode(GramJob.STATUS_FAILED);
		status.setMessage("Error while running executable via Globus - " +
				  ge.getMessage());
		if (AppServiceImpl.dbInUse) {
		    try {
			updateStatusInDatabase(jobID, status);
		    } catch (SQLException e) {
			logger.error(e);
			throw new FaultType("Cannot update status into database - " +
					    e.getMessage());
		    }
		}

		return;
	    }
	} else {
	    // launch executable using process exec
	    String cmd = null;
	    
	    if (appConfig.isParallel()) {
		// create command string for parallel run
		cmd = new String(AppServiceImpl.mpiRun + " " +
				 "-np " + jobIn.getNumProcs() + " " +
				 appConfig.getBinaryLocation());
	    } else {
		// create command string for serial run
		cmd = new String(appConfig.getBinaryLocation());
	    }

	    // append arguments
	    if (args != null) {
		cmd += " " + args;
	    }
	    logger.debug("CMD: " + cmd);

	    // run executable in the working directory
	    try {
		proc = Runtime.getRuntime().exec(cmd, null, new File(workingDir));

		// spawn new threads to write out stdout, and stderr
		stdoutThread = writeStdOut(proc, workingDir);
		stderrThread = writeStdErr(proc, workingDir);
	    } catch (IOException ioe) {
		logger.error(ioe);
		status.setCode(GramJob.STATUS_FAILED);
		status.setMessage("Error while running executable via fork - " +
				  ioe.getMessage());
		if (AppServiceImpl.dbInUse) {
		    try {
			updateStatusInDatabase(jobID, status);
		    } catch (SQLException e) {
			logger.error(e);
			throw new FaultType("Cannot update status into database - " +
					    e.getMessage());
		    }
		}
		
		return;
	    }

	    // update status to active
	    status.setCode(GramJob.STATUS_ACTIVE);
	    status.setMessage("Execution in progress");
	    if (AppServiceImpl.dbInUse) {
		try {
		    updateStatusInDatabase(jobID, status);
		} catch (SQLException e) {
		    logger.error(e);
		    throw new FaultType("Cannot update status into database - " +
					e.getMessage());
		}
	    }
	}
	
	// block till the job finishes, so that output metadata can be created
	new Thread() {
	    public void run() {
		// wait for job to finish
		try {
		    waitForCompletion();
		} catch (FaultType f) {
		    // log exception
		    logger.error(f);

		    // wake up threads waiting for job to finish
		    synchronized(status) {
			status.notifyAll();
		    }

		    // no point going further
		    return;
		}

		if (AppServiceImpl.drmaaInUse || 
		    !AppServiceImpl.globusInUse) { // if DRMAA or process fork has been used

		    // let the stdout, stderr threads know that the process is done
		    done = true;

		    // bit of a hack because execution completion does not
		    // equal completion of Web service call
		    status.setCode(GramJob.STATUS_STAGE_OUT);
		    status.setMessage("Writing output metadata");

		    if (AppServiceImpl.dbInUse) {
			try {
			    updateStatusInDatabase(jobID, status);
			} catch (SQLException e) {
			    // set status to FAILED
			    status.setCode(GramJob.STATUS_FAILED);
			    status.setMessage("Cannot update status database after finish - " +
					      e.getMessage());
			    
			    // log exception
			    logger.error(e);

			    // wake up threads waiting for job to finish
			    synchronized(status) {
				status.notifyAll();
			    }

			    // no point going any further
			    return;
			}
		    }
		}

		// retrieve outputs
		try {
		    // make sure that all outputs have been written out
		    if (!AppServiceImpl.drmaaInUse && !AppServiceImpl.globusInUse) {
			try {
			    logger.debug("Waiting for all outputs to be written out");
			    stdoutThread.join();
			    stderrThread.join();
			    logger.debug("All outputs successfully written out");
			} catch (InterruptedException ignore) {}
		    }

		    // make sure the stdout and stderr exist
		    File stdOutFile = new File(workingDir + File.separator + "stdout.txt");
		    if (!stdOutFile.exists()) {
			throw new IOException("Standard output missing for execution");
		    }
		    File stdErrFile = new File(workingDir + File.separator + "stderr.txt");
		    if (!stdErrFile.exists()) {
			throw new IOException("Standard error missing for execution");
		    }

		    // go ahead if they do - at least 2 files exist now
		    File f = new File(workingDir);
		    File[] outputFiles = f.listFiles();

		    OutputFileType[] outputFileObj = new OutputFileType[outputFiles.length-2];
		    int j = 0;
		    for (int i = 0; i < outputFiles.length; i++) {
			if (outputFiles[i].getName().equals("stdout.txt")) {
			    outputs.setStdOut(new URI(AppServiceImpl.tomcatURL +
						      jobID + 
						      "/stdout.txt"));
			} else if (outputFiles[i].getName().equals("stderr.txt")) {
			    outputs.setStdErr(new URI(AppServiceImpl.tomcatURL +
						      jobID + 
						      "/stderr.txt"));
			} else {
			    // NOTE: all input files will also be duplicated here
			    OutputFileType next = new OutputFileType();
			    next.setName(outputFiles[i].getName());
			    next.setUrl(new URI(AppServiceImpl.tomcatURL +
						jobID +
						"/" +
						outputFiles[i].getName()));
			    outputFileObj[j++] = next;
			}
		    }
		    outputs.setOutputFile(outputFileObj);
		} catch (IOException e) {
		    // set status to FAILED
		    status.setCode(GramJob.STATUS_FAILED);
		    status.setMessage("Cannot retrieve outputs after finish - " +
				      e.getMessage());
		    
		    // log exception
		    logger.error(e);

		    // update status in database
		    if (AppServiceImpl.dbInUse) {
			try {
			    updateStatusInDatabase(jobID, status);
			} catch (SQLException se) {
			    // log exception
			    logger.error(se);
			}
		    }

		    // wake up threads waiting for job to finish
		    synchronized(status) {
			status.notifyAll();
		    }

		    // no point going any further
		    return;
		}

		if (!AppServiceImpl.dbInUse) {
		    // use an in memory hash table
		    AppServiceImpl.outputTable.put(jobID, outputs);
		} else {
		    // use a database
		    Connection conn = null;
		    try {
			conn = DriverManager.getConnection(AppServiceImpl.dbUrl,
							   AppServiceImpl.dbUser,
							   AppServiceImpl.dbPasswd);
		    } catch (SQLException e) {
			status.setCode(GramJob.STATUS_FAILED);
			status.setMessage("Cannot connect to database after finish - " +
					  e.getMessage());
			
			// log exception
			logger.error(e);

			// wake up threads waiting for job to finish
			synchronized(status) {
			    status.notifyAll();
			}
			
			// no point going any further
			return;
		    }
		    
		    // Write outputs to database
		    String sqlStmt = 
			"insert into job_output(job_id, std_out, std_err) " + 
			"values ('" + jobID + "', " +
			"'" + outputs.getStdOut().toString() + "', " +
			"'" + outputs.getStdErr().toString() + "');";
		    Statement stmt = null;
		    try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sqlStmt);
		    } catch (SQLException e) {
			status.setCode(GramJob.STATUS_FAILED);
			status.setMessage("Cannot update job output database after finish - " +
					  e.getMessage());
			
			// log exception
			logger.error(e);
			
			// update status in database
			try {
			    updateStatusInDatabase(jobID, status);
			} catch (SQLException se) {
			    // log exception
			    logger.error(se);
			}

			// wake up threads waiting for job to finish
			synchronized(status) {
			    status.notifyAll();
			}

			// no point going any further
			return;
		    }

		    OutputFileType[] outputFile = outputs.getOutputFile();
		    for (int i = 0; i < outputFile.length; i++) {
			sqlStmt = 
			    "insert into output_file(job_id, name, url) " + 
			    "values ('" + jobID + "', " +
			    "'" + outputFile[i].getName() + "', " +
			    "'" + outputFile[i].getUrl().toString() + "');";
			try {
			    stmt = conn.createStatement();
			    stmt.executeUpdate(sqlStmt);
			} catch (SQLException e) {
			    status.setCode(GramJob.STATUS_FAILED);
			    status.setMessage("Cannot update output_file DB after finish - " +
					      e.getMessage());
			    
			    // log exception
			    logger.error(e);

			    // update status in database
			    try {
				updateStatusInDatabase(jobID, status);
			    } catch (SQLException se) {
				// log exception
				logger.error(se);
			    }

			    // wake up threads waiting for job to finish
			    synchronized(status) {
				status.notifyAll();
			    }

			    // no point going any further
			    return;
			}
		    }
 		}

		// set status to DONE or FAILED
		if (terminatedOK()) {
		    status.setCode(GramJob.STATUS_DONE);
		    status.setMessage("Execution complete - " + 
				      "check outputs to verify successful execution");
		} else {
		    status.setCode(GramJob.STATUS_FAILED);
		    status.setMessage("Execution failed");
		}

		if (AppServiceImpl.dbInUse) {
		    try {
			updateStatusInDatabase(jobID, status);
		    } catch (SQLException e) {
			// set status to FAILED
			status.setCode(GramJob.STATUS_FAILED);
			status.setMessage("Cannot update status database after finish - " +
					  e.getMessage());
			
			// log exception
			logger.error(e);

			// wake up threads waiting for job to finish
			synchronized(status) {
			    status.notifyAll();
			}

			// no point going any further
			return;
		    }
		}

		// remove the entry from the jobTable
		AppServiceImpl.jobTable.remove(jobID);
		
		// wake up threads waiting for job to finish
		synchronized(status) {
		    status.notifyAll();
		}

		logger.info("Execution complete for job: " + jobID);
	    }
	}.start();
    }

    /**
     * Terminate execution 
     */
    public void destroy() 
	throws FaultType {
	logger.info("called");

	// destroy the job
	try {
	    if (AppServiceImpl.drmaaInUse) {
		session.control(drmaaJobID, Session.TERMINATE);
	    } else if (AppServiceImpl.globusInUse) {
		job.cancel();
	    } else {
		proc.destroy();
	    }
	} catch (Exception e) {
	    logger.error("Cannot destroy process - " + e.getMessage());
	    throw new FaultType("Cannot destroy process - " + e.getMessage());
	}

	// remove the entry from the jobTable
	AppServiceImpl.jobTable.remove(jobID);
    }

    /**
     * Wait for execution to finish
     */
    public void waitFor()
	throws FaultType {
	logger.info("called");

	synchronized(status) {
	    try {
		while (!((status.getCode() == GramJob.STATUS_DONE) ||
			 (status.getCode() == GramJob.STATUS_FAILED)))
		    status.wait();
	    } catch (InterruptedException ie) {
		logger.error(ie);
		status.setCode(GramJob.STATUS_FAILED);
		status.setMessage("Unexpected interrupt while waiting for " +
				  "completion of job run - " + ie.getMessage()); 
		if (AppServiceImpl.dbInUse) {
		    try {
			updateStatusInDatabase(jobID, status);
		    } catch (SQLException e) {
			logger.error(e);
			throw new FaultType("Cannot update status into database - " +
					    e.getMessage());
		    }
		}
	    }
	}
    }

    /**
     * Method defined inside the GramJobListener interface
     */
    public void statusChanged(GramJob job) {
	logger.info("called for job: " + jobID);
	
	// get the job status code and message
	int code = job.getStatus();
	String message;
	if (code != GramJob.STATUS_FAILED)
	    message = GramJob.getStatusAsString(code);
	else 
	    message = GramJob.getStatusAsString(code) + 
		", Error code - " + job.getError();
	logger.info("Job status: " + message);
	
	// update job status in memory or in database
	if ((code == GramJob.STATUS_DONE) ||
	    (code == GramJob.STATUS_FAILED)) {
	    // bit of a hack because execution completion does not
	    // equal completion of Web service call
	    status.setCode(GramJob.STATUS_STAGE_OUT);
	    status.setMessage("Writing output metadata");

	    // deactivate listener, which gets rid of a GRAM thread
	    job.removeListener(this);
	    Gram.deactivateCallbackHandler(job.getCredentials());

	} else {
	    status.setCode(code);
	    status.setMessage(message);
	}

	if (AppServiceImpl.dbInUse) {
	    try {
		updateStatusInDatabase(jobID, status);
	    } catch (SQLException se) {
		logger.error("Cannot update status into database - " +
			     se.getMessage());
		// can't propagate the exception back up since
		// this is a different thread
	    }
	}

	// notify sleepers that the job is finished
	if ((code == GramJob.STATUS_FAILED) || (code == GramJob.STATUS_DONE)) {
	    logger.info("Job " + jobID + " finished with status - " + message);
	    synchronized(job) {
		job.notifyAll();
	    }
	}
    }

    /**
     * Waits till the execution is finished
     */
    private void waitForCompletion() 
	throws FaultType {
	logger.debug("called");

	try {
	    if (AppServiceImpl.drmaaInUse) {
		jobInfo = session.wait(drmaaJobID, Session.TIMEOUT_WAIT_FOREVER);
	    } else if (AppServiceImpl.globusInUse) {
		synchronized(job) {
		    job.wait();
		}
	    } else {
		proc.waitFor();
	    }
	} catch (Exception ie) {
	    logger.error(ie);
	    status.setCode(GramJob.STATUS_FAILED);
	    status.setMessage("Unexpected exception while waiting for " +
			      "execution to finish - " + ie.getMessage()); 
	    if (AppServiceImpl.dbInUse) {
		try {
		    updateStatusInDatabase(jobID, status);
		} catch (SQLException e) {
		    logger.error(e);
		    throw new FaultType("Cannot update status into database - " +
					e.getMessage());
		}
	    }
	}
    }

    /**
     * utility method to check of the job executed correctly
     */
    private boolean terminatedOK() {
	logger.debug("called");

	if (AppServiceImpl.drmaaInUse) {
	    if (jobInfo.getExitStatus() != 0)
		return false;
	    else 
		return true;
	} else if (AppServiceImpl.globusInUse) {
	    if (job.getStatus() == GramJob.STATUS_FAILED)
		return false;
	    else 
		return true;
	} else {
	    if (proc.exitValue() != 0)
		return false;
	    else 
		return true;
	}
    }
    
    /**
     * utility method to write the status object into database
     */
    private void updateStatusInDatabase(String jobID, StatusOutputType status) 
	throws SQLException {
	logger.debug("called for job: " + jobID);

	Connection conn = DriverManager.getConnection(AppServiceImpl.dbUrl,
						      AppServiceImpl.dbUser,
						      AppServiceImpl.dbPasswd);

	String sqlStmt = 
	    "update job_status " +
	    "set code = ? , " + 
	    "message = ? , " + 
	    "base_url = ? , " + 
	    "last_update = ? " +
	    "where job_id = ?";

	PreparedStatement stmt = conn.prepareStatement(sqlStmt);
	stmt.setInt(1, status.getCode());
	stmt.setString(2, status.getMessage());
	stmt.setString(3, status.getBaseURL().toString());
	stmt.setString(4, 
		       new SimpleDateFormat("MMM d, yyyy h:mm:ss a").format(new Date()));
	stmt.setString(5, jobID);
	stmt.executeUpdate();
	conn.close();
    }
    
    /*
     * utility method to create a new thread to write stdout of a process
     */
    private Thread writeStdOut(Process p, String outputDirName) {
	final File outputDir = new File(outputDirName);
	final InputStreamReader isr 
	    = new InputStreamReader(p.getInputStream());
	final String outfileName =
	    outputDir.getAbsolutePath() + File.separator + "stdout.txt";
	Thread t_input = new Thread() {
		public void run() {
		    FileWriter fw;
		    try {
			fw = new FileWriter(outfileName);
		    } catch (IOException ioe) {
			logger.error(ioe);
			return; // can't do much if the file can't be opened
		    }
		    int bytes = 0;
		    char [] buf = new char[256];
		    while (!(done && (bytes < 0))) {
			try {
			    bytes = isr.read(buf);
			    if (bytes > 0) {
				fw.write(buf, 0, bytes);
				fw.flush();
			    }
			} catch (IOException ignore) {
			    break; // time to quit
			}
		    }

		    try {
			fw.close();
		    } catch (IOException ioe) {
			logger.error(ioe);
			return; // tough to send the exception back from another thread
		    }

		    logger.debug("Done writing standard output");
		}
	    };
	t_input.start();
	return t_input;
    }

    /*
     * utility method to create a new thread to write stderr of a process
     */
    private Thread writeStdErr(Process p, String outputDirName) {
	final File outputDir = new File(outputDirName);
	final InputStreamReader isr 
	    = new InputStreamReader(p.getErrorStream());
	final String errfileName =
	    outputDir.getAbsolutePath() + File.separator + "stderr.txt";
	Thread t_error = new Thread() {
		public void run() {
		    FileWriter fw;
		    try {
			fw = new FileWriter(errfileName);
		    } catch (IOException ioe) {
			logger.error(ioe);
			return; // can't do much if the file can't be opened
		    }
		    int bytes = 0;
		    char [] buf = new char[256];
		    while (!(done && (bytes < 0))) {
			try {
			    bytes = isr.read(buf);
			    if (bytes > 0) {
				fw.write(buf, 0, bytes);
				fw.flush();
			    }
			} catch (IOException ignore) {
			    break; // time to quit
			}
		    }

		    try {
			fw.close();
		    } catch (IOException ioe) {
			logger.error(ioe);
			return; // tough to send the exception back from another thread
		    }

		    logger.debug("Done writing standard error");
		}
	    };
	t_error.start();
	return t_error;
    }
}

