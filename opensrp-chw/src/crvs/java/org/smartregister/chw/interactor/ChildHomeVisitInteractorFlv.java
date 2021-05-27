package org.smartregister.chw.interactor;


import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.immunization.domain.ServiceWrapper;

import java.util.Map;

import timber.log.Timber;

public class ChildHomeVisitInteractorFlv extends DefaultChildHomeVisitInteractorFlv {

    @Override
    protected void bindEvents(Map<String, ServiceWrapper> serviceWrapperMap) throws BaseAncHomeVisitAction.ValidationException {
        try {
            evaluateChildVaccineCard();
            evaluateImmunization();
            evaluateExclusiveBreastFeeding(serviceWrapperMap);
            evaluateVitaminA(serviceWrapperMap);
            evaluateDeworming(serviceWrapperMap);
            evaluateBirthCertForm();
            evaluateMUAC();
            evaluateDietary();
            evaluateECD();
            evaluateLLITN();
            evaluateObsAndIllness();
        } catch (BaseAncHomeVisitAction.ValidationException e) {
            throw (e);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

}