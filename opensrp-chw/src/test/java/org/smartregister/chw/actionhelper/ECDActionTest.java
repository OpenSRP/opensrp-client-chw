package org.smartregister.chw.actionhelper;

import android.content.Context;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.chw.BaseUnitTest;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.core.utils.NativeFormProcessor;

import java.util.HashMap;
import java.util.Map;

public class ECDActionTest extends BaseUnitTest {

    private ECDAction action;
    private final Context context = RuntimeEnvironment.application;

    @Before
    public void setUp() {
        action = new ECDAction();
    }

    @Test
    public void testOnPayloadReceive() throws Exception {
        String formName = "early_childhood_development";

        Map<String, Object> values = new HashMap<>();
        values.put("stim_skills", "Yes");
        values.put("early_learning", "Yes");
        values.put("develop_warning_signs", "Yes");

        JSONObject jsonObject = ReadFormHelper.getFormJson(context, formName);
        NativeFormProcessor.createInstance(jsonObject)
                .populateValues(values);

        action.onPayloadReceived(jsonObject.toString());
        Assert.assertEquals("Yes", ReflectionHelpers.getField(action, "stim_skills"));
        Assert.assertEquals("Yes", ReflectionHelpers.getField(action, "early_learning"));
        Assert.assertEquals("Yes", ReflectionHelpers.getField(action, "develop_warning_signs"));
    }

    @Test
    public void testEvaluateSubTitle() {
        ReflectionHelpers.setField(action, "context", context);

        ReflectionHelpers.setField(action, "stim_skills", "Yes");
        ReflectionHelpers.setField(action, "early_learning", "Yes");
        ReflectionHelpers.setField(action, "develop_warning_signs", "No");

        String result = "Developmental warning signs: No\n" +
                "Caregiver stimulation skills: Yes\n" +
                "Early learning program: Yes";
        Assert.assertEquals(result, action.evaluateSubTitle());
    }

    @Test
    public void testEvaluateStatusOnPayload() {

        Assert.assertEquals(action.evaluateStatusOnPayload(), BaseAncHomeVisitAction.Status.PENDING);

        ReflectionHelpers.setField(action, "stim_skills", "Yes");
        ReflectionHelpers.setField(action, "early_learning", "Yes");
        ReflectionHelpers.setField(action, "develop_warning_signs", "No");
        Assert.assertEquals(action.evaluateStatusOnPayload(), BaseAncHomeVisitAction.Status.COMPLETED);

        ReflectionHelpers.setField(action, "develop_warning_signs", "Yes");
        Assert.assertEquals(action.evaluateStatusOnPayload(), BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED);
    }
}
