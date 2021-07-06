package org.smartregister.chw.dataloader;

import org.junit.Assert;
import org.junit.Test;

public class FamilyMemberDataLoaderTest {

    private final FamilyMemberDataLoader dataLoader = new FamilyMemberDataLoader("", false, "", "", "");

    @Test
    public void testGetEventTypes() {
        Assert.assertEquals(2, dataLoader.getEventTypes().size());
    }
}
