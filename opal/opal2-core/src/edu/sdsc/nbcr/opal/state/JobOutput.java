package edu.sdsc.nbcr.opal.state;

import java.util.Date;

import org.hibernate.Session;

import java.util.List;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * The holder class for the job outputs, used by Hibernate
 * 
 * @author Sriram Krishnan
 */

public class JobOutput {

    // get an instance of the log4j Logger
    private static Logger logger =
	Logger.getLogger(JobOutput.class.getName());

    // the jobID for this job
    private String jobID;

    // standard output and error
    private String stdOut;
    private String stdErr;

    public JobOutput() {}

    // getter and setter methods
    public String getJobID() {
	return jobID;
    }

    public void setJobID(String jobID) {
	this.jobID = jobID;
    }

    public String getStdOut() {
	return stdOut;
    }

    public void setStdOut(String stdOut) {
	this.stdOut = stdOut;
    }

    public String getStdErr() {
	return stdErr;
    }

    public void setStdErr(String stdErr) {
	this.stdErr = stdErr;
    }
}
