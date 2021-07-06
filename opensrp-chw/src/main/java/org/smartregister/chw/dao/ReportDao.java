package org.smartregister.chw.dao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.utils.VaccineScheduleUtil;
import org.smartregister.chw.core.utils.VisitVaccineUtil;
import org.smartregister.chw.domain.EligibleChild;
import org.smartregister.chw.domain.VillageDose;
import org.smartregister.chw.util.ReportingConstants;
import org.smartregister.dao.AbstractDao;
import org.smartregister.domain.Alert;
import org.smartregister.domain.AlertStatus;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.jsonmapping.VaccineGroup;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.repository.EventClientRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import timber.log.Timber;

import static org.smartregister.chw.core.utils.VisitVaccineUtil.getInMemoryAlerts;

/**
 * @author rkodev
 */
public class ReportDao extends AbstractDao {

    @NonNull
    public static Map<String, String> extractRecordedLocations() {
        Map<String, String> locations = new HashMap<>();

        String sql = "SELECT DISTINCT location_id, provider_id FROM ec_family_member_location";
        DataMap<Void> dataMap = cursor -> {
            locations.put(getCursorValue(cursor, "location_id"), getCursorValue(cursor, "provider_id"));
            return null;
        };

        readData(sql, dataMap);
        return locations;
    }

    public static Map<String, List<Vaccine>> fetchAllVaccines() {
        Map<String, List<Vaccine>> result = new HashMap<>();

        String sql = "select * from vaccines";
        DataMap<Void> dataMap = cursor -> {
            String vaccineName = cursor.getString(cursor.getColumnIndex(VaccineRepository.NAME));
            if (vaccineName != null) {
                vaccineName = VaccineRepository.removeHyphen(vaccineName);
            }

            Date createdAt = null;
            String dateCreatedString = cursor.getString(cursor.getColumnIndex(VaccineRepository.CREATED_AT));
            if (StringUtils.isNotBlank(dateCreatedString)) {
                try {
                    createdAt = EventClientRepository.dateFormat.parse(dateCreatedString);
                } catch (ParseException e) {
                    Timber.e(e);
                }
            }
            Vaccine vaccine = new Vaccine(cursor.getLong(cursor.getColumnIndex(VaccineRepository.ID_COLUMN)),
                    cursor.getString(cursor.getColumnIndex(VaccineRepository.BASE_ENTITY_ID)),
                    cursor.getString(cursor.getColumnIndex(VaccineRepository.PROGRAM_CLIENT_ID)),
                    vaccineName,
                    cursor.getInt(cursor.getColumnIndex(VaccineRepository.CALCULATION)),
                    new Date(cursor.getLong(cursor.getColumnIndex(VaccineRepository.DATE))),
                    cursor.getString(cursor.getColumnIndex(VaccineRepository.ANMID)),
                    cursor.getString(cursor.getColumnIndex(VaccineRepository.LOCATION_ID)),
                    cursor.getString(cursor.getColumnIndex(VaccineRepository.SYNC_STATUS)),
                    cursor.getString(cursor.getColumnIndex(VaccineRepository.HIA2_STATUS)),
                    cursor.getLong(cursor.getColumnIndex(VaccineRepository.UPDATED_AT_COLUMN)),
                    cursor.getString(cursor.getColumnIndex(VaccineRepository.EVENT_ID)),
                    cursor.getString(cursor.getColumnIndex(VaccineRepository.FORMSUBMISSION_ID)),
                    cursor.getInt(cursor.getColumnIndex(VaccineRepository.OUT_OF_AREA)),
                    createdAt,
                    cursor.getInt(cursor.getColumnIndex(VaccineRepository.IS_VOIDED))
            );

            vaccine.setTeam(cursor.getString(cursor.getColumnIndex(VaccineRepository.TEAM)));
            vaccine.setTeamId(cursor.getString(cursor.getColumnIndex(VaccineRepository.TEAM_ID)));
            vaccine.setChildLocationId(cursor.getString(cursor.getColumnIndex(VaccineRepository.CHILD_LOCATION_ID)));

            List<Vaccine> vaccines = result.get(vaccine.getBaseEntityId());
            if (vaccines == null) vaccines = new ArrayList<>();
            vaccines.add(vaccine);
            result.put(vaccine.getBaseEntityId(), vaccines);
            return null;
        };

        readData(sql, dataMap);

        return result;
    }

