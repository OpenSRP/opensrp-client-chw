package org.smartregister.chw.interactor;

import androidx.annotation.VisibleForTesting;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.smartregister.chw.R;
import org.smartregister.chw.actionhelper.DeliveryKitAction;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.contract.BaseAncHomeVisitContract;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.fragment.BaseAncHomeVisitFragment;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.model.VaccineTaskModel;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.ContactUtil;

import java.util.LinkedHashMap;
import java.util.Map;

import timber.log.Timber;


public class AncHomeVisitInteractorFlv extends DefaultAncHomeVisitInteractorFlv {

    @Override
    public LinkedHashMap<String, BaseAncHomeVisitAction> calculateActions(BaseAncHomeVisitContract.View view, MemberObject memberObject, BaseAncHomeVisitContract.InteractorCallBack callBack) throws BaseAncHomeVisitAction.ValidationException {
        this.memberObject = memberObject;
        this.view = view;
        actionList = new LinkedHashMap<>();
        context = view.getContext();
        editMode = view.getEditMode();
        // get the preloaded data
        if (view.getEditMode()) {
            Visit lastVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EventType.ANC_HOME_VISIT);
            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }

        // get contact schedule
        Map<Integer, LocalDate> dateMap = ContactUtil.getContactSchedule(memberObject);

        // get vaccine schedule if ga > 13
        VaccineTaskModel vaccineTaskModel = null;

        DateTime lastMenstrualPeriod = DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(memberObject.getLastMenstrualPeriod());
        int ga = Days.daysBetween(lastMenstrualPeriod, new DateTime()).getDays() / 7;

        if (ga >= 13) {
            vaccineTaskModel = getWomanVaccine(memberObject.getBaseEntityId(), lastMenstrualPeriod, getNotGivenVaccines());
        }

        try {
            Constants.JSON_FORM.setLocaleAndAssetManager(ChwApplication.getCurrentLocale(), ChwApplication.getInstance().getApplicationContext().getAssets());
            evaluateDangerSigns();
            evaluateANCCounseling(dateMap);
            evaluateSleepingUnderLLITN();
            evaluateANCCard();
            evaluateDeliveryKit();
            evaluateHealthFacilityVisit(dateMap);
            evaluateTTImmunization(vaccineTaskModel);
            evaluateIPTP();
            evaluateObservation();
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            throw (e);
        } catch (Exception e) {
            Timber.e(e);
        }

        return actionList;
    }

    protected void evaluateDeliveryKit() throws BaseAncHomeVisitAction.ValidationException {
        if (memberObject.getDeliveryKit() != null && memberObject.getDeliveryKit().equalsIgnoreCase("Yes") && !editMode) {
            return;
        }

        BaseAncHomeVisitAction deliveryKitAction = getBuilder(context.getString(R.string.anc_home_visit_delivery_kit_received))
                .withOptional(false)
                .withDetails(details)
                .withHelper(new DeliveryKitAction())
                .withDestinationFragment(BaseAncHomeVisitFragment.getInstance(view, Constants.AncHomeVisitUtil.getDeliveryKitReceived(), null, details, null))
                .build();

        actionList.put(context.getString(R.string.anc_home_visit_delivery_kit_received), deliveryKitAction);

    }

    @VisibleForTesting
    public BaseAncHomeVisitAction.Builder getBuilder(String title) {
        return new BaseAncHomeVisitAction.Builder(context, title);
    }
}

