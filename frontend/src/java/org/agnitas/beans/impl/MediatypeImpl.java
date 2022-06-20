/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import com.agnitas.beans.Mediatype;

public abstract class MediatypeImpl implements Mediatype {
	protected String param = "";

	protected int priority = 5;
    
    protected int status;

    /** Holds value of property companyID. */
    protected int companyID;

    protected String template;

    /**
     * Getter for property param.
     * @return Value of property param.
     */
    // TODO Move from base class to mediatype-related base classes. The implementation depends on the parameters of the media type
    @Override
	public String getParam() throws Exception {
        return param;
    }

    /**
     * Setter for property param.
     * @param param New value of property param.
     */
    // TODO Move from base class to mediatype-related base classes. The implementation depends on the parameters of the media type
    @Override
	public void setParam(String param) throws Exception {
        this.param = param;
    }

    @Override
	public int getPriority() {
        return priority;
    }

    @Override
	public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
	public int getStatus() {
        return status;
    }

    @Override
	public void setStatus(int status) {
        this.status = status;
    }

    /** Getter for property companyID.
     * @return Value of property companyID.
     *
     */
    @Override
	public int getCompanyID() {
        return companyID;
    }

    /** Setter for property companyID.
     * @param companyID New value of property companyID.
     *
     */
    @Override
	public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    /**
     * Getter for property param.
     * @return Value of property param.
     */
    @Override
	public String getTemplate() {
        return template;
    }

    /**
     * Setter for property param.
     * @param param New value of property param.
     */
    @Override
	public void setTemplate(String template) {
        this.template = template;
    }
 
    @Override
	public int hashCode() {
        return param.hashCode();
    }
    
    @Override
	public boolean equals(Object other) {
    	if (other == null || other.getClass() != getClass()) {
    		// According to Object.equals(Object), equals(null) returns false
    		return false;
    	} else {
    		return ((Mediatype) other).hashCode() == hashCode();
    	}
    }
}
