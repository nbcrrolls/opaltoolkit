package cherry.glenda.lan;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ServiceData {
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long ServiceID;
	 /*define Service:AppMetadata= 1:1
	@Persistent
	private AppMetadata appMetadata;
	
	AppMetadata getAppMetadata() {
        return appMetadata;
    }

    void setAppMetadata(AppMetadata appMetadata) {
        this.appMetadata = appMetadata;
    }
	define Service:AppMetadata = 1:1 over
    */   
    @Persistent
	private int id;
	@Persistent
	private String service;
	@Persistent
	private String summary;
	@Persistent
	private String url;

	public ServiceData(){
		
	}
	public ServiceData(int id,String service,String summary,String url){
		this.id = id;
		this.service = service;
		this.summary = summary;
		this.url = url;
	}
	/**
     * @primaryKey ServiceID
     */
    public Long getServiceID() {
        return ServiceID;
    }
	/*
	 * @id
	 */
	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}
	
	
	/*
	 * @service
	 */
	public String getService(){
		return service;
	}
	public void setService(String service){
		this.service = service;
	}
	
	
	/*
	 * @summary
	 */
	public String getSummary(){
		return summary;
	}
	public void setSummary(String summary){
		this.summary = summary;
	}
	
	/*
	 * @url
	 */
	public String getUrl(){
		return url;
	}
	public void setUrl(String url){
		this.url = url;
	}
	
}



