/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.binding.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.factory.BindingEntryFactory;
import com.agnitas.beans.impl.BindingEntryImpl;
import com.agnitas.dao.BindingEntryDao;
import com.agnitas.dao.BindingEntryDao.BulkDeleteBindingsFilter;
import com.agnitas.dao.BindingEntryDao.ListBindingsFilter;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.common.UpsertResult;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.action.ActionNotFoundException;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.binding.BindingIdentifier;
import com.agnitas.emm.core.binding.BulkDeleteBindingCommand;
import com.agnitas.emm.core.binding.BulkUpsertBindingCommand;
import com.agnitas.emm.core.binding.CreateBindingCommand;
import com.agnitas.emm.core.binding.DeleteBindingCommand;
import com.agnitas.emm.core.binding.GetBindingQuery;
import com.agnitas.emm.core.binding.ListBindingsQuery;
import com.agnitas.emm.core.binding.PatchBindingCommand;
import com.agnitas.emm.core.binding.UpsertBindingResult;
import com.agnitas.emm.core.binding.exception.BindingNotExistException;
import com.agnitas.emm.core.binding.service.BindingCreateResult;
import com.agnitas.emm.core.binding.service.BindingMapper;
import com.agnitas.emm.core.binding.service.BindingModel;
import com.agnitas.emm.core.binding.service.BindingService;
import com.agnitas.emm.core.binding.service.validation.BindingModelValidator;
import com.agnitas.emm.core.commons.exception.EntityAlreadyExistsException;
import com.agnitas.emm.core.commons.validation.DeleteResult;
import com.agnitas.emm.core.mailing.exception.MailingNotExistException;
import com.agnitas.emm.core.mailinglist.exception.MailinglistNotExistException;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.recipient.exception.RecipientNotExistException;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.velocity.Constants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("bindingService")
public class BindingServiceImpl implements BindingService {

	private final EmmActionService emmActionService;
	private final BindingEntryDao bindingEntryDao;
	private final MailinglistService mailinglistService;
	private final RecipientService recipientService;
	private final BindingEntryFactory bindingEntryFactory;
	private final BindingModelValidator bindingModelValidator;
	private final MailingDao mailingDao;
	private final BindingMapper bindingMapper;

    public BindingServiceImpl(
            EmmActionService emmActionService,
            BindingEntryDao bindingEntryDao,
			MailinglistService mailinglistService,
            RecipientService recipientService,
            BindingEntryFactory bindingEntryFactory,
            BindingModelValidator bindingModelValidator,
            MailingDao mailingDao,
			BindingMapper bindingMapper
    ) {
        this.emmActionService = emmActionService;
        this.bindingEntryDao = bindingEntryDao;
        this.mailinglistService = mailinglistService;
        this.recipientService = recipientService;
        this.bindingEntryFactory = bindingEntryFactory;
        this.bindingModelValidator = bindingModelValidator;
        this.mailingDao = mailingDao;
        this.bindingMapper = bindingMapper;
    }

	@Override
	public boolean setBindingWithActionId(BindingModel model, boolean runActionInBackground) {
	    bindingModelValidator.assertIsValidToSet(model);
		setBindingInTransaction(model);  // Need this method call to set bindings (and only bindings) within transaction

		final EmmActionOperationErrors actionOperationErrors = new EmmActionOperationErrors();
		
		final Map<String, Object> params = new HashMap<>();
		params.put("customerID", model.getCustomerId());
		params.put(Constants.ACTION_OPERATION_ERRORS_CONTEXT_NAME, actionOperationErrors);
		
		final Runnable actionRunner = () -> emmActionService.executeActions(
				model.getActionId(),
				model.getCompanyId(),
				params,
				actionOperationErrors
		);
		

		if(runActionInBackground) {
			new Thread(actionRunner).start();
		} else {
			actionRunner.run();
		}
		
		return true;
	}
	
