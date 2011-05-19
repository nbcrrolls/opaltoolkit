package cherry.glenda.lan;

import java.util.Iterator;

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

public class SoapQueryStatus {

	public static QueryStatusEnvelope QueryStatus(String jobOpalID,String url)throws Exception {
		QueryStatusEnvelope queryStatusOutputEnve = new QueryStatusEnvelope();
		SOAPConnection soapConnection = SOAPConnectionFactory.newInstance().createConnection();
		SOAPFactory soapFactory = SOAPFactory.newInstance();
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();
		SOAPEnvelope envelope = soapPart.getEnvelope();
		SOAPBody body = envelope.getBody();
		Name queryStatusName= envelope.createName("queryStatus", "opal2","http://nbcr.sdsc.edu/opal");
		SOAPBodyElement queryStatusElement = body.addBodyElement(queryStatusName);
		queryStatusElement.addTextNode(jobOpalID);
		//Name jobIDName = envelope.createName("jobID");
		//SOAPElement jobIDElement = queryStatusElement.addChildElement(jobIDName);
		//jobIDElement.addTextNode(jobOpalID);
		soapMessage.saveChanges();
		
		//receiver
		System.out.println("2222222222222222222222222222222222222222222222222222222222");
		System.out.println("\n============= queryStatus call ==========\n");
		soapMessage.writeTo(System.out);
		System.out.println("\n============= queryStatus call ==========\n");
		
		URLEndpoint endpoint = new URLEndpoint(url);
		System.out.println("\nSending message to URL: " + endpoint.getURL());
		SOAPMessage reply = soapConnection.call(soapMessage, endpoint);
		System.out.println("\n============= queryStatus reply ==========\n");
		reply.writeTo(System.out);
		System.out.println("\n============= queryStatus reply ==========\n");
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
				if(tagName != null && tagName.equals("queryStatusOutput"))
				{
					iterator1 = se.getChildElements();	
					while (iterator1.hasNext()) 
					{
							
								se = (SOAPElement) iterator1.next();
								tagName = se.getElementName().getLocalName();
								System.out.println("\n\n 3333333");
								System.out.println("tagName 2=" + tagName);
								if(tagName != null && tagName.equals("code"))
								{
									String code = se.getValue();
									System.out.println("code =" + code);
									queryStatusOutputEnve.setcode(Integer.parseInt(code));
								}
								if(tagName != null && tagName.equals("message"))
								{
									String message = se.getValue();
									System.out.println("message =" + message);
									queryStatusOutputEnve.setmessage(message);
								}
								if(tagName != null && tagName.equals("baseURL"))
								{
									String baseURL = se.getValue();
									System.out.println("baseURL =" + baseURL);
									queryStatusOutputEnve.setbaseURL(baseURL);
								}
							}
					}//end QueryStatus
			}
		}
	return queryStatusOutputEnve;
	}
}
