/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.service.impl;

import static com.agnitas.beans.Mailing.NONE_SPLIT_ID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.beans.TrackableLink;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailingComponentDao;
import org.agnitas.dao.exception.target.TargetGroupLockedException;
import org.agnitas.dao.exception.target.TargetGroupPersistenceException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.target.exception.UnknownTargetGroupIdException;
import org.agnitas.emm.core.target.service.UserActivityLog;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.target.TargetFactory;
import org.agnitas.util.beanshell.BeanShellInterpreterFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.ListSplit;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.profilefields.ProfileFieldException;
import com.agnitas.emm.core.profilefields.service.ProfileFieldService;
import com.agnitas.emm.core.recipient.dto.RecipientSaveTargetDto;
import com.agnitas.emm.core.recipient.web.RejectAccessByTargetGroupLimit;
import com.agnitas.emm.core.target.AltgMode;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.TargetUtils;
import com.agnitas.emm.core.target.beans.TargetComplexityGrade;
import com.agnitas.emm.core.target.beans.TargetGroupDependentEntry;
import com.agnitas.emm.core.target.beans.TargetGroupDependentType;
import com.agnitas.emm.core.target.complexity.bean.TargetComplexityEvaluationCache;
import com.agnitas.emm.core.target.complexity.bean.impl.TargetComplexityEvaluationCacheImpl;
import com.agnitas.emm.core.target.complexity.service.TargetComplexityEvaluator;
import com.agnitas.emm.core.target.eql.EqlAnalysisResult;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.eql.EqlValidatorService;
import com.agnitas.emm.core.target.eql.codegen.CodeGeneratorException;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.codegen.resolver.ReferenceTableResolveException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlReferenceItemsExtractor;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConverter;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import com.agnitas.emm.core.target.eql.referencecollector.SimpleReferenceCollector;
import com.agnitas.emm.core.target.exception.EqlFormatException;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.target.service.RecipientTargetGroupMatcher;
import com.agnitas.emm.core.target.service.ReferencedItemsService;
import com.agnitas.emm.core.target.service.TargetGroupDependencyService;
import com.agnitas.emm.core.target.service.TargetLightsOptions;
import com.agnitas.emm.core.target.service.TargetSavingAndAnalysisResult;
import com.agnitas.messages.Message;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.helger.collection.pair.Pair;

import bsh.Interpreter;

/**
 * Implementation of {@link ComTargetService} interface.
 */
public class ComTargetServiceImpl implements ComTargetService {

	private static final Logger logger = LogManager.getLogger(ComTargetServiceImpl.class);

	private static final Pattern GENDER_EQUATION_PATTERN = Pattern.compile("(?:cust\\.gender\\s*(?:=|<>|>|>=|<|<=)\\s*(-?\\d+))|(?:mod\\(cust\\.gender,\\s+(\\d+)\\))");

	/** DAO accessing target groups. */
    protected ComTargetDao targetDao;

    /** DAO accessing mailing components. */
    private MailingComponentDao mailingComponentDao;

    /** DAO for accessing mailing data. */
    private ComMailingDao mailingDao;

	/** DAO for accessing recipient data. */
	protected ComRecipientDao recipientDao;

	/** Facade for EQL logic. */
	private EqlFacade eqlFacade;

	/** Service dealing with profile fields. */
	private ProfileFieldService profileFieldService;

	private TargetGroupDependencyService targetGroupDependencyService;
	private TargetComplexityEvaluator complexityEvaluator;
	private EqlReferenceItemsExtractor eqlReferenceItemsExtractor;
	private BeanShellInterpreterFactory beanShellInterpreterFactory;
	private ColumnInfoService columnInfoService;
	private ReferencedItemsService referencedItemsService;
	private TargetFactory targetFactory;
	private QueryBuilderToEqlConverter queryBuilderToEqlConverter;
    private EqlValidatorService eqlValidatorService;

    @Override
	public SimpleServiceResult deleteTargetGroup(int targetGroupID, Admin admin, boolean buildErrorMessages) {
    	int companyID = admin.getCompanyID();

		if (logger.isInfoEnabled()) {
			logger.info("Deleting target group {} of company {}", targetGroupID, companyID);
		}

		List<TargetGroupDependentEntry> dependencies = targetGroupDependencyService.findDependencies(targetGroupID, companyID);
		Optional<TargetGroupDependentEntry> actualDependency = targetGroupDependencyService.findAnyActualDependency(dependencies);

		if (actualDependency.isPresent()) {
			if (buildErrorMessages) {
				String targetName = getTargetName(targetGroupID, companyID);
				Message errorMessage = targetGroupDependencyService.buildErrorMessage(actualDependency.get(), targetName, admin);

				return SimpleServiceResult.simpleError(errorMessage);
			}

			return SimpleServiceResult.simpleError();
		}

		referencedItemsService.removeReferencedItems(companyID, targetGroupID);

		if (dependencies.isEmpty()) {
			if (targetDao.isTargetGroupLocked(targetGroupID, companyID)) {
				return SimpleServiceResult.simpleError(Message.of("target.locked"));
			}

			targetDao.deleteTargetReally(targetGroupID, companyID);
			return SimpleServiceResult.simpleSuccess();
		}

		try {
			targetDao.deleteTarget(targetGroupID, companyID);
			return SimpleServiceResult.simpleSuccess();
		} catch (TargetGroupLockedException e) {
			return SimpleServiceResult.simpleError(Message.of("target.locked"));
		}
	}
    
