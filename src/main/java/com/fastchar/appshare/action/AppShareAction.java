package com.fastchar.appshare.action;

import com.fastchar.annotation.AFastRoute;
import com.fastchar.appshare.entity.*;
import com.fastchar.core.FastAction;
import com.fastchar.utils.FastMD5Utils;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 沈建（Janesen）
 * @date 2020/5/19 15:46
 */
@AFastRoute(interceptorAfter = false)
public class AppShareAction extends FastAction {
    @Override
    protected String getRoute() {
        return "/appshare";
    }

    /**
     * 检测APP版本
     * 参数：
     * packageName 包名
     */
    public void version() {
        String packageName = getParam("packageName");
        if (FastStringUtils.isEmpty(packageName)) {
            packageName = getParam("pkg");
        }
        if (FastStringUtils.isEmpty(packageName)) {
            responseText("无效应用！");
        }
        String appCode = FastMD5Utils.MD5To16(packageName);
        boolean beta = getParamToBoolean("beta");

        FinalAppEntity appByCode = FinalAppEntity.dao().getAppByCode(appCode);
        if (appByCode != null) {
            if (getParamToBoolean("ios", Boolean.FALSE)) {
                FinalAppIosEntity lastVersion = FinalAppIosEntity.dao().getLastVersion(appByCode.getId(), beta);
                if (lastVersion != null) {
                    Map<String, Object> maps = new HashMap<>();
                    maps.put("versionName", lastVersion.get("versionName"));//版本名称，例如1.0.2为字符串类型
                    maps.put("versionCode", lastVersion.get("versionBuild"));//版本编号，例如19或者59数字整形
                    maps.put("versionDesc", lastVersion.get("versionDesc"));//升级描述
                    maps.put("downUrl", getProjectHost() + "appshare/load/ios/" + lastVersion.getId());//ios下载地址
                    maps.put("versionImportant", lastVersion.get("versionImportant"));//版本更新的重要信0不重要，1是重要
                    maps.put("versionImportantDesc", lastVersion.get("versionImportantStr"));//重要信配置
                    maps.put("success", true);
                    responseJson(maps);
                }
            } else {
                FinalAppAndroidEntity lastVersion = FinalAppAndroidEntity.dao().getLastVersion(appByCode.getId(), beta);
                if (lastVersion != null) {
                    Map<String, Object> maps = new HashMap<>();
                    maps.put("versionName", lastVersion.get("versionName"));//版本名称，例如1.0.2为字符串类型
                    maps.put("versionCode", lastVersion.get("versionBuild"));//版本编号，例如19或者59数字整形
                    maps.put("versionDesc", lastVersion.get("versionDesc"));//升级描述
                    maps.put("downUrl", getProjectHost() + "appshare/load/android/" + lastVersion.getId());//apk下载地址
                    maps.put("versionImportant", lastVersion.get("versionImportant"));//版本更新的重要信0不重要，1是重要
                    maps.put("versionImportantDesc", lastVersion.get("versionImportantStr"));//重要信配置
                    maps.put("success", true);
                    responseJson(maps);
                }
            }
        }
        responseJson(-1, "应用信息无效！");
    }

    /**
     * 应用下载页面
     * 参数：
     * packageName 包名
     */
    public void download() throws Exception {
        String appCode = getUrlParam(0);
        boolean beta = getParamToBoolean("beta");
        if (getUrlParams().size() > 1) {
            String urlBeta = getUrlParams().get(1);
            if (urlBeta.equalsIgnoreCase("beta")) {
                beta = true;
            }
        }
        FinalAppEntity appByCode = FinalAppEntity.dao().getAppByCode(appCode);
        if (appByCode == null) {
            appByCode = FinalAppEntity.dao().selectById(appCode);
        }
        if (appByCode != null) {
            appByCode.pullQrUrl(getProjectHost());
            String appLogo = appByCode.getString("appLogo");
            if (!appLogo.startsWith("http")) {
                appByCode.set("appLogo", getProjectHost() + appLogo);
            }

            setRequestAttr("app", appByCode);
            String userAgent = getUserAgent();
            if (userAgent.contains("iPhone")) {
                returnDownloadIOS(appByCode, beta);
                returnDownloadAndroid(appByCode, beta);
            } else {
                returnDownloadAndroid(appByCode, beta);
                returnDownloadIOS(appByCode, beta);
            }
        }
        if (beta) {
            response502("暂无应用的Beta版本信息！");
        }
        response502("暂无应用的版本信息！");
    }


