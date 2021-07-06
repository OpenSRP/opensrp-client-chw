package org.smartregister.chw.activity;

import android.view.Menu;

import org.smartregister.chw.R;

public abstract class DefaultTbProfileActivityFlv implements TbProfileActivity.Flavor {
    @Override
    public void updateHivMenuItems(String baseEntityId, Menu menu) {
        menu.findItem(R.id.action_hiv_registration).setVisible(false);
    }
}
