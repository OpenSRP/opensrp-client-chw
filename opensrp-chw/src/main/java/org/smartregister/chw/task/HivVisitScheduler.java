package org.smartregister.chw.task;

import org.smartregister.chw.anc.domain.Visit;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.core.contract.ScheduleTask;
import org.smartregister.chw.core.domain.BaseScheduleTask;
import org.smartregister.chw.core.rule.HivFollowupRule;
import org.smartregister.chw.core.utils.CoreConstants;
import org.smartregister.chw.core.utils.HomeVisitUtil;
import org.smartregister.chw.hiv.dao.HivDao;
import org.smartregister.chw.hiv.domain.HivMemberObject;

import java.util.Date;
import java.util.List;

public class HivVisitScheduler extends BaseTaskExecutor {

    @Override
    public void resetSchedule(String baseEntityID, String scheduleName) {
        super.resetSchedule(baseEntityID, scheduleName);
        ChwApplication.getInstance().getScheduleRepository().deleteScheduleByGroup(getScheduleGroup(), baseEntityID);
    }

    @Override
    public List<ScheduleTask> generateTasks(String baseEntityID, String eventName, Date eventDate) {

        BaseScheduleTask baseScheduleTask = prepareNewTaskObject(baseEntityID);
        HivMemberObject hivMemberObject = HivDao.getMember(baseEntityID);
        Visit lastVisit = HivDao.getLatestVisit(baseEntityID, org.smartregister.chw.hiv.util.Constants.EventType.FOLLOW_UP_VISIT);
        Date lastVisitDate = lastVisit != null ? lastVisit.getDate() : null;
        HivFollowupRule hivFollowupRule = HomeVisitUtil.getHivVisitStatus(lastVisitDate, hivMemberObject.getHivRegistrationDate());

        baseScheduleTask.setScheduleDueDate(hivFollowupRule.getDueDate());
        baseScheduleTask.setScheduleExpiryDate(hivFollowupRule.getExpiryDate());
        baseScheduleTask.setScheduleOverDueDate(hivFollowupRule.getOverDueDate());

        return toScheduleList(baseScheduleTask);
    }

    @Override
    public String getScheduleName() {
        return CoreConstants.SCHEDULE_TYPES.HIV_VISIT;
    }

    @Override
    public String getScheduleGroup() {
        return CoreConstants.SCHEDULE_GROUPS.HOME_VISIT;
    }
}
