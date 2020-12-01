/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.AdminPreferences;
import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.factory.DynamicTagContentFactory;
import org.agnitas.beans.factory.MailingFactory;
import org.agnitas.dao.AdminPreferencesDao;
import org.agnitas.dao.EmmActionDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.preview.TAGCheckFactory;
import org.agnitas.preview.TagSyntaxChecker;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.CharacterEncodingValidator;
import org.agnitas.web.StrutsActionBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComMailing;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.dao.ComProfileFieldDao;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.service.ComMailingContentService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.util.preview.PreviewImageService;

public class ComMailingContentAction extends StrutsActionBase {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComMailingContentAction.class);

	public static final int ACTION_VIEW_CONTENT = ACTION_LAST + 1;
	public static final int ACTION_IMPORT_CONTENT = ACTION_LAST + 2;
	public static final int ACTION_GENERATE_TEXT_FROM_HTML = ACTION_LAST + 3;
	public static final int ACTION_GENERATE_TEXT_FROM_HTML_CONFIRM = ACTION_LAST + 4;
    
    public static final int CONTENT_ACTION_LAST = ACTION_LAST + 4;

	protected ComProfileFieldDao profileFieldDao;
	protected ComMailingContentService mailingContentService;
	protected ComMailingComponentDao mailingComponentDao;
	protected DynamicTagDao dynamicTagDao;
	protected PreviewImageService previewImageService;
    protected EmmActionDao actionDao;
	protected TagSyntaxChecker tagSyntaxChecker;
    protected ComMailingBaseService mailingBaseService;
    private MaildropService maildropService;
    private MailinglistApprovalService mailinglistApprovalService;
	protected MailingService mailingService;

	protected TAGCheckFactory tagCheckFactory;
	protected CharacterEncodingValidator characterEncodingValidator;

	protected MailingFactory mailingFactory;
	protected DynamicTagContentFactory dynamicTagContentFactory;

	/** Service accessing target groups. */
	protected ComTargetService targetService;

	/** Configuration service. */
	protected ConfigService configService;

	/** DAO accessing preference settings of admins. */
	protected AdminPreferencesDao adminPreferencesDao;
    
    private GridServiceWrapper gridServiceWrapper;

	@Override
    public String subActionMethodName(int subAction) {
        switch (subAction) {
            case ACTION_VIEW_CONTENT:
                return "view_content";
            case ACTION_IMPORT_CONTENT:
                return "import_content";
			case ACTION_GENERATE_TEXT_FROM_HTML:
				return "generate_text_from_html";
			case ACTION_GENERATE_TEXT_FROM_HTML_CONFIRM:
				return "generate_text_from_html_confirm";
    			
            default:
                return super.subActionMethodName(subAction);
        }
    }

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException, Exception {
		ComMailingContentForm aForm = (ComMailingContentForm) form;
		ActionMessages errors = new ActionMessages();
		ActionMessages messages = new ActionMessages();
		ActionForward destination = null;

		try {
			destination = doExecute(mapping, aForm, req, res, errors, messages);
		} catch (Exception e) {
			logger.error("Error occurred: " + e.getMessage(), e);

			String errorMessage = configService.getValue(ConfigValue.SupportEmergencyUrl);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", errorMessage));

			prepareListPage(aForm, req);
			destination = mapping.findForward("list");
		}

		// Report any errors we have discovered back to the original form
		if (!errors.isEmpty()) {
			saveErrors(req, errors);
		}

		// Report any message (non-errors) we have discovered
		if (!messages.isEmpty()) {
			saveMessages(req, messages);
		}

		return destination;
	}

	protected ActionForward doExecute(ActionMapping mapping, ComMailingContentForm form, HttpServletRequest req, HttpServletResponse res, final ActionMessages errors, final ActionMessages messages) throws Exception {
		ComAdmin admin = AgnUtils.getAdmin(req);

		assert admin != null;

		switch (form.getAction()) {
		case ACTION_VIEW_CONTENT:
			prepareListPage(form, req);
			form.setShowHTMLEditor(true);
			writeUserActivityLog(admin, "view content", "active tab - content");
			return mapping.findForward("list");

		case ACTION_GENERATE_TEXT_FROM_HTML:
			if (isMailingEditable(admin, form)) {
				if (generateTextContent(admin, form.getMailingID())) {
					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
				} else {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("Error"));
				}
			} else {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("status_changed"));
			}
			prepareViewPage(form, req, StringUtils.equals(form.getDynName(), AgnUtils.DEFAULT_MAILING_TEXT_DYNNAME));

			return mapping.findForward("list");

		case ACTION_GENERATE_TEXT_FROM_HTML_CONFIRM:
			if (isMailingEditable(admin, form)) {
				final LightweightMailing mailing = mailingService.getLightweightMailing(admin.getCompanyID(), form.getMailingID());
				form.setShortname(mailing.getShortname());
				return mapping.findForward("generate_text_confirm");
			} else {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("status_changed"));
				return null;
			}

		default:
			return null;
		}
	}
	
	private boolean generateTextContent(ComAdmin admin, int mailingId) {
		try {
			return mailingService.generateMailingTextContentFromHtml(admin, mailingId);
		} catch (Exception e) {
			logger.error("Error occurred: " + e.getMessage(), e);
		}
		return false;
	}
	
	private void prepareViewPage(ComMailingContentForm form, HttpServletRequest req, boolean overrideSubmittedValues) {
		Map<Integer, DynamicTagContent> contentMap = form.getContent();

		loadMailing(form, req);

		if (!overrideSubmittedValues) {
			form.setContent(contentMap);
		}

		loadTargetGroups(form, req);
		loadAvailableInterestGroups(form, req);

		if (overrideSubmittedValues) {
			loadSelectedInterestGroup(form, req);
		}
	}

	protected void prepareListPage(ComMailingContentForm form, HttpServletRequest req) {
		loadMailing(form, req);
		loadTargetGroups(form, req);
		loadAvailableInterestGroups(form, req);

		req.setAttribute("limitedRecipientOverview",
				form.isWorldMailingSend() &&
						!mailinglistApprovalService.isAdminHaveAccess(AgnUtils.getAdmin(req), form.getMailinglistID()));
	}

	private void loadMailing(ComMailingContentForm form, HttpServletRequest req) {
		ComAdmin admin = AgnUtils.getAdmin(req);
		Mailing mailing = mailingService.getMailing(admin.getCompanyID(), form.getMailingID());

		AdminPreferences adminPreferences = adminPreferencesDao.getAdminPreferences(admin.getAdminID());

		if (mailing == null) {
			mailing = mailingFactory.newMailing();
			mailing.init(AgnUtils.getCompanyID(req), getApplicationContext(req));
			mailing.setId(0);
			form.setMailingID(0);
			form.setMailingEditable(true);
			form.setMailingExclusiveLockingAcquired(true);
		} else {
			form.setMailingEditable(isMailingEditable(admin, form));
			form.setMailingExclusiveLockingAcquired(tryToLock(admin, form));
		}

		form.setShortname(mailing.getShortname());
		form.setDescription(mailing.getDescription());
		form.setIsTemplate(mailing.isIsTemplate());
		form.setMailinglistID(mailing.getMailinglistID());
		form.setMailingID(mailing.getId());
		form.setMailFormat(mailing.getEmailParam().getMailFormat());
		form.setTags(mailing.getDynTags(), true);
		form.setWorldMailingSend(this.maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID()));

		form.setMailingContentView(adminPreferences.getMailingContentView());
		form.setDynTagNames(mailingBaseService.getDynamicTagNames(mailing));

		writeUserActivityLog(admin, "view " + (form.isIsTemplate() ? "template" : "mailing"),
				form.getShortname() + " (" + form.getMailingID() + ")");

		form.setEnableTextGeneration(!form.isIsTemplate() && mailingContentService.isGenerationAvailable(mailing));

		if (mailing.getId() > 0) {
			form.setIsMailingUndoAvailable(mailingBaseService.checkUndoAvailable(mailing.getId()));
			form.setGridTemplateId(gridServiceWrapper.getGridTemplateIdByMailingId(mailing.getId()));
			form.setWorkflowId(mailingBaseService.getWorkflowId(mailing.getId(), AgnUtils.getCompanyID(req)));

			// Grid mailing should not expose its internals (dynamic tags representing building blocks) for editing.
			if (form.getGridTemplateId() > 0) {
				MailingComponent htmlComponent = mailing.getComponents().get("agnHtml");
				if (htmlComponent != null) {
					Map<String, DynamicTag> tags = form.getTags();
					for (String name : getAgnTags(htmlComponent.getEmmBlock(), req)) {
						tags.remove(name);
					}
				}
			}
		} else {
			form.setIsMailingUndoAvailable(false);
			form.setGridTemplateId(0);
			form.setWorkflowId(0);
		}
	}

	private Vector<String> getAgnTags(String content, HttpServletRequest req) {
		WebApplicationContext context = getApplicationContext(req);
		ComMailing mailing = mailingFactory.newMailing();

		mailing.init(AgnUtils.getCompanyID(req), context);
		try {
			return mailing.findDynTagsInTemplates(content, context);
		} catch (Exception e) {
			logger.error("Error occurred: " + e.getMessage(), e);
			return new Vector<>();
		}
	}

	protected void loadTargetGroups(ComMailingContentForm form, HttpServletRequest req) {
		final boolean showContentBlockTargetGroupsOnly = !AgnUtils.allowed(req, Permission.MAILING_CONTENT_SHOW_EXCLUDED_TARGETGROUPS);
		
		final List<TargetLight> list = targetService
				.getTargetLights(AgnUtils.getAdmin(req), true, true, false, showContentBlockTargetGroupsOnly);
		
		form.setAvailableTargetGroups(list);
	}

	private void loadAvailableInterestGroups(ComMailingContentForm form, HttpServletRequest req) {
		List<ProfileField> availableInterestFields = Collections.emptyList();
		try {
			availableInterestFields = profileFieldDao.getProfileFieldsWithInterest(AgnUtils.getCompanyID(req), AgnUtils.getAdminId(req));
		} catch (Exception e) {
			logger.error("Error occurred: " + e.getMessage(), e);
		}
		form.setAvailableInterestGroups(availableInterestFields);
	}

	private void loadSelectedInterestGroup(ComMailingContentForm form, HttpServletRequest req) {
		String interestGroup = dynamicTagDao.getDynamicTagInterestGroup(AgnUtils.getCompanyID(req), form.getMailingID(), form.getDynNameID());
		form.setDynInterestGroup(interestGroup);
	}

	/**
	 * Check whether or not a mailing is editable.
	 * Basically a world sent mailing is not editable but there's a permission {@link com.agnitas.emm.core.Permission#MAILING_CONTENT_CHANGE_ALWAYS}
	 * that unlocks sent mailing so it could be edited anyway.
	 */
	protected boolean isMailingEditable(ComAdmin admin, ComMailingContentForm form) {
		if (maildropService.isActiveMailing(form.getMailingID(), admin.getCompanyID())) {
			return admin.permissionAllowed(Permission.MAILING_CONTENT_CHANGE_ALWAYS);
		}
		return true;
	}

	private boolean tryToLock(ComAdmin admin, ComMailingContentForm form) {
		try {
			return mailingService.tryToLock(admin, form.getMailingID());
		} catch (MailingNotExistException e) {
			// New mailing is always edited exclusively.
			return true;
		}
	}

	private WebApplicationContext getApplicationContext(HttpServletRequest req) {
		return WebApplicationContextUtils.getRequiredWebApplicationContext(req
				.getSession().getServletContext());
	}

	public void setTagCheckFactory(TAGCheckFactory tagCheckFactory) {
		this.tagCheckFactory = tagCheckFactory;
	}

	public void setCharacterEncodingValidator(CharacterEncodingValidator characterEncodingValidator) {
		this.characterEncodingValidator = characterEncodingValidator;
	}

	public void setMailingFactory(MailingFactory mailingFactory) {
		this.mailingFactory = mailingFactory;
	}

	public void setDynamicTagContentFactory(DynamicTagContentFactory dynamicTagContentFactory) {
		this.dynamicTagContentFactory = dynamicTagContentFactory;
	}

	/**
	 * Sets DAO accessing target groups.
	 *
	 * @param targetService
	 *            DAO accessing target groups
	 */
	@Required
	public void setTargetService(ComTargetService targetService) {
		this.targetService = targetService;
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setAdminPreferencesDao(AdminPreferencesDao adminPreferencesDao) {
		this.adminPreferencesDao = adminPreferencesDao;
	}

	@Required
	public void setProfileFieldDao(ComProfileFieldDao profileFieldDao) {
		this.profileFieldDao = profileFieldDao;
	}

	@Required
	public void setMailingContentService(ComMailingContentService mailingContentService) {
		this.mailingContentService = mailingContentService;
	}

	@Required
	public void setMailingComponentDao(ComMailingComponentDao mailingComponentDao) {
		this.mailingComponentDao = mailingComponentDao;
	}

	@Required
	public void setDynamicTagDao(DynamicTagDao dynamicTagDao) {
		this.dynamicTagDao = dynamicTagDao;
	}

	@Required
	public void setPreviewImageService(PreviewImageService previewImageService) {
		this.previewImageService = previewImageService;
	}

	@Required
	public void setMailingService(MailingService mailingService) {
		this.mailingService = mailingService;
	}

	@Required
    public void setActionDao(EmmActionDao actionDao) {
        this.actionDao = actionDao;
    }

    @Required
    public void setMailingBaseService(ComMailingBaseService mailingBaseService) {
        this.mailingBaseService = mailingBaseService;
    }

	@Required
	public void setTagSyntaxChecker(TagSyntaxChecker tagSyntaxChecker) {
		this.tagSyntaxChecker = tagSyntaxChecker;
	}
	
	@Required
	public final void setMaildropService(final MaildropService service) {
		this.maildropService = service;
	}
	  
    @Required
    public final void setMailinglistApprovalService(final MailinglistApprovalService service) {
    	this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }
	
    @Required
	public void setGridServiceWrapper(GridServiceWrapper gridServiceWrapper) {
		this.gridServiceWrapper = gridServiceWrapper;
	}
}
