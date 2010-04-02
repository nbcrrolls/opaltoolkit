<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-nested" prefix="nested" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@page import="java.util.Enumeration"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Opal2 submission form</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <link href="css/style.css" media="all" rel="stylesheet" type="text/css" /> 
    
    <script src="scripts/scripts.js" language="javascript" type="text/javascript" ></script> 
    <script src="scripts/jquery.js" language="javascript" type="text/javascript" ></script>
    <script src="scripts/jquery.corner.js" language="javascript" type="text/javascript" ></script>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#list-nav ul li.left a").corner("tl bl 10px  cc:#fff");
            $("#list-nav ul li.right a").corner("tr br 10px cc:#fff");
        });
    </script>

<!-- Yahoo UI library --> 
<script src="scripts/yahoo-min.js"></script> 
<script src="scripts/dom-min.js"></script> 

<script language="javascript">
<!--
var state = 'none';

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

function selectElement(element, groupId){

    var elements = YAHOO.util.Dom.getElementsByClassName(groupId, 'input'); 
    for ( i in elements ) {
        elements[i].disabled=true;
        elements[i].value="";
    }

    var selectedElem = document.getElementsByName(element);
    for (i in selectedElem){
        selectedElem[i].disabled=false;
    }//for
}

//-->
</script> 
</head>

<body>

<div class="mainBody">                                                         

<jsp:include page="../dashboard-jsp/header.jsp"/>
<!-- Navigation Menu Bar -->
<table border="0" class="mainnav" cellpadding="0" cellspacing="0">
<tr>
  <td>
    <div id="list-nav">
    <ul>
      <li class="left"><a href="dashboard">Home</a></li>
      <li><a href="dashboard?command=serverInfo">Server Info</a></li>
      <li><a href="dashboard?command=serviceList" class="active">List of applications</a></li>
      <li><a href="dashboard?command=statistics">Usage Statistics</a></li>
      <li class="right"><a href="dashboard?command=docs">Documentation</a></li>
    </ul>
    </div>
  </td>
</tr>
</table> 
<br>

<jsp:include page="header.jsp"/>

<h2 id="centered">
    <span class="Label"><bean:write name="appMetadata" property="serviceName"/>
	submission form </span>
</h2>
<p><span class="Require">*</span> Required parameters.</p>

<html:form action="LaunchJob.do" enctype="multipart/form-data" >

<table border="0" cellspacing="5">
<tr> <!-- the number of cpu  -->
   <td> Insert number of CPUs (only for parallel application): </td>
   <td><nested:text name="appMetadata" property="numCpu" size="40"/></td>
</tr>

<!--  double nested tag does not work with input field vaule so I had to use long format -->

