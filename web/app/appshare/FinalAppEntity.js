/**
 * FinalAppEntity实体类【APP管理】
 */
function FinalAppEntity() {
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
                    dataIndex: "appId",
                    align: "center",
                    width: 220,
                    renderer: renders.normal(),
                    editable: false
                },
                {
                    text: "应用标识",
                    dataIndex: "appCode",
                    align: "center",
                    width: 220,
                    renderer: renders.normal(),
                    field: "textfield"
                },
                {
                    text: "应用名称",
                    dataIndex: "appName",
                    align: "center",
                    width: 220,
                    renderer: renders.normal(),
                    field: "textfield"
                },
                {
                    text: "应用图标",
                    dataIndex: "appLogo",
                    align: "center",
                    width: 220,
                    renderer: renders.image(),
                    field: {
                        xtype: 'fastfile',
                        fileModules: [files.image()]
                    }
                },
                {
                    text: "应用状态",
                    dataIndex: "appState",
                    align: "center",
                    width: 220,
                    rendererFunction: "renders.enum('AppStateEnum')",
                    field: {
                        xtype: 'enumcombo',
                        enumName: 'AppStateEnum'
                    }
                },
                {
                    text: "应用下载页面",
                    dataIndex: "appCode",
                    align: "center",
                    width: 220,
                    renderer: function (val) {
                        return "&nbsp;<a href='appshare/download/" + val + "' target='_blank'>点击查看</a>&nbsp;";
                    }
                },
                {
                    text: "下载二维码",
                    dataIndex: "appCode",
                    align: "center",
                    width: 220,
                    renderer: function (val, metaData, record) {
                        let url = "qrCode?v=1&render=image&logo=" + record.get("appLogo") + "&content=" + system.http + "appshare/download/" + val;
                        let functionStr = "showImage(null,\"" + url + "\")";
                        return "<a href='javascript:" + functionStr + ";'>查看</a>"
                    }
                },
                {
                    text: "录入时间",
                    dataIndex: "appDateTime",
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
                    text: '删除APP',
                    iconCls: 'extIcon extDelete',
                    tipText: '删除APP！',
                    checkSelect: 2,
                    handler: function () {
                        deleteGridData(grid);
                    }
                },
                    {
                        xtype: 'button',
                        text: '添加APP',
                        iconCls: 'extIcon extAdd',
                        handler: function () {
                            me.uploadAppFile(this, function () {
                                dataStore.loadPage(1);
                            });
                        }
                    },
                    {
                        xtype: 'button',
                        text: '提交修改',
                        subtext: 'APP',
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
                    name: "data.appName",
                    xtype: "textfield",
                    fieldLabel: "应用名称",
                    columnWidth: 1,
                    allowBlank: false
                },
                    {
                        name: "data.appLogo",
                        xtype: "fastfile",
                        fileModules: [files.image()],
                        fieldLabel: "应用图标",
                        columnWidth: 1
                    },
                    {
                        name: "data.appState",
                        xtype: "enumcombo",
                        fieldLabel: "应用状态",
                        columnWidth: 1,
                        value: 0,
                        allowBlank: false,
                        enumName: "AppStateEnum"
                    },
                    {
                        name: "data.appDateTime",
                        xtype: "datefield",
                        format: "Y-m-d H:i:s",
                        fieldLabel: "录入时间",
                        columnWidth: 1
                    }]
            });

            let addWin = Ext.create('Ext.window.Window', {
                title: '添加APP',
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
                    text: "应用名称",
                    dataIndex: "appName",
                    align: "center",
                    width: 220,
                    renderer: renders.normal(),
                    field: "textfield",
                    editable: false
                },
                    {
                        text: "应用图标",
                        dataIndex: "appLogo",
                        align: "center",
                        width: 220,
                        renderer: renders.image(),
                        editable: false
                    },
                    {
                        text: "应用状态",
                        dataIndex: "appState",
                        align: "center",
                        width: 220,
                        rendererFunction: "renders.enum('AppStateEnum')",
                        field: {
                            xtype: 'enumcombo',
                            enumName: 'AppStateEnum'
                        },
                        editable: false
                    },
                    {
                        text: "录入时间",
                        dataIndex: "appDateTime",
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
                    text: '添加APP',
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
            showDetailsWindow(obj, "APP详情", me, record);
        });
    };

    this.uploadAppFile = function (obj, callBack, type) {
        obj.blur();
        if (!type) {
            type = "all";
        }

        let formPanel = Ext.create('Ext.form.FormPanel', {
            url: 'appshare/upload',
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
            submitData: function () {
                let form = formPanel.form;
                if (form.isValid()) {
                    form.submit({
                        waitMsg: '正在上传安装包中…',
                        submitEmptyText: false,
                        success: function (form, action) {
                            Ext.Msg.alert('系统提醒', action.result.message, function () {
                                if (action.result.success) {
                                    win.close();
                                    callBack();
                                }
                            });
                        },
                        failure: function (form, action) {
                            win.close();
                            Ext.Msg.alert('系统提醒', action.result.message);
                        }
                    });
                }
            },
            items: [
                {
                    xtype: 'filefield',
                    fieldLabel: "安装包",
                    buttonText: '选择文件',
                    allowBlank: false,
                    name: 'appFile',
                    columnWidth: 1,
                    listeners: {
                        change: function (obj, value, eOpts) {
                            if (value != null && value.length != 0) {
                                let reg = eval(/\.(apk|ipa)$/i);
                                if (type === "android") {
                                    reg = eval(/\.(apk)$/i);
                                } else if (type === "ios") {
                                    reg = eval(/\.(ipa)$/i);
                                }
                                if (!reg.test(value)) {
                                    obj.reset();
                                    if (type === "android") {
                                        Ext.Msg.alert('系统提醒', "请上传有效的安装包文件(.apk)");
                                    } else if (type === "ios") {
                                        Ext.Msg.alert('系统提醒', "请上传有效的安装包文件(.ipa)");
                                    } else {
                                        Ext.Msg.alert('系统提醒', "请上传有效的安装包文件(.apk|.ipa)");
                                    }

                                }
                            }
                        }
                    }
                },
                {
                    xtype: 'button',
                    text: '立即上传',
                    columnWidth: 1,
                    handler: function () {
                        formPanel.submitData();
                    }
                }]
        });

        let win = Ext.create('Ext.window.Window', {
            title: "上传安装包",
            height: 180,
            width: 400,
            layout: 'border',
            modal: true,
            iconCls: 'extIcon extUpload',
            resizable: false,
            constrain: true,
            maximizable: false,
            animateTarget: obj,
            items: [formPanel]
        });
        win.show();
    };
}