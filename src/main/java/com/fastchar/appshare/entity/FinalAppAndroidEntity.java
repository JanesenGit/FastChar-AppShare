package com.fastchar.appshare.entity;

import com.fastchar.core.FastChar;
import com.fastchar.extjs.core.FastExtEntity;
import com.fastchar.database.FastPage;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.utils.FastDateUtils;

import java.util.*;

import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

public class FinalAppAndroidEntity extends FastExtEntity<FinalAppAndroidEntity> {
    private static final long serialVersionUID = 1L;

    public static FinalAppAndroidEntity dao() {
        return FastChar.getOverrides().singleInstance(FinalAppAndroidEntity.class);
    }

    public static FinalAppAndroidEntity newInstance() {
        return FastChar.getOverrides().newInstance(FinalAppAndroidEntity.class);
    }

    @Override
    public String getTableName() {
        return "final_app_android";
    }

    @Override
    public String getTableDetails() {
        return "安卓版本管理";
    }

    @Override
    public String getEntityCode() {
        return this.getClass().getSimpleName();
    }

    @Override
    public FastPage<FinalAppAndroidEntity> showList(int page, int pageSize) {


        String sqlStr = "select t.*,a.appName as a__appName" +
                " from final_app_android as t" +
                " left join final_app as a on a.appId=t.appId";
        FastSqlInfo sqlInfo = toSelectSql(sqlStr);
        return selectBySql(page, pageSize, sqlInfo.getSql(), sqlInfo.toParams());
    }

    @Override
    public void setDefaultValue() {
        set("appId", 0);
        set("versionBuild", 0);
        set("fileSize", 0);
        set("versionState", 0);
        set("versionImportant", 0);
        set("versionDesc", "发现新版本，建议您立即更新！");
        set("countDownload", 0);
        set("versionDateTime", FastDateUtils.getDateString());
    }

    @Override
    public void convertValue() {
        super.convertValue();
        Enum<?> versionState = getEnum("versionState", FinalAppEntity.VersionStateEnum.class);
        if (versionState != null) {
            put("versionStateStr", versionState.name());
        }

        Enum<?> versionImportant = getEnum("versionImportant", FinalAppEntity.VersionImportantEnum.class);
        if (versionImportant != null) {
            put("versionImportantStr", versionImportant.name());
        }

        int fileSize = getInt("fileSize");
        if (fileSize >= 1024 * 1024) {
            put("fileSizeStr", FastNumberUtils.formatToDouble(fileSize / 1024.0 / 1024.0, 2) + "M");
        }else  if (fileSize >= 1024) {
            put("fileSizeStr", FastNumberUtils.formatToDouble(fileSize / 1024.0, 2) + "KB");
        }else{
            put("fileSizeStr", fileSize + "B");
        }
    }

    public boolean isBeta() {
        return getString("versionName", "none").toLowerCase().contains("beta");
    }


    /**
     * 获得数据详情
     */
    public FinalAppAndroidEntity getDetails(int versionId) {
        List<String> linkColumns = new ArrayList<>();
        linkColumns.addAll(FinalAppEntity.dao().toSelectColumns("a"));
        String sqlStr = "select t.*," + FastStringUtils.join(linkColumns, ",") + " from final_app_android as t" +
                " left join final_app as a on a.appId=t.appId" +
                " where t.versionId = ?  ";
        FinalAppAndroidEntity entity = selectFirstBySql(sqlStr, versionId);
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
    public FastPage<FinalAppAndroidEntity> getListByAppId(int page, int appId) {

        List<String> linkColumns = new ArrayList<>();
        linkColumns.addAll(FinalAppEntity.dao().toSelectColumns("a"));

        String sqlStr = "select t.*," + FastStringUtils.join(linkColumns, ",") + " from final_app_android as t" +
                " left join final_app as a on a.appId=t.appId" +
                " where t.appId=? ";
        FastPage<FinalAppAndroidEntity> pageList = selectBySql(page, 10, sqlStr, appId);
        for (FinalAppAndroidEntity entity : pageList.getList()) {
            FinalAppEntity app = entity.toEntity("a", FinalAppEntity.class);
            entity.put("app", app);
        }
        return pageList;
    }

    /**
     * 根据appIds批量查询数据，并整理成Map对应关系
     *
     * @return Map集合，key：appId value: List<FinalAppAndroidEntity>
     */
    public Map<Object, List<FinalAppAndroidEntity>> getMapListByAppIds(Object... appIds) {
        List<String> placeHolder = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (Object appId : appIds) {
            placeHolder.add("?");
            values.add(appId);
        }
        Map<Object, List<FinalAppAndroidEntity>> map = new HashMap<>();
        if (appIds.length == 0) {
            return map;
        }
        List<String> linkColumns = new ArrayList<>();
        linkColumns.addAll(FinalAppEntity.dao().toSelectColumns("a"));

        String sqlStr = "select t.*," + FastStringUtils.join(linkColumns, ",") + " from final_app_android as t" +
                " left join final_app as a on a.appId=t.appId" +
                " where t.appId in (" + FastStringUtils.join(placeHolder, ",") + ") ";
        List<FinalAppAndroidEntity> result = selectBySql(sqlStr, values.toArray());
        for (FinalAppAndroidEntity entity : result) {
            FinalAppEntity app = entity.toEntity("a", FinalAppEntity.class);
            entity.put("app", app);
            Object appId = entity.get("appId");
            if (!map.containsKey(appId)) {
                map.put(appId, new ArrayList<FinalAppAndroidEntity>());
            }
            map.get(appId).add(entity);
        }
        return map;
    }


    public int updateState(int appId, int state) {
        String sqlStr = "update final_app_android set versionState = ? where appId = ? ";
        return updateBySql(sqlStr, state, appId);
    }


    public FinalAppAndroidEntity getLastVersion(int appId,boolean beta) {
        String sqlStr = "select * from final_app_android where appId = ?  ";

        if (beta) {
            sqlStr += " and lower(versionName) like '%beta%' ";
        }else{
            sqlStr += " and lower(versionName) not like '%beta%' ";
        }
        sqlStr += " order by versionDateTime desc ";

        return selectFirstBySql(sqlStr, appId);
    }

}
