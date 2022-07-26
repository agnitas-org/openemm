/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.web;

import static org.agnitas.util.UserActivityUtil.addChangedFieldLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.agnitas.actions.EmmAction;
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.beans.impl.ImportProfileImpl;
import org.agnitas.dao.EmmActionDao;
import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.emm.core.autoimport.bean.AutoImportLight;
import org.agnitas.emm.core.autoimport.service.AutoImportService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.ImportProfileService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.ImportUtils;
import org.agnitas.util.importvalues.Charset;
import org.agnitas.util.importvalues.CheckForDuplicates;
import org.agnitas.util.importvalues.DateFormat;
import org.agnitas.util.importvalues.ImportMode;
import org.agnitas.util.importvalues.MailType;
import org.agnitas.util.importvalues.NullValuesAction;
import org.agnitas.util.importvalues.Separator;
import org.agnitas.util.importvalues.TextRecognitionChar;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.ImportProfileForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ImportProcessAction;
import com.agnitas.beans.ProfileField;
import com.agnitas.dao.ImportProcessActionDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ComColumnInfoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Action that handles import profile actions: view, edit, remove, list,
 * manage gender mappings.
 */
public class ImportProfileAction extends StrutsActionBase {
	/** The logger. */
    private static final transient Logger logger = LogManager.getLogger(ImportProfileAction.class);

    public static final int ACTION_SET_DEFAULT = ACTION_LAST + 3;

    public static final String IMPORT_PROFILE_ERRORS_KEY = "import-profile-errors";
    public static final String IMPORT_PROFILE_ID_KEY = "import-profile-id";

    private static final String UNKNOWN_ACTION = "<unknown action>";
    private static final String NONE = "none";

    protected ConfigService configService;
	protected ImportProcessActionDao importProcessActionDao = null;
	protected EmmActionDao emmActionDao = null;
	
	protected AdminService adminService;
	
	private ImportProfileService importProfileService;
	private ImportRecipientsDao importRecipientsDao;
	private AutoImportService autoImportService = null;
	private ComColumnInfoService columnInfoService;
	private WebStorage webStorage;
    private MailinglistApprovalService mailinglistApprovalService;
    
    @Required
    public final void setMailinglistApprovalService(final MailinglistApprovalService service) {
    	this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }
    
    @Required
    public final void setAdminService(final AdminService service) {
    	this.adminService = Objects.requireNonNull(service, "Admin service is null");
    }

	@Required
    public void setImportProfileService(ImportProfileService importProfileService) {
        this.importProfileService = importProfileService;
    }

	// Not required by intention for OpenEMM
	public void setImportProcessActionDao(ImportProcessActionDao importProcessActionDao) {
		this.importProcessActionDao = importProcessActionDao;
	}

