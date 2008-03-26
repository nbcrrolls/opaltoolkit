package edu.sdsc.nbcr.opal.gui.common;


/**
 * useless... this class not used in the code 
 * TODO delete me
 * @author clem
 *
 */
public class AppMetadataContainer {
	
	private AppMetadata [] appMetadatas;
	
	public AppMetadataContainer() {
		
	}



	public AppMetadata getAppMetadata(String serviceName) {
		for (int i = 0; i < appMetadatas.length; i++ ){
			if (appMetadatas[i].getServiceName().equals(serviceName)){
				return appMetadatas[i];
			}
		}
		//the service doesn't exist
		return null;
	}//getAppMetadata
	

}
