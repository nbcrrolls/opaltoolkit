package cherry.glenda.lan;

import javax.jdo.PersistenceManager;
import javax.xml.messaging.URLEndpoint;
import javax.xml.soap.*;
import java.util.Iterator;
import java.util.ArrayList;
import cherry.glenda.lan.AppMetadataData;

public class SoapClient {
	@SuppressWarnings("null")
	public static AppMetadataData soapCall(String url)throws Exception {
		AppMetadataData appMetadata = new AppMetadataData();
appMetadata.setURL(url);


SOAPConnection soapConnection = SOAPConnectionFactory.newInstance()
		.createConnection();
SOAPFactory soapFactory = SOAPFactory.newInstance();
MessageFactory messageFactory = MessageFactory.newInstance();
SOAPMessage soapMessage = messageFactory.createMessage();
SOAPPart soapPart = soapMessage.getSOAPPart();
SOAPEnvelope envelope = soapPart.getEnvelope();
SOAPBody body = envelope.getBody();
SOAPBodyElement soapBodyElement = body.addBodyElement(envelope
		.createName("getAppConfig", "opal2",
				"http://nbcr.sdsc.edu/opal"));
soapMessage.saveChanges();

//receiver
System.out.println("<br>");
System.out
		.println("\n============= start request msg ==========\n");
soapMessage.writeTo(System.out);
System.out.println("\n============= end request msg ==========\n");

URLEndpoint endpoint = new URLEndpoint(url);
System.out
		.println("\nSending message to URL: " + endpoint.getURL());
SOAPMessage reply = soapConnection.call(soapMessage, endpoint);
System.out.println("\n============= start reply ==========\n");
reply.writeTo(System.out);
System.out.println("\n============= end reply ==========\n");
soapConnection.close();
/*
FileOutputStream fout = new FileOutputStream("E:/xml_config/soapmessage.xml");
soapMessage.writeTo(fout);
fout.close();
 */
/*
ByteArrayOutputStream out1 = new ByteArrayOutputStream();
soapMessage.writeTo( out1 );
String str = out1.toString();  
System.out.println("str = " + str);
out.println("str = " + str);
 */
SOAPBody replyBody = reply.getSOAPBody();
if (replyBody.hasFault()) {
	SOAPFault newFault = replyBody.getFault();
	System.out.println("SAOP FAULT:\n");
	System.out.println("code = " + newFault.getFaultCodeAsName());
	System.out.println("message = " + newFault.getFaultString());
	System.out.println("actor = " + newFault.getFaultActor());
} else {
	Iterator iterator1 = replyBody.getChildElements();
	Iterator iterator2, iterator3, iterator4, iterator5, iterator6, iterator7, iterator8, iterator9,iterator10;
	ArrayList valueList = null;
	ArrayList<ArgFlagData> argFlags = new ArrayList();
	ArrayList<ArgParamData> argParams = new ArrayList();
	ArrayList<GroupData> groups = new ArrayList();
	//ArrayList<String> elements = null;
	SOAPElement se = null;
	String tagName = null;
	String service = "";
	Name attributeName = null;
	int flagPosition = 0;
	int paramPosition = 0;
	if (iterator1.hasNext()) {
		se = (SOAPElement) iterator1.next();//iterator1 has metadata
		tagName = se.getElementName().getLocalName();
		System.out
				.println("\n\n 111111");
		System.out.println("tagName 1=" + tagName);
		iterator1 = se.getChildElements();
		//if(iterator1.hasNext()){///////////////////////////
		while(iterator1.hasNext())
		{
		se = (SOAPElement) iterator1.next();
		tagName = se.getElementName().getLocalName();
		System.out.println("\n\n 22222222");
		System.out.println("tagName 2=" + tagName);
		if (tagName != null && tagName.equals("metadata")) {
			attributeName = soapFactory.createName("appName");
			service = se.getAttributeValue(attributeName);
			System.out.print("service = " + service);
			appMetadata.setServiceName(service);
		
		iterator3 = se.getChildElements();
		while (iterator3.hasNext()) {
			se = (SOAPElement) iterator3.next();//iterator2 has element,usage,info,types
			tagName = se.getElementName().getLocalName();
			System.out
					.println("\n\n3");
			System.out.println("tagName 3=" + tagName);
			if (!tagName.equals("") && tagName.equals("usage")) {
				String usage = se.getValue();
				System.out.println("usage=   " + usage);
				appMetadata.setUsage(usage);
			}
			if (!tagName.equals("") && tagName.equals("info")) {
				
				String info = se.getValue();
				//com.google.appengine.api.datastore.Text info = null;
				//infoStr.valueOf(info);
				System.out.println("info=   " + info);
				appMetadata.setInfo(new com.google.appengine.api.datastore.Text(info));
			}
			if (!tagName.equals("") && tagName.equals("types")) {
				String types = se.getValue();
				System.out.println("types=   " + types);
				iterator4 = se.getChildElements();
				// se = (SOAPElement)iterator3.next();
				while (iterator4.hasNext()) {
					se = (SOAPElement) iterator4.next();
					tagName = se.getElementName().getLocalName();
					//flags parse
					
					if (!tagName.equals("")
							&& tagName.equals("flags")) {
						System.out.println("*****************start flags******************* ");
						System.out.println("flags =   " + tagName);
						iterator5 = se.getChildElements();
						while (iterator5.hasNext()) {
							se = (SOAPElement) iterator5.next();
							tagName = se.getElementName()
									.getLocalName();
							
							if (!tagName.equals("")
									&& tagName.equals("flag")) {
								ArgFlagData argFlag = new ArgFlagData();
								
								System.out.println("$$$$$$$$$flag$$$$$$$$$");
								System.out.println("flag =   "
										+ tagName);
								iterator6 = se.getChildElements();
								
								
								while (iterator6.hasNext()) {
									
									se = (SOAPElement) iterator6
											.next();
									
									tagName = se.getElementName()
											.getLocalName();
									if (!tagName.equals("")&& tagName.equals("id")) {
										String id = se.getValue();
										System.out.println("id =   "+ id); 
										argFlag.setId(id);
										flagPosition++;
										argFlag.setPosition(flagPosition);
									}
									if (!tagName.equals("")&& tagName.equals("tag")) {
										String tag = se.getValue();
										System.out.println("tag =   "+ tag);
										argFlag.setTag(tag);
									}
									if (!tagName.equals("")&& tagName.equals("default")) {
										String defaultFlag = se.getValue();
										System.out.println("default =   "+ defaultFlag);
										if(defaultFlag.equals("true"))
										{
										 argFlag.setDefault(Boolean.parseBoolean(defaultFlag));
										}
									}
									if (!tagName.equals("")&& tagName.equals("textDesc")) {
										String textDesc = se.getValue();
										System.out.println("textDesc =   "+ textDesc);
										argFlag.setTextDesc(textDesc);
										
									}
									
									
								}//while (iterator5.hasNext())	
								argFlags.add(argFlag);
								System.out.println("meimei! look at here!");
							}// if(!tagName.equals("") && tagName.equals("flag")) flag parse over
						}// while (iterator4.hasNext())
						for(int i = 0;i<argFlags.size();i++){
							      System.out.println("flagList.get("+i+")="+(ArgFlagData) argFlags.get(i));
						   }
					//appMetadata.setArgFlags(argFlags);
					}// if(!tagName.equals("") && tagName.equals("flags"))
				
					
					
					
					if (!tagName.equals("") && tagName.equals("taggedParams") || tagName.equals("untaggedParams")) {
						System.out.println("*****************start taggedParams******************* ");
						System.out.println("taggedParams|untaggedParams =   "
								+ tagName);
						iterator7 = se.getChildElements();
						while (iterator7.hasNext()) {
							se = (SOAPElement) iterator7.next();
							tagName = se.getElementName()
									.getLocalName();
							if (!tagName.equals("")
									&& tagName.equals("separator")) {
								String separator = se.getValue();
								System.out.println("separator =   "
										+ separator);
								appMetadata.setSeparator(separator);
							}
							if (!tagName.equals("")
									&& tagName.equals("param")) {
								ArgParamData argParam = new ArgParamData();//
								valueList = new ArrayList();//initiate,if error appears,check the position. 
								System.out.println("##############taggedParams|untaggedParams############");
							
								System.out.println("param =   "
										+ tagName);
								//wangxia
								iterator8 = se.getChildElements();
								while (iterator8.hasNext()) {
									se = (SOAPElement) iterator8
											.next();
									tagName = se.getElementName()
											.getLocalName();
									if (!tagName.equals("")
											&& tagName.equals("id")) {
										String tpID = se.getValue();
										System.out
												.println("tpID =   "
														+ tpID);
										argParam.setId(tpID);
										paramPosition++;
										argParam.setPosition(paramPosition);
									}
									if (!tagName.equals("")
											&& tagName
													.equals("tag")) {
										String tpTag = se
												.getValue();
										System.out
												.println("tpTag =   "
														+ tpTag);
										argParam.setTag(tpTag);
										//argParam.setPosition(1);
									}
									if (!tagName.equals("")
											&& tagName
													.equals("paramType")) {
										String tpType = se
												.getValue();
										System.out
												.println("taggedParamType =   "
														+ tpType);
										argParam.setType(tpType);
									}
									if (!tagName.equals("")
											&& tagName
													.equals("ioType")) {
										String tpIOType = se
												.getValue();
										System.out
										.println("tpIOType =   "
												+ tpIOType);
										if(tpIOType!=null){
											
										argParam.setIoType(tpIOType);
										}
									}
									if (!tagName.equals("")
											&& tagName
													.equals("required")) {
										String tpRequired = se
												.getValue();
										System.out
										.println("tpRequired =   "
												+ tpRequired);
										if(tpRequired.equals("true"))
										{
											argParam.setRequired(Boolean.parseBoolean(tpRequired));
										}
									}
									
									if (!tagName.equals("")
											&& tagName
													.equals("value")) {
										String tpValue = se
												.getValue();
										
										valueList.add(tpValue);
										System.out
												.println("tpValue =   "
														+ tpValue);
										//System.out.println("value test inside!");
									}
									//System.out.println("value test!");
									
									if (!tagName.equals("")
											&& tagName
													.equals("semanticType")) {
										String tpsemanticType = se
												.getValue();
										System.out
												.println("tpsemanticType =   "
														+ tpsemanticType);
										argParam.setSemanticType(tpsemanticType);
									}
									//semanticType
									
									if (!tagName.equals("")
											&& tagName
													.equals("textDesc")) {
										String tpTextDesc = se
												.getValue();
										System.out
												.println("tpTextDesc =   "
														+ tpTextDesc);
										argParam.setTextDesc(tpTextDesc);
									}
									if (!tagName.equals("")
											&& tagName
													.equals("default")) {
										String tpDefault = se
												.getValue();
										System.out
												.println("tpDefault(defaultValue) =   "
														+ tpDefault);
										if(tpDefault!= null){
										argParam.setDefaultParam(tpDefault);
										}
									}
									
								}//while(iterator8.hasNext())
								int size = valueList.size();
								System.out.println("size = "+ size);
								for(int j = 0;j < valueList.size();j++)
								{
									System.out.println("Now test valueList["+j+"]="+valueList.get(j));
									
								}
								argParam.setValues(valueList);
								
								argParams.add(argParam);
							} // if(!tagName.equals("") && tagName.equals("param"))
						}//  while (iterator6.hasNext())
						//appMetadata.setArgParams(argParams);
					}//if(!tagName.equals("") && tagName.equals("taggedParams"))
						//////
					if (!tagName.equals("")
							&& tagName.equals("groups")) {
						System.out.println("*****************start group******************* ");
						System.out.println("groups =   " + tagName);
						iterator9 = se.getChildElements();
						while (iterator9.hasNext()) {
							se = (SOAPElement) iterator9.next();
							tagName = se.getElementName()
									.getLocalName();
							if (!tagName.equals("")
									&& tagName.equals("group")) {
								GroupData group = new GroupData();
								ArrayList<ArgParamData> GargParams = new ArrayList();
								ArrayList<ArgFlagData> GargFlags = new ArrayList();
								//elements = new ArrayList();
								System.out.println("--------------------group-----------------");
								System.out.println("group =   "
										+ tagName);
								iterator10 = se.getChildElements();
								while (iterator10.hasNext()) {
									se = (SOAPElement) iterator10
											.next();
									tagName = se.getElementName()
											.getLocalName();
									if (!tagName.equals("")
											&& tagName.equals("name")) {
										String name = se.getValue();
										System.out
												.println("name =   "
														+ name);
										group.setName(name);
									}
									if (!tagName.equals("")
											&& tagName
													.equals("elements")) {
										String elements = se.getValue();
										System.out
												.println("elements =   "
														+ elements);
										
										group.setElements(elements);
										String [] eleArray = elements.split(" ");
										for(int j = 0;j < eleArray.length; j ++)
										{
											String ele = eleArray[j];								
											System.out.println("eleArray["+ j +"]=" + ele);
											for(int m = 0; m < argParams.size(); m++){
												String paramID = argParams.get(m).getRealID();
												if(ele.equals(paramID))
												{
													GargParams.add(argParams.get(m));
												}
											}
											for(int n = 0; n < argFlags.size(); n++){
												String flagID = argFlags.get(n).getRealID();
												if(ele.equals(flagID))
												{
													GargFlags.add(argFlags.get(n));
												}
											}
										}
										group.setArgFlags(GargFlags);
										group.setArgParams(GargParams);
									}
									if (!tagName.equals("")
											&& tagName
													.equals("required")) {
										String required = se
												.getValue();
										System.out
										.println("required =   "
												+ required);
										if(required.equals("true")){
										
										group.setRequired(Boolean.parseBoolean(required));
										}
									}
									if (!tagName.equals("")
											&& tagName
													.equals("exclusive")) {
										String exclusive = se
												.getValue();
										System.out
										.println("exclusive =   "
												+ exclusive);
										if(exclusive.equals("true")){
										group.setExclusive(Boolean.parseBoolean(exclusive));
										}
									}
									if (!tagName.equals("")
											&& tagName
													.equals("textDesc")) {
										String textDesc = se
												.getValue();
										System.out
												.println("textDesc =   "
														+ textDesc);
										group.setTextDesc(textDesc);
									}
								}//while (iterator5.hasNext())
								
								groups.add(group);
							}// if(!tagName.equals("") && tagName.equals("group")) flag parse over
						}// while (iterator4.hasNext())
						appMetadata.setGroups(groups);
					}// if(!tagName.equals("") && tagName.equals("groups"))


					
				}// while(iterator3.hasNext()         			    
			}// if(!tagName.equals("") && tagName.equals("types"))
		}//while (iterator3.hasNext())
	}
		if (tagName != null && tagName.equals("parallel")) {
			String parallel = se.getValue();
			System.out.println("parallel=   " + parallel);
			if(parallel.equals("false"))	appMetadata.setparallel(false);
			if(parallel.equals("true"))		appMetadata.setparallel(true);
		}
		if (tagName != null && tagName.equals("binaryLocation"))
		{
			String binaryLocation = se.getValue();
			System.out.println("binaryLocation=   " + binaryLocation);
			if(binaryLocation==null||binaryLocation.equals("")){}
			else{appMetadata.setbinaryLocation(binaryLocation);}
		}
	//}//while(iterator2.hasNext())
		}
}//   if(iterator1.hasNext())
}//else
//make persistent
PersistenceManager pm = PMF.get().getPersistenceManager();
try {
    pm.makePersistent(appMetadata);
} finally {
    pm.close();
}
//make persistent

return appMetadata;
}
	
  public static void main( String[] args ) throws Exception {
    
	  
  }
}