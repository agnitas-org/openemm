/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.service.impl;

import static com.agnitas.beans.Mailing.NONE_SPLIT_ID;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import bsh.Interpreter;
import com.agnitas.beans.Admin;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.beans.Target;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.TrackableLink;
import com.agnitas.beans.factory.TargetFactory;
import com.agnitas.dao.MailingComponentDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.RecipientDao;
import com.agnitas.dao.TargetDao;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.commons.dto.IntRange;
import com.agnitas.emm.core.mailing.forms.SplitSettings;
import com.agnitas.emm.core.mailingcontent.dto.ContentBlockAndMailingMetaData;
import com.agnitas.emm.core.profilefields.service.ProfileFieldService;
import com.agnitas.emm.core.recipient.dto.RecipientSaveTargetDto;
import com.agnitas.emm.core.recipient.web.RejectAccessByTargetGroupLimit;
import com.agnitas.emm.core.target.AltgMode;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.TargetUtils;
import com.agnitas.emm.core.target.beans.TargetComplexityGrade;
import com.agnitas.emm.core.target.beans.TargetGroupDeliveryOption;
import com.agnitas.emm.core.target.beans.TargetGroupDependentEntry;
import com.agnitas.emm.core.target.beans.TargetGroupDependentType;
import com.agnitas.emm.core.target.complexity.bean.TargetComplexityEvaluationCache;
import com.agnitas.emm.core.target.complexity.bean.impl.TargetComplexityEvaluationCacheImpl;
import com.agnitas.emm.core.target.complexity.service.TargetComplexityEvaluator;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.eql.EqlValidatorService;
import com.agnitas.emm.core.target.eql.codegen.CodeGeneratorException;
import com.agnitas.emm.core.target.eql.codegen.resolver.ProfileFieldResolveException;
import com.agnitas.emm.core.target.eql.codegen.resolver.ReferenceTableResolveException;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCode;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlReferenceItemsExtractor;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConversionException;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConverter;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;
import com.agnitas.emm.core.target.eql.referencecollector.SimpleReferenceCollector;
import com.agnitas.emm.core.target.exception.EqlFormatException;
import com.agnitas.emm.core.target.exception.TargetGroupLockedException;
import com.agnitas.emm.core.target.exception.TargetGroupNotCompatibleWithContentBlockException;
import com.agnitas.emm.core.target.exception.TargetGroupNotFoundException;
import com.agnitas.emm.core.target.exception.TargetGroupPersistenceException;
import com.agnitas.emm.core.target.service.RecipientTargetGroupMatcher;
import com.agnitas.emm.core.target.service.ReferencedItemsService;
import com.agnitas.emm.core.target.service.TargetGroupDependencyService;
import com.agnitas.emm.core.target.service.TargetLightsOptions;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.target.service.UserActivityLog;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import com.agnitas.service.MailingContentService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.beanshell.BeanShellInterpreterFactory;
import com.agnitas.web.forms.PaginationForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;

/**
 * Implementation of {@link TargetService} interface.
 */
public class TargetServiceImpl implements TargetService {

	private static final Logger logger = LogManager.getLogger(TargetServiceImpl.class);

	/** DAO accessing target groups. */
    protected TargetDao targetDao;

    /** DAO accessing mailing components. */
    private MailingComponentDao mailingComponentDao;

    /** DAO for accessing mailing data. */
    private MailingDao mailingDao;

	/** DAO for accessing recipient data. */
	protected RecipientDao recipientDao;

	/** Facade for EQL logic. */
	private EqlFacade eqlFacade;

	/** Service dealing with profile fields. */
	private ProfileFieldService profileFieldService;

	private TargetGroupDependencyService targetGroupDependencyService;
	private TargetComplexityEvaluator complexityEvaluator;
	private EqlReferenceItemsExtractor eqlReferenceItemsExtractor;
	private BeanShellInterpreterFactory beanShellInterpreterFactory;
	private ReferencedItemsService referencedItemsService;
	private TargetFactory targetFactory;
	private QueryBuilderToEqlConverter queryBuilderToEqlConverter;
    private EqlValidatorService eqlValidatorService;
    private MailingContentService mailingContentService;
	private BulkActionValidationService<Integer, String> bulkActionValidationService;

