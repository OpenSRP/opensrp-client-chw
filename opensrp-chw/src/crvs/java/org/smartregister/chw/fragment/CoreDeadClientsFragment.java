package org.smartregister.chw.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.CoreDeadClientsActivity;
import org.smartregister.chw.core.activity.CoreChildProfileActivity;
import org.smartregister.chw.core.contract.CoreChildRegisterFragmentContract;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.fragment.BaseChwRegisterFragment;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.model.CoreDeadClientsFragmentModel;
import org.smartregister.chw.presenter.CoreDeadClientsFragmentPresenter;
import org.smartregister.chw.provider.CoreDeadClientsProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.FetchStatus;
import org.smartregister.family.fragment.NoMatchDialogFragment;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseRegisterActivity;
import java.util.HashMap;
import java.util.Set;
import timber.log.Timber;

public class CoreDeadClientsFragment extends BaseChwRegisterFragment implements CoreChildRegisterFragmentContract.View {

    public static final String CLICK_VIEW_NORMAL = "click_view_normal";
    public static final String CLICK_VIEW_DOSAGE_STATUS = "click_view_dosage_status";
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
        presenter = new CoreDeadClientsFragmentPresenter(this, new CoreDeadClientsFragmentModel(), viewConfigurationIdentifier);

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
        ((CoreDeadClientsActivity) getActivity()).startFormActivity(CoreConstants.JSON_FORM.getChildRegister(), null, "");
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
    public void onResume() {
        super.onResume();
        Toolbar toolbar = view.findViewById(org.smartregister.chw.core.R.id.register_toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);
        toolbar.setContentInsetsRelative(0, 0);
        toolbar.setContentInsetStartWithNavigation(0);
        NavigationMenu.getInstance(getActivity(), null, toolbar);
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
        if (launchDialog) {
            Timber.i(patient.name);
        }

        Intent intent = new Intent(getActivity(), CoreChildProfileActivity.class);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
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
        CoreDeadClientsProvider deathClientsProvider = new CoreDeadClientsProvider(getActivity(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, deathClientsProvider, context().commonrepository(this.tablename));
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
        this.view = view;

        dueOnlyLayout = view.findViewById(org.smartregister.chw.core.R.id.due_only_layout);
        dueOnlyLayout.setVisibility(View.VISIBLE);
        dueOnlyLayout.setOnClickListener(registerActionHandler);
    }

    @Override
    protected int getToolBarTitle() {
        return R.string.death_certification;
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
        Cursor c2 = null;
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
                query = "Select (SELECT count(*) from ec_family_member LEFT JOIN ec_family ON ec_family_member.base_entity_id = ec_family.primary_caregiver COLLATE NOCASE WHERE ec_family_member.is_closed = 1) + (Select count(*) FROM ec_child LEFT JOIN ec_family ON  ec_child.relational_id = ec_family.id COLLATE NOCASE LEFT JOIN ec_family_member ON  ec_family_member.base_entity_id = ec_family.primary_caregiver COLLATE NOCASE  LEFT JOIN (select base_entity_id , max(visit_date) visit_date from visits GROUP by base_entity_id) VISIT_SUMMARY ON VISIT_SUMMARY.base_entity_id = ec_child.base_entity_id WHERE  ec_child.is_closed is 1) + (Select count(*) from ec_pregnancy_outcome LEFT JOIN ec_family_member ON ec_pregnancy_outcome.base_entity_id = ec_family_member.base_entity_id LEFT JOIN ec_family ON  ec_pregnancy_outcome.relational_id = ec_family.id COLLATE NOCASE WHERE ec_pregnancy_outcome.preg_outcome = 'Stillbirth' "+filters+") as sumcount";
            }else if (StringUtils.isBlank(filters) && !dueFilterActive){
                query = "Select (SELECT count(*) from ec_family_member LEFT JOIN ec_family ON ec_family_member.base_entity_id = ec_family.primary_caregiver COLLATE NOCASE WHERE ec_family_member.is_closed = 1) + (Select count(*) FROM ec_child LEFT JOIN ec_family ON  ec_child.relational_id = ec_family.id COLLATE NOCASE LEFT JOIN ec_family_member ON  ec_family_member.base_entity_id = ec_family.primary_caregiver COLLATE NOCASE  LEFT JOIN (select base_entity_id , max(visit_date) visit_date from visits GROUP by base_entity_id) VISIT_SUMMARY ON VISIT_SUMMARY.base_entity_id = ec_child.base_entity_id WHERE  ec_child.is_closed is 1) + (Select count(*) from ec_pregnancy_outcome LEFT JOIN ec_family_member ON ec_pregnancy_outcome.base_entity_id = ec_family_member.base_entity_id LEFT JOIN ec_family ON  ec_pregnancy_outcome.relational_id = ec_family.id COLLATE NOCASE WHERE ec_pregnancy_outcome.preg_outcome = 'Stillbirth') as sumcount";
            }

            if (dueFilterActive){
                query = "Select (SELECT count(*) from ec_family_member LEFT JOIN ec_family ON ec_family_member.base_entity_id = ec_family.primary_caregiver COLLATE NOCASE WHERE ec_family_member.is_closed = 1) + (Select count(*) FROM ec_child LEFT JOIN ec_family ON  ec_child.relational_id = ec_family.id COLLATE NOCASE LEFT JOIN ec_family_member ON  ec_family_member.base_entity_id = ec_family.primary_caregiver COLLATE NOCASE  LEFT JOIN (select base_entity_id , max(visit_date) visit_date from visits GROUP by base_entity_id) VISIT_SUMMARY ON VISIT_SUMMARY.base_entity_id = ec_child.base_entity_id WHERE  ec_child.is_closed is 1) + (Select count(*) from ec_pregnancy_outcome LEFT JOIN ec_family_member ON ec_pregnancy_outcome.base_entity_id = ec_family_member.base_entity_id LEFT JOIN ec_family ON  ec_pregnancy_outcome.relational_id = ec_family.id COLLATE NOCASE WHERE ec_pregnancy_outcome.preg_outcome = 'Stillbirth' "+dueFilterActive+") as sumcount";
            }

        } catch (Exception e) {
            Timber.e(e);
        }