    protected static String cleanName(String name) {
        return name.toLowerCase().replace("_", "").replace(" ", "");
    }

    public static List<EligibleChild> fetchLiveEligibleChildrenReport(@Nullable List<String> communityIds, Date dueDate) {
        // fetch all children in the region
        String _communityIds = "('" + StringUtils.join(communityIds, "','") + "')";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        int days = Days.daysBetween(new DateTime().toLocalDate(), new DateTime(dueDate).toLocalDate()).getDays();
        String sql = "select c.base_entity_id , c.unique_id , c.first_name , c.last_name , c.middle_name ," +
                "f.first_name family_name  , c.dob , c.gender , l.location_id " +
                "from ec_child c " +
                "left join ec_family f on c.relational_id = f.base_entity_id and f.is_closed = 0 and f.date_removed is null COLLATE NOCASE " +
                "inner join ec_family_member_location l on l.base_entity_id = c.base_entity_id COLLATE NOCASE " +
                "inner join ec_family_member m on m.base_entity_id = c.base_entity_id and m.is_closed = 0 and m.date_removed is null COLLATE NOCASE  " +
                "where c.date_removed is null and c.is_closed = 0 and m.is_closed = 0 ";
        if (communityIds != null && !communityIds.isEmpty())
            sql += " and ( l.location_id IN " + _communityIds + " or '" + communityIds.get(0) + "' = '') ";
        sql += "order by c.first_name , c.last_name , c.middle_name ";
        Map<String, List<Vaccine>> allVaccines = fetchAllVaccines();
        List<EligibleChild> eligibleChildren = new ArrayList<>();
        DataMap<Void> dataMap = c -> {
            // compute constants
            String baseEntityId = getCursorValue(c, "base_entity_id");
            String gender = getCursorValue(c, "gender");
            Date dob = getCursorValueAsDate(c, "dob", sdf);
            Date adjustedDob = new DateTime(dob).minusDays(days).toDate();
            String name = getCursorValue(c, "first_name", "") + " " + getCursorValue(c, "middle_name", "");
            name = name.trim() + " " + getCursorValue(c, "last_name", "");
            int age = (int) Math.floor(Days.daysBetween(new DateTime(dob).toLocalDate(), new DateTime(dueDate).toLocalDate()).getDays() / 365.4);
            if (age < 2 || (age >= 9 && age <= 11 && "Female".equalsIgnoreCase(gender))) {
                List<Vaccine> rawVaccines = allVaccines.get(baseEntityId);
                List<Vaccine> myVaccines = new ArrayList<>();
                if (rawVaccines != null) {
                    for (Vaccine vaccine : rawVaccines) {
                        vaccine.setDate(new DateTime(vaccine.getDate()).minusDays(days).toDate());
                        myVaccines.add(vaccine);
                    }
                }

                List<Alert> raw_alerts = computeChildAlerts(age, new DateTime(dob).minusDays(days), baseEntityId, myVaccines);
                Set<String> myGivenVaccines = new HashSet<>();
                if (myVaccines != null) {
                    for (Vaccine vaccine : myVaccines) {
                        myGivenVaccines.add(cleanName(vaccine.getName()));
                    }
                }
                List<Alert> alerts = new ArrayList<>();
                for (Alert alert : raw_alerts) {
                    if (alert.startDate() != null && alert.status() != AlertStatus.complete && !myGivenVaccines.contains(cleanName(alert.visitCode())))
                        alerts.add(alert);
                }
                String[] dueVaccines = new String[alerts.size()];
                int x = 0;
                while (x < alerts.size()) {
                    dueVaccines[x] = alerts.get(x).scheduleName();
                    x++;
                }
                // create return object
                EligibleChild child = new EligibleChild();
                child.setID(baseEntityId);
                child.setDateOfBirth(adjustedDob);
                child.setFullName(name.trim());
                child.setFamilyName(getCursorValue(c, "family_name") + " Family");
                child.setDueVaccines(dueVaccines);
                child.setAlerts(alerts);
                if (dueVaccines.length > 0) {
                    eligibleChildren.add(child);
                }
            }
            return null;
        };
        readData(sql, dataMap);
        return eligibleChildren;
    }

