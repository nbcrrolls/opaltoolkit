package cherry.glenda.lan;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.mail.Session;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
public class Upload extends HttpServlet 
{
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
    	
    res.setContentType("text/plain");     	  
    PrintWriter out = res.getWriter();
    boolean isMultipart = ServletFileUpload.isMultipartContent(req);
	ServletFileUpload upload = new ServletFileUpload();
	upload.setSizeMax(1024000);
	int o = 0;
	
	List<JobInputFile> jInFileList = new ArrayList<JobInputFile>();
	JobInputFile jobInputFile = null;
	Hashtable hx=new Hashtable();//hx: all flags,all param except file field
	String url="";
	try{
		FileItemIterator iterator = upload.getItemIterator(req); 
		while(iterator.hasNext())
		{
			FileItemStream item = iterator.next();
			if(item.isFormField())
			{
				InputStream stream = item.openStream();
				String name = item.getFieldName();
				String value = Streams.asString(stream);
				System.out.println(o+" Form field: field name = "+ name+ ", value = "+ value);
				if(!(name.equals("")||name==null)&&(name.equals("url"))){url = value ;}
				if(!name.equals("url")&&!(value.equals("")||value==null)){	hx.put(name,value);	}
				
				stream.close();
			}else{
					String field = item.getFieldName();
					String Name = item.getName();
					String ContentType = item.getContentType();
				
					System.out.println(o+" File field name= "+ field + ",  item.getName()  = "
							+item.getName().toString()+",  item.getfileType  = "+ ContentType);
					if(Name==null||Name.equals(""))
					{} else
					  {
							jobInputFile = new JobInputFile();
							jobInputFile.setfileName(Name);
							if(field==null||field.equals(""))
							{} else	{jobInputFile.setfieldName(field);}
														
							if(ContentType==null||ContentType.equals(""))
							{}else	{jobInputFile.setfileType(ContentType);}
							
							System.out.println("jobInputFile.getfieldName ="+ jobInputFile.getfieldName());
							System.out.println("jobInputFile.getfileName ="+ jobInputFile.getfileName());
							System.out.println("jobInputFile.getfileType ="+ jobInputFile.getfileType());
							//System.out.println("	location ="+ jobInputFile.getContent() +"<br>");
							jInFileList.add(jobInputFile);
							
					  }//end if filename not null
				}//end else
			o++;
			}//end while
		} catch (FileUploadException e) {  e.printStackTrace();  }
		System.out.println("##################### url #####################");
		System.out.println("url ="+ url);
		hx.remove("url");
			
		System.out.println("##################### hx table #####################");
		System.out.println("hx.size() ="+ hx.size());
		System.out.println("hx table ="+ hx);
		
		List<BlobKey> blobKeys = new ArrayList<BlobKey>();
		for(int a = 0;a<jInFileList.size();a++){
			
				 Map<String, BlobKey> blobs =  blobstoreService.getUploadedBlobs(req);
				 BlobKey blobKey = blobs.get(jInFileList.get(a).getfieldName());
				 blobKeys.add(blobKey);
				 if (blobKey == null)
				   { System.out.println("blobKey == null"); } 
				    else {
				    		System.out.println("blobKey =" + blobKey.getKeyString());
				    		//jInFileList.get(a).setContent("http://opalappcherry.appspot.com/upload?blob-key="+blobKey.getKeyString());
				    		jInFileList.get(a).setContent("http://localhost:8080/upload?blob-key="+blobKey.getKeyString());
				    		blobKeys.add(blobKey) ;
				    		
				    	 }
			
		}
		//print out the jobInputfileList
		System.out.println("#####################jInFileList#####################");
		if(jInFileList.size()==0){
			System.out.println("no input file"+"<br>");
		}else{
					for(int ff=0; ff<jInFileList.size(); ff++)
					{
						JobInputFile jobInFile = jInFileList.get(ff);
						String field = jobInFile.getfieldName();
						String filename = jobInFile.getfileName();
						String fileType = jobInFile.getfileType();
						String location = jobInFile.getContent();
						System.out.println("jInFileList["+ff+"].fieldName=" + field
								+",   fileName =" + filename
								+",   fileType =" + fileType
								+",   content =" + location);
					}
		}
		
    
   		//call function to get service's AppMetadata ,then compose flagAll,ParamList
		System.out.println("#####################Getting appMetadata flagAll,ParamList by url##########################");
		
