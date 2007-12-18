<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">


<head>
	<title>Opal Toolkit Deployment Information -- karan</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<meta name="keywords" content="enter, keywords, here" />
	<meta name="description" content="enter keywords here" />
 	<link href="css/style.css" media="all" rel="stylesheet" type="text/css" /> 
	<script language="js/javascript" type="text/javascript" >
	<!--
	currentMenu='home';
	-->
	</script>
	<script src="js/scripts.js" language="javascript" type="text/javascript" ></script> 
<style>
#title h1 {
	margin: 30px 0 10px 0;
	padding: 3px; 
	border-bottom: solid #CCC 1px;
	margin-top: 0;
	font-size: 1.7em;
	border-bottom: solid #BBB 1px;
	text-align: center;
	font-weight: normal;
	color: #036;
}

#title p {
   text-align: center
}

</style>

</head>

<%
   String systemServerHostname =  request.getServerName();
   String opalWebsite = (String) request.getAttribute("opalWebsite");
   //String opalDocumentation = (String) request.getAttribute("opalDocumentation");

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
<td class="boxTop colColor"></td><td class="rightCol boxTop colColor"></td>
<td width="15" class="boxTopRight colColor"></td>
</tr>
<tr>
<td class="boxLeft colColor"><br /></td>
<td colspan="3" class="colColor">

<!-- add content here... -->

<div id="title">
<p><img src="images/nbcr_logo.gif" width="300"></p>

<h1>Opal Toolkit</h1>
</div>

<div id="page">

<center>
<p>Opal Contributors: Sriram Krishnan, Luca Clementi, Wes Goodman, Karan Bhatia, </p>
<p>Wilfred Li, and Peter Arzberger</p>
<p>Website: <a href="<%= opalWebsite %>"><%= opalWebsite %></a></p>
<p>Organizations: <a href="http://www.nbcr.net">National Biomedical Computation Resource</a>, 
<a href="http://www.sdsc.edu">San Diego Supercompter Center</a>,
<a href="http://www.calit2.net">CALIT2</a></p>
<a href="http://camera.calit2.net/">CAMERA</a></p>
<p>Funding Agencies: National Institutes of Health, National Science Foundation, Gordon and Betty Moore Foundation</p>
</center>

</div>

<hr>
<center>
<img src="images/nsf-logo.jpg" align="middle"> <img align="middle" height="35" src="images/moore-logo.gif"><img align="middle" height="50" src="images/nih-logo.jpg" >
</center>

</div>

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
