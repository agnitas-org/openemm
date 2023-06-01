/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.ecs.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.agnitas.ecs.backend.beans.ClickStatColor;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.Admin;

public interface EcsService {
    /**
     * Method gets collection of color values for company id
     *
     * @param companyId id of company
     * @return collection of {@link org.agnitas.ecs.backend.beans.ClickStatColor} beans for companyId
     */
    List<ClickStatColor> getClickStatColors(@VelocityCheck int companyId);

    /**
     * Gets all "test" and "admin" recipients for mailing's mailing list.
     * The map entry is "recipientId" -> "recipientFirstName recipientLastName &lt;recipientEmail&gt;"
     *
     * @param mailingId mailing id
     * @param companyId company id
     * @return test and admin recipients for mailing's mailing list.
     */
    Map<Integer, String> getTestAndAdminRecipients(int mailingId, @VelocityCheck int companyId);

    /**
     * Generate a PDF representation of the heat map by url.
     * @return a generated document.
     */
    File generatePDF(Admin admin, String url, String title);
}
