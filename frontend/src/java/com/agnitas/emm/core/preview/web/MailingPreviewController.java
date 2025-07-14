/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.preview.web;

import static com.agnitas.util.Const.Mvc.ERROR_MSG;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.impl.RecipientLiteImpl;
import com.agnitas.dao.MailingComponentDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.RecipientDao;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.preview.dto.MailingPreviewSettings;
import com.agnitas.emm.core.preview.dto.PreviewResult;
import com.agnitas.emm.core.preview.form.PreviewForm;
import com.agnitas.emm.core.preview.service.MailingWebPreviewService;
import com.agnitas.emm.core.preview.service.PreviewSettings;
import com.agnitas.emm.core.recipient.service.RecipientType;
import com.agnitas.preview.TAGCheckFactory;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.PdfService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.agnitas.beans.Mailinglist;
import com.agnitas.emm.core.mailinglist.dao.MailinglistDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.service.MailingModel;
import com.agnitas.preview.AgnTagException;
import com.agnitas.preview.ModeType;
import com.agnitas.preview.Page;
import com.agnitas.preview.Preview;
import com.agnitas.preview.PreviewHelper;
import com.agnitas.preview.TAGCheck;
import com.agnitas.util.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

public class MailingPreviewController implements XssCheckAware {

    private static final Logger LOGGER = LogManager.getLogger(MailingPreviewController.class);

    protected final RecipientDao recipientDao;
    private final MailingDao mailingDao;
    private final MailinglistDao mailinglistDao;
    private final MailingBaseService mailingBaseService;
    private final MailingService mailingService;
    private final MailingWebPreviewService previewService;
    private final GridServiceWrapper gridService;
    private final MailingComponentDao mailingComponentDao;
    private final TAGCheckFactory tagCheckFactory;
    private final ConfigService configService;
    private final PdfService pdfService;
    private final MaildropService maildropService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final WebStorage webStorage;

    public MailingPreviewController(MailingDao mailingDao, MailinglistDao mailinglistDao, RecipientDao recipientDao, MailingService mailingService,
                                    MailingWebPreviewService previewService, GridServiceWrapper gridService, MailingComponentDao mailingComponentDao,
                                    TAGCheckFactory tagCheckFactory, MailingBaseService mailingBaseService, ConfigService configService,
                                    PdfService pdfService, MaildropService maildropService, MailinglistApprovalService mailinglistApprovalService, WebStorage webStorage) {
        this.mailingDao = mailingDao;
        this.mailinglistDao = mailinglistDao;
        this.recipientDao = recipientDao;
        this.mailingService = mailingService;
        this.previewService = previewService;
        this.gridService = gridService;
        this.mailingComponentDao = mailingComponentDao;
        this.tagCheckFactory = tagCheckFactory;
        this.mailingBaseService = mailingBaseService;
        this.configService = configService;
        this.pdfService = pdfService;
        this.maildropService = maildropService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.webStorage = webStorage;
    }

