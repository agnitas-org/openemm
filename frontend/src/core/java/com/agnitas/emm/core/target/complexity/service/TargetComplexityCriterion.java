/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.complexity.service;

public enum TargetComplexityCriterion {
    MANY_CONDITIONS(2),
    COLUMN_TEXT(1),
    COLUMN_DATE(2),
    COLUMN_WITHOUT_INDEX(2),
    REFERENCE_TABLE_KEY_WITHOUT_INDEX(2),
    LIKE_OPERATOR(3),  // LIKE operator (+3) can only be applied to alphanumeric columns (+1) so in fact we always have +4 here
    CUSTOMER_BINDING_CHECK(0),  // Change on demand
    MAILING_RECEIVED_CHECK(1),
    MAILING_OPENED_CHECK(2),
    MAILING_CLICKED_CHECK(1),
    MAILING_NOT_OPENED_CHECK(3),
    MAILING_NOT_CLICKED_CHECK(3),
    MAILING_REVENUE_CHECK(0),  // Change on demand
    AUTO_IMPORT_FINISHED_CHECK(0);  // Change on demand

    private final int complexity;

    TargetComplexityCriterion(int complexity) {
        this.complexity = complexity;
    }

    public int getComplexity() {
        return complexity;
    }
}
