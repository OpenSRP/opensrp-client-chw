package org.smartregister.chw.provider;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;

import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.api.Rules;
import org.smartregister.chw.R;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.model.ChildVisit;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.interactor.ChildProfileInteractor;
import org.smartregister.chw.util.ChildUtils;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.Utils;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.fragment.BaseFamilyProfileMemberFragment;
import org.smartregister.family.provider.FamilyDueRegisterProvider;
import org.smartregister.family.provider.FamilyMemberRegisterProvider;
import org.smartregister.family.util.DBConstants;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.customcontrols.FontVariant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.family.util.Utils.getName;

public class ChwMemberRegisterProvider extends FamilyMemberRegisterProvider {
    private Context context;
    private View.OnClickListener onClickListener;
    private ImageRenderHelper imageRenderHelper;

    public ChwMemberRegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener, String familyHead, String primaryCaregiver) {
        super(context, commonRepository, visibleColumns, onClickListener, paginationClickListener, familyHead, primaryCaregiver);
        this.onClickListener = onClickListener;
        this.context = context;
        this.imageRenderHelper = new ImageRenderHelper(context);
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        super.getView(cursor, client, viewHolder);

        // Update UI cutoffs
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewHolder.profile.getLayoutParams();
        layoutParams.width = context.getResources().getDimensionPixelSize(R.dimen.member_profile_pic_width);
        layoutParams.height = context.getResources().getDimensionPixelSize(R.dimen.member_profile_pic_width);
        layoutParams.setMarginStart(context.getResources().getDimensionPixelSize(R.dimen.change_layout_to_start));
        viewHolder.profile.setLayoutParams(layoutParams);
        viewHolder.patientNameAge.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimensionPixelSize(R.dimen.member_profile_list_title_size));

        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;

        viewHolder.statusLayout.setVisibility(View.GONE);
        viewHolder.status.setVisibility(View.GONE);
        viewHolder.status.getLayoutParams().height = context.getResources().getDimensionPixelSize(R.dimen.member_profile_pic_width);


        String entityType = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.ENTITY_TYPE, false);
        if (Constants.TABLE_NAME.CHILD.equals(entityType)) {
            Utils.startAsyncTask(new UpdateAsyncTask(viewHolder, pc), null);
        }
        populatePatientColumn(pc, client, viewHolder);

    }

    private Map<String, String> getChildDetails(String baseEntityId) {
        SmartRegisterQueryBuilder queryBUilder = new SmartRegisterQueryBuilder();
        queryBUilder.SelectInitiateMainTable(CommonFtsObject.searchTableName(Constants.TABLE_NAME.CHILD), new String[]{CommonFtsObject.idColumn, ChildDBConstants.KEY.LAST_HOME_VISIT, ChildDBConstants.KEY.VISIT_NOT_DONE, ChildDBConstants.KEY.DATE_CREATED});
        String query = queryBUilder.mainCondition(String.format(" %s is null AND %s = '%s' AND %s ",
                DBConstants.KEY.DATE_REMOVED,
                CommonFtsObject.idColumn,
                baseEntityId,
                ChildDBConstants.childAgeLimitFilter()));

        query = query.replace(CommonFtsObject.searchTableName(Constants.TABLE_NAME.CHILD) + ".id as _id ,", "");

        CommonRepository commonRepository = Utils.context().commonrepository(Constants.TABLE_NAME.CHILD);
        List<Map<String, String>> res = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = commonRepository.queryTable(query);
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                int columncount = cursor.getColumnCount();
                Map<String, String> columns = new HashMap<>();
                for (int i = 0; i < columncount; i++) {
                    columns.put(cursor.getColumnName(i), cursor.getType(i) == Cursor.FIELD_TYPE_NULL ? null : String.valueOf(cursor.getString(i)));
                }
                res.add(columns);
                cursor.moveToNext();
            }
        } catch (Exception e) {
            Timber.e(e, e.toString());
        } finally {
            if (cursor != null)
                cursor.close();
        }

        if (res.isEmpty()) {
            return null;
        }
        return res.get(0);
    }

    private ChildVisit retrieveChildVisitList(Rules rules, CommonPersonObjectClient pc, Map<String, String> map) {
        String dob = Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false);
        String dobString = Utils.getDuration(dob);
        String lastVisitDate = map.get(ChildDBConstants.KEY.LAST_HOME_VISIT);
        String visitNotDone = map.get(ChildDBConstants.KEY.VISIT_NOT_DONE);
        String strDateCreated = map.get(ChildDBConstants.KEY.DATE_CREATED);
        long lastVisit = 0;
        long visitNot = 0;
        long dateCreated = 0;
        if (!TextUtils.isEmpty(lastVisitDate)) {
            lastVisit = Long.valueOf(lastVisitDate);
        }
        if (!TextUtils.isEmpty(visitNotDone)) {
            visitNot = Long.valueOf(visitNotDone);
        }
        if (!TextUtils.isEmpty(strDateCreated)) {
            dateCreated = org.smartregister.family.util.Utils.dobStringToDateTime(strDateCreated).getMillis();
        }
        return ChildUtils.getChildVisitStatus(context, rules, dobString, lastVisit, visitNot, dateCreated);
    }


    private void populatePatientColumn(CommonPersonObjectClient pc, SmartRegisterClient client, final RegisterViewHolder viewHolder) {

        String firstName = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true);
        String middleName = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.MIDDLE_NAME, true);
        String lastName = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.LAST_NAME, true);
        String dueDate = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.DUE_DATE, false);
        String overDueDate = Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.OVER_DUE_DATE, false);

        String patientName = getName(firstName, middleName, lastName);

        String entityType = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.ENTITY_TYPE, false);

        String dob = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOB, false);
        String dobString = org.smartregister.family.util.Utils.getDuration(dob);
        dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;

        String dod = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.DOD, false);
        if (StringUtils.isNotBlank(dod)) {

            dobString = org.smartregister.family.util.Utils.getDuration(dod, dob);
            dobString = dobString.contains("y") ? dobString.substring(0, dobString.indexOf("y")) : dobString;

            patientName = patientName + ", " + org.smartregister.family.util.Utils.getTranslatedDate(dobString, context) + " " + context.getString(R.string.deceased_brackets);
            viewHolder.patientNameAge.setFontVariant(FontVariant.REGULAR);
            viewHolder.patientNameAge.setTextColor(Color.GRAY);
            viewHolder.patientNameAge.setTypeface(viewHolder.patientNameAge.getTypeface(), Typeface.ITALIC);
            viewHolder.profile.setImageResource(org.smartregister.family.util.Utils.getMemberProfileImageResourceIDentifier(entityType));
            viewHolder.nextArrow.setVisibility(View.GONE);
        } else {
            patientName = patientName + ", " + org.smartregister.family.util.Utils.getTranslatedDate(dobString, context);
            viewHolder.patientNameAge.setFontVariant(FontVariant.REGULAR);
            viewHolder.patientNameAge.setTextColor(Color.BLACK);
            viewHolder.patientNameAge.setTypeface(viewHolder.patientNameAge.getTypeface(), Typeface.NORMAL);
            imageRenderHelper.refreshProfileImage(pc.getCaseId(), viewHolder.profile, org.smartregister.family.util.Utils.getMemberProfileImageResourceIDentifier(entityType));
            viewHolder.nextArrow.setVisibility(View.VISIBLE);
        }

        fillValue(viewHolder.patientNameAge, patientName);

        String gender_key = org.smartregister.family.util.Utils.getValue(pc.getColumnmaps(), DBConstants.KEY.GENDER, true);
        String gender = "";
        if (gender_key.equalsIgnoreCase("Male")) {
            gender = context.getString(R.string.male);
        } else if (gender_key.equalsIgnoreCase("Female")) {
            gender = context.getString(R.string.female);
        }
        fillValue(viewHolder.gender, gender);

        viewHolder.nextArrowColumn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.nextArrow.performClick();
            }
        });

        viewHolder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.patientColumn.performClick();
            }
        });

        viewHolder.registerColumns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.patientColumn.performClick();
            }
        });
        if (StringUtils.isBlank(dod)) {
            View patient = viewHolder.patientColumn;
            attachPatientOnclickListener(patient, client);

            View nextArrow = viewHolder.nextArrow;
            attachNextArrowOnclickListener(nextArrow, client);
        }

        updateDueColumn(viewHolder, dueDate, overDueDate);

    }
    private void attachPatientOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(R.id.VIEW_ID, BaseFamilyProfileMemberFragment.CLICK_VIEW_NORMAL);
    }

    private void attachNextArrowOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(R.id.VIEW_ID, BaseFamilyProfileMemberFragment.CLICK_VIEW_NEXT_ARROW);
    }


    /*private void updateDueColumn(RegisterViewHolder viewHolder, ChildVisit childVisit) {
        viewHolder.statusLayout.setVisibility(View.VISIBLE);
        if (childVisit != null) {
            viewHolder.status.setVisibility(View.VISIBLE);
            if (childVisit.getVisitStatus().equalsIgnoreCase(ChildProfileInteractor.VisitType.DUE.name())) {
                viewHolder.status.setImageResource(Utils.getDueProfileImageResourceIDentifier());
            } else if (childVisit.getVisitStatus().equalsIgnoreCase(ChildProfileInteractor.VisitType.OVERDUE.name())) {
                viewHolder.status.setImageResource(Utils.getOverDueProfileImageResourceIDentifier());
            } else {
                viewHolder.status.setVisibility(View.INVISIBLE);
            }
        } else {
            viewHolder.status.setVisibility(View.INVISIBLE);
        }
    }*/


    private void updateDueColumn(RegisterViewHolder viewHolder, String dueDate, String overDueDate) {
        viewHolder.statusLayout.setVisibility(View.VISIBLE);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date _dueDate = sdf.parse(dueDate);
            Date _overDueDate = sdf.parse(overDueDate);
            Date now = new Date();

            viewHolder.status.setVisibility(View.VISIBLE);
            if (_overDueDate.getTime() < now.getTime()) {
                viewHolder.status.setImageResource(Utils.getOverDueProfileImageResourceIDentifier());
            } else if (_dueDate.getTime() < now.getTime()) {
                viewHolder.status.setImageResource(Utils.getDueProfileImageResourceIDentifier());
            }
        } catch (ParseException e) {
            Timber.e(e);
        }
    }

    ////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////

    private class UpdateAsyncTask extends AsyncTask<Void, Void, Void> {
        private final RegisterViewHolder viewHolder;
        private final CommonPersonObjectClient pc;

        private final Rules rules;

        private Map<String, String> map;
        private ChildVisit childVisit;

        private UpdateAsyncTask(RegisterViewHolder viewHolder, CommonPersonObjectClient pc) {
            this.viewHolder = viewHolder;
            this.pc = pc;
            this.rules = ChwApplication.getInstance().getRulesEngineHelper().rules(Constants.RULE_FILE.HOME_VISIT);
        }

        @Override
        protected Void doInBackground(Void... params) {
            map = getChildDetails(pc.getCaseId());
            if (map != null) {
                childVisit = retrieveChildVisitList(rules, pc, map);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            // Update status column
            if (childVisit != null) {
               // updateDueColumn(viewHolder, childVisit);
            }
        }
    }
}
