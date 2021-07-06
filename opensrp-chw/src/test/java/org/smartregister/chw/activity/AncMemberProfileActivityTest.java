package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Intent;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.chw.BaseUnitTest;
import org.smartregister.chw.anc.contract.BaseAncMemberProfileContract;
import org.smartregister.chw.anc.util.Constants;

/**
 * @author rkodev
 */
public class AncMemberProfileActivityTest extends BaseUnitTest {

    private AncMemberProfileActivity activity;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private BaseAncMemberProfileContract.Presenter presenter;

    private ActivityController<AncMemberProfileActivity> controller;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = Robolectric.buildActivity(AncMemberProfileActivity.class);
        activity = controller.get();

        activity = Mockito.spy(activity);
        // mute this presenter
        Mockito.doNothing().when(activity).registerPresenter();
        ReflectionHelpers.setField(activity, "presenter", presenter);
    }

    @Test
    public void testStartMeLoadsActivity() {
        Mockito.doNothing().when(activity).startActivityForResult(Mockito.any(Intent.class), Mockito.anyInt());

        String baseEntityID = "baseEntityID";
        AncMemberProfileActivity.startMe(activity, baseEntityID);

        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);

        Mockito.verify(activity).startActivityForResult(intentArgumentCaptor.capture(), integerArgumentCaptor.capture());


        Assert.assertEquals(intentArgumentCaptor.getValue().getStringExtra(Constants.ANC_MEMBER_OBJECTS.BASE_ENTITY_ID), baseEntityID);
        Assert.assertEquals(AncMemberProfileActivity.class.getName(), intentArgumentCaptor.getValue().getComponent().getClassName());
    }

    @Test
    public void testStartFormActivity() {
        JSONObject json = new JSONObject();
        activity.startFormActivity(json);
        Mockito.verify(activity).startActivityForResult(Mockito.any(Intent.class), Mockito.anyInt());
    }

    @Test
    public void testOpenFamilyLocation() {
        activity.openFamilyLocation();
        assertActivityStarted(activity, new AncMemberMapActivity());
    }

    private void assertActivityStarted(Activity currActivity, Activity nextActivity) {
        Intent expectedIntent = new Intent(currActivity, nextActivity.getClass());
        Intent actual = ShadowApplication.getInstance().getNextStartedActivity();
        Assert.assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }

    @After
    public void tearDown() {
        try {
            activity.finish();
            controller.pause().stop().destroy(); //destroy controller if we can
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
