/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetOperator;
import org.apache.commons.lang.StringUtils;

public class TargetNodeDate extends TargetNode implements Serializable {

    /** Holds value of property openBracketBefore. */
    protected boolean openBracketBefore;

    /** Holds value of property closeBracketAfter. */
    protected boolean closeBracketAfter;

    /** Holds value of property chainOperator. */
    protected int chainOperator;

    /** Holds value of property primaryOperator. */
    protected int primaryOperator;

    /** Holds value of property primaryField. */
    protected String primaryField;

    /** Holds value of property primaryFieldType. */
    protected String primaryFieldType;

    /** Holds value of property primaryValue. */
    protected String primaryValue;

    /** Holds value of property dateFormat. */
    protected String dateFormat;

    /** Serial version UID. */
    private static final long serialVersionUID = -6885016603800628942L;

    /** Creates a new instance of TargetNodeString */
    public TargetNodeDate(String dateFormat) {
    	this.dateFormat = dateFormat;
    	initializeOperatorLists();
    }
    
    public static TargetNodeDate withDefaultDateFormat(boolean isOracle) {
        if(isOracle) {
        	return new TargetNodeDate("yyyymmdd");
        } else {
        	return new TargetNodeDate("%Y%m%d");
        }
    }

    public static TargetOperator[] getValidOperators() {
    	return new TargetOperator[] {
            	OPERATOR_EQ, 
            	OPERATOR_NEQ, 
            	OPERATOR_GT, 
            	OPERATOR_LT, 
            	null, 
            	null, 
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
    }
    
    @Override
    protected void initializeOperatorLists() {
        typeOperators = TargetNodeDate.getValidOperators();
    }
    
    @Override
    public String generateSQL() {
		StringBuffer tmpSQL = new StringBuffer("");

		switch (this.chainOperator) {
			case TargetNode.CHAIN_OPERATOR_AND:
				tmpSQL.append(" AND ");
				break;
			case TargetNode.CHAIN_OPERATOR_OR:
				tmpSQL.append(" OR ");
				break;
			default:
				tmpSQL.append(" ");
		}

		if (this.openBracketBefore) {
			tmpSQL.append("(");
		}

        TargetOperator operator = null;

        // Permitted range: [1, N]
        if (this.primaryOperator > 0 && this.primaryOperator < this.typeOperators.length) {
            // Notice that array entry permitted to be null
            operator = this.typeOperators[this.primaryOperator - 1];
        }

        if (operator == null) {
            operator = TargetNode.OPERATOR_IS;
        }

        String columnName;
        if (primaryField.equalsIgnoreCase("CURRENT_TIMESTAMP") || primaryField.equalsIgnoreCase("SYSDATE")) {
            columnName = this.primaryField;
        } else {
            columnName = "cust." + this.primaryField;
        }

		if (operator.getOperatorCode() == TargetNode.OPERATOR_IS.getOperatorCode()) {
			tmpSQL.append(columnName);
			tmpSQL.append(" ");
			tmpSQL.append(operator.getOperatorSymbol());
			tmpSQL.append(" ");
			tmpSQL.append(getSQLSafeString(this.primaryValue));
		} else {
			tmpSQL.append(sqlDateString(ConfigService.isOracleDB(), columnName, this.dateFormat)).append(" ");
			tmpSQL.append(operator.getOperatorSymbol());

            String value = " '" + primaryValue + "' ";
            if (StringUtils.isNotBlank(primaryValue)) {
                String expression = primaryValue.toUpperCase();
                if (expression.contains("CURRENT_TIMESTAMP") || expression.contains("SYSDATE") || expression.contains("NOW()")) {
                    value = primaryValue.replaceAll("(?i)sysdate", "CURRENT_TIMESTAMP")
                            .replaceAll("(?i)now\\(\\)", "CURRENT_TIMESTAMP")
                            .replaceAll("(?i)current_timestamp", "CURRENT_TIMESTAMP");
                    value = " " + sqlDateString(ConfigService.isOracleDB(), value, dateFormat);
                }
            }
            tmpSQL.append(value);
		}

		if (this.closeBracketAfter) {
			tmpSQL.append(")");
		}
		return tmpSQL.toString();
    }
    
	/**
	 * returns a date string
	 */
	private String sqlDateString(boolean isOracleDB, String field, String format) {
		if (isOracleDB) {
			return "TO_CHAR(" + field + ", '" + format + "')";
		} else {
			format = format.replaceAll("yyyy", "%Y");
			format = format.replaceAll("yy", "%y");
			format = format.replaceAll("mm", "%m");
			format = format.replaceAll("dd", "%d");
			return "DATE_FORMAT(" + field + ", '" + format + "')";
		}
	}

    @Override
    public String generateBsh() {
        StringBuffer tmpBsh=new StringBuffer();

        switch(this.chainOperator) {
            case TargetNode.CHAIN_OPERATOR_AND:
                tmpBsh.append(" && ");
                break;
            case TargetNode.CHAIN_OPERATOR_OR:
                tmpBsh.append(" || ");
                break;
            default:
                tmpBsh.append(" ");
        }

        if(this.openBracketBefore) {
            tmpBsh.append("(");
        }

        if(this.primaryOperator == TargetNode.OPERATOR_IS.getOperatorCode()) {
	    	tmpBsh.append(this.primaryField.toLowerCase());
	        if(this.primaryValue.startsWith("null")) {
	            tmpBsh.append("==");
	        } else {
	            tmpBsh.append("!=");
	        }
	        tmpBsh.append("null ");
        } else {
            tmpBsh.append("AgnUtils.compareString(");
            tmpBsh.append("AgnUtils.formatDate(");
            tmpBsh.append(this.primaryField.toLowerCase());
            tmpBsh.append(", \"");
            tmpBsh.append(this.dateFormat.replace('m', 'M')); // from sql-style to java-style
            tmpBsh.append("\") ");
            tmpBsh.append(", ");
            if(this.primaryValue.startsWith("sysdate") || this.primaryValue.contains("now()") ) {
                tmpBsh.append("AgnUtils.formatDate(");
                tmpBsh.append("AgnUtils.getSysdate(\"");
                tmpBsh.append(this.primaryValue);
                tmpBsh.append("\"), \"");
                tmpBsh.append(this.dateFormat.replace('m', 'M'));
                tmpBsh.append("\") ");
            } 
            
            else {
                tmpBsh.append(" \"");
                tmpBsh.append(getSQLSafeString(this.primaryValue));
                tmpBsh.append("\"");
            }
            tmpBsh.append(", ");
            tmpBsh.append(Integer.toString(this.primaryOperator-1));
            tmpBsh.append(") ");
        }

        if(this.closeBracketAfter) {
            tmpBsh.append(")");
        }

        return tmpBsh.toString();
    }

    /** Getter for property dateFormat.
     * @return Value of property dateFormat.
     */
    public String getDateFormat() {
        return this.dateFormat;
    }

    /** Setter for property dateFormat.
     * @param dateFormat New value of property dateFormat.
     */
    public void setDateFormat(String dateFormat) {
        if(dateFormat!=null) {
            this.dateFormat = dateFormat;
        }
    }

    @Override
    public void setPrimaryOperator(int primOp) {
        if(primOp==TargetNode.OPERATOR_LIKE.getOperatorCode())
            primOp=TargetNode.OPERATOR_EQ.getOperatorCode();

        if(primOp==TargetNode.OPERATOR_NLIKE.getOperatorCode())
            primOp=TargetNode.OPERATOR_NEQ.getOperatorCode();

        if(primOp==TargetNode.OPERATOR_CONTAINS.getOperatorCode())
            primOp=TargetNode.OPERATOR_EQ.getOperatorCode();

        if(primOp==TargetNode.OPERATOR_NOT_CONTAINS.getOperatorCode())
            primOp=TargetNode.OPERATOR_NEQ.getOperatorCode();

        if(primOp==TargetNode.OPERATOR_STARTS_WITH.getOperatorCode())
            primOp=TargetNode.OPERATOR_EQ.getOperatorCode();

        if(primOp==TargetNode.OPERATOR_NOT_STARTS_WITH.getOperatorCode())
            primOp=TargetNode.OPERATOR_NEQ.getOperatorCode();

        if(primOp==TargetNode.OPERATOR_MOD.getOperatorCode())
            primOp=TargetNode.OPERATOR_EQ.getOperatorCode();

        this.primaryOperator=primOp;
    }

    @Override
    public void setPrimaryValue(String primVal) {
        if(this.primaryOperator==TargetNode.OPERATOR_IS.getOperatorCode()) {
            if(!primVal.equals("null") && !primVal.equals("not null")) {
                this.primaryValue = "null";
            } else {
                this.primaryValue=primVal;
            }
        } else {
            this.primaryValue = primVal.toLowerCase();
        }
    }

    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField allFields=null;

        allFields=in.readFields();
        this.chainOperator=allFields.get("chainOperator", TargetNode.CHAIN_OPERATOR_NONE);
        this.primaryField=(String)allFields.get("primaryField", "default");
        this.primaryFieldType=(String)allFields.get("primaryFieldType", "DATE");
        this.primaryOperator=allFields.get("primaryOperator", TargetNode.OPERATOR_EQ.getOperatorCode());
        this.primaryValue=(String)allFields.get("primaryValue", "0");
        this.dateFormat=(String)allFields.get("dateFormat", "yyyymmdd");
        this.closeBracketAfter=allFields.get("closeBracketAfter", false);
        this.openBracketBefore=allFields.get("openBracketBefore", false);

        initializeOperatorLists();
    }