		AppMetadataData app = GeneralFunction.getAppMetadataByURL(url);
		String serviceName = app.getServiceName();
	    String separator = app.getSeparator();
	   
	    System.out.println("serviceName="+ serviceName);
	    List<JobArgFlagData> JobFlaglist = new ArrayList<JobArgFlagData>(); 
	    List<JobArgParamData> JobParamlist = new ArrayList<JobArgParamData>(); 
	    List<ArgFlagData> flagAll = new ArrayList<ArgFlagData>();
	    List<ArgParamData> paramAll = new ArrayList<ArgParamData>();
		List<GroupData> groups = app.getGroups(); 
		for(int g = 0; g < groups.size(); g++)
		{
			List<ArgFlagData> argFlags = groups.get(g).getArgFlags();
			for(int a = 0;a<argFlags.size();a++)
			{
				ArgFlagData flag = argFlags.get(a);
				flagAll.add(flag);
			}
			List<ArgParamData> argParams = groups.get(g).getArgParams(); 
			for(int b = 0;b<argParams.size();b++)
			{
				ArgParamData param = argParams.get(b);
				paramAll.add(param);
			}	
		}//end for groups
		  	System.out.println("flagAll.size()="+ flagAll.size());
		  	System.out.println("paramAll.size()="+ paramAll.size());
		  	System.out.println("#####################Handlering hx##########################");
		  	//make hx
			Enumeration keySet = hx.keys();
			while (keySet.hasMoreElements())
			{
				   	String key = keySet.nextElement().toString();
				    if(flagAll.size()!=0)
				   {			   
					   for(int f = 0; f < flagAll.size();f++)
						{
					    	String flagRealID = flagAll.get(f).getRealID();
					    	
							if(key.equals(flagRealID))
							{
								JobArgFlagData jobArgFlagData = new JobArgFlagData();
								jobArgFlagData.setId(key);
								String tag = flagAll.get(f).getTag();
								int positionflag = flagAll.get(f).getPosition();
								jobArgFlagData.setTag(tag);
								String select = hx.get(key).toString();
								if(select.equals("on"))
								{
									jobArgFlagData.setSelected(true);
								}
								JobFlaglist.add(jobArgFlagData);
							}			
								
						}
					   
				   }
				   if(paramAll.size()!=0)
				   {
					   for(int p = 0; p < paramAll.size();p++)
						{
								String ParamRealID = paramAll.get(p).getRealID();
								if(key.equals(ParamRealID))
								{
									JobArgParamData jobArgParamData = new JobArgParamData();
									jobArgParamData.setId(key);
									String tag = paramAll.get(p).getTag();
									if(tag==null||tag.equals(""))
									{}else{
										jobArgParamData.setTag(tag);
									}
									String ioType = paramAll.get(p).getIoType();
									String type = paramAll.get(p).getType();
									int position = paramAll.get(p).getPosition();
									jobArgParamData.setPosition(position);
									String selectValue = hx.get(key).toString();
									if(selectValue==null||selectValue.equals(""))
									{}else{
											jobArgParamData.setSelectedValue(selectValue);
											jobArgParamData.setJobInputFile(null);
										}
									JobParamlist.add(jobArgParamData);
								}
						}
				}
				    		
			}//end while
			 System.out.println("#####################First get JobFlaglist and JobParamlist after hx's handler##########################");
			if(JobFlaglist.size()==0)
			{
				 System.out.println("JobFlaglist is null"+"<br>");
			}else{
					for(int jf = 0;jf<JobFlaglist.size();jf++)
					{
						 System.out.println("JobFlag["+jf+"].RealID()="+((JobArgFlagData)JobFlaglist.get(jf)).getRealID()
								+",  Tag()="+((JobArgFlagData)JobFlaglist.get(jf)).getTag());
					}
			}
			
			if(JobParamlist.size()==0)
			{
				 System.out.println("JobParamlist is null"+"<br>");
			}else
			{
					for(int jp = 0;jp<JobParamlist.size();jp++)
					{
						//out.println("da sha"+"<br>");
						 System.out.println(
								"JobParam["+jp+"].RealID()="+((JobArgParamData)JobParamlist.get(jp)).getRealID()
								+",  Tag()="+((JobArgParamData)JobParamlist.get(jp)).getTag()
								+",  SelectedValue()="+((JobArgParamData)JobParamlist.get(jp)).getSelectedValue());
					}
			}
			
