package org.smartregister.chw.presenter;

import org.smartregister.chw.core.dao.NavigationDao;
import org.smartregister.chw.core.enums.ImmunizationState;
import org.smartregister.chw.core.rule.WashCheckAlertRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.WashCheck;
import org.smartregister.chw.fragment.FamilyProfileDueFragment;
import org.smartregister.chw.interactor.ChildProfileInteractor;
import org.smartregister.chw.model.WashCheckModel;
import org.smartregister.family.contract.FamilyProfileDueContract;
import org.smartregister.family.presenter.BaseFamilyProfileDuePresenter;

public class FamilyProfileDuePresenter extends BaseFamilyProfileDuePresenter {
    private WashCheckModel washCheckModel;

    public FamilyProfileDuePresenter(FamilyProfileDueContract.View view, FamilyProfileDueContract.Model model, String viewConfigurationIdentifier, String familyBaseEntityId) {
        super(view, model, viewConfigurationIdentifier, familyBaseEntityId);
        washCheckModel = new WashCheckModel(familyBaseEntityId);
    }

    @Override
    public void initializeQueries(String mainCondition) {
        String tableName = CoreConstants.TABLE_NAME.FAMILY_MEMBER;

        String countSelect = model.countSelect(tableName, mainCondition);
        String mainSelect = model.mainSelect(tableName, " ec_family_member.relational_id = '" + this.familyBaseEntityId + "' AND " + getDueQuery());

        getView().initializeQueryParams(tableName, countSelect, mainSelect);
        getView().initializeAdapter(visibleColumns);

        getView().countExecute();
        getView().filterandSortInInitializeQueries();
    }

    private String getDueQuery() {
        return " (ifnull(schedule_service.completion_date,'') = '' and schedule_service.expiry_date >= strftime('%Y-%m-%d') and schedule_service.due_date <= strftime('%Y-%m-%d')) ";
    }

    public boolean saveData(String jsonObject) {
        return washCheckModel.saveWashCheckEvent(jsonObject);
    }

    public void fetchLastWashCheck(long dateCreatedFamily) {
        WashCheck washCheck = washCheckModel.getLatestWashCheck();
        if (washCheck != null) {
            WashCheckAlertRule washCheckAlertRule = new WashCheckAlertRule(getView().getContext(), washCheck.getLastVisit(), dateCreatedFamily);
            if (washCheckAlertRule.isOverdueWithinMonth(1)) {
                washCheck.setStatus(ChildProfileInteractor.VisitType.OVERDUE.name());
            } else if (washCheckAlertRule.isDueWithinMonth()) {
                washCheck.setStatus(ChildProfileInteractor.VisitType.DUE.name());
            } else {
                washCheck.setStatus(ImmunizationState.NO_ALERT.name());
            }
            washCheck.setLastVisitDate(washCheckAlertRule.noOfDayDue);
        }
        if (getView() instanceof FamilyProfileDueFragment) {
            FamilyProfileDueFragment familyProfileDueFragment = (FamilyProfileDueFragment) getView();
            familyProfileDueFragment.updateWashCheckBar(washCheck);
        }
    }

    public Integer getDueCount() {
        String sql = "select count(*) from schedule_service where " + getDueQuery() +
                " and schedule_service.base_entity_id in (select object_id from ec_family_member_search  where object_relational_id = '" + this.familyBaseEntityId + "' and date_removed is null ) ";

        return NavigationDao.getQueryCount(sql);
    }
}
