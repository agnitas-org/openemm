/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

public class SendPerDomainStatRow extends SendStatRow {

    private String domainName;
    private int domainNameIndex;

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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("SendStatRow : \n");
        builder.append("Category: " + getCategory()+ "\n" );
        builder.append("CategoryIndex: " + getCategoryindex()+ "\n" );
        builder.append("Targetgroup: " + getTargetgroup()+ "\n" );
        builder.append("TargetgroupIndex: " + getTargetgroupindex()+ "\n" );
        builder.append("DomainName: " + getDomainName()+ "\n" );
        builder.append("DomainNameIndex: " + getDomainNameIndex()+ "\n" );
        builder.append("Value: " + getCount()+"\n");
        builder.append("Rate: " + getRate()+"\n");

        return  builder.toString();
    }
}
