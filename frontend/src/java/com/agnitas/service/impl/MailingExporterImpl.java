/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agnitas.beans.Campaign;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.Target;
import com.agnitas.dao.CampaignDao;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.TargetDao;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.json.JsonWriter;
import jakarta.annotation.Resource;
import com.agnitas.beans.AbstractTrackableLink;
import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.MediaTypeStatus;
import com.agnitas.emm.core.mailinglist.dao.MailinglistDao;
import com.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import com.agnitas.preview.TagSyntaxChecker;
import com.agnitas.service.MailingExporter;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.importvalues.MailType;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailingExporterImpl extends ActionExporter implements MailingExporter {

	private static final Logger logger = LogManager.getLogger(MailingExporterImpl.class);
	
	@Resource(name="CompanyDao")
	protected CompanyDao companyDao;
	
	@Resource(name="MailingDao")
	protected MailingDao mailingDao;
	
	@Resource(name="MailinglistDao")
	protected MailinglistDao mailinglistDao;
	
	@Resource(name="TargetDao")
	protected TargetDao targetDao;
	
	@Resource(name="MediatypesDao")
	protected MediatypesDao mediatypesDao;
	
	@Resource(name="CampaignDao")
	protected CampaignDao campaignDao;
	
	@Override
	public void exportMailingToJson(int companyID, int mailingID, OutputStream output, boolean exportUnusedImages, boolean isForCopying) throws Exception {
		Mailing mailing = mailingDao.getMailing(mailingID, companyID);

        // mailingDao.getMailing returns an empty mailing object if the mailingid does not exist yet
		if (mailing == null || mailing.getId() == 0) {
			throw new Exception("Mailing for export cannot be found. CompanyID: " + companyID + ", MailingID: " + mailingID);
		}
		
		Set<Integer> targetIDs = new HashSet<>();
		Set<Integer> actionIDs = new HashSet<>();
		
		try (JsonWriter writer = new JsonWriter(output, "UTF-8")) {
			writer.openJsonObject();

			if (isForCopying) {
				writeJsonObjectAttribute(writer, "forCopying", true);
			}
			
			exportMailingData(companyID, mailing, targetIDs, actionIDs, writer, exportUnusedImages);
			
			writer.closeJsonObject();
		} catch (Exception e) {
			logger.error("Error in mailing export: " + e.getMessage(), e);
			throw e;
		}
	}
	
	protected void exportMailingData(int companyID, Mailing mailing, Set<Integer> targetIDs, Set<Integer> actionIDs, JsonWriter writer, boolean exportUnusedImages) throws Exception {
		if (mailing == null) {
			throw new IllegalArgumentException("Mailing for export is not defined");
		}
		
		Optional<String> companyTokenOptional = companyDao.getCompanyToken(companyID);
		String companyToken = companyTokenOptional.isPresent() ? companyTokenOptional.get() : null;
		
		Set<String> usedImageComponentNames = new HashSet<>();
		
		writeJsonObjectAttribute(writer, "version", EXPORT_JSON_VERSION);
		
		writeJsonObjectAttribute(writer, "company_id", mailing.getCompanyID());
		
		writeJsonObjectAttribute(writer, "id", mailing.getId());
		
		writeJsonObjectAttributeWhenNotNullOrBlank(writer, "shortname", mailing.getShortname());
		
		if (StringUtils.isNotBlank(mailing.getDescription())) {
			writeJsonObjectAttributeWhenNotNullOrBlank(writer, "description", mailing.getDescription());
		}
		
		Mailinglist mailinglist = mailinglistDao.getMailinglist(mailing.getMailinglistID(), mailing.getCompanyID());
		
		if (mailinglist == null) {
			throw new IllegalStateException("Mailinglist for mailing export cannot be found. CompanyID: " + mailing.getCompanyID() + ", MailingID: " + mailing.getId() + ", MailinglistID: " + mailing.getMailinglistID());
		}

		writeJsonObjectAttribute(writer, "mailinglist_id", mailinglist.getId());
		writeJsonObjectAttributeWhenNotNullOrBlank(writer, "mailinglist_shortname", mailinglist.getShortname());
		writeJsonObjectAttributeWhenNotNullOrBlank(writer, "mailinglist_description", mailinglist.getDescription());
		writeJsonObjectAttribute(writer, "mailingtype", mailing.getMailingType().name());
		
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
			Campaign campaign = campaignDao.getCampaign(mailing.getCampaignID(), companyID);
			if (campaign == null) {
				throw new IllegalStateException(String.format("Found non existing campaign_id %d defined for mailing %d", mailing.getCampaignID(), mailing.getId()));
			}
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
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "preHeader", mediatypeEmail.getPreHeader());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "from_address", mediatypeEmail.getFromEmail());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "from_fullname", mediatypeEmail.getFromFullname());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "reply_address", mediatypeEmail.getReplyEmail());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "reply_fullname", mediatypeEmail.getReplyFullname());
				writeJsonObjectAttributeWhenNotNullOrBlank(writer, "envelope", mediatypeEmail.getEnvelopeEmail());
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
			for (MailingParameter mailingParameter : mailing.getParameters()) {
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
				MailingComponentType mailingComponentType = component.getType();
				if ((mailingComponentType != MailingComponentType.Image && mailingComponentType != MailingComponentType.HostedImage) || exportUnusedImages || usedImageComponentNames.contains(component.getComponentName())) {
					writer.openJsonObject();
					
					writeJsonObjectAttribute(writer, "id", component.getId());
					writeJsonObjectAttributeWhenNotNullOrBlank(writer, "name", component.getComponentName());
					if (StringUtils.isNotBlank(component.getDescription())) {
						writeJsonObjectAttributeWhenNotNullOrBlank(writer, "description", component.getDescription());
					}
					writeJsonObjectAttribute(writer, "type", mailingComponentType.getCode());
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
						
						String dynContentText = dynamicTagContent.getDynContent();
						if (StringUtils.isNotBlank(companyToken) && dynContentText.contains("agnCTOKEN=" + companyToken)) {
							dynContentText = dynContentText.replace("agnCTOKEN=" + companyToken, "agnCTOKEN=[CTOKEN]");
						}
						if (dynContentText.contains("agnCI=" + companyID)) {
							dynContentText = dynContentText.replace("agnCI=" + companyID, "agnCI=[COMPANY_ID]");
						}
						writeJsonObjectAttribute(writer, "text", dynContentText);
						
						writer.closeJsonObject();
					}
					writer.closeJsonArray();
				}
				
				writer.closeJsonObject();
			}
			writer.closeJsonArray();
		}

		if (mailing.getTrackableLinks() != null && !mailing.getTrackableLinks().isEmpty()) {
			exportLinks(writer, mailing.getTrackableLinks().values(), companyToken, companyID, actionIDs);
		}

        exportTargets(writer, companyID, targetIDs);
        exportActions(writer, companyID, actionIDs);
	}

	protected void exportLinks(JsonWriter writer, Collection<? extends AbstractTrackableLink> trackableLinks, String companyToken, int companyID, Set<Integer> actionIDs) throws Exception {
		writer.openJsonObjectProperty("links");
		writer.openJsonArray();
		for (AbstractTrackableLink trackableLink : trackableLinks) {
			writer.openJsonObject();

			writeJsonObjectAttribute(writer, "id", trackableLink.getId());
			writeJsonObjectAttributeWhenNotNullOrBlank(writer, "name", trackableLink.getShortname());

			String linkUrl = trackableLink.getFullUrl();
			if (StringUtils.isNotBlank(companyToken) && linkUrl.contains("agnCTOKEN=" + companyToken)) {
				linkUrl = linkUrl.replace("agnCTOKEN=" + companyToken, "agnCTOKEN=[CTOKEN]");
			}
			if (linkUrl.contains("agnCI=" + companyID)) {
				linkUrl = linkUrl.replace("agnCI=" + companyID, "agnCI=[COMPANY_ID]");
			}
			writeJsonObjectAttributeWhenNotNullOrBlank(writer, "url", linkUrl);

			if (trackableLink.getActionID() > 0) {
				writeJsonObjectAttribute(writer, "action_id", trackableLink.getActionID());
				actionIDs.add(trackableLink.getActionID());
			}

			if (trackableLink.getDeepTracking() > 0) {
				writeJsonObjectAttribute(writer, "deep_tracking", trackableLink.getDeepTracking());
			}

			if (trackableLink.getUsage() > 0) {
				writeJsonObjectAttribute(writer, "usage", trackableLink.getUsage());
			}

			if (trackableLink.isAdminLink()) {
				writeJsonObjectAttribute(writer, "administrative", trackableLink.isAdminLink());
			}

			if (trackableLink.isStaticValue()) {
				writeJsonObjectAttribute(writer, "static", trackableLink.isStaticValue());
			}

			if (trackableLink.isCreateSubstituteLinkForAgnDynMulti()) {
				writeJsonObjectAttribute(writer, "create_substitute_link", trackableLink.isCreateSubstituteLinkForAgnDynMulti());
			}

			if (trackableLink.isMeasureSeparately()) {
				writeJsonObjectAttribute(writer, "measureSeparately", trackableLink.isMeasureSeparately());
			}

			List<LinkProperty> linkProperties = trackableLink.getProperties();
			exportProperties(writer, linkProperties);

			writer.closeJsonObject();
		}
		writer.closeJsonArray();
	}

    protected void exportTargets(JsonWriter writer, int companyId, Set<Integer> targetIds) throws Exception {
        if (targetIds.isEmpty()) {
            return;
        }
        writer.openJsonObjectProperty("targets");
        writer.openJsonArray();

        List<Target> targets = targetIds.stream()
                .map(targetId -> targetDao.getTarget(targetId, companyId))
                .filter(Objects::nonNull)
				.toList();

        for (Target target : targets) {
            writeTarget(writer, target);
        }
        writer.closeJsonArray();
    }

    private void writeTarget(JsonWriter writer, Target target) throws Exception {
        writer.openJsonObject();
        writeJsonObjectAttribute(writer, "id", target.getId());
        writeJsonObjectAttributeWhenNotNullOrBlank(writer, "name", target.getTargetName());
        writeJsonObjectAttributeWhenNotNullOrBlank(writer, "description", target.getTargetDescription());
        writeJsonObjectAttributeWhenNotNullOrBlank(writer, "sql", target.getTargetSQL());
        writeJsonObjectAttributeWhenNotNullOrBlank(writer, "eql", target.getEQL());
        if (target.isAccessLimitation()) {
            writeJsonObjectAttribute(writer, "access_limiting", target.isAccessLimitation());
        }
        writer.closeJsonObject();
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