			System.out.println("#####################put input file param in jobParamList ##########################");
			// make jobInFileList
			if(jInFileList.size()==0){
				System.out.println("no input file"+"<br>");
			}else{
						for(int ff=0; ff<jInFileList.size(); ff++)
						{
							JobInputFile jobInFile = jInFileList.get(ff);
							String field = jobInFile.getfieldName();
							String filename = jobInFile.getfileName();
							String fileType = jobInFile.getfileType();
							if(filename==null||filename.equals("")){}
							else{
								/*
								out.println("inputFileList["+ff+"].fieldName=" + field
										+",   fileName =" + filename
										+",   fileType =" + fileType
										+",   content =" + jobInFile.getContent().getBytes()
										+"<br>");
								*/
									for(int t = 0;t < paramAll.size(); t++)
									{
										String paramRealID = paramAll.get(t).getRealID();
										
										if(field.equals(paramRealID))
										{
											JobArgParamData jobArgParamData = new JobArgParamData();
											jobArgParamData.setId(field);
											String tag = paramAll.get(t).getTag();
											int position = paramAll.get(t).getPosition();
											jobArgParamData.setTag(tag);
											jobArgParamData.setPosition(position);
											jobArgParamData.setJobInputFile(jobInFile);
											jobArgParamData.setSelectedValue(null);
											JobParamlist.add(jobArgParamData);
										}
									}
								}
						}
						
				}
			System.out.println("#######################final output jobParamList###########################");
			if(JobParamlist.size()==0)
			{
				System.out.println("JobParamlist is null"+"<br>");
			}else
			{
					for(int bb = 0;bb<JobParamlist.size();bb++)
					{
						//out.println("da sha"+"<br>");
						JobInputFile jobInF = JobParamlist.get(bb).getJobInputFile();
						if(jobInF!=null){
							System.out.println(
									"JobParam["+bb+"].RealID()="+((JobArgParamData)JobParamlist.get(bb)).getRealID()
									+",  Tag()="+((JobArgParamData)JobParamlist.get(bb)).getTag()
									
									+",  fileLocation= "+ jobInF.getContent()
									//+",  fileLength ="+((JobArgParamData)JobParamlist.get(bb)).getInputFile().getContent().getBytes().length
									+"<br>");
							}	
						
						else{
							System.out.println(
									"JobParam["+bb+"].RealID()="+((JobArgParamData)JobParamlist.get(bb)).getRealID()
									+",  Tag()="+((JobArgParamData)JobParamlist.get(bb)).getTag()
									+",  SelectedValue()="+((JobArgParamData)JobParamlist.get(bb)).getSelectedValue()
									+"<br>");
							}	
						
					}
			}
			
			System.out.println("#######################making a new job,a new job,a new job###########################");
			///////////////////////////////////////////////////////////////
			//make job Object
			UserService userService = UserServiceFactory.getUserService();
			User user = userService.getCurrentUser();
			String userID = user.getUserId();
			String userEmail = user.getEmail();
			Date currentTime = new Date();
			System.out.println( "<br>");
			System.out.println("userID="+ userID +"<br>");
			System.out.println("userEmail="+ userEmail +"<br>");
			System.out.println("currentTime="+ currentTime +"<br>");
			Job job = new Job();
			job.setUserID(userID);
			job.setUserEmail(userEmail);
			job.setSubmitTime(currentTime);
			job.setServiceName(serviceName);
			job.setJobArgFlags(JobFlaglist);
			job.setJobArgParams(JobParamlist);
			
			
			
		
			System.out.println(
					"Job.UserID()="+job.getUserID()+"<br>"
					+"job.UserEmail()="+job.getUserEmail()+"<br>"
					+"job.SubmitTime()="+job.getSubmitTime()+"<br>"
					+"job.Status()="+job.getStatus()+"<br>"
					+"job.StatusCode()="+job.getStatusCode()+"<br>"
					+"job.Message()="+job.getMessage()+"<br>"
					+"job.ServiceName()="+job.getServiceName()+"<br>"
					+"job.JobArgFlags().size()="+job.getJobArgFlags().size()+"<br>"
					+"job.JobArgParams().size()="+job.getJobArgParams().size()+"<br>"
					//+"job.getJobArgParams().size()="+job.getJobArgParams().g+"<br>"
			);
			PersistenceManager pmJob1 = PMF.get().getPersistenceManager();
			//Transaction tx1 = pmJob1.currentTransaction();
			try {
				// tx1.begin();
				pmJob1.makePersistent(job);
			//	tx1.commit();
			} finally {
			//	tx1.commit();
				pmJob1.close();
			}
			
