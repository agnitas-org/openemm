/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import java.util.List;
import java.util.Set;
import javax.annotation.Resource;

import com.agnitas.beans.LinkProperty;
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
import com.agnitas.json.JsonWriter;
import org.agnitas.actions.EmmAction;
import org.agnitas.dao.EmmActionDao;
import org.agnitas.dao.EmmActionOperationDao;
import org.agnitas.dao.EmmActionType;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class ActionExporter extends BaseImporterExporter {
	@Resource(name="EmmActionDao")
	private EmmActionDao actionDao;
	
	@Resource(name="EmmActionOperationDao")
	private EmmActionOperationDao actionOperationDao;
	
	protected void exportActions(JsonWriter writer, int companyID, Set<Integer> actionIDs) throws Exception {
		if (actionIDs.size() > 0) {
			writer.openJsonObjectProperty("actions");
			writer.openJsonArray();
			for (int actionID : actionIDs) {
				exportAction(companyID, actionID, writer);
			}
			writer.closeJsonArray();
		}
	}
	
	public void exportAction(int companyID, int actionID, JsonWriter writer) throws Exception {
		EmmAction action = actionDao.getEmmAction(actionID, companyID);
		action.setActionOperations(actionOperationDao.getOperations(actionID, companyID));
		writer.openJsonObject();
		
		writeJsonObjectAttribute(writer, "id", action.getId());
		writeJsonObjectAttributeWhenNotNullOrBlank(writer, "name", action.getShortname());
		writeJsonObjectAttributeWhenNotNullOrBlank(writer, "description", action.getDescription());
		writeJsonObjectAttributeWhenNotNullOrBlank(writer, "type", EmmActionType.getEmmActionTypeByID(action.getType()).name());
		
		writer.openJsonObjectProperty("actionOperations");
		writer.openJsonArray();
		for (AbstractActionOperationParameters actionOperation : action.getActionOperations()) {
			writer.openJsonObject();
			
			writeJsonObjectAttribute(writer, "id", actionOperation.getId());
			writeJsonObjectAttribute(writer, "type", actionOperation.getOperationType().getName());
			
			if (ActionOperationType.ACTIVATE_DOUBLE_OPT_IN.equals(actionOperation.getOperationType())) {
				ActionOperationActivateDoubleOptInParameters actionOperationActivateDoubleOptIn = (ActionOperationActivateDoubleOptInParameters) actionOperation;
				writeJsonObjectAttribute(writer, "forAllLists", actionOperationActivateDoubleOptIn.isForAllLists());
			} else if (ActionOperationType.CONTENT_VIEW.equals(actionOperation.getOperationType())) {
				ActionOperationContentViewParameters actionOperationContentView = (ActionOperationContentViewParameters) actionOperation;
				writeJsonObjectAttribute(writer, "tagName", actionOperationContentView.getTagName());
			} else if (ActionOperationType.EXECUTE_SCRIPT.equals(actionOperation.getOperationType())) {
				ActionOperationExecuteScriptParameters actionOperationExecuteScript = (ActionOperationExecuteScriptParameters) actionOperation;
				writeJsonObjectAttribute(writer, "script", actionOperationExecuteScript.getScript());
			} else if (ActionOperationType.GET_ARCHIVE_LIST.equals(actionOperation.getOperationType())) {
				ActionOperationGetArchiveListParameters actionOperationGetArchiveList = (ActionOperationGetArchiveListParameters) actionOperation;
				writeJsonObjectAttribute(writer, "campaign_id", actionOperationGetArchiveList.getCampaignID());
			} else if (ActionOperationType.GET_ARCHIVE_MAILING.equals(actionOperation.getOperationType())) {
				ActionOperationGetArchiveMailingParameters actionOperationGetArchiveMailing = (ActionOperationGetArchiveMailingParameters) actionOperation;
				writeJsonObjectAttribute(writer, "expiration", actionOperationGetArchiveMailing.getExpireYear() + "-" + actionOperationGetArchiveMailing.getExpireMonth() + "-" + actionOperationGetArchiveMailing.getExpireDay());
			} else if (ActionOperationType.GET_CUSTOMER.equals(actionOperation.getOperationType())) {
				ActionOperationGetCustomerParameters actionOperationGetCustomer = (ActionOperationGetCustomerParameters) actionOperation;
				writeJsonObjectAttribute(writer, "loadAlways", actionOperationGetCustomer.isLoadAlways());
			} else if (ActionOperationType.IDENTIFY_CUSTOMER.equals(actionOperation.getOperationType())) {
				ActionOperationIdentifyCustomerParameters actionOperationIdentifyCustomer = (ActionOperationIdentifyCustomerParameters) actionOperation;
				writeJsonObjectAttribute(writer, "keyColumn", actionOperationIdentifyCustomer.getKeyColumn());
				writeJsonObjectAttribute(writer, "passColumn", actionOperationIdentifyCustomer.getPassColumn());
			} else if (ActionOperationType.SEND_MAILING.equals(actionOperation.getOperationType())) {
				ActionOperationSendMailingParameters actionOperationSendMailing = (ActionOperationSendMailingParameters) actionOperation;
				writeJsonObjectAttribute(writer, "mailing_id", actionOperationSendMailing.getMailingID());
				writeJsonObjectAttribute(writer, "delayMinutes", actionOperationSendMailing.getDelayMinutes());
				writeJsonObjectAttribute(writer, "bccAddress", actionOperationSendMailing.getBcc());
			} else if (ActionOperationType.SERVICE_MAIL.equals(actionOperation.getOperationType())) {
				ActionOperationServiceMailParameters actionOperationServiceMail = (ActionOperationServiceMailParameters) actionOperation;
				writeJsonObjectAttribute(writer, "mailtype", MailType.getFromInt(actionOperationServiceMail.getMailtype()).name());
				writeJsonObjectAttribute(writer, "toAddress", actionOperationServiceMail.getToAddress());
				writeJsonObjectAttribute(writer, "fromAddress", actionOperationServiceMail.getFromAddress());
				writeJsonObjectAttribute(writer, "replyAddress", actionOperationServiceMail.getReplyAddress());
				writeJsonObjectAttribute(writer, "subject", actionOperationServiceMail.getSubjectLine());
				writeJsonObjectAttribute(writer, "textMail", actionOperationServiceMail.getTextMail());
				writeJsonObjectAttribute(writer, "htmlMail", actionOperationServiceMail.getHtmlMail());
			} else if (ActionOperationType.SUBSCRIBE_CUSTOMER.equals(actionOperation.getOperationType())) {
				ActionOperationSubscribeCustomerParameters actionOperationSubscribeCustomer = (ActionOperationSubscribeCustomerParameters) actionOperation;
				writeJsonObjectAttribute(writer, "keyColumn", actionOperationSubscribeCustomer.getKeyColumn());
				writeJsonObjectAttribute(writer, "doubleCheck", actionOperationSubscribeCustomer.isDoubleCheck());
				writeJsonObjectAttribute(writer, "doubleOptIn", actionOperationSubscribeCustomer.isDoubleOptIn());
			} else if (ActionOperationType.UNSUBSCRIBE_CUSTOMER.equals(actionOperation.getOperationType())) {
				@SuppressWarnings("unused")
				ActionOperationUnsubscribeCustomerParameters actionOperationUnsubscribeCustomer = (ActionOperationUnsubscribeCustomerParameters) actionOperation;
				// ActionOperationUnsubscribeCustomer has no additional data
			} else if (ActionOperationType.UPDATE_CUSTOMER.equals(actionOperation.getOperationType())) {
				ActionOperationUpdateCustomerParameters actionOperationUpdateCustomer = (ActionOperationUpdateCustomerParameters) actionOperation;
				writeJsonObjectAttribute(writer, "columnName", actionOperationUpdateCustomer.getColumnName());
				writeJsonObjectAttribute(writer, "updateType", actionOperationUpdateCustomer.getUpdateType());
				writeJsonObjectAttribute(writer, "updateValue", actionOperationUpdateCustomer.getUpdateValue());
				writeJsonObjectAttribute(writer, "trackingPoint_id", actionOperationUpdateCustomer.getTrackingPointId());
				writeJsonObjectAttribute(writer, "useTrack", actionOperationUpdateCustomer.isUseTrack());
			} else {
				throw new Exception("Invalid actionoperation type: " + actionOperation.getOperationType());
			}
			
			writer.closeJsonObject();
		}
		writer.closeJsonArray();
		
		writer.closeJsonObject();
	}
	
	protected void writeJsonObjectAttribute(JsonWriter writer, String attributeName, Object attributeValue) throws Exception {
		writer.openJsonObjectProperty(attributeName);
		writer.addSimpleJsonObjectPropertyValue(attributeValue);
	}

	protected void writeJsonObjectAttributeWhenNotNullOrBlank(JsonWriter writer, String attributeName, String attributeValueString) throws Exception {
		if (StringUtils.isNotBlank(attributeValueString)) {
			writer.openJsonObjectProperty(attributeName);
			writer.addSimpleJsonObjectPropertyValue(attributeValueString);
		}
	}
	
	protected void exportProperties(JsonWriter writer, List<LinkProperty > linkProperties) throws Exception {
		if (CollectionUtils.isNotEmpty(linkProperties)) {
			writer.openJsonObjectProperty("properties");
			writer.openJsonArray();
			for (LinkProperty linkProperty : linkProperties) {
				writer.openJsonObject();
				
				writeJsonObjectAttribute(writer, "name", linkProperty.getPropertyName());
				writeJsonObjectAttribute(writer, "type", linkProperty.getPropertyType().name());
				writeJsonObjectAttribute(writer, "value", linkProperty.getPropertyValue());
				
				writer.closeJsonObject();
			}
			writer.closeJsonArray();
		}
	}
}
