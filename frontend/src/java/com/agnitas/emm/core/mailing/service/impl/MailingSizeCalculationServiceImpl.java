/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.core.components.service.ComMailingComponentsService;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.linkcheck.service.LinkServiceImpl;
import com.agnitas.emm.core.mailing.service.MailingSizeCalculationService;
import com.agnitas.service.AgnTagService;
import com.agnitas.util.Span;
import org.agnitas.backend.AgnTag;
import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.MailingComponent;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.util.AgnTagUtils;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DynTagException;
import org.agnitas.util.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

public class MailingSizeCalculationServiceImpl implements MailingSizeCalculationService {

    private static final Logger logger = LogManager.getLogger(MailingSizeCalculationServiceImpl.class);

    private static final Pattern BASE64_URL_PATTERN = Pattern.compile("data:image/([a-zA-Z]*);base64,([^\"]*)", Pattern.CASE_INSENSITIVE);

    private static final double QUOTED_PRINTABLE_OVERHEAD_RATIO = 8.f / 7.f;  // Quoted-printable notation uses 7 bit out of 8
    private static final double BASE64_OVERHEAD_RATIO = 8.f / 6.f;  // Base64 uses 6 bit out of 8 (padding is neglectable)

    private static final List<AgnTag> AGN_IMAGE_TAGS = List.of(AgnTag.IMG_LINK, AgnTag.IMAGE);

    private final ComMailingComponentsService mailingComponentsService;
    private final AgnTagService agnTagService;
    private final LinkService linkService;
    private final DynamicTagDao dynamicTagDao;

    public MailingSizeCalculationServiceImpl(ComMailingComponentsService mailingComponentsService, AgnTagService agnTagService, LinkService linkService, DynamicTagDao dynamicTagDao) {
        this.mailingComponentsService = mailingComponentsService;
        this.agnTagService = agnTagService;
        this.linkService = linkService;
        this.dynamicTagDao = dynamicTagDao;
    }

    @Override
    public Tuple<Long, Long> calculateSize(Mailing mailing, Admin admin) {
        MailingSizeCalculator.SizeCalculationResult calculationResult = createCalculator(mailing, admin).calculate();

        long deliverySize = calculationResult.getContentSize() + calculationResult.getAttachmentsSize();
        long imagesSize = calculationResult.getImagesSize();

        if (isOfflineHtmlFormat(mailing)) {
            deliverySize += Math.round(imagesSize * BASE64_OVERHEAD_RATIO);
            return new Tuple<>(deliverySize, deliverySize);
        }

        return new Tuple<>(deliverySize, deliverySize + imagesSize);
    }

    private boolean isOfflineHtmlFormat(Mailing mailing) {
        MediatypeEmail emailParam = mailing.getEmailParam();
        return emailParam != null && emailParam.getMailFormat() == MailingModel.Format.OFFLINE_HTML.getValue();
    }

    protected MailingSizeCalculator createCalculator(Mailing mailing, Admin admin) {
        return new MailingSizeCalculator(mailing);
    }

    public static class Block {

        private String name;
        private String content;
        private Markup markup;

        public Block(String name, String content) {
            this.name = name;
            this.content = content;
        }

        public String getName() {
            return name;
        }

        public String getContent() {
            return content;
        }

        public Markup getMarkup() {
            return markup;
        }

        public void setMarkup(Markup markup) {
            this.markup = markup;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((content == null) ? 0 : content.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Block other = (Block) obj;

            if (content == null) {
                if (other.content != null) {
                    return false;
                }
            } else if (!content.equals(other.content)) {
                return false;
            }

            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }

            return true;
        }
    }

    private static class Markup {

        private long ownSize;
        private Set<String> ownImages;
        private List<String> tags;

        private Markup() {
            this(0, Collections.emptySet(), Collections.emptyList());
        }

        private Markup(long ownSize, Set<String> ownImages, List<String> tags) {
            this.ownSize = ownSize;
            this.ownImages = ownImages;
            this.tags = tags;
        }

        public long getOwnSize() {
            return ownSize;
        }

        public Set<String> getOwnImages() {
            return ownImages;
        }

        public List<String> getTags() {
            return tags;
        }
    }

    private static class ImageLinkSpan extends Span {

        private final String link;

        public ImageLinkSpan(String link, int begin, int end) {
            super(begin, end);
            this.link = link;
        }

        public String getLink() {
            return link;
        }
    }

