/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import static com.agnitas.util.HttpUtils.getContentDispositionAttachment;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.List;

public final class MvcUtils {

    private MvcUtils() {}

    public static void addDeleteAttrs(Model model, Collection<String> items,
                                      String singleTitle, String singleQuestion,
                                      String bulkTitle, String bulkQuestion) {
        boolean bulk = CollectionUtils.size(items) > 1;
        model.addAttribute("title", bulk ? bulkTitle : singleTitle);
        model.addAttribute("question", bulk ? bulkQuestion : singleQuestion);
        model.addAttribute("items", bulk ? items : items.iterator().next());
    }

    public static void addDeleteAttrs(Model model, String item,
                                      String title, String question) {
        addDeleteAttrs(model, List.of(item), title, question, "", "");
    }

    public static ResponseEntity<InputStreamResource> csvFileResponse(byte[] csv, String filename) {
        return ResponseEntity.ok()
            .contentLength(csv.length)
            .contentType(MediaType.parseMediaType("application/csv"))
            .header(HttpHeaders.CONTENT_DISPOSITION, getContentDispositionAttachment(filename))
            .body(new InputStreamResource(new ByteArrayInputStream(csv)));
    }
}
