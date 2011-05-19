<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="cherry.glenda.lan.Job" %>
<%@ page import="cherry.glenda.lan.GeneralFunction" %>
<%@ page import="cherry.glenda.lan.QueryStatusEnvelope" %>
<%@ page import="cherry.glenda.lan.SoapQueryStatus" %>
<%@ page import="cherry.glenda.lan.GetStaticsEnvelope" %>
<%@ page import="cherry.glenda.lan.SoapGetJobStatistics" %>
<%@ page import="cherry.glenda.lan.PMF" %>
<%@ page import="java.util.Date" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<html>
<head>

</head>
<body>
<%
	String jobOpalIDEnv = request.getParameter("jobOpalIDEnv");
	String url = request.getParameter("url");
	System.out.println("the url ="+url);
	if(jobOpalIDEnv==null||jobOpalIDEnv.equals("")){}
	else{
					Job job = GeneralFunction.getJobByJobOpalID(jobOpalIDEnv);
			%>
				<table border="1" class="one" align="center" >
					<form action="/refreshJob?jobOpalIDEnv=<%=jobOpalIDEnv%>&&url=<%=url%>" method="post">
							<h2 align="center">Submission results </h2>
						<tr>
							<td align="left"><h4>JobId :</h4></td>
							<td align="left"><h4><%=job.getJobOpalID() %></h4></td>
						</tr>
						<tr>
							<td align="left"><h4>Status code :</h4></td>
							<td align="left"><h4><%=job.getStatusCode() %></h4></td>
						</tr>
						<tr>
							<td align="left"><h4>Message :</h4></td>
							<td align="left"><h4><%=job.getMessage() %></h4></td>
						</tr>
						<tr>
							<td align="left"><h4>Output Base URL :</h4></td>
							<td align="left"><h4><a href="<%=job.getBeseURL()%>"><%=job.getBeseURL() %></a></h4></td>
						</tr>
						<tr>
				  			<td align="center" colspan=2>
				  				<button type="submit" name="submit">Refresh</button>
				  			</td>
						</tr>
					</form>
				</table>
			<%
		}

%>
</body>
</html>