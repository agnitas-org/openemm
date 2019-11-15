/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import javax.servlet.jsp.JspException;

import org.agnitas.target.TargetRepresentation;
import org.springframework.context.ApplicationContext;

import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.target.service.RecipientTargetGroupMatcher;

import bsh.Interpreter;

/**
 * Heavy-weight extension of {@link TargetLight} interface providing additional and structural
 * information on target groups.
 */
public interface ComTarget extends TargetLight {

    /**
     * Getter for property targetSQL.
     * 
     * @return Value of property targetSQL.
     */
    String getTargetSQL();

    /**
     * Getter for property targetStructure.
     * 
     * @return Value of property targetStructure.
     * 
     * Use setEQL() instead.
     * 
     * @see #setEQL(String)
     */
    @Deprecated
    TargetRepresentation getTargetStructure();

    /**
     * Getter for property customerInGroup.
     * 
     * @return Value of property customerInGroup.
     * 
     * @see ComTargetService#createRecipientTargetGroupMatcher(int, int)
     * @see RecipientTargetGroupMatcher
     */
    @Deprecated
    boolean isCustomerInGroup(Interpreter interpreter);

     /**
     * Getter for property customerInGroup.
     * 
     * @return Value of property customerInGroup.
     * @throws JspException 
     * 
     * @see ComTargetService#createRecipientTargetGroupMatcher(int, int)
     * @see RecipientTargetGroupMatcher
     */
    @Deprecated
    boolean isCustomerInGroup(int customerID, ApplicationContext con) throws JspException;


    /**
     * Setter for property targetSQL.
     * 
     * @param sql New value of property targetSQL.
     */
    void setTargetSQL(String sql);

    /**
     * Setter for property targetStructure.
     * 
     * @param targetStructure New value of property targetStructure.
     * 
     * Use getEQL() instead.
     * 
     * @see #getEQL()
     */
    @Deprecated
    void setTargetStructure(TargetRepresentation targetStructure);

	boolean isAdminTestDelivery();
	void setAdminTestDelivery( boolean adminTestDelivery);

	/**
	 * Set EQL representation of target group.
	 * 
	 * @param eql EQL representation of target group
	 */
	void setEQL(String eql);
	
	/**
	 * Returns EQL representation of target group.
	 * 
	 * @return EQL representation of target group
	 */
	String getEQL();
	
	/**
	 * Set flag, if target group is simple structured (and therefore visualizable with
	 * legacy editor).
	 * 
	 * @param simpleStructured true, if structure of target group is simple
	 */
	void setSimpleStructured(boolean simpleStructured);
	
	/**
	 * Returns, if target group is simple structured and visualizable with legacy editor.
	 * 
	 * @return true, if target group is simple structured
	 */
	boolean isSimpleStructured();


}
