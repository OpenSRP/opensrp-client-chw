package org.smartregister.chw.presenter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartregister.chw.R;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.contract.HomeVisitImmunizationContract;
import org.smartregister.chw.interactor.HomeVisitImmunizationInteractor;
import org.smartregister.chw.util.HomeVisitVaccineGroup;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.util.DateUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static org.smartregister.chw.util.ChildUtils.fixVaccineCasing;

public class HomeVisitImmunizationPresenter implements HomeVisitImmunizationContract.Presenter, HomeVisitImmunizationContract.View {
    private static final String TAG = HomeVisitImmunizationPresenter.class.toString();

    private HomeVisitImmunizationContract.Interactor homeVisitImmunizationInteractor;
    private WeakReference<HomeVisitImmunizationContract.View> view;
    private ArrayList<VaccineRepo.Vaccine> vaccinesDueFromLastVisit = new ArrayList<VaccineRepo.Vaccine>();
    private ArrayList<HomeVisitVaccineGroup> allgroups = new ArrayList<HomeVisitVaccineGroup>();
    private ArrayList<VaccineWrapper> notGivenVaccines = new ArrayList<VaccineWrapper>();
    private HomeVisitVaccineGroup currentActiveGroup;
    private CommonPersonObjectClient childClient;
    private ArrayList<VaccineWrapper> vaccinesGivenThisVisit = new ArrayList<VaccineWrapper>();
    private String groupImmunizationSecondaryText = "";
    private String singleImmunizationSecondaryText = "";
    private final VaccineRepository vaccineRepository;

    public HomeVisitImmunizationPresenter() {
        homeVisitImmunizationInteractor = new HomeVisitImmunizationInteractor();
        vaccineRepository = ImmunizationLibrary.getInstance().vaccineRepository();
    }


    public HomeVisitImmunizationPresenter(HomeVisitImmunizationContract.View view) {
        this.view = new WeakReference<>(view);
        homeVisitImmunizationInteractor = new HomeVisitImmunizationInteractor();
        vaccineRepository = ImmunizationLibrary.getInstance().vaccineRepository();
    }

    @Override
    public void createAllVaccineGroups(List<Alert> alerts, List<Vaccine> vaccines, List<Map<String, Object>> sch) {
        allgroups = homeVisitImmunizationInteractor.determineAllHomeVisitVaccineGroup(alerts, vaccines, notGivenVaccines, sch);
    }

    @Override
    public void getVaccinesNotGivenLastVisit() {
        if (vaccinesDueFromLastVisit.size() == 0) {
            if (homeVisitImmunizationInteractor.hasVaccinesNotGivenSinceLastVisit(allgroups)) {
                vaccinesDueFromLastVisit = homeVisitImmunizationInteractor.getNotGivenVaccinesLastVisitList(allgroups);
            }
        }
    }

    @Override
    public void calculateCurrentActiveGroup() {
        currentActiveGroup = homeVisitImmunizationInteractor.getCurrentActiveHomeVisitVaccineGroupDetail(allgroups);
        if (currentActiveGroup == null) {
            currentActiveGroup = homeVisitImmunizationInteractor.getLastActiveHomeVisitVaccineGroupDetail(allgroups);
        }
    }

    @Override
    public HomeVisitImmunizationContract.View getView() {
        if (view != null) {
            return view.get();
        } else {
            return null;
        }
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        Log.d(TAG, "onDestroy unimplemented");
    }

    @Override
    public boolean isPartiallyComplete() {
        return getHomeVisitImmunizationInteractor().isPartiallyComplete(currentActiveGroup);
    }

    @Override
    public boolean isComplete() {
        return getHomeVisitImmunizationInteractor().isComplete(currentActiveGroup);
    }

    @Override
    public HomeVisitImmunizationContract.Interactor getHomeVisitImmunizationInteractor() {
        return homeVisitImmunizationInteractor;
    }

    @Override
    public void setView(WeakReference<HomeVisitImmunizationContract.View> view) {
        this.view = view;
    }

    @Override
    public ArrayList<VaccineRepo.Vaccine> getVaccinesDueFromLastVisit() {
        return vaccinesDueFromLastVisit;
    }

