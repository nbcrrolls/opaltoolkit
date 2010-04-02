<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@page import="java.net.InetAddress" %>
<%@page import="java.net.URLEncoder" %>
<%@page import="edu.sdsc.nbcr.opal.dashboard.util.DateHelper" %>
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
	<title>Opal2 Server Dashboard Usage Statistics</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

 	<link rel="stylesheet" type="text/css" media="all" href="css/style.css"/> 
    <link rel="stylesheet" type="text/css" media="all" href="css/style-maintag.css" />

    <script src="scripts/jquery.js" language="javascript" type="text/javascript" ></script>
    <script src="scripts/jquery.corner.js" language="javascript" type="text/javascript" ></script>
	<script language="javascript" type="text/javascript" >

    <script type="text/javascript">
        function uncheckAll(){
            var selectedElem = document.getElementsByName("servicesName");
            for (i=0; i < selectedElem.length; i++){
            selectedElem[i].checked=false;
            }//for
        }

        function checkAll(){
            var selectedElem = document.getElementsByName("servicesName");
            for (i=0; i < selectedElem.length; i++){
                selectedElem[i].checked=true;
            }//for  
        }	
	</script>

    <script type="text/javascript">
        $(document).ready(function() {
            $("#list-nav ul li.left a").corner("tl bl 10px  cc:#fff");
            $("#list-nav ul li.right a").corner("tr br 10px cc:#fff");
        });
	</script>

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
<!-- Navigation Menu Bar -->
<table border="0" class="mainnav" cellpadding="0" cellspacing="0">
<tr>
  <td>
    <div id="list-nav">
    <ul>
      <li class="left"><a href="dashboard" >Home</a></li>
      <li><a href="dashboard?command=serverInfo" >Server Info</a></li>
      <li><a href="dashboard?command=serviceList">List of applications</a></li>
      <li><a href="dashboard?command=statistics"class="active">Usage Statistics</a></li>
      <li class="right"><a href="dashboard?command=docs">Documentation</a></li>
    </ul>
    </div>
  </td>
</tr>
</table> 
<br>

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
        <td >Start Date:</td>
        <td ><input type="text" name="startDate" value="<%= startDate %>"/></td>
    </tr>
    <tr>
        <td >End Date:</td>
        <td ><input type="text" name="endDate" value="<%= endDate %>"/></td>
    </tr>
    <tr>
        <td colspan="2" >
        Select the service you want to display:</td>
    </tr>
    <tr>
        <td colspan="2" ><input type=button name="CheckAll" value="Check All" onClick="checkAll()"/> <input type=button name="UnCheckAll" value="Uncheck All" onClick="uncheckAll()"> </td>
    </tr>
<%
   for ( int i = 0; i < servicesName.length; i++ ) {
%>
    <tr>
        <td colspan="2" >
        
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
        <td ><br/><input type="submit" value="Update Charts"/></td>
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
<%@ include file="footer.jsp" %>
</table> <!-- END Body -->
<%@ include file="copyright.jsp" %>
</div>
</body>
</html>
