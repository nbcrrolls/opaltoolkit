<%--
    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at
   
         http://www.apache.org/licenses/LICENSE-2.0
   
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<%
String tomcatUrl = (String) request.getAttribute("tomcatUrl");

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>Available Applications</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

    <link rel="stylesheet" type="text/css" href="css/ext-all.css" />
    <link rel="stylesheet" type="text/css" href="css/feed-viewer.css" /> 

    <link href="css/style.css"  rel="stylesheet" type="text/css" />  
    
    <script src="scripts/scripts.js" language="javascript" type="text/javascript" ></script> 
    <script src="scripts/ext-base.js" language="javascript" type="text/javascript"></script>
    <script src="scripts/ext-all.js" language="javascript" type="text/javascript"></script>

<script type="text/javascript" >


Ext.onReady(function(){

    Ext.BLANK_IMAGE_URL = 'images/default/s.gif';

    //format the title
    function formatTitle(value, p, record) {
        hostName = "<%=tomcatUrl%>";
        submissionFormLink = hostName + "/opal2/CreateSubmissionForm.do?serviceURL=http%3A%2F%2Flocalhost%3A8080%2Fopal2%2Fservices%2F"
        URLarray = record.data.link.split("/");
        serviceName = URLarray[URLarray.length - 1];
        submissionFormLink = submissionFormLink + serviceName;
        return String.format(
                '<div class="topic"><a href="{2}"><b>{0}</b><span class="serviceDescription">{1}</span></a><br/>Web service URL: <a href="{3}">{3}</a> </div>',
                value, record.data.summary, submissionFormLink, record.data.link
                );
    }

    //Format Web Service URL
    function formatURL(value, p, record) {
        return String.format(
            '<a href="{0}"><b>{0}</b></a>', record.data.link,
            record.data.link);
    }

    //load the data
    store = new Ext.data.Store({
        url: '/opal2/opalServices.xml' ,
        reader: new Ext.data.XmlReader(
            {record: 'entry'},
            ['title', 'author', {name:'pubDate', type:'date'}, {name: 'link', mapping: 'link@href'}, 'summary', 'content']
        )
    });
    store.load();
    
    //display the main with the data grid
    grid = new Ext.grid.GridPanel({
        store: store,
        columns: [
            { id: 'title', header: "Service Name (Click for submission form)", dataIndex: 'title', sortable:true, renderer: formatTitle, width: 894 }
            //{ header: "Web Service URL" ,dataIndex: 'link', sortable: true, renderer: formatURL, width: 294 }
        ],
        //stripeRows: true,
        //autoExpandColumn: 'title',
        //foreceFit: true,
        autoHeight: true
    });


    //the search box with the trigger 
    /*var trigger = new Ext.form.TriggerField({
                triggerClass : 'button-search-trigger',
                dlgWidth : 200,
                dlgHeight : 100
    });

    trigger.onTriggerClick = function(){
        var serachTerms = trigger.getValue();
        store.filter("summary",  serachTerms, true, false);
    };*/



    function filterTree(e){
        var text = e.target.value;
        store.filter("summary", text, true, false);
    };

    var searchBox =  new Ext.form.TextField({
        width: 200,
        emptyText:'Type keywords here',
        listeners:{
            render: function(f){
                f.el.on('keydown', filterTree, f, {buffer: 350});
            }
        }
    });

 


    var panel = new Ext.Panel({
        //applyTo: 'feed-viewer',
        //title:'Opal Services',
        //autoHeight: true,
        autoScroll:true,
        //height:500,
        width:900,
        autoHeight: true,
        //autoWidth: true,
        layout:'fit',
        items: grid,
        tbar: [
            'Search: ',
            ' ',
            searchBox
        ]
    });

    panel.render('feed-viewer');

});

</script>




</head>
<body>

<div class="mainBody">

<jsp:include page="../include-jsp/header.jsp"></jsp:include>

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




<h2>List of Applications:</h2>

<br/>
<div id="containing-div" >
<div id="feed-viewer" style="margin-left: auto; margin-right: auto; width: 900px"></div>
</div>
<br/>

<p style="font-size: smaller">*: a customized submission form is avaiable for this application</p>
<br/>
For an Atom feed of the available services <a href="opalServices.xml"><img src="images/feed-icon.png"/> click here</a>.


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



