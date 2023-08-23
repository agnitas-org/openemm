/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.util.List;
import java.util.Map;

import org.agnitas.beans.AgnDBTagError;
import org.agnitas.beans.DynamicTagContent;

public interface MailingContentService {
	public final static String AGNDBTAG_WRONG_FORMAT = "AGNDBTAG_WRONG_FORMAT";
	public final static String AGNDBTAG_UNKNOWN_COLUMN = "AGNDBTAG_UNKNOWN_COLUMN"; 
	public List<AgnDBTagError> getInvalidAgnDBTags(String content,int companyID) throws Exception;
	public List<String> scanForAgnDBTags(String content);
	public Map<String, List<AgnDBTagError>> getAgnDBTagErrors( Map<String, DynamicTagContent> tagMap, int companyID ) throws Exception; 
}
