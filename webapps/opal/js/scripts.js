// Veriables
var g_bLoaded = false;
var g_iMenuTimer = Number(0);

// Functions
function FP_swapImg() {//v1.0
 var doc=document,args=arguments,elm,n; doc.$imgSwaps=new Array(); for(n=2; n<args.length;
 n+=2) { elm=FP_getObjectByID(args[n]); if(elm) { doc.$imgSwaps[doc.$imgSwaps.length]=elm;
 elm.$src=elm.src; elm.src=args[n+1]; } }
}

function FP_preloadImgs() {//v1.0
 var d=document,a=arguments; if(!d.FP_imgs) d.FP_imgs=new Array();
 for(var i=0; i<a.length; i++) { d.FP_imgs[i]=new Image; d.FP_imgs[i].src=a[i]; }
}

function FP_getObjectByID(id,o) {//v1.0
 var c,el,els,f,m,n; if(!o)o=document; if(o.getElementById) el=o.getElementById(id);
 else if(o.layers) c=o.layers; else if(o.all) el=o.all[id]; if(el) return el;
 if(o.id==id || o.name==id) return o; if(o.childNodes) c=o.childNodes; if(c)
 for(n=0; n<c.length; n++) { el=FP_getObjectByID(id,c[n]); if(el) return el; }
 f=o.forms; if(f) for(n=0; n<f.length; n++) { els=f[n].elements;
 for(m=0; m<els.length; m++){ el=FP_getObjectByID(id,els[n]); if(el) return el; } }
 return null;
}

function FP_swapImgRestore() {//v1.0
 var doc=document,i; if(doc.$imgSwaps) { for(i=0;i<doc.$imgSwaps.length;i++) {
  var elm=doc.$imgSwaps[i]; if(elm) { elm.src=elm.$src; elm.$src=null; } } 
  doc.$imgSwaps=null; }
}

// Retrieve an object from the DOM
function DocumentObject( strObjectID, bWithStyle )
{
	if (bWithStyle)
	{
		if (document.getElementById) return (document.getElementById(strObjectID).style); 
		else if (document.all) return (document.all[strObjectID].style); 
		else if ((navigator.appName.indexOf('Netscape') != -1) && (parseInt(navigator.appVersion) == 4)) return (document.layers[strObjectID]); 
	}
	else
	{
		if (document.getElementById) return (document.getElementById(strObjectID)) ; 
		else if (document.all) return (document.all[strObjectID]); 
		else if ((navigator.appName.indexOf('Netscape') != -1) && (parseInt(navigator.appVersion) == 4)) return (document.layers[strObjectID]); 
	}
}

function ShowMenu( strMenu ) {

	DocumentObject( 'about-subnav', true ).display = ( strMenu == "about" ) ? 'block' : 'none';
	DocumentObject( 'metagenomics-subnav', true ).display = ( strMenu == "metagenomics" ) ? 'block' : 'none';
	DocumentObject( 'research-subnav', true ).display = ( strMenu == "research" ) ? 'block' : 'none';
	DocumentObject( 'news-subnav', true ).display = ( strMenu == "news" ) ? 'block' : 'none';
	DocumentObject( 'events-subnav', true ).display = ( strMenu == "events" ) ? 'block' : 'none';
	DocumentObject( 'forums-subnav', true ).display = ( strMenu == "forums" ) ? 'block' : 'none';
	
	// Clear the menu timer, if any
	clearTimeout( g_iMenuTimer );
	g_iMenuTimer = 0;	
}

function HideMenusLater() {
	// Clear the timeout, if any
	clearTimeout( g_iMenuTimer );
	// Set a new timer
	g_iMenuTimer = setTimeout( 'HideMenusNow();', 1500 );
}

function ReturnSelected(strMenu) {
	returnMenu=strMenu;
	// Clear the timeout, if any
	clearTimeout( g_iMenuTimer );
	// Set a new timer
	g_iMenuTimer = setTimeout( 'HideMenusNow();ShowMenu(returnMenu);', 1500 );
}

function PauseHideMenus() {
	// Clear the timeout, if any
	clearTimeout( g_iMenuTimer );
	g_iMenuTimer = 0;
}

function HideMenusNow() {
	ShowMenu('');
}

