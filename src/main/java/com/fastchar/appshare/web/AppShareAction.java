package com.fastchar.appshare.web;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.fastchar.appshare.entity.FinalAppAndroidEntity;
import com.fastchar.appshare.entity.FinalAppEntity;
import com.fastchar.appshare.entity.FinalAppIosEntity;
import com.fastchar.appshare.utils.ConvertHandler;
import com.fastchar.appshare.utils.IPAUtils;
import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.core.FastFile;
import com.fastchar.extjs.annotation.AFastSession;
import com.fastchar.utils.FastFileUtils;
import com.fastchar.utils.FastMD5Utils;
import com.fastchar.utils.FastStringUtils;
import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 沈建（Janesen）
 * @date 2020/5/19 14:55
 */
@AFastSession
public class AppShareAction extends FastAction {
    @Override
    protected String getRoute() {
        return "/appshare";
    }


    public void upload() throws Exception {
        FastFile<?> paramFile = getParamFile();
        if (paramFile.getFileName().toLowerCase().endsWith(".apk")) {
            try (ApkFile apkFile = new ApkFile(paramFile.getFile())) {
                ApkMeta apkMeta = apkFile.getApkMeta();
                String appCode = FastMD5Utils.MD5To16(apkMeta.getPackageName());
                FinalAppEntity appByCode = FinalAppEntity.dao().getAppByCode(appCode);
                if (appByCode == null) {
                    appByCode = FinalAppEntity.newInstance();
                    appByCode.set("appCode", appCode);
                    appByCode.set("appName", apkMeta.getLabel());
                    appByCode.set("appState", FinalAppEntity.AppStateEnum.正常.ordinal());
                    List<IconFace> allIcons = apkFile.getAllIcons();
                    if (allIcons.size() > 0) {
                        File iconFile = new File(FastChar.getConstant().getAttachDirectory(), appCode + ".png");
                        FastFileUtils.writeByteArrayToFile(iconFile, allIcons.get(allIcons.size() - 1).getData());
                        appByCode.set("appLogo", FastFile.newInstance(iconFile).getUrl());
                    }
                    if (!appByCode.save()) {
                        responseJson(-1, "上传失败！" + appByCode.getError());
                    }
                }

                boolean beta = apkMeta.getVersionName().toLowerCase().contains("beta");

                FinalAppAndroidEntity lastVersion = FinalAppAndroidEntity.dao().getLastVersion(appByCode.getId(), beta);

                FinalAppAndroidEntity androidEntity = FinalAppAndroidEntity.newInstance();
                androidEntity.set("appId", appByCode.getId());
                if (lastVersion != null) {
                    androidEntity.setAll(lastVersion);
                    androidEntity.remove("versionId");
                    androidEntity.remove("versionDateTime");
                    androidEntity.remove("countDownload");
                }
                androidEntity.set("packageName", apkMeta.getPackageName());
                androidEntity.set("versionName", apkMeta.getVersionName());
                androidEntity.set("versionBuild", apkMeta.getVersionCode());
                androidEntity.set("appOSType", apkMeta.getMinSdkVersion());
                List<ApkSigner> apkSingers = apkFile.getApkSingers();
                for (ApkSigner apkSinger : apkSingers) {
                    for (CertificateMeta certificateMeta : apkSinger.getCertificateMetas()) {
                        androidEntity.set("appSign", certificateMeta.getCertMd5());
                    }
                }
                androidEntity.set("appPermissions", FastStringUtils.join(apkMeta.getUsesPermissions(), "\n"));

                androidEntity.set("fileSize", paramFile.getFile().length());
                androidEntity.set("appFile", paramFile.getUrl());
                if (androidEntity.save()) {
                    responseJson(0, "上传成功！");
                }
            }
        } else if (paramFile.getFileName().toLowerCase().endsWith(".ipa")) {
            File plistFile = IPAUtils.getPlistFile(paramFile.getFile());
            if (plistFile != null) {
                NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(plistFile);
                String appCode = FastMD5Utils.MD5To16(String.valueOf(rootDict.get("CFBundleIdentifier")));

                FinalAppEntity appByCode = FinalAppEntity.dao().getAppByCode(appCode);
                if (appByCode == null) {
                    appByCode = FinalAppEntity.newInstance();
                    appByCode.set("appCode", appCode);
                    if (rootDict.containsKey("CFBundleDisplayName")) {
                        appByCode.set("appName", String.valueOf(rootDict.get("CFBundleDisplayName")));
                    }else{
                        appByCode.set("appName", String.valueOf(rootDict.get("CFBundleName")));
                    }
                    appByCode.set("appState", FinalAppEntity.AppStateEnum.正常.ordinal());
                    File appIcon = IPAUtils.getAppIcon(paramFile.getFile(), plistFile);
                    if (appIcon != null) {

                        File destFile = new File(appIcon.getParent(), "appIcon_dest.png");
                        File upFile = new File(appIcon.getParent(), "appIcon_up.png");
                        File downFile = new File(appIcon.getParent(), "appIcon_down.png");

                        ConvertHandler handler = new ConvertHandler();
                        handler.convertPNGFile(appIcon, destFile, upFile, downFile);

                        appByCode.set("appLogo", FastFile.newInstance(destFile).getUrl());

                        FastFileUtils.forceDelete(appIcon);
                        FastFileUtils.forceDelete(destFile);
                        FastFileUtils.forceDelete(upFile);
                        FastFileUtils.forceDelete(downFile);
                    }

                    if (!appByCode.save()) {
                        responseJson(-1, "上传失败！" + appByCode.getError());
                    }
                }

                boolean beta = String.valueOf(rootDict.get("CFBundleShortVersionString")).toLowerCase().contains("beta");

                FinalAppIosEntity lastVersion = FinalAppIosEntity.dao().getLastVersion(appByCode.getId(), beta);
                FinalAppIosEntity iosEntity = FinalAppIosEntity.newInstance();
                iosEntity.set("appId", appByCode.getId());
                if (lastVersion != null) {
                    iosEntity.setAll(lastVersion);
                    iosEntity.remove("versionId");
                    iosEntity.remove("versionDateTime");
                    iosEntity.remove("countDownload");
                }
                iosEntity.set("packageName", String.valueOf(rootDict.get("CFBundleIdentifier")));
                iosEntity.set("versionName", String.valueOf(rootDict.get("CFBundleShortVersionString")));
                iosEntity.set("versionBuild", String.valueOf(rootDict.get("CFBundleVersion")));
                iosEntity.set("appOSType", String.valueOf(rootDict.get("MinimumOSVersion")));

                File mobileProvisionFile = IPAUtils.getMobileProvisionFile(paramFile.getFile());
                if (mobileProvisionFile != null && mobileProvisionFile.exists()) {
                    NSDictionary nsDictionary = IPAUtils.parseMobileProvisionFile(mobileProvisionFile);
                    if (nsDictionary != null) {
                        NSArray provisionedDevices = (NSArray) nsDictionary.get("ProvisionedDevices");
                        if (provisionedDevices != null) {
                            List<String> uuidList = new ArrayList<>();
                            for (int i = 0; i < provisionedDevices.count(); i++) {
                                uuidList.add(provisionedDevices.objectAtIndex(i).toString());
                            }
                            iosEntity.set("appDevices", FastStringUtils.join(uuidList, "\n"));
                        }
                        iosEntity.set("appTeamName", String.valueOf(nsDictionary.get("TeamName")));
                    }
                    FastFileUtils.forceDelete(mobileProvisionFile);
                }

                IPAUtils.clearFile(paramFile.getFile());

                iosEntity.set("fileSize", paramFile.getFile().length());
                iosEntity.set("appFile", paramFile.getUrl());
                iosEntity.set("plistFile", iosEntity.getPlistFileUrl(appByCode));
                if (iosEntity.save()) {
                    FastFileUtils.forceDelete(plistFile);
                    responseJson(0, "上传成功！");
                }
            }
        }
        responseJson(-1, "上传失败！请稍后重试！");
    }

}
