/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import org.apache.commons.lang3.StringUtils;

public class WorkflowRule {
	private int primaryOperator;
	private String primaryValue;
	private int chainOperator;
	private int parenthesisOpened;
	private int parenthesisClosed;

	public int getPrimaryOperator() {
		return primaryOperator;
	}

	public void setPrimaryOperator(int primaryOperator) {
		this.primaryOperator = primaryOperator;
	}

	public String getPrimaryValue() {
		return primaryValue;
	}

	public void setPrimaryValue(String primaryValue) {
		this.primaryValue = primaryValue;
	}

	public int getChainOperator() {
		return chainOperator;
	}

	public void setChainOperator(int chainOperator) {
		this.chainOperator = chainOperator;
	}

	public int getParenthesisOpened() {
		return parenthesisOpened;
	}

	public void setParenthesisOpened(int parenthesisOpened) {
		this.parenthesisOpened = parenthesisOpened;
	}

	public int getParenthesisClosed() {
		return parenthesisClosed;
	}

	public void setParenthesisClosed(int parenthesisClosed) {
		this.parenthesisClosed = parenthesisClosed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + chainOperator;
		result = prime * result + parenthesisClosed;
		result = prime * result + parenthesisOpened;
		result = prime * result + primaryOperator;
		result = prime * result + ((primaryValue == null) ? 0 : primaryValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		WorkflowRule rhs = (WorkflowRule) o;

		return primaryOperator == rhs.primaryOperator &&
				StringUtils.equals(primaryValue, rhs.primaryValue) &&
				chainOperator == rhs.chainOperator &&
				parenthesisOpened == rhs.parenthesisOpened &&
				parenthesisClosed == rhs.parenthesisClosed;
	}
}
