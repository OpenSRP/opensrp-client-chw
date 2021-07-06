package org.smartregister.chw.activity;

import android.view.Menu;

import org.smartregister.chw.util.UtilsFlv;

public class HivProfileActivityFlv extends DefaultHivProfileActivityFlv {
    @Override
    public void updateTbMenuItems(String baseEntityId, Menu menu) {
        UtilsFlv.updateTbMenuItems(baseEntityId, menu);
    }
}
