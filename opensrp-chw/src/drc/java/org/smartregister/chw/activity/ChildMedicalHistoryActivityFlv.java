package org.smartregister.chw.activity;

import org.smartregister.chw.R;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.activity.DefaultChildMedicalHistoryActivityFlv;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.util.ChildUtilsFlv;

public class ChildMedicalHistoryActivityFlv extends DefaultChildMedicalHistoryActivityFlv {

    @Override
    public VisitDetailsFormatter getDieataryFormatter() {
        return (title, details, visitDate) -> {
            String diet_diversity = NCUtils.getText(details);
            String value = "";
            if ("chk_none_of_four_products".equalsIgnoreCase(diet_diversity)) {
                value = context.getString(R.string.drc_minimum_dietary_choice_0);
            } else if ("chw_only_one_of_four_products".equalsIgnoreCase(diet_diversity)) {
                value = context.getString(R.string.drc_minimum_dietary_choice_1);
            } else if ("chw_only_two_of_four_products".equalsIgnoreCase(diet_diversity)) {
                value = context.getString(R.string.drc_minimum_dietary_choice_2);
            } else if ("chw_only_three_of_four_products".equalsIgnoreCase(diet_diversity)) {
                value = context.getString(R.string.drc_minimum_dietary_choice_3);
            } else if ("chw_all_of_four_products".equalsIgnoreCase(diet_diversity)) {
                value = context.getString(R.string.drc_minimum_dietary_choice_4);
            }

            return String.format("%s - %s %s",
                    value,
                    context.getString(org.smartregister.chw.core.R.string.done),
                    sdf.format(visitDate)
            );
        };
    }

    public CoreChildUtils.Flavor getChildUtils() {
        return new ChildUtilsFlv();
    }

}