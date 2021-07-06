package org.smartregister.chw.dataloader;

import org.junit.Assert;
import org.junit.Test;

public class AncMemberDataLoaderTest {

    private final AncMemberDataLoader dataLoader = new AncMemberDataLoader("Sample");

    @Test
    public void testGetEventTypes() {
        Assert.assertEquals(2, dataLoader.getEventTypes().size());
    }
}
