package org.smartregister.chw.task;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.api.Rules;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.OutOfAreaChildUpdateFormActivity;
import org.smartregister.chw.activity.OutOfAreaDeathUpdateFormActivity;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.dao.VisitDao;
import org.smartregister.chw.core.domain.VisitSummary;
import org.smartregister.chw.core.holders.RegisterViewHolder;
import org.smartregister.chw.core.model.ChildVisit;
import org.smartregister.chw.core.utils.ChwDBConstants;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.util.Constants;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.opd.activity.BaseOpdFormActivity;
import org.smartregister.opd.utils.OpdConstants;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.chw.core.utils.Utils.getDuration;
import static org.smartregister.chw.util.CrvsConstants.BASE_ENTITY_ID;
import static org.smartregister.chw.util.CrvsConstants.NO;
import static org.smartregister.chw.util.CrvsConstants.RECEIVED_DEATH_CERTIFICATE;
import static org.smartregister.chw.util.CrvsConstants.YES;

public class OutOfAreaChildAsyncTask extends AsyncTask<Void, Void, Void> {
    public final Context context;
    private final CommonRepository commonRepository;
    public final RegisterViewHolder viewHolder;
    public final CommonPersonObjectClient baseEntityId;
    private final Rules rules;
    public CommonPersonObject commonPersonObject;
    public ChildVisit childVisit;
    public View.OnClickListener onClickListener;
    private SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public OutOfAreaChildAsyncTask(Context context, CommonRepository commonRepository, RegisterViewHolder viewHolder, CommonPersonObjectClient baseEntityId, View.OnClickListener onClickListener) {
        this.context = context;
        this.commonRepository = commonRepository;
        this.viewHolder = viewHolder;
        this.baseEntityId = baseEntityId;
        this.onClickListener = onClickListener;
        this.rules = CoreChwApplication.getInstance().getRulesEngineHelper().rules(CoreConstants.RULE_FILE.HOME_VISIT);
    }

    @Override
    public Void doInBackground(Void... params) {
        if (commonRepository != null) {
            commonPersonObject = commonRepository.findByBaseEntityId(Utils.getValue(baseEntityId.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false));

            Map<String, VisitSummary> map = VisitDao.getVisitSummary(Utils.getValue(baseEntityId.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false));
            if (map != null) {
                VisitSummary notDoneSummary = map.get(CoreConstants.EventType.CHILD_VISIT_NOT_DONE);
                VisitSummary lastVisitSummary = map.get(CoreConstants.EventType.CHILD_HOME_VISIT);

                long lastVisit = 0;
                long visitNot = 0;
                long dateCreated = 0;
                try {
                    String createVal = Utils.getValue(commonPersonObject.getColumnmaps(), ChwDBConstants.DATE_CREATED, false);
                    if (StringUtils.isNotBlank(createVal))
                        dateCreated = ISO8601DATEFORMAT.parse(createVal).getTime();

                } catch (Exception e) {
                    Timber.e(e);
                }
                if (lastVisitSummary != null)
                    lastVisit = lastVisitSummary.getVisitDate().getTime();

                if (notDoneSummary != null)
                    visitNot = notDoneSummary.getVisitDate().getTime();

                try {
                    String dobString = getDuration(Utils.getValue(commonPersonObject.getColumnmaps(), DBConstants.KEY.DOB, false));
                    childVisit = CoreChildUtils.getChildVisitStatus(context, rules, dobString, lastVisit, visitNot, dateCreated);
                } catch (Exception e) {
                    childVisit = null;
                    e.printStackTrace();
                }
            }
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        viewHolder.dueButtonLayout.setVisibility(View.VISIBLE);
        viewHolder.dueButton.setVisibility(View.VISIBLE);
        String received_death_certificate = "";
        try {
            received_death_certificate = Utils.getValue(baseEntityId.getColumnmaps(), RECEIVED_DEATH_CERTIFICATE, false);
            if (received_death_certificate.trim().equalsIgnoreCase(YES)) {
                setReceivedButtonColor(context, viewHolder.dueButton);
            } else if (received_death_certificate.trim().equalsIgnoreCase(NO)) {
                setNotReceivedButtonColor(context, viewHolder.dueButton);
            } else {
                setUpdateStatusButtonColor(context, viewHolder.dueButton);
            }
        } catch (Exception e) {
            viewHolder.dueButton.setText(context.getResources().getString(R.string.update_status));
            e.printStackTrace();
        }
        viewHolder.dueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String entityId = Utils.getValue(baseEntityId.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, false);
                Intent intent = new Intent(context, OutOfAreaChildUpdateFormActivity.class);
                intent.putExtra(Constants.ACTIVITY_PAYLOAD.ACTION, Constants.ACTION.START_REGISTRATION);
                intent.putExtra(BASE_ENTITY_ID, entityId);
                context.startActivity(intent);
            }
        });

    }

    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(context, BaseOpdFormActivity.class);
        intent.putExtra(OpdConstants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
        Form form = new Form();
        form.setName(context.getString(R.string.update_client_registration));
        form.setActionBarBackground(R.color.family_actionbar);
        form.setNavigationBackground(R.color.family_navigation);
        form.setHomeAsUpIndicator(R.mipmap.ic_cross_white);
        form.setPreviousLabel(context.getResources().getString(R.string.back));
        form.setWizard(false);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
        ((Activity)context).startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    public void setNotReceivedButtonColor(Context context, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(R.color.black));
        dueButton.setText(context.getString(R.string.certificate_not_received));
        dueButton.setBackgroundResource(0);
    }

    public void setReceivedButtonColor(Context context, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(R.color.certificate_received));
        dueButton.setText(context.getString(R.string.certificate_received));
        dueButton.setBackgroundResource(0);
    }

    public void setUpdateStatusButtonColor(Context context, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(R.color.pie_chart_yellow));
        dueButton.setText(context.getString(R.string.update_status));
        dueButton.setBackgroundResource(R.drawable.update_cert_status_btn);
    }
}
