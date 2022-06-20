/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.blacklist.dao;

import java.util.List;
import java.util.Set;

import org.agnitas.beans.BlackListEntry;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.velocity.VelocityCheck;

public interface ComBlacklistDao {
    /**
     * Adds the given email to the blacklist.
     *
     * @param companyID the company to add it for.
     * @param email the address to add to the blacklist.
     * @param reason the description of why address is added to the blacklist.
     * @return true on success.
     */
	boolean insert(@VelocityCheck int companyID, String email, String reason);


    /**
     * Update info for an email in the blacklist.
     *
     * @param companyID the company to update it for.
     * @param email the address in the blacklist.
     * @param reason the description of why address is added to the blacklist.
     * @return true on success.
     */
    boolean update(@VelocityCheck int companyID, String email, String reason);

    /**
     * Remove the given email from the blacklist.
     *
     * @param companyID the company to work on.
     * @param email the address to remove from to the blacklist.
     * @return true on success.
     */
	boolean delete(@VelocityCheck int companyID, String email);

    /**
     * Get full list of blacklisted recipients
     *
     * @param companyID the company to work on.
     * @return list of blacklisted  recipients
     */
    List<BlackListEntry> getBlacklistedRecipients(@VelocityCheck int companyID);

    /**
     * Get a list of blacklisted recipients
     *
     * @param companyID the company to work on.
     * @param sort the field for sorting of returned list
     * @param direction the direction of  sorting of returned list
     * @param page offset of the first item to return
     * @param rownums maximum number of items to return
     * @return list of blacklisted  recipients
     */
    PaginatedListImpl<BlackListEntry> getBlacklistedRecipients(@VelocityCheck int companyID, String sort, String direction, int page, int rownums);
	
	PaginatedListImpl<BlackListEntry> getBlacklistedRecipients(@VelocityCheck int companyID, String sort, String direction, int page, int rownums, String likePattern);

    /**.
     * Check the presence given company and email in the blacklist.
     *
     * @param companyID the company to check.
     * @param email the address to check.
     * @return true if  the company and the email presents in the blacklist.
     */
    boolean exist(@VelocityCheck int companyID, String email);

    /**
     * Get a list of email addresses for given company from the  blacklist.
     *
     * @param companyID the company to work on.
     * @return list of email addresses for given company from the  blacklist.
     */
    List<String> getBlacklist(@VelocityCheck int companyID);

    /**
     * Lists all mailinglists to these a recipient with given email address is bound with <i>blacklisted</i> state.
     * 
     * @param companyId company ID
     * @param email email to search
     * 
     * @return list of all mailinglists with "blacklisted" binding for given address
     */
	List<Mailinglist> getMailinglistsWithBlacklistedBindings(@VelocityCheck int companyId, String email);

	/**
	 * Updates all blacklisted bindings for a list of mailinglists for a given email to a given user status.
	 * 
	 * @param companyId	company ID
	 * @param email email to update related bindings
	 * @param mailinglistIds list of mailinglist ID to update
	 * @param userStatus new user status
	 */
	void updateBlacklistedBindings(@VelocityCheck int companyId, String email, List<Integer> mailinglistIds, UserStatus userStatus);
	
	default boolean insertGlobal(String email, String reason) {
		return false;
	}
	
	/**
     * Load set of emails of customers from blacklist for given company
     *
     * @param companyID the id of the company
     * @return set of emails of customers from blacklist
     * @throws Exception
     */
	Set<String> loadBlackList(@VelocityCheck int companyID) throws Exception;
	
	boolean blacklistCheckCompanyOnly(String email, @VelocityCheck int companyID);
	
	/**
     * Checks if E-Mail-Adress given in customerData-HashMap is registered in blacklist(s)
     *
     * @param email The address
     * @param companyID The id of the company
     * @return true if E-Mail-Adress is blacklisted
     */
	boolean blacklistCheck(String email, @VelocityCheck int companyID);


	List<BlackListEntry> getBlacklistCheckEntries(int companyID, String email);
}
