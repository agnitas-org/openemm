/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.bean.impl;

import java.util.Objects;

import com.agnitas.emm.core.birtreport.bean.ComLightweightBirtReport;

public class ComLightweightBirtReportImpl implements ComLightweightBirtReport {
    protected int reportID;
    protected String shortname;
    protected String description;
    protected boolean hidden;

    @Override
	public int getId() {
        return reportID;
    }

    @Override
	public void setId(int reportID) {
        this.reportID = reportID;
    }

    @Override
	public String getShortname() {
        return shortname;
    }

    @Override
	public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    @Override
	public String getDescription() {
        return description;
    }

    @Override
	public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setHidden(boolean isHidden) {
        hidden = isHidden;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ComLightweightBirtReportImpl that = (ComLightweightBirtReportImpl) o;
        return reportID == that.reportID &&
                hidden == that.hidden &&
                Objects.equals(shortname, that.shortname) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportID, shortname, description, hidden);
    }
}
