package edu.sdsc.nbcr.opal.state;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

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

	// initialize entry
	JobStatus entry = new JobStatus();
	entry.setJobID("app" + System.currentTimeMillis());
	entry.setCode(0);
	entry.setMessage("This is a test");
	entry.setBaseURL("http://localhost/test");
	entry.setStartTime(new Date());
	entry.setLastUpdate(new Date());
	entry.setClientDN("CN=Test");
	entry.setClientIP("127.0.0.1");
	entry.setServiceName("Command-line");

	// save system entry
	logger.info("Trying to save JobStatus into database");
        session.beginTransaction();
	session.save(entry);
	session.getTransaction().commit();
	session.close();
	logger.info("Saved JobStatus into database successfully");

	// list entries
	logger.info("Listing JobStatus's");
	session = HibernateUtil.getSessionFactory().openSession();
	session.beginTransaction();
	List results = session.createQuery("from JobStatus").list();
	session.getTransaction().commit();
	for (int i = 0; i < results.size(); i++) {
	    entry = (JobStatus) results.get(i);
	    logger.info("Job Status: " + entry.getJobID() +
			" - {" + entry.getCode() +
			", " + entry.getMessage() +
			", " + entry.getBaseURL() +
			", " + entry.getStartTime() + 
			", " + entry.getLastUpdate() +
			", " + entry.getClientIP() + 
			", " + entry.getClientDN() + 
			", " + entry.getServiceName() + "}");
	}
	session.close();
    }
}