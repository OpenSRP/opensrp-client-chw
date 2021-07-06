package org.smartregister.chw.presenter;

import org.jetbrains.annotations.NotNull;
import org.smartregister.chw.R;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.tb.contract.BaseTbRegisterFragmentContract;
import org.smartregister.chw.tb.presenter.BaseTbRegisterFragmentPresenter;
import org.smartregister.chw.tb.util.Constants.Tables;
import org.smartregister.chw.tb.util.DBConstants;
import org.smartregister.chw.util.Constants;

public class TbRegisterFragmentPresenter extends BaseTbRegisterFragmentPresenter {

    public TbRegisterFragmentPresenter(BaseTbRegisterFragmentContract.View view, BaseTbRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    @NotNull
    public String getMainCondition() {
        return " " + Constants.TABLE_NAME.FAMILY_MEMBER + "." + DBConstants.Key.DATE_REMOVED + " is null " +
                "AND " + Tables.TB + "." + DBConstants.Key.TB_CASE_CLOSURE_DATE + " is null " +
                "AND (" + Tables.TB + "." + DBConstants.Key.CLIENT_TB_STATUS_AFTER_TESTING + " = 'Positive' OR " + Tables.TB + "." + DBConstants.Key.CLIENT_TB_STATUS_AFTER_TESTING + " IS NULL ) "+
                "AND " + Tables.TB + "." + DBConstants.Key.IS_CLOSED + " = '0' ";

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
        return Tables.TB;
    }
}
