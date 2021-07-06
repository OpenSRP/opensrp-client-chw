package org.smartregister.chw.util;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.DBConstants;

public class ChildDBConstants extends org.smartregister.chw.core.utils.ChildDBConstants {

    public static String ADD_COLUMN_THINK_MD_ID = "ALTER TABLE ec_child ADD COLUMN thinkmd_id VARCHAR;";
    public static String ADD_COLUMN_HTML_ASSESSMENT = "ALTER TABLE ec_child ADD COLUMN thinkmd_fhir_bundle VARCHAR;";
    public static String ADD_COLUMN_CARE_PLAN_DATE = "ALTER TABLE ec_child ADD COLUMN care_plan_date VARCHAR;";

    public static String childDueVaccinesFilterForChildrenBelowTwoAndGirlsAgeNineToEleven() {
        return childDueVaccinesFilterForChildrenBelowTwoAndGirlsAgeNineToEleven(DBConstants.KEY.DOB, org.smartregister.chw.core.utils.ChildDBConstants.KEY.ENTRY_POINT, org.smartregister.chw.core.utils.ChildDBConstants.KEY.MOTHER_ENTITY_ID);
    }

    public static String childDueVaccinesFilterForChildrenBelowTwoAndGirlsAgeNineToEleven(String tableName) {
        return childDueVaccinesFilterForChildrenBelowTwoAndGirlsAgeNineToEleven(tableColConcat(tableName, DBConstants.KEY.DOB), tableColConcat(tableName, org.smartregister.chw.core.utils.ChildDBConstants.KEY.ENTRY_POINT), tableColConcat(tableName, org.smartregister.chw.core.utils.ChildDBConstants.KEY.MOTHER_ENTITY_ID));
    }

    private static String childDueVaccinesFilterForChildrenBelowTwoAndGirlsAgeNineToEleven(String dateColumn, String entryPoint, String motherEntityId) {
        return " (( ifnull(" + CoreConstants.TABLE_NAME.CHILD + "." + entryPoint + ",'') <> 'PNC' ) " +
                " or (ifnull(" + CoreConstants.TABLE_NAME.CHILD + "." + entryPoint + ",'') = 'PNC'" +
                " and ( date(" + CoreConstants.TABLE_NAME.CHILD + "." + dateColumn + ", '+28 days') <= date() " +
                " and ((SELECT is_closed FROM ec_family_member WHERE base_entity_id = " + CoreConstants.TABLE_NAME.CHILD + "." + motherEntityId + " ) = 0))) " +
                " or (ifnull(ec_child.entry_point,'') = 'PNC' " +
                " and (SELECT is_closed FROM ec_family_member WHERE base_entity_id = ec_child.mother_entity_id ) = 1)) " +
                "and (((julianday('now') - julianday(ec_child.dob))/365.25) < 2 or (ec_child.gender = 'Female' and (((julianday('now') - julianday(ec_child.dob))/365.25) BETWEEN 9 AND 11)))\n";
    }

    public static String tableColConcat(String tableName, String columnName) {
        if (StringUtils.isBlank(tableName) || StringUtils.isBlank(columnName)) {
            return "";
        }
        return tableName.concat(".").concat(columnName);
    }
}