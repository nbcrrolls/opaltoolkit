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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<META HTTP-EQUIV="Refresh" CONTENT="30">
    <title>Job submission result</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <link href="css/style.css" media="all" rel="stylesheet" type="text/css" /> 
    <script language="javascript" type="text/javascript" ></script>
    <script src="js/scripts.js" language="javascript" type="text/javascript" ></script>

<%
StatusOutputType status =  (StatusOutputType) request.getAttribute("status");
String serviceID = (String)request.getAttribute("serviceID");
String jobId = (String)request.getAttribute("jobId");
%>

</head>

<body>
<jsp:include page="header.jsp"/>
<br/>
    <h2>Submission results for <%= serviceID %></h2>
<br/>

<table cellspacing="10">

    <tr><td>JobId :</td><td> <%= jobId %></td>

    <tr><td>Status code:</td><td><%= status.getCode() %></td>
    
    <tr><td>Message:</td><td><%= status.getMessage() %></td>
    
    <tr><td>Output Base URL:</td><td><a href="<%= status.getBaseURL() %>"  target="_blank" ><%= status.getBaseURL() %></a></td>
    
</table>

<br/>
<jsp:include page="footer.jsp"/>

