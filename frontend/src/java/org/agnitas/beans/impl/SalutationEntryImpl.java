/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import org.agnitas.beans.SalutationEntry;

/**
 * This is the Snowflake/Light version of the Title bean
 */
public class SalutationEntryImpl implements SalutationEntry {
    private String description;
    private Integer titleId;
    private Integer companyID;

    public SalutationEntryImpl(int titleId, String description, int companyID) {
        this.titleId = titleId;
        this.description = description;
        this.companyID = companyID;
    }

    @Override
	public int getTitleId() {
        return titleId;
    }

    @Override
	public String getDescription() {
        return description;
    }

    @Override
    public int getCompanyID() {
        return companyID;
    }
}
