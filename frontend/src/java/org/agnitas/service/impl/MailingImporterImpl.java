/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.beans.impl.DynamicTagContentImpl;
import org.agnitas.beans.impl.MailingComponentImpl;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import org.agnitas.service.ImportResult;
import org.agnitas.service.MailingImporter;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.agnitas.beans.Campaign;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingContentType;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.impl.ComTrackableLinkImpl;
import com.agnitas.beans.impl.DynamicTagImpl;
import com.agnitas.beans.impl.MailingImpl;
import com.agnitas.beans.impl.MediatypeEmailImpl;
import com.agnitas.dao.CampaignDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.json.JsonArray;
import com.agnitas.json.JsonNode;
import com.agnitas.json.JsonObject;
import com.agnitas.json.JsonReader;

import jakarta.annotation.Resource;

public class MailingImporterImpl extends ActionImporter implements MailingImporter, ApplicationContextAware {
    
	private static final Logger logger = LogManager.getLogger(MailingImporterImpl.class);
	
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
	protected CampaignDao campaignDao;
	
	@Resource(name="MailingImporterMediatypeFactory")
	protected MailingImporterMediatypeFactory mediatypeFactory;
	
	private ApplicationContext applicationContext;

    /**
     * Import simple mailing or grid mailing or templates
     */
	@Override
	public ImportResult importMailingFromJson(int companyID, InputStream input, boolean importAsTemplate, boolean overwriteTemplate, boolean isGrid) throws Exception {
		return importMailingFromJson(companyID, input, importAsTemplate, null, null, overwriteTemplate, isGrid, null);
	}
    
	@Override
	public ImportResult importMailingFromJson(int companyID, InputStream input, boolean importAsTemplate, boolean overwriteTemplate, boolean isGrid, Set<Integer> adminAltgIds) throws Exception {
		return importMailingFromJson(companyID, input, importAsTemplate, null, null, overwriteTemplate, isGrid, adminAltgIds);
	}

    @Override
    public ImportResult importMailingFromJson(int companyID, InputStream input, boolean importAsTemplate, String shortName, String description, boolean overwriteTemplate, boolean isGrid) throws Exception {
        return importMailingFromJson(companyID, input, importAsTemplate, shortName, description, overwriteTemplate, isGrid, null);
    }

    /**
     * Import simple mailing or template
     */
	@Override
	public ImportResult importMailingFromJson(int companyID, InputStream input, boolean importAsTemplate, String shortName, String description, boolean overwriteTemplate, boolean isGrid, Set<Integer> adminAltgIds) throws Exception {
		return importMailingFromJson(companyID, input, importAsTemplate, shortName, description, overwriteTemplate, true, isGrid, adminAltgIds);
	}
	
	@Override
	public ImportResult importMailingFromJson(int companyID, InputStream input, boolean importAsTemplate, String shortName, String description, boolean overwriteTemplate, boolean checkIsTemplate, boolean isGrid) throws Exception {
        return importMailingFromJson(companyID, input, importAsTemplate, shortName, description, overwriteTemplate, checkIsTemplate, isGrid, null);
    }

