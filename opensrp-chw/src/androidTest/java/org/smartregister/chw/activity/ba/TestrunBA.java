package org.smartregister.chw.activity.ba;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses(
        {
                LoginPageActivityTestBa.class,
                SideNavigationMenuBA.class,
                AddFamilyTestBA.class,
                AddFamilyMemberBA.class,
                AdditionalTestDataBa.class,
                AddChildFamilyMemberBa.class,
                HomePageTestsBa.class,
                ANCRegistrationTestsBa.class,
                ANCRegisterTestsBa.class,
                RemoveMemberTestsBa.class,
                RemoveFamilyTestsBa.class
        })

public class TestrunBA {
}
