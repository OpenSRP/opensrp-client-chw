package org.smartregister.chw.interactor;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.actionhelper.HomeVisitActionHelper;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.fragment.BaseAncHomeVisitFragment;
import org.smartregister.chw.anc.model.BaseAncHomeVisitAction;
import org.smartregister.chw.anc.util.JsonFormUtils;
import org.smartregister.chw.util.Constants;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class ChildHomeVisitInteractorFlv extends DefaultChildHomeVisitInteractorFlv {

    protected void evaluateDietary() throws Exception {
        int age = getAgeInMonths();
        if (age > 60 || age < 6) {
            return;
        }

        Map<String, List<VisitDetail>> details = getDetails(Constants.EventType.MINIMUM_DIETARY_DIVERSITY);

        BaseAncHomeVisitAction action = getBuilder(context.getString(R.string.minimum_dietary_title))
                .withOptional(false)
                .withDetails(details)
                .withBaseEntityID(memberObject.getBaseEntityId())
                .withProcessingMode(BaseAncHomeVisitAction.ProcessingMode.SEPARATE)
                .withHelper(new DietaryHelper())
                .withDestinationFragment(BaseAncHomeVisitFragment.getInstance(view, Constants.JSON_FORM.CHILD_HOME_VISIT.getDIETARY(), null, details, null))
                .build();

        actionList.put(context.getString(R.string.minimum_dietary_title), action);
    }

    private class DietaryHelper extends HomeVisitActionHelper {
        private String diet_diversity;

        @Override
        public void onPayloadReceived(String jsonPayload) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                diet_diversity = JsonFormUtils.getValue(jsonObject, "diet_diversity");
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        @Override
        public String evaluateSubTitle() {
            if (StringUtils.isBlank(diet_diversity)) {
                return null;
            }

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
            return value;
        }

        @Override
        public BaseAncHomeVisitAction.Status evaluateStatusOnPayload() {
            if (StringUtils.isBlank(diet_diversity)) {
                return BaseAncHomeVisitAction.Status.PENDING;
            }

            if ("chw_all_of_four_products".equalsIgnoreCase(diet_diversity)) {
                return BaseAncHomeVisitAction.Status.COMPLETED;
            }

            return BaseAncHomeVisitAction.Status.PARTIALLY_COMPLETED;
        }
    }

}
