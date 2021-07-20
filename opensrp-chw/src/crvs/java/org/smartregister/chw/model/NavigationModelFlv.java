package org.smartregister.chw.model;

import org.smartregister.chw.R;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.model.NavigationOption;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.CrvsConstants;

import java.util.ArrayList;
import java.util.List;

import static org.smartregister.chw.util.CrvsConstants.USER_TYPE;

public class NavigationModelFlv extends DefaultNavigationModelFlv {

    String role = "";

    public NavigationModelFlv(String role) {
        this.role = role;
    }

    public NavigationModelFlv() {
    }

    private List<NavigationOption> navigationOptions = new ArrayList<>();

    @Override
    public List<NavigationOption> getNavigationItems() {

        if (navigationOptions.size() == 0) {
//            if (ChwApplication.getInstance().getContext().allSharedPreferences().getPreference(USER_TYPE).equals(CrvsConstants.USER)) {
            /*if (ChwApplication.getInstance().getContext().allSharedPreferences().getPreference(USER_TYPE).equals(CrvsConstants.USER)) {
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, R.string.menu_all_families, Constants.DrawerMenu.ALL_FAMILIES, 0));
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_anc, R.mipmap.sidemenu_anc_active, R.string.menu_anc, Constants.DrawerMenu.ANC, 0));
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_pnc, R.mipmap.sidemenu_pnc_active, R.string.menu_pnc, Constants.DrawerMenu.PNC, 0));
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_children, R.mipmap.sidemenu_children_active, R.string.menu_child_clients, Constants.DrawerMenu.CHILD_CLIENTS, 0));
//            }else if (role.equals(CrvsConstants.SURVEYOR)){
            }else if (role.equals(CrvsConstants.SURVEYOR)){
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_children, R.mipmap.sidemenu_children_active, R.string.birth_certification, Constants.DrawerMenu.BIRTH_NOTIFICATION, 0));
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_children, R.mipmap.sidemenu_children_active, R.string.death_certification, Constants.DrawerMenu.DEATH_NOTIFICATION, 0));
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_children, R.mipmap.sidemenu_children_active, R.string.out_of_area_registration, Constants.DrawerMenu.OUT_OF_AREA_CHILD, 0));
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_children, R.mipmap.sidemenu_children_active, R.string.out_of_area_death, Constants.DrawerMenu.OUT_OF_AREA_DEATH, 0));
            }else{*/
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_families, R.mipmap.sidemenu_families_active, R.string.menu_all_families, Constants.DrawerMenu.ALL_FAMILIES, 0));
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_anc, R.mipmap.sidemenu_anc_active, R.string.menu_anc, Constants.DrawerMenu.ANC, 0));
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_pnc, R.mipmap.sidemenu_pnc_active, R.string.menu_pnc, Constants.DrawerMenu.PNC, 0));
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_children, R.mipmap.sidemenu_children_active, R.string.menu_child_clients, Constants.DrawerMenu.CHILD_CLIENTS, 0));
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_children, R.mipmap.sidemenu_children_active, R.string.birth_certification, Constants.DrawerMenu.BIRTH_NOTIFICATION, 0));
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_children, R.mipmap.sidemenu_children_active, R.string.death_certification, Constants.DrawerMenu.DEATH_NOTIFICATION, 0));
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_children, R.mipmap.sidemenu_children_active, R.string.out_of_area_registration, Constants.DrawerMenu.OUT_OF_AREA_CHILD, 0));
                navigationOptions.add(new NavigationOption(R.mipmap.sidemenu_children, R.mipmap.sidemenu_children_active, R.string.out_of_area_death, Constants.DrawerMenu.OUT_OF_AREA_DEATH, 0));
//            }
        }

        return navigationOptions;
    }
}