/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service.util;

import static com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus.STATUS_ACTIVE;
import static com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus.STATUS_PAUSED;
import static org.agnitas.web.forms.WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS;
import static org.agnitas.web.forms.WorkflowParametersHelper.WORKFLOW_FORWARD_TARGET_ITEM_ID;
import static org.agnitas.web.forms.WorkflowParametersHelper.WORKFLOW_ID;
import static org.agnitas.web.forms.WorkflowParametersHelper.WORKFLOW_KEEP_FORWARD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import com.agnitas.emm.core.workflow.beans.Workflow;
import org.agnitas.dao.FollowUpType;
import org.agnitas.target.ConditionalOperator;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType;
import org.agnitas.web.forms.WorkflowParameters;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.agnitas.emm.core.workflow.beans.WorkflowConnection;
import com.agnitas.emm.core.workflow.beans.WorkflowDeadline;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.WorkflowMailingAware;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionType;
import com.agnitas.emm.core.workflow.beans.WorkflowStart;
import com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartEventType;
import com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartType;
import com.agnitas.emm.core.workflow.beans.WorkflowStartStop;

import jakarta.servlet.http.HttpServletRequest;

public class WorkflowUtils {
	public static final int GCD_ACCURACY = 10;

	public static final String WORKFLOW_TARGET_NAME_PATTERN = "[campaign target: %s]";
	public static final String WORKFLOW_TARGET_NAME_SQL_PATTERN = "[campaign target: %]";

	public static final int TESTING_MODE_DEADLINE_DURATION = 15;  // minutes
	public static final Deadline TESTING_MODE_DEADLINE = new Deadline(TimeUnit.MINUTES.toMillis(TESTING_MODE_DEADLINE_DURATION));

	public static Map<ConditionalOperator, String> getOperatorTypeSupportMap() {
		Map<ConditionalOperator, String> map = new HashMap<>();

		map.put(ConditionalOperator.EQ, "*");
		map.put(ConditionalOperator.NEQ, "*");
		map.put(ConditionalOperator.GT, "*");
		map.put(ConditionalOperator.LT, "*");
		map.put(ConditionalOperator.LEQ, "*");
		map.put(ConditionalOperator.GEQ, "*");
		map.put(ConditionalOperator.IS, "*");
		map.put(ConditionalOperator.MOD, DbColumnType.GENERIC_TYPE_INTEGER + "," + DbColumnType.GENERIC_TYPE_FLOAT);
		map.put(ConditionalOperator.LIKE, DbColumnType.GENERIC_TYPE_VARCHAR + "," + DbColumnType.GENERIC_TYPE_VARCHAR);
		map.put(ConditionalOperator.NOT_LIKE, DbColumnType.GENERIC_TYPE_VARCHAR + "," + DbColumnType.GENERIC_TYPE_VARCHAR);
		map.put(ConditionalOperator.CONTAINS, DbColumnType.GENERIC_TYPE_VARCHAR + "," + DbColumnType.GENERIC_TYPE_VARCHAR);
		map.put(ConditionalOperator.NOT_CONTAINS, DbColumnType.GENERIC_TYPE_VARCHAR + "," + DbColumnType.GENERIC_TYPE_VARCHAR);
		map.put(ConditionalOperator.STARTS_WITH, DbColumnType.GENERIC_TYPE_VARCHAR + "," + DbColumnType.GENERIC_TYPE_VARCHAR);
		map.put(ConditionalOperator.NOT_STARTS_WITH, DbColumnType.GENERIC_TYPE_VARCHAR + "," + DbColumnType.GENERIC_TYPE_VARCHAR);

		return Collections.unmodifiableMap(map);
	}

	public static Double calculateGCD(List<Double> parts) {
		Iterator<Double> iterator = parts.iterator();
		Double value;

		if (iterator.hasNext()) {
			value = iterator.next();
		} else {
			return null;
		}

		while (iterator.hasNext()) {
			value = calculateGCD(value, iterator.next());
		}

		return value;
	}

	public static double roundGCD(double value) {
		return (double) Math.round(value * GCD_ACCURACY) / GCD_ACCURACY;
	}

	private static double calculateGCD(double a, double b) {
		if (a == b) {
			return roundGCD(a);
		}

		if (a < b) {
			if (a * GCD_ACCURACY < 1) {
				return roundGCD(b);
			}
			return calculateGCD(a, b % a);
		} else { // b < a
			if (b * GCD_ACCURACY < 1) {
				return roundGCD(a);
			}
			return calculateGCD(b, a % b);
		}
	}

	public static boolean is(WorkflowStart start, WorkflowStartType type) {
		Objects.requireNonNull(type);

		return start.isFilled() && start.getStartType() == type;
	}

