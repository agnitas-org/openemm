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

import com.agnitas.emm.core.workflow.beans.WorkflowArchive;
import com.agnitas.emm.core.workflow.beans.WorkflowConnection;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowMailingAware;
import com.agnitas.emm.core.workflow.beans.WorkflowParameter;
import com.agnitas.emm.core.workflow.beans.WorkflowReactionType;
import com.agnitas.emm.core.workflow.beans.WorkflowRecipient;
import com.agnitas.emm.core.workflow.beans.WorkflowStart;
import com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartEventType;
import com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartType;
import com.agnitas.emm.core.workflow.beans.WorkflowStop;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowActionBasedMailingImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowArchiveImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowConnectionImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowDateBasedMailingImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowDecisionImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowMailingImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowParameterImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowRecipientImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowStartImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowStopImpl;

public class ComSampleWorkflowFactory {
	public enum SampleWorkflowType {
		BIRTHDAY("scBirthday"),
		DOI("scDOI"),
		AB_TEST("scABTest");

		private String value;

		SampleWorkflowType(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public static List<WorkflowIcon> createSampleWorkflow(String type, boolean gridEnabled) {
		if (SampleWorkflowType.BIRTHDAY.getValue().equals(type)) {
			return createSampleWorkflowBirthday(gridEnabled);
		}
		if (SampleWorkflowType.DOI.getValue().equals(type)) {
			return createSampleWorkflowDOI(gridEnabled);
		}
		if (SampleWorkflowType.AB_TEST.getValue().equals(type)) {
			return createSampleWorkflowABTest(gridEnabled);
		}
		return null;
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

	private static List<WorkflowIcon> createSampleWorkflowABTest(boolean gridEnabled) {
		WorkflowStart start = new WorkflowStartImpl();
		start.setId(1);
		start.setX(0);
		start.setY(gridEnabled ? 2 : 3);
		start.setStartType(WorkflowStartType.DATE);

		WorkflowRecipient recipient = new WorkflowRecipientImpl();
		recipient.setId(2);
		recipient.setX(gridEnabled ? 2 : 4);
		recipient.setY(gridEnabled ? 2 : 3);

		WorkflowArchive archive = new WorkflowArchiveImpl();
		archive.setId(3);
		archive.setX(gridEnabled ? 4 : 8);
		archive.setY(gridEnabled ? 2 : 3);

		WorkflowParameter parameter1 = new WorkflowParameterImpl();
		parameter1.setId(4);
		parameter1.setX(gridEnabled ? 7 : 12);
		parameter1.setY(0);

		WorkflowParameter parameter2 = new WorkflowParameterImpl();
		parameter2.setId(5);
		parameter2.setX(gridEnabled ? 7 : 12);
		parameter2.setY(gridEnabled ? 4 : 6);

		WorkflowMailingImpl mailing1 = new WorkflowMailingImpl();
		mailing1.setId(6);
		mailing1.setIconTitle("");
		mailing1.setX(gridEnabled ? 9 : 16);
		mailing1.setY(0);

		WorkflowMailingImpl mailing2 = new WorkflowMailingImpl();
		mailing2.setId(7);
		mailing2.setIconTitle("");
		mailing2.setX(gridEnabled ? 9 : 16);
		mailing2.setY(gridEnabled ? 4 : 6);

		WorkflowDecision decision = new WorkflowDecisionImpl();
		decision.setId(8);
        decision.setDecisionType(WorkflowDecision.WorkflowDecisionType.TYPE_AUTO_OPTIMIZATION);
		decision.setX(gridEnabled ? 12 : 20);
		decision.setY(gridEnabled ? 2 : 3);

		WorkflowParameter parameter3 = new WorkflowParameterImpl();
		parameter3.setId(9);
		parameter3.setX(gridEnabled ? 14 : 24);
		parameter3.setY(gridEnabled ? 2 : 3);
        parameter3.setEditable(false);

		WorkflowMailingImpl mailing3 = new WorkflowMailingImpl();
		mailing3.setId(10);
		mailing3.setIconTitle("");
		mailing3.setX(gridEnabled ? 16 : 28);
		mailing3.setY(gridEnabled ? 2 : 3);
		mailing3.setEditable(false);

		WorkflowStopImpl stop = new WorkflowStopImpl();
		stop.setId(11);
        stop.setEndType(WorkflowStop.WorkflowEndType.AUTOMATIC);
		stop.setX(gridEnabled ? 18 : 32);
		stop.setY(gridEnabled ? 2 : 3);

		connect(start, recipient, archive);
		connect(archive, parameter1, mailing1, decision);
		connect(archive, parameter2, mailing2, decision);
		connect(decision, parameter3, mailing3, stop);

		return Arrays.asList(start, recipient, archive, decision, parameter1, parameter2, parameter3, mailing1, mailing2, mailing3, stop);
	}

	private static List<WorkflowIcon> createSampleWorkflowBirthday(boolean gridEnabled) {
		WorkflowStart start = new WorkflowStartImpl();
		start.setId(1);
		start.setX(0);
		start.setY(0);
        start.setStartType(WorkflowStartType.EVENT);
        start.setEvent(WorkflowStartEventType.EVENT_DATE);
        start.setDate(new Date());

		WorkflowRecipientImpl recipient = new WorkflowRecipientImpl();
		recipient.setId(2);
		recipient.setX(gridEnabled ? 3 : 4);
		recipient.setY(0);

		WorkflowMailingAware mailing = new WorkflowDateBasedMailingImpl();
		mailing.setId(3);
		mailing.setIconTitle("");
		mailing.setX(gridEnabled ? 6 : 8);
		mailing.setY(0);

		WorkflowStopImpl stop = new WorkflowStopImpl();
		stop.setId(4);
		stop.setX(gridEnabled ? 9 : 12);
		stop.setY(0);

		return Arrays.asList(connect(start, recipient, mailing, stop));
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
