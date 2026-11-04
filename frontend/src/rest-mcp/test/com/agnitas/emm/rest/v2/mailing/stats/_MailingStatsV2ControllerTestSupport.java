/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.stats;

import static com.epages.restdocs.apispec.ResourceSnippetParameters.builder;

import java.util.Arrays;

import com.agnitas.emm.rest.v2.RestDocsTestSupport;
import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.Schema;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.ResultHandler;

public abstract class _MailingStatsV2ControllerTestSupport extends RestDocsTestSupport {

    private static final ParameterDescriptorWithType MAILING_ID_PATH_PARAM = intParam("mailingId", "Mailing ID");

    private static final FieldDescriptor[] BY_DEVICE_CLASS_FIELDS = {
            requiredField("desktop", "Number of actions performed on desktop devices"),
            requiredField("tablet", "Number of actions performed on tablet devices"),
            requiredField("mobile", "Number of actions performed on mobile devices"),
            requiredField("smartTv", "Number of actions performed on smart TV devices"),
            requiredField("multiple", "Number of actions performed across multiple device types")
    };

    private static final FieldDescriptor[] RESPONSE_FIELDS = fields(
            requiredField("sentEmails", "Number of sent emails"),
            requiredField("openersMeasured", "Number of openers"),
            requiredField("clickersWithoutOpening", "Number of clickers without opening"),
            requiredField("openings", "Number of openings"),
            requiredField("openingsAnonymous", "Number of anonymous openings"),
            requiredField("openersExtrapolated", "Number of extrapolated openers"),
            requiredField("openersByDeviceClass", "Openers by device class"),
            requiredField("clickers", "Number of clickers"),
            requiredField("clicks", "Number of clicks"),
            requiredField("clicksAnonymous", "Number of anonymous clicks"),
            requiredField("clickersByDeviceClass", "Clickers by device class"),
            requiredField("unsubscribers", "Number of unsubscribers"),
            requiredField("hardBounces", "Number of hard bounces"),
            requiredField("revenue", "Revenue"),
            Arrays.stream(applyPrefix("openersByDeviceClass.", BY_DEVICE_CLASS_FIELDS))
                    .map(FieldDescriptor::optional)
                    .toArray(FieldDescriptor[]::new),
            Arrays.stream(applyPrefix("clickersByDeviceClass.", BY_DEVICE_CLASS_FIELDS))
                    .map(FieldDescriptor::optional)
                    .toArray(FieldDescriptor[]::new)
    );

    private final Schema responseSchema = schema("Response");

    @Override
    protected String getDocsTag() {
        return "Mailing Statistics";
    }

    @Override
    protected String getDocsIdentifierPrefix() {
        return "mailing-stats";
    }

    protected ResultHandler documentRead() {
        return document("read", builder()
                .summary("Read mailing summary statistics data")
                .pathParameters(MAILING_ID_PATH_PARAM)
                .responseSchema(responseSchema)
                .responseFields(RESPONSE_FIELDS)
        );
    }
}
