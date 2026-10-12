/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.send;

import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

import com.agnitas.beans.Admin;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.components.exception.InvalidMethodForMailingtypeException;
import com.agnitas.emm.core.components.service.impl.MailingSendServiceImpl;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.rest.v2.infrastructure.exception.BadRequestException;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import org.springframework.stereotype.Service;

@Service
class MailingSendRestService {

    private final MailingSendServiceImpl mailingSendService;
    private final MailingSendRestMapper mapper;

    public MailingSendRestService(MailingSendServiceImpl mailingSendService, MailingSendRestMapper mapper) {
        this.mailingSendService = mailingSendService;
        this.mapper = mapper;
    }

    public MailingStartResponse sendAdmin(int mailingId, Admin admin, SendAdminMailingRequest req) {
        return startMailing(
                () -> mailingSendService.sendAdmin(mailingId, admin, mapper.toSendOptions(req)),
                MailingStartResponse::started
        );
    }

    public MailingStartResponse sendTest(int mailingId, Admin admin, SendTestMailingRequest req) {
        return startMailing(
                () -> mailingSendService.sendTest(mailingId, admin, mapper.toSendOptions(req)),
                MailingStartResponse::started
        );
    }

    public MailingStartResponse sendWorld(int mailingId, Admin admin, StartSendMailingRequest req) {
        return startMailing(
                () -> mailingSendService.sendWorld(mailingId, admin, mapper.toSendOptions(req)),
                MailingStartResponse::started
        );
    }

    public MailingStopResponse cancel(int mailingId, int companyId) {
        try {
            return mailingSendService.cancel(mailingId, companyId)
                    ? MailingStopResponse.stopped()
                    : MailingStopResponse.failed("Can't cancel mailing");
        } catch (InvalidMethodForMailingtypeException ex) {
            MailingType type = ex.getMailingType();
            throw invalidEndpointException(getValidStopEndpoint(type), type.getMessagekey());
        }
    }

    public MailingStartResponse activate(int mailingId, Admin admin, StartSendMailingRequest req) {
        return startMailing(
                () -> mailingSendService.activate(mailingId, admin, mapper.toSendOptions(req)),
                MailingStartResponse::activated
        );
    }

    public MailingStopResponse deactivate(int mailingId, int companyId) {
        try {
            return mailingSendService.deactivate(mailingId, companyId)
                    ? MailingStopResponse.deactivated()
                    : MailingStopResponse.failed("Can't deactivate mailing");
        } catch (InvalidMethodForMailingtypeException ex) {
            MailingType type = ex.getMailingType();
            throw invalidEndpointException(getValidStopEndpoint(type), type.getMessagekey());
        }
    }

    private MailingStartResponse startMailing(
            Supplier<ServiceResult<UserAction>> serviceCall,
            Function<Collection<Message>, MailingStartResponse> responseFactory
    ) {
        try {
            ServiceResult<UserAction> res = serviceCall.get();

            if (!res.isSuccess()) {
                throw new BadRequestException(res.getErrorMessages());
            }
            return responseFactory.apply(res.getWarningMessages());

        } catch (InvalidMethodForMailingtypeException ex) {
            MailingType type = ex.getMailingType();
            throw invalidEndpointException(getValidStartEndpoint(type), type.getMessagekey());
        }
    }

    private static BadRequestException invalidEndpointException(String altEndpoint, String typeMsgKey) {
        return new BadRequestException("Use /%s endpoint for the %s".formatted(
                altEndpoint,
                I18nString.t(typeMsgKey, Locale.ENGLISH)
        ));
    }

    private static String getValidStartEndpoint(MailingType mailingType) {
        return switch (mailingType) {
            case NORMAL, FOLLOW_UP -> "send";
            case ACTION_BASED, DATE_BASED, INTERVAL -> "activate";
        };
    }

    private static String getValidStopEndpoint(MailingType mailingType) {
        return switch (mailingType) {
            case NORMAL, FOLLOW_UP -> "cancel";
            case ACTION_BASED, DATE_BASED, INTERVAL -> "deactivate";
        };
    }
}
