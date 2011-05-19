package cherry.glenda.lan;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.messaging.URLEndpoint;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

public class SoapGetJobStatistics {

	public static GetStaticsEnvelope getStatistics(String jobOpalID,String url)throws Exception {
		GetStaticsEnvelope gsEnve = new GetStaticsEnvelope();
		gsEnve.setjobOpalID(jobOpalID);
		SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
		SOAPFactory soapFactory = SOAPFactory.newInstance();
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();
		SOAPEnvelope envelope = soapPart.getEnvelope();
		SOAPBody body = envelope.getBody();
		Name getJobStatisticsName= envelope.createName("getJobStatistics", "opal2","http://nbcr.sdsc.edu/opal");
		SOAPBodyElement getJobStatisticsElement = body.addBodyElement(getJobStatisticsName);
		getJobStatisticsElement.addTextNode(jobOpalID);
		//Name jobIDName = envelope.createName("jobID");
		//SOAPElement jobIDElement = queryStatusElement.addChildElement(jobIDName);
		//jobIDElement.addTextNode(jobOpalID);
		soapMessage.saveChanges();
		
		//receiver
		System.out.println("33333333333333333333333333333333333333333333333333");
		System.out.println("\n============= getStatics call ==========\n");
		soapMessage.writeTo(System.out);
		System.out.println("\n============= getStatics call ==========\n");
		
		URLEndpoint endpoint = new URLEndpoint(url);
		System.out.println("\nSending message to URL: " + endpoint.getURL());
		SOAPMessage reply = soapConnection.call(soapMessage, endpoint);
		System.out.println("\n============= getStatics reply ==========\n");
		reply.writeTo(System.out);
		System.out.println("\n============= getStatics reply ==========\n");
		soapConnection.close();
		//SOAPBody replyBody = reply.getSOAPBody();
		
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
			if (iterator1.hasNext())
			{
				se = (SOAPElement) iterator1.next();//iterator1 has metadata
				tagName = se.getElementName().getLocalName();
				System.out
						.println("\n\n 111111");
				System.out.println("tagName 1=" + tagName);
				if(tagName != null && tagName.equals("getJobStatisticsOutput"))
				{
					iterator1 = se.getChildElements();	
					while (iterator1.hasNext()) 
					{
								se = (SOAPElement) iterator1.next();
								tagName = se.getElementName().getLocalName();
								System.out.println("\n\n 3333333");
								System.out.println("tagName 2=" + tagName);
								if(tagName != null && tagName.equals("startTime"))
								{
									 System.out.println("startTime="+se.getValue());
									 DateFormat df= new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
									 int a = se.getValue().indexOf(".");
									 System.out.println("a="+a);
									 String before = se.getValue().substring(0, a);
									 int b = before.indexOf("T");
									 String tbefore = before.substring(0, b);
									 String tafter = before.substring(b+1);
							    	 System.out.println("tbefore="+tbefore);
							    	 System.out.println("tbefore="+tafter);
							    	 String dateConvert = tbefore +" "+ tafter;
							    	 System.out.println("dateConvert="+dateConvert);
								     try 
								     {
								    	 Date startTime = df.parse(dateConvert); 
								    	 System.out.println(startTime);
								    	 gsEnve.setstartTime(startTime);
								     } 
								     catch (Exception ex) 
								     { System.out.println(ex.getMessage());} 
								 }
								if(tagName != null && tagName.equals("activationTime"))
								{
									 System.out.println("activationTime="+se.getValue());
									 DateFormat df= new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
									 int a = se.getValue().indexOf(".");
									 String before = se.getValue().substring(0, a);
									 int b = before.indexOf("T");
									 String tbefore = before.substring(0, b);
									 String tafter = before.substring(b+1);
							    	 System.out.println("tbefore="+tbefore);
							    	 System.out.println("tbefore="+tafter);
							    	 String dateConvert = tbefore +" "+ tafter;
							    	 System.out.println("dateConvert="+dateConvert);
								     try 
								     {
								    	 Date activationTime = df.parse(dateConvert);
								    	 System.out.println(activationTime);
								    	 gsEnve.setactivationTime(activationTime);
								     } 
								     catch (Exception ex) 
								     { System.out.println(ex.getMessage());} 
								}
								if(tagName != null && tagName.equals("completionTime"))
								{
									System.out.println("completionTime="+se.getValue());
									 DateFormat df= new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
									 int a = se.getValue().indexOf(".");
									 String before = se.getValue().substring(0, a);
									 int b = before.indexOf("T");
									 String tbefore = before.substring(0, b);
									 String tafter = before.substring(b+1);
							    	 System.out.println("tbefore="+tbefore);
							    	 System.out.println("tbefore="+tafter);
							    	 String dateConvert = tbefore +" "+ tafter;
							    	 System.out.println("dateConvert="+dateConvert);
								     try 
								     {
								    	 Date completionTime = df.parse(dateConvert); 
								    	 System.out.println(completionTime);
								    	 gsEnve.setcompletionTime(completionTime);
								    	 
								     } 
								     catch (Exception ex) 
								     { System.out.println(ex.getMessage());} 
								}
							}
					}//end getStatistics
			}
		}
		
	return gsEnve;
	}
}
