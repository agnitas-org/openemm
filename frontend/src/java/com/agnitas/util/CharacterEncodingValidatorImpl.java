/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.agnitas.emm.core.mailing.forms.MailingSettingsForm;
import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingComponent;
import com.agnitas.exception.CharacterEncodingValidationExceptionMod;
import com.agnitas.exception.EncodingError;

import com.agnitas.beans.DynamicTag;
import com.agnitas.emm.core.mailingcontent.dto.DynContentDto;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;

/**
 * This class validates the content of a mailing against the character set
 * defined in the mailing.
 */
public class CharacterEncodingValidatorImpl implements CharacterEncodingValidator {
	
    @Override
	public void validateContentMod(DynTagDto dynTag, String charset) throws CharacterEncodingValidationExceptionMod {
		CharsetEncoder charsetEncoder = getCharsetEncoder(charset);

		Set<EncodingError> unncodeableDynamicTags = new HashSet<>();
		List<DynContentDto> contentBlocks = dynTag.getContentBlocks();
		for (DynContentDto content : contentBlocks) {
			Set<EncodingError> dynTagErrors = validateMod(content.getContent(), charsetEncoder);
			if (dynTagErrors.size() > 0) {
				for (EncodingError error : dynTagErrors) {
					unncodeableDynamicTags.add(new EncodingError(dynTag.getName(), error.getLine()));
				}
			}
		}

		if (unncodeableDynamicTags.size() > 0) {
			throw new CharacterEncodingValidationExceptionMod(new HashSet<>(), new HashSet<>(), unncodeableDynamicTags);
		}
	}
	
	/**
	 * Validates a mailing component.
	 * 
	 * @param component the mailing component to be validated
	 * @param charsetName the name of the character set to be used
	 * @return true if the mailing component passed validated otherwise false
	 */
	@Override
	public boolean validate(MailingComponent component, String charsetName) {
		CharsetEncoder charsetEncoder = getCharsetEncoder( charsetName);
		return validate(component, charsetEncoder);
	}
	
	/**
	 * Validates a dynamic tag.
	 * 
	 * @param dynTag DynamicTag to be validated
	 * @param charsetName character set to be used for validation
	 * @return true if the DynamicTag passed validated otherwise false
	 */
	@Override
	public boolean validate(DynamicTag dynTag, String charsetName) {
		CharsetEncoder charsetEncoder = getCharsetEncoder( charsetName);
		return validate(dynTag, charsetEncoder);
	}

	/**
	 * Validates a mailing component using the given CharsetEncoder.
	 * 
	 * @param component the mailing component to be validated
	 * @param charsetEncoder CharsetEncoder to be used for validation
	 * @return true if the mailing component passed validated otherwise false
	 */
	private boolean validate(MailingComponent component, CharsetEncoder charsetEncoder) {
		return validate(component.getEmmBlock(), charsetEncoder);
	}
	
