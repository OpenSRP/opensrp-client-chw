package org.smartregister.chw.model;

import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.util.NCUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.repository.AllSharedPreferences;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class WashCheckModel {
    private String familyId;

    public WashCheckModel(String familyId) {
        this.familyId = familyId;
    }

    public boolean saveWashCheckEvent(String jsonString) {
        try {
            AllSharedPreferences allSharedPreferences = AncLibrary.getInstance().context().allSharedPreferences();
            Event baseEvent = org.smartregister.chw.anc.util.JsonFormUtils.processJsonForm(allSharedPreferences, jsonString, CoreConstants.TABLE_NAME.WASH_CHECK_LOG);
            baseEvent.setBaseEntityId(familyId);

            Visit visit = NCUtils.eventToVisit(baseEvent);
            AncLibrary.getInstance().visitRepository().addVisit(visit);
            for (Map.Entry<String, List<VisitDetail>> entry : visit.getVisitDetails().entrySet()) {
                if (entry.getValue() != null) {
                    for (VisitDetail d : entry.getValue()) {
                        AncLibrary.getInstance().visitDetailsRepository().addVisitDetails(d);
                    }
                }
            }
            /*NCUtils.addEvent(allSharedPreferences, baseEvent);
            NCUtils.startClientProcessing();*/
            return true;
        } catch (Exception e) {
            Timber.e(e);
        }
        return false;
    }

}
