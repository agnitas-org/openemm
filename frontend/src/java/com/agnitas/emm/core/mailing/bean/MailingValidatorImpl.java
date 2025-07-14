/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.MailingComponent;
import com.agnitas.preview.AgnTagError;
import com.agnitas.preview.PreviewHelper;
import com.agnitas.preview.TAGCheck;
import com.agnitas.preview.TAGCheckFactory;
import com.agnitas.util.AgnTagUtils;
import com.agnitas.util.DynTagException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.mailing.bean.impl.MailingValidator;
import com.agnitas.messages.I18nString;
import com.agnitas.service.AgnDynTagGroupResolverFactory;
import com.agnitas.service.AgnTagService;
import com.agnitas.web.MailingContentChecker;
import com.agnitas.web.mvc.Popups;

@Component
public class MailingValidatorImpl implements MailingValidator {

    private static final Logger LOGGER = LogManager.getLogger(MailingValidatorImpl.class);
    private static final String TEMPLATE_DYNTAGS_ERROR_KEY = "error.template.dyntags";

    private final AgnDynTagGroupResolverFactory agnDynTagGroupResolverFactory;
    private final ApplicationContext applicationContext;
    private final TAGCheckFactory tagCheckFactory;
    private final AgnTagService agnTagService;
    private final LinkService linkService;

    public MailingValidatorImpl(AgnDynTagGroupResolverFactory agnDynTagGroupResolverFactory, ApplicationContext applicationContext, TAGCheckFactory tagCheckFactory, AgnTagService agnTagService, LinkService linkService) {
        this.agnDynTagGroupResolverFactory = agnDynTagGroupResolverFactory;
        this.applicationContext = applicationContext;
        this.tagCheckFactory = tagCheckFactory;
        this.agnTagService = agnTagService;
        this.linkService = linkService;
    }

    @Override
    public void validateMailingBeforeSave(Mailing mailing, Locale locale, Popups popups) throws Exception {
        validateMailingTagsAndComponents(mailing, locale, popups);

        String rdirDomain = linkService.getRdirDomain(mailing.getCompanyID());

        mailing.getComponents().forEach((name, component) -> {
            if (component.getEmmBlock() != null) {
                String text = AgnTagUtils.unescapeAgnTags(component.getEmmBlock());
                component.setEmmBlock(text, component.getMimeType());

                Integer rdirLinkLine = linkService.getLineNumberOfFirstRdirLink(rdirDomain, text);
                if (rdirLinkLine != null) {
                    popups.warning("warning.mailing.link.encoded", name, rdirLinkLine);
                }
            }
        });
        MailingContentChecker.checkHtmlWarningConditions(mailing, popups);
    }

    @Override
    public void validateMailingTagsAndComponents(Mailing mailing, Locale locale, Popups popups) throws Exception {
        List<String[]> errorReports = new ArrayList<>();

        Map<String, List<AgnTagError>> agnTagsValidationErrors = mailing.checkAgnTagSyntax(applicationContext);
        if (MapUtils.isNotEmpty(agnTagsValidationErrors)) {
            validateMailingTags(popups, errorReports, agnTagsValidationErrors, locale);
        } else {
            validateMailingComponents(mailing, locale, popups, errorReports);
        }

        if (CollectionUtils.isNotEmpty(errorReports)) {
            String report = errorReports.stream()
                    .map(list -> String.join(", ", list))
                    .collect(Collectors.joining("\n"));
            popups.exactAlert(I18nString.getLocaleString(TEMPLATE_DYNTAGS_ERROR_KEY, locale) + "<br><br>" + report);
        }
    }

