/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service;

import java.util.Collections;
import java.util.List;

import com.agnitas.messages.Message;

public class ChangingWorkflowStatusResult {

	private final boolean changed;
	private final boolean anyMailingDeactivated;
    private List<Message> messages = Collections.emptyList();

	public static ChangingWorkflowStatusResult notChanged() {
		return new ChangingWorkflowStatusResult(false, false);
	}

	public static ChangingWorkflowStatusResult success() {
		return new ChangingWorkflowStatusResult(true, false);
	}

	public ChangingWorkflowStatusResult(boolean changed, boolean anyMailingDeactivated) {
		this.changed = changed;
		this.anyMailingDeactivated = anyMailingDeactivated;
	}

    public ChangingWorkflowStatusResult(boolean changed, boolean anyMailingDeactivated, List<Message> messages) {
        this.changed = changed;
        this.anyMailingDeactivated = anyMailingDeactivated;
        this.messages = Collections.unmodifiableList(messages);
    }

	public boolean isAnyMailingDeactivated() {
		return anyMailingDeactivated;
	}

	public boolean isChanged() {
		return changed;
	}

    public List<Message> getMessages() {
        return messages;
    }
}
