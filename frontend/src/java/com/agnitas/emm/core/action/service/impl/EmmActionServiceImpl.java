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
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.action.service.EmmActionOperationService;
import com.agnitas.emm.core.action.service.EmmActionService;

public class EmmActionServiceImpl implements EmmActionService {
	
    protected EmmActionDao emmActionDao;
    private EmmActionOperationDao emmActionOperationDao;
    private ComMailingDao mailingDao;
    
    private EmmActionOperationService emmActionOperationService; 

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
		return saveEmmAction(emmAction);
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
		return saveEmmAction(subscribeAction);
	}

	@Override
	public int saveEmmAction(EmmAction action) {
    	return saveEmmAction(action, null);
	}

	@Override
	@Transactional
	public int saveEmmAction(EmmAction action, List<UserAction> userActions) {
    	EmmAction oldAction = action.getId() > 0 ? getEmmAction(action.getId(), action.getCompanyID()) : null;

		int actionId = emmActionDao.saveEmmAction(action);
		if (actionId > 0) {
			for (AbstractActionOperationParameters operation : action.getActionOperations()) {
				operation.setActionId(actionId);
				emmActionOperationDao.saveOperation(operation);
			}

			if (oldAction != null && oldAction.getActionOperations() != null) {
				for (AbstractActionOperationParameters operation : ListUtils.removeAll(oldAction.getActionOperations(), action.getActionOperations())) {
					emmActionOperationDao.deleteOperation(operation);
				}
			}

			if (userActions != null) {
				userActions.add(getChangesAsUserAction(action, oldAction));
			}
		}

		return actionId;
	}

	private UserAction getChangesAsUserAction(EmmAction newAction, EmmAction oldAction) {
    	List<String> description = new ArrayList<>();

		description.add(String.format("%s (%d)", newAction.getShortname(), newAction.getId()));

		if (oldAction == null) {
			description.add("Set type to " + getTypeAsString(newAction.getType()));
			description.add("Made " + (newAction.getIsActive() ? "active" : "inactive"));

			for (AbstractActionOperationParameters operation : newAction.getActionOperations()) {
				String changes = getChanges(null, operation);
				if (changes != null) {
					description.add(changes);
				}
			}
		} else {
			if (!StringUtils.equals(newAction.getShortname(), oldAction.getShortname())) {
				description.add("Renamed " + oldAction.getShortname() + " to " + newAction.getShortname());
			}
			if (newAction.getType() != oldAction.getType()) {
				description.add("Changed type to " + getTypeAsString(newAction.getType()));
			}
			if (newAction.getIsActive() != oldAction.getIsActive()) {
				description.add("Made " + (newAction.getIsActive() ? "active" : "inactive"));
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

				String changes = getChanges(oldOperation, newOperation);
				if (changes != null) {
					description.add(changes);
				}
			}
		}

    	return new UserAction(oldAction == null ? "create action" : "edit action", StringUtils.join(description, '\n'));
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

	private String getChanges(AbstractActionOperationParameters oldOperation, AbstractActionOperationParameters newOperation) {
    	if (newOperation == null) {
    		return "Removed " + oldOperation.getOperationType().getName() + " module #" + oldOperation.getId();
		}

		String description = newOperation.getUalDescription(oldOperation);
		String changes = (oldOperation == null ? "Added " : "Changed ") + newOperation.getOperationType().getName() +
				" module #" + newOperation.getId();

		if (StringUtils.isNotEmpty(description)) {
    		return changes + " (" + description + ")";
		}

		// Missing description means "nothing changed".
		if (oldOperation != null && StringUtils.isEmpty(description)) {
			return null;
		}

		return changes;
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
    				mailinglistIDs.add(mailingDao.getMailing(mailingID, companyID).getMailinglistID());
    			}
			}
    	}
        return mailinglistIDs;
	}

	public void setEmmActionDao(EmmActionDao emmActionDao) {
		this.emmActionDao = emmActionDao;
	}

	public void setEmmActionOperationDao(EmmActionOperationDao emmActionOperationDao) {
		this.emmActionOperationDao = emmActionOperationDao;
	}

	public void setEmmActionOperationService(EmmActionOperationService emmActionOperationService) {
		this.emmActionOperationService = emmActionOperationService;
	}

	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
}
