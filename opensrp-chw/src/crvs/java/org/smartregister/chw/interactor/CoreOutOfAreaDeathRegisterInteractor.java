package org.smartregister.chw.interactor;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.chw.contract.CoreOutOfAreaDeathRegisterContract;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.interactor.CoreChildRegisterInteractor;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.UniqueId;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.jsonmapping.ClientClassification;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import timber.log.Timber;

public class CoreOutOfAreaDeathRegisterInteractor extends ClientProcessorForJava implements CoreOutOfAreaDeathRegisterContract.Interactor {

    public static final String TAG = CoreChildRegisterInteractor.class.getName();
    private AppExecutors appExecutors;

    public CoreOutOfAreaDeathRegisterInteractor(Context context) {
        super(context);
        new AppExecutors();
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        //TODO set presenter or model to null
    }

    @Override
    public void getNextUniqueId(final Triple<String, String, String> triple, final CoreOutOfAreaDeathRegisterContract.InteractorCallBack callBack, final String familyId) {

        Runnable runnable = () -> {
            UniqueId uniqueId = getUniqueIdRepository().getNextUniqueId();
            final String entityId = uniqueId != null ? uniqueId.getOpenmrsId() : "";
            appExecutors.mainThread().execute(() -> {
                if (StringUtils.isBlank(entityId)) {
                    callBack.onNoUniqueId();
                } else {
                    callBack.onUniqueIdFetched(triple, entityId, familyId);
                }
            });
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveRegistration(final Pair<Client, Event> pair, final String jsonString, final boolean isEditMode, final CoreOutOfAreaDeathRegisterContract.InteractorCallBack callBack) {
        if (saveRegistration(pair, jsonString, isEditMode)) {
            callBack.onRegistrationSaved(isEditMode, true, null);
        }
    }

    @Override
    public void removeChildFromRegister(final String closeFormJsonString, final String providerId) {
        Runnable runnable = () -> {
            //TODO add functionality to remove family from register
        };

        appExecutors.diskIO().execute(runnable);
    }

    @Override
    public void openActivityOnFloatingButtonClick(Context context, Class activity) {
        Intent intent = new Intent(context, activity);
        context.startActivity(intent);
    }

    private boolean saveRegistration(Pair<Client, Event> pair, String jsonString, boolean isEditMode) {

        try {

            Client baseClient = pair.first;
            Event baseEvent = pair.second;

            JSONObject clientJson = null;
            JSONObject eventJson = null;



            if (baseClient != null) {
                clientJson = new JSONObject(JsonFormUtils.gson.toJson(baseClient));
                if (isEditMode) {
                    JsonFormUtils.mergeAndSaveClient(getSyncHelper(), baseClient);
                } else {
                    getSyncHelper().addClient(baseClient.getBaseEntityId(), clientJson);
                }
            }

            if (baseEvent != null) {
                eventJson = new JSONObject(JsonFormUtils.gson.toJson(baseEvent));
                getSyncHelper().addEvent(baseEvent.getBaseEntityId(), eventJson, BaseRepository.TYPE_Unsynced);
            }

            if (isEditMode) {
                // Unassign current OPENSRP ID
                if (baseClient != null) {
                    String currentOpenSRPId = baseClient.getIdentifier(Utils.metadata().uniqueIdentifierKey);
                    getUniqueIdRepository().open(currentOpenSRPId);
                }

            } else {
                if (baseClient != null) {
                    String opensrpId = baseClient.getIdentifier(Utils.metadata().uniqueIdentifierKey);
                    // my generated id above
                    //mark OPENSRP ID as used
                    getUniqueIdRepository().close(opensrpId);
                }
            }

            if (baseClient != null || baseEvent != null) {
                String imageLocation = JsonFormUtils.getFieldValue(jsonString, Constants.KEY.PHOTO);
                JsonFormUtils.saveImage(baseEvent.getProviderId(), baseClient.getBaseEntityId(), imageLocation);
            }


            List<EventClient> eventClientList = new ArrayList<>();

            org.smartregister.domain.Event domainEvent = (eventJson != null) ?
                    JsonFormUtils.gson.fromJson(eventJson.toString(), org.smartregister.domain.Event.class) : null;
            org.smartregister.domain.Client domainClient = (clientJson != null) ?
                    JsonFormUtils.gson.fromJson(clientJson.toString(), org.smartregister.domain.Client.class) : null;

            eventClientList.add(new EventClient(domainEvent, domainClient));

            long lastSyncTimeStamp = getAllSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
            processClient(eventClientList, false);
            getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());

        } catch (Exception e) {
            Timber.e(e);
            return false;
        }
        return true;
    }

    public synchronized void processClient(List<EventClient> eventClientList, boolean localSubmission) throws Exception {
        final String EC_CLIENT_CLASSIFICATION = "ec_client_classification.json";
        ClientClassification clientClassification = assetJsonToJava(EC_CLIENT_CLASSIFICATION, ClientClassification.class);
        if (clientClassification == null) {
            return;
        }

        if (!eventClientList.isEmpty()) {
            for (EventClient eventClient : eventClientList) {
                // Iterate through the events
                org.smartregister.domain.Client client = eventClient.getClient();
                if (client != null) {
                    org.smartregister.domain.Event event = eventClient.getEvent();
                    String eventType = event.getEventType();

                    if (processorMap.containsKey(eventType)) {
                        try {
                            processEventUsingMiniProcessor(clientClassification, eventClient, eventType);
                        } catch (Exception ex) {
                            Timber.e(ex);
                        }
                    } else {
                        processEvent(event, client, clientClassification);
                    }
                }

                if (localSubmission && CoreLibrary.getInstance().getSyncConfiguration().runPlanEvaluationOnClientProcessing()) {
                    processPlanEvaluation(eventClient);
                }
            }
        }
    }

    public ECSyncHelper getSyncHelper() {
        return CoreChwApplication.getInstance().getEcSyncHelper();
    }

    public AllSharedPreferences getAllSharedPreferences() {
        return Utils.context().allSharedPreferences();
    }

    public ClientProcessorForJava getClientProcessorForJava() {
        return CoreChwApplication.getInstance().getClientProcessorForJava();
    }

    public UniqueIdRepository getUniqueIdRepository() {
        return CoreChwApplication.getInstance().getUniqueIdRepository();
    }

    public enum type {SAVED, UPDATED}
}
