/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.nodes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetOperator;
import org.apache.log4j.Logger;

/**
 * Target node to filter customer by "bought by mailing" "not bought by mailing".
 */
public class TargetNodeMailingClickedOnSpecificLink extends TargetNode implements Serializable {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 3659479768587208149L;

	/** 
	 * The logger. 
	 */
	private static final transient Logger logger = Logger.getLogger(TargetNodeMailingClickedOnSpecificLink.class);
	
	/** 
	 * Name of pseudo column. 
	 */
	public static final String PSEUDO_COLUMN_NAME = "pseudo_column_mailing_clicked_on_specific_link";

	/** 
	 * Flag, if opening parenthesis is set. 
	 */
	private boolean openBracketBefore;
	
	/** 
	 * Flag, if closing parenthesis is set. 
	 */
	private boolean closeBracketAfter;
	
	/** 
	 * Type of operator for chaining rules. 
	 */
	private int chainOperator;
	
	/** 
	 * Type of primary operator. 
	 */
	private int primaryOperator;
	
	/** 
	 * Primary value (The mailing ID). 
	 */
	private int mailingID;
	
	/**
	 * Secondary value (the link ID). 
	 */
	private int linkID;
	
	/** 
	 * Company ID. 
	 */
	private int companyId;
	
	/**
	 * Creates a new target node for checking that recipient made some revenue on a specific mailing.
	 * 
	 * @param companyId company ID
	 * @param chainOperator chaining operator (&quot;and&quot; or &quot;or&quot;)
	 * @param openParenthesis true, if node opens parenthesis
	 * @param primaryOperator primary operator
	 * @param mailingID mailing ID (primary value)
	 * @param linkID link ID (secondary value)
	 * @param closeParenthesis true, if node closes parenthesis
	 */
	public TargetNodeMailingClickedOnSpecificLink(@VelocityCheck int companyId, int chainOperator, int openParenthesis, int primaryOperator, int mailingID, int linkID, int closeParenthesis) {
		this.companyId = companyId;
		this.chainOperator = chainOperator;
		this.openBracketBefore = openParenthesis != 0;
		this.primaryOperator = primaryOperator;
		this.mailingID = mailingID;
		this.linkID = linkID;
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
		typeOperators = TargetNodeMailingClickedOnSpecificLink.getValidOperators();
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
	public String generateBsh() {
		try {
			throw new RuntimeException("BSH generation is not supported!");
		} catch( RuntimeException e) {
			logger.error("BSH generation failed", e);
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
		return TargetNodeMailingClickedOnSpecificLink.PSEUDO_COLUMN_NAME;
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
		return Integer.toString(this.mailingID);
	}

	@Override
	public void setPrimaryValue(String primaryValue) {
		this.mailingID = Integer.parseInt(primaryValue);
	}
	
	/**
	 * Return ID of link (secondary value).
	 * 
	 * @return ID of link
	 */
	public String getSecondaryValue() {
		return Integer.toString(this.linkID);
	}
	
	/**
	 * Set ID of link (secondary value).
	 * 
	 * @param secondaryValue ID of link
	 */
	public void setSecondaryValue(String secondaryValue) {
		this.linkID = Integer.parseInt(secondaryValue);
	}

	/**
	 * Method for deserializing target node.
	 * 
	 * @param in {@link ObjectInputStream} to read data from
	 * 
	 * @throws IOException on errors reading object data
	 * @throws ClassNotFoundException when trying to create an instance of a non-existing class
	 */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField allFields=null;
        try {
            allFields=in.readFields();
            this.chainOperator = allFields.get("chainOperator", TargetNode.CHAIN_OPERATOR_NONE);
            this.primaryOperator = allFields.get("primaryOperator", TargetNode.OPERATOR_EQ.getOperatorCode());
            this.mailingID = allFields.get("mailingID", 0);
            this.linkID = allFields.get("linkID", 0);
            this.closeBracketAfter = allFields.get("closeBracketAfter", false);
            this.openBracketBefore = allFields.get("openBracketBefore", false);
        } catch (Exception e) {
            logger.error("Error deserializing " + this.getClass().getCanonicalName(), e);
        }
    	this.initializeOperatorLists();
    }

	@Override
	public boolean sameNodeType(TargetNode node) {
		return node instanceof TargetNodeMailingClickedOnSpecificLink;
	}

	@Override
	public boolean equalNodes(TargetNode node0) {
		if(super.equalNodes(node0)) {
			TargetNodeMailingClickedOnSpecificLink node = (TargetNodeMailingClickedOnSpecificLink) node0;
			
			return this.linkID == node.linkID;
		} else {
			return false;
		}
	}
}
