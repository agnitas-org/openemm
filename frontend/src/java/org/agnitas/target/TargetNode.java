/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.agnitas.target.impl.TargetOperatorImpl;
import org.apache.commons.lang3.StringUtils;

public abstract class TargetNode {
    public static final int CHAIN_OPERATOR_NONE = 0;
    public static final int CHAIN_OPERATOR_AND = 1;
    public static final int CHAIN_OPERATOR_OR = 2;

    public static final TargetOperator OPERATOR_EQ = new TargetOperatorImpl( "eq", "=", "==", 1);
    public static final TargetOperator OPERATOR_NEQ = new TargetOperatorImpl( "neq", "!=", "!=", 2);
    public static final TargetOperator OPERATOR_GT = new TargetOperatorImpl( "gt", ">", ">", 3);
    public static final TargetOperator OPERATOR_LT = new TargetOperatorImpl( "lt", "<", "<", 4);
    public static final TargetOperator OPERATOR_LIKE = new TargetOperatorImpl( "like", "LIKE", null, 5);
    public static final TargetOperator OPERATOR_NLIKE = new TargetOperatorImpl( "not_like", "NOT LIKE", null, 6);
    public static final TargetOperator OPERATOR_MOD = new TargetOperatorImpl( "mod", "mod", "%", 7);
    public static final TargetOperator OPERATOR_IS = new TargetOperatorImpl( "is", "IS", "IS", 8);
    public static final TargetOperator OPERATOR_LT_EQ = new TargetOperatorImpl( "leq", "<=", "<=", 9);
    public static final TargetOperator OPERATOR_GT_EQ = new TargetOperatorImpl( "geq", ">=", ">=", 10);
    public static final TargetOperator OPERATOR_YES = new TargetOperatorImpl( "yes", "--special handling--", "--special handling--", 11);
    public static final TargetOperator OPERATOR_NO = new TargetOperatorImpl(  "no", "--special handling--", "--special handling--", 12);
    public static final TargetOperator OPERATOR_CONTAINS = new TargetOperatorImpl("contains", "CONTAINS", null, 13);
    public static final TargetOperator OPERATOR_NOT_CONTAINS = new TargetOperatorImpl("not_contains", "NOT CONTAINS", null, 14);
    public static final TargetOperator OPERATOR_STARTS_WITH = new TargetOperatorImpl("starts_with", "STARTS WITH", null, 15);
    public static final TargetOperator OPERATOR_NOT_STARTS_WITH = new TargetOperatorImpl("not_starts_with", "NOT STARTS WITH", null, 16);

    public static final TargetOperator[] ALL_OPERATORS = {
    	OPERATOR_EQ,
    	OPERATOR_NEQ,
    	OPERATOR_GT,
    	OPERATOR_LT,
    	OPERATOR_LIKE,
    	OPERATOR_NLIKE,
    	OPERATOR_MOD,
    	OPERATOR_IS,
    	OPERATOR_LT_EQ,
    	OPERATOR_GT_EQ,
    	OPERATOR_YES,
    	OPERATOR_NO,
        OPERATOR_CONTAINS,
        OPERATOR_NOT_CONTAINS,
        OPERATOR_STARTS_WITH,
        OPERATOR_NOT_STARTS_WITH
    };

    public TargetOperator[] typeOperators = {
    	OPERATOR_EQ,
    	OPERATOR_NEQ,
    	OPERATOR_GT,
    	OPERATOR_LT,
    	OPERATOR_LIKE,
    	OPERATOR_NLIKE,
    	null,
    	OPERATOR_IS,
    	OPERATOR_LT_EQ,
    	OPERATOR_GT_EQ,
    	null,
    	null,
        null,
        null,
        null,
        null
    };
    
    public static final TargetOperator[] OPERATORS_ALLOWED_AFTER_MOD_OPERATOR = {
    	OPERATOR_EQ,
    	OPERATOR_NEQ,
    	OPERATOR_GT,
    	OPERATOR_LT,
    	OPERATOR_LT_EQ,
    	OPERATOR_GT_EQ
    };

