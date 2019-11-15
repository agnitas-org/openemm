/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.Mailing;
import org.agnitas.dao.exception.target.TargetGroupPersistenceException;
import org.agnitas.emm.core.target.exception.TargetGroupException;
import org.agnitas.emm.core.target.exception.UnknownTargetGroupIdException;
import org.agnitas.emm.core.target.service.UserActivityLog;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.target.TargetRepresentation;
import org.apache.struts.action.ActionMessages;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.ListSplit;
import com.agnitas.beans.TargetLight;

/**
 * Service for target groups.
 */
public interface ComTargetService {

	/**
	 * Delete target group.
	 * 
	 * @param targetGroupID target group ID to be deleted 
	 * @param companyID company ID of target group
	 * @throws TargetGroupException on errors during processing
	 * @throws TargetGroupPersistenceException on errors during processing 
	 */
	
	void deleteTargetGroup(int targetGroupID, @VelocityCheck int companyID) throws TargetGroupException, TargetGroupPersistenceException;

    /**
     * Validates all target nodes and assigns error messages to correct target rule.
     *
     * @param representation target representation to validate
     * @param errors collection of error messages
     *
     * @return true, if errors were found
     */
    boolean validateTargetRepresentation(TargetRepresentation representation, ActionMessages errors, @VelocityCheck int companyId);

    int saveTarget(ComAdmin admin, ComTarget newTarget, ComTarget target, ActionMessages errors, UserActivityLog userActivityLog) throws Exception;

    int saveTarget(ComTarget target) throws TargetGroupPersistenceException;

    boolean hasMailingDeletedTargetGroups( Mailing mailing);
    Set<Integer> getTargetIdsFromExpression(Mailing mailing);
	
	/**
	 * Do bulk delete on target groups.
	 * 
	 * @param targetIds target IDs to delete
	 * @param companyId company ID of target groups
	 */
	void bulkDelete(Set<Integer> targetIds, @VelocityCheck int companyId) throws TargetGroupPersistenceException, TargetGroupException;

	/**
	 * Method generates SQL expression from mailing target expression.
	 * Supports using of symbols "|", "&", "!", "(", ")".
	 *
	 * @param mailing mailing
	 * @param appendListSplit if true - we need to add list-split target SQL to generated SQL expression
	 * @return SQL expression that represents mailing target expression
	 */
	String getSQLFromTargetExpression(Mailing mailing, boolean appendListSplit);

	String getSQLFromTargetExpression(String targetExpression, @VelocityCheck int companyId);

	String getSQLFromTargetExpression(String targetExpression, int splitId, @VelocityCheck int companyId);

	String getTargetSQL(int targetId, @VelocityCheck int companyId);
	
	String getTargetSQLWithSimpleIfNotExists(int targetId, @VelocityCheck int companyId);
	
	String getTargetSQL(int targetId, @VelocityCheck int companyId, boolean isPositive);

	String getMailingSqlTargetExpression(int mailingId, @VelocityCheck int companyId, boolean appendListSplit);

	/**
	 * Returns target group for given ID or <code>null</code> if given ID is unknown.
	 * Consider using {@link #getTargetGroup(int, int)}.
	 * 
	 * @param targetId ID of target group
	 * @param companyId company ID
	 * 
	 * @return target group or <code>null</code>
	 * 
	 * @see #getTargetGroup(int, int)
	 */
	@Deprecated
    ComTarget getTargetGroupOrNull(int targetId, int companyId);
	
	/**
	 * Returns target group for given ID.
	 * 
	 * @param targetId ID of target group
	 * @param companyId company ID
	 * 
	 * @return target group
	 * 
	 * @throws UnknownTargetGroupIdException if target group ID is unknown
	 */
	ComTarget getTargetGroup(int targetId, int companyId) throws UnknownTargetGroupIdException;

    boolean lockTargetGroup(@VelocityCheck int companyId, int targetId);

	boolean unlockTargetGroup(@VelocityCheck int companyId, int targetId);

	void deleteRecipients(int targetId, @VelocityCheck int companyId);

	String getTargetName(int targetId, @VelocityCheck int companyId);

    String getTargetName(int targetId, @VelocityCheck int companyId, boolean includeDeleted);

    Map<Integer, String> getTargetNames(int companyId, Collection<Integer> targetIds);

	boolean checkIfTargetNameAlreadyExists(int companyID, String targetName, int targetID);

	boolean checkIfTargetNameIsValid(String targetShortname);
	
	boolean isWorkflowManagerListSplit(int companyID, int targetID) throws UnknownTargetGroupIdException;

	/**
	 * Use {@link #getTargetLights(int, boolean, boolean, boolean)} instead 
	 * @Deprecated
	 * 
	 * @see #getTargetLights(int, boolean, boolean, boolean)
	 */
	List<TargetLight> getTargetLights(@VelocityCheck int companyID);
	
	List<TargetLight> getTargetLights(int companyID, boolean worldDelivery, boolean adminTestDelivery, boolean content);

	/**
	 * Get all the valid list split targets represented as {@link com.agnitas.beans.ListSplit}.
	 *
	 * @param companyId an identifier of the company that owns retrieved entities.
	 * @return a list of valid {@link com.agnitas.beans.ListSplit} entities sorted by parts count (primarily) and part number (secondarily).
     */
	List<ListSplit> getListSplits(@VelocityCheck int companyId);
    
    int getTargetListSplitId(String splitBase, String splitPart, boolean isWmSplit);
    
	String getTargetSplitName(int splitId);

	/**
	 * Lists all target groups of the given company ID that reference the given profile field. 
	 * 
	 * @param fieldNameOnDatabase database name of the profile field
	 * @param companyID company ID
	 * 
	 * @return list of target groups referencing given profile field
	 */
	List<TargetLight> listTargetGroupsUsingProfileFieldByDatabaseName(final String fieldNameOnDatabase, final int companyID);

	/**
	 * Lists all target groups of the given company ID that reference the given reference table. 
	 * 
	 * @param tableName of reference table
	 * @param companyID company ID
	 * 
	 * @return list of target groups referencing given reference table
	 */
	List<TargetLight> listTargetGroupsUsingReferenceTable(final String tableName, final int companyID);

	/**
	 * Lists all target groups of the given company ID that reference the given field of reference table. 
	 * 
	 * @param tableName name of reference table
	 * @param columnName name of column in reference table
	 * @param companyID company ID
	 * 
	 * @return list of target groups referencing given reference table field
	 */
	List<TargetLight> listTargetGroupsUsingReferenceTableColumn(final String tableName, final String columnName, final int companyID);

	String toViewUri(int targetId);
	
	/**
	 * Creates a matcher that checks, if a recipient matches a given target group.
	 * 
	 * @param customerID ID of customer
	 * @param companyID company ID of customer
	 * 
	 * @return matcher
	 * @throws Exception on errors creating the matcher 
	 */
 	public RecipientTargetGroupMatcher createRecipientTargetGroupMatcher(final int customerID, final int companyID) throws Exception;
	
	List<TargetLight> getTargetLights(@VelocityCheck int companyId, Collection<Integer> targetGroups, boolean includeDeleted);
	
	List<TargetLight> getSplitTargetLights(@VelocityCheck int companyId, String s);
}
