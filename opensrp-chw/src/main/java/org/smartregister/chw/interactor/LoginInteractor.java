package org.smartregister.chw.interactor;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.chw.BuildConfig;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.job.ChwIndicatorGeneratingJob;
import org.smartregister.chw.core.job.HomeVisitServiceJob;
import org.smartregister.chw.core.job.VaccineRecurringServiceJob;
import org.smartregister.chw.job.BasePncCloseJob;
import org.smartregister.chw.job.ScheduleJob;
import org.smartregister.chw.util.AccountAuthenticatorConstants;
import org.smartregister.domain.LoginResponse;
import org.smartregister.domain.TimeStatus;
import org.smartregister.event.Listener;
import org.smartregister.immunization.job.VaccineServiceJob;
import org.smartregister.job.DistrictFacilitiesServiceJob;
import org.smartregister.job.ImageUploadServiceJob;
import org.smartregister.job.PlanIntentServiceJob;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.job.SyncTaskServiceJob;
import org.smartregister.login.interactor.BaseLoginInteractor;
import org.smartregister.login.task.RemoteLoginTask;
import org.smartregister.service.UserService;
import org.smartregister.view.contract.BaseLoginContract;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static org.smartregister.domain.LoginResponse.NO_INTERNET_CONNECTIVITY;
import static org.smartregister.domain.LoginResponse.UNAUTHORIZED;
import static org.smartregister.domain.LoginResponse.UNKNOWN_RESPONSE;


public class LoginInteractor extends BaseLoginInteractor implements BaseLoginContract.Interactor {

    private RemoteLoginTask remoteLoginTask;

    private AccountManager _accountManager;

    public LoginInteractor(BaseLoginContract.Presenter loginPresenter) {
        super(loginPresenter);
    }

    public AccountManager getAccountManager() {
        if (_accountManager == null)
            _accountManager = AccountManager.get(ChwApplication.getInstance().getContext().applicationContext());

        return _accountManager;
    }

    @Override
    public void login(WeakReference<BaseLoginContract.View> view, String userName, String password) {
        loginWithLocalFlag(view, !getSharedPreferences().fetchForceRemoteLogin()
                && userName.equalsIgnoreCase(getSharedPreferences().fetchRegisteredANM()), userName, password);
    }

    public void loginWithLocalFlag(WeakReference<BaseLoginContract.View> view, boolean localLogin, String userName, String password) {
        getLoginView().hideKeyboard();
        getLoginView().enableLoginButton(false);
        if (localLogin) {
            localLogin(view, userName, password);
        } else {
            remoteLogin(userName, password);
        }

        Timber.i("Login result finished " + DateTime.now().toString());
    }

    private void localLogin(WeakReference<BaseLoginContract.View> view, String userName, String password) {
        getLoginView().enableLoginButton(true);
        boolean isAuthenticated = getUserService().isUserInValidGroup(userName, password);
        if (!isAuthenticated) {

            getLoginView().showErrorDialog(getApplicationContext().getResources().getString(org.smartregister.R.string.unauthorized));

        } else if (isAuthenticated && (!AllConstants.TIME_CHECK || TimeStatus.OK.equals(getUserService().validateStoredServerTimeZone()))) {

            navigateToHomePage(userName, password);

        } else {
            loginWithLocalFlag(view, false, userName, password);
        }
    }