    @RequestMapping("/{mailingId:\\d+}/view.action")
    public String view(@PathVariable("mailingId") int mailingId, @ModelAttribute("form") PreviewForm form, Admin admin, Model model, Popups popups) {
        if (!form.isForEmcTextModules()) {
            syncStoragePreviewSettings(mailingId, form);
        }
        int companyId = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(mailingId, companyId);

        initFormData(mailingId, form, mailing, companyId);

        model.addAttribute("availablePreviewFormats", previewService.getAvailablePreviewFormats(mailing));
        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("isActiveMailing", maildropService.isActiveMailing(mailingId, companyId));
            model.addAttribute("mailinglistDisabled", !mailinglistApprovalService.isAdminHaveAccess(admin, mailing.getMailinglistID()));
            model.addAttribute("previewSizes", Preview.Size.values());
        } else {
            model.addAttribute("isPostMailing", previewService.isPostMailing(mailing));
            model.addAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(admin, mailingId));
        }

        if (!mailinglistDao.exist(mailing.getMailinglistID(), companyId)) {
            model.addAttribute("mailingListExist", false);
            return "mailing_preview_select";
        }

        previewService.updateActiveMailingPreviewFormat(form, mailingId, companyId);
        List<RecipientLiteImpl> recipientList = recipientDao.getMailingAdminAndTestRecipients(mailingId, companyId);
        addPreviewRecipientsModelAttrs(model, recipientList, form.getPersonalizedTestRunRecipients(), admin);

        if ((form.getModeType() == ModeType.RECIPIENT || form.getModeType() == ModeType.MANUAL) && mailingDao.hasPreviewRecipients(mailingId, companyId)) {
            boolean useCustomerEmail = admin.isRedesignedUiUsed()
                    ? form.getModeType().equals(ModeType.MANUAL)
                    : form.isUseCustomerEmail();
            choosePreviewCustomerId(companyId, mailingId, form, useCustomerEmail, popups, recipientList);
        }

        model.addAttribute("availableTargetGroups", mailingService.listTargetGroupsOfMailing(companyId, mailingId));

        loadPreviewHeaderData(mailing, form, admin, model, popups);

        return "mailing_preview_select";
    }

    private void syncStoragePreviewSettings(int mailingId, PreviewForm form) {
        webStorage.access(WebStorage.MAILING_PREVIEW, entry -> {
            MailingPreviewSettings settings = entry.getSettings(mailingId);
            if (form.isReload()) {
                settings.setFormat(form.getFormat());
                settings.setSize(form.getSize());
                settings.setModeType(form.getModeType());
                settings.setCustomerEmail(form.getCustomerEmail());
                settings.setNoImages(form.isNoImages());
                settings.setTargetId(form.getTargetGroupId());
            } else {
                form.setFormat(settings.getFormat());
                form.setSize(settings.getSize());
                form.setModeType(settings.getModeType());
                form.setCustomerEmail(settings.getCustomerEmail());
                form.setTargetGroupId(settings.getTargetId());
                form.setNoImages(settings.isNoImages());
            }
        });
    }

    protected void addPreviewRecipientsModelAttrs(Model model, List<RecipientLiteImpl> recipientList, List<String> personalizedTestRunRecipients, Admin admin) {
        model.addAttribute("previewRecipients", recipientList);
    }

    @GetMapping("/view-content.action")
    public String viewContent(@ModelAttribute("form") PreviewForm form, Model model, Admin admin, Popups popups) {
        try {
            final PreviewSettings settings = formToSettingsBuilder(form).build();
            final PreviewResult previewResult = previewService.getPreview(settings, admin.getCompanyID(), admin);
            form.setPreviewContent(previewResult.getPreviewContent().orElse(""));

            return String.format("preview.%d", previewResult.getPreviewFormat()) ;
        } catch (AgnTagException agnTagException) {
            model.addAttribute("errorReport", agnTagException.getReport());
            popups.alert("error.template.dyntags");
        } catch (Exception e) {
            popups.alert(ERROR_MSG);
        }
        return "mailing_preview_errors";
    }

    @GetMapping("/html.action")
    public Object downloadHtml(PreviewForm previewForm, Admin admin, Model model, Popups popups) {
        try {
            return tryGetMailingHtml(previewForm, admin);
        } catch (AgnTagException agnTagException) {
            model.addAttribute("errorReport", agnTagException.getReport());
            popups.alert("error.template.dyntags");
        } catch (Exception e) {
            popups.alert(ERROR_MSG);
        }
        return "mailing_preview_errors";
    }

    private ResponseEntity<byte[]> tryGetMailingHtml(PreviewForm previewForm, Admin admin) throws Exception {
        final PreviewSettings settings = formToSettingsBuilder(previewForm)
                .withAnonymous(true, true)
                .build();

        final PreviewResult previewResult = previewService.getPreview(settings, admin.getCompanyID(), admin);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/html"))
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpUtils.getContentDispositionAttachment(
                        getHtmlDownloadFileName(previewForm.getMailingId(), admin.getCompanyID()), StandardCharsets.UTF_8.name()))
                .body(previewResult.getPreviewContent().orElse("").getBytes(StandardCharsets.UTF_8));
    }

    private String getHtmlDownloadFileName(int mailingId, int companyId) {
        String mailingName = mailingBaseService.getMailingName(mailingId, companyId);
        return String.format("%s_%s.html", mailingName, mailingId);
    }

    @GetMapping(value = "/pdf.action", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    FileSystemResource saveAsPdf(@ModelAttribute PreviewForm form, Admin admin, HttpSession session, HttpServletResponse response) throws Exception {
        if(configService.getBooleanValue(ConfigValue.Development.UseLocalPreview, admin.getCompanyID())) {
            final PreviewSettings previewSettings = PreviewSettings.builder()
                    .withMailingId(form.getMailingId())
                    .withPreviewFormat(form.getFormat())
                    .withPreviewSize(Preview.Size.getSizeById(form.getSize()))
                    .withMode(ModeType.getByCode(form.getModeTypeId()))
                    .withTargetGroupId(form.getTargetGroupId())
                    .withCustomerId(form.getCustomerID())
                    .withNoImages(form.isNoImages())
                    .withRdirDomain(configService.getPreviewBaseUrl())
                    .build();

            final String mailingName = mailingDao.getMailingName(form.getMailingId(), admin.getCompanyID());

            final File pdfFile = pdfService.generatePDF(admin, previewSettings, false, mailingName, "Mailing");

            HttpUtils.setDownloadFilenameHeader(response, mailingName + ".pdf");
            return new DeleteFileAfterSuccessReadResource(pdfFile);
        } else {
            final String baseUrl = configService.getPreviewBaseUrl();
            final String url = String.format("%s/mailing/preview/view-content.action;jsessionid=%s?mailingId=%d&format=%d&size=%d&modeTypeId=%d&targetGroupId=%d&customerID=%d&noImages=%s&internal=true",
                    baseUrl, session.getId(), form.getMailingId(), form.getFormat(), form.getSize(), form.getModeTypeId(), form.getTargetGroupId(), form.getCustomerID(), form.isNoImages());

            final String mailingName = mailingDao.getMailingName(form.getMailingId(), admin.getCompanyID());

            final File pdfFile = pdfService.generatePDF(admin, url, false, mailingName, "Mailing");

            HttpUtils.setDownloadFilenameHeader(response, mailingName + ".pdf");
            return new DeleteFileAfterSuccessReadResource(pdfFile);
        }

    }

    private void initFormData(int mailingId, PreviewForm form, Mailing mailing, int companyId) {
        int gridTemplateId = gridService.getGridTemplateIdByMailingId(mailingId);
        int workflowId = mailingBaseService.getWorkflowId(mailingId, companyId);

        form.setMailingId(mailingId);
        form.setMailingGrid(gridTemplateId > 0);
        form.setMailingShortname(mailing.getShortname());
        form.setMailingTemplateId(gridTemplateId);
        form.setTemplate(mailing.isIsTemplate());
        form.setMailingUndoAvailable(mailingBaseService.checkUndoAvailable(mailingId));
        form.setWorkflowId(workflowId);
        form.setMediaQuery(Boolean.toString(Preview.Size.getSizeById(form.getSize()).isMediaQuery()));
        form.setWidth(Preview.Size.getSizeById(form.getSize()).getCssWidth());

        if (mailing.getEmailParam() == null || mailing.getEmailParam().getMailFormat() == MailingModel.Format.TEXT.getCode()) {
            form.setEmailFormat(MediaTypes.EMAIL.getMediaCode());
            form.setFormat(MailingWebPreviewService.INPUT_TYPE_TEXT);
        } else {
            form.setEmailFormat(mailing.getEmailParam().getMailFormat());
        }
        if (form.isForEmcTextModules()) {
            form.setFormat(MailingWebPreviewService.INPUT_TYPE_TEXT);
        }
    }

    private void loadPreviewHeaderData(Mailing mailing, PreviewForm form, Admin admin, Model model, Popups popups) {
        if (mailing == null) {
            return;
        }

        String subjectParameter = mailing.getEmailParam().getSubject();
        String preHeaderParameter = mailing.getEmailParam().getPreHeader();
        String fromParameter = "";

        try {
            fromParameter = mailing.getEmailParam().getFromAdr();

            TAGCheck tagCheck = tagCheckFactory.createTAGCheck(form.getMailingId(), admin.getLocale());

            if (!tagCheck.checkContent(fromParameter, new StringBuffer(), new Vector<>())) {
                model.addAttribute("isTagFailureInFromAddress", true);
            }

            if (!tagCheck.checkContent(subjectParameter, new StringBuffer(), new Vector<>())) {
                model.addAttribute("isTagFailureInSubject", true);
            }

            if (!tagCheck.checkContent(preHeaderParameter, new StringBuffer(), new Vector<>())) {
                model.addAttribute("isTagFailureInPreHeader", true);
            }

            tagCheck.done();
        } catch (Exception e) {
            LOGGER.error("Error occurred: {}", e.getMessage(), e);
        }

        Page output = previewService.generateBackEndPreview(form);
        String header = output.getHeader();
        String senderEmail = fromParameter;
        String subject = subjectParameter;
        String preHeader = preHeaderParameter;

        if (header != null) {
            senderEmail = PreviewHelper.getFrom(header);
            subject = PreviewHelper.getSubject(header);
            preHeader = PreviewHelper.getPreHeader(header);
        }

        form.setSenderEmail(senderEmail);
        form.setSubject(subject);
        form.setPreHeader(preHeader);

        if (mailing.getEmailParam().getMailFormat() == MailingModel.Format.TEXT.getCode()) {
            form.setFormat(MailingWebPreviewService.INPUT_TYPE_TEXT);
        }
        model.addAttribute("components", mailingComponentDao.getPreviewHeaderComponents(form.getMailingId(), admin.getCompanyID()));
        
        if (output.getError() != null) {
        	Mailinglist mailinglist = mailinglistDao.getMailinglist(mailing.getMailinglistID(), mailing.getCompanyID());
        	if (recipientDao.getNumberOfRecipients(mailing.getCompanyID(), mailinglist.getId()) == 0) {
                popups.alert("error.preview.recipient.missing", "\"" + mailinglist.getShortname() + "\" (" + mailinglist.getId() + ")");
        	} else if (recipientDao.getNumberOfRecipients(mailing.getCompanyID(), mailinglist.getId(), RecipientType.TEST_RECIPIENT, RecipientType.ADMIN_RECIPIENT, RecipientType.TEST_VIP_RECIPIENT) == 0) {
                popups.alert("error.preview.recipient.test.missing", "\"" + mailinglist.getShortname() + "\" (" + mailinglist.getId() + ")");
        	}
        }
    }

    private void choosePreviewCustomerId(int companyId, int mailingId, PreviewForm previewForm, boolean useCustomerEmail, Popups popups, List<RecipientLiteImpl> recipientList) {
        String previewCustomerEmail = previewForm.getCustomerEmail();

        if (useCustomerEmail && StringUtils.isNotBlank(previewCustomerEmail)) {
            int customerId = recipientDao.getCustomerIdWithEmailInMailingList(companyId, mailingId, previewCustomerEmail);
            if (customerId > 0) {
                previewForm.setCustomerID(customerId);
                previewForm.setCustomerATID(0);

                return;
            }

            popups.alert("mailing.error.previewCustomerEmail");
            previewForm.setCustomerEmail("");
        } else if (previewForm.getCustomerATID() != 0) {
            previewForm.setCustomerID(previewForm.getCustomerATID());
            return;
        }

        setMinCustomerId(previewForm, recipientList);
    }

    private void setMinCustomerId(PreviewForm previewForm, List<RecipientLiteImpl> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            return;
        }
        int minId = Collections.min(recipients.stream().map(RecipientLiteImpl::getId).toList());
        previewForm.setCustomerID(minId);
        previewForm.setCustomerATID(minId);
    }

    private PreviewSettings.PreviewSettingsBuilder formToSettingsBuilder(PreviewForm form) {
        final PreviewSettings.PreviewSettingsBuilder settings = PreviewSettings.builder();

        settings.withPreviewSize(Preview.Size.getSizeById(form.getSize()));
        settings.withMailingId(form.getMailingId());
        settings.withAnonymous(form.isAnon(), form.isOnAnonPreserveLinks());
        settings.withPreviewFormat(form.getFormat());
        settings.withMode(form.getModeType());
        settings.withCustomerId(form.getCustomerID());
        settings.withNoImages(form.isNoImages());
        settings.withTargetGroupId(form.getTargetGroupId());

        if (form.isInternal()) {
            settings.withRdirDomain(configService.getPreviewBaseUrl());
        }

        return settings;
    }
}
