package org.smartregister.chw.model;

import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.model.BaseFamilyProfileDueModel;
import org.smartregister.family.util.DBConstants;

public class FamilyProfileDueModel extends BaseFamilyProfileDueModel {


    @Override
    public String countSelect(String tableName, String mainCondition) {
        SmartRegisterQueryBuilder countQueryBuilder = new SmartRegisterQueryBuilder();
        countQueryBuilder.selectInitiateMainTableCounts(tableName);
        countQueryBuilder.customJoin("LEFT JOIN " + CoreConstants.TABLE_NAME.FAMILY_MEMBER + " ON  " + CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " + tableName + "." + DBConstants.KEY.BASE_ENTITY_ID + " COLLATE NOCASE ");
        countQueryBuilder.customJoin("LEFT JOIN " + CoreConstants.TABLE_NAME.FAMILY + " ON  " + CoreConstants.TABLE_NAME.FAMILY + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " + tableName + "." + DBConstants.KEY.BASE_ENTITY_ID + " COLLATE NOCASE ");
        countQueryBuilder.customJoin("LEFT JOIN " + CoreConstants.TABLE_NAME.CHILD + " ON  " + CoreConstants.TABLE_NAME.CHILD + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " + tableName + "." + DBConstants.KEY.BASE_ENTITY_ID + " COLLATE NOCASE ");
        return countQueryBuilder.mainCondition(mainCondition);
    }

    @Override
    public String mainSelect(String tableName, String mainCondition) {
        SmartRegisterQueryBuilder queryBuilder = new SmartRegisterQueryBuilder();
        queryBuilder.selectInitiateMainTable(tableName, mainColumns(tableName));
        queryBuilder.customJoin("LEFT JOIN " + CoreConstants.TABLE_NAME.FAMILY_MEMBER + " ON  " + CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " + tableName + "." + DBConstants.KEY.BASE_ENTITY_ID + " COLLATE NOCASE ");
        queryBuilder.customJoin("LEFT JOIN " + CoreConstants.TABLE_NAME.FAMILY + " ON  " + CoreConstants.TABLE_NAME.FAMILY + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " + tableName + "." + DBConstants.KEY.BASE_ENTITY_ID + " COLLATE NOCASE ");
        queryBuilder.customJoin("LEFT JOIN " + CoreConstants.TABLE_NAME.CHILD + " ON  " + CoreConstants.TABLE_NAME.CHILD + "." + DBConstants.KEY.BASE_ENTITY_ID + " = " + tableName + "." + DBConstants.KEY.BASE_ENTITY_ID + " COLLATE NOCASE ");
        return queryBuilder.mainCondition(mainCondition);
    }

    protected String[] mainColumns(String tableName) {
        return new String[]{
                CoreConstants.TABLE_NAME.CHILD + ".entry_point",
                CoreConstants.TABLE_NAME.FAMILY_MEMBER + ".relationalid",
                CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.BASE_ENTITY_ID + " AS _id",
                CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.LAST_INTERACTED_WITH,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.BASE_ENTITY_ID,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.FIRST_NAME,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.MIDDLE_NAME,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.LAST_NAME,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.UNIQUE_ID,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.GENDER,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.DOB,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.DOD,
                CoreConstants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.KEY.ENTITY_TYPE,
                CoreConstants.TABLE_NAME.SCHEDULE_SERVICE + "." + ChildDBConstants.KEY.SCHEDULE_NAME,
                CoreConstants.TABLE_NAME.SCHEDULE_SERVICE + "." + ChildDBConstants.KEY.DUE_DATE,
                CoreConstants.TABLE_NAME.SCHEDULE_SERVICE + "." + ChildDBConstants.KEY.OVER_DUE_DATE,
                CoreConstants.TABLE_NAME.SCHEDULE_SERVICE + "." + ChildDBConstants.KEY.NOT_DONE_DATE,
                CoreConstants.TABLE_NAME.SCHEDULE_SERVICE + "." + ChildDBConstants.KEY.EXPIRY_DATE,
                CoreConstants.TABLE_NAME.SCHEDULE_SERVICE + "." + ChildDBConstants.KEY.COMPLETION_DATE,
                CoreConstants.TABLE_NAME.FAMILY + "." + DBConstants.KEY.FIRST_NAME + " AS " + ChildDBConstants.KEY.FAMILY_FIRST_NAME
        };
    }
}
