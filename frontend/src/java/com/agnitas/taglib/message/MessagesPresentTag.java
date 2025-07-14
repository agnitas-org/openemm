/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.message;

import jakarta.servlet.jsp.tagext.TagSupport;

import java.util.List;

public class MessagesPresentTag extends TagSupport implements PopupMessageTag, FieldMessageTag {
    private static final long serialVersionUID = 7652742116037919211L;
    
	private String type;
    private boolean formField;

    public MessagesPresentTag() {
    }

    @Override
    public int doStartTag() {
        List<?> messages = findMessages();
        return !messages.isEmpty() ? 1 : 0;
    }

    private List<?> findMessages() {
        if (formField) {
            return getFieldMessages(pageContext, type);
        }

        return getPopupsMessages(pageContext, type);
    }

    @Override
    public void release() {
        super.release();
        this.type = null;
    }

    public void setType(String type) {
        this.type = type.toUpperCase();
    }

    public void setFormField(boolean formField) {
        this.formField = formField;
    }
}
