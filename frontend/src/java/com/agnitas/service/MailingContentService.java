/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.Target;
import com.agnitas.emm.core.mailingcontent.dto.ContentBlockAndMailingMetaData;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.web.mvc.Popups;

public interface MailingContentService {

    boolean isGenerationAvailable(Mailing mailing);

    void generateTextContent(Mailing mailing);

    String generateDynName(String sourceName, Set<String> namesInUse);

    void saveDynTags(int mailingId, List<DynTagDto> dynTags, Admin admin, Popups popups);

    DynTagDto getDynTag(int companyId, int dynNameId);

    /**
     * Load and save the new mailing with a new dynamic tag to let any adjustments happen that may be needed.
     *
     * @param dynamicTag Dynamic tag to save
     * @param companyID id of the company
     * @return false if provided name not exists in mailing templates, true otherwise
     */
    boolean saveDynTag(DynamicTag dynamicTag, int companyID) throws Exception;

	List<ContentBlockAndMailingMetaData> listContentBlocksUsingTargetGroup(Target target);

    Set<String> findDynNamesUsedInContent(String content, List<DynamicTag> dynTags);

    Map<String, DynTagDto> loadDynTags(Mailing mailing);

    Map<Integer, String> getMailingContentNames(int mailingId, int companyId);

    Map<Integer, String> getNamesOfAvailableTargetsForContent(Admin admin);
}
