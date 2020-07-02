package com.fastchar.appshare.action;

import com.fastchar.appshare.entity.FinalAppAndroidEntity;
import com.fastchar.appshare.entity.FinalAppEntity;
import com.fastchar.appshare.entity.FinalAppIosEntity;
import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.utils.FastMD5Utils;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 沈建（Janesen）
 * @date 2020/5/19 15:46
 */
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
        FinalAppEntity appByCode = FinalAppEntity.dao().getAppByCode(appCode);
        if (appByCode != null) {
            if (getParamToBoolean("ios", Boolean.FALSE)) {
                FinalAppIosEntity lastVersion = FinalAppIosEntity.dao().getLastVersion(appByCode.getId());
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
                FinalAppAndroidEntity lastVersion = FinalAppAndroidEntity.dao().getLastVersion(appByCode.getId());
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
        FinalAppEntity appByCode = FinalAppEntity.dao().getAppByCode(appCode);
        if (appByCode == null) {
            appByCode = FinalAppEntity.dao().selectById(appCode);
        }
        if (appByCode != null) {
            String appLogo = appByCode.getString("appLogo");
            if (!appLogo.startsWith("http")) {
                appByCode.set("appLogo", getProjectHost() + appLogo);
            }

            setRequestAttr("app", appByCode);
            String userAgent = getUserAgent();
            if (userAgent.contains("iPhone")) {
                appByCode.put("typeName", "苹果版");
                FinalAppIosEntity lastVersion = FinalAppIosEntity.dao().getLastVersion(appByCode.getId());
                if (lastVersion != null) {
                    lastVersion.set("appFile", getProjectHost() + "appshare/load/ios/" + lastVersion.getId());
                    setRequestAttr("ver", lastVersion);
                    if (lastVersion.isNotEmpty("appStoreUrl")) {
                        redirect(lastVersion.getString("appStoreUrl"));
                    }
                    responseVelocity("/html/app-download.html");
                }
            } else {
                appByCode.put("typeName", "安卓版");
                FinalAppAndroidEntity lastVersion = FinalAppAndroidEntity.dao().getLastVersion(appByCode.getId());
                if (lastVersion != null) {
                    lastVersion.set("appFile", getProjectHost() + "appshare/load/android/" + lastVersion.getId());
                    setRequestAttr("ver", lastVersion);
                    if (lastVersion.isNotEmpty("appPubUrl")) {
                        redirect(lastVersion.getString("appPubUrl"));
                    }
                    responseVelocity("/html/app-download.html");
                }
            }

        }
        responseText("应用信息不存在！");
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
                    if (lastVersion.isNotEmpty("appStoreUrl")) {
                        redirect(lastVersion.getString("appStoreUrl"));
                    }
                    String url = "itms-services://?action=download-manifest&url=" + lastVersion.get("plistFile");
                    redirect(url);
                }
            }
        }
        responseText("无效应用！");
    }


}
