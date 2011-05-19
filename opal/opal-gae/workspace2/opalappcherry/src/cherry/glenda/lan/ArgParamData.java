package cherry.glenda.lan;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;



/**
 * This class represents a tagged or an untagged parameters. 
 * If the tag field is null then the parameters is untagged 
 * otherwise is tagged.<br/>
 * 
 * Position is needed for the untagged parameters, when we need to built 
 * the command line<br/>
 * 
 * This bean has the following fields:
 * <ul>
 * <li>String id - a unique id representing this field
 * <li>String tag - the tag used to pass this parameter on the command line, it is null if this is a untagged param
 * <li>String type - the type of this parameter (see wsdl of opal for more info)
 * <li>String ioType - the ipType of this parameter (see  wsdl of opal for more info)
 * <li>int position - the position on the command line (used only for untagged param)
 * <li>boolean required - true if this is a required param
 * <li>ArrayList<String> values - if it is a multiple choice param this array contains the various possibilities
 * <li>String semanticType - not used at the moment
 * <li>String textDesc - it holds the textual description of this parameters
 * <li>String defaultParam -it holds for the default value
 * <li>String selected - used by strutus to put the input of the user when he submits the form, it also holds the default value if specified
 * <li>FormFile file - used by strutus to put a file if this parameter is an input file
 * </ul> 
 *  
 * @modify by sqm
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ArgParamData {
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key ParamID; 
	@Persistent
	private GroupData groupData;
	/*
	@Persistent
	private Job job;
	*/
    @Persistent
    private String realID;
	@Persistent
	private String tag;
	@Persistent
	private String type;
	@Persistent
	private String ioType;
	@Persistent
	private int position;
	@Persistent
	private boolean required;
	@Persistent
	private ArrayList<String> values;
	@Persistent
	private String semanticType;
	@Persistent
	private String textDesc;
	@Persistent
	private String defaultParam;
	//these are to hold the values from the form
	@Persistent
	private String selectedValue;
    
    /**
	 * default constructor
	 */
	public ArgParamData(){
	    position = 0;
	    realID = null;
	    tag =  null;
	    type = null;
	    ioType = null;
	    required = false;
	    values = null;
	    semanticType = null;
	    textDesc = null;
	    defaultParam = null;
	    selectedValue = null;
	    
	}
	
	/**
	 * Parametrized constructor see at the top of the page for the information on the various field
	 * 
	 */
    public ArgParamData(String realID, String tag, String type, String ioType,
            boolean required, ArrayList<String> values, 
            String semaricType, 
            String textDesc,String defaultParam) {
        super();
        
        selectedValue = null;
        position = 0;
        this.realID = realID;
        this.tag = tag;
        this.type = type;
        this.ioType = ioType;
        this.required = required;
        this.values = values;
        this.semanticType = semaricType;
        this.textDesc = textDesc;
        this.defaultParam = defaultParam;
         }
    
    /**
     * Reset the value inputed by the user
     */
    public void reset(){
    	
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
     * it returns a textual representation of this instance
     * 
     */
    public String toString(){
        String str = "Param " + realID + " is tagged with " + tag + ", type is: " + type + "/" + ioType + ", ";
        if (required) str += "is required, ";
        else str += "is not required, ";
        if (values != null) {
            str += "possible value are: ";
            for (int i = 0; i < values.size(); i++)
                str += values.get(i) + ", ";
        }
        if (defaultParam != null) str += " default value: " + defaultParam + ", ";
       // if (inputfiles != null) str += " the file is: " + inputfile.getFileName() + ", ";
        if (tag == null) str += " its position is " + position;
        return str;
    }

    /**
     * @Primary Key ParamID
     */
    public Key getParamID() {
        return ParamID;
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
     * @type
     */
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @ioType
     */
    public String getIoType() {
        return ioType;
    }
    public void setIoType(String ioType) {
        this.ioType = ioType;
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
     * @required
     */
    public boolean isRequired() {
        return required;
    }
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @values
     */
    public ArrayList<String> getValues() {
        return values;
    }
    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    /**
     * @semanticType
     */
    public String getSemanticType() {
        return semanticType;
    }
    public void setSemanticType(String semanticType) {
        this.semanticType = semanticType;
    }

    /**
     * @textDesc
     */
    public String getTextDesc() {
        return textDesc;
    }
    public void setTextDesc(String textDesc) {
        this.textDesc = textDesc;
    }

    /**
     * @defaultParam
     */
    public String getDefaultParam() {
        return defaultParam;
    }
    public void setDefaultParam(String defaultParam) {
        this.defaultParam = defaultParam;
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


