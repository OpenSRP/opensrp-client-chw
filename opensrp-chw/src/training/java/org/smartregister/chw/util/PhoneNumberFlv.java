package org.smartregister.chw.util;

import org.smartregister.chw.core.adapter.MemberAdapter;

public class PhoneNumberFlv implements MemberAdapter.Flavor {
    @Override
    public boolean isPhoneNumberLength16Digit() {
        return true;
    }
}
