package cherry.glenda.lan;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.appengine.api.datastore.Text;

public class GeneralFunction {

	public static AppMetadataData getAppMetadataByURL(String url){
		AppMetadataData app = new AppMetadataData();
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query1 = pm.newQuery(AppMetadataData.class);
	    query1.setFilter("URL == url");
	    query1.declareParameters("String url");
	    List<AppMetadataData> appMetaList = (List<AppMetadataData>) query1.execute(url);
	    String serviceName = appMetaList.get(0).getServiceName();
	    String separator = appMetaList.get(0).getSeparator();
	    Text info = appMetaList.get(0).getInfo();
	    String usage = appMetaList.get(0).getUsage();
	    //System.out.println("#####################general field##########################" + "<br>");
	    //System.out.println("serviceName="+ serviceName +"<br>");
	    List<ArgFlagData> flagAll = new ArrayList<ArgFlagData>();
	    List<ArgParamData> paramAll = new ArrayList<ArgParamData>();
		List<GroupData> groups = appMetaList.get(0).getGroups(); 
		for(int g = 0; g < groups.size(); g++)
		{
			//out.println("###########################################################" + "<br>");	
			List<ArgFlagData> argFlags = groups.get(g).getArgFlags();
			//out.println( "groups.get["+g+"]"+"argFlags.size()="+argFlags.size()+"<br>");
			for(int a = 0;a<argFlags.size();a++)
			{
				ArgFlagData flag = argFlags.get(a);
				//out.println("groups.get["+g+"]:#######flag("+a+")##########"+argFlags.get(a).getRealID()+"<br>");
				flagAll.add(flag);
			}
			List<ArgParamData> argParams = groups.get(g).getArgParams(); 
			//out.println("groups.get["+g+"]"+"argParams.size()="+argParams.size()+"<br>");
			for(int b = 0;b<argParams.size();b++)
			{
				ArgParamData param = argParams.get(b);
				//out.println("groups.get["+g+"]#########param("+b+")#############"+argParams.get(b).getRealID()+"<br>");
				paramAll.add(param);
			}	
		}//end for groups
	    
	    app.setSeparator(separator);
	    app.setServiceName(serviceName);
	    app.setInfo(info);
	    app.setUsage(usage);
	    app.setGroups(groups);
		return app;
	}
	public static List<Job> getAllJobByCurrentUser(String userEmail)
	{
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Job.class);
	    query.setFilter("UserEmail == userEmail");
	    query.declareParameters("String userEmail");
	    List<Job> jobList  = (List<Job>) query.execute(userEmail);
		return jobList;
	}
	public static Job getJobByJobOpalID(String jobOpalID){
				
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(Job.class);
	    query.setFilter("JobOpalID == jobOpalID");
	    query.declareParameters("String jobOpalID");
	    List<Job> jobList = (List<Job>) query.execute(jobOpalID);
	    Job job =  jobList.get(0);
	    return job;
	}
}
