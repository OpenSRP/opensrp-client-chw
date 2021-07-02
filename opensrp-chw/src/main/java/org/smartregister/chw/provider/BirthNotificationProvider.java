package org.smartregister.chw.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.text.WordUtils;
import org.smartregister.chw.R;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.holders.RegisterViewHolder;
import org.smartregister.chw.core.provider.CoreChildRegisterProvider;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.task.ChwUpdateBirthNotificationLastAsyncTask;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Set;

import static org.smartregister.chw.core.utils.Utils.getDuration;
import static org.smartregister.chw.util.CrvsConstants.BIRTH_CERT;
import static org.smartregister.chw.util.CrvsConstants.BIRTH_NOTIFICATION;
import static org.smartregister.chw.util.CrvsConstants.BIRTH_REGISTRATION;
import static org.smartregister.chw.util.CrvsConstants.NOTIFICATION_DONE;
import static org.smartregister.chw.util.CrvsConstants.REGISTRATION_DONE;
import static org.smartregister.chw.util.CrvsConstants.YES;
import static org.smartregister.chw.util.Utils.getClientName;

/**
 * Created by keyman on 13/11/2018.
 */

public class BirthNotificationProvider extends CoreChildRegisterProvider {

    private Set<org.smartregister.configurableviews.model.View> visibleColumns;

    private View.OnClickListener onClickListener;

    private Context context;
    private CommonRepository commonRepository;

    public BirthNotificationProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
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

    private void fillChildNameAndAge(RegisterViewHolder viewHolder, String childName, String dobString) {
        String age = context.getResources().getString(R.string.age) + ": " + WordUtils.capitalize(Utils.getTranslatedDate(dobString, context));
        if (!ChwApplication.getApplicationFlavor().prioritizeChildNameOnChildRegister()) {
            fillValue(viewHolder.textViewChildName, WordUtils.capitalize(childName) + ", " + WordUtils.capitalize(Utils.getTranslatedDate(dobString, context)));
        } else {
            fillValue(viewHolder.textViewChildName, WordUtils.capitalize(childName));
            fillValue(viewHolder.textViewChildAge, age);
        }
    }


    protected static void fillValue(TextView v, String value) {
        if (v != null) {
            v.setText(value);
        }
    }

    protected void populatePatientColumn(CommonPersonObjectClient pc, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        String parentFirstName = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.FAMILY_FIRST_NAME, true);
        String parentLastName = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.FAMILY_LAST_NAME, true);
        String parentMiddleName = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.FAMILY_MIDDLE_NAME, true);

        String parentName = context.getResources().getString(R.string.care_giver_initials) + ": " + getClientName(parentFirstName, parentMiddleName, parentLastName);
        String firstName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String middleName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true);
        String lastName = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
        String childName = getClientName(firstName, middleName, lastName);

        fillValue(viewHolder.textViewParentName, WordUtils.capitalize(parentName));

        String dobString = getDuration(Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false));
        //dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;
        fillChildNameAndAge(viewHolder, childName, dobString);
        setAddressAndGender(pc, viewHolder);

        addButtonClickListeners(client, viewHolder);

    }

    public void setAddressAndGender(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        String address = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.FAMILY_HOME_ADDRESS, true);
        String gender = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.GENDER, true);
        fillValue(viewHolder.textViewAddressGender, address + " \u00B7 " + gender);

        String registration = Utils.getValue(pc.getColumnmaps(), BIRTH_REGISTRATION, true);
        String notification = Utils.getValue(pc.getColumnmaps(), BIRTH_NOTIFICATION, true);
        String birth_cert = Utils.getValue(pc.getColumnmaps(), BIRTH_CERT, true);

        if (birth_cert.trim().equalsIgnoreCase(YES)) {
            viewHolder.textViewChildAge.setVisibility(View.GONE);
        } else {
            if (notification.trim().equalsIgnoreCase(YES)) {
                viewHolder.textViewChildAge.setVisibility(View.VISIBLE);
                viewHolder.textViewChildAge.setText(NOTIFICATION_DONE);
            } else if (registration.trim().equalsIgnoreCase(YES)) {
                viewHolder.textViewChildAge.setVisibility(View.VISIBLE);
                viewHolder.textViewChildAge.setText(REGISTRATION_DONE);
            } else {
                viewHolder.textViewChildAge.setVisibility(View.GONE);
            }
        }
    }

    private void populateLastColumn(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        Utils.startAsyncTask(new ChwUpdateBirthNotificationLastAsyncTask(context, commonRepository, viewHolder, pc.entityId(), onClickListener, pc), null);
    }

}
