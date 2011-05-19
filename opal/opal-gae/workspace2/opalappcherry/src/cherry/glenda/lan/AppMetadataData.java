package cherry.glenda.lan;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;
  
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class AppMetadataData
{
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long AppMetadataID;
	 
	@Persistent
	private String serviceName;
	@Persistent
    private String usage;
    @Persistent
    private Text info;
    @Persistent
    private String URL;
    /*
    @Persistent
    private ArrayList<ArgFlag> argFlags;
    @Persistent
    private ArrayList<ArgParam> argParams;
    */
    @Persistent(mappedBy = "appMetadata")
    private List<GroupData> groups = new ArrayList<GroupData>();
    public List<GroupData> getGroups() {
        return groups;
    }
    public void setGroups(List<GroupData> groups) {
        this.groups = groups;
    }
    
    @Persistent
    private String separator;
    //these are to hold the values from the form 
    @Persistent
    private boolean parallel;
    @Persistent
    private String binaryLocation;
    /*
    private String cmdLine;
    private FormFile [] files;
    private String jobId;
    private String numCpu;
    private String userEmail;
    private boolean addFile;
   
    private boolean extractInputs;
    private ArrayList formFiles = null;
    private FormFile inputFile = null;  // needed by formFiles
    private int index;                  // needed by formFiles
    */
    public AppMetadataData() {
        serviceName = null;
        usage = null;
        info = null;
        URL = null;
       // argFlags = null;
       // argParams = null;
        separator = null;  
        parallel = false;
        binaryLocation = null;
        /*
        cmdLine = null;
        userEmail = null;
        files = new FormFile[1];
        addFile = false;
       
        extractInputs = false;
        formFiles = new ArrayList();
        index = 0;
        */
    }
     
    /**
     * @Primary Key AppMetadataID
     */
    public Long getAppMetadataID() {
        return AppMetadataID;
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
     * @usage
     */
    public String getUsage() {
        return usage;
    }
    public void setUsage(String usage) {
        this.usage = usage;
    }

    /**
     * @info
     */
    public Text getInfo() {
        return info;
    }
    public void setInfo(Text info) {
        this.info = info;
    }

    /**
     * uRL
     */
    public String getURL() {
        return URL;
    }
    public void setURL(String url) {
        URL = url;
    }
    /**
     * parallel
     */
    public boolean getparallel() {
        return parallel;
    }
    public void setparallel(boolean parallel) {
    	this.parallel = parallel;
    }
    /**
     * binaryLocation
     */
    public String getbinaryLocation() {
        return binaryLocation;
    }
    public void setbinaryLocation(String binaryLocation) {
    	this.binaryLocation = binaryLocation;
    }
    /**
     * @argFlags
     
    public ArrayList<ArgFlag> getArgFlags() {
        return argFlags;
    }
    public void setArgFlags(ArrayList<ArgFlag> argFlags) {
        this.argFlags = argFlags;
    }

    /**
     * argParams
     
    public ArrayList<ArgParam> getArgParams() {
        return argParams;
    }
    public void setArgParams(ArrayList<ArgParam> argParams) {
        this.argParams = argParams;
    }
     */
    /*
    public ArrayList<Group> getGroups() {
        return groups;
    }
    public void setGroups(ArrayList<Group> groups) {
        this.groups = groups;
    }
     */
    /**
     * @separator
     */
    public String getSeparator() {
        return separator;
    }
    public void setSeparator(String separator) {
        this.separator = separator;
    }
    public String toString(){
        String str = "URL: " + URL + "\n";
        str += "Usage: " + usage + "\n";
        str += "Info:\n";
        if ( info != null ) {
              str += info + "\n";
        }
        /*
        if ((argFlags != null) || (argParams != null)) {
            //display also the args part
            str += "The types of the application are:\n";
        }
        if (argParams != null){
            str += "Parameters separator is " + separator + " and their type is:\n";
            for (int i = 0; i < argParams.size(); i++){
                str += argParams.get(i).toString() + "\n";
            }
        }
        if (argFlags != null){
            str += "Flags:\n";
            for (int i = 0; i < argFlags.size(); i++){
                str += argFlags.get(i).toString() + "\n";
            }
        }
        */
        if ( groups != null ) {
	    str += "Groups:\n";
	    for (int i = 0; i < groups.size(); i++){
                str += groups.get(i).toString() + "\n";
            }
        }
        return str;
    }
}
