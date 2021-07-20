package org.smartregister.chw.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.DBConstants;
import java.util.ArrayList;

public abstract class CoreChildUtils {

    public static Gson gsonConverter;

    static {
        gsonConverter = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(DateTime.class, (JsonSerializer<DateTime>) (json, typeOfSrc, context) -> new JsonPrimitive(ISODateTimeFormat.dateTime().print(json)))
                .registerTypeAdapter(DateTime.class, (JsonDeserializer<DateTime>) (json, typeOfT, context) -> new DateTime(json.getAsJsonPrimitive().getAsString()))
                .create();
    }

    public static String mainSelectRegisterWithoutGroupby(String tableName, String familyTableName, String familyMemberTableName, String mainCondition) {
        SmartRegisterQueryBuilder queryBuilder = new SmartRegisterQueryBuilder();
        queryBuilder.selectInitiateMainTable(tableName, mainColumns(tableName));
        return queryBuilder.mainCondition(mainCondition);
    }

    public static String[] mainColumns(String tableName) {
        ArrayList<String> columnList = new ArrayList<>();
        columnList.add(tableName + "." + DBConstants.KEY.RELATIONAL_ID + " as " + ChildDBConstants.KEY.RELATIONAL_ID);
        columnList.add(tableName + "." + DBConstants.KEY.BASE_ENTITY_ID);
        columnList.add(tableName + "." + DBConstants.KEY.UNIQUE_ID);
        columnList.add(tableName + "." + CrvsConstants.SURNAME);
        columnList.add(tableName + "." + DBConstants.KEY.FIRST_NAME);
        columnList.add(tableName + "." + DBConstants.KEY.MIDDLE_NAME);
        columnList.add(tableName + "." + DBConstants.KEY.DOB);
        columnList.add(tableName + "." + CrvsConstants.DOB_UNKNOWN);
        columnList.add(tableName + "." + DBConstants.KEY.GENDER);
        columnList.add(tableName + "." + CrvsConstants.MOTHER_NAME);
        columnList.add(tableName + "." + CrvsConstants.FATHER_NAME);
        columnList.add(tableName + "." + CrvsConstants.FATHER_BIRTH_PLACE);
        columnList.add(tableName + "." + CrvsConstants.MOTHER_MARITAL_STATUS);
        columnList.add(tableName + "." + CrvsConstants.BIRTH_PLACE_TYPE);
        columnList.add(tableName + "." + CrvsConstants.MOTHER_HIGHEST_EDU_LEVEL);
        columnList.add(tableName + "." + CrvsConstants.FATHER_MARITAL_STATUS);
        columnList.add(tableName + "." + CrvsConstants.FATHER_HIGHEST_EDU_LEVEL);
        columnList.add(tableName + "." + DBConstants.KEY.LAST_INTERACTED_WITH);
        return columnList.toArray(new String[columnList.size()]);
    }

    public interface Flavor {
        String[] getOneYearVaccines();
        String[] getTwoYearVaccines();
    }
}
