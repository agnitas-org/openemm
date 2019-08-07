/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.hashtag;

import java.util.Map;

import com.agnitas.emm.core.hashtag.exception.HashTagException;
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
	
	public abstract String handleWithParametersInternal(final HashTagContext context, final String tagName, final Map<String, String> parameters) throws HashTagException;

}
