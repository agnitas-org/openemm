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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetOperator;
import org.apache.log4j.Logger;

/**
 * @deprecated
 */
@Deprecated
public class TargetNodeNumeric extends TargetNode implements Serializable {
	private static final transient Logger logger = Logger.getLogger(TargetNodeNumeric.class);
    
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
    
    /** Holds value of property secondaryValue. */
    protected int secondaryValue;
    
    /** Holds value of property secondaryOperator. */
    protected int secondaryOperator;
    
    private static final long serialVersionUID = 6666390160147561038L;
    
    /** Creates a new instance of TargetNodeString */
    public TargetNodeNumeric() {
    	initializeOperatorLists();
    }

    @Override
	protected void initializeOperatorLists() {
        typeOperators = new TargetOperator[] {
            	OPERATOR_EQ, 
            	OPERATOR_NEQ, 
            	OPERATOR_GT, 
            	OPERATOR_LT, 
            	null, 
            	null, 
            	OPERATOR_MOD, 
            	OPERATOR_IS, 
            	OPERATOR_LT_EQ, 
            	OPERATOR_GT_EQ
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
        
        if (this.primaryOperator == TargetNode.OPERATOR_MOD.getOperatorCode()) {
                tmpBsh.append("(");
                tmpBsh.append(this.primaryField.toLowerCase());
                tmpBsh.append(" % ");
                tmpBsh.append(getSQLSafeString(this.primaryValue));
                tmpBsh.append(") ");
                tmpBsh.append(this.typeOperators[this.secondaryOperator-1].getBshOperatorSymbol());
                tmpBsh.append(" ");
                tmpBsh.append(this.secondaryValue);
        } else if (this.primaryOperator == TargetNode.OPERATOR_IS.getOperatorCode()) {
                tmpBsh.append(this.primaryField.toLowerCase());
                if(this.primaryValue.startsWith("null")) {
                    tmpBsh.append("==");
                } else {
                    tmpBsh.append("!=");
                }
                tmpBsh.append("null ");
        } else {
                tmpBsh.append(this.primaryField.toLowerCase());
                tmpBsh.append(" ");
                tmpBsh.append(this.typeOperators[this.primaryOperator-1].getBshOperatorSymbol());
                tmpBsh.append(" ");
                tmpBsh.append(getSQLSafeString(this.primaryValue));
        }
        
        
        if(this.closeBracketAfter) {
            tmpBsh.append(")");
        }
        
        return tmpBsh.toString();
    }
    
    @Override
	public void setPrimaryValue(String tmpVal) {
        double tmpNum=0;
        if(this.primaryOperator==TargetNode.OPERATOR_IS.getOperatorCode()) {
            if(!tmpVal.equals("null") && !tmpVal.equals("not null")) {
                this.primaryValue = "null";
            } else {
                this.primaryValue=tmpVal;
            }
        } else {
            try {
                tmpNum=Double.parseDouble(tmpVal);
            } catch (Exception e) {
                if (logger.isInfoEnabled()) logger.info("Error in Number-Parsing: "+e);
            }
            DecimalFormat aFormat=new DecimalFormat("0.###########", new DecimalFormatSymbols(Locale.US));
            this.primaryValue=aFormat.format(tmpNum);
            // this.primaryValue=NumberFormat.getInstance(Locale.US).format(tmpNum);
            // this.primaryValue=Double.toString(tmpNum);
        }
    }
    
    /** Getter for property secondaryValue.
     * @return Value of property secondaryValue.
     */
    public int getSecondaryValue() {
        return this.secondaryValue;
    }
    
    /** Setter for property secondaryValue.
     * @param secondaryValue New value of property secondaryValue.
     */
    public void setSecondaryValue(int secondaryValue) {
        this.secondaryValue = secondaryValue;
    }
    
    /** Getter for property secondaryOperator.
     * @return Value of property secondaryOperator.
     */
    public int getSecondaryOperator() {
        return this.secondaryOperator;
    }
    
    /** Setter for property secondaryOperator.
     * @param secondaryOperator New value of property secondaryOperator.
     */
    public void setSecondaryOperator(int secondaryOperator) {
        this.secondaryOperator = secondaryOperator;
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

        this.primaryOperator=primOp;
    }
    
    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField allFields=null;
        try {
            allFields=in.readFields();
            this.chainOperator=allFields.get("chainOperator", TargetNode.CHAIN_OPERATOR_NONE);
            this.primaryField=(String)allFields.get("primaryField", "default");
            this.primaryFieldType=(String)allFields.get("primaryFieldType", "NUMBER");
            this.primaryOperator=allFields.get("primaryOperator", TargetNode.OPERATOR_EQ.getOperatorCode());
            this.primaryValue=(String)allFields.get("primaryValue", "0");
            this.secondaryOperator=allFields.get("secondaryOperator", TargetNode.OPERATOR_EQ.getOperatorCode());
            this.secondaryValue=allFields.get("secondaryValue", 0);
            this.closeBracketAfter=allFields.get("closeBracketAfter", false);
            this.openBracketBefore=allFields.get("openBracketBefore", false);
        } catch (Exception e) {
            if (logger.isInfoEnabled()) logger.info("read: "+e);
        }
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
    	return node instanceof TargetNodeNumeric;
    }
    
    @Override
	public boolean equalNodes(TargetNode node0) {
    	if(super.equalNodes(node0)) {
    		TargetNodeNumeric node = (TargetNodeNumeric) node0;
    		
    		return this.secondaryOperator == node.secondaryOperator
    				&& this.secondaryValue == node.secondaryValue;
    	} else {
    		return false;
    	}
    }
}
