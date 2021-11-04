/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.target.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.agnitas.emm.core.target.exception.TargetGroupException;
import org.agnitas.emm.core.target.service.TargetGroupLocator;
import org.apache.log4j.Logger;

/**
 * Chains a list of {@link TargetGroupLocator} instances.
 */
public class TargetGroupLocatorChain implements TargetGroupLocator {

	/**
	 * The logger. 
	 */
	private static final transient Logger logger = Logger.getLogger(TargetGroupLocatorChain.class);
	
	/**
	 * List of {@link TargetGroupLocator}s to use.
	 */
	private final List<TargetGroupLocator> locators;
	
	/**
	 * Creates a new instance with empty list of locators.
	 */
	public TargetGroupLocatorChain() {
		this.locators = new ArrayList<>();
	}

	/**
	 * Invokes each locator until the first locator found an objects, that uses given target group.
	 */
	@Override
	public TargetDeleteStatus isTargetGroupCanBeDeleted(int companyID, int targetGroupID) throws TargetGroupException {
        boolean isCanBeOnlyMarkedAsDeleted = false;

		for (TargetGroupLocator locator : this.locators) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking target group locator type " + locator.getClass().getCanonicalName());
			}
            TargetDeleteStatus status = locator.isTargetGroupCanBeDeleted(companyID, targetGroupID);
            if (status == TargetDeleteStatus.CANT_BE_DELETED){
                return TargetDeleteStatus.CANT_BE_DELETED;
            }
            if (status == TargetDeleteStatus.CAN_BE_MARKED_AS_DELETED){
                isCanBeOnlyMarkedAsDeleted = true;
            }
		}
		return isCanBeOnlyMarkedAsDeleted ? TargetDeleteStatus.CAN_BE_MARKED_AS_DELETED : TargetDeleteStatus.CAN_BE_FULLY_DELETED_FROM_DB;
	}

	// ------------------------------------------------------------- Dependency Injection
	/**
	 * Set list of {@link TargetGroupLocator} instances. The order of the locators in the list is the order in which the locators are used.
	 * 
	 * @param locators list of {@link TargetGroupLocator}s
	 */
	public void setTargetGroupLocators(List<TargetGroupLocator> locators) {
		this.locators.clear();
		this.locators.addAll(locators);
	}

}
