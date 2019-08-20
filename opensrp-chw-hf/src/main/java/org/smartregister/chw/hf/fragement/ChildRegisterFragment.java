package org.smartregister.chw.hf.fragement;

import android.content.Intent;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.core.fragment.CoreChildRegisterFragment;
import org.smartregister.chw.hf.R;
import org.smartregister.chw.hf.activity.ChildProfileActivity;
import org.smartregister.chw.hf.provider.HfChildRegisterProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.family.util.DBConstants;
import org.smartregister.util.Utils;

import java.util.Set;

import timber.log.Timber;

public class ChildRegisterFragment extends CoreChildRegisterFragment {
    @Override
    protected void onViewClicked(android.view.View view) {
        super.onViewClicked(view);
        if (view.getTag() != null && view.getTag(R.id.VIEW_ID) == CLICK_VIEW_DOSAGE_STATUS && view.getTag() instanceof CommonPersonObjectClient) {
            CommonPersonObjectClient pc = (CommonPersonObjectClient) view.getTag();
            String baseEntityId = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, true);

            if (StringUtils.isNotBlank(baseEntityId)) {
                HfChildHomeVisitFragment childHomeVisitFragment = HfChildHomeVisitFragment.newInstance();
                childHomeVisitFragment.setContext(getActivity());
                childHomeVisitFragment.setChildClient(pc);
                childHomeVisitFragment.show(getActivity().getFragmentManager(), HfChildHomeVisitFragment.DIALOG_TAG);
            }
        }
    }

    @Override
    public void goToChildDetailActivity(CommonPersonObjectClient patient,
                                        boolean launchDialog) {
        if (launchDialog) {
            Timber.i(patient.name);
        }

        Intent intent = new Intent(getActivity(), ChildProfileActivity.class);
        intent.putExtra(org.smartregister.family.util.Constants.INTENT_KEY.BASE_ENTITY_ID, patient.getCaseId());
        startActivity(intent);
    }

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        HfChildRegisterProvider childRegisterProvider = new HfChildRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, childRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }
}
