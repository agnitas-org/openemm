/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.agnitas.emm.core.commons.password.PasswordCheck;
import org.agnitas.emm.core.validator.annotation.Password;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.agnitas.service.SimpleServiceResult;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    @Autowired
    private PasswordCheck passwordCheck;
    private String message;

    @Override
    public void initialize(Password constraintAnnotation) {
        //set a message variable on initialization
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(password)) {
            // ignore the validation if a password is empty
            return true;
        }

        SimpleServiceResult simpleServiceResult = passwordCheck.checkAdminPassword(password, null);
        if (!simpleServiceResult.isSuccess()) {
            //disable existing violation message
            context.disableDefaultConstraintViolation();

            String messageKey = message;
            if (CollectionUtils.isNotEmpty(simpleServiceResult.getMessages())) {
                // WARNING. the message should be without arguments
                messageKey = simpleServiceResult.getMessages().get(0).getCode();
            }

            //build new violation message and add it
            context.buildConstraintViolationWithTemplate(messageKey).addConstraintViolation();
        }

        return simpleServiceResult.isSuccess();
    }
}
