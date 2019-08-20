package org.smartregister.chw.core.interactor;

import android.database.Cursor;

import org.smartregister.chw.core.contract.CoreApplication;
import org.smartregister.chw.core.contract.NavigationContract;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.ChwDBConstants;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.DBConstants;

import java.text.MessageFormat;
import java.util.Date;

import timber.log.Timber;

public class NavigationInteractor implements NavigationContract.Interactor {

    private static NavigationInteractor instance;
    private AppExecutors appExecutors = new AppExecutors();
    private CoreApplication coreApplication;

    private NavigationInteractor() {

    }

    public static NavigationInteractor getInstance() {
        if (instance == null) {
            instance = new NavigationInteractor();
        }

        return instance;
    }

    @Override
    public Date getLastSync() {
        return null;
    }

    @Override
    public void getRegisterCount(final String tableName,
                                 final NavigationContract.InteractorCallback<Integer> callback) {
        if (callback != null) {
            appExecutors.diskIO().execute(() -> {
                try {
                    final Integer finalCount = getCount(tableName);
                    appExecutors.mainThread().execute(() -> callback.onResult(finalCount));
                } catch (final Exception e) {
                    appExecutors.mainThread().execute(() -> callback.onError(e));
                }
            });

        }
    }

    private int getCount(String tableName) {
        int count;
        Cursor cursor = null;
        String mainCondition;
        if (tableName.equalsIgnoreCase(CoreConstants.TABLE_NAME.CHILD)) {
            mainCondition = String.format(" where %s is null AND %s", DBConstants.KEY.DATE_REMOVED, ChildDBConstants.childAgeLimitFilter());
        } else if (tableName.equalsIgnoreCase(CoreConstants.TABLE_NAME.FAMILY)) {
            mainCondition = String.format(" where %s is null ", DBConstants.KEY.DATE_REMOVED);
        } else if (tableName.equalsIgnoreCase(CoreConstants.TABLE_NAME.ANC_MEMBER)) {
            mainCondition = MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER) +
                    MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.BASE_ENTITY_ID,
                            CoreConstants.TABLE_NAME.ANC_MEMBER, DBConstants.KEY.BASE_ENTITY_ID) +
                    MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY) +
                    MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                            CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID) +
                    MessageFormat.format(" where {0}.{1} is null ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED) +
                    MessageFormat.format(" and {0}.{1} is 0 ", CoreConstants.TABLE_NAME.ANC_MEMBER, org.smartregister.chw.anc.util.DBConstants.KEY.IS_CLOSED);

        } else if (tableName.equalsIgnoreCase(CoreConstants.TABLE_NAME.TASK)) {
            mainCondition =
                    MessageFormat.format(" where {0}.{1} is \''READY'\' AND {0}.{2} is \''Referral'\' ", CoreConstants.TABLE_NAME.CHILD_REFERRAL, ChwDBConstants.TASK_STATUS, ChwDBConstants.TASK_CODE);
        } else if (tableName.equalsIgnoreCase(CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME)) {
            StringBuilder build = new StringBuilder();
            build.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER));
            build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.BASE_ENTITY_ID,
                    CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME, DBConstants.KEY.BASE_ENTITY_ID));

            build.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY));
            build.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                    CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID));

            build.append(MessageFormat.format(" where {0}.{1} is not null AND {0}.{2} is 0 ", CoreConstants.TABLE_NAME.ANC_PREGNANCY_OUTCOME, ChwDBConstants.DELIVERY_DATE, ChwDBConstants.IS_CLOSED));

            mainCondition = build.toString();
        } else if (tableName.equalsIgnoreCase(CoreConstants.TABLE_NAME.MALARIA_CONFIRMATION)) {
            StringBuilder stb = new StringBuilder();

            stb.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER));
            stb.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.BASE_ENTITY_ID,
                    CoreConstants.TABLE_NAME.MALARIA_CONFIRMATION, DBConstants.KEY.BASE_ENTITY_ID));

            stb.append(MessageFormat.format(" inner join {0} ", CoreConstants.TABLE_NAME.FAMILY));
            stb.append(MessageFormat.format(" on {0}.{1} = {2}.{3} ", CoreConstants.TABLE_NAME.FAMILY, DBConstants.KEY.BASE_ENTITY_ID,
                    CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.RELATIONAL_ID));

            stb.append(MessageFormat.format(" where {0}.{1} is null ", CoreConstants.TABLE_NAME.FAMILY_MEMBER, DBConstants.KEY.DATE_REMOVED));
            stb.append(MessageFormat.format(" and {0}.{1} = 1 ", CoreConstants.TABLE_NAME.MALARIA_CONFIRMATION, org.smartregister.chw.malaria.util.DBConstants.KEY.MALARIA));

            mainCondition = stb.toString();
        } else {
            mainCondition = " where 1 = 1 ";
        }

        try {
            SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder();
            String query = MessageFormat.format("select count(*) from {0} {1}", tableName, mainCondition);
            query = sqb.Endquery(query);
            Timber.i("2%s", query);

            cursor = commonRepository(tableName).rawCustomQueryForAdapter(query);
            count = cursor != null && cursor.moveToFirst() ? cursor.getInt(0) : 0;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    private CommonRepository commonRepository(String tableName) {
        return coreApplication.getContext().commonrepository(tableName);
    }

    @Override
    public Date sync() {
        Date res = null;
        try {
            res = new Date(getLastCheckTimeStamp());
        } catch (Exception e) {
            Timber.e(e.toString());
        }
        return res;
    }

    @Override
    public void setApplication(CoreApplication coreApplication) {
        this.coreApplication = coreApplication;
    }

    private Long getLastCheckTimeStamp() {
        return coreApplication.getEcSyncHelper().getLastCheckTimeStamp();
    }
}
