/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.TrackableLink;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.exception.target.TargetGroupLockedException;
import org.agnitas.dao.exception.target.TargetGroupPersistenceException;

import com.agnitas.beans.ComTarget;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.target.beans.TargetGroupDependentType;
import com.agnitas.emm.core.target.service.TargetLightsOptions;
import com.helger.collection.pair.Pair;

public interface ComTargetDao {
    /**
     * Marks target group as "deleted" in the database.
     *
     * @param targetID
     *          The id of the target group to mark.
     * @param companyID
     *          The id of company.
     * @return  true on success.
     * @throws TargetGroupLockedException on errors saving target group
     */
    boolean deleteTarget(int targetID, int companyID) throws TargetGroupLockedException;
    
    /**
     *  Loads target group identified by target id and company id.
     *
     * @param targetID
     *           The id of the target group that should be loaded.
     * @param companyID
     *          The companyID for the target group.
     * @return The Target or null on failure.
     */
    ComTarget getTarget(int targetID, int companyID);

    /**
     *  Loads target group identified by target name and company id.
     *
     * @param targetName
     *           The name of the target group that should be loaded.
     * @param companyID
     *          The companyID for the target group.
     * @return The Target or null on failure.
     */
	/*
	 * IMPORTANT NOTE: Target group names are not unique!
	 *
	 * The only exception to this are list split target groups. (Names of these target groups are unique.)
	 * This method MUST NOT be used for other target groups than list split target groups.
	 *
	 * TODO: Replace this method by a new method that takes list split data as parameters to avoid misuse of this method.
	 */
    ComTarget getTargetByName(String targetName, int companyID);

    /**
     *  Loads target group identified by list split parameters and company id.
     *
     * @param companyID
     *          The companyID for the target group.
     * @return The Target or null on failure.
     */
    ComTarget getListSplitTarget(String splitType, int index, int companyID);

    /**
     *  Loads target group identified by list split parameters and company id.
     *
     * @param companyID
     *          The companyID for the target group.
     * @return The Target or null on failure.
     */
    ComTarget getListSplitTarget(String prefix, String splitType, int index, int companyID);

    /**
     * Loads all target groups marked as "deleted" for company id.
     * Uses JdbcTemplate.
     *
     * @param companyID
     *          The companyID for the target groups.
     *
     * @return List of Targets or empty list.
     */
    List<Integer> getDeletedTargets(int companyID);

    /**
     * Saves or updates target group in database. Target group is visible in lists.
     *
     * @param target
     *          The target group to save.
     * @return Saved target group id.
     * @throws TargetGroupPersistenceException on errors saving target group
     */
    int saveTarget(ComTarget target) throws TargetGroupPersistenceException;
    
    /**
     * Saves or updates target group in database. Target group is hidden in lists.
     *
     * @param target
     *          The target group to save.
     * @return Saved target group id.
     * @throws TargetGroupPersistenceException on errors saving target group
     */
    int saveHiddenTarget(ComTarget target) throws TargetGroupPersistenceException;
    
    /**
     * Loads all target groups allowed for given company.
     * Uses JdbcTemplate.
     *
     * @param companyID
     *      The companyID for the target groups.
     * @return List of Targets or empty list.
     */
	Map<Integer, ComTarget>	getAllowedTargets(int companyID);

    /**
     * Get a shortname of the target referenced by {@code targetId}.
     * @param targetId an identifier of a target.
     * @param companyId an identifier of a company.
     * @param includeDeleted whether ({@code true}) treat a deleted (marked as deleted) target as existing or not.
     * @return a target name or {@code null} if a {@code targetId} doesn't exist
     * or a referenced target belongs to another company.
     */
    String getTargetName(int targetId, int companyId, boolean includeDeleted);

	/**
	 * Check if there's a target having given name.
	 *
	 * @param companyId an identifier of a company.
	 * @param targetName a name to check.
	 * @param includeDeleted whether ({@code true}) or not ({@code false}) deleted targets should also be considered.
	 * @return {@code true} if there's at least one target having given name.
	 */
	boolean isTargetNameInUse(int companyId, String targetName, boolean includeDeleted);

	/**
	 * Load list of Target groups names by IDs.
     * Uses JdbcTemplate.
     *
	 * @param companyId company ID
	 * @param targetIds the IDs of target groups
	 * @return the list of names
	 */
	List<String> getTargetNamesByIds(int companyId, Set<Integer> targetIds);
	
