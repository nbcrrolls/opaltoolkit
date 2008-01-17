package edu.sdsc.nbcr.opal.gui.common;


/**
 * This class is used to group more ArgFlag or ArgParam together.
 * 
 * @author clem
 *
 */
public class Group {
	
	private ArgFlag [] argFlags;
	private ArgParam [] argParams;
	private String name;
	private boolean exclusive;
	private boolean required;
	private String textDesc;
	private String semanticType;
	
	public Group() {
		
		this.argFlags = null;
		this.argParams = null;
		this.name = null;
		this.exclusive = false;
		this.required = false;
		this.textDesc = null;
		this.semanticType = null;
	}
	
	
	public String toString(){
		String str = "Group " + name + " ";
		if (exclusive == true ) str += "is exclusive ";
		else str += "is not exclusive ";
		if (required == true ) str += "is required ";
		else str += "is not required ";
		str += " and is description is: " + textDesc;
		if (argParams != null ){
			str += "\n    Its params are: ";
			for (int i = 0; i < argParams.length; i++ )
				str += argParams[i].getId() + " ";
		}//if
		if (argFlags != null ){
			str += "\n    Its flags are: ";
			for (int i = 0; i < argFlags.length; i++ )
				str += argFlags[i].getId() + " ";
		}//if
		return str;
	}
	
	//         -----------------------------
	//               Getter and Setter
	//         -----------------------------
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isExclusive() {
		return exclusive;
	}
	public void setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	public String getTextDesc() {
		return textDesc;
	}
	public void setTextDesc(String textDesc) {
		this.textDesc = textDesc;
	}
	public String getSemanticType() {
		return semanticType;
	}
	public void setSemanticType(String semanticType) {
		this.semanticType = semanticType;
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

}
