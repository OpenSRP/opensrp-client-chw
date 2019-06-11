package org.smartregister.chw.provider;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import org.jeasy.rules.api.Rules;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.provider.AncRegisterProvider;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.interactor.ChildProfileInteractor;
import org.smartregister.chw.util.AncHomeVisitUtil;
import org.smartregister.chw.util.AncVisit;
import org.smartregister.chw.util.ChildDBConstants;
import org.smartregister.chw.util.ChwDBConstants;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.Utils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.view.contract.SmartRegisterClient;

import java.util.Set;

public class ChwAncRegisterProvider extends AncRegisterProvider {

    private Context context;
    private View.OnClickListener onClickListener;

    public ChwAncRegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns, View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {
        super(context, commonRepository, visibleColumns, onClickListener, paginationClickListener);
        this.context = context;
        this.onClickListener = onClickListener;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        super.getView(cursor, client, viewHolder);

        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        Utils.startAsyncTask(new UpdateAsyncTask(context, viewHolder, pc), null);
    }

    private void setVisitButtonDueStatus(Context context, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(R.color.alert_in_progress_blue));
        dueButton.setText(context.getString(R.string.record_home_visit));
        dueButton.setBackgroundResource(R.drawable.blue_btn_selector);
        dueButton.setOnClickListener(onClickListener);
    }

    private void setVisitButtonOverdueStatus(Context context, Button dueButton, String lastVisitDays) {
        dueButton.setTextColor(context.getResources().getColor(R.color.white));
        if (TextUtils.isEmpty(lastVisitDays)) {
            dueButton.setText(context.getString(R.string.record_visit));
        } else {
            dueButton.setText(context.getString(R.string.due_visit, lastVisitDays));
        }
        dueButton.setBackgroundResource(R.drawable.overdue_red_btn_selector);
        dueButton.setOnClickListener(onClickListener);
    }

    private void setVisitAboveTwentyFourView(Context context, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(R.color.alert_complete_green));
        dueButton.setText(context.getString(R.string.visit_done));
        dueButton.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        dueButton.setOnClickListener(null);
    }

    private void setVisitLessTwentyFourView(Context context, Button dueButton) {
        setVisitAboveTwentyFourView(context, dueButton);
    }

    private void setVisitNotDone(Context context, Button dueButton) {
        dueButton.setTextColor(context.getResources().getColor(R.color.progress_orange));
        dueButton.setText(context.getString(R.string.visit_not_done));
        dueButton.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        dueButton.setOnClickListener(null);
    }

    private void updateDueColumn(Context context, RegisterViewHolder viewHolder, AncVisit ancVisit) {
        viewHolder.dueButton.setVisibility(View.VISIBLE);
        if (ancVisit.getVisitStatus().equalsIgnoreCase(ChildProfileInteractor.VisitType.DUE.name())) {
            setVisitButtonDueStatus(context, viewHolder.dueButton);
        } else if (ancVisit.getVisitStatus().equalsIgnoreCase(ChildProfileInteractor.VisitType.OVERDUE.name())) {
            setVisitButtonOverdueStatus(context, viewHolder.dueButton, ancVisit.getNoOfMonthDue());
        } else if (ancVisit.getVisitStatus().equalsIgnoreCase(ChildProfileInteractor.VisitType.LESS_TWENTY_FOUR.name())) {
            setVisitLessTwentyFourView(context, viewHolder.dueButton);
        } else if (ancVisit.getVisitStatus().equalsIgnoreCase(ChildProfileInteractor.VisitType.VISIT_THIS_MONTH.name())) {
            setVisitAboveTwentyFourView(context, viewHolder.dueButton);
        } else if (ancVisit.getVisitStatus().equalsIgnoreCase(ChildProfileInteractor.VisitType.NOT_VISIT_THIS_MONTH.name())) {
            setVisitNotDone(context, viewHolder.dueButton);
        }
    }

    private class UpdateAsyncTask extends AsyncTask<Void, Void, Void> {
        private final RegisterViewHolder viewHolder;
        private final CommonPersonObjectClient pc;
        private final Context context;

        private final Rules rules;
        private AncVisit ancVisit;

        private UpdateAsyncTask(Context context, RegisterViewHolder viewHolder, CommonPersonObjectClient pc) {
            this.context = context;
            this.viewHolder = viewHolder;
            this.pc = pc;
            this.rules = ChwApplication.getInstance().getRulesEngineHelper().rules(Constants.RULE_FILE.ANC_HOME_VISIT);
        }

        @Override
        protected Void doInBackground(Void... params) {
            //map = getChildDetails(pc.getCaseId());

            String lmpDate = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), ChwDBConstants.LMP, false);
            String visitDate = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), ChildDBConstants.KEY.LAST_HOME_VISIT, false);
            String lastVisitNotDone = org.smartregister.util.Utils.getValue(pc.getColumnmaps(), ChwDBConstants.VISIT_NOT_DONE, false);

            ancVisit = AncHomeVisitUtil.getVisitStatus(context, rules, lmpDate, visitDate, lastVisitNotDone);
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            // Update status column
            if (ancVisit != null && !ancVisit.getVisitStatus().equalsIgnoreCase(ChildProfileInteractor.VisitType.EXPIRY.name())) {
                updateDueColumn(context, viewHolder, ancVisit);
            }
        }
    }

}
