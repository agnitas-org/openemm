package org.agnitas.target.impl;

import java.io.IOException;
import java.io.Serializable;

import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetOperator;

public class TargetNodeIntervalMailing extends TargetNode implements Serializable {
	private static final long serialVersionUID = -6033836340445764186L;
	
	public static final String PSEUDO_COLUMN_NAME = "interval_mailing_pseudo_column_name";

	public TargetNodeIntervalMailing(int companyID, int chainOperator, int openBracketBefore, int operator, String mailingIdString, int closeBracketAfter) throws Exception {
		initializeOperatorLists();
	}

	public static TargetOperator[] getValidOperators() {
		return new TargetOperator[] {
		};
	}

	@Override
	protected void initializeOperatorLists() {
		typeOperators = new TargetOperator[0];
	}

	@Override
	public String generateBsh() {
		throw new UnsupportedOperationException();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOpenBracketBefore() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setOpenBracketBefore(boolean openBracketBefore) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCloseBracketAfter() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCloseBracketAfter(boolean closeBracketAfter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getChainOperator() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void setChainOperator(int chainOperator) {
		throw new UnsupportedOperationException();
	}
	
	@Override
    public String getPrimaryFieldType() {
		throw new UnsupportedOperationException();
    }
	
	@Override
    public void setPrimaryField(String primaryField) {
		throw new UnsupportedOperationException();
    }
	
	@Override
    public void setPrimaryFieldType(String primaryField) {
		throw new UnsupportedOperationException();
    }

	@Override
    public String getPrimaryValue() {
		throw new UnsupportedOperationException();
    }

	@Override
	public int getPrimaryOperator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPrimaryField() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPrimaryOperator(int primaryOperator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPrimaryValue(String primaryValue) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean sameNodeType(TargetNode node) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean equalNodes(TargetNode node0) {
		throw new UnsupportedOperationException();
	}
}
