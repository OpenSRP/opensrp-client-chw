package org.smartregister.chw.application;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.SyncConfiguration;
import org.smartregister.SyncFilter;
import org.smartregister.chw.BuildConfig;
import org.smartregister.chw.activity.LoginActivity;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.view.activity.BaseLoginActivity;

import java.util.Arrays;
import java.util.List;

import static org.smartregister.util.Utils.isEmptyCollection;

/**
 * Created by samuelgithengi on 10/19/18.
 */
public class ChwSyncConfiguration extends SyncConfiguration {
    @Override
    public int getSyncMaxRetries() {
        return BuildConfig.MAX_SYNC_RETRIES;
    }

    @Override
    public SyncFilter getSyncFilterParam() {
        return SyncFilter.LOCATION;
    }

    @Override
    public String getSyncFilterValue() {
        String providerId = org.smartregister.Context.getInstance().allSharedPreferences().fetchRegisteredANM();
        String userLocationId = org.smartregister.Context.getInstance().allSharedPreferences().fetchUserLocalityId(providerId);
        List<String> locationIds = LocationHelper.getInstance().locationsFromHierarchy(true, null);
        if (!isEmptyCollection(locationIds)) {
            int index = locationIds.indexOf(userLocationId);
            List<String> subLocationIds = locationIds.subList(index, locationIds.size());
            return StringUtils.join(subLocationIds, ",");
        }
        return userLocationId;
    }

    @Override
    public int getUniqueIdSource() {
        return BuildConfig.OPENMRS_UNIQUE_ID_SOURCE;
    }

    @Override
    public int getUniqueIdBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_BATCH_SIZE;
    }

    @Override
    public int getUniqueIdInitialBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE;
    }

    @Override
    public boolean isSyncSettings() {
        return BuildConfig.IS_SYNC_SETTINGS;
    }

    @Override
    public SyncFilter getEncryptionParam() {
        return SyncFilter.TEAM_ID;
    }

    @Override
    public boolean updateClientDetailsTable() {
        return false;
    }

    @Override
    public boolean isSyncUsingPost() {
        return !BuildConfig.DEBUG && ChwApplication.getApplicationFlavor().syncUsingPost();
    }

    @Override
    public List<String> getSynchronizedLocationTags() {
        return Arrays.asList("MOH Jhpiego Facility Name", "Health Facility", "Facility");
    }

    @Override
    public SyncFilter getSettingsSyncFilterParam() {
        return SyncFilter.TEAM_ID;
    }

    @Override
    public boolean clearDataOnNewTeamLogin() {
        return false;
    }

    @Override
    public String getTopAllowedLocationLevel() {
        return "District";
    }

    @Override
    public String getOauthClientId() {
        return BuildConfig.OAUTH_CLIENT_ID;
    }

    @Override
    public String getOauthClientSecret() {
        return BuildConfig.OAUTH_CLIENT_SECRET;
    }

    @Override
    public Class<? extends BaseLoginActivity> getAuthenticationActivity() {
        return LoginActivity.class;
    }
}
