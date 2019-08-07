/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;
import java.util.Map;

import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.DynamicTag;

public interface DynamicTagDao {
	/**
     * Getter for dynamic tag id  by given mailing id and dynamic tag name.
     *
     * @return Value dynamic tag id.
     */
    int	getIdForName(int mailingID, String name);

    /**
     * Loads list of  DynamicTag objects with established properties Id and DynName for given company and mailing
     *
     * @param companyId The id of the company
     * @param mailingId The id of the mailing
     * @return List of DynamicTag
     */

	List<DynamicTag> getNameList( @VelocityCheck int companyId, int mailingId);

	void markNameAsDeleted( int mailingID, String name);
	void markNamesAsDeleted( int mailingID, List<String> names);

	void markNameAsUsed(int mailingID, String name);
	void markNamesAsUsed(int mailingID, List<String> names);

	void deleteDynamicTagsMarkAsDeleted(int retentionTime);

	String getDynamicTagInterestGroup(@VelocityCheck int companyId, int mailingId, int dynTagId);

	boolean deleteDynamicTagsByCompany(@VelocityCheck int companyID);

	/**
	 * Dynamic tag id if exists.
	 *
	 * @param companyId - the company id of the content.
	 * @param mailingId - the mailing id of the tag.
	 * @param dynTagName - the dyn tag name of the tag.
	 * @return Dynamic tag id or {@code 0} in case of there is no such a record.
	 */
	int getId(@VelocityCheck int companyId, int mailingId, String dynTagName);

	/**
	 * Dynamic tag id if exists.
	 *
	 * @param companyId - the company id of the content.
	 * @param mailingId - the mailing id of the tag.
	 * @param dynTagId - the dyn tag id of the content.
	 * @return Dynamic tag name or {@code empty string} in case of there is no such a record.
	 */
	String getDynamicTagName(@VelocityCheck int companyId, int mailingId, int dynTagId);
    
    Map<String, Integer> getDynTagIdsByName(@VelocityCheck int companyId, int mailingId, List<String> dynNames);
}
