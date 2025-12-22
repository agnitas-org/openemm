/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.List;
import java.util.Set;

import jakarta.annotation.Resource;

import com.agnitas.emm.core.action.bean.EmmAction;
import com.agnitas.dao.EmmActionDao;
import com.agnitas.emm.core.action.dao.EmmActionOperationDao;
import com.agnitas.emm.core.action.bean.EmmActionType;
import com.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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
import com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters;
import com.agnitas.json.JsonWriter;

public class ActionExporter extends BaseImporterExporter {
	@Resource(name="EmmActionDao")
	private EmmActionDao actionDao;
	
	@Resource(name="EmmActionOperationDao")
	private EmmActionOperationDao actionOperationDao;
	
	protected void exportActions(JsonWriter writer, int companyID, Set<Integer> actionIDs) {
		if (actionIDs.size() > 0) {
			writer.openJsonObjectProperty("actions");
			writer.openJsonArray();
			for (int actionID : actionIDs) {
				exportAction(companyID, actionID, writer);
			}
			writer.closeJsonArray();
		}
	}
	
	public void exportAction(int companyID, int actionID, JsonWriter writer) {
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

			switch (actionOperation.getOperationType()) {
				case ACTIVATE_DOUBLE_OPT_IN:
					ActionOperationActivateDoubleOptInParameters actionOperationActivateDoubleOptIn = (ActionOperationActivateDoubleOptInParameters) actionOperation;
					writeJsonObjectAttribute(writer, "forAllLists", actionOperationActivateDoubleOptIn.isForAllLists());
					break;
				case CONTENT_VIEW:
					ActionOperationContentViewParameters actionOperationContentView = (ActionOperationContentViewParameters) actionOperation;
					writeJsonObjectAttribute(writer, "tagName", actionOperationContentView.getTagName());
					break;
				case EXECUTE_SCRIPT:
					ActionOperationExecuteScriptParameters actionOperationExecuteScript = (ActionOperationExecuteScriptParameters) actionOperation;
					writeJsonObjectAttribute(writer, "script", actionOperationExecuteScript.getScript());
					break;
				case GET_ARCHIVE_LIST:
					ActionOperationGetArchiveListParameters actionOperationGetArchiveList = (ActionOperationGetArchiveListParameters) actionOperation;
					writeJsonObjectAttribute(writer, "campaign_id", actionOperationGetArchiveList.getCampaignID());
					writeJsonObjectAttribute(writer, "limit_type", actionOperationGetArchiveList.getLimitType());
					writeJsonObjectAttribute(writer, "limit_value", actionOperationGetArchiveList.getLimitValue());
					break;
				case GET_ARCHIVE_MAILING:
					ActionOperationGetArchiveMailingParameters actionOperationGetArchiveMailing = (ActionOperationGetArchiveMailingParameters) actionOperation;
					writeJsonObjectAttribute(writer, "expiration", actionOperationGetArchiveMailing.getExpireYear() + "-" + actionOperationGetArchiveMailing.getExpireMonth() + "-" + actionOperationGetArchiveMailing.getExpireDay());
					break;
				case GET_CUSTOMER:
					ActionOperationGetCustomerParameters actionOperationGetCustomer = (ActionOperationGetCustomerParameters) actionOperation;
					writeJsonObjectAttribute(writer, "loadAlways", actionOperationGetCustomer.isLoadAlways());
					break;
				case IDENTIFY_CUSTOMER:
					ActionOperationIdentifyCustomerParameters actionOperationIdentifyCustomer = (ActionOperationIdentifyCustomerParameters) actionOperation;
					writeJsonObjectAttribute(writer, "keyColumn", actionOperationIdentifyCustomer.getKeyColumn());
					writeJsonObjectAttribute(writer, "passColumn", actionOperationIdentifyCustomer.getPassColumn());
					break;
				case SEND_MAILING:
					ActionOperationSendMailingParameters actionOperationSendMailing = (ActionOperationSendMailingParameters) actionOperation;
					writeJsonObjectAttribute(writer, "mailing_id", actionOperationSendMailing.getMailingID());
					writeJsonObjectAttribute(writer, "delayMinutes", actionOperationSendMailing.getDelayMinutes());
					writeJsonObjectAttribute(writer, "bccAddress", actionOperationSendMailing.getBcc());
					break;
				case SERVICE_MAIL:
					ActionOperationServiceMailParameters actionOperationServiceMail = (ActionOperationServiceMailParameters) actionOperation;
					writeJsonObjectAttribute(writer, "mailtype", MailType.getFromInt(actionOperationServiceMail.getMailtype()).name());
					writeJsonObjectAttribute(writer, "toAddress", actionOperationServiceMail.getToAddress());
					writeJsonObjectAttribute(writer, "fromAddress", actionOperationServiceMail.getFromAddress());
					writeJsonObjectAttribute(writer, "replyAddress", actionOperationServiceMail.getReplyAddress());
					writeJsonObjectAttribute(writer, "subject", actionOperationServiceMail.getSubjectLine());
					writeJsonObjectAttribute(writer, "textMail", actionOperationServiceMail.getTextMail());
					writeJsonObjectAttribute(writer, "htmlMail", actionOperationServiceMail.getHtmlMail());
					break;
				case SUBSCRIBE_CUSTOMER:
					ActionOperationSubscribeCustomerParameters actionOperationSubscribeCustomer = (ActionOperationSubscribeCustomerParameters) actionOperation;
					writeJsonObjectAttribute(writer, "keyColumn", actionOperationSubscribeCustomer.getKeyColumn());
					writeJsonObjectAttribute(writer, "doubleCheck", actionOperationSubscribeCustomer.isDoubleCheck());
					writeJsonObjectAttribute(writer, "doubleOptIn", actionOperationSubscribeCustomer.isDoubleOptIn());
					break;
				case UNSUBSCRIBE_CUSTOMER:
					// ActionOperationUnsubscribeCustomer has no additional data
					break;
				case UPDATE_CUSTOMER:
					ActionOperationUpdateCustomerParameters actionOperationUpdateCustomer = (ActionOperationUpdateCustomerParameters) actionOperation;
					writeJsonObjectAttribute(writer, "columnName", actionOperationUpdateCustomer.getColumnName());
					writeJsonObjectAttribute(writer, "updateType", actionOperationUpdateCustomer.getUpdateType());
					writeJsonObjectAttribute(writer, "updateValue", actionOperationUpdateCustomer.getUpdateValue());
					writeJsonObjectAttribute(writer, "trackingPoint_id", actionOperationUpdateCustomer.getTrackingPointId());
					writeJsonObjectAttribute(writer, "useTrack", actionOperationUpdateCustomer.isUseTrack());
					break;
				default:
					throw new UnsupportedOperationException("Invalid action operation type: " + actionOperation.getOperationType());
			}

			writer.closeJsonObject();
		}
		writer.closeJsonArray();
		
		writer.closeJsonObject();
	}
	
	protected void writeJsonObjectAttribute(JsonWriter writer, String attributeName, Object attributeValue) {
		writer.openJsonObjectProperty(attributeName);
		writer.addSimpleJsonObjectPropertyValue(attributeValue);
	}

	protected void writeJsonObjectAttributeWhenNotNullOrBlank(JsonWriter writer, String attributeName, String attributeValueString) {
		if (StringUtils.isNotBlank(attributeValueString)) {
			writer.openJsonObjectProperty(attributeName);
			writer.addSimpleJsonObjectPropertyValue(attributeValueString);
		}
	}
	
	protected void exportProperties(JsonWriter writer, List<LinkProperty > linkProperties) {
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

	public void setActionDao(EmmActionDao actionDao) {
		this.actionDao = actionDao;
	}

	public void setActionOperationDao(EmmActionOperationDao actionOperationDao) {
		this.actionOperationDao = actionOperationDao;
	}
}
