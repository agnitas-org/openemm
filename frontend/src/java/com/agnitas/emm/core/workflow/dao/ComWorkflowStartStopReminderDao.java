/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.dao;

import java.util.List;


import com.agnitas.beans.IntEnum;
import com.agnitas.emm.core.workflow.beans.WorkflowReminder;

public interface ComWorkflowStartStopReminderDao {
    enum ReminderType implements IntEnum {
        MISSING_START(0),
        START(1),
        STOP(2);

        private int id;

        public static ReminderType fromId(int id) {
            return IntEnum.fromId(ReminderType.class, id);
        }

        public static ReminderType fromId(int id, boolean safe) {
            return IntEnum.fromId(ReminderType.class, id, safe);
        }

        ReminderType(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }

    /**
     * Delete existing reminders associated with {@code workflowId} (if any).
     *
     * @param companyId an identifier of a company that owns a referenced workflow.
     * @param workflowId an identifier of a workflow to delete reminders for.
     */
    void deleteReminders(int companyId, int workflowId);

    /**
     * Delete all the existing reminders that belong to a company referenced by {@code companyId}.
     *
     * @param companyId an identifier of a company to delete reminders for.
     */
    void deleteReminders(int companyId);

    /**
     * Delete all the existing reminders recipients that belong to a company referenced by {@code companyId}.
     *
     * @param companyId an identifier of a company to delete reminders recipients for.
     */
    void deleteRecipients(int companyId);

    /**
     * Delete existing reminders associated with {@code workflowId} (if any) and store new ones represented by {@code reminders}.
     *
     * @param companyId an identifier of a company that owns a referenced workflow.
     * @param workflowId an identifier of a workflow to set reminders for.
     * @param reminders a list of reminders (or empty list or {@code null}) to store.
     */
    void setReminders(int companyId, int workflowId, List<WorkflowReminder> reminders);
}
