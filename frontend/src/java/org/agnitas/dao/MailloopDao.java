/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.bounce.form.BounceFilterListForm;
import org.agnitas.beans.Mailloop;
import org.agnitas.beans.MailloopEntry;
import org.agnitas.beans.impl.PaginatedListImpl;

import java.util.List;

public interface MailloopDao {
    /**
     * Deletes mailloop.
     *
     * @param mailloopId
     *            Id of the mailloop
     * @param companyId
     *              Id of the company
     * @return true==success, false==error
     */
    boolean deleteMailloop(int mailloopId, int companyId);

    /**
     * Loads mailloop by mailloop id and company id.
     *
     * @param mailloopId
     *              Id of the mailloop
     * @param companyId
     *              Id of the company
     * @return Mailloop bean object or null
     */
    Mailloop getMailloop(int mailloopId, int companyId);

    /**
     * Saves mailloop.
     *
     * @param loop
     *          Mailloop bean object
     * @return Saved mailloop id.
     */
    int saveMailloop(Mailloop loop);
    
    /**
     * Loads list of mailloops by company id.
     *
     * @param companyId
     *               Id of the company
     * @return List of mailloops.
     */
    List<Mailloop> getMailloops(int companyId);

    boolean deleteMailloopByCompany(int companyId);

    /**
     * Selects all bounce filters of certain company and creates paginated list according to given criteria of sorting and pagination
     *
     * @param companyId
     *              The id of the company for admins
     * @param sortColumn
     *              The name of the column for sorting
     * @param direction
     *              The sort order
     * @param pageNumber
     *              The number of the page
     * @param pageSize
     *              The number of rows to be shown on page
     * @return PaginatedList of MailloopEntry bean objects
     */
    PaginatedListImpl<MailloopEntry> getPaginatedMailloopList(int companyId, String sortColumn, String direction, int pageNumber, int pageSize);

    PaginatedListImpl<BounceFilterDto> getPaginatedMailloopList(BounceFilterListForm filter);

    boolean isMailingUsedInBounceFilterWithActiveAutoResponder(int companyId, int mailingId);
    
    List<MailloopEntry> getDependentBounceFiltersWithActiveAutoResponder(int companyId, int mailingId);
    
    boolean isAddressInUse(String filterAddress);

}