    private static final Map<TargetOperator, TargetOperator> OPPOSITE_OPERATORS;
    static {
    	OPPOSITE_OPERATORS = new HashMap<>();
    	
    	OPPOSITE_OPERATORS.put(OPERATOR_EQ, OPERATOR_NEQ);
    	OPPOSITE_OPERATORS.put(OPERATOR_NEQ, OPERATOR_EQ);

    	OPPOSITE_OPERATORS.put(OPERATOR_GT, OPERATOR_LT_EQ);
    	OPPOSITE_OPERATORS.put(OPERATOR_LT_EQ, OPERATOR_GT);

        OPPOSITE_OPERATORS.put(OPERATOR_LT, OPERATOR_GT_EQ);
        OPPOSITE_OPERATORS.put(OPERATOR_GT_EQ, OPERATOR_LT);

        OPPOSITE_OPERATORS.put(OPERATOR_LIKE, OPERATOR_NLIKE);
        OPPOSITE_OPERATORS.put(OPERATOR_NLIKE, OPERATOR_LIKE);

        OPPOSITE_OPERATORS.put(OPERATOR_YES, OPERATOR_NO);
        OPPOSITE_OPERATORS.put(OPERATOR_NO, OPERATOR_YES);

        OPPOSITE_OPERATORS.put(OPERATOR_CONTAINS, OPERATOR_NOT_CONTAINS);
        OPPOSITE_OPERATORS.put(OPERATOR_NOT_CONTAINS, OPERATOR_CONTAINS);

        OPPOSITE_OPERATORS.put(OPERATOR_STARTS_WITH, OPERATOR_NOT_STARTS_WITH);
        OPPOSITE_OPERATORS.put(OPERATOR_NOT_STARTS_WITH, OPERATOR_STARTS_WITH);
    }

    public static TargetOperator getOppositeOperator(TargetOperator operator) {
        return OPPOSITE_OPERATORS.get(operator);
    }

    public static boolean isInequalityOperator(TargetOperator operator) {
        return operator == OPERATOR_NEQ ||
                operator == OPERATOR_NLIKE ||
                operator == OPERATOR_NOT_CONTAINS ||
                operator == OPERATOR_NOT_STARTS_WITH;
    }

