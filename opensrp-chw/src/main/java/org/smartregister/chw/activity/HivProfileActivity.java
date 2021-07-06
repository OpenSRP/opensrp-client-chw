package org.smartregister.chw.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.vijay.jsonwizard.utils.FormUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.smartregister.chw.BuildConfig;
import org.smartregister.chw.R;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.activity.CoreHivProfileActivity;
import org.smartregister.chw.core.activity.CoreHivUpcomingServicesActivity;
import org.smartregister.chw.core.adapter.NotificationListAdapter;
import org.smartregister.chw.core.contract.FamilyProfileExtendedContract;
import org.smartregister.chw.core.interactor.CoreHivProfileInteractor;
import org.smartregister.chw.core.listener.OnClickFloatingMenu;
import org.smartregister.chw.core.listener.OnRetrieveNotifications;
import org.smartregister.chw.core.task.RunnableTask;
import org.smartregister.chw.core.utils.ChwNotificationUtil;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.custom_view.HivFloatingMenu;
import org.smartregister.chw.hiv.activity.BaseHivRegistrationFormsActivity;
import org.smartregister.chw.hiv.domain.HivMemberObject;
import org.smartregister.chw.hiv.util.HivUtil;
import org.smartregister.chw.model.ReferralTypeModel;
import org.smartregister.chw.presenter.HivProfilePresenter;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;
import org.smartregister.chw.tb.util.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.annotations.Nullable;
import timber.log.Timber;

import static org.smartregister.chw.util.NotificationsUtil.handleNotificationRowClick;
import static org.smartregister.chw.util.NotificationsUtil.handleReceivedNotifications;