    protected static List<Alert> computeChildAlerts(int age, DateTime anchorDate, String baseEntityId, @Nullable List<Vaccine> issuedVaccines) {
        try {
            String category = age < 2 ? "child" : "child_over_5";
            HashMap<String, HashMap<String, VaccineSchedule>> vaccineSchedules = getVaccineSchedules(category);
            return getInMemoryAlerts(vaccineSchedules, baseEntityId, anchorDate, category, issuedVaccines == null ? new ArrayList<>() : issuedVaccines);
        } catch (Exception e) {
            Timber.e(e);
        }
        return new ArrayList<>();
    }

    private static HashMap<String, HashMap<String, VaccineSchedule>> getVaccineSchedules(String category) {
        String fileName = category.equalsIgnoreCase("child") ? "vaccines.json" : "vaccines/child_over_5_vaccines.json";

        List<VaccineGroup> vaccineGroups =
                VaccineScheduleUtil.getVaccineGroups(CoreChwApplication.getInstance().getApplicationContext(), fileName);

        List<org.smartregister.immunization.domain.jsonmapping.Vaccine> specialVaccines =
                VaccinatorUtils.getSpecialVaccines(CoreChwApplication.getInstance().getApplicationContext());

        return VisitVaccineUtil.getSchedule(vaccineGroups, specialVaccines, category);
    }


    @NonNull
    public static List<EligibleChild> eligibleChildrenReport(ArrayList<String> communityIds, Date dueDate) {
        String _communityIds = "('" + StringUtils.join(communityIds, "','") + "')";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String paramDate = sdf.format(dueDate);

        String sql = "select c.base_entity_id , c.unique_id , c.first_name , c.last_name , c.middle_name ," +
                "f.first_name family_name  , c.dob , " +
                "(select group_concat(scheduleName, ', ') from alerts where caseID = c.base_entity_id and status not in ('expired','complete') and startDate <= '" + paramDate + "' and expiryDate >= '" + paramDate + "' order by startDate ) alerts " +
                "from ec_child c " +
                "left join ec_family f on c.relational_id = f.base_entity_id " +
                "inner join ec_family_member_location l on l.base_entity_id = c.base_entity_id " +
                "where ( l.location_id IN " + _communityIds + " or '" + communityIds.get(0) + "' = '') " +
                " and (((julianday('now') - julianday(c.dob))/365.25) < 2 or (c.gender = 'Female' and (((julianday('now') - julianday(c.dob))/365.25) BETWEEN 9 AND 11))) " +
                "and l.base_entity_id in (select caseID from alerts where status not in ('expired','complete') and startDate <= '" + paramDate + "' and expiryDate >= '" + paramDate + "') " +
                "order by c.first_name , c.last_name , c.middle_name ";


        DataMap<EligibleChild> dataMap = c -> {
            EligibleChild child = new EligibleChild();
            child.setID(getCursorValue(c, "base_entity_id"));
            child.setDateOfBirth(getCursorValueAsDate(c, "dob", sdf));

            String name = getCursorValue(c, "first_name", "") + " " + getCursorValue(c, "middle_name", "");
            name = name.trim() + " " + getCursorValue(c, "last_name", "");

            child.setFullName(name.trim());
            child.setFamilyName(getCursorValue(c, "family_name") + " Family");

            String vaccines = getCursorValue(c, "alerts", "");
            child.setDueVaccines(StringUtils.isBlank(vaccines) ? new String[]{} : vaccines.trim().split(","));

            return child;
        };

        List<EligibleChild> res = readData(sql, dataMap);

        if (res == null || res.size() == 0)
            return new ArrayList<>();

        return res;
    }

    @NonNull
    public static List<VillageDose> villageDosesReportSummary(String villageName, Date dueDate) {


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String paramDate = sdf.format(dueDate);

        String sql = "select count(*) cnt , scheduleName " +
                "from ec_child c  " +
                "inner join ec_family_member_location l on l.base_entity_id = c.base_entity_id " +
                "inner join alerts al on caseID = c.base_entity_id " +
                "where status <> 'expired' and startDate <= '" + paramDate + "' " +
                "and (((julianday('now') - julianday(c.dob))/365.25) < 2 or (c.gender = 'Female' and (((julianday('now') - julianday(c.dob))/365.25) BETWEEN 9 AND 11))) " +
                "group by scheduleName " +
                "order by scheduleName";

        Map<String, Integer> map = new TreeMap<>();

        DataMap<Void> dataMap = c -> {
            String scheduleName = getCursorValue(c, "scheduleName", "").replaceAll("\\d", "").trim();
            //String scheduleName = getCursorValue(c, "scheduleName", "");

            Integer count = getCursorIntValue(c, "cnt", 0);
            Integer total = map.get(scheduleName);
            total = ((total == null) ? 0 : total) + count;

            map.put(scheduleName, total);
            return null;
        };
        readData(sql, dataMap);

        VillageDose villageDose = new VillageDose();
        villageDose.setVillageName(villageName);
        villageDose.setID("");
        villageDose.setRecurringServices(map);

        List<VillageDose> res = new ArrayList<>();
        res.add(villageDose);

        return res;
    }

