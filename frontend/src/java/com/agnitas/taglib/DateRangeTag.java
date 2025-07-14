/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib;

import static org.springframework.web.servlet.support.RequestContextUtils.getLocale;

import java.util.Date;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.commons.dto.DateRange;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.tags.form.AbstractHtmlInputElementTag;
import org.springframework.web.servlet.tags.form.TagWriter;

public class DateRangeTag extends AbstractHtmlInputElementTag {

    private boolean inline;
    private boolean firstInputAdded;
    private String options;

    @Override
    protected int writeTagContent(TagWriter tagWriter) throws JspException {
        firstInputAdded = false;

        tagWriter.startTag("div");
        tagWriter.writeAttribute("data-date-range", "");

        if (inline) {
            tagWriter.writeAttribute("class", "inline-input-range");
        }

        writeDatePickerInput(tagWriter);
        writeDatePickerInput(tagWriter);

        tagWriter.endTag();
        return 0;
    }

    private void writeDatePickerInput(TagWriter tagWriter) throws JspException {
        String divClass = "date-picker-container";
        if (!firstInputAdded && !inline) {
            divClass += " mb-1";
        }

        tagWriter.startTag("div");
        tagWriter.writeAttribute("class", divClass);


        tagWriter.startTag("input");
        tagWriter.writeAttribute("type", "text");
        if (StringUtils.isNotBlank(getId())) {
            tagWriter.writeAttribute("id", getId() + "-" + (firstInputAdded ? "to" : "from"));
        }

        tagWriter.writeAttribute("class", "form-control js-datepicker");

        if (StringUtils.isNotBlank(options)) {
            tagWriter.writeAttribute("data-datepicker-options", options);
        }

        tagWriter.writeAttribute(
                "placeholder",
                translateMessage(firstInputAdded ? "To" : "From")
        );

        String value = "";
        if (getBindStatus().getActualValue() instanceof DateRange dateRange) {
            value = formatDate(firstInputAdded ? dateRange.getTo() : dateRange.getFrom());
        }

        tagWriter.writeAttribute("value", value);
        tagWriter.writeAttribute("name", getPropertyPath() + (firstInputAdded ? ".to" : ".from"));

        tagWriter.endTag();
        tagWriter.endTag();

        firstInputAdded = true;
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        Admin admin = AgnUtils.getAdmin((HttpServletRequest) this.pageContext.getRequest());
        if (admin == null) {
            throw new IllegalStateException("Date range date can't be formatted! Admin not found!");
        }
        return admin.getDateFormat().format(date);
    }

    private String translateMessage(String key) {
        HttpServletRequest req = (HttpServletRequest) this.pageContext.getRequest();
        WebApplicationContext appContext = RequestContextUtils.findWebApplicationContext(req);
        String message = appContext.getMessage(key, null, null, getLocale(req));
        return message == null ? "???%s???".formatted(key) : message;
    }

    // region Getters & Setters

    public boolean isInline() {
        return inline;
    }

    public void setInline(boolean inline) {
        this.inline = inline;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    // endregion
}
