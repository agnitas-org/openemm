/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.preview.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.Mediatype;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.preview.form.PreviewForm;
import com.agnitas.emm.core.preview.service.MailingWebPreviewService;
import com.agnitas.mailing.preview.service.MailingPreviewService;
import com.agnitas.messages.I18nString;
import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.emm.core.mediatypes.factory.MediatypeFactory;
import org.agnitas.preview.AgnTagException;
import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewFactory;
import org.agnitas.preview.PreviewHelper;
import org.agnitas.preview.TAGCheck;
import org.agnitas.preview.TAGCheckFactory;
import org.agnitas.util.SafeString;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

public class MailingWebPreviewServiceImpl implements MailingWebPreviewService {

    protected MailingPreviewService previewService;
    private ComMailingDao mailingDao;
    private MediatypeFactory mediatypeFactory;
    private PreviewFactory previewFactory;
    private TAGCheckFactory tagCheckFactory;

    @Override
    public String getPreviewWidth(Preview.Size size) {
        int width;

        switch (size) {
            case DESKTOP:
                return "100%";
            case MOBILE_PORTRAIT:
                width = 320;
                break;
            case MOBILE_LANDSCAPE:
                width = 356;
                break;
            case TABLET_PORTRAIT:
                width = 768;
                break;
            case TABLET_LANDSCAPE:
                width = 1024;
                break;
            default:
                width = 800;
                break;
        }

        return width + 2 + "px";
    }

    @Override
    public String getMediaQuery(Preview.Size size) {
        String mediaQuery;

        switch (size) {
            case MOBILE_PORTRAIT:
            case MOBILE_LANDSCAPE:
            case TABLET_PORTRAIT:
                mediaQuery = "true";
                break;
            case TABLET_LANDSCAPE:
            case DESKTOP:
            default:
                mediaQuery = "false";
                break;
        }

        return mediaQuery;
    }

    @Override
    public String getPreview(PreviewForm previewForm, int companyId, Admin admin) throws Exception {
        if (companyId <= 0) {
            companyId = admin.getCompanyID();
        }

        int previewFormat = previewForm.getFormat();

        Mailing mailing = mailingDao.getMailing(previewForm.getMailingId(), companyId);
        if (mailing == null) {
            return "preview." + previewFormat;
        }

        MediaTypes mediaType = castPreviewFormatToMediaType(previewFormat);
        if (mediaType == null) {
            mediaType = MediaTypes.EMAIL;
            previewFormat = MailingWebPreviewService.INPUT_TYPE_TEXT;
            previewForm.setFormat(previewFormat);
        }

        renderMailingTypeDependentPreview(mediaType, previewFormat, mailing, admin, previewForm);

        return "preview." + previewFormat;
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
                .collect(Collectors.toList());
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
                output = preview.makePreview(previewForm.getMailingId(), 0, previewForm.getTargetGroupId(), isMobileView, previewForm.isAnon(), previewForm.isOnAnonPreserveLinks());
                break;
            case RECIPIENT:
            default:
                output = preview.makePreview(previewForm.getMailingId(), previewForm.getCustomerID(), false, isMobileView, previewForm.isAnon(), previewForm.isOnAnonPreserveLinks());
                break;
        }

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

    protected void renderMailingTypeDependentPreview(MediaTypes mediaType, int previewFormat, Mailing mailing, Admin admin, PreviewForm previewForm) throws Exception {
        if (MediaTypes.EMAIL == mediaType) {
            Page previewPage = generateBackEndPreview(previewForm);

            if (MailingWebPreviewService.INPUT_TYPE_HTML == previewFormat) {
                String previewContent = previewForm.isNoImages() ? previewPage.getStrippedHTML() : previewPage.getHTML();

                if (previewContent != null) {
                    //mobile image urls are resolved in makePreview by isMobile parameter
                    previewForm.setPreviewContent(previewContent);
                } else {
                    analyzeErrors(admin, mailing.getHtmlTemplate().getEmmBlock(), mailing.getId(), mailing);
                }
            }

            if (MailingWebPreviewService.INPUT_TYPE_TEXT == previewFormat) {
                final String previewContent = previewPage.getText();

                if (previewContent != null) {
                    previewForm.setPreviewContent(StringUtils.defaultIfBlank(
                            StringUtils.substringBetween(previewContent, "<pre>", "</pre>"),
                            previewContent));
                } else {
                    analyzeErrors(admin, mailing.getTextTemplate().getEmmBlock(), mailing.getId(), mailing);
                }
            }
        } else if (isPostPreview(mediaType, mailing)) {
            previewForm.setPreviewContent(I18nString.getLocaleString("noPostalPreview.html", admin.getLocale()));
        } else {
            MailingComponent component = mailing.getTemplate(mediaType);

            if (component != null) {
                final String previewContent = previewFormat == MailType.TEXT.getIntValue()
                        ? previewService.renderTextPreview(mailing.getId(), previewForm.getCustomerID())
                        : previewService.renderHtmlPreview(mailing.getId(), previewForm.getCustomerID());

                if (previewContent != null) {
                    previewForm.setPreviewContent(previewContent);
                }
            }
        }
    }

    private void analyzeErrors(Admin admin, String template, int mailingId, Mailing mailing) throws Exception {
        Map<String, DynamicTag> dynTagsMap = mailing.getDynTags();
        analyzePreviewError(admin, template, dynTagsMap, mailingId);
    }

    private void analyzePreviewError(Admin admin, String template, Map<String, DynamicTag> dynTagsMap, int mailingID) throws Exception {
        List<String[]> errorReports = new ArrayList<>();
        Vector<String> outFailures = new Vector<>();
        TAGCheck tagCheck = tagCheckFactory.createTAGCheck(mailingID, admin.getLocale());

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

        if (errorReports.size() == 0) {
            errorReports.add(new String[]{SafeString.getLocaleString("preview.error.empty", admin.getLocale())});
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

    @Required
    public void setMailingDao(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Required
    public void setMediatypeFactory(MediatypeFactory mediatypeFactory) {
        this.mediatypeFactory = mediatypeFactory;
    }

    @Required
    public void setPreviewFactory(PreviewFactory previewFactory) {
        this.previewFactory = previewFactory;
    }

    @Required
    public void setTagCheckFactory(TAGCheckFactory tagCheckFactory) {
        this.tagCheckFactory = tagCheckFactory;
    }

    @Required
    public void setPreviewService(MailingPreviewService previewService) {
        this.previewService = previewService;
    }
}
