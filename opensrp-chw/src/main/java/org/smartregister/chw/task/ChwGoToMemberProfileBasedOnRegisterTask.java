package org.smartregister.chw.task;

import android.app.Activity;
import android.os.Bundle;

import org.smartregister.chw.activity.AboveFiveChildProfileActivity;
import org.smartregister.chw.activity.AncMemberProfileActivity;
import org.smartregister.chw.activity.ChildProfileActivity;
import org.smartregister.chw.activity.FamilyPlanningMemberProfileActivity;
import org.smartregister.chw.activity.HivProfileActivity;
import org.smartregister.chw.activity.MalariaProfileActivity;
import org.smartregister.chw.activity.PncMemberProfileActivity;
import org.smartregister.chw.activity.TbProfileActivity;
import org.smartregister.chw.anc.activity.BaseAncMemberProfileActivity;
import org.smartregister.chw.core.activity.CoreAboveFiveChildProfileActivity;
import org.smartregister.chw.core.activity.CoreChildProfileActivity;
import org.smartregister.chw.core.task.CoreChwNotificationGoToMemberProfileTask;
import org.smartregister.chw.fp.dao.FpDao;
import org.smartregister.chw.hiv.dao.HivDao;
import org.smartregister.chw.malaria.activity.BaseMalariaProfileActivity;
import org.smartregister.chw.pnc.activity.BasePncMemberProfileActivity;
import org.smartregister.chw.tb.dao.TbDao;
import org.smartregister.commonregistry.CommonPersonObjectClient;

public class ChwGoToMemberProfileBasedOnRegisterTask extends CoreChwNotificationGoToMemberProfileTask {

    public ChwGoToMemberProfileBasedOnRegisterTask(CommonPersonObjectClient commonPersonObjectClient, Bundle bundle, String notificationType, Activity activity) {
        super(commonPersonObjectClient, bundle, notificationType, activity);
    }

    @Override
    protected void goToFpProfile(String baseEntityId, Activity activity) {
        FamilyPlanningMemberProfileActivity.startFpMemberProfileActivity(activity, FpDao.getMember(baseEntityId));
    }

    @Override
    protected void goToHivProfile(String baseEntityId, Activity activity) {
        HivProfileActivity.startHivProfileActivity(activity, HivDao.getMember(baseEntityId));
    }

    @Override
    protected void goToTbProfile(String baseEntityId, Activity activity) {
        TbProfileActivity.startTbProfileActivity(activity, TbDao.getMember(baseEntityId));
    }

    @Override
    protected Class<? extends CoreAboveFiveChildProfileActivity> getAboveFiveChildProfileActivityClass() {
        return AboveFiveChildProfileActivity.class;
    }

    @Override
    protected Class<? extends CoreChildProfileActivity> getChildProfileActivityClass() {
        return ChildProfileActivity.class;
    }

    @Override
    protected Class<? extends BaseAncMemberProfileActivity> getAncMemberProfileActivityClass() {
        return AncMemberProfileActivity.class;
    }

    @Override
    protected Class<? extends BasePncMemberProfileActivity> getPncMemberProfileActivityClass() {
        return PncMemberProfileActivity.class;
    }

    @Override
    protected Class<? extends BaseMalariaProfileActivity> getMalariaProfileActivityClass() {
        return MalariaProfileActivity.class;
    }
}
