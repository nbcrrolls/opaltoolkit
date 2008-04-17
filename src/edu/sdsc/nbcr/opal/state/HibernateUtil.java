package edu.sdsc.nbcr.opal.state;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import org.hibernate.criterion.Expression;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Iterator;

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