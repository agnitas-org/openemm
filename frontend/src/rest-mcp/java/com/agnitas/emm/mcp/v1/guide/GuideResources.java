/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.guide;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

@Component
public class GuideResources {

    private static final String DOI_WORKFLOW_GUIDE = """
            # create and activate DOI (double opt-in) workflow

            ## Basics
            - mailinglist, triggers, forms and mailings are needed

            ## Mailinglist
            - create or choose a mailinglist

            ## first Trigger
            - create or choose trigger
            - needed step is action `Add/ update recipient data`

            ## second Trigger
            - create or choose trigger and activate it
            - needed step for trigger is action `Double opt-in confirmation`

            ## Forms
            - create or choose forms and activate them
            - first form has to offer option to enter data (Gender, Firstname, Lastname, EMail, optional sys_tracking_veto, or customer fields) and leads to second form
            - second form has to confirm subscription with introductory trigger: first trigger
            - third form is a welcome form to confirm the subscription with introductory trigger: second trigger

            ## Mailing
            - choose or create an actionbased mailing with or without using a template
            - set link in mailing to welcome form using [agnFORM name="<name of third form>"]
            - activate mailing

            ## update first trigger
            - choose first trigger
            - add step `send action-based mailing`
            - mailing with link to third form should be selected in for this step
            - activate trigger


            ## Testing
            - execute test-URL of first form without agnUID parameter and test workflow

            ## Alternative
            - activate an existing workflow by given ID
            """;

    @McpResource(
            uri = "docs://workflows/doi-setup",
            name = "workflow_doi_setup",
            description = "Step-by-step guide for creating and activating a double opt-in (DOI) workflow: the "
                    + "mailinglist, triggers, forms and mailing involved, how they link together, and how to "
                    + "test the result.",
            mimeType = "text/markdown"
    )
    public String doiWorkflowGuide() {
        return DOI_WORKFLOW_GUIDE;
    }

}