    /** Getter for property openBracketBefore.
     * @return Value of property openBracketBefore.
     */
    @Override
    public boolean isOpenBracketBefore() {
        return this.openBracketBefore;
    }

    /** Setter for property openBracketBefore.
     * @param openBracketBefore New value of property openBracketBefore.
     */
    @Override
    public void setOpenBracketBefore(boolean openBracketBefore) {
        this.openBracketBefore=openBracketBefore;
    }

    /** Getter for property closeBracketAfter.
     * @return Value of property closeBracketAfter.
     */
    @Override
    public boolean isCloseBracketAfter() {
        return this.closeBracketAfter;
    }

    /** Setter for property closeBracketAfter.
     * @param closeBracketAfter New value of property closeBracketAfter.
     */
    @Override
    public void setCloseBracketAfter(boolean closeBracketAfter) {
        this.closeBracketAfter=closeBracketAfter;
    }

    /** Getter for property chainOperator.
     * @return Value of property chainOperator.
     */
    @Override
    public int getChainOperator() {
        return this.chainOperator;
    }

    /** Setter for property chainOperator.
     * @param chainOperator New value of property chainOperator.
     */
    @Override
    public void setChainOperator(int chainOperator) {
        this.chainOperator=chainOperator;
    }

    /** Getter for property primaryOperator.
     * @return Value of property primaryOperator.
     */
    @Override
    public int getPrimaryOperator() {
        return this.primaryOperator;
    }

