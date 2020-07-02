package com.fastchar.appshare.entity;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastFile;
import com.fastchar.extjs.FastExtConfig;
import com.fastchar.extjs.FastExtHelper;
import com.fastchar.extjs.core.FastExtEntity;
import com.fastchar.database.FastPage;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.utils.FastDateUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import com.fastchar.utils.FastFileUtils;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

public class FinalAppIosEntity extends FastExtEntity<FinalAppIosEntity> {
    private static final long serialVersionUID = 1L;

    public static FinalAppIosEntity dao() {
        return FastChar.getOverrides().singleInstance(FinalAppIosEntity.class);
    }

    public static FinalAppIosEntity newInstance() {
        return FastChar.getOverrides().newInstance(FinalAppIosEntity.class);
    }

    @Override
    public String getTableName() {
        return "final_app_ios";
    }

    @Override
    public String getTableDetails() {
        return "苹果版本管理";
    }

    @Override
    public String getEntityCode() {
        return this.getClass().getSimpleName();
    }

    @Override
    public FastPage<FinalAppIosEntity> showList(int page, int pageSize) {


        String sqlStr = "select t.*,a.appName as a__appName" +
                " from final_app_ios as t" +
                " left join final_app as a on a.appId=t.appId";
        FastSqlInfo sqlInfo = toSelectSql(sqlStr);
        return selectBySql(page, pageSize, sqlInfo.getSql(), sqlInfo.toParams());
    }

    @Override
    public void setDefaultValue() {
        set("appId", 0);
        set("versionBuild", 0);
        set("fileSize", 0);
        set("versionImportant", 0);
        set("versionDesc", "发现新版本，建议您立即更新！");
        set("countDownload", 0);
        set("versionState", 0);
        set("versionDateTime", FastDateUtils.getDateString());
    }

    @Override
    public void convertValue() {
        super.convertValue();
        Enum<?> versionImportant = getEnum("versionImportant", FinalAppEntity.VersionImportantEnum.class);
        if (versionImportant != null) {
            put("versionImportantStr", versionImportant.name());
        }
        Enum<?> versionState = getEnum("versionState", FinalAppEntity.VersionStateEnum.class);
        if (versionState != null) {
            put("versionStateStr", versionState.name());
        }

        int fileSize = getInt("fileSize");
        if (fileSize >= 1024 * 1024) {
            put("fileSizeStr", FastNumberUtils.formatToDouble(fileSize / 1024.0 / 1024.0, 2) + "M");
        } else if (fileSize >= 1024) {
            put("fileSizeStr", FastNumberUtils.formatToDouble(fileSize / 1024.0, 2) + "KB");
        } else {
            put("fileSizeStr", fileSize + "B");
        }
    }


    /**
     * 获得数据详情
     */
    public FinalAppIosEntity getDetails(int versionId) {
        List<String> linkColumns = new ArrayList<>();
        linkColumns.addAll(FinalAppEntity.dao().toSelectColumns("a"));
        String sqlStr = "select t.*," + FastStringUtils.join(linkColumns, ",") + " from final_app_ios as t" +
                " left join final_app as a on a.appId=t.appId" +
                " where t.versionId = ?  ";
        FinalAppIosEntity entity = selectFirstBySql(sqlStr, versionId);
        if (entity != null) {
            FinalAppEntity app = entity.toEntity("a", FinalAppEntity.class);
            entity.put("app", app);
        }
        return entity;
    }

    /**
     * 根据appId获得本实体集合
     *
     * @return 分页数据
     */
    public FastPage<FinalAppIosEntity> getListByAppId(int page, int appId) {

        List<String> linkColumns = new ArrayList<>();
        linkColumns.addAll(FinalAppEntity.dao().toSelectColumns("a"));

        String sqlStr = "select t.*," + FastStringUtils.join(linkColumns, ",") + " from final_app_ios as t" +
                " left join final_app as a on a.appId=t.appId" +
                " where t.appId=? ";
        FastPage<FinalAppIosEntity> pageList = selectBySql(page, 10, sqlStr, appId);
        for (FinalAppIosEntity entity : pageList.getList()) {
            FinalAppEntity app = entity.toEntity("a", FinalAppEntity.class);
            entity.put("app", app);
        }
        return pageList;
    }

    /**
     * 根据appIds批量查询数据，并整理成Map对应关系
     *
     * @return Map集合，key：appId value: List<FinalAppIosEntity>
     */
    public Map<Object, List<FinalAppIosEntity>> getMapListByAppIds(Object... appIds) {
        List<String> placeHolder = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (Object appId : appIds) {
            placeHolder.add("?");
            values.add(appId);
        }
        Map<Object, List<FinalAppIosEntity>> map = new HashMap<>();
        if (appIds.length == 0) {
            return map;
        }
        List<String> linkColumns = new ArrayList<>();
        linkColumns.addAll(FinalAppEntity.dao().toSelectColumns("a"));

        String sqlStr = "select t.*," + FastStringUtils.join(linkColumns, ",") + " from final_app_ios as t" +
                " left join final_app as a on a.appId=t.appId" +
                " where t.appId in (" + FastStringUtils.join(placeHolder, ",") + ") ";
        List<FinalAppIosEntity> result = selectBySql(sqlStr, values.toArray());
        for (FinalAppIosEntity entity : result) {
            FinalAppEntity app = entity.toEntity("a", FinalAppEntity.class);
            entity.put("app", app);
            Object appId = entity.get("appId");
            if (!map.containsKey(appId)) {
                map.put(appId, new ArrayList<FinalAppIosEntity>());
            }
            map.get(appId).add(entity);
        }
        return map;
    }


    public String getPlistFileUrl(FinalAppEntity appEntity) throws Exception {
        File file = new File(FastChar.getConstant().getAttachDirectory(), "appshare/ios" + getId() + ".plist");

        URL url = FinalAppIosEntity.class.getResource("ios-plist");
        FastFileUtils.copyURLToFile(url, file);

        String plist = FastFileUtils.readFileToString(file);
        Map<String, Object> params = new HashMap<>();
        params.put("AppUrl", get("appFile"));
        params.put("IconUrl", appEntity.get("appLogo"));
        params.put("BundleIdentifier", get("packageName"));
        params.put("BundleVersion", get("versionName"));
        params.put("Subtitle", appEntity.get("appName"));
        params.put("Title", appEntity.get("appName"));
        plist = FastExtConfig.replacePlaceholder(params, plist);

        FastFileUtils.writeStringToFile(file, plist);

        return FastFile.newInstance(file).getUrl();
    }


    public FinalAppIosEntity getLastVersion(int appId) {
        String sqlStr = "select * from final_app_ios where appId = ? order by versionDateTime desc ";
        return selectFirstBySql(sqlStr, appId);
    }


}
