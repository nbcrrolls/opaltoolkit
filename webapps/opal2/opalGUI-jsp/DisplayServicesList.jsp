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
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-nested" prefix="nested" %>
<%@page import="edu.sdsc.nbcr.opal.gui.common.OPALService"%>


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

    //format the title
    function formatTitle(value, p, record) {
        hostName = "localhost:8080";
        submissionFormLink = "http://" + hostName + "/opal2/CreateSubmissionForm.do?serviceURL=http%3A%2F%2Flocalhost%3A8080%2Fopal2%2Fservices%2F"
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
            { id: 'title', header: "Service Name (Click for submission form)", dataIndex: 'title', sortable:true, renderer: formatTitle, width: 894 },
//            { header: "Web Service URL" ,dataIndex: 'link', sortable: true, renderer: formatURL, width: 294 }
        ],
        stripeRows: false,
//        autoExpandColumn: 'title',
        foreceFit: true

    });

    //the search box
    var trigger = new Ext.form.TriggerField({
                triggerClass : 'button-search-trigger',
                dlgWidth : 200,
                dlgHeight : 100,
                });

    trigger.onTriggerClick = function(){
        var serachTerms = trigger.getValue();
        store.filter("summary",  serachTerms, true, false);
    };


    var panel = new Ext.Panel({
        applyTo: 'feed-viewer',
        title:'Opal Services',
        height:500,
//        autoHeight: true,
        width:900,
        layout:'fit',
//        autoScroll:true,
        items: grid,
        tbar: [
            'Search: ',
            ' ',
            trigger
        ]
//http://extjs.com/forum/showthread.php?t=3613
    });

    panel.render('feed-viewer');

});

</script>




</head>
<body>
<div id="header"><div style="float:right;margin:5px;" class="x-small-editor"></div></div>

<div class="mainBody">

<jsp:include page="../include-jsp/header.jsp"></jsp:include>
<jsp:include page="header.jsp"></jsp:include>


<h2>List of Applications:</h2>


<div id="feed-viewer"></div>

<p style="font-size: smaller">*: a customized submission form is avaiable for this application</p>
<br/>
For an Atom feed of the available services <a href="opalServices.xml"><img src="images/feed-icon.png"/> click here</a>.

<jsp:include page="footer.jsp"></jsp:include>

