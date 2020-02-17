package org.smartregister.chw.application;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.evernote.android.job.JobManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.smartregister.AllConstants;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.P2POptions;
import org.smartregister.chw.BuildConfig;
import org.smartregister.chw.activity.AncRegisterActivity;
import org.smartregister.chw.activity.ChildRegisterActivity;
import org.smartregister.chw.activity.FamilyProfileActivity;
import org.smartregister.chw.activity.FamilyRegisterActivity;
import org.smartregister.chw.activity.FpRegisterActivity;
import org.smartregister.chw.activity.LoginActivity;
import org.smartregister.chw.activity.MalariaRegisterActivity;
import org.smartregister.chw.activity.PncRegisterActivity;
import org.smartregister.chw.activity.ReferralRegisterActivity;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.custom_views.NavigationMenu;
import org.smartregister.chw.core.loggers.CrashlyticsTree;
import org.smartregister.chw.core.service.CoreAuthorizationService;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.FormUtils;
import org.smartregister.chw.custom_view.NavigationMenuFlv;
import org.smartregister.chw.fp.FpLibrary;
import org.smartregister.chw.job.ChwJobCreator;
import org.smartregister.chw.malaria.MalariaLibrary;
import org.smartregister.chw.model.NavigationModelFlv;
import org.smartregister.chw.pnc.PncLibrary;
import org.smartregister.chw.referral.ReferralLibrary;
import org.smartregister.chw.referral.domain.ReferralMetadata;
import org.smartregister.chw.repository.ChwRepository;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;
import org.smartregister.chw.service.ChildAlertService;
import org.smartregister.chw.sync.ChwClientProcessor;
import org.smartregister.chw.util.AccountAuthenticatorConstants;
import org.smartregister.chw.util.FileUtils;
import org.smartregister.chw.util.JsonFormUtils;
import org.smartregister.chw.util.Utils;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.helper.JsonSpecHelper;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.domain.FamilyMetadata;
import org.smartregister.family.util.Constants;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.reporting.ReportingLibrary;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class ChwApplication extends CoreChwApplication {

    private static Flavor flavor = new ChwApplicationFlv();
    private AccountManager _accountManager;
    private String password;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        context = Context.getInstance();
        context.updateApplicationContext(getApplicationContext());
        context.updateCommonFtsObject(createCommonFtsObject());

        //Necessary to determine the right form to pick from assets
        CoreConstants.JSON_FORM.setLocaleAndAssetManager(ChwApplication.getCurrentLocale(),
                ChwApplication.getInstance().getApplicationContext().getAssets());

        //Setup Navigation menu. Done only once when app is created
        NavigationMenu.setupNavigationMenu(this, new NavigationMenuFlv(), new NavigationModelFlv(),
                getRegisteredActivities(), flavor.hasP2P());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashlyticsTree(ChwApplication.getInstance().getContext().allSharedPreferences().fetchRegisteredANM()));
        }

        Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build());

        //Initialize Modules
        P2POptions p2POptions = new P2POptions(true);
        p2POptions.setAuthorizationService(new CoreAuthorizationService());

        CoreLibrary.init(context, new ChwSyncConfiguration(), BuildConfig.BUILD_TIMESTAMP, p2POptions);
        CoreLibrary.getInstance().setEcClientFieldsFile(CoreConstants.EC_CLIENT_FIELDS);

        // init libraries
        ImmunizationLibrary.init(context, getRepository(), null, BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        ConfigurableViewsLibrary.init(context);
        FamilyLibrary.init(context, getMetadata(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        AncLibrary.init(context, getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        PncLibrary.init(context, getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        MalariaLibrary.init(context, getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        FpLibrary.init(context, getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);

        ReferralMetadata referralMetadata = new ReferralMetadata();
        referralMetadata.setLocationIdMap(new HashMap<>());
        ReferralLibrary.init(context, getRepository(), referralMetadata, BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);

        SyncStatusBroadcastReceiver.init(this);

        LocationHelper.init(new ArrayList<>(Arrays.asList(BuildConfig.DEBUG ? BuildConfig.ALLOWED_LOCATION_LEVELS_DEBUG : BuildConfig.ALLOWED_LOCATION_LEVELS)), BuildConfig.DEBUG ? BuildConfig.DEFAULT_LOCATION_DEBUG : BuildConfig.DEFAULT_LOCATION);

        // set up processor
        FamilyLibrary.getInstance().setClientProcessorForJava(ChwClientProcessor.getInstance(getApplicationContext()));

        // init json helper
        this.jsonSpecHelper = new JsonSpecHelper(this);

        // Init Reporting library
        ReportingLibrary.init(context, getRepository(), null, BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);

        //init Job Manager
        JobManager.create(this).addJobCreator(new ChwJobCreator());

        initOfflineSchedules();

        setOpenSRPUrl();

        Configuration configuration = getApplicationContext().getResources().getConfiguration();
        String language;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            language = configuration.getLocales().get(0).getLanguage();
        } else {
            language = configuration.locale.getLanguage();
        }

        if (language.equals(Locale.FRENCH.getLanguage())) {
            saveLanguage(Locale.FRENCH.getLanguage());
        }

        // create a folder for guidebooks
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                prepareGuideBooksFolder();
            }
        } else {
            prepareGuideBooksFolder();
        }

        EventBus.getDefault().register(this);
    }

    public static Flavor getApplicationFlavor() {
        return flavor;
    }

    public AccountManager getAccountManager() {
        if (_accountManager == null)
            _accountManager = AccountManager.get(getApplicationContext());

        return _accountManager;
    }

    public static void prepareGuideBooksFolder() {
        String rootFolder = getGuideBooksDirectory();
        createFolders(rootFolder, false);
        boolean onSdCard = FileUtils.canWriteToExternalDisk();
        if (onSdCard)
            createFolders(rootFolder, true);
    }

    private static void createFolders(String rootFolder, boolean onSdCard) {
        try {
            FileUtils.createDirectory(rootFolder, onSdCard);
        } catch (Exception e) {
            Timber.v(e);
        }
    }

    public static String getGuideBooksDirectory() {
        String[] packageName = ChwApplication.getInstance().getContext().applicationContext().getPackageName().split("\\.");
        String suffix = packageName[packageName.length - 1];
        return "opensrp_guidebooks_" + (suffix.equalsIgnoreCase("chw") ? "liberia" : suffix);
    }

    @Override
    public String getPassword() {

        if (password == null) {
            String username = getContext().allSharedPreferences().fetchRegisteredANM();
            password = getContext().userService().getGroupId(username);
        }

        // read the password from account manager
        // current assumption is that there is only one account per device
        if(password == null || password.trim().equals("")){
            Account[] accounts = getAccountManager().getAccountsByType(AccountAuthenticatorConstants.ACCOUNT_TYPE);
            if(accounts.length > 0){
                Account account = accounts[0];
                // get the device password from account manager
                password = getAccountManager().getUserData(account, AllConstants.ENCRYPTED_GROUP_ID_PREFIX);
                getAccountManager().getPassword(account)
            }
        }
        return password;
    }

    @Override
    public void logoutCurrentUser() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getApplicationContext().startActivity(intent);
        context.userService().logoutSession();
    }

    @Override
    public FamilyMetadata getMetadata() {
        FamilyMetadata metadata = FormUtils.getFamilyMetadata(new FamilyProfileActivity(), getDefaultLocationLevel(), getFacilityHierarchy(), getFamilyLocationFields());

        HashMap<String, String> setting = new HashMap<>();
        setting.put(Constants.CustomConfig.FAMILY_FORM_IMAGE_STEP, JsonFormUtils.STEP1);
        setting.put(Constants.CustomConfig.FAMILY_MEMBER_FORM_IMAGE_STEP, JsonFormUtils.STEP2);
        metadata.setCustomConfigs(setting);
        return metadata;
    }

    @Override
    public ArrayList<String> getAllowedLocationLevels() {
        return new ArrayList<>(Arrays.asList(BuildConfig.DEBUG ? BuildConfig.ALLOWED_LOCATION_LEVELS_DEBUG : BuildConfig.ALLOWED_LOCATION_LEVELS));
    }

    @Override
    public ArrayList<String> getFacilityHierarchy() {
        return new ArrayList<>(Arrays.asList(BuildConfig.LOCATION_HIERACHY));
    }

    @Override
    public String getDefaultLocationLevel() {
        return BuildConfig.DEBUG ? BuildConfig.DEFAULT_LOCATION_DEBUG : BuildConfig.DEFAULT_LOCATION;
    }

    @NotNull
    public Map<String, Class> getRegisteredActivities() {
        Map<String, Class> registeredActivities = new HashMap<>();
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.ANC_REGISTER_ACTIVITY, AncRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.FAMILY_REGISTER_ACTIVITY, FamilyRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.CHILD_REGISTER_ACTIVITY, ChildRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.PNC_REGISTER_ACTIVITY, PncRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.MALARIA_REGISTER_ACTIVITY, MalariaRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.REFERRALS_REGISTER_ACTIVITY, ReferralRegisterActivity.class);
        registeredActivities.put(CoreConstants.REGISTERED_ACTIVITIES.FP_REGISTER_ACTIVITY, FpRegisterActivity.class);
        return registeredActivities;
    }

    @Override
    public Repository getRepository() {
        try {
            if (repository == null) {
                repository = new ChwRepository(getInstance().getApplicationContext(), context);
            }
        } catch (UnsatisfiedLinkError e) {
            Timber.e(e);
        }
        return repository;
    }

    public void setOpenSRPUrl() {
        AllSharedPreferences preferences = Utils.getAllSharedPreferences();
        preferences.savePreference(AllConstants.DRISHTI_BASE_URL,
                BuildConfig.DEBUG ? BuildConfig.opensrp_url_debug : BuildConfig.opensrp_url
        );
    }

    public boolean hasReferrals() {
        return flavor.hasReferrals();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVisitEvent(Visit visit) {
        if (visit != null) {
            Timber.v("Visit Submitted re processing Schedule for event ' %s '  : %s", visit.getVisitType(), visit.getBaseEntityId());
            ChwScheduleTaskExecutor.getInstance().execute(visit.getBaseEntityId(), visit.getVisitType(), visit.getDate());

            ChildAlertService.updateAlerts(visit.getBaseEntityId());
        }
    }

    public interface Flavor {
        boolean hasP2P();

        boolean hasReferrals();

        boolean hasANC();

        boolean hasPNC();

        boolean hasChildSickForm();

        boolean hasFamilyPlanning();

        boolean hasMalaria();

        boolean hasWashCheck();

        boolean hasRoutineVisit();
    }
}