    @Override
	public ImportResult importMailingFromJson(int companyID, InputStream input, boolean importAsTemplate, String shortName, String description, boolean overwriteTemplate, boolean checkIsTemplate, boolean isGrid, Set<Integer> adminAltgIds) throws Exception {
		Map<String, Object[]> warnings = new HashMap<>();
		try (JsonReader reader = new JsonReader(input, "UTF-8")) {
			JsonNode jsonNode = reader.read();
			
			checkIsJsonObject(jsonNode);

			JsonObject jsonObject = (JsonObject) jsonNode.getValue();

			String version = (String) jsonObject.get("version");
			checkJsonVersion(version);

			Map<Integer, Integer> actionIdMappings = new HashMap<>();
			if (jsonObject.containsPropertyKey("actions")) {
				for (Object actionObject : (JsonArray) jsonObject.get("actions")) {
					int importedActionId = importAction(companyID, (JsonObject) actionObject);
					if (((JsonObject) actionObject).get("id") != null && actionIdMappings != null) {
            			actionIdMappings.put((Integer) ((JsonObject) actionObject).get("id"), importedActionId);
            		}
				}
			}
			
			if (!jsonObject.containsPropertyKey("mailingtype")) {
				logger.error("Data does not contain mailing data (This may be some mailing template data)");
				return ImportResult.builder().setSuccess(false).addError("error.mailing.import").build();
			}
			
			boolean isTemplate = importAsTemplate;
			if (checkIsTemplate) {
				isTemplate = jsonObject.get("is_template") != null && (Boolean) jsonObject.get("is_template");
			}
			
			Map<Integer, Integer> targetIdMappings = importTargets(companyID, jsonObject, warnings);

			Mailing mailing = new MailingImpl();
			int importedMailingID = importMailingData(mailing, companyID, isTemplate, shortName, description, warnings, jsonObject, actionIdMappings, targetIdMappings, adminAltgIds);
			if (importedMailingID == 0 && needsAltg(adminAltgIds, targetIdMappings.values())) {
                warnings.put("warning.mailing.altg", null);
                return ImportResult.builder().setSuccess(true)
     						.setMailingID(0)
                            .setImportedMailing(mailing)
     						.setIsTemplate(importAsTemplate)
     						.addWarnings(warnings).build();
            }
			if (importedMailingID <= 0) {
				logger.error("Cannot save mailing");
				return ImportResult.builder().addError("error.mailing.import").build();
			} else {
				// Load and save the new mailing to let any adjustments happen that may be needed
				Mailing storedMailing = mailingDao.getMailing(mailing.getId(), mailing.getCompanyID());
				storedMailing.buildDependencies(true, getApplicationContext());
				mailingDao.saveMailing(storedMailing, false);
				
				return ImportResult.builder().setSuccess(true)
						.setMailingID(importedMailingID)
						.setIsTemplate(importAsTemplate)
						.addWarnings(warnings).build();
			}
		} catch (Exception e) {
			logger.error("Error in mailing import: " + e.getMessage(), e);
			throw e;
		}
	}

    private boolean needsAltg(Set<Integer> adminAltgIds, Collection<Integer> targetIds) {
        return CollectionUtils.isNotEmpty(adminAltgIds) && CollectionUtils.intersection(targetIds, adminAltgIds).isEmpty();
    }

