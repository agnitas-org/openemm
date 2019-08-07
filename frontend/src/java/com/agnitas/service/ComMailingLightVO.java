/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.List;

import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.velocity.VelocityCheck;

/**
 * This service class has following task:
 * Get all Mailings from DB (over the DAO) and put the result into
 * a new Data-Structure consisting of Snowflake-Mailings (lightweight mailings).
 */
public interface ComMailingLightVO {
    // Define Sort Orders
    int SORTORDER_UP = 0;
    int SORTORDER_DOWN = 1;
    int SNOWFLAKE_COUNT = 25;
    int TAKE_ALL_SNOWFLAKE_MAILINGS = 0;

    LightweightMailing getSnowflakeMailing(int mailingID) ;

    /**
     * this method returns a List with ALL Mailings in form of so called
     * Snowflake-Mailings. That means lightweight Mailings without unneeded
     * informations like Attachements and so on.
     *
     * @return a list of lightweight mailings.
     */
    List<LightweightMailing> getSnowflakeMailings(@VelocityCheck int companyID) ;

    /**
    * Check if the mailtracking is active for this company.
    * @return {@code true} if mailtracking is active for this company or {@code false} otherwise.
    */

    boolean isMailtrackingActive(@VelocityCheck int companyID);
}
