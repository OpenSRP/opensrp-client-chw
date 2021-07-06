package org.smartregister.chw.presenter;

import org.jetbrains.annotations.NotNull;
import org.smartregister.chw.R;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.hiv.contract.BaseHivRegisterFragmentContract;
import org.smartregister.chw.hiv.presenter.BaseHivCommunityFollowupPresenter;
import org.smartregister.chw.hiv.util.Constants.Tables;
import org.smartregister.chw.hiv.util.DBConstants;
import org.smartregister.chw.util.Constants;

public class HivCommunityFollowupFragmentPresenter extends BaseHivCommunityFollowupPresenter {

    public HivCommunityFollowupFragmentPresenter(BaseHivRegisterFragmentContract.View view, BaseHivRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    @NotNull
    public String getMainCondition() {
        return " " + Constants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.Key.DATE_REMOVED + " is null " +
                "AND " + Tables.HIV_COMMUNITY_FOLLOWUP + "." + DBConstants.Key.IS_CLOSED + " = '0' " +
                "AND " + Tables.HIV_COMMUNITY_FOLLOWUP + "." + DBConstants.Key.BASE_ENTITY_ID + " NOT IN (SELECT " + DBConstants.Key.COMMUNITY_REFERRAL_FORM_ID + " FROM " + Tables.HIV_COMMUNITY_FEEDBACK + " ) ";

    }

    @Override
    @NotNull
    public String getDueFilterCondition() {
        return CoreConstants.TABLE_NAME.HIV_MEMBER + ".base_entity_id in (select base_entity_id from schedule_service where strftime('%Y-%m-%d') BETWEEN due_date and expiry_date and schedule_name = '" + CoreConstants.SCHEDULE_TYPES.HIV_VISIT + "' and ifnull(not_done_date,'') = '' and ifnull(completion_date,'') = '' )  ";
    }

    @Override
    public void processViewConfigurations() {
        super.processViewConfigurations();
        if (getConfig().getSearchBarText() != null && getView() != null) {
            getView().updateSearchBarHint(getView().getContext().getString(R.string.search_name_or_id));
        }
    }

    @Override
    public String getMainTable() {
        return Tables.HIV_COMMUNITY_FOLLOWUP;
    }
}
