/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.target;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

@Component
public class TargetGroupResources {

    private static final String EQL_SYNTAX_REFERENCE = """
            # EQL syntax reference

            EQL (EMM Query Language) is the boolean expression language used by a target group's `eql` field \
            to define which recipients belong to it.

            ## Basics
            - Combine conditions with `AND`, `OR`, `NOT`, and parentheses `()` for grouping, e.g. \
            `gender = 1 AND (country_field = 'DE' OR country_field = 'AT')`.
            - Operands are profile field names (e.g. `gender`, `firstname`, `country_field`) or constants. Quote \
            string constants with `'single'` or `"double"` quotes; numbers are unquoted.
            - A field name that collides with an EQL keyword (e.g. `email`, `status`, `type`, `test`, `on`, \
            `by`, `active`, `admin` — see the keyword list at the end) must be backtick-quoted to be used as a \
            field, e.g. `` `email` = 'a@b.com' `` — otherwise the expression fails with a syntax error.
            - `TODAY` is a literal for the current date; numeric expressions support `+ - * /` and `%`.

            ## Comparison operators
            - `=`, `<>` or `!=`, `<`, `>`, `<=`, `>=` — e.g. `age >= 18`.
            - Add `DATEFORMAT 'pattern'` after a comparison to parse the compared value as a date. The pattern \
            uses EQL's own mini-syntax, not Java's: `YYYY`/`MM`/`DD` (uppercase only) joined by `.`, `-` or `_` \
            (no `/` and no other letters), e.g. `` `date_time_writable` < '2025-01-01' DATEFORMAT 'YYYY-MM-DD' ``.

            ## Other field conditions
            - `field [NOT] IN (value, value, ...)` — e.g. `gender NOT IN (3, 4)`.
            - `field IS [NOT] EMPTY` — e.g. `firstname IS NOT EMPTY`.
            - `field [NOT] LIKE 'pattern'` — SQL-style wildcard match, e.g. `lastname LIKE 'mann%'`.
            - `field [NOT] CONTAINS 'text'` — e.g. `lastname CONTAINS 'mann'`.
            - `field [NOT] STARTS WITH 'text'` — e.g. `firstname STARTS WITH 'A'`.

            ## Mailing interaction conditions
            These reference a mailing or autoimport by its numeric ID, not by name.
            - `OPENED MAILING <id>` — recipient opened the given mailing.
            - `RECEIVED MAILING <id>` — recipient received the given mailing.
            - `CLICKED [LINK <id>] IN MAILING <id>` — recipient clicked (optionally a specific link) in the \
            given mailing.
            - `REVENUE BY MAILING <id>` — recipient has tracked revenue for the given mailing.
            - `FINISHED AUTOIMPORT <id>` — recipient was added/updated by the given (completed) autoimport.
            - `OPENED MAILING <id>`/`CLICKED IN MAILING <id>` accept an optional device filter: \
            `... ON DEVICE (OS = 'Android' AND CLASS = 'Mobile')` (fields: `OS`, `CLASS`, `CLIENT`; combine \
            with `AND`/`OR`/`NOT`/parentheses).

            ## Binding conditions
            `HAS BINDING` checks the recipient's mailinglist subscription state, with all clauses optional \
            and combinable:
            ```
            HAS BINDING
                [FOR MEDIATYPE EMAIL|SMS|POST]
                [ON MAILINGLIST <id>]
                [OF TYPE ADMIN|TEST|REGULAR]
                [WITH STATUS ACTIVE|OPTOUT|BLACKLISTED|WAIT_FOR_DOI_CONFIRM|BOUNCED]
                [REMARK LIKE 'pattern']
                [TIMESTAMP =|<>|<|>|<=|>= <value> DATEFORMAT 'pattern']
            ```
            e.g. `HAS BINDING ON MAILINGLIST 12 WITH STATUS ACTIVE`.

            ## Reference table conditions
            - Outside of `HAVING`, a reference table column is addressed as `table.column`, e.g. \
            `orders.amount > 100`.
            - `REFERENCES <table> HAVING (<condition>)` checks whether any row of `<table>` matches \
            `<condition>` for the recipient, e.g. `REFERENCES orders HAVING (amount > 100 AND state = 'paid')`. \
            Inside `HAVING`, only `IN`/`NOT IN`, `IS (NOT) EMPTY`, `=`, `<>`, `<`, `>`, `<=`, `>=` are allowed \
            (no `LIKE`/`CONTAINS`/`STARTS WITH`, and no `table.column` — the columns are already those of \
            `<table>`).

            ## Reserved keywords
            These words cannot be used as a bare field name (backtick-quote the field instead, e.g. `` `email` ``): \
            `IS`, `IN`, `LIKE`, `NOT`, `EMPTY`, `AND`, `OR`, `CONTAINS`, `STARTS`, `WITH`, `HAS`, `BINDING`, \
            `OPENED`, `RECEIVED`, `CLICKED`, `LINK`, `MAILING`, `MAILINGLIST`, `OF`, `TYPE`, `STATUS`, `BY`, \
            `REVENUE`, `TODAY`, `DATEFORMAT`, `REFERENCES`, `HAVING`, `ON`, `DEVICE`, `OS`, `CLASS`, `CLIENT`, \
            `FINISHED`, `AUTOIMPORT`, `ACTIVE`, `OPTOUT`, `BLACKLISTED`, `WAIT_FOR_DOI_CONFIRM`, `BOUNCED`, \
            `ADMIN`, `TEST`, `REGULAR`, `FOR`, `MEDIATYPE`, `EMAIL`, `SMS`, `POST`, `TIMESTAMP`, `REMARK` \
            (case-insensitive).
            """;

    @McpResource(
            uri = "docs://target-groups/eql-syntax",
            name = "target_group_eql_syntax",
            description = "Full syntax reference for EQL, the expression language used by a target group's "
                    + "\"eql\" field, including comparison, mailing-interaction, binding and reference-table conditions.",
            mimeType = "text/markdown"
    )
    public String eqlSyntaxReference() {
        return EQL_SYNTAX_REFERENCE;
    }

}
