package org.smartregister.chw.fragment;

import android.view.View;
import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.R;
import org.smartregister.chw.core.fragment.CoreFamilyRegisterFragment;
import org.smartregister.chw.core.provider.CoreRegisterProvider;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.provider.FamilyRegisterProvider;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.DBConstants;

import java.util.Set;

import timber.log.Timber;

public class FamilyRegisterFragment extends CoreFamilyRegisterFragment {

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        CoreRegisterProvider chwRegisterProvider = new FamilyRegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, chwRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    public void onViewClicked(View view) {
        super.onViewClicked(view);
        if (view.getId() == R.id.due_only_layout) {
            toggleFilterSelection(view);
        }
    }

    @Override
    protected String dueFilterAndSortQuery() {

        String query = "";
        try {
            SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);
            sqb.addCondition(getFilterString(filters));
            sqb.addCondition(getDueFilter());
            query = sqb.orderbyCondition(Sortqueries);
            query = sqb.Endquery(sqb.addlimitandOffset(query, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset()));
        } catch (Exception e) {
            Timber.e(e);
        }

        return query;
    }

    private String getDueFilter() {
        return "AND ec_family.base_entity_id in (\n" +
                "    /** Select family members with due services only **/\n" +
                "    select distinct ec_family_member.relational_id\n" +
                "    from ec_family_member\n" +
                "             inner join schedule_service on ec_family_member.base_entity_id = schedule_service.base_entity_id\n" +
                "    where strftime('%Y-%m-%d') BETWEEN schedule_service.due_date and schedule_service.expiry_date\n" +
                "      and ifnull(schedule_service.not_done_date, '') = ''\n" +
                "      and ifnull(schedule_service.completion_date, '') = ''\n" +
                "    UNION ALL\n" +
                "    /**Consider family heads that have due services like Wash Check Task **/\n" +
                "    select distinct ec_family_member.relational_id\n" +
                "    from schedule_service\n" +
                "             inner join ec_family_member\n" +
                "                        on ec_family_member.relational_id = schedule_service.base_entity_id\n" +
                "    where strftime('%Y-%m-%d') BETWEEN schedule_service.due_date and schedule_service.expiry_date\n" +
                "      and ifnull(schedule_service.not_done_date, '') = ''\n" +
                "      and ifnull(schedule_service.completion_date, '') = ''\n" +
                ")";
    }

    private String getFilterString(String filters) {
        if (StringUtils.isBlank(filters))
            return "";

        return " and (" + CoreConstants.TABLE_NAME.FAMILY + "." + DBConstants.KEY.FIRST_NAME + " like '%" + filters + "%' or "
                + CoreConstants.TABLE_NAME.FAMILY + "." + DBConstants.KEY.LAST_NAME + " like '%" + filters + "%')";
    }


}
