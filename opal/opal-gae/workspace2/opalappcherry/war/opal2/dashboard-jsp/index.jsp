<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<%
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    
    if (user != null) 
    {
    	String userEmail = user.getEmail();
        session.setAttribute("userEmail",userEmail);
%>
<p align="right">Hello, <%= user.getNickname() %>! (You can
<a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">sign out</a>.)</p>
<div class="mainBody">
<table border="0" class="mainnav" cellpadding="0" cellspacing="0">
<tr>
  <td>
    <div id="list-nav" >
    <ul>
	  <li><a href="/opal2/dashboard-jsp/displayService.jsp">Submit New Job</a></li>
      <li><a href="/opal2/dashboard-jsp/jobhistory.jsp">JobHistory</a></li>
    </ul>
	</div>
  </td>
</tr>
</table>
<br>
</div>
<%
  } else {
%>
<p align="right">Hello!
<a href="<%= userService.createLoginURL(request.getRequestURI()) %>">Sign in</a>
to submit job and visit job history.</p>
<%
    }
%>
</body>
</html>