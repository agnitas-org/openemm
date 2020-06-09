/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.operations;

import java.util.Locale;

import org.apache.struts.action.ActionMessages;

import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComTrackpointDao;

public abstract class AbstractActionOperationParameters extends ActionOperationParameters {
	private int id;
	private int companyId;
	private int actionId;
	private ActionOperationType type;

	public AbstractActionOperationParameters(ActionOperationType type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
		this.actionId = actionId;
	}

	public ActionOperationType getOperationType() {
		return this.type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractActionOperationParameters other = (AbstractActionOperationParameters) obj;
		if (id == 0) {
			return false;
		} else if (id != other.id) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return id;
	}

	public boolean validate(ActionMessages errors, Locale locale, ComRecipientDao recipientDao, ComTrackpointDao trackpointDao) throws Exception {
		return true;
	}

	public String getUalDescription(AbstractActionOperationParameters oldOperation) {
		return "";
	}
}
