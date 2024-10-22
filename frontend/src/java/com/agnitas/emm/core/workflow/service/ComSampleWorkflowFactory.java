/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import com.agnitas.emm.core.workflow.beans.WorkflowConnection;
import com.agnitas.emm.core.workflow.beans.WorkflowDeadline;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.WorkflowMailingAware;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionType;
import com.agnitas.emm.core.workflow.beans.WorkflowRecipient;
import com.agnitas.emm.core.workflow.beans.WorkflowStart;
import com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartEventType;
import com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartType;
import com.agnitas.emm.core.workflow.beans.WorkflowStop;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowActionBasedMailingImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowConnectionImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowRecipientImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowStartImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowStopImpl;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.messages.I18nString;

public class ComSampleWorkflowFactory {

	public enum SampleWorkflowType {
        AB_TEST("scABTest", "mailing.autooptimization"),
        DOI("scDOI", "workflow.icon.DOI"),
        BIRTHDAY("scBirthday", "workflow.icon.birthday"),
        BIRTHDAY_WITH_COUPON("birthdayWithCoupon", "workflow.icon.birthday.coupon"),
        WELCOME_TRACK("welcomeTrack", "workflow.icon.welcome"),
        WELCOME_TRACK_WITH_INCENTIVE("welcomeTrackWithIncentive", "workflow.icon.welcome.incentive"),
        SHOPPING_CART_ABANDONERS_SMALL("shoppingCartAbandonersSmall", "workflow.icon.sca.small"),
        SHOPPING_CART_ABANDONERS_LARGE("shoppingCartAbandonersLarge", "workflow.icon.sca.large");

		private final String value;
		private final String message;

		SampleWorkflowType(String value, String message) {
			this.value = value;
            this.message = message;
        }

        public static SampleWorkflowType from(String value) {
            for (SampleWorkflowType enumConstant : SampleWorkflowType.values()) {
                if (enumConstant.getValue().equals(value)) {
                    return enumConstant;
                }
            }
            throw new IllegalArgumentException("No SampleWorkflowType constant with value " + value);
        }

		public String getValue() {
			return value;
		}

        public String getMessage() {
            return message;
        }
    }

	public static List<WorkflowIcon> createSampleWorkflow(SampleWorkflowType type, boolean gridEnabled) {
        switch (type) {
            case DOI:
                return createSampleWorkflowDOI(gridEnabled);
            case BIRTHDAY_WITH_COUPON:
            case SHOPPING_CART_ABANDONERS_LARGE:
                return createBirthdayWithCouponSample(gridEnabled);
            case WELCOME_TRACK:
                return createWelcomeTrackSample(gridEnabled);
            case WELCOME_TRACK_WITH_INCENTIVE:
                return createWelcomeTrackWithIncentiveSample(gridEnabled);
            case BIRTHDAY:
            case SHOPPING_CART_ABANDONERS_SMALL:
                return createSampleWorkflowBirthday(gridEnabled);
            default:
                return null;
        }
	}

	private static List<WorkflowIcon> createSampleWorkflowDOI(boolean gridEnabled) {
		WorkflowStart start = new WorkflowStartImpl();
		start.setId(1);
		start.setX(0);
		start.setY(0);
        start.setStartType(WorkflowStartType.EVENT);
        start.setEvent(WorkflowStartEventType.EVENT_REACTION);
        start.setReaction(WorkflowReactionType.WAITING_FOR_CONFIRM);

        WorkflowRecipient recipient = new WorkflowRecipientImpl();
        recipient.setId(2);
        recipient.setX(gridEnabled ? 3 : 4);
        recipient.setY(0);

        WorkflowMailingAware actionMailing = new WorkflowActionBasedMailingImpl();
        actionMailing.setId(3);
        actionMailing.setIconTitle("");
        actionMailing.setX(gridEnabled ? 6 : 8);
        actionMailing.setY(0);

        WorkflowStopImpl stop = new WorkflowStopImpl();
        stop.setEndType(WorkflowStop.WorkflowEndType.AUTOMATIC);
        stop.setId(4);
        stop.setX(gridEnabled ? 9 : 12);
        stop.setY(0);

        return Arrays.asList(connect(start, recipient, actionMailing, stop));
	}

