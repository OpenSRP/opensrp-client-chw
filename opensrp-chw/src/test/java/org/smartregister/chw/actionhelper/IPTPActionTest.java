package org.smartregister.chw.actionhelper;

import android.content.Context;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.smartregister.chw.BaseUnitTest;
import org.smartregister.util.JsonFormUtils;

public class IPTPActionTest extends BaseUnitTest {

    @Test
    public void testEvaluatePreProcess() {
        Context context = RuntimeEnvironment.application;
        IPTPAction iptpAction = new IPTPAction(context, "1");
        JSONObject preProcessJSON = null;

        try {
            preProcessJSON = iptpAction.preProcess(getSampleIPTPJson(),"1","10-02-2020");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONArray fields = JsonFormUtils.fields(preProcessJSON);
        JSONObject iptpJsonObject = JsonFormUtils.getFieldJSONObject(fields, "iptp1_date");

        try {
            Assert.assertEquals("10-02-2020",iptpJsonObject.get(JsonFormConstants.MIN_DATE));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getSampleIPTPJson() throws JSONException {

        return new JSONObject("{\n" +
                "  \"count\": \"1\",\n" +
                "  \"encounter_type\": \"IPTp-SP Service\",\n" +
                "  \"entity_id\": \"123-456-678\",\n" +
                "  \"step1\": {\n" +
                "    \"title\": \"IPTp-SP {0} dose\",\n" +
                "    \"fields\": [\n" +
                "      {\n" +
                "        \"key\": \"iptp{0}_date\",\n" +
                "        \"openmrs_entity_parent\": \"\",\n" +
                "        \"openmrs_entity\": \"concept\",\n" +
                "        \"openmrs_entity_id\": \"\",\n" +
                "        \"type\": \"date_picker\",\n" +
                "        \"image\": \"form_iptp_sp\",\n" +
                "        \"hint\": \"When was IPTp-SP {0} dose given?\",\n" +
                "        \"min_date\": \"today-120y\",\n" +
                "        \"max_date\": \"today\",\n" +
                "        \"v_required\": {\n" +
                "          \"value\": \"true\",\n" +
                "          \"err\": \"Please enter the dose date\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}");
    }


}
