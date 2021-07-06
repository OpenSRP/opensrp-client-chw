package org.smartregister.chw.activity;

import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONException;
import org.smartregister.chw.hiv.activity.BaseHivCommunityFollowupDetailsActivity;

import timber.log.Timber;

public class HivCommunityFollowupDetailsActivity extends BaseHivCommunityFollowupDetailsActivity {
    @Override
    protected void openFollowupForm() {
        try {
            HivRegisterActivity.startHIVFormActivity(this, getMemberObject().getBaseEntityId(), org.smartregister.chw.util.Constants.JSON_FORM.getHivCommunityFollowFeedback(), (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, org.smartregister.chw.util.Constants.JSON_FORM.getHivCommunityFollowFeedback()).toString());
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

}
 