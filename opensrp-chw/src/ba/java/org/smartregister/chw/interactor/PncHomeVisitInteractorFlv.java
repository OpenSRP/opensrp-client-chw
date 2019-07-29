package org.smartregister.chw.interactor;

import org.smartregister.chw.R;
import org.smartregister.chw.actionhelper.DangerSignsAction;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.util.Constants;

import java.util.LinkedHashMap;

import timber.log.Timber;

public class PncHomeVisitInteractorFlv extends DefaultPncHomeVisitInteractorFlv {

    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        actionList = new LinkedHashMap<>();
        context = view.getContext();
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EventType.PNC_HOME_VISIT);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        try {
            evaluateDangerSignsMother();
            evaluateDangerSignsBaby();
            evaluatePNCHealthFacilityVisitOne();
            evaluatePNCHealthFacilityVisitTwo();
            evaluatePNCHealthFacilityVisitThree();
            evaluatePNCHealthFacilityVisitFour();
            evaluateFamilyPlanning();
            evaluateImmunization();
            evaluateExclusiveBreastFeeding();
            evaluateCounselling();
            evaluateNutritionStatus();
            evaluateMalariaPrevention();
            evaluateObsIllnessMother();
            evaluateObsIllnessBaby();
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            throw (e);
        } catch (Exception e) {
            Timber.e(e);
        }
        return actionList;
    }

    private void evaluateDangerSignsMother() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_danger_signs_mother))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_danger_signs_mother), action);
    }

    private void evaluateDangerSignsBaby() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_danger_signs_baby))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_danger_signs_baby), action);
    }

    private void evaluatePNCHealthFacilityVisitOne() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_health_facility_visit_within_fourty_eight_hours))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_health_facility_visit_within_fourty_eight_hours), action);
    }

    private void evaluatePNCHealthFacilityVisitTwo() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_health_facility_visit_days_three_to_seven))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_health_facility_visit_days_three_to_seven), action);
    }

    private void evaluatePNCHealthFacilityVisitThree() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_health_facility_visit_days_eight_to_twenty_eight))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_health_facility_visit_days_eight_to_twenty_eight), action);
    }

    private void evaluatePNCHealthFacilityVisitFour() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_health_facility_visit_days_twenty_nine_to_forty_two))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_health_facility_visit_days_twenty_nine_to_forty_two), action);
    }

    private void evaluateFamilyPlanning() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_family_planning))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_family_planning), action);
    }

    private void evaluateImmunization() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_immunization_at_birth))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_immunization_at_birth), action);
    }

    private void evaluateExclusiveBreastFeeding() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_exclusive_breastfeeding))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_exclusive_breastfeeding), action);
    }

    private void evaluateCounselling() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_counselling))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_counselling), action);
    }

    private void evaluateNutritionStatus() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_nutrition_status))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_nutrition_status), action);
    }

    private void evaluateMalariaPrevention() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_malaria_prevention))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_malaria_prevention), action);
    }

    private void evaluateObsIllnessMother() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_observation_and_illness_mother))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_observation_and_illness_mother), action);
    }

    private void evaluateObsIllnessBaby() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_observation_and_illness_baby))
                .withOptional(true)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_observation_and_illness_baby), action);
    }


}
