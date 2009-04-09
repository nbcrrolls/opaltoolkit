/*
 * Ext JS Library 2.2
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

FeedGrid = function(config ) {
//    this.viewer = viewer;
    Ext.apply(this, config);

    this.store = new Ext.data.Store({
        url: 'http://137.110.115.205:8080/opal2/opalServices.xml' , 
        reader: new Ext.data.XmlReader(
            {record: 'entry'},
            ['title', 'author', {name:'pubDate', type:'date'}, 'link', 'summary', 'content']
        )
    });
//    this.store.setDefaultSort('pubDate', "DESC"); this is just for ordering 

    this.columns = [{
        id: 'title',
        header: "Title",
        dataIndex: 'title',
        sortable:true,
        width: 420,
        renderer: this.formatTitle
      },{
        header: "Author",
        dataIndex: 'author',
        width: 100,
        hidden: true,
        sortable:true
      }];

};

Ext.extend(FeedGrid, Ext.grid.GridPanel, {



//    loadFeed : function(url) {
//        this.store.baseParams = {
//            feed: url
//        };
///        this.store.load();
//    },

//    togglePreview : function(show){
//        this.view.showPreview = show;
//        this.view.refresh();
//    },

    formatTitle: function(value, p, record) {
        return String.format(
                '<div class="topic"><b>{0}</b><span class="author">{1}</span></div>',
                value, record.data.author, record.id, record.data.forumid
                );
    }
});
