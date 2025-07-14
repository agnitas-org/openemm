/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;

import java.util.Objects;
import java.util.Set;

import com.agnitas.emm.core.workflow.beans.WorkflowIconType;

public class WorkflowDateBasedMailingImpl extends WorkflowMailingAwareImpl {

    private boolean enableNotifications;
    private boolean enableNoSendCheckNotifications;
    private Set<String> clearanceEmails;
    private Integer clearanceThreshold;

    public WorkflowDateBasedMailingImpl(){
        super();
        setType(WorkflowIconType.DATE_BASED_MAILING.getId());
    }

    public boolean isEnableNotifications() {
        return enableNotifications;
    }

    public void setEnableNotifications(boolean enableNotifications) {
        this.enableNotifications = enableNotifications;
    }

    public boolean isEnableNoSendCheckNotifications() {
        return enableNoSendCheckNotifications;
    }

    public void setEnableNoSendCheckNotifications(boolean enableNoSendCheckNotifications) {
        this.enableNoSendCheckNotifications = enableNoSendCheckNotifications;
    }

    public Set<String> getClearanceEmails() {
        return clearanceEmails;
    }

    public void setClearanceEmails(Set<String> clearanceEmails) {
        this.clearanceEmails = clearanceEmails;
    }

    public Integer getClearanceThreshold() {
        return clearanceThreshold;
    }

    public void setClearanceThreshold(Integer clearanceThreshold) {
        this.clearanceThreshold = clearanceThreshold;
    }

    @Override
    public boolean equalsIgnoreI18n(Object o) {
        if (!super.equalsIgnoreI18n(o)) {
            return false;
        }

        WorkflowDateBasedMailingImpl that = (WorkflowDateBasedMailingImpl) o;

        return enableNotifications == that.enableNotifications
                && enableNoSendCheckNotifications == that.enableNoSendCheckNotifications
                && Objects.equals(clearanceEmails, that.clearanceEmails)
                && Objects.equals(clearanceThreshold, that.clearanceThreshold);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), enableNotifications, enableNoSendCheckNotifications, clearanceEmails, clearanceThreshold);
    }
}
