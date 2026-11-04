/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailingcontent.dto;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.List;
import java.util.Objects;

import com.agnitas.beans.DynamicTagContent;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.validator.DynTagValidationField;
import com.agnitas.messages.Message;

public record PatchMailingContentCommand(
        PatchField<Boolean> disableLinkExtension,
        PatchField<List<DynamicTagContent>> content
) {
    public PatchMailingContentCommand {
        content.ifPresent(blocks -> blocks.stream()
                .filter(Objects::nonNull)
                .filter(c -> c.getDynOrder() <= 0)
                .forEach(c -> c.setDynOrder(1)));
        content.check(
                PatchMailingContentCommand::isValidOrders,
                DynTagValidationField.CONTENT,
                Message.exact("Duplicate order. Each target group must have a unique order.")
        );
        content.check(
                PatchMailingContentCommand::isTargetIdsPositive,
                DynTagValidationField.CONTENT,
                Message.exact("Invalid negative target ID.")
        );
        content.check(
                PatchMailingContentCommand::isValidTargetIds,
                DynTagValidationField.CONTENT,
                Message.exact("Duplicate target ID. Each block must have a unique target ID. 0 - All recipients.")
        );
    }

    private static boolean isTargetIdsPositive(List<DynamicTagContent> blocks) {
        return emptyIfNull(blocks).stream().allMatch(b -> b.getTargetID() >= 0);
    }

    private static boolean isValidTargetIds(List<DynamicTagContent> blocks) {
        return emptyIfNull(blocks).stream()
                .map(DynamicTagContent::getTargetID)
                .distinct()
                .count() == blocks.size();
    }

    private static boolean isValidOrders(List<DynamicTagContent> blocks) {
        return emptyIfNull(blocks).stream()
                .map(DynamicTagContent::getDynOrder)
                .distinct()
                .count() == blocks.size();
    }
}
