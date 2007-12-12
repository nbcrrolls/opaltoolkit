package edu.sdsc.nbcr.opal.gui.common;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

public class AppMetadata extends ActionForm{
	
	private String serviceName;
	private String usage;
	private String [] info;
	private String URL;
	private ArgFlag [] argFlags;
	private ArgParam [] argParams;
	private Group [] groups;
	private String separator;
	//these are to hold the values from the form 
	private String cmdLine;
	private FormFile file;
	private int numFile;
	private String jobId;
	


	public AppMetadata() {
        serviceName = null;
        usage = null;
        info = null;
        URL = null;
        argFlags = null;
        argParams = null;
        separator = null;
        cmdLine = null;
        file = null;
        numFile = 0;
	}
    
    public boolean isArgMetadataEnable() {
        if ( (argFlags == null) && (argParams == null) ) {
            return false;
        }
        else return true;
    }
    
    public String toString(){
        String str = "URL: " + URL + "\n";
        str += "Usage: " + usage + "\n";
        str += "Info:\n";
        if ( info != null ) {
            for (int i = 0; i < info.length; i++){
                str += info[i] + "\n";
            }
        }
        if ((argFlags != null) || (argParams != null)) {
            //display also the args part
            str += "The types of the application are:\n";
        }
        if (argParams != null){
            str += "Parameters separator is " + separator + " and their type is:\n";
            for (int i = 0; i < argParams.length; i++){
                str += argParams[i].toString() + "\n";
            }
        }
        if (argFlags != null){
            str += "Flags:\n";
            for (int i = 0; i < argFlags.length; i++){
                str += argFlags[i].toString() + "\n";
            }
        }
        if ( groups != null ) {
        	str += "Groups:\n";
        	for (int i = 0; i < groups.length; i++){
                str += groups[i].toString() + "\n";
            }
        }
        return str;
    }
    
    /**
     * it returns the number of files submitted by the user in the ArgParam array
     * 
     * @return
     */
    public int getNumArgFileSubmitted(){
        if (isArgMetadataEnable() ) {
            int numFile = 0;
            for (int i = 0; i < argParams.length; i++){
                if ( argParams[i].isFileUploaded() ) 
                    numFile++;
            }
            return numFile;
        }
        else return -1;
    }
    
    /**
     * it returns the i-th ArgParam value containing a file submitted by the user
     * 
     * @param i
     * @return
     */
    public ArgParam getArgFileSubmitted(int position){
        int currentPosition = 0;
        for (int i = 0; i < argParams.length; i++ ) {
            if (argParams[i].isFileUploaded() ) {
                if (position == currentPosition) {
                    return argParams[i];
                }
                //we are not yet at the right position let's increment
                currentPosition++;
            }//if
        }//for
        return null;
    }
    
    /**
     * returns the number of untagged parameters present in the data structure
     * @return
     */
    public int getNumUnttagedParams(){
        int counter = 0;
        for (int i = 0; i < argParams.length; i++ ){
            if (argParams[i].getTag() == null )
                counter++;
        }
        return counter;
    }

    /**
     * resets the place holder of the submitted values in the entire data structure
     * Currently resets only the check box values
     */
    public void reset(ActionMapping mapping, HttpServletRequest  request){
        if ( argFlags != null ) 
            for (int i = 0; i < argFlags.length; i++ ){
                argFlags[i].setSelected(false);
            }
        if ( argParams != null )
            for (int i = 0; i < argParams.length; i++ ){
                argParams[i].reset();
            }
    }
    
    /**
     * returns the ArgFlag with has the id equals to id
     * if it does not find a ArgFlag with an id equals to id it returns null
     * 
     */
    public ArgFlag getArgFlagId(String id){    	
    	for (int i = 0; i < argFlags.length; i++ ) {
    		if ( argFlags[i].getId().equals(id) )
    			return argFlags[i];
    	}
    	return null;
    }
    
    public ArgParam getArgParamId(String id){
    	for (int i = 0; i < argParams.length; i++ ) {
    		if ( argParams[i].getId().equals(id) )
    			return argParams[i];
    	}
    	return null;
    }
    
    /** -----    below only getter and setter methods   -------    */
    public String getSeparator() {
        return separator;
    }
    public void setSeparator(String separator) {
        this.separator = separator;
    }
	public String getUsage() {
		return usage;
	}
	public void setUsage(String usage) {
		this.usage = usage;
	}
	public String [] getInfo() {
		return info;
	}
	public void setInfo(String [] info) {
		this.info = info;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public ArgFlag[] getArgFlags() {
		return argFlags;
	}
	public void setArgFlags(ArgFlag[] argFlags) {
		this.argFlags = argFlags;
	}
	public ArgParam[] getArgParams() {
		return argParams;
	}
	public void setArgParams(ArgParam[] argParams) {
		this.argParams = argParams;
	}
    public String getURL() {
        return URL;
    }
    public void setURL(String url) {
        URL = url;
    }
    public String getCmdLine() {
        return cmdLine;
    }
    public void setCmdLine(String cmdLine) {
        this.cmdLine = cmdLine;
    }
    public FormFile getFile() {
        return file;
    }
    public void setFile(FormFile file) {
        this.file = file;
    }
    public int getNumFile() {
        return numFile;
    }
    public void setNumFile(int numFile) {
        this.numFile = numFile;
    }
    public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public Group[] getGroups() {
		return groups;
	}

	public void setGroups(Group[] groups) {
		this.groups = groups;
	}

}
