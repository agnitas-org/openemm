/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.preview.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

import com.agnitas.beans.Admin;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.Mediatype;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.preview.dto.PreviewResult;
import com.agnitas.emm.core.preview.form.PreviewForm;
import com.agnitas.emm.core.preview.service.MailingWebPreviewService;
import com.agnitas.emm.core.preview.service.PreviewSettings;
import com.agnitas.emm.core.servicemail.UnknownCompanyIdException;
import com.agnitas.mailing.preview.service.MailingPreviewService;
import com.agnitas.messages.I18nString;
import com.agnitas.preview.PreviewFactory;
import com.agnitas.preview.TAGCheckFactory;
import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MediaTypeStatus;
import com.agnitas.emm.core.mediatypes.factory.MediatypeFactory;
import com.agnitas.preview.AgnTagException;
import com.agnitas.preview.Page;
import com.agnitas.preview.Preview;
import com.agnitas.preview.PreviewHelper;
import com.agnitas.preview.TAGCheck;
import com.agnitas.util.SafeString;
import com.agnitas.util.importvalues.MailType;
import org.apache.commons.lang3.StringUtils;

public class MailingWebPreviewServiceImpl implements MailingWebPreviewService {

    protected MailingPreviewService previewService;
    private MailingDao mailingDao;
    private MediatypeFactory mediatypeFactory;
    private PreviewFactory previewFactory;
    private TAGCheckFactory tagCheckFactory;


    @Override
    public PreviewResult getPreview(PreviewSettings settings, int companyId, Admin admin) throws Exception {
        if (companyId <= 0) {
            throw new UnknownCompanyIdException(companyId);
        }

        final Mailing mailing = mailingDao.getMailing(settings.getMailingId(), companyId);
        if (mailing == null) {
            return new PreviewResult(settings.getFormat(), Optional.empty());
        }

        MediaTypes mediaType = castPreviewFormatToMediaType(settings.getFormat());
        if (mediaType == null) {
            mediaType = MediaTypes.EMAIL;
        }

        return renderMailingTypeDependentPreview(mediaType, mailing, admin.getLocale(), settings);
    }

    @Override
    public boolean isPostMailing(Mailing mailing) {
        return false;
    }

    @Override
    public List<Integer> getAvailablePreviewFormats(Mailing mailing) {
        return mailing.getMediatypes().values().stream()
                .filter(type -> type.getStatus() == MediaTypeStatus.Active.getCode())
                .map(Mediatype::getMediaType)
                .map(MediaTypes::getMediaCode)
                .sorted()
                .toList();
    }

    @Override
    public void updateActiveMailingPreviewFormat(PreviewForm previewForm, int mailingId, int companyID) {
        int activeFormat = computeActivePreviewFormat(previewForm.getFormat(), mailingId, companyID);
        previewForm.setFormat(activeFormat);
    }

    @Override
    public Page generateBackEndPreview(PreviewForm previewForm) {
        Preview.Size size = Preview.Size.getSizeById(previewForm.getSize());
        boolean isMobileView = size == Preview.Size.MOBILE_PORTRAIT || size == Preview.Size.MOBILE_LANDSCAPE;

        Preview preview = previewFactory.createPreview();
        Page output;
        switch (previewForm.getModeType()) {
            case TARGET_GROUP:
                output = preview.makePreview(previewForm.getMailingId(), 0, previewForm.getTargetGroupId(), isMobileView, previewForm.isAnon(), previewForm.isOnAnonPreserveLinks(), null);
                break;
            case RECIPIENT:
            case MANUAL:
            default:
                output = preview.makePreview(previewForm.getMailingId(), previewForm.getCustomerID(), false, isMobileView, previewForm.isAnon(), previewForm.isOnAnonPreserveLinks(), null);
                break;
        }

        preview.done();
        return output;
    }