    protected class MailingSizeCalculator {

        private Mailing mailing;
        private Map<String, List<Block>> blockMap;
        private Map<String, Integer> imageSizeMap;

        public MailingSizeCalculator(Mailing mailing) {
            this.mailing = mailing;
            this.blockMap = getBlockMap(mailing.getDynTags().values());
        }

        private class SizeCalculationResult {

            private long contentSize;
            private long imagesSize;
            private long attachmentsSize;

            public SizeCalculationResult(long contentSize, long imagesSize) {
                this.contentSize = contentSize;
                this.imagesSize = imagesSize;
            }

            public long getContentSize() {
                return contentSize;
            }

            public long getImagesSize() {
                return imagesSize;
            }

            public long getAttachmentsSize() {
                return attachmentsSize;
            }

            public void setAttachmentsSize(long attachmentsSize) {
                this.attachmentsSize = attachmentsSize;
            }

            public long getMarkupSize() {
                return contentSize + imagesSize;
            }
        }

        public SizeCalculationResult calculate() {
            parseMarkupForContentBlocks();

            Markup textMarkup = getMarkup(getContent(mailing.getTextTemplate()));
            Markup htmlMarkup = getMarkup(getContent(mailing.getHtmlTemplate()));

            Map<String, Integer> sizeMapWithoutExternalImages = mailingComponentsService.getImageSizeMap(mailing.getCompanyID(), mailing.getId(), false);
            Map<String, Integer> sizeMapWithExternalImages = mailingComponentsService.getImageSizeMap(mailing.getCompanyID(), mailing.getId(), true);

            imageSizeMap = sizeMapWithoutExternalImages;

            SizeCalculationResult maxMarkupSize = calculateMaxMarkupSize(textMarkup, htmlMarkup);

            if (sizeMapWithExternalImages.size() != sizeMapWithoutExternalImages.size()) {
                imageSizeMap = sizeMapWithExternalImages;
                maxMarkupSize = calculateMaxMarkupSize(textMarkup, htmlMarkup);
            }

            long attachmentsSize = calculateAttachmentsSize(mailing.getCompanyID(), mailing.getId());
            maxMarkupSize.setAttachmentsSize(attachmentsSize);

            return maxMarkupSize;
        }

        private SizeCalculationResult calculateMaxMarkupSize(Markup textMarkup, Markup htmlMarkup) {
            SizeCalculationResult textMarkupSize = calculate(textMarkup);
            SizeCalculationResult htmlMarkupSize = calculate(htmlMarkup);

            if (htmlMarkupSize.getMarkupSize() > textMarkupSize.getMarkupSize()) {
                return htmlMarkupSize;
            }

            return textMarkupSize;
        }

        private void parseMarkupForContentBlocks() {
            for (List<Block> blocks : blockMap.values()) {
                for (Block block : blocks) {
                    if (StringUtils.isEmpty(block.getContent())) {
                        block.setMarkup(new Markup());
                    } else {
                        block.setMarkup(getMarkup(block.getContent()));
                    }
                }
            }
        }

        private SizeCalculationResult calculate(Markup root) {
            Map<String, Block> selection = new HashMap<>();

            long contentSize = evaluate(root, new ArrayDeque<>(), selection);
            List<String> images = extractImages(root, selection);

            long imagesSize = calculateImagesSize(images);
            contentSize = Math.round(contentSize * QUOTED_PRINTABLE_OVERHEAD_RATIO);

            return new SizeCalculationResult(contentSize, imagesSize);
        }

