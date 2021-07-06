package org.smartregister.chw.application;

public class ChwApplicationFlv extends DefaultChwApplicationFlv {

    @Override
    public boolean hasANC() {
        return false;
    }

    @Override
    public boolean syncUsingPost() {
        return false;
    }

    @Override
    public boolean hasPNC() {
        return false;
    }

    @Override
    public boolean hasChildSickForm() {
        return false;
    }

    @Override
    public boolean hasWashCheck() {
        return false;
    }

    @Override
    public boolean hasRoutineVisit() {
        return false;
    }

    @Override
    public boolean hasPinLogin() {
        return true;
    }

    @Override
    public boolean hasReports() {
        return true;
    }

    @Override
    public boolean hasJobAids() {
        return false;
    }

    @Override
    public boolean hasDefaultDueFilterForChildClient() {
        return true;
    }

    @Override
    public boolean hasJobAidsVitaminAGraph() {
        return false;
    }

    @Override
    public boolean hasJobAidsDewormingGraph() {
        return false;
    }

    @Override
    public boolean hasChildrenMNPSupplementationGraph() {
        return false;
    }

    @Override
    public boolean hasJobAidsBreastfeedingGraph() {
        return false;
    }

    @Override
    public boolean hasJobAidsBirthCertificationGraph() {
        return false;
    }

    @Override
    public boolean hasSurname() {
        return false;
    }

    @Override
    public boolean showMyCommunityActivityReport() {
        return true;
    }

    @Override
    public boolean showChildrenUnder5() {
        return false;
    }

    @Override
    public boolean launchChildClientsAtLogin() {
        return true;
    }

    @Override
    public boolean useThinkMd() {
        return true;
    }

    @Override

    public boolean splitUpcomingServicesView() {
        return true;
    }

    @Override
    public boolean showNoDueVaccineView() {
        return true;
    }

    @Override
    public boolean prioritizeChildNameOnChildRegister() {
        return true;
    }

    @Override
    public boolean showChildrenUnderFiveAndGirlsAgeNineToEleven() {
        return true;
    }

    @Override
    public boolean dueVaccinesFilterInChildRegister() {
        return true;
    }

    @Override
    public boolean includeCurrentChild() {
        return false;
    }

    @Override
    public boolean saveOnSubmission() {
        return true;
    }

    @Override
    public boolean relaxVisitDateRestrictions() {
        return true;
    }

    @Override
    public boolean showLastNameOnChildProfile() {
        return true;
    }

    @Override
    public boolean showChildrenAboveTwoDueStatus(){
        return false;
    }

    @Override
    public boolean showFamilyServicesScheduleWithChildrenAboveTwo() {return false;}

    @Override
    public boolean hasForeignData(){return true;}

    @Override
    public  boolean showIconsForChildrenUnderTwoAndGirlsAgeNineToEleven(){
        return true;
    }

    @Override
    public boolean showsPhysicallyDisabledView() { return false; }
}
