package org.smartregister.chw.dao;

import android.content.Context;

import org.smartregister.chw.R;
import org.smartregister.chw.core.dao.FHIRBundleDao;
import org.smartregister.chw.util.Constants;
import org.smartregister.fp.BuildConfig;
import org.smartregister.thinkmd.model.FHIRBundleModel;

public class ChildFHIRBundleDao extends FHIRBundleDao {

    @Override
    public FHIRBundleModel fetchFHIRDateModel(Context context, String childBaseEntityId) {
        FHIRBundleModel bundle = super.fetchFHIRDateModel(context, childBaseEntityId);
        bundle.setRootPackageName(context.getApplicationContext().getPackageName());
        bundle.setAppVersion(String.valueOf(BuildConfig.VERSION_CODE));
        bundle.setDisplayLanguage(context.getResources().getConfiguration().locale.getDisplayLanguage());
        bundle.setAppName(context.getResources().getString(R.string.app_name));
        bundle.setAppLanguage(context.getResources().getConfiguration().locale.getLanguage());
        bundle.setEndPointPackageName(Constants.ThinkMdConstants.CHILD_PROFILE_ACTIVITY);
        return bundle;
    }

}
