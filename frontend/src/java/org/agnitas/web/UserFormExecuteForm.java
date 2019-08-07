/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Form bean for the measure actions
 */
public final class UserFormExecuteForm extends ActionForm {

    private static final long serialVersionUID = -7075986957480597367L;

	/** Holds value of property agnCI. */
    private int agnCI;
    
    /** Holds value of property agnFN. */
    private String agnFN;
    
    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     * 
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     * @return messages for errors, that occured. 
     */
    @Override
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        return new ActionErrors();        
    }
    
    /**
     * Getter for property ci.
     * @return Value of property ci.
     */
    public int getAgnCI() {
        return this.agnCI;
    }
    
    /**
     * Setter for property ci.
     * 
     * @param agnCI 
     */
    public void setAgnCI(int agnCI) {
        this.agnCI = agnCI;
    }
    
    /**
     * Getter for property agnFN.
     * @return Value of property agnFN.
     */
    public String getAgnFN() {
        return this.agnFN;
    }
    
    /**
     * Setter for property agnFN.
     * @param agnFN New value of property agnFN.
     */
    public void setAgnFN(String agnFN) {
        this.agnFN = agnFN;
    }

    /** Holds value of property agnUseSession. */
    private int agnUseSession=0;

    /**
     * Getter for property agnUseSession.
     * @return Value of property agnUseSession.
     */
    public int getAgnUseSession() {

        return this.agnUseSession;
    }

    /**
     * Setter for property agnUseSession.
     * @param agnUseSession New value of property agnUseSession.
     */
    public void setAgnUseSession(int agnUseSession) {

        this.agnUseSession = agnUseSession;
    }
    
}
