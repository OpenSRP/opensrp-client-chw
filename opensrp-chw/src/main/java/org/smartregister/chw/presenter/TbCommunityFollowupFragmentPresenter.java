package org.smartregister.chw.presenter;

import org.jetbrains.annotations.NotNull;
import org.smartregister.chw.R;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.tb.contract.BaseTbRegisterFragmentContract;
import org.smartregister.chw.tb.presenter.BaseTbCommunityFollowupPresenter;
import org.smartregister.chw.tb.util.Constants.Tables;
import org.smartregister.chw.tb.util.DBConstants;
import org.smartregister.chw.util.Constants;

public class TbCommunityFollowupFragmentPresenter extends BaseTbCommunityFollowupPresenter {

    public TbCommunityFollowupFragmentPresenter(BaseTbRegisterFragmentContract.View view, BaseTbRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    @NotNull
    public String getMainCondition() {
        return " " + Constants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.Key.DATE_REMOVED + " is null " +
                "AND " + Tables.TB_COMMUNITY_FOLLOWUP + "." + DBConstants.Key.IS_CLOSED + " = '0' " +
                "AND " + Tables.TB_COMMUNITY_FOLLOWUP + "." + DBConstants.Key.BASE_ENTITY_ID + " NOT IN (SELECT " + DBConstants.Key.COMMUNITY_REFERRAL_FORM_ID + " FROM " + Tables.TB_COMMUNITY_FEEDBACK + " ) ";

    }

    @Override
    @NotNull
    public String getDueFilterCondition() {
        return CoreConstants.TABLE_NAME.TB_MEMBER + ".base_entity_id in (select base_entity_id from schedule_service where strftime('%Y-%m-%d') BETWEEN due_date and expiry_date and schedule_name = '" + CoreConstants.SCHEDULE_TYPES.TB_VISIT + "' and ifnull(not_done_date,'') = '' and ifnull(completion_date,'') = '' )  ";

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
        return Tables.TB_COMMUNITY_FOLLOWUP;
    }
}
