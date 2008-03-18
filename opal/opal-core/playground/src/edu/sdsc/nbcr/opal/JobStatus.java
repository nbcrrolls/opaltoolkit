package edu.sdsc.nbcr.opal;

public class JobStatus {
    
    private int statusCode;
    private String statusDescription;
    
    
    public JobStatus(){}
    
    public JobStatus(int statusCode, String statusDescription) {
        super();
        this.statusCode = statusCode;
        this.statusDescription = statusDescription;
    }
    
    
    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }
    /**
     * @param statusCode the statusCode to set
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    /**
     * @return the statusDescription
     */
    public String getStatusDescription() {
        return statusDescription;
    }
    /**
     * @param statusDescription the statusDescription to set
     */
    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

}
