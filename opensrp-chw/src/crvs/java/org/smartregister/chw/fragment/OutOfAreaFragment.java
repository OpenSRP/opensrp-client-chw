package org.smartregister.chw.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.OutOfAreaChildActivity;
import org.smartregister.chw.activity.OutOfAreaChildUpdateFormActivity;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.contract.CoreChildRegisterFragmentContract;
import org.smartregister.chw.core.fragment.BaseChwRegisterFragment;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.model.CoreOutOfAreaFragmentModel;
import org.smartregister.chw.presenter.CoreDeadClientsFragmentPresenter;
import org.smartregister.chw.presenter.OutOfAreaChildFragmentPresenter;
import org.smartregister.chw.provider.OutOfAreaProvider;
import org.smartregister.chw.util.Constants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.fragment.NoMatchDialogFragment;
import org.smartregister.family.util.DBConstants;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseRegisterActivity;

import java.util.HashMap;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.chw.util.CrvsConstants.BASE_ENTITY_ID;

public class OutOfAreaFragment extends BaseChwRegisterFragment implements CoreChildRegisterFragmentContract.View {

    public static final String CLICK_VIEW_NORMAL = "click_view_normal";
    private static final String DUE_FILTER_TAG = "PRESSED";
    protected View view;
    protected View dueOnlyLayout;
    protected boolean dueFilterActive = false;

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new OutOfAreaChildFragmentPresenter(this, new CoreOutOfAreaFragmentModel(), viewConfigurationIdentifier);
    }

    @Override
    public void setUniqueID(String s) {
        if (getSearchView() != null) {
            getSearchView().setText(s);
        }
    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        //// TODO: 15/08/19
    }

    @Override
    protected void onResumption() {
        if (dueFilterActive && dueOnlyLayout != null) {
            dueFilter(dueOnlyLayout);
        } else {
            super.onResumption();
        }
    }

    @Override
    protected String getMainCondition() {
        return presenter().getMainCondition();
    }

    @Override
    protected String getDefaultSortQuery() {
        return presenter().getDefaultSortQuery();
    }

    @Override
    protected void startRegistration() {
        ((OutOfAreaChildActivity) getActivity()).startFormActivity(CoreConstants.JSON_FORM.getChildRegister(), null, "");
    }

    @Override
    protected void onViewClicked(View view) {
        if (getActivity() == null) {
            return;
        }

        if (view.getTag() != null && view.getTag(org.smartregister.chw.core.R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
            if (view.getTag() instanceof CommonPersonObjectClient) {
                goToChildDetailActivity((CommonPersonObjectClient) view.getTag(), false);
            }
        } else if (view.getId() == org.smartregister.chw.core.R.id.due_only_layout) {
            toggleFilterSelection(view);
        }
    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        if (!SyncStatusBroadcastReceiver.getInstance().isSyncing() && (FetchStatus.fetched.equals(fetchStatus) || FetchStatus.nothingFetched.equals(fetchStatus)) && dueFilterActive && dueOnlyLayout != null) {
            dueFilter(dueOnlyLayout);
            Utils.showShortToast(getActivity(), getString(org.smartregister.chw.core.R.string.sync_complete));
            refreshSyncProgressSpinner();
        } else {
            super.onSyncInProgress(fetchStatus);
        }
    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        if (!SyncStatusBroadcastReceiver.getInstance().isSyncing() && (FetchStatus.fetched.equals(fetchStatus) || FetchStatus.nothingFetched.equals(fetchStatus)) && (dueFilterActive && dueOnlyLayout != null)) {
            dueFilter(dueOnlyLayout);
            Utils.showShortToast(getActivity(), getString(org.smartregister.chw.core.R.string.sync_complete));
            refreshSyncProgressSpinner();
        } else {
            super.onSyncComplete(fetchStatus);
        }

        if (syncProgressBar != null) {
            syncProgressBar.setVisibility(View.GONE);
        }
        if (syncButton != null) {
            syncButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void refreshSyncProgressSpinner() {
        super.refreshSyncProgressSpinner();
        if (syncButton != null) {
            syncButton.setVisibility(View.GONE);
        }
    }

    public void goToChildDetailActivity(CommonPersonObjectClient patient,
                                        boolean launchDialog) {
        String entityId = org.smartregister.family.util.Utils.getValue(patient.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
        Intent intent = new Intent(getActivity(), OutOfAreaChildUpdateFormActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.ACTION, Constants.ACTION.START_REGISTRATION);
        intent.putExtra(BASE_ENTITY_ID, entityId);
        startActivity(intent);
    }

    public void toggleFilterSelection(View dueOnlyLayout) {
        if (dueOnlyLayout != null) {
            if (dueOnlyLayout.getTag() == null) {
                dueFilterActive = true;
                dueFilter(dueOnlyLayout);
            } else if (dueOnlyLayout.getTag().toString().equals(DUE_FILTER_TAG)) {
                dueFilterActive = false;
                normalFilter(dueOnlyLayout);
            }
        }
    }

    private void normalFilter(View dueOnlyLayout) {
        filter(searchText(), "", presenter().getMainCondition());
        dueOnlyLayout.setTag(null);
        switchViews(dueOnlyLayout, false);
    }

    protected String getDueFilterCondition() {
        return presenter().getDueFilterCondition();
    }

    private void dueFilter(View dueOnlyLayout) {
        filter(searchText(), "", getDueFilterCondition());
        dueOnlyLayout.setTag(DUE_FILTER_TAG);
        switchViews(dueOnlyLayout, true);
    }

    protected void filterAndSortExecute() {
        filterandSortExecute(countBundle());
    }

    protected void filter(String filterString, String joinTableString, String mainConditionString) {
        filters = filterString;
        joinTable = joinTableString;
        mainCondition = mainConditionString;
        filterAndSortExecute();
    }

    private String searchText() {
        return (getSearchView() == null) ? "" : getSearchView().getText().toString();
    }

    protected TextView getDueOnlyTextView(View dueOnlyLayout) {
        return dueOnlyLayout.findViewById(org.smartregister.chw.core.R.id.due_only_text_view);
    }

    private void switchViews(View dueOnlyLayout, boolean isPress) {
        if (isPress) {
            getDueOnlyTextView(dueOnlyLayout).setCompoundDrawablesWithIntrinsicBounds(0, 0, org.smartregister.chw.core.R.drawable.ic_due_filter_on, 0);
        } else {
            getDueOnlyTextView(dueOnlyLayout).setCompoundDrawablesWithIntrinsicBounds(0, 0, org.smartregister.chw.core.R.drawable.ic_due_filter_off, 0);

        }
    }

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        OutOfAreaProvider deadClientsProvider = new OutOfAreaProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, deadClientsProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    public CoreChildRegisterFragmentContract.Presenter presenter() {
        return (CoreChildRegisterFragmentContract.Presenter) presenter;
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);

        if (ChwApplication.getApplicationFlavor().hasDefaultDueFilterForChildClient()) {
            View dueOnlyLayout = view.findViewById(org.smartregister.chw.core.R.id.due_only_layout);
            dueOnlyLayout.setVisibility(View.VISIBLE);
            dueOnlyLayout.setOnClickListener(registerActionHandler);
            dueOnlyLayout.setTag(null);
            toggleFilterSelection(dueOnlyLayout);
        }
    }

    @Override
    protected int getToolBarTitle() {
        return R.string.out_of_area_registration;
    }

    @Override
    public void showNotFoundPopup(String uniqueId) {
        if (getActivity() == null) {
            return;
        }
        NoMatchDialogFragment.launchDialog((BaseRegisterActivity) getActivity(), DIALOG_TAG, uniqueId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID) {// Returns a new CursorLoader
            return new CursorLoader(getActivity()) {
                @Override
                public Cursor loadInBackground() {
                    // Count query
                    String query = filterandSortQuery();
                    return commonRepository().rawCustomQueryForAdapter(query);
                }
            };
        }// An invalid id was passed in
        return null;
    }


    @Override
    public void countExecute() {
        Cursor c = null;
        try {
            c = commonRepository().rawCustomQueryForAdapter(getCountSelect());
            c.moveToFirst();
            clientAdapter.setTotalcount(c.getInt(0));
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

    private String getCountSelect() {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(countSelect);

        String query = countSelect;
        try {
            if (StringUtils.isNoneBlank(filters)){
                query = "SELECT count(*) from ec_out_of_area_child";
            }else if (StringUtils.isBlank(filters) && !dueFilterActive){
                query = "SELECT count(*) from ec_out_of_area_child "+getFilters(filters);
            }

            if (dueFilterActive){
                query = "SELECT count(*) from ec_out_of_area_child";
            }

        } catch (Exception e) {
            Timber.e(e);
        }

        return query;
    }

    private String filterandSortQuery() {
        String query = "";
        try {
            if (StringUtils.isNoneBlank(filters)) {
//                query = customDeathQuery(clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset());
                query = customDeathQuery(clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset(), filters);
            } else if (StringUtils.isBlank(filters) && !dueFilterActive){
                query = customDeathQuery(clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset());
            }
            if (dueFilterActive){
                query = customDeathQuery(clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset());
//                query = customDeathDueFilterQuery(clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset(), ((CoreDeadClientsFragmentPresenter) presenter()).getDueCondition());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return query;
    }

    private String customDeathQuery(int limit, int offset) {
        return "Select ec_out_of_area_child.id as _id, ec_out_of_area_child.relationalid, ec_out_of_area_child.last_interacted_with, " +
                "ec_out_of_area_child.base_entity_id , ec_out_of_area_child.first_name, ec_out_of_area_child.middle_name, " +
                "ec_out_of_area_child.surname, ec_out_of_area_child.middle_name as family_middle_name, ec_out_of_area_child.unique_id, " +
                "ec_out_of_area_child.gender, ec_out_of_area_child.dob, ec_out_of_area_child.dob_unknown, ec_out_of_area_child.date_created, " +
                "ec_out_of_area_child.mother_name, ec_out_of_area_child.father_name " +
                "from ec_out_of_area_child ORDER BY ec_out_of_area_child.last_interacted_with DESC LIMIT " + offset + "," + limit;
    }

    private String customDeathQuery(int limit, int offset, String filter) {
        return "Select ec_out_of_area_child.id as _id, ec_out_of_area_child.relationalid, ec_out_of_area_child.last_interacted_with, " +
                "ec_out_of_area_child.base_entity_id , ec_out_of_area_child.first_name, ec_out_of_area_child.middle_name, " +
                "ec_out_of_area_child.surname, ec_out_of_area_child.middle_name as family_middle_name, ec_out_of_area_child.unique_id, " +
                "ec_out_of_area_child.gender, ec_out_of_area_child.dob, ec_out_of_area_child.dob_unknown, ec_out_of_area_child.date_created, " +
                "ec_out_of_area_child.mother_name, ec_out_of_area_child.father_name " +
                "from ec_out_of_area_child "+getFilters(filter)+" ORDER BY ec_out_of_area_child.last_interacted_with DESC LIMIT " + offset + "," + limit;
    }

    private String getFilters(String filter) {
        return "where ( ec_out_of_area_child.first_name like '%" + filter + "%'  or ec_out_of_area_child.middle_name like '%" + filter + "%' or ec_out_of_area_child.unique_id like '%" + filter + "%')";
    }
}