        private List<String> extractImages(Markup root, Map<String, Block> selection) {
            List<String> images = new ArrayList<>(root.getOwnImages());

            for (Block block : selection.values()) {
                images.addAll(block.getMarkup().getOwnImages());
            }

            return images.stream()
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        protected long calculateImagesSize(Collection<String> images) {
            long imagesSize = 0;

            for (String image : images) {
                imagesSize += imageSizeMap.getOrDefault(image, 0);
            }

            return imagesSize;
        }

        private long evaluate(Markup markup, Deque<String> tagStack, Map<String, Block> selection) {
            long size = markup.getOwnSize();

            for (String tag : markup.getTags()) {
                Block block = selection.get(tag);

                // Handle cyclic dependency by ignoring tag.
                if (!tagStack.contains(tag)) {
                    tagStack.push(tag);
                    if (block == null) {
                        long maxBlockSize = 0;

                        // Evaluate size and select the biggest content block.
                        for (Block candidate : blockMap.getOrDefault(tag, Collections.emptyList())) {
                            long blockSize = evaluate(candidate.getMarkup(), tagStack, selection);
                            if (blockSize > maxBlockSize || block == null) {
                                maxBlockSize = blockSize;
                                block = candidate;
                            }
                        }

                        // Remember the choice.
                        if (block != null) {
                            selection.put(tag, block);
                            size += maxBlockSize;
                        }
                    } else {
                        size += evaluate(block.getMarkup(), tagStack, selection);
                    }
                    tagStack.pop();
                }
            }

            return size;
        }

        private String getContent(MailingComponent root) {
            if (root == null || StringUtils.isEmpty(root.getEmmBlock())) {
                return null;
            }

            return root.getEmmBlock();
        }

        private Markup getMarkup(String content) {
            if (StringUtils.isEmpty(content)) {
                return new Markup();
            }

            content = AgnTagUtils.unescapeAgnTags(content);

            // Collect agn-tags representing dyn-tags (agnDYN/agnDVALUE) and image tags (agnIMAGE).
            List<DynamicTag> dynamicTags = getDynamicTags(content);
            List<ImageLinkSpan> imageLinks = getImageLinks(content, mailing);

            // Own content size (to be reduced by number of characters taken by tag lexemes).
            long ownSize = AgnUtils.countBytes(content);

            // Erase content (simple text/html or other agn-tags) embraced with disabled (not defined by user) dynamic tags (if any).
            if (!dynamicTags.isEmpty()) {
                List<Span> excludedSpans = collectExcludedSpans(dynamicTags);
                Iterator<DynamicTag> iterator = dynamicTags.iterator();

                if (excludedSpans.isEmpty()) {
                    // Reduce own content size by number of characters taken by tag lexemes.
                    while (iterator.hasNext()) {
                        DynamicTag tag = iterator.next();

                        ownSize -= AgnUtils.countBytes(content, tag.getStartTagStart(), tag.getStartTagEnd());  // Opening agnDYN
                        ownSize -= AgnUtils.countBytes(content, tag.getValueTagStart(), tag.getValueTagEnd());  // agnDVALUE
                        ownSize -= AgnUtils.countBytes(content, tag.getEndTagStart(), tag.getEndTagEnd());  // Closing agnDYN

                        // Case when there are opening and closing agn-dyn tags but agnDVALUE tag is missing.
                        if (!tag.isStandaloneTag() && tag.getValueTagStart() == tag.getValueTagEnd()) {
                            // Missing agnDVALUE tag means that block content is not inserted
                            // and should be ignored when calculating content size.
                            iterator.remove();
                        }
                    }
                } else {
                    Comparator<Span> comparator = getSubSpanComparator();

                    while (iterator.hasNext()) {
                        DynamicTag tag = iterator.next();

                        if (tag.isStandaloneTag()) {
                            Span span = new Span(tag.getStartTagStart(), tag.getStartTagEnd());

                            // Check if current standalone tag is placed within excluded span.
                            if (Collections.binarySearch(excludedSpans, span, comparator) >= 0) {
                                iterator.remove();
                            }
                        } else {
                            Span valueTagSpan = new Span(tag.getValueTagStart(), tag.getValueTagEnd());
                            Span embracingSpan = new Span(tag.getStartTagStart(), tag.getEndTagEnd());

                            // Case when there are opening and closing agn-dyn tags but agnDVALUE tag is missing.
                            if (valueTagSpan.getBegin() == valueTagSpan.getEnd()) {
                                // Missing agnDVALUE tag means that block content is not inserted
                                // and should be ignored when calculating content size.
                                iterator.remove();

                                // Check if opening and closing agn-dyn tags are NOT placed withing excluded span.
                                if (Collections.binarySearch(excludedSpans, embracingSpan, comparator) < 0) {
                                    ownSize -= AgnUtils.countBytes(content, tag.getStartTagStart(), tag.getStartTagEnd());  // Opening agnDYN
                                    ownSize -= AgnUtils.countBytes(content, tag.getEndTagStart(), tag.getEndTagEnd());  // Closing agnDYN
                                }
                            } else {
                                int index = Collections.binarySearch(excludedSpans, valueTagSpan, comparator);

                                // Check if agnDVALUE tag is placed withing excluded span.
                                if (index >= 0) {
                                    Span excludedSpan = excludedSpans.get(index);

                                    // Check if opening and closing agn-dyn tags are NOT placed within excluded span.
                                    if (!excludedSpan.contains(embracingSpan)) {
                                        ownSize -= AgnUtils.countBytes(content, tag.getStartTagStart(), tag.getStartTagEnd());  // Opening agnDYN
                                        ownSize -= AgnUtils.countBytes(content, tag.getEndTagStart(), tag.getEndTagEnd());  // Closing agnDYN
                                    }

                                    iterator.remove();
                                }
                            }
                        }
                    }

                    // Reduce own content size by number of characters taken by disabled tags.
                    for (Span span : excludedSpans) {
                        ownSize -= AgnUtils.countBytes(content, span.getBegin(), span.getEnd());
                    }

                    // Reduce own content size by number of characters taken by tag lexemes (outside of excluded spans).
                    for (DynamicTag tag : dynamicTags) {
                        ownSize -= AgnUtils.countBytes(content, tag.getStartTagStart(), tag.getStartTagEnd());  // Opening agnDYN
                        ownSize -= AgnUtils.countBytes(content, tag.getValueTagStart(), tag.getValueTagEnd());  // agnDVALUE
                        ownSize -= AgnUtils.countBytes(content, tag.getEndTagStart(), tag.getEndTagEnd());  // Closing agnDYN
                    }

                    imageLinks.removeIf(span -> Collections.binarySearch(excludedSpans, span, comparator) >= 0);
                }
            }

            return new Markup(ownSize, getUniqueImageNames(imageLinks), getDynNames(dynamicTags));
        }

        private Map<String, List<Block>> getBlockMap(Collection<DynamicTag> tags) {
            Map<String, List<Block>> newBlockMap = new HashMap<>();

            for (DynamicTag tag : tags) {
                List<DynamicTagContent> contents = new ArrayList<>(tag.getDynContent().values());

                if (!contents.isEmpty()) {
                    contents.sort(Comparator.comparingInt(DynamicTagContent::getDynOrder));

                    List<Block> blocks = new ArrayList<>(contents.size());

                    for (DynamicTagContent content : contents) {
                        blocks.add(new Block(tag.getDynName(), content.getDynContent()));

                        // Exclude all unreachable blocks (placed below "All recipients" block).
                        if (content.getTargetID() <= 0) {
                            break;
                        }
                    }

                    newBlockMap.put(tag.getDynName(), blocks);
                }
            }

            return newBlockMap;
        }

        private List<Span> collectExcludedSpans(List<DynamicTag> dynamicTags) {
            List<Span> excludedSpans = new ArrayList<>(dynamicTags.size());

            // Collect content spans to be erased (if any).
            for (DynamicTag tag : dynamicTags) {
                if (!blockMap.containsKey(tag.getDynName())) {
                    Span span = new Span(tag.getStartTagStart(), tag.isStandaloneTag() ? tag.getStartTagEnd() : tag.getEndTagEnd());

                    if (excludedSpans.isEmpty()) {
                        excludedSpans.add(span);
                    } else {
                        Span previousSpan = excludedSpans.get(excludedSpans.size() - 1);
                        // Ignore sub-spans of excluded spans.
                        if (previousSpan.getEnd() <= span.getBegin()) {
                            excludedSpans.add(span);
                        }
                    }
                }
            }

            return excludedSpans;
        }

        private Set<String> getUniqueImageNames(List<ImageLinkSpan> imageLinks) {
            return imageLinks.stream()
                    .map(ImageLinkSpan::getLink)
                    .collect(Collectors.toSet());
        }

        private List<String> getDynNames(List<DynamicTag> tags) {
            return tags.stream()
                    .map(DynamicTag::getDynName)
                    .collect(Collectors.toList());
        }

        private Comparator<Span> getSubSpanComparator() {
            // This comparator is going to find a span that contains or matches a searched span.
            return (superSpan, subSpan) -> {
                if (subSpan.getBegin() < superSpan.getBegin()) {
                    return +1;
                }

                if (subSpan.getEnd() > superSpan.getEnd()) {
                    return -1;
                }

                return 0;
            };
        }
    }