    @NonNull
    public static List<VillageDose> fetchLiveVillageDosesReport(List<String> communityIds, Date dueDate, boolean includeAll, String villageName, Map<String, String> locationMap) {
        List<EligibleChild> children = fetchLiveEligibleChildrenReport(communityIds, dueDate);

        Map<String, Integer> allLocation = new TreeMap<>();

        Map<String, TreeMap<String, Integer>> resultMap = new HashMap<>();
        for (EligibleChild child : children) {
            if (child.getAlerts() == null) continue;

            for (Alert alert : child.getAlerts()) {
                TreeMap<String, Integer> vaccineMaps = resultMap.get(child.getLocationId());
                if (vaccineMaps == null) vaccineMaps = new TreeMap<>();
                String scheduleName = alert.scheduleName().replaceAll("\\d", "").trim();

                Integer count = vaccineMaps.get(scheduleName);
                count = count == null ? 1 : count + 1;
                vaccineMaps.put(scheduleName, count);

                resultMap.put(child.getLocationId(), vaccineMaps);

                // count defaults
                if (includeAll) {
                    Integer allCount = allLocation.get(alert.scheduleName());
                    allCount = allCount == null ? 1 : allCount + 1;
                    allLocation.put(alert.scheduleName(), allCount);
                }
            }
        }

        List<VillageDose> result = new ArrayList<>();
        if (includeAll) {
            VillageDose villageDose = new VillageDose();
            villageDose.setVillageName(villageName);
            villageDose.setID("");
            villageDose.setRecurringServices(allLocation);

            result.add(villageDose);
        }

        for (Map.Entry<String, TreeMap<String, Integer>> entry : resultMap.entrySet()) {
            VillageDose villageDose = new VillageDose();
            villageDose.setVillageName(locationMap.get(entry.getKey()));
            villageDose.setID(entry.getKey());
            villageDose.setRecurringServices(entry.getValue());
            result.add(villageDose);
        }

        return result;
    }

    private static String getChildrenUpToDateVaccinations() {
        return "select c.base_entity_id , c.unique_id , c.first_name , c.last_name , c.middle_name ,\n" +
                "f.first_name family_name, c.dob\n" +
                "from ec_child c\n" +
                "left join ec_family f on c.relational_id = f.base_entity_id \n" +
                "where date(c.dob) > date('now', '-24 month')\n" +
                "and ifnull(c.dod,'') = '' and ifnull(c.date_removed,'') = '' and c.base_entity_id not in\n" +
                "(select distinct alerts.caseID from alerts where (alerts.status = 'upcoming' or alerts.status = 'normal' or alerts.status = 'urgent') and alerts.scheduleName\n" +
                "in ('BCG','OPV 1', 'OPV 2', 'OPV 3', 'PCV 1', 'PCV 2', 'PCV 3', 'Penta 1', 'Penta 2', 'Penta 3', 'Rota 1', 'Rota 2', 'HPV 2', 'HPV 1', 'IPV', 'YF', 'MCV 1', 'MCV 2'))";
    }

    private static String getChildrenNotUpToDateVaccinations() {
        return "select c.base_entity_id , c.unique_id , c.first_name , c.last_name , c.middle_name ,f.first_name family_name, c.dob  \n" +
                "from ec_child c\n" +
                "left join ec_family f on c.relational_id = f.base_entity_id \n" +
                "where date(c.dob) > date('now', '-24 month')\n" +
                "and ifnull(c.dod,'') = '' and ifnull(c.date_removed,'') = '' and c.base_entity_id in\n" +
                "(select distinct alerts.caseID from alerts where alerts.status in ('urgent','normal','inProcess','upcoming') and alerts.scheduleName\n" +
                "in ('BCG','OPV 1', 'OPV 2', 'OPV 3', 'PCV 1', 'PCV 2', 'PCV 3', 'Penta 1', 'Penta 2', 'Penta 3', 'Rota 1', 'Rota 2', 'HPV 2', 'HPV 1', 'IPV', 'YF', 'MCV 1', 'MCV 2'))";
    }

