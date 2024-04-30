/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;
import java.util.Map;

import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;

public interface DynamicTagDao {
    /**
     * Loads list of  DynamicTag objects with established properties Id and DynName for given company and mailing
     *
     * @param companyId The id of the company
     * @param mailingId The id of the mailing
     * @return List of DynamicTag
     */
	List<DynamicTag> getNameList(int companyId, int mailingId);

	void markNameAsDeleted(int mailingID, String name);
	void markNamesAsDeleted(int mailingID, List<String> names);

	void markNameAsUsed(int mailingID, String name);
	void markNamesAsUsed(int mailingID, List<String> names);

	void deleteDynamicTagsMarkAsDeleted(int retentionTime);

	String getDynamicTagInterestGroup(int companyId, int mailingId, int dynTagId);

	boolean deleteDynamicTagsByCompany(int companyID);

	/**
	 * Dynamic tag id if exists.
	 *
	 * @param companyId - the company id of the content.
	 * @param mailingId - the mailing id of the tag.
	 * @param dynTagName - the dyn tag name of the tag.
	 * @return Dynamic tag id or {@code 0} in case of there is no such a record.
	 */
	int getId(int companyId, int mailingId, String dynTagName);
	
	/**
	 * Dynamic tag id if exists.
	 *
	 * @param companyId - the company id of the content.
	 * @param mailingId - the mailing id of the tag.
	 * @param dynTagId - the dyn tag id of the content.
	 * @return Dynamic tag name or {@code empty string} in case of there is no such a record.
	 */
	String getDynamicTagName(int companyId, int mailingId, int dynTagId);
    
    Map<String, Integer> getDynTagIdsByName(int companyId, int mailingId, List<String> dynNames);

	DynamicTag getDynamicTag(int dynNameId, int companyId);

	/**
	 * Retrieve a dynamic contents of a mailing referenced by {@code mailingId}.
	 *
	 * @param mailingId an identifier of a mailing whose dynamic content is to be retrieved.
	 * @param companyId an identifier of a company of the current user.
	 * @return a list of an entities representing dynamic contents.
	 */
	List<DynamicTag> getDynamicTags(int mailingId, int companyId, boolean includeDeletedDynTags);

	void deleteAllDynTags(int mailingId);

    /**
     * Deletes all dyn content by dyn tag name
	 * @param mailingId
	 * @param companyId
	 * @param dynName
     * @return true if at least con row was affected otherwise return false
     */
	boolean cleanupContentForDynNames(int mailingId, int companyId, List<String> dynNames);

    /**
     * Deletes all dyn content by dyn tag name
	 * @param mailingId
	 * @param companyId
	 * @param dynName
     * @return true if at least con row was affected otherwise return false
     */
	boolean cleanupContentForDynName(int mailingId, int companyId, String dynName);

	void updateDynamicTags(int companyID, int mailingID, String encodingCharset, List<DynamicTag> dynamicTags) throws Exception;

	void createDynamicTags(int companyID, int mailingID, String encodingCharset, List<DynamicTag> dynamicTags) throws Exception;

	void saveDynamicTags(Mailing mailing, Map<String, DynamicTag> dynTags) throws Exception;
	void saveDynamicTags(Mailing mailing, Map<String, DynamicTag> dynTags, final boolean removeUnusedContent) throws Exception;

	void removeAbsentDynContent(DynamicTag oldDynamicTag, DynamicTag newDynamicTag);
}
