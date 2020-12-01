/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationActivateDoubleOptInParameters;
import com.agnitas.emm.core.action.operations.ActionOperationContentViewParameters;
import com.agnitas.emm.core.action.operations.ActionOperationExecuteScriptParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveListParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetArchiveMailingParameters;
import com.agnitas.emm.core.action.operations.ActionOperationGetCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationIdentifyCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSendMailingParameters;
import com.agnitas.emm.core.action.operations.ActionOperationServiceMailParameters;
import com.agnitas.emm.core.action.operations.ActionOperationSubscribeCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.operations.ActionOperationUnsubscribeCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters;
import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonObject;
import org.agnitas.actions.EmmAction;
import org.agnitas.actions.impl.EmmActionImpl;
import org.agnitas.dao.EmmActionDao;
import org.agnitas.dao.EmmActionOperationDao;
import org.agnitas.dao.EmmActionType;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.lang3.StringUtils;

public class ActionImporter extends BaseImporterExporter {
	@Resource(name="EmmActionDao")
	private EmmActionDao actionDao;
	
	@Resource(name="EmmActionOperationDao")
	private EmmActionOperationDao actionOperationDao;
	
	public int importAction(int companyID, JsonObject actionJsonObject, Map<Integer, Integer> actionIdMappings) throws Exception {
		int actionID = (Integer) actionJsonObject.get("id");
		
		String actionName = (String) actionJsonObject.get("name");
		String actionDescription = (String) actionJsonObject.get("description");
		String actionType = (String) actionJsonObject.get("type");
		JsonArray actionOperationsJsonArray = (JsonArray) actionJsonObject.get("actionOperations");
		
		int foundExistingActionID = -1;
		// Check for existing action with matching actionOperations
		for (EmmAction existingAction : actionDao.getEmmActionsByName(companyID, actionName)) {
			List<AbstractActionOperationParameters> existingActionOperations = actionOperationDao.getOperations(existingAction.getId(), existingAction.getCompanyID());
			boolean isMatching = true;
			if (StringUtils.equals(actionDescription, existingAction.getDescription())
					&& actionType.equals(EmmActionType.getEmmActionTypeByID(existingAction.getType()).name())
					&& actionOperationsJsonArray.size() == existingActionOperations.size()) {
				for (int i = 0; i < actionOperationsJsonArray.size(); i++) {
					JsonObject actionOperation = (JsonObject) actionOperationsJsonArray.get(i);
					String actionOperationType = (String) actionOperation.get("type");
					AbstractActionOperationParameters existingActionOperation = existingActionOperations.get(i);
					if (!actionOperationType.equals(existingActionOperation.getOperationType().getName())) {
						isMatching = false;
						break;
					} else {
						if (ActionOperationType.ACTIVATE_DOUBLE_OPT_IN.getName().equals(actionOperationType)) {
							Boolean forAllLists = (Boolean) actionOperation.get("forAllLists");
							if (forAllLists != ((ActionOperationActivateDoubleOptInParameters) existingActionOperation).isForAllLists()) {
								isMatching = false;
								break;
							}
						} else if (ActionOperationType.CONTENT_VIEW.getName().equals(actionOperationType)) {
							String tagName = (String) actionOperation.get("tagName");
							if (!StringUtils.equals(tagName, ((ActionOperationContentViewParameters) existingActionOperation).getTagName())) {
								isMatching = false;
								break;
							}
						} else if (ActionOperationType.EXECUTE_SCRIPT.getName().equals(actionOperationType)) {
							String script = (String) actionOperation.get("script");
							if (!StringUtils.equals(script, ((ActionOperationExecuteScriptParameters) existingActionOperation).getScript())) {
								isMatching = false;
								break;
							}
						} else if (ActionOperationType.GET_ARCHIVE_LIST.getName().equals(actionOperationType)) {
							int campaignID = (Integer) actionOperation.get("campaign_id");
							if (campaignID != ((ActionOperationGetArchiveListParameters) existingActionOperation).getCampaignID()) {
								isMatching = false;
								break;
							}
						} else if (ActionOperationType.GET_ARCHIVE_MAILING.getName().equals(actionOperationType)) {
							String expirationString = (String) actionOperation.get("expiration");
							ActionOperationGetArchiveMailingParameters actionOperationGetArchiveMailing = (ActionOperationGetArchiveMailingParameters) existingActionOperation;
							if (!StringUtils.equals(expirationString, actionOperationGetArchiveMailing.getExpireYear() + "-" + actionOperationGetArchiveMailing.getExpireMonth() + "-" + actionOperationGetArchiveMailing.getExpireDay())) {
								isMatching = false;
								break;
							}
						} else if (ActionOperationType.GET_CUSTOMER.getName().equals(actionOperationType)) {
							Boolean loadAlways = (Boolean) actionOperation.get("loadAlways");
							if (loadAlways != ((ActionOperationGetCustomerParameters) existingActionOperation).isLoadAlways()) {
								isMatching = false;
								break;
							}
						} else if (ActionOperationType.IDENTIFY_CUSTOMER.getName().equals(actionOperationType)) {
							String keyColumn = (String) actionOperation.get("keyColumn");
							String passColumn = (String) actionOperation.get("passColumn");
							if (!StringUtils.equals(keyColumn, ((ActionOperationIdentifyCustomerParameters) existingActionOperation).getKeyColumn())
									|| !StringUtils.equals(passColumn, ((ActionOperationIdentifyCustomerParameters) existingActionOperation).getPassColumn())) {
								isMatching = false;
								break;
							}
						} else if (ActionOperationType.SEND_MAILING.getName().equals(actionOperationType)) {
							int mailingID = (Integer) actionOperation.get("mailing_id");
							int delayMinutes = (Integer) actionOperation.get("delayMinutes");
							String bccAddress = (String) actionOperation.get("bccAddress");
							if (mailingID != ((ActionOperationSendMailingParameters) existingActionOperation).getMailingID()
									|| delayMinutes != ((ActionOperationSendMailingParameters) existingActionOperation).getDelayMinutes()
									|| !StringUtils.equals(bccAddress, ((ActionOperationSendMailingParameters) existingActionOperation).getBcc())) {
								isMatching = false;
								break;
							}
						} else if (ActionOperationType.SERVICE_MAIL.getName().equals(actionOperationType)) {
							int mailtype = MailType.getFromString((String) actionOperation.get("mailtype")).getIntValue();
							String toAddress = (String) actionOperation.get("toAddress");
							String fromAddress = (String) actionOperation.get("fromAddress");
							String replyAddress = (String) actionOperation.get("replyAddress");
							String subject = (String) actionOperation.get("subject");
							String textMail = (String) actionOperation.get("textMail");
							String htmlMail = (String) actionOperation.get("htmlMail");
							if (mailtype != ((ActionOperationServiceMailParameters) existingActionOperation).getMailtype()
									|| !StringUtils.equals(toAddress, ((ActionOperationServiceMailParameters) existingActionOperation).getToAddress())
									|| !StringUtils.equals(fromAddress, ((ActionOperationServiceMailParameters) existingActionOperation).getFromAddress())
									|| !StringUtils.equals(replyAddress, ((ActionOperationServiceMailParameters) existingActionOperation).getReplyAddress())
									|| !StringUtils.equals(subject, ((ActionOperationServiceMailParameters) existingActionOperation).getSubjectLine())
									|| !StringUtils.equals(textMail, ((ActionOperationServiceMailParameters) existingActionOperation).getTextMail())
									|| !StringUtils.equals(htmlMail, ((ActionOperationServiceMailParameters) existingActionOperation).getHtmlMail())) {
								isMatching = false;
								break;
							}
						} else if (ActionOperationType.SUBSCRIBE_CUSTOMER.getName().equals(actionOperationType)) {
							String keyColumn = (String) actionOperation.get("keyColumn");
							Boolean doubleCheck = (Boolean) actionOperation.get("doubleCheck");
							Boolean doubleOptIn = (Boolean) actionOperation.get("doubleOptIn");
							if (!StringUtils.equals(keyColumn, ((ActionOperationSubscribeCustomerParameters) existingActionOperation).getKeyColumn())
									|| doubleCheck != ((ActionOperationSubscribeCustomerParameters) existingActionOperation).isDoubleCheck()
									|| doubleOptIn != ((ActionOperationSubscribeCustomerParameters) existingActionOperation).isDoubleOptIn()) {
								isMatching = false;
								break;
							}
						} else if (ActionOperationType.UNSUBSCRIBE_CUSTOMER.getName().equals(actionOperationType)) {
							// ActionOperationUnsubscribeCustomer has no additional data
						} else if (ActionOperationType.UPDATE_CUSTOMER.getName().equals(actionOperationType)) {
							String columnName = (String) actionOperation.get("columnName");
							int updateType = (Integer) actionOperation.get("updateType");
							String updateValue = (String) actionOperation.get("updateValue");
							int trackingPointID = (Integer) actionOperation.get("trackingPoint_id");
							boolean useTrack = (Boolean) actionOperation.get("useTrack");
							if (!StringUtils.equals(columnName, ((ActionOperationUpdateCustomerParameters) existingActionOperation).getColumnName())
									|| updateType != ((ActionOperationUpdateCustomerParameters) existingActionOperation).getUpdateType()
									|| !StringUtils.equals(updateValue, ((ActionOperationUpdateCustomerParameters) existingActionOperation).getUpdateValue())
									|| trackingPointID != ((ActionOperationUpdateCustomerParameters) existingActionOperation).getTrackingPointId()
									|| useTrack != ((ActionOperationUpdateCustomerParameters) existingActionOperation).isUseTrack()) {
								isMatching = false;
								break;
							}
						} else {
							throw new Exception("Invalid actionoperation type: " + actionOperationType);
						}
					}
				}
			} else {
				isMatching = false;
			}
			if (isMatching) {
				foundExistingActionID = existingAction.getId();
				break;
			}
		}
		
		if (foundExistingActionID == -1) {
			// Create new action
			EmmAction newAction = new EmmActionImpl();
			newAction.setCompanyID(companyID);
			newAction.setShortname(actionName);
			newAction.setDescription(actionDescription);
			newAction.setType(EmmActionType.getEmmActionTypeByName(actionType).getActionTypeCode());
			
			for (int i = 0; i < actionOperationsJsonArray.size(); i++) {
				JsonObject actionOperation = (JsonObject) actionOperationsJsonArray.get(i);
				String actionOperationType = (String) actionOperation.get("type");
				
				if (ActionOperationType.ACTIVATE_DOUBLE_OPT_IN.getName().equals(actionOperationType)) {
					ActionOperationActivateDoubleOptInParameters actionOperationActivateDoubleOptIn = new ActionOperationActivateDoubleOptInParameters();
					actionOperationActivateDoubleOptIn.setForAllLists((Boolean) actionOperation.get("forAllLists"));
					newAction.getActionOperations().add(actionOperationActivateDoubleOptIn);
				} else if (ActionOperationType.CONTENT_VIEW.getName().equals(actionOperationType)) {
					ActionOperationContentViewParameters actionOperationContentView = new ActionOperationContentViewParameters();
					actionOperationContentView.setTagName((String) actionOperation.get("tagName"));
					newAction.getActionOperations().add(actionOperationContentView);
				} else if (ActionOperationType.EXECUTE_SCRIPT.getName().equals(actionOperationType)) {
					ActionOperationExecuteScriptParameters actionOperationExecuteScript = new ActionOperationExecuteScriptParameters();
					actionOperationExecuteScript.setScript((String) actionOperation.get("script"));
					newAction.getActionOperations().add(actionOperationExecuteScript);
				} else if (ActionOperationType.GET_ARCHIVE_LIST.getName().equals(actionOperationType)) {
					ActionOperationGetArchiveListParameters actionOperationGetArchiveList = new ActionOperationGetArchiveListParameters();
					actionOperationGetArchiveList.setCampaignID((Integer) actionOperation.get("campaign_id"));
					newAction.getActionOperations().add(actionOperationGetArchiveList);
				} else if (ActionOperationType.GET_ARCHIVE_MAILING.getName().equals(actionOperationType)) {
					ActionOperationGetArchiveMailingParameters actionOperationGetArchiveMailing = new ActionOperationGetArchiveMailingParameters();
					String[] dateParts = ((String) actionOperation.get("expiration")).split("-");
					actionOperationGetArchiveMailing.setExpireYear(Integer.parseInt(dateParts[0]));
					actionOperationGetArchiveMailing.setExpireMonth(Integer.parseInt(dateParts[1]));
					actionOperationGetArchiveMailing.setExpireDay(Integer.parseInt(dateParts[2]));
					newAction.getActionOperations().add(actionOperationGetArchiveMailing);
				} else if (ActionOperationType.GET_CUSTOMER.getName().equals(actionOperationType)) {
					ActionOperationGetCustomerParameters actionOperationGetCustomer = new ActionOperationGetCustomerParameters();
					actionOperationGetCustomer.setLoadAlways((Boolean) actionOperation.get("loadAlways"));
					newAction.getActionOperations().add(actionOperationGetCustomer);
				} else if (ActionOperationType.IDENTIFY_CUSTOMER.getName().equals(actionOperationType)) {
					ActionOperationIdentifyCustomerParameters actionOperationIdentifyCustomer = new ActionOperationIdentifyCustomerParameters();
					actionOperationIdentifyCustomer.setKeyColumn((String) actionOperation.get("keyColumn"));
					actionOperationIdentifyCustomer.setPassColumn((String) actionOperation.get("passColumn"));
					newAction.getActionOperations().add(actionOperationIdentifyCustomer);
				} else if (ActionOperationType.SEND_MAILING.getName().equals(actionOperationType)) {
					ActionOperationSendMailingParameters actionOperationSendMailing = new ActionOperationSendMailingParameters();
					actionOperationSendMailing.setMailingID((Integer) actionOperation.get("mailing_id"));
					actionOperationSendMailing.setDelayMinutes((Integer) actionOperation.get("delayMinutes"));
					actionOperationSendMailing.setBcc((String) actionOperation.get("bccAddress"));
					newAction.getActionOperations().add(actionOperationSendMailing);
				} else if (ActionOperationType.SERVICE_MAIL.getName().equals(actionOperationType)) {
					ActionOperationServiceMailParameters actionOperationServiceMail = new ActionOperationServiceMailParameters();
					actionOperationServiceMail.setMailtype(MailType.getFromString((String) actionOperation.get("mailtype")).getIntValue());
					actionOperationServiceMail.setToAddress((String) actionOperation.get("toAddress"));
					actionOperationServiceMail.setSubjectLine((String) actionOperation.get("subject"));
					actionOperationServiceMail.setTextMail((String) actionOperation.get("textMail"));
					actionOperationServiceMail.setHtmlMail((String) actionOperation.get("htmlMail"));
					newAction.getActionOperations().add(actionOperationServiceMail);
				} else if (ActionOperationType.SUBSCRIBE_CUSTOMER.getName().equals(actionOperationType)) {
					ActionOperationSubscribeCustomerParameters actionOperationSubscribeCustomer = new ActionOperationSubscribeCustomerParameters();
					actionOperationSubscribeCustomer.setKeyColumn((String) actionOperation.get("keyColumn"));
					actionOperationSubscribeCustomer.setDoubleCheck((Boolean) actionOperation.get("doubleCheck"));
					actionOperationSubscribeCustomer.setDoubleOptIn((Boolean) actionOperation.get("doubleOptIn"));
					newAction.getActionOperations().add(actionOperationSubscribeCustomer);
				} else if (ActionOperationType.UNSUBSCRIBE_CUSTOMER.getName().equals(actionOperationType)) {
					ActionOperationUnsubscribeCustomerParameters actionOperationUnsubscribeCustomer = new ActionOperationUnsubscribeCustomerParameters();								
					// ActionOperationUnsubscribeCustomer has no additional data
					newAction.getActionOperations().add(actionOperationUnsubscribeCustomer);
				} else if (ActionOperationType.UPDATE_CUSTOMER.getName().equals(actionOperationType)) {
					ActionOperationUpdateCustomerParameters actionOperationUpdateCustomer = new ActionOperationUpdateCustomerParameters();
					actionOperationUpdateCustomer.setColumnName((String) actionOperation.get("columnName"));
					actionOperationUpdateCustomer.setUpdateType((Integer) actionOperation.get("updateType"));
					actionOperationUpdateCustomer.setUpdateValue((String) actionOperation.get("updateValue"));
					actionOperationUpdateCustomer.setTrackingPointId((Integer) actionOperation.get("trackingPoint_id"));
					actionOperationUpdateCustomer.setUseTrack((Boolean) actionOperation.get("useTrack"));
					newAction.getActionOperations().add(actionOperationUpdateCustomer);
				} else {
					throw new Exception("Invalid actionoperation type: " + actionOperationType);
				}
			}
			
			actionDao.saveEmmAction(newAction);
			for (AbstractActionOperationParameters actionOperation : newAction.getActionOperations()) {
				actionOperation.setCompanyId(newAction.getCompanyID());
				actionOperation.setActionId(newAction.getId());
				actionOperationDao.saveOperation(actionOperation);
			}
			foundExistingActionID = newAction.getId();
		}
		
		if (actionIdMappings != null) {
			actionIdMappings.put(actionID, foundExistingActionID);
		}
		
		return foundExistingActionID;
	}
}
