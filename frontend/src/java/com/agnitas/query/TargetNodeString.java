/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.query;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetOperator;
import org.apache.log4j.Logger;

/**
 * @deprecated
 */
@Deprecated
public class TargetNodeString extends TargetNode implements Serializable {
	private static final transient Logger logger = Logger.getLogger(TargetNodeString.class);
    
    //    public static char columnType='C';
    
    private static final long serialVersionUID = -5363353927700548241L;
    
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
    
    /** Creates a new instance of TargetNodeString */
    public TargetNodeString() {
    	this.initializeOperatorLists();
    }

    @Override
	protected void initializeOperatorLists() {
        typeOperators = new TargetOperator[] {
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
                OPERATOR_CONTAINS,
                OPERATOR_NOT_CONTAINS,
                OPERATOR_STARTS_WITH,
                OPERATOR_NOT_STARTS_WITH
            	};
	}
      
    @Override
	public String generateBsh() {
        StringBuffer tmpBsh=new StringBuffer("");
        
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
        
        if ((this.primaryOperator == TargetNode.OPERATOR_LIKE.getOperatorCode()) || (this.primaryOperator == TargetNode.OPERATOR_NLIKE.getOperatorCode())) {
            if(this.primaryOperator==TargetNode.OPERATOR_NLIKE.getOperatorCode()) {
                tmpBsh.append("!");
            }
            tmpBsh.append("AgnUtils.match(AgnUtils.toLowerCase(\"");
            tmpBsh.append(this.primaryValue);
            tmpBsh.append("\"), AgnUtils.toLowerCase(");
            tmpBsh.append(this.primaryField.toLowerCase());
            tmpBsh.append("))");
        } else if (this.primaryOperator == TargetNode.OPERATOR_IS.getOperatorCode()) {
            tmpBsh.append(this.primaryField.toLowerCase());
            if(this.primaryValue.startsWith("null")) {
                tmpBsh.append("==");
            } else {
                tmpBsh.append("!=");
            }
            tmpBsh.append("null ");
        } else {
            tmpBsh.append("AgnUtils.compareString(AgnUtils.toLowerCase(");
            tmpBsh.append(this.primaryField.toLowerCase());
            tmpBsh.append("), ");
            tmpBsh.append("AgnUtils.toLowerCase(\"");
            tmpBsh.append(getSQLSafeString(this.primaryValue));
            tmpBsh.append("\"), ");
            tmpBsh.append(Integer.toString(this.primaryOperator-1));
            tmpBsh.append(") ");
        }
        
        if(this.closeBracketAfter) {
            tmpBsh.append(")");
        }
        
        return tmpBsh.toString();
    }
    
    @Override
	public void setPrimaryOperator(int primOp) {
        if(primOp==TargetNode.OPERATOR_MOD.getOperatorCode())
            primOp=TargetNode.OPERATOR_EQ.getOperatorCode();
        
        this.primaryOperator=primOp;
    }
    
    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField allFields=null;
        if (logger.isInfoEnabled()) logger.info("in readstring");
        allFields=in.readFields();
        this.chainOperator=allFields.get("chainOperator", TargetNode.CHAIN_OPERATOR_NONE);
        this.primaryField=(String)allFields.get("primaryField", "default");
        this.primaryFieldType=(String)allFields.get("primaryFieldType", "VARCHAR2");
        this.primaryOperator=allFields.get("primaryOperator", TargetNode.OPERATOR_EQ.getOperatorCode());
        this.primaryValue=(String)allFields.get("primaryValue", " ");
        this.closeBracketAfter=allFields.get("closeBracketAfter", false);
        this.openBracketBefore=allFields.get("openBracketBefore", false);

        this.initializeOperatorLists();
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
    
    /** Setter for property primaryValue.
     * @param primValue New value of property primaryValue.
     */
    @Override
	public void setPrimaryValue(String primValue) {
        if(this.primaryOperator==TargetNode.OPERATOR_IS.getOperatorCode()) {
            if(!primValue.equals("null") && !primValue.equals("not null")) {
                this.primaryValue = "null";
            } else {
                this.primaryValue=primValue;
            }
        } else {
            this.primaryValue=primValue;
        }
    }

	@Override
	public boolean sameNodeType(TargetNode node) {
    	return node instanceof TargetNodeString;
	}
}
