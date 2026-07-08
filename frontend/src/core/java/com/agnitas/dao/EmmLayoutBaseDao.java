/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;
import java.util.Map;

import com.agnitas.beans.EmmLayoutBase;

public interface EmmLayoutBaseDao  {
	  /**
     * Loads an EmmLayoutBase identified by company id and layout id.
     *
     * @param companyID
     *          The companyID for the layout.
     * @param emmLayoutBaseID
     *          The id of the layout that should be loaded.
     * @return the emmLayoutBase.
     */
	EmmLayoutBase getEmmLayoutBase(int companyID, int emmLayoutBaseID);
	List<EmmLayoutBase> getEmmLayoutsBase(int companyID);
	boolean deleteLayoutByCompany(int companyID);
	String getLayoutDirectory(String requestDomain);
	Map<String, Integer> getMappedDomains();
}
