/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import cz.vutbr.web.css.RuleFontFace;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.cyberneko.html.HTMLConfiguration;
import org.jsoup.nodes.Attributes;
import org.jsoup.parser.SaxParser;
import org.jsoup.parser.SaxParsingHandler;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.agnitas.messages.I18nString;
import com.agnitas.util.ParsingException;

import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.MediaQuery;
import cz.vutbr.web.css.MediaSpec;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleMedia;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermURI;
import cz.vutbr.web.csskit.OutputUtil;
import cz.vutbr.web.csskit.TermIntegerImpl;
import cz.vutbr.web.domassign.Analyzer;
import cz.vutbr.web.domassign.DeclarationMap;

public class HtmlUtils {
    private static final Logger logger = Logger.getLogger(HtmlUtils.class);

    private static final Set<String> STANDALONE_NODE_NAMES = new CaseInsensitiveSet(Arrays.asList(
            "area",
            "base",
            "br",
            "col",
            "frame",
            "hr",
            "img",
            "input",
            "link",
            "meta",
            "param",
            "basefont",
            "isindex"
    ));

    /**
     * Parse an entire HTML document or a document fragment. Use lowercase translation for names of tags and attributes.
     * @param document a HTML code to parse.
     * @param encoding an encoding to use for a parser.
     * @return a parsed document representation.
     */
    public static Document parseDocument(String document, String encoding) throws IOException, SAXException {
        DOMParser parser = new DOMParser(new HTMLConfiguration());

        try {
            // These URLs are predefined parameters' names (check org.cyberneko.html.HTMLConfiguration for more information)
            parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
            parser.setProperty("http://cyberneko.org/html/properties/default-encoding", encoding);
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            logger.error("Unexpected parser configuration error occurred: " + e.getMessage());
            throw new RuntimeException(e);
        }

        StringReader reader = new StringReader(document);
        InputSource source = new InputSource(reader);
        parser.parse(source);

        return parser.getDocument();
    }

    private static List<RuleBlock<?>> getAtRuleBlocks(String css, StylesEmbeddingOptions options) {
        try {
            return getAtRuleBlocks(CSSFactory.parseString(css, options.getBaseUrl()), options);
        } catch (IOException | CSSException e) {
            logger.error("Style sheets parsing failed", e);
        }
        return Collections.emptyList();
    }

    private static List<RuleBlock<?>> getAtRuleBlocks(URL source, StylesEmbeddingOptions options) {
        try {
            return getAtRuleBlocks(CSSFactory.parse(source, options.getEncodingName()), options);
        } catch (IOException | CSSException e) {
            logger.error("Remote style sheets parsing failed", e);
        }
        return Collections.emptyList();
    }

    private static List<RuleBlock<?>> getAtRuleBlocks(StyleSheet styles, StylesEmbeddingOptions options) {
        List<RuleBlock<?>> rules = new ArrayList<>();

        if (styles != null) {
            for (RuleBlock<?> ruleBlock : styles) {
                if (ruleBlock instanceof RuleMedia) {
                    RuleMedia block = (RuleMedia) ruleBlock;

                    for (RuleSet set : block) {
                        set.forEach(HtmlUtils::resolveUris);
                    }

                    resolveMediaType(block.getMediaQueries(), options.getMediaType());

                    rules.add(block);
                } else if (ruleBlock instanceof RuleFontFace) {
                    rules.add(ruleBlock);
                }
            }
        }

        return rules;
    }

    /**
     * Extract all the media queries from a {@code css} stylesheet, resolve relative URIs (if any) in a CSS rules.
     * @param css a stylesheet to parse.
     * @param baseUrl a URL to use for URIs resolution.
     * @return a list of entities that represent media queries.
     */
    public static List<RuleMedia> getMediaQueries(String css, URL baseUrl) {
        try {
            return getMediaQueries(CSSFactory.parseString(css, baseUrl));
        } catch (IOException | CSSException e) {
            logger.error("Style sheets parsing failed", e);
        }
        return Collections.emptyList();
    }

