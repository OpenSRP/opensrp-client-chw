package org.smartregister.chw.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.family.util.Utils;

public class JsonFormUtilsFlv extends DefaultJsonFormUtilsFlv {

    public static void startFormActivity(Context context, JSONObject jsonForm, String title) {
        Intent intent = new Intent(context, Utils.metadata().familyFormActivity);
        intent.putExtra("json", jsonForm.toString());
        Form form = new Form();
        form.setHideSaveLabel(true);
        form.setName(title);
        form.setActionBarBackground(org.smartregister.chw.core.R.color.family_actionbar);
        form.setNavigationBackground(org.smartregister.chw.core.R.color.family_navigation);
        form.setHomeAsUpIndicator(org.smartregister.chw.core.R.mipmap.ic_cross_white);
        form.setPreviousLabel(context.getResources().getString(org.smartregister.chw.core.R.string.back));
        intent.putExtra("form", form);
        ((Activity)context).startActivityForResult(intent, 2244);
    }

}
