package org.smartregister.chw.pinlogin;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.smartregister.CoreLibrary;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.util.Utils;
import org.smartregister.service.UserService;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * provides additional login credentials services
 * allows a user to save the username and
 */
public class SecurePinLogger implements PinLogger {

    private Context ctx = ChwApplication.getInstance().getApplicationContext();
    private SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);

    interface SecureConstants {
        String PIN_LOGIN = "chw-PinLogin";
        String SECURE_PIN = "chw-SecuredPin";
        String PREFERENCES_CONFIGURED = "chw-ConfigDone";
        String PASSWORD = "chw-Password";
        String LAST_LOGIN = "chw-LastLogin";
    }

    @Override
    public boolean isPinSet() {
        String pin = preferences.getString(SecureConstants.SECURE_PIN, "");
        return StringUtils.isNotBlank(pin);
    }

    @Override
    public void setPinStatus(boolean status) {
        preferences.edit().putBoolean(SecureConstants.PREFERENCES_CONFIGURED, true).apply();
        preferences.edit().putBoolean(SecureConstants.PIN_LOGIN, status).apply();
    }

    @Override
    public boolean enabledPin() {
        return preferences.getBoolean(SecureConstants.PIN_LOGIN, false);
    }

    @Override
    public boolean isFirstAuthentication() {
        return !preferences.getBoolean(SecureConstants.PREFERENCES_CONFIGURED, false);
    }

    @Override
    public void setPin(String newPin, @Nullable EventListener eventListener) {
        preferences.edit().putBoolean(SecureConstants.PREFERENCES_CONFIGURED, true).apply();

        if (StringUtils.isBlank(newPin)) {
            if (eventListener != null)
                eventListener.onError(new Exception("Invalid pin"));

            return;
        }

        if (newPin.length() < 4 && eventListener != null) {
            eventListener.onError(new Exception("Pin to short"));
        }

        preferences.edit().putString(SecureConstants.SECURE_PIN, newPin).apply();
        if (eventListener != null)
            eventListener.onSuccess();
    }

    public boolean attemptPinVerification(@NonNull String pin, @Nullable EventListener eventListener) {
        if (StringUtils.isBlank(pin)) {
            if (eventListener != null)
                eventListener.onError(new Exception("Invalid pin"));

            return false;
        }

        String currentPin = preferences.getString(SecureConstants.SECURE_PIN, "");
        return pin.equals(currentPin);
    }

    @Nullable
    @Override
    public String loggedInUser() {
        try {
            String values = Utils.getPrefferedName();
            if (StringUtils.isNotBlank(values))
                return values.split(" ")[0];

        } catch (Exception e) {
            Timber.v(e);
        }
        return null;
    }

    @Nullable
    @Override
    public String getLoggedInUserName() {
        return Utils.getAllSharedPreferences().fetchRegisteredANM();
    }

    @Override
    public void resetPinLogin() {
        preferences.edit().remove(SecureConstants.PIN_LOGIN).apply();
        preferences.edit().remove(SecureConstants.PREFERENCES_CONFIGURED).apply();
        preferences.edit().remove(SecureConstants.SECURE_PIN).apply();
        preferences.edit().remove(SecureConstants.PASSWORD).apply();
        preferences.edit().remove(SecureConstants.LAST_LOGIN).apply();
    }

    @Override
    public void savePassword(String passWord) {
        preferences.edit().putString(SecureConstants.PASSWORD, passWord).apply();
    }

    @Override
    public String getPassword(String pin) {
        String currentPin = preferences.getString(SecureConstants.SECURE_PIN, "");
        if (pin.equals(currentPin))
            return preferences.getString(SecureConstants.PASSWORD, null);

        return null;
    }

    @Override
    public boolean isWithin(Integer hours) {
        long lastLogin = preferences.getLong(SecureConstants.LAST_LOGIN, 0);
        if (lastLogin != 0) {
            Date startTime = new Date(lastLogin);
            Date endTime = new Date();
            long diff = endTime.getTime() - startTime.getTime();
            long diffHours = diff / (60 * 60 * 1000);
            return diffHours < hours;
        }
        return false;
    }

    @Override
    public void updateLastLogin() {
        preferences.edit().putLong(SecureConstants.LAST_LOGIN, new Date().getTime()).apply();
    }

    @Override
    public void autoLogin(@NotNull EventListener listener) {
        ChwApplication application = (ChwApplication) ChwApplication.getInstance();
        application.getAppExecutors();

        Runnable runnable = () -> {

            try {

                // alow the application to start if its awaking from slumber
                Thread.sleep(TimeUnit.SECONDS.toSeconds(5));

                String userName = getLoggedInUserName();
                String password = preferences.getString(SecureConstants.PASSWORD, null);
                boolean isAuthenticated = false; // getUserService().isUserInValidGroup(userName, password);
                if (isAuthenticated) {
                    //getUserService().localLogin(userName, password);
                }

                application.getAppExecutors().mainThread().execute(() -> {
                    if (isAuthenticated) {
                        listener.onSuccess();
                    } else {
                        listener.onError(new Exception("Authentication failure;"));
                    }
                });
            } catch (Exception ex) {
                application.getAppExecutors().mainThread().execute(() -> listener.onError(ex));
            }
        };
        application.getAppExecutors().diskIO().execute(runnable);
    }

    private UserService getUserService() {
        return CoreLibrary.getInstance().context().userService();
    }
}
