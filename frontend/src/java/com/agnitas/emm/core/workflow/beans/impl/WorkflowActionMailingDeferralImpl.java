/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;

import java.util.Date;

import com.agnitas.emm.core.workflow.beans.WorkflowActionMailingDeferral;

public class WorkflowActionMailingDeferralImpl implements WorkflowActionMailingDeferral {
    // An identifier of a deferral.
    private int id;
    private int companyId;
    private int reactionId;
    private int customerId;
    private int mailingId;
    private Date sendDate;
    private boolean isSent;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getCompanyId() {
        return companyId;
    }

    @Override
    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    @Override
    public int getReactionId() {
        return reactionId;
    }

    @Override
    public void setReactionId(int reactionId) {
        this.reactionId = reactionId;
    }

    @Override
    public int getCustomerId() {
        return customerId;
    }

    @Override
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    @Override
    public int getMailingId() {
        return mailingId;
    }

    @Override
    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    @Override
    public Date getSendDate() {
        return sendDate;
    }

    @Override
    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    @Override
    public boolean isSent() {
        return isSent;
    }
    @Override

    public void setSent(boolean isSent) {
        this.isSent = isSent;
    }
}