    @Override
    public Page generateBackEndPreview(PreviewSettings settings) {
        final Preview.Size size = settings.getPreviewSize();
        final boolean isMobileView = size == Preview.Size.MOBILE_PORTRAIT || size == Preview.Size.MOBILE_LANDSCAPE;

        final Preview preview = previewFactory.createPreview();

        final Page output = switch (settings.getMode()) {
            case TARGET_GROUP ->
                    preview.makePreview(
                            settings.getMailingId(),
                            0,
                            settings.getTargetGroupId(),
                            isMobileView,
                            settings.isAnonymous(),
                            settings.isPreserveLinksWhenAnonymous(),
                            settings.getRdirDomain()
                    );
            default ->
                    preview.makePreview(
                            settings.getMailingId(),
                            settings.getCustomerId(),
                            false,
                            isMobileView,
                            settings.isAnonymous(),
                            settings.isPreserveLinksWhenAnonymous(),
                            settings.getRdirDomain()
                    );
        };

        preview.done();
        return output;
    }


    private MediaTypes castPreviewFormatToMediaType(int previewFormat) {
        int mediaTypeCode = previewFormat == INPUT_TYPE_TEXT ? INPUT_TYPE_TEXT : previewFormat - 1;

        if (mediatypeFactory.isTypeSupported(mediaTypeCode)) {
            Mediatype mediatype = mediatypeFactory.create(mediaTypeCode);
            return mediatype.getMediaType();
        }

        return null;
    }

    protected boolean isPostPreview(MediaTypes mediaType, Mailing mailing) {
        return false;
    }

    protected PreviewResult renderMailingTypeDependentPreview(MediaTypes mediaType, Mailing mailing, Locale locale, PreviewSettings settings) throws Exception {
        if (MediaTypes.EMAIL == mediaType) {
            Page previewPage = generateBackEndPreview(settings);

            if (MailingWebPreviewService.INPUT_TYPE_HTML == settings.getFormat()) {
                final String previewContent = settings.isNoImages() ? previewPage.getStrippedHTML() : previewPage.getHTML();

                if (previewContent == null) {
                    analyzeErrors(locale, mailing.getHtmlTemplate().getEmmBlock(), mailing);
                }

                return new PreviewResult(settings.getFormat(), Optional.ofNullable(previewContent));
            } else if (MailingWebPreviewService.INPUT_TYPE_TEXT == settings.getFormat()) {
                final String previewContent = previewPage.getText();

                if (previewContent != null) {
                    final Optional<String> content = Optional.of(StringUtils.defaultIfBlank(
                            StringUtils.substringBetween(previewContent, "<pre>", "</pre>"),
                            previewContent));
                    return new PreviewResult(settings.getFormat(), content);

                } else {
                    analyzeErrors(locale, mailing.getTextTemplate().getEmmBlock(), mailing);
                }
            } else {
                return new PreviewResult(settings.getFormat(), Optional.empty());
            }
        } else if (isPostPreview(mediaType, mailing)) {
            return new PreviewResult(settings.getFormat(), Optional.of(I18nString.getLocaleString("noPostalPreview.html", locale)));
        } else {
            MailingComponent component = mailing.getTemplate(mediaType);

            if (component != null) {
                final String previewContent = settings.getFormat() == MailType.TEXT.getIntValue()
                        ? previewService.renderTextPreview(mailing.getId(), settings.getCustomerId())
                        : previewService.renderHtmlPreview(mailing.getId(), settings.getCustomerId());

                return new PreviewResult(settings.getFormat(), Optional.ofNullable(previewContent));
            }
        }

        return new PreviewResult(settings.getFormat(), Optional.empty());
    }

    private void analyzeErrors(Locale locale, String template, Mailing mailing) throws Exception {
        Map<String, DynamicTag> dynTagsMap = mailing.getDynTags();
        analyzePreviewError(locale, template, dynTagsMap, mailing.getId());
    }

