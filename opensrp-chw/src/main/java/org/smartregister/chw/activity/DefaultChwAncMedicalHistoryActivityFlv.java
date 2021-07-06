package org.smartregister.chw.activity;

import android.content.Context;
import android.view.View;

import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.activity.DefaultAncMedicalHistoryActivityFlv;

import java.util.List;


class DefaultChwAncMedicalHistoryActivityFlv extends DefaultAncMedicalHistoryActivityFlv {
    @Override
    public void processViewData(List<Visit> visits, Context context) {
        super.processViewData(visits, context);
        if (!ChwApplication.getApplicationFlavor().hasDeliveryKit()) {
            linearLayoutDeliveryKit.setVisibility(View.GONE);
        }
    }
}
