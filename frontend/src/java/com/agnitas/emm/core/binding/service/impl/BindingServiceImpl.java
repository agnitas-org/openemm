/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.binding.service.impl;

import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.binding.service.BindingService;
import jakarta.annotation.Resource;
import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.factory.BindingEntryFactory;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.binding.service.BindingModel;
import org.agnitas.emm.core.binding.service.BindingNotExistException;
import org.agnitas.emm.core.binding.service.BindingServiceException;
import org.agnitas.emm.core.binding.service.validation.BindingModelValidator;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.mailinglist.service.MailinglistNotExistException;
import org.agnitas.emm.core.mailinglist.service.impl.MailinglistException;
import org.agnitas.emm.core.recipient.service.RecipientNotExistException;
import org.agnitas.emm.core.velocity.Constants;
import org.agnitas.exceptions.InvalidUserStatusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BindingServiceImpl implements BindingService {

	private static final Logger logger = LogManager.getLogger(BindingServiceImpl.class);

	@Resource
	private EmmActionService emmActionService;
	private ComBindingEntryDao bindingEntryDao;
	private MailinglistDao mailinglistDao;
	private ComRecipientDao recipientDao;
	private BindingEntryFactory bindingEntryFactory;
	private BindingModelValidator bindingModelValidator;
	private MailingDao mailingDao;
	
	@Override
	public final boolean setBindingWithActionId(final BindingModel model, final boolean runActionInBackground) throws MailinglistException {
	    bindingModelValidator.assertIsValidToSet(model);
		setBindingInTransaction(model);  // Need this method call to set bindings (and only bindings) within transaction

		final EmmActionOperationErrors actionOperationErrors = new EmmActionOperationErrors();
		
		final Map<String, Object> params = new HashMap<>();
		params.put("customerID", model.getCustomerId());
		params.put(Constants.ACTION_OPERATION_ERRORS_CONTEXT_NAME, actionOperationErrors);
		
		final Runnable actionRunner = () -> {
            try {
                emmActionService.executeActions(model.getActionId(), model.getCompanyId(), params, actionOperationErrors);
            } catch (Exception e) {
                throw new RuntimeException(e);
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
	protected void setBindingInTransaction(final BindingModel model) throws MailinglistException {
		setBinding(model);
	}

	@Override
	@Transactional
	public BindingEntry getBinding(BindingModel model) {
		bindingModelValidator.assertIsValidToGetOrDelete(model);
		BindingEntry bindingEntry = bindingEntryDao.get(model.getCustomerId(), model.getCompanyId(), model.getMailinglistId(), model.getMediatype());
		if (bindingEntry == null) {
			throw new BindingNotExistException();
		}
		return bindingEntry;
	}

	@Override
	@Transactional
	public void setBinding(BindingModel model) throws MailinglistException {
		bindingModelValidator.assertIsValidToSet(model);
		if (!mailinglistDao.exist(model.getMailinglistId(), model.getCompanyId())) {
			throw new MailinglistNotExistException(model.getMailinglistId(), model.getCompanyId());
		}
		if (!recipientDao.exist(model.getCustomerId(), model.getCompanyId())) {
			throw new RecipientNotExistException();
		}
		if (model.getExitMailingId() != 0 && !mailingDao.exist(model.getExitMailingId(), model.getCompanyId())) {
			throw new MailingNotExistException(model.getCompanyId(), model.getExitMailingId());
		}

		// Check, that user status has valid value
		try {
			UserStatus.getUserStatusByID(model.getStatus());
		} catch(final Exception e) {
			throw new InvalidUserStatusException(model.getStatus());
		}

		BindingEntry binding = bindingEntryDao.get(model.getCustomerId(), model.getCompanyId(), model.getMailinglistId(), model.getMediatype());
		if(binding == null) {
			binding = bindingEntryFactory.newBindingEntry();
			binding.setCustomerID(model.getCustomerId());
			binding.setMailinglistID(model.getMailinglistId());
			binding.setMediaType(model.getMediatype());
		}
		binding.setUserStatus(model.getStatus());
		binding.setUserType(model.getUserType());
		binding.setExitMailingID(model.getExitMailingId());
		binding.setUserRemark(model.getRemark());
		bindingEntryDao.save(model.getCompanyId(), binding);
	}

	@Override
	@Transactional
	public void deleteBinding(BindingModel model) {
		bindingModelValidator.assertIsValidToGetOrDelete(model);
		if (!bindingEntryDao.exist(model.getCustomerId(), model.getCompanyId(), model.getMailinglistId(), model.getMediatype())) {
			throw new BindingNotExistException();
		}
		bindingEntryDao.delete(model.getCustomerId(), model.getCompanyId(), model.getMailinglistId(), model.getMediatype());
	}

	@Override
	@Transactional
	public List<BindingEntry> getBindings(BindingModel model) {
		bindingModelValidator.assertIsValidToList(model);
		if (!recipientDao.exist(model.getCustomerId(), model.getCompanyId())) {
			throw new RecipientNotExistException();
		}
		return bindingEntryDao.getBindings(model.getCompanyId(), model.getCustomerId());
	}

	@Override
	public void updateBindingStatusByEmailPattern(int companyId, String emailPattern, UserStatus userStatus, String remark) throws BindingServiceException {
		try {
			this.bindingEntryDao.updateBindingStatusByEmailPattern( companyId, emailPattern, userStatus.getStatusCode(), remark);
		} catch( Exception e) {
			logger.error( "Error updating binding status by email pattern (company ID: " + companyId + ", pattern: " + emailPattern + ")");

			throw new BindingServiceException( "Error updating binding status by email pattern", e);
		}
	}

	@Required
	public void setBindingEntryDao(ComBindingEntryDao bindingEntryDao) {
		this.bindingEntryDao = bindingEntryDao;
	}

	@Required
	public void setMailinglistDao(MailinglistDao mailinglistDao) {
		this.mailinglistDao = mailinglistDao;
	}

	@Required
	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}

	@Required
	public void setBindingEntryFactory(BindingEntryFactory bindingEntryFactory) {
		this.bindingEntryFactory = bindingEntryFactory;
	}

	@Required
	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Required
	public void setBindingModelValidator(BindingModelValidator bindingModelValidator) {
		this.bindingModelValidator = bindingModelValidator;
	}
}
