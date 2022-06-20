/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.agnitas.actions.EmmAction;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.emm.core.action.operations.ActionOperationType;

/**
 * DAO accessing EMM actions.
 */
public interface EmmActionDao {

	/**
	 * Checks if given action exists.
	 * 
	 * @param actionID action ID to check
	 * @param companyID company ID for action
	 * 
	 * @return <code>true</code> if action exists, otherwise false
	 */
	boolean actionExists(final int actionID, @VelocityCheck final int companyID);
	
    /**
     * Loads emmAction
     *
     * @param actionID
     *              The id of emm action in database
     * @param companyID
     *              The id of the company that uses the action
     * @return  EMMAction bean object or null
     */
    EmmAction getEmmAction(int actionID, @VelocityCheck int companyID);

    /**
     * Saves emmAction.
     *
     * @param action
     *              EMMAction bean object
     * @return Saved action id or 0
     */
    int saveEmmAction(EmmAction action);
    
    /**
     * Deletes emmAction
     *
     * @param actionID
     *              The id of emm action in database
     * @param companyID
     *              The id of the company that uses the action
     * @return true==success
     *false==error
     */
    boolean deleteEmmAction(int actionID, @VelocityCheck int companyID);
    boolean deleteEmmActionReally(int actionID, @VelocityCheck int companyID);
    
    /**
     * Loads all emm actions for certain company
     *
     * @param companyID
     *              The id of the company that uses the actions
     * @return List of emm actions or empty list
     */
    List<EmmAction> getEmmActions(@VelocityCheck int companyID);
    List<EmmAction> getEmmActions(@VelocityCheck int companyID, boolean includeDeleted);

    List<EmmAction> getEmmActionsByName(@VelocityCheck int companyID, String shortName);

    String getEmmActionName(int actionId, @VelocityCheck int companyId);

    Map<Integer, String> getEmmActionNames(@VelocityCheck int companyId, List<Integer> actionIds);

    /**
     *  Loads all emm actions for certain company except actions of form type
     *
     * @param companyID
     *              The id of the company that uses the actions
     * @return List of emm actions or empty list
     */
    List<EmmAction> getEmmNotFormActions( @VelocityCheck int companyID);

    /**
     *  Loads all emm actions for certain company except actions of form type
     *
     * @param companyID
     *              The id of the company that uses the actions
     * @param includeInactive
     *              Whether ({@code true}) or not ({@code false}) actions marked as not active should be included.
     * @return List of emm actions or empty list
     */
    List<EmmAction> getEmmNotFormActions( @VelocityCheck int companyID, boolean includeInactive);

     /**
     *  Loads all emm actions for certain company except actions of link type
     *
     * @param companyID
     *              The id of the company that uses the actions
     * @return List of emm actions or empty list
     */
    List<EmmAction> getEmmNotLinkActions( @VelocityCheck int companyID);

     /**
     *  Loads all emm actions for certain company except actions of link type
     *
     * @param companyID
     *              The id of the company that uses the actions
       @param includeInactive
                    Whether ({@code true}) or not ({@code false}) actions marked as not active should be included.
     * @return List of emm actions or empty list
     */
    List<EmmAction> getEmmNotLinkActions( @VelocityCheck int companyID, boolean includeInactive);

    /**
     * Loads numbers of usage in forms for emm actions of certain company
     *
     * @param companyID
     *              The id of the company that uses the actions
     * @return HashMap object
     */
    Map<Integer, Integer> loadUsed( @VelocityCheck int companyID);

    List<String> getActionUserFormNames(int actionId, @VelocityCheck int companyId);

    /**
     *  Loads list of emm actions with sorting
     * @return List of emm actions
     */
    List<EmmAction> getActionList(int companyID, String sortBy, boolean order);

    /**
     *  Loads list of emm actions with sorting
     * @return List of emm actions
     */
    List<EmmAction> getActionList(int companyID, String sortBy, boolean order, Boolean activenessFilter);

    List<EmmAction> getEmmActionsByOperationType(int companyID, boolean includeInactive, ActionOperationType... actionTypes);

    Map<Integer, Boolean> getActivenessMap(Collection<Integer> actionIds, @VelocityCheck int companyId);

    void setActiveness(Collection<Integer> actionIds, boolean active, @VelocityCheck int companyId);

    /**
     * Loads list of emm actions that contain send mailing operation for a concrete mailing id.
     * @return List of emm actions
     */
    List<EmmAction> getActionListBySendMailingId(@VelocityCheck int companyId, int mailingId);

}
