package com.fastchar.appshare.entity;
import com.fastchar.core.FastChar;
import com.fastchar.extjs.core.FastExtEntity;
import com.fastchar.database.FastPage;
import com.fastchar.database.info.FastSqlInfo;
import com.fastchar.utils.FastDateUtils;

public class FinalAppIosHistoryEntity extends FastExtEntity<FinalAppIosHistoryEntity> {
    private static final long serialVersionUID = 1L;

    public static FinalAppIosHistoryEntity dao() {
        return FastChar.getOverrides().singleInstance(FinalAppIosHistoryEntity.class);
    }
    
    public static FinalAppIosHistoryEntity newInstance() {
        return FastChar.getOverrides().newInstance(FinalAppIosHistoryEntity.class);
    }

    @Override
    public String getTableName() {
        return "final_app_ios_history";
    }

    @Override
    public String getTableDetails() {
        return "苹果版本下载记录";
    }

    @Override
    public String getEntityCode() {
        return this.getClass().getSimpleName();
    }

    @Override
    public FastPage<FinalAppIosHistoryEntity> showList(int page, int pageSize) {
        

        String sqlStr = "select t.*"+
" from final_app_ios_history as t"+
" ";
        FastSqlInfo sqlInfo = toSelectSql(sqlStr);
        return selectBySql(page, pageSize, sqlInfo.getSql(), sqlInfo.toParams());
    }

    @Override
    public void setDefaultValue() {
         set("versionId", 0);
		 set("historyDateTime", FastDateUtils.getDateString());
    }

    @Override
    public void convertValue() {
        super.convertValue();
         
    }

    

    
}
