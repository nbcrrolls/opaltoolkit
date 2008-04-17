package edu.sdsc.nbcr.opal.state;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import org.hibernate.criterion.Expression;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Iterator;

import edu.sdsc.nbcr.opal.StatusOutputType;
import edu.sdsc.nbcr.opal.JobOutputType;
import edu.sdsc.nbcr.opal.OutputFileType;
import edu.sdsc.nbcr.opal.FaultType;

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
        } catch (Throwable ex) {
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
     * @throws FaultType if there is an error during the database commit
     */
    public static void saveJobInfoInDatabase(JobInfo info)
	throws FaultType {
	logger.info("called");

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
	session.save(info);
	session.getTransaction().commit();
	session.close();
    }

    /**
     * Update the job info for a job already in the database
     * 
     * @param jobID the job id for this job
     * @param code the status code for this job
     * @param message the status message for this job
     * @param baseURL the base URL for this job
     * @param handle the manager specific handle to communicate with the job
     * @throws FaultType if there is an error during the database commit
     */
    public static void updateJobInfoInDatabase(String jobID,
					       int code,
					       String message,
					       String baseURL,
					       String handle)
	throws FaultType {
	logger.info("called");
	logger.debug("Updating status to: " + message);

	Session session = HibernateUtil.getSessionFactory().openSession();
	session.beginTransaction();
	Date lastUpdate = new Date();
	int numRows = session.createQuery("update JobInfo info " +
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

	if (numRows == 1) {
	    logger.info("Updated status for job: " + jobID);
	} else {
	    String msg = "Unable to update status for job: " + jobID;
	    logger.error(msg);
	    throw new FaultType(msg);
	}
    }

    /**
     * Saves the job information into the hibernate database
     *
     * @param jobID the job id for this job
     * @param outputs job outputs for this job
     * @throws FaultType if there is an error during the database commit
     */
    public static void saveOutputsInDatabase(String jobID,
					     JobOutputType outputs)
	throws FaultType {
	logger.info("called");

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

	// retrieve the job info object
	List results = session.createCriteria(JobInfo.class)
	    .add(Expression.eq("jobID", jobID))
	    .list();
	if (results.size() != 1) {
	    session.close();
	    throw new FaultType("Can't find job info for job: " + jobID);
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
    }

    /**
     * Retrieves job status from the database using the jobID
     *
     * @param jobID the job id for this job
     * @return the status for this job
     * @throws FaultType if there is an error during status retrieval
     */
    public static StatusOutputType getStatus(String jobID) 
	throws FaultType {
	logger.info("called");

	StatusOutputType status = null;

	// retrieve job status from hibernate
	Session session = HibernateUtil.getSessionFactory().openSession();
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

	if (status == null) {
	    String msg = "Can't retrieve status for job: " + jobID;
	    logger.error(msg);
	    throw new FaultType(msg);
	}

	return status;
    }

    /**
     * Retrieves job status from the database using the jobID
     *
     * @param jobID the job id for this job
     * @return the outputs for this job
     * @throws FaultType if there is an error during status retrieval
     */
    public static JobOutputType getOutputs(String jobID) 
	throws FaultType {
	logger.info("called");

	JobOutputType outputs = new JobOutputType();

	Session session = HibernateUtil.getSessionFactory().openSession();
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
	    throw new FaultType(msg);
	}
	try {
	    // set the stdout and stderr from the DB
	    outputs.setStdOut(new URI(output.getStdOut()));
	    outputs.setStdErr(new URI(output.getStdErr()));
	} catch (MalformedURIException e) {
	    String msg = "Can't set URI for stdout/stderr for job: " + e.getMessage();
	    logger.error(msg);
	    throw new FaultType(msg);
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
		    throw new FaultType(msg);
		}
	    }

	    outputs.setOutputFile(outputFileObj);
	}
	session.close();

	return outputs;
    }

    // A simple main method to test functionality
    public static void main(String[] args) 
	throws Exception {

	// initialize hibernate
	logger.info("Initializing hibernate");
        Session session = HibernateUtil.getSessionFactory().openSession();

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
	logger.info("Trying to save JobInfo into database");
        session.beginTransaction();
	session.save(info);
	session.getTransaction().commit();
	session.close();
	logger.info("Saved JobInfo into database successfully");

	// initialize job outputs
	JobOutput output = new JobOutput();
	output.setJob(info);
	output.setStdOut("stdout.txt");
	output.setStdErr("stderr.txt");

	// save job output
	logger.info("Trying to save JobOutput into database");
	session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
	session.save(output);
	session.getTransaction().commit();
	session.close();
	logger.info("Saved JobOutput into database successfully");

	// initialize output files
	OutputFile file = new OutputFile();
	file.setJob(info);
	file.setName("foo.txt");
	file.setUrl("http://localhost/test/foo.txt");

	// save output files
	logger.info("Trying to save OutputFile into database");
	session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
	session.save(file);
	session.getTransaction().commit();
	session.close();
	logger.info("Saved OutputFile into database successfully");

	// do some searches
	logger.info("Searching for info about job: " + jobID);
	session = HibernateUtil.getSessionFactory().openSession();
	session.beginTransaction();
	List results = session.createCriteria(JobInfo.class)
	    .add(Expression.eq("jobID", jobID))
	    .list();

	for (int i = 0; i < results.size(); i++) {
	    info = (JobInfo) results.get(i);
	    logger.info("Job Info: " + info.getJobID() +
			" - {" + info.getCode() +
			", " + info.getMessage() +
			", " + info.getBaseURL() +
			", " + info.getStartTime() + 
			", " + info.getLastUpdate() +
			", " + info.getClientIP() + 
			", " + info.getClientDN() + 
			", " + info.getServiceName() + "}");
	}
	session.close();

	logger.info("Update job info for job: " + jobID);
	session = HibernateUtil.getSessionFactory().openSession();
	session.beginTransaction();
	Date lastUpdate = new Date();
	int numRows = session.createQuery("update JobInfo info " +
					  "set info.lastUpdate = :lastUpdate " +
					  "where info.jobID = '" +
					  jobID + "'")
	    .setTimestamp("lastUpdate", lastUpdate)
	    .executeUpdate();
	logger.info(numRows + " rows updated");
	session.close();

	logger.info("Searching for job outputs for job: " + jobID);
	session = HibernateUtil.getSessionFactory().openSession();
	session.beginTransaction();
	results = session.createQuery("from JobOutput output " +
				      "left join fetch output.job " +
				      "where output.job.jobID = '" +
				      jobID + "'")
	    .list();
	for (int i = 0; i < results.size(); i++) {
	    output = (JobOutput) results.get(i);
	    logger.info("Job Outputs: " + jobID +
			" - {" + output.getStdOut() +
			", " + output.getStdErr() + "}");
	}
	session.close();

	logger.info("Searching for output files for job: " + jobID);
	session = HibernateUtil.getSessionFactory().openSession();
	session.beginTransaction();
	results = session.createQuery("from OutputFile file " +
				      "left join fetch file.job " +
				      "where file.job.jobID = '" +
				      jobID + "'")
	    .list();
	for (int i = 0; i < results.size(); i++) {
	    file = (OutputFile) results.get(i);
	    logger.info("Output File: " + jobID +
			" - {" + file.getName() +
			", " + file.getUrl() + "}");
	}
	session.close();
    }
}