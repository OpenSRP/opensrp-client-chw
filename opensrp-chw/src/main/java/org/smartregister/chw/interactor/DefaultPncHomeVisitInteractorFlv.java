package org.smartregister.chw.interactor;

import android.content.Context;

import org.smartregister.chw.R;
import org.smartregister.chw.actionhelper.DangerSignsAction;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.util.Constants;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class DefaultPncHomeVisitInteractorFlv implements PncHomeVisitInteractor.Flavor {

    protected LinkedHashMap<String, BaseAncHomeVisitAction> actionList;
    protected Context context;
    protected Map<String, List<VisitDetail>> details = null;

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
            evaluatePNCHealthFacilityVisit();
            evaluateChildVaccineCard();
            evaluateImmunization();
            evaluateUmbilicalCord();
            evaluateExclusiveBreastFeeding();
            evaluateKangerooMotherCare();
            evaluateFamilyPlanning();
            evaluateObservationAndIllnessMother();
            evaluateObservationAndIllnessBaby();
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

    private void evaluatePNCHealthFacilityVisit() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_health_facility_visit))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_health_facility_visit), action);
    }

    private void evaluateChildVaccineCard() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_child_vaccine_card_recevied))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_child_vaccine_card_recevied), action);
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

    private void evaluateUmbilicalCord() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_umblicord_care))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_umblicord_care), action);
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

    private void evaluateKangerooMotherCare() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_kangeroo_mother_care))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_kangeroo_mother_care), action);
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

    private void evaluateObservationAndIllnessMother() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_observation_and_illness_mother))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_observation_and_illness_mother), action);
    }

    private void evaluateObservationAndIllnessBaby() throws Exception {
        BaseAncHomeVisitAction action = new BaseAncHomeVisitAction.Builder(context, context.getString(R.string.pnc_observation_and_illness_baby))
                .withOptional(false)
                .withDetails(details)
                .withFormName(Constants.JSON_FORM.PNC_HOME_VISIT.getDangerSigns())
                .withHelper(new DangerSignsAction())
                .build();
        actionList.put(context.getString(R.string.pnc_observation_and_illness_baby), action);
    }

}
