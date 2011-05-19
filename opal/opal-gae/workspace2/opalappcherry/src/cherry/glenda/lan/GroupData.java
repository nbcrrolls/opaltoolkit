package cherry.glenda.lan;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import com.google.appengine.api.datastore.Key;

import cherry.glenda.lan.ArgFlagData;
import cherry.glenda.lan.ArgParamData;

/**
 * This class is used to group more ArgFlag or ArgParam together.
 * 
 * 
 * This class has the following fields:
 * <ul>
 * <li>ArgFlag [] argFlags - contains references to the ArgFlags that are part of this group
 * <li>ArgParam [] argParams - contains references to the ArgParams that are part of this group
 * <li>String name - the name of this group
 * <li>boolean exclusive - true if this group is exclusive
 * <li>boolean required - true if all the elements of this group are required
 * <li>String textDesc - a textual description of this group
 * <li>String semanticType - semantic description for this group
 * </ul>
 * 
 *@modify by sqm
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class GroupData {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key GroupID;
 

	@Persistent
	private AppMetadataData appMetadata;
	@Persistent
	private String name;
	@Persistent
	private boolean exclusive;
	@Persistent
	private boolean required;
	@Persistent
	private String textDesc;
	@Persistent
	private String semanticType;
	@Persistent
	private String elements;
	@Persistent(mappedBy = "groupData")
	private List<ArgFlagData> GargFlags = new ArrayList<ArgFlagData>();
	public List<ArgFlagData> getArgFlags() {
        return GargFlags;
    }
    public void setArgFlags(List<ArgFlagData> GargFlags) {
        this.GargFlags = GargFlags;
    }
	@Persistent(mappedBy = "groupData")
	private List<ArgParamData> GargParams = new ArrayList<ArgParamData>();
	public List<ArgParamData> getArgParams() {
        return GargParams;
    }
    public void setArgParams(List<ArgParamData> GargParams) {
        this.GargParams = GargParams;
    } 
	
	/**
	 * default constructor
	 */
	public GroupData() {
		
		this.GargFlags = null;
		this.GargParams = null;
		this.name = null;
		this.exclusive = false;
		this.required = false;
		this.textDesc = null;
		this.semanticType = null;
		this.elements = null;
	}
	
	/**
	 * return a textual representation of this string
	 */
	public String toString(){
		String str = "Group " + name + " ";
		if (exclusive == true ) str += "is exclusive ";
		else str += "is not exclusive ";
		if (required == true ) str += "is required ";
		else str += "is not required ";
		str += " and is description is: " + textDesc;
		if (GargParams != null ){
			str += "\n    Its params are: ";
			for (int i = 0; i < GargParams.size(); i++ )
				str += GargParams.get(i).getRealID()+ " ";
		}//if
		if (GargFlags != null ){
			str += "\n    Its flags are: ";
			for (int i = 0; i < GargFlags.size(); i++ )
				str += GargFlags.get(i).getRealID() + " ";
		}//if
		
		return str;
	}


	   
    //         -----------------------------
    //               Getter and Setter
    //         -----------------------------
    


    /**
     * @primaryKey GroupID
     */
    public Key getGroupID() {
        return GroupID;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param elements the elements to set
     */
    public void setElements(String elements) {
        this.elements = elements;
    }
    /**
     * @return the elements
     */
    public String getElements() {
        return elements;
    }


   

    /**
     * @return the exclusive
     */
    public boolean isExclusive() {
        return exclusive;
    }


    /**
     * @param exclusive the exclusive to set
     */
    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }


    /**
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }


    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }


    /**
     * @return the textDesc
     */
    public String getTextDesc() {
        return textDesc;
    }


    /**
     * @param textDesc the textDesc to set
     */
    public void setTextDesc(String textDesc) {
        this.textDesc = textDesc;
    }


    /**
     * @return the semanticType
     */
    public String getSemanticType() {
        return semanticType;
    }


    /**
     * @param semanticType the semanticType to set
     */
    public void setSemanticType(String semanticType) {
        this.semanticType = semanticType;
    }


}


