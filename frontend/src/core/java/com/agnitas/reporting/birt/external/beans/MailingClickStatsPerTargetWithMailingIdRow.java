/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

public class MailingClickStatsPerTargetWithMailingIdRow extends MailingClickStatsPerTargetRow {
    private int linkItemNumber;
    private int mailingId;

    public MailingClickStatsPerTargetWithMailingIdRow(MailingClickStatsPerTargetRow mailingClickStatsPerTargetRow,
                                                      int mailingId, int linkItemNumber
    ) {
        this.linkItemNumber = linkItemNumber;
        this.mailingId = mailingId;
        super.setUrl(mailingClickStatsPerTargetRow.getUrl());
        super.setUrlId(mailingClickStatsPerTargetRow.getUrlId());
        super.setAdminLink(mailingClickStatsPerTargetRow.isAdminLink());
        super.setClicksGross(mailingClickStatsPerTargetRow.getClicksGross());
        super.setClicksGrossPercent(mailingClickStatsPerTargetRow.getClicksGrossPercent());
        super.setClicksNet(mailingClickStatsPerTargetRow.getClicksNet());
        super.setClicksNetPercent(mailingClickStatsPerTargetRow.getClicksNetPercent());
        super.setColumnIndex(mailingClickStatsPerTargetRow.getColumnIndex());
        super.setDeleted(mailingClickStatsPerTargetRow.isDeleted());
        super.setMobile(mailingClickStatsPerTargetRow.isMobile());
        super.setRowIndex(mailingClickStatsPerTargetRow.getRowIndex());
        super.setTargetgroup(mailingClickStatsPerTargetRow.getTargetgroup());
    }

    public int getMailingId() {
        return mailingId;
    }

    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    public int getLinkItemNumber() {
        return linkItemNumber;
    }

    public void setLinkItemNumber(int linkItemNumber) {
        this.linkItemNumber = linkItemNumber;
    }
}
