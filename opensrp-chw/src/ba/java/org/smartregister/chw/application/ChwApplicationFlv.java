package org.smartregister.chw.application;

public class ChwApplicationFlv extends DefaultChwApplicationFlv {
    @Override
    public boolean hasP2P() {
        return false;
    }

    @Override
    public boolean hasReferrals() {
        return true;
    }

    @Override
    public boolean hasOPD() {
        return true;
    }

    @Override
    public boolean flvSetFamilyLocation(){
        return true;
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
        return true;
    }

    @Override
    public boolean hasFamilyPlanning() {
        return true;
    }

    @Override
    public boolean hasWashCheck() {
        return false;
    }

    @Override
    public boolean hasMalaria() {
        return true;
    }

    @Override
    public boolean hasServiceReport() {
        return true;
    }

    public boolean hasQR() {
        return true;
    }

    @Override
    public boolean hasJobAids() {
        return false;
    }

    @Override
    public boolean hasTasks() {
        return true;
    }

    @Override
    public boolean hasStockUsageReport() {
        return true;
    }

    @Override
    public boolean hasFamilyLocationRow() {
        return true;
    }

    @Override
    public boolean usesPregnancyRiskProfileLayout() {
        return true;
    }

    public boolean getChildFlavorUtil(){
        return true;
    }

    @Override
    public boolean includeCurrentChild(){
        return true;
    }

    @Override
    public boolean hasMap(){
        return true;
    }
}