	public static boolean is(WorkflowStart start, WorkflowStartEventType event) {
		Objects.requireNonNull(event);

		return is(start, WorkflowStartType.EVENT) && start.getEvent() == event;
	}

	public static boolean is(WorkflowStart start, WorkflowReactionType reaction) {
		Objects.requireNonNull(reaction);

		return is(start, WorkflowStartEventType.EVENT_REACTION) && start.getReaction() == reaction;
	}

	public static double doubleTo2Digits(Double part) {
		BigDecimal bd = new BigDecimal(part);
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static Date getStartStopIconDate(WorkflowStartStop icon) {
		return getStartStopIconDate(icon, TimeZone.getTimeZone(icon.getAdminTimezone()));
	}

	public static Date getStartStopIconDate(WorkflowStartStop icon, TimeZone timeZone) {
		Date date = icon.getDate();

		if (date == null) {
			return null;
		} else {
			return mergeIconDateAndTime(date, icon.getHour(), icon.getMinute(), timeZone);
		}
	}

	public static Date mergeIconDateAndTime(Date date, int hour, int minute) {
		return mergeIconDateAndTime(date, hour, minute, TimeZone.getDefault());
	}

	public static Date mergeIconDateAndTime(Date date, int hour, int minute, TimeZone timeZone) {
		if (date == null) {
			return null;
		}

		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalTime localTime = LocalTime.of(hour, minute);
		LocalDateTime localDateTime = DateUtilities.merge(localDate, localTime);
		return DateUtilities.toDate(localDateTime, TimeZone.getTimeZone(timeZone.getID()).toZoneId());
	}

	public static Date getReminderSpecificDate(WorkflowStartStop icon, TimeZone timezone) {
		Date date = icon.getRemindDate();

		if (date == null || !icon.isRemindSpecificDate()) {
			return null;
		} else {
			return mergeIconDateAndTime(date, icon.getHour(), icon.getMinute(), timezone);
		}
	}

	public static int getMailingId(WorkflowIcon icon) {
		if (icon instanceof  WorkflowMailingAware) {
			return ((WorkflowMailingAware) icon).getMailingId();
		} else {
			return 0;
		}
	}

    public static boolean isMailingIcon(WorkflowIcon icon) {
        return icon.getType() == WorkflowIconType.ACTION_BASED_MAILING.getId() ||
                icon.getType() == WorkflowIconType.MAILING.getId() ||
                icon.getType() == WorkflowIconType.DATE_BASED_MAILING.getId() ||
                icon.getType() == WorkflowIconType.FOLLOWUP_MAILING.getId() ||
				icon.getType() == WorkflowIconType.MAILING_MEDIATYPE_POST.getId() ||
				icon.getType() == WorkflowIconType.MAILING_MEDIATYPE_SMS.getId();
    }

    public static boolean isStartStopIcon(WorkflowIcon icon) {
        return icon.getType() == WorkflowIconType.START.getId() || icon.getType() == WorkflowIconType.STOP.getId();
    }

    public static boolean isStopIcon(WorkflowIcon icon) {
        return icon.getType() == WorkflowIconType.STOP.getId();
    }

	public static boolean isAutoOptimizationIcon(WorkflowIcon icon) {
		if (icon.getType() == WorkflowIconType.DECISION.getId()) {
			WorkflowDecision decision = (WorkflowDecision) icon;
			return decision.getDecisionType() == WorkflowDecision.WorkflowDecisionType.TYPE_AUTO_OPTIMIZATION;
		}
		return false;
	}

	public static boolean isBranchingDecisionIcon(WorkflowIcon icon) {
		if (icon.getType() == WorkflowIconType.DECISION.getId()) {
			WorkflowDecision decision = (WorkflowDecision) icon;
			return decision.getDecisionType() == WorkflowDecision.WorkflowDecisionType.TYPE_DECISION;
		}
		return false;
	}

	public static boolean isReactionCriteriaDecision(WorkflowDecision decision) {
		return decision.getDecisionCriteria() == WorkflowDecision.WorkflowDecisionCriteria.DECISION_REACTION;
	}

	public static boolean isProfileFieldCriteriaDecision(WorkflowDecision decision) {
		return decision.getDecisionCriteria() == WorkflowDecision.WorkflowDecisionCriteria.DECISION_PROFILE_FIELD;
	}

	public static Deadline resolveDeadline(WorkflowDeadline deadline, TimeZone timezone, boolean testing) {
		if (testing) {
			return TESTING_MODE_DEADLINE;
		} else {
			return WorkflowUtils.asDeadline(deadline, timezone);
		}
	}

	public static Deadline asDeadline(WorkflowStart start, TimeZone timezone) {
		return asDeadline(start.getDate(), start.getHour(), start.getMinute(), timezone);
	}

	public static Deadline asDeadline(WorkflowDeadline deadline, TimeZone timezone) {
		switch (deadline.getDeadlineType()) {
			case TYPE_DELAY:
				return asDelayDeadline(deadline);

			case TYPE_FIXED_DEADLINE:
				return asDeadline(deadline.getDate(), deadline.getHour(), deadline.getMinute(), timezone);

			default:
				throw new UnsupportedOperationException("Unsupported deadline type");
		}
	}

	public static String getFollowUpMethod(WorkflowReactionType reactionType) {
		if (reactionType == null) {
			return null;
		}

		switch (reactionType) {
			case CLICKED:
				return FollowUpType.TYPE_FOLLOWUP_CLICKER.getKey();

			case NOT_CLICKED:
				return FollowUpType.TYPE_FOLLOWUP_NON_CLICKER.getKey();

			case OPENED:
				return FollowUpType.TYPE_FOLLOWUP_OPENER.getKey();

			case NOT_OPENED:
				return FollowUpType.TYPE_FOLLOWUP_NON_OPENER.getKey();

			// FIXME: Not supported as followup methods.
			case BOUGHT:
			case NOT_BOUGHT:
			default:
				return null;
		}
	}

	public static void forEachConnection(List<WorkflowIcon> icons, BiConsumer<Integer, Integer> consumer) {
		for (WorkflowIcon icon : icons) {
			List<WorkflowConnection> connections = icon.getConnections();
			if (connections != null) {
				for (WorkflowConnection connection : connections) {
					consumer.accept(icon.getId(), connection.getTargetIconId());
				}
			}
		}
	}

	private static Deadline asDelayDeadline(WorkflowDeadline deadline) {
		int value = deadline.getDelayValue();
		switch (deadline.getTimeUnit()) {
			case TIME_UNIT_MINUTE:
				return new Deadline(TimeUnit.MINUTES.toMillis(value));
			case TIME_UNIT_HOUR:
				return new Deadline(TimeUnit.HOURS.toMillis(value));
			case TIME_UNIT_DAY:
				if (deadline.isUseTime()) {
					return new Deadline(TimeUnit.DAYS.toMillis(value), deadline.getHour(), deadline.getMinute());
				} else {
					return new Deadline(TimeUnit.DAYS.toMillis(value));
				}
			case TIME_UNIT_WEEK:
				return new Deadline(TimeUnit.DAYS.toMillis(value * 7));
			case TIME_UNIT_MONTH:
				return new Deadline(TimeUnit.DAYS.toMillis(value * 30));
			default:
				throw new UnsupportedOperationException("Unsupported relative deadline time unit");
		}
	}

	private static Deadline asDeadline(Date date, int hours, int minutes, TimeZone timezone) {
		return new Deadline(WorkflowUtils.mergeIconDateAndTime(date, hours, minutes, timezone));
	}

	public static void updateForwardParameters(HttpServletRequest request) {
		updateForwardParameters(request, false);
	}

	public static void updateForwardParameters(HttpServletRequest request, boolean checkKeepForward) {
		WorkflowParameters workflowParameters = new WorkflowParameters();
		if (checkKeepForward) {
			if (Boolean.valueOf(request.getParameter(WORKFLOW_KEEP_FORWARD))) {
				return;
			}
		}

		String targetItemId = request.getParameter(WORKFLOW_FORWARD_TARGET_ITEM_ID);
        workflowParameters.setWorkflowForwardTargetItemId(NumberUtils.toInt(targetItemId));

        String workflowId = request.getParameter(WORKFLOW_ID);
        workflowParameters.setWorkflowId(NumberUtils.toInt(workflowId));

        String forwardParams = request.getParameter(WORKFLOW_FORWARD_PARAMS);
        workflowParameters.setWorkflowForwardParams(StringUtils.trimToEmpty(forwardParams));

		AgnUtils.saveWorkflowForwardParamsToSession(request, workflowParameters, true);
	}

	public static boolean isAutoOptWorkflow(List<WorkflowIcon> workflowIcons) {
		for (WorkflowIcon icon: workflowIcons) {
			if (WorkflowUtils.isAutoOptimizationIcon(icon)) {
				return true;
			}
		}
		return false;
	}

	public static final class Deadline {
		public static boolean equals(Deadline d1, Deadline d2) {
			if (d1 == d2) {
				return true;
			}
			if (d1 == null || d2 == null) {
				return false;
			}
			return d1.equals(d2);
		}

		public static Date toDate(Date base, Deadline deadline, TimeZone timezone) {
			if (deadline.isRelative()) {
				return toDate(base, deadline.getValue(), deadline.getHours(), deadline.getMinutes(), timezone);
			} else {
				return new Date(deadline.getValue());
			}
		}

		private static Date toDate(Date base, long ms, int hours, int minutes, TimeZone timezone) {
			Calendar calendar = Calendar.getInstance(timezone);
			calendar.setTimeInMillis(base.getTime() + ms);

			if (hours != -1) {
				calendar.set(Calendar.HOUR_OF_DAY, hours);
			}

			if (minutes != -1) {
				calendar.set(Calendar.MINUTE, minutes);
			}

			return calendar.getTime();
		}

		private boolean isRelative;
		private long ms;
		private int hours;
		private int minutes;

		public Deadline() {
			this(true, 0, -1, -1);
		}

		public Deadline(long relativeDeadline) {
			this(true, relativeDeadline, -1, -1);
		}

		public Deadline(long relativeDeadline, int hours, int minutes) {
			this(true, relativeDeadline, hours, minutes);
		}

		public Deadline(Date deadline) {
			this(false, deadline.getTime(), -1, -1);
		}

		public Deadline(Deadline deadline) {
			this(deadline.isRelative(), deadline.getValue(), deadline.getHours(), deadline.getMinutes());
		}

		private Deadline(boolean isRelative, long ms, int hours, int minutes) {
			this.isRelative = isRelative;
			this.ms = ms;

			if (isRelative) {
				this.hours = (hours >= 0 && hours < 24) ? hours : -1;
				this.minutes = (minutes >= 0 && minutes < 60) ? minutes : -1;
			} else {
				this.hours = -1;
				this.minutes = -1;
			}
		}

		public boolean isRelative() {
			return isRelative;
		}

		public long getValue() {
			return ms;
		}

		public int getHours() {
			return hours;
		}

		public int getMinutes() {
			return minutes;
		}

		public Deadline add(Deadline deadline) {
			if (deadline.isRelative()) {
				int deadlineHours = deadline.getHours();
				int deadlineMinutes = deadline.getMinutes();

				if (deadlineHours == -1 && deadlineMinutes == -1) {
					deadlineHours = this.hours;
					deadlineMinutes = this.minutes;
				}

				return new Deadline(isRelative, ms + deadline.getValue(), deadlineHours, deadlineMinutes);
			} else if (isRelative) {
				return new Deadline(deadline);
			} else {
				return new Deadline(false, Math.max(ms, deadline.getValue()), -1, -1);
			}
		}

		@Override
		public int hashCode() {
			return (Boolean.toString(isRelative) + ms + "@" + hours + "@" + minutes).hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null) {
				return false;
			}

			if (Deadline.class == o.getClass()) {
				Deadline deadline = (Deadline) o;
				if (isRelative == deadline.isRelative() && ms == deadline.getValue()) {
					return hours == deadline.getHours() && minutes == deadline.getMinutes();
				}
			}

			return false;
		}
	}

