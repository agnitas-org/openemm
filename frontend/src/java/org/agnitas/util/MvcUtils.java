/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.ui.Model;

public final class MvcUtils {

    private MvcUtils() {}

    public static void addDeleteAttrs(Model model, List<String> items,
                                      String singleTitle, String singleQuestion,
                                      String bulkTitle, String bulkQuestion) {
        boolean bulk = CollectionUtils.size(items) > 1;
        model.addAttribute("title", bulk ? bulkTitle : singleTitle);
        model.addAttribute("question", bulk ? bulkQuestion : singleQuestion);
        model.addAttribute("items", bulk ? items : items.get(0));
    }

    public static void addDeleteAttrs(Model model, String item,
                                      String title, String question) {
        addDeleteAttrs(model, List.of(item), title, question, "", "");
    }
}
