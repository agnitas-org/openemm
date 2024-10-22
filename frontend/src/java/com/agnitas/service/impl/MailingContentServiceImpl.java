/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.TrackableLink;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailingcontent.dto.ContentBlockAndMailingMetaData;
import com.agnitas.emm.core.mailingcontent.dto.DynContentDto;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.emm.core.mailingcontent.validator.DynTagChainValidator;
import com.agnitas.emm.core.trackablelinks.exceptions.DependentTrackableLinkException;
import com.agnitas.messages.Message;
import com.agnitas.service.AgnTagService;
import com.agnitas.service.MailingContentService;
import com.agnitas.service.ServiceResult;
import com.agnitas.util.Tag;
import com.agnitas.util.Tag.TagType;
import com.agnitas.web.mvc.Popups;
import org.agnitas.actions.EmmAction;
import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.factory.DynamicTagContentFactory;
import org.agnitas.beans.factory.DynamicTagFactory;
import org.agnitas.dao.EmmActionDao;
import org.agnitas.dao.MailingStatus;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.UserMessageException;
import org.agnitas.util.AgnTagUtils;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DynTagException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MailingContentServiceImpl implements MailingContentService, ApplicationContextAware {
    private static final Logger logger = LogManager.getLogger(MailingContentServiceImpl.class);

    protected static final int LINK_TEXT_MAX_LENGTH = 75;
    protected static final Pattern MULTIPLE_WHITESPACES_PATTERN = Pattern.compile("[\\s\u00A0]{2,}");

    protected ApplicationContext applicationContext;
    
    protected final ExtendedDefaultConversionService conversionService;
	protected final DynamicTagContentFactory dynamicTagContentFactory;
    protected final DynamicTagFactory dynamicTagFactory;
    protected final ComMailingBaseService mailingBaseService;
    protected final AgnTagService agnTagService;
    protected final DynamicTagDao dynamicTagDao;
    protected final MailingDao mailingDao;
    protected final EmmActionDao actionDao;
    protected final LinkService linkService;
    protected final MailingService mailingService;
    protected final UserActivityLogService userActivityLogService;
    private final DynTagChainValidator dynTagChainValidator;
    
    public MailingContentServiceImpl(
            ExtendedDefaultConversionService conversionService,
            DynamicTagContentFactory dynamicTagContentFactory,
            DynamicTagFactory dynamicTagFactory,
            ComMailingBaseService mailingBaseService,
            AgnTagService agnTagService,
            DynamicTagDao dynamicTagDao,
            MailingDao mailingDao,
            EmmActionDao actionDao,
            LinkService linkService,
            MailingService mailingService,
            UserActivityLogService userActivityLogService,
            DynTagChainValidator dynTagChainValidator) {
        this.conversionService = conversionService;
		this.dynamicTagContentFactory = dynamicTagContentFactory;
		this.dynamicTagFactory = dynamicTagFactory;
		this.mailingBaseService = mailingBaseService;
		this.agnTagService = agnTagService;
		this.dynamicTagDao = dynamicTagDao;
        this.mailingDao = mailingDao;
        this.actionDao = actionDao;
        this.linkService = linkService;
        this.mailingService = mailingService;
        this.userActivityLogService = userActivityLogService;
        this.dynTagChainValidator = dynTagChainValidator;
    }

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

    @Override
    public Set<String> findDynNamesUsedInContent(String content, List<DynamicTag> dynamicTags) {
        List<DynTagDto> dynTags = conversionService.convert(dynamicTags, DynamicTag.class, DynTagDto.class);
        return findDynNamesInContent(content, dynTags);
    }

    protected Set<String> findDynNamesInContent(String content, List<DynTagDto> dynTags) {
        if (StringUtils.isBlank(content)) {
            return Collections.emptySet();
        }

        Set<String> result = new HashSet<>();
        try {
            Set<String> dynNames = agnTagService.getDynTags(content)
                    .stream()
                    .map(DynamicTag::getDynName)
                    .collect(Collectors.toSet());

            result.addAll(dynNames);
            dynNames.forEach(dName -> collectDynNamesInsideTheDynContent(dName, dynTags, result));
        } catch (DynTagException e) {
            logger.error("Error occurred while getting dyn tags from content!", e);
        }

        return result;
    }

    private void collectDynNamesInsideTheDynContent(String dynName, Collection<DynTagDto> dynTags, Set<String> foundNames) {
        Optional<DynTagDto> dynTag = dynTags.stream().filter(dt -> dt.getName().equals(dynName)).findAny();
        if (dynTag.isEmpty()) {
            return;
        }

        for (DynContentDto contentBlock : dynTag.get().getContentBlocks()) {
            try {
                List<String> dynNames = agnTagService.getDynTags(contentBlock.getContent())
                        .stream()
                        .map(DynamicTag::getDynName)
                        .filter(n -> !foundNames.contains(n))
                        .collect(Collectors.toList());

                foundNames.addAll(dynNames);
                dynNames.forEach(dName -> collectDynNamesInsideTheDynContent(dName, dynTags, foundNames));
            } catch (DynTagException e) {
                logger.error(String.format("Error occurred while getting dyn tags inside the dyn content. dynName = '%s'", dynName), e);
            }
        }
    }

    @Override
    public void generateTextContent(Mailing mailing) {
        Objects.requireNonNull(mailing, "mailing == null");

        MailingComponent html = mailing.getHtmlTemplate();
        MailingComponent text = mailing.getTextTemplate();

        Objects.requireNonNull(html, "html template is required");
        Objects.requireNonNull(text, "text template is required");

        Map<String, DynamicTag> tags = mailing.getDynTags();

        Function<String, DynamicTag> getSourceTag = tags::get;
        Function<String, DynamicTag> getTargetTag = name -> {
            DynamicTag tag = tags.get(name);

            if (tag == null) {
                tag = dynamicTagFactory.newDynamicTag();

                tag.setDynName(name);
                tags.put(name, tag);

                return tag;
            }

            // Reuse existing tag.
            clearDynTag(mailing, tag);

            return tag;
        };

        // Generate text template out of html template.
        text.setEmmBlock(generateTextContentTemplate(html.getEmmBlock(), getSourceTag, getTargetTag), "text/plain");
    }

    @Override
    public String generateDynName(String sourceName, Set<String> namesInUse) {
        if (AgnUtils.DEFAULT_MAILING_HTML_DYNNAME.equals(sourceName)) {
            return AgnUtils.DEFAULT_MAILING_TEXT_DYNNAME;
        } else {
            String newName = sourceName + " (Text)";

            if (namesInUse.contains(newName)) {
                int index = 1;

                while (namesInUse.contains(newName + "-" + index)) {
                    index++;
                }

                return newName + "-" + index;
            }

            return newName;
        }
    }

    @Override
    public boolean isGenerationAvailable(Mailing mailing) {
        if (mailing.getHtmlTemplate() == null || mailing.getTextTemplate() == null) {
            return false;
        }

        MailingComponent template = mailing.getHtmlTemplate();

        return !mailingBaseService.isContentBlank(template.getEmmBlock(), mailing.getDynTags());
    }

    @Override
    public void saveDynTags(int mailingId, List<DynTagDto> dynTags, Admin admin, Popups popups) {
        Mailing mailing = mailingService.getMailing(admin.getCompanyID(), mailingId);
        trySaveDynTags(mailing, dynTags, admin, popups);
    }

    private void trySaveDynTags(Mailing mailing, List<DynTagDto> dynTags, Admin admin, Popups popups) {
        try {
            saveDynTags(mailing, dynTags, admin, popups);
        } catch (UserMessageException e) {
            popups.alert(e.getErrorMessageKey(), e.getAdditionalErrorData());
        } catch (Exception e) {
            String mailingName = mailing.getShortname();
            logger.error("Error during mailing (id: {}, name: {}) content save", mailingName, mailing.getId(), e);
            popups.alert("error.mailing.save", mailingName);
        }
    }

    private void saveDynTags(Mailing mailing, List<DynTagDto> dynTags, Admin admin, Popups popups) throws Exception {
        List<UserAction> userActions = updateMailingDynTags(mailing, dynTags, admin, popups);
        if (popups.hasAlertPopups()) {
            return;
        }
        mailingBaseService.saveUndoData(mailing.getId(), admin.getAdminID());
        mailingService.saveMailingWithNewContent(mailing, hasNoCleanPermission(admin));
        mailingDao.updateStatus(mailing.getCompanyID(), mailing.getId(), MailingStatus.EDIT, null);
        mailingService.removeApproval(mailing.getId(), admin);
        userActions.forEach(ua -> userActivityLogService.writeUserActivityLog(admin, ua));
        logger.info("Content of mailing (id = {}, name = {}) was changed", mailing.getId(), mailing.getShortname());
    }

    private List<UserAction> updateMailingDynTags(Mailing mailing, List<DynTagDto> dynTags,
                                                  Admin admin, Popups popups) throws Exception {
        if (!dynTags.stream().allMatch(dynTag -> dynTagChainValidator.validate(dynTag, popups, admin))) {
            return Collections.emptyList();
        }

        if (!validateAllowedChars(mailing, dynTags, popups)) {
            return Collections.emptyList();
        }

        final List<Message> errors = new ArrayList<>();
        final ArrayList<String> dynNamesForDeletion = new ArrayList<>();

        Collection<TrackableLink> oldLinks = new ArrayList<>(mailing.getTrackableLinks().values());
        List<UserAction> userActions = replaceDynTags(mailing, dynTags);
        mailing.setPossibleActions(actionDao.getEmmActions(admin.getCompanyID()));
        mailing.buildDependencies(true, dynNamesForDeletion, applicationContext, null, errors, admin);
        isValidChangedLinks(oldLinks, mailing.getTrackableLinks().values(), errors);
        if (Mediatype.isActive(mailing.getEmailParam())) {
            mailingBaseService.doTextTemplateFilling(mailing, admin, popups);
        }

        if (!popups.hasAlertPopups()) {
            dynamicTagDao.markNamesAsDeleted(mailing.getId(), dynNamesForDeletion);
        }

        errors.forEach(popups::alert);
        return popups.hasAlertPopups() ? Collections.emptyList() : userActions;
    }

    protected boolean validateAllowedChars(Mailing mailing, List<DynTagDto> dynTags, Popups popups) {
        return true;
    }

    private boolean hasNoCleanPermission(Admin admin) {
        return admin.permissionAllowed(Permission.MAILING_TRACKABLELINKS_NOCLEANUP);
    }

    private List<UserAction> replaceDynTags(Mailing mailing, List<DynTagDto> dynTags) {
        return dynTags.stream()
                .flatMap(dynTag -> replaceDynTag(mailing, dynTag).stream())
                .collect(Collectors.toList());
    }

    private List<UserAction> replaceDynTag(Mailing mailing, DynTagDto dynTag) {
        DynamicTag oldDynTag = mailing.getDynamicTagById(dynTag.getId());
        DynamicTag newDynTag = convertDynTagDtoToDynamicTag(mailing.getCompanyID(), oldDynTag, dynTag);
        mailing.getDynTags().replace(newDynTag.getDynName(), newDynTag);
        dynamicTagDao.removeAbsentDynContent(oldDynTag, newDynTag);
        return getUserActions(oldDynTag, newDynTag, mailing);
    }

    // TODO: EMMGUI-714: remove when old design will be removed
    @Override
    public ServiceResult<List<UserAction>> updateDynContent(Mailing mailing, DynTagDto dynTagDto, Admin admin, Popups popups) throws Exception {
        DynamicTag oldDynamicTag = mailing.getDynamicTagById(dynTagDto.getId());
        DynamicTag newDynamicTag = convertDynTagDtoToDynamicTag(admin.getCompanyID(), oldDynamicTag, dynTagDto);

        Collection<TrackableLink> oldLinks = new ArrayList<>(mailing.getTrackableLinks().values());
        mailing.getDynTags().replace(newDynamicTag.getDynName(), newDynamicTag);
        List<EmmAction> actions = actionDao.getEmmActions(admin.getCompanyID());
        mailing.setPossibleActions(actions);
        final List<Message> errors = new ArrayList<>();
        final ArrayList<String> dynNamesForDeletion = new ArrayList<>();
        mailing.buildDependencies(true, dynNamesForDeletion, applicationContext, null, errors, admin);

        isValidChangedLinks(oldLinks, mailing.getTrackableLinks().values(), errors);

        if (!errors.isEmpty()) {
            return ServiceResult.error(errors);
        }
        if (Mediatype.isActive(mailing.getEmailParam())) {
            mailingBaseService.doTextTemplateFilling(mailing, admin, popups);
        }

        dynamicTagDao.markNamesAsDeleted(mailing.getId(), dynNamesForDeletion);

        boolean hasNoCleanPermission = admin.permissionAllowed(Permission.MAILING_TRACKABLELINKS_NOCLEANUP);
        mailingBaseService.saveUndoData(mailing.getId(), admin.getAdminID());
        mailingService.saveMailingWithNewContent(mailing, hasNoCleanPermission);
        mailingDao.updateStatus(mailing.getCompanyID(), mailing.getId(), MailingStatus.EDIT, null);
        mailingService.removeApproval(mailing.getId(), admin);

        List<UserAction> userActions = getUserActions(oldDynamicTag, newDynamicTag, mailing);
        dynamicTagDao.removeAbsentDynContent(oldDynamicTag, newDynamicTag);
        return ServiceResult.success(userActions);
    }

    @Override
    public DynTagDto getDynTag(int companyId, int dynNameId) {
        DynamicTag tag = mailingBaseService.getDynamicTag(companyId, dynNameId);

        if (tag == null) {
            return null;
        }

        return conversionService.convert(tag, DynTagDto.class);
    }

	@Override
	public void buildDependencies(int mailingID, int companyID) throws Exception {
		Mailing storedMailing = mailingDao.getMailing(mailingID, companyID);
		storedMailing.buildDependencies(true, applicationContext);
        mailingService.saveMailingWithNewContent(storedMailing, false);
	}

	@Override
	public final List<ContentBlockAndMailingMetaData> listContentBlocksUsingTargetGroup(final ComTarget target) {
		return this.dynamicTagDao.listContentBlocksUsingTargetGroup(target.getId(), target.getCompanyID());
	}
    
    private String generateTextContentTemplate(String html, Function<String, DynamicTag> getSourceTag, Function<String, DynamicTag> getTargetTag) {
        String text = generateTextContent(html);
        List<DynamicTag> mentionedTags = getDynamicTags(text);

        if (mentionedTags.isEmpty()) {
            return text;
        }

        // Source dyn name -> target dyn name (html block -> text block).
        Map<String, String> namesMap = generateDynNamesMap(mentionedTags);

        generateTextContentBlocks(namesMap, getSourceTag, getTargetTag);

        return eraseExtraWhitespaces(replaceTags(text, asReplaceableTags(mentionedTags, namesMap)));
    }

    private void generateTextContentBlocks(Map<String, String> namesMap, Function<String, DynamicTag> getSourceTag, Function<String, DynamicTag> getTargetTag) {
        for (Map.Entry<String, String> entry : namesMap.entrySet()) {
            String sourceName = entry.getKey();
            String targetName = entry.getValue();

            DynamicTag sourceTag = getSourceTag.apply(sourceName);

            if (sourceTag != null) {
                DynamicTag targetTag = getTargetTag.apply(targetName);

                for (DynamicTagContent sourceContent : sourceTag.getDynContent().values()) {
                    DynamicTagContent targetContent = dynamicTagContentFactory.newDynamicTagContent();

                    targetContent.setDynName(targetName);
                    targetContent.setDynOrder(sourceContent.getDynOrder());
                    targetContent.setTargetID(sourceContent.getTargetID());
                    targetContent.setDynContent(generateTextContent(sourceContent.getDynContent(), namesMap));

                    targetTag.addContent(targetContent);
                }
            }
        }
    }

    private String generateTextContent(String html, Map<String, String> namesMap) {
        String text = generateTextContent(html);
        List<DynamicTag> tags = getDynamicTags(text);

        if (tags.isEmpty()) {
            return text;
        } else {
            return replaceTags(text, asReplaceableTags(tags, namesMap));
        }
    }

    private Map<String, String> generateDynNamesMap(List<DynamicTag> tags) {
        Set<String> names = tags.stream().map(DynamicTag::getDynName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Maps source names (html version) to target names (text version).
        Map<String, String> namesMap = new LinkedHashMap<>();

        // Generate target names for all the mentioned dyn tags.
        for (String sourceName : new ArrayList<>(names)) {
            namesMap.put(sourceName, generateDynName(sourceName, names));
        }

        return namesMap;
    }

    private String generateTextContent(String html) {
        if (StringUtils.isBlank(html)) {
            return StringUtils.EMPTY;
        }

        Document document = Jsoup.parse(AgnTagUtils.escapeAgnTags(html));

        StringBuilder sb = new StringBuilder();
        generateTextContent(sb, document.childNodes());
        return sb.toString();
    }

    private String eraseExtraWhitespaces(String content) {
        Matcher matcher = MULTIPLE_WHITESPACES_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String spaces = matcher.group();

            if (spaces.indexOf('\n') >= 0) {
                // Erase line's trailing and leading whitespaces.
                // Collapse blank lines (multiple newline characters become a single one).
                matcher.appendReplacement(sb, "\n");
            } else {
                // Ignore document's leading and trailing whitespaces.
                if (sb.length() > 0 && !matcher.hitEnd()) {
                    matcher.appendReplacement(sb, spaces);
                }
            }
        }

        matcher.appendTail(sb);

        return sb.toString();
    }

    private void generateTextContent(StringBuilder sb, List<Node> nodes) {
        for (Node node : nodes) {
            if (node instanceof Element) {
                Element element = (Element) node;

                switch (element.nodeName()) {
                    case "a":
                        sb.append(getTextLink(element));
                        break;

                    case "br":
                        sb.append('\n');
                        break;

                    default:
                        generateTextContent(sb, element.childNodes());
                        break;
                }
            } else if (node instanceof TextNode) {
                sb.append(((TextNode) node).getWholeText());
            }
        }
    }

    private String getTextLink(Element element) {
        String address = element.attr("href");
        String text = StringUtils.abbreviate(element.text(), LINK_TEXT_MAX_LENGTH);
        String textLink = "";

        if (StringUtils.isNotBlank(address)) {
            if (StringUtils.isNotBlank(text)) {
                textLink = String.format("%s (%s)", address, text);
            } else {
                textLink = address;
            }
        } else if (StringUtils.isNotBlank(text)) {
            textLink = text;
        }
        
        return StringUtils.isNotEmpty(textLink) ? " " + textLink + " " : textLink;
    }

    private void clearDynTag(Mailing mailing, DynamicTag tag) {
    	dynamicTagDao.cleanupContentForDynName(mailing.getId(), mailing.getCompanyID(), tag.getDynName());
        tag.getDynContent().clear();
    }

    private String replaceTags(String text, List<Tag> tags) {
        StringBuilder sb = new StringBuilder();
        int begin = 0;

        for (Tag tag : tags) {
            if (begin < tag.getBegin()) {
                sb.append(text, begin, tag.getBegin());
            }
            sb.append(tag.toString());
            begin = tag.getEnd();
        }

        if (begin < text.length()) {
            sb.append(text, begin, text.length());
        }

        return sb.toString();
    }

    private List<Tag> asReplaceableTags(List<DynamicTag> dynamicTags, Map<String, String> namesMap) {
        List<Tag> tags = new ArrayList<>();

        for (DynamicTag tag : dynamicTags) {
            String name = namesMap.getOrDefault(tag.getDynName(), tag.getDynName());

            tags.add(new Tag(tag.getStartTagStart(), tag.getStartTagEnd(), tag.isStandaloneTag() ? TagType.STANDALONE : TagType.OPENING, name));

            if (tag.getValueTagStart() != tag.getValueTagEnd()) {
                tags.add(new Tag(tag.getValueTagStart(), tag.getValueTagEnd(), TagType.VALUE, name));
            }

            if (tag.getEndTagStart() != tag.getEndTagEnd()) {
                tags.add(new Tag(tag.getEndTagStart(), tag.getEndTagEnd(), TagType.CLOSING, name));
            }
        }

        tags.sort(Comparator.comparingInt(Tag::getBegin));

        return tags;
    }

    private List<DynamicTag> getDynamicTags(String content) {
        try {
            return agnTagService.getDynTags(content);
        } catch (DynTagException e) {
            logger.error("Error occurred: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private boolean isValidChangedLinks(Collection<TrackableLink> oldLinks,
                                        Collection<TrackableLink> newLinks, List<Message> errors) {
        try {
            linkService.assertChangedOrDeletedLinksNotDepended(oldLinks, newLinks);
            return true;
        } catch (DependentTrackableLinkException ex) {
            ex.toMessages(errors);
            return false;
        }
    }

    private List<Integer> getCreatedId(DynamicTag oldDynamicTag, DynamicTag newDynamicTag) {
        Set<Integer> oldIds = oldDynamicTag.getDynContent().values().stream()
                .map(DynamicTagContent::getId)
                .collect(Collectors.toSet());

        Set<Integer> newIds = newDynamicTag.getDynContent().values().stream()
                .map(DynamicTagContent::getId)
                .collect(Collectors.toSet());

        return newIds.stream().filter(oldId -> !oldIds.contains(oldId)).collect(Collectors.toList());
    }

    private List<Integer> getUpdateContentIds(DynamicTag oldDynamicTag, DynamicTag newDynamicTag) {
        return oldDynamicTag.getDynContent().values().stream()
                .filter((oldDynContent) -> {
                    DynamicTagContent newDynContent = newDynamicTag.getDynContentID(oldDynContent.getId());
                    return Objects.nonNull(newDynContent) && !oldDynContent.getDynContent().equals(newDynContent.getDynContent());
                })
                .map(DynamicTagContent::getId)
                .collect(Collectors.toList());
    }

    private List<Integer> getUpdateTargetGroupIds(DynamicTag oldDynamicTag, DynamicTag newDynamicTag) {
        return oldDynamicTag.getDynContent().values().stream()
                .filter((oldDynContent) -> {
                    DynamicTagContent newDynContent = newDynamicTag.getDynContentID(oldDynContent.getId());
                    return Objects.nonNull(newDynContent) && oldDynContent.getTargetID() != newDynContent.getTargetID();
                })
                .map(DynamicTagContent::getId)
                .collect(Collectors.toList());
    }

    private DynamicTag convertDynTagDtoToDynamicTag(int companyId, DynamicTag oldDynamicTag, DynTagDto dynTagDto) {
        DynamicTag clonedDynTag = oldDynamicTag.clone();
        clonedDynTag.setId(dynTagDto.getId());
        clonedDynTag.setDynName(dynTagDto.getName());
        clonedDynTag.setMailingID(dynTagDto.getMailingId());
        clonedDynTag.setDynInterestGroup(dynTagDto.getInterestGroup());
        clonedDynTag.setCompanyID(companyId);

        Map<Integer, DynamicTagContent> dynamicTagContentMap = dynTagDto.getContentBlocks().stream()
                .map(content -> {
                    DynamicTagContent oldContent = oldDynamicTag.getDynContentID(content.getId());
                    return convertDynContentDtoToDynamicTagContent(companyId, oldContent, content, dynTagDto);
                })
                .collect(Collectors.toMap(DynamicTagContent::getDynOrder, Function.identity()));

        clonedDynTag.setDynContent(dynamicTagContentMap);
        return clonedDynTag;
    }

    private List<UserAction> getUserActions(DynamicTag oldDynamicTag, DynamicTag newDynamicTag, Mailing mailing) {
        ArrayList<UserAction> userActions = new ArrayList<>();
        final String formatPattern = "%s (%d) %s %s %s (%d)";

        String mailingEntityName = mailing.isIsTemplate() ? "template" : "mailing";
        String mailingShortname = mailing.getShortname();
        int mailingId = mailing.getId();

        List<Integer> idForRemoving = getIdForRemoving(oldDynamicTag, newDynamicTag);
        idForRemoving.forEach((removedId) -> {
            final Object[] formatParameter = new Object[]{null, null, null, mailingEntityName, mailingShortname, mailingId};
            formatParameter[0] = oldDynamicTag.getDynName();
            formatParameter[1] = removedId;
            formatParameter[2] = "from";
            userActions.add(new UserAction("delete textblock", String.format(formatPattern, formatParameter)));
        });

        List<Integer> updateContentIds = getUpdateContentIds(oldDynamicTag, newDynamicTag);
        updateContentIds.forEach((updatedContentId) -> {
            final Object[] formatParameter = new Object[]{null, null, null, mailingEntityName, mailingShortname, mailingId};
            formatParameter[0] = oldDynamicTag.getDynName();
            formatParameter[1] = updatedContentId;
            formatParameter[2] = "from";
            userActions.add(new UserAction("edit textblock content", String.format(formatPattern, formatParameter)));
        });

        List<Integer> updateTargetGroupIds = getUpdateTargetGroupIds(oldDynamicTag, newDynamicTag);
        updateTargetGroupIds.forEach((updatedTargetId) -> {
            final Object[] formatParameter = new Object[]{null, null, null, mailingEntityName, mailingShortname, mailingId};
            formatParameter[0] = oldDynamicTag.getDynName();
            formatParameter[1] = updatedTargetId;
            formatParameter[2] = "from";
            userActions.add(new UserAction("edit textblock target group", String.format(formatPattern, formatParameter)));
        });

        List<Integer> createdIds = getCreatedId(oldDynamicTag, newDynamicTag);
        createdIds.forEach((createdId) -> {
            final Object[] formatParameter = new Object[]{null, null, null, mailingEntityName, mailingShortname, mailingId};
            formatParameter[0] = newDynamicTag.getDynName();
            formatParameter[1] = createdId;
            formatParameter[2] = "in the";
            userActions.add(new UserAction("create textblock", String.format(formatPattern, formatParameter)));
        });

        return userActions;
    }

    private List<Integer> getIdForRemoving(DynamicTag oldDynamicTag, DynamicTag newDynamicTag) {
        Set<Integer> oldIds = oldDynamicTag.getDynContent().values().stream()
                .map(DynamicTagContent::getId)
                .collect(Collectors.toSet());

        Set<Integer> newIds = newDynamicTag.getDynContent().values().stream()
                .map(DynamicTagContent::getId)
                .collect(Collectors.toSet());

        return oldIds.stream().filter(oldId -> !newIds.contains(oldId)).collect(Collectors.toList());
    }

    private DynamicTagContent convertDynContentDtoToDynamicTagContent(int companyId, DynamicTagContent oldDynContent, DynContentDto dynContentDto, DynTagDto dynTagDto) {
        DynamicTagContent clonedDynContent;
        if (Objects.nonNull(oldDynContent)) {
            clonedDynContent = oldDynContent.clone();
        } else {
            clonedDynContent = dynamicTagContentFactory.newDynamicTagContent();
        }

        clonedDynContent.setId(dynContentDto.getId());
        clonedDynContent.setDynOrder(dynContentDto.getIndex());
        clonedDynContent.setDynContent(dynContentDto.getContent());
        clonedDynContent.setTargetID(dynContentDto.getTargetId());

        clonedDynContent.setCompanyID(companyId);
        clonedDynContent.setDynName(dynTagDto.getName());
        clonedDynContent.setDynNameID(dynTagDto.getId());
        clonedDynContent.setMailingID(dynTagDto.getMailingId());

        return clonedDynContent;
    }
 }
