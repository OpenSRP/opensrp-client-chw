package org.smartregister.chw.interactor;

import android.content.Context;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.AncLibrary;
import org.smartregister.chw.anc.domain.MemberObject;
import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.anc.model.BaseUpcomingService;
import org.smartregister.chw.anc.util.VisitUtils;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.RecurringServiceUtil;
import org.smartregister.chw.core.utils.VisitVaccineUtil;
import org.smartregister.chw.util.Constants;
import org.smartregister.chw.util.ContactUtil;
import org.smartregister.domain.Alert;
import org.smartregister.immunization.domain.ServiceWrapper;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public abstract class DefaultAncUpcomingServicesInteractorFlv implements AncUpcomingServicesInteractor.Flavor {

    @Override
    public List<BaseUpcomingService> getMemberServices(Context context, MemberObject memberObject) {
        List<BaseUpcomingService> services = new ArrayList<>();

        Date createDate = null;
        try {
            createDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(memberObject.getDateCreated());
        } catch (ParseException e) {
            Timber.e(e);
        }
        if (createDate == null) {
            return services;
        }

        // anc card
        evaluateANCCard(services, memberObject, context, createDate);
        evaluateHealthFacility(services, memberObject, context);
        evaluateTT(services, memberObject, context);
        evaluateIPTP(services, memberObject, context);

        return services;
    }

    protected void evaluateANCCard(List<BaseUpcomingService> services, MemberObject memberObject, Context context, Date createDate) {
        if (memberObject.getHasAncCard() != null && !memberObject.getHasAncCard().equalsIgnoreCase("Yes")) {
            BaseUpcomingService cardService = new BaseUpcomingService();
            cardService.setServiceName(context.getString(R.string.anc_card));
            cardService.setServiceDate(createDate);
            services.add(cardService);
        }
    }

    protected void evaluateHealthFacility(List<BaseUpcomingService> services, MemberObject memberObject, Context context) {
        // hfv
        LocalDate dateCreated = new DateTime(memberObject.getDateCreated()).toLocalDate();
        List<LocalDate> dateList = new ArrayList<>(ContactUtil.getContactSchedule(memberObject, dateCreated).values());
        if (dateList.size() > 0) {
            BaseUpcomingService healthFacilityService = new BaseUpcomingService();
            healthFacilityService.setServiceName(MessageFormat.format(context.getString(R.string.health_facility_visit_num), (memberObject.getConfirmedContacts() + 1)));
            healthFacilityService.setServiceDate(dateList.get(0).toDate());
            services.add(healthFacilityService);
        }
    }

    protected void evaluateTT(List<BaseUpcomingService> services, MemberObject memberObject, Context context) {
        Alert alert = getNextTT(memberObject);
        if (alert != null) {
            BaseUpcomingService ttService = new BaseUpcomingService();
            ttService.setServiceName(MessageFormat.format(context.getString(R.string.tt_dose), VisitVaccineUtil.getAlertIteration(alert)));
            ttService.setServiceDate(new DateTime(alert.startDate()).toDate());
            services.add(ttService);
        }
    }

    protected void evaluateIPTP(List<BaseUpcomingService> services, MemberObject memberObject, Context context) {
        Pair<String, Date> iptp = getNextIPTP(memberObject);
        if (iptp != null) {
            BaseUpcomingService iptpService = new BaseUpcomingService();
            iptpService.setServiceName(MessageFormat.format(context.getString(R.string.iptp_sp_dose), iptp.getLeft()));
            iptpService.setServiceDate(iptp.getRight());
            services.add(iptpService);
        }
    }

    private Alert getNextTT(MemberObject memberObject) {
        DateTime lastMenstrualPeriod = DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(memberObject.getLastMenstrualPeriod());
        int ga = Days.daysBetween(lastMenstrualPeriod, new DateTime()).getDays() / 7;

        if (ga >= 13) {
            List<Alert> alerts = VisitVaccineUtil.getNextVaccines(memberObject.getBaseEntityId(), lastMenstrualPeriod, CoreConstants.SERVICE_GROUPS.WOMAN, true);
            Map<String, List<Alert>> map = VisitVaccineUtil.groupByType(alerts);
            List<Alert> res = map.get("TT");
            if (res != null && !res.isEmpty())
                return res.get(0);
        }

        return null;
    }

    private Pair<String, Date> getNextIPTP(MemberObject memberObject) {
        DateTime lmp = DateTimeFormat.forPattern("dd-MM-yyyy").parseDateTime(memberObject.getLastMenstrualPeriod());

        Map<String, List<ServiceWrapper>> nextWrappers = RecurringServiceUtil.getNextWrappers(memberObject.getBaseEntityId(), lmp, CoreConstants.SERVICE_GROUPS.WOMAN, true);
        if (nextWrappers == null) return null;

        List<ServiceWrapper> wrappers = nextWrappers.get("IPTp-SP");
        if (wrappers == null || nextWrappers.isEmpty()) return null;

        ServiceWrapper serviceWrapper = wrappers.get(0);
        if (serviceWrapper == null) return null;


        String iteration = serviceWrapper.getName().substring(serviceWrapper.getName().length() - 1);
        Visit latestVisit = AncLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EventType.IPTP_SP);
        Map<String, List<VisitDetail>> visitDetails = VisitUtils.getVisitGroups(AncLibrary.getInstance().visitDetailsRepository().getVisits(latestVisit.getVisitId()));
        Object firstKey = visitDetails.keySet().toArray()[0];
        DateTime lastVisitDate = DateTime.parse(visitDetails.get(firstKey).get(0).getDetails());
        if (latestVisit != null && latestVisit.getUpdatedAt() != null)
            return Pair.of(iteration, lastVisitDate.plusMonths(1).toDate());

        return Pair.of(iteration, serviceWrapper.getVaccineDate().toDate());
    }
}