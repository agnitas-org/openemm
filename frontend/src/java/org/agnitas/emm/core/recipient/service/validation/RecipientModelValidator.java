/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient.service.validation;

import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.validator.BaseValidator;

public class RecipientModelValidator extends BaseValidator {

    private static final int GENDER_MIN_VAL = 0;
    private static final int GENDER_MAX_VAL = 5;
    private static final int MAILTYPE_MIN_VAL = 0;
    private static final int MAILTYPE_MAX_VAL = 2;
    private static final String EMAIL = "email";
    private static final String GENDER = "gender";
    private static final String MAILTYPE = "mailtype";
    private static final String COMPANY_ID = "company.id";
    private static final String CUSTOMER_ID = "customer.id";

    public RecipientModelValidator(String propertiesFile) {
        super(propertiesFile);
    }

    public void assertIsValidToGetOrDelete(RecipientModel model) {
        assertPositive(model.getCompanyId(), COMPANY_ID);
        assertPositive(model.getCustomerId(), CUSTOMER_ID);
    }

    public void assertIsValidToAdd(RecipientModel model) {
        assertPositive(model.getCompanyId(), COMPANY_ID);
        assertNotNull(model.getGender(), GENDER);
        assertInRange(model.getGender(), GENDER_MIN_VAL, GENDER_MAX_VAL, GENDER);
        assertIsNotBlank(model.getEmail(), EMAIL);
        assertIsEmail(model.getEmail(), EMAIL);
        assertNotNull(model.getMailtype(), MAILTYPE);
        assertInRange(model.getMailtype(), MAILTYPE_MIN_VAL, MAILTYPE_MAX_VAL, MAILTYPE);
    }

    public void assertIsValidToUpdate(RecipientModel model) {
        assertPositive(model.getCompanyId(), COMPANY_ID);
        assertPositive(model.getCustomerId(), CUSTOMER_ID);
        if (model.getGender() != null) {
            assertInRange(model.getGender(), GENDER_MIN_VAL, GENDER_MAX_VAL, GENDER);
        }
        if (model.getEmail() != null) {
            assertIsEmail(model.getEmail(), EMAIL);
        }
        if (model.getMailtype() != null) {
            assertInRange(model.getMailtype(), MAILTYPE_MIN_VAL, MAILTYPE_MAX_VAL, MAILTYPE);
        }
    }
}
