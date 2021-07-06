package org.smartregister.chw.util;

public interface ReportingConstants {

    interface PncIndicatorKeysHelper {
        String COUNT_WOMEN_DELIVERED_IN_HF = "pnc_report_indicator_1_1";
        String COUNT_WOMEN_DELIVERED_ELSEWHERE = "pnc_report_indicator_1_2";
        String COUNT_BABIES_NORMAL_BIRTHWEIGHT = "pnc_report_indicator_2_1";
        String COUNT_BABIES_LOW_BIRTHWEIGHT = "pnc_report_indicator_2_2";
        String COUNT_BABIES_INITIATED_EARLY_BREASTFEEDING = "pnc_report_indicator_3_1";
        String COUNT_BABIES_NOT_INITIATED_EARLY_BREASTFEEDING = "pnc_report_indicator_3_2";
        String COUNT_BABIES_RECEIVED_BGC_ON_TIME = "pnc_report_indicator_4_1";
        String COUNT_BABIES_DID_NOT_RECEIVE_BGC_ON_TIME = "pnc_report_indicator_4_2";
        String COUNT_OTHER_WRA_DEATHS = "pnc_report_indicator_5_1";
        String COUNT_MATERNAL_DEATHS = "pnc_report_indicator_5_2";
        String COUNT_WOMEN_WITH_NO_DANGER_SIGNS = "pnc_report_indicator_6_1";
        String COUNT_WOMEN_WITH_DANGER_SIGNS = "pnc_report_indicator_6_2";
        String COUNT_NEWBORNS_WITH_NO_DANGER_SIGNS = "pnc_report_indicator_7_1";
        String COUNT_NEWBORNS_WITH_DANGER_SIGNS = "pnc_report_indicator_7_2";
        String COUNT_POSTPARTUM_FP_METHOD_NONE = "pnc_report_indicator_8_1";
        String COUNT_POSTPARTUM_FP_METHOD_ABSTINENCE = "pnc_report_indicator_8_2";
        String COUNT_POSTPARTUM_FP_METHOD_CONDOM = "pnc_report_indicator_8_3";
        String COUNT_POSTPARTUM_FP_METHOD_TABLETS = "pnc_report_indicator_8_4";
        String COUNT_POSTPARTUM_FP_METHOD_INJECTABLE = "pnc_report_indicator_8_5";
        String COUNT_POSTPARTUM_FP_METHOD_IUD = "pnc_report_indicator_8_6";
        String COUNT_POSTPARTUM_FP_METHOD_IMPLANT = "pnc_report_indicator_8_7";
        String COUNT_POSTPARTUM_FP_METHOD_OTHER = "pnc_report_indicator_8_8";
        String COUNT_DECEASED_NEWBORNS_0_28 = "pnc_report_indicator_9_1";
        String COUNT_WOMAN_PNC_VISIT_DONE = "pnc_report_indicator_10_1";
        String COUNT_WOMAN_PNC_VISIT_NOT_DONE = "pnc_report_indicator_10_2";
    }

    interface AncIndicatorKeys {
        String COUNT_WRA = "anc_report_indicator_1";
        String COUNT_PREGNANT_WOMEN = "anc_report_indicator_2";
        String COUNT_WOMEN_DUE_HOME_VISIT = "anc_report_indicator_3_1";
        String COUNT_WOMEN_OVERDUE_HOME_VISIT = "anc_report_indicator_3_2";
        String COUNT_WOMEN_DUE_HEALTH_FACILITY_VISIT = "anc_report_indicator_4_1";
        String COUNT_WOMEN_OVERDUE_HEALTH_FACILITY_VISIT = "anc_report_indicator_4_2";
        String COUNT_WOMEN_TESTED_HIV = "anc_report_indicator_5_1";
        String COUNT_WOMEN_NOT_TESTED_HIV = "anc_report_indicator_5_2";
        String COUNT_WOMEN_TESTED_SYPHILIS = "anc_report_indicator_6_1";
        String COUNT_WOMEN_NOT_TESTED_SYPHILIS = "anc_report_indicator_6_2";
        String COUNT_WOMEN_DUE_TT_IMMUNIZATION = "anc_report_indicator_7_1";
        String COUNT_WOMEN_OVERDUE_TT_IMMUNIZATION = "anc_report_indicator_7_2";
        String COUNT_WOMEN_DUE_IPTPSP = "anc_report_indicator_8_1";
        String COUNT_WOMEN_OVERDUE_IPTPSP = "anc_report_indicator_8_2";
    }

    interface ChildIndicatorKeys {
        String COUNT_CHILDREN_UNDER_5 = "CHW_001";
        String DECEASED_CHILDREN_0_11_MONTHS = "CHW_002";
        String DECEASED_CHILDREN_12_59_MONTHS = "CHW_003";
        String COUNT_OF_CHILDREN_0_59_WITH_BIRTH_CERT = "CHW_004";
        String COUNT_OF_CHILDREN_0_59_WITH_NO_BIRTH_CERT = "CHW_005";
        String COUNT_OF_CHILDREN_12_59_DEWORMED = "CHW_006";
        String COUNT_OF_CHILDREN_12_59_NOT_DEWORMED = "CHW_007";
        String COUNT_OF_CHILDREN_6_59_VITAMIN_RECEIVED_A = "CHW_008";
        String COUNT_OF_CHILDREN_6_59_VITAMIN_NOT_RECEIVED_A = "CHW_009";
        String COUNT_OF_CHILDREN_0_5_EXCLUSIVELY_BREASTFEEDING = "CHW_010";
        String COUNT_OF_CHILDREN_0_5_NOT_EXCLUSIVELY_BREASTFEEDING = "CHW_011";
        String COUNT_OF_CHILDREN_6_23_UPTO_DATE_MNP = "CHW_012";
        String COUNT_OF_CHILDREN_6_23_OVERDUE_MNP = "CHW_013";
        String COUNT_OF_CHILDREN_0_24_UPTO_DATE_VACCINATIONS = "CHW_014";
        String COUNT_OF_CHILDREN_0_24_OVERDUE_VACCINATIONS = "CHW_015";
    }
}
