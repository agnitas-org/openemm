/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.preview.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.preview.form.PreviewForm;
import com.agnitas.emm.core.preview.service.MailingWebPreviewService;
import com.agnitas.emm.core.workflow.service.GenerationPDFService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.agnitas.dao.MailingComponentDao;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.preview.AgnTagException;
import org.agnitas.preview.ModeType;
import org.agnitas.preview.Page;
import org.agnitas.preview.Preview;
import org.agnitas.preview.PreviewHelper;
import org.agnitas.preview.TAGCheck;
import org.agnitas.preview.TAGCheckFactory;
import org.agnitas.util.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;

@Controller
@RequestMapping("/mailing/preview")
@PermissionMapping("mailing.preview")
public class MailingPreviewController implements XssCheckAware {

    private static final Logger LOGGER = LogManager.getLogger(MailingPreviewController.class);

    private final ComMailingDao mailingDao;
    private final MailinglistDao mailinglistDao;
    private final ComMailingBaseService mailingBaseService;
    private final ComRecipientDao recipientDao;
    private final MailingService mailingService;
    private final MailingWebPreviewService previewService;
    private final GridServiceWrapper gridService;
    private final MailingComponentDao mailingComponentDao;
    private final TAGCheckFactory tagCheckFactory;
    private final ConfigService configService;
    private final GenerationPDFService generationPDFService;

    public MailingPreviewController(ComMailingDao mailingDao, MailinglistDao mailinglistDao, ComRecipientDao recipientDao, MailingService mailingService,
                                    MailingWebPreviewService previewService, GridServiceWrapper gridService, MailingComponentDao mailingComponentDao,
                                    TAGCheckFactory tagCheckFactory, ComMailingBaseService mailingBaseService, ConfigService configService,
                                    GenerationPDFService generationPDFService) {
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
        this.generationPDFService = generationPDFService;
    }

    @RequestMapping("/{mailingId:\\d+}/view.action")
    public String view(@PathVariable("mailingId") int mailingId, @ModelAttribute("form") PreviewForm form, Admin admin, Model model, Popups popups) {
        int companyId = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(mailingId, companyId);

        initFormData(mailingId, form, mailing, companyId);

        model.addAttribute("availablePreviewFormats", previewService.getAvailablePreviewFormats(mailing));
        model.addAttribute("isPostMailing", previewService.isPostMailing(mailing));
        model.addAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(admin, mailingId));

        if (!mailinglistDao.exist(mailing.getMailinglistID(), companyId)) {
            model.addAttribute("mailingListExist", false);
            return "mailing_preview_select_new";
        }

        previewService.updateActiveMailingPreviewFormat(form, mailingId, companyId);

        Map<Integer, String> recipientList = recipientDao.getAdminAndTestRecipientsDescription(companyId, mailingId);
        model.addAttribute("previewRecipients", recipientList);

        if (form.getModeType() == ModeType.RECIPIENT && mailingDao.hasPreviewRecipients(mailingId, companyId)) {
            choosePreviewCustomerId(companyId, mailingId, form, form.isUseCustomerEmail(), popups, recipientList);
        }

        model.addAttribute("availableTargetGroups", mailingService.listTargetGroupsOfMailing(companyId, mailingId));