	/**
	 * Validates a dynamic tag using the given CharsetEncoder.
	 * 
	 * @param dynTag DynamicTag to be validated
	 * @param charsetEncoder CharsetEncoder to be used for validation
	 * @return true if the DynamicTag passed validated otherwise false
	 */
	private boolean validate(DynamicTag dynTag, CharsetEncoder charsetEncoder) {
		Collection<DynamicTagContent> contents = dynTag.getDynContent().values();
		
		for (DynamicTagContent content : contents) {
			if (!validate( content, charsetEncoder)) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Validates the content of a DynamicTagContent objects
	 * @param content object to be validates
	 * @param charsetEncoder CharacterEncoder to be used for validation
	 * @return
	 */
	private boolean validate(DynamicTagContent content, CharsetEncoder charsetEncoder) {
		return validate(content.getDynContent(), charsetEncoder);
	}
	
	/**
	 * Validates a String using a CharsetEncoder.
	 * 
	 * @param string String to be validated
	 * @param charsetEncoder CharsetEncoder to be used for validation
	 * @return true if the String passed the validation otherwise false
	 */
	private boolean validate(String string, CharsetEncoder charsetEncoder) {
		return charsetEncoder.canEncode(string);
	}

	/**
	 * Creates a CharsetEncoder for the given charset.
	 * 
	 * @param charsetName name of character set
	 * @return CharsetEncoder for given charset
	 */
	private CharsetEncoder getCharsetEncoder(String charsetName) {
		Charset charset = Charset.forName(charsetName);
		
		return charset.newEncoder();
	}

    @Override
    public void validateMod(MailingSettingsForm form, Mailing mailing) throws CharacterEncodingValidationExceptionMod {
        Set<EncodingError> subjectErrors = new HashSet<>();
        Set<EncodingError> unencodeableMailingComponents = new HashSet<>();
        Set<EncodingError> unencodeableDynamicTags = new HashSet<>();

        validateMod(form, mailing, subjectErrors, unencodeableMailingComponents, unencodeableDynamicTags);

        if (!unencodeableMailingComponents.isEmpty() || !unencodeableDynamicTags.isEmpty() || !subjectErrors.isEmpty()) {
            throw new CharacterEncodingValidationExceptionMod(subjectErrors, unencodeableMailingComponents, unencodeableDynamicTags);
        }
    }

    private void validateMod(MailingSettingsForm form, Mailing mailing, Set<EncodingError> subjectErrors, Set<EncodingError> unencodeableMailingComponents, Set<EncodingError> unencodeableDynamicTags) {
		CharsetEncoder charsetEncoder = getCharsetEncoder(form.getEmailMediatype().getCharset());
		if (form.getEmailMediatype().isActive()) {
            validateSubject(form, subjectErrors, charsetEncoder);
            validateMod(form, unencodeableMailingComponents, charsetEncoder);
        }
		validateDynamicTagsMod(mailing, unencodeableDynamicTags, charsetEncoder);
	}

    private void validateSubject(MailingSettingsForm form, Set<EncodingError> subjectErrors, CharsetEncoder encoder) {
        subjectErrors.addAll(validateMod(form.getEmailMediatype().getSubject(), encoder));
    }

    private void validateMod(MailingSettingsForm form, Set<EncodingError> unencodeableMailingComponents, CharsetEncoder encoder) {
		String textTemplate = form.getEmailMediatype().getTextTemplate();
    	if (textTemplate != null) {
	        Set<EncodingError> textTemplateErrors =  validateMod(textTemplate, encoder);
			if (!textTemplateErrors.isEmpty()) {
	            for (EncodingError error : textTemplateErrors) {
	            	unencodeableMailingComponents.add(new EncodingError("agnText", error.getLine()));
	            }
	        }
    	}
    	
    	String htmlTemplate = form.getEmailMediatype().getHtmlTemplate();
		if (htmlTemplate != null) {
			Set<EncodingError> htmlTemplateErrors = validateMod(htmlTemplate, encoder);
			if (!htmlTemplateErrors.isEmpty()) {
	            for (EncodingError error : htmlTemplateErrors) {
	            	unencodeableMailingComponents.add(new EncodingError("agnHtml", error.getLine()));
	            }
	        }
		}
	}

    private void validateDynamicTagsMod(Mailing mailing, Set<EncodingError> unencodeableDynamicTags, CharsetEncoder charsetEncoder) {
		// No mailing? Nothing to validate!
		if (mailing == null) {
			return;
		}

		Collection<DynamicTag> dynTags = mailing.getDynTags().values();

		for (DynamicTag dynTag : dynTags) {
			unencodeableDynamicTags.addAll(validateContent(dynTag, charsetEncoder));
        }
	}

	private Set<EncodingError> validateContent(DynamicTag dynTag, CharsetEncoder charsetEncoder) {
		Set<EncodingError> unencodeableDynamicTags = new HashSet<>();
		Collection<DynamicTagContent> dynamicTagContents = dynTag.getDynContent().values();
		for (DynamicTagContent content : dynamicTagContents) {
			Set<EncodingError> dynTagErrors = validateMod(content.getDynContent(), charsetEncoder);
			if (dynTagErrors.size() > 0) {
				for (EncodingError error : dynTagErrors) {
					unencodeableDynamicTags.add(new EncodingError(dynTag.getDynName(), error.getLine()));
				}
			}
		}

		return unencodeableDynamicTags;
	}

    @Override
	public Set<EncodingError> validateMod(String string, CharsetEncoder charsetEncoder) {
    	/*
    	 * We lost information about column of un-encodable character.
    	 * The old implementation did not respect codepoints (and therefore unicode characters with more then 2 bytes length).
    	 * For example the character U+1f382 leads to reports about *two* characters, that are not
    	 * encodable. (0x1f382 is splitted to characters 0x0001 and 0xf382).
    	 */
    	
        String[] stringLines = string.split("\n");
        Set<EncodingError> errors = new HashSet<>();
        for (int lineIndex = 0; lineIndex < stringLines.length; lineIndex++) {
        	
        	if(!charsetEncoder.canEncode(stringLines[lineIndex])) {
                errors.add(new EncodingError(stringLines[lineIndex], lineIndex + 1));
        	}
        	
        }
		return errors;
	}
}
