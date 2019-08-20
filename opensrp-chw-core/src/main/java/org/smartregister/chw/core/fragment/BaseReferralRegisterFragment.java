package org.smartregister.chw.core.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;

import org.smartregister.chw.core.R;
import org.smartregister.chw.core.contract.BaseReferralRegisterFragmentContract;
import org.smartregister.chw.core.provider.BasereferralRegisterProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.commonregistry.CommonFtsObject.searchTableName;

public abstract class BaseReferralRegisterFragment extends BaseChwRegisterFragment implements BaseReferralRegisterFragmentContract.View {

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns, String tableName) {
        BasereferralRegisterProvider registerProvider = new BasereferralRegisterProvider(getActivity(), registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, registerProvider, context().commonrepository(tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }


    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        View topRightLayout = view.findViewById(R.id.top_right_layout);
        topRightLayout.setVisibility(View.GONE);
    }

    @Override
    protected int getToolBarTitle() {
        return R.string.menu_referrals;
    }

    @Override
    public void setUniqueID(String s) {
        //// TODO: 15/08/19  
    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        //// TODO: 15/08/19  
    }

    @Override
    protected String getMainCondition() {
        return "task.status = '" + Task.TaskStatus.READY.name() + "'";
    }

    @Override
    protected String getDefaultSortQuery() {
        return "";
    }

    @Override
    protected void startRegistration() {
        //// TODO: 15/08/19  
    }

    @Override
    public void showNotFoundPopup(String s) {
        //// TODO: 15/08/19  
    }

    @Override
    public void countExecute() {
        Cursor c = null;

        try {
            SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(countSelect);
            String query;
            if (isValidFilterForFts(commonRepository())) {
                String sql = sqb.countQueryFts(tablename, joinTable, mainCondition, filters);
                sql = sql.replace("WHERE", String.format("JOIN %s ON task.%s = %s.%s WHERE", CoreConstants.TABLE_NAME.TASK, CoreConstants.DB_CONSTANTS.FOR, searchTableName(tablename), CommonFtsObject.idColumn));
                Timber.i("FTS query %s", sql);

                clientAdapter.setTotalcount(commonRepository().countSearchIds(sql));
                Timber.v("total count here %s", clientAdapter.getTotalcount());


            } else {
                sqb.addCondition(filters);
                query = sqb.orderbyCondition(Sortqueries);
                query = sqb.Endquery(query);

                Timber.i(query);
                c = commonRepository().rawCustomQueryForAdapter(query);
                c.moveToFirst();
                clientAdapter.setTotalcount(c.getInt(0));
                Timber.v("total count here %s", clientAdapter.getTotalcount());
            }

            clientAdapter.setCurrentlimit(20);
            clientAdapter.setCurrentoffset(0);


        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        switch (id) {
            case LOADER_ID:
                // Returns a new CursorLoader
                return new CursorLoader(getActivity()) {
                    @Override
                    public Cursor loadInBackground() {
                        // Count query
                        if (args != null && args.getBoolean("count_execute")) {
                            countExecute();
                        }
                        String query = "";
                        // Select register query


                        query = filterandSortQuery();

                        return commonRepository().rawCustomQueryForAdapter(query);
                    }
                };
            default:
                // An invalid id was passed in
                return null;
        }

    }


    private String filterandSortQuery() {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);

        String query = "";
        try {
            if (isValidFilterForFts(commonRepository())) {
                String sql = sqb
                        .searchQueryFts(tablename, joinTable, mainCondition, filters, Sortqueries,
                                clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset());
                sql = sql.replace("WHERE", String.format("JOIN %s ON task.%s = %s.%s WHERE", CoreConstants.TABLE_NAME.TASK, CoreConstants.DB_CONSTANTS.FOR, searchTableName(tablename), CommonFtsObject.idColumn));
                Timber.i("FTS query %s", sql);

                List<String> ids = commonRepository().findSearchIds(sql);
                query = sqb.toStringFts(ids, tablename, CommonRepository.ID_COLUMN,
                        Sortqueries);
                query = query.concat(String.format(" ORDER BY %s.%s desc", CoreConstants.TABLE_NAME.TASK, CoreConstants.DB_CONSTANTS.START));
                query = sqb.Endquery(query);
            } else {
                sqb.addCondition(filters);
                query = sqb.orderbyCondition(Sortqueries);
                query = sqb.Endquery(sqb.addlimitandOffset(query, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset()));

            }
        } catch (Exception e) {
            Log.e(getClass().getName(), e.toString(), e);
        }

        return query;
    }


}
