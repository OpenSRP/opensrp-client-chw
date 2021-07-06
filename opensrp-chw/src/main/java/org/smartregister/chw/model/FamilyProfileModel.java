package org.smartregister.chw.model;

import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.family.FamilyLibrary;
import org.smartregister.family.domain.FamilyEventClient;
import org.smartregister.family.model.BaseFamilyProfileModel;

public class FamilyProfileModel extends BaseFamilyProfileModel {
    public FamilyProfileModel(String familyName) {
        super(familyName);
    }

    @Override
    public void updateWra(FamilyEventClient familyEventClient) {
        new FamilyProfileModelFlv().updateWra(familyEventClient);
    }

    public interface Flavor {
        void updateWra(FamilyEventClient familyEventClient);
    }

    public FamilyEventClient processFamilyRegistrationForm(String jsonString, String familyBaseEntityId) {
        return CoreJsonFormUtils.processFamilyUpdateForm(FamilyLibrary.getInstance().context().allSharedPreferences(), jsonString, familyBaseEntityId);
    }
}
