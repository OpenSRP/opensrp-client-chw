package org.smartgresiter.wcaro.contract;

import org.smartregister.family.contract.FamilyOtherMemberContract;

public interface FamilyOtherMemberProfileExtendedContract {

    interface Presenter extends FamilyOtherMemberContract.Presenter {

        void updateFamilyMember(String jsonString);

    }

    interface View extends FamilyOtherMemberContract.View {

        void showProgressDialog(int saveMessageStringIdentifier);

        void hideProgressDialog();

        void refreshList();

    }
}
