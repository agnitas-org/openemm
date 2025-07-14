/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.web;

import java.util.Locale;

import com.agnitas.emm.core.trackablelinks.form.TrackableLinkForm;
import com.agnitas.messages.I18nString;
import jakarta.servlet.http.HttpServletRequest;
import org.displaytag.decorator.TableDecorator;
import org.springframework.web.servlet.support.RequestContextUtils;

public class TrackableLinkListDecorator extends TableDecorator {

    @Override
    public String addRowId() {
        final int linkId = ((TrackableLinkForm) getCurrentRowObject()).getId();
        if (linkId == 0) {
            return super.addRowId();
        }

        return "link-" + linkId;
    }

    @Override
    public String addRowClass() {
        Locale locale = RequestContextUtils.getLocale((HttpServletRequest) getPageContext().getRequest());
        return ((TrackableLinkForm) getCurrentRowObject()).isDeleted()
                ? "disabled\" data-tooltip=\"" + I18nString.getLocaleString("target.Deleted", locale)
                : "";
    }
}
