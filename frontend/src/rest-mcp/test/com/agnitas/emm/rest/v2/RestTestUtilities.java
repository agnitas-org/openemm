/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;

public final class RestTestUtilities {

    private RestTestUtilities() {}

    public static void assertPage(PageResponse<?> pageResponse, int pageNumber, int pageSize, int totalSize) {
        assertNotNull(pageResponse);
        assertEquals(pageNumber, pageResponse.page().number(), "Page number");
        assertEquals(pageSize, pageResponse.page().size(), "Page size");
        assertEquals(totalSize, pageResponse.page().totalCount(), "Full list size");
    }
}
