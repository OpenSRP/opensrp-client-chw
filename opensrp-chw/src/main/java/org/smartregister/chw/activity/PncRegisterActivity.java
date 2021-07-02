package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import org.apache.commons.lang3.EnumUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.activity.CoreFamilyRegisterActivity;
import org.smartregister.chw.core.activity.CorePncRegisterActivity;
import org.smartregister.chw.core.task.RunnableTask;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.fragment.PncRegisterFragment;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.view.fragment.BaseRegisterFragment;
import java.util.Date;
import timber.log.Timber;

public class PncRegisterActivity extends CorePncRegisterActivity {

    private boolean closeOnCancel = false;

    public static void startPncRegistrationActivity(Activity activity, String memberBaseEntityID, String phoneNumber, String formName,
                                                    String uniqueId, String familyBaseID, String family_name, String last_menstrual_period) {
        Intent intent = new Intent(activity, PncRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, memberBaseEntityID);

        intent.putExtra(CoreConstants.ACTIVITY_PAYLOAD.PHONE_NUMBER, phoneNumber);
        intent.putExtra(CoreConstants.ACTIVITY_PAYLOAD.FAMILY_BASE_ENTITY_ID, familyBaseID);
        intent.putExtra(CoreConstants.ACTIVITY_PAYLOAD.FORM_NAME, formName);
        intent.putExtra(CoreConstants.ACTIVITY_PAYLOAD.FAMILY_NAME, family_name);
        intent.putExtra(CoreConstants.ACTIVITY_PAYLOAD.UNIQUE_ID, uniqueId);
        intent.putExtra(CoreConstants.ACTIVITY_PAYLOAD.LAST_LMP, last_menstrual_period);

        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.TABLE_NAME, "ec_pregnancy_outcome");
        intent.putExtra("closeOnCancel", true);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getBoolean("closeOnCancel")) {
            closeOnCancel = true;
        }
    }
    
    @Override
    public void onRegistrationSaved(String encounterType, boolean isEdit, boolean hasChildren) {
        if (encounterType.equalsIgnoreCase(Constants.EVENT_TYPE.PREGNANCY_OUTCOME)) {
            Timber.d("We are home - PNC Register");
            if (ChwApplication.getApplicationFlavor().hasFamilyKitCheck()) {
                Runnable runnable = () -> ChwScheduleTaskExecutor.getInstance().execute(familyBaseEntityId, CoreConstants.EventType.FAMILY_KIT, new Date());
                org.smartregister.chw.util.Utils.startAsyncTask(new RunnableTask(runnable), null);
            }
        } else {
            super.onRegistrationSaved(encounterType, isEdit, hasChildren);
        }
    }

    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();
        FamilyRegisterActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
    }

    @Override
    protected Class<? extends CoreFamilyRegisterActivity> getFamilyRegisterActivity() {
        return FamilyRegisterActivity.class;
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new PncRegisterFragment();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResultExtended(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            try {
                JSONObject form = new JSONObject(data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
                String encounter_type = form.optString(Constants.JSON_FORM_EXTRA.ENCOUNTER_TYPE);

                if (CoreConstants.EventType.PREGNANCY_OUTCOME.equals(encounter_type)) {
                    JSONArray fields = org.smartregister.util.JsonFormUtils.fields(form);
                    String pregnancyOutcome = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, org.smartregister.chw.util.Constants.pregnancyOutcome).optString(JsonFormUtils.VALUE);
                    if (EnumUtils.isValidEnum(org.smartregister.chw.util.Constants.FamilyRegisterOptionsUtil.class, pregnancyOutcome)) {
                        startRegisterActivity(FamilyRegisterActivity.class);
                        this.finish();
                        return;
                    }
                }
                SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);

            } catch (Exception e) {
                Timber.e(e);
            }
        } else if (resultCode == Activity.RESULT_CANCELED && closeOnCancel) {
            this.finish();
        }
    }
}
