package org.smartregister.chw.contract;

import android.content.Context;
import android.util.Pair;
import org.json.JSONObject;
import org.smartregister.chw.util.ChildService;
import org.smartregister.chw.util.ChildVisit;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.FetchStatus;
import org.smartregister.view.contract.BaseProfileContract;

public interface MalariaProfileContract {

    interface View extends BaseProfileContract.View {

        Context getApplicationContext();

        Context getContext();

        String getString(int resourceId);

        void startFormActivity(JSONObject form);

        void refreshProfile(final FetchStatus fetchStatus);

        void displayShortToast(int resourceId);

        void setProfileImage(String baseEntityId);

        void setParentName(String parentName);

        void setGender(String gender);

        void setAddress(String address);

        void setId(String id);

        void setProfileName(String fullName);

        void setAge(String age);

        void setVisitButtonDueStatus();

        void setVisitButtonOverdueStatus();

        void setVisitNotDoneThisMonth();

        void setLastVisitRowView(String days);

        void setServiceNameDue(String name, String dueDate);

        void setServiceNameOverDue(String name, String dueDate);

        void setServiceNameUpcoming(String name, String dueDate);

        void setVisitLessTwentyFourView(String monthName);

        void setVisitAboveTwentyFourView();

        void setFamilyHasNothingDue();

        void setFamilyHasServiceDue();

        void setFamilyHasServiceOverdue();

        MalariaProfileContract.Presenter presenter();

        void updateHasPhone(boolean hasPhone);

        void enableEdit(boolean enable);

        void hideProgressBar();

        void openVisitMonthView();

        void showUndoVisitNotDoneView();
    }

    interface Presenter extends BaseProfileContract.Presenter {

        void updatePatientProfile(String jsonObject);

        MalariaProfileContract.View getView();

        void fetchProfileData();

        void updatePatientCommonPerson(String baseEntityId);

//        void fetchUpcomingServiceAndFamilyDue(String baseEntityId);

    }

    interface Interactor {
        void updateVisitNotDone(long value, MalariaProfileContract.InteractorCallBack callback);

        void refreshChildVisitBar(Context context, String baseEntityId, MalariaProfileContract.InteractorCallBack callback);

        void refreshUpcomingServiceAndFamilyDue(Context context, String familyId, String baseEntityId, MalariaProfileContract.InteractorCallBack callback);

        void onDestroy(boolean isChangingConfiguration);

        void updateChildCommonPerson(String baseEntityId);

        void refreshProfileView(String baseEntityId, boolean isForEdit, MalariaProfileContract.InteractorCallBack callback);

        void saveRegistration(final Pair<Client, Event> pair, final String jsonString, final boolean isEditMode, final MalariaProfileContract.InteractorCallBack callBack);

        JSONObject getAutoPopulatedJsonEditFormString(String formName, String title, Context context, CommonPersonObjectClient client);
    }

    interface InteractorCallBack {
//        void updateChildVisit(ChildVisit childVisit);

//        void updateChildService(ChildService childService);

//        void updateFamilyMemberServiceDue(String serviceDueStatus);

        void startFormForEdit(String title, CommonPersonObjectClient client);

        void refreshProfileTopSection(CommonPersonObjectClient client);

        void hideProgressBar();

        void onRegistrationSaved(boolean isEditMode);

        void setFamilyID(String familyID);

        void setFamilyName(String familyName);

        void setFamilyHeadID(String familyHeadID);

        void setPrimaryCareGiverID(String primaryCareGiverID);

//        void updateVisitNotDone();

//        void undoVisitNotDone();
    }

    interface Model {

        JSONObject getFormAsJson(String formName, String entityId, String currentLocationId, String familyID) throws Exception;

    }

}