    public static List<WorkflowIcon> autoOptWorkflowSample(int mailingsCount, boolean gridEnabled, Locale locale) {
        int centerRowY = gridEnabled ? (mailingsCount - 1) : ((mailingsCount - 1) * 3);

        WorkflowIcon start = getIcon(WorkflowIconType.START, 0, centerRowY);
        WorkflowIcon recipient = getIcon(WorkflowIconType.RECIPIENT, gridEnabled ? 2 : 4, centerRowY);
        WorkflowIcon archive = getIcon(WorkflowIconType.ARCHIVE, gridEnabled ? 4 : 8, centerRowY);
        WorkflowIcon decision = getIcon(WorkflowIconType.DECISION, gridEnabled ? 10 : 20, centerRowY);
        WorkflowIcon finalParameter = getIcon(WorkflowIconType.PARAMETER, gridEnabled ? 12 : 24, centerRowY);
        WorkflowIcon finalMailing = getIcon(WorkflowIconType.MAILING, gridEnabled ? 14 : 28, centerRowY);
        WorkflowIcon stop = getIcon(WorkflowIconType.STOP, gridEnabled ? 16 : 32, centerRowY);
        List<WorkflowIcon> parameterIcons = getAutoOptTestIcon(WorkflowIconType.PARAMETER, mailingsCount, gridEnabled, gridEnabled ? 6 : 12);
        List<WorkflowIcon> mailingIcons = getAutoOptTestIcon(WorkflowIconType.MAILING, mailingsCount, gridEnabled, gridEnabled ? 8 : 16);
        
        ((WorkflowStart) start).setStartType(WorkflowStartType.DATE);
        ((WorkflowStopImpl) stop).setEndType(WorkflowStop.WorkflowEndType.AUTOMATIC);
        ((WorkflowDecision) decision).setDecisionType(WorkflowDecision.WorkflowDecisionType.TYPE_AUTO_OPTIMIZATION);
        finalMailing.setIconTitle(I18nString.getLocaleString("resultMailing", locale));
        finalMailing.setEditable(false);
        finalParameter.setEditable(false);
        mailingIcons.forEach(mailingIcon -> mailingIcon.setIconTitle(""));

        List<WorkflowIcon> icons = new ArrayList<>(7 + mailingsCount * 2);
        icons.addAll(List.of(start, recipient, archive));
        icons.addAll(parameterIcons);
        icons.addAll(mailingIcons);
        icons.addAll(List.of(decision, finalParameter, finalMailing,  stop));

        IntStream.range(0, icons.size()).forEach(i -> icons.get(i).setId(i + 1));
        
		connect(start, recipient, archive);
        for (int i = 0; i < mailingsCount; i++) {
            connect(archive, parameterIcons.get(i), mailingIcons.get(i), decision);
        }
		connect(decision, finalParameter, finalMailing, stop);
        return icons;
    }

    private static WorkflowIcon getIcon(WorkflowIconType type, int x, int y) {
        return getIcon(type, x, y, null);
    }

    private static WorkflowIcon getIcon(WorkflowIconType type, int x, int y, Integer id) {
        WorkflowIcon icon = WorkflowUtils.getEmptyIcon(type);
        icon.setX(x);
        icon.setY(y);
        if (id != null) {
            icon.setId(id);
        }
        return icon;
    }
    
    private static List<WorkflowIcon> getAutoOptTestIcon(WorkflowIconType type, int count, boolean gridEnabled, int x) {
        List<WorkflowIcon> icons = new ArrayList<>(count);
        int y = 0;
        for (int i = 0; i < count; i++) {
            WorkflowIcon parameter = getIcon(type, x, y);
            icons.add(parameter);
            y += gridEnabled ? 2 : 6;
        }
        return icons;
    }

	private static List<WorkflowIcon> createSampleWorkflowBirthday(boolean gridEnabled) {
        WorkflowStart start = getEventDateStartIcon();
        WorkflowIcon recipient = getIcon(WorkflowIconType.RECIPIENT, gridEnabled ? 3 : 4, 0, 2);
        WorkflowIcon mailing = getIcon(WorkflowIconType.DATE_BASED_MAILING, gridEnabled ? 6 : 8, 0, 3);
        WorkflowIcon stop = getIcon(WorkflowIconType.STOP, gridEnabled ? 9 : 12, 0, 4);

		return Arrays.asList(connect(start, recipient, mailing, stop));
	}

    private static List<WorkflowIcon> createBirthdayWithCouponSample(boolean gridEnabled) {
        WorkflowStart start = getEventDateStartIcon();
        return getDefaultDecisionChain(gridEnabled, start);
    }

