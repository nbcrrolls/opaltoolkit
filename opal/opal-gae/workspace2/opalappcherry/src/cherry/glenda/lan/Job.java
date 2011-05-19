package cherry.glenda.lan;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import java.util.Date;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class Job {

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long JobID;
	@Persistent
	private String JobOpalID;
	@Persistent
	private String status;
	@Persistent
	private int statusCode;
	@Persistent
	private String serviceType; 
	@Persistent
	private String serviceName;
	@Persistent
	private String UserID; 
	@Persistent
	private String UserEmail; 
	@Persistent
	private String message;
	@Persistent
	private Date submitTime; 
	@Persistent
	private Date activeTime;
	@Persistent
	private Date endTime;
	@Persistent
	private String beseURL;
	
	@Persistent(mappedBy = "job")
	private List<JobArgFlagData> JobArgFlags = new ArrayList<JobArgFlagData>();
	public List<JobArgFlagData> getJobArgFlags() {
        return JobArgFlags;
    }
    public void setJobArgFlags(List<JobArgFlagData> JobArgFlags) {
        this.JobArgFlags = JobArgFlags;
    }   
	@Persistent(mappedBy = "job")
	private List<JobArgParamData> JobArgParams = new ArrayList<JobArgParamData>();
	public List<JobArgParamData> getJobArgParams() {
        return JobArgParams;  
    }
    public void setJobArgParams(List<JobArgParamData> JobArgParams) {
        this.JobArgParams = JobArgParams;
    } 
	   
	public Job(){
		JobOpalID = null;
	}
	public Job(String status, int statusCode, String serviceType,
			String serviceName,String UserID,String UserEmail,String message,
			Date submitTime,Date activeTime,Date endTime,
			String beseURL){
    	this.status = status;
    	this.statusCode = statusCode;
    	this.serviceType = serviceType;
    	this.serviceName = serviceName;
    	this.UserID = UserID;
    	this.UserEmail = UserEmail;
    	this.message = message;
    	this.submitTime = submitTime;
    	this.activeTime = activeTime;
    	this.endTime = endTime;
    	this.beseURL = beseURL;
 
       }
	/**
     * @primaryKey JobID
     */
    public Long getJobID() {
        return JobID;
    }
    /**
     * @JobOpalID
     */
    public String getJobOpalID() {
        return JobOpalID;
    }
    public void setJobOpalID(String JobOpalID) {
        this.JobOpalID = JobOpalID;
    }
    /**
     * @status
     */
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * @statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    
    /**
     * @serviceType
     */
    public String getServiceType() {
        return serviceType;
    }
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    
    /**
     * @serviceName
     */
    public String getServiceName() {
        return serviceName;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    /**
     * @UserID
     */
    public String getUserID() {
        return UserID;
    }
    public void setUserID(String UserID) {
        this.UserID = UserID;
    }
    /**
     * @UserEmail
     */
    public String getUserEmail() {
        return UserEmail;
    }
    public void setUserEmail(String UserEmail) {
        this.UserEmail = UserEmail;
    }
    /**
     * @message
     */
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * submitTime
     */
    public Date getSubmitTime() {
        return submitTime;
    }
    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }
    
    /**
     * activeTime
     */
    public Date getActiveTime() {
        return activeTime;
    }
    public void setActiveTime(Date activeTime) {
        this.activeTime = activeTime;
    }
    
    /**
     * endTime
     */
    public Date getEndTime() {
        return endTime;
    }
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    
    /**
     * beseURL
     */
    public String getBeseURL() {
        return beseURL;
    }
    public void setBeseURL(String beseURL) {
        this.beseURL = beseURL;
    }
    
}

