/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.operations;

import java.util.Set;

public class ActionOperationUnsubscribeCustomerParameters extends AbstractActionOperationParameters {

    private Set<Integer> mailinglistIds;
    
    private boolean allMailinglistsSelected;
    
    private boolean additionalMailinglists;
    
	public ActionOperationUnsubscribeCustomerParameters() {
		super(ActionOperationType.UNSUBSCRIBE_CUSTOMER);
	}

    public Set<Integer> getMailinglistIds() {
        return mailinglistIds;
    }

    public void setMailinglistIds(Set<Integer> mailinglistIds) {
        this.mailinglistIds = mailinglistIds;
    }
    
    public boolean isAllMailinglistsSelected() {
        return allMailinglistsSelected;
    }

    public void setAllMailinglistsSelected(boolean allMailinglistsSelected) {
        this.allMailinglistsSelected = allMailinglistsSelected;
    }

    public boolean isAdditionalMailinglists() {
        return additionalMailinglists;
    }

    public void setAdditionalMailinglists(boolean additionalMailinglists) {
        this.additionalMailinglists = additionalMailinglists;
    }
}
