package org.smartgresiter.wcaro.model;

import org.smartgresiter.wcaro.contract.FamilyChangeContract;
import org.smartgresiter.wcaro.domain.FamilyMember;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FamilyChangeContractModel implements FamilyChangeContract.Model {

    @Override
    public List<FamilyMember> getMembersExcluding(List<FamilyMember> clients, String primaryCareID, String headOfHouseID, String... ids) {
        List<FamilyMember> members = new ArrayList<>();
        List<String> listIDs = Arrays.asList(ids);

        for (FamilyMember client : clients) {

            if(client.getMemberID().equalsIgnoreCase(primaryCareID)){
                client.setPrimaryCareGiver(true);
            }

            if(client.getMemberID().equalsIgnoreCase(headOfHouseID)){
                client.setFamilyHead(true);
            }

            if (!listIDs.contains(client.getMemberID())) {
                members.add(client);
            }
        }

        return members;
    }

}

