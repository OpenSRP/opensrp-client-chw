package org.smartregister.chw.core.interactor;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.contract.ImmunizationContact;
import org.smartregister.chw.core.domain.HomeVisit;
import org.smartregister.chw.core.model.ImmunizationModel;
import org.smartregister.chw.core.model.VaccineTaskModel;
import org.smartregister.chw.core.utils.ChildDBConstants;
import org.smartregister.chw.core.utils.ChwServiceSchedule;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.HomeVisitVaccineGroup;
import org.smartregister.chw.core.utils.Utils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Alert;
import org.smartregister.family.util.DBConstants;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.service.AlertService;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static org.smartregister.immunization.util.VaccinatorUtils.generateScheduleList;
import static org.smartregister.immunization.util.VaccinatorUtils.receivedVaccines;

public class ImmunizationViewInteractor implements ImmunizationContact.Interactor {

    private ImmunizationModel model;
    private AlertService alertService;
    private VaccineRepository vaccineRepository;

    public ImmunizationViewInteractor(Context context) {
        model = new ImmunizationModel(context);
        alertService = CoreChwApplication.getInstance().getContext().alertService();
        vaccineRepository = CoreChwApplication.getInstance().vaccineRepository();
    }

    public List<Vaccine> getVaccines() {
        return model.getVaccines();
    }

