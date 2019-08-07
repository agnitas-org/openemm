/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.preview;

import java.util.Locale;

import org.agnitas.util.AgnUtils;

import com.agnitas.messages.I18nString;

/**
 * AgnTagError descripes an errorneous agnTag in a text component.
 */
public class AgnTagError extends RuntimeException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8609478855972730080L;
	
	/**
	 * The Enum AgnTagErrorKey.
	 */
	public enum AgnTagErrorKey {
		exceptionWhileChecking("error.agntag.exceptionWhileChecking"),
		
		missingClosingBracket("error.agntag.missingClosingBracket"),
		invalidParameterSyntax("error.agntag.invalidParameterSyntax"),
		invalidClosingAgnDynTag_notOpened("error.agntag.invalidClosingAgnDynTag.notOpened"),
		invalidClosingAgnDynTag_notMatchingLastOpenedName("error.agntag.invalidClosingAgnDynTag.notMatchingLastOpenedName"),
		unknownAgnTag("error.agntag.unknownAgnTag"),
		invalidAgnTagSlashes("error.agntag.invalidAgnTagSlashes"),
		missingAgnTagClosingSlash("error.agntag.missingAgnTagClosingSlash"),
		invalidClosingAgnTag("error.agntag.invalidClosingAgnTag"),
		missingParameter("error.agntag.missingParameter"),
		missingClosingAgnDynTag("error.agntag.missingClosingAgnDynTag"),
		unwrappedAgnDvalueTag("error.agntag.unwrappedValueTag"),
		
		// Parameter Errors
		invalidWhitespace("error.agntag.parameter.invalidWhitespace"),
		invalidQuotedKey("error.agntag.parameter.invalidQuotedKey"),
		invalidEmptyKey("error.agntag.parameter.invalidEmptyKey"),
		invalidUnquotedValue("error.agntag.parameter.invalidUnquotedValue"),
		unexpectedEndOfValue("error.agntag.parameter.unexpectedEndOfValue"),
		unexpectedEndOfKey("error.agntag.parameter.unexpectedEndOfKey"),
		duplicateKey("error.agntag.parameter.duplicateKey"),
		invalidQuotesInValue("error.agntag.parameter.invalidQuotesInValue");
		
		/**
		  * Instantiates a new agn tag error key.
		  *
		  * @param messageKey the message key
		  */
		private AgnTagErrorKey(String messageKey) {
			this.messageKey = messageKey;
		}
		
		/** The message key. */
		private final String messageKey;
		
		/**
		 * Gets the message key.
		 *
		 * @return the message key
		 */
		public String getMessageKey() {
			return messageKey;
		}
	}

	/** The tag name. */
	private String tagName = "Unknown";
	
	/** The full agn tag text. */
	private String fullAgnTagText = "<empty>";
	
	/** The error key. */
	private AgnTagErrorKey errorKey;
	
	/** The additional error data. */
	private String[] additionalErrorData;
	
	/** The line number. */
	private int lineNumber = -1;
	
	/** The position within line. */
	private int positionWithinLine = -1;
	
	/**
	 * Instantiates a new agn tag error.
	 *
	 * @param tagName the tag name
	 * @param fullAgnTagText the full agn tag text
	 * @param errorKey the error key
	 * @param lineNumber the line number
	 * @param positionWithinLine the position within line
	 * @param additionalErrorData the additional error data
	 */
	public AgnTagError(String tagName, String fullAgnTagText, AgnTagErrorKey errorKey, int lineNumber, int positionWithinLine, String... additionalErrorData) {
		this.additionalErrorData = additionalErrorData;
		this.tagName = tagName;
		this.fullAgnTagText = fullAgnTagText;
		this.errorKey = errorKey;
		this.lineNumber = lineNumber;
		this.positionWithinLine = positionWithinLine;
	}

	/**
	 * Instantiates a new agn tag error.
	 *
	 * @param tagName the tag name
	 * @param fullAgnTagText the full agn tag text
	 * @param errorKey the error key
	 * @param contentText the content text
	 * @param textPosition the text position
	 * @param additionalErrorData the additional error data
	 */
	public AgnTagError(String tagName, String fullAgnTagText, AgnTagErrorKey errorKey, String contentText, int textPosition, String... additionalErrorData) {
		this.additionalErrorData = additionalErrorData;
		this.tagName = tagName;
		this.fullAgnTagText = fullAgnTagText;
		this.errorKey = errorKey;
		setTextPosition(contentText, textPosition);
	}

	/**
	 * Instantiates a new agn tag error.
	 *
	 * @param tagName the tag name
	 * @param fullAgnTagText the full agn tag text
	 * @param errorKey the error key
	 * @param additionalErrorData the additional error data
	 */
	public AgnTagError(String tagName, String fullAgnTagText, AgnTagErrorKey errorKey, String... additionalErrorData) {
		this.additionalErrorData = additionalErrorData;
		this.tagName = tagName;
		this.fullAgnTagText = fullAgnTagText;
		this.errorKey = errorKey;
	}

	/**
	 * Instantiates a new agn tag error.
	 * Used for interim data of paramereter errors
	 *
	 * @param errorKey the error key
	 * @param additionalErrorData the additional error data
	 */
	public AgnTagError(AgnTagErrorKey errorKey, String... additionalErrorData) {
		this.additionalErrorData = additionalErrorData;
		this.errorKey = errorKey;
	}
	
	/**
	 * Gets the localized message.
	 *
	 * @param locale the locale
	 * @return the localized message
	 */
	public String getLocalizedMessage(Locale locale) {
		return I18nString.getLocaleString(errorKey.getMessageKey(), locale, getAdditionalErrorDataWithLineInfo());
	}

	/**
	 * Gets the tag name.
	 *
	 * @return the tag name
	 */
	public String getTagName() {
		return tagName;
	}

	/**
	 * Gets the full agn tag text.
	 *
	 * @return the full agn tag text
	 */
	public String getFullAgnTagText() {
		return fullAgnTagText;
	}

	/**
	 * Gets the error key.
	 *
	 * @return the error key
	 */
	public AgnTagErrorKey getErrorKey() {
		return errorKey;
	}

	/**
	 * Gets the additional error data.
	 *
	 * @return the additional error data
	 */
	public String[] getAdditionalErrorData() {
		return additionalErrorData;
	}

	/**
	 * Gets the additional error data with line info.
	 *
	 * @return the additional error data with line info
	 */
	public Object[] getAdditionalErrorDataWithLineInfo() {
		Object[] extendedErrorData;
		if (lineNumber >= 0 && positionWithinLine >= 0) {
			if (additionalErrorData != null) {
				extendedErrorData = new Object[additionalErrorData.length + 2];
				for (int i = 0; i < additionalErrorData.length; i++) {
					extendedErrorData[i] = additionalErrorData[i];
				}
			} else {
				extendedErrorData = new Object[2];
			}
			extendedErrorData[extendedErrorData.length - 2] = lineNumber;
			extendedErrorData[extendedErrorData.length - 1] = positionWithinLine;
		} else if (lineNumber >= 0) {
			if (additionalErrorData != null) {
				extendedErrorData = new Object[additionalErrorData.length + 1];
				for (int i = 0; i < additionalErrorData.length; i++) {
					extendedErrorData[i] = additionalErrorData[i];
				}
			} else {
				extendedErrorData = new Object[1];
			}
			extendedErrorData[extendedErrorData.length - 1] = lineNumber;
		} else {
			if (additionalErrorData != null) {
				extendedErrorData = new Object[additionalErrorData.length];
				for (int i = 0; i < additionalErrorData.length; i++) {
					extendedErrorData[i] = additionalErrorData[i];
				}
			} else {
				extendedErrorData = new Object[0];
			}
		}
		
		return extendedErrorData;
	}

	/**
	 * Gets the line number.
	 *
	 * @return the line number
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * Gets the position within line.
	 *
	 * @return the position within line
	 */
	public int getPositionWithinLine() {
		return positionWithinLine;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public void setFullAgnTagText(String fullAgnTagText) {
		this.fullAgnTagText = fullAgnTagText;
	}

	public void setAdditionalErrorData(String[] additionalErrorData) {
		this.additionalErrorData = additionalErrorData;
	}
	
	public void setTextPosition(String contentText, int textPosition) {
		this.lineNumber = AgnUtils.getLineNumberOfTextposition(contentText, textPosition);
		this.positionWithinLine = textPosition - AgnUtils.getStartIndexOfLineAtIndex(contentText, textPosition) + 1;
	}
}
