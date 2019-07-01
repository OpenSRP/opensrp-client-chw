package org.smartregister.chw.interactor;

import android.content.Context;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.util.Constants.JSON_FORM.ANC_HOME_VISIT;
import org.smartregister.chw.util.ContactUtil;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.chw.util.JsonFormUtils.getCheckBoxValue;
import static org.smartregister.chw.util.JsonFormUtils.getValue;
import static org.smartregister.util.JsonFormUtils.fields;
import static org.smartregister.util.JsonFormUtils.getFieldJSONObject;

public class AncHomeVisitInteractorFlv implements AncHomeVisitInteractor.Flavor {
    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        LinkedHashMap<String, BaseAncHomeVisitAction> actionList = new LinkedHashMap<>();

        Context context = view.getContext();

        // get contact
        LocalDate lastContact = new DateTime(memberObject.getDateCreated()).toLocalDate();
        boolean isFirst = (StringUtils.isBlank(memberObject.getLastContactVisit()));
        LocalDate lastMenstrualPeriod = DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(memberObject.getLastMenstrualPeriod());

        if (StringUtils.isNotBlank(memberObject.getLastContactVisit()))
            lastContact = DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(memberObject.getLastContactVisit());

        Map<Integer, LocalDate> dateMap = new LinkedHashMap<>();

        // today is the due date for the very first visit
        if (isFirst) {
            dateMap.put(0, LocalDate.now());
        }

        dateMap.putAll(ContactUtil.getContactWeeks(isFirst, lastContact, lastMenstrualPeriod));

        evaluateDangerSigns(actionList, context);
        evaluateHealthFacilityVisit(actionList, memberObject, dateMap, context);
        evaluateFamilyPlanning(actionList, context);
        evaluateNutritionStatus(actionList, context);
        evaluateCounsellingStatus(actionList, context);
        evaluateMalaria(actionList, context);
        evaluateObservation(actionList, context);
        evaluateRemarks(actionList, context);

