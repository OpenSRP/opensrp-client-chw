package org.smartregister.chw.repository;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.util.RepositoryUtils;
import org.smartregister.domain.db.Column;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.IMDatabaseUtils;
import org.smartregister.reporting.ReportingLibrary;
import org.smartregister.repository.AlertRepository;
import org.smartregister.repository.EventClientRepository;

import java.util.Arrays;
import java.util.Collections;

import timber.log.Timber;

public class ChwRepositoryFlv {

    private static final String TAG = ChwRepositoryFlv.class.getCanonicalName();

    public static void onUpgrade(Context context, SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.w(ChwRepository.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            switch (upgradeTo) {
                case 2:
                    upgradeToVersion2(context, db);
                    break;
                case 3:
                    upgradeToVersion3(db);
                    break;
                case 9:
                    upgradeToVersion9(db);
                    break;
                case 10:
                    upgradeToVersion10(context, db);
                    break;
                case 11:
                    upgradeToVersion11(db);
                    break;
                default:
                    break;
            }
            upgradeTo++;
        }
    }

    private static void upgradeToVersion11(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE ec_family_member ADD COLUMN marital_status VARCHAR;");
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion11");
        }
    }


    private static void upgradeToVersion2(Context context, SQLiteDatabase db) {
        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_EVENT_ID_COL);
            db.execSQL(VaccineRepository.EVENT_ID_INDEX);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL);
            db.execSQL(VaccineRepository.FORMSUBMISSION_INDEX);

            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX);

//            EventClientRepository.createTable(db, EventClientRepository.Table.path_reports, EventClientRepository.report_column.values());
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_HIA2_STATUS_COL);

        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion2 ");
        }

    }

    private static void upgradeToVersion3(SQLiteDatabase db) {
        try {
            Column[] columns = {EventClientRepository.event_column.formSubmissionId};
            EventClientRepository.createIndex(db, EventClientRepository.Table.event, columns);

            db.execSQL(VaccineRepository.ALTER_ADD_CREATED_AT_COLUMN);
            VaccineRepository.migrateCreatedAt(db);

            db.execSQL(RecurringServiceRecordRepository.ALTER_ADD_CREATED_AT_COLUMN);
            RecurringServiceRecordRepository.migrateCreatedAt(db);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion3 - Part 0");
        }

        try {
            Column[] columns = {EventClientRepository.event_column.formSubmissionId};
            EventClientRepository.createIndex(db, EventClientRepository.Table.event, columns);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion3 - Part 1");
        }

        try {
            db.execSQL(AlertRepository.ALTER_ADD_OFFLINE_COLUMN);
            db.execSQL(AlertRepository.OFFLINE_INDEX);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_COL);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
            db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_TEAM_COL);
            db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_TEAM_ID_COL);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion3 - Part 2");
        }

        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
            db.execSQL(RecurringServiceRecordRepository.UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion3 - Part 3");
        }

        try {
            RepositoryUtils.addDetailsColumnToFamilySearchTable(db);
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion3 - Part 4");
        }

        try {
            // setup reporting
            ReportingLibrary reportingLibrary = ReportingLibrary.getInstance();
            String childIndicatorsConfigFile = "config/child-reporting-indicator-definitions.yml";
            String ancIndicatorConfigFile = "config/anc-reporting-indicator-definitions.yml";
            String pncIndicatorConfigFile = "config/pnc-reporting-indicator-definitions.yml";
            for (String configFile : Collections.unmodifiableList(
                    Arrays.asList(childIndicatorsConfigFile, ancIndicatorConfigFile, pncIndicatorConfigFile))) {
                reportingLibrary.readConfigFile(configFile, db);
            }
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion3 - Part 4");
        }
    }

    private static void upgradeToVersion9(SQLiteDatabase db) {
        try {
            db.execSQL(VisitRepository.ADD_VISIT_GROUP_COLUMN);
            db.execSQL("ALTER TABLE ec_anc_register ADD COLUMN delivery_kit VARCHAR;");
        } catch (Exception e) {
            Timber.e(e, "upgradeToVersion9");
        }
    }

    private static void upgradeToVersion10(Context context, SQLiteDatabase db) {
        try {
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_IS_VOIDED_COL);
            db.execSQL(VaccineRepository.UPDATE_TABLE_ADD_IS_VOIDED_COL_INDEX);

            IMDatabaseUtils.accessAssetsAndFillDataBaseForVaccineTypes(context, db);
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
