package cherry.glenda.lan;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

 /*  
 * @modify by sqm
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class JobArgParamData {
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key JobParamID; 
	@Persistent
	private Job job;
	@Persistent
    private String realID;
	@Persistent
	private String tag;
	@Persistent
	private int position;
	@Persistent
	private Long JobID;
	//these are to hold the values from the form
	@Persistent
	private String selectedValue;
     
	@Persistent
    private JobInputFile jobInputFile;
    /**
     * @jobInputFile
     */
    public JobInputFile getJobInputFile() {
        return jobInputFile;
    }
    public void setJobInputFile(JobInputFile jobInputFile) {
        this.jobInputFile = jobInputFile;
    }
	/**
	 * default constructor
	 */
	public JobArgParamData(){
	    position = 0;
	    realID = null;
	    tag =  null;
	    selectedValue = null;
	    jobInputFile = null;
	}
	    
	/**
	 * Parametrized constructor see at the top of the page for the information on the various field
	 * 
	 */
    public JobArgParamData(String realID, String tag) {
        super();
        jobInputFile = null;
        selectedValue = null;
        position = -1;
        this.realID = realID;
        this.tag = tag;
        }
        
    /**
     * Reset the value inputed by the user
     */
    public void reset(){
    	jobInputFile = null;
    	selectedValue = null;
    }
    
    /**
     * Return true if a file has been uploaded
     * 
     
    public boolean isFileUploaded(){
    	for(int j = 0;j<inputfiles.size();j++)
    	{
    	return (inputfiles != null) && (inputfiles.get(j).getFileName().length() > 0);
    	}
		return required;
    }
    */
    
     /**
     * @Primary Key ParamID
     */
    public Key getParamID() {
        return JobParamID;
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
   
    /**
     * @selectedValue
     */
    public String getSelectedValue() {
        return selectedValue;
    }
    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }
  
}