    @Override
    public ArrayList<HomeVisitVaccineGroup> getAllgroups() {
        return allgroups;
    }

    public ArrayList<VaccineWrapper> getNotGivenVaccines() {
        return notGivenVaccines;
    }


    @Override
    public HomeVisitVaccineGroup getCurrentActiveGroup() {
        return currentActiveGroup;
    }

    @Override
    public boolean groupIsDue() {
        return homeVisitImmunizationInteractor.groupIsDue(currentActiveGroup);
    }

    @Override
    public ArrayList<VaccineWrapper> createVaccineWrappers(HomeVisitVaccineGroup duevaccines) {

        ArrayList<VaccineWrapper> vaccineWrappers = new ArrayList<VaccineWrapper>();
        for (VaccineRepo.Vaccine vaccine : duevaccines.getDueVaccines()) {
            VaccineWrapper vaccineWrapper = new VaccineWrapper();
            vaccineWrapper.setVaccine(vaccine);
            vaccineWrapper.setName(vaccine.display());
            Long id = getVaccineId(vaccine.display());
            vaccineWrapper.setDbKey(id);
            vaccineWrapper.setDefaultName(vaccine.display());
            vaccineWrappers.add(vaccineWrapper);
        }
        return vaccineWrappers;
    }
    public Long getVaccineId(String vaccineName){
        List<Vaccine> vaccines = ((HomeVisitImmunizationInteractor)homeVisitImmunizationInteractor).getVaccines();
        for(Vaccine vaccine:vaccines){
            if(vaccine.getName().equalsIgnoreCase(vaccineName)){
                return vaccine.getId();
            }
        }
        return null;
    }
    @Override
    public ArrayList<VaccineWrapper> createGivenVaccineWrappers(HomeVisitVaccineGroup duevaccines) {

        ArrayList<VaccineWrapper> vaccineWrappers = new ArrayList<VaccineWrapper>();
        for (VaccineRepo.Vaccine vaccine : duevaccines.getGivenVaccines()) {
            VaccineWrapper vaccineWrapper = new VaccineWrapper();
            vaccineWrapper.setVaccine(vaccine);
            vaccineWrapper.setName(vaccine.display());
            Long id = getVaccineId(vaccine.display());
            vaccineWrapper.setDbKey(id);
            vaccineWrapper.setDefaultName(vaccine.display());
            vaccineWrappers.add(vaccineWrapper);
        }
        return vaccineWrappers;
    }

    @Override
    public CommonPersonObjectClient getchildClient() {
        return childClient;
    }

    @Override
    public void setActivity(Activity activity) {
        // TODO update the method
    }

    @Override
    public void setChildClient(CommonPersonObjectClient childClient) {
        this.childClient = childClient;
    }

    @Override
    public void refreshPresenter(List<Alert> alerts, List<Vaccine> vaccines, List<Map<String, Object>> sch) {
        // TODO update the method
    }

    @Override
    public HomeVisitImmunizationContract.Presenter initializePresenter() {
        return null;
    }

    @Override
    public HomeVisitImmunizationContract.Presenter getPresenter() {
        return null;
    }


    @Override
    public void updateImmunizationState() {
        // TODO update the method
    }

    @Override
    public void updateNotGivenVaccine(VaccineWrapper name) {
        if (!notGivenVaccines.contains(name)) {
            notGivenVaccines.add(name);
        }
        vaccinesGivenThisVisit.remove(name);
    }

    @Override
    public ArrayList<VaccineWrapper> getVaccinesGivenThisVisit() {
        return vaccinesGivenThisVisit;
    }

    @Override
    public void assigntoGivenVaccines(ArrayList<VaccineWrapper> tagsToUpdate) {
        vaccinesGivenThisVisit.addAll(tagsToUpdate);
        for (VaccineWrapper vaccineWrapper:tagsToUpdate){
            notGivenVaccines.remove(vaccineWrapper);
        }
    }

    /**
     * sometimes asynctask not started and vaccine not reset.so comment out the startAsyncTask
     * and using thread to reset the given vaccine.
     */

