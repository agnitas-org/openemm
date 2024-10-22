/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.common.service.impl;

import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service("bulkActionValidationService")
public class BulkActionValidationServiceImpl<ID, R> implements BulkActionValidationService<ID, R> {

    @Override
    public ServiceResult<List<R>> checkAllowedForDeletion(Collection<ID> ids, Function<ID, ServiceResult<R>> validationFunction) {
        return checkAllowedToPerformAction(
                ids,
                validationFunction,
                "warning.bulkAction.delete"
        );
    }

    @Override
    public ServiceResult<List<R>> checkAllowedForActivation(Collection<ID> ids, Function<ID, ServiceResult<R>> validationFunction) {
        return checkAllowedToPerformAction(
                ids,
                validationFunction,
                "warning.bulkAction.general.activate"
        );
    }

    @Override
    public ServiceResult<List<R>> checkAllowedForDeactivation(Collection<ID> ids, Function<ID, ServiceResult<R>> validationFunction) {
        return checkAllowedToPerformAction(
                ids,
                validationFunction,
                "warning.bulkAction.general.deactivate"
        );
    }

    private ServiceResult<List<R>> checkAllowedToPerformAction(Collection<ID> ids, Function<ID, ServiceResult<R>> validationFunction,
                                                               String warningMsgKey) {
        Map<ID, ServiceResult<R>> checks = new HashSet<>(ids).stream()
                .collect(Collectors.toMap(Function.identity(), validationFunction));

        List<R> allowedEntries = checks.values().stream()
                .filter(ServiceResult::isSuccess)
                .map(ServiceResult::getResult)
                .collect(Collectors.toList());

        if (allowedEntries.isEmpty()) {
            return ServiceResult.error(checks.values().stream().flatMap(c -> c.getErrorMessages().stream()).collect(Collectors.toList()));
        }

        if (allowedEntries.size() != ids.size()) {
            return ServiceResult.warning(
                    allowedEntries,
                    true,
                    Message.of(warningMsgKey, ids.size() - allowedEntries.size())
            );
        }

        return ServiceResult.success(allowedEntries);
    }
}
