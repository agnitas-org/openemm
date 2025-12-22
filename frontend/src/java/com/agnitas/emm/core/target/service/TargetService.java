/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.Target;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.TrackableLink;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.mailing.forms.SplitSettings;
import com.agnitas.emm.core.recipient.dto.RecipientSaveTargetDto;
import com.agnitas.emm.core.recipient.web.RejectAccessByTargetGroupLimit;
import com.agnitas.emm.core.target.AltgMode;
import com.agnitas.emm.core.target.beans.TargetComplexityGrade;
import com.agnitas.emm.core.target.beans.TargetGroupDeliveryOption;
import com.agnitas.emm.core.target.beans.TargetGroupDependentType;
import com.agnitas.emm.core.target.exception.TargetGroupPersistenceException;
import com.agnitas.emm.core.target.exception.TargetGroupNotFoundException;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.web.forms.PaginationForm;
import org.springframework.ui.Model;

/**
 * Service for target groups.
 */
public interface TargetService {

	/**
	 * Delete target group.
	 * 
	 * @param targetGroupID target group ID to be deleted
	 */
	SimpleServiceResult deleteTargetGroup(int targetGroupID, Admin admin);
	
	boolean deleteTargetGroupByCompanyID(int companyID);

	int saveTarget(Admin admin, Target newTarget, Target target, List<Message> errors, List<UserAction> userActions) throws Exception;

	int saveTarget(Admin admin, Target newTarget, Target target, List<Message> errors, UserActivityLog userActivityLog) throws Exception;

	int saveTarget(Target target) throws TargetGroupPersistenceException;

    SimpleServiceResult canBeDeleted(int targetId, Admin admin);

    boolean hasMailingDeletedTargetGroups(Mailing mailing);

    Set<Integer> getTargetIdsFromExpression(Mailing mailing);
	
	List<Integer> bulkDelete(Set<Integer> ids, Admin admin);

	/**
	 * Method generates SQL expression from mailing target expression.
	 * Supports using of symbols "|", "&", "!", "(", ")".
	 *
	 * @param mailing mailing
	 * @param appendListSplit if true - we need to add list-split target SQL to generated SQL expression
	 * @return SQL expression that represents mailing target expression
	 */
	String getSQLFromTargetExpression(Mailing mailing, boolean appendListSplit);

	String getSQLFromTargetExpression(String targetExpression, int splitId, int companyId);

	@Deprecated // Use getTargetGroup(int, int) and Target.getTargetSQL() instead
	String getTargetSQL(int targetId, int companyId);
	
	@Deprecated // Use getTargetGroup(int, int) and Target.getTargetSQL() instead
	String getTargetSQL(int targetId, int companyId, boolean isPositive);

	String getMailingSqlTargetExpression(int mailingId, int companyId, boolean appendListSplit);

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
    Target getTargetGroupOrNull(int targetId, int companyId);

    Target getTargetGroupOrNull(int targetId, int companyId, int adminId);

	/**
	 * Returns target group for given ID.
	 * 
	 * @param targetId ID of target group
	 * @param companyId company ID
	 * 
	 * @return target group
	 * 
	 * @throws TargetGroupNotFoundException if target group not found
	 */
	Target getTargetGroup(int targetId, int companyId);

	Target getTargetGroup(int targetId, int companyId, int adminId);

	Optional<Integer> getNumberOfRecipients(int targetId, int companyId);

	boolean lockTargetGroup(int companyId, int targetId);

	boolean unlockTargetGroup(int companyId, int targetId);

	boolean deleteRecipients(int targetId, int companyId);

	String getTargetName(int targetId, int companyId, Locale locale);

	String getTargetName(int targetId, int companyId);

	ServiceResult<List<String>> getTargetNamesForDeletion(List<Integer> ids, Admin admin);

    String getTargetName(int targetId, int companyId, boolean includeDeleted);

	boolean checkIfTargetNameAlreadyExists(int companyID, String targetName, int targetID);

	boolean checkIfTargetNameIsValid(String targetShortname);
	
