package org.smartregister.chw.presenter;

import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.contract.CoreChildRegisterFragmentContract;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.util.ChildDBConstants;
import org.smartregister.family.util.DBConstants;

public class DeadClientsFragmentPresenter extends CoreDeadClientsFragmentPresenter {
    public DeadClientsFragmentPresenter(CoreChildRegisterFragmentContract.View view, CoreChildRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }

    @Override
    public String getDefaultSortQuery() {
        return " MAX(ec_child.last_interacted_with , ifnull(VISIT_SUMMARY.visit_date,0)) DESC ";
    }

    @Override
    public String getMainCondition() {
        return " " + CoreConstants.TABLE_NAME.CHILD + "." + org.smartregister.chw.anc.util.DBConstants.KEY.IS_CLOSED + " is 1 ";
    }

    @Override
    public String getMainCondition(String tableName) {
        if (ChwApplication.getApplicationFlavor().dueVaccinesFilterInChildRegister())
            return String.format(" %s is null AND %s", tableName + "." + DBConstants.KEY.DATE_REMOVED, ChildDBConstants.childDueVaccinesFilterForChildrenBelowTwoAndGirlsAgeNineToEleven(tableName));
        return super.getMainCondition(tableName);
    }
}