    private static WorkflowDecision getDecisionBoughtIcon(boolean gridEnabled) {
        WorkflowDecision decision = (WorkflowDecision) getIcon(WorkflowIconType.DECISION, gridEnabled ? 12 : 16, 0, 5);
        decision.setDecisionType(WorkflowDecision.WorkflowDecisionType.TYPE_DECISION);
        decision.setDecisionCriteria(WorkflowDecision.WorkflowDecisionCriteria.DECISION_REACTION);
        decision.setReaction(WorkflowReactionType.BOUGHT);
        return decision;
    }

    private static WorkflowStart getEventDateStartIcon() {
        WorkflowStart start = (WorkflowStart) getIcon(WorkflowIconType.START, 0, 0, 1);
        start.setStartType(WorkflowStartType.EVENT);
        start.setEvent(WorkflowStartEventType.EVENT_DATE);
        start.setDate(new Date());
        return start;
    }

    private static WorkflowDeadline get7daysDeadlineIcon(boolean gridEnabled) {
        WorkflowDeadline deadline = (WorkflowDeadline) getIcon(WorkflowIconType.DEADLINE, gridEnabled ? 9 : 12, 0, 4);
        deadline.setDeadlineType(WorkflowDeadline.WorkflowDeadlineType.TYPE_DELAY);
        deadline.setTimeUnit(WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_DAY);
        deadline.setDelayValue(7);
        return deadline;
    }

    private static List<WorkflowIcon> createWelcomeTrackSample(boolean gridEnabled) {
        WorkflowStart start = (WorkflowStart) getIcon(WorkflowIconType.START, 0, 0, 1);
        start.setStartType(WorkflowStartType.EVENT);
        start.setEvent(WorkflowStartEventType.EVENT_REACTION);
        start.setDate(new Date());

        WorkflowIcon recipient = getIcon(WorkflowIconType.RECIPIENT, gridEnabled ? 3 : 4, 0, 2);

        WorkflowIcon mailing = getIcon(WorkflowIconType.DATE_BASED_MAILING, gridEnabled ? 6 : 8, 0, 3);
        mailing.setIconTitle("");

        WorkflowIcon stop = getIcon(WorkflowIconType.STOP, gridEnabled ? 9 : 12, 0, 4);

        return Arrays.asList(connect(start, recipient, mailing, stop));
    }

    private static List<WorkflowIcon> createWelcomeTrackWithIncentiveSample(boolean gridEnabled) {
        WorkflowStart start = (WorkflowStart) getIcon(WorkflowIconType.START, 0, 0, 1);
        start.setStartType(WorkflowStartType.EVENT);
        start.setEvent(WorkflowStartEventType.EVENT_REACTION);
        return getDefaultDecisionChain(gridEnabled, start);
    }

    private static List<WorkflowIcon> getDefaultDecisionChain(boolean gridEnabled, WorkflowStart start) {
        WorkflowIcon recipient = getIcon(WorkflowIconType.RECIPIENT, gridEnabled ? 3 : 4, 0, 2);
        WorkflowIcon mailing = getIcon(WorkflowIconType.DATE_BASED_MAILING, gridEnabled ? 6 : 8, 0, 3);
        WorkflowIcon deadline = get7daysDeadlineIcon(gridEnabled);
        WorkflowIcon decision = getDecisionBoughtIcon(gridEnabled);
        WorkflowIcon reminderMailing = getIcon(WorkflowIconType.DATE_BASED_MAILING, gridEnabled ? 12 : 16, gridEnabled ? 2 : 6, 7);
        WorkflowIcon stop = getIcon(WorkflowIconType.STOP, gridEnabled ? 15 : 20, 0, 6);

        connect(start, recipient, mailing, deadline, decision, stop);
        connect(decision, reminderMailing, stop);
        return List.of(start, recipient, mailing, deadline, decision, reminderMailing, stop);
    }

    private static WorkflowIcon[] connect(WorkflowIcon... icons) {
		for (int i = 0; i < icons.length - 1; i++) {
			connectIcons(icons[i], icons[i + 1]);
		}

		return icons;
	}

	private static void connectIcons(WorkflowIcon source, WorkflowIcon destination) {
		List<WorkflowConnection> connections = source.getConnections();

		if (connections == null) {
			connections = new ArrayList<>();
			source.setConnections(connections);
		}

		connections.add(new WorkflowConnectionImpl(destination.getId()));
	}
}
