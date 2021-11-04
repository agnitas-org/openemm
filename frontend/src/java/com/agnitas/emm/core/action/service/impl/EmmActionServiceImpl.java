/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.agnitas.actions.EmmAction;
import org.agnitas.dao.EmmActionDao;
import org.agnitas.dao.EmmActionOperationDao;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DateUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.action.dto.EmmActionDto;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationService;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.commons.ActivenessStatus;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ExtendedConversionService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class EmmActionServiceImpl implements EmmActionService {
    protected EmmActionDao emmActionDao;
    
    private EmmActionOperationDao emmActionOperationDao;
    
    private ComMailingDao mailingDao;
    
    private EmmActionOperationService emmActionOperationService;

    private ExtendedConversionService conversionService;

	@Required
	public void setEmmActionDao(EmmActionDao emmActionDao) {
		this.emmActionDao = emmActionDao;
	}

	@Required
	public void setEmmActionOperationDao(EmmActionOperationDao emmActionOperationDao) {
		this.emmActionOperationDao = emmActionOperationDao;
	}

	@Required
	public void setEmmActionOperationService(EmmActionOperationService emmActionOperationService) {
		this.emmActionOperationService = emmActionOperationService;
	}

	@Required
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Required
	public void setConversionService(ExtendedConversionService conversionService) {
		this.conversionService = conversionService;
	}

	@Override
    public boolean actionExists(final int actionID, @VelocityCheck final int companyID) {
    	return emmActionDao.actionExists(actionID, companyID);
    }
    
	@Override
	public boolean executeActions(int actionID, @VelocityCheck int companyID, Map<String, Object> params, final EmmActionOperationErrors errors) throws Exception {
    	if (actionID == 0 || companyID <= 0) {
    		return false;
    	}

    	List<AbstractActionOperationParameters> operations = emmActionOperationDao.getOperations(actionID, companyID);
    	if (!operations.isEmpty()) {
    		for (AbstractActionOperationParameters operation : operations) {
    			if (!emmActionOperationService.executeOperation(operation, params, errors)) {
    				return false;
    			}
			}
    	}
    	
        return true;
	}
	
	@Override
	@Transactional
	public int copyEmmAction(EmmAction emmAction, int toCompanyId) {
		List<AbstractActionOperationParameters> ops
			= emmActionOperationDao.getOperations(emmAction.getId(), emmAction.getCompanyID());
		for (AbstractActionOperationParameters op : ops) {
			op.setId(0);
			op.setCompanyId(toCompanyId);
		}
		emmAction.setActionOperations(ops);
		emmAction.setId(0);
		emmAction.setCompanyID(toCompanyId);
		return saveEmmAction(toCompanyId, emmAction);
	}
	
	@Override
	@Transactional
	public int copyActionOperations(int sourceActionCompanyID, int sourceActionID, int destinationActionCompanyId, int destinationActionID) {
		EmmAction subscribeAction = getEmmAction(destinationActionID, destinationActionCompanyId);
		List<AbstractActionOperationParameters> ops = emmActionOperationDao.getOperations(sourceActionID, sourceActionCompanyID);
		for (AbstractActionOperationParameters op : ops) {
			op.setId(0);
			op.setCompanyId(destinationActionCompanyId);
		}
		subscribeAction.setActionOperations(ops);
		return saveEmmAction(destinationActionCompanyId, subscribeAction);
	}

	@Transactional
	@Override
	public int saveEmmAction(int companyId, EmmAction action) {
    	return saveEmmAction(companyId, action, null);
	}

	@Override
	@Transactional
    public int saveEmmAction(int companyId, EmmAction action, List<UserAction> userActions) {
    	EmmAction oldAction = action.getId() > 0 ? getEmmAction(action.getId(), action.getCompanyID()) : null;

		int actionId = emmActionDao.saveEmmAction(action);
		if (actionId > 0) {
			for (AbstractActionOperationParameters operation : action.getActionOperations()) {
				operation.setCompanyId(companyId);
				operation.setActionId(actionId);
				emmActionOperationDao.saveOperation(operation);
			}

			if (oldAction != null && CollectionUtils.isNotEmpty(oldAction.getActionOperations())) {
				for (AbstractActionOperationParameters operation : ListUtils.removeAll(oldAction.getActionOperations(), action.getActionOperations())) {
					emmActionOperationDao.deleteOperation(operation);
				}
			}

			if (userActions != null) {
				UserAction userAction =
						new UserAction(oldAction == null ? "create action" : "edit action",
						String.format("%s (%d)%n%s", action.getShortname(), action.getId(),
								StringUtils.join(getChangesDescriptions(action, oldAction), '\n')));

				userActions.add(userAction);
			}
		}

		return actionId;
	}

	private List<String> getChangesDescriptions(EmmAction newAction, EmmAction oldAction) {
    	List<String> descriptions = new ArrayList<>();

		if (oldAction == null) {
			descriptions.add(String.format("Set type to %s.", getTypeAsString(newAction.getType())));
			descriptions.add(String.format("Made %s.", newAction.getIsActive() ? "active" : "inactive"));

			for (AbstractActionOperationParameters operation : newAction.getActionOperations()) {
				String operationDescription = asUalDescription(operation, null);
				// Missing description means "nothing changed".
				if (StringUtils.isNotBlank(operationDescription)) {
					descriptions.add(operationDescription);
				}
			}
		} else {
			if (!StringUtils.equals(newAction.getShortname(), oldAction.getShortname())) {
				descriptions.add(String.format("Renamed %s to %s.", oldAction.getShortname(), newAction.getShortname()));
			}

			if (newAction.getType() != oldAction.getType()) {
				descriptions.add(String.format("Changed type from %s to %s.", getTypeAsString(oldAction.getType()), getTypeAsString(newAction.getType())));
			}

			if (newAction.getIsActive() != oldAction.getIsActive()) {
				descriptions.add(String.format("Made %s.", newAction.getIsActive() ? "active" : "inactive"));
			}

			List<AbstractActionOperationParameters> newOperations = newAction.getActionOperations();
			List<AbstractActionOperationParameters> oldOperations = oldAction.getActionOperations();

			// Order by id to join both lists by id below.
			newOperations.sort(Comparator.comparingInt(AbstractActionOperationParameters::getId));
			oldOperations.sort(Comparator.comparingInt(AbstractActionOperationParameters::getId));

			int newIndex = 0;
			int oldIndex = 0;

			while (newIndex < newOperations.size() || oldIndex < oldOperations.size()) {
				AbstractActionOperationParameters newOperation = null;
				AbstractActionOperationParameters oldOperation = null;

				if (newIndex < newOperations.size()) {
					newOperation = newOperations.get(newIndex++);
				}

				if (oldIndex < oldOperations.size()) {
					oldOperation = oldOperations.get(oldIndex++);

					if (newOperation != null) {
						// Join if ids match or roll back a side having greater id otherwise.
						if (newOperation.getId() < oldOperation.getId()) {
							oldOperation = null;
							oldIndex--;
						} else if (newOperation.getId() > oldOperation.getId()) {
							newOperation = null;
							newIndex--;
						}
					}
				}

				String operationDescription = asUalDescription(newOperation, oldOperation);
				if (StringUtils.isEmpty(operationDescription)) {
					// Missing description means "nothing changed".
					descriptions.add(operationDescription);
				}
			}
		}

    	return descriptions;
	}

	private String getTypeAsString(int type) {
    	switch (type) {
			case EmmAction.TYPE_LINK:
				return "Links";
			case EmmAction.TYPE_FORM:
				return "Forms";
			case EmmAction.TYPE_ALL:
				return "Links and forms";
			default:
				return "Unknown";
		}
	}

	private String asUalDescription(AbstractActionOperationParameters newOperation, AbstractActionOperationParameters oldOperation) {
		if (newOperation == null && oldOperation == null) {
			return "";
		}

		if (newOperation == null) {
			return String.format("Removed %s module #%d.", oldOperation.getOperationType().getName(), oldOperation.getId());
		}

		String description = "";

		if (newOperation instanceof ActionOperationSendMailingParameters) {
			ActionOperationSendMailingParameters sendMailingOp = (ActionOperationSendMailingParameters) newOperation;

			if (oldOperation instanceof ActionOperationSendMailingParameters) {
				ActionOperationSendMailingParameters sendMailingOldOp = (ActionOperationSendMailingParameters) oldOperation;
				if (!StringUtils.equals(sendMailingOldOp.getBcc(), sendMailingOp.getBcc())) {
					description = "Changed bcc emails to: " + sendMailingOp.getBcc();
				}
			} else {
				description = "Set bcc emails to: " + sendMailingOp.getBcc();
			}

		}

		return StringUtils.isNotEmpty(description) ? String.format("%s %s module #%d (%s)",
				oldOperation == null ? "Added " : "Changed ",
				newOperation.getOperationType().getName(), newOperation.getId(), description) : "";
	}


	@Override
	public EmmAction getEmmAction(int actionID, @VelocityCheck int companyID) {
		EmmAction action = emmActionDao.getEmmAction(actionID, companyID);
		if (action != null) {
			List<AbstractActionOperationParameters> operations = emmActionOperationDao.getOperations(actionID, companyID);
			
			action.setActionOperations(operations);
		}
		return action;
	}
	
	@Override
	@Transactional
	public boolean deleteEmmAction(int actionID, @VelocityCheck int companyID) {
		// Action operations must not be deleted when action itself is marked as deleted.
		// emmActionOperationDao.deleteOperations(actionID, companyID);
		
		return emmActionDao.deleteEmmAction(actionID, companyID);
	}
    
	@Override
	public List<Integer> getReferencedMailinglistsFromAction(int companyID, int actionID) {
		List<Integer> mailinglistIDs = new ArrayList<>();
    	if (actionID != 0) {
    		List<AbstractActionOperationParameters> operations = emmActionOperationDao.getOperations(actionID, companyID);
    		for (AbstractActionOperationParameters operation : operations) {
    			if (operation instanceof ActionOperationSendMailingParameters) {
    				int mailingID = ((ActionOperationSendMailingParameters) operation).getMailingID();
    				mailinglistIDs.add(mailingDao.getMailinglistId(mailingID, companyID));
    			}
			}
    	}
        return mailinglistIDs;
	}

	@Override
	public List<EmmAction> getActionListBySendMailingId(@VelocityCheck int companyId, int mailingId) {
		return emmActionDao.getActionListBySendMailingId(companyId, mailingId);
	}

    @Override
    public EmmActionDto getCopyOfAction(ComAdmin admin, int originId) {
		EmmAction originAction = emmActionDao.getEmmAction(originId, admin.getCompanyID());
		if (originAction != null) {
			String copyShortname = I18nString.getLocaleString("mailing.CopyOf", admin.getLocale()) + " " + originAction.getShortname();
			originAction.setId(0);
			originAction.setShortname(copyShortname);

			// An operations should be cloned, not referenced
			List<AbstractActionOperationParameters> operations = emmActionOperationDao.getOperations(originId, admin.getCompanyID());
			for (AbstractActionOperationParameters operation : CollectionUtils.emptyIfNull(operations)) {
				operation.setId(0);
				operation.setActionId(0);
			}
			originAction.setActionOperations(operations);

			return conversionService.convert(originAction, EmmActionDto.class);
		} else {
			return new EmmActionDto();
		}
    }

    @Override
    public JSONArray getEmmActionsJson(ComAdmin admin) {
        JSONArray actionsJson = new JSONArray();
		for (EmmAction action: emmActionDao.getEmmActions(admin.getCompanyID())) {
            JSONObject entry = new JSONObject();

			entry.element("id", action.getId());
			entry.element("shortname", action.getShortname());
			entry.element("description", action.getDescription());
			entry.element("formNames", emmActionDao.getActionUserFormNames(action.getId(), admin.getCompanyID()));
			entry.element("creationDate", DateUtilities.toLong(action.getCreationDate()));
			entry.element("changeDate", DateUtilities.toLong(action.getChangeDate()));
			entry.element("activeStatus", action.getIsActive() ? ActivenessStatus.ACTIVE : ActivenessStatus.INACTIVE);

			actionsJson.add(entry);
		}
		return actionsJson;
    }
}
