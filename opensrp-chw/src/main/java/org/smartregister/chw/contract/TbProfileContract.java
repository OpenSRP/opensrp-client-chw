package org.smartregister.chw.contract;

import org.smartregister.chw.core.contract.CoreTbProfileContract;

public interface TbProfileContract extends CoreTbProfileContract {
    interface Presenter {
        void referToFacility();
    }
}
