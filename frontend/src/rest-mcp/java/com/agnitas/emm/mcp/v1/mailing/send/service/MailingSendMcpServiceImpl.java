/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.send.service;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.components.exception.InvalidMethodForMailingtypeException;
import com.agnitas.emm.core.components.service.MailingSendService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.mcp.v1.mailing.send.MailingStartResponse;
import com.agnitas.emm.mcp.v1.mailing.send.MailingStopResponse;
import com.agnitas.emm.mcp.v1.mailing.send.dto.SendAdminMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.send.dto.SendTestMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.send.dto.StartSendMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.send.exception.MailingSendException;
import com.agnitas.emm.mcp.v1.mailing.send.mapper.MailingSendMcpMapper;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import org.springframework.stereotype.Service;

@Service
public class MailingSendMcpServiceImpl implements MailingSendMcpService {

    private final MailingSendService mailingSendService;
    private final MailingSendMcpMapper mapper;

    public MailingSendMcpServiceImpl(MailingSendService mailingSendService, MailingSendMcpMapper mapper) {
        this.mailingSendService = mailingSendService;
        this.mapper = mapper;
    }

    @Override
    public MailingStartResponse sendAdmin(SendAdminMailingRequest request, Admin admin) {
        return startMailing(
                () -> mailingSendService.sendAdmin(request.mailingId(), admin, mapper.toSendOptions(request)),
                MailingStartResponse::started
        );
    }

    @Override
    public MailingStartResponse sendTest(SendTestMailingRequest request, Admin admin) {
        return startMailing(
                () -> mailingSendService.sendTest(request.mailingId(), admin, mapper.toSendOptions(request)),
                MailingStartResponse::started
        );
    }

    @Override
    public MailingStartResponse send(StartSendMailingRequest request, Admin admin) {
        try {
            return startMailing(
                    () -> mailingSendService.sendWorld(request.mailingId(), admin, mapper.toSendOptions(request)),
                    MailingStartResponse::started
            );
        } catch (InvalidMethodForMailingtypeException ex) {
            return startMailing(
                    () -> mailingSendService.activate(request.mailingId(), admin, mapper.toSendOptions(request)),
                    MailingStartResponse::activated
            );
        }
    }

    @Override
    public MailingStopResponse stop(int mailingId, int companyId) {
        try {
            return mailingSendService.cancel(mailingId, companyId)
                    ? MailingStopResponse.stopped()
                    : MailingStopResponse.failed("Can't cancel mailing");
        } catch (InvalidMethodForMailingtypeException ex) {
            return mailingSendService.deactivate(mailingId, companyId)
                    ? MailingStopResponse.deactivated()
                    : MailingStopResponse.failed("Can't deactivate mailing");
        }
    }

    private MailingStartResponse startMailing(
            Supplier<ServiceResult<UserAction>> serviceCall,
            Function<Collection<Message>, MailingStartResponse> responseFactory
    ) {
        ServiceResult<UserAction> result = serviceCall.get();

        if (!result.isSuccess()) {
            throw new MailingSendException(result.getErrorMessages());
        }

        return responseFactory.apply(result.getWarningMessages());
    }

}
