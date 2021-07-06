package org.smartregister.chw.actionhelper;

import android.content.Context;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.JsonFormUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class ObservationAction implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
    private Context context;
    private String date_of_illness;
    private String illness_description;
    private String action_taken;
    private LocalDate illnessDate = null;
    private JSONObject jsonObject;
    private String dob;

    public ObservationAction() {
    }

    public ObservationAction(String dob) {
        this.dob = dob;
    }

    @Override
    public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
        this.context = context;
        try {
            if (StringUtils.isNotBlank(dob)) {
                jsonObject = new JSONObject(jsonString);
                JSONArray fields = JsonFormUtils.fields(jsonObject);
                JSONObject dateOfIllness = org.smartregister.util.JsonFormUtils.getFieldJSONObject(fields, "date_of_illness");
                dateOfIllness.put(JsonFormConstants.MIN_DATE, dob);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public String getPreProcessed() {
        if (StringUtils.isNotBlank(dob)) {
            return jsonObject.toString();
        } else {
            return null;
        }
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            date_of_illness = JsonFormUtils.getValue(jsonObject, "date_of_illness");
            illness_description = JsonFormUtils.getValue(jsonObject, "illness_description");
            action_taken = JsonFormUtils.getValue(jsonObject, "action_taken");
        } catch (JSONException e) {
            Timber.e(e);
        }

        if (StringUtils.isNotBlank(date_of_illness)) {
            illnessDate = DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(date_of_illness);
        } else {
            illnessDate = null;
        }
    }

    @Override
    public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
        return null;
    }

    @Override
    public String getPreProcessedSubTitle() {
        return null;
    }

    @Override
    public String postProcess(String s) {
        return null;
    }

    @Override
    public String evaluateSubTitle() {

        String translated_action_taken = "";
        if ("Managed".equalsIgnoreCase(action_taken)) {
            translated_action_taken = context.getString(R.string.managed);
        } else if ("Referred".equalsIgnoreCase(action_taken)) {
            translated_action_taken = context.getString(R.string.referred);
        } else if ("No action taken".equalsIgnoreCase(action_taken)) {
            translated_action_taken = context.getString(R.string.no_action_taken);
        }

        return MessageFormat.format("{0}: {1}\n {2}: {3}",
                illnessDate != null ? DateTimeFormat.forPattern("dd MMM yyyy").print(illnessDate) : "",
                illness_description, context.getString(R.string.action_taken),
                translated_action_taken
        );
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isNotBlank(date_of_illness)) {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        } else {
            return BaseAncHomeVisitAction.Status.PENDING;
        }
    }

    @Override
    public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
        Timber.v("onPayloadReceived");
    }
}
