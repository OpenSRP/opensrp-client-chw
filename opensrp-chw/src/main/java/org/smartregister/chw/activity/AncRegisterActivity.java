package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;

import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.core.activity.CoreAncRegisterActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.fragment.AncRegisterFragment;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.Date;

import timber.log.Timber;

import static org.smartregister.chw.core.utils.CoreConstants.EventType.ANC_REGISTRATION;

public class AncRegisterActivity extends CoreAncRegisterActivity {
    protected static boolean shouldFinishOnBack;

    public static void startAncRegistrationActivity(Activity activity, String memberBaseEntityID, String phoneNumber, String formName,
                                                    String uniqueId, String familyBaseID, String family_name) {
        Intent intent = new Intent(activity, AncRegisterActivity.class);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, memberBaseEntityID);

        shouldFinishOnBack = activity instanceof FamilyOtherMemberProfileActivity;

        intent.putExtra(CoreConstants.ACTIVITY_PAYLOAD.PHONE_NUMBER, phoneNumber);
        intent.putExtra(CoreConstants.ACTIVITY_PAYLOAD.FAMILY_BASE_ENTITY_ID, familyBaseID);
        intent.putExtra(CoreConstants.ACTIVITY_PAYLOAD.FORM_NAME, formName);
        intent.putExtra(CoreConstants.ACTIVITY_PAYLOAD.FAMILY_NAME, family_name);
        intent.putExtra(CoreConstants.ACTIVITY_PAYLOAD.UNIQUE_ID, uniqueId);
        intent.putExtra(org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.anc.util.Constants.ACTIVITY_PAYLOAD_TYPE.REGISTRATION);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.TABLE_NAME, "ec_anc_register");
        activity.startActivity(intent);
    }

    @Override
    public Class getRegisterActivity(String register) {
        if (register.equals(ANC_REGISTRATION))
            return AncRegisterActivity.class;
        else
            return PncRegisterActivity.class;
    }

    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        FamilyRegisterActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new AncRegisterFragment();
    }

    @Override
    public void switchToBaseFragment() {
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        startActivity(intent);
        this.finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_CODE_GET_JSON) {
//            process the form
            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);
                String baseEnityId = form.optString(Constants.JSON_FORM_EXTRA.ENTITY_TYPE);
                String encounter_type = form.optString(Constants.JSON_FORM_EXTRA.ENCOUNTER_TYPE);

                if (encounter_type.equalsIgnoreCase(CoreConstants.EventType.PNC_HOME_VISIT)) {
                    ChwScheduleTaskExecutor.getInstance().execute(baseEnityId, CoreConstants.EventType.PNC_HOME_VISIT, new Date());
                } else if (encounter_type.equalsIgnoreCase(CoreConstants.EventType.ANC_HOME_VISIT)) {
                    ChwScheduleTaskExecutor.getInstance().execute(baseEnityId, CoreConstants.EventType.ANC_HOME_VISIT, new Date());
                }
                SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
            } catch (Exception e) {
                Timber.e(e);
            }
        } else if (shouldFinishOnBack) {
            finish();
        }
    }
}
