/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dyncontent.service.validation;

import com.agnitas.emm.core.dyncontent.entity.ContentModel;
import com.agnitas.emm.core.validator.BaseValidator;

public class ContentModelValidator extends BaseValidator {

    private static final String COMPANY_ID = "company.id";
    private static final String MAILING_ID = "mailing.id";
    private static final String BLOCK_NAME = "blockName";
    private static final String TARGET_ID = "target.id";
    private static final String ORDER = "order";
    private static final String CONTENT = "content";
    private static final String CONTENT_ID = "content.id";

    public ContentModelValidator(final String propertiesFile) {
        super(propertiesFile);
    }

    public void assertIsValidToAdd(final ContentModel model) {
        assertPositive(model.getCompanyId(), COMPANY_ID);
        assertPositive(model.getMailingId(), MAILING_ID);
        assertIsNotBlank(model.getBlockName(), BLOCK_NAME);
        assertIsPositiveOrZero(model.getTargetId(), TARGET_ID);
        assertPositive(model.getOrder(), ORDER);
        assertIsNotBlank(model.getContent(), CONTENT);
    }

    public void assertIsValidToGetOrDelete(final ContentModel model) {
        assertPositive(model.getCompanyId(), COMPANY_ID);
        assertPositive(model.getContentId(), CONTENT_ID);
    }

    public void assertIsValidToUpdate(final ContentModel model) {
        assertPositive(model.getCompanyId(), COMPANY_ID);
        assertPositive(model.getContentId(), CONTENT_ID);
        assertIsPositiveOrZero(model.getTargetId(), TARGET_ID);
        assertPositive(model.getOrder(), ORDER);
        assertIsNotBlank(model.getContent(), CONTENT);
    }

    public void assertIsValidToGetList(final ContentModel model) {
        assertPositive(model.getCompanyId(), COMPANY_ID);
        assertPositive(model.getMailingId(), MAILING_ID);
    }    
}
