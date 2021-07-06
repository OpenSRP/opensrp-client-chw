package org.smartregister.chw.interactor;

import android.content.ContentValues;
import android.content.Context;
import android.util.Pair;

import androidx.annotation.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.contract.CoreChildProfileContract;
import org.smartregister.chw.core.interactor.CoreChildProfileInteractor;
import org.smartregister.chw.core.model.ChildVisit;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.ChildHomeVisit;
import org.smartregister.chw.core.utils.CoreChildService;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.dao.ChildFHIRBundleDao;
import org.smartregister.chw.schedulers.ChwScheduleTaskExecutor;
import org.smartregister.chw.util.ChildUtils;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.Utils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Photo;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.thinkmd.ThinkMDLibrary;
import org.smartregister.thinkmd.model.FHIRBundleModel;
import org.smartregister.util.FormUtils;
import org.smartregister.util.ImageUtils;
import org.smartregister.view.LocationPickerView;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static org.smartregister.chw.core.utils.CoreConstants.ThinkMdConstants.THINKMD_IDENTIFIER_TYPE;
import static org.smartregister.opd.utils.OpdJsonFormUtils.locationId;

public class ChildProfileInteractor extends CoreChildProfileInteractor {
    public static final String TAG = ChildProfileInteractor.class.getName();
    private AppExecutors appExecutors;
    private Map<String, Date> vaccineList = new LinkedHashMap<>();
    private static ChildProfileInteractor.Flavour childProfileInteractorFlv = new ChildProfileInteractorFlv();

    public ChildProfileInteractor() {
        this(new AppExecutors());
    }

    @VisibleForTesting
    ChildProfileInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    @Override

    public Map<String, Date> getVaccineList() {
        return vaccineList;
    }

    @Override
    public void setVaccineList(Map<String, Date> vaccineList) {
        this.vaccineList = vaccineList;
    }