    /**
     * Extract all the media queries from a stylesheet available for {@code source} URL, resolve relative URIs (if any) in a CSS rules.
     * @param source a stylesheet document URL.
     * @param encoding an encoding to use for a CSS parser.
     * @return a list of entities that represent media queries.
     */
    public static List<RuleMedia> getMediaQueries(URL source, String encoding) {
        try {
            return getMediaQueries(CSSFactory.parse(source, encoding));
        } catch (IOException | CSSException e) {
            logger.error("Remote style sheets parsing failed", e);
        }
        return Collections.emptyList();
    }

    /**
     * Extract all the media queries from a {@code styles} stylesheet, resolve relative URIs (if any) in a CSS rules.
     * @param styles a stylesheet to process.
     * @return a list of entities that represent media queries.
     */
    private static List<RuleMedia> getMediaQueries(StyleSheet styles) {
        List<RuleMedia> rules = new ArrayList<>();

        if (styles != null) {
            for (RuleBlock<?> ruleBlock : styles) {
                if (ruleBlock instanceof RuleMedia) {
                    RuleMedia block = (RuleMedia) ruleBlock;

                    for (RuleSet set : block) {
                        set.forEach(HtmlUtils::resolveUris);
                    }

                    rules.add(block);
                }
            }
        }

        return rules;
    }

    public static String resolveMediaType(String mediaSpec, String mediaType, URL baseUrl) {
        if (StringUtils.isNotEmpty(mediaType)) {
            List<RuleMedia> rules = getMediaQueries("@media " + mediaSpec + " { * {} }", baseUrl);
            if (CollectionUtils.isNotEmpty(rules)) {
                List<MediaQuery> queries = rules.get(0).getMediaQueries();
                if (CollectionUtils.isNotEmpty(queries)) {
                    resolveMediaType(queries, mediaType);
                    return queries.stream().map(MediaQuery::toString)
                            .collect(Collectors.joining(", "));
                }
            }
        }
        return mediaSpec;
    }

    private static void resolveMediaType(List<MediaQuery> queries, String mediaType) {
        if (StringUtils.isNotEmpty(mediaType)) {
            for (MediaQuery q : queries) {
                final String type = q.getType();

                if ("all".equalsIgnoreCase(type)) {
                    continue;
                }

                q.setType("all");

                if (!mediaType.equalsIgnoreCase(type)) {
                    q.setNegative(!q.isNegative());
                    q.clear();
                }
            }
        }
    }

    /**
     * Convert all the URIs (if any) to an absolute.
     * @param declaration a rules to process.
     */
    private static void resolveUris(Declaration declaration) {
        for (Term<?> term : declaration) {
            if (term instanceof TermURI) {
                resolveUri((TermURI) term);
            }
        }
    }

    /**
     * Resolve the URI to an absolute.
     * @param termUri a rule to process.
     */
    private static void resolveUri(TermURI termUri) {
        URL base = termUri.getBase();
        String value = termUri.getValue();

        if (base == null || base.getHost() == null) {
            logger.error("Base URL is required");
            return;
        }

        if (StringUtils.isEmpty(value)) {
            logger.error("Empty URI");
            return;
        }

        termUri.setValue(HttpUtils.resolveRelativeUri(base, value));
    }

    /**
     * Check whether a {@code name} belongs to a standalone (self-closing) tag (one that is not allowed to contain a children).
     * @param name a name of a tag to check.
     * @return {@code true} if tag is standalone or {@code false} otherwise.
     */
    private static boolean isStandaloneTag(String name) {
        return STANDALONE_NODE_NAMES.contains(name);
    }

    /**
     * Check whether a {@code node} represents an MSIE-compatible conditional comment.
     * See https://msdn.microsoft.com/en-us/library/ms537512.aspx for more details.
     * @param node a node to check.
     * @return {@code true} if a node represents a conditional comment or {@code false} otherwise.
     */
    private static boolean isConditionalCommentNode(Node node) {
        if (node.getNodeType() == Node.COMMENT_NODE) {
            final String content = StringUtils.trimToNull(node.getNodeValue());
            if (content != null) {
                // <!--[]><![]-->
                return content.startsWith("[") && content.endsWith("]");
            }
        }
        return false;
    }

    /**
     * Get a string representation of a doctype element.
     * @param documentType a doctype element.
     * @return a string representation.
     */
    public static String toString(DocumentType documentType) {
        StringBuilder builder = new StringBuilder();
        toString(builder, documentType);
        return builder.toString();
    }

