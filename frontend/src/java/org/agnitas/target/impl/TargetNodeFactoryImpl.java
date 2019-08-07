/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target.impl;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetNodeFactory;

public class TargetNodeFactoryImpl implements TargetNodeFactory {

	@Override
	public TargetNodeDate newDateNode() {
		return TargetNodeDate.withDefaultDateFormat(ConfigService.isOracleDB());
	}

	@Override
	public TargetNodeDate newDateNode(int chainOperator, int parenthesisOpened,
			String primaryField, String primaryType, int primaryOperator,
			String dateFormat, String primaryValue, int parenthesisClosed) {
		
		TargetNodeDate targetNode = newDateNode();

		targetNode.setChainOperator(chainOperator);
		targetNode.setOpenBracketBefore(parenthesisOpened == 1);
		targetNode.setPrimaryField(primaryField);
		targetNode.setPrimaryFieldType(primaryType);
		targetNode.setPrimaryOperator(primaryOperator);
		
		if( !dateFormat.equals(""))
			targetNode.setDateFormat(dateFormat);
		
		targetNode.setPrimaryValue(primaryValue);
		targetNode.setCloseBracketAfter(parenthesisClosed == 1);
		
		return targetNode;
	}

	@Override
	public TargetNodeNumeric newNumericNode() {
		return new TargetNodeNumeric();
	}

	@Override
	public TargetNodeNumeric newNumericNode(int chainOperator,
			int parenthesisOpened, String primaryField, String primaryType,
			int primaryOperator, String primaryValue, int secondaryOperator,
			int secondaryValue, int parenthesisClosed) {

		TargetNodeNumeric targetNode = newNumericNode();
		
		targetNode.setChainOperator(chainOperator);
		targetNode.setOpenBracketBefore(parenthesisOpened == 1);
		targetNode.setPrimaryField(primaryField);
		targetNode.setPrimaryFieldType(primaryType);
		targetNode.setPrimaryOperator(primaryOperator);
		targetNode.setPrimaryValue(primaryValue);
		targetNode.setCloseBracketAfter(parenthesisClosed == 1);

		if(targetNode.getPrimaryOperator()==TargetNode.OPERATOR_MOD.getOperatorCode()) {
			targetNode.setSecondaryOperator(secondaryOperator);
			targetNode.setSecondaryValue(secondaryValue);
        }
		
		return targetNode;
	}

	@Override
	public TargetNodeString newStringNode() {
		return new TargetNodeString();
	}

	@Override
	public TargetNodeString newStringNode(int chainOperator,
			int parenthesisOpened, String primaryField, String primaryType,
			int primaryOperator, String primaryValue, int parenthesisClosed) {
		
		TargetNodeString targetNode = newStringNode();
		
		targetNode.setChainOperator(chainOperator);
		targetNode.setOpenBracketBefore(parenthesisOpened == 1);
		targetNode.setPrimaryField(primaryField);
		targetNode.setPrimaryFieldType(primaryType);
		targetNode.setPrimaryOperator(primaryOperator);
		targetNode.setPrimaryValue(primaryValue);
		targetNode.setCloseBracketAfter(parenthesisClosed == 1);
		
		return targetNode;
	}

}
