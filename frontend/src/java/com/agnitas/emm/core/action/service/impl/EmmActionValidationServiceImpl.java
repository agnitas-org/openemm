/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.action.operations.ActionOperationParameters;
import com.agnitas.emm.core.action.service.EmmActionValidationService;
import com.agnitas.emm.core.action.validators.ActionOperationValidator;
import com.agnitas.service.SimpleServiceResult;

@Service
public class EmmActionValidationServiceImpl implements EmmActionValidationService {

    private List<ActionOperationValidator> validatorsByOperationType;

    public EmmActionValidationServiceImpl(List<ActionOperationValidator> validatorsByOperationType) {
        this.validatorsByOperationType = validatorsByOperationType;
    }

    private ActionOperationValidator getValidatorByType(Class<? extends ActionOperationParameters> clazz) {
        for (ActionOperationValidator validator : validatorsByOperationType) {
            if (validator.supports(clazz)) {
                return validator;
            }
        }

        return null;
    }

    @Override
    public SimpleServiceResult validate(ComAdmin admin, ActionOperationParameters operation) throws Exception {
        ActionOperationValidator validatorByType = getValidatorByType(operation.getClass());
        if (validatorByType == null) {
            return new SimpleServiceResult(true);
        }

        return validatorByType.validate(admin, operation);
    }
}
