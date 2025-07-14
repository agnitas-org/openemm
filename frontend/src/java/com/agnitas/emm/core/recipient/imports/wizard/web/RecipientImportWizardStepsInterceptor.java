/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.imports.wizard.web;

import com.agnitas.emm.core.recipient.imports.wizard.exception.NotAllowedImportWizardStepException;
import com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps;
import com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps.Step;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class RecipientImportWizardStepsInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String methodName = ((HandlerMethod) handler).getMethod().getName();

        Object importWizardSteps = request.getSession(false).getAttribute("importWizardSteps");
        if (importWizardSteps instanceof ImportWizardSteps) {
            ImportWizardSteps steps = (ImportWizardSteps) importWizardSteps;

            if (steps.isImportRunning() || Step.fromControllerMethodName(methodName).ordinal() > steps.getCurrentStep().ordinal()) {
                throw new NotAllowedImportWizardStepException();                 
            }
        }
        return true;
    }
}
