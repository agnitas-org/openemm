/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;

import java.util.Objects;

import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.WorkflowMailing;

public class WorkflowMailingImpl extends WorkflowMailingAwareImpl implements WorkflowMailing {

    private int autoReport;
    private boolean skipEmptyBlocks = false;
    private boolean doubleCheck = true;
    private int maxRecipients;
    private int blocksize;

    public WorkflowMailingImpl() {
        super();
        setType(WorkflowIconType.MAILING.getId());
    }

    @Override
    public int getAutoReport() {
        return autoReport;
    }

    @Override
    public void setAutoReport(int autoReport) {
        this.autoReport = autoReport;
    }

    @Override
    public boolean isSkipEmptyBlocks() {
        return skipEmptyBlocks;
    }

    @Override
    public void setSkipEmptyBlocks(boolean skipEmptyBlocks) {
        this.skipEmptyBlocks = skipEmptyBlocks;
    }

    @Override
    public boolean isDoubleCheck() {
        return doubleCheck;
    }

    @Override
    public void setDoubleCheck(boolean doubleCheck) {
        this.doubleCheck = doubleCheck;
    }

    @Override
    public int getMaxRecipients() {
        return maxRecipients;
    }

    @Override
    public void setMaxRecipients(int maxRecipients) {
        this.maxRecipients = maxRecipients;
    }

    @Override
    public int getBlocksize() {
        return blocksize;
    }

    @Override
    public void setBlocksize(int blocksize) {
        this.blocksize = blocksize;
    }

    @Override
    public boolean equalsIgnoreI18n(Object o) {
        if (!super.equalsIgnoreI18n(o)) {
            return false;
        }
        WorkflowMailingImpl that = (WorkflowMailingImpl) o;
        return autoReport == that.autoReport
            && skipEmptyBlocks == that.skipEmptyBlocks
            && doubleCheck == that.doubleCheck
            && maxRecipients == that.maxRecipients
            && blocksize == that.blocksize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), autoReport, skipEmptyBlocks, doubleCheck, maxRecipients, blocksize);
    }
}
