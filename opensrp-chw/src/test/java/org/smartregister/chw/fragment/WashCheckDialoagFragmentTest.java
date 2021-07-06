package org.smartregister.chw.fragment;

import android.widget.RadioButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.chw.BaseUnitTest;

public class WashCheckDialoagFragmentTest extends BaseUnitTest {

    private WashCheckDialogFragment fragment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        WashCheckDialogFragment fragment = new WashCheckDialogFragment();
        this.fragment = Mockito.spy(fragment);
    }



    @Test
    public void testGetLastWashCheckDate() {

        RadioButton handwashingYes = Mockito.mock(RadioButton.class);
        RadioButton drinkingYes = Mockito.mock(RadioButton.class);
        RadioButton latrineYes = Mockito.mock(RadioButton.class);

        handwashingYes.setChecked(false);
        drinkingYes.setChecked(false);
        latrineYes.setChecked(false);

        ReflectionHelpers.setField(fragment, "handwashingYes", handwashingYes);
        ReflectionHelpers.setField(fragment, "drinkingYes", drinkingYes);
        ReflectionHelpers.setField(fragment, "latrineYes", latrineYes);


        try {
            String updateVisitJson = fragment.updateVisitJson(getSampleVisitJson());
            Assert.assertEquals("{\"baseEntityId\":\"4a0e8c2c-0ebe-4542-b900-6ed2eb618af3\",\"duration\":0,\"entityType\":\"ec_wash_check_log\",\"eventDate\":\"2020-11-03T05:00:00.000+05:00\",\"eventType\":\"WASH check\",\"formSubmissionId\":\"7efe3fe4-0eae-4c88-8fc2-a6dffe7ab829\",\"identifiers\":{},\"locationId\":\"402ecf03-af72-4c93-b099-e1ce327d815b\",\"obs\":[{\"fieldCode\":\"164863AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"fieldDataType\":\"select one\",\"fieldType\":\"concept\",\"formSubmissionField\":\"handwashing_facilities\",\"humanReadableValues\":[\"No\"],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"1267AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"]},{\"fieldCode\":\"\",\"fieldDataType\":\"select one\",\"fieldType\":\"concept\",\"formSubmissionField\":\"drinking_water\",\"humanReadableValues\":[\"No\"],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"1267AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"]},{\"fieldCode\":\"\",\"fieldDataType\":\"select one\",\"fieldType\":\"concept\",\"formSubmissionField\":\"hygienic_latrine\",\"humanReadableValues\":[\"No\"],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"1267AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"]},{\"fieldCode\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"fieldDataType\":\"start\",\"fieldType\":\"concept\",\"formSubmissionField\":\"start\",\"humanReadableValues\":[],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"2020-11-03 15:07:10\"]},{\"fieldCode\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"fieldDataType\":\"end\",\"fieldType\":\"concept\",\"formSubmissionField\":\"end\",\"humanReadableValues\":[],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"2020-11-03 15:07:37\"]},{\"fieldCode\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"fieldDataType\":\"deviceid\",\"fieldType\":\"concept\",\"formSubmissionField\":\"deviceid\",\"humanReadableValues\":[],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"358240051111110\"]},{\"fieldCode\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"fieldDataType\":\"subscriberid\",\"fieldType\":\"concept\",\"formSubmissionField\":\"subscriberid\",\"humanReadableValues\":[],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"310260000000000\"]},{\"fieldCode\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"fieldDataType\":\"simserial\",\"fieldType\":\"concept\",\"formSubmissionField\":\"simserial\",\"humanReadableValues\":[],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"89014103211118510720\"]},{\"fieldCode\":\"163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"fieldDataType\":\"phonenumber\",\"fieldType\":\"concept\",\"formSubmissionField\":\"phonenumber\",\"humanReadableValues\":[],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"+15555215554\"]}],\"providerId\":\"chaone\",\"team\":\"Clinic A Team\",\"teamId\":\"d9eb010a-6d03-4bf8-b57a-b488dedd6f51\",\"version\":1604398104301,\"clientApplicationVersion\":13,\"clientDatabaseVersion\":16,\"dateCreated\":\"2020-11-03T15:16:53.126+05:00\",\"serverVersion\":1604398613126,\"id\":\"8e6682e1-7cc2-47e1-a0d9-5f7d443f6631\",\"revision\":\"v1\",\"type\":\"Event\"}",
                    updateVisitJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String getSampleVisitJson(){
        return "{\"baseEntityId\":\"4a0e8c2c-0ebe-4542-b900-6ed2eb618af3\",\"duration\":0,\"entityType\":\"ec_wash_check_log\",\"eventDate\":\"2020-11-03T05:00:00.000+05:00\",\"eventType\":\"WASH check\",\"formSubmissionId\":\"7efe3fe4-0eae-4c88-8fc2-a6dffe7ab829\",\"identifiers\":{},\"locationId\":\"402ecf03-af72-4c93-b099-e1ce327d815b\",\"obs\":[{\"fieldCode\":\"164863AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"fieldDataType\":\"select one\",\"fieldType\":\"concept\",\"formSubmissionField\":\"handwashing_facilities\",\"humanReadableValues\":[\"Yes\"],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"1267AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"]},{\"fieldCode\":\"\",\"fieldDataType\":\"select one\",\"fieldType\":\"concept\",\"formSubmissionField\":\"drinking_water\",\"humanReadableValues\":[\"Yes\"],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"1267AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"]},{\"fieldCode\":\"\",\"fieldDataType\":\"select one\",\"fieldType\":\"concept\",\"formSubmissionField\":\"hygienic_latrine\",\"humanReadableValues\":[\"Yes\"],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"1267AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"]},{\"fieldCode\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"fieldDataType\":\"start\",\"fieldType\":\"concept\",\"formSubmissionField\":\"start\",\"humanReadableValues\":[],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"2020-11-03 15:07:10\"]},{\"fieldCode\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"fieldDataType\":\"end\",\"fieldType\":\"concept\",\"formSubmissionField\":\"end\",\"humanReadableValues\":[],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"2020-11-03 15:07:37\"]},{\"fieldCode\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"fieldDataType\":\"deviceid\",\"fieldType\":\"concept\",\"formSubmissionField\":\"deviceid\",\"humanReadableValues\":[],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"358240051111110\"]},{\"fieldCode\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"fieldDataType\":\"subscriberid\",\"fieldType\":\"concept\",\"formSubmissionField\":\"subscriberid\",\"humanReadableValues\":[],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"310260000000000\"]},{\"fieldCode\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"fieldDataType\":\"simserial\",\"fieldType\":\"concept\",\"formSubmissionField\":\"simserial\",\"humanReadableValues\":[],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"89014103211118510720\"]},{\"fieldCode\":\"163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"fieldDataType\":\"phonenumber\",\"fieldType\":\"concept\",\"formSubmissionField\":\"phonenumber\",\"humanReadableValues\":[],\"parentCode\":\"\",\"saveObsAsArray\":false,\"set\":[],\"values\":[\"+15555215554\"]}],\"providerId\":\"chaone\",\"team\":\"Clinic A Team\",\"teamId\":\"d9eb010a-6d03-4bf8-b57a-b488dedd6f51\",\"version\":1604398104301,\"clientApplicationVersion\":13,\"clientDatabaseVersion\":16,\"dateCreated\":\"2020-11-03T15:16:53.126+05:00\",\"serverVersion\":1604398613126,\"id\":\"8e6682e1-7cc2-47e1-a0d9-5f7d443f6631\",\"revision\":\"v1\",\"type\":\"Event\"}";
    }

    @Test
    public void testGetValueFromJsonFieldNodeWrongInput() throws JSONException {
        JSONArray testArray = new JSONArray("[{\"key\":\"some_value\"}]");
        Assert.assertNull(fragment.getValueFromJsonFieldNode(testArray, "some_value"));
    }

}


