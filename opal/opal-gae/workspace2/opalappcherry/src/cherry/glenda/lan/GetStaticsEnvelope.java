package cherry.glenda.lan;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;
/*
 * the lanched job success , faild , or other states the request envelop and response envelops are different.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class GetStaticsEnvelope {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long getstaticsEnvePID;
	@Persistent
	private Date startTime;
	@Persistent
	private Date activationTime;
    @Persistent
    private Date completionTime;
    @Persistent
    private String jobOpalID; 
    GetStaticsEnvelope(){}
    /**
     * @primaryKey
     */
    public Long getLanchJobEnvePID() {
        return getstaticsEnvePID;
    }
    
    /**
     * @jobOpalID
     */
    public String getjobOpalID() {
        return jobOpalID;
    }
    public void setjobOpalID(String jobOpalID) {
        this.jobOpalID = jobOpalID;
    }
    /**
     * @startTime
     */
    public Date getstartTime() {
        return startTime;
    }
    public void setstartTime(Date startTime) {
        this.startTime = startTime;
    }
    /**
     * activationTime
     */
    public Date getactivationTime() {
        return activationTime;
    }
    public void setactivationTime(Date activationTime) {
        this.activationTime = activationTime;
    }
    /**
     * @completionTime
     */
    public Date getcompletionTime() {
        return completionTime;
    }
    public void setcompletionTime(Date completionTime) {
        this.completionTime = completionTime;
    }
}
