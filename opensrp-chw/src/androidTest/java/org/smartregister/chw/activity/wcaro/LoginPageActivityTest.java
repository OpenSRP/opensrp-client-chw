package org.smartregister.chw.activity.wcaro;

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
public class LoginPageActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    private Utils utils = new Utils();

    @Test
    @Order(order = 5)
    public void testSuccessfulLogin() throws InterruptedException {
        onView(withId(R.id.login_user_name_edit_text))
                .perform(typeText("chaone"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edit_text))
                .perform(typeText("Wcaro123"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn))
                .perform(click());
        Thread.sleep(5000);
        onView(withId(R.id.action_family))
                .check(matches(isDisplayed()));
        utils.openDrawer();
        utils.logOut();
        Thread.sleep(1000);
    }

    @Test
    @Order(order = 1)
    public void testEmptyCredentials() throws InterruptedException {
        onView(withId(R.id.login_user_name_edit_text))
                .perform(clearText());
        onView(withId(R.id.login_password_edit_text))
                .perform(clearText());
        onView(withId(R.id.login_login_btn))
                .perform(click());
        Thread.sleep(5000);
        onView(withText("Please check the credentials."))
                .check(matches(isDisplayed()));

    }

    @Test
    @Order(order = 2)

    public void testEmptyPassword() throws InterruptedException {
        onView(withId(R.id.login_user_name_edit_text))
                .perform(typeText("demo"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edit_text))
                .perform(clearText());
        onView(withId(R.id.login_login_btn))
                .perform(click());
        Thread.sleep(5000);
        onView(withText("Please check the credentials."))
                .check(matches(isDisplayed()));

    }

    @Test
    @Order(order = 3)
    public void testEmptyUsername() throws InterruptedException {
        onView(withId(R.id.login_user_name_edit_text))
                .perform(clearText());
        onView(withId(R.id.login_password_edit_text))
                .perform(typeText("Amani123"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn))
                .perform(click());
        Thread.sleep(5000);
        onView(withText("Please check the credentials."))
                .check(matches(isDisplayed()));

    }

    @Test
    @Order(order = 6)
    public void testUnauthorizedUserGroup() throws InterruptedException {

       // utils.logIn(Constants.WcaroConfigs.wCaro_username, Constants.WcaroConfigs.wCaro_password);
        //utils.logOut();

        onView(withId(R.id.login_user_name_edit_text))
                .perform(typeText("chatwo"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edit_text))
                .perform(typeText("Wcaro123"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn))
                .perform(click());
        Thread.sleep(5000);
        onView(withText("Your user group is not allowed to access this device."))
                .check(matches(isDisplayed()));

    }

    @Test
    @Order(order = 4)
    public void testInvalidCredentials() throws InterruptedException {
        onView(withId(R.id.login_user_name_edit_text))
                .perform(typeText("demo"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edit_text))
                .perform(typeText("Amani123"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn))
                .perform(click());
        Thread.sleep(500);
        onView(withText("Please check the credentials."))
                .check(matches(isDisplayed()));

    }


}
