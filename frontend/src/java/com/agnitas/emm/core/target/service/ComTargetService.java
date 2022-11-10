/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.exception.target.TargetGroupPersistenceException;
import org.agnitas.emm.core.target.exception.TargetGroupException;
import org.agnitas.emm.core.target.exception.UnknownTargetGroupIdException;
import org.agnitas.emm.core.target.service.UserActivityLog;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.struts.action.ActionMessages;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.ListSplit;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.recipient.dto.RecipientSaveTargetDto;
import com.agnitas.emm.core.recipient.web.RejectAccessByTargetGroupLimit;
import com.agnitas.emm.core.target.AltgMode;
import com.agnitas.emm.core.target.beans.TargetComplexityGrade;
import com.agnitas.emm.core.target.beans.TargetGroupDependentType;
import com.agnitas.emm.core.target.complexity.bean.TargetComplexityEvaluationCache;
import com.agnitas.messages.Message;
import com.agnitas.service.SimpleServiceResult;

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

    int saveTarget(ComAdmin admin, ComTarget newTarget, ComTarget target, ActionMessages errors, UserActivityLog userActivityLog) throws Exception;

	int saveTarget(ComAdmin admin, ComTarget newTarget, ComTarget target, List<Message> errors, List<UserAction> userActions) throws Exception;

	TargetSavingAndAnalysisResult saveTargetWithAnalysis(ComAdmin admin, ComTarget newTarget, ComTarget target, ActionMessages errors, UserActivityLog userActivityLog) throws Exception;

	int saveTarget(ComAdmin admin, ComTarget newTarget, ComTarget target, List<Message> errors, UserActivityLog userActivityLog) throws Exception;

	int saveTarget(ComTarget target) throws TargetGroupPersistenceException;

    SimpleServiceResult canBeDeleted(int targetId, @VelocityCheck int companyId);

    boolean hasMailingDeletedTargetGroups(Mailing mailing);
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

	@Deprecated // Use getTargetGroup(int, int) and ComTarget.getTargetSQL() instead
	String getTargetSQL(int targetId, @VelocityCheck int companyId);
	
	@Deprecated // Use getTargetGroup(int, int) and ComTarget.getTargetSQL() instead
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
	 * @see #getTargetGroup(int, int)
	 */
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

	Optional<Integer> getNumberOfRecipients(int targetId, int companyId);

	boolean lockTargetGroup(@VelocityCheck int companyId, int targetId);

	boolean unlockTargetGroup(@VelocityCheck int companyId, int targetId);

	boolean deleteRecipients(int targetId, @VelocityCheck int companyId);

	String getTargetName(int targetId, @VelocityCheck int companyId);

    String getTargetName(int targetId, @VelocityCheck int companyId, boolean includeDeleted);

	boolean checkIfTargetNameAlreadyExists(@VelocityCheck int companyID, String targetName, int targetID);

	boolean checkIfTargetNameIsValid(String targetShortname);
	
	boolean isWorkflowManagerListSplit(int companyID, int targetID) throws UnknownTargetGroupIdException;

	/**
	 * Use it for WS if you are sure you do not need ALTG check
	 *
	 * @param companyID
	 * @return
	 */
	List<TargetLight> getWsTargetLights(@VelocityCheck int companyID);

	/**
	 * Use {@link #getTargetLights(com.agnitas.beans.ComAdmin, boolean, boolean, boolean)} instead
	 * @Deprecated
	 *
	 * @see #getTargetLights(com.agnitas.beans.ComAdmin, boolean, boolean, boolean)
	 */
	List<TargetLight> getTargetLights(ComAdmin admin);
	List<TargetLight> getTargetLights(int companyID, boolean includeDeleted, boolean worldDelivery, boolean adminTestDelivery, boolean content);

	List<TargetLight> getTargetLights(ComAdmin admin, boolean worldDelivery, boolean adminTestDelivery,
			boolean content);

	List<TargetLight> getTargetLights(ComAdmin admin, boolean includeDeleted, boolean worldDelivery, boolean adminTestDelivery, boolean content);

	List<TargetLight> getTargetLights(TargetLightsOptions options);

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
	List<TargetLight> listTargetGroupsUsingProfileFieldByDatabaseName(final String fieldNameOnDatabase, @VelocityCheck final int companyID);

    List<String> getTargetNamesByIds(@VelocityCheck int companyId, Set<Integer> targetIds);

    /**
	 * Creates a matcher that checks, if a recipient matches a given target group.
	 * 
	 * @param customerID ID of customer
	 * @param companyID company ID of customer
	 * 
	 * @return matcher
	 * @throws Exception on errors creating the matcher
	 */
 	RecipientTargetGroupMatcher createRecipientTargetGroupMatcher(final int customerID, final int companyID) throws Exception;

	RecipientTargetGroupMatcher createRecipientTargetGroupMatcher(Map<String, Object> recipientData, int companyID) throws Exception;

	List<TargetLight> getTargetLights(@VelocityCheck int companyId, Collection<Integer> targetGroups, boolean includeDeleted);
	
	List<TargetLight> getSplitTargetLights(@VelocityCheck int companyId, String splitType);

    PaginatedListImpl<Dependent<TargetGroupDependentType>> getDependents(@VelocityCheck int companyId, int targetId, Set<TargetGroupDependentType> allowedTypes, int pageNumber, int pageSize, String sortColumn, String order);

	Map<Integer, TargetComplexityGrade> getTargetComplexities(@VelocityCheck int companyId);

	TargetComplexityGrade getTargetComplexityGrade(@VelocityCheck int companyId, int targetId);

	int calculateComplexityIndex(String eql, @VelocityCheck int companyId);

	int calculateComplexityIndex(String eql, @VelocityCheck int companyId, TargetComplexityEvaluationCache cache);

	void initializeComplexityIndex(@VelocityCheck int companyId);

	List<TargetLight> getAccessLimitationTargetLights(int companyId);

    List<TargetLight> getNoAccessLimitationTargetLights(int companyId);

    List<TargetLight> extractAdminAltgsFromTargetLights(List<TargetLight> targets, ComAdmin admin);

    List<TargetLight> filterTargetLightsByAltgMode(List<TargetLight> targets, AltgMode mode);

	boolean isBasicFullTextSearchSupported();

	boolean isRecipientMatchTarget(ComAdmin admin, int targetGroupId, int customerId);

	void checkRecipientTargetGroupAccess(ComAdmin admin, int customerId) throws RejectAccessByTargetGroupLimit;
	
	boolean isAltg(int targetId);

    Set<Integer> getAltgIdsWithoutAdminAltgIds(int companyId, Set<Integer> adminAltgIds);

    boolean isEqlContainsInvisibleFields(String eql, int companyId, int adminId);

	boolean isValid(int companyId, int targetId);

	boolean isLocked(int companyId, int targetId);

	int saveTargetFromRecipientSearch(ComAdmin admin, RecipientSaveTargetDto targetDto, List<Message> errors, List<UserAction> userActions);

    void addToFavorites(int targetId, int companyId);

    void removeFromFavorites(int targetId, int companyId);

	void deleteWorkflowTargetConditions(int companyID);

	List<TargetLight> getTargetLights(int fromCompanyID, boolean b);

	int getAccessLimitingTargetgroupsAmount(int companyId);
}
