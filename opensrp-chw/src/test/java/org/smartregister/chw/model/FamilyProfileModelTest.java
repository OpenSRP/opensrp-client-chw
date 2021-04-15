package org.smartregister.chw.model;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.smartregister.chw.BaseUnitTest;

public class FamilyProfileModelTest extends BaseUnitTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void testGetFormAsJson() {
        FamilyProfileModel profileModel = Mockito.mock(FamilyProfileModel.class);
        profileModel.processFamilyRegistrationForm(Mockito.anyString(),Mockito.anyString());
        Mockito.verify(profileModel).processFamilyRegistrationForm(Mockito.anyString(),Mockito.anyString());
    }

}
