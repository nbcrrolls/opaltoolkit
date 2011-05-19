package cherry.glenda.lan;


import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;
/**
 * 
 * write by sqm hahadada
 *  srrs
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class JobInputFile {
	    
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key jobInputFileID;
	
    @Persistent
	private String fileName;
	@Persistent
	private String fileType;
	@Persistent
	private String location;
	@Persistent
	private String fieldName;
	
	/**
	 * default constructor
	 */
    public JobInputFile(){
    	fileName = null;
    	fileType = null;
    	fieldName = null;
    	location = null;
	}
    
   
    public JobInputFile(String fieldName, String fileName, String fileType, String location){
    	this.fieldName = fieldName;
    	this.fileName = fileName;
    	this.fileType = fileType;
    	this.location = location;
    }
      
    
    /**
     * @primaryKey InputFileID
     */
    public Key getInputFileID() {
        return jobInputFileID;
    }
    /**
     * @fieldName
     */
    public void setfieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    public String getfieldName() {
        return fieldName; 
    }
    /**
     * @fileName
     */
    public void setfileName(String fileName) {
        this.fileName = fileName;
    }
    public String getfileName() {
        return fileName;
    }
    /**
     * @fileType
     */
    public String getfileType() {
        return fileType;
    }
    public void setfileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * @location
     */
    public String getContent() {
        return location;
    }
    public void setContent(String location) {
        this.location = location;
    }

         
}


