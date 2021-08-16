/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.web;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.exception.target.TargetGroupTooLargeException;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.target.exception.UnknownTargetGroupIdException;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.service.WebStorage;
import org.agnitas.target.TargetFactory;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.SafeString;
import org.agnitas.web.DispatchBaseAction;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTarget;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatusStatisticDto;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.TargetUtils;
import com.agnitas.emm.core.target.beans.TargetComplexityGrade;
import com.agnitas.emm.core.target.beans.TargetGroupDependentType;
import com.agnitas.emm.core.target.eql.EqlAnalysisResult;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.eql.parser.EqlSyntaxError;
import com.agnitas.emm.core.target.eql.parser.EqlSyntaxErrorException;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.target.service.TargetCopyService;
import com.agnitas.emm.core.target.service.TargetSavingAndAnalysisResult;
import com.agnitas.emm.core.target.web.util.EditorContentSynchronizationException;
import com.agnitas.emm.core.target.web.util.EditorContentSynchronizer;
import com.agnitas.emm.core.target.web.util.FormHelper;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.messages.I18nString;
import com.agnitas.service.GridServiceWrapper;

/**
 * Action handling the target group query builder editor view.
 */
public class QueryBuilderTargetGroupAction extends DispatchBaseAction {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(QueryBuilderTargetGroupAction.class);

	/** Server class dealing with target groups. */
	private ComTargetService targetService;

	private RecipientService recipientService;

	/** Copy service for target groups. */
	private TargetCopyService targetCopyService;

	/** Factory to create new ComTarget instances. */
	private TargetFactory targetFactory;

	/** Presentation-layer utility to synchronize editor views. */
	private EditorContentSynchronizer editorContentSynchronizer;

	private GridServiceWrapper gridService;

	private EqlFacade eqlFacade;

    private MailinglistApprovalService mailinglistApprovalService;

	protected WebStorage webStorage;
	
	protected BirtStatisticsService birtStatisticsService;

	/**
	 * Called from dispatcher in {@link #execute(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)}, if dispatch URL parameter is missing.
	 * This method does nothing but calling {@link #show(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)}.
	 *
	 * @param mapping mapped forwards
	 * @param form0 form bean
	 * @param request request data
	 * @param response response data
	 *
	 * @return selected forward
	 *
	 * @throws Exception on errors performing request
	 */
	@Override
	public final ActionForward unspecified(final ActionMapping mapping, final ActionForm form0, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		return show(mapping, form0, request, response);
	}

	/**
	 * Called from dispatcher in {@link #execute(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)}, if dispatch URL parameter indicates
	 * showing a target group is requested. This method is also invoked by {@link #unspecified(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)}.
	 *
	 * @param mapping mapped forwards
	 * @param form0 form bean
	 * @param request request data
	 * @param response response data
	 *
	 * @return selected forward
	 *
	 * @throws Exception on errors performing request
	 */
	public final ActionForward show(final ActionMapping mapping, final ActionForm form0, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final QueryBuilderTargetGroupForm form = (QueryBuilderTargetGroupForm) form0;
		final ComAdmin admin = AgnUtils.getAdmin(request);
		int companyId = admin.getCompanyID();
		WorkflowUtils.updateForwardParameters(request);
		final Integer forwardTargetItemId = (Integer) request.getSession().getAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_TARGET_ITEM_ID);
		if (forwardTargetItemId != null && forwardTargetItemId != 0) {
			form.setTargetID(forwardTargetItemId);
		}

		boolean mailTrackingAvailable = AgnUtils.isMailTrackingAvailable(admin);
		request.setAttribute("mailTrackingAvailable", mailTrackingAvailable);

		if(form.getFormat() == null) {
			form.setFormat(TargetgroupViewFormat.QUERY_BUILDER);
		}

