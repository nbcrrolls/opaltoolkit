


<!-- BEGIN Logo -->
<table border="0" class="header" cellpadding="0" cellspacing="0"  align="center">
  <tr>
    <td>
	<div id="headerLeft" class="alignLeft">
	Opal Dashboard
	</div>
	<div id="headerRight" class="alignRight">
		Opal server host: <%= systemServerHostname %>
    	</div>
    </td>
  </tr>
</table>
<!-- END Logo -->

<!-- BEGIN Top Menu -->
<div align="center">
<table border="0" class="mainnav" cellpadding="0" cellspacing="0">
	<tr>
	<td width="190" align="left">
	<a href="dashboard"><img src="images/MainNav/summary_off.jpg" alt="home" name="img1" width="190" height="13" border="0" id="img1" 
	onmouseover="FP_swapImg(1,1,/*id*/'img1',/*url*/'images/MainNav/summary_on.jpg'); HideMenusNow();" 
	onmouseout="FP_swapImgRestore(); ReturnSelected(currentMenu);" /></a></td>
	<td width="190">
	<a href="dashboard?command=statistics"><img src="images/MainNav/userinfo_off.jpg" alt="about" name="img2" width="190" height="13" border="0" id="img2" 
	onmouseover="FP_swapImg(1,1,/*id*/'img2',/*url*/'images/MainNav/userinfo_on.jpg'); ShowMenu('about');" 
	onmouseout="FP_swapImgRestore(); ReturnSelected(currentMenu);" /></a></td>
	<td width="190">
	<a href="dashboard?command=sysinfo"><img src="images/MainNav/sysinfo_off.jpg" alt="metagenomics" name="img3" width="190" height="13" border="0" id="img3" 
	onmouseover="FP_swapImg(1,1,/*id*/'img3',/*url*/'images/MainNav/sysinfo_on.jpg'); ShowMenu('metagenomics');" 
	onmouseout="FP_swapImgRestore(); ReturnSelected(currentMenu);" /></a></td>
	<td width="190">
	<a href="dashboard?command=doc"><img src="images/MainNav/docs_off.jpg" alt="research" name="img4" width="190" height="13" border="0" id="img4" 
	onmouseover="FP_swapImg(1,1,/*id*/'img4',/*url*/'images/MainNav/docs_on.jpg');" 
	onmouseout="FP_swapImgRestore();" /></a></td>
	<td width="190">
	<a href="dashboard?command=contactus"><img src="images/MainNav/contactus_off.jpg" alt="forums" name="img7" width="190" height="13" border="0" id="img7" 
	onmouseover="FP_swapImg(1,1,/*id*/'img7',/*url*/'images/MainNav/contactus_on.jpg');" 
	onmouseout="FP_swapImgRestore();" /></a></td>
</tr>
</table>
</div>
<!-- END Top Menu -->

<!-- BEGIN Sub Menu Items-->        
<div class="subnav-holder">
</div>
<!-- END Sub Menu Items-->

