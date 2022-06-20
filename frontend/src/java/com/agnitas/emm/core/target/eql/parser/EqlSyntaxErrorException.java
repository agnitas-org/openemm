/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.parser;

import java.util.Collections;
import java.util.List;

public class EqlSyntaxErrorException extends EqlParserException {
	private static final long serialVersionUID = -5676929961591931296L;
	
	private final List<EqlSyntaxError> errors;
	
	public EqlSyntaxErrorException(List<EqlSyntaxError> errors) {
		super(errors.size() == 1 ? "Syntax error" : "Syntax errors");
		
		this.errors = Collections.unmodifiableList(errors);
	}
	
	public List<EqlSyntaxError> getErrors() {
		return this.errors;
	}
}
