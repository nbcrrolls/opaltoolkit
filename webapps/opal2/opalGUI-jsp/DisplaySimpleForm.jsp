<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-nested" prefix="nested" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@page import="java.util.Enumeration"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="edu.sdsc.nbcr.opal.gui.common.AppMetadata"%>
<%@page import="org.apache.struts.upload.FormFile"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Submission form for <bean:write name="appMetadata" property="serviceName" /></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <link href="css/style.css" media="all" rel="stylesheet" type="text/css" /> 
    
    <script src="scripts/scripts.js" language="javascript" type="text/javascript" ></script> 

<script language="javascript">
<!--

var state = 'none';

function makeTrue(){

}

function showHide(layer_ref) {
    if (state == 'block') {
        state = 'none';
    }
    else {
        state = 'block';
    }
    if (document.all) { //IS IE 4 or 5 (or 6 beta)
        eval( "document.all." + layer_ref + ".style.display = state");
    }
    if (document.layers) { //IS NETSCAPE 4 or below
        document.layers[layer_ref].display = state;
    }
    if (document.getElementById &&!document.all) {
        hza = document.getElementById(layer_ref);
        hza.style.display = state;
    }
}

//-->
</script> 
</head>

<body>
<div class="mainBody">



<jsp:include page="../include-jsp/header.jsp"/>
<jsp:include page="header.jsp"/>


<br/>
    <h2>Submission form for <bean:write name="appMetadata" property="serviceName" /></h2>
<br/>

<html:form action="LaunchJob.do" enctype="multipart/form-data" >
<table cellspacing="10">

    <tr><td>Insert command line here:</td><td><html:text property="cmdLine" size="50"/></td>
 <logic:equal name="appMetadata" property="parallel" value="true">
    <tr><td>Insert number of CPU for parallel application:</td><td><html:text property="numCpu" size="50"/></td>
 </logic:equal>
     
<% 

AppMetadata app = (AppMetadata) request.getSession().getAttribute("appMetadata");
FormFile [] files = app.getFiles();

if ( files.length > 1 ) {
    //let's print the name of the file already uploaded
    for (int i = 0; i < files.length - 1; i++ ){
        
        String name = null;
        if ( files[i] != null ) {
            name = files[i].getFileName();
        }else name = "null file at " + i;
        
        out.println("<tr><td colspan=\"2\"> Uploaded file: " + name + "</td></tr>");
    }
}

String index = "" + (files.length - 1);
%>
    <tr><td>Chose input file:</td><td><nested:file property="<%= \"files[\" + index + \"]\" %>" size="40"/></td>
    
    <!-- end file upload part -->

    
    
    <html:hidden property="addFile" value="false" />
    <!-- submit and reset button  -->
    <tr> <td colspan="2" align="left"> <html:submit value="Submit"/> <html:reset value="Reset"/> 
    <input type="button" onclick="document.appMetadata.addFile.value='true'; document.appMetadata.submit()" value="Add Another File"/> </td> </tr>
</table>
</html:form>
<span style="text-decoration: underline; " onclick="showHide('help')">Show/Hide help</span>

<div id="help" style="display: none;">
    <p class="manual" >Application Description: <bean:write name="appMetadata" property="usage"/> </p>

    <logic:notEmpty name="appMetadata" property="info">
    <p class="manual" >Usage Info:</br>
        <logic:iterate id="infoString" name="appMetadata" property="info"> 
            <pre> <c:out value="<%= infoString %>"> </c:out> </pre> 
        </logic:iterate>
    </p>
    </logic:notEmpty>
</div>
</br>

<p>* Required parameters.</p>

<br/>
<jsp:include page="footer.jsp"/>