	// Not required by intention for OpenEMM
	public void setEmmActionDao(EmmActionDao emmActionDao) {
		this.emmActionDao = emmActionDao;
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setImportRecipientsDao(ImportRecipientsDao importRecipientsDao) {
		this.importRecipientsDao = importRecipientsDao;
	}
	
	public void setAutoImportService(AutoImportService autoImportService) {
		this.autoImportService = autoImportService;
	}

	@Required
	public void setColumnInfoService(ComColumnInfoService columnInfoService) {
		this.columnInfoService = columnInfoService;
	}

	@Required
    public void setWebStorage(WebStorage webStorage) {
	    this.webStorage = webStorage;
    }

	@Override
    public String subActionMethodName(int subAction) {
        if (subAction == ACTION_SET_DEFAULT) {
            return "set_default";
        }
        return super.subActionMethodName(subAction);
    }

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     * <br>
	 * ACTION_LIST: loads list of profile fields and default profile field into request and forwards to profile
     *     field list page.
	 * <br><br>
     * ACTION_VIEW: loads data of chosen profile field into form and forwards to profile field view page.
	 * <br><br>
	 * ACTION_SAVE: checks, if profile field with entered name already exists. Saves profile field data or creates
     *     a new one and forwards to profile field view page.
	 * <br><br>
     * ACTION_NEW: creates empty instance of profile field and forwards to profile field view page.<br>
     * Supported modes for import: <br>
     *      ImportMode.ADD (adds only new recipients)
     *          requires "import.mode.add" permission<br>
     *      ImportMode.ADD_AND_UPDATE (adds new recipients and update existing recipients)
     *          requires "import.mode.add_update" permission<br>
     *      ImportMode.UPDATE (only updates existing recipients)
     *          requires "import.mode.only_update" permission<br>
     *      ImportMode.MARK_OPT_OUT (marks recipients as Opt Out)
     *          requires "import.mode.unsubscribe" permission<br>
     *      ImportMode.MARK_BOUNCED (marks recipients as Bounced)
     *          requires "import.mode.bounce" permission<br>
     *      ImportMode.TO_BLACKLIST (adds recipients to blacklist)
     *          requires "import.mode.blacklist" permission
	 * <br><br>
     * ACTION_SET_DEFAULT: saves the default profile id for admin in database and in session.
     *     Forwards to profile field list page.
	 * <br><br>
	 * ACTION_CONFIRM_DELETE: loads profile field data and forwards to jsp with question to confirm deletion.
	 * <br><br>
	 * ACITON_DELETE: deletes profile field and forwards to profile field list page.
	 * <br><br>
     * @param mapping The ActionMapping used to select this instance
     * @param form    The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing<br>
     *     If the request parameter "setDefault" is set - changes action to ACTION_SET_DEFAULT.<br>
     * @param response     The HTTP response we are creating
     * @throws java.io.IOException            if an input/output error occurs
     * @throws jakarta.servlet.ServletException if a servlet exception occurs
     * @return destination specified in struts-config.xml to forward to next jsp
     */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ComAdmin admin = AgnUtils.getAdmin(request);

        assert (admin != null);
        
        // Validate the request parameters specified by the user
        ImportProfileForm aForm;
        ActionMessages errors = new ActionMessages();
        ActionMessages messages = new ActionMessages();
        ActionForward destination = null;

        if (!AgnUtils.isUserLoggedIn(request)) {
            return mapping.findForward("logon");
        }
        
        int companyId = admin.getCompanyID();

        if (form != null) {
            aForm = (ImportProfileForm) form;
        } else {
            aForm = new ImportProfileForm();
        }

        if (logger.isInfoEnabled()) {
        	logger.info("Action: " + aForm.getAction());
        }

        if (request.getSession().getAttribute(IMPORT_PROFILE_ID_KEY) != null) {
            errors = (ActionMessages) request.getSession().getAttribute(IMPORT_PROFILE_ERRORS_KEY);
            int profileId = (Integer) request.getSession().getAttribute(IMPORT_PROFILE_ID_KEY);
            aForm.setProfileId(profileId);
            aForm.setAction(ACTION_VIEW);
            request.getSession().removeAttribute(IMPORT_PROFILE_ERRORS_KEY);
            request.getSession().removeAttribute(IMPORT_PROFILE_ID_KEY);
        }

        if (AgnUtils.parameterNotEmpty(request, "setDefault")) {
            aForm.setAction(ACTION_SET_DEFAULT);
        }

        try {
            switch (aForm.getAction()) {
                case ImportProfileAction.ACTION_LIST:
                    destination = mapping.findForward("list");
                    aForm.clearLists();
                    aForm.setAction(ImportProfileAction.ACTION_LIST);
                    break;

                case ImportProfileAction.ACTION_VIEW:
                    aForm.reset(mapping, request);
                    if (importProcessActionDao != null) {
                    	aForm.setImportProcessActions(importProcessActionDao.getAvailableImportProcessActions(companyId));
                    }

                    loadImportProfile(aForm);
                    if (emmActionDao != null) {
                    	aForm.setActionsForNewRecipients(emmActionDao.getEmmActionsByOperationType(companyId, false, ActionOperationType.SUBSCRIBE_CUSTOMER, ActionOperationType.SEND_MAILING));
                    }
                    aForm.setAction(ImportProfileAction.ACTION_SAVE);
                    destination = mapping.findForward("view");

                    writeUserActivityLog(AgnUtils.getAdmin(request), "view import profile", getImportProfileDescription(aForm.getProfile()));
                    break;

                case ImportProfileAction.ACTION_SAVE:
                	if (AgnUtils.parameterNotEmpty(request, "save") && (isValidImportToSave(admin, aForm))) {
                		if (aForm.getProfile().getImportProcessActionID() > 0 && !admin.permissionAllowed(Permission.IMPORT_PREPROCESSING)) {
                			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.notAllowed"));
                		} else if (aForm.getProfile().isAutoMapping() && aForm.getProfile().isNoHeaders()) {
                			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.automapping.missing.header"));
                		} else if ((aForm.getProfile().getImportMode() == ImportMode.ADD_AND_UPDATE.getIntValue() || aForm.getProfile().getImportMode() == ImportMode.UPDATE.getIntValue())
                				&& aForm.getProfile().getCheckForDuplicates() != CheckForDuplicates.COMPLETE.getIntValue()) {
                			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.updateNeedsCheckForDuplicates"));
                		} else {
		                    if (!checkErrorsOnSave(aForm, request, errors)) {
		                        saveImportProfile(aForm, request);
		                        loadImportProfile(aForm);
		                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
		                        if (aForm.getProfile().getImportMode() == ImportMode.REACTIVATE_BOUNCED.getIntValue()) {
		                        	messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage(I18nString.getLocaleString("warning.import.mode.bounceractivation", AgnUtils.getLocale(request)), false));
		                        }
		                        
		                        checkProfileKeyColumnIndexed(messages, errors, aForm.getProfile());
		                    }
                		}
                	} else {
                        loadImportProfile(aForm);
                    }
                	if (emmActionDao != null) {
                		aForm.setActionsForNewRecipients(emmActionDao.getEmmActionsByOperationType(companyId, false, ActionOperationType.SUBSCRIBE_CUSTOMER, ActionOperationType.SEND_MAILING));
                	}
                    if (importProcessActionDao != null) {
                    	aForm.setImportProcessActions(importProcessActionDao.getAvailableImportProcessActions(companyId));
                    }
                    aForm.setAction(ImportProfileAction.ACTION_SAVE);
                    request.setAttribute("isGenderSectionFocused", Boolean.valueOf(request.getParameter("isGenderSectionFocused")));
                    destination = mapping.findForward("view");
                    break;

                case ImportProfileAction.ACTION_NEW:
                    aForm.reset(mapping, request);
                    aForm.setAction(ImportProfileAction.ACTION_SAVE);
                    createEmptyProfile(aForm, request);
                    if (importProcessActionDao != null) {
                    	aForm.setImportProcessActions(importProcessActionDao.getAvailableImportProcessActions(companyId));
                    }
                    destination = mapping.findForward("view");
                    break;

                case ImportProfileAction.ACTION_SET_DEFAULT:
                    setDefaultProfile(aForm, request);
                    aForm.setAction(ImportProfileAction.ACTION_LIST);
                    destination = mapping.findForward("list");

                    messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
                    break;

                case ImportProfileAction.ACTION_CONFIRM_DELETE:
                    loadImportProfile(aForm);
                    aForm.setAction(ImportProfileAction.ACTION_DELETE);
                    destination = mapping.findForward("delete");
                    break;

                case ImportProfileAction.ACTION_DELETE:
                    if (request.getParameter("kill") != null) {
                        removeProfile(aForm, request, errors);
                        if (errors.isEmpty()) {
	                        aForm.setAction(ImportProfileAction.ACTION_LIST);
	                        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
	                        destination = mapping.findForward("list");
                        } else {
                        	destination = mapping.findForward("view");
                        }
                    } else {
                    	destination = mapping.findForward("list");
                    }
                    break;

                default:
                    aForm.setAction(ImportProfileAction.ACTION_LIST);
                    destination = mapping.findForward("list");
            }

        } catch (Exception e) {
            logger.error("execute: " + e, e);
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
        }

