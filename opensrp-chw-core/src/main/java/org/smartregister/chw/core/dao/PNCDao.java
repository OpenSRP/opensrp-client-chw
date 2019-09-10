package org.smartregister.chw.core.dao;

import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;

public class PNCDao extends AlertDao {

    @Nullable
    public static Date getPNCDeliveryDate(String baseEntityID) {
        String sql = "select delivery_date from ec_pregnancy_outcome where base_entity_id = '" + baseEntityID + "'";

        AbstractDao.DataMap<Date> dataMap = cursor -> getCursorValueAsDate(cursor, "delivery_date", getNativeFormsDateFormat());

        List<Date> res = readData(sql, dataMap);
        if (res == null || res.size() != 1)
            return null;

        return res.get(0);
    }
}
