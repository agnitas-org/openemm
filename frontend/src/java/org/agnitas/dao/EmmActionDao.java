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
	boolean actionExists(final int actionID, final int companyID);
	
    /**
     * Loads emmAction
     *
     * @param actionID
     *              The id of emm action in database
     * @param companyID
     *              The id of the company that uses the action
     * @return  EMMAction bean object or null
     */
    EmmAction getEmmAction(int actionID, int companyID);

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
    boolean deleteEmmAction(int actionID, int companyID);
    boolean deleteEmmActionReally(int actionID, int companyID);
    
    /**
     * Loads all emm actions for certain company
     *
     * @param companyID
     *              The id of the company that uses the actions
     * @return List of emm actions or empty list
     */
    List<EmmAction> getEmmActions(int companyID);
    List<EmmAction> getEmmActions(int companyID, boolean includeDeleted);

    List<EmmAction> getEmmActionsByName(int companyID, String shortName);

    String getEmmActionName(int actionId, int companyId);

    Map<Integer, String> getEmmActionNames(int companyId, List<Integer> actionIds);

    /**
     *  Loads all emm actions for certain company except actions of form type
     *
     * @param companyID
     *              The id of the company that uses the actions
     * @return List of emm actions or empty list
     */
    List<EmmAction> getEmmNotFormActions( int companyID);

    /**
     *  Loads all emm actions for certain company except actions of form type
     *
     * @param companyID
     *              The id of the company that uses the actions
     * @param includeInactive
     *              Whether ({@code true}) or not ({@code false}) actions marked as not active should be included.
     * @return List of emm actions or empty list
     */
    List<EmmAction> getEmmNotFormActions( int companyID, boolean includeInactive);

     /**
     *  Loads all emm actions for certain company except actions of link type
     *
     * @param companyID
     *              The id of the company that uses the actions
     * @return List of emm actions or empty list
     */
    List<EmmAction> getEmmNotLinkActions( int companyID);

     /**
     *  Loads all emm actions for certain company except actions of link type
     *
     * @param companyID
     *              The id of the company that uses the actions
       @param includeInactive
                    Whether ({@code true}) or not ({@code false}) actions marked as not active should be included.
     * @return List of emm actions or empty list
     */
    List<EmmAction> getEmmNotLinkActions( int companyID, boolean includeInactive);

    /**
     * Loads numbers of usage in forms for emm actions of certain company
     *
     * @param companyID
     *              The id of the company that uses the actions
     * @return HashMap object
     */
    Map<Integer, Integer> loadUsed( int companyID);

    List<String> getActionUserFormNames(int actionId, int companyId);

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

    Map<Integer, Boolean> getActivenessMap(Collection<Integer> actionIds, int companyId);

    void setActiveness(Collection<Integer> actionIds, boolean active, int companyId);

    /**
     * Loads list of emm actions that contain send mailing operation for a concrete mailing id.
     * @return List of emm actions
     */
    List<EmmAction> getActionListBySendMailingId(int companyId, int mailingId);

    boolean isAdvertising(int id, int companyId);
}