	public enum StartType {
		// Start icon is absent or a start type is invalid
		UNKNOWN,
		// Normal and follow-up mailings
		REGULAR,
		// Action-based mailings
		REACTION,
		// Date-based (rule-based) mailings
		RULE;

		public static StartType of(WorkflowStart start) {
			if (start.getStartType() == null) {
				return UNKNOWN;
			}

			switch (start.getStartType()) {
			case DATE:
				return REGULAR;

			case EVENT:
				switch (start.getEvent()) {
				case EVENT_REACTION:
					return REACTION;

				case EVENT_DATE:
					return RULE;

				default:
					return UNKNOWN;
				}

				//$FALL-THROUGH$ - should never happen
			default:
				return UNKNOWN;
			}
		}
	}
	
    public static String getWorkflowDescription(Workflow workflow) {
        return workflow.getShortname() + " (" + workflow.getWorkflowId() + ")";
    }

    public static boolean isPausing(Workflow.WorkflowStatus oldStatus, Workflow.WorkflowStatus newStatus) {
        return oldStatus == STATUS_ACTIVE && newStatus == STATUS_PAUSED;
    }

    public static boolean isUnpausing(Workflow.WorkflowStatus oldStatus, Workflow.WorkflowStatus newStatus) {
        return oldStatus == STATUS_PAUSED && newStatus == STATUS_ACTIVE;
    }

    public static boolean isStoppingOnPause(Workflow.WorkflowStatus oldStatus, Workflow.WorkflowStatus newStatus) {
        return oldStatus == STATUS_PAUSED && newStatus == Workflow.WorkflowStatus.STATUS_INACTIVE;
    }

    public static boolean isDuringPause(Workflow.WorkflowStatus oldStatus, Workflow.WorkflowStatus newStatus) {
        return oldStatus == STATUS_PAUSED && newStatus == STATUS_PAUSED;
    }
}
