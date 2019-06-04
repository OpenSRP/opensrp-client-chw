package org.smartregister.chw.util;

import java.util.ArrayList;
import java.util.List;

public class BirthIllnessData {
    private String question;
    private String answer;
    private String birthCertDate;
    private String birthCertNumber;
    private String illnessDate;
    private String illnessDescription;
    private String actionTaken;
    private List<String> counseled = new ArrayList<>();
    private List<String> notCounseled = new ArrayList<>();

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    private boolean isBirthCertHas;

    public boolean isBirthCertHas() {
        return isBirthCertHas;
    }

    public void setBirthCertHas(boolean birthCertHas) {
        isBirthCertHas = birthCertHas;
    }

    public String getBirthCertDate() {
        return birthCertDate;
    }

    public void setBirthCertDate(String birthCertDate) {
        this.birthCertDate = birthCertDate;
    }

    public String getBirthCertNumber() {
        return birthCertNumber;
    }

    public void setBirthCertNumber(String birthCertNumber) {
        this.birthCertNumber = birthCertNumber;
    }

    public String getIllnessDate() {
        return illnessDate;
    }

    public void setIllnessDate(String illnessDate) {
        this.illnessDate = illnessDate;
    }

    public String getIllnessDescription() {
        return illnessDescription;
    }

    public void setIllnessDescription(String illnessDescription) {
        this.illnessDescription = illnessDescription;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public List<String> getCounseled() {
        return counseled;
    }

    public void setCounseled(List<String> counseled) {
        this.counseled = counseled;
    }

    public List<String> getNotCounseled() {
        return notCounseled;
    }

    public void setNotCounseled(List<String> notCounseled) {
        this.notCounseled = notCounseled;
    }
}
