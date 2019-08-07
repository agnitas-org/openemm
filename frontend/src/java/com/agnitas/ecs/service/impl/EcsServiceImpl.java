/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.ecs.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.ecs.backend.beans.ClickStatColor;
import org.agnitas.ecs.backend.dao.EmbeddedClickStatDao;
import org.agnitas.ecs.backend.web.EmbeddedClickStatView;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HttpUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.MediaType;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.ecs.service.EcsService;
import com.agnitas.emm.core.workflow.service.GenerationPDFService;
import com.agnitas.messages.I18nString;

public class EcsServiceImpl implements EcsService {
    private static final Logger logger = Logger.getLogger(EcsServiceImpl.class);

    public static final String PDF_ORIENTATION = "Portrait";
    public static final String PDF_FOOTER_MESSAGE_KEY = "ecs.Heatmap";

    protected GenerationPDFService generationPDFService;
    protected ComRecipientDao recipientDao;
    protected ComMailingDao mailingDao;
    protected EmbeddedClickStatDao embeddedClickStatDao;
    protected ConfigService configService;

    @Override
    public List<ClickStatColor> getClickStatColors(@VelocityCheck int companyId) {
        return embeddedClickStatDao.getClickStatColors(companyId);
    }

    @Override
    public Map<Integer, String> getTestAndAdminRecipients(int mailingId, @VelocityCheck int companyId) {
        return recipientDao.getAdminAndTestRecipientsDescription(companyId, mailingId);
    }

    @Override
    public File exportHeatMap(HttpServletRequest request, int mailingId, int recipientId, int viewMode, int previewSize) {
    	ComAdmin admin = AgnUtils.getAdmin(request);
        if (admin == null) {
            return null;
        }

        String title = mailingDao.getMailingName(mailingId, admin.getCompanyID());
        return exportHeatMap(admin, request.getRequestedSessionId(), title, mailingId, recipientId, viewMode, previewSize);
    }

    @Override
    public boolean exportHeatMap(HttpServletRequest request, HttpServletResponse response, int mailingId, int recipientId, int viewMode, int previewSize) {
    	ComAdmin admin = AgnUtils.getAdmin(request);
        if (admin == null) {
            return false;
        }

        String title = mailingDao.getMailingName(mailingId, admin.getCompanyID());
        File document = exportHeatMap(admin, request.getRequestedSessionId(), title, mailingId, recipientId, viewMode, previewSize);

        if (document != null && document.exists()) {
            if (document.length() > 0) {
                response.setContentType(MediaType.APPLICATION_PDF_VALUE);

                try (OutputStream stream = response.getOutputStream()) {
                    String filename = getExportFilename(title, admin.getLocale());

                    HttpUtils.setDownloadFilenameHeader(response, filename);
                    response.setContentLength((int) document.length());

                    try (FileInputStream documentStream = new FileInputStream(document)) {
                        IOUtils.copy(documentStream, stream);
                    }

                    return true;
                } catch (IOException e) {
                    logger.error("Error occurred: " + e.getMessage(), e);
                }
            }

            if (!document.delete()) {
                logger.debug("Unable to delete temporary file: " + document.getAbsolutePath());
            }
        }

        return false;
    }

    private File exportHeatMap(ComAdmin admin, String sessionId, String title, int mailingId, int recipientId, int viewMode, int previewSize) {
        String url = getHeatMapUrl(admin, sessionId, mailingId, recipientId, viewMode, previewSize);
        return generationPDFService.generatePDF(configService.getValue(ConfigValue.WkhtmlToPdfToolPath), url, title == null ? "" : title, admin, "", PDF_ORIENTATION, PDF_FOOTER_MESSAGE_KEY);
    }

    private String getExportFilename(String mailingName, Locale locale) {
        return mailingName + "_" +
                I18nString.getLocaleString("ecs.Heatmap", locale) + "_" +
                new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM).format(new Date()) +
                ".pdf";
    }

    protected String getHeatMapUrl(ComAdmin admin, String sessionId, int mailingId, int recipientId, int viewMode, int previewSize) {
        return configService.getValue(ConfigValue.SystemUrl) +
            EmbeddedClickStatView.PATH +
            ";jsessionid=" + sessionId + "?" +
            "mailingID=" + mailingId +
            "&recipientId=" + recipientId +
            "&viewMode=" + viewMode +
            "&previewSize=" + previewSize +
            "&language=" + admin.getAdminLang();
    }

    @Required
    public void setGenerationPDFService(GenerationPDFService generationPDFService) {
        this.generationPDFService = generationPDFService;
    }

    @Required
    public void setRecipientDao(ComRecipientDao recipientDao) {
        this.recipientDao = recipientDao;
    }

    @Required
    public void setMailingDao(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Required
    public void setEmbeddedClickStatDao(EmbeddedClickStatDao embeddedClickStatDao) {
        this.embeddedClickStatDao = embeddedClickStatDao;
    }

    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
}
