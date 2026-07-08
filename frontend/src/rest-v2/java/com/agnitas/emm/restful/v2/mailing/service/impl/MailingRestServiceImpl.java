/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.mailing.service.impl;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.BaseTrackableLink;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.mailing.bean.LightweightMailing;
import com.agnitas.emm.core.mailing.service.CopyMailingService;
import com.agnitas.emm.core.mailing.service.MailingModel;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.thumbnails.service.ThumbnailService;
import com.agnitas.emm.restful.v2.infrastructure.exception.BadRequestException;
import com.agnitas.emm.restful.v2.infrastructure.exception.ResourceNotFoundException;
import com.agnitas.emm.restful.v2.mailing.dto.MailingLightResponse;
import com.agnitas.emm.restful.v2.mailing.dto.MailingResponse;
import com.agnitas.emm.restful.v2.mailing.dto.MailingsPage;
import com.agnitas.emm.restful.v2.mailing.dto.UpdateMailingRequest;
import com.agnitas.emm.restful.v2.mailing.exception.MailingNotFoundException;
import com.agnitas.emm.restful.v2.mailing.mapping.MailingRestMapper;
import com.agnitas.emm.restful.v2.mailing.service.MailingRestService;
import com.agnitas.emm.restful.v2.mailinglists.exception.MailinglistNotFoundException;
import com.agnitas.service.ImportResult;
import com.agnitas.service.MailingExporter;
import com.agnitas.service.MailingImporter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

@Service
@Validated
public class MailingRestServiceImpl implements MailingRestService {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private final MailingImporter mailingImporter;
    private final MailingExporter mailingExporter;
    private final MailingService mailingService;
    private final MailingRestMapper mailingRestMapper;
    private final CopyMailingService copyMailingService;
    private final ThumbnailService thumbnailService;
    private final MailinglistService mailinglistService;
    private final CompanyTokenService companyTokenService;

    public MailingRestServiceImpl(MailingImporter mailingImporter,
                                  MailingExporter mailingExporter,
                                  MailingService mailingService,
                                  MailingRestMapper mailingRestMapper,
                                  CopyMailingService copyMailingService,
                                  ThumbnailService thumbnailService,
                                  MailinglistService mailinglistService,
                                  CompanyTokenService companyTokenService
    ) {
        this.mailingImporter = mailingImporter;
        this.mailingExporter = mailingExporter;
        this.mailingService = mailingService;
        this.mailingRestMapper = mailingRestMapper;
        this.copyMailingService = copyMailingService;
        this.thumbnailService = thumbnailService;
        this.mailinglistService = mailinglistService;
        this.companyTokenService = companyTokenService;
    }

    @Override
    public PaginatedList<MailingLightResponse> list(MailingsPage pageForm, Admin admin) {
        MailingsListProperties listProps = mailingRestMapper.toListProperties(pageForm);
        PaginatedList<Map<String, Object>> overview = mailingService.getOverview(admin, listProps);
        return mailingRestMapper.toMailingLightResponsePage(overview);
    }

    @Override
    public void delete(int id, Admin admin) {
        if (!mailingService.exists(id, admin.getCompanyID())) {
            throw new MailingNotFoundException(id);
        }
        mailingService.deleteMailing(id, admin);
    }

