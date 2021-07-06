package org.smartregister.chw.interactor;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.actionhelper.DangerSignsAction;
import org.smartregister.chw.actionhelper.ExclusiveBreastFeedingAction;
import org.smartregister.chw.actionhelper.ImmunizationActionHelper;
import org.smartregister.chw.actionhelper.ObservationAction;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.VaccineDisplay;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.fragment.BaseAncHomeVisitFragment;
import org.smartregister.chw.anc.fragment.BaseHomeVisitImmunizationFragment;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.repository.VisitDetailsRepository;
import org.smartregister.chw.anc.repository.VisitRepository;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.dao.VisitDao;
import org.smartregister.chw.core.domain.Person;
import org.smartregister.chw.core.rule.PNCHealthFacilityVisitRule;
import org.smartregister.chw.core.utils.RecurringServiceUtil;
import org.smartregister.chw.core.utils.VaccineScheduleUtil;
import org.smartregister.chw.dao.ChwPNCDao;
import org.smartregister.chw.dao.PersonDao;
import org.smartregister.chw.domain.PNCHealthFacilityVisitSummary;
import org.smartregister.chw.domain.PncBaby;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.PNCVisitUtil;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public abstract class DefaultPncHomeVisitInteractorFlv implements PncHomeVisitInteractor.Flavor {
    protected LinkedHashMap<String, BaseAncHomeVisitAction> actionList;
    protected Context context;
    protected Map<String, List<VisitDetail>> details = null;
    protected List<PncBaby> children;
    protected MemberObject memberObject;
    protected BaseAncHomeVisitContract.View view;
    protected Boolean editMode = false;
    protected Boolean hasBirthCert = false;
    protected String parsedDate;

    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        actionList = new LinkedHashMap<>();
        context = view.getContext();
        this.memberObject = memberObject;
        editMode = view.getEditMode();
        this.view = view;
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EventType.PNC_HOME_VISIT);
            if (lastVisit != null) {
                details = Collections.unmodifiableMap(VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId())));
            }
        }

        if (children == null) {
            children = new ArrayList<>();
        }

        children.addAll(getChildren(memberObject.getBaseEntityId()));
        try {
            Constants.JSON_FORM.setLocaleAndAssetManager(ChwApplication.getCurrentLocale(), ChwApplication.getInstance().getApplicationContext().getAssets());
        } catch (Exception e) {
            Timber.e(e);
        }
        try {
            parsedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(children.get(0).getDob());
        } catch (Exception e) {
            Timber.e(e);
        }

        try {
            evaluateDangerSignsMother();
            evaluatePNCHealthFacilityVisit();
            evaluateFamilyPlanning();
            evaluateObservationAndIllnessMother();

            for (Person baby : children) {
                evaluateDangerSignsBaby(baby);
                evaluateChildVaccineCard(baby);
                evaluateImmunization(baby);
                evaluateUmbilicalCord(baby);
                evaluateExclusiveBreastFeeding(baby);
                evaluateKangarooMotherCare(baby);
                evaluateBirthCertForm(baby);
                evaluateObservationAndIllnessBaby(baby);
            }

        } catch (BaseAncHomeVisitAction.ValidationException e) {
            throw (e);
        } catch (Exception e) {
            Timber.e(e);
        }
        return actionList;
    }

    @VisibleForTesting
    public List<PncBaby> getChildren(String baseID) {
        return PersonDao.getMothersPNCBabies(baseID);
    }

    protected VisitRepository getVisitRepository() {
        return AncLibrary.getInstance().visitRepository();
    }

    protected VisitDetailsRepository getVisitDetailsRepository() {
        return AncLibrary.getInstance().visitDetailsRepository();
    }

    protected Map<String, List<VisitDetail>> getDetails(String eventName) {
        return getDetails(memberObject.getBaseEntityId(), eventName);
    }

    protected Map<String, List<VisitDetail>> getDetails(String baseEntityID, String eventName) {
        if (!editMode)
            return null;

        Map<String, List<VisitDetail>> visitDetails = null;
        Visit lastVisit = getVisitRepository().getLatestVisit(baseEntityID, eventName);
        if (lastVisit != null) {
            visitDetails = VisitUtils.getVisitGroups(getVisitDetailsRepository().getVisits(lastVisit.getVisitId()));
        }

        return visitDetails;
    }

    @VisibleForTesting
    public BaseAncHomeVisitAction.Builder getBuilder(String title) {
        return new BaseAncHomeVisitAction.Builder(context, title);
    }

    @VisibleForTesting
    public JSONObject getFormJson(String name) throws Exception {
        return FormUtils.getInstance(context).getFormJson(name);
    }

    @VisibleForTesting
    public JSONObject getFormJson(String name, String baseEntityID) throws Exception {
        return org.smartregister.chw.util.JsonFormUtils.getJson(view.getContext(), name, baseEntityID);
    }

    private void evaluateDangerSignsMother() throws Exception {
        BaseAncHomeVisitAction action = getBuilder(context.getString(R.string.pnc_danger_signs_mother))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSignsMother())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_danger_signs_mother), action);
    }

    private void evaluateDangerSignsBaby(Person baby) throws Exception {
        if (getAgeInDays(baby.getDob()) <= 28) {

            Map<String, List<VisitDetail>> details = getDetails(baby.getBaseEntityID(), Constants.EventType.DANGER_SIGNS_BABY);

            BaseAncHomeVisitAction action = getBuilder(MessageFormat.format(context.getString(R.string.pnc_danger_signs_baby), baby.getFullName()))
                    .withOptional(false)
                    .withDetails(details)
                    .withBaseEntityID(baby.getBaseEntityID())
                    .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                    .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSignsBaby())
                    .withHelper(new DangerSignsAction())
                    .build();
            actionList.put(MessageFormat.format(context.getString(R.string.pnc_danger_signs_baby), baby.getFullName()), action);
        }
    }

    @VisibleForTesting
    public PNCHealthFacilityVisitSummary getLastHealthFacilityVisitSummary() {
        return ChwPNCDao.getLastHealthFacilityVisitSummary(memberObject.getBaseEntityId());
    }

    @VisibleForTesting
    public PNCHealthFacilityVisitRule getNextPNCHealthFacilityVisit(Date deliveryDate, Date lastVisitDate) {
        return PNCVisitUtil.getNextPNCHealthFacilityVisit(deliveryDate, lastVisitDate);
    }

    protected void evaluatePNCHealthFacilityVisit() throws Exception {

        PNCHealthFacilityVisitSummary summary = getLastHealthFacilityVisitSummary();
        if (summary != null) {
            PNCHealthFacilityVisitRule visitRule = getNextPNCHealthFacilityVisit(summary.getDeliveryDate(), summary.getLastVisitDate());

            if (visitRule != null && visitRule.getVisitName() != null) {

                int visit_num;
                switch (visitRule.getVisitName()) {
                    case "1":
                        visit_num = 1;
                        break;
                    case "7":
                        visit_num = 2;
                        break;
                    case "42":
                        visit_num = 3;
                        break;
                    default:
                        visit_num = 1;
                        break;
                }

                BaseAncHomeVisitAction action = getBuilder(MessageFormat.format(context.getString(R.string.pnc_health_facility_visit_mother), visitRule.getVisitName()))
                        .withOptional(false)
                        .withDetails(details)
                        .withBaseEntityID(memberObject.getBaseEntityId())
                        .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getHealthFacilityVisit())
                        .withHelper(new PNCHealthFacilityVisitHelper(visitRule, visit_num))
                        .build();
                actionList.put(MessageFormat.format(context.getString(R.string.pnc_health_facility_visit_mother), visitRule.getVisitName()), action);

            }
        }
    }

    protected void evaluateChildVaccineCard(Person baby) throws Exception {
        // if not given and less than 1 yr
        if (getAgeInDays(baby.getDob()) <= 28) {

            Map<String, List<VisitDetail>> details = getDetails(baby.getBaseEntityID(), Constants.EventType.VACCINE_CARD_RECEIVED);

            BaseAncHomeVisitAction action = getBuilder(MessageFormat.format(context.getString(R.string.pnc_child_vaccine_card_recevied), baby.getFullName()))
                    .withOptional(false)
                    .withDetails(details)
                    .withBaseEntityID(baby.getBaseEntityID())
                    .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                    .withDestinationFragment(BaseAncHomeVisitFragment.getInstance(view, Constants.JSON_FORM.PNC_HOME_VISIT.getVaccineCard(), null, details, null))
                    .withHelper(new VaccineCardHelper(baby.getDob()))
                    .build();
            actionList.put(MessageFormat.format(context.getString(R.string.pnc_child_vaccine_card_recevied), baby.getFullName()), action);
        }
    }
    //bootstrap dependencies
    //native code....so

    @VisibleForTesting
    public List<VaccineWrapper> getWrappers(Person baby) {
        return VaccineScheduleUtil.getChildDueVaccines(baby.getBaseEntityID(), baby.getDob(), 0);
    }

    protected void evaluateImmunization(Person baby) throws Exception {
        if (getAgeInDays(baby.getDob()) <= 28) {
            List<VaccineWrapper> wrappers = getWrappers(baby);
            if (wrappers.size() > 0) {
                List<VaccineDisplay> displays = new ArrayList<>();
                for (VaccineWrapper vaccineWrapper : wrappers) {
                    VaccineDisplay display = new VaccineDisplay();
                    display.setVaccineWrapper(vaccineWrapper);
                    display.setStartDate(baby.getDob());
                    display.setEndDate(new Date());
                    displays.add(display);
                }

                Map<String, List<VisitDetail>> details = getDetails(baby.getBaseEntityID(), Constants.EventType.IMMUNIZATION_VISIT);

                BaseAncHomeVisitAction action = getBuilder(MessageFormat.format(context.getString(R.string.pnc_immunization_at_birth), baby.getFullName()))
                        .withOptional(false)
                        .withDetails(details)
                        .withBaseEntityID(baby.getBaseEntityID())
                        .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                        .withDestinationFragment(BaseHomeVisitImmunizationFragment.getInstance(view, baby.getBaseEntityID(), details, displays))
                        .withHelper(new ImmunizationActionHelper(context, wrappers))
                        .build();
                actionList.put(MessageFormat.format(context.getString(R.string.pnc_immunization_at_birth), baby.getFullName()), action);
            }
        }
    }

    private void evaluateUmbilicalCord(Person baby) throws Exception {
        if (getAgeInDays(baby.getDob()) <= 28) {

            Map<String, List<VisitDetail>> details = getDetails(baby.getBaseEntityID(), Constants.EventType.UMBILICAL_CORD_CARE);

            BaseAncHomeVisitAction action = getBuilder(MessageFormat.format(context.getString(R.string.pnc_umblicord_care_child), baby.getFullName()))
                    .withOptional(false)
                    .withDetails(details)
                    .withBaseEntityID(baby.getBaseEntityID())
                    .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                    .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getUmbilicalCord())
                    .withHelper(new UmbilicalCordHelper())
                    .build();
            actionList.put(MessageFormat.format(context.getString(R.string.pnc_umblicord_care_child), baby.getFullName()), action);
        }
    }


    @VisibleForTesting
    public Map<String, ServiceWrapper> getWrapperMap(Person baby) {
        return RecurringServiceUtil.getRecurringServices(
                baby.getBaseEntityID(),
                new DateTime(baby.getDob()),
                Constants.SERVICE_GROUPS.CHILD
        );
    }

    private void evaluateExclusiveBreastFeeding(Person baby) throws Exception {
        if (getAgeInDays(baby.getDob()) <= 28) {

            Map<String, ServiceWrapper> serviceWrapperMap = getWrapperMap(baby);

            ServiceWrapper serviceWrapper = serviceWrapperMap.get("Exclusive breastfeeding");
            if (serviceWrapper == null) return;

            Alert alert = serviceWrapper.getAlert();
            if (alert == null || !new LocalDate().isAfter(new LocalDate(alert.startDate())))
                return;

            final String serviceIteration = serviceWrapper.getName().substring(serviceWrapper.getName().length() - 1);

            String title = MessageFormat.format(context.getString(R.string.pnc_exclusive_breastfeeding), baby.getFullName());

            // alert if overdue after 14 days
            boolean isOverdue = new LocalDate().isAfter(new LocalDate(alert.startDate()).plusDays(14));
            String dueState = !isOverdue ? context.getString(R.string.due) : context.getString(R.string.overdue);

            ExclusiveBreastFeedingAction helper = new ExclusiveBreastFeedingAction(context, alert);
            JSONObject jsonObject = getFormJson(Constants.JSON_FORM.PNC_HOME_VISIT.getExclusiveBreastFeeding(), memberObject.getBaseEntityId());

            Map<String, List<VisitDetail>> details = getDetails(baby.getBaseEntityID(), Constants.EventType.EXCLUSIVE_BREASTFEEDING);

            BaseAncHomeVisitAction action = getBuilder(title)
                    .withHelper(helper)
                    .withDetails(details)
                    .withOptional(false)
                    .withBaseEntityID(baby.getBaseEntityID())
                    .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                    .withPayloadType(BaseAncHomeVisitAction.PayloadType.SERVICE)
                    .withPayloadDetails(MessageFormat.format("Exclusive_breastfeeding{0}", serviceIteration))
                    .withDestinationFragment(BaseAncHomeVisitFragment.getInstance(view, null, jsonObject, details, serviceIteration))
                    .withScheduleStatus(!isOverdue ? BaseAncHomeVisitAction.ScheduleStatus.DUE : BaseAncHomeVisitAction.ScheduleStatus.OVERDUE)
                    .withSubtitle(MessageFormat.format("{0} {1}", dueState, DateTimeFormat.forPattern("dd MMM yyyy").print(new DateTime(serviceWrapper.getVaccineDate()))))
                    .build();

            // don't show if its after now
            if (!serviceWrapper.getVaccineDate().isAfterNow())
                actionList.put(MessageFormat.format(context.getString(R.string.pnc_exclusive_breastfeeding), baby.getFullName()), action);

        }
    }

    protected void evaluateBirthCertForm(Person person) throws Exception {
        PncBaby baby = (PncBaby) person;
        String title = MessageFormat.format(context.getString(R.string.pnc_birth_certification), baby.getFullName());
        hasBirthCert = getBirthCert(person);

        if (!hasBirthCert) {
            Map<String, List<VisitDetail>> details = getDetails(baby.getBaseEntityID(), Constants.EventType.BIRTH_CERTIFICATION);

            BaseAncHomeVisitAction action = getBuilder(title)
                    .withOptional(false)
                    .withDetails(details)
                    .withBaseEntityID(baby.getBaseEntityID())
                    .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                    .withHelper(new DefaultChildHomeVisitInteractorFlv.BirthCertHelper(baby.getDob()))
                    .withFormName(Constants.JSON_FORM.getBirthCertification())
                    .build();

            actionList.put(MessageFormat.format(context.getString(R.string.pnc_birth_certification), baby.getFullName()), action);
        }
    }

    private void evaluateKangarooMotherCare(Person person) throws Exception {
        PncBaby baby = (PncBaby) person;
        if (baby.getLbw().equalsIgnoreCase("yes")) {

            Map<String, List<VisitDetail>> details = getDetails(person.getBaseEntityID(), Constants.EventType.KANGAROO_CARE);

            String title = MessageFormat.format(context.getString(R.string.pnc_kangaroo_mother_care), baby.getFullName());
            BaseAncHomeVisitAction action = getBuilder(title)
                    .withOptional(false)
                    .withDetails(details)
                    .withBaseEntityID(person.getBaseEntityID())
                    .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                    .withDestinationFragment(BaseAncHomeVisitFragment.getInstance(view, Constants.JSON_FORM.PNC_HOME_VISIT.getKangarooCare(), null, details, null))
                    .withHelper(new KangarooHelper())
                    .build();
            actionList.put(title, action);
        }
    }

    private void evaluateFamilyPlanning() throws Exception {
        BaseAncHomeVisitAction action = getBuilder(context.getString(R.string.pnc_family_planning))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getFamilyPlanning())
                .withHelper(new FamilyPlanningHelper(context, parsedDate))
                .build();
        actionList.put(context.getString(R.string.pnc_family_planning), action);
    }

    private void evaluateObservationAndIllnessMother() throws Exception {
        BaseAncHomeVisitAction action = getBuilder(context.getString(R.string.pnc_observation_and_illness_mother))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.ANC_HOME_VISIT.getObservationAndIllness())
                .withHelper(new ObservationAction(parsedDate))
                .build();
        actionList.put(context.getString(R.string.pnc_observation_and_illness_mother), action);
    }

    private void evaluateObservationAndIllnessBaby(Person baby) throws Exception {
        if (getAgeInDays(baby.getDob()) <= 28) {
            Map<String, List<VisitDetail>> details = getDetails(baby.getBaseEntityID(), Constants.EventType.OBSERVATIONS_AND_ILLNESS);

            BaseAncHomeVisitAction action = getBuilder(MessageFormat.format(context.getString(R.string.pnc_observation_and_illness_baby), baby.getFullName()))
                    .withOptional(true)
                    .withDetails(details)
                    .withBaseEntityID(baby.getBaseEntityID())
                    .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                    .withFormName(Constants.JSON_FORM.ANC_HOME_VISIT.getObservationAndIllness())
                    .withHelper(new ObservationAction(parsedDate))
                    .build();
            actionList.put(MessageFormat.format(context.getString(R.string.pnc_observation_and_illness_baby), baby.getFullName()), action);
        }
    }

    protected int getAgeInDays(Date dob) {
        return Days.daysBetween(new DateTime(dob).toLocalDate(), new DateTime().toLocalDate()).getDays();
    }

    protected Boolean getBirthCert(Person person) {
        return VisitDao.memberHasBirthCert(person.getBaseEntityID());
    }

    private class VaccineCardHelper extends HomeVisitActionHelper {
        private String vaccine_card;
        private Date dob;

        public VaccineCardHelper(Date dob) {
            this.dob = dob;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return MessageFormat.format("{0} {1}", context.getString(R.string.due), new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(dob));
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                vaccine_card = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "vaccine_card");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public String evaluateSubTitle() {
            return vaccine_card.equalsIgnoreCase("Yes") ? context.getString(R.string.yes) : context.getString(R.string.no);
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(vaccine_card)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            }

            if (vaccine_card.equalsIgnoreCase("Yes")) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            } else if (vaccine_card.equalsIgnoreCase("No")) {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.PENDING;
            }
        }
    }

    private class UmbilicalCordHelper extends HomeVisitActionHelper {
        private String cord_care;

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                cord_care = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "cord_care");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public String evaluateSubTitle() {
            if ("No products".equalsIgnoreCase(cord_care)) {
                return context.getString(R.string.no_products);
            } else if ("Chlorhexidine".equalsIgnoreCase(cord_care)) {
                return context.getString(R.string.chlorhexidine);
            } else if ("Other".equalsIgnoreCase(cord_care)) {
                return context.getString(R.string.other);
            }
            return "";
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(cord_care)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            }

            if ("Chlorhexidine".equalsIgnoreCase(cord_care)) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            }
        }
    }

    private class FamilyPlanningHelper extends HomeVisitActionHelper {
        private String fp_counseling;
        private String fp_method;
        private String fp_start_date;
        private Date start_date;
        private String dob;
        private JSONObject jsonObject;
        private Context context;

        public FamilyPlanningHelper(Context context, String dob) {
            this.dob = dob;
            this.context = context;
        }

        @Override
        public void onJsonFormLoaded(String jsonString, Context context, Map<String, List<VisitDetail>> details) {
            try {
                if (StringUtils.isNotBlank(dob)) {
                    jsonObject = new JSONObject(jsonString);
                    JSONArray fields = JsonFormUtils.fields(jsonObject);
                    JSONObject dateOfIllness = JsonFormUtils.getFieldJSONObject(fields, "fp_start_date");
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
                fp_counseling = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "fp_counseling");
                fp_method = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "fp_method");
                fp_start_date = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "fp_start_date");

                if (StringUtils.isNotBlank(fp_start_date)) {
                    start_date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(fp_start_date);
                }
            } catch (JSONException e) {
                Timber.e(e);
            } catch (ParseException e) {
                Timber.e(e);
            }
        }

        @Override
        public String evaluateSubTitle() {
            StringBuilder builder = new StringBuilder();
            builder.append(
                    MessageFormat.format("{0}: {1}\n",
                            context.getString(R.string.fp_counseling),
                            "Yes".equalsIgnoreCase(fp_counseling) ? context.getString(R.string.done).toLowerCase() : context.getString(R.string.not_done).toLowerCase()
                    )
            );

            if (StringUtils.isNotBlank(fp_method)) {
                String method = "";
                switch (fp_method) {
                    case "None":
                        method = context.getString(R.string.none);
                        break;
                    case "Abstinence":
                        method = context.getString(R.string.abstinence);
                        break;
                    case "Condom":
                        method = context.getString(R.string.condom);
                        break;
                    case "Tablets":
                        method = context.getString(R.string.tablets);
                        break;
                    case "Injectable":
                        method = context.getString(R.string.injectable);
                        break;
                    case "IUD":
                        method = context.getString(R.string.iud);
                        break;
                    case "Implant":
                        method = context.getString(R.string.implant);
                        break;
                    case "Other":
                        method = context.getString(R.string.other);
                        break;
                    default:
                        break;
                }

                builder.append(
                        MessageFormat.format("{0}: {1}", context.getString(R.string.fp_method_chosen), method)
                );
            }

            if (StringUtils.isNotBlank(fp_start_date)) {
                builder.append(
                        MessageFormat.format("\n{0}: {1}",
                                context.getString(R.string.fp_method_start_date),
                                new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(start_date)
                        )
                );
            }

            return builder.toString();
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(fp_counseling)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            }


            if ("Yes".equalsIgnoreCase(fp_counseling)) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            }
        }
    }

    private class PNCHealthFacilityVisitHelper implements BaseAncHomeVisitAction.AncHomeVisitActionHelper {
        private Context context;
        private String jsonPayload;

        private PNCHealthFacilityVisitRule visitRule;
        private int visit_num;

        private String pnc_visit;
        private String pnc_hf_visit_date;
        private String baby_weight;
        private String baby_temp;

        private BaseAncHomeVisitAction.ScheduleStatus scheduleStatus;
        private String subTitle;
        private Date date;

        public PNCHealthFacilityVisitHelper(PNCHealthFacilityVisitRule visitRule, int visit_num) {
            this.visitRule = visitRule;
            this.visit_num = visit_num;
        }

        @Override
        public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
            this.context = context;
            this.jsonPayload = jsonPayload;
        }

        @Override
        public String getPreProcessed() {
            try {
                scheduleStatus = (visitRule.getOverDueDate().toLocalDate().isBefore(LocalDate.now())) ? BaseAncHomeVisitAction.ScheduleStatus.OVERDUE : BaseAncHomeVisitAction.ScheduleStatus.DUE;
                String due = (visitRule.getOverDueDate().toLocalDate().isBefore(LocalDate.now())) ? context.getString(R.string.overdue) : context.getString(R.string.due);

                subTitle = MessageFormat.format("{0} {1}", due, DateTimeFormat.forPattern("dd MMM yyyy").print(visitRule.getOverDueDate().toLocalDate()));
                JSONObject jsonObject = new JSONObject(jsonPayload);
                JSONArray fields = JsonFormUtils.fields(jsonObject);


                String title = jsonObject.getJSONObject(JsonFormConstants.STEP1).getString(JsonFormConstants.STEP_TITLE);
                jsonObject.getJSONObject(JsonFormConstants.STEP1).put("title", MessageFormat.format(title, visitRule.getVisitName()));

                JSONObject pnc_visit = JsonFormUtils.getFieldJSONObject(fields, "pnc_visit_{0}");
                pnc_visit.put(JsonFormConstants.KEY, MessageFormat.format("pnc_visit_{0}", visit_num));
                pnc_visit.put("hint",
                        MessageFormat.format(pnc_visit.getString(JsonFormConstants.HINT),
                                visitRule.getVisitName(),
                                DateTimeFormat.forPattern("dd MMM yyyy").print(visitRule.getDueDate()
                                )
                        )
                );

                JSONObject pnc_visit_date = JsonFormUtils.getFieldJSONObject(fields, "pnc_hf_visit{0}_date");
                if (StringUtils.isNotBlank(parsedDate)) pnc_visit_date.put(JsonFormConstants.MIN_DATE, parsedDate);
                pnc_visit_date.put(JsonFormConstants.KEY, MessageFormat.format("pnc_hf_visit{0}_date", visit_num));
                pnc_visit_date.put("hint",
                        MessageFormat.format(pnc_visit_date.getString(JsonFormConstants.HINT), visitRule.getVisitName())
                );

                JSONObject pnc_hf_next_visit_date = JsonFormUtils.getFieldJSONObject(fields, Constants.FORM_SUBMISSION_FIELD.pncHfNextVisitDateFieldType);
                pnc_hf_next_visit_date.put(JsonFormConstants.VALUE, DateTimeFormat.forPattern("dd-MM-yyyy").print(visitRule.getDueDate()));

                updateObjectRelevance(pnc_visit_date);
                updateObjectRelevance(JsonFormUtils.getFieldJSONObject(fields, "baby_weight"));
                updateObjectRelevance(JsonFormUtils.getFieldJSONObject(fields, "baby_temp"));

                return jsonObject.toString();
            } catch (Exception e) {
                Timber.e(e);
            }
            return null;
        }

        private void updateObjectRelevance(JSONObject jsonObject) throws JSONException {
            JSONObject relevance = jsonObject.getJSONObject(JsonFormConstants.RELEVANCE);
            JSONObject step = relevance.getJSONObject("step1:pnc_visit_{0}");
            relevance.put(MessageFormat.format("step1:pnc_visit_{0}", visit_num), step);
            relevance.remove("step1:pnc_visit_{0}");
        }

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                pnc_visit = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, MessageFormat.format("pnc_visit_{0}", visit_num));
                pnc_hf_visit_date = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, MessageFormat.format("pnc_hf_visit{0}_date", visit_num));
                baby_weight = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "baby_weight");
                baby_temp = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "baby_temp");

                if (StringUtils.isNotBlank(pnc_hf_visit_date)) {
                    date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(pnc_hf_visit_date);
                }
            } catch (JSONException e) {
                Timber.e(e);
            } catch (ParseException e) {
                Timber.e(e);
            }
        }

        @Override
        public BaseAncHomeVisitAction.ScheduleStatus getPreProcessedStatus() {
            return scheduleStatus;
        }

        @Override
        public String getPreProcessedSubTitle() {
            return subTitle;
        }

        @Override
        public String postProcess(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);

                JSONArray field = JsonFormUtils.fields(jsonObject);
                JSONObject confirmed_visits = JsonFormUtils.getFieldJSONObject(field, "confirmed_health_facility_visits");
                JSONObject facility_visit_date = JsonFormUtils.getFieldJSONObject(field, "last_health_facility_visit_date");
                JSONObject pnc_hf_next_visit_date = JsonFormUtils.getFieldJSONObject(field, Constants.FORM_SUBMISSION_FIELD.pncHfNextVisitDateFieldType);
                pnc_hf_visit_date = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, MessageFormat.format("pnc_hf_visit{0}_date", visit_num));

                String count = String.valueOf(visit_num);
                String value = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, MessageFormat.format("pnc_visit_{0}", visit_num));
                if (value.equalsIgnoreCase("Yes")) {
                    count = String.valueOf(visit_num + 1);
                    facility_visit_date.put(JsonFormConstants.VALUE, pnc_hf_visit_date);
                } else {
                    facility_visit_date.remove(JsonFormConstants.VALUE);
                    pnc_hf_next_visit_date.put(JsonFormConstants.VALUE, "");
                }

                if (!confirmed_visits.getString(JsonFormConstants.VALUE).equals(count)) {
                    confirmed_visits.put(JsonFormConstants.VALUE, count);
                    return jsonObject.toString();
                }

            } catch (JSONException e) {
                Timber.e(e);
            }
            return null;
        }

        @Override
        public String evaluateSubTitle() {
            if (pnc_visit == null) {
                return null;
            }

            if (pnc_visit.equalsIgnoreCase("No")) {
                return context.getString(R.string.visit_not_done).replace("\n", "");
            }


            StringBuilder builder = new StringBuilder();
            builder.append(MessageFormat.format("{0}: {1}\n",
                    context.getString(R.string.date),
                    new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date))
            );
            builder.append(MessageFormat.format("{0}: {1} {2}\n",
                    context.getString(R.string.babys_weight),
                    baby_weight,
                    context.getString(R.string.kg))
            );
            builder.append(MessageFormat.format("{0}: {1} {2}",
                    context.getString(R.string.babys_temperature),
                    baby_temp,
                    context.getString(R.string.degrees_centigrade))
            );

            return builder.toString();
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(pnc_visit)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            }

            if (pnc_visit.equalsIgnoreCase("Yes")) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            } else if (pnc_visit.equalsIgnoreCase("No")) {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.PENDING;
            }
        }

        @Override
        public void onPayloadReceived(BaseAncHomeVisitAction baseAncHomeVisitAction) {
            Timber.d("onPayloadReceived");
        }
    }

    private class KangarooHelper extends HomeVisitActionHelper {
        private String kangaroo;

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                kangaroo = org.smartregister.chw.util.JsonFormUtils.getValue(jsonObject, "kangaroo");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public String evaluateSubTitle() {
            if (StringUtils.isBlank(kangaroo)) {
                return null;
            }

            return kangaroo.equalsIgnoreCase("Yes") ? context.getString(R.string.yes) : context.getString(R.string.no);
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(kangaroo)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            }

            if (kangaroo.equalsIgnoreCase("Yes")) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            } else if (kangaroo.equalsIgnoreCase("No")) {
                return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
            } else {
                return BaseAncHomeVisitAction.Status.PENDING;
            }
        }
    }
}

