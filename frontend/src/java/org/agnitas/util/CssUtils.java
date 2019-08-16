/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import com.helger.commons.io.resource.URLResource;
import com.helger.commons.system.ENewLineMode;
import com.helger.css.ECSSVersion;
import com.helger.css.ICSSWriteable;
import com.helger.css.decl.CSSImportRule;
import com.helger.css.decl.CSSMediaExpression;
import com.helger.css.decl.CSSMediaQuery;
import com.helger.css.decl.CSSMediaRule;
import com.helger.css.decl.CSSNamespaceRule;
import com.helger.css.decl.CSSStyleRule;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.decl.ICSSTopLevelRule;
import com.helger.css.reader.CSSReader;
import com.helger.css.writer.CSSWriterSettings;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang.StringUtils;

public class CssUtils {
    private static final String DEFAULT_INDENT = "    ";

    public static String stripEmbeddableStyles(String css, String mediaType, boolean prettyPrint) {
        CascadingStyleSheet styles = CSSReader.readFromString(css, StandardCharsets.UTF_8, ECSSVersion.CSS30);
        return stripEmbeddableStyles(styles, mediaType, prettyPrint);
    }

    public static String stripEmbeddableStyles(URL remoteStyleSheet, String mediaType, boolean prettyPrint) {
        CascadingStyleSheet styles = CSSReader.readFromStream(new URLResource(remoteStyleSheet), StandardCharsets.UTF_8, ECSSVersion.CSS30);
        return stripEmbeddableStyles(styles, mediaType, prettyPrint);
    }

    private static String stripEmbeddableStyles(CascadingStyleSheet styles, String mediaType, boolean prettyPrint) {
        if (styles == null) {
            return "";
        }

        CSSWriterSettings settings = new CSSWriterSettings(ECSSVersion.CSS30);
        settings.setNewLineMode(ENewLineMode.UNIX);

        if (prettyPrint) {
            settings.setIndent(DEFAULT_INDENT);
        } else {
            settings.setOptimizedOutput(true);
        }

        stripEmbeddableRules(styles);

        if (isExactMediaType(mediaType)) {
            stripMediaType(styles, mediaType);
        }

        return toString(styles, settings);
    }

    private static void stripEmbeddableRules(CascadingStyleSheet styles) {
        // Remove all media-independent style rules.
        styles.removeRules(CSSStyleRule.class::isInstance);
    }

    private static void stripMediaType(CascadingStyleSheet styles, String useType) {
        if (!isExactMediaType(useType)) {
            throw new IllegalArgumentException("A media type to strip should be neither `all` nor empty");
        }

        for (CSSImportRule rule : styles.getAllImportRules()) {
            if (stripMediaType(rule, useType)) {
                styles.removeImportRule(rule);
            }
        }

        for (CSSMediaRule rule : styles.getAllMediaRules()) {
            if (stripMediaType(rule, useType)) {
                styles.removeRule(rule);
            }
        }
    }

    private static boolean stripMediaType(CSSImportRule rule, String useType) {
        return stripMediaType(CSSRuleAdapter.wrap(rule), useType);
    }

    private static boolean stripMediaType(CSSMediaRule rule, String useType) {
        if (stripMediaType(CSSRuleAdapter.wrap(rule), useType)) {
            return true;
        }

        for (CSSMediaRule child : rule.getAllMediaRules()) {
            if (stripMediaType(child, useType)) {
                rule.removeRule(child);
            }
        }

        return false;
    }

    private static boolean stripMediaType(CSSRuleAdapter rule, String useType) {
        if (rule.hasMediaQueries()) {
            List<CSSMediaQuery> queries = rule.getAllMediaQueries().stream()
                    .map(q -> stripMediaType(q, useType))
                    .filter(q -> !isAlwaysNegative(q))
                    .collect(Collectors.toList());

            if (queries.isEmpty()) {
                return true;
            } else {
                rule.removeAllMediaQueries();

                if (IterableUtils.matchesAny(queries, CssUtils::isAlwaysPositive)) {
                    rule.addMediaQuery(new CSSMediaQuery("all"));
                } else {
                    queries.forEach(rule::addMediaQuery);
                }
            }
        }

        return false;
    }

