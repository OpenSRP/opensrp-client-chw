package org.smartregister.chw.application;

import androidx.annotation.NonNull;

import org.smartregister.AllConstants;
import org.smartregister.CoreLibrary;
import org.smartregister.chw.core.service.CoreAuthorizationService;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.util.Constants;
import org.smartregister.domain.jsonmapping.Location;
import org.smartregister.domain.jsonmapping.util.TreeNode;
import org.smartregister.p2p.authorizer.P2PAuthorizationService;
import org.smartregister.repository.AllSharedPreferences;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class LmhAuthorizationService implements P2PAuthorizationService {

    private Map<String, Object> authorizationDetails = new HashMap<>();
    private CoreAuthorizationService coreAuthorizationService = new CoreAuthorizationService();

    @Override
    public void authorizeConnection(@NonNull final Map<String, Object> peerDeviceMap, @NonNull final AuthorizationCallback authorizationCallback) {
        getAuthorizationDetails(map -> {
                Object peerDeviceLocationId = peerDeviceMap.get(CoreConstants.PEER_TO_PEER.LOCATION_ID);
                Object myLocationId = authorizationDetails.get(CoreConstants.PEER_TO_PEER.LOCATION_ID);
                Object myPeerStatus = authorizationDetails.get(org.smartregister.p2p.util.Constants.AuthorizationKeys.PEER_STATUS);
                Object myCountryId = authorizationDetails.get(Constants.PeerToPeerUtil.COUNTRY_ID);

                if (peerDeviceLocationId instanceof String && myLocationId instanceof String && myPeerStatus instanceof String && myCountryId instanceof String) {

                    if (isKnownLocation((String) myCountryId)) {
                        authorizationCallback.onConnectionAuthorized();
                    } else {
                        rejectConnection(authorizationCallback);
                    }
                } else {
                    rejectConnection(authorizationCallback);
                }
        });
    }

    private LinkedHashMap<String, TreeNode<String, Location>> getLocationTreeMap() {
        return coreAuthorizationService.retrieveLocationHierarchyMap();
    }

    @NonNull
    private String getCountryId() {
        LinkedHashMap<String, TreeNode<String, Location>> locationHierarchyMap = getLocationTreeMap();
        if (locationHierarchyMap == null) {
            throw new IllegalStateException("Missing Location Hierarchy");
        }
        return Objects.requireNonNull(locationHierarchyMap.keySet().toArray())[0].toString();
    }

    private boolean isKnownLocation(@NonNull String countryId) {
        return countryId.equalsIgnoreCase(getCountryId());
    }

    private void rejectConnection(@NonNull AuthorizationCallback authorizationCallback) {
        authorizationCallback.onConnectionAuthorizationRejected("Incorrect authorization details provided");
    }

    @Override
    public void getAuthorizationDetails(@NonNull OnAuthorizationDetailsProvidedCallback onAuthorizationDetailsProvidedCallback) {
        // Load the preferences here
        AllSharedPreferences allSharedPreferences = CoreLibrary.getInstance().context().allSharedPreferences();
        authorizationDetails.put(AllConstants.PeerToPeer.KEY_TEAM_ID, allSharedPreferences.fetchDefaultTeamId(allSharedPreferences.fetchRegisteredANM()));
        authorizationDetails.put(CoreConstants.PEER_TO_PEER.LOCATION_ID, allSharedPreferences.fetchUserLocalityId(allSharedPreferences.fetchRegisteredANM()));
        authorizationDetails.put(Constants.PeerToPeerUtil.COUNTRY_ID, getCountryId());
        onAuthorizationDetailsProvidedCallback.onAuthorizationDetailsProvided(authorizationDetails);
    }
}