    /**
     * Get a string representation of a doctype element and append it to a {@code builder}.
     * @param builder a string builder to write result to.
     * @param documentType a doctype element.
     */
    public static void toString(StringBuilder builder, DocumentType documentType) {
        builder.append("<!DOCTYPE ").append(documentType.getName());

        if (StringUtils.isNotBlank(documentType.getPublicId())) {
            builder.append(" PUBLIC \"").append(documentType.getPublicId()).append('"');
        }

        if (StringUtils.isNotBlank(documentType.getSystemId())) {
            builder.append(" \"").append(documentType.getSystemId()).append('"');
        }

        builder.append('>');
    }

    /**
     * This method provides a workaround for bug in jStyleParser library that is currently used. To be used instead of
     * a plain {@link cz.vutbr.web.css.Declaration#toString()} method.
     * (see https://github.com/radkovo/jStyleParser/commit/356f39348929569eb8117bc14afd70687d6a23dc)
     * @param declaration CSS declaration representation to stringify.
     * @return a string representation of a CSS declaration.
     */
    public static String toString(Declaration declaration) {
        StringBuilder sb = new StringBuilder();
        sb.append(declaration.getProperty()).append(OutputUtil.PROPERTY_OPENING);
        for (Term<?> term : declaration.asList()) {
            if (term instanceof TermIntegerImpl) {
                Term.Operator operator = term.getOperator();
                if (operator != null) {
                    sb.append(operator.value());
                }
                sb.append(term.toString());
            } else {
                sb.append(term.toString());
            }
        }
        if (declaration.isImportant()) {
            sb.append(OutputUtil.SPACE_DELIM).append(OutputUtil.IMPORTANT_KEYWORD);
        }
        sb.append(OutputUtil.PROPERTY_CLOSING.trim());
        return sb.toString();
    }

    public static String embedStyles(Document document, DeclarationMap styleMap, StylesEmbeddingOptions options) {
        StringBuilder builder = new StringBuilder();
        embedStyles(builder, document, styleMap, options);
        return builder.toString();
    }

    public static String embedStyles(NodeList nodes, DeclarationMap styleMap, StylesEmbeddingOptions options) {
        StringBuilder builder = new StringBuilder();
        embedStyles(builder, nodes, styleMap, options);
        return builder.toString();
    }

    private static void embedStyles(StringBuilder builder, Document document, DeclarationMap styleMap, StylesEmbeddingOptions options) {
        DocumentType documentType = document.getDoctype();
        if (documentType != null) {
            toString(builder, document.getDoctype());
            if (options.isPrettyPrint()) {
                builder.append('\n');
            }
        }
        embedStyles(builder, document.getDocumentElement(), styleMap, options);
    }

    private static void embedStyles(StringBuilder builder, NodeList nodes, DeclarationMap styleMap, StylesEmbeddingOptions options) {
        for (int i = 0; i < nodes.getLength(); i++) {
            embedStyles(builder, nodes.item(i), styleMap, options);
        }
    }

    private static void embedStyles(StringBuilder builder, Node node, DeclarationMap styleMap, StylesEmbeddingOptions options) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                Element element = (Element) node;
                String nodeName = node.getNodeName();

