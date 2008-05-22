package edu.sdsc.nbcr.opal.state;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.HibernateException;

import org.hibernate.criterion.Expression;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Iterator;

import org.globus.gram.GramJob;

import edu.sdsc.nbcr.opal.StatusOutputType;
import edu.sdsc.nbcr.opal.JobOutputType;
import edu.sdsc.nbcr.opal.OutputFileType;

import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;

/**
 *
 * Utility class for hibernate functions
 *
 * @author Sriram Krishnan
 */

public class HibernateUtil {

    // get an instance of the log4j Logger
    private static Logger logger = 
	Logger.getLogger(HibernateUtil.class.getName());
    
    private static final SessionFactory sessionFactory;

    static {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            sessionFactory = 
		new Configuration().configure("hibernate-opal.cfg.xml").buildSessionFactory();
        } catch (HibernateException ex) {
            // Make sure you log the exception, as it might be swallowed
            logger.error("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Return a hibernate session factory loaded from the configuration
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Saves the job information into the hibernate database
     *
     * @param info the job information object to be saved
     * @throws StateManagerException if there is an error during the database commit
     */
    public static void saveJobInfoInDatabase(JobInfo info)
	throws StateManagerException {
	logger.info("called");
	
	try {
	    Session session = getSessionFactory().openSession();
	    session.beginTransaction();
	    session.save(info);
	    session.getTransaction().commit();
	    session.close();
	} catch (HibernateException ex) {
	    String msg = "Error during database update: " + ex.getMessage();
	    logger.error(msg);
	    throw new StateManagerException(msg);
	}
    }

    /**
     * Marks all jobs currently active as zombies - useful during startup
     */
    public static int markZombieJobs() 
	throws StateManagerException {
	logger.info("called");

	try {
	    Session session = getSessionFactory().openSession();
	    session.beginTransaction();
	    Date lastUpdate = new Date();
	    int numUpdates = session.createQuery("update JobInfo info " +
						 "set info.lastUpdate = :lastUpdate, " +
						 "info.code = :code, " +
						 "info.message = :message " +
						 "where info.code != " + GramJob.STATUS_DONE + 
						 "and info.code != " + GramJob.STATUS_FAILED)
		.setTimestamp("lastUpdate", lastUpdate)
		.setInteger("code", GramJob.STATUS_FAILED)
		.setString("message", "Job failed - server was restarted during job execution")
		.executeUpdate();
	    session.getTransaction().commit();
	    session.close();
	    
	    return numUpdates;
	} catch (HibernateException ex) {
	    String msg = "Error during database update: " + ex.getMessage();
	    logger.error(msg);
	    throw new StateManagerException(msg);
	}
    }

    /**
     * Update the job info for a job already in the database
     * 
     * @param jobID the job id for this job
     * @param code the status code for this job
     * @param message the status message for this job
     * @param baseURL the base URL for this job
     * @param handle the manager specific handle to communicate with the job
     * @throws StateManagerException if there is an error during the database commit
     */
    public static void updateJobInfoInDatabase(String jobID,
					       int code,
					       String message,
					       String baseURL,
					       String handle)
	throws StateManagerException {
	logger.info("called");
	logger.debug("Updating status to: " + message);
	
	int numRows = 1;
	try {
	    Session session = getSessionFactory().openSession();
	    session.beginTransaction();
	    Date lastUpdate = new Date();
	    numRows = session.createQuery("update JobInfo info " +
					  "set info.lastUpdate = :lastUpdate, " +
					  "info.code = :code, " +
					  "info.message = :message, " +
					  "info.baseURL = :baseURL, " +
					  "info.handle = :handle " +
					  "where info.jobID = '" +
					  jobID + "'")
		.setTimestamp("lastUpdate", lastUpdate)
		.setInteger("code", code)
		.setString("message", message)
		.setString("baseURL", baseURL)
		.setString("handle", handle)
		.executeUpdate();
	    session.getTransaction().commit();
	    session.close();
	} catch (HibernateException ex) {
	    String msg = "Error during database update: " + ex.getMessage();
	    logger.error(msg);
	    throw new StateManagerException(msg);
	}
	    
	if (numRows == 1) {
	    logger.info("Updated status for job: " + jobID);
	} else {
	    String msg = "Unable to update status for job: " + jobID;
	    logger.error(msg);
	    throw new StateManagerException(msg);
	}
    }

    /**
     * Saves the job information into the hibernate database
     *
     * @param jobID the job id for this job
     * @param outputs job outputs for this job
     * @throws StateManagerException if there is an error during the database commit
     */
    public static void saveOutputsInDatabase(String jobID,
					     JobOutputType outputs)
	throws StateManagerException {
	logger.info("called");

	try {
	    Session session = getSessionFactory().openSession();
	    session.beginTransaction();
	    
	    // retrieve the job info object
	    List results = session.createCriteria(JobInfo.class)
		.add(Expression.eq("jobID", jobID))
		.list();
	    if (results.size() != 1) {
		session.close();
		throw new StateManagerException("Can't find job info for job: " + jobID);
	    }
	    JobInfo info = (JobInfo) results.get(0);
	    
	    // initialize job outputs
	    JobOutput out = new JobOutput();
	    out.setJob(info);
	    out.setStdOut(outputs.getStdOut().toString());
	    out.setStdErr(outputs.getStdErr().toString());
	    
	    // initialize the output files
	    OutputFile files[] = new OutputFile[outputs.getOutputFile().length];
	    for (int i = 0; i < outputs.getOutputFile().length; i++) {
		// initialize output files
		files[i] = new OutputFile();
		files[i].setJob(info);
		files[i].setName(outputs.getOutputFile()[i].getName());
		files[i].setUrl(outputs.getOutputFile()[i].getUrl().toString());
	    }
	    
	    // save the outputs
	    session.save(out);
	    for (int i = 0; i < files.length; i++) {
		session.save(files[i]);
	    }
	    session.getTransaction().commit();
	    session.close();
	} catch (HibernateException ex) {
	    String msg = "Error during database update: " + ex.getMessage();
	    logger.error(msg);
	    throw new StateManagerException(msg);
	}
    }

    /**
     * Retrieves job status from the database using the jobID
     *
     * @param jobID the job id for this job
     * @return the status for this job
     * @throws StateManagerException if there is an error during status retrieval
     */
    public static StatusOutputType getStatus(String jobID) 
	throws StateManagerException {
	logger.info("called");

	StatusOutputType status = null;

	try {
	    // retrieve job status from hibernate
	    Session session = getSessionFactory().openSession();
	    session.beginTransaction();
	    List results = session.createCriteria(JobInfo.class)
		.add(Expression.eq("jobID", jobID))
		.list();
	    if (results.size() == 1) {
		JobInfo info = (JobInfo) results.get(0);
		status = new StatusOutputType();
		status.setCode(info.getCode());
		status.setMessage(info.getMessage());
		try {
		    status.setBaseURL(new URI(info.getBaseURL()));
		} catch (MalformedURIException e) {
		    // log and contiue
		    logger.error(e.getMessage());
		}
	    }
	    session.close();
	} catch (HibernateException ex) {
	    String msg = "Error while getting status from database: " + ex.getMessage();
	    logger.error(msg);
	    throw new StateManagerException(msg);
	}
	
	if (status == null) {
	    String msg = "Can't retrieve status for job: " + jobID;
	    logger.error(msg);
	    throw new StateManagerException(msg);
	}

	return status;
    }

    /**
     * Retrieves job status from the database using the jobID
     *
     * @param jobID the job id for this job
     * @return the outputs for this job
     * @throws StateManagerException if there is an error during status retrieval
     */
    public static JobOutputType getOutputs(String jobID) 
	throws StateManagerException {
	logger.info("called");

	JobOutputType outputs = new JobOutputType();

	try {
	    Session session = getSessionFactory().openSession();
	    session.beginTransaction();
	    List results = session.createQuery("from JobOutput output " +
					       "left join fetch output.job " +
					       "where output.job.jobID = '" +
					       jobID + "'")
		.list();
	    JobOutput output = null;
	    if (results.size() == 1) {
		output = (JobOutput) results.get(0);
	    } else {
		String msg = "Can't get job outputs for job: " + jobID;
		logger.error(msg);
		throw new StateManagerException(msg);
	    }
	    try {
		// set the stdout and stderr from the DB
		outputs.setStdOut(new URI(output.getStdOut()));
		outputs.setStdErr(new URI(output.getStdErr()));
	    } catch (MalformedURIException e) {
		String msg = "Can't set URI for stdout/stderr for job: " + e.getMessage();
		logger.error(msg);
		throw new StateManagerException(msg);
	    }
	    
	    results = session.createQuery("from OutputFile file " +
					  "left join fetch file.job " +
					  "where file.job.jobID = '" +
					  jobID + "'")
		.list();
	    if (results != null) {
		OutputFileType[] outputFileObj = new OutputFileType[results.size()];
		// set the output file objects from the database
		for (int i = 0; i < results.size(); i++) {
		    OutputFile file = (OutputFile) results.get(i);
		    outputFileObj[i] = new OutputFileType();
		    outputFileObj[i].setName(file.getName());
		    try {
			outputFileObj[i].setUrl(new URI(file.getUrl()));
		    } catch (MalformedURIException e) {
			String msg = "Can't set URI for output file: " + e.getMessage();
			logger.error(msg);
			throw new StateManagerException(msg);
		    }
		}
		
		outputs.setOutputFile(outputFileObj);
	    }
	    session.close();
	} catch (HibernateException ex) {
	    String msg = "Error while getting outputs from database: " + ex.getMessage();
	    logger.error(msg);
	    throw new StateManagerException(msg);
	}

	return outputs;
    }

    // A simple main method to test functionality
    public static void main(String[] args) 
	throws Exception {

	// initialize hibernate
	System.out.println("Initializing hibernate");
        Session session = getSessionFactory().openSession();

	// initialize info
	JobInfo info = new JobInfo();
	String jobID = "app" + System.currentTimeMillis();
	info.setJobID(jobID);
	info.setCode(0);
	info.setMessage("This is a test");
	info.setBaseURL("http://localhost/test");
	info.setStartTime(new Date());
	info.setLastUpdate(new Date());
	info.setClientDN("CN=Test");
	info.setClientIP("127.0.0.1");
	info.setServiceName("Command-line");

	// save job info
	System.out.println("Trying to save JobInfo into database");
	saveJobInfoInDatabase(info);
	System.out.println("Saved JobInfo into database successfully");

	// save output files
	System.out.println("Trying to save job outputs into database");
	JobOutputType outputs = new JobOutputType();
	outputs.setStdOut(new URI("http://localhost/test/stdout.txt"));
	outputs.setStdErr(new URI("http://localhost/test/stderr.txt"));
	OutputFileType[] files = new OutputFileType[1];
	files[0] = new OutputFileType();
	files[0].setName("foo.txt");
	files[0].setUrl(new URI("http://localhost/test/foo.txt"));
	outputs.setOutputFile(files);
	saveOutputsInDatabase(jobID, outputs);
	System.out.println("Saved OutputFile into database successfully");

	System.out.println("Update job info for job: " + jobID);
	updateJobInfoInDatabase(jobID,
				1,
				"This is a test update",
				info.getBaseURL(),
				"testHandle");

	// do some searches
	System.out.println("Searching for status for job: " + jobID);
	StatusOutputType status = getStatus(jobID);
	System.out.println("Job Status: " + jobID +
			   " - {" + status.getCode() +
			   ", " + status.getMessage() +
			   ", " + status.getBaseURL() + "}");

	System.out.println("Searching for job outputs for job: " + jobID);
	outputs = getOutputs(jobID);
	System.out.println("Standard output: " + outputs.getStdOut());
	System.out.println("Standard error: " + outputs.getStdErr());
	files = outputs.getOutputFile();
	for (int i = 0; i < files.length; i++) {
	    System.out.println(files[i].getName() + ": " + files[i].getUrl());
	}
    }
}