/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity.event;

import com.agnitas.messages.Message;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.InvalidReferenceEventHandler;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.util.introspection.Info;

import java.util.ArrayList;
import java.util.List;

public class MessageEventHandlerImpl implements MethodExceptionEventHandler, InvalidReferenceEventHandler {

	private final List<Message> errors = new ArrayList<>();

	public MessageEventHandlerImpl(Context ctx) {
		EventCartridge ec = new EventCartridge();
		ec.addEventHandler(this);
		ec.attachToContext(ctx);
	}

	@Override
    public Object methodException(final Context context, Class aClass, String method, Exception e, final Info info) {
    	String exceptionMessage = e.getMessage() != null ? e.getMessage() : "<no exception message>";
    	
        String error = "an " + e.getClass().getName() + " was thrown by the " + method
        + " method of the " + aClass.getName() + " class [" + StringEscapeUtils.escapeHtml4(exceptionMessage.split("\n")[0]) + "]";
        errors.add(new Message("Method exception: " + error, false));
        return error;
    }

    @Override
    public Object invalidGetMethod(Context context, String s, Object o, String s1, Info info) {
        String str = "Error in line " + info.getLine() + ", column " + info.getColumn() + ": ";
        errors.add(new Message(str + "Null reference " + s + ".", false));
        return null;
    }

    @Override
    public boolean invalidSetMethod(Context context, String s, String s1, Info info) {
        return false;
    }

    @Override
    public Object invalidMethod(Context context, String s, Object o, String s1, Info info) {
        String str = "Error in line " + info.getLine() + ", column " + info.getColumn() + ": ";
        errors.add(new Message(str + "Invalid method "+s+".", false));
        return null;
    }

    /**
     * Returns errors collected by the event handler.
     * 
     * @return errors
     */
    public List<Message> getErrors() {
        return errors;
    }
}

