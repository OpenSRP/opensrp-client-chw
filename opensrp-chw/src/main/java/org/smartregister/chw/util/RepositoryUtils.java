package org.smartregister.chw.util;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.util.DatabaseMigrationUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public interface RepositoryUtils {

    String ADD_MISSING_REPORTING_COLUMN = "ALTER TABLE 'indicator_queries' ADD COLUMN expected_indicators TEXT NULL;";
    String ADD_DEATH_RECEIVE_COLUMN_TO_ECCHILD = "ALTER TABLE ec_child ADD received_death_certificate VARCHAR;";
    String ADD_DEATH_CERT_DATE_TO_ECCHILD = "ALTER TABLE ec_child ADD death_certificate_issue_date VARCHAR;";
    String ADD_DEATH_RECEIVE_COLUMNS_TO_FAMILY_MEMBER = "ALTER TABLE ec_family_member ADD received_death_certificate VARCHAR;";
    String ADD_DEATH_CERT_DATE_TO_FAMILY_MEMBER = "ALTER TABLE ec_family_member ADD death_certificate_issue_date VARCHAR;";
    String ADD_BIRTH_REG_TO_CHILD = "ALTER TABLE ec_child ADD birth_registration VARCHAR;";

    String FAMILY_MEMBER_ADD_REASON_FOR_REGISTRATION = "ALTER TABLE 'ec_family_member' ADD COLUMN reasons_for_registration TEXT NULL;";
    String EC_REFERRAL_ADD_FP_METHOD_COLUMN = "ALTER TABLE 'ec_referral' ADD COLUMN fp_method_accepted_referral TEXT NULL;";

    String[] UPDATE_REPOSITORY_TYPES = {
            "UPDATE recurring_service_types SET service_group = 'woman' WHERE type = 'IPTp-SP';",
            "UPDATE recurring_service_types SET service_group = 'child' WHERE type != 'IPTp-SP';",
    };

    String[] UPGRADE_V10 = {
            "ALTER TABLE ec_child ADD COLUMN mother_entity_id VARCHAR;",
            "ALTER TABLE ec_child ADD COLUMN entry_point VARCHAR;"
    };

    String DELETE_DUPLICATE_SCHEDULES = "delete from schedule_service where id not in ( " +
            "select max(id) from schedule_service " +
            "group by base_entity_id , schedule_group_name , schedule_name " +
            "having count(*) > 1 " +
            ")";

    static void addDetailsColumnToFamilySearchTable(SQLiteDatabase db) {
        try {

            db.execSQL("ALTER TABLE ec_family ADD COLUMN entity_type VARCHAR; " +
                    "UPDATE ec_family SET entity_type = 'ec_family' WHERE id is not null;");

            List<String> columns = new ArrayList<>();
            columns.add(CoreConstants.DB_CONSTANTS.DETAILS);
            columns.add(DBConstants.KEY.ENTITY_TYPE);
            DatabaseMigrationUtils.addFieldsToFTSTable(db, CoreChwApplication.createCommonFtsObject(), CoreConstants.TABLE_NAME.FAMILY, columns);

        } catch (Exception e) {
            Timber.e(e, "commonUpgrade -> Failed to add column 'entity_type' and 'details' to ec_family_search ");
        }
    }

}
