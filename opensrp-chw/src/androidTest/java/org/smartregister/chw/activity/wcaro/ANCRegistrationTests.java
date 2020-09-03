package org.smartregister.chw.activity.wcaro;

import android.Manifest;
import android.app.Activity;

import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.vijay.jsonwizard.activities.JsonFormActivity;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartregister.chw.activity.LoginActivity;
import org.smartregister.chw.activity.utils.Configs;
import org.smartregister.chw.activity.utils.Constants;
import org.smartregister.chw.activity.utils.Order;
import org.smartregister.chw.activity.utils.OrderedRunner;
import org.smartregister.chw.activity.utils.Utils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSubstring;

import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.smartregister.chw.activity.utils.Utils.getViewId;

@LargeTest
//@RunWith(AndroidJUnit4.class)
@RunWith(OrderedRunner.class)
public class ANCRegistrationTests {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.CALL_PHONE);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule1 = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    private Utils utils = new Utils();

    public void setUp() throws InterruptedException {
        Thread.sleep(10000);
        utils.logIn(Constants.WcaroConfigUtils.wCaro_username, Constants.WcaroConfigUtils.wCaro_password);

    }

    @Test
    @Order(order = 2)
    public void registerANCSuccessfuly() throws Throwable {
        onView(withSubstring(Configs.TestConfigHelper.familyName + " Family"))
                .perform(click());
        /*onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring(Configs.TestConfigs.aboveFiveFirstNameTwo
                + " " + Configs.TestConfigs.aboveFiveSecondNameTwo + " " + Configs.TestConfigs.familyName + ", "
                + Configs.TestConfigs.aboveFiveage))
                .perform(click());*/
        utils.locateLayout(1,2).perform(click());
        Thread.sleep(500);
        utils.openFamilyMenu();
        Thread.sleep(500);
        onView(withSubstring("ANC Registration"))
                .perform(click());
        Activity activity = getCurrentActivity();
        Thread.sleep(500);
        onView(withId(getViewId((JsonFormActivity) activity, "step1:last_menstrual_period")))
         .perform(click());
        Thread.sleep(1000);
        onView(withSubstring("done")).perform(click());
        Thread.sleep(500);
        onView(withId(getViewId((JsonFormActivity) activity, "step1:no_prev_preg")))
                .perform(typeText("2"));
        onView(withId(getViewId((JsonFormActivity) activity, "step1:no_surv_children")))
                .perform(scrollTo(), typeText("2"));
        onView(withId(getViewId((JsonFormActivity) activity, "step1:phone_number")))
                .perform(scrollTo(), typeText("0882454545"));
        onView(withId(getViewId((JsonFormActivity) activity, "step1:marital_status")))
                .perform(scrollTo(), click());
        onView(withSubstring("Co-habiting"))
                .perform(click());
        Thread.sleep(500);
        onView(withSubstring("Save"))
                .perform(click());
        Thread.sleep(500);
        onView(withSubstring("ANC Clients"))
                .check(matches(isDisplayed()));
    }

    @Test
    @Order(order = 1)
    public void registerANCWithoutAllFields() throws Throwable {
        onView(withSubstring(Configs.TestConfigHelper.familyName + " Family"))
                .perform(click());
        /*onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring(Configs.TestConfigs.aboveFiveFirstNameTwo
                + " " + Configs.TestConfigs.aboveFiveSecondNameTwo + " " + Configs.TestConfigs.familyName + ", "
                + Configs.TestConfigs.aboveFiveage))
                .perform(click());*/
        utils.locateLayout(1,2).perform(click());
        Thread.sleep(500);
        utils.openFamilyMenu();
        Thread.sleep(500);
        onView(withSubstring("ANC Registration"))
                .perform(click());
        Activity activity = getCurrentActivity();
        Thread.sleep(500);
        onView(withId(getViewId((JsonFormActivity) activity, "step1:last_menstrual_period")))
                .perform(click());
        Thread.sleep(1000);
        onView(withSubstring("done")).perform(click());
        Thread.sleep(500);
        onView(withSubstring("Save"))
                .perform(click());
        onView(withSubstring("Found 3 error(s) in the form. Please correct them to submit."))
                .check(matches(isDisplayed()));
    }
    @After
    public void completeTests(){
        mActivityTestRule.finishActivity();
    }


    private Activity getCurrentActivity() throws Throwable {
        getInstrumentation().waitForIdleSync();
        final Activity[] activity = new Activity[1];
        runOnUiThread(() -> {
            java.util.Collection<Activity> activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
            activity[0] = Iterables.getOnlyElement(activities);
        });
        return activity[0];
    }
}