    private long calculateAttachmentsSize(int companyId, int mailingId) {
        List<MailingComponent> attachments = mailingComponentsService.getPreviewHeaderComponents(companyId, mailingId);

        return attachments.stream()
                .map(attachment -> StringUtils.length(AgnUtils.encodeBase64(attachment.getBinaryBlock())))
                .reduce(0, Integer::sum);
    }

    private List<DynamicTag> getDynamicTags(String content) {
        try {
            return agnTagService.getDynTags(content);
        } catch (DynTagException e) {
            logger.error(format("Error occurred: {0}", e.getMessage()), e);
            return Collections.emptyList();
        }
    }

    private List<ImageLinkSpan> getImageLinks(String content, Mailing mailing) {
        List<ImageLinkSpan> imageLinks = new ArrayList<>();

        try {
            imageLinks.addAll(findAllLinks(content, mailing));
            removeLinksWithAgnImages(imageLinks);
            removeBase64Links(imageLinks);
        } catch (Exception e) {
            logger.error(format("Error occurred: {0}", e.getMessage()), e);
        }

        try {
            agnTagService.collectTags(content, tag -> isImageTag(tag.getTagName()))
                    .forEach(tag -> imageLinks.add(new ImageLinkSpan(tag.getName(), tag.getStartPos(), tag.getEndPos())));
        } catch (Exception e) {
            logger.error(format("Error occurred: {0}", e.getMessage()), e);
        }

        return imageLinks;
    }

