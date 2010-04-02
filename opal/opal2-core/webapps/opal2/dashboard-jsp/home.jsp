<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
    <title>Opal2: A Toolkit for Scientific Software as a Service.</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="keywords" content="enter, keywords, here" />
    <meta name="description" content="enter keywords here" />
    <link rel="stylesheet" type="text/css" media="all" href="css/style.css"/> 
    <link rel="stylesheet" type="text/css" media="all"href="css/style-maintag.css"/>
    <script src="scripts/jquery.js" language="javascript" type="text/javascript" ></script> 
    <script src="scripts/jquery.corner.js" language="javascript" type="text/javascript" ></script> 
    <script type="text/javascript">
        $(document).ready(function() { 
		    $("#list-nav ul li.left a").corner("tl bl 10px  cc:#fff"); 
		    $("#list-nav ul li.right a").corner("tr br 10px cc:#fff"); 
		});
    </script>
</head>

<%
   String systemServerHostname =  request.getServerName();
   String opalWebsite = (String) request.getAttribute("opalWebsite");
   String opalDocumentation = (String) request.getAttribute("opalDocumentation");

%>

<body >

<div class="mainBody">

<!-- [headerInclude] -->
<%@ include file="header.jsp" %>
<!-- Navigation Menu Bar -->
<table border="0" class="mainnav" cellpadding="0" cellspacing="0">
<tr>
  <td>
    <div id="list-nav" >
    <ul>
      <li class="left"><a href="dashboard" class="active">Home</a></li>
      <li><a href="dashboard?command=serverInfo">Server Info</a></li>
      <li><a href="dashboard?command=serviceList">List of applications</a></li>
      <li><a href="dashboard?command=statistics">Usage Statistics</a></li>
      <li class="right"><a href="dashboard?command=docs">Documentation</a></li>
    </ul>
	</div>
  </td>
</tr>
</table>
<br>

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
    <!-- add content here... -->
    <table border="0" width="100%" cellpadding="2px" >
      <tr>
        <td colspan="3" class="boxBody colColor">
		<span class="infoTitle"> Opal2 :</span> is a toolkit for wrapping scientific applications as Web 
		services.  It leverages open standards and toolkits DRMAA, Condor and the Globus 
		for cluster scheduling, standards-based Grid security and data management in an easy to use 
		and highly configurable manner.  Opal is released under the 
		<a href="http://nbcr.net/software/opal/LICENSE.txt"><span class="PubTitle">BSD License</span></a>
		<br>
		<hr>
		</td>
      </tr>
      <tr>
        <td valign="top" width="38%" class="boxBody colColor"><span class="infoTitle">The
		Opal Dashboard </span><br>
		provides an easy interface for jobs submision and monitoring. The key features at a glance: 
		   <ul class="menu1">
		     <li><span class="Content">Easy to use and free of charge</span> </li>
		     <li><span class="Content">Sends the application job at any time</span> </li>
		     <li><span class="Content">You need a web access and aplication input </span> </li>
		     <li><span class="Content">Track the job progress and get job
			 results online</span> </li>
		   </ul>
		<br>
		</td>
		<td class="colColor rightColVertBar">
        <td valign="top" class="boxBody colColor"> 
		<span class="infoTitle">How to use </span><br>
		Click on the tabs in the navigation bar at the top of the page. They provide:
		   <ul class="menu2">
		     <li><span class="Nav">Home</span><span class="Content"> - this page</span> </li>
		     <li><span class="Nav">List of applications</span><span class="Content"> - choose 
			     an available applications to run your job</span> </li>
		     <li><span class="Nav">Usage Statistics</span><span class="Content"> - shows usage 
			     statistics for Opal server by job.</span> </li>
		     <li><span class="Nav">Server Info</span><span class="Content"> - information 
			     about Opal server host.</span> </li>
		     <li><span class="Nav">Documentation</span><span class="Content"> - Opal documentation, 
			     tutorials, support</li>
		   </ul>
		<br>
		</td>
      </tr>

    </table>
  </td>
  <td class="boxRight colColor"><br /></td>
</tr>
<%@ include file="footer.jsp" %>
</table> <!-- END Body -->
<%@ include file="copyright.jsp" %>
</div>
</body>
</html>