    private void analyzePreviewError(Locale locale, String template, Map<String, DynamicTag> dynTagsMap, int mailingID) throws Exception {
        List<String[]> errorReports = new ArrayList<>();
        Vector<String> outFailures = new Vector<>();
        TAGCheck tagCheck = tagCheckFactory.createTAGCheck(mailingID, locale);

        StringBuffer templateReport = new StringBuffer();
        if (!tagCheck.checkContent(template, templateReport, outFailures)) {
            appendErrorsToList(TEMPLATE, errorReports, templateReport);
        }

        for (DynamicTag tag : dynTagsMap.values()) {
            Map<Integer, DynamicTagContent> tagContentMap = tag.getDynContent();

            for (DynamicTagContent tagContentValue : tagContentMap.values()) {
                StringBuffer contentOutReport = new StringBuffer();

                if (!tagCheck.checkContent(tagContentValue.getDynContent(), contentOutReport, outFailures)) {
                    appendErrorsToList(tag.getDynName(), errorReports, contentOutReport);
                }
            }
        }

        if (errorReports.isEmpty()) {
            errorReports.add(new String[]{SafeString.getLocaleString("preview.error.empty", locale)});
        }

        throw new AgnTagException("error.template.dyntags", errorReports);
    }

    private void appendErrorsToList(String blockName, List<String[]> errorReports, StringBuffer templateReport) {
        Map<String, String> tagsWithErrors = PreviewHelper.getTagsWithErrors(templateReport);
        List<String> errorsWithoutATag = PreviewHelper.getErrorsWithoutATag(templateReport);

        for (Map.Entry<String, String> entry : tagsWithErrors.entrySet()) {
            String[] errorRow = new String[3];
            errorRow[0] = blockName; // block
            errorRow[1] = entry.getKey(); // tag
            errorRow[2] = entry.getValue(); // value

            errorReports.add(errorRow);
        }

        for (String error : errorsWithoutATag) {
            String[] errorRow = new String[3];
            errorRow[0] = blockName;
            errorRow[1] = "";
            errorRow[2] = error;
            errorReports.add(errorRow);
        }
    }

    private int computeActivePreviewFormat(int currentFormat, int mailingID, int companyID) {
        Mailing mailing = mailingDao.getMailing(mailingID, companyID);

        if (mailing.getId() == 0) {
            return UNDEFINED_PREVIEW_FORMAT;
        }

        // Get media types of mailing
        Map<Integer, Mediatype> mediaTypes = mailing.getMediatypes();
        List<Integer> orderedTypeCodes = new ArrayList<>(mediaTypes.keySet());

        /*
         * Here, we have to do some mapping.
         *
         * Preview format HTML and text (0 and 1) are both used by media type 0
         * preview format 2 is used for media type 1,
         * preview format 3 is used for media type 2, and so on.
         *
         * We have to use the media type later
         */
        int currentMediaType = currentFormat >= 2 ? currentFormat - 1 : 0;

        // Get media type of current select preview format
        Mediatype mt = mediaTypes.get(currentMediaType);

        // Check, that mailing has this media type and media type is used
        if (mt != null && mt.getStatus() == MediaTypeStatus.Active.getCode()) {
            return currentFormat;  // If so, keep this format as active
        }

        Collections.sort(orderedTypeCodes);
        for (int code : orderedTypeCodes) {
            if (mediaTypes.get(code).getStatus() == MediaTypeStatus.Active.getCode()) {
                /*
                 * Here, we have to do same mapping as above, but reverse now:
                 *
                 * Media type 0 maps to preview format 0,
                 * media type 1 maps to preview format 2,
                 * media type 2 maps to preview format 3,
                 * and so on
                 */
                if (code == 0) {
                    return code;
                } else {
                    return code + 1;
                }
            }
        }

        return UNDEFINED_PREVIEW_FORMAT;
    }

    public void setMailingDao(MailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    public void setMediatypeFactory(MediatypeFactory mediatypeFactory) {
        this.mediatypeFactory = mediatypeFactory;
    }

    public void setPreviewFactory(PreviewFactory previewFactory) {
        this.previewFactory = previewFactory;
    }

    public void setTagCheckFactory(TAGCheckFactory tagCheckFactory) {
        this.tagCheckFactory = tagCheckFactory;
    }

    public void setPreviewService(MailingPreviewService previewService) {
        this.previewService = previewService;
    }
}
