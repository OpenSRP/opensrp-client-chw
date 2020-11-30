package org.smartregister.chw.application;

public abstract class DefaultChwApplicationFlv implements ChwApplication.Flavor {
    @Override
    public boolean checkP2PTeamId() {
        return false;
    }

    @Override
    public boolean hasCustomDate() {
        return false;
    }

    @Override
    public boolean hasP2P() {
        return true;
    }

    @Override
    public boolean syncUsingPost() {
        return true;
    }

    @Override
    public boolean hasReferrals() {
        return false;
    }

    @Override
    public boolean hasOPD() {
        return false;
    }

    @Override
    public boolean flvSetFamilyLocation() {
        return false;
    }

    @Override
    public boolean hasANC() {
        return true;
    }

    @Override
    public boolean hasPNC() {
        return true;
    }

    @Override
    public boolean hasChildSickForm() {
        return false;
    }

    @Override
    public boolean hasFamilyPlanning() {
        return false;
    }

    @Override
    public boolean hasMalaria() {
        return false;
    }

    @Override
    public boolean hasWashCheck() {
        return true;
    }

    @Override
    public boolean hasFamilyKitCheck() {
        return false;
    }

    @Override
    public boolean hasRoutineVisit() {
        return false;
    }

    @Override
    public boolean hasServiceReport() {
        return false;
    }

    @Override
    public boolean hasStockUsageReport() {
        return false;
    }

    @Override
    public boolean hasPinLogin() {
        return false;
    }

    @Override
    public boolean hasReports() {
        return false;
    }

    @Override
    public boolean hasJobAids() {
        return true;
    }

    @Override
    public boolean hasQR() {
        return false;
    }

    @Override
    public boolean hasTasks() {
        return false;
    }

    @Override
    public boolean hasDefaultDueFilterForChildClient() {
        return false;
    }

    public boolean hasJobAidsVitaminAGraph() {
        return true;
    }

    @Override
    public boolean hasJobAidsDewormingGraph() {
        return true;
    }

    @Override
    public boolean hasChildrenMNPSupplementationGraph() {
        return true;
    }

    @Override
    public boolean hasJobAidsBreastfeedingGraph() {
        return true;
    }

    @Override
    public boolean hasJobAidsBirthCertificationGraph() {
        return true;
    }

    @Override
    public boolean hasSurname() {
        return true;
    }

    @Override
    public boolean showMyCommunityActivityReport() {
        return false;
    }

    @Override
    public boolean showChildrenUnder5() {
        return true;
    }

    @Override
    public boolean launchChildClientsAtLogin() {
        return false;
    }

    @Override
    public boolean showNoDueVaccineView() {
        return false;
    }

    @Override
    public boolean useThinkMd() {
        return false;
    }

    @Override
    public boolean hasFamilyLocationRow() {
        return false;
    }

    @Override
    public boolean usesPregnancyRiskProfileLayout() {
        return false;
    }

    @Override
    public boolean getChildFlavorUtil() {
        return false;
    }

    @Override
    public boolean prioritizeChildNameOnChildRegister() {
        return false;
    }

    @Override
    public boolean splitUpcomingServicesView() {
        return false;
    }

    @Override
    public boolean showChildrenUnderFiveAndGirlsAgeNineToEleven() {
        return false;
    }

    @Override
    public boolean dueVaccinesFilterInChildRegister() {
        return false;
    }

    @Override
    public boolean includeCurrentChild() {
        return true;
    }

    @Override
    public boolean saveOnSubmission() {
        return false;
    }

    @Override
    public boolean relaxVisitDateRestrictions() {
        return false;
    }

    @Override
    public boolean showLastNameOnChildProfile() {
        return false;
    }

    @Override
    public boolean showChildrenAboveTwoDueStatus() {
        return true;
    }

    @Override
    public boolean showFamilyServicesScheduleWithChildrenAboveTwo() {
        return true;
    }

    @Override
    public boolean hasForeignData() {
        return false;
    }

    @Override
    public boolean showIconsForChildrenUnderTwoAndGirlsAgeNineToEleven() {
        return false;
    }

    @Override
    public boolean hasMap() {
        return false;
    }
}
