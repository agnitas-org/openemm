/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Vector;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComMailing;
import com.agnitas.beans.ComProfileField;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComProfileFieldDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.core.LinkService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.service.ComMailingContentService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.util.preview.PreviewImageService;
import org.agnitas.actions.EmmAction;
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
import org.agnitas.exceptions.CharacterEncodingValidationExceptionMod;
import org.agnitas.exceptions.EncodingError;
import org.agnitas.preview.AgnTagError;
import org.agnitas.preview.PreviewHelper;
import org.agnitas.preview.TAGCheck;
import org.agnitas.preview.TAGCheckFactory;
import org.agnitas.preview.TagSyntaxChecker;
import org.agnitas.util.AgnTagUtils;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.CharacterEncodingValidator;
import org.agnitas.web.StrutsActionBase;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ComMailingContentAction extends StrutsActionBase {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComMailingContentAction.class);

	public static final int ACTION_VIEW_CONTENT = ACTION_LAST + 1;
	public static final int ACTION_VIEW_TEXTBLOCK = ACTION_LAST + 2;
	public static final int ACTION_ADD_TEXTBLOCK = ACTION_LAST + 3;
	public static final int ACTION_SAVE_TEXTBLOCK = ACTION_LAST + 4;
	public static final int ACTION_SAVE_COMPONENT_EDIT = ACTION_LAST + 5;
	public static final int ACTION_DELETE_TEXTBLOCK = ACTION_LAST + 6;
	public static final int ACTION_CHANGE_ORDER_UP = ACTION_LAST + 7;
	public static final int ACTION_CHANGE_ORDER_DOWN = ACTION_LAST + 8;
	public static final int ACTION_CHANGE_ORDER_TOP = ACTION_LAST + 9;
	public static final int ACTION_CHANGE_ORDER_BOTTOM = ACTION_LAST + 10;
	public static final int ACTION_SAVE_TEXTBLOCK_AND_BACK = ACTION_LAST + 11;
	public static final int ACTION_MAILING_CONTENT_LAST = ACTION_LAST + 11;
	public static final int ACTION_IMPORT_CONTENT = ACTION_MAILING_CONTENT_LAST + 1;
	public static final int ACTION_GENERATE_TEXT_FROM_HTML = ACTION_MAILING_CONTENT_LAST + 2;
	public static final int ACTION_ADD_TEXTBLOCK_AND_BACK = ACTION_MAILING_CONTENT_LAST + 3;

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
    
	protected TAGCheckFactory tagCheckFactory;
	protected CharacterEncodingValidator characterEncodingValidator;

	/** DAO accessing mailing data. */
	protected ComMailingDao mailingDao;
	protected MailingFactory mailingFactory;
	protected DynamicTagContentFactory dynamicTagContentFactory;

	/** DAO accessing target groups. */
	protected ComTargetDao targetDao;

	/** Configuration service. */
	protected ConfigService configService;

	/** DAO accessing preference settings of admins. */
	protected AdminPreferencesDao adminPreferencesDao;
    
    private LinkService linkService;
    
    private GridServiceWrapper gridServiceWrapper;

	@Override
    public String subActionMethodName(int subAction) {
        switch (subAction) {
            case ACTION_VIEW_CONTENT:
                return "view_content";
            case ACTION_IMPORT_CONTENT:
                return "import_content";
            case ACTION_VIEW_TEXTBLOCK:
                return "view_textblock";  
            case ACTION_SAVE_TEXTBLOCK_AND_BACK:
                return "save_textblock_and_back";
            case ACTION_SAVE_TEXTBLOCK:
                return "save_textblock";
            case ACTION_ADD_TEXTBLOCK:
                return "add_textblock";
            case ACTION_DELETE_TEXTBLOCK:
                return "delete_textblock";
            case ACTION_CHANGE_ORDER_UP:
                return "change_order_up"; 
            case ACTION_CHANGE_ORDER_DOWN:
                return "change_order_down";
            case ACTION_CHANGE_ORDER_TOP:
                return "change_order_top";
            case ACTION_CHANGE_ORDER_BOTTOM:
                return "change_order_bottom";
            case ACTION_GENERATE_TEXT_FROM_HTML:
                return "generate_text_from_html";
			case ACTION_ADD_TEXTBLOCK_AND_BACK:
				return "add_textblock_and_back";
                
            default:
                return super.subActionMethodName(subAction);
        }
    }

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
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
		switch (form.getAction()) {
		case ACTION_VIEW_CONTENT:
			prepareListPage(form, req);
			form.setShowHTMLEditor(true);
			
			writeUserActivityLog(AgnUtils.getAdmin(req), "view content", "active tab - content");
			return mapping.findForward("list");

		case ACTION_VIEW_TEXTBLOCK:
			prepareViewPage(form, req, true);
			writeUserActivityLog(AgnUtils.getAdmin(req), "view content text block", "active tab - content");
			return mapping.findForward("view");

		case ACTION_SAVE_TEXTBLOCK_AND_BACK:
		case ACTION_ADD_TEXTBLOCK_AND_BACK:
			if (saveChanges(form, req, messages, errors)) {
				prepareListPage(form, req);
				return mapping.findForward("list");
			} else {
				prepareViewPage(form, req, false);
				return mapping.findForward("view");
			}

		case ACTION_SAVE_TEXTBLOCK:
		case ACTION_ADD_TEXTBLOCK:
		case ACTION_CHANGE_ORDER_UP:
		case ACTION_CHANGE_ORDER_DOWN:
		case ACTION_CHANGE_ORDER_TOP:
		case ACTION_CHANGE_ORDER_BOTTOM:
		case ACTION_DELETE_TEXTBLOCK:
			if (saveChanges(form, req, messages, errors)) {
				prepareViewPage(form, req, true);
			} else {
				prepareViewPage(form, req, false);
			}
			return mapping.findForward("view");

		case ACTION_GENERATE_TEXT_FROM_HTML:
			if (isMailingEditable(form, req)) {
				if (generateTextContent(req, form)) {
					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
				} else {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("Error"));
				}
			} else {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("status_changed"));
			}
			prepareViewPage(form, req, StringUtils.equals(form.getDynName(), AgnUtils.DEFAULT_MAILING_TEXT_DYNNAME));
			
			return mapping.findForward("list");

		default:
			return null;
		}
	}
	
	private void prepareViewPage(ComMailingContentForm form, HttpServletRequest req, boolean overrideSubmittedValues) {
		Map<Integer, DynamicTagContent> contentMap = form.getContent();

		loadMailing(form, req, true);

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
		loadMailing(form, req, false);
		loadTargetGroups(form, req);
		loadAvailableInterestGroups(form, req);

		req.setAttribute("limitedRecipientOverview",
				form.isWorldMailingSend() &&
						!mailinglistApprovalService.isAdminHaveAccess(AgnUtils.getAdmin(req), form.getMailinglistID()));
	}

	private Mailing loadMailing(ComMailingContentForm form, HttpServletRequest req, boolean loadContentBlock) {
		Mailing mailing = mailingDao.getMailing(form.getMailingID(), AgnUtils.getCompanyID(req));

		ComAdmin admin = AgnUtils.getAdmin(req);
		AdminPreferences adminPreferences = adminPreferencesDao.getAdminPreferences(admin.getAdminID());

		if (mailing == null) {
			mailing = mailingFactory.newMailing();
			mailing.init(AgnUtils.getCompanyID(req), getApplicationContext(req));
			mailing.setId(0);
			form.setMailingID(0);
			form.setMailingEditable(true);
		} else {
			form.setMailingEditable(isMailingGridEditable(form.getMailingID(), req));
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

		form.setEnableTextGeneration(mailingContentService.isGenerationAvailable(mailing));

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

		return mailing;
	}

	/**
	 * Check whether or not a mailing is editable.
	 * Basically a world sent mailing is not editable but there's a permission {@link com.agnitas.emm.core.Permission#MAILING_CONTENT_CHANGE_ALWAYS}
	 * that unlocks sent mailing so it could be edited anyway.
	 */
	private boolean isMailingGridEditable(int mailingID, HttpServletRequest request){
		if(maildropService.isActiveMailing(mailingID, AgnUtils.getCompanyID(request))) {
			return AgnUtils.allowed(request, Permission.MAILING_CONTENT_CHANGE_ALWAYS);
		} else {
			return true;
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

	private void loadTargetGroups(ComMailingContentForm form, HttpServletRequest req) {
		final boolean showContentBlockTargetGroupsOnly = !AgnUtils.allowed(req, Permission.MAILING_CONTENT_SHOW_EXCLUDED_TARGETGROUPS);
		
		final List<TargetLight> list = targetDao.getTargetLights(AgnUtils.getCompanyID(req), true, true, false, showContentBlockTargetGroupsOnly);
		
		form.setAvailableTargetGroups(list);
	}

	private void loadAvailableInterestGroups(ComMailingContentForm form, HttpServletRequest req) {
		List<ComProfileField> availableInterestFields = Collections.emptyList();
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

	private boolean saveChanges(ComMailingContentForm form, HttpServletRequest req, ActionMessages messages, ActionMessages errors) throws Exception {
		if (isMailingEditable(form, req)) {
			if (doSaveChanges(form, req, messages, errors)) {
				previewImageService.generateMailingPreview(AgnUtils.getAdmin(req), req.getSession(false).getId(), form.getMailingID(), true);
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
				return true;
			}
		} else {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("status_changed"));
		}
		return false;
	}

	private boolean validateTextBlockTags(ComMailingContentForm form, HttpServletRequest req, ActionMessages messages, ActionMessages errors) throws Exception {
		boolean success = true;

		if (form.getMailingID() > 0) {
			List<AgnTagError> agnTagSyntaxErrors = new ArrayList<>();
			List<String[]> errorReport = new ArrayList<>();

			String dynName = form.getDynName();

			TAGCheck tagCheck = tagCheckFactory.createTAGCheck(form.getMailingID());
			for (String textBlockContent : form.getContentForValidation()) {
				// Validate HTML link syntax
				Integer errorLine = linkService.getLineNumberOfFirstInvalidLink(textBlockContent);
				if (errorLine != null) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid_link", dynName, errorLine));
				}

				// Validate agn-tag syntax
				if (!tagSyntaxChecker.check(AgnUtils.getCompanyID(req), textBlockContent, agnTagSyntaxErrors)) {
					for (AgnTagError agnTagError : agnTagSyntaxErrors) {
						appendErrorToList(errorReport, dynName, agnTagError.getFullAgnTagText(), agnTagError.getLocalizedMessage(req.getLocale()));
					}
					success = false;
				}

				if (success) {
					StringBuffer tagErrorReport = new StringBuffer();
					Vector<String> failures = new Vector<>();
					// Use the old check method only if the new one has not found any errors
					if (!tagCheck.checkContent(textBlockContent, tagErrorReport, failures)) {
						appendErrorsToList(errorReport, dynName, new StringBuffer(StringEscapeUtils.escapeHtml(tagErrorReport.toString())));
						success = false;
					}
				}

				// Check for content warnings
				ComMailingContentChecker.checkHtmlWarningConditions(textBlockContent, messages);
			}
			tagCheck.done();

			if (!success) {
				req.setAttribute("errorReport", errorReport);

				if (agnTagSyntaxErrors.isEmpty()) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.template.dyntags"));
				} else {
					AgnTagError error = agnTagSyntaxErrors.get(0);
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(error.getErrorKey().getMessageKey(), error.getAdditionalErrorDataWithLineInfo()));
				}
			}
		} else {
			success = false;
		}

		return success;
	}

	@SuppressWarnings("unused")
    private boolean validateTextBlockEncoding(ComMailingContentForm form, ActionMessages errors) {
        try {
            String parameters = mailingDao.getEmailParameter(form.getMailingID());
            if (parameters != null) {
                String charset = AgnUtils.getAttributeFromParameterString(parameters, "charset");
                characterEncodingValidator.validateContentMod(form, charset);
            }
        } catch (CharacterEncodingValidationExceptionMod e) {
            for (EncodingError ignored : e.getSubjectErrors()) {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.charset.subject"));
			}
            for (EncodingError mailingComponent : e.getFailedMailingComponents()) {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.charset.component", mailingComponent.getStrWithError(), mailingComponent.getLine()));
			}
            for (EncodingError dynTag : e.getFailedDynamicTags()) {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.charset.content", dynTag.getStrWithError(), dynTag.getLine()));
			}
			return false;
        }
        return true;
    }

	public boolean isMailingEditable(ComMailingContentForm aForm, HttpServletRequest req) {
		if (maildropService.isActiveMailing(aForm.getMailingID(), AgnUtils.getCompanyID(req))) {
			return AgnUtils.allowed(req, Permission.MAILING_CONTENT_CHANGE_ALWAYS);
		}
		return true;
	}

	private boolean generateTextContent(HttpServletRequest req, ComMailingContentForm aForm) {
		try {
			Mailing mailing = mailingDao.getMailing(aForm.getMailingID(), AgnUtils.getCompanyID(req));
			if (mailing.getId() > 0) {
				mailingContentService.generateTextContent(mailing);
				saveContentChangesInDB(req, aForm, mailing);
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Error occurred: " + e.getMessage(), e);
			return false;
		}
		return true;
	}

	private boolean doSaveChanges(ComMailingContentForm form, HttpServletRequest req, ActionMessages messages, ActionMessages errors) throws Exception {
		Mailing mailing = mailingDao.getMailing(form.getMailingID(), AgnUtils.getCompanyID(req));
		
		if (mailing != null) {
            if (mailing.getId() == 0 || mailing.getCompanyID() == 0) {
                logger.error("Mailing with zero IDs found. Mailing ID is: " + mailing.getId() + ", Company ID is: " +
						mailing.getCompanyID() + ", Mailing ID stored in Form is: " + form.getMailingID());
            }

			validateTextBlockEncoding(form, errors);
            if (!validateTextBlockTags(form, req, messages, errors)) {
            	return false;
			}

			DynamicTag tag = mailing.getDynamicTagById(form.getDynNameID());

			Map<Integer, DynamicTagContent> contentOld = null;

			if (tag != null) {
				contentOld = tag.getDynContent();
				tag.setDynContent(form.getContent());
				tag.setDynInterestGroup(form.getDynInterestGroup());
				tag.setDisableLinkExtension(form.isDisableLinkExtension());

				switch (form.getAction()) {
				case ACTION_SAVE_TEXTBLOCK:
				case ACTION_SAVE_TEXTBLOCK_AND_BACK:
					if (StringUtils.isNotBlank(form.getNewContent())) {
						addNewTextBlock(form, tag);
					}
					break;

				case ACTION_ADD_TEXTBLOCK:
				case ACTION_ADD_TEXTBLOCK_AND_BACK:
					addNewTextBlock(form, tag);
					break;

				case ACTION_DELETE_TEXTBLOCK:
					// Bug-Fix
					// 1st delete content from database
					// Jira AGNEMM-319: Deleting content block *after* saving
					// the undo data, otherwise the undo data won't contain the
					// deleted block!
					// ((ComMailingDaoImpl)mDao).deleteContentFromMailing(aMailing,
					// aForm.getContentID());
					// 2nd delete content from list
					tag.removeContent(form.getContentID());
					break;

				case ACTION_CHANGE_ORDER_UP:
					tag.moveContentDown(form.getContentID(), -1);
					break;

				case ACTION_CHANGE_ORDER_DOWN:
					tag.moveContentDown(form.getContentID(), 1);
					break;

				case ACTION_CHANGE_ORDER_TOP:
					for (int numOfContent = 0; numOfContent < tag.getDynContentCount(); numOfContent++) {
						tag.moveContentDown(form.getContentID(), -1);
					}
					break;

				case ACTION_CHANGE_ORDER_BOTTOM:
					for (int numOfContent = 0; numOfContent < tag.getDynContentCount(); numOfContent++) {
						tag.moveContentDown(form.getContentID(), 1);
					}
					break;
				}

                fixAngTagsQuotes(form);
			}

			try {
                List<EmmAction> actions = actionDao.getEmmActions(AgnUtils.getCompanyID(req));
                mailing.setPossibleActions(actions);

                ComAdmin admin = AgnUtils.getAdmin(req);
				mailing.buildDependencies(false, null, getApplicationContext(req), messages, errors, admin);
			} catch (Exception e) {
				logger.error("Error building dependencies", e);
				if (errors != null) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid.dependencies", e.getMessage()));
				} else {
					throw e;
				}
			}
			
            mailingBaseService.saveMailingWithUndo(mailing, AgnUtils.getAdminId(req), AgnUtils.allowed(req, Permission.MAILING_TRACKABLELINKS_NOCLEANUP));

			// If user requested a target block to be deleted, do it now, after
			// saving the undo data
			if (form.getAction() == ACTION_DELETE_TEXTBLOCK) {
				mailingDao.deleteContentFromMailing(mailing, form.getContentID());
			}

            mailingDao.updateStatus(mailing.getId(), "edit");
            writeTextblocksChangeLog(form, AgnUtils.getAdmin(req), mailing, contentOld);
            logger.info("change content of mailing: " + form.getMailingID());
			return true;
		} else {
            logger.warn(String.format("unable to change content of mailing: %d. Mailing is null.", form.getMailingID()));
			return false;
        }
	}

	private DynamicTagContent addNewTextBlock(ComMailingContentForm form, DynamicTag tag) {
		DynamicTagContent block = dynamicTagContentFactory.newDynamicTagContent();

		block.setCompanyID(tag.getCompanyID());
		block.setDynContent(form.getNewContent());
		block.setTargetID(form.getNewTargetID());
		block.setDynOrder(tag.getMaxOrder() + 1);
		block.setDynNameID(tag.getId());
		block.setMailingID(tag.getMailingID());

		tag.addContent(block);

		// Add the new text block to a content map
		form.getContent().put(block.getDynOrder(), block);

		// Reset new text block controls
		form.setNewContent(StringUtils.EMPTY);
		form.setNewTargetID(0);

		return block;
    }

	private void saveContentChangesInDB(HttpServletRequest req, ComMailingContentForm aForm, Mailing aMailing) {
		try {
			// Rebuild dependencies to hide unused content blocks
			List<String> dynNamesForDeletion = new Vector<>();

			aMailing.buildDependencies(true, dynNamesForDeletion, getApplicationContext(req));

			// Mark names in dynNamesForDeletion as "deleted"
			dynamicTagDao.markNamesAsDeleted(aMailing.getId(), dynNamesForDeletion);
		} catch (Exception e) {
			logger.error("Error building dependencies", e);
		}
        mailingBaseService.saveMailingWithUndo(aMailing, AgnUtils.getAdminId(req), false);
		logger.info("change content of mailing: " + aForm.getMailingID());
	}

	private boolean calculateShowingHTMLEditor(String dynTargetName, Mailing mailing, HttpServletRequest req) {
		MailingComponent htmlTemplate = mailing.getHtmlTemplate();
		if (htmlTemplate == null) {
			return false;
		}

		String htmlEmmBlock = htmlTemplate.getEmmBlock();
		try {
			Vector<String> tagsInHTMLTemplate = mailing.findDynTagsInTemplates(htmlEmmBlock, getApplicationContext(req));
			return tagsInHTMLTemplate.contains(dynTargetName);
		} catch (Exception e) {
			logger.error("Error occurred: " + e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Format and write to user event log message about textblock
	 *
	 * @param aForm
	 *            - MailingContentForm
	 * @param admin
	 *            - Admin
	 * @param aMailing
	 *            - Mailing which is container for textblock
	 * @param contentOld
	 * 			  - Texblocks before changes.
	 */
	private void writeTextblocksChangeLog(ComMailingContentForm aForm, ComAdmin admin, Mailing aMailing,
											Map<Integer, DynamicTagContent> contentOld) {
		String entityName = aMailing.isIsTemplate() ? "template" : "mailing";
		DynamicTag aTag = aMailing.getDynamicTagById(aForm.getDynNameID());
		String blockName = aTag.getDynName();
		
		final String formatPattern = "%s (%d) %s %s %s (%d)";
		final Object[] formatParameter = new Object[] { blockName, null, null, entityName, aMailing.getShortname(), aMailing.getId() };
		
		Map<Integer, DynamicTagContent> contentNew = aForm.getContent();

		switch (aForm.getAction()) {
			case ACTION_ADD_TEXTBLOCK:
			case ACTION_ADD_TEXTBLOCK_AND_BACK:
				contentNew.forEach((index, block) -> {
					if (MapUtils.isEmpty(contentOld) || !contentOld.containsKey(index)) {
						formatParameter[1] = block.getId();
						formatParameter[2] = "in the";
						
						writeUserActivityLog(admin, "create textblock", String.format(formatPattern, formatParameter));
					}
				});
				break;

			case ACTION_DELETE_TEXTBLOCK:
				formatParameter[1] = aForm.getContentID();
				formatParameter[2] = "from";
				
				writeUserActivityLog(admin, "delete textblock", String.format(formatPattern, formatParameter));
				break;

			case ACTION_CHANGE_ORDER_UP:
				formatParameter[1] = aForm.getContentID();
				formatParameter[2] = "from";
				
				writeUserActivityLog(admin, "do move textblock up", String.format(formatPattern, formatParameter));
				break;

			case ACTION_CHANGE_ORDER_DOWN:
				formatParameter[1] = aForm.getContentID();
				formatParameter[2] = "from";
				
				writeUserActivityLog(admin, "do move textblock down", String.format(formatPattern, formatParameter));
				break;

			case ACTION_CHANGE_ORDER_TOP:
				formatParameter[1] = aForm.getContentID();
				formatParameter[2] = "from";
				
				writeUserActivityLog(admin, "do move textblock top", String.format(formatPattern, formatParameter));
				break;

			case ACTION_CHANGE_ORDER_BOTTOM:
				formatParameter[1] = aForm.getContentID();
				formatParameter[2] = "from";
				
				writeUserActivityLog(admin, "do move textblock bottom", String.format(formatPattern, formatParameter));
				break;

			case ACTION_SAVE_TEXTBLOCK:
			case ACTION_SAVE_TEXTBLOCK_AND_BACK:
				if (MapUtils.isNotEmpty(contentOld)) {
					contentNew.forEach((index, blockNew) -> {
						DynamicTagContent blockOld = contentOld.get(index);
						if (blockOld != null && !StringUtils.equals(blockOld.getDynContent(), blockNew.getDynContent())) {
							formatParameter[1] = blockNew.getId();
							formatParameter[2] = "from";
							
							writeUserActivityLog(admin, "edit textblock content", String.format(formatPattern, formatParameter));
						} else if(blockOld == null) {
							formatParameter[1] = blockNew.getId();
							formatParameter[2] = "in the";
							
							writeUserActivityLog(admin, "create textblock", String.format(formatPattern, formatParameter));
						}
					});
				} else {
					contentNew.forEach((index, block) -> {
						if (MapUtils.isEmpty(contentOld) || !contentOld.containsKey(index)) {
							formatParameter[1] = block.getId();
							formatParameter[2] = "in the";
							
							writeUserActivityLog(admin, "create textblock", String.format(formatPattern, formatParameter));
						}
					});
				}
				
				break;
		}
	}

	private void fixAngTagsQuotes(ComMailingContentForm aForm) {
		Map<Integer, DynamicTagContent> content = aForm.getContent();
		for (DynamicTagContent dynContent : content.values()) {
			String text = dynContent.getDynContent();
			String fixedText = AgnTagUtils.unescapeAgnTags(text);
			dynContent.setDynContent(fixedText);
		}
	}

	private void appendErrorsToList(List<String[]> errorReports, String blockName, StringBuffer templateReport) {
		for (Entry<String, String> entry : PreviewHelper.getTagsWithErrors(templateReport).entrySet()) {
			errorReports.add(new String[]{blockName, entry.getKey(), entry.getValue()});
		}

		for (String error : PreviewHelper.getErrorsWithoutATag(templateReport)) {
			errorReports.add(new String[]{blockName, "", error});
		}
	}

	private void appendErrorToList(List<String[]> errorReports, String blockName, String erroneousText, String errorDescription) {
		errorReports.add(new String[]{ blockName, erroneousText, errorDescription });
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

	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
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
	 * @param targetDao
	 *            DAO accessing target groups
	 */
	@Required
	public void setTargetDao(ComTargetDao targetDao) {
		this.targetDao = targetDao;
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
    public void setLinkService(LinkService linkService) {
        this.linkService = linkService;
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
