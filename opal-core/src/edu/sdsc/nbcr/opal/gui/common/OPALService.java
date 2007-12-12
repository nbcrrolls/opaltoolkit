package edu.sdsc.nbcr.opal.gui.common;

public class OPALService {
    private String serviceName;
    private String serviceID;
    private String URL;
    
    
    public OPALService(){
    	URL = null;
    	serviceName = null;
    	serviceID = null;
    }
    
    public OPALService(String serviceName, String url, String serviceID) {
        this.serviceName = serviceName;
        URL = url;
        this.serviceID = serviceID;
    }
    public String getServiceName() {
        return serviceName;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    public String getURL() {
        return URL;
    }
    public void setURL(String url) {
        URL = url;
    }
    
    public String toString(){
    	return "Service name: " + serviceName + " URL: " + URL + " ID: " + serviceID;
    }

	public String getServiceID() {
		return serviceID;
	}

	public void setServiceID(String serviceID) {
		this.serviceID = serviceID;
	}
	
	
	/** return a string containing a debugging of the OPALSerivce
	 *  array.
	 * 
	 */
	public static String arrayToString(OPALService [] service) {
	    String ret = new String();
	    for (int i = 0; i < service.length; i++ ){
	        ret += service[i] + "\n";
	    }
	    return ret;
	}//arrayToString
}
