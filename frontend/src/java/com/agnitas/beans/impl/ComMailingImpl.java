/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComMailing;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.LinkService;
import com.agnitas.emm.core.LinkService.ErrorneousLink;
import com.agnitas.emm.core.LinkService.LinkScanResult;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.messages.I18nString;
import org.agnitas.actions.EmmAction;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.TrackableLink;
import org.agnitas.beans.impl.MailingImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.target.exception.UnknownTargetGroupIdException;
import org.agnitas.preview.AgnTagError;
import org.agnitas.preview.TagSyntaxChecker;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.MailoutClient;
import org.agnitas.util.SafeString;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.context.ApplicationContext;

public class ComMailingImpl extends MailingImpl implements ComMailing {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComMailingImpl.class);

	// Default Value is non openers.
	protected String followUpType = TYPE_FOLLOWUP_NON_OPENER;

	protected int splitID;
	
	private String statusmailRecipients;

    protected Date planDate;
    
    private List<ComMailingParameter> parameters;

	private int previewComponentId;
	
	private MailingContentType mailingContentType;

	@Override
	public int getPreviewComponentId() {
		return previewComponentId;
	}

	@Override
	public void setPreviewComponentId(int previewComponentId) {
		this.previewComponentId = previewComponentId;
	}

	@Override
    public Date getPlanDate() {
        return planDate;
    }

    @Override
    public void setPlanDate(Date planDate) {
        this.planDate = planDate;
    }

    @Override
	public int getSplitID() {
		return this.splitID;
	}

	@Override
	public void setSplitID(int splitID) {
		this.splitID = splitID;
	}

	@Override
	public int getArchived() {
		return this.archived ? 1 : 0;
	}

	@Override
	public void setArchived(int archived) {
		this.archived = (archived != 0);
	}

	@Override
	public int getLocked() {
		return this.locked ? 1 : 0;
	}

	@Override
	public void setLocked(int locked) {
		this.locked = (locked != 0);
	}

	@Override
	public boolean triggerMailing(int maildropStatusID, Map<String, Object> opts, ApplicationContext con) {
		// Annotation from mu: I believe that "< 4" is a safety feature, but i
		// don't know why.
		// I think that whoever used it has a good reason for that i leave that
		// condition in here.
		if (getMailingType() < 4) {
			if (logger.isDebugEnabled()) {
				logger.debug("Before Mailgun");
			}
			MailoutClient aClient = new MailoutClient();
			// aClient.invoke("fire", Integer.toString(maildropStatusID) + blockType);
			aClient.invoke("fire", Integer.toString(maildropStatusID));
			if (logger.isDebugEnabled()) {
				logger.debug("After Mailgun");
			}
		}
		return true;
	}

	public boolean triggerMailing(int maildropStatusID) {
		return this.triggerMailing(maildropStatusID, null, null);
	}

	@Override
	public Vector<String> scanForLinks(String text, String textModuleName, ApplicationContext applicationContext, ActionMessages actionMessages, ActionMessages errors) throws Exception {
		return scanForLinks(text, textModuleName, applicationContext, actionMessages, errors, null);
	}

	@Override
	public Vector<String> scanForLinks(String text, String textModuleName, ApplicationContext applicationContext, ActionMessages actionMessages, ActionMessages errors, ComAdmin admin) throws Exception {
		try {
			if (text != null) {
				Vector<String> foundLinkUrls = new Vector<>();
				LinkScanResult linkScanResult = getLinkService(applicationContext).scanForLinks(text, id, mailinglistID, companyID);
				for (ComTrackableLink linkFound : linkScanResult.getTrackableLinks()) {
					if (linkFound.getActionID() > 0 && !isActionExist(linkFound.getActionID())) {
						if (actionMessages != null) {
							actionMessages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.link.invalidActionID", textModuleName, linkFound.getActionID()));
						}
						linkFound.setActionID(0);
					}
					
					if (!trackableLinks.containsKey(linkFound.getFullUrl())) {
						linkFound.setCompanyID(companyID);
						linkFound.setMailingID(id);
						linkFound.setUsage(TrackableLink.TRACKABLE_TEXT_HTML);
						
						// Extend new links with the default company extension if set
						setDefaultExtension(linkFound, applicationContext);
						
						// Extend new links with the link extensions of mailing
						// (= extensions all existing links have in common, because the mailing itself doesn't store link extensions)
						List<LinkProperty> currentLinkProperties = linkFound.getProperties();
						List<LinkProperty> commonLinkProperties = getCommonLinkExtensions();
						for (LinkProperty commonLinkProperty : commonLinkProperties) {
							boolean containsProperty = false;
							for (LinkProperty currentLinkProperty : currentLinkProperties) {
								if (currentLinkProperty.getPropertyName().equals(commonLinkProperty.getPropertyName())) {
									containsProperty = true;
									break;
								}
							}
							if (!containsProperty) {
								currentLinkProperties.add(commonLinkProperty);
							}
						}
						linkFound.setProperties(currentLinkProperties);
						
						trackableLinks.put(linkFound.getFullUrl(), linkFound);
					}
					foundLinkUrls.add(linkFound.getFullUrl());
				}
				
				if (actionMessages != null) {
					// Check for rdir links
					Integer rdirLinkLineNumber = getLinkService(applicationContext).getLineNumberOfFirstRdirLink(text);
					if (rdirLinkLineNumber != null) {
						actionMessages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.link.encoded", textModuleName, rdirLinkLineNumber));
					}
					
					// Check for not measurable links
					if (linkScanResult.getNotTrackableLinks().size() > 0) {
						actionMessages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.link.agntag", textModuleName, StringEscapeUtils.escapeHtml(linkScanResult.getNotTrackableLinks().get(0))));
					}
					
					if (linkScanResult.getLocalLinks().size() > 0) {
						for (final ErrorneousLink link : linkScanResult.getLocalLinks()) {
							actionMessages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING_PERMANENT,
									new ActionMessage("error.mailing.localLink",
											textModuleName,
											StringEscapeUtils.escapeHtml(link.getLinkText())));
						}
					}
					
					// Check for not errorneous links
					if (linkScanResult.getErrorneousLinks().size() > 0) {
						errors.add(ActionMessages.GLOBAL_MESSAGE,
								new ActionMessage("error.mailing.links.errorneous",
										linkScanResult.getErrorneousLinks().size(),
										textModuleName,
										StringEscapeUtils.escapeHtml(linkScanResult.getErrorneousLinks().get(0).getLinkText()),
										I18nString.getLocaleString(linkScanResult.getErrorneousLinks().get(0).getErrorMessageKey(), admin.getLocale())));
					}
				}
				
				return foundLinkUrls;
			}
		} catch (LinkService.ParseLinkException e) {
			logger.error("scanForLinks error in " + textModuleName + ": " + e.getMessage(), e);
			if (errors == null) {
				throw e;
			}
			Locale locale = admin.getLocale();
			String message = SafeString.getLocaleString(e.getErrorMessage(), locale) + ":<br>" + e.getErrorLink();
			
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid.link", SafeString.getLocaleString("error.in", locale)
					+ " " + textModuleName + "<br> " + message));
		} catch (Exception e) {
			logger.error("scanForLinks error in " + textModuleName + ": " + e.getMessage(), e);
			if (errors == null) {
				throw e;
			}

			Locale locale = admin.getLocale();
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid.link", SafeString.getLocaleString("error.in", locale)
						+ " " + textModuleName + "<br> " + e.getMessage()));
		}
		
		return new Vector<>();
	}

    private boolean isActionExist(int actionId){
        try {
            List<EmmAction> actions = getPossibleActions();
            for (EmmAction action : actions) {
                if (action.getId() == actionId){
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("isActionExist: error processing links", e);
            return false;
        }
        return false;
    }

	/**
	 * Sort the dynamic tags by their interest group value, the position of
	 * 'non-interest-group' tags is kept untouched.
	 */
	@Override
	protected Map<DynamicTag, DynamicTag> getTagsReorderMap(List<DynamicTag> tags, int customerId, ApplicationContext con) {
		ComRecipientDao recipientDao = (ComRecipientDao) con.getBean("RecipientDao");

		Map<String, Integer> interestCache = new HashMap<>();
		// Sort tags by interest group value (if specified) or keep an order untouched (if not).
		Comparator<DynamicTag> comparator = new DynTagInterestComparator(group -> {
			// Use interest values cache in order to avoid numerous database queries.
			return interestCache.computeIfAbsent(group, name -> {
				// Get interest value by interest group name.
				return NumberUtils.toInt(recipientDao.getField(name, customerId, companyID));
			});
		});

		return getTagsReorderMap(tags, comparator);
	}

	private Map<DynamicTag, DynamicTag> getTagsReorderMap(List<DynamicTag> tags, Comparator<DynamicTag> comparator) {
		Iterator<DynamicTag> sortedTags = tags.stream().sorted(comparator).iterator();

		Map<DynamicTag, DynamicTag> map = new HashMap<>();
		for (DynamicTag tag : tags) {
			map.put(tag, sortedTags.next());
		}

		return map;
	}

	@Override
	public String getStatusmailRecipients() {
		return statusmailRecipients;
	}

	@Override
	public void setStatusmailRecipients(String statusmailRecipients) {
		this.statusmailRecipients = statusmailRecipients;
	}

	@Override
	public String getFollowUpType() {
		return followUpType;
	}

	@Override
	public void setFollowUpType(String followUpType) {
		this.followUpType = followUpType;
	}

	private void setDefaultExtension(ComTrackableLink link, ApplicationContext con) throws UnsupportedEncodingException {
		ConfigService configService = (ConfigService) con.getBean("ConfigService");
		String defaultExtensionString = configService.getValue(ConfigValue.DefaultLinkExtension, link.getCompanyID());
		if (StringUtils.isNotBlank(defaultExtensionString)) {
			if (defaultExtensionString.startsWith("?")) {
				defaultExtensionString = defaultExtensionString.substring(1);
			}
			String[] extensionProperties = defaultExtensionString.split("&");
			for (String extensionProperty : extensionProperties) {
				final int eqIndex = extensionProperty.indexOf('=');
				final String[] extensionPropertyData = (eqIndex == -1) ? new String[] { extensionProperty, "" } : new String[] { extensionProperty.substring(0, eqIndex), extensionProperty.substring(eqIndex + 1) };
				
				String extensionPropertyName = URLDecoder.decode(extensionPropertyData[0], "UTF-8");
				String extensionPropertyValue = "";
				if (extensionPropertyData.length > 1) {
					extensionPropertyValue = URLDecoder.decode(extensionPropertyData[1], "UTF-8");
				}
		
				// Change link properties
				List<LinkProperty> properties = link.getProperties();
				boolean changedProperty = false;
				for (LinkProperty property : properties) {
					if (property.getPropertyType() == PropertyType.LinkExtension && property.getPropertyName().equals(extensionPropertyName)) {
						property.setPropertyValue(extensionPropertyValue);
						changedProperty = true;
					}
				}
				if (!changedProperty) {
					LinkProperty newProperty = new LinkProperty(PropertyType.LinkExtension, extensionPropertyName, extensionPropertyValue);
					properties.add(newProperty);
				}
			}
		}
	}
	
	/**
	 * Returns a list of link properties with are contained in all links of this mailing.
	 * Link properties contained in a link but not in all the others are not contained in this list,
	 * but are contained in the link property list of the specific link additionally to the link properties of this list.
	 */
	@Override
	public List<LinkProperty> getCommonLinkExtensions() {
		List<LinkProperty> commonLinkProperties = null;
		for (TrackableLink link : getTrackableLinks().values()) {
			if (commonLinkProperties == null) {
				commonLinkProperties = new ArrayList<>(((ComTrackableLink) link).getProperties());
			} else {
				commonLinkProperties.retainAll(((ComTrackableLink) link).getProperties());
			}
		}
		
		if (commonLinkProperties != null) {
			return commonLinkProperties;
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public List<ComMailingParameter> getParameters() {
		return parameters;
	}

	@Override
	public void setParameters(List<ComMailingParameter> parameters) {
		this.parameters = parameters;
	}

	@Override
	public Object clone(ApplicationContext con) {
		ComMailing clonedMailing = (ComMailing) super.clone(con);
		
		// copy link properties of trackable links
		for (Entry<String, TrackableLink> entry : getTrackableLinks().entrySet()) {
			String trackableLinkFullUrl = entry.getKey();
			ComTrackableLink linkOriginal = (ComTrackableLink) entry.getValue();
			ComTrackableLink linkCloned = (ComTrackableLink) clonedMailing.getTrackableLinks().get(trackableLinkFullUrl);

	        // clone old version of link extension
			linkCloned.setExtendByMailingExtensions(linkOriginal.isExtendByMailingExtensions());

	        // clone new version of link extension
			linkCloned.setProperties(new ArrayList<>(linkOriginal.getProperties()));
		}
		clonedMailing.setSplitID(getSplitID());
				
		// Create a clone copy of all mailing parameters
		if (getParameters() != null) {
			List<ComMailingParameter> copiedMailingParameters = new ArrayList<>();
			for (ComMailingParameter templateMailingParameter : getParameters()) {
				ComMailingParameter copiedMailingParameter = new ComMailingParameter();
				
				copiedMailingParameter.setName(templateMailingParameter.getName());
				copiedMailingParameter.setValue(templateMailingParameter.getValue());
				copiedMailingParameter.setDescription(templateMailingParameter.getDescription());
				copiedMailingParameter.setCreationDate(templateMailingParameter.getCreationDate());
				
				copiedMailingParameters.add(copiedMailingParameter);
			}
			clonedMailing.setParameters(copiedMailingParameters);
		}
		
		// If a list split is set in mailing, check, if it is a workflow manager list split
		if (splitID != 0) {
			ComTargetService targetService = (ComTargetService) con.getBean("targetService");
			
			try {
				// If list split is workflow manager list split, remove it
				if (targetService.isWorkflowManagerListSplit(companyID, splitID)) {
					splitID = 0;
					
					if (logger.isInfoEnabled()) {
						logger.info("Removed list split - was from workflow manager");
					}
				}
			} catch(UnknownTargetGroupIdException e) {
				logger.error("Error checking target group for workflow manager list split", e);
			}
		}
		
		return clonedMailing;
	}
	
	@Override
	public Map<String, List<AgnTagError>> checkAgnTagSyntax(ApplicationContext applicationContext) throws Exception {
		Map<String, List<AgnTagError>> returnMap = new HashMap<>();
		TagSyntaxChecker tagSyntaxChecker = (TagSyntaxChecker) applicationContext.getBean("TagSyntaxChecker");
		
		for (Entry<String, MailingComponent> entry : components.entrySet()) {
			if (entry.getValue().getEmmBlock() != null) {
				String componentName = entry.getKey();
				List<AgnTagError> errorList = tagSyntaxChecker.check(companyID, entry.getValue().getEmmBlock());
				if (errorList != null && errorList.size() > 0) {
					returnMap.put(componentName, errorList);
				}
			}
		}

		return returnMap;
	}
	
	@Override
	public String toString() {
		return (StringUtils.isNotBlank(shortname) ? shortname : "<empty shortname>") + " (" + id + ")";
	}

	private static class DynTagInterestComparator implements Comparator<DynamicTag> {
		DynTagInterestResolver resolver;

		public DynTagInterestComparator(DynTagInterestResolver resolver) {
			this.resolver = resolver;
		}

		@Override
		public int compare(DynamicTag tag1, DynamicTag tag2) {
			String group1 = tag1.getDynInterestGroup();
			String group2 = tag2.getDynInterestGroup();

			// Keep an existing order when no interest group selected.
			if (group1 == null || group2 == null) {
				return 0;
			} else {
				return -Integer.compare(resolver.resolve(group1), resolver.resolve(group2));
			}
		}
	}

	private interface DynTagInterestResolver {
		int resolve(String interestGroup);
	}

	@Override
	public MailingContentType getMailingContentType() {
		return mailingContentType == null ? MailingContentType.advertising : mailingContentType;
	}

	@Override
	public void setMailingContentType(MailingContentType mailingContentType) {
		this.mailingContentType = mailingContentType;
	}
}