    public static TargetOperator getOperatorByCode(final int operatorCode) {
        return Arrays.stream(ALL_OPERATORS)
                .filter(operator -> operator.getOperatorCode() == operatorCode)
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if an operator follows the three-valued logic (NULL values are excluded for both direct and negative expression).
     * @param operator an operator to check.
     * @return {@code true} for operators which are affected by a three-valued logic and {@code false} for others.
     */
    public static boolean isThreeValuedLogicOperator(TargetOperator operator) {
        return operator == OPERATOR_EQ ||
               operator == OPERATOR_NEQ ||
               operator == OPERATOR_GT ||
               operator == OPERATOR_GT_EQ ||
               operator == OPERATOR_LT ||
               operator == OPERATOR_LT_EQ ||
               operator == OPERATOR_LIKE ||
               operator == OPERATOR_NLIKE ||
               operator == OPERATOR_CONTAINS ||
               operator == OPERATOR_NOT_CONTAINS ||
               operator == OPERATOR_STARTS_WITH ||
               operator == OPERATOR_NOT_STARTS_WITH;
    }

    public static TargetOperator[] getAllowedSecondaryOperatorsForPrimaryOperator(TargetOperator primaryOperator) {
    	if (primaryOperator == OPERATOR_MOD) {
    		return OPERATORS_ALLOWED_AFTER_MOD_OPERATOR;
    	} else {
    		return new TargetOperator[0];
    	}
    }
    
    public static TargetOperator findSecondaryOperatorForMod(final int operatorCode) {
    	for(TargetOperator operator : OPERATORS_ALLOWED_AFTER_MOD_OPERATOR) {
    		if(operator.getOperatorCode() == operatorCode) {
    			return operator;
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Initializes the arrays OPERATORS and BSH_OPERATORS 
     */
    protected abstract void initializeOperatorLists();
    
      /** 
       * Getter for property openBracketBefore.
       *
     * @return Value of property openBracketBefore.
     */
    public abstract boolean isOpenBracketBefore();
    
    /** 
     * Setter for property openBracketBefore.
     *
     * @param openBracketBefore New value of property openBracketBefore.
     */
    public abstract void setOpenBracketBefore(boolean openBracketBefore);
    
    /**
     * Getter for property closeBracketAfter.
     *
     * @return Value of property closeBracketAfter.
     */
    public abstract boolean isCloseBracketAfter();
    
    /**
     * Setter for property closeBracketAfter.
     *
     * @param closeBracketAfter New value of property closeBracketAfter.
     */
    public abstract void setCloseBracketAfter(boolean closeBracketAfter);
    
    /**
     * Getter for property chainOperator.
     *
     * @return Value of property chainOperator.
     */
    public abstract int getChainOperator();
    
    /**
     * Setter for property chainOperator.
     *
     * @param chainOperator New value of property chainOperator.
     */
    public abstract void setChainOperator(int chainOperator);
    
    /**
     * Generates bsh
     */
    public abstract String generateBsh();
    
    /** 
     * Getter for property primaryOperator.
     *
     * @return Value of property primaryOperator.
     */
    public abstract int getPrimaryOperator();
    
    /**
     * Setter for property primaryOperator.
     *
     * @param primaryOperator New value of property primaryOperator.
     */
    public abstract void setPrimaryOperator(int primaryOperator);
    
    /**
     * Getter for property primaryField.
     *
     * @return Value of property primaryField.
     */
    public abstract String getPrimaryField();
    
    /**
     * Setter for property primaryField.
     *
     * @param primaryField New value of property primaryField.
     */
    public abstract void setPrimaryField(String primaryField);
    
    /**
     * Getter for property primaryFieldType.
     *
     * @return Value of property primaryFieldType.
     */
    public abstract String getPrimaryFieldType();
    
    /**
     * Setter for property primaryFieldType.
     *
     * @param primaryFieldType New value of property primaryFieldType.
     */
    public abstract void setPrimaryFieldType(String primaryFieldType);
    
    /**
     * Getter for property primaryValue.
     *
     * @return Value of property primaryValue.
     */
    public abstract String getPrimaryValue();
    
    /**
     * Setter for property primaryValue.
     *
     * @param primaryValue New value of property primaryValue.
     */
    public abstract void setPrimaryValue(String primaryValue);   
    
    /**
     * Compares two nodes on equality.
     * 
     * @param node node to compare with this node
     * 
     * @return true, if nodes are equal
     */
    public boolean equalNodes(TargetNode node) {
    	if(sameNodeType(node)) {
    		return this.isOpenBracketBefore() == node.isOpenBracketBefore()
    				&& this.isCloseBracketAfter() == node.isCloseBracketAfter()
    				&& this.getChainOperator() == node.getChainOperator()
    				&& this.getPrimaryOperator() == node.getPrimaryOperator()
    				&& StringUtils.equalsIgnoreCase(this.getPrimaryField(), node.getPrimaryField())
//    				&& StringUtils.equals(this.getPrimaryFieldType(), node.getPrimaryField())
    				&& StringUtils.equalsIgnoreCase(this.getPrimaryValue(), node.getPrimaryValue()); 
    	} else {
    		return false;
    	}
    }
    
    /**
     * Compares types of nodes.
     * 
     * @param node node to compare type
     * 
     * @return true, if types of nodes are equals
     */
    public abstract boolean sameNodeType(TargetNode node);
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(typeOperators);
		return result;
	}
    
    @Override
    public boolean equals(Object o) {
    	if (o instanceof TargetNode) {
    		return equalNodes((TargetNode) o);
    	} else {
    		return false;
    	}
    }
    
    /**
     * Make a given String save for SQL
     * This is not the very best way, but keep the most attacks outside
     * TODO: Completely remove TargetNode classes in future
     */
    public static String getSQLSafeString(String input) {
		if (input == null) {
			return " ";
		} else {
			return input.replace("'", "''");
		}
    }
}
