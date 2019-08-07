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

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetOperator;
import org.apache.log4j.Logger;

/**
 * Target node to filter customer by "opened a specific mailing" / "did not open
 * a specific mailing".
 */
public class TargetNodeMailingOpened extends TargetNode implements Serializable {
	
	/** Serial version UID. */
	private static final long serialVersionUID = -8771964082783894035L;

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger( TargetNodeMailingOpened.class);
	
	/** Name of pseudo column. */
	public static final String PSEUDO_COLUMN_NAME = "pseudo_column_mailing_opened";

	/** Flag, if opening parenthesis is set. */
	private boolean openBracketBefore;
	
	/** Flag, if closing parenthesis is set. */
	private boolean closeBracketAfter;
	
	/** Type of operator for chaining rules. */
	private int chainOperator;
	
	/** Type of primary operator. */
	private int primaryOperator;
	
	/** Primary value. */
	private String primaryValue;
	
	/** Company ID. */
	private int companyId;

	public TargetNodeMailingOpened( @VelocityCheck int companyId, int chainOperator, int openParenthesis, int primaryOperator, String primaryValue, int closeParenthesis) {
		this.companyId = companyId;
		this.chainOperator = chainOperator;
		this.openBracketBefore = openParenthesis != 0;
		this.primaryOperator = primaryOperator;
		this.primaryValue = primaryValue;
		this.closeBracketAfter = closeParenthesis != 0;
	}
	
	/**
	 * Returns a list of valid operators for this target node.
	 * 
	 * @return array of valid operators
	 */
	public static TargetOperator[] getValidOperators() {
		return new TargetOperator[] { 
				OPERATOR_YES, 
				OPERATOR_NO
				};
	}

	@Override
	protected void initializeOperatorLists() {
		typeOperators = TargetNodeMailingOpened.getValidOperators();
	}

	@Override
	public boolean isOpenBracketBefore() {
		return this.openBracketBefore;
	}

	@Override
	public void setOpenBracketBefore(boolean openBracketBefore) {
		this.openBracketBefore = openBracketBefore;
	}

	@Override
	public boolean isCloseBracketAfter() {
		return this.closeBracketAfter;
	}

	@Override
	public void setCloseBracketAfter(boolean closeBracketAfter) {
		this.closeBracketAfter = closeBracketAfter;		
	}

	@Override
	public int getChainOperator() {
		return this.chainOperator;
	}

	@Override
	public void setChainOperator(int chainOperator) {
		this.chainOperator = chainOperator;
	}

	@Override
	public String generateEmbeddedSQL() {
		StringBuffer buffer = new StringBuffer();
		
		// Get the mailing ID
		int mailingId = Integer.parseInt( this.primaryValue);
		
		// Negate result if primary operator says to
		if( primaryOperator == TargetNode.OPERATOR_NO.getOperatorCode())
			buffer.append( " NOT ");
		
		// Build the WHERE clause
		buffer.append( " EXISTS (SELECT 1 FROM ");
		
		buffer.append( "onepixellog_");
		buffer.append( companyId);
		buffer.append( "_tbl");
		
		buffer.append( " opl WHERE opl.customer_id=cust.customer_id AND opl.mailing_id=");
		buffer.append( mailingId);
		buffer.append( " AND opl.company_id=");
		buffer.append( companyId);
		buffer.append( ")");
		
		return buffer.toString();
	}

	@Override
	public String generateBsh() {
		try {
			throw new RuntimeException( "BSH generation is not supported!");
		} catch( RuntimeException e) {
			logger.error( "BSH generation failed", e);
			throw e;
		}
	}

	@Override
	public int getPrimaryOperator() {
		return this.primaryOperator;
	}

	@Override
	public void setPrimaryOperator(int primaryOperator) {
		this.primaryOperator = primaryOperator;
	}

	@Override
	public String getPrimaryField() {
		return TargetNodeMailingOpened.PSEUDO_COLUMN_NAME;
	}

	@Override
	public void setPrimaryField(String primaryField) {
		// Nothing to do here
	}

	@Override
	public String getPrimaryFieldType() {
		return null;
	}

	@Override
	public void setPrimaryFieldType(String primaryFieldType) {
		// Nothing to do here
	}

	@Override
	public String getPrimaryValue() {
		return this.primaryValue;
	}

	@Override
	public void setPrimaryValue(String primaryValue) {
		this.primaryValue = primaryValue;
	}

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField allFields=null;
        try {
            allFields=in.readFields();
            this.chainOperator=allFields.get("chainOperator", TargetNode.CHAIN_OPERATOR_NONE);
            this.primaryOperator=allFields.get("primaryOperator", TargetNode.OPERATOR_EQ.getOperatorCode());
            this.primaryValue=(String)allFields.get("primaryValue", "0");
            this.closeBracketAfter=allFields.get("closeBracketAfter", false);
            this.openBracketBefore=allFields.get("openBracketBefore", false);
        } catch (Exception e) {
            logger.error("Error deserializing " + this.getClass().getCanonicalName(), e);
        }
    	this.initializeOperatorLists();
    }

    @Override
    public boolean sameNodeType(TargetNode node) {
    	return node instanceof TargetNodeMailingOpened;
    }
    
    @Override
	public boolean equalNodes(TargetNode node0) {
    	if(super.equalNodes(node0)) {
    		TargetNodeMailingOpened node = (TargetNodeMailingOpened) node0;
    		
    		return this.companyId == node.companyId;
    	} else {
    		return false;
    	}
    }
}
