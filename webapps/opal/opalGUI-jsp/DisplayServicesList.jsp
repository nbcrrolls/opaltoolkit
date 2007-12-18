<%--
    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at
   
         http://www.apache.org/licenses/LICENSE-2.0
   
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-nested" prefix="nested" %>
<%@page import="edu.sdsc.nbcr.opal.gui.common.OPALService"%>

<html>
<head>
    <title>Opal Based Web Services Available</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <link href="css/style.css" media="all" rel="stylesheet" type="text/css" /> 
    <script language="javascript" type="text/javascript" ></script>
    <script src="js/scripts.js" language="javascript" type="text/javascript" ></script> 
</head>
<body>
<jsp:include page="header.jsp"></jsp:include>
<h1>Opal Based Web Services Available</h1>
<p>Click on one of the available services to get a submission form</p>

<ul>
<nested:iterate id="service" name="servicesList" indexId="indexService">
<br/>
<%  
    OPALService opalService =  (OPALService) service;
    String url = opalService.getURL();
%>
<li style="font-size: large"> <a href="CreateSubmissionForm.do?serviceURL=<%= java.net.URLEncoder.encode(url)  %>"> <bean:write name="service" property="serviceName" /></a></li>
<!-- <html:form action="CreateSubmissionForm" method="GET">
    <p>Service <bean:write name="service" property="serviceName" /> <br/><html:submit value="Invoke it"></html:submit>
    </p>
    <html:hidden property="serviceURL" value="<%= url %>"></html:hidden>
</html:form>  -->

</nested:iterate>
</ul>

<jsp:include page="footer.jsp"></jsp:include>

