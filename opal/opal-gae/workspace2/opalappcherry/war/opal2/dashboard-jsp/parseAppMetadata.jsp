<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.List"%>
<%@ page import="cherry.glenda.lan.AppMetadataData"%>
<%@ page import="javax.xml.messaging.URLEndpoint"%>
<%@ page import="java.io.ByteArrayOutputStream"%>
<%@ page import="java.io.OutputStream"%>
<%@ page import="java.io.InputStream"%>
<%@ page import="java.net.URL"%>
<%@ page import="javax.xml.soap.*"%>
<%@ page import="java.io.FileOutputStream"%>
<%@ page import="java.io.File"%>
<%@page import="java.util.Iterator"%>
<%@ page import="cherry.glenda.lan.SoapClient"%>
<%@ page import="cherry.glenda.lan.ArgFlagData"%>
<%@ page import="cherry.glenda.lan.ArgParamData"%>
<%@ page import="cherry.glenda.lan.GroupData"%>
<%@ page import="com.google.appengine.api.datastore.Text"%>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="cherry.glenda.lan.PMF" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>


<%@ page import="javax.jdo.Query" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>

 <style type="text/css">
p1 	{
		font: normal bold 24px arial, sans-serif;
	}
p2 	{
		font: normal bold 20px arial, sans-serif;
	}
p3 	{
		font: normal normal 14px arial, sans-serif;
	}
p4 	{
		font: normal normal 12px arial, sans-serif;
	}
p5 	{
		color:red;
	}

</style>
<!-- Yahoo UI library --> 
<script src="scripts/yahoo-min.js"></script> 
<script src="scripts/dom-min.js"></script> 
<script language="javascript">
function selectElement(element, groupId){

    var elements = YAHOO.util.Dom.getElementsByClassName(groupId, 'input'); 
    for ( i in elements ) {
        elements[i].disabled=true;
        elements[i].value="";
    }

    var selectedElem = document.getElementsByName(element);
    for (i in selectedElem){
        selectedElem[i].disabled=false;
    }//for
}
</script> 
</head>
<body>

<%
	String serviceName = request.getParameter("serviceName");
	
	String url = request.getParameter("url");
%>
<br>
	url = <%=url%>
<br>
	serviceName = <%=serviceName%>
<br>
<%
	//url = "http://ws.nbcr.net/opal2/services/Pdb2pqrOpalService";
	//sender
	
	PersistenceManager pm0 = PMF.get().getPersistenceManager();
	Query query0 = pm0.newQuery(AppMetadataData.class);
    query0.setFilter("URL == url");
    query0.declareParameters("String url");


	List<AppMetadataData> appMetadataDatas0 = (List<AppMetadataData>) query0.execute(url);
	out.println("isEmpty="+appMetadataDatas0.isEmpty()+"<br>");
	out.println("size="+appMetadataDatas0.size()+"<br>");
	
%>
<%
	String appServiceName="";
	String appUrl;
	Text info = null; 
	String usage;
	String separator;
	List<GroupData> groups = new ArrayList<GroupData>();
	if (appMetadataDatas0.isEmpty())
	{
		AppMetadataData appMetadata = SoapClient.soapCall(url);
		
		appServiceName = appMetadata.getServiceName();
		appUrl = appMetadata.getURL();
		info = appMetadata.getInfo();
		usage = appMetadata.getUsage();
		separator = appMetadata.getSeparator();
		groups = appMetadata.getGroups();
		
		pm0= PMF.get().getPersistenceManager();
		Query query1 = pm0.newQuery(AppMetadataData.class);
	    query1.setFilter("URL == url");
	    query1.declareParameters("String url");


		appMetadataDatas0 = (List<AppMetadataData>) query1.execute(url);
		
	} 	
	session.setAttribute("URL",url);
	
	
