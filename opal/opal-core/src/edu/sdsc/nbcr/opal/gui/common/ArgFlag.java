package edu.sdsc.nbcr.opal.gui.common;

import org.apache.struts.action.ActionForm;

/**
 * this class is suppsed to keep the data necessary to represet a flag
 * @author clem
 *
 */
public class ArgFlag {

	private String id;
	private String tag;
	private String textDesc;
	private boolean selected; 
	
    public ArgFlag(){
	    id = null;
	    tag = null;
	    textDesc = null;
	}
    
    public ArgFlag(String id, String tag, String textDesc){
    	this.id = id;
    	this.tag = tag;
    	this.textDesc = textDesc;
    }
    
    public void reset(){
    	selected = false;
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
	public String getTextDesc() {
		return textDesc;
	}
	public void setTextDesc(String textDesc) {
		this.textDesc = textDesc;
	}

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public String toString(){
        return "Tag " + id + " has a tag " + tag + " (" + textDesc + ")";
    }
    
}
