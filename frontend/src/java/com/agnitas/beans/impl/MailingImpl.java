/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import static org.agnitas.beans.impl.MailingComponentImpl.COMPONENT_NAME_MAX_LENGTH;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.agnitas.actions.EmmAction;
import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.beans.Mediatype;
import org.agnitas.beans.TrackableLink;
import org.agnitas.beans.impl.MailingBaseImpl;
import org.agnitas.dao.FollowUpType;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.preview.AgnTagError;
import org.agnitas.preview.TagSyntaxChecker;
import org.agnitas.service.UserMessageException;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.MailoutClient;
import org.agnitas.util.SafeString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.context.ApplicationContext;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingContentType;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.linkcheck.service.LinkService.ErrorneousLink;
import com.agnitas.emm.core.linkcheck.service.LinkService.LinkScanResult;
import com.agnitas.emm.core.mailing.bean.ComMailingParameter;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;
import com.agnitas.emm.core.trackablelinks.web.LinkScanResultToActionMessages;
import com.agnitas.messages.I18nString;
import com.agnitas.service.AgnDynTagGroupResolverFactory;
import com.agnitas.service.AgnTagService;
import com.agnitas.util.ImageUtils;
import com.agnitas.util.LinkUtils;

public class MailingImpl extends MailingBaseImpl implements Mailing {
	public static final String LINK_SWYN_PREFIX = "SWYN: ";

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailingImpl.class);

	/**
	 * mailingType can hold the values 0-3 0: Normal mailing 1: Action-Based 2:
	 * Date-Based 3: Followup Defined in Mailing.java eg. TYPE_NORMAL
	 */
	protected int mailingType;
	
	protected int mailTemplateID;
	protected int targetID;
	protected Map<String, DynamicTag> dynTags = new LinkedHashMap<>();
	protected Map<String, MailingComponent> components = new LinkedHashMap<>();
	protected Hashtable<String, MailingComponent> attachments;
	protected Map<String, ComTrackableLink> trackableLinks = new LinkedHashMap<>();
	protected int clickActionID;
	protected int openActionID;
	protected Set<MaildropEntry> maildropStatus = new LinkedHashSet<>();
	protected Map<Integer, Mediatype> mediatypes = new LinkedHashMap<>();
	protected Date creationDate;
	protected Date changeDate;
	protected Map<Integer, ComTarget> allowedTargets = null;
	protected Collection<Integer> targetGroups;
	protected int templateOK;
	protected boolean isTemplate;
	protected int targetMode;
	protected String targetExpression;
	protected int deleted;
	protected boolean needsTarget;
	protected boolean locked;
	protected int priority;
	protected boolean isPrioritizationAllowed;
	protected boolean archived;
    private List<EmmAction> possibleActions;
	private boolean isFrequencyCounterDisabled;

	private LinkService linkService;

	// Default Value is non openers.
	protected String followUpType = FollowUpType.TYPE_FOLLOWUP_NON_OPENER.getKey();

	protected int splitID;
	
	private String statusmailRecipients;
	
	private boolean statusmailOnErrorOnly;

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
		return archived ? 1 : 0;
	}

	@Override
	public void setArchived(int archived) {
		this.archived = (archived != 0);
	}

	@Override
	public int getLocked() {
		return locked ? 1 : 0;
	}

	@Override
	public void setLocked(int locked) {
		this.locked = (locked != 0);
	}

	@Override
	public boolean triggerMailing(int maildropStatusID, Map<String, Object> opts, ApplicationContext con) throws Exception {
		try {
			if (maildropStatusID <= 0) {
				logger.warn( "maildropStatisID is 0");
				throw new Exception("maildropStatusID is 0");
			}
			
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
		} catch (Exception e) {
			return false;
		}
	}

	public boolean triggerMailing(int maildropStatusID) throws Exception {
		return triggerMailing(maildropStatusID, null, null);
	}

	@Override
	public Vector<String> scanForLinks(String text, String textModuleName, ApplicationContext applicationContext, ActionMessages actionMessages, ActionMessages errors) throws Exception {
		return scanForLinks(text, textModuleName, applicationContext, actionMessages, errors, null);
	}

	@Override
	public Vector<String> scanForLinks(String text, String textModuleName, ApplicationContext applicationContext, ActionMessages warnings, ActionMessages errors, ComAdmin admin) throws Exception {
		try {
			if (text != null) {
				Vector<String> foundLinkUrls = new Vector<>();
				LinkScanResult linkScanResult = getLinkService(applicationContext).scanForLinks(text, id, mailinglistID, companyID);
				for (ComTrackableLink linkFound : linkScanResult.getTrackableLinks()) {
					if (linkFound.getActionID() > 0 && !isActionExist(linkFound.getActionID())) {
						if (warnings != null) {
							warnings.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.link.invalidActionID", textModuleName, linkFound.getActionID()));
						}
						linkFound.setActionID(0);
					}
					
					if (linkFound.getFullUrl().contains("/form.do?")) {
						if (warnings != null) {
							warnings.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.link.oldFormDoLink", textModuleName));
						}
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

					if (foundLinkUrls.contains(linkFound.getFullUrl())) {
						trackableLinks.get(linkFound.getFullUrl()).setMeasureSeparately(false);
					}
					foundLinkUrls.add(linkFound.getFullUrl());
				}
				
				if (warnings != null) {
					// Check for rdir links
					Integer rdirLinkLineNumber = getLinkService(applicationContext).getLineNumberOfFirstRdirLink(companyID, text);
					if (rdirLinkLineNumber != null) {
						warnings.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.link.encoded", textModuleName, rdirLinkLineNumber));
					}
					
					// Check for not measurable links
					if (linkScanResult.getNotTrackableLinks().size() > 0) {
						warnings.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.mailing.link.agntag", textModuleName, StringEscapeUtils.escapeHtml4(linkScanResult.getNotTrackableLinks().get(0))));
					}
					
					if (linkScanResult.getLocalLinks().size() > 0) {
						for (final ErrorneousLink link : linkScanResult.getLocalLinks()) {
							warnings.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING_PERMANENT,
									new ActionMessage("error.mailing.localLink",
											textModuleName,
											StringEscapeUtils.escapeHtml4(link.getLinkText())));
						}
					}
					
					LinkScanResultToActionMessages.linkWarningsToActionMessages(linkScanResult, warnings);
					
					// Check for not errorneous links
					if (linkScanResult.getErrorneousLinks().size() > 0) {
						final String linkText = StringEscapeUtils.escapeHtml4(
								linkScanResult.getErrorneousLinks().get(0).getLinkText());
						
						final String errorText = I18nString.getLocaleString(
								linkScanResult.getErrorneousLinks().get(0).getErrorMessageKey(),
								admin.getLocale(),
								linkText
							);
						
						errors.add(ActionMessages.GLOBAL_MESSAGE,
								new ActionMessage("error.mailing.links.errorneous",
										linkScanResult.getErrorneousLinks().size(),
										textModuleName,
										linkText,
										errorText));
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
            if (e.getCause() instanceof LinkService.ErrorLinkStorage) {
                LinkService.ErrorLinkStorage linkError = (LinkService.ErrorLinkStorage) e.getCause();
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid.link",
                        String.format("%s %s<br> %s", SafeString.getLocaleString("error.in", locale), textModuleName, linkError.getErrorLink())));
            } else {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid.link", SafeString.getLocaleString("error.in", locale)
                        + " " + textModuleName + "<br> " + e.getMessage()));
            }
        }
		
		return new Vector<>();
	}

	@Override
	public Vector<String> scanForLinks(ApplicationContext con) throws Exception {
        return scanForLinks(con, null, null, null);
    }

	private Vector<String> scanForLinks(ApplicationContext con, ActionMessages messages, ActionMessages errors, ComAdmin admin) throws Exception {
		Vector<String> addedLinks = new Vector<>();

		for (MailingComponent component : components.values()) {
			if (component.getType() == MailingComponentType.Template) {
				addedLinks.addAll(scanForLinks(component.getEmmBlock(), component.getComponentName(), con, messages, errors, admin));
			}
		}

		for (DynamicTag tag : dynTags.values()) {
			for (DynamicTagContent content : tag.getDynContent().values()) {
				addedLinks.addAll(scanForLinks(content.getDynContent(), tag.getDynName(), con, messages, errors, admin));
				removeActionTag(content);
			}
		}

		return addedLinks;
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
	public boolean isStatusmailOnErrorOnly() {
		return statusmailOnErrorOnly;
	}

	@Override
	public void setStatusmailOnErrorOnly(boolean statusmailOnErrorOnly) {
		this.statusmailOnErrorOnly = statusmailOnErrorOnly;
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
					if (LinkUtils.isExtension(property) && property.getPropertyName().equals(extensionPropertyName)) {
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
			if (link.getShortname() == null || !link.getShortname().startsWith(LINK_SWYN_PREFIX)) {
				if (commonLinkProperties == null) {
					commonLinkProperties = new ArrayList<>(link.getProperties());
				} else {
					commonLinkProperties.retainAll(link.getProperties());
				}
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

	@Override
	public final Set<Integer> getAllReferencedTargetGroups() {
		final Set<Integer> targetIds = new HashSet<>();
		
		// Collect target groups from target expression
		if(this.getTargetGroups() != null) {
			targetIds.addAll(this.getTargetGroups());
		}
	
		// Collect target groups from components
		for(final MailingComponent mailingComponent : this.getComponents().values()) {
			targetIds.add(mailingComponent.getTargetID());
		}
		
		// Collect target groups from content blocks
		for(final DynamicTag dynamicTag : this.dynTags.values()) {
			targetIds.addAll(dynamicTag.getAllReferencedTargetGroups());
		}
		
		// Remove ID 0 (may be added if something has no target group set
		targetIds.remove(0);

		return targetIds;
	}


	@Override
	public boolean parseTargetExpression(String tExp) {
		targetMode = Mailing.TARGET_MODE_AND;
		if (tExp == null) {
			return false;
		}
		if (tExp.indexOf('|') != -1) {
			targetMode = Mailing.TARGET_MODE_OR;
		}
		targetGroups = TargetExpressionUtils.getTargetIds(tExp);
		return true;
	}

    @Override
	public void updateTargetExpression() {
		targetExpression = TargetExpressionUtils.makeTargetExpression(targetGroups, targetMode != Mailing.TARGET_MODE_OR);
    }

	@Override
	public void addComponent(MailingComponent aComp) {

		if (components == null) {
			components = new HashMap<>();
		}

		if (!components.containsKey(aComp.getComponentName())) {
			components.put(aComp.getComponentName(), aComp);
		}
	}

	private void addComponents( Set<MailingComponent> componentsToAdd) {
		for( MailingComponent component : componentsToAdd) {
			addComponent( component);
		}
	}

	@Override
	public void addAttachment(MailingComponent aComp) {

		if (attachments == null) {
			attachments = new Hashtable<>();
		}

		attachments.put(aComp.getComponentName(), aComp);
	}

	@Override
	public void setTargetID(int tmpid) {
		targetID = tmpid;
	}

	@Override
	public void setMailTemplateID(int tmpid) {
		mailTemplateID = tmpid;
	}

	@Override
	public Map<String, DynamicTag> getDynTags() {
		return dynTags;
	}

	/**
     * Search for tags and adds then to a vector.
     *
     * @return Vector of added tags.
     * @deprecated use {@link com.agnitas.service.AgnTagService#getDynTags(String, com.agnitas.service.AgnDynTagGroupResolver)}.
     */
    @Deprecated
	@Override
	public Vector<String> findDynTagsInTemplates(String aTemplate, ApplicationContext con) throws Exception {
		AgnTagService service = con.getBean("AgnTagService", AgnTagService.class);
		AgnDynTagGroupResolverFactory resolverFactory = con.getBean("AgnDynTagGroupResolverFactory", AgnDynTagGroupResolverFactory.class);
		List<DynamicTag> tags = service.getDynTags(aTemplate, resolverFactory.create(companyID, id));

		Vector<String> names = new Vector<>();
		for (DynamicTag tag : tags) {
			tag.setCompanyID(companyID);
			tag.setMailingID(id);
			addDynamicTag(tag);

			names.add(tag.getDynName());
		}

		return names;
	}

	private Vector<String> findDynTagsInTemplates(ApplicationContext con) throws Exception {
		Vector<String> names = new Vector<>();

		for (MailingComponent component : components.values()) {
			if (component.getType() == MailingComponentType.Template) {
				MediaTypes mediaType = MediaTypes.findMediatypeForComponentName(component.getComponentName());
				Mediatype type = mediaType != null ? getMediatypes().get(mediaType.getMediaCode()) : null;

				if (mediaType == null || type == null || type.getStatus() == MediaTypeStatus.Active.getCode()) {
					names.addAll(findDynTagsInTemplates(component.getEmmBlock(), con));
				}
			}
		}

		return names;
	}

	public Vector<String> scanForComponents(ApplicationContext con, int companyIDToCheckFor, ActionMessages errors) throws Exception {
		Vector<String> addedTags = new Vector<>();

		Set<MailingComponent> componentsToAdd = new HashSet<>();

		for (Entry<String, MailingComponent> componentsEntry : components.entrySet()) {
			MailingComponent tmpComp = componentsEntry.getValue();
			if (tmpComp.getType() == MailingComponentType.Template) {
				addedTags.addAll(scanForComponents(tmpComp.getEmmBlock(), con, componentsToAdd, companyIDToCheckFor, errors));
                addedTags.addAll(TagSyntaxChecker.scanForAgnTagNameValues(tmpComp.getEmmBlock(), "agnIMAGE", "agnIMGLINK"));
            }
		}
		addComponents( componentsToAdd);

		componentsToAdd.clear();
		for (Entry<String, DynamicTag> dynTagEntry : dynTags.entrySet()) {
			String name = dynTagEntry.getKey();
			DynamicTag dyntag = dynTagEntry.getValue();

			for (Entry<Integer, DynamicTagContent> dynTagContentEntry : dyntag.getDynContent().entrySet()) {
				DynamicTagContent dyncontent = dynTagContentEntry.getValue();
				try {
					addedTags.addAll(scanForComponents(dyncontent.getDynContent(), con, componentsToAdd, companyIDToCheckFor, errors));
					addedTags.addAll(TagSyntaxChecker.scanForAgnTagNameValues(dyncontent.getDynContent(), "agnIMAGE", "agnIMGLINK"));
				} catch (LinkService.ParseLinkException e){
					logger.error("Error in dyncontent " + name + ": " + e.getMessage(), e);
					throw new LinkService.ParseLinkException ("Error in dyncontent " + name + ": " + e.getMessage(), e);
				} catch (Exception e) {
					logger.error("Error in dyncontent " + name + ": " + e.getMessage(), e);
					throw new Exception("Error in dyncontent " + name + ": " + e.getMessage(), e);
				}
			}
		}
		addComponents( componentsToAdd);

		return addedTags;
	}

	@Override
	public List<String> replaceAndGetMeasuredSeparatelyLinks(List<String> links, ApplicationContext context) throws Exception {
		Map<String, Long> counters = scanForLinks(context).stream()
                .filter(links::contains)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Set<String> duplicates = counters.entrySet().stream()
                .filter(pair -> pair.getValue() > 1)
                .map(Map.Entry::getKey).collect(Collectors.toSet());

        List<String> measuredSeparatelyLinks = new ArrayList<>();
        if (duplicates.size() > 0) {
            Map<String, Integer> duplicateCounters = duplicates.stream().collect(Collectors.toMap(Function.identity(), v -> 1));

            components.values().stream()
					.filter((component) -> component.getType() == MailingComponentType.Template)
					.forEach((component) -> component.setEmmBlock(addNumbersToLinks(duplicateCounters, measuredSeparatelyLinks, component.getEmmBlock(), context), component.getMimeType()));

            dynTags.values().stream()
                    .flatMap((tag) -> tag.getDynContent().values().stream())
                    .forEach((content) -> content.setDynContent(addNumbersToLinks(duplicateCounters, measuredSeparatelyLinks, content.getDynContent(), context)));
        }

        return measuredSeparatelyLinks;
	}

	protected String addNumbersToLinks(Map<String, Integer> linksCounters, List<String> separatelyMeasuredLinks, String originContent, ApplicationContext context) {
		StringBuilder changedContent = new StringBuilder(originContent);
        AtomicInteger accumulatedDiff = new AtomicInteger(0);

		getLinkService(context).findAllLinks(originContent, (start, end) -> {
		    String linkUrlToReplace = originContent.substring(start, end);
			Integer count = linksCounters.get(linkUrlToReplace);
			if(count != null){
				String linkNumber = "#" + count;
				changedContent.insert(end + accumulatedDiff.get(), linkNumber);
				accumulatedDiff.addAndGet(linkNumber.length());
				linksCounters.put(linkUrlToReplace, count + 1);
				separatelyMeasuredLinks.add(linkUrlToReplace + linkNumber);
			}
		});
		return changedContent.toString();
	}

    @Override
	public void addDynamicTag(DynamicTag newTag) {
    	DynamicTag tag = dynTags.get(newTag.getDynName());
		if (tag == null) {
			dynTags.put(newTag.getDynName(), newTag);
		} else {
			tag.setGroup(newTag.getGroup());
		}
	}

	private Vector<String> scanForComponents(String text, ApplicationContext applicationContext, Set<MailingComponent> componentsToAdd, int companyIDToCheckFor, ActionMessages errors) throws Exception {
		final Vector<String> foundComponentUrls = new Vector<>();
		final LinkScanResult linkScanResult = getLinkService(applicationContext).scanForLinks(text, companyIDToCheckFor);

		// Create set to get unique links.
		final Set<String> uniqueImageLinks = new HashSet<>(linkScanResult.getImageLinks());
		
        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (final String componentLinkString : uniqueImageLinks) {
            executorService.execute(() -> {
                MailingComponent foundComponent = applicationContext.getBean("MailingComponent", MailingComponent.class);
                foundComponent.setCompanyID(companyIDToCheckFor);
                foundComponent.setMailingID(id);
                foundComponent.setComponentName(componentLinkString);
                foundComponent.setType(MailingComponentType.Image);

                if (!components.containsKey(componentLinkString)) {
                    if (foundComponent.getComponentName().contains("[agn")) {
                        // Don't check image mimetype, if img-link url contains agnTags
                        componentsToAdd.add(foundComponent);
                    } else {
                        foundComponent.loadContentFromURL();
                        if (foundComponent.getMimeType().startsWith("image")) {
                            componentsToAdd.add(foundComponent);
                        }
                    }
                } else {
                    foundComponent = components.get(componentLinkString);
                }

                if (foundComponent.getMimeType().startsWith("image")) {
                    foundComponentUrls.add(componentLinkString);
                }
            });
        }

        try {
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            logger.error("Unable to find some component", e);
        }

		return foundComponentUrls;
	}

    /**
     * Remove ActionID tag (ex: actionID="123") from mailing content.
     *
     * @param dynContent
     */
    private void removeActionTag(DynamicTagContent dynContent) {
        try {
            String content = dynContent.getDynContent();
            Pattern pattern = Pattern.compile("actionID=\"[ 0-9A-Za-z_.+-]+\"");
            Matcher match = pattern.matcher(content);
            if (match.find()) {
                dynContent.setDynContent(content.replace(match.group(), ""));
            }
        } catch (Exception e) {
            logger.error("removeActionTag: error removing actionID from link", e);
        }
    }

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Map<String, MailingComponent> getComponents() {
		return components;
	}

	@Override
	public Map<String, ComTrackableLink> getTrackableLinks() {
		return trackableLinks;
	}

	@Override
	public int getTargetID() {
		return targetID;
	}

	/**
	 * Getter for property mailingType.
	 *
	 * @return Value of property mailingType.
	 */
	@Override
	public int getMailingType() {
		return mailingType;
	}

	/**
	 * Setter for property mailingType.
	 *
	 * @param mailingType
	 *            New value of property mailingType.
	 */
	@Override
	public void setMailingType(int mailingType) {
		this.mailingType = mailingType;
	}

	@Override
	public void setMailingType(MailingType mailingType) {
		this.mailingType = mailingType.getCode();
	}

	/**
	 * Scans a textblock for trackable links and replaces them with encoded
	 * rdir-links.
	 */
	public String insertTrackableLinks(String aText1, int customerID, ApplicationContext context) {
		if (trackableLinks == null) {
			return aText1;
		}

		if (aText1 == null) {
			return null;
		}

		/*
		 * trackableLinks is an unordered HashMap. When there are 2 links in the
		 * Map, where one is part of the other, this could lead to an link
		 * replacement, depending on the map ordering.
		 *
		 * Link 1: http://www.mydomain.de Link 2:
		 * http://www.mydomain.de/path/index.htm
		 *
		 * If Link 1 is returned before Link 2 from the iterator this resulted
		 * in: https://rdir.de/r.html?uid=<uid of Link1>/path/index.htm
		 */
		Set<String> sorted = new TreeSet<>(Comparator.reverseOrder());
		sorted.addAll(trackableLinks.keySet());

		int start_link = 0;
		int end_link = 0;
		StringBuilder sb = new StringBuilder(aText1);
		boolean isHref = false;
		for (String aLink : sorted) {
			end_link = 0;
			while ((start_link = sb.indexOf(aLink, end_link)) != -1) {
				end_link = start_link + 1;
				isHref = false;
				if (start_link > 5 && (sb.substring(start_link - 6, start_link).equalsIgnoreCase("href=\""))) {
					isHref = true;
				}
				if (start_link > 6 && (sb.substring(start_link - 7, start_link).equalsIgnoreCase("href=\""))) {
					isHref = true;
				}
				if (sb.length() > (start_link + aLink.length())) {
					if (!(sb.charAt(start_link + aLink.length()) == ' ' || sb.charAt(start_link + aLink.length()) == '\'' || sb.charAt(start_link + aLink.length()) == '"')) {
						isHref = false;
					}
				}
				if (isHref) {
					TrackableLink trackableLink = trackableLinks.get(aLink);
					sb.replace(start_link, start_link + aLink.length(), getLinkService(context).encodeTagStringLinkTracking(trackableLink.getCompanyID(), trackableLink.getMailingID(), trackableLink.getId(), customerID));
				}
			}
		}
		return sb.toString();
	}

	@Override
	public MailingComponent getTemplate(String type) {
		return components.get("agn" + type);
	}
	
	@Override
	public MailingComponent getTemplate(MediaTypes type) {
		return components.get(type.getComponentNames()[0]);
	}

	@Override
	public MailingComponent getHtmlTemplate() {
		return getTemplate("Html");
	}

	@Override
	public MailingComponent getTextTemplate() {
		return getTemplate("Text");
	}

	@Override
	public boolean hasComplexTargetExpression() {
		if (StringUtils.isBlank(targetExpression)) {
			return false;
		} else {
			return (targetExpression.contains("&") && targetExpression.contains("|")) || StringUtils.containsAny(targetExpression, "()!");
		}
	}

	/**
	 * Getter for property emailParam.
	 *
	 * @return Value of property emailParam.
	 */
	@Override
	public MediatypeEmail getEmailParam() {
		return (MediatypeEmail) mediatypes.get(MediaTypes.EMAIL.getMediaCode());
	}

	/**
	 * Getter for property targetGroups.
	 *
	 * @return Value of property targetGroups.
	 */
	@Override
	public Collection<Integer> getTargetGroups() {
		return targetGroups;
	}

	/**
	 * Setter for property targetGroups.
	 *
	 * @param targetGroups
	 *            New value of property targetGroups.
	 */
	@Override
	public void setTargetGroups(Collection<Integer> targetGroups) {
		this.targetGroups = targetGroups;
		updateTargetExpression();
	}

	/**
	 * Setter for property htmlTemplate.
	 *
	 * @param htmlTemplate
	 *            New value of property htmlTemplate.
	 */
	@Override
	public void setHtmlTemplate(MailingComponent htmlTemplate) {
		if (htmlTemplate != null) {
			components.put("agnHtml", htmlTemplate);
		}
	}

	/**
	 * Setter for property dynTags.
	 *
	 * @param dynTags
	 *            New value of property dynTags.
	 */
	@Override
	public void setDynTags(Map<String, DynamicTag> dynTags) {
		this.dynTags = dynTags;
	}

	/**
	 * Setter for property dynTags.
	 *
	 * @param trackableLinks
	 */
	@Override
	public void setTrackableLinks(Map<String, ComTrackableLink> trackableLinks) {
		this.trackableLinks = trackableLinks;
	}

	/**
	 * Setter for property components.
	 *
	 * @param components
	 *            New value of property components.
	 */
	@Override
	public void setComponents(Map<String, MailingComponent> components) {
		this.components = components;
	}

	/**
	 * Setter for property textTemplate.
	 *
	 * @param textTemplate
	 */
	@Override
	public void setTextTemplate(MailingComponent textTemplate) {
		if (textTemplate != null) {
			components.put("agnText", textTemplate);
		}
	}

	/**
	 * Getter for property mailTemplateID.
	 *
	 * @return Value of property mailTemplateID.
	 */
	@Override
	public int getMailTemplateID() {
		return mailTemplateID;
	}

	/**
	 * Getter for property templateOK.
	 *
	 * @return Value of property templateOK.
	 */
	@Override
	public int getTemplateOK() {
		return templateOK;
	}

	/**
	 * Setter for property templateOK.
	 *
	 * @param templateOK
	 *            New value of property templateOK.
	 */
	@Override
	public void setTemplateOK(int templateOK) {
		this.templateOK = templateOK;
	}

	/**
	 * Getter for property isTemplate.
	 *
	 * @return Value of property isTemplate.
	 */
	@Override
	public boolean isIsTemplate() {
		return isTemplate;
	}

	/**
	 * Setter for property isTemplate.
	 *
	 * @param isTemplate
	 *            New value of property isTemplate.
	 */
	@Override
	public void setIsTemplate(boolean isTemplate) {
		this.isTemplate = isTemplate;
	}

	/**
	 * Getter for property targetMode.
	 *
	 * @return Value of property targetMode.
	 */
	@Override
	public int getTargetMode() {
		return targetMode;
	}

	/**
	 * Setter for property targetMode.
	 *
	 * @param targetMode
	 *            New value of property targetMode.
	 */
	@Override
	public void setTargetMode(int targetMode) {
		this.targetMode = targetMode;
	}

	/**
	 * Getter for property targetExpression.
	 *
	 * @return Value of property targetExpression.
	 */
	@Override
	public String getTargetExpression() {
		return targetExpression;
	}

	/**
	 * Setter for property targetExpression.
	 *
	 * @param targetExpression
	 *            New value of property targetExpression.
	 */
	@Override
	public void setTargetExpression(String targetExpression) {
		this.targetExpression = targetExpression;
		parseTargetExpression(this.targetExpression);
	}

	/**
	 * Getter for property mediatypes.
	 *
	 * @return Value of property mediatypes.
	 */
	@Override
	public Map<Integer, Mediatype> getMediatypes() {
		return mediatypes;
	}

	/**
	 * Setter for property mediatypes.
	 *
	 * @param mediatypes
	 *            New value of property mediatypes.
	 */
	@Override
	public void setMediatypes(Map<Integer, Mediatype> mediatypes) {
		this.mediatypes = mediatypes;
	}

	@Override
	public void init( @VelocityCheck int newCompanyID, ApplicationContext con) {
		MailingComponent comp;
		Mediatype type;

		this.companyID = newCompanyID;

		comp = con.getBean("MailingComponent", MailingComponent.class);
		comp.setCompanyID(newCompanyID);
		comp.setComponentName("agnText");
		comp.setType(MailingComponentType.Template);
		comp.setEmmBlock("[agnDYN name=\"Text-Version\"/]", "text/plain");
		components.put("agnText", comp);

		comp = con.getBean("MailingComponent", MailingComponent.class);
		comp.setCompanyID(newCompanyID);
		comp.setComponentName("agnHtml");
		comp.setType(MailingComponentType.Template);
		comp.setEmmBlock("[agnDYN name=\"HTML-Version\"/]", "text/html");
		components.put("agnHtml", comp);

		type = con.getBean("MediatypeEmail", Mediatype.class);
		type.setCompanyID(newCompanyID);
		mediatypes.put(MediaTypes.EMAIL.getMediaCode(), type);
	}

	@Override
	public List<String> cleanupDynTags(Vector<String> keep) {
		List<String> remove = new ArrayList<>();

		dynTags.keySet().removeIf(name -> {
			if (keep.contains(name)) {
				return false;
			} else {
				remove.add(name);
				return true;
			}
		});

		return remove;
	}

	@Override
	public void cleanupTrackableLinks(Vector<String> keep) {
		trackableLinks.keySet().removeIf(name -> !keep.contains(name));
	}

	@Override
	public void cleanupMailingComponents(Vector<String> keep) {
		// first find keys which should be removed
		List<String> removeKeys = new Vector<>();
		for (MailingComponent mailingComponent : components.values()) {
			String baseComponentName = mailingComponent.getComponentName();
			if (baseComponentName.startsWith(ImageUtils.MOBILE_IMAGE_PREFIX)) {
				baseComponentName = baseComponentName.substring(ImageUtils.MOBILE_IMAGE_PREFIX.length());
			}

			if ((mailingComponent.getType() == MailingComponentType.Image
						|| mailingComponent.getType() == MailingComponentType.HostedImage)
					&& !keep.contains(baseComponentName)) {
				removeKeys.add(mailingComponent.getComponentName());
			}
		}

		// now remove them!
		for (String key : removeKeys) {
			components.remove(key);
		}
	}

	@Override
	public DynamicTag getDynamicTagById(int dynId) {

		for (DynamicTag tmp : dynTags.values()) {
			if (dynId == tmp.getId()) {
				return tmp;
			}
		}
		return null;
	}

	@Override
	public TrackableLink getTrackableLinkById(int urlID) {
		for (TrackableLink tmp : trackableLinks.values()) {
			if (urlID == tmp.getId()) {
				return tmp;
			}
		}
		return null;
	}

	@Override
	public boolean buildDependencies(boolean scanDynTags, ApplicationContext con) throws Exception {
		return buildDependencies(scanDynTags, null, con, null, null);
	}

	@Override
    public boolean buildDependencies(boolean scanDynTags, List<String> dynNamesForDeletion, ApplicationContext con) throws Exception {
		return buildDependencies(scanDynTags, dynNamesForDeletion, con, null, null);
	}

	@Override
	public boolean buildDependencies(boolean scanDynTags, List<String> dynNamesForDeletion, ApplicationContext con, ActionMessages messages, ActionMessages errors) throws Exception {
		return buildDependencies(scanDynTags, dynNamesForDeletion, con, messages, errors, null);
	}

	@Override
	public boolean buildDependencies(boolean scanDynTags, List<String> dynNamesForDeletion, ApplicationContext con, ActionMessages messages, ActionMessages errors, ComAdmin admin) throws Exception {
		Vector<String> componentsToCheck = new Vector<>();
		Vector<String> links = new Vector<>();

		// scan for Dyntags
		// in template-components and Mediatype-Params
		if (scanDynTags) {
			Vector<String> dynNamesInUse = new Vector<>(findDynTagsInTemplates(con));

			MediatypeEmail emailParam = getEmailParam();
			if (emailParam != null) {
				dynNamesInUse.addAll(findDynTagsInTemplates(emailParam.getSubject(), con));
				dynNamesInUse.addAll(findDynTagsInTemplates(emailParam.getReplyAdr(), con));
				dynNamesInUse.addAll(findDynTagsInTemplates(emailParam.getFromAdr(), con));
			}

			if (dynNamesInUse.size() > 0) {
				dynNamesInUse.addAll(findDynTagsInDynContent(dynNamesInUse, con));
			}

			List<String> excludedDynNames = cleanupDynTags(dynNamesInUse);

			if (dynNamesForDeletion != null) {
				dynNamesForDeletion.addAll(excludedDynNames);
			}
		}

		// scan for Components
		// in template-components and dyncontent
		try {
			componentsToCheck.addAll(scanForComponents(con, companyID, errors));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		cleanupMailingComponents(componentsToCheck);
        for (MailingComponent component : components.values()) {
            if (StringUtils.length(component.getComponentName()) > COMPONENT_NAME_MAX_LENGTH) {
                throw new UserMessageException("error.compname.too.long", component.getComponentName());
            }
        }

		// scan for Links
		// in template-components and dyncontent
		links.addAll(scanForLinks(con, messages, errors, admin));
		// if(ConfigService.isOracleDB()) {
		// causes problem with links in OpenEMM
		cleanupTrackableLinks(links);
		// }

		return true;
	}

	private Vector<String> findDynTagsInDynContent(Vector<String> names, ApplicationContext con) throws Exception {
		Set<String> dynNamesInUse = new HashSet<>(names);
		Set<String> namesToScan = new HashSet<>(dynNamesInUse);

		while (namesToScan.size() > 0) {
			Vector<String> found = new Vector<>();

			for (String name : namesToScan) {
				DynamicTag dynamicTag = dynTags.get(name);

				if (dynamicTag != null) {
					for (DynamicTagContent dynamicTagContent : dynamicTag.getDynContent().values()) {
						found.addAll(findDynTagsInTemplates(dynamicTagContent.getDynContent(), con));
					}
				}
			}

			namesToScan.clear();

			for (String name : found) {
				if (dynNamesInUse.add(name)) {
					namesToScan.add(name);
				}
			}
		}

		dynNamesInUse.removeAll(names);

		return new Vector<>(dynNamesInUse);
	}

	@Override
	public Map<Integer, ComTarget> getAllowedTargets(ApplicationContext myContext) {
		if (allowedTargets == null) {
			ComTargetDao dao = myContext.getBean("TargetDao", ComTargetDao.class);

			allowedTargets = dao.getAllowedTargets(companyID);
			if (allowedTargets != null) {
				ComTarget aTarget = myContext.getBean("Target", ComTarget.class);

				aTarget.setCompanyID(companyID);
				aTarget.setId(0);
				aTarget.setTargetName("All Subscribers");
				allowedTargets.put(0, aTarget);
			}
		}

		return allowedTargets;
	}

	/**
	 * Getter for property maildropStatus.
	 *
	 * @return Value of property maildropStatus.
	 */
	@Override
	public Set<MaildropEntry> getMaildropStatus() {
		return maildropStatus;
	}

	/**
	 * Setter for property maildropStatus.
	 *
	 * @param maildropStatus
	 *            New value of property maildropStatus.
	 */
	@Override
	public void setMaildropStatus(Set<MaildropEntry> maildropStatus) {
		this.maildropStatus = maildropStatus;
	}

	/**
	 * Getter for property deleted.
	 *
	 * @return Value of property deleted.
	 */
	@Override
	public int getDeleted() {
		return deleted;
	}

	/**
	 * Setter for property deleted.
	 *
	 * @param deleted
	 *            New value of property deleted.
	 */
	@Override
	public void setDeleted(int deleted) {
		this.deleted = deleted;
	}

	@Override
	public boolean getNeedsTarget() {
		return needsTarget;
	}

	@Override
	public void setNeedsTarget(boolean needsTarget) {
		this.needsTarget = needsTarget;
	}

	@Override
	public int getPriority(){
		return priority;
	}

	@Override
	public void setPriority(int priority){
		this.priority = priority;
	}

	@Override
	public boolean isPrioritizationAllowed() {
		return isPrioritizationAllowed;
	}

	@Override
	public void setPrioritizationAllowed(boolean prioritizationAllowed) {
		isPrioritizationAllowed = prioritizationAllowed;
	}

	@Override
	public int getOpenActionID() {
		return openActionID;
	}

	@Override
	public void setOpenActionID(int id) {
		this.openActionID = id;
	}

	@Override
	public int getClickActionID() {
		return clickActionID;
	}

	@Override
	public void setClickActionID(int id) {
		this.clickActionID = id;
	}

	public Date getChangeDate() {
		return changeDate;
	}

	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}

    @Override
    public List<EmmAction> getPossibleActions() {
        return possibleActions;
    }

    @Override
    public void setPossibleActions(List<EmmAction> possibleActions) {
        this.possibleActions = possibleActions;
    }

	@Override
	public boolean isFrequencyCounterDisabled() {
		return isFrequencyCounterDisabled;
	}

	@Override
	public void setFrequencyCounterDisabled(boolean isDisabled) {
		isFrequencyCounterDisabled = isDisabled;
	}

    /**
     * For testing purposes only
     *
     * @param linkService
     */
    public void setLinkService(LinkService linkService) {
        this.linkService = linkService;
    }

    // TODO Replace this method. Access to ApplicationContext is not a good practice.
    protected final LinkService getLinkService(ApplicationContext applicationContext) {
    	if (linkService == null) {
    		linkService = applicationContext.getBean("LinkService", LinkService.class);
    		
    		if(linkService == null) {
    			logger.error("Link service is still null!");
    			
    			throw new NullPointerException("Link service is still null");
    		}
    			
    	}
    	return linkService;
    }
}
