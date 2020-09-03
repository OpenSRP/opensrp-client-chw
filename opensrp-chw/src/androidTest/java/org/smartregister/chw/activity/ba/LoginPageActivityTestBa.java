package org.smartregister.chw.activity.ba;


import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.LoginActivity;
import org.smartregister.chw.activity.utils.Order;
import org.smartregister.chw.activity.utils.OrderedRunner;
import org.smartregister.chw.activity.utils.Utils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
//@RunWith(AndroidJUnit4.class)
@RunWith(OrderedRunner.class)
public class LoginPageActivityTestBa {

    private Utils utils = new Utils();

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    @Order(order = 5)
    public void testSuccessfulLogin() throws InterruptedException {
        onView(withId(R.id.login_user_name_edit_text)).perform(typeText("chwone"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edit_text)).perform(typeText("Boresha123"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());
        Thread.sleep(5000);
        onView(withId(R.id.action_family)).check(matches(isDisplayed()));
        utils.openDrawer();
        utils.logOutBA();
    }

    @Test
    @Order(order = 1)
    public void testEmptyCredentials() throws InterruptedException {
        onView(withId(R.id.login_user_name_edit_text)).perform(clearText());
        onView(withId(R.id.login_password_edit_text)).perform(clearText());
        onView(withId(R.id.login_login_btn)).perform(click());
        Thread.sleep(5000);
        onView(withText("Please check the credentials.")).check(matches(isDisplayed()));
    }

    @Test
    @Order(order = 2)

    public void testEmptyPassword() throws InterruptedException {
        onView(withId(R.id.login_user_name_edit_text)).perform(typeText("demo"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edit_text)).perform(clearText());
        onView(withId(R.id.login_login_btn)).perform(click());
        Thread.sleep(5000);
        onView(withText("Please check the credentials.")).check(matches(isDisplayed()));
    }

    @Test
    @Order(order = 3)
    public void testEmptyUsername() throws InterruptedException {
        onView(withId(R.id.login_user_name_edit_text)).perform(clearText());
        onView(withId(R.id.login_password_edit_text)).perform(typeText("Amani123"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());
        Thread.sleep(5000);
        onView(withText("Please check the credentials.")).check(matches(isDisplayed()));
    }

    @Test
    @Order(order = 4)
    public void testInvalidCredentials() throws InterruptedException {
        onView(withId(R.id.login_user_name_edit_text)).perform(typeText("demo1"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edit_text)).perform(typeText("Amani123"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());
        Thread.sleep(5000);
        onView(withText("Please check the credentials.")).check(matches(isDisplayed()));
    }

}
