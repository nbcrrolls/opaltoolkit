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
    private String handle;
    private java.sql.Date startTimeDate;
    private java.sql.Time startTimeTime;
    private Date activationTime;
    private Date completionTime;
    private java.sql.Date lastUpdateDate;
    private java.sql.Time lastUpdateTime;
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

    public String getHandle() {
	return handle;
    }

    public void setHandle(String handle) {
	this.handle = handle;
    }

    public java.sql.Date getStartTimeDate() {
	return startTimeDate;
    }

    public void setStartTimeDate(java.sql.Date startTime) {
	this.startTimeDate = startTime;
    }

    public java.sql.Time getStartTimeTime() {
	return startTimeTime;
    }

    public void setStartTimeTime(java.sql.Time startTime) {
	this.startTimeTime = startTime;
    }

    public Date getActivationTime() {
	return activationTime;
    }

    public void setActivationTime(Date activationTime) {
	this.activationTime = activationTime;
    }

    public Date getCompletionTime() {
	return completionTime;
    }

    public void setCompletionTime(Date completionTime) {
	this.completionTime = completionTime;
    }

    public java.sql.Date getLastUpdateDate() {
	return lastUpdateDate;
    }

    public void setLastUpdateDate(java.sql.Date lastUpdate) {
	this.lastUpdateDate = lastUpdate;
    }

    public java.sql.Time getLastUpdateTime() {
	return lastUpdateTime;
    }

    public void setLastUpdateTime(java.sql.Time lastUpdate) {
	this.lastUpdateTime = lastUpdate;
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