        return query;
    }

    private String filterandSortQuery() {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);

        String query = "";
        try {
            if (StringUtils.isNoneBlank(filters)) {
                query = customDeathQuery(clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset(), filters);
            } else if (StringUtils.isBlank(filters) && !dueFilterActive){
                query = customDeathQuery(clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset());
            }
            if (dueFilterActive){
                query = customDeathDueFilterQuery(clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset(), ((CoreDeadClientsFragmentPresenter) presenter()).getDueCondition());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return query;
    }

    private String customDeathQuery(int limit, int offset) {
        return "Select ec_family_member.id as _id , ec_family_member.relational_id as relationalid , 'a' as last_interacted_with , ec_family_member.base_entity_id , 'a' as first_name , 'a' as middle_name , ec_family_member.first_name as family_first_name , ec_family_member.last_name as family_last_name , ec_family_member.middle_name as family_middle_name , ec_family_member.phone_number as family_member_phone_number , ec_family_member.other_phone_number as family_member_phone_number_other , ec_family.village_town as family_home_address ,'a' as last_name, ec_family_member.unique_id, ec_family_member.gender, ec_family_member.dob, ec_family_member.dob_unknown, 'a' as last_home_visit, 'a' as visit_not_done, 'a' as early_bf_1hr, 'a' as physically_challenged, 'a' as birth_cert, 'a' as birth_cert_issue_date, 'a' as birth_cert_num, 'a' as birth_notification, 'a' as date_of_illness, 'a' as illness_description, 'a' as date_created, 'a' as action_taken, 'a' as vaccine_card, '' as preg_outcome, ec_family_member.received_death_certificate, ec_family_member.death_certificate_issue_date, 'adult' as clienttype from ec_family_member LEFT JOIN ec_family ON ec_family_member.base_entity_id = ec_family.primary_caregiver COLLATE NOCASE WHERE ec_family_member.is_closed = 1 UNION Select ec_child.id as _id , ec_child.relational_id as relationalid , ec_child.last_interacted_with , ec_child.base_entity_id , ec_child.first_name , ec_child.middle_name , ec_family_member.first_name as family_first_name , ec_family_member.last_name as family_last_name , ec_family_member.middle_name as family_middle_name , ec_family_member.phone_number as family_member_phone_number , ec_family_member.other_phone_number as family_member_phone_number_other , ec_family.village_town as family_home_address , ec_child.last_name , ec_child.unique_id , ec_child.gender , ec_child.dob , ec_child.dob_unknown , ec_child.last_home_visit , ec_child.visit_not_done , ec_child.early_bf_1hr , ec_child.physically_challenged , ec_child.birth_cert , ec_child.birth_cert_issue_date , ec_child.birth_cert_num , ec_child.birth_notification , ec_child.date_of_illness , ec_child.illness_description , ec_child.date_created , ec_child.action_taken , ec_child.vaccine_card, '' as preg_outcome, ec_child.received_death_certificate, ec_child.death_certificate_issue_date, 'child' as clienttype FROM ec_child LEFT JOIN ec_family ON  ec_child.relational_id = ec_family.id COLLATE NOCASE LEFT JOIN ec_family_member ON  ec_family_member.base_entity_id = ec_family.primary_caregiver COLLATE NOCASE  LEFT JOIN (select base_entity_id , max(visit_date) visit_date from visits GROUP by base_entity_id) VISIT_SUMMARY ON VISIT_SUMMARY.base_entity_id = ec_child.base_entity_id WHERE  ec_child.is_closed is 1 UNION Select ec_pregnancy_outcome.id as _id, ec_pregnancy_outcome.relational_id as relationalid, 'a' as last_interacted_with, ec_pregnancy_outcome.base_entity_id, 'a' as first_name, 'a' as middle_name, ec_family_member.first_name as family_first_name , ec_family_member.last_name as family_last_name , ec_family_member.middle_name as family_middle_name , ec_family_member.phone_number as family_member_phone_number , ec_family_member.other_phone_number as family_member_phone_number_other , ec_family.village_town as family_home_address ,'a' as last_name, ec_family_member.unique_id, ec_family_member.gender, ec_family_member.dob, ec_family_member.dob_unknown, 'a' as last_home_visit, 'a' as visit_not_done, 'a' as early_bf_1hr, 'a' as physically_challenged, 'a' as birth_cert, 'a' as birth_cert_issue_date, 'a' as birth_cert_num, 'a' as birth_notification, 'a' as date_of_illness, 'a' as illness_description, 'a' as date_created, 'a' as action_taken, 'a' as vaccine_card, ec_pregnancy_outcome.preg_outcome, ec_family_member.received_death_certificate, ec_family_member.death_certificate_issue_date, 'still' as clienttype from ec_pregnancy_outcome LEFT JOIN ec_family_member ON ec_pregnancy_outcome.base_entity_id = ec_family_member.base_entity_id LEFT JOIN ec_family ON  ec_pregnancy_outcome.relational_id = ec_family.id COLLATE NOCASE WHERE ec_pregnancy_outcome.preg_outcome = 'Stillbirth' LIMIT " + offset + "," + limit;
    }

    private String customDeathDueFilterQuery(int limit, int offset, String dueFilterActive) {
        return "Select ec_family_member.id as _id , ec_family_member.relational_id as relationalid , 'a' as last_interacted_with , ec_family_member.base_entity_id , 'a' as first_name , 'a' as middle_name , ec_family_member.first_name as family_first_name , ec_family_member.last_name as family_last_name , ec_family_member.middle_name as family_middle_name , ec_family_member.phone_number as family_member_phone_number , ec_family_member.other_phone_number as family_member_phone_number_other , ec_family.village_town as family_home_address ,'a' as last_name, ec_family_member.unique_id, ec_family_member.gender, ec_family_member.dob, ec_family_member.dob_unknown, 'a' as last_home_visit, 'a' as visit_not_done, 'a' as early_bf_1hr, 'a' as physically_challenged, 'a' as birth_cert, 'a' as birth_cert_issue_date, 'a' as birth_cert_num, 'a' as birth_notification, 'a' as date_of_illness, 'a' as illness_description, 'a' as date_created, 'a' as action_taken, 'a' as vaccine_card, '' as preg_outcome, ec_family_member.received_death_certificate, ec_family_member.death_certificate_issue_date, 'adult' as clienttype from ec_family_member LEFT JOIN ec_family ON ec_family_member.base_entity_id = ec_family.primary_caregiver COLLATE NOCASE WHERE ec_family_member.is_closed = 1 UNION Select ec_child.id as _id , ec_child.relational_id as relationalid , ec_child.last_interacted_with , ec_child.base_entity_id , ec_child.first_name , ec_child.middle_name , ec_family_member.first_name as family_first_name , ec_family_member.last_name as family_last_name , ec_family_member.middle_name as family_middle_name , ec_family_member.phone_number as family_member_phone_number , ec_family_member.other_phone_number as family_member_phone_number_other , ec_family.village_town as family_home_address , ec_child.last_name , ec_child.unique_id , ec_child.gender , ec_child.dob , ec_child.dob_unknown , ec_child.last_home_visit , ec_child.visit_not_done , ec_child.early_bf_1hr , ec_child.physically_challenged , ec_child.birth_cert , ec_child.birth_cert_issue_date , ec_child.birth_cert_num , ec_child.birth_notification , ec_child.date_of_illness , ec_child.illness_description , ec_child.date_created , ec_child.action_taken , ec_child.vaccine_card, '' as preg_outcome, ec_child.received_death_certificate, ec_child.death_certificate_issue_date, 'child' as clienttype FROM ec_child LEFT JOIN ec_family ON  ec_child.relational_id = ec_family.id COLLATE NOCASE LEFT JOIN ec_family_member ON  ec_family_member.base_entity_id = ec_family.primary_caregiver COLLATE NOCASE  LEFT JOIN (select base_entity_id , max(visit_date) visit_date from visits GROUP by base_entity_id) VISIT_SUMMARY ON VISIT_SUMMARY.base_entity_id = ec_child.base_entity_id WHERE  ec_child.is_closed is 1 UNION Select ec_pregnancy_outcome.id as _id, ec_pregnancy_outcome.relational_id as relationalid, 'a' as last_interacted_with, ec_pregnancy_outcome.base_entity_id, 'a' as first_name, 'a' as middle_name, ec_family_member.first_name as family_first_name , ec_family_member.last_name as family_last_name , ec_family_member.middle_name as family_middle_name , ec_family_member.phone_number as family_member_phone_number , ec_family_member.other_phone_number as family_member_phone_number_other , ec_family.village_town as family_home_address ,'a' as last_name, ec_family_member.unique_id, ec_family_member.gender, ec_family_member.dob, ec_family_member.dob_unknown, 'a' as last_home_visit, 'a' as visit_not_done, 'a' as early_bf_1hr, 'a' as physically_challenged, 'a' as birth_cert, 'a' as birth_cert_issue_date, 'a' as birth_cert_num, 'a' as birth_notification, 'a' as date_of_illness, 'a' as illness_description, 'a' as date_created, 'a' as action_taken, 'a' as vaccine_card, ec_pregnancy_outcome.preg_outcome, ec_family_member.received_death_certificate, ec_family_member.death_certificate_issue_date, 'still' as clienttype from ec_pregnancy_outcome LEFT JOIN ec_family_member ON ec_pregnancy_outcome.base_entity_id = ec_family_member.base_entity_id LEFT JOIN ec_family ON  ec_pregnancy_outcome.relational_id = ec_family.id COLLATE NOCASE WHERE ec_pregnancy_outcome.preg_outcome = 'Stillbirth' "+dueFilterActive+" LIMIT " + offset + "," + limit;
    }

    private String customDeathQuery(int limit, int offset, String filter) {
        return "Select ec_family_member.id as _id , ec_family_member.relational_id as relationalid , 'a' as last_interacted_with , ec_family_member.base_entity_id , 'a' as first_name , 'a' as middle_name , ec_family_member.first_name as family_first_name , ec_family_member.last_name as family_last_name , ec_family_member.middle_name as family_middle_name , ec_family_member.phone_number as family_member_phone_number , ec_family_member.other_phone_number as family_member_phone_number_other , ec_family.village_town as family_home_address ,'a' as last_name, ec_family_member.unique_id, ec_family_member.gender, ec_family_member.dob, ec_family_member.dob_unknown, 'a' as last_home_visit, 'a' as visit_not_done, 'a' as early_bf_1hr, 'a' as physically_challenged, 'a' as birth_cert, 'a' as birth_cert_issue_date, 'a' as birth_cert_num, 'a' as birth_notification, 'a' as date_of_illness, 'a' as illness_description, 'a' as date_created, 'a' as action_taken, 'a' as vaccine_card, '' as preg_outcome, ec_family_member.received_death_certificate, ec_family_member.death_certificate_issue_date, 'adult' as clienttype from ec_family_member LEFT JOIN ec_family ON ec_family_member.base_entity_id = ec_family.primary_caregiver COLLATE NOCASE WHERE ec_family_member.is_closed = 1                " + getFilters("first", filter) + "                UNION Select ec_child.id as _id , ec_child.relational_id as relationalid , ec_child.last_interacted_with , ec_child.base_entity_id , ec_child.first_name , ec_child.middle_name , ec_family_member.first_name as family_first_name , ec_family_member.last_name as family_last_name , ec_family_member.middle_name as family_middle_name , ec_family_member.phone_number as family_member_phone_number , ec_family_member.other_phone_number as family_member_phone_number_other , ec_family.village_town as family_home_address , ec_child.last_name , ec_child.unique_id , ec_child.gender , ec_child.dob , ec_child.dob_unknown , ec_child.last_home_visit , ec_child.visit_not_done , ec_child.early_bf_1hr , ec_child.physically_challenged , ec_child.birth_cert , ec_child.birth_cert_issue_date , ec_child.birth_cert_num , ec_child.birth_notification , ec_child.date_of_illness , ec_child.illness_description , ec_child.date_created , ec_child.action_taken , ec_child.vaccine_card, '' as preg_outcome, ec_child.received_death_certificate, ec_child.death_certificate_issue_date, 'child' as clienttype FROM ec_child LEFT JOIN ec_family ON  ec_child.relational_id = ec_family.id COLLATE NOCASE LEFT JOIN ec_family_member ON  ec_family_member.base_entity_id = ec_family.primary_caregiver COLLATE NOCASE  LEFT JOIN (select base_entity_id , max(visit_date) visit_date from visits GROUP by base_entity_id) VISIT_SUMMARY ON VISIT_SUMMARY.base_entity_id = ec_child.base_entity_id WHERE ec_child.is_closed is 1                " + getFilters("second", filter) + "                UNION Select ec_pregnancy_outcome.id as _id, ec_pregnancy_outcome.relational_id as relationalid, 'a' as last_interacted_with, ec_pregnancy_outcome.base_entity_id, 'a' as first_name, 'a' as middle_name, ec_family_member.first_name as family_first_name , ec_family_member.last_name as family_last_name , ec_family_member.middle_name as family_middle_name , ec_family_member.phone_number as family_member_phone_number , ec_family_member.other_phone_number as family_member_phone_number_other , ec_family.village_town as family_home_address ,'a' as last_name, ec_family_member.unique_id, ec_family_member.gender, ec_family_member.dob, ec_family_member.dob_unknown, 'a' as last_home_visit, 'a' as visit_not_done, 'a' as early_bf_1hr, 'a' as physically_challenged, 'a' as birth_cert, 'a' as birth_cert_issue_date, 'a' as birth_cert_num, 'a' as birth_notification, 'a' as date_of_illness, 'a' as illness_description, 'a' as date_created, 'a' as action_taken, 'a' as vaccine_card, ec_pregnancy_outcome.preg_outcome, ec_family_member.received_death_certificate, ec_family_member.death_certificate_issue_date, 'still' as clienttype from ec_pregnancy_outcome LEFT JOIN ec_family_member ON ec_pregnancy_outcome.base_entity_id = ec_family_member.base_entity_id LEFT JOIN ec_family ON  ec_pregnancy_outcome.relational_id = ec_family.id COLLATE NOCASE WHERE ec_pregnancy_outcome.preg_outcome = 'Stillbirth'                " + getFilters("third", filter) + "                 LIMIT " + offset + "," + limit;
    }

    private String getFilters(String query, String filter) {
        String filterQuery = "";
        if (query.equals("first")) {
            filterQuery = "and (  ec_family_member.first_name like '%" + filter + "%'  or ec_family_member.last_name like '%" + filter + "%'  or ec_family_member.middle_name like '%" + filter + "%'  or ec_family_member.unique_id like '%" + filter + "%')";
        } else if (query.equals("second")) {
            filterQuery = "and (  ec_child.first_name like '%" + filter + "%'  or ec_family_member.last_name like '%" + filter + "%'  or ec_child.middle_name like '%" + filter + "%'  or ec_child.unique_id like '%" + filter + "%')";
        } else if (query.equals("third")) {
            filterQuery = "and (  ec_family_member.first_name like '%" + filter + "%'  or ec_family_member.last_name like '%" + filter + "%'  or ec_family_member.middle_name like '%" + filter + "%'  or ec_family_member.unique_id like '%" + filter + "%')";
        }
        return filterQuery;
    }
}