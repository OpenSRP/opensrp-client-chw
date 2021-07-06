package org.smartregister.chw.interactor;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.dao.AbstractDao;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.domain.jsonmapping.Vaccine;
import org.smartregister.immunization.domain.jsonmapping.VaccineGroup;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.repository.Repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class DefaultChildHomeVisitInteractorFlvTest extends BaseHomeVisitInteractorFlvTest {

    private DefaultChildHomeVisitInteractorFlv interactor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        interactor = Mockito.mock(DefaultChildHomeVisitInteractorFlv.class, Mockito.CALLS_REAL_METHODS);

        ReflectionHelpers.setField(interactor, "editMode", false);
        ReflectionHelpers.setField(interactor, "context", context);
        ReflectionHelpers.setField(interactor, "memberObject", memberObject);
        ReflectionHelpers.setField(interactor, "actionList", actionList);
        ReflectionHelpers.setField(interactor, "vaccineCardReceived", false);

        Date dob = LocalDate.now().minusDays(62).toDate();
        ReflectionHelpers.setField(interactor, "dob", dob);

        CoreConstants.JSON_FORM.setLocaleAndAssetManager(locale, assetManager);
        Mockito.doReturn(title).when(context).getString(Mockito.anyInt());

        Mockito.doReturn(builder).when(builder).withOptional(Mockito.anyBoolean());
        Mockito.doReturn(builder).when(builder).withBaseEntityID(Mockito.any());
        Mockito.doReturn(builder).when(builder).withDetails(Mockito.any());
        Mockito.doReturn(builder).when(builder).withDestinationFragment(Mockito.any());
        Mockito.doReturn(builder).when(builder).withHelper(Mockito.any());
        Mockito.doReturn(builder).when(builder).withProcessingMode(Mockito.any());
        Mockito.doReturn(builder).when(builder).withFormName(Mockito.any());
        Mockito.doReturn(builder).when(builder).withJsonPayload(Mockito.any());
        Mockito.doReturn(builder).when(builder).withPayloadType(Mockito.any());
        Mockito.doReturn(builder).when(builder).withPayloadDetails(Mockito.any());
        Mockito.doReturn(builder).when(builder).withScheduleStatus(Mockito.any());
        Mockito.doReturn(builder).when(builder).withSubtitle(Mockito.any());

        Mockito.doReturn(builder).when(interactor).getBuilder(Mockito.any());

        Mockito.doReturn(ancHomeVisitAction).when(builder).build();
        Mockito.doReturn(visitRepository).when(interactor).getVisitRepository();

        JSONObject jsonObject = Mockito.mock(JSONObject.class);
        Mockito.doReturn("").when(jsonObject).toString();
        Mockito.doReturn(jsonObject).when(interactor).getFormJson(Mockito.anyString());
        Mockito.doReturn(jsonObject).when(interactor).getFormJson(Mockito.any(), Mockito.any());
    }

    @Test
    public void testCalculateActionsPopulatesServicesForChild() throws BaseAncHomeVisitAction.ValidationException {

        Mockito.doReturn(context).when(view).getContext();
        Mockito.doReturn(false).when(view).getEditMode();

        Repository repository = Mockito.mock(Repository.class);
        ReflectionHelpers.setStaticField(AbstractDao.class, "repository", repository);

        Map<String, ServiceWrapper> serviceWrapperMap = new HashMap<>();
        Mockito.doReturn(serviceWrapperMap).when(interactor).getServices();

        ReflectionHelpers.setField(interactor, "hasBirthCert", false);

        List<VaccineGroup> vaccineGroups = new ArrayList<>();
        Mockito.doReturn(vaccineGroups).when(interactor).getVaccineGroups();

        List<Vaccine> vaccines = new ArrayList<>();
        Mockito.doReturn(vaccines).when(interactor).getSpecialVaccines();

        VaccineRepository vaccineRepository = Mockito.mock(VaccineRepository.class);
        Mockito.doReturn(vaccineRepository).when(interactor).getVaccineRepo();

        LinkedHashMap<String, BaseAncHomeVisitAction> services = interactor.calculateActions(view, memberObject, callBack);

        Assert.assertTrue(services.size() > 0);
    }

    @Test
    public void testGetDetailsReturnsDBResultsEditing() {
        ReflectionHelpers.setField(interactor, "editMode", true);

        String baseID = "12345";
        String eventName = "Sample Event Name";

        Mockito.doReturn(baseID).when(memberObject).getBaseEntityId();

        ReflectionHelpers.callInstanceMethod(interactor, "getDetails"
                , ReflectionHelpers.ClassParameter.from(String.class, eventName));

        Mockito.verify(visitRepository).getLatestVisit(baseID, eventName);
    }


    @Test
    public void testEvaluateChildVaccineCard() {
        ReflectionHelpers.setField(interactor, "hasBirthCert", false);

        ReflectionHelpers.callInstanceMethod(interactor, "evaluateChildVaccineCard");

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateExclusiveBreastFeeding() {
        Map<String, ServiceWrapper> serviceWrapperMap = new HashMap<>();
        ServiceWrapper serviceWrapper = Mockito.mock(ServiceWrapper.class);
        Mockito.doReturn("Exclusive 1").when(serviceWrapper).getName();
        Mockito.doReturn(new DateTime()).when(serviceWrapper).getVaccineDate();
        serviceWrapperMap.put("Exclusive breastfeeding", serviceWrapper);

        Alert alert = Mockito.mock(Alert.class);
        Mockito.doReturn("2019-01-01").when(alert).startDate();
        Mockito.doReturn(alert).when(serviceWrapper).getAlert();

        Mockito.doReturn("My name").when(context).getString(Mockito.anyInt(), Mockito.any());


        ReflectionHelpers.callInstanceMethod(interactor, "evaluateExclusiveBreastFeeding"
                , ReflectionHelpers.ClassParameter.from(Map.class, serviceWrapperMap));

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateVitaminA() {
        Map<String, ServiceWrapper> serviceWrapperMap = new HashMap<>();
        ServiceWrapper serviceWrapper = Mockito.mock(ServiceWrapper.class);
        Mockito.doReturn("Vitamin A 1").when(serviceWrapper).getName();
        Mockito.doReturn(new DateTime()).when(serviceWrapper).getVaccineDate();
        serviceWrapperMap.put("Vitamin A", serviceWrapper);

        Alert alert = Mockito.mock(Alert.class);
        Mockito.doReturn("2019-01-01").when(alert).startDate();
        Mockito.doReturn(alert).when(serviceWrapper).getAlert();

        Mockito.doReturn("My name").when(context).getString(Mockito.anyInt(), Mockito.any());


        ReflectionHelpers.callInstanceMethod(interactor, "evaluateVitaminA"
                , ReflectionHelpers.ClassParameter.from(Map.class, serviceWrapperMap));

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateDeworming() {
        Map<String, ServiceWrapper> serviceWrapperMap = new HashMap<>();
        ServiceWrapper serviceWrapper = Mockito.mock(ServiceWrapper.class);
        Mockito.doReturn("Deworming 1").when(serviceWrapper).getName();
        Mockito.doReturn(new DateTime()).when(serviceWrapper).getVaccineDate();
        serviceWrapperMap.put("Deworming", serviceWrapper);

        Alert alert = Mockito.mock(Alert.class);
        Mockito.doReturn("2019-01-01").when(alert).startDate();
        Mockito.doReturn(alert).when(serviceWrapper).getAlert();

        Mockito.doReturn("My name").when(context).getString(Mockito.anyInt(), Mockito.any());


        ReflectionHelpers.callInstanceMethod(interactor, "evaluateDeworming"
                , ReflectionHelpers.ClassParameter.from(Map.class, serviceWrapperMap));

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateMNP() {
        Map<String, ServiceWrapper> serviceWrapperMap = new HashMap<>();
        ServiceWrapper serviceWrapper = Mockito.mock(ServiceWrapper.class);
        Mockito.doReturn("MNP 1").when(serviceWrapper).getName();
        Mockito.doReturn(new DateTime()).when(serviceWrapper).getVaccineDate();
        serviceWrapperMap.put("MNP", serviceWrapper);

        Alert alert = Mockito.mock(Alert.class);
        Mockito.doReturn("2019-01-01").when(alert).startDate();
        Mockito.doReturn(alert).when(serviceWrapper).getAlert();

        Mockito.doReturn("My name").when(context).getString(Mockito.anyInt(), Mockito.any());


        ReflectionHelpers.callInstanceMethod(interactor, "evaluateMNP"
                , ReflectionHelpers.ClassParameter.from(Map.class, serviceWrapperMap));

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateBirthCert() {
        ReflectionHelpers.setField(interactor, "hasBirthCert", false);

        ReflectionHelpers.callInstanceMethod(interactor, "evaluateBirthCertForm");

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateMUAC() {
        Date dob = LocalDate.now().minusDays(280).toDate();
        ReflectionHelpers.setField(interactor, "dob", dob);

        ReflectionHelpers.callInstanceMethod(interactor, "evaluateMUAC");

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateDietary() {
        Date dob = LocalDate.now().minusDays(280).toDate();
        ReflectionHelpers.setField(interactor, "dob", dob);

        ReflectionHelpers.callInstanceMethod(interactor, "evaluateDietary");

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateECD() {
        ReflectionHelpers.callInstanceMethod(interactor, "evaluateECD");

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateLLITN() {
        ReflectionHelpers.setField(interactor, "hasBirthCert", false);

        ReflectionHelpers.callInstanceMethod(interactor, "evaluateLLITN");

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateObsAndIllness() {
        ReflectionHelpers.setField(interactor, "hasBirthCert", false);

        ReflectionHelpers.callInstanceMethod(interactor, "evaluateObsAndIllness");

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testGetAgeInMonths() {
        int months = ReflectionHelpers.callInstanceMethod(interactor, "getAgeInMonths");
        assertEquals(2, months);
    }
}
