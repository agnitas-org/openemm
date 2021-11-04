/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.ecs.service.impl;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.agnitas.ecs.backend.beans.ClickStatColor;
import org.agnitas.ecs.backend.dao.EmbeddedClickStatDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.ecs.service.EcsService;
import com.agnitas.emm.core.workflow.service.GenerationPDFService;

public class EcsServiceImpl implements EcsService {
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EcsServiceImpl.class);

    public static final String PDF_ORIENTATION = "Portrait";
    public static final String PDF_FOOTER_MESSAGE_KEY = "ecs.Heatmap";

    protected GenerationPDFService generationPDFService;
    protected ComRecipientDao recipientDao;
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
    public File generatePDF(ComAdmin admin, String url, String title) {
        return generationPDFService.generatePDF(configService.getValue(ConfigValue.WkhtmlToPdfToolPath),
                url,
                StringUtils.defaultString(title),
                admin,
                "heatmapLoadFinished",
                PDF_ORIENTATION,
                PDF_FOOTER_MESSAGE_KEY);
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
    public void setEmbeddedClickStatDao(EmbeddedClickStatDao embeddedClickStatDao) {
        this.embeddedClickStatDao = embeddedClickStatDao;
    }

    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }
}
