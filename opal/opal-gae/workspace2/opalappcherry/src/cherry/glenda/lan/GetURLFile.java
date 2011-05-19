package cherry.glenda.lan;



import javax.jdo.PersistenceManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.w3c.dom.*;

import java.io.*; 
import java.util.ArrayList;

public class GetURLFile extends DefaultHandler
{
	ServiceData service ;
    ArrayList<ServiceData> arrayList = new ArrayList<ServiceData>();
	public ArrayList<ServiceData> getHandle(String urltest)
	{
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		
	    //o.getClass().
		try {
				builder = factory.newDocumentBuilder();
				
				try{
						Document doc = builder.parse(urltest);
						NodeList entryList = doc.getElementsByTagName("entry");
						for(int i = 0; i < entryList.getLength(); i++)
						{
								System.out.println("the " + i + " node 's name : "+ entryList.item(i).getNodeName());
								System.out.println("the " + i + " node 's value : "+ entryList.item(i).getNodeValue());
								
								NodeList firstList = entryList.item(i).getChildNodes();
								
								for (int j = 0; j < firstList.getLength(); j++)
								{
									//System.out.println(j + " child node name :" + firstList.item(j).getNodeName());
									//System.out.println(j + " child node value :" + firstList.item(j).getNodeValue());
									NodeList secondList = firstList.item(j).getChildNodes();
									for (int jj = 0; jj < secondList.getLength(); jj++)
									{
										//System.out.println("	"+j +"["+ jj +"]"+ " child node name :" + secondList.item(jj).getNodeName());
										//System.out.println("	"+j +"["+ jj +"]"+ " child node value :" +secondList.item(jj).getNodeValue());
										
										NodeList thirdList = secondList.item(jj).getChildNodes();
										for (int jjj = 0; jjj < thirdList.getLength(); jjj++)
										{
											//System.out.println("		"+ j +"["+ jj +"]("+ jjj + ") child node name :" + thirdList.item(jjj).getNodeName());
											//System.out.println("		"+ j +"["+ jj +"]("+ jjj + ") child node value :" + thirdList.item(jjj).getNodeValue());
										}
									}
									//System.out.println(ll.item(j).get);
								}
								
								String titleName = doc.getElementsByTagName("title").item(i+1).getFirstChild().getNodeName();
								String titleValue = doc.getElementsByTagName("title").item(i+1).getFirstChild().getNodeValue();
								NamedNodeMap linkNode = doc.getElementsByTagName("link").item(i+1).getAttributes();
								Node linkHref = linkNode.getNamedItem("href");
								String href = linkHref.getNodeValue();
								System.out.println("href = " + href);
								
								String a = doc.getElementsByTagName("author").item(i).getFirstChild().getNodeName();
								String b = doc.getElementsByTagName("author").item(i).getFirstChild().getNodeValue();
								String summaryName = doc.getElementsByTagName("summary").item(i).getFirstChild().getNodeName();
								String summaryValue = doc.getElementsByTagName("summary").item(i).getFirstChild().getNodeValue();
								
								System.out.println("title's FirstChild name: " + titleName);
								System.out.println("title's getFirstChild value: " + titleValue);
							
								//System.out.println("ServiceName: " + titleValue);
								//System.out.println("author's FirstChild name: " + a);
								//System.out.println("author's FirstChild value: " + b);
								System.out.println("summary's FirstChild name: " + summaryName);
								System.out.println("summary's FirstChild value: " + summaryValue);
							
								service = new ServiceData();
								service.setId(i+1);
								service.setService(titleValue);
								service.setSummary(summaryValue);
								
								service.setUrl(href);
								
								//make persistent
								PersistenceManager pmAdd = PMF.get().getPersistenceManager();
								try {
									pmAdd.makePersistent(service);
						        } finally {
						        	pmAdd.close();
						        }
						    	//make persistent
								arrayList.add(i, service);
															
						}
				} catch (SAXException e){ System.out.println("SAXException when connecting to URL:"); } 
				  catch (IOException ee){ System.out.println("IOException when connecting to URL: "); }
				  
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			System.out.println("ParserConfigurationException:");
		}
		System.out.println("arrayList size = " + arrayList.size());
		return arrayList;
	}
}

