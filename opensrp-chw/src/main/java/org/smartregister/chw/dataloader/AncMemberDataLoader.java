package org.smartregister.chw.dataloader;

import android.content.Context;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.form_data.NativeFormsDataLoader;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.dao.ChwANCDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AncMemberDataLoader extends NativeFormsDataLoader {

    private String title;

    public AncMemberDataLoader(String title) {
        this.title = title;
    }

    @Override
    public void bindNativeFormsMetaData(@NotNull JSONObject jsonObjectForm, Context context, String baseEntityID) throws JSONException {
        super.bindNativeFormsMetaData(jsonObjectForm, context, baseEntityID);
        JSONObject stepOne = jsonObjectForm.getJSONObject("step1");
        if (StringUtils.isNotBlank(title))
            stepOne.put("title", title);
    }

    @Override
    public String getValue(Context context, String baseEntityID, JSONObject jsonObject, Map<String, Map<String, Object>> dbData) throws JSONException {
        String key = jsonObject.getString(JsonFormConstants.KEY);

        if ("delivery_method".equalsIgnoreCase(key))
            jsonObject.put(JsonFormConstants.READ_ONLY, true);
        if ("last_menstrual_period".equalsIgnoreCase(key)) {
            String date = ChwANCDao.getLastContactDate(baseEntityID);
            jsonObject.put(JsonFormConstants.MAX_DATE, date);
        }

        return super.getValue(context, baseEntityID, jsonObject, dbData);
    }

    @Override
    protected List<String> getEventTypes() {
        List<String> res = new ArrayList<>();
        res.add(CoreConstants.EventType.UPDATE_ANC_REGISTRATION);
        res.add(CoreConstants.EventType.ANC_REGISTRATION);
        return res;
    }

}
