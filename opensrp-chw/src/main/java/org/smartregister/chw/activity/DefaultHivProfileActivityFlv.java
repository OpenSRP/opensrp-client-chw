package org.smartregister.chw.activity;

import android.view.Menu;

import org.smartregister.chw.R;

public abstract class DefaultHivProfileActivityFlv implements HivProfileActivity.Flavor {

    @Override
    public void updateTbMenuItems(String baseEntityId, Menu menu) {
        menu.findItem(R.id.action_tb_registration).setVisible(false);
    }
}
