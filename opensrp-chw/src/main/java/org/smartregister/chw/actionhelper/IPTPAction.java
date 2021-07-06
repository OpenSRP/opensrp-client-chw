package org.smartregister.chw.actionhelper;

import android.content.Context;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.util.JsonFormUtils;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class IPTPAction extends HomeVisitActionHelper {
    private Context context;
    private String serviceIteration;
    private String str_date;
    private Date parsedDate;

    public IPTPAction(Context context, String serviceIteration) {
        this.context = context;
        this.serviceIteration = serviceIteration;
    }

    public JSONObject preProcess(JSONObject jsonObject, String iteration, String lastMenstrualPeriod) throws JSONException {
        JSONArray fields = JsonFormUtils.fields(jsonObject);

        JSONObject iptpJsonObject = JsonFormUtils.getFieldJSONObject(fields, "iptp{0}_date");
        iptpJsonObject.put(JsonFormConstants.MIN_DATE, lastMenstrualPeriod);

        String title = jsonObject.getJSONObject(JsonFormConstants.STEP1).getString("title");

        String formatted_count = MessageFormat.format("{0}{1}", iteration, Utils.getDayOfMonthSuffix(iteration));
        jsonObject.getJSONObject(JsonFormConstants.STEP1).put("title", MessageFormat.format(title, formatted_count));

        JSONObject visit_field = JsonFormUtils.getFieldJSONObject(fields, "iptp{0}_date");
        visit_field.put("key", MessageFormat.format(visit_field.getString("key"), iteration));
        if (iteration.equalsIgnoreCase("1")) {
            visit_field.put("hint", MessageFormat.format(visit_field.getString("hint"), context.getString(R.string.one)));
        } else if (iteration.equalsIgnoreCase("2")) {
            visit_field.put("hint", MessageFormat.format(visit_field.getString("hint"), context.getString(R.string.two)));
        } else if (iteration.equalsIgnoreCase("3")) {
            visit_field.put("hint", MessageFormat.format(visit_field.getString("hint"), context.getString(R.string.three)));
        }

        return jsonObject;
    }

    @Override
    public void onJsonFormLoaded(String s, Context context, Map<String, List<VisitDetail>> map) {
        Timber.v("onJsonFormLoaded");
    }

    @Override
    public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            str_date = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, MessageFormat.format("iptp{0}_date", serviceIteration));

            try {
                parsedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(str_date);
            } catch (ParseException e) {
                parsedDate = null;
            }

        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public String evaluateSubTitle() {
        if (parsedDate != null) {
            return MessageFormat.format("{0} {1}",
                    context.getString(R.string.date_given),
                    new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(parsedDate));
        }
        return context.getString(R.string.not_given);
    }

    @Override
    public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isBlank(str_date)) {
            return BaseAncHomeVisitAction.Status.PENDING;
        }

        if (parsedDate != null) {
            return BaseAncHomeVisitAction.Status.COMPLETED;
        } else {
            return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
        }
    }
}
