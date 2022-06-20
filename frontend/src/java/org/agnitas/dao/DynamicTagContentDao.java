/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.List;
import java.util.Map;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.DynamicTag;

public interface DynamicTagContentDao {
	void saveDynamicContent(DynamicTagContent dynamicTagContent, String mailingCharset);

    /**
     * Deletes tag content for the given company from the database.
     *
     * @param companyID The id of the company for content.
     * @param contentID The id of the content to delete.
	 * @return true - success; false - nothing was deleted
     */
    boolean deleteContent(@VelocityCheck int companyID, int contentID);

	/**
	 * Loads tag content identified by content id company id.
	 *
	 * @param contentID The id of the content that should be loaded.
	 * @param companyID The companyID for the content.
	 * @return The DynamicTagContent or null on failure.
	 */
    DynamicTagContent getContent(@VelocityCheck int companyID, int contentID);

	/**
	 * Loads all tag contents for given mailing and company.
	 *
	 * @param companyID The companyID for the content.
	 * @param mailingID The mailingID for the content.
	 * @return List of DynamicTagContent or empty list.
	 */
	List<DynamicTagContent> getContentList(@VelocityCheck int companyID, int mailingID);

	/**
	 * Checks if content exists.
	 *
	 * @param companyId    - the company id for of content.
	 * @param mailingId    - the mailing id for of content.
	 * @param dynNameId    - the dynamic tag name id of the content.
	 * @param dynContentId - the dynamic content id of the content.
	 * @return {@code true} if at least one record exists otherwise returns {@code false}
	 */
	boolean isExisting(@VelocityCheck int companyId, int mailingId, int dynNameId, int dynContentId);

	/**
	 * Checks if at least one non-empty content exists.
	 *
	 * @param companyId - the company id for of content.
	 * @param mailingId - the mailing id for of content.
	 * @param dynNameId- the dynamic tag name id of the content.
	 * @return {@code true} if at least one non-empty content exists otherwise returns {@code false}
	 */
	boolean isContentValueNotEmpty(@VelocityCheck int companyId, int mailingId, int dynNameId);
	
	Map<Integer, List<Integer>> getExistingDynContentForDynName(@VelocityCheck int companyId, int mailingId, List<Integer> dynamicTags);

	void saveDynamicTagContent(int companyID, int mailingID, String encodingCharset, List<DynamicTag> dynamicTags) throws Exception;
	void saveDynamicTagContent(int companyID, int mailingID, String encodingCharset, List<DynamicTag> dynamicTags, final boolean removeUnusedContent) throws Exception;

	boolean deleteContentFromMailing(int companyId, int mailingId, int contentId);
}