		try {
			final ComTarget target = this.targetService.getTargetGroup(form.getTargetID(), companyId);
			form.setTargetID(target.getId());
			form.setShortname(target.getTargetName());
			form.setDescription(target.getTargetDescription());
			form.setEql(target.getEQL());
			form.setUseForAdminAndTestDelivery(target.isAdminTestDelivery());
			form.setLocked(target.isLocked());
			form.setComplexityGrade(TargetUtils.getComplexityGrade(target.getComplexityIndex(), recipientService.getNumberOfRecipients(companyId)));

			try {
				// Make data for QueryBuilder available from EQL
				final ActionMessages actionMessages = new ActionMessages();
				EqlAnalysisResult simpleEqlAnalysisResult = eqlFacade.analyseEql(form.getEql());
				if (simpleEqlAnalysisResult.isMailTrackingRequired() && !AgnUtils.isMailTrackingAvailable(admin)) {
					form.setFormat(TargetgroupViewFormat.EQL);
					actionMessages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.target.mailtrackingRequired"));
					saveMessages(request, actionMessages);
				} else {
					this.editorContentSynchronizer.synchronizeEqlToQuerybuilder(admin, form);
				}

				switch(form.getFormat()) {
				case "qb":
					return viewQB(mapping, form0, request, response);
				case "eql": // Fall through
				default:
					return viewEQL(mapping, form0, request, response);
				}
			} catch (final EditorContentSynchronizationException | EqlSyntaxErrorException e) {
				form.setFormat(TargetgroupViewFormat.EQL);

				return viewEQL(mapping, form0, request, response);
			}
		} catch(final UnknownTargetGroupIdException e) {
			logger.warn(String.format("Unknown target group ID %d", form.getTargetID()), e);

			return mapping.findForward("list");
		}
	}

	public ActionForward lock(final ActionMapping mapping, final ActionForm form0, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		QueryBuilderTargetGroupForm form = (QueryBuilderTargetGroupForm) form0;
		ComAdmin admin = AgnUtils.getAdmin(request);

		if (targetService.lockTargetGroup(admin.getCompanyID(), form.getTargetID())) {
			writeUserActivityLog(admin, "do lock target group", form.getShortname() + " (" + form.getTargetID() + ")", logger);
			ActionMessages msg = new ActionMessages();
			msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
			saveMessages(request, msg);
		} else {
			ActionMessages msg = new ActionMessages();
			msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "error.target.not_saved"));
			saveErrors(request, msg);
		}

		return show(mapping, form0, request, response);
	}

	public ActionForward unlock(final ActionMapping mapping, final ActionForm form0, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		QueryBuilderTargetGroupForm form = (QueryBuilderTargetGroupForm) form0;
		ComAdmin admin = AgnUtils.getAdmin(request);

		if (targetService.unlockTargetGroup(admin.getCompanyID(), form.getTargetID())) {
			writeUserActivityLog(admin, "do unlock target group", form.getShortname() + " (" + form.getTargetID() + ")", logger);
			ActionMessages msg = new ActionMessages();
			msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
			saveMessages(request, msg);
		} else {
			ActionMessages msg = new ActionMessages();
			msg.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage( "error.target.not_saved"));
			saveErrors(request, msg);
		}
		return show(mapping, form0, request, response);
	}

	public final ActionForward create(final ActionMapping mapping, final ActionForm form0, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final QueryBuilderTargetGroupForm form = (QueryBuilderTargetGroupForm) form0;

		final ComAdmin admin = AgnUtils.getAdmin(request);

		form.setTargetID(0);
		form.setShortname(I18nString.getLocaleString("Name", admin.getLocale()));
		form.setDescription(I18nString.getLocaleString("default.description", admin.getLocale()));
		form.setEql("");
		form.setFormat(TargetgroupViewFormat.QUERY_BUILDER);

		// Make data for QueryBuilder available from EQL
		this.editorContentSynchronizer.synchronizeEqlToQuerybuilder(admin, form);

		return viewQB(mapping, form0, request, response);
	}

	/**
	 * Called from dispatcher in {@link #execute(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)}, if the user requested switching the editor view to
	 * the Querybuilder.
	 *
	 * @param mapping mapped forwards
	 * @param form0 form bean
	 * @param request request data
	 * @param response response data
	 *
	 * @return selected forward
	 *
	 * @throws Exception on errors performing request
	 */
	public final ActionForward viewQB(final ActionMapping mapping, final ActionForm form0, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final QueryBuilderTargetGroupForm form = (QueryBuilderTargetGroupForm) form0;
		final ComAdmin admin = AgnUtils.getAdmin(request);
		final ActionMessages errors = new ActionMessages();

		final TargetgroupViewFormat currentViewFormat = TargetgroupViewFormat.fromCode(form.getFormat(), TargetgroupViewFormat.QUERY_BUILDER);

		boolean mailTrackingAvailable = AgnUtils.isMailTrackingAvailable(admin);
		request.setAttribute("mailTrackingAvailable", mailTrackingAvailable);

		try {
			editorContentSynchronizer.synchronizeEditors(admin, form, TargetgroupViewFormat.EQL);
		} catch (Exception e) {
			logger.error("Error occurred: " + e.getMessage(), e);
		}

		try {
			EqlAnalysisResult analyseEql = eqlFacade.analyseEql(form.getEql());
			final ActionMessages actionMessages = new ActionMessages();
			if (analyseEql.isMailTrackingRequired() && !AgnUtils.isMailTrackingAvailable(admin)) {
				form.setFormat(TargetgroupViewFormat.EQL);
				actionMessages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.target.mailtrackingRequired"));
				saveMessages(request, actionMessages);
			} else {
				form.setFormat(this.editorContentSynchronizer.synchronizeEditors(admin, form, TargetgroupViewFormat.QUERY_BUILDER));
			}
		} catch(final EqlSyntaxErrorException e) {
			form.setFormat(currentViewFormat);
			final List<EqlSyntaxError> syntaxErrors = e.getErrors();

			syntaxErrors.forEach(syntaxError -> {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.eql.syntax", syntaxError.getLine(), syntaxError.getColumn(), syntaxError.getSymbol()));
				errors.add("eqlErrors", new ActionMessage("error.target.eql.syntax", syntaxError.getLine(), syntaxError.getColumn(), syntaxError.getSymbol()));
			});
			
			saveErrors(request, errors);
		} catch(final EditorContentSynchronizationException e) {
			form.setFormat(TargetgroupViewFormat.EQL);

			if(logger.isInfoEnabled()) {
				logger.info("There was an error synchronizing editor content. Keeping current view format.", e);
				logger.info("EQL: " + form.getEql());
			}

			errors.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("targetgroup.tooComplex"));
		}

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
			return viewEQL(mapping, form0, request, response);
		}
		
		form.setMailinglists(mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
		form.setComplexityGrade(getComplexityGrade(form.getEql(), admin.getCompanyID()));

		return mapping.findForward("view");
	}

	/**
	 * Called from dispatcher in {@link #execute(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)}, if the user requested switching the editor view to
	 * the EQL editor.
	 *
	 * @param mapping mapped forwards
	 * @param form0 form bean
	 * @param request request data
	 * @param response response data
	 *
	 * @return selected forward
	 *
	 * @throws Exception on errors performing request
	 */
	public final ActionForward viewEQL(final ActionMapping mapping, final ActionForm form0, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final QueryBuilderTargetGroupForm form = (QueryBuilderTargetGroupForm) form0;
		final ComAdmin admin = AgnUtils.getAdmin(request);
		final ActionMessages errors = new ActionMessages();

		final TargetgroupViewFormat currentViewFormat = TargetgroupViewFormat.fromCode(form.getFormat(), TargetgroupViewFormat.QUERY_BUILDER);
		TargetgroupViewFormat targetFormat = TargetgroupViewFormat.EQL;

		try {
			targetFormat = this.editorContentSynchronizer.synchronizeEditors(admin, form, TargetgroupViewFormat.EQL);
		} catch(final EditorContentSynchronizationException e) {
			targetFormat = currentViewFormat;

			logger.info("There was an error synchronizing editor content. Keeping current view format.", e);

			errors.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("targetgroup.tooComplex"));

			logger.warn("EQL: " + form.getEql(), e);
		}

		form.setMailinglists(mailinglistApprovalService.getEnabledMailinglistsForAdmin(AgnUtils.getAdmin(request)));
		form.setFormat(targetFormat);
		form.setComplexityGrade(getComplexityGrade(form.getEql(), admin.getCompanyID()));

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
			return mapping.findForward("messages");
		}

		if(targetFormat != TargetgroupViewFormat.EQL) {
			return viewQB(mapping, form0, request, response);
		} else {
			return mapping.findForward("view");
		}
	}

	public final ActionForward save(final ActionMapping mapping, final ActionForm form0, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final QueryBuilderTargetGroupForm form = (QueryBuilderTargetGroupForm) form0;
		final ComAdmin admin = AgnUtils.getAdmin(request);
		final int companyID = AgnUtils.getCompanyID(request);

		final ActionMessages errors = new ActionMessages();

		boolean reloadTargetGroupFromDB = true;

		if (!targetService.checkIfTargetNameIsValid(form.getShortname())) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.namenotallowed"));
		} else if (targetService.checkIfTargetNameAlreadyExists(companyID, form.getShortname(), form.getTargetID())) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.namealreadyexists"));
		}
		if(!errors.isEmpty()){
			saveErrors(request, errors);
			return mapping.findForward("messages");
		}

		try {
			// Do not set view format. We just mis-use synchronizeEditors to get EQL from any editor view
			this.editorContentSynchronizer.synchronizeEditors(admin, form, TargetgroupViewFormat.EQL);

			// Load target group or create new one
			final ComTarget oldTarget = form.getTargetID() != 0 ? this.targetService.getTargetGroup(form.getTargetID(), companyID) : this.targetFactory.newTarget(companyID);
			final ComTarget newTarget = this.targetCopyService.copyTargetGroup(oldTarget, this.targetFactory.newTarget());

			// Update editable properties
			FormHelper.formPropertiesToTargetGroup(newTarget, form);
			boolean isTargetGroupValid = true;

			try {
				final TargetSavingAndAnalysisResult savingResult = targetService.saveTargetWithAnalysis(admin, newTarget, oldTarget, errors, this::writeUserActivityLog);
				final int newTargetId = savingResult.getTargetID();

				if (newTargetId > 0 && errors.isEmpty()) {
					ActionMessages actionMessages = new ActionMessages();
					actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));

					TargetComplexityGrade complexityGrade = targetService.getTargetComplexityGrade(companyID, newTargetId);

					if (complexityGrade == TargetComplexityGrade.RED) {
						actionMessages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.target.group.performance.red"));
					} else if (complexityGrade == TargetComplexityGrade.YELLOW) {
						actionMessages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.target.group.performance.yellow"));
					}

					if(savingResult.getAnalysisResult().isPresent()) {
						if(savingResult.getAnalysisResult().get().isMailTrackingRequired() && !AgnUtils.isMailTrackingAvailable(admin)) {
							actionMessages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.target.mailtrackingRequired"));
						}
					}

					saveMessages(request, actionMessages);
					form.setTargetID(newTargetId);
				} else {
					saveErrors(request, errors);
					reloadTargetGroupFromDB = newTargetId > 0;
					isTargetGroupValid = false;
				}
			} catch(final TargetGroupTooLargeException e) {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.too_large"));
				saveErrors(request, errors);

			} catch (final Exception e) {
				logger.warn("There was an error Saving the target group. ", e);

				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.saving"));
				saveErrors(request, errors);

				reloadTargetGroupFromDB = false;
				isTargetGroupValid = false;
			}

			if(isTargetGroupValid && form.isShowStatistic()) {
				form.setStatisticUrl(getReportUrl(admin, request, form));
			}

		} catch(final EqlSyntaxErrorException e) {
			final List<EqlSyntaxError> syntaxErrors = e.getErrors();

			syntaxErrors.forEach(syntaxError -> {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.eql.syntax", syntaxError.getLine(), syntaxError.getColumn(), syntaxError.getSymbol()));
				errors.add("eqlErrors", new ActionMessage("error.target.eql.syntax", syntaxError.getLine(), syntaxError.getColumn(), syntaxError.getSymbol()));
			});

			saveErrors(request, errors);

			reloadTargetGroupFromDB = false;
		} catch(final EditorContentSynchronizationException e) {
			logger.info("There was an error synchronizing editor content.", e);

			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.saving"));
			saveErrors(request, errors);

			reloadTargetGroupFromDB = false;
		}

		if (reloadTargetGroupFromDB) {
			return show(mapping, form0, request, response);
		} else {
			return mapping.findForward("messages");
		}
	}

	private TargetComplexityGrade getComplexityGrade(String eql, @VelocityCheck int companyId) {
		int complexityIndex = targetService.calculateComplexityIndex(eql, companyId);
		int recipientsCount = recipientService.getNumberOfRecipients(companyId);

		return TargetUtils.getComplexityGrade(complexityIndex, recipientsCount);
	}

	private String getReportUrl(ComAdmin admin, HttpServletRequest request, QueryBuilderTargetGroupForm form) throws Exception {
		try {
			RecipientStatusStatisticDto statisticDto = new RecipientStatusStatisticDto();
			statisticDto.setMediaType(0);
			statisticDto.setTargetId(form.getTargetID());
			statisticDto.setMailinglistId(form.getMailinglistId());
			statisticDto.setFormat("html");

			return birtStatisticsService.getRecipientStatusStatisticUrl(admin, request.getSession(false).getId(), statisticDto);
		} catch (Exception e) {
			logger.error("Error during generation statistic url " + e);
		}

		return StringUtils.EMPTY;
	}

	public final ActionForward copy(final ActionMapping mapping, final ActionForm form0, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final QueryBuilderTargetGroupForm form = (QueryBuilderTargetGroupForm) form0;
		final int companyID = AgnUtils.getCompanyID(request);
		final ActionMessages errors = new ActionMessages();

		ComTarget copiedTarget = form.getTargetID() != 0 ? this.targetService.getTargetGroup(form.getTargetID(), companyID) : this.targetFactory.newTarget(companyID);
		copiedTarget.setId(0);
		copiedTarget.setCreationDate(null);
		copiedTarget.setChangeDate(null);

		final String newName = SafeString.getLocaleString("mailing.CopyOf", AgnUtils.getLocale(request)) +
				" " + copiedTarget.getTargetName();

		copiedTarget.setTargetName(newName);
		form.setShortname(newName);

		try {
			int targetID = targetService.saveTarget(AgnUtils.getAdmin(request), copiedTarget, null, errors, this::writeUserActivityLog);
	
			form.setTargetID(targetID);
			saveErrors(request, errors);
		} catch(final TargetGroupTooLargeException e) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.target.too_large"));

			form.setTargetID(e.getTargetId());
			saveErrors(request, errors);
		}

		return show(mapping, form0, request, response);
	}

	public final ActionForward listDependents(final ActionMapping mapping, final ActionForm form0, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final int companyId = AgnUtils.getCompanyID(request);
		final QueryBuilderTargetGroupForm form = (QueryBuilderTargetGroupForm) form0;

        webStorage.access(WebStorage.TARGET_DEPENDENTS_OVERVIEW, entry -> {
        	if (form.getNumberOfRows() > 0) {
        		entry.setRowsCount(form.getNumberOfRows());
        		if (form.getFilterTypes() == null) {
					entry.setFilterTypes(null);
				} else {
					entry.setFilterTypes(Arrays.asList(form.getFilterTypes()));
				}
			} else {
        		form.setNumberOfRows(entry.getRowsCount());
        		form.setFilterTypes(entry.getFilterTypes().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
			}
		});

		PaginatedListImpl<Dependent<TargetGroupDependentType>> dependents = targetService.getDependents(companyId, form.getTargetID(), form.getFilterTypesSet(), form.getPageNumber(), form.getNumberOfRows(), form.getSort(), form.getOrder());
		form.setShortname(targetService.getTargetName(form.getTargetID(), AgnUtils.getCompanyID(request)));
		form.setDependents(dependents);

		List<Integer> mailingIds = dependents.getList().stream()
				.filter(dependent -> TargetGroupDependentType.MAILING == dependent.getType() || TargetGroupDependentType.MAILING_CONTENT == dependent.getType())
				.map(Dependent::getId)
				.collect(Collectors.toList());

		request.setAttribute("mailingGridTemplateMap", gridService.getGridTemplateIdsByMailingIds(companyId, mailingIds));

		return mapping.findForward("dependents_list");
	}

	@Required
	public final void setTargetService(final ComTargetService service) {
		this.targetService = service;
	}

	@Required
	public final void setEditorContentSynchronizer(final EditorContentSynchronizer synchronizer) {
		this.editorContentSynchronizer = synchronizer;
	}

	@Required
	public final void setTargetFactory(final TargetFactory factory) {
		this.targetFactory = factory;
	}

	@Required
	public final void setTargetCopyService(final TargetCopyService service) {
		this.targetCopyService = service;
	}

	@Required
	public void setEqlFacade(EqlFacade eqlFacade) {
		this.eqlFacade = eqlFacade;
	}

	@Required
	public final void setMailinglistApprovalService(final MailinglistApprovalService service) {
		this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
	}

    @Required
    public void setWebStorage(WebStorage webStorage) {
        this.webStorage = webStorage;
    }

    @Required
	public void setGridService(GridServiceWrapper gridService) {
		this.gridService = gridService;
	}
	
	@Required
	public void setBirtStatisticsService(BirtStatisticsService birtStatisticsService) {
		this.birtStatisticsService = birtStatisticsService;
	}

	@Required
	public void setRecipientService(RecipientService recipientService) {
		this.recipientService = recipientService;
	}
}
