<%@ page contentType="text/html;charset=ISO-8859-1" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="cherry.glenda.lan.GetURLFile" %>
<%@ page import="cherry.glenda.lan.ServiceData" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="cherry.glenda.lan.PMF" %>
<html>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<head>
<link href="/css/style.css" media="all" rel="stylesheet" type="text/css" /> 
<style type="text/css">
table.one 
{
border-style: solid;

border-top-width: 1px;
border-bottom-width: 1px;
border-left-width: 1px;
border-right-width: 1px;
}
table.two 
{
border-style: solid;
border-top-width: thin
}
</style>
</head>
 <body>

<%
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
   
    if (user != null) 
    {
    	 String userCurrentEmail = user.getEmail();
    	 String userEmail = session.getAttribute("userEmail").toString();
    	if( userCurrentEmail.equals(userEmail))
    	{
%>
<p align="right">Hello, <%= user.getNickname() %>! (You can
<a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">sign out</a>.)</p>
<%
 
%>
<table align = "right">
<form action = "jobhistory.jsp">
<input type="submit" value="JobHistory">
</form>
</table>

<%



PersistenceManager pm0 = PMF.get().getPersistenceManager();
String query0= "select from " + ServiceData.class.getName();
List<ServiceData> services0 = (List<ServiceData>) pm0.newQuery(query0).execute();
out.println("services.isEmpty()= "+services0.isEmpty()+"<br>");

if (services0.isEmpty())
{
			String urltest = "http://ws.nbcr.net/opal2/opalServices.xml";
			GetURLFile gg = new GetURLFile();
			out.println("<br>");
										
			ArrayList<ServiceData> serviceList = gg.getHandle(urltest);
									
			PersistenceManager pm1 = PMF.get().getPersistenceManager();
			String query1 = "select from " + ServiceData.class.getName();
			List<ServiceData> services1 = (List<ServiceData>) pm1.newQuery(query1).execute();
			out.println("services.isEmpty()= "+services1.isEmpty()+"<br>");
			%>
			
			<br/>
			 <h2 align="center">Opal Service List</h2>
			<br/>
			<table border="1" class="one">
				<tr>
						<td align="center"><h4>No.</h4></td>
						<td align="center"><h4>Service</h4></td>
						<td align="center"><h4>Note</h4></td>
				</tr>
				<%	 
					for (ServiceData s : services1) 
					{
					%>
					<tr>
						<td><blockquote><%= s.getId()%></blockquote></td>
						<td><a href="/opal2/dashboard-jsp/parseAppMetadata.jsp?serviceName=<%=s.getService()%>&&url=<%=s.getUrl() %>"><%=s.getService() %></a></td>
						<td><%=s.getSummary()%>
						<br>Web service URL: <a href="<%=s.getUrl() %>"><%=s.getUrl() %></a>
						</td>
					</tr>
					<%
		 			}
			pm1.close();
	
} else {
				out.println("have records.");
			%>
			<br/>
			 <h2 align="center">Opal Service List</h2>
			<br/>
			<table border="1" class="one">
			
			<tr>
			<td align="center"><h4>No.</h4></td>
			<td align="center"><h4>Service</h4></td>
			<td align="center"><h4>Note</h4></td>
			</tr>
			<%	 
			for (ServiceData s : services0) 
			{
			%>
			<tr>
				<td><blockquote><%= s.getId()%></blockquote></td>
				<td><a href="/opal2/dashboard-jsp/parseAppMetadata.jsp?serviceName=<%=s.getService()%>&&url=<%=s.getUrl() %>"><%=s.getService() %></a></td>
				<td><%=s.getSummary()%>
				<br>Web service URL: <a href="<%=s.getUrl() %>"><%=s.getUrl() %></a>
				</td>
				</tr>
			<%
			}
		pm0.close();
}
%>


<%
    	}
 } else {
%>
<p align="right">Hello!
<a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Sign in</a>
to include your name with greetings you post.</p>
<%
    }
%>     		
  </body>
</html>
