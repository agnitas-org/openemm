/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import static com.agnitas.emm.core.admin.service.AdminChangesLogService.getDashboardMailingsView;
import static com.agnitas.emm.core.admin.service.AdminChangesLogService.getGenderText;
import static com.agnitas.emm.core.admin.service.AdminChangesLogService.getMailingContentViewName;
import static com.agnitas.emm.core.admin.service.AdminChangesLogService.getMailingLivePreviewPosition;
import static com.agnitas.emm.core.admin.service.AdminChangesLogService.getMailingSettingsViewName;
import static com.agnitas.emm.core.admin.service.AdminChangesLogService.getNavigationLocationName;
import static com.agnitas.emm.core.admin.service.AdminChangesLogService.getStatisticLoadType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.agnitas.beans.AdminGroup;
import org.agnitas.emm.core.commons.password.PasswordCheck;
import org.agnitas.emm.core.commons.password.PasswordCheckHandler;
import org.agnitas.emm.core.commons.password.StrutsPasswordCheckHandler;
import org.agnitas.emm.core.commons.password.util.PasswordPolicyUtil;
import org.agnitas.emm.core.commons.password.util.PasswordUtil;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.logintracking.LoginTrackServiceRequestHelper;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComAdminPreferences;
import com.agnitas.dao.ComAdminGroupDao;
import com.agnitas.dao.ComAdminPreferencesDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComEmmLayoutBaseDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.service.ComWebStorage;

/**
 * Implementation of <strong>Action</strong> that lets an user change his password and other profiledata.
 */