    @Override
    public MailingLightResponse create(Map<String, Object> body, int companyId) throws JsonProcessingException {
        try {
            InputStream inputStream = new ByteArrayInputStream(jsonMapper.writeValueAsBytes(body));
            ImportResult result = mailingImporter.importMailingFromJson(
                    companyId, inputStream, false, null, null, true, false, true
            );
            if (!result.isSuccess()) {
                throw new BadRequestException("Can't create mailing. Invalid input");
            }
            thumbnailService.updateMailingThumbnailByWebservice(companyId, result.getMailingID());
            return getLight(result.getMailingID(), companyId);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Override
    public MailingLightResponse createFromTemplate(int templateId, int companyId) {
        Mailing template = mailingService.getMailing(companyId, templateId);
        if (template == null || !template.isIsTemplate()) {
            throw new ResourceNotFoundException("Template not found with id = " + templateId);
        }
        MailingModel model = new MailingModel();
        model.setCompanyId(companyId);
        model.setTemplateId(templateId);
        model.setShortname(template.getShortname());
        model.setDescription(template.getDescription());
        int newMailingId = mailingService.addMailingFromTemplate(model);
        return getLight(newMailingId, companyId);
    }

    @Override
    public MailingLightResponse copy(int copyFromId, int companyId) {
        int newMailingId = copyMailingService.copyMailing(companyId, copyFromId, companyId, null, null);
        thumbnailService.updateMailingThumbnailByWebservice(companyId, newMailingId);
        return getLight(newMailingId, companyId);
    }

    @Override
    public MailingResponse patch(int mailingId, @Valid UpdateMailingRequest updateDto, Admin admin) {
        int companyId = admin.getCompanyID();
        Mailing mailing = mailingService.getMailing(companyId, mailingId);
        MediatypeEmail emailParam = mailing.getEmailParam();

        updateDto.shortname().ifPresent(mailing::setShortname);
        updateDto.description().ifPresent(mailing::setDescription);

        updateDto.mailinglist_id().ifPresent(mailing::setMailinglistID);
        updateDto.mailinglist_shortname().ifPresent(mailinglistShortname ->
                mailing.setMailinglistID(mailinglistService.getMailinglistIdByName(mailinglistShortname, companyId))
        );
        validateNewMailinglistIfSet(updateDto, mailing, companyId);
        updateDto.mailingtype().ifPresent(mailing::setMailingType);
        updateDto.mailing_content_type().ifPresent(mailing::setMailingContentType);
        updateDto.subject().ifPresent(emailParam::setSubject);
        updateDto.preHeader().ifPresent(emailParam::setPreHeader);
        updateDto.reply_address().ifPresent(emailParam::setReplyEmail);
        updateDto.is_template().ifPresent(mailing::setIsTemplate);
        updateDto.open_action_id().ifPresent(mailing::setOpenActionID);
        updateDto.click_action_id().ifPresent(mailing::setClickActionID);
        updateDto.campaign_id().ifPresent(mailing::setCampaignID);
        updateDto.parameters().ifPresent(params -> mailing.setParameters(mailingRestMapper.toParameters(params)));

        updateDto.links().ifPresent(links -> {
            String companyToken = companyTokenService.getCompanyToken(companyId).orElse(null);
            mailing.setTrackableLinks(links.stream()
                    .map(mailingRestMapper::toTrackableLinks)
                    .map(link -> {
                        link.setFullUrl(replacePlaceholders(companyToken, link.getFullUrl(), admin));
                        return link;
                    })
                    .collect(Collectors.toMap(
                            BaseTrackableLink::getFullUrl,
                            Function.identity()
                    )));
        });

        mailingService.saveMailing(mailing, true);
        return getFull(mailingId, companyId);
    }

    private void validateNewMailinglistIfSet(UpdateMailingRequest updateDto, Mailing mailing, int companyId) {
        if ((updateDto.mailinglist_shortname().isPresent() || updateDto.mailinglist_id().isPresent())
               && !mailinglistService.exist(mailing.getMailinglistID(), companyId)) {
            throw new MailinglistNotFoundException(mailing.getMailinglistID());
        }
    }

    private static String replacePlaceholders(String companyToken, String fullUrl, Admin admin) {
        int companyId = admin.getCompanyID();
        fullUrl = fullUrl
                .replace("[COMPANY_ID]", Integer.toString(companyId))
                .replace("[RDIR_DOMAIN]", admin.getCompany().getRdirDomain());
        return isNotBlank(companyToken)
                ? fullUrl.replace("[CTOKEN]", companyToken)
                : fullUrl.replace("agnCTOKEN=[CTOKEN]", "agnCI=" + companyId);
    }

    @Override
    public MailingResponse getFull(int mailingId, int companyId) {
        if (!mailingService.exists(mailingId, companyId)) {
            throw new MailingNotFoundException(mailingId);
        }
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            mailingExporter.exportMailingToJson(companyId, mailingId, buffer, false, false);
            return jsonMapper.readValue(buffer.toByteArray(), MailingResponse.class);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error occurred while trying to export mailing to JSON.",
                    e
            );
        }
    }

    @Override
    public MailingLightResponse getLight(int mailingId, int companyId) {
        LightweightMailing mailingLight = mailingService.getLightweightMailing(companyId, mailingId);
        return mailingRestMapper.toLightResponse(mailingLight);
    }
}
