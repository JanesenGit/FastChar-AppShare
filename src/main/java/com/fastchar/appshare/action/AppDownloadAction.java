package com.fastchar.appshare.action;

import com.fastchar.annotation.AFastRoute;
import com.fastchar.appshare.entity.FinalAppEntity;
import com.fastchar.core.FastAction;

/**
 * 应用明示的短连接下载
 * @author 沈建（Janesen）
 * @date 2021/1/25 14:43
 */
@AFastRoute(interceptorAfter = false)
public class AppDownloadAction extends FastAction {
    @Override
    protected String getRoute() {
        return "/app";
    }

    public void index() {
        String appId = getUrlParam(0);
        FinalAppEntity appEntity = FinalAppEntity.dao().selectById(appId);
        if (appEntity != null) {
            String appCode = appEntity.getString("appCode");
            forward("appshare/download/" + appCode);
        }
        response502("无效应用！");
    }

}
