package edu.sdsc.nbcr.opal;

/**
 * this represent the configuration used to deploy an new application on opal
 *
 */
public class OpalApplication {
    
    /**
     * Am I missing something? Maybe description?
     */
    private String appName;
    private String defaultArguments;
    private String binaryLocation;
    private boolean parallel;
    
    
    /**
     * @return the appName
     */
    public String getAppName() {
        return appName;
    }


    /**
     * @param appName the appName to set
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }


    /**
     * @return the binaryLocation
     */
    public String getBinaryLocation() {
        return binaryLocation;
    }


    /**
     * @param binaryLocation the binaryLocation to set
     */
    public void setBinaryLocation(String binaryLocation) {
        this.binaryLocation = binaryLocation;
    }


    /**
     * @return the parallel
     */
    public boolean isParallel() {
        return parallel;
    }


    /**
     * @param parallel the parallel to set
     */
    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }


    public OpalApplication(){}


    /**
     * @return the defaultArguments
     */
    public String getDefaultArguments() {
        return defaultArguments;
    }


    /**
     * @param defaultArguments the defaultArguments to set
     */
    public void setDefaultArguments(String defaultArguments) {
        this.defaultArguments = defaultArguments;
    }
    
    

}
