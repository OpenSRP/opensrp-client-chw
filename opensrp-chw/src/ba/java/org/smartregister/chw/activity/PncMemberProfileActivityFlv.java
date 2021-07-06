package org.smartregister.chw.activity;

import android.view.Menu;

import org.smartregister.chw.R;
import org.smartregister.chw.fp.dao.FpDao;
import org.smartregister.chw.util.UtilsFlv;

public class PncMemberProfileActivityFlv implements PncMemberProfileActivity.Flavor {

    @Override
    public Boolean onCreateOptionsMenu(Menu menu, String baseEntityId) {
        UtilsFlv.updateMalariaMenuItems(baseEntityId, menu);
        if (FpDao.isRegisteredForFp(baseEntityId)) {
            menu.findItem(R.id.action_fp_change).setVisible(true);
        } else {
            menu.findItem(R.id.action_fp_initiation_pnc).setVisible(true);
        }
        UtilsFlv.updateHivMenuItems(baseEntityId, menu);
        UtilsFlv.updateTbMenuItems(baseEntityId, menu);
        return true;
    }
}
