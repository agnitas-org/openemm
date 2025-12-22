/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.binding.service.validation;

import com.agnitas.emm.core.binding.service.BindingModel;
import com.agnitas.emm.core.validator.BaseValidator;

public class BindingModelValidator extends BaseValidator {

    private static final String COMPANY_ID = "company.id";
    private static final String CUSTOMER_ID = "customer.id";
    private static final String MAILINGLIST_ID = "mailinglist.id";
    private static final String MEDIA_TYPE = "mediatype";
    private static final String USER_TYPE = "userType";
    private static final String REMARK = "remark";
    private static final String EXIT_MAILING_ID = "exit.mailing.id";

    public BindingModelValidator(final String propertiesFile) {
        super(propertiesFile);
    }
    
    public void assertIsValidToList(BindingModel model) {
        assertPositive(model.getCompanyId(), COMPANY_ID);
        assertPositive(model.getCustomerId(), CUSTOMER_ID);
    }
    
    public void assertIsValidToGetOrDelete(BindingModel model) {
        assertIsValidToList(model);
        assertPositive(model.getMailinglistId(), MAILINGLIST_ID);
        assertInRange(model.getMediatype(), 0, 4, MEDIA_TYPE); 
    }
    
    public void assertIsValidToSet(BindingModel model) {
        assertIsValidToGetOrDelete(model);
        assertIsNotBlank(model.getUserType(), USER_TYPE);
        assertMaxLength(model.getUserType(), USER_TYPE, 1);
        assertMaxLength(model.getRemark(), REMARK, 150);
        assertIsPositiveOrZero(model.getExitMailingId(), EXIT_MAILING_ID);
    }
}