    protected int importMailingData(Mailing mailing, int companyID, boolean importAsTemplate, String shortName, String description, Map<String, Object[]> warnings, JsonObject jsonObject, Map<Integer, Integer> actionIdMappings, Map<Integer, Integer> targetIdMappings, Set<Integer> adminAltgIds) throws Exception {
		String rdirDomain = companyDao.getRedirectDomain(companyID);

		Optional<String> companyTokenOptional = companyDao.getCompanyToken(companyID);
		String companyToken = companyTokenOptional.isPresent() ? companyTokenOptional.get() : null;
		
		mailing.setCompanyID(companyID);
		
		mailing.setShortname(StringUtils.defaultString(shortName, (String) jsonObject.get("shortname")));
		mailing.setDescription(StringUtils.defaultString(description, (String) jsonObject.get("description")));
		
		mailing.setIsTemplate(importAsTemplate);

		if (jsonObject.containsPropertyKey("mailing_content_type")) {
			mailing.setMailingContentType(MailingContentType.getFromString((String) jsonObject.get("mailing_content_type")));
		}

		// Use the existing mailinglist with same shortname or the mailinglist with the lowest id (= default mailinglist)
		if (jsonObject.containsPropertyKey("mailinglist_shortname")) {
			String mailinglistShortname = (String) jsonObject.get("mailinglist_shortname");
			int mailinglistID = Integer.MAX_VALUE;
			for (Mailinglist mailinglist : mailinglistDao.getMailinglists(companyID)) {
				if (StringUtils.equals(mailinglist.getShortname(), mailinglistShortname)) {
					mailinglistID = mailinglist.getId();
					break;
				} else if (mailinglist.getId() < mailinglistID) {
					mailinglistID = mailinglist.getId();
				}
			}
			mailing.setMailinglistID(mailinglistID);
		} else {
			// Use lowest available mailinglistid, if there is no mailinglist shortname in JSON data
			int mailinglistID = Integer.MAX_VALUE;
			for (Mailinglist mailinglist : mailinglistDao.getMailinglists(companyID)) {
				if (mailinglist.getId() < mailinglistID) {
					mailinglistID = mailinglist.getId();
				}
			}
			mailing.setMailinglistID(mailinglistID);
		}

		mailing.setMailingType(MailingType.fromName((String) jsonObject.get("mailingtype")));

		if (jsonObject.containsPropertyKey("target_expression")) {
			mailing.setTargetExpression(replaceOldTargetIds((String) jsonObject.get("target_expression"), targetIdMappings));
		}

		if (jsonObject.get("is_template") != null) {
			mailing.setIsTemplate((Boolean) jsonObject.get("is_template"));
		}

		if (jsonObject.containsPropertyKey("open_action_id")) {
			mailing.setOpenActionID(actionIdMappings.get(jsonObject.get("open_action_id")));
			warnings.put("warning.mailing.import.action", null);
		}

		if (jsonObject.containsPropertyKey("click_action_id")) {
			mailing.setClickActionID(actionIdMappings.get(jsonObject.get("click_action_id")));
		}

		if (jsonObject.containsPropertyKey("campaign_id")) {
			int campaignID = (Integer) jsonObject.get("campaign_id");
			String campaignName = (String) jsonObject.get("campaign_name");
			Campaign campaign = campaignDao.getCampaign(campaignID, companyID);
			if (campaign != null && StringUtils.equals(campaignName, campaign.getShortname())) {
				mailing.setCampaignID(campaignID);
			} else {
				warnings.put("warning.mailing.import.archive", null);
			}
		}

		readMediatypes(mailing, jsonObject);

		if (jsonObject.containsPropertyKey("parameters")) {
			List<ComMailingParameter> parameters = new ArrayList<>();
			for (Object parameterObject : (JsonArray) jsonObject.get("parameters")) {
				JsonObject parameterJsonObject = (JsonObject) parameterObject;
				ComMailingParameter mailingParameter = new ComMailingParameter();
				mailingParameter.setName((String) parameterJsonObject.get("name"));
				mailingParameter.setValue((String) parameterJsonObject.get("value"));
				mailingParameter.setDescription((String) parameterJsonObject.get("description"));
				parameters.add(mailingParameter);
			}
			mailing.setParameters(parameters);
		}

		if (jsonObject.containsPropertyKey("components")) {
			Map<String, MailingComponent> components = new HashMap<>();
			for (Object componentObject : (JsonArray) jsonObject.get("components")) {
				JsonObject componentJsonObject = (JsonObject) componentObject;
				MailingComponent mailingComponent = new MailingComponentImpl();
				mailingComponent.setComponentName((String) componentJsonObject.get("name"));
				mailingComponent.setDescription((String) componentJsonObject.get("description"));
				if (!componentJsonObject.containsPropertyKey("type")) {
					// Default value is Template
					mailingComponent.setType(MailingComponentType.Template);
				} else if (componentJsonObject.get("type") instanceof String) {
					mailingComponent.setType(MailingComponentType.getMailingComponentTypeByName((String) componentJsonObject.get("type")));
				} else {
					mailingComponent.setType(MailingComponentType.getMailingComponentTypeByCode((Integer) componentJsonObject.get("type")));
				}
				if (componentJsonObject.containsPropertyKey("emm_block")) {
					String emmBlock = (String) componentJsonObject.get("emm_block");
					emmBlock = emmBlock.replace("[COMPANY_ID]", Integer.toString(companyID)).replace("[RDIR_DOMAIN]", rdirDomain);
					if (StringUtils.isNotBlank(companyToken)) {
						emmBlock = emmBlock.replace("[CTOKEN]", companyToken);
					} else {
						emmBlock = emmBlock.replace("agnCTOKEN=[CTOKEN]", "agnCI=" + companyID);
					}
					if (componentJsonObject.containsPropertyKey("mimetype")) {
						mailingComponent.setEmmBlock(emmBlock, (String) componentJsonObject.get("mimetype"));
					} else {
						mailingComponent.setEmmBlock(emmBlock, "text/html");
					}
				}
				if (componentJsonObject.containsPropertyKey("target_id")) {
					Integer targetID = targetIdMappings.get(componentJsonObject.get("target_id"));
					if (targetID == null) {
						throw new Exception("Invalid target_id found for component: " + (String) componentJsonObject.get("name"));
					}
					mailingComponent.setTargetID(targetID);
				}

				if (componentJsonObject.containsPropertyKey("url")) {
					mailingComponent.setLink((String) componentJsonObject.get("url"));
				}

				if (componentJsonObject.containsPropertyKey("bin_block")) {
					mailingComponent.setBinaryBlock(AgnUtils.decodeZippedBase64((String) componentJsonObject.get("bin_block")), (String) componentJsonObject.get("mimetype"));
				}
				components.put(mailingComponent.getComponentName(), mailingComponent);
			}
			mailing.setComponents(components);
		}

		if (jsonObject.containsPropertyKey("contents")) {
			Map<String, DynamicTag> dynTags = new HashMap<>();
			for (Object contentObject : (JsonArray) jsonObject.get("contents")) {
				JsonObject contentJsonObject = (JsonObject) contentObject;
				DynamicTag dynamicTag = new DynamicTagImpl();
				dynamicTag.setDynName((String) contentJsonObject.get("name"));
				if (contentJsonObject.containsPropertyKey("disableLinkExtension")) {
					dynamicTag.setDisableLinkExtension((Boolean) contentJsonObject.get("disableLinkExtension"));
				}
				if (contentJsonObject.containsPropertyKey("content")) {
					for (Object dynContentObject : (JsonArray) contentJsonObject.get("content")) {
						JsonObject dynContentJsonObject = (JsonObject) dynContentObject;
						DynamicTagContent dynamicTagContent = new DynamicTagContentImpl();
						dynamicTagContent.setDynOrder((Integer) dynContentJsonObject.get("order"));
						String contentText = (String) dynContentJsonObject.get("text");
						contentText = contentText.replace("[COMPANY_ID]", Integer.toString(companyID)).replace("[RDIR_DOMAIN]", rdirDomain);
						if (StringUtils.isNotBlank(companyToken)) {
							contentText = contentText.replace("[CTOKEN]", companyToken);
						} else {
							contentText = contentText.replace("agnCTOKEN=[CTOKEN]", "agnCI=" + companyID);
						}
						dynamicTagContent.setDynContent(contentText);
						if (dynContentJsonObject.containsPropertyKey("target_id")) {
							dynamicTagContent.setTargetID(targetIdMappings.get(dynContentJsonObject.get("target_id")));
						}
						dynamicTag.addContent(dynamicTagContent);
					}
				}
				dynTags.put(dynamicTag.getDynName(), dynamicTag);
			}
			mailing.setDynTags(dynTags);
		}

		if (jsonObject.containsPropertyKey("links")) {
			Map<String, ComTrackableLink> trackableLinks = new HashMap<>();
			for (Object linkObject : (JsonArray) jsonObject.get("links")) {
				JsonObject linkJsonObject = (JsonObject) linkObject;
				ComTrackableLink trackableLink = new ComTrackableLinkImpl();
				trackableLink.setShortname((String) linkJsonObject.get("name"));
				
				String fullUrl = (String) linkJsonObject.get("url");
				fullUrl = fullUrl .replace("[COMPANY_ID]", Integer.toString(companyID)) .replace("[RDIR_DOMAIN]", rdirDomain);
				if (StringUtils.isNotBlank(companyToken)) {
					fullUrl = fullUrl.replace("[CTOKEN]", companyToken);
				} else {
					fullUrl = fullUrl.replace("agnCTOKEN=[CTOKEN]", "agnCI=" + companyID);
				}
				trackableLink.setFullUrl(fullUrl);

				if (linkJsonObject.containsPropertyKey("deep_tracking")) {
					trackableLink.setDeepTracking((Integer) linkJsonObject.get("deep_tracking"));
				}

				if (linkJsonObject.containsPropertyKey("usage")) {
					trackableLink.setUsage((Integer) linkJsonObject.get("usage"));
				}

				if (linkJsonObject.containsPropertyKey("action_id")) {
					trackableLink.setActionID(actionIdMappings.get(linkJsonObject.get("action_id")));
				}
				
				if (linkJsonObject.containsPropertyKey("administrative")) {
					trackableLink.setAdminLink((Boolean) linkJsonObject.get("administrative"));
				}
				
				if (linkJsonObject.containsPropertyKey("static")) {
					trackableLink.setStaticValue((Boolean) linkJsonObject.get("static"));
				}
				
				if (linkJsonObject.containsPropertyKey("create_substitute_link")) {
					trackableLink.setCreateSubstituteLinkForAgnDynMulti((Boolean) linkJsonObject.get("create_substitute_link"));
				}

				if (linkJsonObject.containsPropertyKey("properties")) {
					List<LinkProperty> linkProperties = new ArrayList<>();
					for (Object propertyObject : (JsonArray) linkJsonObject.get("properties")) {
						JsonObject propertyJsonObject = (JsonObject) propertyObject;
						String propertyName = (String) propertyJsonObject.get("name");
						if (propertyName == null) {
							propertyName = "";
						}
						String propertyValue = (String) propertyJsonObject.get("value");
						if (propertyValue == null) {
							propertyValue = "";
						}
						if (StringUtils.isNotEmpty(propertyName) || StringUtils.isNotEmpty(propertyValue)) {
							LinkProperty linkProperty = new LinkProperty(PropertyType.parseString((String) propertyJsonObject.get("type")), propertyName, propertyValue);
							linkProperties.add(linkProperty);
						}
					}
					trackableLink.setProperties(linkProperties);
				}

				trackableLinks.put(trackableLink.getFullUrl(), trackableLink);
			}
			mailing.setTrackableLinks(trackableLinks);
		}

		if (mailing.getDescription() == null) {
			// Mark mailing as newly imported
			mailing.setDescription("Imported at " + new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM).format(new Date()));
		}