	/**
	 * Use it for WS if you are sure you do not need ALTG check
	 */
	List<TargetLight> getWsTargetLights(int companyID);

	/**
	 * Use {@link #getTargetLights(com.agnitas.beans.Admin, boolean, boolean, boolean)} instead
	 * @Deprecated
	 *
	 * @see #getTargetLights(com.agnitas.beans.Admin, boolean, boolean, boolean)
	 */
	List<TargetLight> getTargetLights(Admin admin);

	List<TargetLight> getTargetLights(Admin admin, boolean content, TargetGroupDeliveryOption delivery);

	PaginatedList<TargetLight> getTargetLightsPaginated(TargetLightsOptions options, TargetComplexityGrade complexityGrade);

    int getTargetListSplitId(String splitBase, String splitPart, boolean isWmSplit);
    
	/**
	 * Lists all target groups of the given company ID that reference the given profile field.
	 * 
	 * @param fieldNameOnDatabase database name of the profile field
	 * @param companyID company ID
	 * 
	 * @return list of target groups referencing given profile field
	 */
	List<TargetLight> listTargetGroupsUsingProfileFieldByDatabaseName(final String fieldNameOnDatabase, final int companyID);

    List<String> getTargetNamesByIds(int companyId, Set<Integer> targetIds);

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

	List<TargetLight> getTargetLights(int companyId, Collection<Integer> targetGroups, boolean includeDeleted);
	
	List<TargetLight> getSplitTargetLights(int companyId, String splitType);

    PaginatedList<Dependent<TargetGroupDependentType>> getDependents(int targetId, int companyId, PaginationForm filter);

    Map<Integer, TargetComplexityGrade> getTargetComplexities(int companyId);

	TargetComplexityGrade getTargetComplexityGrade(int companyId, int targetId);

	int calculateComplexityIndex(String eql, int companyId);

	List<TargetLight> getAccessLimitationTargetLights(int companyId);

	List<TargetLight> getAccessLimitationTargetLights(int adminId, int companyId);

    List<TargetLight> extractAdminAltgsFromTargetLights(List<TargetLight> targets, Admin admin);

    List<TargetLight> getAdminAltgs(Admin admin);

    List<TargetLight> filterTargetLightsByAltgMode(List<TargetLight> targets, AltgMode mode);

	boolean isBasicFullTextSearchSupported();

	void checkRecipientTargetGroupAccess(Admin admin, int customerId) throws RejectAccessByTargetGroupLimit;
	
	boolean isAltg(int targetId);

    boolean isHidden(int targetId, int companyId);

    Set<Integer> getAltgIdsWithoutAdminAltgIds(int companyId, Set<Integer> adminAltgIds);

    boolean exist(int targetId, int companyId);

    boolean isEqlContainsInvisibleFields(String eql, int companyId, int adminId);

	boolean isValid(int companyId, int targetId);

	boolean isLocked(int companyId, int targetId);

	int saveTargetFromRecipientSearch(Admin admin, RecipientSaveTargetDto targetDto, List<Message> errors, List<UserAction> userActions);

    void addToFavorites(int targetId, int companyId);

    void removeFromFavorites(int targetId, int companyId);

	void deleteWorkflowTargetConditions(int companyID);

	int getTargetListSplitIdForSave(int splitId, String splitBase, String splitPart);

	void setSplitSettings(SplitSettings split, int splitId, boolean preserveCmListSplit);

	void addSplitTargetModelAttrs(Model model, int companyId, int splitId, String splitBase, String splitPart);

	List<TargetLight> getTargetLights(int fromCompanyID);

	int getAccessLimitingTargetgroupsAmount(int companyId);

    boolean isLinkUsedInTarget(TrackableLink link);

    void markAsFavorite(int targetId, int adminId, int companyId);

    void unmarkAsFavorite(int targetId, int adminId, int companyId);

	List<TargetLight> listTargetLightsForMailingSettings(final Admin admin, final Mailing mailing);

	Set<Integer> getInvalidTargets(int companyId, Set<Integer> targets);

    void restore(Set<Integer> ids, Admin admin);

	void removeMarkedAsDeletedBefore(Date date, int companyID);
}
