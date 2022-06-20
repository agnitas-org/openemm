/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.target.eql.codegen.beanshell;

import com.agnitas.emm.core.target.eql.ast.BinaryOperatorBooleanEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorExpressionalEqlNode;
import com.agnitas.emm.core.target.eql.ast.BinaryOperatorRelationalEqlNode;
import com.agnitas.emm.core.target.eql.ast.RelationalInfixOperator;
import com.agnitas.emm.core.target.eql.codegen.UnhandledOperatorException;

public final class OperatorUtils {

	/**
	 * Converts operator to BeanShell operator symbol.
	 * 
	 * @param node node with boolean operator
	 * 
	 * @return BeanShell operator symbol
	 * 
	 * @throws UnhandledOperatorException in case of an unhandled operator
	 */
	public static final String beanShellOperatorSymbol(final BinaryOperatorBooleanEqlNode node) throws UnhandledOperatorException {
		return beanShellOperatorSymbol(node.getOperator());
	}

	/**
	 * Converts operator to BeanShell operator symbol.
	 * 
	 * @param operator operator
	 * 
	 * @return BeanShell operator symbol
	 * 
	 * @throws UnhandledOperatorException in case of an unhandled operator
	 */
	public static final String beanShellOperatorSymbol(final BinaryOperatorBooleanEqlNode.InfixOperator operator) throws UnhandledOperatorException {
		switch(operator) {
		case AND:	return "&&";
		case OR:	return "||";
		
		default:
			throw new UnhandledOperatorException(operator);
		}
	}

	public static final String numericBeanShellOperatorSymbol(final BinaryOperatorRelationalEqlNode node) throws UnhandledOperatorException {
		return numericBeanShellOperatorSymbol(node.getOperator());
	}

	public static final String numericBeanShellOperatorSymbol(final RelationalInfixOperator operator) throws UnhandledOperatorException {
		switch(operator) {
		case EQ:	return "==";
		case GEQ:	return ">=";
		case GT:	return ">";
		case LEQ:	return "<=";
		case LT:	return "<";
		case NEQ:	return "!=";
			
		default:
			throw new UnhandledOperatorException(operator);
		}
	}
	
	public static final String numericBeanShellOperatorSymbol(final BinaryOperatorExpressionalEqlNode node) throws UnhandledOperatorException {
		return numericBeanShellOperatorSymbol(node.getOperator());
	}
	
	public static final String numericBeanShellOperatorSymbol(final BinaryOperatorExpressionalEqlNode.InfixOperator operator) throws UnhandledOperatorException {
		switch(operator) {
		case ADD:	return "+";
		case SUB:	return "-";
		case MUL:	return "*";
		case DIV:	return "/";
		case MOD:	return "%";

		default:
			throw new UnhandledOperatorException(operator);
		}
	}

}
