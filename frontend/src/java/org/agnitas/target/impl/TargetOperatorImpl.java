/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target.impl;

import org.agnitas.target.TargetOperator;

public class TargetOperatorImpl implements TargetOperator {

	private final String key;
	private final String symbol;
	private final String bshSymbol;
	private final int code;
	
	public TargetOperatorImpl(String key, String symbol, String bshSymbol, int code) {
		super();
		this.symbol = symbol;
		this.bshSymbol = bshSymbol;
		this.code = code;
		this.key = key;
	}

	@Override
	public String getOperatorKey() {
		return this.key;
	}
	
	@Override
	public String getBshOperatorSymbol() {
		return this.bshSymbol;
	}

	@Override
	public int getOperatorCode() {
		return this.code;
	}

	@Override
	public String getOperatorSymbol() {
		return this.symbol;
	}

	@Override
	public int hashCode() {
		return code;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TargetOperatorImpl) {
			return this == o || ((TargetOperatorImpl) o).code == code;
		}
		return false;
	}
}