	@Transactional
	protected void setBindingInTransaction(BindingModel model) {
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
	public void setBinding(BindingModel model) {
		bindingModelValidator.assertIsValidToSet(model);
		if (!mailinglistService.exist(model.getMailinglistId(), model.getCompanyId())) {
			throw new MailinglistNotExistException(model.getMailinglistId(), model.getCompanyId());
		}
		if (!recipientService.recipientExists(model.getCompanyId(), model.getCustomerId())) {
			throw new RecipientNotExistException();
		}
		if (model.getExitMailingId() != 0 && !mailingDao.exist(model.getExitMailingId(), model.getCompanyId())) {
			throw new MailingNotExistException(model.getCompanyId(), model.getExitMailingId());
		}

		// Check, that user status has valid value
		UserStatus.getByCode(model.getStatus());

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
		if (!recipientService.recipientExists(model.getCompanyId(), model.getCustomerId())) {
			throw new RecipientNotExistException();
		}
		return bindingEntryDao.getBindings(model.getCompanyId(), model.getCustomerId());
	}

	@Override
	public void updateBindingStatusByEmailPattern(int companyId, String emailPattern, UserStatus userStatus, String remark) {
		this.bindingEntryDao.updateBindingStatusByEmailPattern( companyId, emailPattern, userStatus, remark);
	}

	@Override
	public int updateBindings(int companyId, BindingEntry... bindings) {
		return bindingEntryDao.updateBindings(companyId, bindings);
	}

	@Override
	public int updateBindings(int companyId, List<BindingEntry> bindings) {
		return bindingEntryDao.updateBindings(companyId, bindings);
	}

	@Override
	public void updateBinding(PatchBindingCommand command, int companyId) {
		BindingEntry binding = get(command.getQuery(), companyId);

		command.userStatus().ifPresent(status -> binding.setUserStatus(status.getStatusCode()));
		command.userRemark().ifPresent(binding::setUserRemark);
		command.userType().ifPresent(userType -> binding.setUserType(userType != null ? userType.getTypeCode() : null));
		command.referrer().ifPresent(binding::setReferrer);
		command.entryMailingId().ifPresent(id -> binding.setEntryMailingID(id != null ? id : 0));
		command.exitMailingId().ifPresent(id -> binding.setExitMailingID(id != null ? id : 0));

		updateBindings(companyId, binding);
	}

	@Override
	public boolean updateStatus(BindingEntry binding, int companyId) {
		return bindingEntryDao.updateStatus(binding, companyId);
	}

	@Override
	public int insertBindings(int companyId, BindingEntry... bindings) {
		return bindingEntryDao.insertBindings(companyId, bindings);
	}

	@Override
	public int insertBindings(int companyId, List<BindingEntry> bindings) {
		return bindingEntryDao.insertBindings(companyId, bindings);
	}

	@Override
	public boolean exist(int recipientId, int companyId, int mailinglistId, MediaTypes mediatype) {
		return bindingEntryDao.exist(recipientId, companyId, mailinglistId, mediatype.getMediaCode());
	}

	@Override
	public BindingEntry get(GetBindingQuery query, int companyId) {
		return find(query, companyId).orElseThrow(() -> new BindingNotExistException(query.toString()));
	}

	@Override
	public Optional<BindingEntry> find(GetBindingQuery query, int companyId) {
		return Optional.ofNullable(bindingEntryDao.get(
				query.recipientId(),
				companyId,
				query.mailinglistId(),
				query.mediatype().getMediaCode()
		));
	}

    @Override
	public PaginatedList<BindingEntry> find(ListBindingsQuery query, int companyId) {
		ListBindingsFilter filter = bindingMapper.toFilter(query);
		return bindingEntryDao.find(filter, companyId);
	}

	@Override
	public BindingCreateResult create(CreateBindingCommand command, int companyId) {
		validateForCreate(command, companyId);

		BindingEntryImpl binding = bindingMapper.toEntity(command);
		insertBindings(companyId, binding);

		return new BindingCreateResult(
				get(bindingMapper.toGetQuery(command), companyId),
				command.triggerId() > 0 ? executeBindingCreateAction(command, companyId) : null
		);
	}

	@Override
	public void deleteBinding(DeleteBindingCommand command, int companyId) {
		validateRecipient(command.recipientId(), companyId);
		validateMailinglist(command.mailinglistId(), companyId);
		bindingEntryDao.delete(
				command.recipientId(),
				companyId,
				command.mailinglistId(),
				command.mediatype().getMediaCode()
		);
	}

	@Override
	public List<DeleteResult> bulkDelete(BulkDeleteBindingCommand command) {
		BulkDeleteBindingsFilter filter = bindingMapper.toFilter(command);
		List<Integer> deletedIds = bindingEntryDao.delete(filter);
		return command.recipientIds()
				.stream()
				.map(id -> deletedIds.contains(id)
						? DeleteResult.deleted(id)
						: DeleteResult.notFound(id)
				)
				.toList();
	}

	private void validateForCreate(CreateBindingCommand query, int companyId) {
		validateRecipient(query.recipientId(), companyId);
		validateMailinglist(query.mailinglistId(), companyId);
		if (exist(query.recipientId(), companyId, query.mailinglistId(), query.mediatype())) {
			throw new EntityAlreadyExistsException("Binding");
		}
		if (query.triggerId() > 0 && !emmActionService.actionExists(query.triggerId(), companyId)) {
			throw new ActionNotFoundException(query.triggerId());
		}
	}

	private void validateRecipient(int recipientId, int companyId) {
		if (!recipientService.recipientExists(companyId, recipientId)) {
			throw new RecipientNotExistException(recipientId);
		}
	}

	private void validateMailinglist(int mailinglistId, int companyId) {
		if (!mailinglistService.exist(mailinglistId, companyId)) {
			throw new MailinglistNotExistException(mailinglistId, companyId);
		}
	}

	private String executeBindingCreateAction(CreateBindingCommand command, int companyId) {
		EmmActionOperationErrors errors = new EmmActionOperationErrors();
		Map<String, Object> params = Map.of(
				"customerID", command.recipientId(),
				Constants.ACTION_OPERATION_ERRORS_CONTEXT_NAME, errors
		);
		Runnable task = () -> emmActionService.executeActions(
				command.triggerId(),
				companyId,
				params,
				errors
		);
		if (command.runActionAsynchronous()) {
			Thread.startVirtualThread(task);
			return "Action started";
		} else {
			task.run();
			return errors.isEmpty() ? "Action executed" : errors.toString();
		}
	}

	@Override
	public List<UpsertBindingResult> upsert(BulkUpsertBindingCommand command, int companyId) {
		ListBindingsFilter query = bindingMapper.toFilter(command);
		Map<BindingIdentifier, UserStatus> existingState = bindingEntryDao.getExistingState(query, companyId);
		List<CreateBindingCommand> inserts = new ArrayList<>();
		List<CreateBindingCommand> updates = new ArrayList<>();
		List<UpsertBindingResult> results = new ArrayList<>();

		for (MediaTypes mediaType : command.mediatypes()) {
			for (Integer mlId : command.mailinglistIds()) {
				for (Integer recipientId : command.recipientIds()) {
					UserStatus currentStatus = existingState.get(new BindingIdentifier(recipientId, mlId, mediaType));
					if (currentStatus == null) {
						inserts.add(CreateBindingCommand.builder()
								.recipientId(recipientId)
								.userType(UserType.World)
								.userStatus(command.status())
								.mailinglistId(mlId)
								.mediatype(mediaType)
								.userRemark(command.remark())
								.build());
						results.add(new UpsertBindingResult(mediaType, mlId, recipientId, UpsertResult.INSERTED));
					} else if (currentStatus != command.status()) {
						updates.add(CreateBindingCommand.builder()
								.recipientId(recipientId)
								.userStatus(command.status())
								.mailinglistId(mlId)
								.mediatype(mediaType)
								.userRemark(command.remark())
								.build());
						results.add(new UpsertBindingResult(mediaType, mlId, recipientId, UpsertResult.UPDATED));
					} else {
						results.add(new UpsertBindingResult(mediaType, mlId, recipientId, UpsertResult.NOT_CHANGED));
					}
				}
			}
		}
		bindingEntryDao.batchInsertBindings(inserts, companyId);
		bindingEntryDao.batchUpdateBindings(updates, companyId);
		return results;
	}
}
