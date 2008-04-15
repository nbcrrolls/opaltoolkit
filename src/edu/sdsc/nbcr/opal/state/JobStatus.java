package edu.sdsc.nbcr.opal.state;

import java.util.Date;

import org.hibernate.Session;

import java.util.List;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * The holder class for the job state, used by Hibernate
 * 
 * @author Sriram Krishnan
 */

public class JobStatus {

    // get an instance of the log4j Logger
    private static Logger logger =
	Logger.getLogger(JobStatus.class.getName());

    // the jobID for this job
    private String jobID;

    // the other fields - self explanatory
    private int code;
    private String message;
    private String baseURL;
    private Date startTime;
    private Date lastUpdate;
    private String clientDN;
    private String clientIP;
    private String serviceName;

    // default constructor
    public JobStatus() {}

    // getter and setter methods
    public String getJobID() {
	return jobID;
    }

    public void setJobID(String jobID) {
	this.jobID = jobID;
    }

    public int getCode() {
	return code;
    }

    public void setCode(int code) {
	this.code = code;
    }

    public String getMessage() {
	return message;
    }

    public void setMessage(String message) {
	this.message = message;
    }

    public String getBaseURL() {
	return baseURL;
    }

    public void setBaseURL(String baseURL) {
	this.baseURL = baseURL;
    }

    public Date getStartTime() {
	return startTime;
    }

    public void setStartTime(Date startTime) {
	this.startTime = startTime;
    }

    public Date getLastUpdate() {
	return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
	this.lastUpdate = lastUpdate;
    }

    public String getClientDN() {
	return clientDN;
    }

    public void setClientDN(String clientDN) {
	this.clientDN = clientDN;
    }

    public String getClientIP() {
	return clientIP;
    }

    public void setClientIP(String clientIP) {
	this.clientIP = clientIP;
    }

	
    public String getServiceName() {
	return serviceName;
    }

    public void setServiceName(String serviceName) {
	this.serviceName = serviceName;
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