    @Override
    public boolean deleteTargetGroupByCompanyID(int companyID) {
		if (logger.isInfoEnabled()) {
			logger.info("Deleting target group of company {}", companyID);
		}

		try {
			targetDao.deleteTargetsReally(companyID);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public SimpleServiceResult canBeDeleted(int targetId, Admin admin) {
		int companyId = admin.getCompanyID();

		List<TargetGroupDependentEntry> dependencies = targetGroupDependencyService.findDependencies(targetId, companyId);
		Optional<TargetGroupDependentEntry> actualDependency = targetGroupDependencyService.findAnyActualDependency(dependencies);

		if (actualDependency.isPresent()) {
			String targetName = getTargetName(targetId, companyId);
			Message errorMessage = targetGroupDependencyService.buildErrorMessage(actualDependency.get(), targetName, admin);

			return SimpleServiceResult.simpleError(errorMessage);
		}

		if (isLocked(companyId, targetId)) {
			return SimpleServiceResult.simpleError(Message.of("target.locked"));
		}

		return SimpleServiceResult.simpleSuccess();
	}

    @Override
    public boolean hasMailingDeletedTargetGroups(Mailing mailing) {
		if (logger.isInfoEnabled()) {
			logger.info(String.format("Checking mailing %d for deleted target groups", mailing.getId()));
		}

    	Set<Integer> targetIds = getAllTargetIdsForMailing( mailing);

    	for (int targetId : targetIds) {
    		ComTarget target = targetDao.getTarget(targetId, mailing.getCompanyID());

    		if (target == null) {
    			if (logger.isInfoEnabled()) {
    				logger.info(String.format("Found non-existing target group %d. It's assumed to be physically deleted.", targetId));
    			}

    			continue;
    		}

    		if (target.getDeleted() != 0) {
    			if (logger.isInfoEnabled()) {
    				logger.info(String.format("Found deleted target group %d.", targetId));
    			}

    			return true;
    		}
    	}

    	if (logger.isInfoEnabled()) {
			logger.info(String.format("Mailing %d does not contain any deleted target groups", mailing.getId()));
		}

    	return false;
    }

	private Set<Integer> getAllTargetIdsForMailing( Mailing mailing) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Collecting target groups IDs for mailing %d", mailing.getId()));
		}

    	Set<Integer> targetIds = new HashSet<>();

    	targetIds.addAll(getTargetIdsFromExpression(mailing));
    	targetIds.addAll(getTargetIdsFromContent(mailing));
    	targetIds.addAll(getTargetIdsFromAttachments(mailing));

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Collected %d different target group IDs in total for mailing %d", mailing.getId(), mailing.getId()));
		}

