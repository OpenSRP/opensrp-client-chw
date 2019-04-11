package org.smartregister.chw.util;

import org.smartregister.immunization.db.VaccineRepo;

import java.util.ArrayList;

/**
 * Created by raihan on 1/15/19.
 */

public class HomeVisitVaccineGroup {
    private ArrayList<VaccineRepo.Vaccine> givenVaccines = new ArrayList<VaccineRepo.Vaccine>();
    private ArrayList<VaccineRepo.Vaccine> dueVaccines = new ArrayList<VaccineRepo.Vaccine>();
    private ArrayList<VaccineRepo.Vaccine> notGivenVaccines = new ArrayList<VaccineRepo.Vaccine>();
    private ArrayList<VaccineRepo.Vaccine> notGivenInThisVisitVaccines = new ArrayList<VaccineRepo.Vaccine>();
    private String group = "";
    private ImmunizationState alert = ImmunizationState.NO_ALERT;
    private String dueDisplayDate = "";
    private String dueDate = "";

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }


    public ArrayList<VaccineRepo.Vaccine> getNotGivenInThisVisitVaccines() {
        return notGivenInThisVisitVaccines;
    }

    public ArrayList<VaccineRepo.Vaccine> getGivenVaccines() {
        return givenVaccines;
    }


    public ArrayList<VaccineRepo.Vaccine> getDueVaccines() {
        return dueVaccines;
    }


    public ArrayList<VaccineRepo.Vaccine> getNotGivenVaccines() {
        return notGivenVaccines;
    }


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public ImmunizationState getAlert() {
        return alert;
    }

    public void setAlert(ImmunizationState alert) {
        this.alert = alert;
    }

    public void calculateNotGivenVaccines() {
        for (VaccineRepo.Vaccine vaccine : dueVaccines) {
            boolean isGiven = false;
            for (VaccineRepo.Vaccine givenVaccine : givenVaccines) {
                if (givenVaccine.display().equalsIgnoreCase(vaccine.display())) {
                    isGiven = true;
                }
            }
            if (!isGiven && !notGivenVaccines.contains(vaccine)) {
                notGivenVaccines.add(vaccine);
            }
        }
    }

    public String getDueDisplayDate() {
        return dueDisplayDate;
    }

    public void setDueDisplayDate(String dueDate) {
        this.dueDisplayDate = dueDate;
    }
}
