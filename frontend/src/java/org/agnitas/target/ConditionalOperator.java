/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ConditionalOperator {
	
	EQ(1, "="),
	NEQ(2, "<>"),
	GT(3, ">"),
	LT(4, "<"),
	LIKE(5, "LIKE"),
	NOT_LIKE(6, "NOT LIKE"),
	MOD(7, "MOD"),
	IS(8, "IS"),
	LEQ(9, "<="),
	GEQ(10, ">="),
	YES(11, null),
	NO(12, null),
	CONTAINS(13, "CONTAINS"),
	NOT_CONTAINS(14, "NOT CONTAINS"),
	STARTS_WITH(15, "STARTS WITH"),
	NOT_STARTS_WITH(16, "NOT STARTS WITH");

    private static final Map<ConditionalOperator, ConditionalOperator> OPPOSITE_OPERATORS = new HashMap<>();
    static {
        OPPOSITE_OPERATORS.put(EQ, NEQ);
        OPPOSITE_OPERATORS.put(NEQ, EQ);

        OPPOSITE_OPERATORS.put(GT, LEQ);
        OPPOSITE_OPERATORS.put(LEQ, GT);

        OPPOSITE_OPERATORS.put(LT, GEQ);
        OPPOSITE_OPERATORS.put(GEQ, LT);

        OPPOSITE_OPERATORS.put(LIKE, NOT_LIKE);
        OPPOSITE_OPERATORS.put(NOT_LIKE, LIKE);

        OPPOSITE_OPERATORS.put(YES, NO);
        OPPOSITE_OPERATORS.put(NO, YES);

        OPPOSITE_OPERATORS.put(CONTAINS, NOT_CONTAINS);
        OPPOSITE_OPERATORS.put(NOT_CONTAINS, CONTAINS);

        OPPOSITE_OPERATORS.put(STARTS_WITH, NOT_STARTS_WITH);
        OPPOSITE_OPERATORS.put(NOT_STARTS_WITH, STARTS_WITH);
    }

	/*
	Operators as found in TargetNode:
	
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
	 */
	
	
	private final int operatorCode;
	private final String eqlSymbol;
	
	ConditionalOperator(final int operatorCode, final String eqlSymbol) {
		this.operatorCode = operatorCode;
		this.eqlSymbol = eqlSymbol;
	}
	
	public final int getOperatorCode() {
		return this.operatorCode;
	}
	
	public final String getEqlSymbol() {
		return this.eqlSymbol;
	}

    public boolean isThreeValuedLogicOperator() {
        return this == EQ ||
                this == NEQ ||
                this == GT ||
                this == GEQ ||
                this == LT ||
                this == LEQ ||
                this == LIKE ||
                this == NOT_LIKE ||
                this == CONTAINS ||
                this == NOT_CONTAINS ||
                this == STARTS_WITH ||
                this == NOT_STARTS_WITH;
    }

    public boolean isInequalityOperator() {
        return this == NEQ ||
                this == NOT_LIKE ||
                this == NOT_CONTAINS ||
                this == NOT_STARTS_WITH;
    }

    public ConditionalOperator getOppositeOperator() {
        return OPPOSITE_OPERATORS.get(this);
    }
	
	public static Optional<ConditionalOperator> fromOperatorCode(final int code) {
		for(final ConditionalOperator op : values()) {
			if(op.operatorCode == code) {
				return Optional.of(op);
			}
		}
		
		return Optional.empty();
	}

    public static ConditionalOperator[] getValidOperatorsForNumber() {
        return new ConditionalOperator[] {
                EQ,
                NEQ,
                GT,
                LT,
                MOD,
                IS,
                LEQ,
                GEQ
        };
    }

    public static Optional<ConditionalOperator> getOperatorForNumberByCode(final int code) {
        return findByCode(getValidOperatorsForNumber(), code);
    }

    public static ConditionalOperator[] getValidOperatorsForString() {
        return new ConditionalOperator[] {
                EQ,
                NEQ,
                GT,
                LT,
                LIKE,
                NOT_LIKE,
                IS,
                LEQ,
                GEQ,
                CONTAINS,
                NOT_CONTAINS,
                STARTS_WITH,
                NOT_STARTS_WITH
        };
    }

    public static Optional<ConditionalOperator> getOperatorForStringByCode(final int code) {
        return findByCode(getValidOperatorsForString(), code);
    }

    public static ConditionalOperator[] getValidOperatorsForDate() {
        return new ConditionalOperator[] {
                EQ,
                NEQ,
                GT,
                LT,
                IS,
                LEQ,
                GEQ,
        };
    }

    public static Optional<ConditionalOperator> getOperatorForDateByCode(final int code) {
        return findByCode(getValidOperatorsForDate(), code);
    }

    private static Optional<ConditionalOperator> findByCode(final ConditionalOperator[] operators, final int code) {
        for(ConditionalOperator operator : operators) {
            if(operator.operatorCode == code) {
                return Optional.of(operator);
            }
        }

        return Optional.empty();
    }
    
    public final String getOperatorKey() {
    	return name().toLowerCase();
    }
    
    public static final ConditionalOperator[] getSecondaryOperatorsForMod() {
    	return new ConditionalOperator[] {
    			EQ,
    	    	NEQ,
    	    	GT,
    	    	LT,
    	    	LEQ,
    	    	GEQ
    	};
    }
    
    public static final ConditionalOperator[] getValidOperatorsForMailingOperators() {
    	return new ConditionalOperator[] {
    			YES, 
    			NO
    	};
    }
}
