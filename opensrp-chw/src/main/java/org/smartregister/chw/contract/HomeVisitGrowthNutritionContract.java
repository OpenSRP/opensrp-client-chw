package org.smartregister.chw.contract;

import android.content.Context;

import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.immunization.domain.ServiceWrapper;

import java.util.Map;

public interface HomeVisitGrowthNutritionContract {
    interface View {
        Presenter initializePresenter();

        void updateExclusiveFeedingData(String name,String dueDate);

        void updateMnpData(String name,String dueDate);

        void updateVitaminAData(String name,String dueDate);

        void updateDewormingData(String name,String dueDate);

        void statusImageViewUpdate(String type, boolean value,String message,String yesNoValue);
        void allDataLoaded();

        Context getViewContext();

    }

    interface Presenter {
        void parseRecordServiceData(CommonPersonObjectClient commonPersonObjectClient,boolean isEditMode);

        void setSaveState(String type, ServiceWrapper serviceWrapper);

        void setNotVisitState(String type, ServiceWrapper serviceWrapper);

        HomeVisitGrowthNutritionContract.View getView();

        void onDestroy(boolean isChangingConfiguration);

        Map<String, ServiceWrapper> getSaveStateMap();
        Map<String, ServiceWrapper> getNotSaveStateMap();

    }

    interface Interactor {
        void parseEditRecordServiceData(CommonPersonObjectClient commonPersonObjectClient, InteractorCallBack callBack);

        void parseRecordServiceData(CommonPersonObjectClient commonPersonObjectClient, InteractorCallBack callBack);

        void onDestroy(boolean isChangingConfiguration);

    }

    interface InteractorCallBack {
        void allDataLoaded();
        void updateGivenRecordVisitData(Map<String, ServiceWrapper> stringServiceWrapperMap);
        void updateNotGivenRecordVisitData(Map<String, ServiceWrapper> stringServiceWrapperMap);

    }
}
