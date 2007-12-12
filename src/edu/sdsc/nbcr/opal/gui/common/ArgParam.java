package edu.sdsc.nbcr.opal.gui.common;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

/**
 * This class represents a tagged or an untagged parameters. 
 * If the tag field is null then the parameters is untagged 
 * otherwise is tagged 
 * 
 * Position is needed for the untagged parameters, when we need to built 
 * the command line
 *  
 * @author clem
 *
 */
public class ArgParam extends ActionForm{
    
	private String id;
	private String tag;
	private String type;
	private String ioType;
	private int position;
	private boolean required;
	private String [] values;
	private String semanticType;
	private String textDesc;
	//these are to hold the values from the form
	private String selectedValue;
	
	private FormFile file;
	
	public ArgParam(){
	    position = -1;
	    id = null;
	    tag =  null;
	    type = null;
	    ioType = null;
	    required = false;
	    values = null;
	    semanticType = null;
	    textDesc = null;
	    selectedValue = null;
	    file = null;
	}
	
    public ArgParam(String id, String tag, String type, String ioType,
            boolean required, String [] values, String semaricType, String textDesc) {
        super();
        file = null;
        position = -1;
        this.id = id;
        this.tag = tag;
        this.type = type;
        this.ioType = ioType;
        this.required = required;
        this.values = values;
        this.semanticType = semaricType;
        this.textDesc = textDesc;
    }
    
    public void reset(){
    	file = null;
    	selectedValue = null;
    }
    
    /**
     * return true if a file has been uploaded
     * @return
     */
    public boolean isFileUploaded(){
    	return (file != null) && (file.getFileName().length() > 0);
    }
    
    public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getIoType() {
		return ioType;
	}
	public void setIoType(String ioType) {
		this.ioType = ioType;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	public String[] getValues() {
        return values;
    }
    public void setValues(String[] values) {
        this.values = values;
    }
    public String getSemanticType() {
        return semanticType;
	}
	public void setSemanticType(String semanticType) {
		this.semanticType = semanticType;
	}
	public String getTextDesc() {
		return textDesc;
	}
	public void setTextDesc(String textDesc) {
		this.textDesc = textDesc;
	}
    public String getSelectedValue() {
        return selectedValue;
    }
    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }
    public FormFile getFile() {
        return file;
    }
    public void setFile(FormFile file) {
        this.file = file;
    }
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
	
	public String toString(){
	    String str = "Param " + id + " is tagged with " + tag + ", type is: " + type + "/" + ioType + ", ";
	    if (required) str += "is required, ";
	    else str += "is not required, ";
	    if (values != null) {
	        str += "possible value are: ";
	        for (int i = 0; i < values.length; i++)
	            str += values[i] + ", ";
	    }
	    if (selectedValue != null) str += " selected value: " + selectedValue + ", ";
	    if (file != null) str += " the file is: " + file.getFileName() + ", ";
	    if (tag == null) str += " its position is " + position;
	    return str;
	}

}
