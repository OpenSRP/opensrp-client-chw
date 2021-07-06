package org.smartregister.chw.reporting;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import org.smartregister.chw.R;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.listener.ChwChartListener;
import org.smartregister.chw.util.ReportingConstants;
import org.smartregister.reporting.contract.ReportContract;
import org.smartregister.reporting.domain.IndicatorTally;
import org.smartregister.reporting.domain.PieChartSlice;
import org.smartregister.reporting.model.NumericDisplayModel;
import org.smartregister.reporting.util.ReportingUtil;
import org.smartregister.reporting.view.NumericIndicatorView;
import org.smartregister.reporting.view.PieChartIndicatorView;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class ChwReport {
    /**
     * Create and add indicator chart and numeric visualisations in dashboard
     *
     * @param mainLayout       view holding the visualisations being displayed
     * @param indicatorTallies list containing indicator counts for display
     */
    public static void showIndicatorVisualisations(ViewGroup mainLayout, List<Map<String, IndicatorTally>> indicatorTallies, Activity context) {
        // Display order as determined in https://docs.google.com/spreadsheets/d/1q9YiWqjLiToTd0--Q8CbwhBwwcNDspbDxVrUfDm8VGU/edit#gid=315573423
      //  ChwChartListener chwChartListener = new ChwChartListener(context);
        int indicator1String = ChwApplication.getApplicationFlavor().showChildrenUnder5() ? R.string.total_under_5_children_label : R.string.total_under_2_children_label;
        NumericDisplayModel indicator1 = ReportingUtil.getIndicatorDisplayModel(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.ChildIndicatorKeys.COUNT_CHILDREN_UNDER_5, indicator1String, indicatorTallies);
        appendView(mainLayout, new NumericIndicatorView(mainLayout.getContext(), indicator1));

        if (ChwApplication.getApplicationFlavor().hasANC())
            createAncReportViews(mainLayout, indicatorTallies);

        if (ChwApplication.getApplicationFlavor().hasPNC())
            createPncReportViews(mainLayout, indicatorTallies);

        PieChartSlice indicator2_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_24_UPTO_DATE_VACCINATIONS, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_24_UPTO_DATE_VACCINATIONS);
        PieChartSlice indicator2_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_24_OVERDUE_VACCINATIONS, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_24_OVERDUE_VACCINATIONS);
        appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(indicator2_1, indicator2_2), R.string.children_0_24_months_upto_date_vaccinations, R.string.opv_0_not_included, null)));

        if (ChwApplication.getApplicationFlavor().hasPNC()) {
            PieChartSlice pnc_indicator_4_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_BABIES_RECEIVED_BGC_ON_TIME, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_BABIES_RECEIVED_BGC_ON_TIME);
            PieChartSlice pnc_indicator_4_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_BABIES_DID_NOT_RECEIVE_BGC_ON_TIME, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_BABIES_DID_NOT_RECEIVE_BGC_ON_TIME);
            appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(pnc_indicator_4_1, pnc_indicator_4_2), R.string.pnc_indicator_4, R.string.pnc_indicator_4_note, null)));
        }

        createChildReportViews(mainLayout, indicatorTallies, context);

        if (ChwApplication.getApplicationFlavor().hasPNC()) {
            NumericDisplayModel pnc_indicator_9 = ReportingUtil.getIndicatorDisplayModel(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_DECEASED_NEWBORNS_0_28, R.string.pnc_indicator_9, indicatorTallies);
            appendView(mainLayout, new NumericIndicatorView(mainLayout.getContext(), pnc_indicator_9));
        }

        NumericDisplayModel indicator8 = ReportingUtil.getIndicatorDisplayModel(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.ChildIndicatorKeys.DECEASED_CHILDREN_0_11_MONTHS, R.string.deceased_children_0_11_months, indicatorTallies);
        appendView(mainLayout, new NumericIndicatorView(mainLayout.getContext(), indicator8));

        int indicator9String = ChwApplication.getApplicationFlavor().showChildrenUnder5() ? R.string.deceased_children_12_59_months : R.string.deceased_children_12_23_months;
        NumericDisplayModel indicator9 = ReportingUtil.getIndicatorDisplayModel(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.ChildIndicatorKeys.DECEASED_CHILDREN_12_59_MONTHS, indicator9String , indicatorTallies);
        appendView(mainLayout, new NumericIndicatorView(mainLayout.getContext(), indicator9));

        if (ChwApplication.getApplicationFlavor().hasPNC()) {
            PieChartSlice pnc_indicator_5_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_OTHER_WRA_DEATHS, mainLayout.getContext().getResources().getString(R.string.other_wra_deaths_slice_label), mainLayout.getContext().getResources().getColor(R.color.black), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_OTHER_WRA_DEATHS);
            PieChartSlice pnc_indicator_5_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_MATERNAL_DEATHS, mainLayout.getContext().getResources().getString(R.string.maternal_deaths_slice_label), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_MATERNAL_DEATHS);
            appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(pnc_indicator_5_1, pnc_indicator_5_2), R.string.pnc_indicator_5, R.string.pnc_indicator_5_note, null)));
        }
    }

    public static void createPncReportViews(ViewGroup mainLayout, List<Map<String, IndicatorTally>> indicatorTallies) {
        PieChartSlice pnc_indicator_1_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_WOMEN_DELIVERED_IN_HF, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_WOMEN_DELIVERED_IN_HF);
        PieChartSlice pnc_indicator_1_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_WOMEN_DELIVERED_ELSEWHERE, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_WOMEN_DELIVERED_ELSEWHERE);

        appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(pnc_indicator_1_1, pnc_indicator_1_2), R.string.pnc_indicator_1, null, null)));

        PieChartSlice pnc_indicator_2_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_BABIES_NORMAL_BIRTHWEIGHT, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_BABIES_NORMAL_BIRTHWEIGHT);
        PieChartSlice pnc_indicator_2_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_BABIES_LOW_BIRTHWEIGHT, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_BABIES_LOW_BIRTHWEIGHT);
        appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(pnc_indicator_2_1, pnc_indicator_2_2), R.string.pnc_indicator_2, null, null)));

        PieChartSlice pnc_indicator_3_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_BABIES_INITIATED_EARLY_BREASTFEEDING, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_BABIES_INITIATED_EARLY_BREASTFEEDING);
        PieChartSlice pnc_indicator_3_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_BABIES_NOT_INITIATED_EARLY_BREASTFEEDING, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_BABIES_NOT_INITIATED_EARLY_BREASTFEEDING);
        appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(pnc_indicator_3_1, pnc_indicator_3_2), R.string.pnc_indicator_3, null, null)));

        PieChartSlice pnc_indicator_10_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_WOMAN_PNC_VISIT_DONE, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_WOMAN_PNC_VISIT_DONE);
        PieChartSlice pnc_indicator_10_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_WOMAN_PNC_VISIT_NOT_DONE, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_WOMAN_PNC_VISIT_NOT_DONE);
        appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(pnc_indicator_10_1, pnc_indicator_10_2), R.string.pnc_indicator_10, null, null)));

        PieChartSlice pnc_indicator_6_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_NEWBORNS_WITH_NO_DANGER_SIGNS, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_NEWBORNS_WITH_NO_DANGER_SIGNS);
        PieChartSlice pnc_indicator_6_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_NEWBORNS_WITH_DANGER_SIGNS, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_NEWBORNS_WITH_DANGER_SIGNS);
        appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(pnc_indicator_6_1, pnc_indicator_6_2), R.string.pnc_indicator_7, null, null)));

        PieChartSlice pnc_indicator_7_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_WOMEN_WITH_NO_DANGER_SIGNS, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_WOMEN_WITH_NO_DANGER_SIGNS);
        PieChartSlice pnc_indicator_7_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_WOMEN_WITH_DANGER_SIGNS, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_WOMEN_WITH_DANGER_SIGNS);
        appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(pnc_indicator_7_1, pnc_indicator_7_2), R.string.pnc_indicator_6, null, null)));

        PieChartSlice pnc_indicator_8_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_NONE, mainLayout.getContext().getResources().getString(R.string.none), mainLayout.getContext().getResources().getColor(R.color.pie_chart_red), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_NONE);
        PieChartSlice pnc_indicator_8_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_ABSTINENCE, mainLayout.getContext().getResources().getString(R.string.abstinence), mainLayout.getContext().getResources().getColor(R.color.pie_chart_blue), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_ABSTINENCE);
        PieChartSlice pnc_indicator_8_3 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_CONDOM, mainLayout.getContext().getResources().getString(R.string.condom), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yellow), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_CONDOM);
        PieChartSlice pnc_indicator_8_4 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_TABLETS, mainLayout.getContext().getResources().getString(R.string.tablets), mainLayout.getContext().getResources().getColor(R.color.pie_chart_green), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_TABLETS);
        PieChartSlice pnc_indicator_8_5 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_INJECTABLE, mainLayout.getContext().getResources().getString(R.string.injectable), mainLayout.getContext().getResources().getColor(R.color.pie_chart_purple), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_INJECTABLE);
        PieChartSlice pnc_indicator_8_6 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_IUD, mainLayout.getContext().getResources().getString(R.string.iud), mainLayout.getContext().getResources().getColor(R.color.pie_chart_orange), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_IUD);
        PieChartSlice pnc_indicator_8_7 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_IMPLANT, mainLayout.getContext().getResources().getString(R.string.implant), mainLayout.getContext().getResources().getColor(R.color.pie_chart_brown), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_IMPLANT);
        PieChartSlice pnc_indicator_8_8 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_OTHER, mainLayout.getContext().getResources().getString(R.string.other), mainLayout.getContext().getResources().getColor(R.color.black), indicatorTallies, ReportingConstants.PncIndicatorKeysHelper.COUNT_POSTPARTUM_FP_METHOD_OTHER);
        List<PieChartSlice> slices = ReportingUtil.addPieChartSlices(pnc_indicator_8_1, pnc_indicator_8_2, pnc_indicator_8_3, pnc_indicator_8_4, pnc_indicator_8_5, pnc_indicator_8_6, pnc_indicator_8_7, pnc_indicator_8_8);
        appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(slices, R.string.pnc_indicator_8, null, null)));
    }


    public static void createAncReportViews(ViewGroup mainLayout, List<Map<String, IndicatorTally>> indicatorTallies) {
        //Disclaimer: Pie charts have binary slices yes and no with different tallying done separately ;)
        NumericDisplayModel anc_indicator1 = ReportingUtil.getIndicatorDisplayModel(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.AncIndicatorKeys.COUNT_WRA, R.string.anc_indicator1, indicatorTallies);
        appendView(mainLayout, new NumericIndicatorView(mainLayout.getContext(), anc_indicator1));

        NumericDisplayModel anc_indicator2 = ReportingUtil.getIndicatorDisplayModel(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.AncIndicatorKeys.COUNT_PREGNANT_WOMEN, R.string.anc_indicator2, indicatorTallies);
        appendView(mainLayout, new NumericIndicatorView(mainLayout.getContext(), anc_indicator2));

        PieChartSlice anc_indicator3_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_DUE_HOME_VISIT, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_DUE_HOME_VISIT);
        PieChartSlice anc_indicator3_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_OVERDUE_HOME_VISIT, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_OVERDUE_HOME_VISIT);
        appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(anc_indicator3_1, anc_indicator3_2), R.string.anc_indicator3, null, null)));

        PieChartSlice anc_indicator4_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_DUE_HEALTH_FACILITY_VISIT, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_DUE_HEALTH_FACILITY_VISIT);
        PieChartSlice anc_indicator4_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_OVERDUE_HEALTH_FACILITY_VISIT, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_OVERDUE_HEALTH_FACILITY_VISIT);
        appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(anc_indicator4_1, anc_indicator4_2), R.string.anc_indicator4, null, null)));

        PieChartSlice anc_indicator5_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_TESTED_HIV, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_TESTED_HIV);
        PieChartSlice anc_indicator5_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_NOT_TESTED_HIV, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_NOT_TESTED_HIV);
        appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(anc_indicator5_1, anc_indicator5_2), R.string.anc_indicator5, null, null)));

        PieChartSlice anc_indicator6_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_TESTED_SYPHILIS, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_TESTED_SYPHILIS);
        PieChartSlice anc_indicator6_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_NOT_TESTED_SYPHILIS, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_NOT_TESTED_SYPHILIS);
        appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(anc_indicator6_1, anc_indicator6_2), R.string.anc_indicator6, null, null)));

        PieChartSlice anc_indicator7_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_DUE_TT_IMMUNIZATION, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_DUE_TT_IMMUNIZATION);
        PieChartSlice anc_indicator7_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_OVERDUE_TT_IMMUNIZATION, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_OVERDUE_TT_IMMUNIZATION);
        appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(anc_indicator7_1, anc_indicator7_2), R.string.anc_indicator7, null, null)));

        PieChartSlice anc_indicator8_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_DUE_IPTPSP, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_DUE_IPTPSP);
        PieChartSlice anc_indicator8_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_OVERDUE_IPTPSP, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.AncIndicatorKeys.COUNT_WOMEN_OVERDUE_IPTPSP);
        appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(anc_indicator8_1, anc_indicator8_2), R.string.anc_indicator8, null, null)));
    }

    public static void createChildReportViews(ViewGroup mainLayout, List<Map<String, IndicatorTally>> indicatorTallies, Activity context) {
        //Disclaimer: Pie charts have binary slices yes and no with different tallying done separately ;)
        ChwChartListener chwChartListener = new ChwChartListener(context);

        if (ChwApplication.getApplicationFlavor().hasJobAidsBirthCertificationGraph()) {
            PieChartSlice indicator7_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_59_WITH_BIRTH_CERT, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_59_WITH_BIRTH_CERT);
            PieChartSlice indicator7_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_59_WITH_NO_BIRTH_CERT, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_59_WITH_NO_BIRTH_CERT);
            appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(indicator7_1, indicator7_2), R.string.children_0_59_months_with_birth_certificate, null, chwChartListener)));
        }

        if (ChwApplication.getApplicationFlavor().hasJobAidsBreastfeedingGraph()) {
            PieChartSlice indicator3_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_5_EXCLUSIVELY_BREASTFEEDING, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_5_EXCLUSIVELY_BREASTFEEDING);
            PieChartSlice indicator3_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_5_NOT_EXCLUSIVELY_BREASTFEEDING, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_5_NOT_EXCLUSIVELY_BREASTFEEDING);
            appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(indicator3_1, indicator3_2), R.string.children_0_5_months_exclusively_breastfeeding, null, chwChartListener)));
        }

        if (ChwApplication.getApplicationFlavor().hasJobAidsVitaminAGraph()) {
            PieChartSlice indicator4_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_6_59_VITAMIN_RECEIVED_A, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_6_59_VITAMIN_RECEIVED_A);
            PieChartSlice indicator4_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_6_59_VITAMIN_NOT_RECEIVED_A, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_6_59_VITAMIN_NOT_RECEIVED_A);
            appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(indicator4_1, indicator4_2), R.string.children_6_59_months_received_vitamin_A, null, chwChartListener)));
        }

        if (ChwApplication.getApplicationFlavor().hasJobAidsDewormingGraph()) {
            PieChartSlice indicator5_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_12_59_DEWORMED, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_12_59_DEWORMED);
            PieChartSlice indicator5_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_12_59_NOT_DEWORMED, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_12_59_NOT_DEWORMED);
            appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(indicator5_1, indicator5_2), R.string.children_12_59_months_dewormed, null, chwChartListener)));
        }


        if (ChwApplication.getApplicationFlavor().hasChildrenMNPSupplementationGraph()) {
            PieChartSlice indicator6_1 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_6_23_UPTO_DATE_MNP, mainLayout.getContext().getResources().getString(R.string.yes), mainLayout.getContext().getResources().getColor(R.color.pie_chart_yes_green), indicatorTallies, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_6_23_UPTO_DATE_MNP);
            PieChartSlice indicator6_2 = ReportingUtil.getPieChartSlice(ReportContract.IndicatorView.CountType.LATEST_COUNT, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_6_23_OVERDUE_MNP, mainLayout.getContext().getResources().getString(R.string.no), mainLayout.getContext().getResources().getColor(R.color.pie_chart_no_red), indicatorTallies, ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_6_23_OVERDUE_MNP);
            appendView(mainLayout, new PieChartIndicatorView(mainLayout.getContext(), ReportingUtil.getPieChartDisplayModel(ReportingUtil.addPieChartSlices(indicator6_1, indicator6_2), R.string.children_6_23_months_upto_date_mnp, null, chwChartListener)));
        }
    }


    /**
     * Generating a pie chart is memory intensive in lower end devices.
     * Allow @java.lang.OutOfMemoryError is recorded in some devices
     *
     * @return view
     */
    public static void appendView(ViewGroup parentView, ReportContract.IndicatorView indicatorView) {
        try {
            View view = indicatorView.createView();
            if (view != null)
                parentView.addView(view);
        } catch (OutOfMemoryError e) {
            Timber.e(e);
        }
    }
}
