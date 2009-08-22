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
    <link href="css/style-maintag.css" media="all" rel="stylesheet" type="text/css" />
	<script src="scripts/scripts.js" language="javascript" type="text/javascript" ></script> 

</head>

<%
   String systemServerHostname =  request.getServerName();
   String opalWebsite = (String) request.getAttribute("opalWebsite");
   //String opalDocumentation = (String) request.getAttribute("opalDocumentation");

%>

<body > 
<div class="mainBody">


<!-- [headerInclude] -->
<%@ include file="../include-jsp/header.jsp" %>

<!-- [/headerInclude] -->

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



<!-- add content here... -->

<p align="right"><img src="images/nbcr_logo_trans.gif" /></p>
<div id="title" >

<h1>Opal Toolkit</h1>
</div>

<div id="page">

<center>
<p>Opal Contributors: Sriram Krishnan, Luca Clementi, Wes Goodman, 
Jane Ren, Karan Bhatia, Wilfred Li, and Peter Arzberger</p>

<p>Website: <a href="<%= opalWebsite %>">Opal Web Site</a>, 
<a href="http://sourceforge.net/projects/opaltoolkit/">Opal at SourceForge</a></p>

<p>Documentation: <a href="<%= opalWebsite %>/documentation.html">Opal Documentation</a></p> 

<p>Support: <a href="mailto:support@nbcr.net">NBCR Support mailing list</a>, 
<a href="http://sourceforge.net/mail/?group_id=211778">Opal mailing list</a>, 
<a href="https://www.nbcr.net/forum/">NBCR Forum</a></p>

<p>Opal use cases:
<a href="http://meme.nbcr.net/">MEME Website</a>,
<a href="http://nbcr.net/pdb2pqr">NBCR PDB2PQR Website</a>,
<a href="http://camera.calit2.net/">CAMERA</a></p>

<p>Pubblications:<br/> 
 <b>Providing Dynamic Virtualized Access to Grid Resources via the Web 2.0 Paradigm</b>
 Luca Clementi, Zhaohui Ding, Sriram Krishnan, Xiaohui Wei, Peter W. Arzberger, and Wilfred Li. GCE07, Grid Computing Environments Workshop (Supercomputing 2007), November 2007 
 <a class="externalLink" href="http://www.collab-ogce.org/gce07/index.php/Main_Page">Conference Website</a>
 <br/>
 <b>Opal: Simple Web Services Wrappers for Scientific Applications.</b>
 Sriram Krishnan, Brent Stearn, Karan Bhatia, Kim K. Baldridge, Wilfred Li, and Peter Arzberger. In proceedings of ICWS 2006, IEEE International Conference on Web Services, September 2006 
 <a href="http://nbcr.net/software/opal/publications/SDSC-TR-2006-5-opal.pdf">pdf</a>
 <br/>
 <b>An End-to-end Web Services-based Infrastructure for Biomedical Applications.</b>
 Sriram Krishnan, Kim Baldridge, Jerry Greenberg, Brent Stearn, and Karan Bhatia. In proceedings of Grid 2005, 6th IEEE/ACM International Workshop on Grid Computing, November 2005 
 <a href="http://nbcr.net/software/opal/publications/grid2005.pdf">pdf</a>
</p>

</center>
</div>

<hr/>
<center>
<a href="http://www.nsf.gov/" ><img src="images/nsf-logo.jpg" align="middle"></img></a>
<a href="http://www.moore.org/" ><img align="middle" src="images/moore-logo.gif"></img></a>
<a href="http://www.nih.gov/" ><img align="middle" height="50" src="images/nih-logo.jpg" ></img></a>
<a href="http://www.ncrr.nih.gov/"> <img align="middle" src="images/ncrr_logo.jpg"></img></a>
<a href="http://camera.calit2.net/"> <img align="middle" height="50" src="images/Camera-Logo.jpg"></img></a>
<a href="http://www.calit2.net"> <img align="middle" src="images/calit_logo.jpg"></img></a>
<a href="http://www.sdsc.edu"> <img align="middle" src="images/SDSC-logo-red.gif"></img></a>

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