        if (destination != null && "list".equals(destination.getName())) {
            try {
                // if we will go to list page we need to load profiles list
                // and load the default profile for current admin to show
                // that on list-page
                FormUtils.syncNumberOfRows(webStorage, WebStorage.IMPORT_PROFILE_OVERVIEW, aForm);

                request.setAttribute("profileList", getProfileList(request));
                aForm.setDefaultProfileId(AgnUtils.getAdmin(request).getDefaultImportProfileID());
            } catch (Exception e) {
                logger.error("getCampaignList: " + e, e);
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
            }
        }
        
        if (destination != null && StringUtils.equalsIgnoreCase("view", destination.getName())) {
        	setUpViewPageVariables(request, aForm, admin);
        }

        // Report any errors we have discovered back to the original form
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
        }

        if (!messages.isEmpty()) {
            saveMessages(request, messages);
        }

        return destination;
    }

    protected boolean isValidImportToSave(ComAdmin admin, ImportProfileForm form) throws Exception {
        return isUserHasPermissionForSelectedMode(admin, form)
                && (form.getProfile().getImportProcessActionID() == 0 || admin.permissionAllowed(Permission.IMPORT_PREPROCESSING))
                && (!"json".equalsIgnoreCase(form.getProfile().getDatatype()));
    }

    protected boolean isUserHasPermissionForSelectedMode(ComAdmin admin, ImportProfileForm aForm) throws Exception {
        return admin.permissionAllowed(Permission
                .getPermissionByToken(ImportMode.getFromInt(aForm.getProfile().getImportMode()).getMessageKey()));
	}

    private List<Integer> getAllowedModesForAllMailinglists() {
        return Stream.of(
                    ImportMode.ADD,
                    ImportMode.ADD_AND_UPDATE,
                    ImportMode.UPDATE,
                    ImportMode.MARK_OPT_OUT,
                    ImportMode.MARK_BOUNCED,
                    ImportMode.REACTIVATE_BOUNCED,
                    ImportMode.MARK_SUSPENDED,
                    ImportMode.ADD_AND_UPDATE_FORCED,
                    ImportMode.REACTIVATE_SUSPENDED)
                .map(ImportMode::getIntValue)
                .collect(Collectors.toList());
    }

    private void setUpViewPageVariables(HttpServletRequest request, ImportProfileForm form, ComAdmin admin) throws Exception {
		form.setAvailableImportProfileFields(getAvailableImportProfileFields(admin));
		form.setAvailableMailinglists(mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
		
		request.setAttribute("isCustomerIdImportNotAllowed", !admin.permissionAllowed(Permission.IMPORT_CUSTOMERID));

        List<Integer> genders = new ArrayList<>();
    		
        int maxGenderValue;
        if (admin.permissionAllowed(Permission.RECIPIENT_GENDER_EXTENDED)) {
            maxGenderValue = ConfigService.MAX_GENDER_VALUE_EXTENDED;
        } else {
            maxGenderValue = ConfigService.MAX_GENDER_VALUE_BASIC;
        }
        
        for (int i = 0; i <= maxGenderValue; i++) {
            genders.add(i);
        }

        setImportModes(form, request);
        
        request.setAttribute("availableGenderIntValues", genders);
        request.setAttribute("allowedModesForAllMailinglists", getAllowedModesForAllMailinglists());
        request.setAttribute("mediatypes", MediaTypes.valuesSortedByCode());
        request.setAttribute("isUserHasPermissionForSelectedMode", isUserHasPermissionForSelectedMode(admin, form));
    }

	protected boolean checkErrorsOnSave(ImportProfileForm aForm, HttpServletRequest request, ActionMessages errors) throws InstantiationException, IllegalAccessException {
        boolean hasErrors = false;
        if (profileNameIsDuplicate(aForm, request)) {
            errors.add("shortname", new ActionMessage("error.import.duplicate_profile_name"));
            hasErrors = true;
        }
        
        boolean isCustomerIdImported = false;
		for (ColumnMapping mapping : aForm.getProfile().getColumnMapping()) {
			if ("customer_id".equalsIgnoreCase(mapping.getDatabaseColumn())) {
				isCustomerIdImported = true;
			}
		}
		if (isCustomerIdImported && (aForm.getProfile().getImportMode() == ImportMode.ADD.getIntValue() || aForm.getProfile().getImportMode() == ImportMode.ADD_AND_UPDATE.getIntValue())) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.import.customerid_insert"));
            hasErrors = true;
		}
		
        return hasErrors;
    }

    /**
     * Loads the list of allowed import modes for current user.
     *
     * @param request request
     * @throws Exception
     */
    private List<ImportMode> getAvailableImportModes(HttpServletRequest request) throws Exception {
        List<ImportMode> allowedModes = new ArrayList<>();
        for (ImportMode mode : ImportMode.values()) {
            if (AgnUtils.allowed(request, Permission.getPermissionsByToken(mode.getMessageKey()))) {
                allowedModes.add(mode);
            }
        }
        return allowedModes;
    }
    
    private void setImportModes(ImportProfileForm aForm, HttpServletRequest request) throws Exception {
        Set<ImportMode> modes = new TreeSet<>(Comparator.comparingInt(ImportMode::getIntValue));
        modes.addAll(getAvailableImportModes(request));
        if (aForm.getProfileId() != 0) {
            modes.add(ImportMode.getFromInt(aForm.getProfile().getImportMode()));
        }
        aForm.setImportModes(modes.toArray(new ImportMode[0]));
    }

    /**
     * Method checks if there is already a profile with a name user entered creating new profile
     *
     * @param aForm   form
     * @param request request
     * @return true if the profile with such name alreay exists, false if not
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private boolean profileNameIsDuplicate(ImportProfileForm aForm, HttpServletRequest request)
            throws IllegalAccessException, InstantiationException {
        String profileName = aForm.getProfile().getName();
        int profileId = aForm.getProfileId();
        List<ImportProfile> importProfileList = getProfileList(request);
        for (ImportProfile importProfile : importProfileList) {
            if (importProfile.getName().equals(profileName) && importProfile.getId() != profileId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Saves the default profile id for admin that is set on overview-page
     *
     * @param aForm   a form
     * @param request request
     * @throws Exception
     */
    private void setDefaultProfile(ImportProfileForm aForm, HttpServletRequest request) throws Exception {
        int defaultProfileId = aForm.getDefaultProfileId();
        ComAdmin admin = AgnUtils.getAdmin(request);
        admin.setDefaultImportProfileID(defaultProfileId);

        final ComAdmin adminFromDao = adminService.getAdmin(admin.getAdminID(), admin.getCompanyID());
        adminFromDao.setDefaultImportProfileID(defaultProfileId);
        adminService.save(adminFromDao);
    }

    /**
     * Creates empty profile for displaying on view-page when user wants to
     * create new import profile
     *
     * @param aForm   a form
     * @param request request
     */
    private void createEmptyProfile(ImportProfileForm aForm, HttpServletRequest request) {
        ComAdmin admin = AgnUtils.getAdmin(request);
        ImportProfile newProfile = new ImportProfileImpl();

        if (Objects.nonNull(admin)) {
            newProfile.setAdminId(admin.getAdminID());
            newProfile.setCompanyId(AgnUtils.getCompanyID(request));
            newProfile.setKeyColumn("email");
            newProfile.setCheckForDuplicates(1);
            newProfile.setDefaultMailType(1);
            aForm.setProfile(newProfile);
            aForm.setProfileId(0);
        }
    }

    /**
     * Removes profile from system with all its data using Dao
     *
     * @param aForm a form
     */
    public void removeProfile(ImportProfileForm aForm, HttpServletRequest req, ActionMessages errors) {
        ImportProfile importProfile = importProfileService.getImportProfileById(aForm.getProfileId());
        
        if (importProfile != null) {
        	List<AutoImportLight> autoImportsList = autoImportService == null ? null : autoImportService.getListAutoImportsByProfileId(importProfile.getId());
        	if (autoImportsList != null && autoImportsList.size() > 0) {
        		AutoImportLight autoImport = autoImportsList.get(0);
        		errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.profileStillUsed", autoImport.getShortname() + " (ID: " + autoImport.getAutoImportId() + ")"));
        	} else {
		        importProfileService.deleteImportProfileById(aForm.getProfileId());
		
		        writeUserActivityLog(AgnUtils.getAdmin(req), "delete import profile", getImportProfileDescription(importProfile));
        	}
        }
    }

    /**
     * @param request request
     * @return list of import profiles for overview page with current company id
     */
    private List<ImportProfile> getProfileList(HttpServletRequest request){
        return importProfileService.getImportProfilesByCompanyId(AgnUtils.getCompanyID(request));
    }

    /**
     * Saves import profile (or creats new if its' "New import profile" page)
     *
     * @param aForm a form
     */
    private void saveImportProfile(ImportProfileForm aForm, HttpServletRequest request) {
    	ComAdmin admin = AgnUtils.getAdmin(request);
    	
        ImportProfile importProfile = aForm.getProfile();
        importProfile.setId(aForm.getProfileId());
        importProfile.setCompanyId(AgnUtils.getCompanyID(request));
        importProfile.setAdminId(AgnUtils.getAdminId(request));
        if (admin.permissionAllowed(Permission.MAILINGLIST_SHOW) && importProfile.getActionForNewRecipients() <= 0) {
            importProfile.setMailinglists(new ArrayList<>(aForm.getMailinglists()));
        }
        importProfile.setMediatypes(aForm.getMediatypes());
        
        setupGenderMappings(importProfile);

        if (aForm.getProfileId() != 0) {
            ImportProfile oldImportProfile = importProfileService.getImportProfileById(aForm.getProfileId());
            if (!admin.permissionAllowed(Permission.MAILINGLIST_SHOW)) {
                importProfile.setMailinglistsAll(oldImportProfile.isMailinglistsAll());
                importProfile.setMailinglists(oldImportProfile.getMailinglistIds());
            }
            importProfileService.saveImportProfileWithoutColumnMappings(importProfile);

            writeImportChangeLog(oldImportProfile, importProfile, aForm, admin);
        } else {
            importProfileService.saveImportProfile(importProfile);
            aForm.setProfileId(aForm.getProfile().getId());

            writeUserActivityLog(admin, "create import profile", getImportProfileDescription(importProfile));
        }
    }

    private void setupGenderMappings(ImportProfile importProfile) {
        importProfile.getGenderMappingsToSave().entrySet().stream()
                .filter(entry -> NumberUtils.isDigits(String.valueOf(entry.getKey())))
                .forEach(mapping -> {
                    int intValue = NumberUtils.toInt(String.valueOf(mapping.getKey()));
                    String textValuesStr = mapping.getValue();
                    Arrays.stream(textValuesStr.split(",")).map(String::trim).filter(v -> !v.isEmpty()).distinct()
                            .forEach(textVal -> importProfile.getGenderMapping().put(textVal, intValue));
                });
    }

    private void writeImportChangeLog(ImportProfile oldImport, ImportProfile newImport, ImportProfileForm form, ComAdmin admin) {
        StringBuilder logDescription = new StringBuilder();
        logDescription.append(addChangedFieldLog("Import name", newImport.getName(), oldImport.getName()));
        try {
            String newSeparator = String.valueOf(Separator.getSeparatorById(newImport.getSeparator()).getValueChar());
            String oldSeparator = String.valueOf(Separator.getSeparatorById(oldImport.getSeparator()).getValueChar());
            logDescription.append(addChangedFieldLog("Separator", newSeparator, oldSeparator));
        } catch (Exception e) {
            //Should never happen
        }
        try {
            String newCharset = Charset.getCharsetById(newImport.getCharset()).getCharsetName();
            String oldCharset = Charset.getCharsetById(oldImport.getCharset()).getCharsetName();
            logDescription.append(addChangedFieldLog("Charset", newCharset, oldCharset));
        } catch (Exception e) {
            //Should never happen
        }
        try {
            String newTextRecognitionChar = TextRecognitionChar.getTextRecognitionCharById(newImport.getTextRecognitionChar()).getValueString();
            String oldTextRecognitionChar = TextRecognitionChar.getTextRecognitionCharById(oldImport.getTextRecognitionChar()).getValueString();
            logDescription.append(addChangedFieldLog("Recognition character", newTextRecognitionChar, oldTextRecognitionChar));
        } catch (Exception e) {
            //Should never happen
        }
        try {
            String newDateFormat = DateFormat.getDateFormatById(newImport.getDateFormat()).getValue();
            String oldDateFormat = DateFormat.getDateFormatById(oldImport.getDateFormat()).getValue();
            logDescription.append(addChangedFieldLog("Date format", newDateFormat, oldDateFormat));
        } catch (Exception e) {
            //Should never happen
        }
        logDescription.append(addChangedFieldLog("Decimal separator",
                String.valueOf(newImport.getDecimalSeparator()),
                String.valueOf(oldImport.getDecimalSeparator())));
        logDescription.append(addChangedFieldLog("No csv headers", newImport.isNoHeaders(), oldImport.isNoHeaders()));
        logDescription.append(addChangedFieldLog("Zip password", newImport.getZipPassword(), oldImport.getZipPassword()));
        try {
            String newImportMode = ImportMode.getFromInt(newImport.getImportMode()).getMessageKey();
            String oldImportMode = ImportMode.getFromInt(oldImport.getImportMode()).getMessageKey();
            logDescription.append(addChangedFieldLog("Mode", newImportMode, oldImportMode));
        } catch (Exception e) {
            //Should never happen
        }
        try {
            String newNullValueAction = NullValuesAction.getFromInt(newImport.getNullValuesAction()).getMessageKey();
            String oldNullValueAction = NullValuesAction.getFromInt(oldImport.getNullValuesAction()).getMessageKey();
            logDescription.append(addChangedFieldLog("Null values action", newNullValueAction, oldNullValueAction));
        } catch (Exception e) {
            //Should never happen
        }
        logDescription.append(addChangedFieldLog("First key column", newImport.getFirstKeyColumn(), oldImport.getFirstKeyColumn()));
        try {
            String newDuplicatesCheck = CheckForDuplicates.getFromInt(newImport.getCheckForDuplicates()).getMessageKey();
            String oldDuplicatesCheck = CheckForDuplicates.getFromInt(oldImport.getCheckForDuplicates()).getMessageKey();
            logDescription.append(addChangedFieldLog("Duplicates check", newDuplicatesCheck, oldDuplicatesCheck));
        } catch (Exception e) {
            //Should never happen
        }
        try {
            String newDefaultMailingType = MailType.getFromInt(newImport.getDefaultMailType()).getMessageKey();
            String oldDefaultMailingType = MailType.getFromInt(oldImport.getDefaultMailType()).getMessageKey();
            logDescription.append(addChangedFieldLog("Default mailing type", newDefaultMailingType, oldDefaultMailingType));
        } catch (Exception e) {
            //Should never happen
        }
        logDescription.append(getPreImportActionLog(oldImport.getImportProcessActionID(), newImport.getImportProcessActionID(), admin.getCompanyID()));
        logDescription.append(addChangedFieldLog("Action for new recipients",
                getActionForNewRecipientsName(newImport.getActionForNewRecipients(), admin.getCompanyID()),
                getActionForNewRecipientsName(oldImport.getActionForNewRecipients(),admin.getCompanyID())));
        logDescription.append(addChangedFieldLog("Report mail", newImport.getMailForReport(), oldImport.getMailForReport()))
                .append(addChangedFieldLog("Error mail", newImport.getMailForError(), oldImport.getMailForError()))
                .append(addChangedFieldLog("Handling of duplicates",
                        formatBoolean(newImport.getUpdateAllDuplicates()),
                        formatBoolean(oldImport.getUpdateAllDuplicates())))
                .append(addChangedFieldLog("Automatic mapping", newImport.isAutoMapping(), oldImport.isAutoMapping()))
                .append(addChangedFieldLog("Genders", getGendersValue(newImport.getGenderMapping()), getGendersValue(oldImport.getGenderMapping())));
        if (StringUtils.isNotBlank(logDescription.toString())) {
            logDescription.insert(0, ". ");
            logDescription.insert(0, getImportProfileDescription(oldImport));
            writeUserActivityLog(admin, "edit import profile", logDescription.toString());
        }
    }

    private String getGendersValue(Map<?,?> gendersMapping) {
        return gendersMapping.size() != 0 ? gendersMapping.toString() : "\"no mapping\"";
    }

    /**
     * Loads import profile to show it on profile view-page
     *
     * @param form a form bean object.
     */
    private void loadImportProfile(ImportProfileForm form) {
        ImportProfile profile = importProfileService.getImportProfileById(form.getProfileId());

        form.setProfile(profile);
        form.clearLists();

        if (profile.isMailinglistsAll()) {
        	form.getMailinglists().clear();
            form.setMailinglistsToShow(null);
        } else {
	        if (profile.getActionForNewRecipients() != 0) {
	            form.getMailinglists().addAll(importProfileService.getSelectedMailingListIds(profile.getId(), profile.getCompanyId()));
	            form.setMailinglistsToShow(new HashSet<>(profile.getMailinglistIds()));
	        } else {
	            form.getMailinglists().addAll(profile.getMailinglistIds());
	            form.setMailinglistsToShow(null);
	        }
        }
        
        form.setMediatypes(profile.getMediatypes());
    }

    private String formatBoolean(boolean value) {
        return value ? "update all recipients" : "update only one recipient";
    }

    private String getPreImportActionLog(int oldId, int newId, int companyId) {
        List<ImportProcessAction> allActions = importProcessActionDao.getAvailableImportProcessActions(companyId);
        return addChangedFieldLog("Pre import action",
                getImportProcessActionName(newId, allActions),
                getImportProcessActionName(oldId, allActions));
    }
    
    private String getImportProcessActionName(int actionId, List<ImportProcessAction> allActions) {
        if (actionId != 0) {
            ImportProcessAction importAction = allActions.stream()
                    .filter(action -> action.getImportactionID() == actionId)
                    .findFirst().orElse(null);
            return importAction != null ? importAction.getName() : UNKNOWN_ACTION;
        }
        return NONE;
    }

    private String getActionForNewRecipientsName(int actionId, int companyId) {
        if (actionId != 0) {
        	EmmAction emmAction = emmActionDao.getEmmActionsByOperationType(companyId, false, ActionOperationType.SUBSCRIBE_CUSTOMER, ActionOperationType.SEND_MAILING)
                    .stream().filter(action -> action.getId() == actionId).findFirst().orElse(null);
            return emmAction != null ? emmAction.getShortname() : UNKNOWN_ACTION;
        }
        return NONE;
    }

    private String getImportProfileDescription(ImportProfile importProfile) {
    	if (importProfile == null) {
    		return "";
    	} else {
	        StringBuilder descriptionSb = new StringBuilder();
	        descriptionSb.append(importProfile.getName()).append(" (").append(importProfile.getId()).append(")");
	        return descriptionSb.toString();
    	}
    }

	/**
	 * Method checks if all the key columns are indexed in database
	 *
	 * @param messages
	 *            messages to add warning to if key column is not indexed
	 * @throws Exception
	 */
	private void checkProfileKeyColumnIndexed(ActionMessages messages, ActionMessages errors, ImportProfile importProfile) throws Exception {
		List<String> columnsToCheck = importProfile.getKeyColumns();
		if (CollectionUtils.isNotEmpty(columnsToCheck)) {
			if (!importRecipientsDao.isKeyColumnIndexed(importProfile.getCompanyId(), columnsToCheck)) {
				int unindexedLimit = configService.getIntegerValue(ConfigValue.MaximumContentLinesForUnindexedImport, importProfile.getCompanyId());
				if (unindexedLimit >= 0 && importRecipientsDao.getResultEntriesCount("SELECT COUNT(*) FROM customer_" + importProfile.getCompanyId() + "_tbl") > unindexedLimit) {
					errors.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("error.import.keyColumn.index"));
				} else {
					messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("warning.import.keyColumn.index"));
				}
			}
		}
	}

	private List<ProfileField> getAvailableImportProfileFields(ComAdmin admin) throws Exception {
		List<ProfileField> dbColumnsAvailable = columnInfoService.getComColumnInfos(admin.getCompanyID(), admin.getAdminID(), true);
        for (String hiddenColumn : ImportUtils.getHiddenColumns(admin)) {
        	for (int i = 0; i < dbColumnsAvailable.size(); i++) {
        		if (dbColumnsAvailable.get(i).getColumn().equalsIgnoreCase(hiddenColumn)) {
        			dbColumnsAvailable.remove(i);
        		}
        	}
        }
        return dbColumnsAvailable;
	}
}
