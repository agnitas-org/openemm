/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.factory.DynamicTagContentFactory;
import org.agnitas.beans.factory.DynamicTagFactory;
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
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.service.AgnTagService;
import com.agnitas.service.ComMailingContentService;
import com.agnitas.util.Span;

public class ComMailingContentServiceImpl implements ComMailingContentService {
    private static final Logger logger = LogManager.getLogger(ComMailingContentServiceImpl.class);

    private static final int LINK_TEXT_MAX_LENGTH = 75;
    private static final Pattern MULTIPLE_WHITESPACES_PATTERN = Pattern.compile("[\\s\u00A0]{2,}");

    private DynamicTagContentFactory dynamicTagContentFactory;
    private DynamicTagFactory dynamicTagFactory;
    private ComMailingBaseService mailingBaseService;
    private AgnTagService agnTagService;
    private ComMailingDao mailingDao;
    private DynamicTagDao dynamicTagDao;

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

    @Override
    public boolean isGenerationAvailable(Mailing mailing) {
        if (mailing.getHtmlTemplate() == null || mailing.getTextTemplate() == null) {
            return false;
        }

        MailingComponent template = mailing.getHtmlTemplate();

        return !mailingBaseService.isContentBlank(template.getEmmBlock(), mailing.getDynTags());
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

    private enum TagType {
        STANDALONE, OPENING, VALUE, CLOSING
    }

    public static class Tag extends Span {
        private TagType type;
        private String name;

        public Tag(int begin, int end, TagType type, String name) {
            super(begin, end);
            this.type = type;
            this.name = name;
        }

        public TagType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            switch (type) {
                case STANDALONE:
                    return String.format("[agnDYN name=\"%s\"/]", name);
                case OPENING:
                    return String.format("[agnDYN name=\"%s\"]", name);
                case VALUE:
                    return String.format("[agnDVALUE name=\"%s\"]", name);
                case CLOSING:
                    return String.format("[/agnDYN name=\"%s\"]", name);
				default:
					break;
            }

            throw new RuntimeException("Unexpected type value (" + type + ")");
        }
    }
    
    public final ComMailingDao getMailingDao() {
    	return this.mailingDao;
    }

    @Required
    public void setDynamicTagContentFactory(DynamicTagContentFactory dynamicTagContentFactory) {
        this.dynamicTagContentFactory = dynamicTagContentFactory;
    }

    @Required
    public void setDynamicTagFactory(DynamicTagFactory dynamicTagFactory) {
        this.dynamicTagFactory = dynamicTagFactory;
    }

    @Required
    public void setMailingBaseService(ComMailingBaseService mailingBaseService) {
        this.mailingBaseService = mailingBaseService;
    }

    @Required
    public void setAgnTagService(AgnTagService agnTagService) {
        this.agnTagService = agnTagService;
    }

    @Required
    public void setMailingDao(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Required
    public void setDynamicTagDao(DynamicTagDao dynamicTagDao) {
        this.dynamicTagDao = dynamicTagDao;
    }
 }
