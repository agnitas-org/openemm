/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.InvalidReferenceEventHandler;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.app.event.NullSetEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.util.introspection.Info;

public class EventHandler implements InvalidReferenceEventHandler,
        NullSetEventHandler, MethodExceptionEventHandler {

      ActionErrors errors = new ActionErrors();

      public EventHandler(Context ctx) {
        EventCartridge ec = new EventCartridge();
        ec.addEventHandler(this);
        ec.attachToContext(ctx);
      }
    @Override
    public Object methodException(@SuppressWarnings("rawtypes") Class aClass, String method, Exception e) throws Exception {
    	String exceptionMessage = e.getMessage() != null ? e.getMessage() : "<no exception message>";
    	
        String error = "an " + e.getClass().getName() + " was thrown by the " + method
        + " method of the " + aClass.getName() + " class [" + StringEscapeUtils.escapeHtml(exceptionMessage.split("\n")[0]) + "]";
        errors.add(error, new ActionMessage("Method exception: " + error));
        return error;
    }

    @Override
    public boolean shouldLogOnNullSet(String s, String s1) {
        return false;
    }

    @Override
    public Object invalidGetMethod(Context context, String s, Object o, String s1, Info info) {
        String str = "Error in line " + info.getLine() + ", column " + info.getColumn() + ": ";
        errors.add(str,new ActionMessage(str + "Null reference " + s + "."));
        return null;
    }

    @Override
    public boolean invalidSetMethod(Context context, String s, String s1, Info info) {
        return false;
    }

    @Override
    public Object invalidMethod(Context context, String s, Object o, String s1, Info info) {
        String str = "Error in line " + info.getLine() + ", column " + info.getColumn() + ": ";
        errors.add(str, new ActionMessage(str + "Invalid method "+s+"."));
        return null;
    }

    /**
     * Returns errors collected by the event handler.
     * 
     * @return errors
     */
    public ActionErrors getErrors() {
        return errors;
    }
}

