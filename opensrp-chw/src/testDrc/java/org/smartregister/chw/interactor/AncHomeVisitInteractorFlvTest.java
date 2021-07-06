package org.smartregister.chw.interactor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.util.Constants;

import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class AncHomeVisitInteractorFlvTest extends BaseHomeVisitInteractorFlvTest {

    private AncHomeVisitInteractorFlv interactor;
    @Mock
    private Map<String, List<VisitDetail>> details;

    @Mock
    protected Context mContext;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        interactor = Mockito.mock(AncHomeVisitInteractorFlv.class, Mockito.CALLS_REAL_METHODS);

        CoreLibrary.init(mContext);
        ReflectionHelpers.setField(interactor, "memberObject", memberObject);
        Mockito.doReturn(builder).when(interactor).getBuilder(Mockito.any());
        Mockito.doReturn(ancHomeVisitAction).when(builder).build();
        ReflectionHelpers.setField(interactor, "context", context);
        ReflectionHelpers.setField(interactor, "view", view);
        ReflectionHelpers.setField(interactor, "details", details);

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

        Mockito.doReturn(ancHomeVisitAction).when(builder).build();
        Constants.JSON_FORM.setLocaleAndAssetManager(locale, assetManager);
        ReflectionHelpers.setField(interactor, "actionList", actionList);

        CoreConstants.JSON_FORM.setLocaleAndAssetManager(locale, assetManager);
        Mockito.doReturn(title).when(context).getString(Mockito.anyInt());
        Mockito.doReturn(ancHomeVisitAction).when(builder).build();
    }


    @Test
    public void testEvaluateDeliveryKit() {
        ReflectionHelpers.callInstanceMethod(interactor, "evaluateDeliveryKit");

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateAncCard() {
        ReflectionHelpers.callInstanceMethod(interactor, "evaluateANCCard");

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }

    @Test
    public void testEvaluateDangerSigns() {
        ReflectionHelpers.callInstanceMethod(interactor, "evaluateDangerSigns");

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }
}
