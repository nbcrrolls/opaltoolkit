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
	
 	<script src="scripts/scripts.js" language="javascript" type="text/javascript" ></script>  
</head> 
 
<%  
   
   String error = (String) request.getAttribute("error");
       
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
</td>

<td class="boxRight colColor"><br /></td>
</tr>

<!-- hits chart --> 
<tr> 
<td class="boxLeft colColor"><br /></td> 
<td colspan="3" class="colColor"> 
<h2>Error plotting charts.</h2>
<p>There was an error while plotting the charts: <%= error %></p>

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
