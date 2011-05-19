package cherry.glenda.lan;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
/**
  * @modify by sqm
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class JobArgFlagData {
 
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key JobFlagID;
	@Persistent
	private Job job;
	@Persistent
	private String realID;
	@Persistent
	private String tag;
	@Persistent
	private boolean selected;
	@Persistent
	private int position;
	@Persistent
	private Long JobID;
	/**
	 * default constructor
	 */
    public JobArgFlagData(){
    	realID = null;
	    tag = null;
	    selected = false;
	    position = 0;
	}
    
     public JobArgFlagData(String realID, String tag, boolean selected,int position){
    	this.realID = realID;
    	this.tag = tag;
    	this.selected = selected;
    	this.position = position;
    }
    
    
    /**
     * @primaryKey FlagID
     */
    public Key getJobFlagID() {
        return JobFlagID;
    }
    /**
     * @id
     */
    public String getRealID() {
        return realID;
    }
    public void setId(String realID) {
        this.realID = realID;
    }

    /**
     * @tag
     */
    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }

     /**
     * @selected
     */
    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    /**
     * @position
     */
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    /**
     * @JobID
     */
    public Long getJobID() {
        return JobID;
    }
    public void setJobID(Long JobID) {
        this.JobID = JobID;
    }
}