    private void validateMailingTags(Popups popups, List<String[]> errorReports, Map<String, List<AgnTagError>> agnTagsValidationErrors, Locale locale) {
        agnTagsValidationErrors.forEach((componentName, validationErrors) -> {
            // noinspection ThrowableResultOfMethodCallIgnored

            if (componentName.startsWith("agn")) {
                componentName = componentName.substring(3);
            }
            AgnTagError firstError = validationErrors.get(0);
            popups.alert("error.agntag.mailing.component", componentName, firstError.getFullAgnTagText());
            popups.alert(firstError.getErrorKey().getMessageKey(), firstError.getAdditionalErrorDataWithLineInfo());

            for (AgnTagError error : validationErrors) {
                errorReports.add(new String[]{componentName, error.getFullAgnTagText(), error.getLocalizedMessage(locale)});
            }
        });
    }

    private void validateMailingComponents(Mailing mailing, Locale locale, Popups popups, List<String[]> errorReports) throws Exception {
        Map<String, MailingComponent> components = mailing.getComponents();
        if (validateDeprecatedTags(mailing.getCompanyID(), components, popups) && mailing.getId() > 0) {
            // Only use backend/preview agn syntax check if mailing was already stored in database before (= not new mailing)
            List<String> outFailures = new ArrayList<>();
            TAGCheck tagCheck = tagCheckFactory.createTAGCheck(mailing.getCompanyID(), mailing.getId(), mailing.getMailinglistID(), locale);

            try {
                components.forEach((name, component) -> {
                    StringBuffer reportContents = new StringBuffer();
                    if (component.getEmmBlock() != null && !tagCheck.checkContent(component.getEmmBlock(), reportContents, outFailures)) {
                        appendErrorsToList(name, errorReports, reportContents);
                    }
                });
            } finally {
                tagCheck.done();
            }
        }
    }

    /**
     * Creates report about errors in dynamic tags.
     *
     * @param blockName      name of content block with invalid content
     * @param errorReports   list of messages about parsing errors (is changing inside the method)
     * @param templateReport content with errors
     */
    private void appendErrorsToList(String blockName, List<String[]> errorReports, StringBuffer templateReport) {
        Map<String, String> tagsWithErrors = PreviewHelper.getTagsWithErrors(templateReport);
        for (Map.Entry<String, String> entry : tagsWithErrors.entrySet()) {
            String[] errorRow = new String[3];
            errorRow[0] = blockName; // block
            errorRow[1] = entry.getKey(); // tag
            errorRow[2] = entry.getValue(); // value

            errorReports.add(errorRow);
        }
        List<String> errorsWithoutATag = PreviewHelper.getErrorsWithoutATag(templateReport);
        for (String error : errorsWithoutATag) {
            String[] errorRow = new String[3];
            errorRow[0] = blockName;
            errorRow[1] = "";
            errorRow[2] = error;
            errorReports.add(errorRow);
        }
    }

    private boolean validateDeprecatedTags(final int companyId, final Map<String, MailingComponent> components, Popups popups) {
        boolean valid = true;
        for (MailingComponent component : components.values()) {
            if (component.getEmmBlock() != null) {
                final Set<String> deprecatedTagsNames = agnTagService.parseDeprecatedTagNamesFromString(component.getEmmBlock(), companyId);
                if (CollectionUtils.isNotEmpty(deprecatedTagsNames)) {
                    deprecatedTagsNames.forEach(el -> popups.warning("warning.mailing.agntag.deprecated", el));
                    valid = false;
                }
            }
        }
        return valid;
    }

    /**
     * Validate agn-tags and dyn-tags.
     */
    @Override
    public void validateMailingModules(Mailing mailing, Popups popups) {
        try {
            validateMailingModule(mailing.getTextTemplate());
            validateMailingModule(mailing.getHtmlTemplate());
        } catch (DynTagException e) {
            LOGGER.info("General error in tag", e);
            popups.alert("error.template.dyntags.general_tag_error", e.getLineNumber(), e.getTag());
        }
    }

    private void validateMailingModule(MailingComponent template) throws DynTagException {
        if (template != null) {
            agnTagService.getDynTags(template.getEmmBlock(), agnDynTagGroupResolverFactory.create(template.getCompanyID(), template.getMailingID()));
        }
    }
}
