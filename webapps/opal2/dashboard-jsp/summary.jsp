<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">


<head>
	<title>Opal Server Deployment Information</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
 	<link href="css/style.css" media="all" rel="stylesheet" type="text/css" /> 
    <link href="css/style-maintag.css" media="all" rel="stylesheet" type="text/css" />
    <script src="scripts/scripts.js" language="javascript" type="text/javascript" ></script> 
</head>

<% 
   String systemServerHostname = request.getServerName(); 
   String systemIPAddress = (String) request.getAttribute("systemIPAddress"); 
   String systemUptime = (String) request.getAttribute("systemUptime"); 
   String systemBuildDate = (String) request.getAttribute("systemBuildDate"); 
   String opalVersion = (String) request.getAttribute("opalVersion");  
/* not userd anymore!
   String dbUrl = (String) request.getAttribute("dbURL");
   String dbUsername = (String) request.getAttribute("dbUsername"); */
   String dbDriver = (String) request.getAttribute("dbDriver");
   String opalWebsite = (String) request.getAttribute("opalWebsite");
   String opalDocumentation = (String) request.getAttribute("opalDocumentation");

   String submissionSystem = (String) request.getAttribute("submissionSystem");
   
   String opalDataLifetime = (String) request.getAttribute("opalDataLifetime");
   
%>

<body> 
<div class="mainBody">

<!-- [headerInclude] -->
<%@ include file="../include-jsp/header.jsp" %>

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
<td colspan="2" class="colColor">

<table width="500">
	<tr>
	 	<td colspan="2" class="leftCol boxBody colColor">
	    	<h2>System Basics</h2>
                </td>
 	</tr>
	<tr>
		<td class="boxBody colColor">
                Hostname
		</td>
		<td class="boxBody colColor">
		<%= systemServerHostname %>
		</td>
	</tr>
	<tr>
		<td class="boxBody colColor">
                IP Address
		</td>
		<td class="boxBody colColor">
		<%= systemIPAddress %>
		</td>
	</tr>
	<tr>
		<td class="boxBody colColor">
                Build Date
		</td>
		<td class="boxBody colColor">
		<%= systemBuildDate %>
		</td>
	</tr>
	<tr>
		<td class="boxBody colColor">
                Uptime
		</td>
		<td class="boxBody colColor">
		<%= systemUptime %>
		</td>
	</tr>
	<tr/>
	<tr>
	 	<td colspan="2" class="leftCol boxBody colColor"><h2>Opal Server Config</h2></td>
 	</tr>
    <tr>
        <td class="boxBody colColor">OPAL Version</td>
        <td class="boxBody colColor"><%= opalVersion %></td>
    </tr>   
 <!--  unsafe to display this infomation  <tr>
        <td class="boxBody colColor">Data base URL:</td>
        <td class="boxBody colColor"></td>
    </tr>
   <tr>
        <td class="boxBody colColor">Data base username:</td>
        <td class="boxBody colColor"></td>
    </tr>   -->
    <tr>
        <td class="boxBody colColor">Data base driver:</td>
        <td class="boxBody colColor"><%= dbDriver %></td>
    </tr>
    

    <tr>
        <td class="boxBody colColor">Submission system:</td>
        <td class="boxBody colColor"><%= submissionSystem %></td>
    </tr>

<% if ( (opalDataLifetime != null) && (opalDataLifetime.length() > 1) ) { %>
    <tr>
        <td class="boxBody colColor">User data lifetime:</td>
        <td class="boxBody colColor"><%= opalDataLifetime %></td>
    </tr>
    <% } %>


</table>
</td>

<td class="colColor rightColVertBar">

<table>
<tr><td valign="top">
<div>
<p><b>Opal:</b> an automatic Web service wrappers for scientific applications on Grid resources.</p>
<p style="font-size: smaller"><a href="http://nbcr.net/software/opal/LICENSE.txt">BSD License</a></p>
<br/><br/><br/><br/><br/>
</div>
</td></tr>
<tr><td valign="bottom">
<div>
<a href="http://nbcr.net" ><img src="images/nbcr_logo_trans.gif" alt="NBCR logo"/></a><br/>
&#169; UC Regents 2005-2008
</div>
</td></tr>
</table>


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
