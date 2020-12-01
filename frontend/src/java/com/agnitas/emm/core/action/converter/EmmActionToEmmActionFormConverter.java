/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.converter;

import org.agnitas.actions.EmmAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.action.form.EmmActionForm;
import com.agnitas.emm.core.action.operations.ActionOperationParametersParser;

@Component
public class EmmActionToEmmActionFormConverter implements Converter<EmmAction, EmmActionForm> {

    @Lazy
    @Autowired
    private final ActionOperationParametersParser parametersParser;

    public EmmActionToEmmActionFormConverter(ActionOperationParametersParser parametersParser) {
        this.parametersParser = parametersParser;
    }

    @Override
    public EmmActionForm convert(EmmAction emmAction) {
        EmmActionForm form = new EmmActionForm();
        form.setId(emmAction.getId());
        form.setShortname(emmAction.getShortname());
        form.setDescription(emmAction.getDescription());
        form.setType(emmAction.getType());
        form.setActive(emmAction.getIsActive());
        form.setModulesSchema(parametersParser.serializeActionModules(emmAction.getActionOperations()));
        return form;
    }
}
