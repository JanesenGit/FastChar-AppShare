/**
 * FinalAppIosHistoryEntity实体类【苹果版本下载记录】
 */
function FinalAppIosHistoryEntity() {
    this.getList = function (where) {
        let me = this;
        let dataStore = getEntityDataStore(me, where);
        let grid = Ext.create('Ext.grid.Panel', {
            entityList: true,
            tabPanelList: false,
            selModel: getGridSelModel(),
            region: 'center',
            multiColumnSort: true,
            border: 0,
            columnLines: true,
            contextMenu: true,
            power: true,
            columnContextMenu: true,
            columnSearch: true,
            store: dataStore,
            enableLocking: true,
            reserveScrollbar: false,
            operate: {
                alertDelete: true,
                alertUpdate: true,
                autoUpdate: false,
                autoDetails: false,
                hoverTip: false,
                excelOut: true,
                excelIn: true
            },
            columns: [
                {
                    text: "编号",
                    dataIndex: "historyId",
                    align: "center",
                    width: 220,
                    renderer: renders.normal(),
                    editable: false
                },
                {
                    text: "客户端IP",
                    dataIndex: "clientIp",
                    align: "center",
                    width: 220,
                    field: "textfield",
                    renderer: renders.normal()
                },
                {
                    text: "客户端信息",
                    dataIndex: "clientInfo",
                    align: "center",
                    width: 220,
                    field: "contentfield",
                    renderer: renders.normal()
                },
                {
                    text: "录入时间",
                    dataIndex: "historyDateTime",
                    align: "center",
                    flex: 1,
                    minWidth: 220,
                    rendererFunction: "renders.dateFormat('Y-m-d H:i:s')",
                    field: {
                        xtype: "datefield",
                        format: "Y-m-d H:i:s"
                    }
                }],
            tbar: {
                xtype: 'toolbar',
                overflowHandler: 'menu',
                items: [
                    {
                        xtype: 'button',
                        text: '删除苹果版本下载记录',
                        iconCls: 'extIcon extDelete',
                        tipText: '删除苹果版本下载记录！',
                        checkSelect: 2,
                        handler: function () {
                            deleteGridData(grid);
                        }
                    },
                    {
                        xtype: 'button',
                        text: '提交修改',
                        subtext: '苹果版本下载记录',
                        checkUpdate: true,
                        iconCls: 'extIcon extSave',
                        handler: function () {
                            updateGridData(grid);
                        }
                    }]
            },
            onBeforeLoad: function (obj, store, params) {
                //此处可追加额外参数
                return true;
            },
            bbar: getPageToolBar(dataStore),
            plugins: [Ext.create('Ext.grid.plugin.CellEditing', {
                clicksToEdit: 2
            })],
            viewConfig: {
                loadingText: '正在为您在加载数据…'
            }
        });
        let panel = Ext.create('Ext.panel.Panel', {
            layout: 'border',
            region: 'center',
            border: 0,
            items: [grid, getDetailsPanel(grid)]
        });
        return panel;
    };

    this.showAdd = function (obj, where) {
        let me = this;
        if (!where) {
            where = {};
        }
        return new Ext.Promise(function (resolve, reject) {
            let formPanel = Ext.create('Ext.form.FormPanel', {
                url: 'entity/save',
                cacheKey: me.entityCode,
                bodyPadding: 5,
                method: 'POST',
                region: 'center',
                fileUpload: true,
                autoScroll: true,
                defaults: {
                    labelWidth: 60,
                    margin: '5 5 5 5',
                    labelAlign: 'right',
                    emptyText: '请填写'
                },
                layout: "column",
                listeners: {
                    render: function (obj, eOpts) {
                        new Ext.util.KeyMap({
                            target: obj.getEl(),
                            key: 13,
                            fn: function (keyCode, e) {
                                formPanel.submitForm(me).then(function (result) {
                                    if (result.success) {
                                        resolve(result);
                                        formPanel.deleteCache();
                                        addWin.close();
                                    }
                                });
                            },
                            scope: this
                        });
                    }
                },
                items: [{
                    name: "data.versionId",
                    xtype: "numberfield",
                    fieldLabel: "所属版本",
                    columnWidth: 1,
                    allowBlank: false
                },
                    {
                        name: "data.clientInfo",
                        xtype: "contentfield",
                        allowBlank: false,
                        fieldLabel: "客户端信息",
                        columnWidth: 1
                    },
                    {
                        name: "data.historyDateTime",
                        xtype: "datefield",
                        format: "Y-m-d H:i:s",
                        fieldLabel: "录入时间",
                        columnWidth: 1
                    }]
            });

            let addWin = Ext.create('Ext.window.Window', {
                title: '添加苹果版本下载记录',
                height: 426,
                icon: obj.icon,
                iconCls: obj.iconCls,
                width: 550,
                layout: 'border',
                resizable: true,
                maximizable: true,
                constrain: true,
                animateTarget: obj,
                items: [formPanel],
                modal: true,
                listeners: {
                    show: function (obj) {
                        formPanel.restoreCache();
                        obj.focus();
                    }
                },
                buttons: [{
                    text: '暂存',
                    iconCls: 'extIcon extSave whiteColor',
                    handler: function () {
                        formPanel.saveCache();
                    }
                },
                    {
                        text: '重置',
                        iconCls: 'extIcon extReset',
                        handler: function () {
                            formPanel.form.reset();
                            formPanel.deleteCache();
                        }
                    },
                    {
                        text: '添加',
                        iconCls: 'extIcon extOk',
                        handler: function () {
                            formPanel.submitForm(me).then(function (result) {
                                if (result.success) {
                                    resolve(result);
                                    formPanel.deleteCache();
                                    addWin.close();
                                }
                            });
                        }
                    }]
            });
            addWin.show();
        });
    };

    this.showWinList = function (obj, title, where, modal) {
        let me = this;
        me.menu = {
            id: $.md5(title),
            text: title
        };
        let gridList = me.getList(where);
        let entityOwner = gridList.down("[entityList=true]");
        if (entityOwner) {
            entityOwner.code = $.md5(title);
        }
        if (!modal) {
            modal = false;
        }
        let win = Ext.create('Ext.window.Window', {
            title: title,
            height: 550,
            width: 700,
            layout: 'border',
            resizable: true,
            constrain: true,
            maximizable: true,
            animateTarget: obj,
            modal: modal,
            listeners: {
                show: function (obj) {
                    obj.focus();
                }
            },
            items: [gridList]
        });
        if (obj != null) {
            win.setIcon(obj.icon);
            win.setIconCls(obj.iconCls);
        } else {
            win.setIconCls("extIcon extSee");
        }
        win.show();
    };

    this.showTabList = function (obj, title, where) {
        let me = this;
        me.menu = {
            id: $.md5(title),
            text: title
        };
        let gridList = me.getList(where);
        let entityOwner = gridList.down("[entityList=true]");
        if (entityOwner) {
            entityOwner.code = $.md5(title);
        }
        let icon = "icons/icon_function.svg";
        if (obj != null) {
            icon = obj.icon;
        }
        system.addTab(gridList, $.md5(title), title, icon);
    };

    this.showSelect = function (obj, title, where, multi) {
        let me = this;
        return new Ext.Promise(function (resolve, reject) {
            me.menu = {
                id: $.md5(title),
                text: title
            };
            let dataStore = getEntityDataStore(me, where);
            let selModel = null;
            if (multi) {
                selModel = getGridSelModel();
            }
            let grid = Ext.create('Ext.grid.Panel', {
                entityList: true,
                code: $.md5(title),
                selModel: selModel,
                region: 'center',
                multiColumnSort: true,
                border: 0,
                power: true,
                columnLines: true,
                contextMenu: false,
                columnMenu: false,
                store: dataStore,
                enableLocking: true,
                columns: [{
                    text: "录入时间",
                    dataIndex: "historyDateTime",
                    align: "center",
                    flex: 1,
                    minWidth: 220,
                    rendererFunction: "renders.dateFormat('Y-m-d H:i:s')",
                    field: {
                        xtype: "datefield",
                        format: "Y-m-d H:i:s"
                    },
                    editable: false
                }],
                bbar: getPageToolBar(dataStore),
                viewConfig: {
                    loadingText: '正在为您在加载数据…'
                }
            });

            let win = Ext.create('Ext.window.Window', {
                title: title,
                height: 550,
                width: 700,
                iconCls: 'extIcon extSelect',
                layout: 'border',
                resizable: true,
                constrain: true,
                maximizable: true,
                animateTarget: obj,
                items: [grid],
                modal: true,
                listeners: {
                    close: function (winObj, eOpts) {
                        if (!resolve.called) {
                            resolve.called = true;
                            resolve();
                        }
                    },
                    show: function (obj) {
                        obj.focus();
                    }
                },
                buttons: [{
                    text: '添加苹果版本下载记录',
                    iconCls: 'extIcon extAdd whiteColor',
                    handler: function () {
                        me.showAdd(this, where).then(function (result) {
                            if (result.success) {
                                dataStore.loadPage(1);
                            }
                        });
                    }
                },
                    '->', {
                        text: '取消',
                        iconCls: 'extIcon extClose',
                        handler: function () {
                            win.close();
                        }
                    },
                    {
                        text: '确定',
                        iconCls: 'extIcon extOk',
                        handler: function () {
                            let data = grid.getSelectionModel().getSelection();
                            if (data.length > 0) {
                                if (!resolve.called) {
                                    resolve.called = true;
                                    resolve(data);
                                }
                            }
                            win.close();
                        }
                    }]
            });
            win.show();
        });
    };

    this.showDetails = function (obj, where) {
        let me = this;
        let dataStore = getEntityDataStore(me, where);
        showWait("请稍后……");
        dataStore.load(function (records, operation, success) {
            hideWait();
            if (records.length == 0) {
                Ext.Msg.alert("系统提醒", "未获得到详情数据！");
                return;
            }
            let record = records[0];
            showDetailsWindow(obj, "苹果版本下载记录详情", me, record);
        });
    };
}