                switch (nodeName) {
                    case "style":
                        if (StringUtils.isNotBlank(node.getTextContent())) {
                            String mediaSpec = element.getAttribute("media");
                            if (StringUtils.isNotBlank(mediaSpec)) {
                                element.setAttribute("media", resolveMediaType(mediaSpec, options.getMediaType(), options.getBaseUrl()));

                                builder.append("<style");
                                appendAttributes(builder, styleMap, element, options);
                                builder.append('>');
                                builder.append(node.getTextContent());
                                builder.append("</style>");

                                if (options.isPrettyPrint()) {
                                    builder.append('\n');
                                }
                            } else {
                                // Strip all the styles except non-embeddable ones (like @media, @font-face etc)
                                if (options.isUseNewLib()) {
                                    appendStyleTag(builder, CssUtils.stripEmbeddableStyles(node.getTextContent(), options.getMediaType(), options.isPrettyPrint()), styleMap, element, options);
                                } else {
                                    appendStyleTag(builder, getAtRuleBlocks(node.getTextContent(), options), styleMap, element, options);
                                }
                            }
                        }
                        break;

                    case "link":
                        if (StringUtils.equalsIgnoreCase("stylesheet", element.getAttribute("rel"))) {
                            String address = element.getAttribute("href");
                            if (StringUtils.isBlank(element.getAttribute("media")) && StringUtils.isNotBlank(address)) {
                                if (options.getBaseUrl() != null) {
                                    address = HttpUtils.resolveRelativeUri(options.getBaseUrl(), address);
                                }

                                try {
                                    URL remoteResource = new URL(address);

                                    if (options.isUseNewLib()) {
                                        appendStyleTag(builder, CssUtils.stripEmbeddableStyles(remoteResource, options.getMediaType(), options.isPrettyPrint()), options);
                                    } else {
                                        appendStyleTag(builder, getAtRuleBlocks(remoteResource, options), options);
                                    }
                                } catch (MalformedURLException e) {
                                    logger.error("Unable to resolve a URL: " + address);
                                }

                                // Don't append a link tag
                                break;
                            }
                        }

                    //$FALL-THROUGH$
				default:
                        if (options.isPrettyPrint()) {
                            builder.append('\n');
                        }

                        builder.append('<').append(nodeName);

                        appendAttributes(builder, styleMap, element, options);

                        if (isStandaloneTag(nodeName)) {
                            builder.append("/>");

                            if (options.isPrettyPrint()) {
                                builder.append('\n');
                            }
                        } else {
                            builder.append('>');

                            if (node.hasChildNodes()) {
                                NodeList children = node.getChildNodes();
                                for (int i = 0; i < children.getLength(); i++) {
                                    embedStyles(builder, children.item(i), styleMap, options);
                                }
                            }

                            builder.append("</").append(nodeName).append('>');

                            if (options.isPrettyPrint()) {
                                builder.append('\n');
                            }
                        }
                        break;
                }
                break;

            case Node.COMMENT_NODE:
                if (isConditionalCommentNode(node)) {
                    builder.append("<!--").append(node.getNodeValue()).append("-->");
                }
                break;

            case Node.TEXT_NODE:
                if (StringUtils.isNotBlank(node.getNodeValue())) {
                    builder.append(node.getNodeValue());
                }
                break;

            default:
                builder.append(node.getNodeValue());

