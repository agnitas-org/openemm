/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.service;

import java.util.List;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.LinkProperty;
import com.agnitas.emm.core.trackablelinks.dto.FormTrackableLinkDto;
import com.agnitas.emm.core.userform.dto.UserFormDto;
import com.agnitas.messages.Message;

public interface FormTrackableLinkService {

	void saveTrackableLinks(ComAdmin admin, UserFormDto userFormDto, List<Message> errors);

	List<FormTrackableLinkDto> getFormTrackableLinks(ComAdmin admin, int formId);

	void bulkUpdateTrackableLinks(ComAdmin admin, int formId, List<FormTrackableLinkDto> links, int trackable, List<LinkProperty> commonExtensions);

	void bulkUpdateTrackableLinksExtensions(ComAdmin admin, int formId, List<LinkProperty> commonExtensions);

	void bulkUpdateTrackableLinksUsage(ComAdmin admin, int formId, int usage);

	List<LinkProperty> getFormTrackableLinkCommonExtensions(ComAdmin admin, int formId);

	FormTrackableLinkDto getFormTrackableLink(ComAdmin admin, int formId, int linkId);

	boolean updateTrackableLink(ComAdmin admin, int formId, FormTrackableLinkDto trackableLinkDto);
}