    @Override
    public void updateVisitNotDone(final long value, final CoreChildProfileContract.InteractorCallBack callback) {
        updateHomeVisitAsEvent(value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Object o) {
                        if (value == 0) {
                            callback.undoVisitNotDone();
                        } else {
                            callback.updateVisitNotDone();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.hideProgressBar();

                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    public void refreshChildVisitBar(Context context, String baseEntityId, final CoreChildProfileContract.InteractorCallBack callback) {
        if (getpClient() == null) {
            return;
        }
        ChildHomeVisit childHomeVisit = ChildUtils.getLastHomeVisit(Constants.TABLE_NAME.CHILD, baseEntityId);

        String dobString = Utils.getDuration(Utils.getValue(getpClient().getColumnmaps(), DBConstants.KEY.DOB, false));

        Visit visit = AncLibrary.getInstance().visitRepository().getLatestVisit(baseEntityId, CoreConstants.EventType.CHILD_VISIT_NOT_DONE);

        final ChildVisit childVisit = ChildUtils.getChildVisitStatus(context, dobString, childHomeVisit.getLastHomeVisitDate(), childHomeVisit.getVisitNotDoneDate(), childHomeVisit.getDateCreated());
        childVisit.setLastNotVisitDate((visit == null || visit.getProcessed()) ? null : visit.getDate().getTime());
        Runnable runnable = () -> appExecutors.mainThread().execute(() -> callback.updateChildVisit(childVisit));
        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void refreshUpcomingServiceAndFamilyDue(Context context, String familyId, String baseEntityId, final CoreChildProfileContract.InteractorCallBack callback) {
        if (getpClient() == null) {
            return;
        }
        updateUpcomingServices(callback, context);
        updateFamilyDueStatus(context, familyId, baseEntityId, callback);
    }

    @Override
    public void saveRegistration(final Pair<Client, Event> pair, final String jsonString, final boolean isEditMode, final CoreChildProfileContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            saveRegistration(pair, jsonString, isEditMode);
            appExecutors.mainThread().execute(() -> {
                if (callBack != null) {
                    callBack.onRegistrationSaved(isEditMode);
                }
            });
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public JSONObject getAutoPopulatedJsonEditFormString(String formName, String title, Context context, CommonPersonObjectClient client) {
        try {
            JSONObject form = FormUtils.getInstance(context).getFormJson(formName);
            LocationPickerView lpv = new LocationPickerView(context);
            lpv.init();
            if (form != null) {
                form.put(JsonFormUtils.ENTITY_ID, client.getCaseId());
                form.put(JsonFormUtils.ENCOUNTER_TYPE, CoreConstants.EventType.UPDATE_CHILD_REGISTRATION);

                JSONObject metadata = form.getJSONObject(JsonFormUtils.METADATA);
                String lastLocationId = LocationHelper.getInstance().getOpenMrsLocationId(lpv.getSelectedItem());

                metadata.put(JsonFormUtils.ENCOUNTER_LOCATION, lastLocationId);

                form.put(JsonFormUtils.CURRENT_OPENSRP_ID, Utils.getValue(client.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false));

                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);

                if (StringUtils.isNotBlank(title)) {
                    stepOne.put(org.smartregister.chw.util.JsonFormUtils.TITLE, title);
                }
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    processPopulatableFields(client, jsonObject, jsonArray);

                }

                return form;
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }

    public void processPopulatableFields(CommonPersonObjectClient client, JSONObject jsonObject, JSONArray jsonArray) throws JSONException {

        switch (jsonObject.getString(JsonFormUtils.KEY).toLowerCase()) {
            case org.smartregister.family.util.Constants.JSON_FORM_KEY.DOB_UNKNOWN:
                jsonObject.put(JsonFormUtils.READ_ONLY, false);
                JSONObject optionsObject = jsonObject.getJSONArray(org.smartregister.family.util.Constants.JSON_FORM_KEY.OPTIONS).getJSONObject(0);
                optionsObject.put(JsonFormUtils.VALUE, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), org.smartregister.family.util.Constants.JSON_FORM_KEY.DOB_UNKNOWN, false));
                break;
            case "age": {
                getAge(client, jsonObject);
            }
            break;
            case DBConstants.KEY.DOB:
                String dobString = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
                getDob(jsonObject, dobString);
                break;
            case org.smartregister.family.util.Constants.KEY.PHOTO:
                getPhoto(client, jsonObject);
                break;
            case DBConstants.KEY.UNIQUE_ID:
                String uniqueId = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), DBConstants.KEY.UNIQUE_ID, false);
                jsonObject.put(JsonFormUtils.VALUE, uniqueId.replace("-", ""));
                break;
            case CoreConstants.JsonAssets.FAM_NAME:
                childProfileInteractorFlv.getFamilyName(client, jsonObject, jsonArray);
                break;
            case CoreConstants.JsonAssets.INSURANCE_PROVIDER:
                jsonObject.put(JsonFormUtils.VALUE, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.INSURANCE_PROVIDER, false));
                break;
            case CoreConstants.JsonAssets.INSURANCE_PROVIDER_NUMBER:
                jsonObject.put(JsonFormUtils.VALUE, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.INSURANCE_PROVIDER_NUMBER, false));
                break;
            case CoreConstants.JsonAssets.INSURANCE_PROVIDER_OTHER:
                jsonObject.put(JsonFormUtils.VALUE, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.INSURANCE_PROVIDER_OTHER, false));
                break;
            case CoreConstants.JsonAssets.DISABILITIES:
                jsonObject.put(JsonFormUtils.VALUE, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.CHILD_PHYSICAL_CHANGE, false));
                break;
            case CoreConstants.JsonAssets.DISABILITY_TYPE:
                jsonObject.put(JsonFormUtils.VALUE, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.TYPE_OF_DISABILITY, false));
                break;
            case CoreConstants.JsonAssets.BIRTH_CERT_AVAILABLE:
                jsonObject.put(JsonFormUtils.VALUE, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.BIRTH_CERT, false));
                break;
            case CoreConstants.JsonAssets.BIRTH_REGIST_NUMBER:
                jsonObject.put(JsonFormUtils.VALUE, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.BIRTH_CERT_NUMBER, false));
                break;
            case CoreConstants.JsonAssets.RHC_CARD:
                jsonObject.put(JsonFormUtils.VALUE, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.RHC_CARD, false));
                break;
            case CoreConstants.JsonAssets.NUTRITION_STATUS:
                jsonObject.put(JsonFormUtils.VALUE, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), ChildDBConstants.KEY.NUTRITION_STATUS, false));
                break;
            case DBConstants.KEY.GPS:
                jsonObject.put(JsonFormUtils.VALUE, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), DBConstants.KEY.GPS, false));
                break;
            default:
                jsonObject.put(JsonFormUtils.VALUE, org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), jsonObject.getString(JsonFormUtils.KEY), false));
                break;

        }
    }

    private void getAge(CommonPersonObjectClient client, JSONObject jsonObject) throws JSONException {
        String dobString = org.smartregister.chw.core.utils.Utils.getValue(client.getColumnmaps(), DBConstants.KEY.DOB, false);
        dobString = Utils.getDuration(dobString);
        dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : "0";
        jsonObject.put(JsonFormUtils.VALUE, Integer.valueOf(dobString));
    }

    private void getDob(JSONObject jsonObject, String dobString) throws JSONException {
        if (StringUtils.isNotBlank(dobString)) {
            Date dob = org.smartregister.chw.core.utils.Utils.dobStringToDate(dobString);
            if (dob != null) {
                jsonObject.put(JsonFormUtils.VALUE, JsonFormUtils.dd_MM_yyyy.format(dob));
            }
        }
    }

    @Override
    public void launchThinkMDHealthAssessment(@NotNull Context context) {
        Runnable runnable = () -> {
            try {
                ChildFHIRBundleDao fhirBundleDao = new ChildFHIRBundleDao();
                FHIRBundleModel bundle = fhirBundleDao.fetchFHIRDateModel(context, getChildBaseEntityId());
                addThinkmdIdentifier(bundle.getUniqueIdGeneratedForThinkMD(), getChildBaseEntityId());
                ThinkMDLibrary.getInstance().processHealthAssessment(context, bundle);
            } catch (Exception e) {
                Timber.e(e);
            }
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void addThinkmdIdentifier(String uniqueIdGeneratedForThinkMD, @NotNull String childBaseEntityId) {
        Event event = new Event()
                .withBaseEntityId(childBaseEntityId)
                .withEventType("update_thinkmd_id")
                .withEntityType("ec_child")
                .withEventDate(new Date())
                .addIdentifier(THINKMD_IDENTIFIER_TYPE, uniqueIdGeneratedForThinkMD);
        event.withDateCreated(new Date());
        tagSyncMetadata(ChwApplication.getInstance().getContext().allSharedPreferences(), event);

        try {
            // update event
            JSONObject eventPartialJson = new JSONObject(JsonFormUtils.gson.toJson(event));
            getSyncHelper().addEvent(childBaseEntityId, eventPartialJson);
            // update local storage
            AllCommonsRepository allCommonsRepository = CoreChwApplication.getInstance().getAllCommonsRepository("ec_child");
            //Update REGISTER and FTS Tables
            if (allCommonsRepository != null) {
                ContentValues values = new ContentValues();
                values.put(THINKMD_IDENTIFIER_TYPE, uniqueIdGeneratedForThinkMD);
                allCommonsRepository.update("ec_child", values, childBaseEntityId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tagSyncMetadata(AllSharedPreferences allSharedPreferences, Event event) {
        String providerId = allSharedPreferences.fetchRegisteredANM();
        event.setProviderId(providerId);
        event.setLocationId(locationId(allSharedPreferences));
        event.setChildLocationId(allSharedPreferences.fetchCurrentLocality());
        event.setTeam(allSharedPreferences.fetchDefaultTeam(providerId));
        event.setTeamId(allSharedPreferences.fetchDefaultTeamId(providerId));
        event.setClientDatabaseVersion(FamilyLibrary.getInstance().getDatabaseVersion());
        event.setClientApplicationVersion(FamilyLibrary.getInstance().getApplicationVersion());
    }

    private void getPhoto(CommonPersonObjectClient client, JSONObject jsonObject) throws JSONException {
        Photo photo = ImageUtils.profilePhotoByClientID(client.getCaseId(), org.smartregister.chw.core.utils.Utils.getProfileImageResourceIDentifier());
        if (StringUtils.isNotBlank(photo.getFilePath())) {
            jsonObject.put(JsonFormUtils.VALUE, photo.getFilePath());
        }
    }

    @Override
    public void processBackGroundEvent(final CoreChildProfileContract.InteractorCallBack callback) {
        Runnable runnable = () -> {
            ChildUtils.processClientProcessInBackground();
            appExecutors.mainThread().execute(() -> callback.updateAfterBackGroundProcessed());
        };

        appExecutors.diskIO().execute(runnable);
    }

    //TODO Child Refactor
    private void updateUpcomingServices(final CoreChildProfileContract.InteractorCallBack callback, Context context) {
        updateUpcomingServices(context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CoreChildService>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        //TODO  disposing the observable immediately was causing  a baug in code
                        //d.dispose();
                    }

                    @Override
                    public void onNext(CoreChildService childService) {
                        callback.updateChildService(childService);
                        callback.hideProgressBar();
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.hideProgressBar();
                    }

                    @Override
                    public void onComplete() {
                        callback.hideProgressBar();
                    }
                });
    }

    private void updateFamilyDueStatus(Context context, String familyId, String baseEntityId, final CoreChildProfileContract.InteractorCallBack callback) {
        FamilyInteractor familyInteractor = new FamilyInteractor();
        familyInteractor.updateFamilyDueStatus(context, baseEntityId, familyId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        callback.updateFamilyMemberServiceDue(s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.hideProgressBar();
                    }

                    @Override
                    public void onComplete() {
                        callback.hideProgressBar();
                    }
                });
    }

    private Observable<Object> updateHomeVisitAsEvent(final long value) {
        return Observable.create(objectObservableEmitter -> {
            if (value == 0) {
                CoreChildUtils.undoVisitNotDone(getpClient().entityId());
            } else {
                CoreChildUtils.visitNotDone(getpClient().entityId());
            }
            ChwScheduleTaskExecutor.getInstance().execute(getpClient().entityId(), CoreConstants.EventType.CHILD_VISIT_NOT_DONE, new Date());
            objectObservableEmitter.onNext("");
        });
    }

    interface Flavour {
        void getFamilyName(CommonPersonObjectClient client, JSONObject jsonObject, JSONArray jsonArray) throws JSONException;
    }
}
