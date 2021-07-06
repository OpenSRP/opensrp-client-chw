package org.smartregister.chw.activity;


import com.vijay.jsonwizard.utils.FormUtils;

import org.json.JSONException;
import org.smartregister.chw.tb.activity.BaseTbCommunityFollowupDetailsActivity;
import org.smartregister.chw.util.Constants;

import timber.log.Timber;

public class TbCommunityFollowupDetailsActivity extends BaseTbCommunityFollowupDetailsActivity {

    @Override
    public void openFollowupForm() {
        try {
            TbRegisterActivity.startTbFormActivity(this, getMemberObject().getBaseEntityId(), Constants.JSON_FORM.getHivCommunityFollowFeedback(), (new FormUtils()).getFormJsonFromRepositoryOrAssets(this, Constants.JSON_FORM.getHivCommunityFollowFeedback()).toString());
        } catch (JSONException e) {
            Timber.e(e);
        }
    }
}
 