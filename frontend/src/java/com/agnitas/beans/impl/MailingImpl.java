/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans.impl;

import static com.agnitas.beans.impl.MailingComponentImpl.COMPONENT_NAME_MAX_LENGTH;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingContentType;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.TrackableLink;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.components.service.MailingComponentsService;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.linkcheck.service.LinkService.ErroneousLink;
import com.agnitas.emm.core.linkcheck.service.LinkService.LinkScanResult;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.trackablelinks.web.LinkScanResultToMessages;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.AgnDynTagGroupResolverFactory;
import com.agnitas.service.AgnTagService;
import com.agnitas.util.ImageUtils;
import com.agnitas.util.LinkUtils;
import com.agnitas.web.mvc.Popups;
import com.agnitas.emm.core.action.bean.EmmAction;
import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.MediaTypeStatus;
import com.agnitas.emm.common.FollowUpType;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.preview.AgnTagError;
import com.agnitas.preview.TagSyntaxChecker;
import com.agnitas.service.UserMessageException;
import com.agnitas.util.AgnTagUtils;
import com.agnitas.util.MailoutClient;
import com.agnitas.util.SafeString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

public class MailingImpl extends MailingBaseImpl implements Mailing {
	public static final String LINK_SWYN_PREFIX = "SWYN: ";

	private static final Logger logger = LogManager.getLogger(MailingImpl.class);

	protected MailingType mailingType;
	protected int mailTemplateID;
	protected int targetID;
	protected Map<String, DynamicTag> dynTags = new LinkedHashMap<>();
	protected Map<String, MailingComponent> components = new LinkedHashMap<>();
	protected Map<String, TrackableLink> trackableLinks = new LinkedHashMap<>();
	protected int clickActionID;
	protected int openActionID;
	protected Set<MaildropEntry> maildropStatus = new LinkedHashSet<>();
	protected Map<Integer, Mediatype> mediatypes = new LinkedHashMap<>();
	protected Date creationDate;
	protected Collection<Integer> targetGroups;
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
	private MailingComponentsService mailingComponentsService;

	// Default Value is non openers.
	protected String followUpType = FollowUpType.TYPE_FOLLOWUP_NON_OPENER.getKey();

	protected int splitID;
	
	private String statusmailRecipients;
	
	private boolean statusmailOnErrorOnly;

	private Integer clearanceThreshold;
	
	private String clearanceEmail;

	protected Date planDate;

	private List<MailingParameter> parameters;

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
	public boolean isLocked() {
		return locked;
	}

