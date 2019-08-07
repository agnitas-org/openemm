/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.dao.impl;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.springframework.beans.factory.InitializingBean;

import com.agnitas.emm.core.action.dao.ActionOperationDao;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;

public abstract class AbstractActionOperationDaoImpl<T> extends BaseDaoImpl implements ActionOperationDao, InitializingBean {
	@Override
	public void afterPropertiesSet() throws Exception {
		// nothing to do
	}

	@Override
	@SuppressWarnings("unchecked")
	public final void getOperation(AbstractActionOperationParameters operation) {
		processGetOperation((T) operation);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public final void saveOperation(AbstractActionOperationParameters operation) {
		processSaveOperation((T) operation);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public final void updateOperation(AbstractActionOperationParameters operation) {
		processUpdateOperation((T) operation);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void deleteOperation(AbstractActionOperationParameters operation) {
		processDeleteOperation((T) operation);
	}
	
	protected abstract void processGetOperation(T operation);

	protected abstract void processSaveOperation(T operation);

	protected abstract void processUpdateOperation(T operation);
	
	protected abstract void processDeleteOperation(T operation);

}
