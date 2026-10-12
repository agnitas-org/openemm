/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.recipient.web;

import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;

import java.util.LinkedHashSet;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.commons.validation.DeleteCheck;
import com.agnitas.emm.core.commons.validation.DeleteResult;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.exception.BadRequestException;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.UserActivityLogService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class RecipientUiService {

    private final BulkActionValidationService<Integer, String> bulkActionValidationService;
    private final RecipientService recipientService;
    private final UserActivityLogService ualService;

    public RecipientUiService(
            BulkActionValidationService<Integer, String> bulkActionValidationService,
            RecipientService recipientService,
            UserActivityLogService ualService
    ) {
        this.bulkActionValidationService = bulkActionValidationService;
        this.recipientService = recipientService;
        this.ualService = ualService;
    }

    public ServiceResult<List<String>> getAllowedForDeletion(List<Integer> ids, Admin admin) {
        validateIds(ids);
        return bulkActionValidationService.checkAllowedForDeletion(ids, id -> canBeDeleted(id, admin));
    }

    private ServiceResult<String> canBeDeleted(int id, Admin admin) {
        DeleteCheck deleteCheck = recipientService.canBeDeleted(id, admin);
        if (deleteCheck.isAllowed()) {
            return ServiceResult.success(recipientService.getRecipientLightDto(admin.getCompanyID(), id).getMention());
        }
        return ServiceResult.error(deleteCheck.errors());
    }

    public void delete(List<Integer> ids, Admin admin) {
        validateIds(ids);
        List<Integer> deletedIds = recipientService.delete(new LinkedHashSet<>(ids), admin)
                .stream()
                .filter(DeleteResult::isDeleted)
                .map(DeleteResult::id)
                .toList();
        ualService.writeUserActivityLog(
                admin,
                "delete recipients",
                "Recipients IDs %s".formatted(StringUtils.join(deletedIds, ", "))
        );
    }

    private void validateIds(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BadRequestException(NOTHING_SELECTED_MSG);
        }
    }
}
