/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

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

	private OperatorUtils() {

	}

	/**
	 * Converts operator to BeanShell operator symbol.
	 * 
	 * @param node node with boolean operator
	 * 
	 * @return BeanShell operator symbol
	 */
	public static String beanShellOperatorSymbol(BinaryOperatorBooleanEqlNode node) {
		return beanShellOperatorSymbol(node.getOperator());
	}

	/**
	 * Converts operator to BeanShell operator symbol.
	 * 
	 * @param operator operator
	 * 
	 * @return BeanShell operator symbol
	 */
	public static String beanShellOperatorSymbol(BinaryOperatorBooleanEqlNode.InfixOperator operator) {
        return switch (operator) {
            case AND -> "&&";
            case OR -> "||";
        };
	}

	public static String numericBeanShellOperatorSymbol(BinaryOperatorRelationalEqlNode node) {
		return numericBeanShellOperatorSymbol(node.getOperator());
	}

	public static String numericBeanShellOperatorSymbol(RelationalInfixOperator operator) {
        return switch (operator) {
            case EQ -> "==";
            case GEQ -> ">=";
            case GT -> ">";
            case LEQ -> "<=";
            case LT -> "<";
            case NEQ -> "!=";
        };
	}
	
	public static String numericBeanShellOperatorSymbol(BinaryOperatorExpressionalEqlNode node) {
		return numericBeanShellOperatorSymbol(node.getOperator());
	}
	
	public static String numericBeanShellOperatorSymbol(BinaryOperatorExpressionalEqlNode.InfixOperator operator) {
        return switch (operator) {
            case ADD -> "+";
            case SUB -> "-";
            case MUL -> "*";
            case DIV -> "/";
            case MOD -> "%";
        };
	}

}
