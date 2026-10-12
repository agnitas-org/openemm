/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.trigger;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

@Component
public class TriggerResources {

    private static final String OPERATION_TYPES_REFERENCE = """
            # Trigger operation types

            A trigger's `operations` tool parameter is a JSON-encoded string (not a JSON array literal) – e.g. \
            pass the string `[{"type":"SendMailing","mailingID":123}]`, not an actual array value. Each element \
            of the decoded array must have a `"type"` field set to one of the discriminators below, plus that \
            type's own fields. The base fields `id`, `companyId`, `actionId` and `readonly` are managed by the \
            server and must not be set by the caller.

            ## Trigger type (top-level `type` field, separate from the operations above)
            Every trigger also has its own top-level `type` – `Link`, `Form`, or `All` – set when creating or \
            updating the trigger itself (not part of `operations`). It controls where the trigger can be \
            referenced:
            - `Link`: usable as a mailing's link/open/click trigger (e.g. a mailing's `clickTriggerId` or a \
            link's `triggerId`).
            - `Form`: usable as a form's `startTriggerId`/`endTriggerId`.
            - `All`: usable in both places.

            ## ActivateDoubleOptIn
            - `forAllLists` (boolean, optional, default `false`): apply to all mailing lists.
            - `mediaType` (string, optional, default `EMAIL`): media type of the DOI confirmation, one of \
            `EMAIL`, `POST`, `SMS`, `RCS`.

            ## ContentView
            - `tagName` (string, required): name of the content block tag to display.

            ## ExecuteScript
            - `script` (string, required): script source to execute.

            ## GetArchiveList
            - `campaignID` (int, required): campaign ID whose archive list entries to fetch.
            - `limitValue` (int, optional): result limit value.
            - `limitType` (int, optional): unit/type of the limit.

            ## GetArchiveMailing
            - `expireDate` (string `dd.MM.yyyy`, optional): expiry date. Equivalent to setting `expireDay`, \
            `expireMonth` and `expireYear` (ints, optional, `0` each meaning no expiry) directly; use one \
            representation, not both.

            ## GetCustomer
            - `loadAlways` (boolean, optional, default `false`): reload recipient data even if already loaded.

            ## IdentifyCustomer
            - `keyColumn` (string, required): profile field holding the login key.
            - `passColumn` (string, required): profile field holding the password.

            ## SendMailing
            - `mailingID` (int, required): ID of the mailing to send.
            - `delayMinutes` (int, optional, default `0`): delay in minutes before sending.
            - `bcc` (string, optional): BCC address.
            - `userStatusesOption` (int, optional, default `0`): which subscriber statuses to include.

            ## ServiceMail
            - `subjectLine` (string, required)
            - `fromAddress` (string, required)
            - `toAddress` (string, required)
            - `replyAddress` (string, optional)
            - `textMail` (string, plain-text body; provide this and/or `htmlMail`)
            - `htmlMail` (string, HTML body; provide this and/or `textMail`)
            - `mailtype` (int, optional): mail format code.

            ## SubscribeCustomer
            - `keyColumn` (string, required): profile field used as the matching key.
            - `doubleOptIn` (boolean, optional, default `false`)
            - `doubleCheck` (boolean, optional, default `false`)

            ## UnsubscribeCustomer
            - `mailinglistIds` (array of int, optional): mailing lists to unsubscribe from.
            - `allMailinglistsSelected` (boolean, optional, default `false`): unsubscribe from all mailing lists.
            - `additionalMailinglists` (boolean, optional, default `false`)

            ## UpdateCustomer
            - `columnName` (string, required): profile field to update.
            - `updateType` (int, required): `1` = increment by `updateValue`, `2` = decrement by `updateValue`, \
            `3` = set to `updateValue`.
            - `updateValue` (string, required): a numeric value, or for date fields `SYSDATE`/`CURRENT_TIMESTAMP` \
            optionally followed by `+`/`-` and a number of days.
            - `trackingPointId` (int, optional)
            - `useTrack` (boolean, optional, default `false`)

            ## SendLastNewsletter
            No fields beyond `type`.
            """;

    @McpResource(
            uri = "docs://triggers/operation-types",
            name = "trigger_operation_types",
            description = "Reference of all trigger operation types (the \"type\" discriminator used in a "
                    + "trigger's operations JSON) and the fields each one accepts.",
            mimeType = "text/markdown"
    )
    public String operationTypesReference() {
        return OPERATION_TYPES_REFERENCE;
    }

}
