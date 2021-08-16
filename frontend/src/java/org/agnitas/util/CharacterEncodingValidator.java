/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;


import com.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.exceptions.CharacterEncodingValidationException;
import org.agnitas.exceptions.CharacterEncodingValidationExceptionMod;
import org.agnitas.exceptions.EncodingError;
import org.agnitas.web.forms.MailingBaseForm;

import com.agnitas.beans.DynamicTag;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.web.ComMailingContentForm;

import java.nio.charset.CharsetEncoder;
import java.util.Set;

/**
 * This class validates the content of a mailing against the character set
 * defined in the mailing.
 */
public interface CharacterEncodingValidator {

	/**
	 * Validates text and HTML template of the given form and the content blocks of the given Mailing object. This method is called directly <b>before modifying</b>
	 * the MailingBaseForm object. 
	 * 
	 * @param form form to validate text and HTML template
	 * @param mailing mailing to validate content blocks
	 */
	void validate( MailingBaseForm form, Mailing mailing) throws CharacterEncodingValidationException;

	/**
	 * Validates the content from given MailingContentForm.
	 * 
	 * @param form MailingContentForm to be validated
	 * @param mailing mailing object to determined character encoding
	 * @throws CharacterEncodingValidationException
	 */
	void validate(ComMailingContentForm form, Mailing mailing) throws CharacterEncodingValidationException;

    /**
	 * Validates the content from given MailingContentForm.
	 *
	 * @param form MailingContentForm to be validated
	 * @param charset mailing charset
	 * @throws CharacterEncodingValidationException
	 */
	void validate(ComMailingContentForm form, String charset) throws CharacterEncodingValidationException;
	
	/**
	 * Validates a mailing component.
	 * 
	 * @param component the mailing component to be validated
	 * @param charsetName the name of the character set to be used
	 * @return true if the mailing component passed validated otherwise false
	 */
	boolean validate( MailingComponent component, String charsetName);
	
	/**
	 * Validates a dynamic tag.
	 * 
	 * @param dynTag DynamicTag to be validated
	 * @param charsetName character set to be used for validation
	 * @return true if the DynamicTag passed validated otherwise false
	 */
	boolean validate(DynamicTag dynTag, String charsetName);

    void validateMod( MailingBaseForm form, Mailing mailing) throws CharacterEncodingValidationExceptionMod;

	Set<EncodingError> validateMod(String string, CharsetEncoder charsetEncoder);

    void validateContentMod(ComMailingContentForm form, String charset) throws CharacterEncodingValidationExceptionMod;

    void validateContentMod(DynTagDto dynTag, String charset) throws CharacterEncodingValidationExceptionMod;
}
