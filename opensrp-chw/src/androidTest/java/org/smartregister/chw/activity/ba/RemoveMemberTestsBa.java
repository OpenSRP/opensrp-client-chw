package org.smartregister.chw.activity.ba;

import android.app.Activity;

import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.vijay.jsonwizard.activities.JsonFormActivity;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.LoginActivity;
import org.smartregister.chw.activity.utils.Configs;
import org.smartregister.chw.activity.utils.Constants;
import org.smartregister.chw.activity.utils.Order;
import org.smartregister.chw.activity.utils.OrderedRunner;
import org.smartregister.chw.activity.utils.Utils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.smartregister.chw.activity.utils.Utils.getViewId;

//import org.junit.Before;


@LargeTest
//@RunWith(AndroidJUnit4.class)
@RunWith(OrderedRunner.class)
public class RemoveMemberTestsBa {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    private Utils utils = new Utils();

    public void setUp() throws InterruptedException{
        utils.logIn(Constants.BoreshaAfyaConfigUtils.ba_username, Constants.BoreshaAfyaConfigUtils.ba_password);
        Thread.sleep(5000);
    }

    @Test
    @Order(order = 2)
    public void confirmWarningWidgetWhenRemovingMember() throws Throwable{
        onView(withHint("Search name or ID"))
                .perform(typeText(Configs.TestConfigHelper.familyName), closeSoftKeyboard());
        onView(ViewMatchers.withSubstring(Configs.TestConfigHelper.familyName + " Family"))
                .perform(click());
        Thread.sleep(500);
        utils.openFamilyMenu();
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring("Remove existing family member"))
                .perform(click());
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring(Configs.AdditionalTestDataHelper.memberOneFirstname
                + " " +
                Configs.AdditionalTestDataHelper.memberOneSecondname + " " + Configs.TestConfigHelper.familyName
                + ", " + Configs.AdditionalTestDataHelper.extraMemberAge1))
                .perform(click());
        Activity activity = getCurrentActivity();
        onView(withId(getViewId((JsonFormActivity) activity, "step1:remove_reason")))
                .perform(click());
        onView(ViewMatchers.withSubstring("Other"))
                .perform(click());
        onView(withId(R.id.action_save))
                .perform(click());
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring("REMOVE"))
                .perform(click());
        Thread.sleep(500);
    }

    @Test
    @Order(order = 6)
    public void removeFamilyMemberWithOtherAsReason() throws Throwable{
        onView(withHint("Search name or ID"))
                .perform(typeText(Configs.TestConfigHelper.familyName), closeSoftKeyboard());
        onView(ViewMatchers.withSubstring(Configs.TestConfigHelper.familyName + " Family"))
                .perform(click());
        Thread.sleep(500);
        utils.openFamilyMenu();
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring("Remove existing family member"))
                .perform(click());
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring(Configs.AdditionalTestDataHelper.memberTwoFirstname
                + " " + Configs.AdditionalTestDataHelper.memberTwoSecondname + " "
                + Configs.TestConfigHelper.familyName
                + ", " + Configs.AdditionalTestDataHelper.extraMemberAge2))

                .perform(click());
        Activity activity = getCurrentActivity();
        onView(withId(getViewId((JsonFormActivity) activity, "step1:remove_reason")))
                .perform(click());
        onView(ViewMatchers.withSubstring("Moved away"))
                .perform(click());
        onView(withId(getViewId((JsonFormActivity) activity, "step1:date_moved")))
                .perform(click());
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring("done")).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.action_save))
                .perform(click());
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring("REMOVE"))
                .check(matches(isDisplayed()));
    }

    @Test
    @Order(order = 5)
    public void removeFamilyMemberWithDeathAsReason() throws Throwable {
        onView(withHint("Search name or ID"))
                .perform(typeText(Configs.TestConfigHelper.familyName), closeSoftKeyboard());
        onView(ViewMatchers.withSubstring(Configs.TestConfigHelper.familyName + " Family"))
                .perform(click());
        Thread.sleep(500);
        utils.openFamilyMenu();
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring("Remove existing family member"))
                .perform(click());
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring(Configs.TestConfigHelper.kidNameFirst
                + " " + Configs.TestConfigHelper.kidNameSecond + " " + Configs.TestConfigHelper.familyName + ", 0"))
                .perform(click());
        Activity activity = getCurrentActivity();
        onView(withId(getViewId((JsonFormActivity) activity, "step1:remove_reason")))
                .perform(click());
        onView(ViewMatchers.withSubstring("Died"))
                .perform(click());
        Thread.sleep(500);
        onView(withId(getViewId((JsonFormActivity) activity, "step1:date_died")))
                .perform(click());
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring("done")).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.action_save))
                .perform(click());
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring("REMOVE"))
                .perform(click());
        Thread.sleep(500);
    }

    @Test
    @Order(order = 1)
    public void removeFamilyMemberWithoutReason() throws InterruptedException{
        onView(withHint("Search name or ID"))
                .perform(typeText(Configs.TestConfigHelper.familyName), closeSoftKeyboard());
        onView(ViewMatchers.withSubstring(Configs.TestConfigHelper.familyName + " Family"))
                .perform(click());
        Thread.sleep(500);
        utils.openFamilyMenu();
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring("Remove existing family member"))
                .perform(click());
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring(Configs.AdditionalTestDataHelper.memberTwoFirstname
                + " " + Configs.AdditionalTestDataHelper.memberTwoSecondname + " " + Configs.TestConfigHelper.familyName
                + ", " + Configs.AdditionalTestDataHelper.extraMemberAge2))
                .perform(click());
        onView(withId(R.id.action_save))
                .perform(click());
        onView(ViewMatchers.withSubstring("Found 1 error(s) in the form. Please correct them to submit."))
                .check(matches(isDisplayed()));
    }

    @Order(order = 4)
    @Test
    public void confirmCaregiverReplacementBeforeRemoval() throws InterruptedException {
        onView(withHint("Search name or ID"))
                .perform(typeText(Configs.TestConfigHelper.familyName), closeSoftKeyboard());
        onView(ViewMatchers.withSubstring(Configs.TestConfigHelper.familyName + " Family"))
                .perform(click());
        Thread.sleep(500);
        utils.openFamilyMenu();
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring("Remove existing family member"))
                .perform(click());
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring(Configs.TestConfigHelper.aboveFiveFirstNameTwo
                + " " + Configs.TestConfigHelper.aboveFiveSecondNameTwo + " " + Configs.TestConfigHelper.familyName
                + ", " + Configs.TestConfigHelper.aboveFiveage))
                .perform(click());
        onView(ViewMatchers.withSubstring("Before you remove this member " +
                "you must select a new primary caregiver"))
                .check(matches(isDisplayed()));
        Thread.sleep(500);
    }

    @Test
    @Order(order = 3)
    public void confirmFamilyHeadReplacementBeforeRemoval() throws InterruptedException {
        onView(withHint("Search name or ID"))
                .perform(typeText(Configs.TestConfigHelper.familyName), closeSoftKeyboard());
        onView(ViewMatchers.withSubstring(Configs.TestConfigHelper.familyName + " Family"))
                .perform(click());
        Thread.sleep(500);
        utils.openFamilyMenu();
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring("Remove existing family member"))
                .perform(click());
        Thread.sleep(500);
        onView(ViewMatchers.withSubstring(Configs.TestConfigHelper.aboveFiveFirstNameOne
                + " " + Configs.TestConfigHelper.aboveFiveSecondNameOne
                + ", " + Configs.TestConfigHelper.aboveFiveage))
                .perform(click());
        onView(ViewMatchers.withSubstring("Before you remove this member " +
                "you must select a new family head."))
                .check(matches(isDisplayed()));
        Thread.sleep(500);
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
