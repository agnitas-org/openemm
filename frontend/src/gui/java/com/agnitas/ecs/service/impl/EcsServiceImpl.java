/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.ecs.service.impl;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.dao.RecipientDao;
import com.agnitas.ecs.backend.beans.ClickStatColor;
import com.agnitas.ecs.service.EcsService;
import com.agnitas.service.PdfService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class EcsServiceImpl implements EcsService {

    public static final String PDF_FOOTER_MESSAGE_KEY = "ecs.Heatmap";

    private static final List<ClickStatColor> CLICK_STAT_COLORS = List.of(
            new ClickStatColor(0, 5, "#E5CEF0"),
            new ClickStatColor(5, 10, "#569AFF"),
            new ClickStatColor(10, 15, "#8BF4AF"),
            new ClickStatColor(15, 20, "#FAFF00"),
            new ClickStatColor(20, 25, "#F8CE39"),
            new ClickStatColor(25, 100, "#E12E59")
    );

    private final PdfService pdfService;
    private final RecipientDao recipientDao;

    public EcsServiceImpl(PdfService pdfService, RecipientDao recipientDao) {
        this.pdfService = pdfService;
        this.recipientDao = recipientDao;
    }

    @Override
    public List<ClickStatColor> getClickStatColors(int companyId) {
        return CLICK_STAT_COLORS;
    }

    @Override
    public Map<Integer, String> getTestAndAdminRecipients(int mailingId, int companyId) {
        return recipientDao.getAdminAndTestRecipientsDescription(companyId, mailingId);
    }

    @Override
    public File generatePDF(Admin admin, String url, String title) {
        return pdfService.generatePDF(
                admin,
                url,
                false,
                StringUtils.defaultString(title),
                PDF_FOOTER_MESSAGE_KEY,
                "heatmapLoadFinished");
    }

}