    private void returnDownloadIOS(FinalAppEntity appByCode, boolean beta) {
        appByCode.put("typeName", "苹果版");
        if (beta) {
            appByCode.put("typeName", "苹果Beta版");
        }
        FinalAppIosEntity lastVersion = FinalAppIosEntity.dao().getLastVersion(appByCode.getId(), beta);
        if (lastVersion != null) {
            lastVersion.formatDate("versionDateTime", "yyyy-MM-dd HH:mm");
            lastVersion.set("appFile", getProjectHost() + "appshare/load/ios/" + lastVersion.getId());
            setRequestAttr("ver", lastVersion);
            setRequestAttr("type", "ios");
            setRequestAttr("http", getProjectHost());
            if (lastVersion.isNotEmpty("appStoreUrl") && !beta) {
                redirect(lastVersion.getString("appStoreUrl"));
            }
            responseVelocity("/html/appshare/app-download.html");
        }
    }

    private void returnDownloadAndroid(FinalAppEntity appByCode, boolean beta) {
        appByCode.put("typeName", "安卓版");
        if (beta) {
            appByCode.put("typeName", "安卓Beta版");
        }
        FinalAppAndroidEntity lastVersion = FinalAppAndroidEntity.dao().getLastVersion(appByCode.getId(), beta);
        if (lastVersion != null) {
            lastVersion.formatDate("versionDateTime", "yyyy-MM-dd HH:mm");
            lastVersion.set("appFile", getProjectHost() + "appshare/load/android/" + lastVersion.getId());
            setRequestAttr("ver", lastVersion);
            setRequestAttr("type", "android");
            setRequestAttr("http", getProjectHost());
            if (lastVersion.isNotEmpty("appPubUrl") && !beta) {
                redirect(lastVersion.getString("appPubUrl"));
            }
            responseVelocity("/html/appshare/app-download.html");
        }
    }


    /**
     * 开始下载
     */
    public void load() {
        String type = getUrlParam(0);
        int versionId = FastNumberUtils.formatToInt(getUrlParam(1));
        if (type.equalsIgnoreCase("android")) {
            FinalAppAndroidEntity lastVersion = FinalAppAndroidEntity.dao().selectById(versionId);
            if (lastVersion != null) {
                lastVersion.set("countDownload", lastVersion.getInt("countDownload") + 1);
                if (lastVersion.update()) {
                    FinalAppAndroidHistoryEntity history = FinalAppAndroidHistoryEntity.newInstance();
                    history.set("versionId", lastVersion.getId());
                    history.set("clientIp", getRemoteIp());
                    history.set("clientInfo", getUserAgent());
                    history.save();

                    String appFile = lastVersion.getString("appFile");
                    if (!appFile.startsWith("http")) {
                        redirect(getProjectHost() + appFile);
                    }
                    redirect(appFile);
                }
            }
        } else if (type.equalsIgnoreCase("ios")) {
            FinalAppIosEntity lastVersion = FinalAppIosEntity.dao().selectById(versionId);
            if (lastVersion != null) {
                lastVersion.set("countDownload", lastVersion.getInt("countDownload") + 1);
                if (lastVersion.update()) {

                    FinalAppIosHistoryEntity history = FinalAppIosHistoryEntity.newInstance();
                    history.set("versionId", lastVersion.getId());
                    history.set("clientIp", getRemoteIp());
                    history.set("clientInfo", getUserAgent());
                    history.save();

                    if (lastVersion.isNotEmpty("appStoreUrl") && !lastVersion.isBeta()) {
                        redirect(lastVersion.getString("appStoreUrl"));
                    }
                    String url = "itms-services://?action=download-manifest&url=" + lastVersion.get("plistFile");
                    redirect(url);
                }
            }
        }
        response502("无效应用！");
    }

}