    private static String getChildrenWithBirthCerts() {
        return "select ec_child.base_entity_id , ec_child.unique_id , ec_child.first_name , ec_child.last_name , ec_child.middle_name,\n" +
                "f.first_name family_name, ec_child.dob\n" +
                "from ec_child \n" +
                "left join ec_family f on ec_child.relational_id = f.base_entity_id\n" +
                "where date(ec_child.dob) > date('now', '-24 month')\n" +
                "and ifnull(ec_child.dod,'') = '' and ifnull(ec_child.date_removed,'') = ''\n" +
                "and ec_child.base_entity_id in (\n" +
                "select v.base_entity_id from visits v\n" +
                "inner join visit_details vd on v.visit_id = vd.visit_id\n" +
                "where vd.visit_key = 'birth_cert' and vd.human_readable_details = 'Yes'\n" +
                ")";
    }

    private static String getChildrenWithNoBirthCerts() {
        return "select ec_child.base_entity_id , ec_child.unique_id , ec_child.first_name , ec_child.last_name , ec_child.middle_name ,\n" +
                "f.first_name family_name, ec_child.dob\n" +
                "from ec_child\n" +
                "left join ec_family f on ec_child.relational_id = f.base_entity_id\n" +
                "where date(ec_child.dob) > date('now', '-24 month')\n" +
                "and ifnull(ec_child.dod,'') = '' and ifnull(ec_child.date_removed,'') = ''\n" +
                "and ec_child.base_entity_id not in (\n" +
                "select v.base_entity_id from visits v\n" +
                "inner join visit_details vd on v.visit_id = vd.visit_id\n" +
                "where vd.visit_key = 'birth_cert' and vd.human_readable_details = 'Yes'\n" +
                ")";
    }

    private static String getChildrenExclusiveBreastFeeding() {
        return "select ec.base_entity_id , ec.unique_id , ec.first_name , ec.last_name , ec.middle_name ,\n" +
                "f.first_name family_name  ,ec.dob \n" +
                "from ec_child ec\n" +
                "left join ec_family f on ec.relational_id = f.base_entity_id\n" +
                "inner join (\n" +
                "select e.baseEntityId , max(e.eventDate) eventDate\n" +
                "from event e\n" +
                "where e.eventType = 'Child Home Visit'\n" +
                "group by e.baseEntityId\n" +
                ") e on ec.base_entity_id = e.baseEntityId\n" +
                "inner join ec_family_member ef on ec.base_entity_id = ef.base_entity_id and ef.date_removed is null\n" +
                "inner join (\n" +
                "select re.base_entity_id , STRFTIME('%Y-%m-%d', datetime(max(re.date)/1000,'unixepoch')) last_exclusive_date\n" +
                "from recurring_service_records re\n" +
                "inner join recurring_service_types rt on re.recurring_service_id = rt._id\n" +
                "where rt.type = 'Exclusive_breastfeeding' and ifnull(re.value,'yes') = 'yes'\n" +
                "group by re.base_entity_id\n" +
                ") ex on ex.base_entity_id = e.baseEntityId and SUBSTR(e.eventDate,1,10) between date(ex.last_exclusive_date, '-1 day') and date(ex.last_exclusive_date, '1 day')\n" +
                "where (( ifnull(ec.entry_point,'') <> 'PNC' ) or (ifnull(ec.entry_point,'') = 'PNC' and date(ec.dob, '+28 days')  <= date()))\n" +
                "and date(ec.dob) between date('now', '-5 month') and date('now')";
    }

