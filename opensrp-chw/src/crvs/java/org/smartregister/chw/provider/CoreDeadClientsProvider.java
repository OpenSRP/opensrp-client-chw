package org.smartregister.chw.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.text.WordUtils;
import org.smartregister.chw.core.holders.FooterViewHolder;
import org.smartregister.chw.core.holders.RegisterViewHolder;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewProvider;
import org.smartregister.family.fragment.BaseFamilyRegisterFragment;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.smartregister.view.viewholder.OnClickFormLauncher;

import java.text.MessageFormat;
import java.util.Set;

import static org.smartregister.chw.core.utils.Utils.getDuration;

/**
 * Created by keyman on 13/11/2018.
 */

public class CoreDeadClientsProvider implements RecyclerViewProvider<RegisterViewHolder> {
    public final LayoutInflater inflater;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;
    private View.OnClickListener onClickListener;
    private View.OnClickListener paginationClickListener;
    private Context context;

    public CoreDeadClientsProvider(Context context, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.visibleColumns = visibleColumns;
        this.onClickListener = onClickListener;
        this.paginationClickListener = paginationClickListener;
        this.context = context;
    }

    protected static void fillValue(TextView v, String value) {
        if (v != null) {
            v.setText(value);
        }
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, client, viewHolder);
        }
    }

    @Override
    public void getFooterView(RecyclerView.ViewHolder viewHolder, int currentPageCount, int totalPageCount, boolean hasNext, boolean hasPrevious) {
        FooterViewHolder footerViewHolder = (FooterViewHolder) viewHolder;
        footerViewHolder.pageInfoView.setText(
                MessageFormat.format(context.getString(org.smartregister.R.string.str_page_info), currentPageCount,
                        totalPageCount));

        footerViewHolder.nextPageView.setVisibility(hasNext ? View.VISIBLE : View.INVISIBLE);
        footerViewHolder.previousPageView.setVisibility(hasPrevious ? View.VISIBLE : View.INVISIBLE);

        footerViewHolder.nextPageView.setOnClickListener(paginationClickListener);
        footerViewHolder.previousPageView.setOnClickListener(paginationClickListener);
    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption serviceModeOption, FilterOption searchFilter, SortOption sortOption) {
        return null;
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {//Implement Abstract Method
    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String metaData) {
        return null;
    }

    @Override
    public LayoutInflater inflater() {
        return inflater;
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater.inflate(org.smartregister.chw.core.R.layout.adapter_child_register_list_row, parent, false);

        /*
        ConfigurableViewsHelper helper = ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper();
        if (helper.isJsonViewsEnabled()) {

            ViewConfiguration viewConfiguration = helper.getViewConfiguration(Constants.CONFIGURATION.HOME_REGISTER_ROW);
            ViewConfiguration commonConfiguration = helper.getViewConfiguration(COMMON_REGISTER_ROW);

            if (viewConfiguration != null) {
                return helper.inflateDynamicView(viewConfiguration, commonConfiguration, view, R.id.register_columns, false);
            }
        }*/

        return new RegisterViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder createFooterHolder(ViewGroup parent) {
        View view = inflater.inflate(org.smartregister.chw.core.R.layout.smart_register_pagination, parent, false);
        return new FooterViewHolder(view);
    }

    @Override
    public boolean isFooterViewHolder(RecyclerView.ViewHolder viewHolder) {
        return viewHolder instanceof FooterViewHolder;
    }

    protected void populatePatientColumn(CommonPersonObjectClient pc, SmartRegisterClient client, RegisterViewHolder viewHolder) {

        try {
            String parentFirstName = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.FAMILY_FIRST_NAME, true);
            String parentLastName = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.FAMILY_LAST_NAME, true);
            String parentMiddleName = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.FAMILY_MIDDLE_NAME, true);

            String parentName = context.getResources().getString(org.smartregister.chw.core.R.string.care_giver_initials) + ": " + org.smartregister.util.Utils.getName(parentFirstName, parentMiddleName + " " + parentLastName);
            String firstName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
            String middleName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true);
            String lastName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
            String childName = org.smartregister.util.Utils.getName(firstName, middleName + " " + lastName);

            fillValue(viewHolder.textViewParentName, WordUtils.capitalize(parentName));

            String dobString = getDuration(Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false));
            fillValue(viewHolder.textViewChildName, WordUtils.capitalize(childName) + ", " + WordUtils.capitalize(Utils.getTranslatedDate(dobString, context)));
        }catch (Exception e){
            String parentFirstName = "test";
            String parentLastName = "test";
            String parentMiddleName = "test";

            String parentName = context.getResources().getString(org.smartregister.chw.core.R.string.care_giver_initials) + ": " + org.smartregister.util.Utils.getName(parentFirstName, parentMiddleName + " " + parentLastName);
            String firstName = "test";
            String middleName = "test";
            String lastName = "test";
            String childName = org.smartregister.util.Utils.getName(firstName, middleName + " " + lastName);

            fillValue(viewHolder.textViewParentName, WordUtils.capitalize(parentName));

            String dobString = "20-20-20";
            fillValue(viewHolder.textViewChildName, WordUtils.capitalize(childName) + ", " + WordUtils.capitalize(Utils.getTranslatedDate(dobString, context)));
            e.printStackTrace();
        }

        addButtonClickListeners(client, viewHolder);
    }

    public void addButtonClickListeners(SmartRegisterClient client, RegisterViewHolder viewHolder) {
        View patient = viewHolder.childColumn;
        attachPatientOnclickListener(patient, client);


        View dueButton = viewHolder.dueButton;
        attachDosageOnclickListener(dueButton, client);
    }

    protected void attachPatientOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(org.smartregister.chw.core.R.id.VIEW_ID, BaseFamilyRegisterFragment.CLICK_VIEW_NORMAL);
    }

    protected void attachDosageOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(org.smartregister.chw.core.R.id.VIEW_ID, BaseFamilyRegisterFragment.CLICK_VIEW_DOSAGE_STATUS);
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public View.OnClickListener getPaginationClickListener() {
        return paginationClickListener;
    }

    public void setPaginationClickListener(View.OnClickListener paginationClickListener) {
        this.paginationClickListener = paginationClickListener;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
