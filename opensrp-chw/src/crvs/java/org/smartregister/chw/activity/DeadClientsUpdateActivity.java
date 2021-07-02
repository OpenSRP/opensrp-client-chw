package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.activity.CoreFamilyRegisterActivity;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.fragment.DeadClientsFragment;
import org.smartregister.chw.listener.ChwBottomNavigationListener;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.CrvsConstants;
import org.smartregister.chw.util.JsonFormUtilsFlv;
import org.smartregister.chw.util.Utils;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;
import java.util.HashMap;
import java.util.Map;
import timber.log.Timber;
import static org.smartregister.chw.core.utils.FormUtils.getFormUtils;
import static org.smartregister.chw.util.CrvsConstants.*;

public class DeadClientsUpdateActivity extends CoreFamilyRegisterActivity {

    public CommonPersonObject commonPersonObject;

    public static void registerBottomNavigation(
            BottomNavigationHelper bottomNavigationHelper, BottomNavigationView bottomNavigationView, Activity activity
    ) {
        Utils.setupBottomNavigation(bottomNavigationHelper, bottomNavigationView, new ChwBottomNavigationListener(activity));
    }

    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();
        DeadClientsUpdateActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavigationMenu.getInstance(this, null, null);
        ChwApplication.getInstance().notifyAppContextChange(); // initialize the language (bug in translation)

        action = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.ACTION);
        startAncDangerSignsOutcomeForm();
    }

    public void startAncDangerSignsOutcomeForm() {

        try {
            String death_cert_issue_date = getIntent().getStringExtra(DEATH_CERTIFICATE_ISSUE_DATE);
            String death_cert = getIntent().getStringExtra(RECEIVED_DEATH_CERTIFICATE);
            if (death_cert == null) {
                death_cert = "";
            }

            JSONObject formJsonObject = getFormUtils().getFormJson(DEATH_MEMBER_FORM);
            Map<String, String> valueMap = new HashMap<>();
            valueMap.put(HAS_DEATH_CERTIFICATE, death_cert);
            valueMap.put(DEATH_CERTIFICATE_ISSUE_DATE, death_cert_issue_date);
            CoreJsonFormUtils.populateJsonForm(formJsonObject, valueMap);
            JsonFormUtilsFlv.startFormActivity(DeadClientsUpdateActivity.this, formJsonObject, DEATH_FORM);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new DeadClientsFragment();
    }

    @Override
    public void startFormActivity(String s, String s1, Map<String, String> map) {
    }

    @Override
    protected void onActivityResultExtended(int requestCode, int resultCode, Intent data) {
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);

                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(CrvsConstants.REMOVE_FAMILY_MEMBER)
                ) {
                    AllCommonsRepository childCommonsRepository = CoreChwApplication.getInstance().getAllCommonsRepository("ec_child");
                    AllCommonsRepository familyCommonsRepository = CoreChwApplication.getInstance().getAllCommonsRepository("ec_family_member");

                    if (childCommonsRepository != null && familyCommonsRepository != null) {
                        updateDeathCertificate(childCommonsRepository, familyCommonsRepository, jsonString);
                    }
                }
            } catch (Exception e) {
                Timber.e(e);
            }

        }
    }

    private void updateDeathCertificate(AllCommonsRepository childCommonsRepository, AllCommonsRepository familyCommonsRepository, String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject stepOne = jsonObject.getJSONObject("step1");
            JSONArray fields = stepOne.getJSONArray("fields");

            JSONObject has_death_certificate = fields.getJSONObject(1);
            JSONObject death_certificate_issue_date = fields.getJSONObject(2);
            JSONObject certificateNumber = fields.getJSONObject(3);

            String hasCertificate = has_death_certificate.getString("value");
            String issueDate = death_certificate_issue_date.getString("value");
            String deathCertNum = certificateNumber.getString("value");

            if (getIntent().getStringExtra(CLIENT_TYPE).equals("child")) {
                String tableName = "ec_child";
                String sql = "UPDATE "+tableName+" SET received_death_certificate = ?, death_certificate_issue_date = ? WHERE id = ?";
                String[] selectionArgs = {hasCertificate, issueDate, getIntent().getStringExtra(BASE_ENTITY_ID).toLowerCase()};
                childCommonsRepository.customQuery(sql, selectionArgs, tableName);
            } else {
                String tableName = "ec_family_member";
                String sql = "UPDATE "+tableName+" SET received_death_certificate = ?, death_certificate_issue_date = ? WHERE id = ?";
                String[] selectionArgs = {hasCertificate, issueDate, getIntent().getStringExtra(BASE_ENTITY_ID).toLowerCase()};
                familyCommonsRepository.customQuery(sql, selectionArgs, tableName);
            }
            startActivity(new Intent(this, DeadClientsActivity.class));
            finish();

        } catch (Exception e) {
            Timber.e(e);
        }
    }
}