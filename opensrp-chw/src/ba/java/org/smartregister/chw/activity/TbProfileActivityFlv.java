package org.smartregister.chw.activity;

import android.view.Menu;

import org.smartregister.chw.util.UtilsFlv;

public class TbProfileActivityFlv extends DefaultTbProfileActivityFlv {
    @Override
    public void updateHivMenuItems(String baseEntityId, Menu menu) {
        UtilsFlv.updateHivMenuItems(baseEntityId, menu);
    }
}