%>
<br>
<%
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>
<form action="<%= blobstoreService.createUploadUrl("/upload")%>" enctype="multipart/form-data" method="post">
<table class="" border="1" cellspacing="1" cellpadding="1" align="center">
<tr><th align="center" colspan=3><p1>Submission form for <%=serviceName %></p1></th></tr>
<tr><td colspan=3><p5>*</p5> <p4>Required parameters.</p4></td></tr>
<tr><td colspan=3><input type="hidden" name="url" value="<%=url%>"></td></tr>
<%

	for (AppMetadataData app : appMetadataDatas0) 
	{
		
		for(int b = 0; b < app.getGroups().size(); b++)
		{
			String textdesc = app.getGroups().get(b).getTextDesc();
			List<ArgParamData> GargParams = app.getGroups().get(b).getArgParams();
			List<ArgFlagData> GargFlags = app.getGroups().get(b).getArgFlags();
			
		%>
			<tr>
				<th colspan=3 align="left"><p2><%=textdesc %>:<%=app.getGroups().get(b).isExclusive() %> </p2></th>
			</tr>
		<%	
			for(int h = 0;h< GargFlags.size();h++)
			{
		%>
			 
		     <tr>
		<%
			if(app.getGroups().get(b).isExclusive()==true)
			{
		%>
				<td>
					<input type="radio" checked="checked" name=<%=GargFlags.get(h).getRealID() %> value=<%=GargFlags.get(h).getTag()%>>
					<p3><%=GargFlags.get(h).getTextDesc()%></p3>
				</td>
		<%	
			}else{
		%>
				
		<%		
			}
			
				boolean checked = GargFlags.get(h).getDefault();
				String check = "";
				if(checked==true)
				{
					
		%>     
			    <td>
					<input type="checkbox" name=<%=GargFlags.get(h).getRealID() %> checked="yes">
					<p3><%=GargFlags.get(h).getTextDesc()%></p3>
				</td>
		<%
				}else{
		%>
		 		<td>
					<input type="checkbox" name=<%=GargFlags.get(h).getRealID() %> >
					<p3><%=GargFlags.get(h).getTextDesc()%></p3>
				</td>
		<%			
				}
			
		%>		
				
			 </tr>
		<%
			}//for flags
		
			for(int k = 0;k< GargParams.size();k++)
			{
				String defaultP = "";
		%>
				
				 <tr>
		
		
					
					
		<%
					boolean required = GargParams.get(k).isRequired();
					String defaultParam =  GargParams.get(k).getDefaultParam();
					if((GargParams.get(k).getType().equals("STRING"))&&(GargParams.get(k).getValues().size()!=0))
					{
		%>
					<td>
		
						<%
								if(app.getGroups().get(b).isExclusive()==true)
								{
						%>
									
										<input type="radio" name=unused<%=app.getGroups().get(b).getName()%> value=<%=GargParams.get(k).getRealID()%>>
									
						<%
								}else{
						%>
									
						<%			
								}
						%>
					
						<p3><%=GargParams.get(k).getTextDesc() %></p3>
		<% 
						
						if(required==true)
						{
		%>	
							<p5>*</p5>
		<%
						}
		%>			
					</td>
					<td>
		
		<%
						ArrayList<String> valueList = GargParams.get(k).getValues();
						
						
						
							for(int n = 0; n<valueList.size(); n++)
							{
								String value = valueList.get(n);
								if(value.equals(defaultParam))
								{
			%>
									
									<input type="radio" checked="checked" name=<%=GargParams.get(k).getRealID() %> value=<%=value%>><p4><%=value %></p4>
									
			<%
								}else{
			%>
								
									<input type="radio" name=<%=GargParams.get(k).getRealID() %> value=<%=value %>><p4><%=value %></p4>
									
			<%					
								}
							}
		%>
					</td>
		<%
						
					}//end if value
					else
					if((GargParams.get(k).getType().equals("FILE"))&&(GargParams.get(k).getIoType().equals("INPUT")))
					{
		%>
					<td>
		<%
								if(app.getGroups().get(b).isExclusive()==true)
								{
						%>
										
										<input type="radio" name=unused<%=app.getGroups().get(b).getName()%> value=<%=GargParams.get(k).getRealID()%>>
										
						<%
								}else{
						%>
									
						<%			
								}
						%>
					
						<p3><%=GargParams.get(k).getTextDesc() %></p3>
		<% 
						
						if(required==true)
						{
		%>	
							<p5>*</p5>
		<%
						}
		%>			
					</td>
					<td>
						<input type="file" name=<%=GargParams.get(k).getRealID() %> size="">
					</td>
		<%				
					}//end if input file
					else
					if(GargParams.get(k).getType().equals("BOOLEAN"))
					{
		%>
					<td>
		<%
								if(app.getGroups().get(b).isExclusive()==true)
								{
						%>
										
										<input type="radio" name=unused<%=app.getGroups().get(b).getName()%> value=<%=GargParams.get(k).getRealID()%>>
										
						<%
								}else{
						%>
									
						<%			
								}
						%>
					
						<p3><%=GargParams.get(k).getTextDesc() %></p3>
		<% 
						
						if(required==true)
						{
		%>	
							<p5>*</p5>
		<%
						}
		%>			
					</td>
					<td>
		<%			
						if(required==true)
						{
		%>
							
							<input type="checkbox" name=<%=GargParams.get(k).getRealID()%> checked="yes">
							
		<%				
						}else{
		%>
							
							<input type="checkbox" name=<%=GargParams.get(k).getRealID() %> >
							
		<%					
						}
		%>
					</td>
		<%
					}//end if boolean
					else{
		%>
					<td>
		<%
								if(app.getGroups().get(b).isExclusive()==true)
								{
						%>
									
										<input type="radio" name=unused<%=app.getGroups().get(b).getName()%> value=<%=GargParams.get(k).getRealID()%>>
									
						<%
								}else{
						%>
									
						<%			
								}
						%>
					
						<p3><%=GargParams.get(k).getTextDesc() %></p3>
		<% 
						
						if(required==true)
						{
		%>	
							<p5>*</p5>
		<%
						}
		%>			
					</td>
		<%
						
							if(defaultParam==null)
							{
							 	defaultP = "";
							}else{
								defaultP = defaultParam;
							}
						
		%>
					<td>				
									<input type="text" name=<%=GargParams.get(k).getRealID() %> value=<%=defaultP %>>
					</td>				
		<%					
							
		%>
		<%					
						}
		%>
				
			 </tr>
		<%
			}//for params
		
		}//for groups
	}//for (a:b c)
pm0.close();
%>
<!-- submit and reset buttons -->
<tr> 
  <td align="center" colspan=2>
  		<button type="submit" name="submit">Submit</button>
  
		<button class="Reset" type="reset" onClick="window.location.reload()">Reset</button>
  </td>
</tr>
</table>
</form>
<%
session.setAttribute("tab","sessionHave");
%>
</body>
</html>