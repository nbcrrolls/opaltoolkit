<%@ page contentType="text/html;charset=ISO-8859-1" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="cherry.glenda.lan.GetURLFile" %>
<%@ page import="cherry.glenda.lan.ServiceData" %>
<html>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
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
%>
<p align="right">Hello, <%= user.getNickname() %>! (You can
<a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">sign out</a>.)</p>
<%
  } else {
%>
<p align="right">Hello!
<a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Sign in</a>
to include your name with greetings you post.</p>
<%
    }
%>
<table align = "right">
<form action = "jobhistory.jsp">
<input type="submit" value="JobHistory">
</form>
</table>

<%
String urltest = "http://ws.nbcr.net/opal2/opalServices.xml";
GetURLFile gg = new GetURLFile();
ArrayList<ServiceData> arrayList2 = gg.getHandle(urltest);
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
		for (int t = 0; t < arrayList2.size(); t++)
		{
%>
  <tr>
	  <td><%=arrayList2.get(t).getId()%></td>
<%
		String serviceName = arrayList2.get(t).getService();
		String summary = arrayList2.get(t).getSummary();
		String url = arrayList2.get(t).getUrl();
%>
	  <td><a href="/opal2/dashboard-jsp/parse.jsp?serviceName=<%=serviceName%>&&url=<%=url %>"><%=serviceName %></a></td>
	  <td><%=summary%>
	  <br>Web service URL: <a href="<%=url %>"><%=url %></a>
	  </td>
	  
  </tr>


<%				
		}
%>
</table>
     		
  </body>
</html>
