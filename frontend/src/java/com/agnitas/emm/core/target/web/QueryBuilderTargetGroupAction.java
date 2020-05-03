/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.web;

import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComTarget;
import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatusStatisticDto;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.eql.emm.legacy.EqlToTargetRepresentationConversionException;
import com.agnitas.emm.core.target.eql.parser.EqlSyntaxError;
import com.agnitas.emm.core.target.eql.parser.EqlSyntaxErrorException;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.target.service.TargetCopyService;
import com.agnitas.emm.core.target.web.util.EditorContentSynchronizationException;
import com.agnitas.emm.core.target.web.util.EditorContentSynchronizer;
import com.agnitas.emm.core.target.web.util.FormHelper;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.messages.I18nString;
import org.agnitas.emm.core.target.exception.UnknownTargetGroupIdException;
import org.agnitas.target.TargetFactory;
import org.agnitas.target.impl.TargetRepresentationImpl;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.SafeString;
import org.agnitas.web.DispatchBaseAction;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

/**
 * Action handling the target group query builder editor view.
 */
public final class QueryBuilderTargetGroupAction extends DispatchBaseAction {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(QueryBuilderTargetGroupAction.class);
	
	/** Server class dealing with target groups. */
	private ComTargetService targetService;
	
	/** Copy service for target groups. */
	private TargetCopyService targetCopyService;
	
	/** Factory to create new ComTarget instances. */
	private TargetFactory targetFactory;
	
	/** Presentation-layer utility to synchronize editor views. */
	private EditorContentSynchronizer editorContentSynchronizer;

	private ComMailingBaseService mailingService;

	private EqlFacade eqlFacade;
	
    private MailinglistApprovalService mailinglistApprovalService;

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
		
		WorkflowUtils.updateForwardParameters(request);
		final Integer forwardTargetItemId = (Integer) request.getSession().getAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_TARGET_ITEM_ID);
		if (forwardTargetItemId != null && forwardTargetItemId != 0) {
			form.setTargetID(forwardTargetItemId);
		}

		if(form.getFormat() == null) {
			form.setFormat(TargetgroupViewFormat.QUERY_BUILDER);
		}
		
		try {
			final ComTarget target = this.targetService.getTargetGroup(form.getTargetID(), admin.getCompanyID());
			form.setTargetID(target.getId());
			form.setShortname(target.getTargetName());
			form.setDescription(target.getTargetDescription());
			form.setEql(target.getEQL());
			form.setUseForAdminAndTestDelivery(target.isAdminTestDelivery());
			form.setLocked(target.isLocked());
			form.setSimpleStructure(target.isSimpleStructured());

			try {
				// Make data for QueryBuilder available from EQL
				this.editorContentSynchronizer.synchronizeEqlToQuerybuilder(admin, form);

				if (!form.isSimpleStructure()) {
					form.setFormat(TargetgroupViewFormat.EQL);
				}
			} catch (final EditorContentSynchronizationException e) {
				form.setFormat(TargetgroupViewFormat.EQL);

				return viewEQL(mapping, form0, request, response);
			}

			return form.isSimpleStructure()
					? viewQB(mapping, form0, request, response)
					: viewEQL(mapping, form0, request, response);
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
		form.setSimpleStructure(true);
		
		
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

		final TargetgroupViewFormat currentViewFormat = TargetgroupViewFormat.fromCode(form.getFormat(), TargetgroupViewFormat.QUERY_BUILDER);

		try {
			form.setFormat(this.editorContentSynchronizer.synchronizeEditors(admin, form, TargetgroupViewFormat.QUERY_BUILDER));
		} catch(final EditorContentSynchronizationException e) {
			form.setFormat(currentViewFormat);
			
			if(logger.isInfoEnabled()) {
				logger.info("There was an error synchronizing editor content. Keeping current view format.", e);
				logger.info("EQL: " + form.getEql());
			}
			
			final ActionMessages errors = new ActionMessages();
			errors.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("targetgroup.tooComplex"));
			saveErrors(request, errors);
		}

		form.setMailinglists(mailinglistApprovalService.getEnabledMailinglistsForAdmin(AgnUtils.getAdmin(request)));

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

		final TargetgroupViewFormat currentViewFormat = TargetgroupViewFormat.fromCode(form.getFormat(), TargetgroupViewFormat.QUERY_BUILDER);
		TargetgroupViewFormat targetFormat = TargetgroupViewFormat.EQL;
		
		try {
			targetFormat = this.editorContentSynchronizer.synchronizeEditors(admin, form, TargetgroupViewFormat.EQL);
		} catch(final EditorContentSynchronizationException e) {
			targetFormat = currentViewFormat;
			
			logger.info("There was an error synchronizing editor content. Keeping current view format.", e);
			
			final ActionMessages errors = new ActionMessages();
			errors.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("targetgroup.tooComplex"));
			saveErrors(request, errors);
			
			logger.warn("EQL: " + form.getEql(), e);
		}

		form.setMailinglists(mailinglistApprovalService.getEnabledMailinglistsForAdmin(AgnUtils.getAdmin(request)));
		form.setFormat(targetFormat);
		
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
				newTarget.setTargetStructure(eqlFacade.convertEqlToTargetRepresentation(newTarget.getEQL(), companyID));
			} catch(final EqlToTargetRepresentationConversionException e) {
				if (logger.isInfoEnabled()) {
					logger.info("EQL expression is not convertible to TargetRepresentation", e);
				}
				
				newTarget.setTargetStructure(new TargetRepresentationImpl());
				isTargetGroupValid = false;
			}

			try {
				int newTargetId = targetService.saveTarget(admin, newTarget, oldTarget, errors, this::writeUserActivityLog);

				if (newTargetId > 0 && errors.isEmpty()) {
					ActionMessages messages = new ActionMessages();
					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
					saveMessages(request, messages);
					form.setTargetID(newTargetId);
				} else {
					saveErrors(request, errors);
					reloadTargetGroupFromDB = newTargetId > 0;
					isTargetGroupValid = false;
				}
			} catch (final Exception e) {
				logger.info("There was an error Saving the target group. ", e);

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

		int targetID = targetService.saveTarget(AgnUtils.getAdmin(request), copiedTarget, null, errors, this::writeUserActivityLog);

		form.setTargetID(targetID);
		saveErrors(request, errors);

		return show(mapping, form0, request, response);
	}

	public final ActionForward viewMailings(final ActionMapping mapping, final ActionForm form0, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final QueryBuilderTargetGroupForm form = (QueryBuilderTargetGroupForm) form0;
		form.setUsedInMailings(mailingService.getMailingsDependentOnTargetGroup(AgnUtils.getCompanyID(request), form.getTargetID()));
		form.setShortname(targetService.getTargetName(form.getTargetID(), AgnUtils.getCompanyID(request)));
		return mapping.findForward("view_mailings");
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
	public void setMailingService(ComMailingBaseService mailingService) {
		this.mailingService = mailingService;
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
	public void setBirtStatisticsService(BirtStatisticsService birtStatisticsService) {
		this.birtStatisticsService = birtStatisticsService;
	}
}
