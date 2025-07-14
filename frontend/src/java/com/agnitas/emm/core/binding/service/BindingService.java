/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.binding.service;

import com.agnitas.beans.BindingEntry;
import com.agnitas.emm.common.UserStatus;
import org.agnitas.emm.core.binding.service.BindingModel;
import org.agnitas.emm.core.mailinglist.service.impl.MailinglistException;

import java.util.List;

public interface BindingService {

	boolean setBindingWithActionId(BindingModel model, final boolean runActionInBackground) throws MailinglistException;

	BindingEntry getBinding(BindingModel model);

	void setBinding(BindingModel model) throws MailinglistException;

	void deleteBinding(BindingModel model);

	List<BindingEntry> getBindings(BindingModel model);

	void updateBindingStatusByEmailPattern(int companyId, String emailPattern, UserStatus userStatus, String remark);
	
}
