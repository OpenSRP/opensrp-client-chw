package org.smartregister.chw.presenter;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.contract.CoreChildRegisterFragmentContract;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.model.CoreOutOfAreaFragmentModel;
import org.smartregister.configurableviews.model.Field;
import org.smartregister.configurableviews.model.RegisterConfiguration;
import org.smartregister.configurableviews.model.View;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.family.util.DBConstants;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class OutOfAreaChildFragmentPresenter implements CoreChildRegisterFragmentContract.Presenter {


    protected Set<View> visibleColumns = new TreeSet<>();
    private WeakReference<CoreChildRegisterFragmentContract.View> viewReference;
    private CoreChildRegisterFragmentContract.Model model;
    private RegisterConfiguration config;
    private String viewConfigurationIdentifier;

    public OutOfAreaChildFragmentPresenter(CoreChildRegisterFragmentContract.View view, CoreOutOfAreaFragmentModel model, String viewConfigurationIdentifier) {
        this.viewReference = new WeakReference<>(view);
        this.model = model;
        this.viewConfigurationIdentifier = viewConfigurationIdentifier;
        this.config = model.defaultRegisterConfiguration();
    }

    @Override
    public void processViewConfigurations() {
        if (StringUtils.isBlank(viewConfigurationIdentifier)) {
            return;
        }

        ViewConfiguration viewConfiguration = model.getViewConfiguration(viewConfigurationIdentifier);
        if (viewConfiguration != null) {
            config = (RegisterConfiguration) viewConfiguration.getMetadata();
            visibleColumns = model.getRegisterActiveColumns(viewConfigurationIdentifier);
        }

        if (config.getSearchBarText() != null && getView() != null) {
            getView().updateSearchBarHint(getView().getContext().getString(org.smartregister.chw.core.R.string.search_name_or_id));
        }
    }

    @Override
    public void initializeQueries(String mainCondition) {
        String countSelect = model.countSelect(CoreConstants.TABLE_NAME.CHILD, mainCondition, CoreConstants.TABLE_NAME.FAMILY_MEMBER);
        String mainSelect = model.mainSelect(CoreConstants.TABLE_NAME.CHILD, CoreConstants.TABLE_NAME.FAMILY, CoreConstants.TABLE_NAME.FAMILY_MEMBER, mainCondition);

        getView().initializeQueryParams(CoreConstants.TABLE_NAME.CHILD, countSelect, mainSelect);
        getView().initializeAdapter(visibleColumns);

        getView().countExecute();
        getView().filterandSortInInitializeQueries();
    }

    @Override
    public void startSync() {
        //ServiceTools.startSyncService(getActivity());
    }

    @Override
    public void searchGlobally(String uniqueId) {
        // TODO implement search global
    }

    protected CoreChildRegisterFragmentContract.View getView() {
        if (viewReference != null) {
            return viewReference.get();
        } else {
            return null;
        }
    }

    @Override
    public void updateSortAndFilter(List<Field> filterList, Field sortField) {
        String filterText = model.getFilterText(filterList, getView().getString(org.smartregister.R.string.filter));
        String sortText = model.getSortText(sortField);

        getView().updateFilterAndFilterStatus(filterText, sortText);
    }

    @Override
    public String getMainCondition() {
        return " " + "ec_out_of_area_child" + "." + org.smartregister.chw.anc.util.DBConstants.KEY.IS_CLOSED + " is 0 ";
    }

    @Override
    public String getMainCondition(String tableName) {
        if (ChwApplication.getApplicationFlavor().dueVaccinesFilterInChildRegister())
            return String.format(" %s is null AND %s", tableName + "." + DBConstants.KEY.DATE_REMOVED, org.smartregister.chw.util.ChildDBConstants.childDueVaccinesFilterForChildrenBelowTwoAndGirlsAgeNineToEleven(tableName));
        return getMainCondition(tableName);
    }

    @Override
    public String getDefaultSortQuery() {
        return " MAX(ec_out_of_area_child.last_interacted_with , 0 DESC ";
    }

    @Override
    public String getDueFilterCondition() {
        return "";
    }

    public void setModel(CoreChildRegisterFragmentContract.Model model) {
        this.model = model;
    }
}
