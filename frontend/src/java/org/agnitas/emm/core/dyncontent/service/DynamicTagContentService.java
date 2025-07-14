/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.dyncontent.service;

import java.util.List;

import com.agnitas.beans.DynamicTagContent;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;


public interface DynamicTagContentService {

	boolean deleteContent(ContentModel model, List<UserAction> userActions);

	DynamicTagContent getContent(ContentModel model);

	List<DynamicTagContent> getContentList(ContentModel model);

	int addContent(ContentModel model, List<UserAction> userActions);

	void updateContent(ContentModel model, List<UserAction> userActions);

    DynamicTagContent getContent(int companyID, int contentID);

	List<Integer> findTargetDependentMailingsContents(int targetGroupId, int companyId);

	List<Integer> filterContentsOfNotSentMailings(List<Integer> dependencies);
}
