package org.smartregister.chw.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.smartregister.chw.authenticator.OpenSRPAccountAuthenticator;

public class OpenSRPAccountTypeService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        OpenSRPAccountAuthenticator authenticator = new OpenSRPAccountAuthenticator(this);
        return authenticator.getIBinder();
    }
}