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
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.fragment.FamilyRegisterFragment;
import org.smartregister.chw.listener.ChwBottomNavigationListener;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.JsonFormUtilsFlv;
import org.smartregister.chw.util.Utils;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.view.fragment.BaseRegisterFragment;
import java.util.HashMap;
import java.util.Map;
import timber.log.Timber;
import static org.smartregister.chw.core.utils.FormUtils.getFormUtils;
import static org.smartregister.chw.util.CrvsConstants.BIRTH_CERT;
import static org.smartregister.chw.util.CrvsConstants.BIRTH_CERTIFICATE_ISSUE_DATE;
import static org.smartregister.chw.util.CrvsConstants.BIRTH_CERTIFICATION_CHANGED;
import static org.smartregister.chw.util.CrvsConstants.BIRTH_CERT_NUM;
import static org.smartregister.chw.util.CrvsConstants.BIRTH_FORM;
import static org.smartregister.chw.util.CrvsConstants.BIRTH_NOTIFICATION;
import static org.smartregister.chw.util.CrvsConstants.BIRTH_REGISTRATION;

public class BirthNotificationUpdateActivity extends CoreFamilyRegisterActivity {

    public CommonPersonObject commonPersonObject;

    public static void registerBottomNavigation(
            BottomNavigationHelper bottomNavigationHelper, BottomNavigationView bottomNavigationView, Activity activity
    ) {
        Utils.setupBottomNavigation(bottomNavigationHelper, bottomNavigationView, new ChwBottomNavigationListener(activity));
    }

    @Override
    protected void registerBottomNavigation() {
        super.registerBottomNavigation();
        BirthNotificationUpdateActivity.registerBottomNavigation(bottomNavigationHelper, bottomNavigationView, this);
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
            String birth_cert = getIntent().getStringExtra(BIRTH_CERT);
            String birth_cert_issue_date = getIntent().getStringExtra(BIRTH_CERTIFICATE_ISSUE_DATE);
            String birth_cert_num = getIntent().getStringExtra(BIRTH_CERT_NUM);
            String birth_notification = getIntent().getStringExtra(BIRTH_NOTIFICATION);
            String birth_registration = getIntent().getStringExtra(BIRTH_REGISTRATION);
            if (birth_cert==null){
                birth_cert = "";
            }

            JSONObject formJsonObject = getFormUtils().getFormJson(BIRTH_CERTIFICATION_CHANGED);
            Map<String, String> valueMap = new HashMap<>();
            valueMap.put(BIRTH_CERT, birth_cert);
            valueMap.put(BIRTH_CERTIFICATE_ISSUE_DATE, birth_cert_issue_date);
            valueMap.put(BIRTH_CERT_NUM, birth_cert_num);
            valueMap.put(BIRTH_NOTIFICATION, birth_notification);
            valueMap.put(BIRTH_REGISTRATION, birth_registration);
            CoreJsonFormUtils.populateJsonForm(formJsonObject, valueMap);
            JsonFormUtilsFlv.startFormActivity(BirthNotificationUpdateActivity.this, formJsonObject, BIRTH_FORM);

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new FamilyRegisterFragment();
    }

    @Override
    public void startFormActivity(String s, String s1, Map<String, String> map) {
        // code
    }

    @Override
    protected void onActivityResultExtended(int requestCode, int resultCode, Intent data) {
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(org.smartregister.family.util.Constants.JSON_FORM_EXTRA.JSON);

                JSONObject form = new JSONObject(jsonString);

                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(CoreConstants.EventType.BIRTH_CERTIFICATION)
                ) {

                    AllCommonsRepository allCommonsRepository = CoreChwApplication.getInstance().getAllCommonsRepository("ec_child");
                    //Update ec_child table
                    if (allCommonsRepository != null) {
                        updateBirthCertificate(allCommonsRepository, jsonString);
                    }
                }
            } catch (Exception e) {
                Timber.e(e);
            }

        }
    }

    private void updateBirthCertificate(AllCommonsRepository allCommonsRepository, String jsonString){
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject stepOne = jsonObject.getJSONObject("step1");
            JSONArray fields = stepOne.getJSONArray("fields");

            JSONObject birth_certification = fields.getJSONObject(0);
            JSONObject birth_registration = fields.getJSONObject(1);
            JSONObject birth_notification = fields.getJSONObject(2);
            JSONObject objBirthCertificate = fields.getJSONObject(4);
            JSONObject objBirthCertificateNum = fields.getJSONObject(5);

            String certificate = birth_certification.getString("value");

            String tableName = "ec_child";
            if (certificate.equals("Yes")){
                String dateOfBirthCertificate = objBirthCertificate.getString("value");
                String birthCertNum = objBirthCertificateNum.getString("value");
                String sql = "UPDATE ec_child SET birth_cert = ?, birth_cert_issue_date = ?, birth_cert_num = ? WHERE id = ?";
                String[] selectionArgs = {certificate, dateOfBirthCertificate, birthCertNum, getIntent().getStringExtra(DBConstants.KEY.BASE_ENTITY_ID).toLowerCase()};
                allCommonsRepository.customQuery(sql, selectionArgs, tableName);
            }else {
                String registration = birth_registration.getString("value");
                if (registration.equals("Yes")){
                    String sql = "UPDATE ec_child SET birth_cert = ?, birth_registration = ? WHERE id = ?";

                    String[] selectionArgs = {certificate, "Yes", getIntent().getStringExtra(DBConstants.KEY.BASE_ENTITY_ID).toLowerCase()};
                    allCommonsRepository.customQuery(sql, selectionArgs, tableName);
                }else {
                    String notification = birth_notification.getString("value");
                    String sql = "UPDATE ec_child SET birth_cert = ?, birth_registration = ?, birth_notification = ? WHERE id = ?";
                    String[] selectionArgs = {certificate, "No", notification, getIntent().getStringExtra(DBConstants.KEY.BASE_ENTITY_ID).toLowerCase()};
                    allCommonsRepository.customQuery(sql, selectionArgs, tableName);
                }
            }
            startActivity(new Intent(this, BirthNotificationRegisterActivity.class));
            finish();

        } catch (Exception e) {
            Timber.e(e);
        }
    }
}