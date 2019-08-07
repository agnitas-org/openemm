/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.List;
import java.util.Map;

import org.agnitas.beans.TagDefinition;
import org.agnitas.emm.core.velocity.VelocityCheck;

/**
 * Dao for tag_tabl which contains the agnTAG definitions
 */
public interface TagDao {
	TagDefinition getTag(@VelocityCheck int companyID, String name);
	
	List<String> getTagNames(@VelocityCheck int companyID);
	
	List<TagDefinition> getTagDefinitions(@VelocityCheck int companyID);

	Map<String, TagDefinition> getTagDefinitionsMap(@VelocityCheck int companyID);

	/**
	 * Loads list of dynamic tags of certain company, also includes default dynamic tags
	 *
	 * Watchout: This method is used in ckeditor JSPs
	 *
	 * @param companyID Id of the company
	 * @return
	 */
	List<Map<String, String>> getTags(@VelocityCheck int companyID);
	
	boolean deleteTagsByCompany(@VelocityCheck int companyId);

	int insertTag(String tagName, String tagSelectValue, String tagType, int companyId, String tagDescription);
}