    /** Getter for property primaryField.
     * @return Value of property primaryField.
     */
    @Override
    public String getPrimaryField() {
        return this.primaryField;
    }

    /** Setter for property primaryField.
     * @param primaryField New value of property primaryField.
     */
    @Override
    public void setPrimaryField(String primaryField) {
        this.primaryField=primaryField;
    }

    /** Getter for property primaryFieldType.
     * @return Value of property primaryFieldType.
     */
    @Override
    public String getPrimaryFieldType() {
        return this.primaryFieldType;
    }

    /** Setter for property primaryFieldType.
     * @param primaryFieldType New value of property primaryFieldType.
     */
    @Override
    public void setPrimaryFieldType(String primaryFieldType) {
        this.primaryFieldType=primaryFieldType;
    }

    /** Getter for property primaryValue.
     * @return Value of property primaryValue.
     */
    @Override
	public String getPrimaryValue() {
        return this.primaryValue;
    }

	@Override
	public boolean sameNodeType(TargetNode node) {
		return node instanceof TargetNodeDate;
	}
	
	@Override
	public boolean equalNodes(TargetNode node0) {
		if(super.equalNodes(node0)) {
			TargetNodeDate node = (TargetNodeDate) node0;
			return StringUtils.equals(this.dateFormat, node.dateFormat);
		} else {
			return false;
		}
	}

}
