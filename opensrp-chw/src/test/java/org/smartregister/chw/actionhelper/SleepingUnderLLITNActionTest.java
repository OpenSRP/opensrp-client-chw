package org.smartregister.chw.actionhelper;

import android.content.Context;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.chw.BaseUnitTest;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.utils.NativeFormProcessor;

import java.util.HashMap;
import java.util.Map;

public class SleepingUnderLLITNActionTest extends BaseUnitTest {

    private SleepingUnderLLITNAction action;
    private final Context context = RuntimeEnvironment.application;

    @Before
    public void setUp() {
        action = new SleepingUnderLLITNAction();
    }


    @Test
    public void testOnPayloadReceive() throws Exception {
        String formName = "anc_hv_sleeping_under_llitn";

        Map<String, Object> values = new HashMap<>();
        values.put("sleeping_llitn", "Yes");

        JSONObject jsonObject = ReadFormHelper.getFormJson(context, formName);
        NativeFormProcessor.createInstance(jsonObject)
                .populateValues(values);

        action.onPayloadReceived(jsonObject.toString());
        Assert.assertEquals("Yes", ReflectionHelpers.getField(action, "sleeping_llitn"));
    }

    @Test
    public void testEvaluateSubTitle() {
        ReflectionHelpers.setField(action, "context", context);
        ReflectionHelpers.setField(action, "sleeping_llitn", "Yes");

        Assert.assertEquals(context.getString(R.string.yes), action.evaluateSubTitle());
    }

    @Test
    public void testEvaluateStatusOnPayload() {

        Assert.assertEquals(action.evaluateStatusOnPayload(), BaseAncHomeVisitAction.Status.PENDING);

        ReflectionHelpers.setField(action, "sleeping_llitn", "Yes");
        Assert.assertEquals(action.evaluateStatusOnPayload(), BaseAncHomeVisitAction.Status.COMPLETED);

        ReflectionHelpers.setField(action, "sleeping_llitn", "No");
        Assert.assertEquals(action.evaluateStatusOnPayload(), BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED);
    }
}
