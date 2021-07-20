package org.smartregister.chw.interactor;

import android.util.Log;

import org.smartregister.chw.contract.LoginJobScheduler;
import org.smartregister.domain.LoginResponse;
import org.smartregister.login.interactor.BaseLoginInteractor;
import org.smartregister.view.contract.BaseLoginContract;

import static org.smartregister.chw.util.CrvsConstants.USER_TYPE;


/***
 * @author rkodev
 */
public class LoginInteractor extends BaseLoginInteractor implements BaseLoginContract.Interactor {

    /**
     * add all schedule jobs to the schedule instance to enable
     * job start at pin login
     */
    private LoginJobScheduler scheduler = new LoginJobSchedulerProvider();

    public LoginInteractor(BaseLoginContract.Presenter loginPresenter) {
        super(loginPresenter);
    }

    @Override
    protected void processServerSettings(LoginResponse loginResponse) {
        super.processServerSettings(loginResponse);
        String type = loginResponse.payload().user.getType();
        Log.d("imresponse", "try:"+type);
        getSharedPreferences().savePreference(USER_TYPE, type);
    }

    @Override
    protected void scheduleJobsPeriodically() {
        scheduler.scheduleJobsPeriodically();
    }

    @Override
    protected void scheduleJobsImmediately() {
        super.scheduleJobsImmediately();
        scheduler.scheduleJobsImmediately();
    }
}