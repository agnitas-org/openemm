/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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
        super.setUrl_id(mailingClickStatsPerTargetRow.getUrl_id());
        super.setAdmin_link(mailingClickStatsPerTargetRow.isAdmin_link());
        super.setClicks_gross(mailingClickStatsPerTargetRow.getClicks_gross());
        super.setClicks_gross_percent(mailingClickStatsPerTargetRow.getClicks_gross_percent());
        super.setClicks_net(mailingClickStatsPerTargetRow.getClicks_net());
        super.setClicks_net_percent(mailingClickStatsPerTargetRow.getClicks_net_percent());
        super.setColumn_index(mailingClickStatsPerTargetRow.getColumn_index());
        super.setDeleted(mailingClickStatsPerTargetRow.isDeleted());
        super.setMobile(mailingClickStatsPerTargetRow.isMobile());
        super.setRow_index(mailingClickStatsPerTargetRow.getRow_index());
        super.setTotal_clicks_gros(mailingClickStatsPerTargetRow.getTotal_clicks_gros());
        super.setTotal_clicks_net(mailingClickStatsPerTargetRow.getTotal_clicks_net());
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