        return actionList;
    }

    private JSONObject getJson(BaseAncHomeVisitAction ba, MemberObject memberObject) throws Exception {
        String locationId = ChwApplication.getInstance().getContext().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
        JSONObject jsonObject = JsonFormUtils.getFormAsJson(ba.getFormName());
        JsonFormUtils.getRegistrationForm(jsonObject, memberObject.getBaseEntityId(), locationId);
        return jsonObject;
    }

    private void evaluateDangerSigns(LinkedHashMap<String, BaseAncHomeVisitAction> actionList, final Context context) throws BaseAncHomeVisitAction.ValidationException {
        final BaseAncHomeVisitAction ba = new BaseAncHomeVisitAction(context.getString(R.string.anc_home_visit_danger_signs), "", false, null,
                ANC_HOME_VISIT.getDangerSigns());
        ba.setAncHomeVisitActionHelper(new BaseAncHomeVisitAction.AncHomeVisitActionHelper() {
            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (ba.getJsonPayload() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(ba.getJsonPayload());

                        String value = getValue(jsonObject, "danger_signs_counseling");

                        ba.setSubTitle(getDangerSignsText(jsonObject, context));
                        if (value.equalsIgnoreCase("Yes")) {
                            return BaseAncHomeVisitAction.Status.COMPLETED;
                        } else if (value.equalsIgnoreCase("No")) {
                            return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
                        } else {
                            ba.setSubTitle("");
                            return BaseAncHomeVisitAction.Status.PENDING;
                        }
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
                return ba.computedStatus();
            }
        });

        actionList.put(context.getString(R.string.anc_home_visit_danger_signs), ba);
    }

    private String getDangerSignsText(JSONObject jsonObject, Context context) {
        StringBuilder stringBuilder = new StringBuilder();

        String signs_present = getCheckBoxValue(jsonObject, "danger_signs_present");
        String counseling = getValue(jsonObject, "danger_signs_counseling");

        stringBuilder.append(MessageFormat.format("Danger signs: {0}", signs_present));
        stringBuilder.append("\n");
        stringBuilder.append(MessageFormat.format("Health facility counselling {0}",
                (counseling.equalsIgnoreCase("Yes") ? context.getString(R.string.done).toLowerCase() : context.getString(R.string.not_done).toLowerCase())
        ));
        return stringBuilder.toString();
    }

    private void evaluateHealthFacilityVisit(LinkedHashMap<String, BaseAncHomeVisitAction> actionList, final MemberObject memberObject, Map<Integer, LocalDate> dateMap, final Context context) throws BaseAncHomeVisitAction.ValidationException {

        String visit = MessageFormat.format(context.getString(R.string.anc_home_visit_facility_visit), memberObject.getConfirmedContacts() + 1);
        final BaseAncHomeVisitAction ba = new BaseAncHomeVisitAction(visit, "", false, null, ANC_HOME_VISIT.getHealthFacilityVisit());

        evaluateHealthFacilityVisitPre(ba, memberObject, dateMap, context);

        ba.setAncHomeVisitActionHelper(new BaseAncHomeVisitAction.AncHomeVisitActionHelper() {
            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (ba.getJsonPayload() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(ba.getJsonPayload());

                        String value = getValue(jsonObject, "anc_hf_visit");
                        ba.setSubTitle(getHealthFacilityVisitText(jsonObject, context));
                        if (value.equalsIgnoreCase("Yes")) {

                            return BaseAncHomeVisitAction.Status.COMPLETED;
                        } else if (value.equalsIgnoreCase("No")) {
                            return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
                        } else {
                            return BaseAncHomeVisitAction.Status.PENDING;
                        }
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
                return ba.computedStatus();
            }
        });

        ba.setOnPayLoadReceived(new Runnable() {
            @Override
            public void run() {
                try {

                    JSONObject jsonObject = new JSONObject(ba.getJsonPayload());

                    JSONArray field = fields(jsonObject);
                    JSONObject confirmed_visits = getFieldJSONObject(field, "confirmed_visits");

                    String count = String.valueOf(memberObject.getConfirmedContacts());
                    String value = getValue(jsonObject, "anc_hf_visit");
                    if (value.equalsIgnoreCase("Yes")) {
                        count = String.valueOf(memberObject.getConfirmedContacts() + 1);
                    }

                    if (!confirmed_visits.getString(JsonFormConstants.VALUE).equals(count)) {
                        confirmed_visits.put(JsonFormConstants.VALUE, memberObject.getConfirmedContacts() + 1);
                        ba.setProcessedJsonPayload(jsonObject.toString());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        actionList.put(visit, ba);
    }

    private void evaluateHealthFacilityVisitPre(BaseAncHomeVisitAction ba, MemberObject memberObject, Map<Integer, LocalDate> dateMap, Context context) {
        // open the form and inject the values
        try {
            if (StringUtils.isBlank(ba.getJsonPayload())) {

                JSONObject jsonObject = getJson(ba, memberObject);
                JSONArray fields = fields(jsonObject);

                if (dateMap.size() > 0) {
                    Map.Entry<Integer, LocalDate> entry = dateMap.entrySet().iterator().next();
                    LocalDate visitDate = entry.getValue();

                    ba.setTitle(MessageFormat.format(context.getString(R.string.anc_home_visit_facility_visit), memberObject.getConfirmedContacts() + 1));

                    // update form
                    if (visitDate.isBefore(LocalDate.now())) {
                        ba.setSubTitle(MessageFormat.format("{0} {1}", context.getString(R.string.overdue), DateTimeFormat.forPattern("dd MMM yyyy").print(visitDate)));
                        ba.setScheduleStatus(BaseAncHomeVisitAction.ScheduleStatus.OVERDUE);
                    } else {
                        ba.setSubTitle(MessageFormat.format("{0} {1}", context.getString(R.string.due), DateTimeFormat.forPattern("dd MMM yyyy").print(visitDate)));
                        ba.setScheduleStatus(BaseAncHomeVisitAction.ScheduleStatus.DUE);
                    }


                    String title = jsonObject.getJSONObject(JsonFormConstants.STEP1).getString("title");
                    jsonObject.getJSONObject(JsonFormConstants.STEP1).put("title", MessageFormat.format(title, memberObject.getConfirmedContacts() + 1));

                    JSONObject visit_field = getFieldJSONObject(fields, "anc_hf_visit");
                    visit_field.put("label_info_title", MessageFormat.format(visit_field.getString("label_info_title"), memberObject.getConfirmedContacts() + 1));
                    visit_field.put("hint", MessageFormat.format(visit_field.getString("hint"), memberObject.getConfirmedContacts() + 1, visitDate));

                    // current visit count
                    JSONObject confirmed_visits = getFieldJSONObject(fields, "confirmed_visits");
                    confirmed_visits.put(JsonFormConstants.VALUE, memberObject.getConfirmedContacts());
                }

                ba.setProcessedJsonPayload(jsonObject.toString());
                ba.setActionStatus(BaseAncHomeVisitAction.Status.PENDING);
            }
        } catch (Exception e) {
            Timber.e(e);
        }

    }

    private String getHealthFacilityVisitText(JSONObject jsonObject, Context context) throws ParseException {

        String value = getValue(jsonObject, "anc_hf_visit");
        StringBuilder stringBuilder = new StringBuilder();
        if (value.equalsIgnoreCase("No")) {
            stringBuilder.append(context.getString(R.string.visit_not_done).replace("\n", ""));
        } else {

            String date_str = getValue(jsonObject, "anc_hf_visit_date");
            String testsDone = getCheckBoxValue(jsonObject, "tests_done");
            String treatments = getCheckBoxValue(jsonObject, "imm_medicine_given");
            String llin_given = getValue(jsonObject, "llin_given");

            Date date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(date_str);

            stringBuilder.append(MessageFormat.format("{0}: {1}\n", context.getString(R.string.date), new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)));
            stringBuilder.append(MessageFormat.format("{0}: {1}\n", context.getString(R.string.tests_done), testsDone));
            stringBuilder.append(MessageFormat.format("{0}: {1}\n", context.getString(R.string.treatment_given), treatments));
            stringBuilder.append(MessageFormat.format("{0}: {1}", context.getString(R.string.received_llin), llin_given));
        }

        return stringBuilder.toString();
    }

    private void evaluateFamilyPlanning(LinkedHashMap<String, BaseAncHomeVisitAction> actionList, final Context context) throws BaseAncHomeVisitAction.ValidationException {
        final BaseAncHomeVisitAction ba = new BaseAncHomeVisitAction(context.getString(R.string.anc_home_visit_family_planning), "", false, null,
                ANC_HOME_VISIT.getFamilyPlanning());
        ba.setAncHomeVisitActionHelper(new BaseAncHomeVisitAction.AncHomeVisitActionHelper() {
            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (ba.getJsonPayload() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(ba.getJsonPayload());

                        String value = getValue(jsonObject, "fam_planning");

                        String subTitle = (value.equalsIgnoreCase("Yes") ? context.getString(R.string.done).toLowerCase() : context.getString(R.string.not_done).toLowerCase());
                        ba.setSubTitle(StringUtils.capitalize(subTitle));
                        if (value.equalsIgnoreCase("Yes")) {
                            return BaseAncHomeVisitAction.Status.COMPLETED;
                        } else {
                            return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
                        }
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
                return ba.computedStatus();
            }
        });

        actionList.put(context.getString(R.string.anc_home_visit_family_planning), ba);
    }

    private void evaluateNutritionStatus(LinkedHashMap<String, BaseAncHomeVisitAction> actionList, final Context context) throws BaseAncHomeVisitAction.ValidationException {
        final BaseAncHomeVisitAction ba = new BaseAncHomeVisitAction(context.getString(R.string.anc_home_visit_nutrition_status), "", false, null,
                ANC_HOME_VISIT.getNutritionStatus());

        ba.setAncHomeVisitActionHelper(new BaseAncHomeVisitAction.AncHomeVisitActionHelper() {
            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (ba.getJsonPayload() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(ba.getJsonPayload());

                        String value = getValue(jsonObject, "nutrition_status").toLowerCase();

                        String subTitle = MessageFormat.format("{0}: {1}", context.getString(R.string.nutrition_status), StringUtils.capitalize(value));
                        ba.setSubTitle(subTitle);

                        return BaseAncHomeVisitAction.Status.COMPLETED;
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
                return ba.computedStatus();
            }
        });

        actionList.put(context.getString(R.string.anc_home_visit_nutrition_status), ba);
    }

    private void evaluateCounsellingStatus(LinkedHashMap<String, BaseAncHomeVisitAction> actionList, final Context context) throws BaseAncHomeVisitAction.ValidationException {
        final BaseAncHomeVisitAction ba = new BaseAncHomeVisitAction(context.getString(R.string.anc_home_visit_counselling_task), "", false, null,
                ANC_HOME_VISIT.getCOUNSELLING());

        ba.setAncHomeVisitActionHelper(new BaseAncHomeVisitAction.AncHomeVisitActionHelper() {
            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (ba.getJsonPayload() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(ba.getJsonPayload());

                        String value = getCheckBoxValue(jsonObject, "counselling_given").toLowerCase();

                        String subTitle = (!value.contains("none") ? context.getString(R.string.done).toLowerCase() : context.getString(R.string.not_done).toLowerCase());
                        ba.setSubTitle(MessageFormat.format("{0} {1}", context.getString(R.string.counselling), subTitle));

                        return BaseAncHomeVisitAction.Status.COMPLETED;
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
                return ba.computedStatus();
            }
        });

        actionList.put(context.getString(R.string.anc_home_visit_counselling_task), ba);
    }

    private void evaluateMalaria(LinkedHashMap<String, BaseAncHomeVisitAction> actionList, final Context context) throws BaseAncHomeVisitAction.ValidationException {
        final BaseAncHomeVisitAction ba = new BaseAncHomeVisitAction(context.getString(R.string.anc_home_visit_malaria_prevention), "", false, null,
                ANC_HOME_VISIT.getMALARIA());

        ba.setAncHomeVisitActionHelper(new BaseAncHomeVisitAction.AncHomeVisitActionHelper() {
            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (ba.getJsonPayload() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(ba.getJsonPayload());

                        String value1 = getValue(jsonObject, "fam_llin");
                        String value2 = getValue(jsonObject, "llin_2days");
                        String value3 = getValue(jsonObject, "llin_condition");

                        ba.setSubTitle(getMalariaText(jsonObject, context));

                        if (value1.equalsIgnoreCase("Yes") && value2.equalsIgnoreCase("Yes") && value3.equalsIgnoreCase("Okay")) {
                            return BaseAncHomeVisitAction.Status.COMPLETED;
                        } else {
                            return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
                        }
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
                return ba.computedStatus();
            }
        });

        actionList.put(context.getString(R.string.anc_home_visit_malaria_prevention), ba);
    }

    private void evaluateObservation(LinkedHashMap<String, BaseAncHomeVisitAction> actionList, final Context context) throws BaseAncHomeVisitAction.ValidationException {
        final BaseAncHomeVisitAction ba = new BaseAncHomeVisitAction(context.getString(R.string.anc_home_visit_observations_n_illnes), "", true, null,
                ANC_HOME_VISIT.getObservationAndIllness());

        ba.setAncHomeVisitActionHelper(new BaseAncHomeVisitAction.AncHomeVisitActionHelper() {
            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (ba.getJsonPayload() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(ba.getJsonPayload());


                        LocalDate illnessDate = DateTimeFormat.forPattern("dd-MM-yyyy").parseLocalDate(getValue(jsonObject, "date_of_illness"));
                        String desc = getValue(jsonObject, "illness_description");
                        String action = getCheckBoxValue(jsonObject, "action_taken");

                        String builder = MessageFormat.format("{0}: {1}\n {2}: {3}",
                                DateTimeFormat.forPattern("dd MMM yyyy").print(illnessDate),
                                desc, context.getString(R.string.action_taken), action
                        );
                        ba.setSubTitle(builder);

                        return BaseAncHomeVisitAction.Status.COMPLETED;
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
                return ba.computedStatus();
            }
        });

        actionList.put(context.getString(R.string.anc_home_visit_observations_n_illnes), ba);
    }

    private String getMalariaText(JSONObject jsonObject, Context context) {

        String fam_llin = getValue(jsonObject, "fam_llin");
        String llin_2days = getValue(jsonObject, "llin_2days");
        String llin_condition = getValue(jsonObject, "llin_condition");

        StringBuilder stringBuilder = new StringBuilder();
        if (fam_llin.equalsIgnoreCase("No")) {
            stringBuilder.append(MessageFormat.format("{0}: {1}\n", context.getString(R.string.uses_net), StringUtils.capitalize(fam_llin.trim().toLowerCase())));
        } else {

            stringBuilder.append(MessageFormat.format("{0}: {1} · ", context.getString(R.string.uses_net), StringUtils.capitalize(fam_llin.trim().toLowerCase())));
            stringBuilder.append(MessageFormat.format("{0}: {1} · ", context.getString(R.string.slept_under_net), StringUtils.capitalize(llin_2days.trim().toLowerCase())));
            stringBuilder.append(MessageFormat.format("{0}: {1}", context.getString(R.string.net_condition), StringUtils.capitalize(llin_condition.trim().toLowerCase())));
        }

        return stringBuilder.toString();
    }

    private void evaluateRemarks(LinkedHashMap<String, BaseAncHomeVisitAction> actionList, final Context context) throws BaseAncHomeVisitAction.ValidationException {
        final BaseAncHomeVisitAction ba = new BaseAncHomeVisitAction(context.getString(R.string.anc_home_visit_remarks_and_comments), "", true, null,
                ANC_HOME_VISIT.getRemarksAndComments());

        ba.setAncHomeVisitActionHelper(new BaseAncHomeVisitAction.AncHomeVisitActionHelper() {
            @Override
            public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
                if (ba.getJsonPayload() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(ba.getJsonPayload());

                        String value = getValue(jsonObject, "chw_comment_anc");
                        String builder = MessageFormat.format("{0}: {1}",
                                context.getString(R.string.remarks_and__comments), StringUtils.capitalize(value));

                        ba.setSubTitle(builder);

                        return BaseAncHomeVisitAction.Status.COMPLETED;
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
                return ba.computedStatus();
            }
        });

        actionList.put(context.getString(R.string.anc_home_visit_remarks_and_comments), ba);
    }
}
