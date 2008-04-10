<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="java.net.InetAddress" %>
<%@page import="java.net.URLEncoder" %>
<%@page import="edu.sdsc.nbcr.opal.dashboard.util.DateHelper" %>
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
	<title>Opal Dashboard</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

 	<link href="css/style.css" media="all" rel="stylesheet" type="text/css" /> 
	<script language="javascript" type="text/javascript" >

function uncheckAll(){

    var selectedElem = document.getElementsByName("servicesName");
    for (i in selectedElem){
        selectedElem[i].checked=false;
    }//for
    
}

function checkAll(){

    var selectedElem = document.getElementsByName("servicesName");
    for (i in selectedElem){
        selectedElem[i].checked=true;
    }//for  
}	

	</script>
	<script src="js/scripts.js" language="javascript" type="text/javascript" ></script> 
</head>

<% 
   String systemServerHostname =  request.getServerName();
   String startDate = (String) request.getAttribute("startDate");
   String endDate = (String) request.getAttribute("endDate");
   //String opalDocumentation = (String) request.getAttribute("opalDocumentation");

   
   String [] servicesName = (String []) request.getAttribute("servicesName");
   String [] servicesNameSelected = (String []) request.getAttribute("servicesNameSelected");
   String serviceNameURL = "";
   //let create the URL for the selected services...
   for ( int i = 0; i < servicesNameSelected.length; i++ ) {
       serviceNameURL += "&servicesName=" + servicesNameSelected[i];
   }
   
   
%>

<body > 
<div class="mainBody">

<!-- [headerInclude] -->
<%@ include file="header.jsp" %>

<!-- [/headerInclude] -->

<!-- BEGIN Body -->
<table border="0" cellpadding="0" cellspacing="0" width="950" class="boxContainer" align="center">
<tr>
<td width="15" height="15" class="boxTopLeft colColor"></td>
<td class="leftCol boxTop colColor"></td>
<td class="boxTop colColor"></td>
<td class="rightCol boxTop colColor"></td>
<td width="15" class="boxTopRight colColor"></td>
</tr>

<tr>
<td class="boxLeft colColor"><br /></td>
<td colspan="3" class="colColor">

<h2>Usage statistics for Opal Server</h2>
<form action="dashboard" method="get">
<table width="700">
    <input type="hidden" name="command" value="statistics"/>
    <tr>
        <td class="boxBody colColor">Start Date:</td>
        <td class="boxBody colColor"><input type="text" name="startDate" value="<%= startDate %>"/></td>
    </tr>
    <tr>
        <td class="boxBody colColor">End Date:</td>
        <td class="boxBody colColor"><input type="text" name="endDate" value="<%= endDate %>"/></td>
    </tr>
    <tr>
        <td colspan="2" class="leftCol boxBody colColor" align="right">
        Select the service you want to display:</td>
    </tr>
    <tr>
        <td colspan="2" class="leftCol boxBody colColor" align="right"><input type=button name="CheckAll" value="Check All" onClick="checkAll()"/> <input type=button name="UnCheckAll" value="Uncheck All" onClick="uncheckAll()"> </td>
    </tr>
<%
   for ( int i = 0; i < servicesName.length; i++ ) {
%>
    <tr>
        <td colspan="2" class="leftCol boxBody colColor" align="right">
        
        <% if ( DateHelper.containsString(servicesNameSelected, servicesName[i]) ) {%>
        <input checked="checked"  type="checkbox" name="servicesName" value="<%= servicesName[i] %>"  />
        <%} else {%>
        <input type="checkbox" name="servicesName" value="<%= servicesName[i] %>"  />
        <%}%>
        <%= servicesName[i] %>
        </td>
    </tr>
<% 
   }
%>
    <tr>
        <td colspan="2" class="leftCol boxBody colColor" align="right"><br/><input type="submit" value="Update Charts"/></td>
    </tr>
    
</table>
</form>
</td>

<td class="boxRight colColor"><br /></td>
</tr>

<!-- hits chart -->
<tr>
<td class="boxLeft colColor"><br /></td>
<td colspan="3" class="colColor">
<h2>Number of invocations per day:</h2>
<% String href = "plotchart?type=hits&width=800&height=400&startDate=" + URLEncoder.encode(startDate, "UTF-8") + "&endDate=" + URLEncoder.encode(endDate, "UTF-8") +  serviceNameURL; %>
<a href="<%= href %>"> 
  <img src="<%= href %>"/>
</a>
<br/>
</td>
<td class="boxRight colColor"><br /></td>
</tr>

<!-- average execution time chart -->
<tr>
<td class="boxLeft colColor"><br /></td>
<td colspan="3" class="colColor">
<h2>Daily average execution time:</h2>
<% href = "plotchart?type=exectime&width=800&height=400&startDate=" + URLEncoder.encode(startDate, "UTF-8") + "&endDate=" + URLEncoder.encode(endDate, "UTF-8") +  serviceNameURL; %>
<a href="<%= href %>"> 
  <img src="<%= href %>"/>
</a>
<br/>
</td>
<td class="boxRight colColor"><br /></td>
</tr>


<!-- number of errors  -->
<tr>
<td class="boxLeft colColor"><br /></td>
<td colspan="3" class="colColor">
<h2>Number of errors per day:</h2>
<% href = "plotchart?type=error&width=800&height=400&startDate=" + URLEncoder.encode(startDate, "UTF-8") + "&endDate=" + URLEncoder.encode(endDate, "UTF-8") +  serviceNameURL; %>
<a href="<%= href %>"> 
  <img src="<%= href %>"/>
</a>
<br/>
</td>
<td class="boxRight colColor"><br /></td>
</tr>


<!-- running jobs  -->
<tr>
<td class="boxLeft colColor"><br /></td>
<td colspan="3" class="colColor">
<h2>Number of jobs currently in execution:</h2>
<% href = "plotchart?type=runningjobs&width=800&height=400" + serviceNameURL; %>
<a href="<%= href %>"> 
  <img src="<%= href %>"/>
</a>
<br/>
</td>
<td class="boxRight colColor"><br /></td>
</tr>

<tr>
<td width="15" height="15" class="boxBottomLeft colColor"></td>
<td class="leftCol boxBottom colColor"><br /></td>
<td class="boxBottom colColor"><br /></td><td class="rightCol boxBottom colColor"><br /></td>
<td width="15" class="boxBottomRight colColor"></td>
</tr>
</table>
<!-- END Body -->

<br />

<!-- BEGIN Footer -->

<!-- END Footer -->
</div>
</body>
</html>