public class ComUserSelfServiceAction extends DispatchAction {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComUserSelfServiceAction.class);

	// ----------------------------------------------------------------------------------------------------------------
	// Dependency Injection

	protected AdminService adminService;
	protected ComAdminGroupDao adminGroupDao;
    protected ComAdminPreferencesDao adminPreferencesDao;
	protected ComCompanyDao companyDao;
	protected ComEmmLayoutBaseDao layoutBaseDao;
	private LoginTrackServiceRequestHelper loginTrackHelper;
	protected ConfigService configService;

	/** Password check and error reporter. */
	private PasswordCheck passwordCheck;
	private WebStorage webStorage;
	
	@Required
	public void setAdminService(AdminService service) {
		this.adminService = Objects.requireNonNull(service, "Admin service is null");
	}

	@Required
	public void setAdminGroupDao(ComAdminGroupDao adminGroupDao) {
		this.adminGroupDao = adminGroupDao;
	}

    @Required
    public void setAdminPreferencesDao(ComAdminPreferencesDao adminPreferencesDao){
        this.adminPreferencesDao = adminPreferencesDao;
    }

    @Required
	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	@Required
	public void setLayoutBaseDao(ComEmmLayoutBaseDao layoutBaseDao) {
		this.layoutBaseDao = layoutBaseDao;
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	/**
	 * Set helper for login tracking.
	 * 
	 * @param helper helper for login tracking
	 */
	@Required
	public void setLoginTrackServiceRequestHelper( LoginTrackServiceRequestHelper helper) {
		this.loginTrackHelper = helper;
	}
	
	/**
	 * Set password check and error reporter.
	 * 
	 * @param passwordCheck password check and error reporter.
	 */
	@Required
	public void setPasswordCheck(PasswordCheck passwordCheck) {
		this.passwordCheck = passwordCheck;
	}

    @Required
    public void setWebStorage(WebStorage webStorage) {
        this.webStorage = webStorage;
    }
	
	// ----------------------------------------------------------------------------------------------------------------
	// Business Logic
	
	public ActionForward showChangeForm(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (form == null || !(form instanceof ComAdminForm)) {
			throw new RuntimeException("Invalid Form for showChangeForm in ComUserSelfServiceAction");
		}
		
		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			return mapping.findForward("logon");
		}

        FormUtils.syncNumberOfRows(webStorage, ComWebStorage.ADMIN_LOGIN_LOG_OVERVIEW, (ComAdminForm) form);
		loginTrackHelper.setLoginTrackingDataToRequest(request, admin, LoginTrackServiceRequestHelper.DEFAULT_LOGIN_MIN_PERIOD_DAYS);
		loginTrackHelper.removeFailedLoginWarningFromRequest( request);

        ComAdminPreferences adminPreferences = adminPreferencesDao.getAdminPreferences(admin.getAdminID());
        fillAdminFormWithOriginalValues((ComAdminForm) form, adminPreferences, admin);
        fillRequestWithOriginalValues(request, admin);
        
        request.setAttribute("SHOW_SUPERVISOR_PERMISSION_MANAGEMENT", admin.getSupervisor() == null && this.configService.getBooleanValue(ConfigValue.SupervisorRequiresLoginPermission, admin.getCompanyID()));
        request.setAttribute("PASSWORD_POLICY", PasswordPolicyUtil.loadCompanyPasswordPolicy(admin.getCompanyID(), configService).getPolicyName());
        	
		return mapping.findForward("show");
	}
	
	public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			return mapping.findForward("logon");
		}
        request.setAttribute("PASSWORD_POLICY", PasswordPolicyUtil.loadCompanyPasswordPolicy(admin.getCompanyID(), configService).getPolicyName());

        try {
			ActionMessages errors = new ActionMessages();
			
			if (form == null || !(form instanceof ComAdminForm)) {
				throw new RuntimeException("Invalid Form for save in ComUserSelfServiceAction");
			}

            ComAdminForm adminForm = (ComAdminForm) form;

            ComAdminPreferences adminPreferences = adminPreferencesDao.getAdminPreferences(admin.getAdminID());
            FormUtils.syncNumberOfRows(webStorage, ComWebStorage.ADMIN_LOGIN_LOG_OVERVIEW, adminForm);
			loginTrackHelper.setLoginTrackingDataToRequest(request, admin, LoginTrackServiceRequestHelper.DEFAULT_LOGIN_MIN_PERIOD_DAYS);

			try {
                //Log changes
                writeUserChangesLog(admin, adminForm);
                writeUserPreferencesChangesLog(admin, adminPreferences, adminForm);

                if (adminPreferences != null) {
                    //save preferences
                    adminPreferences.setAdminID(adminForm.getAdminID());
                    adminPreferences.setNavigationLocation(adminForm.getNavigationLocation());
                    adminPreferences.setDashboardMailingsView(adminForm.getDashboardMailingsView());
                    adminPreferences.setLivePreviewPosition(adminForm.getLivePreviewPosition());
                    adminPreferences.setMailingContentView(adminForm.getMailingContentView());
                    adminPreferences.setMailingSettingsView(adminForm.getMailingSettingsView());
                    adminPreferences.setStartPage(adminForm.getStartPage());
                    adminPreferences.setStatisticLoadType(adminForm.getStatisticLoadType());
                }

				// Set new Fullname
				if (StringUtils.isNotBlank(adminForm.getFullname())) {
					admin.setFullname(adminForm.getFullname());
				} else {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid.username"));
				}

                // Set new Firstname
                if (StringUtils.isNotBlank(adminForm.getFirstname())) {
                    admin.setFirstName(adminForm.getFirstname());
                }

				// Set new UserCompanyName
				if (StringUtils.isNotBlank(adminForm.getCompanyName())) {
					admin.setCompanyName(adminForm.getCompanyName());
				} else {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid.companyname"));
				}
				
				// Set new Email
				if (AgnUtils.isEmailValid(adminForm.getEmail())) {
					admin.setEmail(adminForm.getEmail());
				} else {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid.email"));
				}
				
				// Set new Statistics Email
				if (StringUtils.isEmpty(adminForm.getStatEmail())) {
					admin.setStatEmail("");
				} else if (AgnUtils.isEmailValid(adminForm.getStatEmail())) {
					admin.setStatEmail(adminForm.getStatEmail());
				} else {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.invalid.email"));
				}
				
				// Set new Password
				if (StringUtils.isNotEmpty(adminForm.getPassword())) {

					// Only change if user entered a new password
					PasswordCheckHandler handler = new StrutsPasswordCheckHandler(errors, "password");
					if (adminService.isAdminPassword(admin, adminForm.getPassword())) {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.password_must_differ"));
					} else if (!passwordCheck.checkAdminPassword(adminForm.getPassword(), admin, handler)) {
						adminForm.setPassword("");
						adminForm.setPasswordConfirm("");
						// PasswordCheckHandler handles the password error type
					} else if (!adminForm.getPassword().equals(adminForm.getPasswordConfirm())) {
						adminForm.setPassword("");
						adminForm.setPasswordConfirm("");
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.password.mismatch"));
					} else {
						admin.setPasswordForStorage(adminForm.getPassword());
                        writeUserActivityLog(AgnUtils.getAdmin(request), "change password", adminForm.getUsername() + " (" + adminForm.getAdminID() + ")");
					}
				}

				// Set new Language and Country
				admin.setAdminLang(adminForm.getAdminLocale().getLanguage());
				admin.setAdminCountry(adminForm.getAdminLocale().getCountry());
				
				// Set new Layout
				admin.setLayoutBaseID(adminForm.getLayoutBaseId());

				// Set new Timezone
				admin.setAdminTimezone(adminForm.getAdminTimezone());
				
                //Set gender
                admin.setGender(adminForm.getGender());

                //Set user group
                if (adminForm.getGroupIDs() != null && admin.permissionAllowed(Permission.ADMIN_SETGROUP)) {
	                List<AdminGroup> adminGroups = new ArrayList<>();
	                for (String adminGroupId : adminForm.getGroupIDs()) {
	                	adminGroups.add(adminGroupDao.getAdminGroup(Integer.parseInt(adminGroupId), adminForm.getCompanyID()));
	                }
                    admin.setGroups(adminGroups);
                }
			} catch (Exception e) {
				logger.error("ComUserSelfServiceAction.save: " + e.getMessage(), e);
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			}
			
			if (errors.isEmpty()) {
				adminService.save(admin);
                adminPreferencesDao.save(adminPreferences);

				// Set the new values for this session
				HttpSession session = request.getSession();
				session.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN, admin);
                session.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMINPREFERENCES, adminPreferences);
				session.setAttribute("emmLayoutBase", layoutBaseDao.getEmmLayoutBase(admin.getCompanyID(), admin.getLayoutBaseID()));
				session.setAttribute("emm.locale", admin.getLocale());
				session.setAttribute(org.apache.struts.Globals.LOCALE_KEY, admin.getLocale());
                fillRequestWithOriginalValues(request, admin);

				ActionMessages actionMessages = new ActionMessages();
				actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
				saveMessages(request, actionMessages);
				return mapping.findForward("show");
			} else {
				// Revert Admin Data Changes
				AgnUtils.setAdmin(request, adminService.getAdmin(admin.getAdminID(), admin.getCompanyID()));
				
				saveErrors(request, errors);
				// Reload OptionLists which will otherwise be empty
                fillRequestWithOriginalValues(request, admin);

				return mapping.findForward("show");
			}
		} catch (Exception e) {
			logger.error("Error occured: " + e.getMessage(), e);
			throw new Exception("Error in saving new userdata", e);
		}
	}
	
	public ActionForward changePassword(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
	    try {
			ActionMessages errors = new ActionMessages();
			
			if (form == null || !(form instanceof ComAdminForm)) {
				throw new RuntimeException("Invalid Form for changePassword in ComUserSelfServiceAction");
			}
			
			ComAdmin admin = AgnUtils.getAdmin(request);
			if (admin == null) {
				return mapping.findForward("logon");
			}

			try {
				ComAdminForm adminForm = (ComAdminForm) form;
				
				// Set new Password
				if (StringUtils.isNotEmpty(adminForm.getPassword())) {
					// Only change if user entered a new password
					
					
					
					PasswordCheckHandler handler = new StrutsPasswordCheckHandler(errors, "password");
					if (adminService.isAdminPassword(admin, adminForm.getPassword())) {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.password_must_differ"));
					} else if (!passwordCheck.checkAdminPassword(adminForm.getPassword(), admin, handler)) {
						adminForm.setPassword("");
						adminForm.setPasswordConfirm("");
						// PasswordCheckHandler handles the password error type
					} else if (!adminForm.getPassword().equals(adminForm.getPasswordConfirm())) {
						adminForm.setPassword("");
						adminForm.setPasswordConfirm("");
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.password.mismatch"));
					} else {
						admin.setPasswordForStorage(adminForm.getPassword());
                        writeUserActivityLog(AgnUtils.getAdmin(request), "change password", adminForm.getUsername() + " (" + adminForm.getAdminID() + ")");
					}
				}
			} catch (Exception e) {
				logger.error("ComUserSelfServiceAction.changePassword: " + e.getMessage(), e);
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			}
			
			if (errors.isEmpty()) {
				adminService.save(admin);

				ActionMessages actionMessages = new ActionMessages();
				actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
				saveMessages(request, actionMessages);
			} else {
				saveErrors(request, errors);
			}
			
            fillRequestWithOriginalValues(request, admin);
            
			return mapping.findForward("show");
		} catch (Exception e) {
			logger.error("Error occured: " + e.getMessage(), e);
			throw new Exception("Error in saving new userdata", e);
		}
	}
	
    /**
     * Load an admin account.
     * Loads the data of the admin from the database and stores it in the
     * form.
     *
     * @param comAdminForm     the formula passed from the jsp
     * @param adminPreferences existed admin preferences data
     * @param admin            existed admin account data
     */
	protected void fillAdminFormWithOriginalValues(ComAdminForm comAdminForm, ComAdminPreferences adminPreferences, ComAdmin admin) {
        comAdminForm.setGender(admin.getGender());
        comAdminForm.setAdminID(admin.getAdminID());
        comAdminForm.setUsername(admin.getUsername());
        comAdminForm.setPassword("");
        comAdminForm.setPasswordConfirm("");
        comAdminForm.setCompanyID(admin.getCompanyID());
        comAdminForm.setFullname(admin.getFullname());
        comAdminForm.setAdminLocale(new Locale(admin.getAdminLang(), admin.getAdminCountry()));
        comAdminForm.setAdminTimezone(admin.getAdminTimezone());
        String[] groupIds = new String[admin.getGroupIds().size()];
        for (int i = 0; i < admin.getGroupIds().size(); i++) {
        	groupIds[i] = Integer.toString(admin.getGroupIds().get(i));
        }
        comAdminForm.setGroupIDs(groupIds);
        comAdminForm.setStatEmail(admin.getStatEmail());
        comAdminForm.setCompanyName(admin.getCompanyName());
        comAdminForm.setEmail(admin.getEmail());
        comAdminForm.setLayoutBaseId(admin.getLayoutBaseID());
		comAdminForm.setInitialCompanyName(companyDao.getCompany(admin.getCompanyID()).getShortname());
        comAdminForm.setStartPage(adminPreferences.getStartPage());
        comAdminForm.setFirstname(admin.getFirstName());
        comAdminForm.setMailingContentView(adminPreferences.getMailingContentView());
        comAdminForm.setDashboardMailingsView(adminPreferences.getDashboardMailingsView());
        comAdminForm.setNavigationLocation(adminPreferences.getNavigationLocation());
        comAdminForm.setMailingSettingsView(adminPreferences.getMailingSettingsView());
        comAdminForm.setLivePreviewPosition(adminPreferences.getLivePreviewPosition());
        comAdminForm.setStatisticLoadType(adminPreferences.getStatisticLoadType());
		
		if (logger.isDebugEnabled()) {
			logger.debug("loadAdmin: admin " + comAdminForm.getAdminID() + " loaded");
		}
    }
    /**
     * Fill request with data.
     *
     * @param request Http request
     * @param admin current admin
     */
    protected void fillRequestWithOriginalValues(HttpServletRequest request, ComAdmin admin){
        request.setAttribute("availableAdminGroups", adminGroupDao.getAdminGroupsByCompanyIdAndDefault(AgnUtils.getCompanyID(request), admin.getGroupIds()));

        request.setAttribute("availableLayouts", adminService.getEmmLayoutsBase(admin.getCompanyID()));
    }

    /** Service class for accessing user activity log. */
    private UserActivityLogService userActivityLogService;

    /**
     * Set service class for accessing user activity log.
     *
     * @param userActivityLogService service class for accessing user activity log
     */
    @Required
    public void setUserActivityLogService(UserActivityLogService userActivityLogService) {
        this.userActivityLogService = userActivityLogService;
    }

    /**
     * Write user activity log.
     *
     * @param admin user
     * @param action performed action
     * @param description description
     */
    protected void writeUserActivityLog(ComAdmin admin, String action, String description)  {
        try {
            if (userActivityLogService != null) {
                userActivityLogService.writeUserActivityLog(admin, action, description);
            } else {
                logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
                logger.info("Userlog: " + admin.getUsername() + " " + action + " " +  description);
            }
        } catch (Exception e) {
            logger.error("Error writing ActivityLog: " + e.getMessage(), e);
            logger.info("Userlog: " + admin.getUsername() + " " + action + " " +  description);
        }
    }

    /**
     * Compare existed and new user data and write changes in user log
     *
     * @param adminForm the data passed from the jsp
     * @param admin     existed admin account data
     */
    private void writeUserChangesLog(ComAdmin admin, ComAdminForm adminForm){
        try {
            String userName = admin.getUsername();
            //Log changes of gender (Salutation)
            if (admin.getGender()!= adminForm.getGender()){
                writeUserActivityLog(admin, "edit user",
                        userName + ". Gender changed from " + getGenderText(admin.getGender()) + " to " + getGenderText(adminForm.getGender()));
            }
            //Log changes of first name
            if (!(admin.getFirstName().equals(adminForm.getFirstname()))){
                writeUserActivityLog(admin, "edit user",
                        userName + ". First Name changed from " + admin.getFirstName() + " to " + adminForm.getFirstname());
            }
            //Log changes of last name
            if (!(admin.getFullname().equals(adminForm.getFullname()))){
                writeUserActivityLog(admin, "edit user",
                        userName + ". Last Name changed from " + admin.getFullname() + " to " + adminForm.getFullname());
            }
            //Log changes of email
            if (!(admin.getEmail().equals(adminForm.getEmail()))){
                writeUserActivityLog(admin, "edit user",
                        userName + ". Email changed from " + admin.getEmail() + " to " + adminForm.getEmail());
            }
            //Log changes of password
            if (PasswordUtil.passwordChanged(adminService, admin.getUsername(), adminForm.getPassword())){
                writeUserActivityLog(admin, "edit user", userName + ". Password changed");
            }
            //Log changes of language
            if (!(admin.getAdminLang().equals(adminForm.getAdminLocale().getLanguage()))){
                writeUserActivityLog(admin, "edit user",
                        userName + ". Language changed from " + Locale.forLanguageTag(admin.getAdminLang()).getDisplayLanguage() +
                                " to " + Locale.forLanguageTag(adminForm.getAdminLocale().getLanguage()).getDisplayLanguage());
            }
            //Log changes of timezone
            if (!(admin.getAdminTimezone().equals(adminForm.getAdminTimezone()))){
                writeUserActivityLog(admin, "edit user",
                        userName + ". Timezone changed from " + admin.getAdminTimezone() + " to " + adminForm.getAdminTimezone());
            }

            //Log changes of statistic email address
            String existingStatEmail = admin.getStatEmail();
            if (StringUtils.isBlank(existingStatEmail)) {
            	existingStatEmail = "";
            }
            String newStatEmail = adminForm.getStatEmail();
            if (StringUtils.isBlank(newStatEmail)) {
            	newStatEmail = "";
            }

            if (!existingStatEmail.equals(newStatEmail)){
                if (existingStatEmail.isEmpty() && !newStatEmail.isEmpty()){
                    writeUserActivityLog(admin, "edit user",
                            userName + ". Statistic email " + newStatEmail + " added");
                }
                if (!existingStatEmail.isEmpty() && newStatEmail.isEmpty()){
                    writeUserActivityLog(admin, "edit user",
                            userName + ". Statistic email " + existingStatEmail + " removed");
                }
                if (!existingStatEmail.isEmpty() && !newStatEmail.isEmpty()){
                    writeUserActivityLog(admin, "edit user",
                            userName + ". Statistic email changed from " + existingStatEmail + " to " + newStatEmail);
                }
            }
            
            // Log changes of userGroup
            Set<Integer> currentGroupIds = new HashSet<>(admin.getGroupIds());
            Set<Integer> newGroupIds = new HashSet<>();
            if (adminForm.getGroupIDs() != null) {
            	for (String groupId : adminForm.getGroupIDs()) {
            		newGroupIds.add(Integer.parseInt(groupId));
            	}
            }
            if (!currentGroupIds.equals(newGroupIds)) {
                String oldGroupIdsList = StringUtils.join(currentGroupIds, ",");
                String newGroupIdsList = StringUtils.join(newGroupIds, ",");

                writeUserActivityLog(admin, "edit user", userName + ". User Group changed from " + oldGroupIdsList + " to " + newGroupIdsList);
            }

            if (logger.isInfoEnabled()) {
                logger.info("saveEmmUser: self edit save user " + adminForm.getAdminID());
            }
        } catch (Exception e) {
            logger.error("Log EMM User self changes error: " + e.getMessage(), e);
        }
    }

    /**
     * Compare existed and new user preferences data and write changes in user log
     *
     * @param admin            existed admin account data
     * @param adminPreferences existed admin preferences data
     * @param adminForm        the data passed from the jsp
     */
    private void writeUserPreferencesChangesLog(ComAdmin admin, ComAdminPreferences adminPreferences, ComAdminForm adminForm){
        try {
            String userName = admin.getUsername();

            //Log changes of default dashboard mailings view
            int oldDashboardMailingsView = adminPreferences.getDashboardMailingsView();
            int newDashboardMailingsView = adminForm.getDashboardMailingsView();

            if (oldDashboardMailingsView != newDashboardMailingsView){
                writeUserActivityLog(admin, "edit user",
                        userName + ". Dashboard mailings view type changed from " + getDashboardMailingsView(oldDashboardMailingsView) +
                                " to " + getDashboardMailingsView(newDashboardMailingsView));
            }

            //Log changes of startpage
            if (adminPreferences.getStartPage() != adminForm.getStartPage()){
                if ((adminPreferences.getStartPage() == 0)&& (adminForm.getStartPage() == 1)){
                    writeUserActivityLog(admin, "edit user",
                            userName + ". Startpage changed from Dashboard  to Calendar");
                }
                if ((adminPreferences.getStartPage() == 1)&& (adminForm.getStartPage() == 0)){
                    writeUserActivityLog(admin, "edit user",
                            userName + ". Startpage changed from Calendar to Dashboard");
                }
            }

            // Log changes of Statistic-Summary load type
            int oldStatisticLoadType = adminPreferences.getStatisticLoadType();
            int newStatisticLoadType = adminForm.getStatisticLoadType();

            if (oldStatisticLoadType != newStatisticLoadType){
                writeUserActivityLog(admin, "edit user",
                        userName + ". Statistic-Summary load type changed from " + getStatisticLoadType(oldStatisticLoadType) +
                                " to " + getStatisticLoadType(newStatisticLoadType));
            }

            // Log changes of default mailing content view
            int oldMailingContentView = adminPreferences.getMailingContentView();
            int newMailingContentView = adminForm.getMailingContentView();

            if (oldMailingContentView != newMailingContentView){
                writeUserActivityLog(admin, "edit user",
                        userName + ". User mailing content view type changed from " + getMailingContentViewName(oldMailingContentView) +
                                " to " + getMailingContentViewName(newMailingContentView));
            }

            // Log changes of default navigation location
            int oldNavigationLocation = adminPreferences.getNavigationLocation();
            int newNavigationLocation = adminForm.getNavigationLocation();

            if (oldNavigationLocation != newNavigationLocation){
                writeUserActivityLog(admin, "edit user",
                        userName + ". Navigation location changed from " + getNavigationLocationName(oldNavigationLocation) +
                                " to " + getNavigationLocationName(newNavigationLocation));
            }

            // Log changes of default mailing settings view (expanded ot collapsed)
            int oldMailingSettingsView = adminPreferences.getMailingSettingsView();
            int newMailingSettingsView = adminForm.getMailingSettingsView();

            if (oldMailingSettingsView != newMailingSettingsView){
                writeUserActivityLog(admin, "edit user",
                        userName + ". Default mailing settings view changed from " + getMailingSettingsViewName(oldMailingSettingsView) +
                                " to " + getMailingSettingsViewName(newMailingSettingsView));
            }

            // Log changes of default position of the mailing content live preview (right/bottom/deactivated)
            int oldLivePreviewPosition = adminPreferences.getLivePreviewPosition();
            int newLivePreviewPosition = adminForm.getLivePreviewPosition();

            if (oldLivePreviewPosition != newLivePreviewPosition){
                writeUserActivityLog(admin, "edit user",
                        userName + ". Mailing content live preview position changed from " + getMailingLivePreviewPosition(oldLivePreviewPosition) +
                                " to " + getMailingLivePreviewPosition(newLivePreviewPosition));
            }

            if (logger.isInfoEnabled()) {
                logger.info("saveEmmUser: self edit save user preferences " + adminForm.getAdminID());
            }
        } catch (Exception e) {
            logger.error("Log EMM User self user preferences changes error: " + e.getMessage(), e);
        }
    }
}