		if (needsAltg(adminAltgIds, targetIdMappings.values())) {
		    return 0;
        }
        return mailingDao.saveMailing(mailing, false);
	}

	protected Map<Integer, Integer> importTargets(int companyID, JsonObject jsonObject, Map<String, Object[]> warnings) {
		Map<Integer, Integer> targetIdMappings = new HashMap<>();

		targetIdMappings.put(0, 0);

		if (jsonObject.containsPropertyKey("targets")) {
			for (Object targetObject : (JsonArray) jsonObject.get("targets")) {
				JsonObject targetJsonObject = (JsonObject) targetObject;

				// Look for an existing target item with the same data
				int targetID = 0;
				
				String targetName = (String) targetJsonObject.get("name");
				String targetSQL = (String) targetJsonObject.get("sql");
				String targetEQL = (String) targetJsonObject.get("eql");
				boolean isAccessLimitation = targetJsonObject.containsPropertyKey("access_limiting") && (Boolean) targetJsonObject.get("access_limiting");
				for (ComTarget existingTarget : targetDao.getTargetByNameAndSQL(companyID, targetName, targetSQL, false, true, true)) {
					if (AgnUtils.equalsIgnoreLineBreaks(existingTarget.getEQL(), targetEQL) && existingTarget.isAccessLimitation() == isAccessLimitation) {
						targetID = existingTarget.getId();
						break;
					}
				}
				
				if (targetID == 0) {
					// Do not create new target groups, because in most cases they need special profile fields, which may no exist.
					warnings.put("warning.mailing.import.targetgroup", null);
				}
				targetIdMappings.put((Integer) targetJsonObject.get("id"), targetID);
			}
		}

		return targetIdMappings;
	}

	protected String replaceOldTargetIds(String targetExpression, Map<Integer, Integer> targetIdMappings) {
		StringBuilder newTargetExpression = new StringBuilder();
		StringBuilder targetIdBuffer = new StringBuilder();
		for (char character : targetExpression.toCharArray()) {
			if (Character.isDigit(character)) {
				targetIdBuffer.append(character);
			} else {
				if (targetIdBuffer.length() > 0) {
					newTargetExpression.append(targetIdMappings.get(Integer.parseInt(targetIdBuffer.toString())));
					targetIdBuffer = new StringBuilder();
				}
				newTargetExpression.append(character);
			}
		}
		if (targetIdBuffer.length() > 0) {
			newTargetExpression.append(targetIdMappings.get(Integer.parseInt(targetIdBuffer.toString())));
		}
		return newTargetExpression.toString();
	}

	protected void readMediatypes(Mailing mailing, JsonObject jsonObject) throws Exception {
		final Map<Integer, Mediatype> mediatypes = new HashMap<>();
		if (jsonObject.containsPropertyKey("mediatypes")) {
			for (Object mediatypeObject : (JsonArray) jsonObject.get("mediatypes")) {
				final JsonObject mediatypeJsonObject = (JsonObject) mediatypeObject;
				final Mediatype mediatype = mediatypeFactory.createMediatypeFromJson(mediatypeJsonObject);
	
				mediatypes.put(mediatype.getMediaType().getMediaCode(), mediatype);
			}
		}
		if (mediatypes.isEmpty()) {
			// if no mediatype is set, we assume email as mediatype and fill it with minimal example data
			final MediatypeEmail mediatype = new MediatypeEmailImpl();
			
			mediatype.setSubject("Subject text");
			mediatype.setFromEmail("sender@example.com");
			mediatype.setFromFullname(null);
			mediatype.setReplyEmail("replyto@example.com");
			mediatype.setReplyFullname(null);
			mediatype.setCharset("UTF-8");
			mediatype.setMailFormat(MailType.getFromString("HTML_OFFLINE"));
			mediatype.setOnepixel("top");
			mediatype.setLinefeed(72);
			mediatype.setStatus(MediaTypeStatus.Active.getCode());

			mediatypes.put(mediatype.getMediaType().getMediaCode(), mediatype);
			
		}
		mailing.setMediatypes(mediatypes);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = Objects.requireNonNull(applicationContext);
	}
	
	public final ApplicationContext getApplicationContext() {
		return this.applicationContext;
	}
}
