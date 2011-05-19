<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="cherry.glenda.lan.GeneralFunction"%>
<%@ page import="cherry.glenda.lan.Job" %>
<%@ page import="cherry.glenda.lan.QueryStatusEnvelope" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Date" %>
<%@ page import="cherry.glenda.lan.SoapQueryStatus" %>

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
  String userID = user.getUserId();
  String userEmail = user.getEmail();
  //String url = session.getAttribute("URL").toString();
  List<Job> jobList  =  GeneralFunction.getAllJobByCurrentUser(userEmail);
  if(jobList.size()!=0)
  {
		%>
			<table border="1"  align = "center">
			<h3 align = "center">All the jobs Of <%=userEmail %></h3>
			<tr>
			<th>jobID</th>
			<th>service</th>
			<th>status</th>
			<th>statusCode</th>
			<th>message</th>
			<th>baseURL</th>
			<th>SubmitTime</th>
			<th>ActiveTime</th>
			<th>EndTime</th>
			</tr>
		<%
		  Job job = new Job();
		  for(int i = 0;i<jobList.size();i++)
		  {
			    job = jobList.get(i);
			  	String jobOpalID = job.getJobOpalID();
				String serviceName = job.getServiceName();
				int statusCode = job.getStatusCode();
				String status = job.getStatus();
				String message = job.getMessage();
				String baseURL = job.getBeseURL();
				Date SubmitTime = job.getSubmitTime();
				Date ActiveTime = job.getActiveTime();
				Date EndTime = job.getEndTime();
	%>			
				<tr>
					<td align = "right"><%=jobOpalID%></td>
					<td align = "center"><%=serviceName %></td>
					<td align = "center"><%=status %></td>
					<td align = "right"><%=statusCode %></td>
					<td align = "center"><%=message %></td>
					<td align = "center"><%=baseURL %></td>
					<td align = "right"><%=SubmitTime %></td>
					<%
					if(ActiveTime!=null){
					%>
					<td align = "right"><%=ActiveTime %></td>
					<%
					}else{
					%>
					<td align = "right"></td>
					<%	
					}
					
					if(EndTime!=null){
					%>
					<td align = "right"><%=EndTime %></td>
					<%
					}else{
					%>
					<td align = "right"></td>
					<%	
					}
					%>
				</tr>
				<%
		 }
  }else{
%>
You have not submitted any jobs. If you want to submit new job, please transfer to the related tab.
<% 
  }
		  %>
</body>
</html>