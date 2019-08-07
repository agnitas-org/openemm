/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mailing.service.validator;

import org.agnitas.emm.core.mailing.service.MailingModel;
import org.apache.commons.validator.Field;
import org.apache.commons.validator.ValidatorAction;
import org.apache.commons.validator.ValidatorResults;
import org.apache.commons.validator.util.ValidatorUtils;

import com.agnitas.emm.core.maildrop.MaildropStatus;

public class MailingModelChecks {

    public static boolean validateMailingType(Object bean, Field field,
            ValidatorResults results, ValidatorAction action) {
       String value = ValidatorUtils.getValueAsString(bean, field.getProperty());

       boolean valid = MailingModel.mailingTypeMap.containsKey(value.toLowerCase());
       results.add(field, action.getName(), valid, value);
       return valid;
    }

    public static boolean validateMailingFormat(Object bean, Field field,
            ValidatorResults results, ValidatorAction action) {
       String value = ValidatorUtils.getValueAsString(bean, field.getProperty());

       boolean valid = MailingModel.formatMap.containsKey(value.toLowerCase());
       results.add(field, action.getName(), valid, value);
       return valid;
    }

    public static boolean validateOnePixel(Object bean, Field field,
            ValidatorResults results, ValidatorAction action) {
       String value = ValidatorUtils.getValueAsString(bean, field.getProperty());

       boolean valid = MailingModel.onePixelMap.containsKey(value.toLowerCase());
       results.add(field, action.getName(), valid, value);
       return valid;
    }

    public static boolean validateTargetMode(Object bean, Field field,
            ValidatorResults results, ValidatorAction action) {
       String value = ValidatorUtils.getValueAsString(bean, field.getProperty());

       boolean valid = MailingModel.targetModeMap.containsKey(value.toLowerCase());
       results.add(field, action.getName(), valid, value);
       return valid;
    }

	public static boolean validateMaildropStatus(Object bean, Field field, ValidatorResults results, ValidatorAction action) {
		String value = ValidatorUtils.getValueAsString(bean, field.getProperty());

		boolean valid;
		try {
			MaildropStatus.fromName(value.toLowerCase());
			valid = true;
		} catch (Exception e) {
			valid = false;
		}
		results.add(field, action.getName(), valid, value);
		return valid;
	}
}
