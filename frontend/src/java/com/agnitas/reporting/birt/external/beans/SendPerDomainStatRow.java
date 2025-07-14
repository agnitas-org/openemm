/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

public class SendPerDomainStatRow extends SendStatRow {

    private String domainName;
    private int domainNameIndex;
    private int mailingId;

    public int getDomainNameIndex() {
        return domainNameIndex;
    }

    public void setDomainNameIndex(int domainNameIndex) {
        this.domainNameIndex = domainNameIndex;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public int getMailingId() {
        return mailingId;
    }

    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    @Override
    public String toString() {
        return "SendStatRow : \n" + "Category: " + getCategory() + "\n"
               + "CategoryIndex: " + getCategoryindex() + "\n"
               + "Targetgroup: " + getTargetgroup() + "\n"
               + "TargetgroupIndex: " + getTargetgroupindex() + "\n"
               + "DomainName: " + getDomainName() + "\n"
               + "DomainNameIndex: " + getDomainNameIndex() + "\n"
               + "MailingId: " + getMailingId() + "\n"
               + "Value: " + getCount() + "\n"
               + "Rate: " + getRate() + "\n";
    }
}
