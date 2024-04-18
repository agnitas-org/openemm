/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.agnitas.emm.core.hashtag.exception.EmptyRequiredParameterHashTagException;
import com.agnitas.emm.core.hashtag.exception.HashTagException;
import com.agnitas.emm.core.hashtag.exception.MissingRequiredParameterHashTagException;
import com.agnitas.emm.core.hashtag.keyvaluelist.KeyValueListParser;
import com.agnitas.emm.core.hashtag.keyvaluelist.KeyValueListParserException;

public abstract class AbstractColonHashTagWithParameters extends AbstractColonHashTag {

	@Override
	public String handleInternal(final HashTagContext context, final String tagName, final String appendix) throws HashTagException {
		try {
			final Map<String, String> parameters = KeyValueListParser.parseKeyValueList(appendix);
			
			return handleWithParametersInternal(context, tagName, parameters);
		} catch(final KeyValueListParserException e) {
			throw new HashTagException("Malformed parameter list: " + appendix, e);
		}
	}

	/**
	 * Returns the value of the required parameter.
	 * 
	 * @param name parameter name
	 * @param parameters parameters map
	 * 
	 * @return value of parameter
	 * 
	 * @throws MissingRequiredParameterHashTagException if parameter is not defined
	 */
	protected static final String getRequiredParameter(final String name, final Map<String, String> parameters) throws MissingRequiredParameterHashTagException {
		final String value = parameters.get(name);
		
		if(value == null) {
			throw new MissingRequiredParameterHashTagException(name);
		}

		return value;
	}
	
	/**
	 * Returns the value of the non-empty required parameter.
	 * 
	 * @param name parameter name
	 * @param parameters parameters map
	 * 
	 * @return value of parameter
	 * 
	 * @throws MissingRequiredParameterHashTagException if parameter is not defined
	 * @throws EmptyRequiredParameterHashTagException if parameter value is empty
	 */
	protected static final String getRequiredNonEmptyParameter(final String name, final Map<String, String> parameters) throws MissingRequiredParameterHashTagException, EmptyRequiredParameterHashTagException {
		final String value = getRequiredParameter(name, parameters);
		
		if (StringUtils.isEmpty(value)) {
			throw new EmptyRequiredParameterHashTagException(name);
		}

		return value;
	}

	
	public abstract String handleWithParametersInternal(final HashTagContext context, final String tagName, final Map<String, String> parameters) throws HashTagException;

}
