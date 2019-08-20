package org.smartregister.chw.core.presenter;

import org.json.JSONObject;
import org.smartregister.chw.core.contract.ChildHomeVisitContract;
import org.smartregister.chw.core.interactor.CoreChildHomeVisitInteractor;
import org.smartregister.chw.core.utils.BirthCertDataModel;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.chw.core.utils.ObsIllnessDataModel;
import org.smartregister.chw.core.utils.ServiceTask;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.util.FormUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import timber.log.Timber;

public class ChildHomeVisitPresenter implements ChildHomeVisitContract.Presenter, ChildHomeVisitContract.InteractorCallback {
    private WeakReference<ChildHomeVisitContract.View> view;
    private ChildHomeVisitContract.Interactor interactor;
    private CommonPersonObjectClient childClient;
    private FormUtils formUtils = null;
    private String editedBirthCertFormJson, editedIllnessJson, editedCounselingJson;
    private ArrayList<ServiceTask> serviceTasks = new ArrayList<>();

    public ChildHomeVisitPresenter(ChildHomeVisitContract.View view) {
        this.view = new WeakReference<>(view);
        interactor = new CoreChildHomeVisitInteractor();
    }

    public void setChildClient(CommonPersonObjectClient childClient) {
        this.childClient = childClient;

    }

    @Override
    public void updateBirthCertEditData(String jsonString) {
        editedBirthCertFormJson = jsonString;
        interactor.generateBirthCertForm(jsonString, this, true);
    }

    @Override
    public void updateObsIllnessEditData(String json) {
        editedIllnessJson = json;
        interactor.generateObsIllnessForm(json, this, true);
    }

    @Override
    public void updateBirthStatusTick(String jsonString) {
        getView().updateBirthStatusTick(jsonString);
    }

    @Override
    public void updateObsIllnessStatusTick(String jsonString) {
        getView().updateObsIllnessStatusTick(jsonString);

    }

    @Override
    public void updateTaskAdapter(ArrayList<ServiceTask> serviceTasks) {
        this.serviceTasks.clear();
        this.serviceTasks.addAll(serviceTasks);
        getView().updateTaskService();
    }

    @Override
    public ChildHomeVisitContract.View getView() {
        if (view != null) {
            return view.get();
        } else {
            return null;
        }
    }

    @Override
    public void getLastEditData() {
        interactor.getLastEditData(childClient, this);

    }

    @Override
    public void startBirthCertForm(JSONObject previousJson) {
        try {
            JSONObject form = getFormUtils().getFormJson(CoreConstants.JSON_FORM.getBirthCertification());
            String dobString = org.smartregister.family.util.Utils.getValue
                    (childClient.getColumnmaps(), DBConstants.KEY.DOB, false);
            if (previousJson != null) {
                getView().startFormActivity(new JSONObject(previousJson.getString("birtCert")));
            } else {
                JSONObject revForm = CoreJsonFormUtils.getBirthCertFormAsJson(form, childClient.getCaseId(), "", dobString);
                getView().startFormActivity(revForm);

            }

        } catch (Exception e) {

        }
    }

    @Override
    public void startObsIllnessCertForm(JSONObject previousJson) {
        try {

            JSONObject form = getFormUtils().getFormJson(CoreConstants.JSON_FORM.getObsIllness());
            String dobString = org.smartregister.family.util.Utils.getValue
                    (childClient.getColumnmaps(), DBConstants.KEY.DOB, false);
            if (previousJson != null) {
                getView().startFormActivity(new JSONObject(previousJson.getString("obsIllness")));
            } else {
                JSONObject revForm = CoreJsonFormUtils.getOnsIllnessFormAsJson(form, childClient.getCaseId(), "", dobString);
                getView().startFormActivity(revForm);

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    @Override
    public void generateBirthCertForm(String jsonString) {
        interactor.generateBirthCertForm(jsonString, this, false);
    }

    @Override
    public void generateObsIllnessForm(String jsonString) {
        interactor.generateObsIllnessForm(jsonString, this, false);
    }

    @Override
    public void generateTaskService(boolean isEditMode) {
        interactor.generateTaskService(childClient, this, getView().getContext(), isEditMode);
    }

    /*
    @Override
    public void startCounselingForm(JSONObject previousJson) {
        try {

            JSONObject form = getFormUtils().getFormJson(Constants.JSON_FORM.HOME_VISIT_COUNSELLING);
            if (previousJson != null && previousJson.getString("counseling") != null) {
                getView().startFormActivity(new JSONObject(previousJson.getString("counseling")));
            } else {
                getView().startFormActivity(form);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
    */

    @Override

    public ArrayList<ServiceTask> getServiceTasks() {
        return serviceTasks;
    }

    @Override
    public void saveForm() {
        interactor.saveForm(childClient);
    }


    /*
    @Override
    public void updateCounselingStatusTick() {
        getView().updateCounselingStatusTick();
    }
    */

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        view = null;//set to null on destroy

        // Inform interactor
        interactor.onDestroy(isChangingConfiguration);

        // Activity destroyed set interactor to null
        if (!isChangingConfiguration) {
            interactor = null;
        }
    }

    private FormUtils getFormUtils() {
        if (formUtils == null) {
            try {
                formUtils = FormUtils.getInstance(org.smartregister.family.util.Utils.context().applicationContext());
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return formUtils;
    }

    /*
    @Override
    public void generateCounselingForm(String jsonString) {
        interactor.generateCounselingForm(jsonString, this, false);
    }
    */

    public ArrayList<ObsIllnessDataModel> getIllnessDataList() {
        return ((CoreChildHomeVisitInteractor) interactor).getIllnessDataList();
    }

    public ArrayList<BirthCertDataModel> getBirthCertDataList() {
        return ((CoreChildHomeVisitInteractor) interactor).getBirthCertDataList();
    }

    public String getEditedBirthCertFormJson() {
        return editedBirthCertFormJson;
    }

    public String getEditedIllnessJson() {
        return editedIllnessJson;
    }

    public String getEditedCounselingJson() {
        return editedCounselingJson;
    }


    /*
    public ArrayList<BirthIllnessData> getCounselingDataList() {
        return ((ChildHomeVisitInteractor) interactor).getCounselingDataList();
    }
    */

    public int getSaveSize() {
        return ((CoreChildHomeVisitInteractor) interactor).getSaveSize();
    }


}