	String getTargetSplitName(int targetId);
	
	int getTargetSplitID(String name);

	String getTargetSQL(int targetId, int companyId);

	List<String> getSplitNames(int companyID);
	
	int getSplits(int companyID, String shortName);

	/**
	 * Safely copy sample target groups from company 1 to selected company.
	 * @param companyID more than 1
	 * @return number of copied target groups
	 */
	int createSampleTargetGroups(int companyID);

    boolean isTargetGroupLocked(int targetID, int companyID);

    void updateTargetLockState(int targetID, int companyID, boolean locked);

    boolean deleteTargetReally(int targetID, int companyId);

    boolean deleteTargetsReally(int companyID);

    List<TargetLight> getTargetLights(int companyID);
    
    List<TargetLight> getTargetLights(int companyID, boolean includeDeleted);
    
    List<TargetLight> getTargetLights(int companyID, boolean includeDeleted, boolean worldDelivery, boolean adminTestDelivery);

    List<TargetLight> getTargetLights(int adminId, int companyID, boolean includeDeleted, boolean worldDelivery, boolean adminTestDelivery);
    
	List<TargetLight> getTargetLights(int companyID, boolean includeDeleted, boolean worldDelivery, boolean adminTestDelivery, boolean content);

	List<TargetLight> getTargetLights(int adminId, int companyID, boolean includeDeleted, boolean worldDelivery, boolean adminTestDelivery, boolean content);

	List<TargetLight> getTargetLights(int companyID, Collection<Integer> targetIds, boolean includeDeleted);

	List<TargetLight> getTargetLightsBySearchParameters(TargetLightsOptions options);

	List<TargetLight> getUnchoosenTargetLights(int companyID, Collection<Integer> targetIds);
	
	List<TargetLight> getChoosenTargetLights(String targetExpression, final int companyID);
	
	List<TargetLight> getTestAndAdminTargetLights(int companyId);

	List<TargetLight> getTestAndAdminTargetLights(int adminId, int companyId);

    List<TargetLight> getSplitTargetLights(int companyID, String splitType);

	PaginatedListImpl<TargetLight> getPaginatedTargetLightsBySearchParameters(TargetLightsOptions options);

	boolean isBasicFullTextSearchSupported();
    
	boolean deleteWorkflowTargetConditions(int companyId, int workflowId);
	
	void deleteWorkflowTargetConditions(int companyId);
	
    // -------------------------------------------------------------------------------------- Deprecated API

	Map<Integer, TargetLight> getAllowedTargetLights(int companyID);

    Set<Integer> getInvalidTargets(int companyId, Set<Integer> targets);

    boolean isOracle();

	/**
	 * Lists all (non-deleted) of a company with ID, name and EQL.
	 * @param companyId company ID
	 * @return list of target groups
	 */
	List<ComTarget> listRawTargetGroups(int companyId, String ...eqlRawFragments);
	
	List<ComTarget> getTargetByNameAndSQL(int companyId, String targetName, String targetSQL, boolean includeDeleted, boolean worldDelivery, boolean adminTestDelivery);

    PaginatedListImpl<Dependent<TargetGroupDependentType>> getDependents(int companyId, int targetId, Set<TargetGroupDependentType> allowedTypes, int pageNumber, int pageSize, String sortColumn, String order);

	Map<Integer, Integer> getTargetComplexityIndices(int companyId);

	Integer getTargetComplexityIndex(int companyId, int targetId);

	List<Pair<Integer, String>> getTargetsToInitializeComplexityIndices(int companyId);

	void saveComplexityIndices(int companyId, Map<Integer, Integer> complexities);

    boolean isValid(int companyId, int targetId);

    void addToFavorites(int targetId, int companyId);
    
    void removeFromFavorites(int targetId, int companyId);

    void markAsFavorite(int targetId, int adminId, int companyId);

    void unmarkAsFavorite(int targetId, int adminId, int companyId);

	boolean isValidEql(int companyID, String eql);

    boolean isAltg(int targetId);

    boolean isHidden(int targetId, int companyId);

	int getAccessLimitingTargetgroupsAmount(int companyId);

    boolean isLinkUsedInTarget(TrackableLink link);

    boolean isTargetFavoriteForAdmin(ComTarget target, int adminId);
}
