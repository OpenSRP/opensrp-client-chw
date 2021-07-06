package org.smartregister.chw.custom_view;

import android.app.Activity;
import android.content.Intent;

import org.apache.commons.lang3.tuple.Pair;
import org.smartregister.chw.core.custom_views.NavigationMenu;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public abstract class DefaultNavigationMenuFlv implements NavigationMenu.Flavour {
    @Override
    public List<Pair<String, Locale>> getSupportedLanguages() {
        return Arrays.asList(Pair.of("English", Locale.ENGLISH), Pair.of("Français", Locale.FRENCH));
    }

    @Override
    public HashMap<String, String> getTableMapValues() {
        return new HashMap<>();
    }

    @Override
    public boolean hasStockReport() {
        return false;
    }

    @Override
    public boolean hasServiceReport() {
        return false;
    }

    @Override
    public boolean hasCommunityResponders() {
        return false;
    }

    @Override
    public Intent getStockReportIntent(Activity activity) {
        return null;
    }

    @Override
    public Intent getServiceReportIntent(Activity activity) {
        return null;
    }

    @Override
    public String childNavigationMenuCountString() {
        return null;
    }

    @Override
    public Intent getHIA2ReportActivityIntent(Activity activity) {
        return null;
    }

}
