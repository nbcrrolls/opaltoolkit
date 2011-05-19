package cherry.glenda.lan;

import javax.activation.DataHandler;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.xml.messaging.URLEndpoint;
import javax.xml.soap.*;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import javax.xml.soap.MimeHeader;

import com.google.appengine.api.datastore.Query.SortDirection;

import cherry.glenda.lan.AppMetadataData;

public class SoapLanuchJob {
	@SuppressWarnings("null")
	public static LanchJobEnvelope LanuchJob(String url,Job job,Long jobID)throws Exception 
	{
		//List<JobArgFlagData> JobFlaglist = job.getJobArgFlags();
		//List<JobArgParamData> JobParamlist = job.getJobArgParams();
		
		//get separator
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(AppMetadataData.class);
	    query.setFilter("URL == url");
	    query.declareParameters("String url");
	    List<AppMetadataData> appMetaList = (List<AppMetadataData>) query.execute(url);
	    String serviceName = appMetaList.get(0).getServiceName();
	    String separator = appMetaList.get(0).getSeparator();
		
	    //get JobArgFlagData by JobID
	    PersistenceManager pm1 = PMF.get().getPersistenceManager();
		Query query1 = pm1.newQuery(JobArgFlagData.class);
	    query1.setFilter("JobID == jobID");
	    query1.declareParameters("String jobID");
	    query1.setOrdering("position asc");
	    List<JobArgFlagData> JobFlaglist = (List<JobArgFlagData>) query1.execute(jobID);
	    //get JobParamData by JobID
	    PersistenceManager pm2 = PMF.get().getPersistenceManager();
		Query query2 = pm2.newQuery(JobArgParamData.class);
	    query2.setFilter("JobID == jobID");
	    query2.declareParameters("String jobID");
	    query2.setOrdering("position asc");
	    List<JobArgParamData> JobParamlist = (List<JobArgParamData>) query2.execute(jobID);
	    
	    //
	    System.out.println("*******************output JobFlaglist order by position asc **************");
	    for(int a = 0;a<JobFlaglist.size();a++)
		{
				
				JobArgFlagData jobArgFlaga = JobFlaglist.get(a);
				String realIDa = jobArgFlaga.getRealID();
				System.out.println("flag["+a+"].realID="+realIDa);
		}
	    System.out.println("*******************output JobParamlist order by position asc **************");
	    for(int b = 0;b<JobParamlist.size();b++)
		{
				JobArgParamData jobArgParamb = JobParamlist.get(b);
				String realIDb = jobArgParamb.getRealID();
				System.out.println("Param["+b+"].realID="+realIDb);
		}
		
	    System.out.println("******************cmd**************");
		String cmd ="";
		
		for(int ll = 0;ll<JobFlaglist.size();ll++)
		{
				
				JobArgFlagData jobArgFlagll = JobFlaglist.get(ll);
				String flagtagll = jobArgFlagll.getTag();
				//System.out.println("flagtagll");
				cmd +=  " " +flagtagll ;
		}
		for(int mm = 0;mm<JobParamlist.size();mm++)
		{
			JobArgParamData jobArgParam = JobParamlist.get(mm);
			String paramtag = jobArgParam.getTag();
		
			if(paramtag==null||paramtag.equals(""))
			{
					//untag param
					String selectv =  jobArgParam.getSelectedValue();
					if ( (selectv != null) && ( selectv.length() > 0) )
					{
						cmd += " "+ selectv;
					}else{
							JobInputFile jobF = jobArgParam.getJobInputFile();
							if(jobF==null){}else
							{
								cmd += " " + jobF.getfileName();
							}
							
						}
			}else{
					//tag param
					String selectv =  jobArgParam.getSelectedValue();
					if ( (selectv != null) && ( selectv.length() > 0) )
					{
						cmd += " "+ paramtag +separator + selectv;
					}else{
							JobInputFile jobF = jobArgParam.getJobInputFile();
							if(jobF==null){}else
							{
								cmd += " "+ paramtag +separator + jobF.getfileName();
							}
							
						}
				
				}
			
		}
		System.out.println("cmd ="+cmd);
		
		LanchJobEnvelope JobOutputEnve = new LanchJobEnvelope();
		
		SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
		SOAPFactory soapFactory = SOAPFactory.newInstance();
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();
		SOAPEnvelope envelope = soapPart.getEnvelope();
		SOAPBody body = envelope.getBody();
		Name lanuchJobName= envelope.createName("launchJob", "opal2","http://nbcr.sdsc.edu/opal");
		SOAPBodyElement lanuchJobElement = body.addBodyElement(lanuchJobName);
		Name argListName = envelope.createName("argList");
		SOAPElement argListElement = lanuchJobElement.addChildElement(argListName);
		argListElement.addTextNode(cmd);
		
/*		
		for(int mm = 0;mm<JobParamlist.size();mm++)
		{
			JobArgParamData jobArgParam = JobParamlist.get(mm);
			JobInputFile jobF = jobArgParam.getJobInputFile();
			if(jobF==null){}else
			{
				String name = jobF.getfileName();
				String type = jobF.getfileType();
				System.out.println("#############################");
				System.out.println("field ="+ jobF.getfieldName()+"<br>"
						+ "name ="+ jobF.getfileName()+" "
						+ "type ="+ jobF.getfileType()+" "
						+ "location ="+ jobF.getContent()+" ");
				
				String location = jobF.getContent();
				Name inputFileName = envelope.createName("inputFile");
				SOAPElement inputFileElement = lanuchJobElement.addChildElement(inputFileName);
				
				
				Name attachmentName = envelope.createName("location");
				SOAPElement attachmentElement = inputFileElement.addChildElement(attachmentName);
				attachmentElement.addTextNode(location);
				
				
				Name attachmentName = envelope.createName("attachment");
				SOAPElement attachmentElement = inputFileElement.addChildElement(attachmentName);
				attachmentElement.addTextNode((new DataHandler( new URL(location))).toString());
				
			
        	}
		}

	*/	
				
		for(int mm = 0;mm<JobParamlist.size();mm++)
		{
			JobArgParamData jobArgParam = JobParamlist.get(mm);
			JobInputFile jobF = jobArgParam.getJobInputFile();
			if(jobF==null){}else
			{
				String name = jobF.getfileName();
				String type = jobF.getfileType();
				System.out.println("#############################");
				System.out.println("field ="+ jobF.getfieldName()+"<br>"
						+ "name ="+ jobF.getfileName()+" "
						+ "type ="+ jobF.getfileType()+" "
						+ "location ="+ jobF.getContent()+" ");
				
				String location = jobF.getContent();
				Name inputFileName = envelope.createName("inputFile");
				SOAPElement inputFileElement = lanuchJobElement.addChildElement(inputFileName);
				
				URL fileURL = new URL(location);
				DataHandler inputFile = new DataHandler(fileURL);
				AttachmentPart attachment = soapMessage.createAttachmentPart(inputFile);
				attachment.setContentId(name);
				soapMessage.addAttachmentPart(attachment);
				Name fileName = envelope.createName("name");
				SOAPElement fileNameElement = inputFileElement.addChildElement(fileName);
				fileNameElement.addTextNode(name);
				Name attachmentName = envelope.createName("attachment");
				SOAPElement attachmentElement = inputFileElement.addChildElement(attachmentName);
				attachmentElement.addAttribute(envelope.createName("href"),
	                       "cid:" + attachment.getContentId());

				
			
        	}
		}

	/*

		//if shows ,make hide from here	
		for(int mm = 0;mm<JobParamlist.size();mm++)
		{
			JobArgParamData jobArgParam = JobParamlist.get(mm);
			JobInputFile jobF = jobArgParam.getJobInputFile();
			if(jobF==null){}else
			{
				String name = jobF.getfileName();
				String type = jobF.getfileType();
				System.out.println("#############################");
				System.out.println("field ="+ jobF.getfieldName()+"<br>"
						+ "name ="+ jobF.getfileName()+" "
						+ "type ="+ jobF.getfileType()+" "
						+ "location ="+ jobF.getContent()+" ");
				
				String location = jobF.getContent();
				//attachment
				URL fileURL = new URL(location);
				DataHandler inputFile = new DataHandler(fileURL);
				AttachmentPart attachment = soapMessage.createAttachmentPart(inputFile);
				attachment.setContentId(name);
				soapMessage.addAttachmentPart(attachment);
        	}
		}
		*/
		soapMessage.saveChanges();
		
		//receiver
		System.out.println("111111111111111111111111111111111111111111111");
		System.out.println("\n=============launchJob call ==========\n");
		soapMessage.writeTo(System.out);
		System.out.println("\n=============launchJob call ==========\n");
		
		System.out.println("@@@@@@@@@@@@@@@@@@@@@cmd ="+cmd);
		for(int mm = 0;mm<JobParamlist.size();mm++)
		{
			JobArgParamData jobArgParam = JobParamlist.get(mm);
			JobInputFile jobF = jobArgParam.getJobInputFile();
			if(jobF==null){}else
			{
				String name = jobF.getfileName();
				System.out.println("@@@@@@@@@@@@@@@@@@@@@filename ="+name);
			}
		}
		URLEndpoint endpoint = new URLEndpoint(url);
		System.out.println("\nSending message to URL: " + endpoint.getURL());
		
		
		
		SOAPMessage reply = soapConnection.call(soapMessage, endpoint);
		System.out.println("\n============= launchJob reply ==========\n");
		reply.writeTo(System.out);
		System.out.println("\n============= launchJob reply ==========\n");
		soapConnection.close();
		
		SOAPBody replyBody = reply.getSOAPBody();
		if (replyBody.hasFault()) {
			SOAPFault newFault = replyBody.getFault();
			System.out.println("SAOP FAULT:\n");
			System.out.println("code = " + newFault.getFaultCodeAsName());
			System.out.println("message = " + newFault.getFaultString());
			System.out.println("actor = " + newFault.getFaultActor());
		} else
		{
			Iterator iterator2;
			Iterator iterator1 = replyBody.getChildElements();
			String tagName = null;
			SOAPElement se = null;
			if (iterator1.hasNext()) {
				se = (SOAPElement) iterator1.next();//iterator1 has metadata
				tagName = se.getElementName().getLocalName();
				System.out
						.println("\n\n 111111");
				System.out.println("tagName 1=" + tagName);
				if(tagName != null && tagName.equals("launchJobOutput"))
				{
					iterator1 = se.getChildElements();	
					while (iterator1.hasNext()) {
						se = (SOAPElement) iterator1.next();
						tagName = se.getElementName().getLocalName();
						System.out.println("\n\n 22222222");
						System.out.println("tagName 2=" + tagName);
						if(tagName != null && tagName.equals("jobID"))
						{
							String jobIDLan = se.getValue();
							System.out.println("jobID =" + jobIDLan);
							JobOutputEnve.setjobOpalID(jobIDLan);
						}
						if(tagName != null && tagName.equals("status"))
						{
							iterator2 = se.getChildElements();
							while (iterator2.hasNext())
							{
								se = (SOAPElement) iterator2.next();
								tagName = se.getElementName().getLocalName();
								System.out.println("\n\n 3333333");
								System.out.println("tagName 3=" + tagName);
								if(tagName != null && tagName.equals("code"))
								{
									String code = se.getValue();
									System.out.println("code =" + code);
									JobOutputEnve.setcode(Integer.parseInt(code));
								}
								if(tagName != null && tagName.equals("message"))
								{
									String message = se.getValue();
									System.out.println("message =" + message);
									JobOutputEnve.setmessage(message);
								}
								if(tagName != null && tagName.equals("baseURL"))
								{
									String baseURL = se.getValue();
									System.out.println("baseURL =" + baseURL);
									JobOutputEnve.setbaseURL(baseURL);
								}
							}
						}
					}
				}//end launchJobOutput
			}
		}
		java.util.Iterator it = reply.getAttachments();
		while (it.hasNext()) {
			DataHandler dh = ((AttachmentPart)it.next()).getDataHandler();
			  String fname = dh.getName();
		         if(null != fname)
		           
	         System.out.println("returnName =" + fname);
	   }
		
		/*
		 if(!body.hasFault())
   {
      Iterator iterator = result.getAttachments();
      if(iterator.hasNext())
      {
         dh = ((AttachmentPart)iterator.next()).getDataHandler();
         String fname = dh.getName();
         if(null != fname)
            return new File(fname);
      }
   }

		 */

		return JobOutputEnve;
	}
	
  public static void main( String[] args ) throws Exception {
    
	  
  }
}