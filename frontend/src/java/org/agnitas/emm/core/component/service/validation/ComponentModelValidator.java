/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.component.service.validation;

import org.agnitas.emm.core.component.service.ComponentModel;
import org.agnitas.emm.core.validator.BaseValidator;

public class ComponentModelValidator extends BaseValidator {

    public ComponentModelValidator(final String propertiesFile) {
        super(propertiesFile);
    }

    public void assertIsValidToAdd(final ComponentModel model) throws IllegalArgumentException {
        assertPositive(model.getCompanyId(), "company.id");
        assertPositive(model.getMailingId(), "mailing.id");
        assertIsNotBlank(model.getMimeType(), "mimeType");
        assertMaxLength(model.getMimeType(), "mimeType", 100);
        assertPositive(model.getComponentType().getCode(), "componentType");
        assertIsNotBlank(model.getComponentName(), "componentName");
        assertMaxLength(model.getComponentName(), "componentName", 100);
        assertIsNotEmpty(model.getData(), "data");
    }

    public void assertIsValidToGetOrDelete(final ComponentModel model) throws IllegalArgumentException {
        assertPositive(model.getCompanyId(), "company.id");
        assertPositive(model.getComponentId(), "component.id");
    }

    public void assertIsValidToUpdateGroup(final ComponentModel model) throws IllegalArgumentException {
        assertPositive(model.getCompanyId(), "company.id");
        assertPositive(model.getComponentId(), "component.id");
        assertIsNotBlank(model.getMimeType(), "mimeType");
        assertMaxLength(model.getMimeType(), "mimeType", 100);
        assertPositive(model.getComponentType().getCode(), "componentType");
        assertIsNotBlank(model.getComponentName(), "componentName");
        assertMaxLength(model.getComponentName(), "componentName", 100);
    }

    public void assertIsValidToUpdateMailingContentGroup(final ComponentModel model) throws IllegalArgumentException {
        assertPositive(model.getCompanyId(), "company.id");
        assertPositive(model.getMailingId(), "mailing.id");
        assertIsNotBlank(model.getComponentName(), "componentName");
        assertMaxLength(model.getComponentName(), "componentName", 100);
        assertIsNotEmpty(model.getData(), "data");
    }

}
