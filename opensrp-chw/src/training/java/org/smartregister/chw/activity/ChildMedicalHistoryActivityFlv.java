package org.smartregister.chw.activity;

import org.smartregister.chw.core.activity.DefaultChildMedicalHistoryActivityFlv;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.util.ChildUtilsFlv;

public class ChildMedicalHistoryActivityFlv extends DefaultChildMedicalHistoryActivityFlv {

    public CoreChildUtils.Flavor getChildUtils() {
        return new ChildUtilsFlv();
    }

}
