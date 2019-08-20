package org.smartregister.chw.core.fragment;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vijay.jsonwizard.customviews.CheckBox;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartregister.chw.core.R;
import org.smartregister.chw.core.application.CoreChwApplication;
import org.smartregister.chw.core.custom_views.ImmunizationView;
import org.smartregister.chw.core.utils.CoreChildUtils;
import org.smartregister.chw.core.utils.CoreJsonFormUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.family.util.DBConstants;
import org.smartregister.family.util.Utils;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.ServiceSchedule;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.repository.VaccineRepository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

@SuppressLint("ValidFragment")
public class VaccinationDialogFragment extends DialogFragment implements View.OnClickListener {
    public static final String DIALOG_TAG = "VaccinationDialogFragment";
    public static final String WRAPPER_TAG = "tag";
    public static final String NOT_GIVEN = "not_given";
    public static final String GIVEN = "given";
    public static final String GROUP_NAME = "group_name";
    private List<VaccineWrapper> tags, notGivenList, givenList;
    private Date dateOfBirth;
    private ImmunizationView immunizationView;
    private int selectCount = 0;
    private Button saveBtn;
    private LinearLayout multipleVaccineDatePickerView, singleVaccineAddView, vaccinationNameLayout;
    private CheckBox checkBoxNoVaccine;
    private LayoutInflater inflater;
    private DatePicker earlierDatePicker;
    private Map<VaccineWrapper, DatePicker> singleVaccineMap = new LinkedHashMap<>();
    private CommonPersonObjectClient childDetails;
    private String groupName;
    private VaccineRepository vaccineRepository;

    public static VaccinationDialogFragment newInstance(Date dateOfBirth, ArrayList<VaccineWrapper> notGiven, ArrayList<VaccineWrapper> given,
                                                        ArrayList<VaccineWrapper> tags, String groupName) {

        VaccinationDialogFragment customVaccinationDialogFragment = new VaccinationDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable(WRAPPER_TAG, tags);
        args.putSerializable(NOT_GIVEN, notGiven);
        args.putSerializable(GIVEN, given);
        args.putString(GROUP_NAME, groupName);
        customVaccinationDialogFragment.setArguments(args);
        customVaccinationDialogFragment.setDateOfBirth(dateOfBirth);
        customVaccinationDialogFragment.setDisableConstraints(true);

        return customVaccinationDialogFragment;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setDisableConstraints(boolean disableConstraints) {
        if (disableConstraints) {
            Calendar dcToday = Calendar.getInstance();
            VaccineSchedule.standardiseCalendarDate(dcToday);
        }
    }

    public void setChildDetails(CommonPersonObjectClient childDetails) {
        this.childDetails = childDetails;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_NoActionBar);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onStart() {
        super.onStart();

        // without a handler, the window sizes itself correctly
        // but the keyboard does not show up
        new Handler().post(() -> {
            Window window = null;
            if (getDialog() != null) {
                window = getDialog().getWindow();
            }

            if (window == null) {
                return;
            }

            Point size = new Point();

            Display display = window.getWindowManager().getDefaultDisplay();
            display.getSize(size);

            window.setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            window.setGravity(Gravity.CENTER);
        });
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.fragment_vaccination_dialog_view, container, false);
        initUi(dialogView);
        return dialogView;
    }

