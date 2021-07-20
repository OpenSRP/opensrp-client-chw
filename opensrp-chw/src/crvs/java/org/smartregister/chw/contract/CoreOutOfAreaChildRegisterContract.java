package org.smartregister.chw.contract;

import android.content.Context;
import android.util.Pair;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.view.contract.BaseRegisterContract;

import java.util.List;

public interface CoreOutOfAreaChildRegisterContract {

    interface View extends BaseRegisterContract.View {
        CoreOutOfAreaChildRegisterContract.Presenter presenter();

        void openFamilyListView();
    }

    interface Presenter extends BaseRegisterContract.Presenter {

        void saveLanguage(String language);

        void startForm(String formName, String entityId, String metadata, String currentLocationId, String familyID) throws Exception;

        void saveOutOfAreaForm(String jsonString, boolean isEditMode);

        void closeFamilyRecord(String jsonString);

        void addOutOfAreaChild(Class activity);

        void registerFloatingActionButton(android.view.View view, int visibility);

    }

    interface Model {

        void registerViewConfigurations(List<String> viewIdentifiers);

        void unregisterViewConfiguration(List<String> viewIdentifiers);

        void saveLanguage(String language);

        String getLocationId(String locationName);

        Pair<Client, Event> processRegistration(String jsonString);

        JSONObject getFormAsJson(String formName, String entityId,
                                 String currentLocationId, String familyId) throws Exception;

        String getInitials();

    }

    interface Interactor {

        void onDestroy(boolean isChangingConfiguration);

        void getNextUniqueId(Triple<String, String, String> triple, CoreOutOfAreaChildRegisterContract.InteractorCallBack callBack, String familyID);

        void saveRegistration(final Pair<Client, Event> pair, final String jsonString, final boolean isEditMode, final CoreOutOfAreaChildRegisterContract.InteractorCallBack callBack);

        void removeChildFromRegister(String closeFormJsonString, String providerId);

        void openActivityOnFloatingButtonClick(Context context, Class activity);

    }

    interface InteractorCallBack {

        void onNoUniqueId();

        void onUniqueIdFetched(Triple<String, String, String> triple, String entityId, String familyId);

        void onRegistrationSaved(boolean editMode, boolean isSaved, FamilyEventClient familyEventClient);

    }
}
