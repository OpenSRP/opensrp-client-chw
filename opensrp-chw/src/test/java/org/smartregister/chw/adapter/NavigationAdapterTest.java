package org.smartregister.chw.adapter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.FamilyRegisterActivity;
import org.smartregister.chw.activity.LoginActivity;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.adapter.NavigationAdapter;
import org.smartregister.chw.core.adapter.NavigationAdapterHost;
import org.smartregister.chw.core.model.NavigationOption;
import org.smartregister.chw.util.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(application = ChwApplication.class, sdk = 22)
public class NavigationAdapterTest {

    private LoginActivity activity;
    private ActivityController<LoginActivity> controller;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = Robolectric.buildActivity(LoginActivity.class).create().start();
        activity = controller.get();
    }


    @After
    public void tearDown() {
        try {
            activity.finish();
            controller.pause().stop().destroy(); //destroy controller if we can

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.gc();
    }

    @Test
    public void getItemCount() {
        NavigationOption model1 = new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, R.string.menu_all_families, Constants.DrawerMenu.ALL_FAMILIES, 0);
        NavigationOption model2 = new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, R.string.menu_all_families, Constants.DrawerMenu.ALL_FAMILIES, 0);

        Map<String, Class> registeredActivities = new HashMap<>();
        registeredActivities.put(Constants.REGISTERED_ACTIVITIES.FAMILY_REGISTER_ACTIVITY, FamilyRegisterActivity.class);
        NavigationAdapter adapter = new NavigationAdapter(Arrays.asList(model1, model2), activity, registeredActivities, Mockito.mock(NavigationAdapterHost.class));

        Assert.assertEquals(adapter.getItemCount(), 2);
    }

}
