package org.smartregister.chw.contract;

import org.smartregister.chw.core.contract.CoreHivProfileContract;

public interface HivProfileContract extends CoreHivProfileContract {
    interface Presenter {
        void referToFacility();
    }
}
