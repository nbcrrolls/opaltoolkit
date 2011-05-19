<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="cherry.glenda.lan.Job" %>
<%@ page import="cherry.glenda.lan.GeneralFunction" %>
<%@ page import="cherry.glenda.lan.QueryStatusEnvelope" %>
<%@ page import="cherry.glenda.lan.SoapQueryStatus" %>
<%@ page import="cherry.glenda.lan.GetStaticsEnvelope" %>
<%@ page import="cherry.glenda.lan.SoapGetJobStatistics" %>
<%@ page import="cherry.glenda.lan.PMF" %>
<%@ page import="javax.jdo.JDOHelper" %>
<%@ page import="java.util.Date" %>
<%@ page import="javax.jdo.PersistenceManager" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>

<%
String jobOpalIDEnv = request.getParameter("jobOpalIDEnv");
String url = request.getParameter("url");
if(jobOpalIDEnv==null||jobOpalIDEnv.equals("")){}
else
{
	Job job = GeneralFunction.getJobByJobOpalID(jobOpalIDEnv);
	QueryStatusEnvelope queryStatusENV;
	try 
	{
		queryStatusENV = SoapQueryStatus.QueryStatus(job.getJobOpalID(),url);
		if(queryStatusENV!=null)
		{
			if(queryStatusENV.getcode()!=4 && queryStatusENV.getcode()!=8)
			{
						queryStatusENV = SoapQueryStatus.QueryStatus(jobOpalIDEnv,url);
						System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+"<br>");
						System.out.println("code[according to soapQueryStatus] ="+queryStatusENV.getcode()+"<br>");
						System.out.println("message[according to soapQueryStatus] ="+queryStatusENV.getmessage()+"<br>");
						System.out.println("baseURL[according to soapQueryStatus] = "+queryStatusENV.getbaseURL()+"<br>");
						job.setStatusCode(queryStatusENV.getcode());
						job.setMessage(queryStatusENV.getmessage());
						job.setBeseURL(queryStatusENV.getbaseURL());
					if((job.getStatusCode())==1){job.setStatus("STATUS_PENDING");}
					if((job.getStatusCode())==2)
					{
						job.setStatus("STATUS_ACTIVE");
						GetStaticsEnvelope gsVe1 = SoapGetJobStatistics.getStatistics(job.getJobOpalID(),url);
						if(gsVe1!=null){
						Date activationTime =gsVe1.getactivationTime();
						job.setActiveTime(activationTime);
						}
					}
					/*
					PersistenceManager pmJob4 = PMF.get().getPersistenceManager();
					try {
						pmJob4.makePersistent(job);
					} finally {
						pmJob4.close();
					}
					*/
			

				%>
				<script language="JavaScript"> 
				function refresh() { window.location.reload( true ); }
				setTimeout("refresh()", 30*100); 
				</script> 
				<%
				
			}
			if((job.getStatusCode())==4){job.setStatus("STATUS_FAILED");}
			if((job.getStatusCode())==8)
			{
				job.setStatus("STATUS_DONE");
				//List<Date> dateList =
				GetStaticsEnvelope gsVe2 = SoapGetJobStatistics.getStatistics(jobOpalIDEnv,url);
			
				if(gsVe2 !=null){
				Date completionTime = gsVe2.getcompletionTime();
				job.setEndTime(completionTime);
				}
			}
			
			PersistenceManager pmJob3 = PMF.get().getPersistenceManager();
			try {
				pmJob3.makePersistent(job);
				JDOHelper.getPersistenceManager(job);
			} finally {
				pmJob3.close();
			}
			
		}
	} catch (Exception e) {	e.printStackTrace();} 


%>
</head>
<body>
<%
	
%>
				<table border="1" class="one" align="center" >
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
				</table>
<%
}
%>
</body>
</html>