package org.smartregister.chw.interactor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.utils.CoreConstants;

public class DefaultAncHomeVisitInteractorFlvTest extends BaseHomeVisitInteractorFlvTest {

    private DefaultAncHomeVisitInteractorFlv interactor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        interactor = Mockito.mock(DefaultAncHomeVisitInteractorFlv.class, Mockito.CALLS_REAL_METHODS);

        ReflectionHelpers.setField(interactor, "editMode", false);
        ReflectionHelpers.setField(interactor, "context", context);
        ReflectionHelpers.setField(interactor, "memberObject", memberObject);
        ReflectionHelpers.setField(interactor, "actionList", actionList);

        CoreConstants.JSON_FORM.setLocaleAndAssetManager(locale, assetManager);
        Mockito.doReturn(title).when(context).getString(Mockito.anyInt());
    }

    @Test
    public void testEvaluateDangerSigns() {
        ReflectionHelpers.callInstanceMethod(interactor, "evaluateDangerSigns");

        Mockito.verify(actionList).put(Mockito.anyString(), Mockito.any(BaseAncHomeVisitAction.class));
    }
}
