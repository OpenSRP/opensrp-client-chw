package org.smartregister.chw.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import org.apache.commons.lang3.text.WordUtils;
import org.smartregister.chw.R;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.holders.RegisterViewHolder;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.task.DeadUpdateLastAsyncTask;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;
import java.util.Set;
import static org.smartregister.chw.core.utils.Utils.getDuration;
import static org.smartregister.chw.util.CrvsConstants.*;
import static org.smartregister.chw.util.Utils.getClientName;

public class DeadClientsProvider extends CoreDeadClientsProvider {

    private Set<org.smartregister.configurableviews.model.View> visibleColumns;
    private View.OnClickListener onClickListener;
    private Context context;
    private CommonRepository commonRepository;

    public DeadClientsProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, visibleColumns, onClickListener, paginationClickListener);
        this.visibleColumns = visibleColumns;
        this.onClickListener = onClickListener;
        this.context = context;
        this.commonRepository = commonRepository;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, client, viewHolder);
            populateIdentifierColumn(pc, viewHolder);
            populateLastColumn(pc, viewHolder);
            return;
        }
    }

    private int getChildRegisterLayout() {
        return !ChwApplication.getApplicationFlavor().prioritizeChildNameOnChildRegister() ? R.layout.adapter_child_register_list_row : R.layout.adapter_prioritize_child_register_list_row;
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater.inflate(getChildRegisterLayout(), parent, false);
        return new RegisterViewHolder(view);
    }


    protected void populatePatientColumn(CommonPersonObjectClient pc, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        try {
            String parentFirstName = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.FAMILY_FIRST_NAME, true);
            String parentLastName = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.FAMILY_LAST_NAME, true);
            String parentMiddleName = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.FAMILY_MIDDLE_NAME, true);

            String dobString = getDuration(Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false));
            String firstName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
            String middleName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true);
            String lastName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
            String stillBirth = Utils.getValue(pc.getColumnmaps(), PREG_OUTCOME, true);
            String clientType = Utils.getValue(pc.getColumnmaps(), CLIENT_TYPE, true);
            String childName = getClientName(firstName, middleName, lastName);

            if (clientType.equals("Still")){
                String parentName = context.getResources().getString(R.string.care_giver_initials) + ": " + getClientName(parentFirstName, parentMiddleName, parentLastName);
                fillValue(viewHolder.textViewParentName, WordUtils.capitalize(parentName));
                fillValue(viewHolder.textViewChildName, "Baby (" + stillBirth + ")");
                String address = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.FAMILY_HOME_ADDRESS, true);
                fillValue(viewHolder.textViewAddressGender, address);
            }else if (clientType.equals("Adult")){
                viewHolder.textViewChildName.setVisibility(View.GONE);
                viewHolder.textViewChildAge.setVisibility(View.GONE);
                String age = WordUtils.capitalize(Utils.getTranslatedDate(dobString, context));
                String parentName = getClientName(parentFirstName, parentMiddleName, parentLastName);
                fillValue(viewHolder.textViewParentName, WordUtils.capitalize(parentName)+", "+age);
                String address = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.FAMILY_HOME_ADDRESS, true);
                String gender_key = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.GENDER, true);
                String gender = "";
                if (gender_key.equalsIgnoreCase("Male")) {
                    gender = context.getString(org.smartregister.chw.core.R.string.male);
                } else if (gender_key.equalsIgnoreCase("Female")) {
                    gender = context.getString(org.smartregister.chw.core.R.string.female);
                }
                fillValue(viewHolder.textViewAddressGender, address + " \u00B7 " + gender);
            }else if(clientType.equals("Child")){
                String parentName = context.getResources().getString(R.string.care_giver_initials) + ": " + getClientName(parentFirstName, parentMiddleName, parentLastName);
                fillValue(viewHolder.textViewParentName, WordUtils.capitalize(parentName));
                String age = WordUtils.capitalize(Utils.getTranslatedDate(dobString, context));
                fillValue(viewHolder.textViewChildName, WordUtils.capitalize(childName)+", "+age);
                String address = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.FAMILY_HOME_ADDRESS, true);
                String gender_key = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.GENDER, true);
                String gender = "";
                if (gender_key.equalsIgnoreCase("Male")) {
                    gender = context.getString(org.smartregister.chw.core.R.string.male);
                } else if (gender_key.equalsIgnoreCase("Female")) {
                    gender = context.getString(org.smartregister.chw.core.R.string.female);
                }
                fillValue(viewHolder.textViewAddressGender, address + " \u00B7 " + gender);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        addButtonClickListeners(client, viewHolder);

    }

    private void populateLastColumn(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        Utils.startAsyncTask(new DeadUpdateLastAsyncTask(context, commonRepository, viewHolder, pc, onClickListener), null);
    }

}
