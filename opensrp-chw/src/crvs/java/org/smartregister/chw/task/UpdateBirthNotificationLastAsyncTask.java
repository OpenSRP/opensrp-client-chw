package org.smartregister.chw.task;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.api.Rules;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.BirthNotificationUpdateActivity;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.dao.VisitDao;
import org.smartregister.chw.core.domain.VisitSummary;
import org.smartregister.chw.core.holders.RegisterViewHolder;
import org.smartregister.chw.core.model.ChildVisit;
import org.smartregister.chw.core.utils.ChwDBConstants;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import timber.log.Timber;
import static org.smartregister.chw.core.utils.Utils.getDuration;
import static org.smartregister.chw.util.CrvsConstants.BIRTH_CERT;
import static org.smartregister.chw.util.CrvsConstants.BIRTH_CERTIFICATE_ISSUE_DATE;
import static org.smartregister.chw.util.CrvsConstants.BIRTH_CERT_NUM;
import static org.smartregister.chw.util.CrvsConstants.BIRTH_NOTIFICATION;
import static org.smartregister.chw.util.CrvsConstants.BIRTH_REGISTRATION;
import static org.smartregister.chw.util.CrvsConstants.YES;

public class UpdateBirthNotificationLastAsyncTask extends AsyncTask<Void, Void, Void> {
    public final Context context;
    private final CommonRepository commonRepository;
    public final RegisterViewHolder viewHolder;
    public final String baseEntityId;
    private final Rules rules;
    public CommonPersonObject commonPersonObject;
    public ChildVisit childVisit;
    public View.OnClickListener onClickListener;
    private SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    private CommonPersonObjectClient pc;

    public UpdateBirthNotificationLastAsyncTask(Context context, CommonRepository commonRepository, RegisterViewHolder viewHolder, String baseEntityId, View.OnClickListener onClickListener, CommonPersonObjectClient pc) {
        this.context = context;
        this.commonRepository = commonRepository;
        this.viewHolder = viewHolder;
        this.baseEntityId = baseEntityId;
        this.onClickListener = onClickListener;
        this.rules = CoreChwApplication.getInstance().getRulesEngineHelper().rules(CoreConstants.RULE_FILE.HOME_VISIT);
        this.pc = pc;
    }

    @Override
    public Void doInBackground(Void... params) {
        if (commonRepository != null) {
            commonPersonObject = commonRepository.findByBaseEntityId(baseEntityId);

            Map<String, VisitSummary> map = VisitDao.getVisitSummary(baseEntityId);
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

                String dobString = getDuration(Utils.getValue(commonPersonObject.getColumnmaps(), DBConstants.KEY.DOB, false));
                childVisit = CoreChildUtils.getChildVisitStatus(context, rules, dobString, lastVisit, visitNot, dateCreated);
            }
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        if (commonPersonObject != null) {
            viewHolder.dueButton.setVisibility(View.VISIBLE);
            String birth_cert = Utils.getValue(commonPersonObject.getColumnmaps(), BIRTH_CERT, true);
            String birth_notification = Utils.getValue(commonPersonObject.getColumnmaps(), BIRTH_NOTIFICATION, true);
            String birth_registration = Utils.getValue(pc.getColumnmaps(), BIRTH_REGISTRATION, true);
            try {
                if (birth_cert.trim().equalsIgnoreCase(YES)) {
                    setReceivedButtonColor(context, viewHolder.dueButton);
                } else {
                    if (birth_notification.trim().equalsIgnoreCase(YES) || birth_registration.trim().equalsIgnoreCase(YES)) {
                        setStatusUpdated(context, viewHolder.dueButton);
                    } else {
                        setUpdateStatusButtonColor(context, viewHolder.dueButton);
                    }
                }
            } catch (Exception e) {
                viewHolder.dueButton.setText(context.getResources().getString(R.string.update_status));
                e.printStackTrace();
            }
            viewHolder.dueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String entityId = Utils.getValue(commonPersonObject.getColumnmaps(), DBConstants.KEY.BASE_ENTITY_ID, true);
                    String birth_cert_issue_date = Utils.getValue(commonPersonObject.getColumnmaps(), BIRTH_CERTIFICATE_ISSUE_DATE, true);
                    String birth_cert_num = Utils.getValue(commonPersonObject.getColumnmaps(), BIRTH_CERT_NUM, true);

                    Intent intent = new Intent(context, BirthNotificationUpdateActivity.class);
                    intent.putExtra(org.smartregister.chw.util.Constants.ACTIVITY_PAYLOAD.ACTION, org.smartregister.chw.util.Constants.ACTION.START_REGISTRATION);
                    intent.putExtra(DBConstants.KEY.BASE_ENTITY_ID, entityId);
                    intent.putExtra(BIRTH_CERT, birth_cert);
                    intent.putExtra(BIRTH_CERTIFICATE_ISSUE_DATE, birth_cert_issue_date);
                    intent.putExtra(BIRTH_CERT_NUM, birth_cert_num);
                    intent.putExtra(BIRTH_NOTIFICATION, birth_notification);
                    intent.putExtra(BIRTH_REGISTRATION, birth_registration);
                    context.startActivity(intent);

                }
            });
        } else {
            viewHolder.dueButton.setVisibility(View.GONE);
        }

    }

    public void setStatusUpdated(Context context, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(R.color.black));
        dueButton.setText(context.getString(R.string.status_updated));
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

    public void startFormActivity(JSONObject jsonForm) {

        Intent intent = new Intent(context, Utils.metadata().familyMemberFormActivity);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());


        Form form = new Form();
        form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
        form.setWizard(false);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);
    }

    public void setVisitButtonDueStatus(Context context, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(org.smartregister.chw.core.R.color.alert_in_progress_blue));
        dueButton.setText(context.getString(org.smartregister.chw.core.R.string.record_home_visit));
        dueButton.setBackgroundResource(org.smartregister.chw.core.R.drawable.blue_btn_selector);
        dueButton.setOnClickListener(onClickListener);
    }

    public void setVisitButtonOverdueStatus(Context context, Button dueButton, String lastVisitDays) {
        dueButton.setTextColor(context.getResources().getColor(org.smartregister.chw.core.R.color.white));
        if (TextUtils.isEmpty(lastVisitDays)) {
            dueButton.setText(context.getString(org.smartregister.chw.core.R.string.record_visit));
        } else {
            dueButton.setText(context.getString(org.smartregister.chw.core.R.string.due_visit, lastVisitDays));
        }

        dueButton.setBackgroundResource(org.smartregister.chw.core.R.drawable.overdue_red_btn_selector);
        dueButton.setOnClickListener(onClickListener);
    }

    public void setVisitLessTwentyFourView(Context context, Button dueButton) {
        setVisitAboveTwentyFourView(context, dueButton);
    }

    public void setVisitAboveTwentyFourView(Context context, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(org.smartregister.chw.core.R.color.alert_complete_green));
        dueButton.setText(context.getString(org.smartregister.chw.core.R.string.visit_done));
        dueButton.setBackgroundColor(context.getResources().getColor(org.smartregister.chw.core.R.color.transparent));
        dueButton.setOnClickListener(null);
    }

    public void setVisitNotDone(Context context, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(org.smartregister.chw.core.R.color.progress_orange));
        dueButton.setText(context.getString(org.smartregister.chw.core.R.string.visit_not_done));
        dueButton.setBackgroundColor(context.getResources().getColor(org.smartregister.chw.core.R.color.transparent));
        dueButton.setOnClickListener(null);
    }
}
