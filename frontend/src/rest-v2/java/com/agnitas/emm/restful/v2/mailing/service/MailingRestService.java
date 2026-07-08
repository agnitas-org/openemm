/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.mailing.service;

import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.restful.v2.mailing.dto.MailingLightResponse;
import com.agnitas.emm.restful.v2.mailing.dto.MailingResponse;
import com.agnitas.emm.restful.v2.mailing.dto.MailingsPage;
import com.agnitas.emm.restful.v2.mailing.dto.UpdateMailingRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.Positive;

public interface MailingRestService {

    MailingResponse getFull(int mailingId, int companyId);

    MailingLightResponse getLight(int mailingId, int companyId);

    PaginatedList<MailingLightResponse> list(MailingsPage pageForm, Admin admin);

    void delete(int id, Admin admin);

    MailingLightResponse create(Map<String, Object> body, int companyId) throws JsonProcessingException;

    MailingLightResponse createFromTemplate(@Positive int templateId, int companyId);

    MailingLightResponse copy(int copyFromId, int companyId);

    MailingResponse patch(int id, UpdateMailingRequest updateRequest, Admin admin);
}
