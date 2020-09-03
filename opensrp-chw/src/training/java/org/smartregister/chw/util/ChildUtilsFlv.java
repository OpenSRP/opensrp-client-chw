package org.smartregister.chw.util;

import java.util.ArrayList;

public class ChildUtilsFlv implements ChildUtils.Flavor {
    @Override
    public ArrayList<String> mainColumns(String tableName, String familyTable, String familyMemberTable) {
        return new ArrayList<>();
    }

    @Override
    public String[] getOneYearVaccines() {
        return new String[]{"bcg", "opv1", "penta1", "pcv1", "rota1", "opv2", "penta2", "pcv2", "rota2", "opv3", "penta3", "pcv3", "ipv", "mcv1",
                "yellowfever", "rota3", "mena", "rubella1"
        };
    }

    @Override
    public String[] getTwoYearVaccines() {
        return new String[]{"bcg", "opv1", "penta1", "pcv1", "rota1", "opv2", "penta2", "pcv2", "rota2", "opv3", "penta3", "pcv3", "ipv", "mcv1",
                "yellowfever", "mcv2", "rota3", "mena", "rubella1", "rubella2"};
    }
}