public class HivProfileActivity extends CoreHivProfileActivity
        implements FamilyProfileExtendedContract.PresenterCallBack, OnRetrieveNotifications {

    private List<ReferralTypeModel> referralTypeModels = new ArrayList<>();
    private NotificationListAdapter notificationListAdapter = new NotificationListAdapter();
    private Flavor flavor = new HivProfileActivityFlv();

    public static void startHivProfileActivity(Activity activity, HivMemberObject memberObject) {
        Intent intent = new Intent(activity, HivProfileActivity.class);
        intent.putExtra(Constants.ActivityPayload.MEMBER_OBJECT, memberObject);
        activity.startActivity(intent);
    }

    public static void startHivFollowupActivity(Activity activity, String baseEntityID) throws JSONException {
        Intent intent = new Intent(activity, BaseHivRegistrationFormsActivity.class);
        intent.putExtra(org.smartregister.chw.hiv.util.Constants.ActivityPayload.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(org.smartregister.chw.hiv.util.Constants.ActivityPayload.JSON_FORM, (new FormUtils()).getFormJsonFromRepositoryOrAssets(activity, org.smartregister.chw.util.Constants.JSON_FORM.getHivFollowupVisit()).toString());
        intent.putExtra(org.smartregister.chw.hiv.util.Constants.ActivityPayload.ACTION, Constants.ActivityPayloadType.FOLLOW_UP_VISIT);
        intent.putExtra(org.smartregister.chw.hiv.util.Constants.ActivityPayload.USE_DEFAULT_NEAT_FORM_LAYOUT, false);

        activity.startActivityForResult(intent, org.smartregister.chw.anc.util.Constants.REQUEST_CODE_HOME_VISIT);
    }

    @Override
    protected void onCreation() {
        super.onCreation();
        addHivReferralTypes();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationAndReferralRecyclerView.setAdapter(notificationListAdapter);
        notificationListAdapter.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        notificationListAdapter.canOpen = true;
        ChwNotificationUtil.retrieveNotifications(ChwApplication.getApplicationFlavor().hasReferrals(),
                getHivMemberObject().getBaseEntityId(), this);
    }

    @Override
    protected void removeMember() {
        IndividualProfileRemoveActivity.startIndividualProfileActivity(HivProfileActivity.this,
                getClientDetailsByBaseEntityID(getHivMemberObject().getBaseEntityId()),
                getHivMemberObject().getFamilyBaseEntityId(), getHivMemberObject().getFamilyHead(),
                getHivMemberObject().getPrimaryCareGiver(), FpRegisterActivity.class.getCanonicalName());
    }

    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        setHivProfilePresenter(new HivProfilePresenter(this, new CoreHivProfileInteractor(this), getHivMemberObject()));
        fetchProfileData();
    }

    private void checkPhoneNumberProvided() {
        boolean phoneNumberAvailable = (StringUtils.isNotBlank(getHivMemberObject().getPhoneNumber())
                || StringUtils.isNotBlank(getHivMemberObject().getPrimaryCareGiverPhoneNumber()));

        ((HivFloatingMenu) getHivFloatingMenu()).redraw(phoneNumberAvailable);
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        int id = view.getId();
        if (id == R.id.record_hiv_followup_visit) {
            openFollowUpVisitForm(false);
        }
        handleNotificationRowClick(this, view, notificationListAdapter, getHivMemberObject().getBaseEntityId());
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void verifyHasPhone() {
        // Implement
    }

    @Override
    public void notifyHasPhone(boolean b) {
        // Implement
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // recompute schedule
        Runnable runnable = () -> ChwScheduleTaskExecutor.getInstance().execute(getHivMemberObject().getBaseEntityId(), org.smartregister.chw.hiv.util.Constants.EventType.FOLLOW_UP_VISIT, new Date());
        org.smartregister.chw.util.Utils.startAsyncTask(new RunnableTask(runnable), null);

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CoreConstants.ProfileActivityResults.CHANGE_COMPLETED && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(this, HivRegisterActivity.class);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void openMedicalHistory() {
        //TODO implement
    }

    @Override
    public void openHivRegistrationForm() {
        try {
            HivRegisterActivity.startHIVFormActivity(this, getHivMemberObject().getBaseEntityId(), CoreConstants.JSON_FORM.getHivRegistration(), (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, CoreConstants.JSON_FORM.getHivRegistration()).toString());
        } catch (JSONException e) {
            Timber.e(e);
        }

    }

    @Override
    public void openUpcomingServices() {
        CoreHivUpcomingServicesActivity.startMe(this, HivUtil.toMember(getHivMemberObject()));
    }

    @Override
    public void openFamilyDueServices() {
        Intent intent = new Intent(this, FamilyProfileActivity.class);

        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_BASE_ENTITY_ID, getHivMemberObject().getFamilyBaseEntityId());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_HEAD, getHivMemberObject().getFamilyHead());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.PRIMARY_CAREGIVER, getHivMemberObject().getPrimaryCareGiver());
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.FAMILY_NAME, getHivMemberObject().getFamilyName());

        intent.putExtra(CoreConstants.INTENT_KEY.SERVICE_DUE, true);
        startActivity(intent);
    }

    @Override
    public void openFollowUpVisitForm(boolean isEdit) {
        if (!isEdit) {
            try {
                startHivFollowupActivity(this, getHivMemberObject().getBaseEntityId());
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
    }

    private void addHivReferralTypes() {
        if (BuildConfig.USE_UNIFIED_REFERRAL_APPROACH) {
            referralTypeModels.add(new ReferralTypeModel(getString(R.string.hiv_referral),
                    CoreConstants.JSON_FORM.getHivReferralForm(), CoreConstants.TASKS_FOCUS.SUSPECTED_HIV));

            referralTypeModels.add(new ReferralTypeModel(getString(R.string.gbv_referral),
                    CoreConstants.JSON_FORM.getGbvReferralForm(), CoreConstants.TASKS_FOCUS.SUSPECTED_GBV));
        }

    }

    public List<ReferralTypeModel> getReferralTypeModels() {
        return referralTypeModels;
    }

    @Override
    public void initializeCallFAB() {
        setHivFloatingMenu(new HivFloatingMenu(this, getHivMemberObject()));

        OnClickFloatingMenu onClickFloatingMenu = viewId -> {
            switch (viewId) {
                case R.id.hiv_fab:
                    checkPhoneNumberProvided();
                    ((HivFloatingMenu) getHivFloatingMenu()).animateFAB();
                    break;
                case R.id.call_layout:
                    ((HivFloatingMenu) getHivFloatingMenu()).launchCallWidget();
                    ((HivFloatingMenu) getHivFloatingMenu()).animateFAB();
                    break;
                case R.id.refer_to_facility_layout:
                    ((HivProfilePresenter) getHivProfilePresenter()).referToFacility();
                    break;
                default:
                    Timber.d("Unknown fab action");
                    break;
            }

        };

        ((HivFloatingMenu) getHivFloatingMenu()).setFloatMenuClickListener(onClickFloatingMenu);
        getHivFloatingMenu().setGravity(Gravity.BOTTOM | Gravity.END);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        addContentView(getHivFloatingMenu(), linearLayoutParams);
    }

    @Override
    public void onReceivedNotifications(List<Pair<String, String>> notifications) {
        handleReceivedNotifications(this, notifications, notificationListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(org.smartregister.chw.core.R.menu.hiv_profile_menu, menu);

        flavor.updateTbMenuItems(getHivMemberObject().getBaseEntityId(), menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == org.smartregister.chw.core.R.id.action_tb_registration) {
            startTbRegister();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void startTbRegister() {
        try {
            TbRegisterActivity.startTbFormActivity(HivProfileActivity.this, getHivMemberObject().getBaseEntityId(), CoreConstants.JSON_FORM.getTbRegistration(), (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, CoreConstants.JSON_FORM.getTbRegistration()).toString());
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    public interface Flavor {
        void updateTbMenuItems(@Nullable String baseEntityId, @Nullable Menu menu);
    }
}

