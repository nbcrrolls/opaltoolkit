<%--
 copy the license here

 
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-nested" prefix="nested" %>

<%@page import="java.util.Enumeration"%>
<%@page import="edu.sdsc.nbcr.opal.JobSubOutputType"%>
<%@page import="edu.sdsc.nbcr.opal.StatusOutputType"%>
<html>
<head>
<title>Job submission result</title>
<link rel="stylesheet" type="text/css" href="css/default.css" /> 
<link rel="stylesheet" type="text/css" href="css/base.css" />



</head>

<body>
<jsp:include page="header.jsp"/>
<br/>
    <h1>Submission results for <bean:write name="appMetadata" property="serviceName" /></h1>
<br/>

<table cellspacing="10">

    <tr><td>JobId :</td><td> <bean:write name="appMetadata" property="jobId" /> </td>

    <tr><td>Status code:</td><td><%= status.getCode() %></td>
    
    <tr><td>Message:</td><td><%= status.getMessage() %></td>
    
    <tr><td>Output Base URL:</td><td><a href="<%= status.getBaseURL() %>"><%= status.getBaseURL() %></a></td>
    
</table>

<br/>
<jsp:include page="footer.jsp"/>
</body>
</html>
