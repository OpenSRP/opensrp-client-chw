package org.smartregister.chw.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import org.smartregister.chw.R;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.presenter.LoginPresenter;
import org.smartregister.chw.util.FileUtils;
import org.smartregister.chw.util.Utils;
import org.smartregister.family.util.Constants;
import org.smartregister.task.SaveTeamLocationsTask;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.view.activity.BaseLoginActivity;
import org.smartregister.view.contract.BaseLoginContract;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;


public class LoginActivity extends BaseLoginActivity implements BaseLoginContract.View {
    public static final String TAG = BaseLoginActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button btn = findViewById(R.id.login_export_data);
        btn.setOnClickListener(v -> {
            exportDB();
            exportSharedPref();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLoginPresenter.processViewCustomizations();
        if (!mLoginPresenter.isUserLoggedOut()) {
            goToHome(false);
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_login;
    }

    @Override
    protected void initializePresenter() {
        mLoginPresenter = new LoginPresenter(this);
    }

    @Override
    public void goToHome(boolean remote) {
        if (remote) {
            Utils.startAsyncTask(new SaveTeamLocationsTask(), null);
        }

        getToFamilyList(remote);

        finish();
    }

    private void getToFamilyList(boolean remote) {
        Intent intent = new Intent(this, FamilyRegisterActivity.class);
        intent.putExtra(Constants.INTENT_KEY.IS_REMOTE_LOGIN, remote);
        startActivity(intent);
    }

    private void exportDB() {
        File env = FileUtils.hasExternalDisk() ? Environment.getExternalStorageDirectory() : Environment.getDataDirectory();
        File downloads = new File(env, Environment.DIRECTORY_DOWNLOADS + "/TOGO/");
        if (!downloads.exists()) downloads.mkdirs();

        File dest = new File(env, Environment.DIRECTORY_DOWNLOADS + "/TOGO/" + System.currentTimeMillis() + ".db");
        File source = new File("/data/data/" + ChwApplication.getInstance().getContext().applicationContext().getPackageName() + "/databases/drishti.db");
        OpenSRPImageLoader.copyFile(source, dest);

        Toast.makeText(this,
                "Exporting 1/2 Done'",
                Toast.LENGTH_LONG).show();
    }

    private void exportSharedPref() {
        SharedPreferences prefs = getDefaultSharedPreferences(ChwApplication.getInstance().getContext().applicationContext());
        File env = FileUtils.hasExternalDisk() ? Environment.getExternalStorageDirectory() : Environment.getDataDirectory();
        File myFile = new File(env, Environment.DIRECTORY_DOWNLOADS + "/TOGO/ " + System.currentTimeMillis() + "shared_pref.enc");
        try {
            FileWriter fw = new FileWriter(myFile);
            PrintWriter pw = new PrintWriter(fw);

            Map<String, ?> prefsMap = prefs.getAll();

            for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
                pw.println(entry.getKey() + ": " + entry.getValue().toString());
            }

            pw.close();
            fw.close();
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }

        Toast.makeText(this,
                "Exporting 2/2 Done'",
                Toast.LENGTH_LONG).show();
    }

}