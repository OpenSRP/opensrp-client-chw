package org.smartregister.chw.dao;

import android.database.Cursor;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.dao.ChildDao;
import org.smartregister.chw.core.domain.Child;
import org.smartregister.dao.AbstractDao;
import org.smartregister.domain.Alert;
import org.smartregister.domain.AlertStatus;
import org.smartregister.immunization.domain.Vaccine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChwChildDao extends ChildDao {

    public static boolean hasDueVaccines(String baseEntityID) {
        String sql = "select count(*) count \n" +
                "FROM alerts  " +
                "where caseID = '" + baseEntityID + "'";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count");

        List<Integer> res = readData(sql, dataMap);
        if (res == null || res.get(0) == 0)
            return false;

        res.size();
        return true;
    }

    public static boolean hasDueAlerts(String baseEntityID) {
        String sql = "select count(*) count \n" +
                "FROM alerts  " +
                "where caseID = '" + baseEntityID + "'" +
                "and (status = 'upcoming' or status = 'normal' or status = 'urgent')";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count");

        List<Integer> res = readData(sql, dataMap);
        if (res == null || res.get(0) == 0)
            return false;

        res.size();
        return true;
    }

    public static boolean hasActiveSchedule(String baseEntityID) {
        String sql = "select count(*) count from schedule_service\n" +
                "where base_entity_id = '" + baseEntityID + "'" +
                " and completion_date is null ";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count");

        List<Integer> res = readData(sql, dataMap);
        if (res == null || res.get(0) == 0)
            return false;

        res.size();
        return true;
    }

    public static boolean hasDueTodayVaccines(String baseEntityID) {
        Map<String, List<Vaccine>> allVaccines = ReportDao.fetchAllVaccines();
        String dob = PersonDao.getDob(baseEntityID);
        int age = (int) Math.floor(Days.daysBetween(new DateTime(dob).toLocalDate(), new DateTime().toLocalDate()).getDays() / 365.4);

        List<Vaccine> myVaccines = allVaccines.get(baseEntityID);
        List<Alert> raw_alerts = ReportDao.computeChildAlerts(age, new DateTime(dob), baseEntityID, allVaccines.get(baseEntityID));

        Set<String> myGivenVaccines = new HashSet<>();
        if (myVaccines != null) {
            for (Vaccine vaccine : myVaccines) {
                myGivenVaccines.add(ReportDao.cleanName(vaccine.getName()));
            }
        }
        List<Alert> alerts = new ArrayList<>();
        for (Alert alert : raw_alerts) {
            if (alert.startDate() != null && alert.status() != AlertStatus.complete && !myGivenVaccines.contains(ReportDao.cleanName(alert.visitCode())))
                alerts.add(alert);
        }
        String[] dueVaccines = new String[alerts.size()];
        int x = 0;
        while (x < alerts.size()) {
            dueVaccines[x] = alerts.get(x).scheduleName();
            x++;
        }

        return dueVaccines.length != 0;
    }


    public static String getChildGender(String baseEntityID) {
        String sql = String.format("SELECT gender from ec_child\n" +
                "where base_entity_id = '%s'", baseEntityID);

        DataMap<String> dataMap = c -> getCursorValue(c, "gender");

        List<String> values = AbstractDao.readData(sql, dataMap);
        if (values == null || values.size() == 0)
            return "";

        return values.get(0) == null ? "" : values.get(0); // Return a default value of Low
    }

    private static String getChildrenUnderFiveAndGirlsAgeNineToElevenQuery(String baseEntityID) {
        return "select  c.base_entity_id , c.first_name , c.last_name , c.middle_name , c.mother_entity_id , c.relational_id , c.dob , c.date_created ,  lastVisit.last_visit_date , last_visit_not_done_date " +
                "from ec_child c " +
                "inner join ec_family_member m on c.base_entity_id = m.base_entity_id COLLATE NOCASE " +
                "inner join ec_family f on f.base_entity_id = m.relational_id COLLATE NOCASE  " +
                "left join ( " +
                " select base_entity_id , max(visit_date) last_visit_date " +
                " from visits " +
                " where visit_type in ('Child Home Visit') " +
                " group by base_entity_id " +
                ") lastVisit on lastVisit.base_entity_id = c.base_entity_id " +
                "left join ( " +
                " select base_entity_id , max(visit_date) last_visit_not_done_date " +
                " from visits " +
                " where visit_type in ('Visit not done') " +
                " group by base_entity_id " +
                ") lastVisitNotDone on lastVisitNotDone.base_entity_id = c.base_entity_id " +
                "where c.base_entity_id = '" + baseEntityID + "' " +
                "and  m.date_removed is null and m.is_closed = 0 " +
                "and (((julianday('now') - julianday(c.dob))/365.25) <= 5 or (c.gender = 'Female' and (((julianday('now') - julianday(c.dob))/365.25) BETWEEN 9 AND 11))) " +
                "and c.is_closed = 0 ";
    }

    public static String getChildQuery(String baseEntityID) {
        if (!ChwApplication.getApplicationFlavor().showChildrenUnderFiveAndGirlsAgeNineToEleven()) {
            return ChildDao.getChildQuery(baseEntityID);
        } else {
            return getChildrenUnderFiveAndGirlsAgeNineToElevenQuery(baseEntityID);
        }
    }

    public static Child getChild(String baseEntityID) {
        String sql = getChildQuery(baseEntityID);

        List<Child> values = AbstractDao.readData(sql, getChildDataMap());
        if (values == null || values.size() != 1)
            return null;

        return values.get(0);
    }


    public static String getChildFamilyName(String relationalId) {
        String sql = String.format("select DISTINCT f.first_name from ec_family f\n" +
                "INNER JOIN ec_family_member fm on fm.relational_id = f.base_entity_id\n" +
                "and fm.relational_id = '%s'", relationalId);

        DataMap<String> dataMap = c -> getCursorValue(c, "first_name");

        List<String> values = AbstractDao.readData(sql, dataMap);
        if (values == null || values.size() == 0)
            return "";

        return values.get(0) == null ? "" : values.get(0); // Return a default value of Low
    }

    public static Boolean isPNCChild(String baseEntityId) {
        String sql = "select 1 child_exists , (SELECT is_closed FROM ec_family_member WHERE base_entity_id = mother_entity_id ) mother_alive " +
                "from ec_child where base_entity_id = '" + baseEntityId + "' " +
                "and entry_point = 'PNC' and date (dob, '+28 days') >= date() ";

        final boolean[] childExists = {false};
        final boolean[] motherAlive = {true};
        DataMap<Void> dataMap = new DataMap<Void>() {
            @Override
            public Void readCursor(Cursor cursor) {
                childExists[0] = (getCursorIntValue(cursor, "child_exists", 0) == 1);
                motherAlive[0] = (getCursorIntValue(cursor, "mother_alive", 0) == 0);
                return null;
            }
        };

        AbstractDao.readData(sql, dataMap);
        return childExists[0] && motherAlive[0];
    }

    public static List<String> getRegisteredCertificateNumbers() {
        String sql = "SELECT birth_cert_num FROM ec_child e WHERE e.birth_cert_num NOTNULL GROUP BY e.birth_cert_num;";

        AbstractDao.DataMap<String> dataMap = c -> getCursorValue(c, "birth_cert_num");

        List<String> res = readData(sql, dataMap);
        if (res == null || res.size() < 1)
            return new ArrayList<>();

        return res;
    }
}