        return "mailing_preview_select_new";
    }

    @RequestMapping("/header.action")
    public String header(@ModelAttribute("form") PreviewForm form, Model model, Admin admin) {
        int companyId = admin.getCompanyID();
        int mailingId = form.getMailingId();

        loadPreviewHeaderData(form, admin, model);
        model.addAttribute("components", mailingComponentDao.getPreviewHeaderComponents(mailingId, companyId));

        return "mailing_preview_header";
    }

    @GetMapping("/view-content.action")
    public String viewContent(@ModelAttribute("form") PreviewForm form, Model model, Admin admin, HttpServletRequest req, Popups popups) {
        try {
            return previewService.getPreview(form, getBulkCompanyId(req), admin);
        } catch (AgnTagException agnTagException) {
            model.addAttribute("errorReport", agnTagException.getReport());
            popups.alert("error.template.dyntags");
        } catch (Exception e) {
            popups.alert("Error");
        }
        return "mailing_preview_errors";
    }

    @GetMapping(value = "/pdf.action", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody
    FileSystemResource saveAsPdf(@ModelAttribute PreviewForm form, Admin admin, HttpSession session, HttpServletResponse response) {
        String baseUrl = configService.getValue(ConfigValue.PreviewUrl);

        if (StringUtils.isBlank(baseUrl)) {
            baseUrl = configService.getValue(ConfigValue.SystemUrl);
        }

        String url = String.format("%s/mailing/preview/view-content.action;jsessionid=%s?mailingId=%d&format=%d&size=%d&modeTypeId=%d&targetGroupId=%d&customerID=%d&noImages=%s",
                baseUrl, session.getId(), form.getMailingId(), MailingWebPreviewService.INPUT_TYPE_HTML, form.getSize(), form.getModeTypeId(), form.getTargetGroupId(), form.getCustomerID(), form.isNoImages());

        String mailingName = mailingDao.getMailingName(form.getMailingId(), admin.getCompanyID());

        File pdfFile = generationPDFService.generatePDF(configService.getValue(ConfigValue.WkhtmlToPdfToolPath), url, mailingName,
                admin, "", "Portrait", "Mailing");

        HttpUtils.setDownloadFilenameHeader(response, mailingName + ".pdf");
        return new DeleteFileAfterSuccessReadResource(pdfFile);
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
        form.setMediaQuery(previewService.getMediaQuery(Preview.Size.getSizeById(form.getSize())));
        form.setWidth(previewService.getPreviewWidth(Preview.Size.getSizeById(form.getSize())));

        if (mailing.getEmailParam() == null || mailing.getEmailParam().getMailFormat() == MailingModel.Format.TEXT.getCode()) {
            form.setEmailFormat(MediaTypes.EMAIL.getMediaCode());
            form.setFormat(MailingWebPreviewService.INPUT_TYPE_TEXT);
        } else {
            form.setEmailFormat(mailing.getEmailParam().getMailFormat());
        }
    }

    private int getBulkCompanyId(HttpServletRequest req) {
        if (req.getSession().getAttribute("bulkGenerate") != null) {
            String companyIdString = req.getParameter("previewCompanyId");
            return NumberUtils.toInt(companyIdString, -1);
        }

        return -1;
    }

    private void loadPreviewHeaderData(PreviewForm form, Admin admin, Model model) {
        Mailing mailing = mailingDao.getMailing(form.getMailingId(), admin.getCompanyID());

        if (mailing == null) {
            return;
        }

        String subjectParameter = mailing.getEmailParam().getSubject();
        String fromParameter = "";

        try {
            fromParameter = mailing.getEmailParam().getFromAdr();

            TAGCheck tagCheck = tagCheckFactory.createTAGCheck(form.getMailingId(), admin.getLocale());

            Vector<String> failures = new Vector<>();
            StringBuffer fromReportBuffer = new StringBuffer();
            StringBuffer subjectReportBuffer = new StringBuffer();

            if (!tagCheck.checkContent(fromParameter, fromReportBuffer, failures)) {
                model.addAttribute("isTagFailureInFromAddress", true);
            }

            if (!tagCheck.checkContent(subjectParameter, subjectReportBuffer, failures)) {
                model.addAttribute("isTagFailureInSubject", true);
            }

            tagCheck.done();
        } catch (Exception e) {
            LOGGER.error("Error occurred: {}", e.getMessage(), e);
        }

        Page output = previewService.generateBackEndPreview(form);
        String header = output.getHeader();
        String senderEmail = fromParameter;
        String subject = subjectParameter;

        if (header != null) {
            senderEmail = PreviewHelper.getFrom(header);
            subject = PreviewHelper.getSubject(header);
        }

        form.setSenderEmail(senderEmail);
        form.setSubject(subject);

        if (mailing.getEmailParam().getMailFormat() == MailingModel.Format.TEXT.getCode()) {
            form.setFormat(MailingWebPreviewService.INPUT_TYPE_TEXT);
        }
    }

    private void choosePreviewCustomerId(int companyId, int mailingId, PreviewForm previewForm, boolean useCustomerEmail, Popups popups, Map<Integer, String> recipientList) {
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

    private void setMinCustomerId(PreviewForm previewForm, Map<Integer, String> recipientList) {
        if (recipientList != null && !recipientList.isEmpty()) {
            int minId = Collections.min(recipientList.keySet());

            previewForm.setCustomerID(minId);
            previewForm.setCustomerATID(minId);
        }
    }
}
