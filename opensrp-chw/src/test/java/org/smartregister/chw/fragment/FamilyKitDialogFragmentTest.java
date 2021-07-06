package org.smartregister.chw.fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.smartregister.chw.BaseUnitTest;

/**
 * Created by Qazi Abubakar
 */
public class FamilyKitDialogFragmentTest extends BaseUnitTest {
    private FamilyKitDialogFragment familyKitDialogFragment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        FamilyKitDialogFragment familyKitDialogFragment = new FamilyKitDialogFragment();
        this.familyKitDialogFragment = Mockito.spy(familyKitDialogFragment);
    }

    @Test
    public void testGetValueFromJsonFieldNode() {
        try {
            Assert.assertEquals("", familyKitDialogFragment.getValueFromJsonFieldNode(new JSONArray("[\n" +
                    "{\n" +
                    "\"key\":\"family_kit_received\",\n" +
                    "\"openmrs_entity_parent\":\"\",\n" +
                    "\"openmrs_entity\":\"concept\",\n" +
                    "\"openmrs_entity_id\":\"family_kit_entity_id\",\n" +
                    "\"openmrs_data_type\":\"select one\",\n" +
                    "\"type\":\"spinner\",\n" +
                    "\"label_info_title\":\"What type of kit has the family received?\",\n" +
                    "\"hint\":\"What type of kit has the family received?\",\n" +
                    "\"values\":[\n" +
                    "\"Simple Kit (ORS/ZINC and Paracetamol)\",\n" +
                    "\"Simple Kit (ORS/ZINC and Paracetamol) with micronutrients\",\n" +
                    "\"No kit received\"\n" +
                    "],\n" +
                    "\"openmrs_choice_ids\":{\n" +
                    "\"Simple Kit (ORS/ZINC and Paracetamol)\":\"key_simple_kit_simple\",\n" +
                    "\"Simple Kit (ORS/ZINC and Paracetamol) with micronutrients\":\"key_simple_kit_with_micronutrients\",\n" +
                    "\"No kit received\":\"key_no_kit_received\"\n" +
                    "},\n" +
                    "\"v_required\":{\n" +
                    "\"value\":true,\n" +
                    "\"err\":\"Please select option\"\n" +
                    "}\n" +
                    "}\n" +
                    "]"), "family_kit_received"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
