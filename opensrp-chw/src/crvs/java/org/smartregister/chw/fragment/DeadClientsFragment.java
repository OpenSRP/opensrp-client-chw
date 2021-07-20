package org.smartregister.chw.fragment;

import android.content.Intent;
import android.util.Log;
import org.smartregister.chw.activity.ChildProfileActivity;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.model.CoreDeadClientsFragmentModel;
import org.smartregister.chw.presenter.DeadClientsFragmentPresenter;
import org.smartregister.chw.provider.DeadClientsProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.family.util.Utils;
import org.smartregister.view.activity.BaseRegisterActivity;
import java.util.Set;
import timber.log.Timber;
import static org.smartregister.chw.core.utils.ChildDBConstants.KEY.FAMILY_LAST_NAME;

public class DeadClientsFragment extends CoreDeadClientsFragment {

    protected void onViewClicked(android.view.View view) {
        super.onViewClicked(view);
        Log.d("nothing", "do nothing");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            default:
                break;
        }
    }

    @Override
    public void goToChildDetailActivity(CommonPersonObjectClient patient, boolean launchDialog) {
        /*if (launchDialog) {
            Timber.i(patient.name);
        }
        MemberObject memberObject = new MemberObject(patient);
        try {
            memberObject.setFamilyName(Utils.getValue(patient.getColumnmaps(), FAMILY_LAST_NAME, false));
        }catch (Exception e){
            memberObject.setFamilyName("test family");
            e.printStackTrace();
        }
        ChildProfileActivity.startMe(getActivity(), memberObject, ChildProfileActivity.class);*/
    }

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        DeadClientsProvider deadClientsProvider = new DeadClientsProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, deadClientsProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new DeadClientsFragmentPresenter(this, new CoreDeadClientsFragmentModel(), viewConfigurationIdentifier);
    }

    @Override
    public void setupViews(android.view.View view) {
        super.setupViews(view);

        if (ChwApplication.getApplicationFlavor().hasDefaultDueFilterForChildClient()) {
            android.view.View dueOnlyLayout = view.findViewById(org.smartregister.chw.core.R.id.due_only_layout);
            dueOnlyLayout.setVisibility(android.view.View.VISIBLE);
            dueOnlyLayout.setOnClickListener(registerActionHandler);
            dueOnlyLayout.setTag(null);
            toggleFilterSelection(dueOnlyLayout);
        }
    }

}
