package org.smartregister.chw.interactor;

import org.smartregister.chw.R;
import org.smartregister.chw.actionhelper.DisabilityAction;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.util.Constants;
import org.smartregister.immunization.domain.ServiceWrapper;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class ChildHomeVisitInteractorFlv extends DefaultChildHomeVisitInteractorFlv {

    @Override
    protected void bindEvents(Map<String, ServiceWrapper> serviceWrapperMap) throws BaseAncHomeVisitAction.ValidationException {
        try {
            evaluateChildVaccineCard();
            evaluateImmunization();
            evaluateMNP(serviceWrapperMap);
            evaluateVitaminA(serviceWrapperMap);
            evaluateDeworming(serviceWrapperMap);
            evaluateECD();
            evaluateLLITN();
            evaluateObsAndIllness();
            evaluateBirthCertForm();
            evaluateDisability();
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            throw (e);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void evaluateDisability() throws Exception {

        Map<String, List<VisitDetail>> details = getDetails(Constants.EventType.DISABILITY);

        BaseAncHomeVisitAction action = getBuilder(context.getString(R.string.disability))
                .withOptional(false)
                .withDetails(details)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                .withHelper(new DisabilityAction())
                .withFormName(Constants.JSON_FORM.getDisability())
                .build();

        actionList.put(context.getString(R.string.disability), action);
    }

}