			Long JobID = job.getJobID();
			System.out.println("JobID = "+JobID);
			for(int q=0;q<job.getJobArgFlags().size();q++)
			{
				job.getJobArgFlags().get(q).setJobID(JobID);
			}
			for(int w=0;w<job.getJobArgParams().size();w++)
			{
				job.getJobArgParams().get(w).setJobID(JobID);
			}
			PersistenceManager pmJob2 = PMF.get().getPersistenceManager();
			//Transaction tx2 = pmJob2.currentTransaction();
			try {
			//	 tx2.begin();
				pmJob2.makePersistent(job);
			//	tx2.commit();
			} finally {
			//	tx2.commit();
				pmJob2.close();
			}
			//SoapLanuchJob call
			
			LanchJobEnvelope lanJobEncelop;
			try {
				lanJobEncelop = SoapLanuchJob.LanuchJob(url,job,JobID);
				String jobOpalIDEnv = lanJobEncelop.getjobOpalID();
				System.out.println("jobOpalIDEnv ="+jobOpalIDEnv+"<br>");
				int codeEnv = lanJobEncelop.getcode();
				String messageEnv = lanJobEncelop.getmessage();
				String baseURLENV = lanJobEncelop.getbaseURL();
				
				
				
				
				
				job.setStatusCode(codeEnv);
				System.out.println("codeEnv = "+job.getStatusCode()+"<br>");
						
				if((job.getStatusCode())==1){job.setStatus("STATUS_PENDING");
				System.out.println("status[according to soapLanchJob] ="+job.getStatus()+"<br>");}
					
				if((job.getStatusCode())==2){job.setStatus("STATUS_ACTIVE");
				System.out.println("status[according to soapLanchJob] ="+job.getStatus()+"<br>");}
						
				if((job.getStatusCode())==4){job.setStatus("STATUS_FAILED");
				System.out.println("status[according to soapLanchJob] ="+job.getStatus()+"<br>");}
						
				if((job.getStatusCode())==8){job.setStatus("STATUS_DONE");
				System.out.println("status[according to soapLanchJob] ="+job.getStatus()+"<br>");}
						
						
				job.setMessage(messageEnv);
				System.out.println("message[according to soapLanchJob] ="+job.getMessage()+"<br>");
						
				job.setBeseURL(baseURLENV);
				System.out.println("baseURL[according to soapLanchJob] ="+job.getBeseURL()+"<br>");
						
				job.setJobOpalID(jobOpalIDEnv);
				System.out.println("jobOpalID[according to soapLanchJob] ="+job.getJobOpalID()+"<br>");
				
				PersistenceManager pmJob3 = PMF.get().getPersistenceManager();
				//Transaction tx3 = pmJob3.currentTransaction();
				try {
					// tx3.begin();
					pmJob3.makePersistent(job);
					JDOHelper.getPersistenceManager(job);
					//tx3.commit();
				} finally {
					//tx3.commit();
					pmJob3.close();
				}
				res.sendRedirect("/opal2/dashboard-jsp/launchJobHaveFile.jsp?jobOpalIDEnv="+jobOpalIDEnv+"&&url="+url); 
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//lanch job get jobOpalID
			
					
			
			
			
			
		
				

    }
    public void doGet( HttpServletRequest req, HttpServletResponse res)throws IOException 
    { 
    	//List<BlobKey> blobKeys = null;
    	//for(int a = 0; a<blobKeys.size();a++)
    	//{
    		BlobKey blobKey =new BlobKey( req.getParameter("blob-key")); 
    		blobstoreService.serve(blobKey, res);
    	//}
    } 

    
}