    @Override
    public void fetchImmunizationData(final CommonPersonObjectClient commonPersonObjectClient, final ImmunizationContact.InteractorCallBack callBack) {
        getVaccineTask(commonPersonObjectClient, new ArrayList<>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<VaccineTaskModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Timber.v("fetchImmunizationData -> onSubscribe");
                    }

                    @Override
                    public void onNext(VaccineTaskModel vaccineTaskModel) {
                        ArrayList<HomeVisitVaccineGroup> homeVisitVaccineGroupsList = model.determineAllHomeVisitVaccineGroup(commonPersonObjectClient, vaccineTaskModel.getAlerts(), vaccineTaskModel.getVaccines(), vaccineTaskModel.getNotGivenVaccine(), vaccineTaskModel.getScheduleList());
                        callBack.updateData(homeVisitVaccineGroupsList, vaccineTaskModel.getReceivedVaccines());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {
                        Timber.v("fetchImmunizationData -> onComplete");
                    }
                });
    }

    @Override
    public void fetchImmunizationEditData(final CommonPersonObjectClient commonPersonObjectClient, final ImmunizationContact.InteractorCallBack callBack) {
        getLastVaccineTask(commonPersonObjectClient, new ArrayList<>())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<VaccineTaskModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Timber.v("fetchImmunizationData -> onSubscribe");
                    }

                    @Override
                    public void onNext(VaccineTaskModel vaccineTaskModel) {
                        ArrayList<HomeVisitVaccineGroup> homeVisitVaccineGroupsList = model.determineAllHomeVisitVaccineGroup(commonPersonObjectClient, vaccineTaskModel.getAlerts(), vaccineTaskModel.getVaccines(), vaccineTaskModel.getNotGivenVaccine(), vaccineTaskModel.getScheduleList());
                        for (Iterator<HomeVisitVaccineGroup> iterator = homeVisitVaccineGroupsList.iterator(); iterator.hasNext(); ) {
                            HomeVisitVaccineGroup homeVisitVaccineGroup = iterator.next();
                            if (homeVisitVaccineGroup.getDueVaccines().size() == 0) {
                                homeVisitVaccineGroup.setViewType(HomeVisitVaccineGroup.TYPE_HIDDEN);
                            }

                        }
                        callBack.updateEditData(homeVisitVaccineGroupsList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onComplete() {
                        Timber.v("fetchImmunizationData -> onComplete");
                    }
                });
    }

    /**
     * Replacement of previous vaccinationasynctask
     * it'll calculate the received vaccine list of a child.
     *
     * @param childClient
     * @param notDoneVaccines
     * @return
     */
    public Observable<VaccineTaskModel> getLastVaccineTask(final CommonPersonObjectClient childClient, final ArrayList<VaccineWrapper> notDoneVaccines) {

        return Observable.create(emitter -> {
            String lastHomeVisitStr = org.smartregister.util.Utils.getValue(childClient.getColumnmaps(), ChildDBConstants.KEY.LAST_HOME_VISIT, false);
            long lastHomeVisit = TextUtils.isEmpty(lastHomeVisitStr) ? 0 : Long.parseLong(lastHomeVisitStr);
            HomeVisit homeVisit = CoreChwApplication.homeVisitRepository().findByDate(lastHomeVisit);

            String dobString = org.smartregister.util.Utils.getValue(childClient.getColumnmaps(), DBConstants.KEY.DOB, false);
            DateTime dob = Utils.dobStringToDateTime(dobString);

            List<Alert> alerts = CoreChwApplication.getInstance().getContext().alertService().findByEntityIdAndAlertNames(childClient.entityId(), VaccinateActionUtils.allAlertNames("child"));
            List<Vaccine> vaccines = CoreChwApplication.getInstance().vaccineRepository().findLatestTwentyFourHoursByEntityId(childClient.entityId());
            Map<String, Date> recievedVaccines = receivedVaccines(vaccines);
            List<Map<String, Object>> sch = generateScheduleList("child", dob, recievedVaccines, alerts);
            VaccineTaskModel vaccineTaskModel = new VaccineTaskModel();
            vaccineTaskModel.setAlerts(alerts);
            vaccineTaskModel.setVaccines(vaccines);
            vaccineTaskModel.setReceivedVaccines(recievedVaccines);
            vaccineTaskModel.setScheduleList(sch);
            if (homeVisit != null) {
                try {
                    JSONObject jsonObject = new JSONObject(homeVisit.getVaccineNotGiven().toString());
                    JSONArray array = jsonObject.getJSONArray("vaccineNotGiven");
                    if (array != null) {
                        ArrayList<VaccineWrapper> notGivenVaccine = CoreChildUtils.gsonConverter.fromJson(array.toString(), new TypeToken<ArrayList<VaccineWrapper>>() {
                        }.getType());
                        vaccineTaskModel.setNotGivenVaccine(notGivenVaccine);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            emitter.onNext(vaccineTaskModel);
        });
    }


    /**
     * Replacement of previous vaccinationasynctask
     * it'll calculate the received vaccine list of a child.
     *
     * @param childClient
     * @param notDoneVaccines
     * @return
     */
    public Observable<VaccineTaskModel> getVaccineTask(final CommonPersonObjectClient childClient, final ArrayList<VaccineWrapper> notDoneVaccines) {
        final String dobString = org.smartregister.util.Utils.getValue(childClient.getColumnmaps(), DBConstants.KEY.DOB, false);

        return Observable.create(emitter -> {
            DateTime dob = Utils.dobStringToDateTime(dobString);
            if (dob == null) {
                dob = new DateTime();
            }

            if (!TextUtils.isEmpty(dobString)) {
                DateTime dateTime = new DateTime(dobString);
                try {
                    VaccineSchedule.updateOfflineAlerts(childClient.getCaseId(), dateTime, "child");
                } catch (Exception e) {
                    Timber.e(e, "ImmunizationViewInteractor --> getVaccineTask");
                }
                try {
                    ChwServiceSchedule.updateOfflineAlerts(childClient.getCaseId(), dateTime, "child");
                } catch (Exception e) {
                    Timber.e(e, "ImmunizationViewInteractor --> getVaccineTask");
                }
            }
            List<Alert> alerts = alertService.findByEntityIdAndAlertNames(childClient.getCaseId(), VaccinateActionUtils.allAlertNames("child"));
            List<Vaccine> vaccines = vaccineRepository.findByEntityId(childClient.getCaseId());
            Map<String, Date> receivedVaccines = receivedVaccines(vaccines);
            int size = notDoneVaccines.size();
            for (int i = 0; i < size; i++) {
                receivedVaccines.put(notDoneVaccines.get(i).getName().toLowerCase(), new Date());
            }

            List<Map<String, Object>> sch = generateScheduleList("child", dob, receivedVaccines, alerts);
            VaccineTaskModel vaccineTaskModel = new VaccineTaskModel();
            vaccineTaskModel.setAlerts(alerts);
            vaccineTaskModel.setVaccines(vaccines);
            vaccineTaskModel.setReceivedVaccines(receivedVaccines);
            vaccineTaskModel.setScheduleList(sch);
            emitter.onNext(vaccineTaskModel);
        });
    }
}