    private List<ImageLinkSpan> findAllLinks(String content, Mailing mailing) {
        List<ImageLinkSpan> imageLinks = new ArrayList<>();
        char[] ignoredCharacters = {' ', '\'', '"', '\n', '\r', '\t'};
        String contentForScan = LinkServiceImpl.getTextWithReplacedAgnTags(content, "x");

        linkService.findAllLinks(contentForScan, (begin, end) -> {
            if (AgnUtils.checkPreviousTextEquals(content, begin, "src=", ignoredCharacters)
                    || AgnUtils.checkPreviousTextEquals(content, begin, "background=", ignoredCharacters)
                    || AgnUtils.checkPreviousTextEquals(content, begin, "url(", ignoredCharacters)) {

                String url = content.substring(begin, end);

                if (url.contains("[agnDVALUE")) {
                    String dynTagName = AgnTagUtils.getAgnTagName(url);
                    url = getDynTagContent(dynTagName, mailing);

                    if (url == null) {
                        return;
                    }
                }
                imageLinks.add(new ImageLinkSpan(url, begin, end));
            }
        });

        return imageLinks;
    }

    private String getDynTagContent(String dynTagName, Mailing mailing) {
        List<DynamicTag> dynamicTags = dynamicTagDao.getDynamicTags(mailing.getId(), mailing.getCompanyID(), false);

        Optional<DynamicTag> dynTag = dynamicTags.stream().filter(dt -> dynTagName.equals(dt.getDynName())).findFirst();

        if (dynTag.isEmpty()) {
            return null;
        }

        String content = null;

        for (DynamicTagContent contentBlock : dynTag.get().getDynContent().values()) {
            if (contentBlock.getTargetID() == 0) {
                content = contentBlock.getDynContent().trim();
            }
        }

        return content;
    }

    private void removeLinksWithAgnImages(List<ImageLinkSpan> links) {
        links.removeIf(il -> isAgnImageLink(il.getLink()));
    }

    private void removeBase64Links(List<ImageLinkSpan> links) {
        links.removeIf(l -> isBase64Image(l.getLink()));
    }

    private boolean isBase64Image(String imageLink) {
        return imageLink != null && BASE64_URL_PATTERN.matcher(imageLink.trim()).matches();
    }

    private boolean isAgnImageLink(String link) {
        Optional<AgnTag> imageTag = findAgnImageTagInsideImageLink(link);
        return imageTag.isPresent();
    }

    private Optional<AgnTag> findAgnImageTagInsideImageLink(String link) {
        final String trimmedLink = StringUtils.trimToNull(link); // can be a case when image tag can be with spaces like here: <img src="  [agnIMAGE name='image.jpg']" />

        if (trimmedLink == null || !trimmedLink.startsWith("[")) {
            return Optional.empty();
        }

        return AGN_IMAGE_TAGS.stream()
                .filter(t -> trimmedLink.startsWith("[" + t.getName()))
                .findAny();
    }

    private boolean isImageTag(String tag) {
        return AGN_IMAGE_TAGS.stream()
                .anyMatch(t -> t.getName().equals(tag));
    }
}
