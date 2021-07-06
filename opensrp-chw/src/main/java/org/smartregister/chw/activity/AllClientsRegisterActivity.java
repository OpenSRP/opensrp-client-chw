package org.smartregister.chw.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.chw.R;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.activity.CoreAllClientsRegisterActivity;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.fragment.AllClientsRegisterFragment;
import org.smartregister.chw.model.ChwAllClientsRegisterModel;
import org.smartregister.chw.presenter.ChwAllClientRegisterPresenter;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.JsonFormUtils;
import org.smartregister.chw.util.Utils;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.listener.BottomNavigationListener;
import org.smartregister.opd.activity.BaseOpdFormActivity;
import org.smartregister.opd.contract.OpdRegisterActivityContract;
import org.smartregister.opd.pojo.RegisterParams;
import org.smartregister.opd.presenter.BaseOpdRegisterActivityPresenter;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdJsonFormUtils;
import org.smartregister.opd.utils.OpdUtils;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.Map;

import timber.log.Timber;

public class AllClientsRegisterActivity extends CoreAllClientsRegisterActivity{

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChwApplication.getInstance().notifyAppContextChange();
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new AllClientsRegisterFragment();
    }

    @Override
    public void startFormActivity(String s, String s1, Map<String, String> map) {
        Timber.v("startFormActivity");
    }

    @Override
    public void startRegistration() {
        this.startFormActivity(Constants.ALL_CLIENT_REGISTRATION_FORM, null, "");
    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        try {
            String locationId = org.smartregister.family.util.Utils.context().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
            ((ChwAllClientRegisterPresenter) presenter()).startForm(formName, entityId, metaData, locationId);

        } catch (Exception e) {
            Timber.e(e);
            displayToast(org.smartregister.family.R.string.error_unable_to_start_form);
        }
    }

    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        BottomNavigationListener bottomNavigationListener = new BottomNavigationListener(this);
        Utils.setupBottomNavigation(bottomNavigationHelper, bottomNavigationView, bottomNavigationListener);
        bottomNavigationView.getMenu().findItem(R.id.action_register).setTitle(R.string.add_client).setIcon(R.drawable.ic_input_add);
    }

    @Override
    protected BaseOpdRegisterActivityPresenter createPresenter(
            @NonNull OpdRegisterActivityContract.View view, @NonNull OpdRegisterActivityContract.Model model) {
        return new ChwAllClientRegisterPresenter(view, model);
    }

    @Override
    public void switchToBaseFragment() {
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_scan_qr:
                startQrCodeScanner();
                return true;
            case R.id.action_family:
                switchToBaseFragment();
                break;
            case R.id.action_register:
                startRegistration();
                break;
            default:
                return true;
        }
        return true;
    }

    public OpdRegisterActivityContract.Model createActivityModel() {
        return new ChwAllClientsRegisterModel();
    }
}
