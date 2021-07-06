package org.smartregister.chw.dao;

import org.jetbrains.annotations.Nullable;
import org.smartregister.dao.AbstractDao;

import java.util.List;

public class ScheduleDao extends AbstractDao {

    //TODO
    public static @Nullable List<String> getActiveANCWomen(String scheduleName, String scheduleGroup) {
        String sql = "select base_entity_id from ec_anc_register where is_closed = 0 and base_entity_id not in " +
                "(select base_entity_id from schedule_service where schedule_name = '" + scheduleName + "' and schedule_group_name = '" + scheduleGroup + "')";

        DataMap<String> dataMap = c -> getCursorValue(c, "base_entity_id");
        return AbstractDao.readData(sql, dataMap);
    }

    public static @Nullable List<String> getActivePNCWomen(String scheduleName, String scheduleGroup) {
        String sql = "select base_entity_id from ec_pregnancy_outcome where is_closed = 0 and base_entity_id not in " +
                "(select base_entity_id from schedule_service where schedule_name = '" + scheduleName + "' and schedule_group_name = '" + scheduleGroup + "')";

        DataMap<String> dataMap = c -> getCursorValue(c, "base_entity_id");
        return AbstractDao.readData(sql, dataMap);
    }

    public static @Nullable List<String> getActiveChildrenUnder5AndGirlsAge9to11(String scheduleName, String scheduleGroup) {
        String sql = "select ec_child.base_entity_id " +
                "from ec_child " +
                "left join ec_family_member on ec_family_member.base_entity_id = ec_child.mother_entity_id " +
                "and (ec_family_member.is_closed = 1 or ec_family_member.base_entity_id is null ) " +
                "where ec_child.is_closed = 0 and ec_child.base_entity_id not in " +
                "(select base_entity_id from schedule_service where" +
                " schedule_name = '" + scheduleName + "' and schedule_group_name = '" + scheduleGroup + "') " +
                "and (((julianday('now') - julianday(ec_child.dob))/365.25) <= 5 or (ec_child.gender = 'Female'" +
                " and (((julianday('now') - julianday(ec_child.dob))/365.25) BETWEEN 9 AND 11))) ";

        DataMap<String> dataMap = c -> getCursorValue(c, "base_entity_id");
        return AbstractDao.readData(sql, dataMap);
    }


    public static @Nullable List<String> getActiveChildren(String scheduleName, String scheduleGroup) {
        String sql = "select ec_child.base_entity_id from ec_child " +
                "left join ec_family_member on ec_family_member.base_entity_id = ec_child.mother_entity_id and (ec_family_member.is_closed = 1 or ec_family_member.base_entity_id is null ) " +
                "where ec_child.is_closed = 0 and ec_child.base_entity_id not in " +
                "(select base_entity_id from schedule_service where schedule_name = '" + scheduleName + "' and schedule_group_name = '" + scheduleGroup + "')";

        DataMap<String> dataMap = c -> getCursorValue(c, "base_entity_id");
        return AbstractDao.readData(sql, dataMap);
    }

    public static void deleteChildrenVaccines(){
        getRepository().getWritableDatabase().execSQL("delete from alerts where caseID in (select base_entity_id from ec_child)");
    }

    public static @Nullable List<String> getActiveFamilies(String scheduleName, String scheduleGroup) {
        String sql = "select base_entity_id from ec_family where is_closed = 0 and base_entity_id not in " +
                "(select base_entity_id from schedule_service where schedule_name = '" + scheduleName + "' and schedule_group_name = '" + scheduleGroup + "')";

        DataMap<String> dataMap = c -> getCursorValue(c, "base_entity_id");
        return AbstractDao.readData(sql, dataMap);
    }

    public static @Nullable List<String> getActiveFPWomen(String scheduleName, String scheduleGroup) {
        String sql = "select base_entity_id from ec_family_planning where is_closed = 0 and base_entity_id not in " +
                "(select base_entity_id from schedule_service where schedule_name = '" + scheduleName + "' and schedule_group_name = '" + scheduleGroup + "')";

        DataMap<String> dataMap = c -> getCursorValue(c, "base_entity_id");
        return AbstractDao.readData(sql, dataMap);
    }

    public static @Nullable List<String> getActiveHivClients(String scheduleName, String scheduleGroup) {
        String sql = "select base_entity_id from ec_hiv_register where is_closed = 0 and base_entity_id not in " +
                "(select base_entity_id from schedule_service where schedule_name = '" + scheduleName + "' and schedule_group_name = '" + scheduleGroup + "')";

        DataMap<String> dataMap = c -> getCursorValue(c, "base_entity_id");
        return AbstractDao.readData(sql, dataMap);
    }

    public static @Nullable List<String> getActiveTbClients(String scheduleName, String scheduleGroup) {
        String sql = "select base_entity_id from ec_tb_register where is_closed = 0 and tb_case_closure_date is null and base_entity_id not in " +
                "(select base_entity_id from schedule_service where schedule_name = '" + scheduleName + "' and schedule_group_name = '" + scheduleGroup + "')";

        DataMap<String> dataMap = c -> getCursorValue(c, "base_entity_id");
        return AbstractDao.readData(sql, dataMap);
    }
}
