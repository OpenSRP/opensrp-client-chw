package org.smartregister.chw.fragment;

import android.content.Intent;
import android.os.Bundle;

import org.smartregister.chw.activity.HivCommunityFollowupDetailsActivity;
import org.smartregister.chw.activity.HivRegisterActivity;
import org.smartregister.chw.core.fragment.CoreHivCommunityFollowupRegisterFragment;
import org.smartregister.chw.hiv.dao.HivDao;
import org.smartregister.chw.hiv.util.Constants;
import org.smartregister.chw.hiv.util.DBConstants;
import org.smartregister.chw.model.HivCommunityFollowupFragmentModel;
import org.smartregister.chw.presenter.HivCommunityFollowupFragmentPresenter;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import timber.log.Timber;

import static android.view.View.GONE;

public class HivFollowupRegisterFragment extends CoreHivCommunityFollowupRegisterFragment {
    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        String viewConfigurationIdentifier = null;
        try {
            viewConfigurationIdentifier = ((HivRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        } catch (NullPointerException e) {
            Timber.e(e);
        }
        presenter = new HivCommunityFollowupFragmentPresenter(this, new HivCommunityFollowupFragmentModel(), viewConfigurationIdentifier);
    }

    @Override
    public void setupViews(android.view.View view) {
        super.setupViews(view);
        view.findViewById(org.smartregister.chw.core.R.id.due_only_layout).setVisibility(GONE);
    }

    @Override
    protected void openProfile(CommonPersonObjectClient client) {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), HivCommunityFollowupDetailsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.HivMemberObject.MEMBER_OBJECT, HivDao.getCommunityFollowupMember(client.getColumnmaps().get(DBConstants.Key.BASE_ENTITY_ID)));
            intent.putExtras(bundle);

            getActivity().startActivity(intent);
        }
    }


}


