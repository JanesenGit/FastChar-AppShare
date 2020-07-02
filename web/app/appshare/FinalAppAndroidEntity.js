/**
 * FinalAppAndroidEntity实体类【安卓版本管理】
 */
function FinalAppAndroidEntity() {
    this.getList = function (where) {
        let me = this;
        let dataStore = getEntityDataStore(me, where);
        let grid = Ext.create('Ext.grid.Panel', {
            entityList: true,
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
                autoDetails: true,
                hoverTip: false,
                excelOut: true,
                excelIn: true
            },
            columns: [
                {
                    text: "编号",
                    dataIndex: "versionId",
                    align: "center",
                    width: 220,
                    renderer: renders.normal(),
                    editable: false
                },
                {
                    text: "所属应用",
                    dataIndex: "a__appName",
                    align: "center",
                    width: 220,
                    rendererFunction: "renders.link('appId','FinalAppEntity', 'appId')",
                    field: {
                        xtype: 'linkfield',
                        name: 'appId',
                        entityCode: 'FinalAppEntity',
                        entityId: 'appId',
                        entityText: 'appName'
                    }
                },
                {
                    text: "包名",
                    dataIndex: "packageName",
                    align: "center",
                    width: 220,
                    renderer: renders.normal(),
                    field: "textfield"
                },
                {
                    text: "版本号",
                    dataIndex: "versionName",
                    align: "center",
                    width: 220,
                    renderer: renders.normal(),
                    field: "textfield"
                },
                {
                    text: "构建号",
                    dataIndex: "versionBuild",
                    align: "center",
                    width: 220,
                    operation: true,
                    renderer: renders.normal(),
                    field: {
                        xtype: "numberfield"
                    }
                },
                {
                    text: "版本检查",
                    dataIndex: "packageName",
                    align: "center",
                    width: 220,
                    renderer: function (val) {
                        return "&nbsp;<a href='appshare/version?pkg=" + val + "' target='_blank'>点击查看</a>&nbsp;";
                    }
                },
                {
                    text: "安装包",
                    dataIndex: "appFile",
                    align: "center",
                    width: 220,
                    renderer: renders.file(),
                    field: {
                        xtype: 'fastfile',
                        fileModules: [files.file()]
                    }
                },
                {
                    text: "应用商店地址",
                    dataIndex: "appPubUrl",
                    align: "center",
                    width: 220,
                    field: "textfield",
                    renderer: renders.href()
                },
                {
                    text: "安装包大小",
                    dataIndex: "fileSize",
                    align: "center",
                    width: 220,
                    operation: true,
                    renderer: renders.fileSize(),
                    field: {
                        xtype: "numberfield"
                    }
                },
                {
                    text: "版本介绍",
                    dataIndex: "versionDesc",
                    align: "center",
                    width: 220,
                    field: "contentfield",
                    renderer: renders.normal()
                },
                {
                    text: "重要程度",
                    dataIndex: "versionImportant",
                    align: "center",
                    width: 220,
                    rendererFunction: "renders.enum('VersionImportantEnum')",
                    field: {
                        xtype: 'enumcombo',
                        enumName: 'VersionImportantEnum'
                    }
                },
                {
                    text: "版本状态",
                    dataIndex: "versionState",
                    align: "center",
                    width: 220,
                    rendererFunction: "renders.enum('VersionStateEnum')",
                    field: {
                        xtype: 'enumcombo',
                        enumName: 'VersionStateEnum'
                    }
                },
                {
                    text: "下载次数",
                    dataIndex: "countDownload",
                    align: "center",
                    width: 220,
                    operation: true,
                    renderer: renders.normal(),
                    field: {
                        minValue: 0,
                        xtype: "numberfield"
                    }
                },
                {
                    text: "系统要求",
                    dataIndex: "appOSType",
                    align: "center",
                    width: 220,
                    renderer: function (val) {
                        if (Ext.isEmpty(val)) {
                            if (Ext.isEmpty(val)) {
                                return "<span style='color: #ccc;'>未知</span>";
                            }
                        }
                        return "android sdk" + val + "或更高版本";
                    },
                    field: "textfield"
                },
                {
                    text: "应用MD5签名",
                    dataIndex: "appSign",
                    align: "center",
                    width: 220,
                    renderer: renders.normal(),
                    field: "textfield"
                },
                {
                    text: "录入时间",
                    dataIndex: "versionDateTime",
                    align: "center",
                    flex: 1,
                    minWidth: 220,
                    renderer: renders.normal(),
                    field: {
                        xtype: 'datefield',
                        format: 'Y-m-d H:i:s'
                    }
                }],
            tbar: {
                xtype: 'toolbar',
                overflowHandler: 'menu',
                items: [{
                    xtype: 'button',
                    text: '删除安卓版本',
                    iconCls: 'extIcon extDelete',
                    tipText: '删除安卓版本！',
                    checkSelect: 2,
                    handler: function () {
                        deleteGridData(grid);
                    }
                },
                    {
                        xtype: 'button',
                        text: '添加安卓版本',
                        iconCls: 'extIcon extAdd',
                        handler: function () {
                            new FinalAppEntity().uploadAppFile(this,
                                function () {
                                    dataStore.loadPage(1);
                                },
                                "android");
                        }
                    },
                    {
                        xtype: 'button',
                        text: '提交修改',
                        subtext: '安卓版本',
                        checkUpdate: true,
                        iconCls: 'extIcon extSave',
                        handler: function () {
                            updateGridData(grid);
                        }
                    }]
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
                    name: "data.appId",
                    xtype: "linkfield",
                    fieldLabel: "所属应用",
                    columnWidth: 1,
                    multiSelect: true,
                    entityCode: "FinalAppEntity",
                    entityId: "appId",
                    entityText: "appName",
                    linkValue: {
                        appId: where['t.appId'],
                        appName: where['^appName']
                    }
                },
                    {
                        name: "data.packageName",
                        xtype: "textfield",
                        fieldLabel: "包名",
                        columnWidth: 1,
                        allowBlank: false
                    },
                    {
                        name: "data.versionName",
                        xtype: "textfield",
                        fieldLabel: "版本号",
                        columnWidth: 1,
                        allowBlank: false
                    },
                    {
                        name: "data.versionBuild",
                        xtype: "numberfield",
                        fieldLabel: "构建号",
                        columnWidth: 1,
                        allowBlank: false
                    },
                    {
                        name: "data.appFile",
                        xtype: "fastfile",
                        fileModules: [files.file()],
                        fieldLabel: "安装包",
                        columnWidth: 1
                    },
                    {
                        name: "data.fileSize",
                        xtype: "numberfield",
                        fieldLabel: "安装包大小",
                        columnWidth: 1,
                        allowBlank: false
                    },
                    {
                        name: "data.versionDesc",
                        xtype: "contentfield",
                        allowBlank: false,
                        fieldLabel: "版本介绍",
                        columnWidth: 1
                    },
                    {
                        name: "data.versionImportant",
                        xtype: "enumcombo",
                        fieldLabel: "重要程度",
                        columnWidth: 1,
                        value: 0,
                        allowBlank: false,
                        enumName: "VersionImportantEnum"
                    },
                    {
                        name: "data.appOSType",
                        xtype: "textfield",
                        fieldLabel: "系统要求",
                        columnWidth: 1,
                        allowBlank: false
                    },
                    {
                        name: "data.appSign",
                        xtype: "textfield",
                        fieldLabel: "应用MD5签名",
                        columnWidth: 1,
                        allowBlank: false
                    },
                    {
                        name: "data.appPermissions",
                        xtype: "contentfield",
                        allowBlank: false,
                        fieldLabel: "权限要求",
                        columnWidth: 1
                    }]
            });

            let addWin = Ext.create('Ext.window.Window', {
                title: '添加安卓版本',
                height: 400,
                icon: obj.icon,
                iconCls: obj.iconCls,
                width: 520,
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
                    text: "所属应用",
                    dataIndex: "a__appName",
                    align: "center",
                    width: 220,
                    rendererFunction: "renders.link('appId','FinalAppEntity', 'appId')",
                    field: {
                        xtype: 'linkfield',
                        name: 'appId',
                        entityCode: 'FinalAppEntity',
                        entityId: 'appId',
                        entityText: 'appName'
                    },
                    editable: false
                },
                    {
                        text: "包名",
                        dataIndex: "packageName",
                        align: "center",
                        width: 220,
                        renderer: renders.normal(),
                        field: "textfield",
                        editable: false
                    },
                    {
                        text: "版本号",
                        dataIndex: "versionName",
                        align: "center",
                        width: 220,
                        renderer: renders.normal(),
                        field: "textfield",
                        editable: false
                    },
                    {
                        text: "安装包",
                        dataIndex: "appFile",
                        align: "center",
                        width: 220,
                        renderer: renders.file(),
                        editable: false
                    },
                    {
                        text: "版本状态",
                        dataIndex: "versionState",
                        align: "center",
                        width: 220,
                        rendererFunction: "renders.enum('VersionStateEnum')",
                        field: {
                            xtype: 'enumcombo',
                            enumName: 'VersionStateEnum'
                        },
                        editable: false
                    },
                    {
                        text: "录入时间",
                        dataIndex: "versionDateTime",
                        align: "center",
                        flex: 1,
                        minWidth: 220,
                        renderer: renders.normal(),
                        field: {
                            xtype: 'datefield',
                            format: 'Y-m-d H:i:s'
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
                    text: '添加安卓版本',
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
            showDetailsWindow(obj, "安卓版本详情", me, record);
        });
    };
}