    private static CSSMediaQuery stripMediaType(CSSMediaQuery query, String useType) {
        if (useType.equalsIgnoreCase(query.getMedium())) {
            CSSMediaQuery newQuery = new CSSMediaQuery(query.getModifier(), "all");

            for (CSSMediaExpression expression : query.getAllMediaExpressions()) {
                newQuery.addMediaExpression(expression);
            }

            return newQuery;
        } else {
            return query;
        }
    }

    // Attention: this method does only make sense after media type strip.
    private static boolean isAlwaysPositive(CSSMediaQuery query) {
        if (query.isNot()) {
            return isExactMediaType(query.getMedium());
        } else {
            return !(query.isOnly() || query.hasMediaExpressions());
        }
    }

    // Attention: this method does only make sense after media type strip.
    private static boolean isAlwaysNegative(CSSMediaQuery query) {
        return !query.isNot() && isExactMediaType(query.getMedium());
    }

    private static String toString(CascadingStyleSheet styles, CSSWriterSettings settings) {
        StringBuilder builder = new StringBuilder();
        toString(builder, styles, settings);
        return builder.toString();
    }

    private static void toString(StringBuilder builder, CascadingStyleSheet styles, CSSWriterSettings settings) {
        for (CSSImportRule rule : styles.getAllImportRules()) {
            toString(builder, rule, settings);
        }

        for (CSSNamespaceRule rule : styles.getAllNamespaceRules()) {
            toString(builder, rule, settings);
        }

        for (ICSSTopLevelRule rule : styles.getAllRules()) {
            toString(builder, rule, settings);
        }
    }

    private static void toString(StringBuilder builder, ICSSWriteable writeable, CSSWriterSettings settings) {
        builder.append(writeable.getAsCSSString(settings));

        if (!settings.isOptimizedOutput()) {
            builder.append('\n');
        }
    }

    private static boolean isExactMediaType(String mediaType) {
        return StringUtils.isNotEmpty(mediaType) && !mediaType.equalsIgnoreCase("all");
    }

    private interface CSSRuleAdapter {
        static CSSRuleAdapter wrap(CSSImportRule rule) {
            return new CSSImportRuleAdapter(rule);
        }

        static CSSRuleAdapter wrap(CSSMediaRule rule) {
            return new CSSMediaRuleAdapter(rule);
        }

        boolean hasMediaQueries();
        List<CSSMediaQuery> getAllMediaQueries();
        void removeAllMediaQueries();
        void addMediaQuery(CSSMediaQuery mediaQuery);
    }

    private static class CSSImportRuleAdapter implements CSSRuleAdapter {
        private CSSImportRule rule;

        private CSSImportRuleAdapter(CSSImportRule rule) {
            this.rule = rule;
        }

        @Override
        public boolean hasMediaQueries() {
            return rule.hasMediaQueries();
        }

        @Override
        public List<CSSMediaQuery> getAllMediaQueries() {
            return rule.getAllMediaQueries();
        }

        @Override
        public void removeAllMediaQueries() {
            rule.removeAllMediaQueries();
        }

        @Override
        public void addMediaQuery(CSSMediaQuery mediaQuery) {
            rule.addMediaQuery(mediaQuery);
        }
    }

    private static class CSSMediaRuleAdapter implements CSSRuleAdapter {
        private CSSMediaRule rule;

        private CSSMediaRuleAdapter(CSSMediaRule rule) {
            this.rule = rule;
        }

        @Override
        public boolean hasMediaQueries() {
            return rule.hasMediaQueries();
        }

        @Override
        public List<CSSMediaQuery> getAllMediaQueries() {
            return rule.getAllMediaQueries();
        }

        @Override
        public void removeAllMediaQueries() {
            rule.removeAllMediaQueries();
        }

        @Override
        public void addMediaQuery(CSSMediaQuery mediaQuery) {
            rule.addMediaQuery(mediaQuery);
        }
    }
}
