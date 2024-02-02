/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.message;

import com.agnitas.messages.Message;
import com.agnitas.web.mvc.impl.PopupsImpl;

import java.util.List;

public class FieldMessagesTag extends BaseMessageTag implements FieldMessageTag {
    private static final long serialVersionUID = 7681382871213245024L;
    
	private String fieldNameVar;

    @Override
    public void release() {
        super.release();
        fieldNameVar = null;
    }

    @Override
    protected List<?> getMessages() {
        return getFieldMessages(pageContext, type);
    }

    @Override
    protected Message getMessage(Object messageObj) {
        return ((PopupsImpl.FieldMessage) messageObj).getMessage();
    }

    @Override
    protected void removePageContextAttributes() {
        super.removePageContextAttributes();
        pageContext.removeAttribute(fieldNameVar);
    }

    @Override
    protected void addPageContextAttributes(String message, Object messageObj) {
        super.addPageContextAttributes(message, messageObj);
        pageContext.setAttribute(fieldNameVar, ((PopupsImpl.FieldMessage) messageObj).getName());
    }

    public void setFieldNameVar(String fieldNameVar) {
        this.fieldNameVar = fieldNameVar;
    }
}
