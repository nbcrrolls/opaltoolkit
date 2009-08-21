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
<%@ page import="org.apache.struts.Globals" %>
<%@ page import="edu.sdsc.nbcr.opal.gui.common.Constants;" %>
<html:html>
  <head>
    <title>Unexpected Error</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <link href="css/style.css" media="all" rel="stylesheet" type="text/css" />
    <script src="scripts/scripts.js" language="javascript" type="text/javascript" ></script>

  </head>
  <body>
    <jsp:include page="header.jsp"/>
    
    <h3>An unexpected error has occured</h3>
    <logic:present name="<%=Constants.ERROR_MESSAGES%>">
        <ul>
            <logic:iterate id="error" name="<%=Constants.ERROR_MESSAGES%>" indexId="index">
                <li><bean:write name="error"/></li>
            </logic:iterate>
        </ul>
    </logic:present>
    <logic:present name="<%=Globals.EXCEPTION_KEY%>">
        <p><bean:write name="<%=Globals.EXCEPTION_KEY%>"
                       property="message"/></p>
    </logic:present>

    <jsp:include page="footer.jsp"/>

  </body>
</html:html>
