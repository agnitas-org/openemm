/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;

import bsh.Interpreter;
import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.LinkService;
import com.agnitas.emm.core.LinkService.LinkScanResult;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;
import com.agnitas.service.AgnDynTagGroupResolverFactory;
import com.agnitas.service.AgnTagService;
import com.agnitas.web.ShowImageServlet;
import org.agnitas.actions.EmmAction;
import org.agnitas.backend.Mailgun;
import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.Mediatype;
import org.agnitas.beans.TrackableLink;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.preview.AgnTagException;
import org.agnitas.preview.TagSyntaxChecker;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.SafeString;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionMessages;
import org.springframework.context.ApplicationContext;

public class MailingImpl extends MailingBaseImpl implements Mailing {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailingImpl.class);

	protected int mailTemplateID;
	protected int targetID;
	protected Map<String, DynamicTag> dynTags = new LinkedHashMap<>();
	protected Map<String, MailingComponent> components = new LinkedHashMap<>();
	protected Hashtable<String, MailingComponent> attachments;
	protected Map<String, TrackableLink> trackableLinks = new LinkedHashMap<>();
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

    private LinkService linkService;

	/**
	 * mailingType can hold the values 0-3 0: Normal mailing 1: Action-Based 2:
	 * Date-Based 3: Followup Defined in Mailing.java eg. TYPE_NORMAL
	 */
	protected int mailingType;

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

		if (components == null)
			components = new HashMap<>();

		if (!components.containsKey(aComp.getComponentName())) {
			components.put(aComp.getComponentName(), aComp);
		}
	}

	private void addComponents( Set<MailingComponent> components) {
		for( MailingComponent component : components)
			addComponent( component);
	}

	@Override
	public void addAttachment(MailingComponent aComp) {

		if (attachments == null)
			attachments = new Hashtable<>();

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
		List<DynamicTag> tags = service.getDynTags(aTemplate, resolverFactory.create(id));

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
			if (component.getType() == MailingComponent.TYPE_TEMPLATE) {
				MediaTypes mediaType = MediaTypes.findMediatypeForComponentName(component.getComponentName());
				Mediatype type = mediaType != null ? getMediatypes().get(mediaType.getMediaCode()) : null;

				if (mediaType == null || type == null || type.getStatus() == Mediatype.STATUS_ACTIVE) {
					names.addAll(findDynTagsInTemplates(component.getEmmBlock(), con));
				}
			}
		}

		return names;
	}

	public Vector<String> scanForComponents(ApplicationContext con, int companyID, ActionMessages errors) throws Exception {
		Vector<String> addedTags = new Vector<>();

		Set<MailingComponent> componentsToAdd = new HashSet<>();

		for (Entry<String, MailingComponent> componentsEntry : components.entrySet()) {
			MailingComponent tmpComp = componentsEntry.getValue();
			if (tmpComp.getType() == MailingComponent.TYPE_TEMPLATE) {
				addedTags.addAll(scanForComponents(tmpComp.getEmmBlock(), con, componentsToAdd, companyID, errors));
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
					addedTags.addAll(scanForComponents(dyncontent.getDynContent(), con, componentsToAdd, companyID, errors));
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
	public Vector<String> scanForLinks(ApplicationContext con) throws Exception {
        return scanForLinks(con, null, null, null);
    }

	private Vector<String> scanForLinks(ApplicationContext con, ActionMessages messages, ActionMessages errors, ComAdmin admin) throws Exception {
		Vector<String> addedLinks = new Vector<>();

		for (MailingComponent component : components.values()) {
			if (component.getType() == MailingComponent.TYPE_TEMPLATE) {
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

	@Override
	public boolean replaceDuplicatedLinks(List<String> links, ApplicationContext context) throws Exception {
		Map<String, Integer> duplicateCounters = new HashMap<>();
		boolean hasDuplicates = false;
		List<String> existing = scanForLinks(context);
		for(String link : existing){
			if(links.contains(link)) {
				Integer counter = duplicateCounters.get(link);
				if (counter == null) {
					counter = 1;
				} else {
					counter++;
					hasDuplicates = true;
				}
				duplicateCounters.put(link, counter);
			}
		}
		if(hasDuplicates){
			Map<String, Integer> moreThanOne = new HashMap<>();
			for(Map.Entry<String, Integer> entry : duplicateCounters.entrySet()){
				if(entry.getValue() != null && entry.getValue() > 1){
					moreThanOne.put(entry.getKey(), 1);
				}
			}

			LinkService linkService = getLinkService(context);
			components.values().stream()
					.filter((component) -> component.getType() == MailingComponent.TYPE_TEMPLATE)
					.forEach((component) -> component.setEmmBlock(addNumbersToLinks(moreThanOne, component.getEmmBlock(), linkService), component.getMimeType()));

			dynTags.values().stream()
					.flatMap((tag) -> tag.getDynContent().values().stream())
					.forEach((content) -> content.setDynContent(addNumbersToLinks(moreThanOne, content.getDynContent(), linkService)));
		}

		return hasDuplicates;
	}

	protected String addNumbersToLinks(Map<String, Integer> linksCounters, String original, LinkService linkService){
		StringBuilder changed = new StringBuilder(original);
		final int[] accumulatedDiff = {0};
		linkService.findAllLinks(this.companyID, original, (start, end) -> {
			String toReplace = original.substring(start, end);
			Integer count = linksCounters.get(original.substring(start, end));
			if(count != null){
				String linkNumber = "#"+count;
				changed.insert(end + accumulatedDiff[0], linkNumber);
				accumulatedDiff[0] += linkNumber.length();
				linksCounters.put(toReplace, count + 1);
			}
		});
		return changed.toString();
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

	private Vector<String> scanForComponents(String text, ApplicationContext applicationContext, Set<MailingComponent> componentsToAdd, int companyID, ActionMessages errors) throws Exception {
		Vector<String> foundComponentUrls = new Vector<>();
		LinkScanResult linkScanResult = getLinkService(applicationContext).scanForLinks(text, companyID);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (String componentLinkString : linkScanResult.getImageLinks()) {
            executorService.execute(() -> {
                MailingComponent foundComponent = (MailingComponent) applicationContext.getBean("MailingComponent");
                foundComponent.setCompanyID(companyID);
                foundComponent.setMailingID(id);
                foundComponent.setComponentName(componentLinkString);
                foundComponent.setType(MailingComponent.TYPE_IMAGE);

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

	@Override
	public Vector<String> scanForLinks(String aText1, String textModuleName, ApplicationContext con, ActionMessages messages, ActionMessages errors) throws Exception {
		throw new NotImplementedException();
	}

	@Override
	public Vector<String> scanForLinks(String aText1, String textModuleName, ApplicationContext con, ActionMessages messages, ActionMessages errors, ComAdmin admin) throws Exception {
		throw new NotImplementedException();
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
	public boolean triggerMailing(int maildropStatusID, Map<String, Object> opts, ApplicationContext con) {
		Mailgun aMailgun = null;
		DataSource ds = (DataSource) con.getBean("dataSource");
		boolean exitValue = true;

		try(final Connection dbCon = ds.getConnection()) {
			if (maildropStatusID == 0) {
				logger.warn( "maildropStatisID is 0");

				throw new Exception("maildropStatusID is 0");
			}
			aMailgun = (Mailgun) con.getBean("Mailgun");
			aMailgun.initialize(Integer.toString(maildropStatusID));
			aMailgun.prepare(new Hashtable<>());
			aMailgun.execute(opts);
		} catch (Exception e) {
			logger.error("triggerMailing", e);
			exitValue = false;
		}

		return exitValue;
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

	@Override
	public String getPreview(String input, int inputType, int customerID, ApplicationContext con) throws Exception {
		return getPreview(input, inputType, customerID, false, con);
	}

	@Override
	public String getPreview(String input, int inputType, int customerID, boolean overwriteMailtype, ApplicationContext con) throws Exception {
		AgnTagService agnTagService = con.getBean("AgnTagService", AgnTagService.class);

		// Resolve non-dynamic agn-tags at first.
		StringBuilder output = new StringBuilder(agnTagService.resolveTags(input, companyID, id, mailinglistID, customerID));
		TargetGroupMatcher targetGroupMatcher = new TargetGroupMatcher(con, companyID, customerID, overwriteMailtype, inputType);

		Map<String, String> contentMap = new HashMap<>();
		for (DynamicTag tag : dynTags.values()) {
			String content = null;

			Map<Integer, DynamicTagContent> dynamicTagContentMap = tag.getDynContent();
			if (dynamicTagContentMap != null) {
				content = dynamicTagContentMap.values()
					.stream()
					.sorted(Comparator.comparingInt(DynamicTagContent::getDynOrder))
					.filter(e -> targetGroupMatcher.matches(e.getTargetID()))
					.map(DynamicTagContent::getDynContent)
					.findFirst()
					.orElse(null);
				content = agnTagService.resolveTags(content, companyID, id, mailinglistID, customerID);
			}

			contentMap.put(tag.getDynName(), content);
		}

		List<DynamicTag> tags = agnTagService.getDynTags(output.toString());
		while (tags.size() > 0) {
			for (DynamicTag tag : tags) {
				tag.setMailingID(id);
				tag.setCompanyID(companyID);

				DynamicTag existingTag = dynTags.get(tag.getDynName());
				if (existingTag != null) {
					tag.setDynInterestGroup(existingTag.getDynInterestGroup());
				}
				addDynamicTag(tag);
			}

			Map<DynamicTag, DynamicTag> reorderMap = getTagsReorderMap(tags, customerID, con);

			// Replace tags from right to left in order to prevent positions invalidation.
			Collections.reverse(tags);

			for (DynamicTag tag : tags) {
				String name = reorderMap.getOrDefault(tag, tag).getDynName();

				if (contentMap.containsKey(name)) {
					replaceTag(output, tag, contentMap.get(name));
				} else {
					// Cannot resolve dynamic tag.
					List<String[]> errorReports = Collections.singletonList(new String[] {"", name, ""});
					throw new AgnTagException("error.template.dyntags.unknown", errorReports);
				}
			}

			tags = agnTagService.getDynTags(output.toString());
		}

		String preview = output.toString();

		if (inputType == Mailing.INPUT_TYPE_TEXT) {
			preview = SafeString.removeHTMLTags(preview);
			if (getEmailParam().getLinefeed() > 0) {
				preview = SafeString.cutLineLength(preview, getEmailParam().getLinefeed());
			}
		}

		return insertTrackableLinks(preview, customerID, con);
	}

	private void replaceTag(StringBuilder content, DynamicTag tag, String value) {
		// Replace or erase tag.
		if (StringUtils.isEmpty(value)) {
			// Erase tag (no suitable content available).
			if (tag.isStandaloneTag()) {
				content.delete(tag.getStartTagStart(), tag.getStartTagEnd());
			} else {
				content.delete(tag.getStartTagStart(), tag.getEndTagEnd());
			}
		} else {
			// Replace tag with resolved content.
			if (tag.isStandaloneTag()) {
				content.replace(tag.getStartTagStart(), tag.getStartTagEnd(), value);
			} else {
				content.delete(tag.getEndTagStart(), tag.getEndTagEnd());
				content.replace(tag.getValueTagStart(), tag.getValueTagEnd(), value);
				content.delete(tag.getStartTagStart(), tag.getStartTagEnd());
			}
		}
	}

	protected Map<DynamicTag, DynamicTag> getTagsReorderMap(List<DynamicTag> tags, int customerId, ApplicationContext con) {
		return Collections.emptyMap();
	}

	/**
	 * Scans a textblock for trackable links and replaces them with encoded
	 * rdir-links.
	 */
	public String insertTrackableLinks(String aText1, int customerID, ApplicationContext con) {
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
		 * in: http://rdir.de/r.html?uid=<uid of Link1>/path/index.htm
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
					LinkService linkService = (LinkService) con.getBean("LinkService");
					TrackableLink trackableLink = trackableLinks.get(aLink);
					sb.replace(start_link, start_link + aLink.length(), linkService.encodeTagStringLinkTracking(trackableLink.getCompanyID(), trackableLink.getMailingID(), trackableLink.getId(), customerID));
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
	public MailingComponent getHtmlTemplate() {
		return getTemplate("Html");
	}

	@Override
	public MailingComponent getTextTemplate() {
		return getTemplate("Text");
	}

	@Override
	public boolean hasComplexTargetExpression() {
		if (targetExpression != null) {
			return (targetExpression.contains("&") && targetExpression.contains("|")) || StringUtils.containsAny(targetExpression, "()!");
		}
		return false;
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
	public void init( @VelocityCheck int companyID, ApplicationContext con) {
		MailingComponent comp = null;
		Mediatype type = null;

		this.companyID = companyID;

		comp = (MailingComponent) con.getBean("MailingComponent");
		comp.setCompanyID(companyID);
		comp.setComponentName("agnText");
		comp.setType(MailingComponent.TYPE_TEMPLATE);
		comp.setEmmBlock("[agnDYN name=\"Text-Version\"/]", "text/plain");
		components.put("agnText", comp);

		comp = (MailingComponent) con.getBean("MailingComponent");
		comp.setCompanyID(companyID);
		comp.setComponentName("agnHtml");
		comp.setType(MailingComponent.TYPE_TEMPLATE);
		comp.setEmmBlock("[agnDYN name=\"HTML-Version\"/]", "text/html");
		components.put("agnHtml", comp);

		type = (Mediatype) con.getBean("MediatypeEmail");
		type.setCompanyID(companyID);
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
			if (baseComponentName.startsWith(ShowImageServlet.MOBILE_IMAGE_PREFIX)) {
				baseComponentName = baseComponentName.substring(ShowImageServlet.MOBILE_IMAGE_PREFIX.length());
			}

			if ((mailingComponent.getType() == MailingComponent.TYPE_IMAGE
						|| mailingComponent.getType() == MailingComponent.TYPE_HOSTED_IMAGE)
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
	public Object clone(ApplicationContext con) {
		Mailing tmpMailing = (Mailing) con.getBean("Mailing");
		MailingComponent compNew = null;
		TrackableLink linkNew = null;
		DynamicTag tagNew = null;
		DynamicTagContent contentNew = null;

		try {
			ConvertUtils.register(new DateConverter(null), Date.class);
			// copy components
			for (MailingComponent compOrg : components.values()) {
				compNew = (MailingComponent) con.getBean("MailingComponent");
				BeanUtils.copyProperties(compNew, compOrg);
				if (compOrg.getBinaryBlock() != null) {
					compNew.setBinaryBlock(compOrg.getBinaryBlock(), compOrg.getMimeType());
				} else {
					compNew.setEmmBlock(compOrg.getEmmBlock(), compOrg.getMimeType());
				}
				compNew.setId(0);
				compNew.setMailingID(0);
				tmpMailing.addComponent(compNew);
			}

			// copy dyntags
			for (DynamicTag tagOrg : dynTags.values()) {
				tagNew = (DynamicTag) con.getBean("DynamicTag");
				for (DynamicTagContent contentOrg : tagOrg.getDynContent().values()) {
					contentNew = (DynamicTagContent) con.getBean("DynamicTagContent");
					BeanUtils.copyProperties(contentNew, contentOrg);
					contentNew.setId(0);
					contentNew.setDynNameID(0);
					tagNew.addContent(contentNew);
				}
				tagNew.setCompanyID(tagOrg.getCompanyID());
				tagNew.setDynName(tagOrg.getDynName());
				tagNew.setDisableLinkExtension(tagOrg.isDisableLinkExtension());
				tmpMailing.addDynamicTag(tagNew);
			}

			// copy urls
			for (TrackableLink linkOrg : trackableLinks.values()) {
				linkNew = (TrackableLink) con.getBean("TrackableLink");
				BeanUtils.copyProperties(linkNew, linkOrg);
				linkNew.setId(0);
				linkNew.setMailingID(0);
				linkNew.setActionID(linkOrg.getActionID());
				tmpMailing.getTrackableLinks().put(linkNew.getFullUrl(), linkNew);
			}

			// copy active media types
			for (Entry<Integer, Mediatype> entry : mediatypes.entrySet()) {
				Mediatype mediatype = entry.getValue();
				if (mediatype.getStatus() == Mediatype.STATUS_ACTIVE) {
					Mediatype mediatypeCopy = mediatype.copy();
					tmpMailing.getMediatypes().put(entry.getKey(), mediatypeCopy);
				}
			}

			tmpMailing.setOpenActionID(openActionID);
			tmpMailing.setClickActionID(clickActionID);

			return tmpMailing;
		} catch (Exception e) {
			logger.error("could not copy", e);
			return null;
		}
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
		Vector<String> dynTags = new Vector<>();
		Vector<String> components = new Vector<>();
		Vector<String> links = new Vector<>();

		// scan for Dyntags
		// in template-components and Mediatype-Params
		if (scanDynTags) {
			dynTags.addAll(findDynTagsInTemplates(con));
			MediatypeEmail emailParam = getEmailParam();
			if (emailParam != null) {
				dynTags.addAll(findDynTagsInTemplates(emailParam.getSubject(), con));
				dynTags.addAll(findDynTagsInTemplates(emailParam.getReplyAdr(), con));
				dynTags.addAll(findDynTagsInTemplates(emailParam.getFromAdr(), con));
			}
			List<String> excludedDynNames = cleanupDynTags(dynTags);

			if (dynNamesForDeletion != null) {
				dynNamesForDeletion.addAll(excludedDynNames);
			}
		}
		// scan for Components
		// in template-components and dyncontent
		try {
			components.addAll(scanForComponents(con, companyID, errors));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		cleanupMailingComponents(components);

		// scan for Links
		// in template-components and dyncontent
		links.addAll(scanForLinks(con, messages, errors, admin));
		// if(ConfigService.isOracleDB()) {
		// causes problem with links in OpenEMM
		cleanupTrackableLinks(links);
		// }

		return true;
	}

	@Override
	public Map<Integer, ComTarget> getAllowedTargets(ApplicationContext myContext) {
		if (allowedTargets == null) {
			ComTargetDao dao = (ComTargetDao) myContext.getBean("TargetDao");

			allowedTargets = dao.getAllowedTargets(companyID);
			if (allowedTargets != null) {
				ComTarget aTarget = (ComTarget) myContext.getBean("Target");

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
	public int getLocked() {
		return locked ? 1 : 0;
	}

	@Override
	public void setLocked(int locked) {
		this.locked = (locked != 0);
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
	public int getArchived() {
		return archived ? 1 : 0;
	}

	@Override
	public void setArchived(int archived) {
		this.archived = (archived != 0);
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

    /**
     * For testing purposes only
     *
     * @param linkService
     */
    public void setLinkService(LinkService linkService) {
        this.linkService = linkService;
    }

    protected LinkService getLinkService(ApplicationContext applicationContext) {
    	if (linkService == null) {
    		linkService = (LinkService) applicationContext.getBean("LinkService");
    	}
    	return linkService;
    }

	private static class TargetGroupMatcher {
    	private ApplicationContext context;
		private int companyId;

		private ComTargetDao targetDao;
		private Interpreter interpreter;

		private Map<Integer, ComTarget> targetsCache = new HashMap<>();

		public TargetGroupMatcher(ApplicationContext context, int companyId, int customerId, boolean overwriteMailtype, int inputType) throws Exception {
			this.context = context;
			this.companyId = companyId;

			targetDao = context.getBean("TargetDao", ComTargetDao.class);

			interpreter = AgnUtils.getBshInterpreter(companyId, customerId, context);
			if (interpreter == null) {
				throw new Exception("error.template.dyntags.bshInterpreter");
			}
			if (overwriteMailtype) {
				interpreter.set("mailtype", new Integer(inputType));
			}
		}

    	public boolean matches(int targetId) {
			if (targetId == 0) {
				return true;
			} else {
				ComTarget target = targetsCache.computeIfAbsent(targetId, this::getTarget);

				if (target.isCustomerInGroup(interpreter)) {
					return true;
				}
			}
			return false;
		}

		private ComTarget getTarget(int targetId) {
			ComTarget value = targetDao.getTarget(targetId, companyId);
			if (value == null) {
				value = (ComTarget) context.getBean("Target");
				value.setCompanyID(companyId);
				value.setId(targetId);
			}
			return value;
		}
	}
}