    private void navigateToHomePage(String userName, String password) {

        getUserService().localLogin(userName, password);
        getLoginView().goToHome(false);

        CoreLibrary.getInstance().initP2pLibrary(userName);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Timber.i("Starting DrishtiSyncScheduler " + DateTime.now().toString());

                scheduleJobsImmediately();

                Timber.i("Started DrishtiSyncScheduler " + DateTime.now().toString());

                CoreLibrary.getInstance().context().getUniqueIdRepository().releaseReservedIds();
            }
        }).start();
    }

    public @Nullable String remoteLoginResponse(final String userName, final String password) {

        final LoginResponse[] response = {null};

        if (!getSharedPreferences().fetchBaseURL("").isEmpty()) {
            tryRemoteLogin(userName, password, new Listener<LoginResponse>() {

                public void onEvent(LoginResponse loginResponse) {
                    getLoginView().enableLoginButton(true);
                    if (loginResponse == LoginResponse.SUCCESS) {
                        String username = loginResponse.payload() != null && loginResponse.payload().user != null && StringUtils.isNotBlank(loginResponse.payload().user.getUsername())
                                ? loginResponse.payload().user.getUsername() : userName;
                        if (getUserService().isUserInPioneerGroup(username)) {
                            TimeStatus timeStatus = getUserService().validateDeviceTime(
                                    loginResponse.payload(), AllConstants.MAX_SERVER_TIME_DIFFERENCE
                            );
                            if (!AllConstants.TIME_CHECK || timeStatus.equals(TimeStatus.OK)) {

                                remoteLoginWith(username, password, loginResponse);
                                response[0] = loginResponse;

                            } else {
                                if (timeStatus.equals(TimeStatus.TIMEZONE_MISMATCH)) {
                                    TimeZone serverTimeZone = UserService.getServerTimeZone(loginResponse.payload());

                                    getLoginView().showErrorDialog(getApplicationContext().getString(timeStatus.getMessage(),
                                            serverTimeZone.getDisplayName()));
                                } else {
                                    getLoginView().showErrorDialog(getApplicationContext().getString(timeStatus.getMessage()));
                                }
                            }
                        } else {
                            // Valid user from wrong group trying to log in
                            getLoginView().showErrorDialog(getApplicationContext().getString(org.smartregister.R.string.unauthorized_group));
                        }
                    } else {
                        if (loginResponse == null) {
                            getLoginView().showErrorDialog("Sorry, your loginWithLocalFlag failed. Please try again");
                        } else {
                            if (loginResponse == NO_INTERNET_CONNECTIVITY) {
                                getLoginView().showErrorDialog(getApplicationContext().getResources().getString(org.smartregister.R.string.no_internet_connectivity));
                            } else if (loginResponse == UNKNOWN_RESPONSE) {
                                getLoginView().showErrorDialog(getApplicationContext().getResources().getString(org.smartregister.R.string.unknown_response));
                            } else if (loginResponse == UNAUTHORIZED) {
                                getLoginView().showErrorDialog(getApplicationContext().getResources().getString(org.smartregister.R.string.unauthorized));
                            } else {
                                getLoginView().showErrorDialog(loginResponse.message());
                            }
                        }
                    }
                }
            });
        }

        return response[0] == null ? null : new Gson().toJson(response[0].payload());
    }

    private void remoteLogin(final String userName, final String password) {

        try {
            if (getSharedPreferences().fetchBaseURL("").isEmpty() && StringUtils.isNotBlank(this.getApplicationContext().getString(org.smartregister.R.string.opensrp_url))) {
                getSharedPreferences().savePreference("DRISHTI_BASE_URL", getApplicationContext().getString(org.smartregister.R.string.opensrp_url));
            }
            if (!getSharedPreferences().fetchBaseURL("").isEmpty()) {
                tryRemoteLogin(userName, password, new Listener<LoginResponse>() {

                    public void onEvent(LoginResponse loginResponse) {
                        getLoginView().enableLoginButton(true);
                        if (loginResponse == LoginResponse.SUCCESS) {
                            String username = loginResponse.payload() != null && loginResponse.payload().user != null && StringUtils.isNotBlank(loginResponse.payload().user.getUsername())
                                    ? loginResponse.payload().user.getUsername() : userName;
                            if (getUserService().isUserInPioneerGroup(username)) {
                                TimeStatus timeStatus = getUserService().validateDeviceTime(
                                        loginResponse.payload(), AllConstants.MAX_SERVER_TIME_DIFFERENCE
                                );
                                if (!AllConstants.TIME_CHECK || timeStatus.equals(TimeStatus.OK)) {

                                    remoteLoginWith(username, password, loginResponse);

                                } else {
                                    if (timeStatus.equals(TimeStatus.TIMEZONE_MISMATCH)) {
                                        TimeZone serverTimeZone = UserService.getServerTimeZone(loginResponse.payload());

                                        getLoginView().showErrorDialog(getApplicationContext().getString(timeStatus.getMessage(),
                                                serverTimeZone.getDisplayName()));
                                    } else {
                                        getLoginView().showErrorDialog(getApplicationContext().getString(timeStatus.getMessage()));
                                    }
                                }
                            } else {
                                // Valid user from wrong group trying to log in
                                getLoginView().showErrorDialog(getApplicationContext().getString(org.smartregister.R.string.unauthorized_group));
                            }
                        } else {
                            if (loginResponse == null) {
                                getLoginView().showErrorDialog("Sorry, your loginWithLocalFlag failed. Please try again");
                            } else {
                                if (loginResponse == NO_INTERNET_CONNECTIVITY) {
                                    getLoginView().showErrorDialog(getApplicationContext().getResources().getString(org.smartregister.R.string.no_internet_connectivity));
                                } else if (loginResponse == UNKNOWN_RESPONSE) {
                                    getLoginView().showErrorDialog(getApplicationContext().getResources().getString(org.smartregister.R.string.unknown_response));
                                } else if (loginResponse == UNAUTHORIZED) {
                                    getLoginView().showErrorDialog(getApplicationContext().getResources().getString(org.smartregister.R.string.unauthorized));
                                } else {
                                    getLoginView().showErrorDialog(loginResponse.message());
                                }
                            }
                        }
                    }
                });
            } else {
                getLoginView().enableLoginButton(true);
                getLoginView().showErrorDialog("OpenSRP Base URL is missing. Please add it in Setting and try again");
            }
        } catch (Exception e) {
            Timber.e(e);
            getLoginView().showErrorDialog("Error occurred trying to loginWithLocalFlag in. Please try again...");
        }
    }

    private void tryRemoteLogin(final String userName, final String password, final Listener<LoginResponse> afterLogincheck) {
        if (remoteLoginTask != null && !remoteLoginTask.isCancelled()) {
            remoteLoginTask.cancel(true);
        }
        remoteLoginTask = new RemoteLoginTask(getLoginView(), userName, password, afterLogincheck);
        remoteLoginTask.execute();
    }

    private void remoteLoginWith(String userName, String password, LoginResponse loginResponse) {
        // persist the remote login user
        // shortcut to use basic auth. Persist the entire login response payload as a token
        final Account account = new Account(userName, AccountAuthenticatorConstants.ACCOUNT_TYPE);
        getAccountManager().addAccountExplicitly(account, password, null);
        getAccountManager().setAuthToken(account, AccountAuthenticatorConstants.BASIC_AUTH_TOKEN, new Gson().toJson(loginResponse.payload()));
        getAccountManager().setPassword(account, password);

        // continue old login
        getUserService().remoteLogin(userName, password, loginResponse.payload());
        processServerSettings(loginResponse);

        scheduleJobsPeriodically();
        scheduleJobsImmediately();

        CoreLibrary.getInstance().initP2pLibrary(userName);

        getLoginView().goToHome(true);
    }

    @Override
    protected void scheduleJobsPeriodically() {
        SyncServiceJob.scheduleJob(SyncServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.DATA_SYNC_DURATION_MINUTES), getFlexValue(BuildConfig
                .DATA_SYNC_DURATION_MINUTES));

        VaccineRecurringServiceJob.scheduleJob(VaccineRecurringServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.VACCINE_SYNC_PROCESSING_MINUTES), getFlexValue(BuildConfig.VACCINE_SYNC_PROCESSING_MINUTES));

        ImageUploadServiceJob.scheduleJob(ImageUploadServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.IMAGE_UPLOAD_MINUTES), getFlexValue(BuildConfig.IMAGE_UPLOAD_MINUTES));

        PullUniqueIdsServiceJob.scheduleJob(PullUniqueIdsServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.PULL_UNIQUE_IDS_MINUTES), getFlexValue(BuildConfig.PULL_UNIQUE_IDS_MINUTES));

        ChwIndicatorGeneratingJob.scheduleJob(ChwIndicatorGeneratingJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.REPORT_INDICATOR_GENERATION_MINUTES), getFlexValue(BuildConfig.REPORT_INDICATOR_GENERATION_MINUTES));

        HomeVisitServiceJob.scheduleJob(HomeVisitServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.HOME_VISIT_MINUTES), getFlexValue(BuildConfig.HOME_VISIT_MINUTES));

        BasePncCloseJob.scheduleJob(BasePncCloseJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.BASE_PNC_CLOSE_MINUTES), getFlexValue(BuildConfig.BASE_PNC_CLOSE_MINUTES));

        PlanIntentServiceJob.scheduleJob(PlanIntentServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.DATA_SYNC_DURATION_MINUTES), getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES));

        SyncTaskServiceJob.scheduleJob(PlanIntentServiceJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.DATA_SYNC_DURATION_MINUTES), getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES));

        ScheduleJob.scheduleJob(ScheduleJob.TAG, TimeUnit.MINUTES.toMinutes(BuildConfig.SCHEDULE_SERVICE_MINUTES), getFlexValue(BuildConfig.SCHEDULE_SERVICE_MINUTES));

    }

    @Override
    protected void scheduleJobsImmediately() {
        super.scheduleJobsImmediately();
        // Run initial job immediately on log in since the job will run a bit later (~ 15 mins +)
        ScheduleJob.scheduleJobImmediately(ScheduleJob.TAG);
        SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
        HomeVisitServiceJob.scheduleJobImmediately(HomeVisitServiceJob.TAG);
        BasePncCloseJob.scheduleJobImmediately(BasePncCloseJob.TAG);
        PlanIntentServiceJob.scheduleJobImmediately(PlanIntentServiceJob.TAG);
        SyncTaskServiceJob.scheduleJobImmediately(SyncTaskServiceJob.TAG);
        VaccineServiceJob.scheduleJobImmediately(VaccineServiceJob.TAG);
        VaccineRecurringServiceJob.scheduleJobImmediately(VaccineRecurringServiceJob.TAG);
        DistrictFacilitiesServiceJob.scheduleJobImmediately(DistrictFacilitiesServiceJob.TAG);
    }
}
