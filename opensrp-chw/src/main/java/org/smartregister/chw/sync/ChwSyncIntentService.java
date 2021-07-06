package org.smartregister.chw.sync;

import org.smartregister.sync.intent.SyncIntentService;

public class ChwSyncIntentService extends SyncIntentService {

    @Override
    public int getEventPullLimit() {
        return 1000;
    }
}