    public Observable undoVaccine() {

        return Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                for (VaccineWrapper tag : vaccinesGivenThisVisit) {
                    if (tag != null && tag.getDbKey() != null) {
                        Long dbKey = tag.getDbKey();
                        vaccineRepository.deleteVaccine(dbKey);

                    }
                }
                String dobString = org.smartregister.util.Utils.getValue(childClient.getColumnmaps(), "dob", false);
                if (!TextUtils.isEmpty(dobString)) {
                    DateTime dateTime = new DateTime(dobString);
                    VaccineSchedule.updateOfflineAlerts(childClient.entityId(), dateTime, "child");
                }
                e.onComplete();
            }
        });
    }

    @Override
    public void updateImmunizationState(HomeVisitImmunizationContract.InteractorCallBack callBack) {
        homeVisitImmunizationInteractor.updateImmunizationState(childClient, notGivenVaccines, callBack);
    }

    @Override
    public ArrayList<VaccineRepo.Vaccine> getVaccinesDueFromLastVisitStillDueState() {
        ArrayList<VaccineRepo.Vaccine> vaccinesToReturn = new ArrayList<VaccineRepo.Vaccine>();
        Stack<VaccineRepo.Vaccine> vaccinesStack = new Stack<VaccineRepo.Vaccine>();
        for (VaccineRepo.Vaccine vaccinedueLastVisit : vaccinesDueFromLastVisit) {
            vaccinesStack.add(vaccinedueLastVisit);
            for (VaccineWrapper givenThisVisit : vaccinesGivenThisVisit) {
                if (!vaccinesStack.isEmpty() && givenThisVisit.getDefaultName().equalsIgnoreCase(vaccinesStack.peek().display())) {
                    vaccinesStack.pop();
                }
            }
        }
        vaccinesToReturn.addAll(vaccinesStack);
        vaccinesStack = new Stack<VaccineRepo.Vaccine>();
        for (VaccineRepo.Vaccine vaccinesDueYetnotGiven : vaccinesToReturn) {
            vaccinesStack.add(vaccinesDueYetnotGiven);
            for (VaccineWrapper vaccine : notGivenVaccines) {
                if (
                        !vaccinesStack.isEmpty()
                                && vaccine.getDefaultName().equalsIgnoreCase(vaccinesStack.peek().display())
                ) {
                    vaccinesStack.pop();
                }
            }
        }
        vaccinesToReturn = new ArrayList<VaccineRepo.Vaccine>();
        vaccinesToReturn.addAll(vaccinesStack);
        return vaccinesToReturn;
    }

    @Override
    public boolean isSingleVaccineGroupPartialComplete() {
        boolean toReturn = false;
        ArrayList<VaccineRepo.Vaccine> singleVaccineInDueState = getVaccinesDueFromLastVisitStillDueState();
        if (singleVaccineInDueState.size() == 0) {
            for (VaccineRepo.Vaccine vaccineDueLastVisit : vaccinesDueFromLastVisit) {
                for (VaccineWrapper notgivenVaccine : notGivenVaccines) {
                    if (notgivenVaccine.getDefaultName().equalsIgnoreCase(vaccineDueLastVisit.display())) {
                        toReturn = true;
                    }
                }
            }
        }
        return toReturn;
    }

    @Override
    public boolean isSingleVaccineGroupComplete() {
        boolean toReturn = true;
        ArrayList<VaccineRepo.Vaccine> singleVaccineInDueState = getVaccinesDueFromLastVisitStillDueState();
        if (singleVaccineInDueState.size() == 0) {
            for (VaccineRepo.Vaccine vaccineDueLastVisit : vaccinesDueFromLastVisit) {
                for (VaccineWrapper notgivenVaccine : notGivenVaccines) {
                    if (notgivenVaccine.getDefaultName().equalsIgnoreCase(vaccineDueLastVisit.display())) {
                        toReturn = false;
                    }
                }
            }
        } else if (singleVaccineInDueState.size() > 0) {
            toReturn = false;
        }
        return toReturn;
    }

    @Override
    public void setGroupVaccineText(List<Map<String, Object>> sch) {
        ArrayList<VaccineRepo.Vaccine> allgivenVaccines = new ArrayList<VaccineRepo.Vaccine>();
        allgivenVaccines.addAll(getCurrentActiveGroup().getGivenVaccines());

        LinkedHashMap<DateTime, ArrayList<VaccineRepo.Vaccine>> groupedByDate = groupVaccines(allgivenVaccines, sch);

        String notGiven = addNotGivenVaccines(sch).trim();
        StringBuilder groupSecondaryText = new StringBuilder();
        Iterator<Map.Entry<DateTime, ArrayList<VaccineRepo.Vaccine>>> iterator = groupedByDate.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<DateTime, ArrayList<VaccineRepo.Vaccine>> entry = iterator.next();
            DateTime dueDate = entry.getKey();
            ArrayList<VaccineRepo.Vaccine> vaccines = entry.getValue();
            // now work with key and value...
            for (VaccineRepo.Vaccine vaccineGiven : vaccines) {
                groupSecondaryText.append(fixVaccineCasing(vaccineGiven.display())).append(", ");
            }

            if (groupSecondaryText.toString().endsWith(", ")) {
                groupSecondaryText = new StringBuilder(groupSecondaryText.toString().trim());
                groupSecondaryText = new StringBuilder(groupSecondaryText.substring(0, groupSecondaryText.length() - 1));
            }

            groupSecondaryText.append(getView().getContext().getString(R.string.given_on_with_spaces)).append(DateUtil.formatDate(dueDate.toLocalDate(), "dd MMM yyyy"));

            if (StringUtils.isNotBlank(notGiven) || iterator.hasNext()) {
                groupSecondaryText.append(" \u00B7 ");
            }
        }

        groupSecondaryText.append(notGiven);
        groupImmunizationSecondaryText = groupSecondaryText.toString();
    }

    /**
     * Groups vaccines by date
     *
     * @param givenVaccines
     * @param sch
     * @return
     */
    private LinkedHashMap<DateTime, ArrayList<VaccineRepo.Vaccine>> groupVaccines(ArrayList<VaccineRepo.Vaccine> givenVaccines, List<Map<String, Object>> sch) {
        LinkedHashMap<DateTime, ArrayList<VaccineRepo.Vaccine>> groupedByDate = new LinkedHashMap<DateTime, ArrayList<VaccineRepo.Vaccine>>();

        for (VaccineRepo.Vaccine vaccineGiven : givenVaccines) {
            for (Map<String, Object> mapToProcess : sch) {
                if (((VaccineRepo.Vaccine) mapToProcess.get("vaccine")).display().equalsIgnoreCase(vaccineGiven.display())) {
                    if (groupedByDate.get(mapToProcess.get("date")) == null) {
                        ArrayList<VaccineRepo.Vaccine> givenVaccinesAtDate = new ArrayList<VaccineRepo.Vaccine>();
                        givenVaccinesAtDate.add(vaccineGiven);
                        groupedByDate.put((DateTime) mapToProcess.get("date"), givenVaccinesAtDate);
                    } else {
                        groupedByDate.get(mapToProcess.get("date")).add(vaccineGiven);
                    }
                }
            }
        }

        return groupedByDate;
    }

    private String addNotGivenVaccines(List<Map<String, Object>> sch) {
        ArrayList<VaccineRepo.Vaccine> allgivenVaccines = new ArrayList<VaccineRepo.Vaccine>();
        allgivenVaccines.addAll(getCurrentActiveGroup().getNotGivenVaccines());

        LinkedHashMap<DateTime, ArrayList<VaccineRepo.Vaccine>> groupedByDate = groupVaccines(allgivenVaccines, sch);

        StringBuilder groupSecondaryText = new StringBuilder();
        Iterator<Map.Entry<DateTime, ArrayList<VaccineRepo.Vaccine>>> iterator = groupedByDate.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<DateTime, ArrayList<VaccineRepo.Vaccine>> entry = iterator.next();
            ArrayList<VaccineRepo.Vaccine> vaccines = entry.getValue();
            // now work with key and value...
            for (VaccineRepo.Vaccine vaccineGiven : vaccines) {
                groupSecondaryText.append(fixVaccineCasing(vaccineGiven.display())).append(", ");
            }

            if (groupSecondaryText.toString().endsWith(", ")) {
                groupSecondaryText = new StringBuilder(groupSecondaryText.toString().trim());
                groupSecondaryText = new StringBuilder(groupSecondaryText.substring(0, groupSecondaryText.length() - 1));
            }

            groupSecondaryText.append(getView().getContext().getString(R.string.not_given_with_spaces));
            if (iterator.hasNext()) {
                groupSecondaryText.append(" \u00B7 ");
            }
        }

        return groupSecondaryText.toString();
    }

    @Override
    public void setSingleVaccineText(ArrayList<VaccineRepo.Vaccine> vaccinesDueFromLastVisit, List<Map<String, Object>> sch) {
        ArrayList<VaccineRepo.Vaccine> allgivenVaccines = new ArrayList<VaccineRepo.Vaccine>();
        for (VaccineRepo.Vaccine vaccineDueFromLastVisit : vaccinesDueFromLastVisit) {
            for (VaccineWrapper vaccineWrapper : vaccinesGivenThisVisit) {
                if (vaccineWrapper.getDefaultName().equalsIgnoreCase(vaccineDueFromLastVisit.display())) {
                    allgivenVaccines.add(vaccineDueFromLastVisit);

                }
            }
        }
        LinkedHashMap<DateTime, ArrayList<VaccineRepo.Vaccine>> groupedByDate = new LinkedHashMap<DateTime, ArrayList<VaccineRepo.Vaccine>>();
        for (VaccineRepo.Vaccine vaccineGiven : allgivenVaccines) {
            for (Map<String, Object> mapToProcess : sch) {
                if (((VaccineRepo.Vaccine) mapToProcess.get("vaccine")).display().equalsIgnoreCase(vaccineGiven.display())) {
                    if (groupedByDate.get((DateTime) mapToProcess.get("date")) == null) {
                        ArrayList<VaccineRepo.Vaccine> givenVaccinesAtDate = new ArrayList<VaccineRepo.Vaccine>();
                        givenVaccinesAtDate.add(vaccineGiven);
                        groupedByDate.put((DateTime) mapToProcess.get("date"), givenVaccinesAtDate);
                    } else {
                        groupedByDate.get(mapToProcess.get("date")).add(vaccineGiven);
                    }
                }
            }
        }
        String groupSecondaryText = "";
        for (Map.Entry<DateTime, ArrayList<VaccineRepo.Vaccine>> entry : groupedByDate.entrySet()) {
            DateTime dateTime = entry.getKey();
            ArrayList<VaccineRepo.Vaccine> vaccines = entry.getValue();
            // now work with key and value...
            for (VaccineRepo.Vaccine vaccineGiven : vaccines) {
                groupSecondaryText = groupSecondaryText + fixVaccineCasing(vaccineGiven.display()) + ", ";
            }

            if (groupSecondaryText.endsWith(", ")) {
                groupSecondaryText = groupSecondaryText.trim();
                groupSecondaryText = groupSecondaryText.substring(0, groupSecondaryText.length() - 1);
            }
            groupSecondaryText = groupSecondaryText + getView().getContext().getString(R.string.given_on_with_spaces);

            String duedateString = DateUtil.formatDate(dateTime.toLocalDate(), "dd MMM yyyy");
            groupSecondaryText = groupSecondaryText + duedateString + " \u00B7 ";

        }
        singleImmunizationSecondaryText = groupSecondaryText;
    }

    @Override
    public String getGroupImmunizationSecondaryText() {
        return groupImmunizationSecondaryText;
    }

    @Override
    public void setGroupImmunizationSecondaryText(String groupImmunizationSecondaryText) {
        this.groupImmunizationSecondaryText = groupImmunizationSecondaryText;
    }

    @Override
    public String getSingleImmunizationSecondaryText() {
        return TextUtils.isEmpty(singleImmunizationSecondaryText) ? ChwApplication.getInstance().getString(R.string.not_given) : singleImmunizationSecondaryText;
    }

    @Override
    public void setSingleImmunizationSecondaryText(String singleImmunizationSecondaryText) {
        this.singleImmunizationSecondaryText = singleImmunizationSecondaryText;
    }


    @Override
    public Context getContext() {
        return getView().getContext();
    }

    @Override
    public void immunizationState(List<Alert> alerts, List<Vaccine> vaccines, Map<String, Date> receivedVaccine, List<Map<String, Object>> sch) {
        //TODO no need to do
    }
}
