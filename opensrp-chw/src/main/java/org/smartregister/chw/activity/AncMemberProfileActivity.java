package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.activity.BaseAncMemberProfileActivity;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.Util;
import org.smartregister.chw.interactor.FamilyProfileInteractor;
import org.smartregister.chw.model.FamilyProfileModel;
import org.smartregister.chw.presenter.AncMemberProfilePresenter;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.repository.AllSharedPreferences;

import timber.log.Timber;

import static org.smartregister.util.Utils.getAllSharedPreferences;

public class AncMemberProfileActivity extends BaseAncMemberProfileActivity {
    private static String baseEntityId;
    private static String familyBaseEntityId;
    private static String familyHead;
    private static String primaryCareGiver;
    private static String familyName;
    private static FamilyProfileContract.Interactor profileInteractor;
    private static FamilyProfileContract.Model profileModel;

    public static void startMe(Activity activity, MemberObject memberObject) {
        baseEntityId = memberObject.getBaseEntityId();
        familyBaseEntityId = memberObject.getFamilyBaseEntityId();
        familyHead = memberObject.getFamilyHead();
        primaryCareGiver = memberObject.getPrimaryCareGiver();
        familyName = memberObject.getFamilyName();
        Intent intent = new Intent(activity, AncMemberProfileActivity.class);
        intent.putExtra(Constants.ANC_MEMBER_OBJECTS.MEMBER_PROFILE_OBJECT, memberObject);
        profileInteractor = new FamilyProfileInteractor();
        profileModel = new FamilyProfileModel(memberObject.getFamilyName());
        activity.startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.anc_member_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_anc_member_registration:
                startFormForEdit(R.string.edit_member_form_title,
                        org.smartregister.chw.util.Constants.JSON_FORM.FAMILY_MEMBER_REGISTER);
                return true;
            case R.id.action_anc_registration:
                startFormForEdit(R.string.edit_anc_registration_form_title,
                        org.smartregister.chw.util.Constants.JSON_FORM.ANC_REGISTRATION);
                return true;
            case R.id.action_remove_member:

                CommonRepository commonRepository = org.smartregister.chw.util.Utils.context().commonrepository(org.smartregister.chw.util.Utils.metadata().familyMemberRegister.tableName);

                final CommonPersonObject commonPersonObject = commonRepository.findByBaseEntityId(baseEntityId);
                final CommonPersonObjectClient client =
                        new CommonPersonObjectClient(commonPersonObject.getCaseId(), commonPersonObject.getDetails(), "");
                client.setColumnmaps(commonPersonObject.getColumnmaps());

                IndividualProfileRemoveActivity.startIndividualProfileActivity(AncMemberProfileActivity.this, client, familyBaseEntityId, familyHead, primaryCareGiver);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void startFormForEdit(Integer title_resource, String formName) {

        CommonRepository commonRepository = org.smartregister.chw.util.Utils.context().commonrepository(org.smartregister.chw.util.Utils.metadata().familyMemberRegister.tableName);

        final CommonPersonObject personObject = commonRepository.findByBaseEntityId(baseEntityId);
        final CommonPersonObjectClient client =
                new CommonPersonObjectClient(personObject.getCaseId(), personObject.getDetails(), "");
        client.setColumnmaps(personObject.getColumnmaps());

        JSONObject form = null;

        if (formName.equals(org.smartregister.chw.util.Constants.JSON_FORM.FAMILY_MEMBER_REGISTER)) {
            form = org.smartregister.chw.util.JsonFormUtils.getAutoPopulatedJsonEditMemberFormString(
                    (title_resource != null) ? getResources().getString(title_resource) : null,
                    org.smartregister.chw.util.Constants.JSON_FORM.FAMILY_MEMBER_REGISTER,
                    this, client, org.smartregister.chw.util.Utils.metadata().familyMemberRegister.updateEventType, familyName, false);
        } else if (formName.equals(org.smartregister.chw.util.Constants.JSON_FORM.ANC_REGISTRATION)) {
            form = org.smartregister.chw.util.JsonFormUtils.getAutoJsonEditAncFormString(
                    baseEntityId, this, formName, org.smartregister.chw.util.Constants.EventType.UPDATE_ANC_REGISTRATION, getResources().getString(title_resource));
        }

        try {
            startFormActivity(form);
        } catch (Exception e) {
            Timber.e(e.getMessage());
        }
    }

    public void startFormActivity(JSONObject jsonForm) {

        Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
        intent.putExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());


        Form form = new Form();
        form.setActionBarBackground(R.color.family_actionbar);
        form.setWizard(false);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);


        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    public AncMemberProfilePresenter ancMemberProfilePresenter() {
        return new AncMemberProfilePresenter(this, MEMBER_OBJECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case org.smartregister.chw.util.Constants.ProfileActivityResults.CHANGE_COMPLETED:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(AncMemberProfileActivity.this, AncRegisterActivity.class);
                    intent.putExtras(getIntent().getExtras());
                    startActivity(intent);
                    finish();
                }
                break;
            case JsonFormUtils.REQUEST_CODE_GET_JSON:
                if (resultCode == RESULT_OK) {
                    try {
                        String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                        JSONObject form = new JSONObject(jsonString);
                        if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyMemberRegister.updateEventType)) {
                            FamilyEventClient familyEventClient = profileModel.processUpdateMemberRegistration(jsonString, baseEntityId);
                            profileInteractor.saveRegistration(familyEventClient, jsonString, true, ancMemberProfilePresenter());
                        } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(org.smartregister.chw.util.Constants.EventType.UPDATE_ANC_REGISTRATION)) {
                            AllSharedPreferences allSharedPreferences = getAllSharedPreferences();
                            Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, jsonString);
                            Util.processEvent(allSharedPreferences, baseEvent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;

        }
    }


}