    	return targetIds;
	}

	@Override
    public Set<Integer> getTargetIdsFromExpression(Mailing mailing) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Collecting target groups IDs for mailing %d from mailing target expression.", mailing.getId()));
		}

    	if (mailing == null) {
    		return new HashSet<>();
    	}

		String expression = mailing.getTargetExpression();
		Set<Integer> targetIds = TargetExpressionUtils.getTargetIds(expression);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Collected %d different target group IDs from target expression for mailing %d", mailing.getId(), mailing.getId()));
		}

		return targetIds;
    }

    private Set<Integer> getTargetIdsFromContent(Mailing mailing) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Collecting target groups IDs for mailing %d from content blocks.", mailing.getId()));
		}

    	Set<Integer> targetIds = new HashSet<>();

    	for (DynamicTag tag : mailing.getDynTags().values()) {
    		for(Object contentObject : tag.getDynContent().values()) {
    			DynamicTagContent content = (DynamicTagContent) contentObject;
    			targetIds.add( content.getTargetID());
    		}
    	}

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Collected %d different target group IDs from content blocks for mailing %d", mailing.getId(), mailing.getId()));
		}

    	return targetIds;
    }

    private Set<Integer> getTargetIdsFromAttachments(Mailing mailing) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Collecting target groups IDs for mailing %d from attachments.", mailing.getId()));
		}

		List<MailingComponent> result = mailingComponentDao.getMailingComponents(mailing.getId(), mailing.getCompanyID(), MailingComponentType.Attachment);

    	Set<Integer> targetIds = new HashSet<>();
    	for( MailingComponent component : result) {
    		targetIds.add( component.getTargetID());
    	}

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Collected %d different target group IDs from attachments for mailing %d", mailing.getId(), mailing.getId()));
		}

    	return targetIds;
    }

    @Override
    public int saveTarget(Admin admin, ComTarget newTarget, ComTarget target, ActionMessages errors, UserActivityLog userActivityLog) throws Exception {	// TODO: Remove "ActionMessages" to remove dependencies to Struts
    	final TargetSavingAndAnalysisResult result = saveTargetWithAnalysis(admin, newTarget, target, errors, userActivityLog);

    	return result.getTargetID();
    }

	@Override
	public TargetSavingAndAnalysisResult saveTargetWithAnalysis(Admin admin, ComTarget newTarget, ComTarget target, ActionMessages errors, UserActivityLog userActivityLog) throws Exception {	// TODO: Remove "ActionMessages" to remove dependencies to Struts
		if (target == null) {
			// be sure to use id 0 if there is no existing object
			newTarget.setId(0);
		}

		if (validateTargetDefinition(admin.getCompanyID(), newTarget.getEQL())) {
			newTarget.setComplexityIndex(calculateComplexityIndex(newTarget.getEQL(), admin.getCompanyID()));

			final int newId = saveTarget(newTarget);

			postValidation(admin, newTarget).forEach(error -> errors.add(ActionMessages.GLOBAL_MESSAGE, error));

			newTarget.setId(newId);

			final EqlAnalysisResult analysisResultOrNull = this.eqlFacade.analyseEql(newTarget.getEQL());

			getSavingLogs(newTarget, target).forEach(action -> userActivityLog.write(admin, action.getAction(), action.getDescription()));

			return new TargetSavingAndAnalysisResult(newId, analysisResultOrNull);
		} else {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.definition"));
			return new TargetSavingAndAnalysisResult(0, null);
		}
	}

    @Override
    public int saveTarget(Admin admin, ComTarget newTarget, ComTarget target, List<Message> errors, List<UserAction> userActions) throws TargetGroupPersistenceException {
        if (target == null) {
            // be sure to use id 0 if there is no existing object
            newTarget.setId(0);
        }

        Collection<Message> validationErrors = eqlValidatorService.validateEql(admin, newTarget.getEQL());
        if(CollectionUtils.isNotEmpty(validationErrors)) {
            errors.addAll(validationErrors);
            return 0;
        }

        newTarget.setComplexityIndex(calculateComplexityIndex(newTarget.getEQL(), admin.getCompanyID()));
        newTarget.setSavingAdminId(admin.getAdminID());

        final int newId = saveTarget(newTarget);

        postValidation(newTarget).forEach(code -> errors.add(Message.of(code)));

        newTarget.setId(newId);

        userActions.addAll(getSavingLogs(newTarget, target));

        return newId;
    }

    @Override
    public int saveTarget(Admin admin, ComTarget newTarget, ComTarget target, List<Message> errors, UserActivityLog userActivityLog) throws Exception {
        List<UserAction> userActions = new LinkedList<>();
        int newTargetId = saveTarget(admin, newTarget, target, errors, userActions);
        userActions.forEach(userAction -> userActivityLog.write(admin, userAction.getAction(), userAction.getDescription()));
        return newTargetId;
    }

	@Override
	public int saveTarget(ComTarget target) throws TargetGroupPersistenceException {
		try {
			final SimpleReferenceCollector referencedItemsCollector = new SimpleReferenceCollector();
			eqlFacade.convertEqlToSql(target.getEQL(), target.getCompanyID(), referencedItemsCollector);
			
			final int targetID = targetDao.saveTarget(target);
			
			referencedItemsService.saveReferencedItems(referencedItemsCollector, target.getCompanyID(), targetID);
			
			return targetID;
		} catch (EqlParserException | CodeGeneratorException | ProfileFieldResolveException | ReferenceTableResolveException e) {
			logger.error("Error parsing EQL", e);
			
			throw new EqlFormatException("Error parsing EQL representation.", e);
		}
	}

	@Override
	public ServiceResult<List<Integer>> bulkDelete(Set<Integer> targetIds, Admin admin) {
		List<Integer> deletedTargets = new ArrayList<>();
		List<Message> errorMessages = new ArrayList<>();

		for (int targetId : targetIds) {
			SimpleServiceResult deletionResult = deleteTargetGroup(targetId, admin, true);

			if (deletionResult.isSuccess()) {
				deletedTargets.add(targetId);
			} else {
				errorMessages.addAll(deletionResult.getErrorMessages());
			}
		}

		return new ServiceResult<>(deletedTargets, errorMessages.isEmpty(), errorMessages);
	}

	@Override
	public String getSQLFromTargetExpression(Mailing mailing, boolean appendListSplit) {
		String targetExpression = mailing.getTargetExpression();

		// if the final SQL expression should contain split-target-SQL - append it to expression and target list
		int splitId = 0;

		if (appendListSplit) {
			splitId = mailing.getSplitID();
		}

		return getSQLFromTargetExpression(targetExpression, splitId, mailing.getCompanyID());
	}

	@Override
	public String getSQLFromTargetExpression(String targetExpression, int splitId, int companyId) {
		if (splitId > 0) {
			if (StringUtils.isNotBlank(targetExpression)) {
				targetExpression = "(" + targetExpression + ")&" + splitId;
			} else {
				targetExpression = Integer.toString(splitId);
			}
		}

		if (StringUtils.isNotBlank(targetExpression)) {
			return getSQLFromTargetExpression(targetExpression, new TargetSqlCachingResolver(companyId));
		} else {
			return "";
		}
	}

	private String getSQLFromTargetExpression(String targetExpression, TargetSqlResolver resolver) {
    	if (StringUtils.isBlank(targetExpression)) {
    		return targetExpression;
		}

		// the regex will find all numbers, spaces and symbols "(", ")", "&", "|", "!"
		String targetExpressionRegex = "[&|()! ]|[\\d]+";

		// iterate through the tokens of target expression (we should replace targets IDs
		// with targetSQL; boolean operators - with SQL boolean operators)
		Pattern pattern = Pattern.compile(targetExpressionRegex);
		Matcher matcher = pattern.matcher(targetExpression);
		StringBuilder sqlExpression = new StringBuilder();

		while (matcher.find()) {
			String token = matcher.group();

			switch (token) {
				case "&":
					// replace boolean operators according to SQL syntax
					sqlExpression.append(" AND ");
					break;

				case "|":
					sqlExpression.append(" OR ");
					break;

				case "!":
					sqlExpression.append(" NOT ");
					break;

				default:
					// If token is targetId - replace it with target SQL-expression
					if (StringUtils.isNumeric(token)) {
						// Wrap target SQL expression with brackets (the unwrapped SQL can break the whole SQL expression).
						sqlExpression.append('(').append(resolver.resolve(Integer.parseInt(token))).append(')');
					} else {
						// If it's "(", ")" or space - leave as it is.
						sqlExpression.append(token);
					}
			}
		}

		return sqlExpression.toString();
	}

	@Override
	public String getTargetSQL(int targetId, int companyId) {
    	String sql = targetDao.getTargetSQL(targetId, companyId);

		if (StringUtils.isBlank(sql)) {
			return null;
		}

		return sql;
	}

	@Override
	public String getTargetSQL(int targetId, int companyId, boolean isPositive) {
		String sql = getTargetSQL(targetId, companyId);

		if (sql == null || isPositive) {
			return sql;
		} else {
			return " NOT " + sql;
		}
	}

	@Override
	public String getMailingSqlTargetExpression(int mailingId, int companyId, boolean appendListSplit) {
    	String expression = mailingDao.getTargetExpression(mailingId, companyId, appendListSplit);
    	return getSQLFromTargetExpression(expression, new TargetSqlCachingResolver(companyId));
	}

	@Override
	public ComTarget getTargetGroupOrNull(int targetId, int companyId) {
		try {
			return getTargetGroup(targetId, companyId);
		} catch (UnknownTargetGroupIdException e) {
			logger.warn(String.format("Could not find target group ID: %d CID: %d", targetId, companyId));
		}

		return null;
	}

	@Override
	public final ComTarget getTargetGroup(final int targetId, final int companyId) throws UnknownTargetGroupIdException {
        return getTargetGroup(targetId, companyId, 0);
    }

    @Override
	public final ComTarget getTargetGroup(final int targetId, final int companyId, final int adminId) throws UnknownTargetGroupIdException {
		if (logger.isInfoEnabled()) {
			logger.info(String.format("Retrieving target group ID %d for company ID %d", targetId, companyId));
		}

		final ComTarget result = this.targetDao.getTarget(targetId, companyId);

		if (result == null) {
			// Logged at INFO level, because this is not an internally caused error
			if (logger.isInfoEnabled()) {
				logger.info(String.format("Target ID %d not found for company ID %d", targetId, companyId));
			}

			throw new UnknownTargetGroupIdException(targetId);
		} else {
			if (logger.isInfoEnabled()) {
				logger.info(String.format("Found target group ID %d for company ID %d", targetId, companyId));
			}

			if (adminId > 0) {
			    result.setFavorite(isTargetFavoriteForAdmin(result, adminId));
            }

			return result;
		}
	}

    protected boolean isTargetFavoriteForAdmin(ComTarget target, int adminId) {
        return target.isFavorite(); // overridden in extended class
    }

	@Override
	public Optional<Integer> getNumberOfRecipients(final int targetId, final int companyId) {
		final String sql = targetDao.getTargetSQL(targetId, companyId);
		if(sql == null){
			return Optional.empty();
		}
		return Optional.of(recipientDao.sumOfRecipients(companyId, sql));
	}

	@Override
	public boolean lockTargetGroup(int companyId, int targetId) {
		if (targetId > 0) {
			targetDao.updateTargetLockState(targetId, companyId, true);
			return true;
		}
		return false;
	}

	@Override
	public boolean unlockTargetGroup(int companyId, int targetId) {
		if (targetId > 0) {
			targetDao.updateTargetLockState(targetId, companyId, false);
			return true;
		}
		return false;
	}

	/**
	 * Removes recipients affected in a target group.
	 */
	@Override
	public boolean deleteRecipients(int targetId, int companyId) {
		ComTarget target = targetDao.getTarget(targetId, companyId);
		return recipientDao.deleteRecipients(companyId, target.getTargetSQL());
	}

	@Override
	public String getTargetName(int targetId, int companyId) {
		return getTargetName(targetId, companyId, false);
	}

	@Override
	public String getTargetName(int targetId, int companyId, boolean includeDeleted) {
		return targetDao.getTargetName(targetId, companyId, includeDeleted);
	}

	@Override
	public boolean checkIfTargetNameAlreadyExists(int companyId, String targetName, int targetId) {
		String existingName = StringUtils.defaultString(targetDao.getTargetName(targetId, companyId, true));

		// Allow to keep existing name anyway.
		return !existingName.equals(targetName) && targetDao.isTargetNameInUse(companyId, targetName, false);
	}

	@Override
	public boolean checkIfTargetNameIsValid(String targetShortname) {
		return StringUtils.isNotBlank(targetShortname) &&
				!StringUtils.equalsIgnoreCase(targetShortname.trim(), "Alle Empfänger") &&
				!StringUtils.equalsIgnoreCase(targetShortname.trim(), "Blacklist");
	}

	@Override
	public boolean isWorkflowManagerListSplit(final int companyID, final int targetID) throws UnknownTargetGroupIdException {
		ComTarget target = this.getTargetGroup(targetID, companyID);
		return target.isWorkflowManagerListSplit();
	}

	@Override
	public List<TargetLight> getWsTargetLights(final int companyId) {
		return targetDao.getTargetLights(companyId);
	}

	@Override
	public List<TargetLight> getTargetLights(final Admin admin) {
		return getTargetLights(admin, true, true, false);
	}

	@Override
	public List<TargetLight> getTargetLights(int adminId, final int companyID, boolean includeDeleted, boolean worldDelivery, boolean adminTestDelivery, boolean content) {
		TargetLightsOptions options = TargetLightsOptions.builder()
                .setAdminId(adminId)
				.setCompanyId(companyID)
				.setWorldDelivery(worldDelivery)
				.setAdminTestDelivery(adminTestDelivery)
				.setIncludeDeleted(includeDeleted)
				.setContent(content)
				.build();

		return getTargetLights(options);
	}

	@Override
	public List<TargetLight> getTargetLights(final Admin admin, boolean worldDelivery, boolean adminTestDelivery, boolean content) {
		return getTargetLights(admin.getAdminID(), admin.getCompanyID(), false, worldDelivery, adminTestDelivery, content);
	}

	@Override
	public List<TargetLight> getTargetLights(final Admin admin,  boolean includeDeleted, boolean worldDelivery, boolean adminTestDelivery, boolean content) {
		return getTargetLights(admin.getAdminID(), admin.getCompanyID(), includeDeleted,worldDelivery, adminTestDelivery, content);
	}

	@Override
	public List<TargetLight> getTargetLights(TargetLightsOptions options) {
		try {
			return targetDao.getTargetLightsBySearchParameters(options);
		} catch (Exception e) {
			logger.error(String.format("Getting target light error: %s", e.getMessage()), e);
		}

		return new ArrayList<>();
	}

	@Override
	public PaginatedListImpl<TargetLight> getTargetLightsPaginated(TargetLightsOptions options) {
		try {
			return targetDao.getPaginatedTargetLightsBySearchParameters(options);
		} catch (Exception e) {
			logger.error(String.format("Getting target light error: %s", e.getMessage()), e);
		}
		return new PaginatedListImpl<>();
	}

	@Override
	public List<ListSplit> getListSplits(int companyId) {
		List<TargetLight> targets = targetDao.getSplitTargetLights(companyId, "");

		if (CollectionUtils.isNotEmpty(targets)) {
			return targets.stream()
					.map(TargetLight::toListSplit)
					.filter(Objects::nonNull)
					.sorted((s1, s2) -> {
						// Primary sorting criteria
						int d = s1.getParts().length - s2.getParts().length;
						if (d == 0) {
							// Secondary sorting criteria
							return s1.getPartIndex() - s2.getPartIndex();
						}
						return d;
					})
					.collect(Collectors.toList());
		}

		return Collections.emptyList();
	}

	@Override
	public int getTargetListSplitId(String splitBase, String splitPart, boolean isWmSplit) {
		if (StringUtils.isEmpty(splitBase) || StringUtils.isEmpty(splitPart)) {
            return Mailing.NONE_SPLIT_ID;
        }

        switch (StringUtils.lowerCase(splitBase)) {
            case Mailing.NONE_SPLIT:
                return Mailing.NONE_SPLIT_ID;
            case Mailing.YES_SPLIT:
                return Mailing.YES_SPLIT_ID;
            default:
            	//nothing
        }

		String prefix = isWmSplit ? TargetLight.LIST_SPLIT_CM_PREFIX : TargetLight.LIST_SPLIT_PREFIX;
        int targetId = targetDao.getTargetSplitID(prefix + splitBase + "_" + splitPart);
        return targetId > 0 ? targetId : NONE_SPLIT_ID;
	}

	@Override
	public String getTargetSplitName(int splitId) {
		return targetDao.getTargetSplitName(splitId);
	}

	private interface TargetSqlResolver {
		String resolve(int targetId);
	}

	private class TargetSqlCachingResolver implements TargetSqlResolver {
		private int companyId;
		private Map<Integer, String> cache = new HashMap<>();

		public TargetSqlCachingResolver(int companyId) {
			this.companyId = companyId;
		}

		@Override
		public String resolve(int targetId) {
			return cache.computeIfAbsent(targetId, id -> {
				String sql = targetDao.getTargetSQL(targetId, companyId);
				// If target not found - that means that it's a list-split target.
				if (sql == null) {
					return targetDao.getTargetSQL(targetId, 0);
				}
				return sql;
			});
		}
	}

	@Override
	public final List<TargetLight> listTargetGroupsUsingProfileFieldByDatabaseName(final String fieldNameOnDatabase, final int companyID) {
		try {
			final String visibleShortname = profileFieldService.translateDatabaseNameToVisibleName(companyID, fieldNameOnDatabase);
	
			final Set<TargetLight> set = new HashSet<>();
			set.addAll(referencedItemsService.listTargetGroupsReferencingProfileFieldByVisibleName(companyID, visibleShortname));
			set.addAll(listTargetGroupsUsingProfileFieldByDatabaseNameLegacy(visibleShortname, companyID));
	
			return new ArrayList<>(set);
		} catch (final ProfileFieldException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Cannot determine visible name for profile field", e);
			}
			return Collections.emptyList();
		}
	}

	@Deprecated // TODO Remove when reference data is present for all target groups
	private List<TargetLight> listTargetGroupsUsingProfileFieldByDatabaseNameLegacy(final String visibleShortname, final int companyID) {
		final List<ComTarget> list = targetDao.listRawTargetGroups(companyID, visibleShortname);

		return list.stream()
				.filter(t -> checkTargetGroupReferencesProfileField(t, visibleShortname, companyID))
				.collect(Collectors.toList());
	}

	@Override
	public List<String> getTargetNamesByIds(final int companyId, final Set<Integer> targetIds) {
		return targetDao.getTargetNamesByIds(companyId, targetIds);
	}

	/**
	 * Checks if given target group references given profile field.
	 *
	 * @param targetGroup target group to check
	 * @param fieldShortname visible name of profile field
	 * @param companyID company ID of target group
	 *
	 * @return <code>true</code> if target group references given profile field
	 */
	private boolean checkTargetGroupReferencesProfileField(final ComTarget targetGroup, final String fieldShortname, final int companyID) {
		try {
			final String eql = normalizeToEQL(targetGroup.getEQL());
			final SimpleReferenceCollector collector = new SimpleReferenceCollector();

			this.eqlFacade.convertEqlToSql(eql, companyID, collector);

			return collector.getReferencedProfileFields()
					.stream()
					.anyMatch(fieldShortname::equalsIgnoreCase);
		} catch(final EqlParserException |
                CodeGeneratorException |
                ProfileFieldResolveException |
                ReferenceTableResolveException e) {
			return false;	// Invalid target group
		}
    }

	/**
	 * Returns an EQL expression for given information.
	 *
	 * The conversion rule is:
	 * <ol>
	 *   <li>If a non-empty EQL expression is given, this expression is returned.</li>
	 *   <li>An empty EQL expression is returned as fallback.</li>
	 * </ol>
	 *
	 * @param eql EQL expression from target group
	 * @return EQL expression representing target group settings
	 */
	final String normalizeToEQL(final String eql) {
		if (eql != null) {
			return eql;
		} else {
			return "";
		}
	}

	@Override
	public final RecipientTargetGroupMatcher createRecipientTargetGroupMatcher(final int customerID, final int companyID) throws Exception {
		final Interpreter beanShellInterpreter = this.beanShellInterpreterFactory.createBeanShellInterpreter(companyID, customerID);

		return new BeanShellRecipientTargetGroupMatcher(companyID, beanShellInterpreter, this.eqlFacade);
	}

	@Override
	public final RecipientTargetGroupMatcher createRecipientTargetGroupMatcher(final Map<String, Object> recipientData, final int companyID) throws Exception {
		final Interpreter beanShellInterpreter = this.beanShellInterpreterFactory.createBeanShellInterpreter(companyID, recipientData, columnInfoService.getColumnInfoMap(companyID));

		return new BeanShellRecipientTargetGroupMatcher(companyID, beanShellInterpreter, this.eqlFacade);
	}

	@Override
	public List<TargetLight> getTargetLights(int companyId, Collection<Integer> targetGroups, boolean includeDeleted) {
		return targetDao.getTargetLights(companyId, targetGroups, includeDeleted);
	}

	@Override
	public List<TargetLight> getSplitTargetLights(int companyId, String splitType) {
		return targetDao.getSplitTargetLights(companyId, splitType);
	}

	@Override
	public PaginatedListImpl<Dependent<TargetGroupDependentType>> getDependents(int companyId, int targetId,
                                                                                Set<TargetGroupDependentType> allowedTypes, int pageNumber,
                                                                                int pageSize, String sortColumn, String order) {
		return targetDao.getDependents(companyId, targetId, allowedTypes, pageNumber, pageSize, sortColumn, order);
	}

	@Override
	public Map<Integer, TargetComplexityGrade> getTargetComplexities(int companyId) {
		Map<Integer, Integer> complexityIndicesMap = targetDao.getTargetComplexityIndices(companyId);
		Map<Integer, TargetComplexityGrade> complexityGradeMap = new HashMap<>(complexityIndicesMap.size());

		int recipientsCount = recipientDao.getNumberOfRecipients(companyId);

		complexityIndicesMap.forEach((targetId, complexityIndex) ->
			complexityGradeMap.put(targetId, TargetUtils.getComplexityGrade(complexityIndex, recipientsCount))
		);

		return complexityGradeMap;
	}

	@Override
	public TargetComplexityGrade getTargetComplexityGrade(int companyId, int targetId) {
		Integer complexityIndex = targetDao.getTargetComplexityIndex(companyId, targetId);

		if (complexityIndex == null || complexityIndex < 0) {
			return null;
		}

		return TargetUtils.getComplexityGrade(complexityIndex, recipientDao.getNumberOfRecipients(companyId));
	}

	@Override
	public int calculateComplexityIndex(String eql, int companyId) {
		return calculateComplexityIndex(eql, companyId, new TargetComplexityEvaluationCacheImpl());
	}

	@Override
	public int calculateComplexityIndex(String eql, int companyId, TargetComplexityEvaluationCache cache) {
		try {
			return complexityEvaluator.evaluate(eql, companyId, cache);
		} catch (Exception e) {
			logger.error("Target group complexity index evaluation failed", e);
			return 0;
		}
	}

	@Override
	public void initializeComplexityIndex(int companyId) {
		Map<Integer, Integer> complexities = new HashMap<>();
		TargetComplexityEvaluationCache cache = new TargetComplexityEvaluationCacheImpl();

		for (Pair<Integer, String> pair : targetDao.getTargetsToInitializeComplexityIndices(companyId)) {
			try {
				complexities.put(pair.getFirst(), calculateComplexityIndex(pair.getSecond(), companyId, cache));
			} catch (Exception e) {
				logger.error(String.format("Error occurred: %s", e.getMessage()), e);
			}
		}

		targetDao.saveComplexityIndices(companyId, complexities);
	}

	@Override
	public List<TargetLight> getAccessLimitationTargetLights(int companyId) {
		return Collections.emptyList();
	}

	@Override
	public List<TargetLight> getAccessLimitationTargetLights(int adminId, int companyId) {
		return Collections.emptyList();
	}
	
    @Override
    public List<TargetLight> getNoAccessLimitationTargetLights(int companyId) {
        TargetLightsOptions options = TargetLightsOptions.builder()
 				.setCompanyId(companyId)
 				.setWorldDelivery(true)
 				.setAdminTestDelivery(true)
 				.setAltgMode(AltgMode.NO_ALTG)
 				.build();
 		return getTargetLights(options);
    }
    
    @Override
    public List<TargetLight> extractAdminAltgsFromTargetLights(List<TargetLight> targets, Admin admin) {
        return Collections.emptyList();
    }
    
    @Override
    public List<TargetLight> filterTargetLightsByAltgMode(List<TargetLight> targets, AltgMode mode) {
	    switch (mode) {
            case ALTG_ONLY:
                return targets.stream().filter(TargetLight::isAccessLimitation).collect(Collectors.toList());
            case NO_ALTG:
                return targets.stream().filter(target -> !target.isAccessLimitation()).collect(Collectors.toList());
            default:
                return targets;
        }
    }

	@Override
	public boolean isBasicFullTextSearchSupported(){
		return targetDao.isBasicFullTextSearchSupported();
	}

	@Override
	public boolean isRecipientMatchTarget(Admin admin, int targetGroupId, int customerId) {
		try {
			String targetExpression = targetDao.getTargetSQL(targetGroupId, admin.getCompanyID());
			if (StringUtils.isEmpty(targetExpression)) {
				return true;
			}
			return recipientDao.isRecipientMatchTarget(admin.getCompanyID(), targetExpression, customerId);
		} catch (Exception e) {
			logger.error(String.format("Error occurs while checking if recipient match target group: %s", e.getMessage()), e);
		}

		return false;
	}

	@Override
	public void checkRecipientTargetGroupAccess(Admin admin, int customerId) throws RejectAccessByTargetGroupLimit {
        // nothing to do
    }
    
    @Override
    public boolean isAltg(int targetId) {
        return targetDao.isAltg(targetId);
	}

	@Override
    public boolean isHidden(int targetId, int companyId) {
        return targetDao.isHidden(targetId, companyId);
	}

    @Override
    public Set<Integer> getAltgIdsWithoutAdminAltgIds(int companyId, Set<Integer> adminAltgIds) {
        Collection<Integer> altgIds = CollectionUtils.emptyIfNull(adminAltgIds);
        return CollectionUtils.emptyIfNull(getAccessLimitationTargetLights(companyId)).stream()
                .map(TargetLight::getId)
                .filter(id -> !altgIds.contains(id))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(final int companyId, final int targetId) {
        return targetDao.isValid(companyId, targetId);
    }

    @Override
    public boolean isLocked(final int companyId, final int targetId) {
        return targetDao.isTargetGroupLocked(targetId, companyId);
    }
    
    @Override
    public void addToFavorites(final int targetId, final int companyId) {
        targetDao.addToFavorites(targetId, companyId);
    }

    @Override
    public void removeFromFavorites(final int targetId, final int companyId) {
        targetDao.removeFromFavorites(targetId, companyId);
    }

    @Override
    public void markAsFavorite(int targetId, int adminId, int companyId) {
        targetDao.markAsFavorite(targetId, adminId, companyId);
    }

    @Override
    public void unmarkAsFavorite(final int targetId, int adminId, final int companyId) {
        targetDao.unmarkAsFavorite(targetId, adminId, companyId);
    }

	@Override
	public int saveTargetFromRecipientSearch(Admin admin, RecipientSaveTargetDto targetDto, List<Message> errors, List<UserAction> userActions) {
		int targetId = 0;
		try {
			final ComTarget oldTarget = getTargetGroupOrNull(targetDto.getTargetId(), admin.getCompanyID());

			final ComTarget newTarget = targetFactory.newTarget();
			newTarget.setId(0);
			newTarget.setTargetName(targetDto.getShortname());
			newTarget.setTargetDescription(targetDto.getDescription());

			newTarget.setEQL(queryBuilderToEqlConverter.convertQueryBuilderJsonToEql(targetDto.getQueryBuilderRules(), admin.getCompanyID()));
			newTarget.setCompanyID(admin.getCompanyID());

			targetId = saveTarget(admin, newTarget, oldTarget, errors, userActions);
		} catch (QueryBuilderToEqlConversionException e) {
			logger.error("Could not convert query builder rule.", e);
		} catch (Exception e) {
			logger.error("Could not save target group.");
		}

		return targetId;
	}

	@Override
	public boolean isEqlContainsInvisibleFields(String eql, int companyId, int adminId) {
		try {
			Set<String> referencedProfileFields = eqlReferenceItemsExtractor.collectReferences(eql)
					.getReferencedProfileFields();

			return referencedProfileFields.stream()
					.map(pf -> profileFieldService.getProfileFieldByShortname(companyId, pf, adminId))
					.filter(Objects::nonNull)
					.anyMatch(pf -> ProfileFieldMode.NotVisible.equals(pf.getModeEdit()));
		} catch (EqlParserException e) {
			logger.error("Could not retrieve profile fields from EQL.", e);
			return false;
		}
	}

	private int getMaxGenderValue(Admin admin) {
		if (admin.permissionAllowed(Permission.RECIPIENT_GENDER_EXTENDED)) {
			return ConfigService.MAX_GENDER_VALUE_EXTENDED;
		} else {
			return ConfigService.MAX_GENDER_VALUE_BASIC;
		}
	}

	private boolean validateTargetDefinition(int companyId, String eql) {
		try {
			eqlFacade.convertEqlToSql(eql, companyId);
			return true;
		} catch (Exception e) {
			logger.error(String.format("Error occurred: %s", e.getMessage()), e);
			return false;
		}
	}

	private List<UserAction> getSavingLogs(ComTarget newTarget, ComTarget oldTarget) {
		List<UserAction> userActions;
		try {
			if (oldTarget == null) {
				userActions = getCreatingLogs(newTarget);
			} else {
				userActions = getEditingLogs(newTarget, oldTarget);
			}

			if (logger.isInfoEnabled()) {
				logger.info(String.format("saveTarget: save target %d", newTarget.getId()));
			}
		} catch (Exception e) {
			if (logger.isInfoEnabled()) {
				logger.error(String.format("Log Target Group changes error: %s", e));
			}
			userActions = Collections.emptyList();
		}

		return userActions;
	}

	private List<UserAction> getCreatingLogs(ComTarget newTarget) {
		final String CREATE_ACTION = "create target group";
		final List<UserAction> userActions = new LinkedList<>();
		final String descriptionPrefix = "Created new target group (id: %d) ";

		if(StringUtils.isNotBlank(newTarget.getEQL())) {
			String eqlDesc = String.format(descriptionPrefix + "with rule: \"%s\"", newTarget.getId(), newTarget.getEQL());
			userActions.add(new UserAction(CREATE_ACTION, eqlDesc));
		}

		String state = newTarget.isAdminTestDelivery() ? "checked" : "unchecked";
		String adminTestDelDesc = String.format(descriptionPrefix + "with admin- and test-delivery state: \"%s\"", newTarget.getId(), state);
		userActions.add(new UserAction(CREATE_ACTION, adminTestDelDesc));

		if(StringUtils.isNotBlank(newTarget.getTargetDescription())) {
			String descriptionDesc = String.format( descriptionPrefix + "with description: \"%s\"", newTarget.getId(), newTarget.getTargetDescription());
			userActions.add(new UserAction(CREATE_ACTION, descriptionDesc));
		}

		String nameDesc = String.format(descriptionPrefix + "with name: \"%s\"", newTarget.getId(), newTarget.getTargetName());
		userActions.add(new UserAction(CREATE_ACTION, nameDesc));

		return userActions;
	}

	private List<UserAction> getEditingLogs(ComTarget newTarget, ComTarget oldTarget) {
		final String EDIT_ACTION = "edit target group";
		final List<UserAction> userActions = new LinkedList<>();

		// Log target group changes:
		// log rule(s) changes
		if (isEqlModified(newTarget, oldTarget)) {
			final String desc = String.format("Changed rule in target group (id: %d), old rule: \"%s\"; new rule: \"%s\".",
					newTarget.getId(),
					oldTarget.getEQL(),
					newTarget.getEQL());

			userActions.add(new UserAction(EDIT_ACTION, desc));
		}

		// Log if "For admin- and test-delivery" checkbox changed
		if (oldTarget.isAdminTestDelivery() != newTarget.isAdminTestDelivery()) {
			final String newState = (newTarget.isAdminTestDelivery() ? "checked" : "unchecked");
			final String oldState = (newTarget.isAdminTestDelivery() ? "unchecked" : "checked");
			final String desc = String.format("Changed state for toggle admin- and test-delivery for target group (id:%d) from: \"%s\" to \"%s\"",
					newTarget.getId(),
					oldState,
					newState);
			userActions.add(new UserAction(EDIT_ACTION, desc));
		}

		final String oldDescription = StringUtils.trimToNull(oldTarget.getTargetDescription());
		final String newDescription = StringUtils.trimToNull(newTarget.getTargetDescription());

		// Log description changes
		if (!StringUtils.equals(oldDescription, newDescription)) {
			if (oldDescription == null) {
				final String desc = String.format("Added description for target group (id: %d): \"%s\"",
						newTarget.getId(),
						newDescription);
				userActions.add(new UserAction(EDIT_ACTION, desc));
			} else if (newDescription == null) {
				final String desc = String.format("Description for target group (id: %d) has been removed. Old description: \"%s\"",
						newTarget.getId(),
						oldDescription);
				userActions.add(new UserAction(EDIT_ACTION, desc));
			} else {
				final String desc = String.format("Changed description for target group (id: %d) from: \"%s\" to: \"%s\"",
						newTarget.getId(),
						oldDescription,
						newDescription);
				userActions.add(new UserAction(EDIT_ACTION, desc));
			}
		}

		// Log name changes
		if (!oldTarget.getTargetName().equals(newTarget.getTargetName())) {
			final String desc = String.format("Target group (id: %d) renamed from: \"%s\" to: \"%s\"",
					newTarget.getId(),
					oldTarget.getTargetName(),
					newTarget.getTargetName());
			userActions.add(new UserAction(EDIT_ACTION, desc));
		}

		return userActions;
	}

	/**
	 * Checks if target group rules differ by comparing EQL code.
	 *
	 * @param newTarget new target group
	 * @param oldTarget target group from DB
	 *
	 * @return <code>true</code> if target groups differ
	 */
	private static boolean isEqlModified(final ComTarget newTarget, final ComTarget oldTarget) {
		return !Objects.equals(newTarget.getEQL(), oldTarget.getEQL());
	}

	private List<String> postValidation(ComTarget target) {
		if (target.getTargetSQL().equals("1=0")) {
			return Collections.singletonList("error.target.definition");
		}

		return Collections.emptyList();
	}

	private List<ActionMessage> postValidation(Admin admin, ComTarget target) {
		// Check for maximum "compare to"-value of gender equations
		// Must be done after saveTarget(..)-call because there the new target sql expression is generated
		if (target.getTargetSQL().contains("cust.gender")) {
			final int maxGenderValue = getMaxGenderValue(admin);

			Matcher matcher = GENDER_EQUATION_PATTERN.matcher(target.getTargetSQL());
			while (matcher.find()) {
				int genderValue = NumberUtils.toInt(matcher.group(1));
				if (genderValue < 0 || genderValue > maxGenderValue) {
					return Collections.singletonList(new ActionMessage("error.gender.invalid", maxGenderValue));
				}
			}
		} else if (target.getTargetSQL().equals("1=0")) {
			return Collections.singletonList(new ActionMessage("error.target.definition"));
		}

		return Collections.emptyList();
	}
	
	@Override
	public List<TargetLight> getTargetLights(final int companyId, boolean includeDeleted) {
		return targetDao.getTargetLights(companyId, includeDeleted);
	}

	@Override
	public void deleteWorkflowTargetConditions(int companyID) {
		targetDao.deleteWorkflowTargetConditions(companyID);
	}

	@Override
	public int getAccessLimitingTargetgroupsAmount(int companyId) {
		return 0;
	}
	
	@Override
	public boolean isLinkUsedInTarget(TrackableLink link) {
	    return targetDao.isLinkUsedInTarget(link);
    }

    // region Dependency Injection

	@Required
	public void setTargetDao(ComTargetDao targetDao) {
		this.targetDao = targetDao;
	}

	@Required
	public void setMailingComponentDao( MailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
	}

	@Required
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Required
	public void setTargetGroupDependencyService(TargetGroupDependencyService targetGroupDependencyService) {
		this.targetGroupDependencyService = targetGroupDependencyService;
	}

	@Required
	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}

	@Required
	public void setEqlFacade(EqlFacade eqlFacade) {
		this.eqlFacade = eqlFacade;
	}

	@Required
	public void setProfileFieldService(ProfileFieldService service) {
		this.profileFieldService = Objects.requireNonNull(service, "Profile field service cannot be null");
	}

	@Required
	public void setBeanShellInterpreterFactory(BeanShellInterpreterFactory factory) {
		this.beanShellInterpreterFactory = Objects.requireNonNull(factory, "BeanShellInterpreterFactory is null");
	}

	@Required
	public void setTargetComplexityEvaluator(TargetComplexityEvaluator complexityEvaluator) {
		this.complexityEvaluator = complexityEvaluator;
	}

	@Required
	public void setEqlReferenceItemsExtractor(EqlReferenceItemsExtractor eqlReferenceItemsExtractor) {
		this.eqlReferenceItemsExtractor = eqlReferenceItemsExtractor;
	}

	@Required
	public void setColumnInfoService(ColumnInfoService columnInfoService) {
		this.columnInfoService = columnInfoService;
	}

	@Required
	public void setReferencedItemsService(ReferencedItemsService service) {
		this.referencedItemsService = Objects.requireNonNull(service, "ReferencedItemsService is null");
	}

	@Required
	public void setTargetFactory(TargetFactory targetFactory) {
		this.targetFactory = targetFactory;
	}

	@Required
	public void setQueryBuilderToEqlConverter(QueryBuilderToEqlConverter queryBuilderToEqlConverter) {
		this.queryBuilderToEqlConverter = queryBuilderToEqlConverter;
	}

	@Required
	public void setEqlValidatorService(EqlValidatorService eqlValidatorService) {
		this.eqlValidatorService = eqlValidatorService;
	}

	// endregion
}
