package cherry.glenda.lan;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
/**
 * This class is supposed to keep the data necessary to represent a flag
 * 
 * This class has the following fields:
 * <ul>
 * <li>String id - a unique id representing this tag
 * <li>String tag - the tag used to activate this flag on the command line
 * <li>String textDesc - the textual description for this flag
 * <li>boolean selected - this field is used by struts to place the input of the user when he submit the form
 * <li>boolean defaultFlag - this field is the default value from the appMetadata.
 * </li>
 * 
 * @modify by sqm
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ArgFlagData {
 
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key FlagID;
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
	private String textDesc;
	@Persistent
	private boolean defaultFlag; 
	@Persistent
	private boolean selected;
	@Persistent
	private int position;

	/**
	 * default constructor
	 */
    public ArgFlagData(){
    	realID = null;
	    tag = null;
	    textDesc = null;
	    defaultFlag = false;
	    selected = false;
	    position = 0;
	}
    
    /**
     * Parametrized constructor see at the top of the page for the information on the various field
     * 
     * @param id a unique id representing this tag
     * @param tag the tag used to activate this flag on the command line
     * @param textDesc the textual description for this flag
     */
    public ArgFlagData(String realID, String tag, String textDesc,boolean defaultFlag,boolean selected,int position){
    	this.realID = realID;
    	this.tag = tag;
    	this.textDesc = textDesc;
    	this.defaultFlag = defaultFlag;
    	this.selected = selected;
    	this.position = position;
    }
    
    
    /**
     * this method is called to reset the input of the user, aka selected field
     */
    public void reset(){
    	defaultFlag = false;
    }
	
    /**
     * @primaryKey FlagID
     */
    public Key getFlagID() {
        return FlagID;
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
     * @textDesc
     */
    public String getTextDesc() {
        return textDesc;
    }
    public void setTextDesc(String textDesc) {
        this.textDesc = textDesc;
    }

    /**
     * @defaultFlag
     */
    public boolean getDefault() {
        return defaultFlag;
    }
    public void setDefault(boolean defaultFlag) {
        this.defaultFlag = defaultFlag;
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
     * returns a textual representation of this ArgFlag 
     */
    /*
    public String toString(){
        return "Tag: " + id +"default: " + selected +" has a tag: " + tag + " (" + textDesc + ")";
    }
    */
    
}


