/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target.impl;

import org.agnitas.target.TargetNodeValidator;
import org.agnitas.target.TargetNodeValidatorKit;

/**
 * Auxiliary class containing validators for all type of target nodes.
 */
public class TargetNodeValidatorKitImpl implements TargetNodeValidatorKit {

	/** Validator for DATE nodes. */
	private TargetNodeValidator dateNodeValidator;
	
	/** Validator for NUMERIC nodes. */
	private TargetNodeValidator numericNodeValidator;
	
	/** Validator for STRING nodes. */
	private TargetNodeValidator stringNodeValidator;
	
	/** Validator for INTERVAL MAILING nodes. */
	private TargetNodeValidator intervalMailingNodeValidator;
	
	/** Validator for MAILING CLICKED nodes. */
	private TargetNodeValidator mailingClickedNodeValidator;
	
	/** Validator for MAILING OPENED nodes. */
	private TargetNodeValidator mailingOpenedNodeValidator;
	
	/** Validator for MAILING RECEIVED nodes. */
	private TargetNodeValidator mailingReceivedNodeValidator;
	
	/** Validator for CLICKED SPECIFIC LINK IN MAILING nodes. */
	private TargetNodeValidator mailingSpecificLinkClickNodeValidator;
	
	/** Validator for MAILING REVENUE nodes. */
	private TargetNodeValidator mailingRevenueNodeValidator;

	/** Validator for AUTOIMPORT FINISHED nodes. */
	private TargetNodeValidator autoImportFinishedNodeValidator;

	/* (non-Javadoc)
	 * @see org.agnitas.target.impl.TargetNodeValidatorKit#getDateNodeValidator()
	 */
	@Override
	public TargetNodeValidator getDateNodeValidator() {
		return dateNodeValidator;
	}
	
	/**
	 * Set validator for DATE nodes.
	 * 
	 * @param validator validator
	 */
	public void setDateNodeValidator(TargetNodeValidator validator) {
		this.dateNodeValidator = validator;
	}

	/* (non-Javadoc)
	 * @see org.agnitas.target.impl.TargetNodeValidatorKit#getNumericNodeValidator()
	 */
	@Override
	public TargetNodeValidator getNumericNodeValidator() {
		return numericNodeValidator;
	}
	
	/**
	 * Set validator for NUMERIC nodes.
	 * 
	 * @param validator validator
	 */
	public void setNumericNodeValidator(TargetNodeValidator validator) {
		this.numericNodeValidator = validator;
	}
	
	/* (non-Javadoc)
	 * @see org.agnitas.target.impl.TargetNodeValidatorKit#getStringNodeValidator()
	 */
	@Override
	public TargetNodeValidator getStringNodeValidator() {
		return stringNodeValidator;
	}

	/**
	 * Set validator for STRING nodes.
	 * 
	 * @param validator validator
	 */
	public void setStringNodeValidator(TargetNodeValidator validator) {
		this.stringNodeValidator = validator;
	}
	
	/* (non-Javadoc)
	 * @see org.agnitas.target.impl.TargetNodeValidatorKit#getIntervalMailingNodeValidator()
	 */
	@Override
	public TargetNodeValidator getIntervalMailingNodeValidator() {
		return intervalMailingNodeValidator;
	}
	
	/**
	 * Set validator for INTERVAL MAILING nodes.
	 * 
	 * @param validator validator
	 */
	public void setIntervalMailingNodeValidator( TargetNodeValidator validator) {
		this.intervalMailingNodeValidator = validator;
	}
	
	/* (non-Javadoc)
	 * @see org.agnitas.target.impl.TargetNodeValidatorKit#getMailingClickedNodeValidator()
	 */
	@Override
	public TargetNodeValidator getMailingClickedNodeValidator() {
		return mailingClickedNodeValidator;
	}
	
	/**
	 * Set validator for MAILING CLICKED nodes.
	 * 
	 * @param validator validator
	 */
	public void setMailingClickedNodeValidator( TargetNodeValidator validator) {
		this.mailingClickedNodeValidator = validator;
	}
	
	/* (non-Javadoc)
	 * @see org.agnitas.target.impl.TargetNodeValidatorKit#getMailingOpenedNodeValidator()
	 */
	@Override
	public TargetNodeValidator getMailingOpenedNodeValidator() {
		return mailingOpenedNodeValidator;
	}
	
	/**
	 * Set validator for MAILING OPENED nodes.
	 * 
	 * @param validator validator
	 */
	public void setMailingOpenedNodeValidator( TargetNodeValidator validator) {
		this.mailingOpenedNodeValidator = validator;
	}
	
	/* (non-Javadoc)
	 * @see org.agnitas.target.impl.TargetNodeValidatorKit#getMailingReceivedNodeValidator()
	 */
	@Override
	public TargetNodeValidator getMailingReceivedNodeValidator() {
		return mailingReceivedNodeValidator;
	}
	
	/**
	 * Set validator for MAILING RECEIVED nodes.
	 * 
	 * @param validator validator
	 */
	public void setMailingReceivedNodeValidator( TargetNodeValidator validator) {
		this.mailingReceivedNodeValidator = validator;
	}

	@Override
	public TargetNodeValidator getMailingRevenueNodeValidator() {
		return this.mailingRevenueNodeValidator;
	}
	
	/**
	 * Set validator for MAILING REVENUE nodes.
	 * 
	 * @param validator validator
	 */
	public void setMailingRevenueNodeValidator(TargetNodeValidator validator) {
		this.mailingRevenueNodeValidator = validator;
	}
	
	@Override
	public TargetNodeValidator getMailingSpecificLinkClickNodeValidator() {
		return this.mailingSpecificLinkClickNodeValidator;
	}
	
	/**
	 * Set validator for CLICKED SPECIFIC LINK IN MAILING nodes.
	 * 
	 * @param mailingSpecificLinkClickNodeValidator validator
	 */
	public void setMailingSpecificLinkClickNodeValidator(TargetNodeValidator mailingSpecificLinkClickNodeValidator) {
		this.mailingSpecificLinkClickNodeValidator = mailingSpecificLinkClickNodeValidator;
	}

	/**
	 * Returns the validator for FINISHED AUTOIMPORT nodes.
	 *
	 * @return validator for FINISHED AUTOIMPORT nodes
	 */
	@Override
	public TargetNodeValidator getAutoImportFinishedNodeValidator() {
		return autoImportFinishedNodeValidator;
	}

	public void setAutoImportFinishedNodeValidator(TargetNodeValidator autoImportFinishedNodeValidator) {
		this.autoImportFinishedNodeValidator = autoImportFinishedNodeValidator;
	}
}
