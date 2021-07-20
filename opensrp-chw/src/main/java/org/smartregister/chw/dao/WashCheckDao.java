package org.smartregister.chw.dao;

import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.dao.AbstractDao;
import org.smartregister.domain.Event;
import org.smartregister.domain.db.EventClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mvel2.DataConversion.convert;

public class WashCheckDao extends AbstractDao {

    public static long getLastWashCheckDate(String familyBaseEntityID) {
        String sql = "select CASE WHEN created_at <= visit_date THEN created_at ELSE visit_date END wash_check_date from visits where visit_type = 'WASH check' and " +
                "base_entity_id = '" + familyBaseEntityID + "' order by created_at desc limit 1";

        DataMap<Long> dataMap = c -> getCursorLongValue(c, "wash_check_date");

        List<Long> res = AbstractDao.readData(sql, dataMap);

        return res == null || res.isEmpty() ? 0 : res.get(0);
    }

    public static List<String> getAllWashCheckVisits(SQLiteDatabase db) {
        String sql = "select visit_id from visits where visit_type = 'WASH check'";

        DataMap<String> dataMap = c -> getCursorValue(c, "visit_id");
        List<String> res = AbstractDao.readData(sql, dataMap, db);
        if (res == null || res.size() == 0)
            return new ArrayList<>();

        return res;
    }

    public static void updateWashCheckVisitDetails(Long washDate, String baseEntityID,
                                                   String handWashingFacilities, String drinkingWater, String hygienicLatrine) {
        String sql = "  UPDATE visit_details SET human_readable_details =  CASE " +
                " WHEN visit_details.visit_key = 'handwashing_facilities'  THEN  '" + handWashingFacilities + "' " +
                " WHEN visit_details.visit_key = 'drinking_water'   THEN  '" + drinkingWater + "' " +
                " WHEN visit_details.visit_key = 'hygienic_latrine'   THEN  '" + hygienicLatrine + "' " +
                " END , " +
                " processed = '" + '0' + "' " +
                " WHERE EXISTS (SELECT visits.visit_date, visits.base_entity_id FROM visits WHERE visit_details.visit_id = visits.visit_id  " +
                " AND visits.base_entity_id = '" + baseEntityID + "' " +
                " AND visits.visit_date = '" + washDate + "' " +
                " AND visits.visit_type = 'WASH check') ";
        updateDB(sql);
    }

    public static void updateWashCheckVisits(Long washDate, String baseEntityID,
                                             String visitJson) {
        String sql = "  UPDATE visits SET visit_json =  '" + visitJson + "' ," +
                " processed = '" + '0' + "' " +
                " WHERE EXISTS (SELECT visits.visit_date, visits.base_entity_id FROM visit_details WHERE visit_details.visit_id = visits.visit_id  " +
                " AND visits.base_entity_id = '" + baseEntityID + "' " +
                " AND visits.visit_date = '" + washDate + "' " +
                " AND visits.visit_type = 'WASH check') ";
        updateDB(sql);
    }

    public static Map<String, VisitDetail> getWashCheckDetails(Long washDate, String baseEntityID) {
        String sql = "select vd.processed,  vd.updated_at,  vd.created_at,v.visit_json, v.visit_date,  vd.visit_key , vd.parent_code , vd.preprocessed_type , vd.details, vd.human_readable_details , vd.visit_id , v.base_entity_id from visits v " +
                "inner join visit_details vd on vd.visit_id = v.visit_id and v.base_entity_id = '" + baseEntityID + "' " +
                "where v.visit_date = " + washDate + " and v.visit_type = 'WASH check'";

        Map<String, VisitDetail> map = new HashMap<>();

        DataMap<VisitDetail> dataMap = c -> {
            VisitDetail detail = new VisitDetail();
            detail.setVisitId(getCursorValue(c, "visit_id"));
            detail.setBaseEntityId(getCursorValue(c, "base_entity_id"));
            detail.setVisitKey(getCursorValue(c, "visit_key"));
            detail.setParentCode(getCursorValue(c, "parent_code"));
            detail.setPreProcessedType(getCursorValue(c, "preprocessed_type"));
            detail.setDetails(getCursorValue(c, "details"));
            detail.setHumanReadable(getCursorValue(c, "human_readable_details"));
            detail.setJsonDetails(getCursorValue(c, "visit_json"));
            detail.setCreatedAt(new Date(Long.parseLong(getCursorValue(c, "created_at"))));
            detail.setUpdatedAt(new Date(Long.parseLong(getCursorValue(c, "updated_at"))));
            detail.setProcessed(getCursorValue(c, "processed").equals("1"));

            map.put(detail.getVisitKey(), detail);

            return detail;
        };

        List<VisitDetail> res = AbstractDao.readData(sql, dataMap);
        if (res == null || res.size() == 0)
            return new HashMap<>();

        return map;
    }

    public static List<EventClient> getWashCheckEvents(SQLiteDatabase db) {
        String sql = "select json from event where eventType = 'WASH check' order by eventDate asc";

        DataMap<EventClient> dataMap = c -> processEventClientCursor(getCursorValue(c, "json"));
        List<EventClient> res = AbstractDao.readData(sql, dataMap, db);
        if (res == null || res.size() == 0)
            return new ArrayList<>();

        return res;
    }

    private static EventClient processEventClientCursor(String jsonEventStr) {
        if (StringUtils.isBlank(jsonEventStr)
                || "{}".equals(jsonEventStr)) { // Skip blank/empty json string
            return null;
        }
        String eventJson = jsonEventStr.replaceAll("'", "");
        Event event = convert(eventJson, Event.class);

        return new EventClient(event, null);
    }

}
