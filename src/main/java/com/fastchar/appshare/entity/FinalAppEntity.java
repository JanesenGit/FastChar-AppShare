package com.fastchar.appshare.entity;

import com.fastchar.core.FastChar;
import com.fastchar.extjs.core.FastExtEntity;
import com.fastchar.database.FastPage;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.utils.FastDateUtils;

public class FinalAppEntity extends FastExtEntity<FinalAppEntity> {
    private static final long serialVersionUID = 1L;

    public static FinalAppEntity dao() {
        return FastChar.getOverrides().singleInstance(FinalAppEntity.class);
    }

    public static FinalAppEntity newInstance() {
        return FastChar.getOverrides().newInstance(FinalAppEntity.class);
    }

    @Override
    public String getTableName() {
        return "final_app";
    }

    @Override
    public String getTableDetails() {
        return "APP管理";
    }

    @Override
    public String getEntityCode() {
        return this.getClass().getSimpleName();
    }

    @Override
    public FastPage<FinalAppEntity> showList(int page, int pageSize) {


        String sqlStr = "select t.*" +
                " from final_app as t" +
                " ";
        FastSqlInfo sqlInfo = toSelectSql(sqlStr);
        return selectBySql(page, pageSize, sqlInfo.getSql(), sqlInfo.toParams());
    }

    @Override
    public void setDefaultValue() {
        set("appState", 0);
        set("appDateTime", FastDateUtils.getDateString());
    }

    @Override
    public void convertValue() {
        super.convertValue();
        Enum<?> appState = getEnum("appState", AppStateEnum.class);
        if (appState != null) {
            put("appStateStr", appState.name());
        }
    }

    public enum AppStateEnum {
        正常,
        下线
    }

    public enum VersionStateEnum {
        正常,
        禁用
    }

    public enum VersionImportantEnum {
        一般,
        重要
    }



    public FinalAppEntity getAppByCode(String appCode) {
        String sqlStr = "select * from final_app where appCode = ? ";
        return selectFirstBySql(sqlStr,appCode);
    }


}
