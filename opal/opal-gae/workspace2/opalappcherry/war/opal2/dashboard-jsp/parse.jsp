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
<%
	//url = "http://ws.nbcr.net/opal2/services/Pdb2pqrOpalService";
	//sender
	
	AppMetadataData appMetadata = SoapClient.soapCall(url);
	String appServiceName = appMetadata.getServiceName();
	String appUrl = appMetadata.getURL();
	Text info = appMetadata.getInfo();
	String usage = appMetadata.getUsage();
	//ArrayList<ArgFlag> argFlags = appMetadata.getArgFlags();
	String separator = appMetadata.getSeparator();
	//ArrayList<ArgParam> argParams = appMetadata.getArgParams();
	List<GroupData> groups = appMetadata.getGroups();
%>
<br>
<form action="launchJob.jsp" enctype="multipart/form-data" >
<table class="" border="1" cellspacing="1" cellpadding="1" align="center">
<tr><th align="center" colspan=3><p1>Submission form for <%=serviceName %></p1></th></tr>
<tr><td colspan=3><p5>*</p5> <p4>Required parameters.</p4></td></tr>

<%
for(int b = 0; b<groups.size(); b++)
{
	String textdesc = groups.get(b).getTextDesc();
	
	List<ArgParamData> GargParams = groups.get(b).getArgParams();
	List<ArgFlagData> GargFlags = groups.get(b).getArgFlags();
	
%>
	<tr>
		<th colspan=3 align="left"><p2><%=textdesc %>:<%=groups.get(b).isExclusive() %> </p2></th>
	</tr>
<%	
	for(int h = 0;h< GargFlags.size();h++)
	{
%>
	 <tr>
		<td>
			<p3><%=GargFlags.get(h).getRealID() %></p3>
		</td>
	 </tr>
     <tr>
<%
	if(groups.get(b).isExclusive()==true)
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
			<td>
				<p3><%=GargParams.get(k).getRealID() %></p3>
			</td>
		 </tr>
		 <tr>


			
			
<%
			boolean required = GargParams.get(k).isRequired();
			String defaultParam =  GargParams.get(k).getDefaultParam();
			if((GargParams.get(k).getType().equals("STRING"))&&(GargParams.get(k).getValues().size()!=0))
			{
%>
			<td>

				<%
						if(groups.get(b).isExclusive()==true)
						{
				%>
							
								<input type="radio" name=unused<%=groups.get(b).getName()%> value=<%=GargParams.get(k).getRealID()%>>
							
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
						if(groups.get(b).isExclusive()==true)
						{
				%>
								
								<input type="radio" name=unused<%=groups.get(b).getName()%> value=<%=GargParams.get(k).getRealID()%>>
								
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
						if(groups.get(b).isExclusive()==true)
						{
				%>
								
								<input type="radio" name=unused<%=groups.get(b).getName()%> value=<%=GargParams.get(k).getRealID()%>>
								
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
						if(groups.get(b).isExclusive()==true)
						{
				%>
							
								<input type="radio" name=unused<%=groups.get(b).getName()%> value=<%=GargParams.get(k).getRealID()%>>
							
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

%>
<!-- submit and reset buttons -->
<tr> 
  <td align="center" colspan=2>
  		<button class="Submit" type="submit">Submit</button>
  
		<button class="Reset" type="reset" onClick="window.location.reload()">Reset</button>
  </td>
</tr>
</table>
</form>
</body>
</html>