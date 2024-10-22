/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.message;

import com.agnitas.messages.Message;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import org.agnitas.util.AgnUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public abstract class BaseMessageTag extends BodyTagSupport {
    private static final long serialVersionUID = -6601095892368588653L;
    
	protected String type;
    protected String var;
    private Iterator<?> iterator = null;

    @Override
    public void release() {
        super.release();
        iterator = null;
        var = null;
        type = null;
    }

    @Override
    public int doStartTag() throws JspException {
        List<?> messages = getMessages();
        iterator = messages.iterator();
        return processNextMessage();
    }

    @Override
    public int doAfterBody() throws JspException {
        printBodyContent();
        return processNextMessage();
    }

    protected abstract List<?> getMessages();

    private int processNextMessage() {
        if (iterator.hasNext()) {
            Object messageObj = iterator.next();

            Message message = getMessage(messageObj);
            String msg = getMessageText(message);
            setPageContextAttributes(msg, messageObj);
            return EVAL_BODY_BUFFERED;
        } else {
            return SKIP_BODY;
        }
    }

    private void setPageContextAttributes(String message, Object messageObj) {
        if (message == null) {
            removePageContextAttributes();
        } else {
            addPageContextAttributes(message, messageObj);
        }
    }

    protected void addPageContextAttributes(String message, Object messageObj) {
        pageContext.setAttribute(var, message);
    }

    protected void removePageContextAttributes() {
        pageContext.removeAttribute(var);
    }

    private String getMessageText(Message message) {
        if (message.isResolvable()) {
            try {
                MessageSource messageSource = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext()).getBean(MessageSource.class);
                return messageSource.getMessage(message.getCode(), message.getArguments(), AgnUtils.getLocale(pageContext));
            } catch (NoSuchMessageException e) {
                return "ERROR: Message not found with code '" + message.getCode() + "'";
            }
        }

        return message.getCode();
    }

    protected abstract Message getMessage(Object messageObj);

    private void printBodyContent() throws JspException {
        if (bodyContent != null) {
            JspWriter writer = pageContext.getOut();
            if (writer instanceof BodyContent) {
                writer = ((BodyContent) writer).getEnclosingWriter();
            }

            try {
                writer.print(bodyContent.getString());
                bodyContent.clearBody();
            } catch (IOException e) {
                throw new JspException("Error writing body content", e);
            }
        }
    }

    public void setType(String type) {
        this.type = type.toUpperCase();
    }

    public void setVar(String var) {
        this.var = var;
    }
}
