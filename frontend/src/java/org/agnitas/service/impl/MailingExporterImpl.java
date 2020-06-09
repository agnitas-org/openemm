/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.beans.Mediatype;
import org.agnitas.beans.TrackableLink;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDaoException;
import org.agnitas.preview.TagSyntaxChecker;
import org.agnitas.service.MailingExporter;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.beans.ComCampaign;
import com.agnitas.beans.ComMailing;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.ComCampaignDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;
import com.agnitas.json.JsonWriter;

public class MailingExporterImpl extends ActionExporter implements MailingExporter {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailingExporterImpl.class);
	
	@Resource(name="CompanyDao")
	protected ComCompanyDao companyDao;
	
	@Resource(name="MailingDao")
	protected ComMailingDao mailingDao;
	
	@Resource(name="MailinglistDao")
	protected MailinglistDao mailinglistDao;
	
	@Resource(name="TargetDao")
	protected ComTargetDao targetDao;
	
	@Resource(name="MediatypesDao")
	protected MediatypesDao mediatypesDao;
	
	@Resource(name="CampaignDao")
	protected ComCampaignDao campaignDao;

	@Override
	public void exportMailingToJson(int companyID, int mailingID, OutputStream output, boolean exportUnusedImages) throws Exception {
		ComMailing mailing = (ComMailing) mailingDao.getMailing(mailingID, companyID);
		Set<Integer> targetIDs = new HashSet<>();
		Set<Integer> actionIDs = new HashSet<>();
		
		try (JsonWriter writer = new JsonWriter(output, "UTF-8")) {
			writer.openJsonObject();
			
			exportMailingData(companyID, mailing, targetIDs, actionIDs, writer, exportUnusedImages);
			
			writer.closeJsonObject();
		} catch (Exception e) {
			logger.error("Error in mailing export: " + e.getMessage(), e);
			throw e;
		}
	}

	protected void exportMailingData(int companyID, ComMailing mailing, Set<Integer> targetIDs, Set<Integer> actionIDs, JsonWriter writer, boolean exportUnusedImages) throws Exception, MediatypesDaoException, IOException {
		Set<String> usedImageComponentNames = new HashSet<>();
		
		writeJsonObjectAttribute(writer, "version", EXPORT_JSON_VERSION);
		
		writeJsonObjectAttribute(writer, "company_id", mailing.getCompanyID());
		
		writeJsonObjectAttribute(writer, "id", mailing.getId());
		
		writeJsonObjectAttributeWhenNotNullOrBlank(writer, "shortname", mailing.getShortname());
		
		if (StringUtils.isNotBlank(mailing.getDescription())) {
			writeJsonObjectAttributeWhenNotNullOrBlank(writer, "description", mailing.getDescription());
		}
		
		Mailinglist mailinglist = mailinglistDao.getMailinglist(mailing.getMailinglistID(), mailing.getCompanyID());
		writeJsonObjectAttribute(writer, "mailinglist_id", mailinglist.getId());
		writeJsonObjectAttributeWhenNotNullOrBlank(writer, "mailinglist_shortname", mailinglist.getShortname());
		writeJsonObjectAttributeWhenNotNullOrBlank(writer, "mailinglist_description", mailinglist.getDescription());
		writeJsonObjectAttribute(writer, "mailingtype", MailingType.fromCode(mailing.getMailingType()).name());
		
		if (StringUtils.isNotBlank(mailing.getTargetExpression())) {
			writeJsonObjectAttributeWhenNotNullOrBlank(writer, "target_expression", mailing.getTargetExpression());
			targetIDs.addAll(getInvolvedTargetIdsFromTargetExpression(mailing.getTargetExpression()));
		}
		
		if (mailing.isIsTemplate()) {
			writeJsonObjectAttribute(writer, "is_template", mailing.isIsTemplate());
		}
		
		if (mailing.getMailingContentType() != null) {
			writeJsonObjectAttribute(writer, "mailing_content_type", mailing.getMailingContentType().name());
		}
		
		if (mailing.getOpenActionID() > 0) {
			writeJsonObjectAttribute(writer, "open_action_id", mailing.getOpenActionID());
			actionIDs.add(mailing.getOpenActionID());
		}

		if (mailing.getClickActionID() > 0) {
			writeJsonObjectAttribute(writer, "click_action_id", mailing.getClickActionID());
			actionIDs.add(mailing.getClickActionID());
		}

		if (mailing.getCampaignID() > 0) {
			writeJsonObjectAttribute(writer, "campaign_id", mailing.getCampaignID());
			ComCampaign campaign = campaignDao.getCampaign(mailing.getCampaignID(), companyID);
			writeJsonObjectAttribute(writer, "campaign_name", campaign.getShortname());
			writeJsonObjectAttribute(writer, "campaign_description", campaign.getDescription());
		}
		
		writeJsonObjectAttribute(writer, "creation_date", mailing.getCreationDate());

		writer.openJsonObjectProperty("mediatypes");
		writer.openJsonArray();
		for (Mediatype mediatype : mediatypesDao.loadMediatypes(mailing.getId(), mailing.getCompanyID()).values()) {
			writer.openJsonObject();
			
			writeJsonObjectAttribute(writer, "type", mediatype.getMediaType().name());

			writeJsonObjectAttribute(writer, "priority", mediatype.getPriority());
			
			writeJsonObjectAttribute(writer, "status", MediaTypeStatus.getMediaTypeStatusTypeByCode(mediatype.getStatus()).name());
			
			if (mediatype.getMediaType() == MediaTypes.EMAIL) {
				MediatypeEmail mediatypeEmail = (MediatypeEmail) mediatype;
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "subject", mediatypeEmail.getSubject());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "from_address", mediatypeEmail.getFromEmail());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "from_fullname", mediatypeEmail.getFromFullname());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "reply_address", mediatypeEmail.getReplyEmail());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "reply_fullname", mediatypeEmail.getReplyFullname());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "charset", mediatypeEmail.getCharset());
				writeJsonObjectAttribute(writer, "mailformat", MailType.getFromInt(mediatypeEmail.getMailFormat()).name());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "followup_for", mediatypeEmail.getFollowupFor());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "followup_method", mediatypeEmail.getFollowUpMethod());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "onepixel", mediatypeEmail.getOnepixel());
				writeJsonObjectAttribute(writer, "linefeed", mediatypeEmail.getLinefeed());
			} else {
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "param", mediatype.getParam());
			}
			
			writer.closeJsonObject();
		}
		writer.closeJsonArray();
		
		if (mailing.getParameters() != null && mailing.getParameters().size() > 0) {
			writer.openJsonObjectProperty("parameters");
			writer.openJsonArray();
			for (ComMailingParameter mailingParameter : mailing.getParameters()) {
				writer.openJsonObject();
				
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "name", mailingParameter.getName());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "value", mailingParameter.getValue());
				if (StringUtils.isNotBlank(mailingParameter.getDescription())) {
					writeJsonObjectAttributeWhenNotNullOrBlank(writer, "description", mailingParameter.getDescription());
				}
				writeJsonObjectAttribute(writer, "creationDate", mailingParameter.getCreationDate());
				
				writer.closeJsonObject();
			}
			writer.closeJsonArray();
		}
		
		if (!exportUnusedImages) {
			// Scan for agnIMAGE-Tags
			if (mailing.getDynTags() != null && mailing.getDynTags().size() > 0) {
				for (DynamicTag dynamicTag : mailing.getDynTags().values()) {
					if (dynamicTag.getDynContent().size() > 0) {
						for (DynamicTagContent dynamicTagContent : dynamicTag.getDynContent().values()) {
							if (dynamicTagContent.getDynContent() != null) {
								usedImageComponentNames.addAll(TagSyntaxChecker.scanForAgnTagNameValues(dynamicTagContent.getDynContent(), "agnIMAGE", "agnIMGLINK"));
							}
						}
					}
				}
			}
			if (mailing.getComponents() != null && mailing.getComponents().size() > 0) {
				for (MailingComponent component : mailing.getComponents().values()) {
					if (component.getEmmBlock() != null) {
						usedImageComponentNames.addAll(TagSyntaxChecker.scanForAgnTagNameValues(component.getEmmBlock(), "agnIMAGE", "agnIMGLINK"));
					}
				}
			}
		}

		if (mailing.getComponents() != null && mailing.getComponents().size() > 0) {
			writer.openJsonObjectProperty("components");
			writer.openJsonArray();
			for (MailingComponent component : mailing.getComponents().values()) {
				MailingComponentType mailingComponentType = MailingComponentType.getMailingComponentTypeByCode(component.getType());
				if ((mailingComponentType != MailingComponentType.Image && mailingComponentType != MailingComponentType.HostedImage) || exportUnusedImages || usedImageComponentNames.contains(component.getComponentName())) {
					writer.openJsonObject();
					
					writeJsonObjectAttribute(writer, "id", component.getId());
					writeJsonObjectAttributeWhenNotNullOrBlank(writer, "name", component.getComponentName());
					if (StringUtils.isNotBlank(component.getDescription())) {
						writeJsonObjectAttributeWhenNotNullOrBlank(writer, "description", component.getDescription());
					}
					writeJsonObjectAttribute(writer, "type", mailingComponentType.name());
					if (component.getTargetID() > 0) {
						writeJsonObjectAttribute(writer, "target_id", component.getTargetID());
						targetIDs.add(component.getTargetID());
					}
					if (component.getUrlID() > 0) {
						writeJsonObjectAttribute(writer, "url_id", component.getUrlID());
						writeJsonObjectAttributeWhenNotNullOrBlank(writer, "url", component.getLink());
					}
					
					if (component.getEmmBlock() != null && component.getEmmBlock().length() > 0) {
						writeJsonObjectAttributeWhenNotNullOrBlank(writer, "emm_block", component.getEmmBlock().replace("\r\n", "\n").replace("\r", "\n"));
						writeJsonObjectAttributeWhenNotNullOrBlank(writer, "mimetype", component.getMimeType());
					} else if (component.getBinaryBlock() != null && component.getBinaryBlock().length > 0) {
						// Do not store empty 1-byte arrays (they are created by clone-mailing-method for no purpose)
						if (!(component.getBinaryBlock().length == 1 && component.getBinaryBlock()[0] == 0)) {
							writeJsonObjectAttributeWhenNotNullOrBlank(writer, "mimetype", component.getMimeType());
							writeJsonObjectAttributeWhenNotNullOrBlank(writer, "bin_block", AgnUtils.encodeZippedBase64(component.getBinaryBlock()));
						}
					}
					
					writer.closeJsonObject();
				}
			}
			writer.closeJsonArray();
		}

		if (mailing.getDynTags() != null && mailing.getDynTags().size() > 0) {
			writer.openJsonObjectProperty("contents");
			writer.openJsonArray();
			for (DynamicTag dynamicTag : mailing.getDynTags().values()) {
				writer.openJsonObject();
				
				writeJsonObjectAttribute(writer, "id", dynamicTag.getId());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "name", dynamicTag.getDynName());
				writeJsonObjectAttribute(writer, "disableLinkExtension", dynamicTag.isDisableLinkExtension());
				
				if (dynamicTag.getDynContent().size() > 0) {
					writer.openJsonObjectProperty("content");
					writer.openJsonArray();
					for (DynamicTagContent dynamicTagContent : dynamicTag.getDynContent().values()) {
						writer.openJsonObject();
						
						writeJsonObjectAttribute(writer, "id", dynamicTagContent.getId());
						if (dynamicTagContent.getTargetID() > 0) {
							writeJsonObjectAttribute(writer, "target_id", dynamicTagContent.getTargetID());
							targetIDs.add(dynamicTagContent.getTargetID());
						}
						writeJsonObjectAttribute(writer, "order", dynamicTagContent.getDynOrder());
						writeJsonObjectAttribute(writer, "text", dynamicTagContent.getDynContent());
						
						writer.closeJsonObject();
					}
					writer.closeJsonArray();
				}
				
				writer.closeJsonObject();
			}
			writer.closeJsonArray();
		}

		if (mailing.getTrackableLinks() != null && mailing.getTrackableLinks().size() > 0) {
			writer.openJsonObjectProperty("links");
			writer.openJsonArray();
			for (TrackableLink trackableLink : mailing.getTrackableLinks().values()) {
				writer.openJsonObject();
				
				writeJsonObjectAttribute(writer, "id", trackableLink.getId());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "name", trackableLink.getShortname());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "url", trackableLink.getFullUrl());

				if (trackableLink.getActionID() > 0) {
					writeJsonObjectAttribute(writer, "action_id", trackableLink.getActionID());
					actionIDs.add(trackableLink.getActionID());
				}
				
				if (trackableLink.getDeepTracking() > 0) {
					writeJsonObjectAttribute(writer, "deep_tracking", trackableLink.getDeepTracking());
				}
				
				if (trackableLink.getRelevance() > 0) {
					writeJsonObjectAttribute(writer, "relevance", trackableLink.getRelevance());
				}
				
				if (trackableLink.getUsage() > 0) {
					writeJsonObjectAttribute(writer, "usage", trackableLink.getUsage());
				}
				
				List<LinkProperty> linkProperties = ((ComTrackableLink) trackableLink).getProperties();
				exportProperties(writer, linkProperties);
				
				writer.closeJsonObject();
			}
			writer.closeJsonArray();
		}
		
		if (targetIDs.size() > 0) {
			writer.openJsonObjectProperty("targets");
			writer.openJsonArray();
			for (int targetID : targetIDs) {
				ComTarget target = targetDao.getTarget(targetID, mailing.getCompanyID());
				writer.openJsonObject();
				
				writeJsonObjectAttribute(writer, "id", target.getId());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "name", target.getTargetName());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "description", target.getTargetDescription());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "sql", target.getTargetSQL());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "eql", target.getEQL());
				
				writer.closeJsonObject();
			}
			writer.closeJsonArray();
		}
		
		exportActions(writer, companyID, actionIDs);
	}
	
	protected List<Integer> getInvolvedTargetIdsFromTargetExpression(String targetExpression) {
		List<Integer> returnList = new ArrayList<>();
    	if (StringUtils.isNotBlank(targetExpression)) {
			// the regex will find all numbers, spaces and symbols "(", ")", "&", "|", "!"
			String targetExpressionRegex = "[&|()! ]|[\\d]+";
	
			// iterate through the tokens of target expression
			Pattern pattern = Pattern.compile(targetExpressionRegex);
			Matcher matcher = pattern.matcher(targetExpression);
			while (matcher.find()) {
				String token = matcher.group();
				if (StringUtils.isNumeric(token)) {
					int targetID = Integer.parseInt(token);
					if (targetID > 0) {
						returnList.add(targetID);
					}
				}
			}
    	}
		return returnList;
	}
}