	@Deprecated
	/**
	  * TODO: use {@link com.agnitas.emm.core.components.service.MailingTriggerService}
	 */
	@Override
	public boolean triggerMailing(int maildropStatusID) {
		try {
			if (maildropStatusID <= 0) {
				logger.warn( "maildropStatisID is 0");
				return false;
			}
			
			// Interval Mailings are only triggered by an Jobqueue Worker
			if (getMailingType() != MailingType.INTERVAL) {
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

	@Override
	public Vector<String> scanForLinks(String text, String textModuleName, ApplicationContext applicationContext, String rdirDomain) throws Exception {
		return scanForLinks(text, textModuleName, applicationContext, null, null, null, rdirDomain);
	}

	@Override
	public Vector<String> scanForLinks(String text, String textModuleName, ApplicationContext applicationContext, List<Message> warnings, List<Message> errors, Admin admin, String rdirDomain) throws Exception {
		final int defaultTrackingMode = readDefaultLinkTrackingMode(applicationContext, companyID);
		
		try {
			if (text != null) {
				Vector<String> foundLinkUrls = new Vector<>();
				LinkScanResult linkScanResult = getLinkService(applicationContext).scanForLinks(text, id, mailinglistID, companyID);
				for (TrackableLink linkFound : linkScanResult.getTrackableLinks()) {
					if (linkFound.getActionID() > 0 && !isActionExist(linkFound.getActionID())) {
						if (warnings != null) {
							warnings.add(Message.of("warning.mailing.link.invalidActionID", textModuleName, linkFound.getActionID()));
						}
						linkFound.setActionID(0);
					}
					
					if (linkFound.getFullUrl().contains("/form.do?")) {
						if (warnings != null) {
							warnings.add(Message.of("warning.mailing.link.oldFormDoLink", textModuleName));
						}
					}

					if (!trackableLinks.containsKey(linkFound.getFullUrl())) {
						linkFound.setCompanyID(companyID);
						linkFound.setMailingID(id);
						linkFound.setUsage(defaultTrackingMode);

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
					Integer rdirLinkLineNumber = getLinkService(applicationContext).getLineNumberOfFirstRdirLink(rdirDomain, text);
					if (rdirLinkLineNumber != null) {
						warnings.add(Message.of("warning.mailing.link.encoded", textModuleName, rdirLinkLineNumber));
					}
					
					// Check for not measurable links
					if (linkScanResult.getNotTrackableLinks().size() > 0) {
						warnings.add(Message.of("warning.mailing.link.agntag", textModuleName, StringEscapeUtils.escapeHtml4(linkScanResult.getNotTrackableLinks().get(0))));
					}
					
					if (linkScanResult.getLocalLinks().size() > 0) {
						for (final ErroneousLink link : linkScanResult.getLocalLinks()) {
							warnings.add(Message.of("error.mailing.localLink",
									textModuleName,
									StringEscapeUtils.escapeHtml4(link.getLinkText())));
						}
					}

					LinkScanResultToMessages.linkWarningsToActionMessages(linkScanResult, warnings);
					
					// Check for not erroneous links
					if (linkScanResult.getErroneousLinks().size() > 0) {
						final String linkText = StringEscapeUtils.escapeHtml4(
								linkScanResult.getErroneousLinks().get(0).getLinkText());
						
						final String errorText = I18nString.getLocaleString(
								linkScanResult.getErroneousLinks().get(0).getErrorMessageKey(),
								admin.getLocale(),
								linkText
							);

						errors.add(Message.of("error.mailing.links",
								linkScanResult.getErroneousLinks().size(),
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

			errors.add(Message.of("error.invalid.link", SafeString.getLocaleString("error.in", locale)
					+ " " + textModuleName + "<br> " + message));
		} catch (Exception e) {
            logger.error("scanForLinks error in " + textModuleName + ": " + e.getMessage(), e);
            if (errors == null) {
                throw e;
            }

            Locale locale = admin.getLocale();
            if (e.getCause() instanceof LinkService.ErrorLinkStorage) {
                LinkService.ErrorLinkStorage linkError = (LinkService.ErrorLinkStorage) e.getCause();
                errors.add(Message.of("error.invalid.link",
                        String.format("%s %s<br> %s", SafeString.getLocaleString("error.in", locale), textModuleName, linkError.getErrorLink())));
            } else {
                errors.add(Message.of("error.invalid.link", SafeString.getLocaleString("error.in", locale)
                        + " " + textModuleName + "<br> " + e.getMessage()));
            }
        }
		
		return new Vector<>();
	}

	@Override
	public Vector<String> scanForLinks(ApplicationContext applicationContext) throws Exception {
        return scanForLinks(applicationContext, null, null, null);
    }

	private Vector<String> scanForLinks(ApplicationContext applicationContext, List<Message> messages, List<Message> errors, Admin admin) throws Exception {
		Vector<String> addedLinks = new Vector<>();
		
        String rdirDomain = getLinkService(applicationContext).getRdirDomain(companyID);

		for (MailingComponent component : components.values()) {
			if (component.getType() == MailingComponentType.Template) {
				addedLinks.addAll(scanForLinks(component.getEmmBlock(), component.getComponentName(), applicationContext, messages, errors, admin, rdirDomain));
			}
		}

		for (DynamicTag tag : dynTags.values()) {
			for (DynamicTagContent content : tag.getDynContent().values()) {
				addedLinks.addAll(scanForLinks(content.getDynContent(), tag.getDynName(), applicationContext, messages, errors, admin, rdirDomain));
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
	public Integer getClearanceThreshold() {
		return clearanceThreshold;
	}

	@Override
	public void setClearanceThreshold(Integer clearanceThreshold) {
		this.clearanceThreshold = clearanceThreshold;
	}

	@Override
	public String getClearanceEmail() {
		return clearanceEmail;
	}

	@Override
	public void setClearanceEmail(String clearanceEmail) {
		this.clearanceEmail = clearanceEmail;
	}

	@Override
	public String getFollowUpType() {
		return followUpType;
	}

	@Override
	public void setFollowUpType(String followUpType) {
		this.followUpType = followUpType;
	}

	private void setDefaultExtension(TrackableLink link, ApplicationContext con) {
		ConfigService configService = (ConfigService) con.getBean("ConfigService");
		String defaultExtensionString = configService.getValue(ConfigValue.DefaultLinkExtension, link.getCompanyID());
		if (StringUtils.isNotBlank(defaultExtensionString)) {
			LinkUtils.extendTrackableLink(link, defaultExtensionString);
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
			if (!link.isDeleted() && (link.getShortname() == null || !link.getShortname().startsWith(LINK_SWYN_PREFIX))) {
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
	public List<MailingParameter> getParameters() {
		return parameters;
	}

	@Override
	public void setParameters(List<MailingParameter> parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public Map<String, List<AgnTagError>> checkAgnTagSyntax(ApplicationContext applicationContext) {
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

	public Vector<String> scanForComponents(ApplicationContext con, int companyIDToCheckFor) throws Exception {
		Vector<String> addedTags = new Vector<>();
		Set<MailingComponent> componentsToAdd = new HashSet<>();
		Set<String> mediapoolImages = new HashSet<>();

        List<String> mailingHostedImages = getMailingHostedImages(con);

		for (Entry<String, MailingComponent> componentsEntry : components.entrySet()) {
			MailingComponent tmpComp = componentsEntry.getValue();
			if (tmpComp.getType() == MailingComponentType.Template) {
				addedTags.addAll(scanForComponents(tmpComp.getEmmBlock(), con, componentsToAdd, mediapoolImages, companyIDToCheckFor, mailingHostedImages));
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
					addedTags.addAll(scanForComponents(dyncontent.getDynContent(), con, componentsToAdd, mediapoolImages, companyIDToCheckFor, mailingHostedImages));
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
        mailingComponentsService.updateMailingMediapoolImagesReferences(getId(), getCompanyID(), mediapoolImages);

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
        if (!duplicates.isEmpty()) {
            Map<String, Integer> duplicateCounters = duplicates.stream().collect(Collectors.toMap(Function.identity(), v -> 1));

			components.values().stream()
					.filter(component -> component.getType() == MailingComponentType.Template)
					.forEach(component -> component.setEmmBlock(getLinkService(context).addNumbersToLinks(duplicateCounters, measuredSeparatelyLinks, component.getEmmBlock()), component.getMimeType()));

            dynTags.values().stream()
                    .flatMap(tag -> tag.getDynContent().values().stream())
                    .forEach(content -> content.setDynContent(getLinkService(context).addNumbersToLinks(duplicateCounters, measuredSeparatelyLinks, content.getDynContent())));
        }

        return measuredSeparatelyLinks;
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

	private Vector<String> scanForComponents(String text, ApplicationContext applicationContext, Set<MailingComponent> componentsToAdd, Set<String> mediapoolImages, int companyIDToCheckFor, List<String> mailingHostedImageNames) throws Exception {
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
                    if (foundComponent.getComponentName().contains("[agnDVALUE")) {
                        String name = AgnTagUtils.getAgnTagName(componentLinkString);
                        for (DynamicTagContent contentBlock : dynTags.get(name).getDynContent().values()) {
                            if (contentBlock.getTargetID() == 0) {
                                name = contentBlock.getDynContent().trim();
                            }
                        }
                        if (name.contains("[agn")) {
                            name = AgnTagUtils.getAgnTagName(name);
                            foundComponent = components.get(name);
                        } else {
                            foundComponent.setComponentName(name);
                            foundComponent.loadContentFromURL();
                            if (foundComponent.getMimeType().startsWith("image")) {
                                componentsToAdd.add(foundComponent);
                            }
                        }
                    } else if (!isGridMailing() && foundComponent.getComponentName().trim().startsWith("[agnIMAGE")) {
                        // Collect mediapool images and later save to mailing_ref_mediapool_tbl
                        // in order to check if they is still used by classic mailing while mediapool element deletion:
                        String imageName = AgnTagUtils.getAgnTagName(foundComponent.getComponentName());
                        if (StringUtils.isNotBlank(imageName) && !mailingHostedImageNames.contains(imageName)) {
                            mediapoolImages.add(imageName);
                        }
                    } else if (foundComponent.getComponentName().contains("[agn")) {
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
                    foundComponentUrls.add(foundComponent.getComponentName());
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
	public Map<String, TrackableLink> getTrackableLinks() {
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
	public MailingType getMailingType() {
		return mailingType;
	}

   	@Override
   	public int getMailingTypeCode() {
   		return mailingType == null ? 0 : mailingType.getCode();
   	}

	/**
	 * Setter for property mailingType.
	 *
	 * @param mailingType
	 *            New value of property mailingType.
	 */
	@Override
	public void setMailingType(MailingType mailingType) {
		this.mailingType = mailingType;
	}
	
	@Override
	public void setMailingTypeCode(int mailingTypeCode) {
		try {
			this.mailingType = MailingType.fromCode(mailingTypeCode);
		} catch (Exception e) {
			throw new RuntimeException("Invalid code for MailngType: " + mailingTypeCode);
		}
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

	@Override
	public boolean isEncryptedSend() {
		return getEmailParam() != null && getEmailParam().isEncryptedSend();
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
	 * @deprecated
	 */
	@Deprecated
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
	public void setTrackableLinks(Map<String, TrackableLink> trackableLinks) {
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
	 * @deprecated
	 */
	@Deprecated
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
	public void init(int newCompanyID, ApplicationContext con) {
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
	public boolean buildDependencies(boolean scanDynTags, ApplicationContext con) throws Exception {
		return buildDependencies(scanDynTags, null, con);
	}

	@Override
	public boolean buildDependencies(boolean scanDynTags, List<String> dynNamesForDeletion, ApplicationContext con) throws Exception {
		return buildDependencies(scanDynTags, dynNamesForDeletion, con, null, null, null);
	}

	@Override
	public boolean buildDependencies(Popups popups, boolean scanDynTags, List<String> dynNamesForDeletion, ApplicationContext con, Admin admin) throws Exception {
		List<Message> errors = new ArrayList<>();
		List<Message> warnings = new ArrayList<>();

		boolean result = buildDependencies(scanDynTags, dynNamesForDeletion, con, warnings, errors, admin);

		errors.forEach(popups::alert);
		warnings.forEach(popups::warning);

		return result;
	}

	@Override
	public boolean buildDependencies(boolean scanDynTags, List<String> dynNamesForDeletion, ApplicationContext con, List<Message> warnings, List<Message> errors, Admin admin) throws Exception {
		Vector<String> componentsToCheck = new Vector<>();

		// scan for Dyntags
		// in template-components and Mediatype-Params
		if (scanDynTags) {
			Vector<String> dynNamesInUse = new Vector<>(findDynTagsInTemplates(con));

			MediatypeEmail emailParam = getEmailParam();
			if (emailParam != null) {
				dynNamesInUse.addAll(findDynTagsInTemplates(emailParam.getSubject(), con));
				dynNamesInUse.addAll(findDynTagsInTemplates(emailParam.getPreHeader(), con));
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
			componentsToCheck.addAll(scanForComponents(con, companyID));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		cleanupMailingComponents(componentsToCheck);
        validateScannedComponents();

		// scan for Links
		// in template-components and dyncontent
		Vector<String> links = new Vector<>(scanForLinks(con, warnings, errors, admin));
		// if(ConfigService.isOracleDB()) {
		// causes problem with links in OpenEMM
		cleanupTrackableLinks(links);
		// }

		return true;
	}

    private void validateScannedComponents() throws UserMessageException {
        for (MailingComponent component : components.values()) {
            if (StringUtils.length(component.getComponentName()) > COMPONENT_NAME_MAX_LENGTH) {
                throw new UserMessageException(isImageComponent(component)
                        ? "error.image.name.tooLong"
                        : "error.compname.too.long", component.getComponentName());
            }
        }
    }

    private static boolean isImageComponent(MailingComponent component) {
        return List.of(MailingComponentType.Image, MailingComponentType.HostedImage).contains(component.getType());
    }

    private List<String> getMailingHostedImages(ApplicationContext con) {
        return getMailingComponentService(con)
                .getMailingComponents(getId(), getCompanyID(), MailingComponentType.HostedImage, false)
                .stream()
                .map(MailingComponent::getComponentName)
                .collect(Collectors.toList());
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
     */
    public void setLinkService(LinkService linkService) {
        this.linkService = linkService;
    }

    // TODO Replace this method. Access to ApplicationContext is not a good practice.
    protected final LinkService getLinkService(ApplicationContext applicationContext) {
    	if (linkService == null) {
    		linkService = applicationContext.getBean("LinkService", LinkService.class);
    	}
    	return linkService;
    }
    
    protected final MailingComponentsService getMailingComponentService(ApplicationContext applicationContext) {
    	if (mailingComponentsService == null) {
            mailingComponentsService = applicationContext.getBean("mailingComponentService", MailingComponentsService.class);
    	}
    	return mailingComponentsService;
    }

    private int readDefaultLinkTrackingMode(final ApplicationContext applicationContext, final int companyId) {
    	final ConfigService configService = applicationContext.getBean("ConfigService", ConfigService.class);
    	return configService.getIntegerValue(ConfigValue.TrackableLinkDefaultTracking, companyId);
    }
    
}
