/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.binding.service;

import java.util.List;
import java.util.Optional;

import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.binding.BulkDeleteBindingCommand;
import com.agnitas.emm.core.binding.BulkUpsertBindingCommand;
import com.agnitas.emm.core.binding.CreateBindingCommand;
import com.agnitas.emm.core.binding.DeleteBindingCommand;
import com.agnitas.emm.core.binding.GetBindingQuery;
import com.agnitas.emm.core.binding.ListBindingsQuery;
import com.agnitas.emm.core.binding.PatchBindingCommand;
import com.agnitas.emm.core.binding.UpsertBindingResult;
import com.agnitas.emm.core.commons.validation.DeleteResult;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public interface BindingService {

	boolean setBindingWithActionId(BindingModel model, boolean runActionInBackground);

	BindingEntry getBinding(BindingModel model);

	void setBinding(BindingModel model);

	void deleteBinding(BindingModel model);

	List<BindingEntry> getBindings(BindingModel model);

	void updateBindingStatusByEmailPattern(int companyId, String emailPattern, UserStatus userStatus, String remark);

    int updateBindings(int companyId, BindingEntry... bindings);

	int updateBindings(int companyId, List<BindingEntry> bindings);

	void updateBinding(PatchBindingCommand command, int companyId);

	int insertBindings(int companyId, BindingEntry... bindings);

	int insertBindings(int companyId, List<BindingEntry> bindings);

	boolean exist(int recipientId, int companyId, int mailinglistId, MediaTypes mediatype);

    BindingEntry get(GetBindingQuery query, int companyId);

    Optional<BindingEntry> find(GetBindingQuery query, int companyId);

	PaginatedList<BindingEntry> find(ListBindingsQuery query, int companyId);

	BindingCreateResult create(CreateBindingCommand command, int companyId);

	void deleteBinding(DeleteBindingCommand command, int companyId);

    List<DeleteResult> bulkDelete(BulkDeleteBindingCommand command);

	boolean updateStatus(BindingEntry binding, int companyId);

	List<UpsertBindingResult> upsert(BulkUpsertBindingCommand command, int companyId);
}
