/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target;

import org.agnitas.target.impl.TargetNodeDate;
import org.agnitas.target.impl.TargetNodeNumeric;
import org.agnitas.target.impl.TargetNodeString;

public interface TargetNodeFactory {
	
	/**
	 * Creates a new target group node for numerical fields without any properties set.
	 * 
	 * @return new target node for numerical fields
	 */
	public TargetNodeNumeric newNumericNode();
	
	/**
	 * Creates a new target group node for numerical fields with properties set to given values.
	 * 
	 * @param chainOperator
	 * @param parenthesisOpened 
	 * @param primaryField
	 * @param primaryType
	 * @param primaryOperator
	 * @param primaryValue
	 * @param secondaryOperator
	 * @param secondaryValue
	 * @param parenthesisClosed
	 * 
	 * @return new target group node for numerical fields
	 */
	public TargetNodeNumeric newNumericNode(int chainOperator, 
											int parenthesisOpened, 
											String primaryField, 
											String primaryType,
											int primaryOperator,
											String primaryValue,
											int secondaryOperator,
											int secondaryValue,
											int parenthesisClosed);
	
	/**
	 * Creates a new target group node for text fields without any properties set.
	 * 
	 * @return new target node for text fields
	 */
	public TargetNodeString newStringNode();
	
	/**
	 * Creates a new target group node for text fields with properties set to given values.
	 * 
	 * @param chainOperator
	 * @param parenthesisOpened
	 * @param primaryField
	 * @param primaryType
	 * @param primaryOperator
	 * @param primaryValue
	 * @param parenthesisClosed
	 * 
	 * @return new target node for text fields
	 */
	public TargetNodeString newStringNode(	int chainOperator,
											int parenthesisOpened,
											String primaryField,
											String primaryType,
											int primaryOperator,
											String primaryValue,
											int parenthesisClosed);

	/**
	 * Creates a new target group node for date fields without any properties set.
	 * 
	 * @return new target node for date fields
	 */
	public TargetNodeDate newDateNode();
	
	/**
	 * Creates a new target group node for date fields with properties set to given values.
	 * 
	 * @param chainOperator
	 * @param parenthesisOpened
	 * @param primaryField
	 * @param primaryType
	 * @param primaryOperator
	 * @param dateFormat
	 * @param primaryValue
	 * @param parenthesisClosed
	 * 
	 * @return new target node for date fields
	 */
	public TargetNodeDate newDateNode(		int chainOperator,
											int parenthesisOpened,
											String primaryField,
											String primaryType,
											int primaryOperator,
											String dateFormat,
											String primaryValue,
											int parenthesisClosed);
}
