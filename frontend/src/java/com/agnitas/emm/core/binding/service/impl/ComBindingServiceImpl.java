/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.binding.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.agnitas.emm.core.binding.service.BindingModel;
import org.agnitas.emm.core.binding.service.impl.BindingServiceImpl;
import org.agnitas.emm.core.mailinglist.service.impl.MailinglistException;
import org.agnitas.emm.core.validator.annotation.Validate;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.emm.core.action.service.ComEmmActionService;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.binding.service.ComBindingService;

public abstract class ComBindingServiceImpl extends BindingServiceImpl implements ComBindingService {

	@Resource
	private ComEmmActionService emmActionService;
	
	@Override
	@Validate("setBinding")
	public final boolean setBindingWithActionId(final BindingModel model, final boolean runActionInBackground) throws MailinglistException {
		setBindingInTransaction(model);  // Need this method call to set bindings (and only bindings) within transation

		final EmmActionOperationErrors actionOperationErrors = new EmmActionOperationErrors();
		
		final Map<String, Object> params = new HashMap<>();
		params.put("customerID", model.getCustomerId());
		params.put("actionErrors", actionOperationErrors);
		
		final Runnable actionRunner = new Runnable() {
			@Override
			public final void run() {
				try {
					emmActionService.executeActions(model.getActionId(), model.getCompanyId(), params, actionOperationErrors);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		

		if(runActionInBackground) {
			new Thread(actionRunner).start();
		} else {
			actionRunner.run();
		}
		
		return true;
	}
	
	@Transactional
	private void setBindingInTransaction(final BindingModel model) throws MailinglistException {
		setBinding(model);
	}

}