    private static String getChildrenNotExclusiveBreastFeeding() {
        return "select ec.base_entity_id , ec.unique_id , ec.first_name , ec.last_name , ec.middle_name ,\n" +
                "f.first_name family_name  , ec.dob\n" +
                "from ec_child ec\n" +
                "left join ec_family f on ec.relational_id = f.base_entity_id \n" +
                "inner join (\n" +
                "select e.baseEntityId , max(e.eventDate) eventDate\n" +
                "from event e\n" +
                "where e.eventType = 'Child Home Visit'\n" +
                "group by e.baseEntityId\n" +
                ") e on ec.base_entity_id = e.baseEntityId\n" +
                "inner join ec_family_member ef on ec.base_entity_id = ef.base_entity_id and ef.date_removed is null\n" +
                "left join (\n" +
                "select re.base_entity_id , STRFTIME('%Y-%m-%d', datetime(max(re.date)/1000,'unixepoch')) last_exclusive_date\n" +
                "from recurring_service_records re\n" +
                "inner join recurring_service_types rt on re.recurring_service_id = rt._id\n" +
                "where rt.type = 'Exclusive_breastfeeding' and ifnull(re.value,'yes') = 'yes'\n" +
                "group by re.base_entity_id\n" +
                ") ex on ex.base_entity_id = e.baseEntityId and SUBSTR(e.eventDate,1,10) between date(ex.last_exclusive_date, '-1 day') and date(ex.last_exclusive_date, '1 day')\n" +
                "where (( ifnull(ec.entry_point,'') <> 'PNC' ) or (ifnull(ec.entry_point,'') = 'PNC' and date(ec.dob, '+28 days')  <= date()))\n" +
                "and date(ec.dob) between date('now', '-5 month') and date('now') and ex.base_entity_id is null";
    }

    private static String getChildrenVitaminAReceived() {
        return "select ec.base_entity_id , ec.unique_id , ec.first_name , ec.last_name , ec.middle_name ,\n" +
                "f.first_name family_name  , ec.dob \n" +
                "from recurring_service_types rt \n" +
                "inner join recurring_service_records re on re.recurring_service_id = rt._id \n" +
                "inner join ec_child ec on ec.base_entity_id = re.base_entity_id and (( ifnull(ec.entry_point,'') <> 'PNC' ) or (ifnull(ec.entry_point,'') = 'PNC' and date(ec.dob, '+28 days') <= date())) \n" +
                "inner join ec_family_member ef on ec.base_entity_id = ef.base_entity_id and ef.date_removed is null \n" +
                "inner join ec_family f on ec.relational_id = f.base_entity_id  \n" +
                "where rt.type = 'Vitamin_A' and date(ec.dob) between date('now', '-23 month') and date('now', '-6 month') \n" +
                "and ifnull(re.value,'yes') = 'yes' \n" +
                "and STRFTIME('%Y-%m-%d', datetime(re.date/1000,'unixepoch')) >=date('now', '-6 month')";
    }

    private static String getChildrenVitaminANotReceived() {
        return "select ec.base_entity_id , ec.unique_id , ec.first_name , ec.last_name , ec.middle_name , \n" +
                "f.first_name family_name  , ec.dob \n" +
                "from ec_child ec \n" +
                "left join ec_family f on ec.relational_id = f.base_entity_id  \n" +
                "inner join ec_family_member ef on ec.base_entity_id = ef.base_entity_id and ef.date_removed is null \n" +
                "where (( ifnull(ec.entry_point,'') <> 'PNC' ) or (ifnull(ec.entry_point,'') = 'PNC' and date(ec.dob, '+28 days')  <= date())) \n" +
                "and date(ec.dob) between date('now', '-23 month') and date('now', '-6 month') \n" +
                "and ec.base_entity_id not in ( \n" +
                "select re.base_entity_id from recurring_service_records re \n" +
                "inner join recurring_service_types rt on re.recurring_service_id = rt._id \n" +
                "where rt.type = 'Vitamin_A' and ifnull(re.value,'yes') = 'yes' \n" +
                "and STRFTIME('%Y-%m-%d', datetime(re.date/1000,'unixepoch')) >=date('now', '-6 month'))";
    }

