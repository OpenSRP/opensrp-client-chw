package org.smartregister.chw.contract;

import android.content.Context;

import org.smartregister.chw.util.HomeVisitVaccineGroup;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public interface ImmunizationContact {

    interface View {

        Presenter initializePresenter();

        void allDataLoaded();

        void updateAdapter(int position, Context context);

        void updateSubmitBtn();

        void onUpdateNextPosition();

        Context getMyContext();
    }

    interface Presenter {

        void fetchImmunizationData(CommonPersonObjectClient commonPersonObjectClient, String groupName);

        void fetchImmunizationEditData(CommonPersonObjectClient commonPersonObjectClient);

        View getView();
    }

    interface Interactor {

        void fetchImmunizationData(CommonPersonObjectClient commonPersonObjectClient, InteractorCallBack callBack);

        void fetchImmunizationEditData(CommonPersonObjectClient commonPersonObjectClient, InteractorCallBack callBack);
    }

    interface InteractorCallBack {

        void updateData(ArrayList<HomeVisitVaccineGroup> homeVisitVaccineGroupDetails, Map<String, Date> receivedVaccine);

        void updateEditData(ArrayList<HomeVisitVaccineGroup> homeVisitVaccineGroupDetails);
    }
}
