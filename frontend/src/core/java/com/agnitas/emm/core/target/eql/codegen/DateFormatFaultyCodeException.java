/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

import com.agnitas.emm.core.target.eql.ast.AbstractEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.TimestampExpressionNode;

public class DateFormatFaultyCodeException extends FaultyCodeException {
	private static final long serialVersionUID = 419496062568416660L;
	
	private final String dateFormat;
	
	public DateFormatFaultyCodeException(final BinaryOperatorRelationalEqlNode node, final Throwable cause) {
		this(node.getDateFormat(), node, cause);
	}
	
	public DateFormatFaultyCodeException(final TimestampExpressionNode node, final Throwable cause) {
		this(node.getDateFormat(), node, cause);
	}

	private DateFormatFaultyCodeException(final String dateFormat, final AbstractEqlNode node, final Throwable cause) {
		super(node, String.format("Error parsing date format '%s'", dateFormat), cause);
		
		this.dateFormat = dateFormat;
	}
	
	public String getDateFormat() {
		return this.dateFormat;
	}
}