    private static String getChildrenDewormed() {
        return "select ec.base_entity_id , ec.unique_id , ec.first_name , ec.last_name , ec.middle_name, \n" +
                "f.first_name family_name  , ec.dob \n" +
                "from recurring_service_types rt \n" +
                "inner join recurring_service_records re on re.recurring_service_id = rt._id \n" +
                "inner join ec_child ec on ec.base_entity_id = re.base_entity_id and (( ifnull(ec.entry_point,'') <> 'PNC' ) or (ifnull(ec.entry_point,'') = 'PNC' and date(ec.dob, '+28 days') <= date())) \n" +
                "inner join ec_family_member ef on ec.base_entity_id = ef.base_entity_id and ef.date_removed is null \n" +
                "inner join ec_family f on ec.relational_id = f.base_entity_id \n" +
                "where rt.type = 'Deworming' and date(ec.dob) between date('now', '-23 month') and date('now', '-12 month') \n" +
                "and ifnull(re.value,'yes') = 'yes' \n" +
                "and STRFTIME('%Y-%m-%d', datetime(re.date/1000,'unixepoch')) >=date('now', '-6 month')";
    }

    private static String getChildrenNotDewormed() {
        return "select ec.base_entity_id , ec.unique_id , ec.first_name , ec.last_name , ec.middle_name, \n" +
                "f.first_name family_name  , ec.dob  \n" +
                "from ec_child ec \n" +
                "left join ec_family f on ec.relational_id = f.base_entity_id \n" +
                "inner join ec_family_member ef on ec.base_entity_id = ef.base_entity_id and ef.date_removed is null \n" +
                "where (( ifnull(ec.entry_point,'') <> 'PNC' ) or (ifnull(ec.entry_point,'') = 'PNC' and date(ec.dob, '+28 days') <= date())) \n" +
                "and date(ec.dob) between date('now', '-23 month') and date('now', '-12 month') \n" +
                "and ec.base_entity_id not in ( \n" +
                "select re.base_entity_id from recurring_service_records re \n" +
                "inner join recurring_service_types rt on re.recurring_service_id = rt._id \n" +
                "where rt.type = 'Deworming' and ifnull(re.value,'yes') = 'yes' \n" +
                "and STRFTIME('%Y-%m-%d', datetime(re.date/1000,'unixepoch')) >=date('now', '-6 month'))";
    }

    @Contract(pure = true)
    private static String getSql(String indicatorCode) {
        String sql = "";
        switch (indicatorCode) {
            case ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_24_UPTO_DATE_VACCINATIONS:
                sql = ReportDao.getChildrenUpToDateVaccinations();
                break;
            case ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_24_OVERDUE_VACCINATIONS:
                sql = ReportDao.getChildrenNotUpToDateVaccinations();
                break;

            case ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_59_WITH_BIRTH_CERT:
                sql = ReportDao.getChildrenWithBirthCerts();
                break;
            case ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_59_WITH_NO_BIRTH_CERT:
                sql = ReportDao.getChildrenWithNoBirthCerts();
                break;
            case ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_5_EXCLUSIVELY_BREASTFEEDING:
                sql = ReportDao.getChildrenExclusiveBreastFeeding();
                break;
            case ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_0_5_NOT_EXCLUSIVELY_BREASTFEEDING:
                sql = ReportDao.getChildrenNotExclusiveBreastFeeding();
                break;
            case ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_6_59_VITAMIN_RECEIVED_A:
                sql = ReportDao.getChildrenVitaminAReceived();
                break;
            case ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_6_59_VITAMIN_NOT_RECEIVED_A:
                sql = ReportDao.getChildrenVitaminANotReceived();
                break;
            case ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_12_59_DEWORMED:
                sql = ReportDao.getChildrenDewormed();
                break;
            case ReportingConstants.ChildIndicatorKeys.COUNT_OF_CHILDREN_12_59_NOT_DEWORMED:
                sql = ReportDao.getChildrenNotDewormed();
                break;
            default:
                break;
        }
        return sql;
    }

    @NonNull
    public static List<EligibleChild> myCommunityActivityReportDetails(String indicatorCode) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        String sql = ReportDao.getSql(indicatorCode);

        DataMap<EligibleChild> dataMap = c -> {
            EligibleChild child = new EligibleChild();
            child.setID(getCursorValue(c, "base_entity_id"));
            child.setDateOfBirth(getCursorValueAsDate(c, "dob", sdf));

            String name = getCursorValue(c, "first_name", "") + " " + getCursorValue(c, "middle_name", "");
            name = name.trim() + " " + getCursorValue(c, "last_name", "");

            child.setFullName(name.trim());
            child.setFamilyName(getCursorValue(c, "family_name") + " Family");

            return child;
        };

        List<EligibleChild> res = readData(sql, dataMap);

        if (res == null || res.size() == 0)
            return new ArrayList<>();

        return res;
    }
}