                if (options.isPrettyPrint()) {
                    builder.append('\n');
                }
                break;
        }
    }

    private static void appendStyleTag(StringBuilder builder, List<RuleBlock<?>> rules, DeclarationMap styleMap, Element element, StylesEmbeddingOptions options) {
        if (!rules.isEmpty()) {
            builder.append("<style");
            appendAttributes(builder, styleMap, element, options);
            builder.append('>');

            if (options.isPrettyPrint()) {
                builder.append('\n');
            }

            for (RuleBlock<?> rule : rules) {
                builder.append(rule.toString());
            }

            builder.append("</style>");

            if (options.isPrettyPrint()) {
                builder.append('\n');
            }
        }
    }

    private static void appendStyleTag(StringBuilder builder, String css, DeclarationMap styleMap, Element element, StylesEmbeddingOptions options) {
        if (StringUtils.isNotBlank(css)) {
            builder.append("<style");
            appendAttributes(builder, styleMap, element, options);
            builder.append('>');

            if (options.isPrettyPrint()) {
                builder.append('\n');
            }

            builder.append(css);

            builder.append("</style>");

            if (options.isPrettyPrint()) {
                builder.append('\n');
            }
        }
    }

    private static void appendStyleTag(StringBuilder builder, List<RuleBlock<?>> rules, StylesEmbeddingOptions options) {
        if (!rules.isEmpty()) {
            builder.append("<style type=\"text/css\">");

            if (options.isPrettyPrint()) {
                builder.append('\n');
            }

            for (RuleBlock<?> rule : rules) {
                builder.append(rule.toString());
            }

            builder.append("</style>");

            if (options.isPrettyPrint()) {
                builder.append('\n');
            }
        }
    }

    private static void appendStyleTag(StringBuilder builder, String css, StylesEmbeddingOptions options) {
        if (StringUtils.isNotBlank(css)) {
            builder.append("<style type=\"text/css\">");

            if (options.isPrettyPrint()) {
                builder.append('\n');
            }

            builder.append(css);

            builder.append("</style>");

            if (options.isPrettyPrint()) {
                builder.append('\n');
            }
        }
    }

    private static void appendAttributes(StringBuilder builder, DeclarationMap styleMap, Element element, StylesEmbeddingOptions options) {
        if (element.hasAttributes()) {
            NamedNodeMap attributes = element.getAttributes();

            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);

                String name = attribute.getNodeName();
                String value = attribute.getNodeValue();

                switch (name) {
                    case "style":
                        // All the styles (inline ones as well) are already accumulated in a style map - do nothing
                        break;

                    case "src":
                        if (options.getBaseUrl() != null && StringUtils.equals(element.getTagName(), "img") && !value.startsWith("[")) {
                            value = HttpUtils.resolveRelativeUri(options.getBaseUrl(), value);
                        }
                        //$FALL-THROUGH$

                    default:
                        value = value.replace("\n", "&#10;");
                        if (options.isEscapeAgnTags()) {
                            value = AgnTagUtils.escapeAgnTags(value);
                        }
                        builder.append(' ').append(name).append("=\"").append(value).append("\"");
                        break;
                }
            }
        }

        List<Declaration> declarations = styleMap.get(element);

        if (CollectionUtils.isNotEmpty(declarations)) {
            String styles = declarations.stream()
                    .map(declaration -> {
                        resolveUris(declaration);
                        return toString(declaration);
                    }).collect(Collectors.joining(" "));

            // Hack for IE (See GWUA-2423)
            if (StringUtils.equals(element.getTagName(), "img") && StringUtils.isBlank(element.getAttribute("border"))) {
                final String noBorder = "border:none;";
                final String noBottomBorder = "border-bottom-style:none;";
                final String noLeftBorder = "border-left-style:none;";
                final String noRightBorder = "border-right-style:none;";
                final String noTopBorder = "border-top-style:none;";

                final String s = styles.replaceAll("\\s+", "");

                if (s.contains(noBorder) || (s.contains(noBottomBorder) && s.contains(noLeftBorder) && s.contains(noRightBorder) && s.contains(noTopBorder))) {
                    if (!s.contains(noBorder)) {
                        styles += " border: none;";
                    }
                    builder.append(" border=\"0\"");
                }
            }

            builder.append(" style=\"").append(styles).append('\"');
        }
    }

    public static DeclarationMap getDeclarationMap(Document document, String encoding, URL base) {
        // Use empty media name to retrieve media-independent styles only.
        return getDeclarationMap(document, encoding, base, "");
    }

    public static DeclarationMap getDeclarationMap(Document document, String encoding, URL base, String media) {
        StyleSheet stylesheet = CSSFactory.getUsedStyles(document, encoding, base, media);
        return getDeclarationMap(document, stylesheet, media);
    }

    private static DeclarationMap getDeclarationMap(Document document, StyleSheet stylesheet, String media) {
        CssAnalyzer analyzer = new CssAnalyzer(stylesheet);
        return analyzer.getDeclarationMap(document, media);
    }

    /**
     * Try to parse enum from string. Return {@code null} If string is empty or does not have valid enum name
     */
    public static <T extends Enum<?>> T parseEnumSafe(Class<T> enumClass, String stringValue) {
        return parseEnumSafe(enumClass, stringValue, null);
    }

    /**
     * Try to parse enum from string. Return {@code defaultValue} If string is empty or does not have valid enum name
     */
    public static <T extends Enum<?>> T parseEnumSafe(Class<T> enumClass, String stringValue, T defaultValue) {
        if (StringUtils.isBlank(stringValue)) {
            return defaultValue;
        }
        T value = null;
        for (T enumEntry : enumClass.getEnumConstants()) {
            if (enumEntry.name().equals(stringValue)) {
                value = enumEntry;
                break;
            }
        }
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    /**
     * Replace:
     * <p><b>\n &emsp;->&emsp; &lt;br&gt;</b></p>
     * <p><b>\r &emsp;->&emsp; &lt;br&gt;</b></p>
     * <p><b>\t &emsp;->&emsp; &#38;emsp;</b></p>
     */
    public static String replaceLineFeedsForHTML(String text) {
        return text.replace("\r\n", "<br>\n").replace("\n", "<br>\n").replace("\r", "<br>\n").replace("\t", "&emsp;");
    }

    private static class CssAnalyzer extends Analyzer {
        public CssAnalyzer(StyleSheet sheet) {
            super(sheet);
        }

        public DeclarationMap getDeclarationMap(Document document, String media) {
            // To keep media queries working (if any) don't assign inherited styles.
            final boolean useInheritance = StringUtils.isEmpty(media);

            return assingDeclarationsToDOM(document, new MediaSpec(media), useInheritance);
        }
    }

    public static List<ParsingException> validate(String content) {
        return validate(content, Locale.getDefault());
    }

    public static List<ParsingException> validate(String content, Locale locale) {
        if (StringUtils.isEmpty(content)) {
            return Collections.emptyList();
        }

        try {
            SaxParser tokenizer = new SaxParser(new HtmlParsingHandler(locale), locale);
            tokenizer.parse(content);
        } catch (ParsingException e) {
            return Collections.singletonList(e);
        }

        return Collections.emptyList();
    }

    public static StylesEmbeddingOptionsBuilder stylesEmbeddingOptionsBuilder() {
        return new StylesEmbeddingOptionsBuilder();
    }

    public static class StylesEmbeddingOptions {
        private Charset encoding;
        private URL baseUrl;
        private String mediaType;
        private boolean escapeAgnTags;
        private boolean prettyPrint;
        private boolean useNewLib;

        private StylesEmbeddingOptions(StylesEmbeddingOptionsBuilder builder) {
            this.encoding = builder.encoding;
            this.baseUrl = builder.baseUrl;
            this.mediaType = builder.mediaType;
            this.escapeAgnTags = builder.escapeAgnTags;
            this.prettyPrint = builder.prettyPrint;
            this.useNewLib = builder.useNewLib;
        }

        public Charset getEncoding() {
            return encoding;
        }

        public String getEncodingName() {
            return encoding.name();
        }

        public URL getBaseUrl() {
            return baseUrl;
        }

        public String getMediaType() {
            return mediaType;
        }

        public boolean isEscapeAgnTags() {
            return escapeAgnTags;
        }

        public boolean isPrettyPrint() {
            return prettyPrint;
        }

        public boolean isUseNewLib() {
            return useNewLib;
        }
    }

    public static class StylesEmbeddingOptionsBuilder {
        private Charset encoding = StandardCharsets.UTF_8;
        private URL baseUrl;
        private String mediaType;
        private boolean escapeAgnTags;
        private boolean prettyPrint;
        private boolean useNewLib;

        public StylesEmbeddingOptionsBuilder setEncoding(Charset encoding) {
            this.encoding = Objects.requireNonNull(encoding, "encoding == null");
            return this;
        }

        public StylesEmbeddingOptionsBuilder setBaseUrl(URL baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public StylesEmbeddingOptionsBuilder setMediaType(String mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public StylesEmbeddingOptionsBuilder setEscapeAgnTags(boolean escapeAgnTags) {
            this.escapeAgnTags = escapeAgnTags;
            return this;
        }

        public StylesEmbeddingOptionsBuilder setPrettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
            return this;
        }

        public StylesEmbeddingOptionsBuilder setUseNewLib(boolean useNewLib) {
            this.useNewLib = useNewLib;
            return this;
        }

        public StylesEmbeddingOptions build() {
            return new StylesEmbeddingOptions(this);
        }
    }

    private static class HtmlParsingHandler implements SaxParsingHandler {
        private Locale locale;
        private Stack<String> stack = new Stack<>();

        public HtmlParsingHandler(Locale locale) {
            this.locale = locale;
        }

        @Override
        public void onOpeningTag(String name, Attributes attributes, boolean isStandalone) {
            if (!isStandalone && !isStandaloneTag(name)) {
                stack.push(name);
            }
        }

        @Override
        public void onClosingTag(String name) throws SAXException {
            if (stack.empty()) {
                throw exception("error.html.UnexpectedClosingTag", name);
            }

            String expected = stack.pop();
            if (!StringUtils.equals(name, expected)) {
                throw exception("error.html.UnexpectedClosingTagMixing", name, expected);
            }
        }

        @Override
        public void onEnd() throws SAXException {
            if (!stack.empty()) {
                throw exception("error.html.MissingClosingTag", stack.peek());
            }
        }

        private SAXException exception(String key, Object... args) {
            return new SAXException(I18nString.getLocaleString(key, locale, args));
        }
    }
}
