package edu.sdsc.nbcr.opal.state;

import java.util.Date;

import org.apache.log4j.Logger;

/**
 * The holder class for the job state, used by Hibernate
 * 
 * @author Sriram Krishnan
 */

public class JobInfo {

    // get an instance of the log4j Logger
    private static Logger logger =
	Logger.getLogger(JobInfo.class.getName());

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
    public JobInfo() {}

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
}
