package org.smartregister.chw.fragment;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.dao.WashCheckDao;
import org.smartregister.chw.domain.FormDetails;
import org.smartregister.chw.model.FamilyProfileActivityModel;
import org.smartregister.chw.presenter.FamilyProfileActivityPresenter;
import org.smartregister.chw.provider.FamilyActivityRegisterProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.adapter.FamilyRecyclerViewCustomAdapter;
import org.smartregister.family.fragment.BaseFamilyProfileActivityFragment;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.Utils;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.chw.core.utils.CoreConstants.EventType.WASH_CHECK;


public class FamilyProfileActivityFragment extends BaseFamilyProfileActivityFragment {
    private String familyBaseEntityId;

    public static BaseFamilyProfileActivityFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        BaseFamilyProfileActivityFragment fragment = new FamilyProfileActivityFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        FamilyActivityRegisterProvider familyActivityRegisterProvider = new FamilyActivityRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new FamilyRecyclerViewCustomAdapter(null, familyActivityRegisterProvider, context().commonrepository(this.tablename), Utils.metadata().familyActivityRegister.showPagination);
        clientAdapter.setCurrentlimit(Utils.metadata().familyActivityRegister.currentLimit);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected void initializePresenter() {
        familyBaseEntityId = getArguments().getString(Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID);
        presenter = new FamilyProfileActivityPresenter(this, new FamilyProfileActivityModel(), null, familyBaseEntityId);
    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        //TODO
        Timber.d("setAdvancedSearchFormData");
    }

    @Override
    public void setupViews(android.view.View view) {
        super.setupViews(view);
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
                        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);
                        String query = sqb.orderbyCondition(Sortqueries);
                        query = sqb.Endquery(query);
                        return commonRepository().rawCustomQueryForAdapter(query);
                    }
                };
            default:
                // An invalid id was passed in
                return null;
        }
    }

    public void countExecute() {
        Cursor c = null;

        try {
            SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(countSelect);
            sqb.addCondition(filters);
            String query = sqb.orderbyCondition(Sortqueries);
            query = sqb.Endquery(query);

            Timber.i(getClass().getName(), query);
            c = commonRepository().rawCustomQueryForAdapter(query);
            c.moveToFirst();
            clientAdapter.setTotalcount(c.getInt(0));
            Timber.v("total count here %s", clientAdapter.getTotalcount());
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
    protected void onViewClicked(android.view.View view) {
        super.onViewClicked(view);
        switch (view.getId()) {
            case R.id.patient_column:
                if (view.getTag() != null && view.getTag(org.smartregister.family.R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
                    displayWashCheckHistory((CommonPersonObjectClient) view.getTag());
                }
                break;
            case R.id.next_arrow:
                if (view.getTag() != null && view.getTag(org.smartregister.family.R.id.VIEW_ID) == CLICK_VIEW_NEXT_ARROW) {
                    displayWashCheckHistory((CommonPersonObjectClient) view.getTag());
                }
                break;
            default:
                break;
        }
    }

    private void displayWashCheckHistory(CommonPersonObjectClient commonPersonObjectClient) {
        String type = commonPersonObjectClient.getColumnmaps().get("visit_type");
        Long visitDate = Long.parseLong(commonPersonObjectClient.getColumnmaps().get("visit_date"));

        if (WASH_CHECK.equalsIgnoreCase(type)) {
            if (ChwApplication.getApplicationFlavor().launchWashCheckOnNativeForm()) {
                startForm(org.smartregister.chw.util.JsonFormUtils.REQUEST_CODE_GET_JSON_WASH, visitDate);
            } else {
                WashCheckDialogFragment dialogFragment = WashCheckDialogFragment.getInstance(familyBaseEntityId, visitDate);
                FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
                dialogFragment.show(ft, WashCheckDialogFragment.DIALOG_TAG);
            }

        } else if (CoreConstants.EventType.ROUTINE_HOUSEHOLD_VISIT.equalsIgnoreCase(type)) {

            FormDetails formDetails = new FormDetails();
            formDetails.setTitle(getString(R.string.routine_household_visit));
            formDetails.setBaseEntityID(familyBaseEntityId);
            formDetails.setEventDate(visitDate);
            formDetails.setEventType(org.smartregister.chw.util.Constants.EventType.ROUTINE_HOUSEHOLD_VISIT);
            formDetails.setFormName(org.smartregister.chw.util.Constants.JSON_FORM.getRoutineHouseholdVisit());

            FormHistoryDialogFragment dialogFragment = FormHistoryDialogFragment.getInstance(formDetails);
            FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
            dialogFragment.show(ft, FormHistoryDialogFragment.DIALOG_TAG);
        } else if (CoreConstants.EventType.FAMILY_KIT.equalsIgnoreCase(type)) {
            FamilyKitDialogFragment dialogFragment = FamilyKitDialogFragment.getInstance(familyBaseEntityId, visitDate);
            FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
            dialogFragment.show(ft, FamilyKitDialogFragment.DIALOG_TAG);
        }
    }

    private void startForm(int requestCode, Long visitDate) {
        try {
            JSONObject jsonForm = populateWashCheckFields(visitDate);
            jsonForm.put(JsonFormUtils.ENTITY_ID, familyBaseEntityId);
            Intent intent = new Intent(getActivity(), Utils.metadata().familyMemberFormActivity);
            intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

            Form form = new Form();
            form.setWizard(false);
            form.setActionBarBackground(R.color.family_actionbar);

            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
            intent.putExtra(Constants.WizardFormActivity.EnableOnCloseDialog, true);
            if (getActivity() != null) {
                getActivity().startActivityForResult(intent, requestCode);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private JSONObject populateWashCheckFields(Long visitDate) throws Exception {
        JSONObject jsonForm = FormUtils.getInstance(getActivity()).getFormJson(org.smartregister.chw.util.Constants.JSON_FORM.getWashCheck());
        Map<String, VisitDetail> washData = WashCheckDao.getWashCheckDetails(visitDate, familyBaseEntityId);
        JSONArray jsonArray = jsonForm.getJSONObject("step1").getJSONArray("fields");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String key = jsonObject.getString(JsonFormUtils.KEY);
            if (washData.containsKey(key)) {
                String value = washData.get(key).getHumanReadable();
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, value);
            }
        }
        return jsonForm;
    }

}
