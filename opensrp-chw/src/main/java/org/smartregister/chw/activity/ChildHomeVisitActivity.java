package org.smartregister.chw.activity;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.presenter.BaseAncHomeVisitPresenter;
import org.smartregister.chw.anc.util.Constants;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.activity.CoreChildHomeVisitActivity;
import org.smartregister.chw.core.dao.VisitDao;
import org.smartregister.chw.core.domain.VisitSummary;
import org.smartregister.chw.core.interactor.CoreChildHomeVisitInteractor;
import org.smartregister.chw.interactor.ChildHomeVisitInteractorFlv;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class ChildHomeVisitActivity extends CoreChildHomeVisitActivity {
    @Override
    protected void registerPresenter() {
        presenter = new BaseAncHomeVisitPresenter(memberObject, this, new CoreChildHomeVisitInteractor(new ChildHomeVisitInteractorFlv()));
    }


    @Override
    public void submittedAndClose() {
        super.submittedAndClose();

        // updating birth certification
        String baseEntityID = memberObject.getBaseEntityId();
        List<Visit> summaryMap = VisitDao.getVisitsByMemberID(baseEntityID);
        if (summaryMap.size() > 0) {
            for (int i = 0; i < summaryMap.size(); i++) {
                if (summaryMap.get(i).getVisitType().equals("Birth Certification")) {
                    String birthCertification = summaryMap.get(i).getJson();
                    updateBirthRegistration(birthCertification);
                    break;
                }
            }
        }

        ChildProfileActivity.startMe(this, memberObject, ChildProfileActivity.class);
    }

    private void updateBirthRegistration(String birthCertification) {
        try {
            JSONObject response = new JSONObject(birthCertification);
            JSONArray array = response.getJSONArray("obs");
            JSONObject birthCert = array.getJSONObject(0);
            if (birthCert.getJSONArray("humanReadableValues").get(0).toString().equals("Yes")){
                String birth_cert_issue_date = array.getJSONObject(1).getJSONArray("values").get(0).toString();
                String birth_cert_num = array.getJSONObject(2).getJSONArray("values").get(0).toString();
            }else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
