package org.smartregister.chw.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.utils.ChwDBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.util.FormUtils;
import org.smartregister.view.LocationPickerView;

import java.util.Date;

import timber.log.Timber;

import static org.smartregister.util.JsonFormUtils.dd_MM_yyyy;

public class JsonFormUtilsFlv extends DefaultJsonFormUtilsFlv {

    public static void startFormActivity(Context context, JSONObject jsonForm, String title) {
        Intent intent = new Intent(context, Utils.metadata().familyFormActivity);
        intent.putExtra("json", jsonForm.toString());
        Form form = new Form();
        form.setHideSaveLabel(true);
        form.setName(title);
        form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
        form.setNavigationBackground(org.smartregister.chw.core.R.color.family_navigation);
        form.setHomeAsUpIndicator(org.smartregister.chw.core.R.mipmap.ic_cross_white);
        form.setPreviousLabel(context.getResources().getString(org.smartregister.chw.core.R.string.back));
        intent.putExtra("form", form);
        ((Activity)context).startActivityForResult(intent, 2244);
    }

    public static JSONObject getAutoPopulatedJsonEditFormString(String formName, Context context, CommonPersonObjectClient client,
                                                                String eventType) {
        try {
            JSONObject form = FormUtils.getInstance(context).getFormJson(formName);
            LocationPickerView lpv = new LocationPickerView(context);
            lpv.init();
            // JsonFormUtils.addWomanRegisterHierarchyQuestions(form);
            Timber.d("Form is %s", form.toString());
            if (form != null) {
                form.put(org.smartregister.family.util.JsonFormUtils.ENTITY_ID, client.getCaseId());
                form.put(org.smartregister.family.util.JsonFormUtils.ENCOUNTER_TYPE, eventType);

                JSONObject stepOne = form.getJSONObject(org.smartregister.family.util.JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(org.smartregister.family.util.JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    processPopulatableFields(client, jsonObject);

                }

                org.smartregister.family.util.JsonFormUtils.addLocHierarchyQuestions(form);

                return form;
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }

    private static void getDob(CommonPersonObjectClient client, JSONObject jsonObject) throws JSONException {
        String dobString = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
        if (StringUtils.isNotBlank(dobString)) {
            Date dob = org.smartregister.chw.core.utils.Utils.dobStringToDate(dobString);
            if (dob != null) {
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, dd_MM_yyyy.format(dob));
            }
        }
    }

    protected static void processPopulatableFields(CommonPersonObjectClient client, JSONObject jsonObject) throws JSONException {

        String key = jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY).toLowerCase();
        switch (key) {
            case "name":
                String name = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "name", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, name);
                break;
            case "unique_id":
                String unique_id = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "unique_id", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, unique_id);
                break;
            case "national_id":
                String national_id = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "national_id", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, national_id);
                break;
            case "remove_reason":
                String remove_reason = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "remove_reason", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, remove_reason);
                break;
            case "date_died":
                String date_died = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "date_died", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, date_died);
                break;
            case "death_place":
                String death_place = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "death_place", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, death_place);
                break;
            case "nationality":
                String nationality = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "nationality", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, nationality);
                break;
            case "marital_status":
                String marital_status = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "marital_status", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, marital_status);
                break;
            case "informant_name":
                String informant_name = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "informant_name", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, informant_name);
                break;
            case "informant_relationship":
                String informant_relationship = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "informant_relationship", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, informant_relationship);
                break;
            case "informant_address":
                String informant_address = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "informant_address", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, informant_address);
                break;
            case "informant_phone":
                String informant_phone = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "informant_phone", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, informant_phone);
                break;
            case "official_name":
                String official_name = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "official_name", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, official_name);
                break;
            case "official_id":
                String official_id = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "official_id", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, official_id);
                break;
            case "official_position":
                String official_position = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "official_position", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, official_position);
                break;
            case "physician_name":
                String physician_name = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "physician_name", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, physician_name);
                break;
            case "physician_position":
                String physician_position = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "physician_position", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, physician_position);
                break;
            case "death_cause":
                String death_cause = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "death_cause", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, death_cause);
                break;
            case "death_manner":
                String death_manner = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "death_manner", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, death_manner);
                break;
            case "official_address":
                String official_address = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "official_address", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, official_address);
                break;
            case "official_number":
                String official_number = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "official_number", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, official_number);
                break;
            case "surname":
                String surname = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "surname", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, surname);
                break;
            case "first_name":
                String first_name = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "first_name", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, first_name);
                break;
            case "middle_name":
                String middle_name = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "middle_name", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, middle_name);
                break;
            case "gender":
                String gender = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "gender", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, gender);
                break;
            case "birth_place_type":
                String birth_place_type = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "birth_place_type", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, birth_place_type);
                break;
            case "birth_place_name":
                String birth_place_name = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "birth_place_name", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, birth_place_name);
                break;
            case "mother_name":
                String mother_name = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "mother_name", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, mother_name);
                break;
            case "mother_marital_status":
                String mother_marital_status = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "mother_marital_status", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, mother_marital_status);
                break;
            case "mother_highest_edu_level":
                String mother_highest_edu_level = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "mother_highest_edu_level", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, mother_highest_edu_level);
                break;
            case "mother_usual_residence":
                String mother_usual_residence = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "mother_usual_residence", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, mother_usual_residence);
                break;
            case "mother_id":
                String mother_id = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "mother_id", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, mother_id);
                break;
            case "father_name":
                String father_name = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "father_name", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, father_name);
                break;
            case "father_id":
                String father_id = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "father_id", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, father_id);
                break;
            case "father_dob_entered":
                String father_dob_entered = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "father_dob_entered", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, father_dob_entered);
                break;
            case "father_marital_status":
                String father_marital_status = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "father_marital_status", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, father_marital_status);
                break;
            case "father_highest_edu_level":
                String father_highest_edu_level = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "father_highest_edu_level", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, father_highest_edu_level);
                break;
            case "father_birth_place":
                String father_birth_place = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "father_birth_place", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, father_birth_place);
                break;
            case "mother_birth_place":
                String mother_birth_place = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), "mother_birth_place", false);
                jsonObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, mother_birth_place);
                break;
            case DBConstants.KEY.DOB:
                getDob(client, jsonObject);
                break;

            default:
                Timber.e("ERROR:: Unprocessed Form Object Key %s", jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY));
                break;
        }
//        updateOptions(client, jsonObject);
    }

    private static void updateOptions(CommonPersonObjectClient client, JSONObject jsonObject) throws JSONException {
        if (jsonObject.getString(org.smartregister.family.util.JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.DOB)) {
            jsonObject.put(org.smartregister.family.util.JsonFormUtils.READ_ONLY, false);
            JSONObject optionsObject = jsonObject.getJSONArray(Constants.JSON_FORM_KEY.OPTIONS).getJSONObject(0);
            optionsObject.put(org.smartregister.family.util.JsonFormUtils.VALUE, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false));
        }
    }

}