    @Override
	public SimpleServiceResult deleteTargetGroup(int targetGroupID, Admin admin) {
    	int companyID = admin.getCompanyID();

		if (logger.isInfoEnabled()) {
			logger.info("Deleting target group {} of company {}", targetGroupID, companyID);
		}

		Optional<TargetGroupDependentEntry> dependency = targetGroupDependencyService.findAnyActualDependency(targetGroupID, companyID);

		if (dependency.isPresent()) {
			String targetName = getTargetName(targetGroupID, companyID);
			Message errorMessage = targetGroupDependencyService.buildErrorMessage(dependency.get(), targetName);

			return SimpleServiceResult.simpleError(errorMessage);
		}

		referencedItemsService.removeReferencedItems(companyID, targetGroupID);

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

		Optional<TargetGroupDependentEntry> dependency = targetGroupDependencyService.findAnyActualDependency(targetId, companyId);

		if (dependency.isPresent()) {
			String targetName = getTargetName(targetId, companyId);
			Message errorMessage = targetGroupDependencyService.buildErrorMessage(dependency.get(), targetName);

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
    		Target target = targetDao.getTarget(targetId, mailing.getCompanyID());

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
    public int saveTarget(Admin admin, Target newTarget, Target target, List<Message> errors, List<UserAction> userActions) throws TargetGroupPersistenceException {
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
    public int saveTarget(Admin admin, Target newTarget, Target target, List<Message> errors, UserActivityLog userActivityLog) throws Exception {
        List<UserAction> userActions = new LinkedList<>();
        int newTargetId = saveTarget(admin, newTarget, target, errors, userActions);
        userActions.forEach(userAction -> userActivityLog.write(admin, userAction.getAction(), userAction.getDescription()));
        return newTargetId;
    }

	@Override
	public int saveTarget(Target target) throws TargetGroupPersistenceException {
		try {
			final SimpleReferenceCollector referencedItemsCollector = new SimpleReferenceCollector();
			final SqlCode sqlCode = eqlFacade.convertEqlToSql(target.getEQL(), target.getCompanyID(), referencedItemsCollector);
			
			if(target.getId() != 0 && !TargetUtils.canBeUsedInContentBlocks(sqlCode)) {
				// Check, if target group is used by content blocks

				final List<ContentBlockAndMailingMetaData> contentBlocks = this.mailingContentService.listContentBlocksUsingTargetGroup(target);
				if(!contentBlocks.isEmpty()) {
					throw new TargetGroupNotCompatibleWithContentBlockException(contentBlocks);
				}
				
			}
			
			final int targetID = targetDao.saveTarget(target);
			
			referencedItemsService.saveReferencedItems(referencedItemsCollector, target.getCompanyID(), targetID);
			
			return targetID;
		} catch (EqlParserException | CodeGeneratorException | ProfileFieldResolveException | ReferenceTableResolveException e) {
			logger.error("Error parsing EQL", e);
			
			throw new EqlFormatException("Error parsing EQL representation.", e);
		}
	}

	@Override
	public List<Integer> bulkDelete(Set<Integer> ids, Admin admin) {
		List<Integer> deletedTargets = new ArrayList<>();

		for (int targetId : ids) {
			SimpleServiceResult deletionResult = deleteTargetGroup(targetId, admin);

			if (deletionResult.isSuccess()) {
				deletedTargets.add(targetId);
			}
		}

		return deletedTargets;
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
	public Target getTargetGroupOrNull(int targetId, int companyId) {
		try {
			return getTargetGroup(targetId, companyId);
		} catch (TargetGroupNotFoundException e) {
			logger.warn("Could not find target group ID: {} CID: {}", targetId, companyId);
			return null;
		}
	}

	@Override
	public Target getTargetGroupOrNull(int targetId, int companyId, int adminId) {
		try {
			return getTargetGroup(targetId, companyId, adminId);
		} catch (TargetGroupNotFoundException e) {
			logger.warn("Could not find target group ID: {} CID: {} adminID: {}", targetId, companyId, adminId);
			return null;
		}
	}

	@Override
	public Target getTargetGroup(int targetId, int companyId) {
        return getTargetGroup(targetId, companyId, 0);
    }

    @Override
	public Target getTargetGroup(int targetId, int companyId, int adminId) {
		logger.info("Retrieving target group ID {} for company ID {}", targetId, companyId);

		final Target result = this.targetDao.getTarget(targetId, companyId);

		if (result == null) {
			logger.info("Target ID {} not found for company ID {}", targetId, companyId);
			throw new TargetGroupNotFoundException(targetId);
		}

		logger.info("Found target group ID {} for company ID {}", targetId, companyId);

		if (adminId > 0) {
			result.setFavorite(isTargetFavoriteForAdmin(result, adminId));
		}

		return result;
	}

    protected boolean isTargetFavoriteForAdmin(Target target, int adminId) {
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
		Target target = targetDao.getTarget(targetId, companyId);
		return recipientDao.deleteRecipients(companyId, target.getTargetSQL());
	}

	@Override
	public ServiceResult<List<String>> getTargetNamesForDeletion(List<Integer> ids, Admin admin) {
		return bulkActionValidationService.checkAllowedForDeletion(ids, id -> {
			SimpleServiceResult result = canBeDeleted(id, admin);
			if (result.isSuccess()) {
				return ServiceResult.success(getTargetName(id, admin.getCompanyID()));
			}

			return ServiceResult.from(result);
		});
	}

	@Override
	public String getTargetName(int targetId, int companyId, Locale locale) {
		return targetId == 0
			? I18nString.getLocaleString(CommonKeys.ALL_SUBSCRIBERS, locale)
			: getTargetName(targetId, companyId, false);
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
	public List<TargetLight> getWsTargetLights(final int companyId) {
		return targetDao.getTargetLights(companyId);
	}

	@Override
	public List<TargetLight> getTargetLights(final Admin admin) {
		return getTargetLights(admin, false, null);
	}
	
	@Override
	public List<TargetLight> listTargetLightsForMailingSettings(final Admin admin, final Mailing mailing) {
		TargetLightsOptions options = TargetLightsOptions.builder()
                .setAdminId(admin.getAdminID())
				.setCompanyId(admin.getCompanyID())
				.setContent(false)
				.build();
		
		final List<TargetLight> targets = getTargetLights(options);
		
		final Collection<Integer> targetIdsInMailing = mailing.getTargetGroups() != null
				? mailing.getTargetGroups()
				: List.of();

		/*
		 * Return all valid target groups and invalid target groups already added to mailing.
		 */
		return targets.stream()
				.filter(target -> target.isValid() || targetIdsInMailing.contains(target.getId()))
				.collect(Collectors.toList());
	}

	@Override
	public Set<Integer> getInvalidTargets(int companyId, Set<Integer> targets) {
		return targetDao.getInvalidTargets(companyId, targets);
	}

	@Override
	public void restore(Set<Integer> ids, Admin admin) {
		targetDao.restore(ids, admin.getCompanyID());
	}

	@Override
	public List<TargetLight> getTargetLights(final Admin admin, boolean content, TargetGroupDeliveryOption delivery) {
		TargetLightsOptions options = TargetLightsOptions.builder()
			.setAdminId(admin.getAdminID())
			.setCompanyId(admin.getCompanyID())
			.setDeliveryOption(delivery)
			.setContent(content)
			.build();
		return getTargetLights(options);
	}

	protected List<TargetLight> getTargetLights(TargetLightsOptions options) {
		try {
			return targetDao.getTargetLightsBySearchParameters(options);
		} catch (Exception e) {
			logger.error(String.format("Getting target light error: %s", e.getMessage()), e);
		}

		return new ArrayList<>();
	}

	@Override
	public PaginatedList<TargetLight> getTargetLightsPaginated(TargetLightsOptions options, TargetComplexityGrade complexity) {
		try {
			int numberOfRecipients = recipientDao.getNumberOfRecipients(options.getCompanyId());
			if (numberOfRecipients < TargetUtils.MIN_RECIPIENT_COUNT_CONDITION_THRESHOLD
					&& (TargetComplexityGrade.YELLOW.equals(complexity) || TargetComplexityGrade.RED.equals(complexity))) {
				return new PaginatedList<>(
						Collections.emptyList(),
						0,
						options.getPageSize(),
						1,
						options.getSorting(),
						options.getDirection()
				);
			}

			if (complexity != null) {
				if (numberOfRecipients < TargetUtils.MIN_RECIPIENT_COUNT_CONDITION_THRESHOLD) {
					options.setComplexity(new IntRange(null, Integer.MAX_VALUE));
				} else {
					options.setComplexity(TargetUtils.getComplexityIndexesRange(complexity));
				}
			}

			options.setRecipientCountBasedComplexityAdjustment(TargetUtils.getComplexityAdjustment(numberOfRecipients));
			return targetDao.getPaginatedTargetLightsBySearchParameters(options);
		} catch (Exception e) {
			logger.error(String.format("Getting target light error: %s", e.getMessage()), e);
		}
		return new PaginatedList<>();
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

	private String getTargetSplitName(int splitId) {
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
	public List<TargetLight> listTargetGroupsUsingProfileFieldByDatabaseName(String fieldNameOnDatabase, int companyID) {
		final String visibleShortname = profileFieldService.translateDatabaseNameToVisibleName(companyID, fieldNameOnDatabase);

		final Set<TargetLight> set = new HashSet<>();
		set.addAll(referencedItemsService.listTargetGroupsReferencingProfileFieldByVisibleName(companyID, visibleShortname));
		set.addAll(listTargetGroupsUsingProfileFieldByDatabaseNameLegacy(visibleShortname, companyID));

		return new ArrayList<>(set);
	}

	@Deprecated // TODO Remove when reference data is present for all target groups
	private List<TargetLight> listTargetGroupsUsingProfileFieldByDatabaseNameLegacy(final String visibleShortname, final int companyID) {
		final List<Target> list = targetDao.listRawTargetGroups(companyID, visibleShortname);

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
	private boolean checkTargetGroupReferencesProfileField(final Target targetGroup, final String fieldShortname, final int companyID) {
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
	public List<TargetLight> getTargetLights(int companyId, Collection<Integer> targetGroups, boolean includeDeleted) {
		return targetDao.getTargetLights(companyId, targetGroups, includeDeleted);
	}

	@Override
	public List<TargetLight> getSplitTargetLights(int companyId, String splitType) {
		return targetDao.getSplitTargetLights(companyId, splitType);
	}

	@Override
	public PaginatedList<Dependent<TargetGroupDependentType>> getDependents(int targetId, int companyId, PaginationForm filter) {
		return targetDao.getDependents(companyId, targetId, emptySet(),
			filter.getPage(), filter.getNumberOfRows(), filter.getSort(), filter.getOrder());
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

	private int calculateComplexityIndex(String eql, int companyId, TargetComplexityEvaluationCache cache) {
		try {
			return complexityEvaluator.evaluate(eql, companyId, cache);
		} catch (Exception e) {
			logger.error("Target group complexity index evaluation failed", e);
			return 0;
		}
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
    public List<TargetLight> extractAdminAltgsFromTargetLights(List<TargetLight> targets, Admin admin) {
        return Collections.emptyList();
    }

	@Override
	public List<TargetLight> getAdminAltgs(Admin admin) {
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
	public void removeMarkedAsDeletedBefore(Date date, int companyID) {
		List<Integer> ids = targetDao.getMarkedAsDeletedBefore(date, companyID)
				.stream()
				.filter(id -> !targetGroupDependencyService.exists(id, companyID))
				.collect(Collectors.toList());

		if (!ids.isEmpty()) {
			targetDao.deleteTargetsReally(ids);
		}
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
			final Target oldTarget = getTargetGroupOrNull(targetDto.getTargetId(), admin.getCompanyID());

			final Target newTarget = targetFactory.newTarget();
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
	public boolean exist(int targetId, int companyId) {
		return targetDao.exist(targetId, companyId);
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
			logger.error("Could not retrieve profile fields from EQL: '%s'".formatted(eql), e);
			return false;
		}
	}

	private List<UserAction> getSavingLogs(Target newTarget, Target oldTarget) {
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

	private List<UserAction> getCreatingLogs(Target newTarget) {
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

	private List<UserAction> getEditingLogs(Target newTarget, Target oldTarget) {
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
	private static boolean isEqlModified(final Target newTarget, final Target oldTarget) {
		return !Objects.equals(newTarget.getEQL(), oldTarget.getEQL());
	}

	private List<String> postValidation(Target target) {
		if (target.getTargetSQL().equals("1=0")) {
			return Collections.singletonList("error.target.definition");
		}

		return Collections.emptyList();
	}

	@Override
	public int getTargetListSplitIdForSave(int splitId, String splitBase, String splitPart) {
		int id = getTargetListSplitId(splitBase, splitPart, isWmSplit(splitId));
		if (id == Mailing.YES_SPLIT_ID) { //-1 Should not be saved to DB
			id = Mailing.NONE_SPLIT_ID;
		}
		return id;
	}

	private boolean isWmSplit(int splitId) {
		if (splitId > 0) {
			String name = getTargetSplitName(splitId);
			return isNotEmpty(name)
				   && name.startsWith(TargetLight.LIST_SPLIT_CM_PREFIX);
		}
		return false;
	}

	@Override
	public void setSplitSettings(SplitSettings split, int splitId, boolean preserveCmListSplit) {
		if (splitId > 0) {
			String name = getTargetSplitName(splitId);

			if (isNotEmpty(name)) {
				if (name.startsWith(TargetLight.LIST_SPLIT_CM_PREFIX)) {
					if (preserveCmListSplit) {
						split.setSplitBase(name.substring(TargetLight.LIST_SPLIT_CM_PREFIX.length(), name.lastIndexOf('_')));
						split.setSplitPart(name.substring(name.lastIndexOf("_") + 1));
						return;
					}
				} else {
					split.setSplitBase(name.substring(12, name.indexOf('_', 13)));
					split.setSplitPart(name.substring(name.indexOf('_', 13) + 1));
					return;
				}
			}
		}
		split.setSplitBase(splitId == Mailing.YES_SPLIT_ID ? Mailing.YES_SPLIT : Mailing.NONE_SPLIT);
		split.setSplitPart("1");
	}

	@Override
	public void addSplitTargetModelAttrs(Model model, int companyId, int splitId, String splitBase, String splitPart) {
		model.addAttribute("splitId", splitId);
		model.addAttribute("splitTargets", getSplitTargetLights(companyId, "").stream().limit(500).toList());
		model.addAttribute("splitTargetsForSplitBase", getSplitTargetLights(companyId, splitBase).stream().limit(500).toList());
		if (splitId > 0) {
			String name = getTargetSplitName(splitId);
			if (isNotEmpty(name) && name.startsWith(TargetLight.LIST_SPLIT_CM_PREFIX)) {
				String[] parts = splitBase.split(";");
				StringBuilder splitBaseMessage = new StringBuilder();
				for (int i = 1; i <= parts.length; i++) {
					String part = parts[i - 1];
					splitBaseMessage.append(part).append("% / ");
					if (i == Integer.parseInt(splitPart)) {
						model.addAttribute("splitPartMessage", i + ". " + part + "%");
					}
				}
				model.addAttribute("splitBaseMessage", splitBaseMessage.substring(0, splitBaseMessage.length() - 2));
				model.addAttribute("wmSplit", true);
			}
		}
	}

	@Override
	public List<TargetLight> getTargetLights(final int companyId) {
		return targetDao.getTargetLights(companyId);
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

	public void setTargetDao(TargetDao targetDao) {
		this.targetDao = targetDao;
	}

	public void setMailingComponentDao( MailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
	}

	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	public void setTargetGroupDependencyService(TargetGroupDependencyService targetGroupDependencyService) {
		this.targetGroupDependencyService = targetGroupDependencyService;
	}

	public void setRecipientDao(RecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}

	public void setEqlFacade(EqlFacade eqlFacade) {
		this.eqlFacade = eqlFacade;
	}

	public void setProfileFieldService(ProfileFieldService service) {
		this.profileFieldService = Objects.requireNonNull(service, "Profile field service cannot be null");
	}

	public void setBeanShellInterpreterFactory(BeanShellInterpreterFactory factory) {
		this.beanShellInterpreterFactory = Objects.requireNonNull(factory, "BeanShellInterpreterFactory is null");
	}

	public void setTargetComplexityEvaluator(TargetComplexityEvaluator complexityEvaluator) {
		this.complexityEvaluator = complexityEvaluator;
	}

	public void setEqlReferenceItemsExtractor(EqlReferenceItemsExtractor eqlReferenceItemsExtractor) {
		this.eqlReferenceItemsExtractor = eqlReferenceItemsExtractor;
	}

	public void setReferencedItemsService(ReferencedItemsService service) {
		this.referencedItemsService = Objects.requireNonNull(service, "ReferencedItemsService is null");
	}

	public void setBulkActionValidationService(BulkActionValidationService<Integer, String> bulkActionValidationService) {
		this.bulkActionValidationService = bulkActionValidationService;
	}

	public void setTargetFactory(TargetFactory targetFactory) {
		this.targetFactory = targetFactory;
	}

	public void setQueryBuilderToEqlConverter(QueryBuilderToEqlConverter queryBuilderToEqlConverter) {
		this.queryBuilderToEqlConverter = queryBuilderToEqlConverter;
	}

	public void setEqlValidatorService(EqlValidatorService eqlValidatorService) {
		this.eqlValidatorService = eqlValidatorService;
	}
	
	public final void setMailingContentService(final MailingContentService service) {
		this.mailingContentService = Objects.requireNonNull(service, "mailing content service");
	}

	// endregion
}
