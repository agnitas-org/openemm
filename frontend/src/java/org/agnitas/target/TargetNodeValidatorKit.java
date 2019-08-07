/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.target;

/**
 * Interface for collection of all target node validators.
 */
public interface TargetNodeValidatorKit {

	/**
	 * Returns the validator for DATE nodes. 
	 * 
	 * @return validator for DATE nodes
	 */
	TargetNodeValidator getDateNodeValidator();

	/**
	 * Returns the validator for NUMERIC nodes. 
	 * 
	 * @return validator for NUMERIC nodes
	 */
	TargetNodeValidator getNumericNodeValidator();

	/**
	 * Returns the validator for STRING nodes. 
	 * 
	 * @return validator for STRING nodes
	 */
	TargetNodeValidator getStringNodeValidator();

	/**
	 * Returns the validator for INTERVAL MAILING nodes. 
	 * 
	 * @return validator for INTERVAL MAILING nodes
	 */
	TargetNodeValidator getIntervalMailingNodeValidator();

	/**
	 * Returns the validator for MAILING CLICKED nodes. 
	 * 
	 * @return validator for MAILING CLICKED nodes
	 */
	TargetNodeValidator getMailingClickedNodeValidator();

	/**
	 * Returns the validator for MAILING OPENED nodes. 
	 * 
	 * @return validator for MAILING OPENED nodes
	 */
	TargetNodeValidator getMailingOpenedNodeValidator();

	/**
	 * Returns the validator for MAILING RECEIVED nodes. 
	 * 
	 * @return validator for MAILING RECEIVED nodes
	 */
	TargetNodeValidator getMailingReceivedNodeValidator();
	
	/**
	 * Returns the validator for MAILING REVENUE nodes.
	 * 
	 * @return validator for MAILING REVENUE nodes
	 */
	TargetNodeValidator getMailingRevenueNodeValidator();

	/**
	 * Returns the validator for CLICKED SPECIFIC LINK IN MAILING nodes.
	 * 
	 * @return validator for CLICKED SPECIFIC LINK IN MAILING nodes
	 */
	TargetNodeValidator getMailingSpecificLinkClickNodeValidator();

	/**
	 * Returns the validator for FINISHED AUTOIMPORT nodes.
	 *
	 * @return validator for FINISHED AUTOIMPORT nodes
	 */
	TargetNodeValidator getAutoImportFinishedNodeValidator();

}
