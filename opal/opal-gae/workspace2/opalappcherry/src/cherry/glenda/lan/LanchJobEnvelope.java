package cherry.glenda.lan;

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
public class LanchJobEnvelope {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long LanchJobEnvePID;
	@Persistent
	private String jobOpalID;
	@Persistent
    private int code;
    @Persistent
    private String message;
    @Persistent
    private String baseURL; 
    LanchJobEnvelope(){}
    /**
     * @primaryKey
     */
    public Long getLanchJobEnvePID() {
        return LanchJobEnvePID;
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
     * @code
     */
    public int getcode() {
        return code;
    }
    public void setcode(int code) {
        this.code = code;
    }
    /**
     * @message
     */
    public String getmessage() {
        return message;
    }
    public void setmessage(String message) {
        this.message = message;
    }
    /**
     * @baseURL
     */
    public String getbaseURL() {
        return baseURL;
    }
    public void setbaseURL(String baseURL) {
        this.baseURL = baseURL;
    }
}
