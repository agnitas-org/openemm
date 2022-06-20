/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.validators;

import org.agnitas.emm.core.velocity.scriptvalidator.IllegalVelocityDirectiveException;
import org.agnitas.emm.core.velocity.scriptvalidator.ScriptValidationException;
import org.agnitas.emm.core.velocity.scriptvalidator.ScriptValidator;
import org.springframework.stereotype.Component;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.action.operations.ActionOperationExecuteScriptParameters;
import com.agnitas.emm.core.action.operations.ActionOperationParameters;
import com.agnitas.messages.Message;
import com.agnitas.service.SimpleServiceResult;

@Component
public class ExecuteScriptValidator implements ActionOperationValidator {

    private ScriptValidator velocityDirectiveScriptValidator;

    public ExecuteScriptValidator(ScriptValidator velocityDirectiveScriptValidator) {
        this.velocityDirectiveScriptValidator = velocityDirectiveScriptValidator;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ActionOperationExecuteScriptParameters.class.isAssignableFrom(clazz);
    }

    @Override
    public SimpleServiceResult validate(ComAdmin admin, ActionOperationParameters target) {
        ActionOperationExecuteScriptParameters operation = (ActionOperationExecuteScriptParameters) target;
        try {
            velocityDirectiveScriptValidator.validateScript(operation.getScript());
        } catch(ScriptValidationException e) {
            String directive = ((IllegalVelocityDirectiveException) e).getDirective();
            return new SimpleServiceResult(false, Message.of("error.action.illegal_directive", directive));
        }

        return new SimpleServiceResult(true);
    }
}