    private void initUi(View dialogView) {
        multipleVaccineDatePickerView = dialogView.findViewById(R.id.multiple_vaccine_date_pickerview);
        singleVaccineAddView = dialogView.findViewById(R.id.single_vaccine_add_layout);
        saveBtn = dialogView.findViewById(R.id.save_btn);
        View noVaccineLayout = dialogView.findViewById(R.id.checkbox_no_vaccination);
        noVaccineLayout.setOnClickListener(this);
        checkBoxNoVaccine = noVaccineLayout.findViewById(R.id.select);
        checkBoxNoVaccine.setChecked(false);
        checkBoxNoVaccine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    resetAllSelectedVaccine();
                } else {
                    updateSaveButton();
                }

            }
        });
        saveBtn = dialogView.findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);
        vaccinationNameLayout = dialogView.findViewById(R.id.vaccination_name_layout);
        earlierDatePicker = dialogView.findViewById(R.id.earlier_date_picker);
        TextView textViewAddDate = dialogView.findViewById(R.id.add_date_separately);
        textViewAddDate.setOnClickListener(this);
        dialogView.findViewById(R.id.close).setOnClickListener(this);
    }

    private void resetAllSelectedVaccine() {
        Map<CheckBox, String> getSelectedCheckBox = getSelectedCheckBoxes();
        for (CheckBox checkBox : getSelectedCheckBox.keySet()) {
            checkBox.toggle();
        }
        multipleVaccineDatePickerView.setAlpha(0.3f);
        selectCount = 0;

    }

    private void updateSaveButton() {
        if (saveBtn != null) {
            if (selectCount == 0) {
                checkBoxNoVaccine.setChecked(true);
                multipleVaccineDatePickerView.setVisibility(View.VISIBLE);
                multipleVaccineDatePickerView.setAlpha(0.3f);
                singleVaccineAddView.setVisibility(View.GONE);
                //saveBtn.setAlpha(0.3f);
            } else {
                checkBoxNoVaccine.setChecked(false);
                multipleVaccineDatePickerView.setAlpha(1.0f);
                if (singleVaccineAddView.getVisibility() == View.VISIBLE) {
                    showSingleVaccineDetailsView();
                }
                saveBtn.setAlpha(1.0f);
            }

        }
    }

    private Map<CheckBox, String> getSelectedCheckBoxes() {
        Map<CheckBox, String> vaccineCheckMap = new LinkedHashMap<>();
        for (int i = 0; i < vaccinationNameLayout.getChildCount(); i++) {
            View chilView = vaccinationNameLayout.getChildAt(i);
            CheckBox selectChild = chilView.findViewById(R.id.select);
            if (selectChild.isChecked()) {
                TextView childVaccineView = chilView.findViewById(R.id.vaccine);
                String checkedName = childVaccineView.getText().toString();
                vaccineCheckMap.put(selectChild, checkedName);
            }
        }
        return vaccineCheckMap;
    }

    private void showSingleVaccineDetailsView() {
        multipleVaccineDatePickerView.setVisibility(View.GONE);
        ArrayList<VaccineWrapper> vaccineWrappers = new ArrayList<VaccineWrapper>();
        List<String> selectedCheckboxes = findSelectedCheckBoxes(vaccinationNameLayout);
        singleVaccineAddView.removeAllViews();
        singleVaccineMap.clear();

        singleVaccineAddView.setVisibility(View.VISIBLE);
        String dobString = org.smartregister.util.Utils.getValue(childDetails, DBConstants.KEY.DOB, false);

        for (String checkedName : selectedCheckboxes) {
            VaccineWrapper tag = searchWrapperByName(checkedName);

            if (tag != null && !TextUtils.isEmpty(dobString)) {
                View layout = inflater.inflate(R.layout.custom_single_vaccine_view, null);
                TextView question = layout.findViewById(R.id.vaccines_given_when_title_question);
                DatePicker datePicker = layout.findViewById(R.id.earlier_date_picker);
                question.setText(getString(R.string.when_vaccine, checkedName));
                updateDatePicker(datePicker);

                vaccineWrappers.add(tag);
                singleVaccineMap.put(tag, datePicker);
                singleVaccineAddView.addView(layout);
            }
        }

    }

    private List<String> findSelectedCheckBoxes(LinearLayout vaccinationNameLayout) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < vaccinationNameLayout.getChildCount(); i++) {
            View chilView = vaccinationNameLayout.getChildAt(i);
            CheckBox selectChild = chilView.findViewById(R.id.select);
            if (selectChild.isChecked()) {
                TextView childVaccineView = chilView.findViewById(R.id.vaccine);
                String checkedName = childVaccineView.getText().toString();
                names.add(checkedName);
            }
        }

        return names;
    }

    private VaccineWrapper searchWrapperByName(String name) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }

        for (VaccineWrapper tag : tags) {
            if (tag.getVaccine() != null) {
                if (tag.getVaccine().display().equalsIgnoreCase(name)) {
                    return tag;
                }
            } else {
                if (tag.getName().equalsIgnoreCase(name)) {
                    return tag;
                }
            }
        }
        return null;
    }

    private void updateDatePicker(DatePicker datePicker) {
        DateTime minDate = new DateTime(dateOfBirth);
        DateTime dcToday = ServiceSchedule.standardiseDateTime(DateTime.now());
        DateTime maxDate = ServiceSchedule.standardiseDateTime(dcToday);

        if (minDate.getMillis() > maxDate.getMillis()) {
            datePicker.setMinDate(minDate.getMillis());
            datePicker.setMaxDate(minDate.getMillis());
        } else {
            datePicker.setMinDate(minDate.getMillis());
            datePicker.setMaxDate(maxDate.getMillis());
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vaccineRepository = ImmunizationLibrary.getInstance().vaccineRepository();
        parseBundleData();
        updateDatePicker(earlierDatePicker);
        updateVaccineList();
    }

    private void parseBundleData() {
        Bundle bundle = getArguments();
        final Serializable serializable = bundle.getSerializable(WRAPPER_TAG);
        if (serializable instanceof ArrayList) {
            tags = (ArrayList<VaccineWrapper>) serializable;
            notGivenList = (ArrayList<VaccineWrapper>) bundle.getSerializable(NOT_GIVEN);
            givenList = (ArrayList<VaccineWrapper>) bundle.getSerializable(GIVEN);
        }
        groupName = bundle.getString(GROUP_NAME, "");
    }

    private void updateVaccineList() {
        if (tags == null) {
            return;
        }
        for (VaccineWrapper vaccineWrapper : tags) {

            View vaccinationName = inflater.inflate(R.layout.custom_vaccine_name_check, null);
            TextView vaccineView = vaccinationName.findViewById(R.id.vaccine);


            VaccineRepo.Vaccine vaccine = vaccineWrapper.getVaccine();
            if (vaccineWrapper.getVaccine() != null) {
                vaccineView.setText(CoreChildUtils.fixVaccineCasing(vaccine.display()));
            } else {
                vaccineView.setText(vaccineWrapper.getName());
            }
            vaccinationNameLayout.addView(vaccinationName);
        }

        selectCount = vaccinationNameLayout.getChildCount();
        for (int i = 0; i < vaccinationNameLayout.getChildCount(); i++) {
            View childView = vaccinationNameLayout.getChildAt(i);
            final CheckBox childSelect = childView.findViewById(R.id.select);
            TextView vaccineView = childView.findViewById(R.id.vaccine);
            if (vaccineView != null && isExistInNotGiven(vaccineView.getText().toString())) {
                childSelect.toggle();
            }
            childSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selectCount++;
                    } else {
                        selectCount--;
                    }
                    updateSaveButton();
                }
            });
            childView.setOnClickListener(view -> childSelect.toggle());
        }


    }

    private boolean isExistInNotGiven(String name) {
        for (VaccineWrapper vaccineWrapper : notGivenList) {
            if (vaccineWrapper.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSaveButton();
        if (tags.size() == notGivenList.size()) {
            checkBoxNoVaccine.setChecked(true);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.add_date_separately) {
            if (selectCount == 0) {
                return;
            }
            showSingleVaccineDetailsView();
        } else if (i == R.id.checkbox_no_vaccination) {
            checkBoxNoVaccine.toggle();
        } else if (i == R.id.save_btn) {
            saveData(earlierDatePicker, singleVaccineMap, selectCount,
                    singleVaccineAddView.getVisibility() == View.VISIBLE, dateOfBirth, findUnSelectedCheckBoxes(vaccinationNameLayout), findSelectedCheckBoxes(vaccinationNameLayout));
            ((CoreChildHomeVisitFragment) getActivity().getFragmentManager().findFragmentByTag(CoreChildHomeVisitFragment.DIALOG_TAG)).updateImmunizationState();

            dismiss();
        } else if (i == R.id.close) {
            dismiss();
        }
    }

    private void saveData(DatePicker earlierDatePicker, Map<VaccineWrapper, DatePicker> singleVaccineMap
            , int selectCount, boolean isVisibleSingleVaccineView, Date dateOfBirth, List<String> unselectedCheckBox, List<String> selectedCheckBox) {
        int day = earlierDatePicker.getDayOfMonth();
        int month = earlierDatePicker.getMonth();
        int year = earlierDatePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        DateTime dateTime = new DateTime(calendar.getTime());
        if (selectCount == 0) {
            handleNotGivenVaccines(dateTime, dateOfBirth, unselectedCheckBox);
            dismiss();
            return;
        }

        if (isVisibleSingleVaccineView && singleVaccineMap.size() > 0) {
            handleSingleVaccineLogic(singleVaccineMap, dateOfBirth);
            handleNotGivenVaccines(dateTime, dateOfBirth, unselectedCheckBox);
            dismiss();
            return;
        }
        handleMultipleVaccineGiven(dateTime, dateOfBirth, selectedCheckBox);
        handleNotGivenVaccines(dateTime, dateOfBirth, unselectedCheckBox);
    }

    private void handleSingleVaccineLogic(Map<VaccineWrapper, DatePicker> singleVaccineMap, Date dateOfBirth) {
        ArrayList<VaccineWrapper> tagsToUpdate = new ArrayList<>();
        for (VaccineWrapper wrapper : singleVaccineMap.keySet()) {
            DatePicker datePicker = singleVaccineMap.get(wrapper);
            if (datePicker != null) {
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth();
                int year = datePicker.getYear();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                DateTime dateTime = new DateTime(calendar.getTime());
                if (validateVaccinationDate(dateOfBirth, dateTime.toDate())) {
                    wrapper.setUpdatedVaccineDate(dateTime, false);
                    tagsToUpdate.add(wrapper);
                } else {
                    showToast(String.format(getString(R.string.cannot_record_vaccine),
                            wrapper.getName()));
                }
            }
        }
        if (tagsToUpdate.size() > 0) {
            saveGivenVaccine(childDetails, tagsToUpdate);
        }
        immunizationView.getPresenter().assigntoGivenVaccines(tagsToUpdate);


    }

    private void handleMultipleVaccineGiven(DateTime dateTime, Date dateOfBirth, List<String> selectedCheckboxes) {
        ArrayList<VaccineWrapper> tagsToUpdate = new ArrayList<>();
        for (String checkedName : selectedCheckboxes) {
            VaccineWrapper tag = searchWrapperByName(checkedName);
            if (tag != null) {
                if (validateVaccinationDate(dateOfBirth, dateTime.toDate())) {
                    tag.setUpdatedVaccineDate(dateTime, false);
                    tagsToUpdate.add(tag);
                } else {
                    showToast(String.format(getString(R.string.cannot_record_vaccine),
                            tag.getName()));
                }
            }
        }
        if (tagsToUpdate.size() > 0) {
            saveGivenVaccine(childDetails, tagsToUpdate);
        }
        immunizationView.getPresenter().assigntoGivenVaccines(tagsToUpdate);


    }

    private void handleNotGivenVaccines(DateTime dateTime, Date dateOfBirth, List<String> UnselectedCheckboxes) {
        ArrayList<VaccineWrapper> UngiventagsToUpdate = new ArrayList<>();
        for (String uncheckedName : UnselectedCheckboxes) {
            VaccineWrapper untag = searchWrapperByName(uncheckedName);
            if (untag != null) {
                if (validateVaccinationDate(dateOfBirth, dateTime.toDate())) {
                    untag.setUpdatedVaccineDate(dateTime, false);
                    UngiventagsToUpdate.add(untag);

                } else {
                    showToast(String.format(getString(R.string.cannot_record_vaccine),
                            untag.getName()));
                }
            }
        }
        if (UngiventagsToUpdate.size() > 0) {
            undoPreviousGivenVaccine(childDetails, UngiventagsToUpdate);
        }

        immunizationView.getPresenter().assignToNotGivenVaccines(UngiventagsToUpdate, groupName);


    }

    public void saveGivenVaccine(final CommonPersonObjectClient childClient, final List<VaccineWrapper> givenVaccine) {
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        try {
            for (VaccineWrapper tag : givenVaccine) {
                saveVaccine(tag, childClient);
            }
            String dobString = org.smartregister.util.Utils.getValue(childClient.getColumnmaps(), "dob", false);
            if (!TextUtils.isEmpty(dobString)) {
                DateTime dateTime = new DateTime(dobString);
                VaccineSchedule.updateOfflineAlerts(childClient.entityId(), dateTime, "child");
            }
        } catch (Exception e) {
            Timber.e("saveGivenVaccine %s", e.toString());
        }
//            }
//        }).start();
    }

    private void undoPreviousGivenVaccine(final CommonPersonObjectClient childClient, final ArrayList<VaccineWrapper> notGivenVaccine) {

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        for (VaccineWrapper untag : notGivenVaccine) {

            if (isExistInGiven(untag.getName())) {
                Long dbKey = untag.getDbKey();
                vaccineRepository.deleteVaccine(dbKey);
            }
        }
        String dobString = org.smartregister.util.Utils.getValue(childClient.getColumnmaps(), "dob", false);
        if (!TextUtils.isEmpty(dobString)) {
            DateTime dateTime = new DateTime(dobString);
            VaccineSchedule.updateOfflineAlerts(childClient.entityId(), dateTime, "child");
        }
//            }
//        }).start();
    }

    private void saveVaccine(VaccineWrapper tag, CommonPersonObjectClient childClient) {
        if (tag.getUpdatedVaccineDate() == null) {
            return;
        }
        Vaccine vaccine = new Vaccine();
        if (tag.getDbKey() != null) {
            vaccine = vaccineRepository.find(tag.getDbKey());
        }
        vaccine.setBaseEntityId(childClient.entityId());
        vaccine.setName(tag.getName());
        vaccine.setDate(tag.getUpdatedVaccineDate().toDate());

        String lastChar = vaccine.getName().substring(vaccine.getName().length() - 1);
        if (StringUtils.isNumeric(lastChar)) {
            vaccine.setCalculation(Integer.valueOf(lastChar));
        } else {
            vaccine.setCalculation(-1);
        }

        CoreJsonFormUtils.tagSyncMetadata(Utils.context().allSharedPreferences(), vaccine);
        vaccineRepository.add(vaccine);
        tag.setDbKey(vaccine.getId());
    }

    private Context getApplicationContext() {
        return CoreChwApplication.getInstance().getApplicationContext();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

    }

    private boolean isExistInGiven(String name) {
        for (VaccineWrapper vaccineWrapper : givenList) {
            if (vaccineWrapper.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean validateVaccinationDate(Date dateOfBirth, Date date) {
        // Assuming that the vaccine wrapper provided to this method isn't one where there's more than one vaccine in a wrapper
        Date minDate;
        Date maxDate;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateOfBirth);
        Calendar dcToday = Calendar.getInstance();
        VaccineSchedule.standardiseCalendarDate(calendar);
        minDate = calendar.getTime();
        maxDate = dcToday.getTime();
        Calendar vaccineDate = Calendar.getInstance();
        vaccineDate.setTime(date);
        VaccineSchedule.standardiseCalendarDate(vaccineDate);
        boolean result;

        // A null min date means the vaccine is not due (probably because the prerequisite hasn't been done yet)
        result = minDate != null;

        // Check if vaccination was done before min date
        if (minDate != null) {
            Calendar min = Calendar.getInstance();
            min.setTime(minDate);
            VaccineSchedule.standardiseCalendarDate(min);
            result = min.getTimeInMillis() <= vaccineDate.getTimeInMillis();
        }

        // A null max date means the vaccine doesn't have a max date check
        //Check if vaccination was done after the max date
        if (maxDate != null) {
            Calendar max = Calendar.getInstance();
            max.setTime(maxDate);
            VaccineSchedule.standardiseCalendarDate(max);

            result = result && vaccineDate.getTimeInMillis() <= max.getTimeInMillis();
        }

        return result;
    }

    private List<String> findUnSelectedCheckBoxes(LinearLayout vaccinationNameLayout) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < vaccinationNameLayout.getChildCount(); i++) {
            View chilView = vaccinationNameLayout.getChildAt(i);
            CheckBox selectChild = chilView.findViewById(R.id.select);
            if (!selectChild.isChecked()) {
                TextView childVaccineView = chilView.findViewById(R.id.vaccine);
                String checkedName = childVaccineView.getText().toString();
                names.add(checkedName);
            }
        }

        return names;
    }

    public void setView(ImmunizationView view) {
        immunizationView = view;
    }


}