<% boolean exclusiveGroup = false; %>
<!-- BEGIN iteration over groups -->
<logic:notEmpty name="appMetadata" property="groups"> 
  <nested:iterate id="group" name="appMetadata" property="groups" indexId="indexGroup">
    <!--------- --------------------------------------------------------------------->
    <!--------- print group description if available ---------------------------------------->
    <logic:notEmpty name="group" property="textDesc">
    <tr>
      <td colspan="2"><br><span class="groupTitle"><nested:write filter="false" property="textDesc"/></span></td>
    </tr>
	</logic:notEmpty>

    <nested:equal value="true" name="group" property="exclusive" > <% exclusiveGroup = true; %> </nested:equal>

    <!-- BEGIN tagged and untagged parameters -->
    <% boolean testCond = false; %>
    <logic:notEmpty name="group" property="argParams">
      <nested:iterate id="param" name="group" property="argParams" indexId="indexParam" >

        <nested:equal value="true" name="param" property="required"> <tr class="requiredParam" > </nested:equal>
        <nested:notEqual value="true" name="param" property="required"> <tr> </nested:notEqual>

        <nested:equal value="BOOLEAN" name="param" property="type">
        <!-- we have a check box -->
        <% testCond = true; %>
          <td colspan="2"> 
            <!-- BEGIN radio box for exclusive group -->
            <logic:equal value="true" name="appMetadata" property="<%=\"groups[\" + indexGroup + \"].exclusive\"%>">
              <input type="radio" name="unused" value="value" onClick="selectElement('<%="groups[" + indexGroup + "].argParams[" + indexParam + "].selectedValue"%>', 'group<%=indexGroup%>')"/>
            </logic:equal> <!-- END radio box -->

             <nested:checkbox name="appMetadata" styleClass="<%=\"group\" + indexGroup%>" property="<%=\"groups[\" + indexGroup + \"].argParams[\" + indexParam + \"].selectedValue\"%>" />
             <nested:write  filter="false" name="param" property="textDesc"/>
             <nested:equal value="true" name="param" property="required"><span class=Require">*</span></nested:equal>
          </td>
        </nested:equal>

        <nested:equal value="FILE" name="param" property="type"> 
          <nested:equal value="INPUT" name="param" property="ioType">
          <!--  we have a file -->
          <% testCond = true; %>
            <td>
              <!-- BEGIN radio box for exclusive group -->
              <logic:equal value="true" name="appMetadata" property="<%=\"groups[\" + indexGroup + \"].exclusive\"%>">
                <input type="radio" name="unused" value="value" onClick="selectElement('<%="groups[" + indexGroup + "].argParams[" + indexParam + "].file"%>', 'group<%=indexGroup%>')" /> 
              </logic:equal> <!-- END radio box -->
  
              <nested:write  filter="false" name="param" property="textDesc"/>
			  <span class="Require"><nested:equal value="true" name="param" property="required"><span class=Require">*</span></nested:equal></span>
            </td>
            <td>
              <nested:file name="appMetadata" styleClass="<%=\"group\" + indexGroup%>" property="<%=\"groups[\" + indexGroup + \"].argParams[\" + indexParam + \"].file\"%>" size="30"/>
            </td>
          </nested:equal>
        </nested:equal>

        <nested:equal value="STRING" name="param" property="type"> 
          <nested:notEmpty name="param" property="values">
          <!-- we have radio button -->
          <% testCond = true; %>
            <td>
              <!-- BEGIN radio box for exclusive group -->
              <logic:equal value="true" name="appMetadata" property="<%=\"groups[\" + indexGroup + \"].exclusive\"%>">
                <input type="radio" name="unused" value="value" onClick="selectElement('<%="groups[" + indexGroup + "].argParams[" + indexParam + "].selectedValue"%>', 'group<%=indexGroup%>')"/> 
              </logic:equal> <!-- END radio box -->
  
              <nested:write  filter="false" name="param" property="textDesc"/>
              <nested:equal value="true" name="param" property="required"><span class="Require">*</span></nested:equal>
            </td>
            <td>
              <nested:iterate id="valueRadio" name="param" property="values" >
              <html:radio name="appMetadata" styleClass="<%=\"group\" + indexGroup%>" property="<%=\"groups[\" + indexGroup + \"].argParams[\" + indexParam + \"].selectedValue\"%>" value="<%= valueRadio.toString() %>"/>
              <%= valueRadio.toString() %>  
              </nested:iterate>
            </td>
          </nested:notEmpty> 
        </nested:equal>     

        <% if (testCond == false) { %>
        <!-- this is a normal text box -->
          <td>
            <!-- BEGIN radio box for exclusive group -->
            <logic:equal value="true" name="appMetadata" property="<%=\"groups[\" + indexGroup + \"].exclusive\"%>">
              <input type="radio" name="unused" value="value" onClick="selectElement('<%="groups[" + indexGroup + "].argParams[" + indexParam + "].selectedValue"%>', 'group<%=indexGroup%>')"/> 
            </logic:equal> <!-- END radio box -->

            <nested:write  filter="false" name="param" property="textDesc"/>
            <nested:equal value="true" name="param" property="required"><span class="Require">*</span></nested:equal>
          </td>
          <td>
            <nested:text name="appMetadata" styleClass="<%=\"group\" + indexGroup%>" property="<%=\"groups[\" + indexGroup + \"].argParams[\" + indexParam + \"].selectedValue\"%>" size="40"/>
          </td> 
        <% } %>            
        <% testCond = false; %>
        </tr>
      </nested:iterate>
	</logic:notEmpty> <!-- END tagged and untagged parameters -->
        
    <!-- BEGIN flag  -->
    <logic:notEmpty name="group" property="argFlags">
      <nested:iterate id="flag" name="group" property="argFlags" indexId="indexFlag">
      <!-- lets draw flag parameters  name="flag"  -->
        <tr>
          <td colspan="2">
            <!-- BEGIN radio box for exclusive group -->
            <logic:equal value="true" name="appMetadata" property="<%=\"groups[\" + indexGroup + \"].exclusive\"%>">
              <input type="radio" name="unused" value="value" onClick="selectElement('<%="groups[" + indexGroup + "].argFlags[" + indexFlag + "].selected"%>', 'group<%=indexGroup%>')"/>
            </logic:equal> <!-- END radio box -->
            <nested:checkbox styleClass="<%=\"group\" + indexGroup%>" name="appMetadata" property="<%=\"groups[\" + indexGroup + \"].argFlags[\" + indexFlag + \"].selected\"%>" /> 
            <nested:write filter="false" name="flag" property="textDesc"/>
          </td>
        </tr>
      </nested:iterate>
    </logic:notEmpty> <!-- END flag -->
    
    <% exclusiveGroup = false; %>
  </nested:iterate>
</logic:notEmpty> <!-- END iteration over groups -->

<!-- submit and reset button  -->
<tr> 
  <td align="right"> <html:submit value="Submit"/> </td>
  <td align="left"> <html:reset value="Reset"/> </td> 
</tr>

</table>
</html:form>

<span style="text-decoration: underline; " onclick="showHide('help')">Show/Hide help</span>

<div id="help" style="display: none;">
    <p class="manual" >Application Description: <bean:write name="appMetadata" property="usage"/></p>
    <logic:notEmpty name="appMetadata" property="info">
    <p class="manual" >Usage Info:</br>
        <logic:iterate id="infoString" name="appMetadata" property="info"> 
            <pre><c:out value="<%= infoString %>"/> </pre> 
        </logic:iterate>
    </p>
    </logic:notEmpty>
</div>
<br>

<jsp:include page="footer.jsp"/>

