/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.AdminPreferencesDao;
import org.agnitas.emm.core.commons.password.PasswordCheck;
import org.agnitas.emm.core.commons.password.PasswordCheckHandler;
import org.agnitas.emm.core.commons.password.StrutsPasswordCheckHandler;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.Tuple;
import org.agnitas.web.RecipientForm;
import org.agnitas.web.StrutsActionBase;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComAdminPreferences;
import com.agnitas.dao.ComAdminDao;
import com.agnitas.dao.ComAdminGroupDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComEmmLayoutBaseDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminChangesLogService;
import com.agnitas.emm.core.admin.service.AdminSavingResult;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.admin.service.PermissionFilter;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.service.ComCSVService;
import com.agnitas.service.ComPDFService;
import com.agnitas.service.impl.ComAdminListQueryWorker;
import com.agnitas.util.FutureHolderMap;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfWriter;

public class ComAdminAction extends StrutsActionBase {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComAdminAction.class);
	
	/** DAO for accessing admin group data. */
	protected ComAdminGroupDao adminGroupDao;
	protected ComAdminDao adminDao;

	/** DAO for accessing admin preferences data. */
	protected AdminPreferencesDao adminPreferencesDao;

	private AdminChangesLogService adminChangesLogService;

	/** DAO for accessing company data. */
	protected ComCompanyDao companyDao;
	protected FutureHolderMap futureHolder;
	protected ExecutorService workerExecutorService;
	
	/** Service for accessing configuration. */
	protected ConfigService configService;
	
	/** Password checker and error reporter. */
	protected PasswordCheck passwordCheck;

	/**
	 * DAO for accessing layout data.
	 */
	private ComEmmLayoutBaseDao layoutBaseDao;

	/**
	 * DAO for accessing target groups.
	 */
	protected ComTargetDao targetDao;

	protected ComCSVService comCSVService;

	protected ComPDFService comPDFService;

	protected ComMailinglistService mailinglistService;
    private MailinglistApprovalService mailinglistApprovalService;
    
	protected AdminService adminService;

	protected WebStorage webStorage;
	
	private PermissionFilter permissionFilter;

	public static final int ACTION_VIEW_RIGHTS = ACTION_LAST + 1;
	public static final int ACTION_SAVE_RIGHTS = ACTION_LAST + 2;
	public static final int ACTION_VIEW_WITHOUT_LOAD = ACTION_LAST + 3;
	protected static final String FUTURE_TASK = "GET_ADMIN_LIST";

	public static final int ACTION_EXPORT_CSV = ACTION_LAST + 4;

	public static final int ACTION_EXPORT_PDF = ACTION_LAST + 5;

	public static final int ACTION_VIEW_MAILINGLISTS = ACTION_LAST + 6;

	public static final int ACTION_SAVE_MAILINGLISTS = ACTION_LAST + 7;

	@Required
	public void setAdminGroupDao(ComAdminGroupDao adminGroupDao) {
		this.adminGroupDao = adminGroupDao;
	}
	@Required
	public void setAdminDao(ComAdminDao adminDao) {
		this.adminDao = adminDao;
	}

	@Required
	public void setAdminPreferencesDao(AdminPreferencesDao adminPreferencesDao) {
		this.adminPreferencesDao = adminPreferencesDao;
	}

	@Required
	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	@Required
	public void setFutureHolder(FutureHolderMap futureHolder) {
		this.futureHolder = futureHolder;
	}

	@Required
	public void setWorkerExecutorService(ExecutorService workerExecutorService) {
		this.workerExecutorService = workerExecutorService;
	}
	   
    @Required
    public final void setMailinglistApprovalService(final MailinglistApprovalService service) {
    	this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	@Required
	public void setPasswordCheck(PasswordCheck check) {
		this.passwordCheck = check;
	}

	/**
	 * Set DAO for accessing target group data.
	 *
	 * @param targetDao DAO for accessing target group data.
	 */
	@Required
	public void setTargetDao(ComTargetDao targetDao) {
		this.targetDao = targetDao;
	}

	@Required
	public void setComCSVService(ComCSVService comCSVService) {
		this.comCSVService = comCSVService;
	}

	@Required
	public void setComPDFService(ComPDFService comPDFService) {
		this.comPDFService = comPDFService;
	}

	@Required
	public void setMailinglistService(ComMailinglistService mailinglistService) {
		this.mailinglistService = mailinglistService;
	}

	@Required
	public void setAdminService(AdminService adminService) {
		this.adminService = adminService;
	}

	@Required
	public void setAdminChangesLogService(AdminChangesLogService adminChangesLogService) {
		this.adminChangesLogService = adminChangesLogService;
	}

	@Required
	public void setWebStorage(WebStorage webStorage) {
		this.webStorage = webStorage;
	}

	@Override
	public String subActionMethodName(int subAction) {
		switch (subAction) {
			case ACTION_VIEW_RIGHTS:
				return "view_rights";
			case ACTION_SAVE_RIGHTS:
				return "save_rights";
			case ACTION_VIEW_WITHOUT_LOAD:
				return "view_without_load";
			case ACTION_EXPORT_CSV:
				return "export_csv";
			case ACTION_EXPORT_PDF:
				return "export_pdf";
			case ACTION_VIEW_MAILINGLISTS:
				return "view_mailinglists";
			case ACTION_SAVE_MAILINGLISTS:
				return "save_mailinglists";
			default:
				return super.subActionMethodName(subAction);
		}
	}
	
	/**
	 * Process the specified HTTP request, and create the corresponding HTTP
	 * response (or forward to another web component that will create it).
	 * Return an <code>ActionForward</code> instance describing where and how
	 * control should be forwarded, or <code>null</code> if the response has
	 * already been completed.
	 *
	 * @param mapping The ActionMapping used to select this instance
	 * @param form the form from .jsp
	 * @param req  the Servlet Request
	 * @param response  the Servlet Response
	 * @exception IOException if an input/output error occurs
	 * @exception ServletException if a servlet exception occurs
	 * @return destination
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest req, HttpServletResponse response) throws IOException, ServletException {
		ComAdminForm aForm = null;
		ActionMessages errors = new ActionMessages();
		ActionMessages messages = new ActionMessages();
		ActionForward destination = null;

		ComAdmin admin = AgnUtils.getAdmin(req);

		assert (admin != null);

		if (form != null) {
			aForm = (ComAdminForm) form;
		} else {
			aForm = new ComAdminForm();
		}

		if (logger.isInfoEnabled()) {
			logger.info("Action: " + aForm.getAction());
		}

		if (StringUtils.equals(req.getParameter("delete"), "delete")) {
			aForm.setAction(ACTION_CONFIRM_DELETE);
		} else if (StringUtils.equals(req.getParameter("export_action"), "export_csv")) {
			aForm.setAction(ACTION_EXPORT_CSV);
		} else if (StringUtils.equals(req.getParameter("export_action"), "export_pdf")) {
			aForm.setAction(ACTION_EXPORT_PDF);
		}

		try {
			switch(aForm.getAction()) {
				case ACTION_VIEW:
					if (aForm.getAdminID() != 0) {
						aForm.setAction(ACTION_SAVE);
						loadAdmin(aForm, req, false, errors);
					} else {
						aForm.setAction(ACTION_NEW);
					}
					req.setAttribute("availableTimeZones", TimeZone.getAvailableIDs());
					destination = mapping.findForward("view");
					break;

				case ACTION_SAVE:
					if (StringUtils.equals(req.getParameter("save"), "save")) {
						if (adminUsernameChangedToExisting(aForm)) {
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.username.duplicate"));
						} else if (aForm.getGroupID() <= 0) {
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.user.group"));
						} else {
							if (StringUtils.isEmpty(aForm.getPassword()) || checkPassword(aForm, errors)) {
								saveAdmin(aForm, admin, req.getSession(), errors);

								// Show "changes saved"
								messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
							}
						}
					}
					req.setAttribute("availableTimeZones", TimeZone.getAvailableIDs());
					destination = mapping.findForward("view");
					break;

				case ACTION_VIEW_RIGHTS:
					loadAdmin(aForm, req, true, errors);
					aForm.setAction(ACTION_SAVE_RIGHTS);
					destination = mapping.findForward("rights");
					break;

				case ACTION_SAVE_RIGHTS:
					try {
						saveAdminRights(admin, aForm, errors);
						loadAdmin(aForm, req, true, errors);
						aForm.setAction(ACTION_SAVE_RIGHTS);
						destination = mapping.findForward("rights");
						
						if (errors.isEmpty()) {
							// Show "changes saved"
							messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
						}
					} catch (Exception e) {
						logger.error("Exception saving rights", e);
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.admin.save", e));
					}
					break;

				case ACTION_NEW:
					if (configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfAdmins) >= 0 && configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfAdmins) <= adminService.getNumberOfAdmins()) {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.numberOfAdminsExceeded"));
					} else if (StringUtils.equals(req.getParameter("save"), "save")) {
						aForm.setAdminID(0);
						if (aForm.getPassword().length() <= 0) {
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.password.missing"));
							destination = mapping.findForward("view");
							aForm.setAction(ACTION_NEW);
						} else if (aForm.getGroupID() <= 0) {
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.user.group"));
							destination = mapping.findForward("view");
							aForm.setAction(ACTION_NEW);
						} else  if (adminService.adminExists(aForm.getUsername())) {
							errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.username.duplicate"));
							aForm.setAction(ACTION_NEW);
							destination = mapping.findForward("view");
						} else {
							if (checkPassword(aForm, errors)) {
								ComAdmin newAdmin = saveAdmin(aForm, admin, req.getSession(), errors);

								if (newAdmin != null) {
									aForm.setAdminID(newAdmin.getAdminID());
									messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
								}

								if (aForm.getAdminID() != 0) {
									loadAdmin(aForm, req, false, errors);
									aForm.setAction(ACTION_SAVE);
								}
								destination = mapping.findForward("view");
							} else {
								destination = mapping.findForward("view");
							}
						}
						req.setAttribute("availableTimeZones", TimeZone.getAvailableIDs());
					}
					break;

				case ACTION_LIST:
					destination = prepareList(mapping, req, errors, aForm);
					if (aForm.getColumnwidthsList() == null) {
						aForm.setColumnwidthsList(getInitializedColumnWidthList(4));
					}
					break;

				case ACTION_VIEW_WITHOUT_LOAD:
					if (aForm.getAdminID() != 0) {
						aForm.setAction(ACTION_SAVE);
					} else {
						aForm.setAction(ACTION_NEW);
					}
					destination = mapping.findForward("view");
					break;

				case ACTION_EXPORT_CSV:
					aForm.setNumberOfRows(-1);
					Future<PaginatedListImpl<AdminEntry>> adminFuture = getAdminlistFuture(req, aForm);
					PaginatedListImpl<AdminEntry> users = adminFuture.get();
					String csv = comCSVService.getUserCSV(users.getList());
					String fileName = "users.csv";

					byte bytes[] = new byte[16384];
					int len = 0;
					if (csv != null) {
						InputStream instream = null;
						try {
							instream = new ByteArrayInputStream(csv.getBytes("UTF-8"));
							response.setContentType("text/csv");
			                HttpUtils.setDownloadFilenameHeader(response, fileName);
							response.setContentLength(csv.length());
							try(ServletOutputStream ostream = response.getOutputStream()) {
								while((len = instream.read(bytes)) != -1) {
									ostream.write(bytes, 0, len);
								}
							}
						} finally {
							if (instream != null) {
								instream.close();
							}
							String description = "Page: " + getPage(req)
									+ ", sort: " + getAdminSort(req)
									+ ", direction: " + getDirection(req, aForm);
							writeUserActivityLog(admin, "export admins csv", description, logger);
						}
						destination = null;
					} else {
						errors.add("global", new ActionMessage("error.export.file_not_ready"));
					}
					break;

				case ACTION_DELETE:
					if (req.getParameter("kill") != null) {
						if (aForm.getAdminID() > 0) {
							ComAdmin adminToDelete = adminService.getAdmin(aForm.getAdminID(), admin.getCompanyID());
							if (adminToDelete != null) {
								adminService.deleteAdmin(admin.getCompanyID(), aForm.getAdminID(), admin.getAdminID());

								writeUserActivityLog(admin, "delete user", adminToDelete.getUsername() + " (" + adminToDelete.getAdminID() + ")", logger);
								// Show "changes saved"
								messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
							}
						}
					
						//aForm = new ComAdminForm();
					}
					destination = prepareList(mapping, req, errors, aForm);
					
					break;
					
				case ACTION_EXPORT_PDF:
					aForm.setNumberOfRows(-1);
					PaginatedListImpl<AdminEntry> userList = getAdminlistFuture(req, aForm).get();

					response.setContentType("application/pdf");
	                HttpUtils.setDownloadFilenameHeader(response, "users.pdf");

					Document document = new Document();
					try {
						try(ServletOutputStream ostream = response.getOutputStream()) {
							PdfWriter.getInstance(document, response.getOutputStream());
							document.open();
	
							comPDFService.writeUsersPDF(userList.getList(), document);
	
							document.close();
						}
					} finally {
						document.close();
						String description = "Page: " + getPage(req)
								+ ", sort: " + getAdminSort(req)
								+ ", direction: " + getDirection(req, aForm);
						writeUserActivityLog(admin, "export admins pdf", description, logger);
					}
					break;

				case ACTION_CONFIRM_DELETE:
					loadAdmin(aForm, req, false, errors);
					aForm.setAction(ACTION_DELETE);
					destination = mapping.findForward("delete");
					break;

				case ACTION_VIEW_MAILINGLISTS:
					loadMailinglists(aForm, req);
					destination = mapping.findForward("view_mailinglists");
					break;

				case ACTION_SAVE_MAILINGLISTS:
					ComAdmin editedAdmin = adminService.getAdmin(aForm.getAdminID(), admin.getCompanyID());
					if(mailinglistApprovalService.setDisabledMailinglistForAdmin(
							editedAdmin.getCompanyID(),
							aForm.getAdminID(),
							aForm.getDisabledMailinglistsIds())){
						messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
					} else {
						errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("changes_not_saved"));
					}
					loadMailinglists(editedAdmin, aForm);
					destination = mapping.findForward("view_mailinglists");
					break;

				default:
					aForm.setAction(ACTION_LIST);
					destination = prepareList(mapping, req, errors, aForm);
			}
		} catch (Exception e) {
			logger.error("Error in ComAdminAction.execute()", e);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			throw new ServletException(e);
		}

		if (destination != null && "view".equals(destination.getName())) {
			loadAdminFormData(admin, req);
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



	/**
	 * Load an admin account.
	 * Loads the data of the admin from the database and stores it in the
	 * form.
	 *
	 * @param aForm the formular passed from the jsp
	 * @param request the Servlet Request (needed to get the company id)
	 * @throws Exception 
	 */
	protected ComAdmin loadAdmin(ComAdminForm aForm, HttpServletRequest request, boolean loadPermissionData, ActionMessages errors) throws Exception {
		int adminIdToEdit = aForm.getAdminID();
		int compID = AgnUtils.getAdmin(request).getCompanyID();
		ComAdmin adminToEdit = adminService.getAdmin(adminIdToEdit, compID);

		if (adminToEdit != null) {
			if (adminToEdit.getGroup() == null) {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.admin.invalidGroup"));
			}
				
			ComAdminPreferences adminPreferences = adminPreferencesDao.getAdminPreferences(adminIdToEdit);

			aForm.setUsername(adminToEdit.getUsername());
			aForm.setPassword("");
			aForm.setPasswordConfirm("");
			aForm.setCompanyID(adminToEdit.getCompanyID());
			aForm.setFullname(adminToEdit.getFullname());
			aForm.setFirstname(adminToEdit.getFirstName());
			aForm.setAdminLocale(new Locale(adminToEdit.getAdminLang(), adminToEdit.getAdminCountry()));
			aForm.setAdminTimezone(adminToEdit.getAdminTimezone());
			aForm.setGroupID(adminToEdit.getGroup() == null ? 1 : adminToEdit.getGroup().getGroupID());
			aForm.setStatEmail(adminToEdit.getStatEmail());
			aForm.setCompanyName(adminToEdit.getCompanyName());
			aForm.setEmail(adminToEdit.getEmail());
			aForm.setLayoutBaseId(adminToEdit.getLayoutBaseID());
			aForm.setInitialCompanyName(adminToEdit.getInitialCompanyName());
			aForm.setAdminPhone(adminToEdit.getAdminPhone());
			aForm.setStartPage(adminPreferences.getStartPage());
			aForm.setMailingContentView(adminPreferences.getMailingContentView());
			aForm.setDashboardMailingsView(adminPreferences.getDashboardMailingsView());
			aForm.setNavigationLocation(adminPreferences.getNavigationLocation());
			aForm.setMailingSettingsView(adminPreferences.getMailingSettingsView());
			aForm.setLivePreviewPosition(adminPreferences.getLivePreviewPosition());
			aForm.setStatisticLoadType(adminPreferences.getStatisticLoadType());
			aForm.setOneTimePassword(adminToEdit.isOneTimePassword());

			if (logger.isInfoEnabled()) {
				logger.info("loadAdmin: admin " + aForm.getAdminID() + " loaded");
			}
			aForm.setGender(adminToEdit.getGender());
			aForm.setTitle(adminToEdit.getTitle());
			if (logger.isInfoEnabled()) {
				logger.info("loadAdmin: admin " + aForm.getAdminID() + " loaded");
			}

			writeUserActivityLog(AgnUtils.getAdmin(request), "view user", adminToEdit.getUsername());
			
			if (loadPermissionData) {
				ComAdmin admin = AgnUtils.getAdmin(request);

				List<String> permissionCategories = new ArrayList<>();
				Map<String, Map<String, List<String>>> permissionsByCategory = new HashMap<>();
				Map<String, String> permissionGranted = new HashMap<>();
				Map<String, String> permissionChangeable = new HashMap<>();
				Map<String, List<String>> subCategoriesByCategory = new HashMap<>();

				// For information on rules for changing user rights, see:
				// http://wiki.agnitas.local/doku.php?id=abteilung:allgemein:premiumfeatures&s[]=rechtevergabe#rechtevergabe-moeglichkeiten_in_der_emm-gui
				List<String> standardCategories = Arrays.asList(Permission.ORDERED_STANDARD_RIGHT_CATEGORIES);
				List<String> premiumCategories = Arrays.asList(Permission.ORDERED_PREMIUM_RIGHT_CATEGORIES);
				permissionCategories.addAll(standardCategories);
				permissionCategories.addAll(premiumCategories);
				if (admin.permissionAllowed(Permission.MASTER_SHOW) || admin.getAdminID() == 1) {
					permissionCategories.add(Permission.CATEGORY_KEY_SYSTEM);
					permissionCategories.add(Permission.CATEGORY_KEY_OTHERS);
				}
				
				Set<Permission> companyPermissions = companyDao.getCompanyPermissions(adminToEdit.getCompanyID());
				
				for (Permission permission : Permission.getAllPermissionsAndCategories().keySet()) {
					if(this.permissionFilter.isVisible(permission)) {
						String subCategory = StringUtils.isEmpty(permission.getSubCategory()) ? "" : permission.getSubCategory();
						if (permissionCategories.contains(permission.getCategory())) {
							String permissionName = permission.toString();
							if (!permissionsByCategory.containsKey(permission.getCategory())) {
								permissionsByCategory.put(permission.getCategory(), new HashMap<String, List<String>>());
							}
							if (!permissionsByCategory.get(permission.getCategory()).containsKey(subCategory)) {
								permissionsByCategory.get(permission.getCategory()).put(subCategory, new ArrayList<>());
							}
							if (!subCategoriesByCategory.containsKey(permission.getCategory())) {
								subCategoriesByCategory.put(permission.getCategory(), new ArrayList<>());
							}
							if (!subCategoriesByCategory.get(permission.getCategory()).contains(subCategory)) {
								subCategoriesByCategory.get(permission.getCategory()).add(subCategory);
							}
							permissionsByCategory.get(permission.getCategory()).get(subCategory).add(permissionName);
							permissionGranted.put(permissionName, adminToEdit.permissionAllowed(permission) ? "checked" : "");
							boolean isChangeable;
							if (adminToEdit.getGroup().permissionAllowed(permission)) {
								isChangeable = false;
							} else if (standardCategories.contains(permission.getCategory())) {
								isChangeable = true;
							} else if (premiumCategories.contains(permission.getCategory())) {
								isChangeable = companyPermissions.contains(permission);
							} else {
								isChangeable = admin.permissionAllowed(permission) || admin.permissionAllowed(Permission.MASTER_SHOW) || admin.getAdminID() == 1;
							}
							permissionChangeable.put(permissionName, isChangeable ? "" : "disabled");
						}
					}
				}
				
				for (Map<String,List<String>> categoryMap : permissionsByCategory.values()) {
					for (List<String> permissionList : categoryMap.values()) {
						Collections.sort(permissionList);
					}
				}
				
				for (List<String> subCategoriesList : subCategoriesByCategory.values()) {
					Collections.sort(subCategoriesList);
				}

				request.setAttribute("permissionCategories", permissionCategories);
				request.setAttribute("permissionsByCategory", permissionsByCategory);
				request.setAttribute("permissionGranted", permissionGranted);
				request.setAttribute("permissionChangeable", permissionChangeable);
				request.setAttribute("subCategoriesByCategory", subCategoriesByCategory);
			}
		} else {
			aForm.setAdminID(0);
			aForm.setCompanyID(compID);
			logger.warn("loadAdmin: admin " + aForm.getAdminID() + " could not be loaded");
		}
		return adminToEdit;
	}

	/**
	 * Save an admin account.
	 * Gets the admin data from a form and stores it in the database.
	 *
	 * @param aForm the formula passed from the jsp
	 * @param admin currently authorized admin.
	 * @param session current session representation.
	 */
	private ComAdmin saveAdmin(ComAdminForm aForm, ComAdmin admin, HttpSession session, ActionMessages messages) {
		boolean isNew = aForm.getAdminID() == 0;

		ComAdmin oldSavingAdmin = null;
		ComAdminPreferences oldSavingAdminPreferences = null;

		if (!isNew) {
			oldSavingAdmin = adminDao.getAdmin(aForm.getAdminID(), aForm.getCompanyID());
			oldSavingAdminPreferences = adminPreferencesDao.getAdminPreferences(aForm.getAdminID());
		}

		AdminSavingResult result = adminService.saveAdmin(aForm, admin);

		if (result.isSuccess()) {
			ComAdmin savedAdmin = result.getResult();

			if (isNew) {
				// Log successful creation of new user
				writeUserActivityLog(admin, "create user", savedAdmin.getUsername() + " (" + savedAdmin.getAdminID() + ")");
			} else {
				adminChangesLogService.getChangesAsUserActions(aForm, oldSavingAdmin, oldSavingAdminPreferences)
						.forEach(action -> writeUserActivityLog(admin, action));

				if (result.isPasswordChanged()) {
					writeUserActivityLog(admin, "change password", aForm.getUsername() + " (" + aForm.getAdminID() + ")");
				}

				// Set the new values for this session if user edit own profile via Administration -> User -> OwnProfile
				if (savedAdmin.getAdminID() == admin.getAdminID()) {
					ComAdminPreferences adminPreferences = adminPreferencesDao.getAdminPreferences(savedAdmin.getAdminID());

					savedAdmin.setSupervisor(admin.getSupervisor());

					session.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN, savedAdmin);
					session.setAttribute(AgnUtils.SESSION_CONTEXT_KEYNAME_ADMINPREFERENCES, adminPreferences);
				}
			}

			return savedAdmin;
		} else {
			messages.add(result.getErrors());
			return null;
		}
	}

	/**
	 * Set DAO for accessing layout data.
	 *
	 * @param layoutBaseDao DAO for accessing layout data.
	 */
	public void setLayoutBaseDao(ComEmmLayoutBaseDao layoutBaseDao) {
		this.layoutBaseDao = layoutBaseDao;
	}

	private void loadAdminFormData(ComAdmin admin, HttpServletRequest req) {
		req.setAttribute("adminGroups", adminGroupDao.getAdminGroupsByCompanyIdAndDefault(admin.getCompanyID()));
		req.setAttribute("layouts", layoutBaseDao.getEmmLayoutsBase(admin.getCompanyID()));
		req.setAttribute("createdCompanies", companyDao.getCreatedCompanies(admin.getCompanyID()));
	}

	protected ActionForward prepareList(ActionMapping mapping, HttpServletRequest req, ActionMessages errors, ComAdminForm adminForm) {
		ActionMessages messages = null;
		ActionForward destination = null;

		try {
			FormUtils.syncNumberOfRows(webStorage, WebStorage.ADMIN_OVERVIEW, adminForm);

			destination = mapping.findForward("loading");
			String key = FUTURE_TASK + "@" + req.getSession(false).getId();
			if (!futureHolder.containsKey(key)) {
				Future<PaginatedListImpl<AdminEntry>> adminFuture = getAdminlistFuture(req, adminForm);
				futureHolder.put(key, adminFuture);
			}

			// if we perform AJAX request (load next/previous page) we have to wait for preparing data
			if (HttpUtils.isAjax(req)) {
				while (!futureHolder.containsKey(key) || !futureHolder.get(key).isDone()) {
					if (adminForm.getRefreshMillis() < 1000) { // raise the refresh time
						adminForm.setRefreshMillis( adminForm.getRefreshMillis() + 50 );
					}
					Thread.sleep(adminForm.getRefreshMillis());
				}
			}

			@SuppressWarnings("unchecked")
			Future<PaginatedListImpl<AdminEntry>> future = (Future<PaginatedListImpl<AdminEntry>>) futureHolder.get(key);
			if (future != null && future.isDone()) {
				req.setAttribute("adminEntries", future.get());

				int companyID = AgnUtils.getCompanyID(req);
				adminForm.setCompanies(companyDao.getCreatedCompanies(companyID));
				adminForm.setAdminGroups(adminGroupDao.getAdminGroupsByCompanyIdAndDefault(companyID));
				adminForm.setMailinglists(mailinglistService.getAllMailingListsNames(companyID));

				destination = mapping.findForward("list");
				futureHolder.remove(key);
				adminForm.setRefreshMillis(RecipientForm.DEFAULT_REFRESH_MILLIS);
				messages = adminForm.getMessages();

				if (messages != null && !messages.isEmpty()) {
					saveMessages(req, messages);
					adminForm.setMessages(null);
				}
			} else {
				// raise the refresh time
				if (adminForm.getRefreshMillis() < 1000) {
					adminForm.setRefreshMillis(adminForm.getRefreshMillis() + 50);
				}
				adminForm.setError(false);
			}
		} catch (Exception e) {
			logger.error("Error preparing list", e);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			// do not refresh when an error has been occurred
			adminForm.setError(true);
		}

		if (HttpUtils.isAjax(req)) {
			if (destination != null && "loading".equals(destination.getName())) {
				destination = mapping.findForward("ajax_loading");
			}
		}
		return destination;
	}

	protected Future<PaginatedListImpl<AdminEntry>> getAdminlistFuture(HttpServletRequest request, StrutsFormBase form) throws NumberFormatException, IllegalAccessException, InstantiationException, InterruptedException, ExecutionException {
		ComAdminForm aForm = (ComAdminForm)form;
		int rownums = aForm.getNumberOfRows();
		// Sets value to max available for temporary disable pagination
		if (rownums == -1) {
			rownums = Integer.MAX_VALUE;
		}

		String direction = getDirection(request, form);
		request.getSession().setAttribute("admin_dir",direction);

		String sort = getAdminSort(request);

		String pageStr = getPage(request);

		if (aForm.isNumberOfRowsChanged()) {
			aForm.setPage("1");
			request.getSession().setAttribute("admin_page", "1");
			aForm.setNumberOfRowsChanged(false);
			pageStr = "1";
		}

		String searchFirstName = aForm.getSearchFirstName();
		String searchLastName = aForm.getSearchLastName();
		String searchEmail = aForm.getSearchEmail();
		String searchCompany = aForm.getSearchCompany();

		Integer filterCompanyId = null;
		Integer filterAdminGroupId = null;
		Integer filterMailinglistId = null;

		try {
			if (StringUtils.isNotBlank(aForm.getFilterCompanyId())) {
				filterCompanyId = Integer.parseInt(aForm.getFilterCompanyId());
			}
		} catch (NumberFormatException e) {
			logger.warn("Cannot convert company ID to integer value: " + aForm.getFilterCompanyId(), e);
		}

		try {
			// Try to parse content of property if and only if property is not blank
			if (StringUtils.isNotBlank(aForm.getFilterAdminGroupId())) {
				filterAdminGroupId = Integer.parseInt(aForm.getFilterAdminGroupId());
			}
		} catch (NumberFormatException e) {
			logger.warn("Cannot convert admin group ID to integer value: " + aForm.getFilterAdminGroupId(), e);
		}

		try {
			// Try to parse content of property if and only if property is not blank
			if (StringUtils.isNotBlank(aForm.getFilterMailinglistId())) {
				filterMailinglistId = Integer.parseInt(aForm.getFilterMailinglistId());
			}
		} catch (NumberFormatException e) {
			logger.warn("Cannot convert mailinglist ID to integer value: " + aForm.getFilterMailinglistId(), e);
		}
		String filterLanguage = aForm.getFilterLanguage();
		int companyID = AgnUtils.getCompanyID(request);
		Future<PaginatedListImpl<AdminEntry>> future = workerExecutorService.submit(new ComAdminListQueryWorker(adminService, companyID, searchFirstName, searchLastName, searchEmail, searchCompany, filterCompanyId, filterAdminGroupId, filterMailinglistId, filterLanguage, sort, direction, Integer.parseInt(pageStr), rownums));

		return future;
	}

	private String getDirection(HttpServletRequest request, StrutsFormBase form){
		String direction = request.getParameter("dir");
		if(direction == null){
			direction = form.getOrder();
		}
		if (direction.isEmpty()) {
			direction = request.getSession().getAttribute("admin_dir") == null ? "" : (String) request.getSession().getAttribute("admin_dir");
		}
		return direction;
	}

	private String getAdminSort(HttpServletRequest request){
		String sort = request.getParameter("sort");
		if (sort == null) {
			sort = request.getSession().getAttribute("admin_sort") == null ? "" : (String) request.getSession().getAttribute("admin_sort");
		} else {
			request.getSession().setAttribute("admin_sort", sort);
		}
		return sort;
	}

	private String getPage(HttpServletRequest request){
		String pageStr = request.getParameter("page");
		if (pageStr == null || "".equals(pageStr.trim())) {
			pageStr = request.getSession().getAttribute("admin_page") == null ? "1" : (String) request.getSession().getAttribute("admin_page");
		} else {
			request.getSession().setAttribute("admin_page", pageStr);
		}
		return pageStr;
	}


	/**
	 * Method checks if username was changed to existing one.
	 * 
	 * @param aForm
	 *			the form
	 * @return true if username was changed to existing one; false - if the
	 *		 username was changed to none-existing or if the username was not
	 *		 changed at all
	 */
	protected boolean adminUsernameChangedToExisting(ComAdminForm aForm) {
		ComAdmin currentAdmin = adminService.getAdmin(aForm.getAdminID(), aForm.getCompanyID());
		if (currentAdmin.getUsername().equals(aForm.getUsername())) {
			return false;
		} else {
			return adminService.adminExists(aForm.getUsername());
		}
	}

	/**
	 * Check password and return result.
	 * This method should be uses in following usecases:
	 * 	1. A new admin is created. A password is required, so check it.
	 * 	2. An existing admin is updated. Password check is required, if and only if password is set in form (-> user requested password to be updated)
	 * 
	 * @param form FormBean used to retrieve admin and password data
	 * @param errors data structure to report errors found during validation of password
	 * 
	 * @return {@code true} if password is ok, otherwise {@code false}
	 */
	protected boolean checkPassword(ComAdminForm form, ActionMessages errors) {
		if (StringUtils.isNotEmpty(form.getPassword())) {
			ComAdmin admin = adminService.getAdmin(form.getAdminID(), form.getCompanyID());
			PasswordCheckHandler handler = new StrutsPasswordCheckHandler(errors, "password");
			if (admin != null && admin.getAdminID() != 0) {
				// Existing user changes his password
				return passwordCheck.checkAdminPassword(form.getPassword(), admin, handler);
			} else {
				// New user changes wants to set his initial password
				return passwordCheck.checkAdminPassword(form.getPassword(), null, handler);
			}
		} else {
			// No password or empty password is always invalid
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.password.general"));
			return false;
		}
	}

	/**
	 * Save the permission for an admin.
	 * Gets the permissions for the admin from the form and stores it in the database.
	 * 
	 * Rules for changing admin rights:
	 * - Rights granted by the admingroup cannot be changed in anyway (Change admin's group itself if needed to do so)
	 * - Standard rights can be changed in anyway by any GUI user, who has the right to change admin rights
	 * - Premium rights can only be changed, if the GUI user has the specific premium right himself and has the right to change admin rights
	 * - "Others" rights and rights of unknown categories can only be changed by emm-master
	 * 
	 * For information on rules for changing user rights, see also:
	 *  http://wiki.agnitas.local/doku.php?id=abteilung:allgemein:premiumfeatures&s[]=rechtevergabe#rechtevergabe-moeglichkeiten_in_der_emm-gui
	 *
	 * @param admin current admin.
	 * @param aForm the form passed from the client.
	 * @param errors storage for error messages (if any) to be shown to user.
	 *
	 * @throws Exception 
	 */
	protected void saveAdminRights(ComAdmin admin, ComAdminForm aForm, ActionMessages errors) throws Exception {
		if (!admin.permissionAllowed(Permission.ADMIN_CHANGE)) {
			throw new Exception("Missing permission to change rights");
		}

		try {
			Tuple<List<String>, List<String>> changes = adminService.saveAdminPermissions(admin.getCompanyID(), aForm.getAdminID(), aForm.getUserRights(), admin.getAdminID());

			if (changes == null) {
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.admin.change.permission"));
			} else {
				ComAdmin savingAdmin = adminService.getAdmin(aForm.getAdminID(), admin.getCompanyID());

				String action = String.format("User: \"%s\"(%d).", savingAdmin.getUsername(), savingAdmin.getAdminID());
				String added = StringUtils.join(changes.getFirst(), ", ");
				String removed = StringUtils.join(changes.getSecond(), ", ");

				if (!added.isEmpty()) {
					writeUserActivityLog(admin, "edit user", action + " Added permissions: " + added, logger);
				}
				if (!removed.isEmpty()) {
					writeUserActivityLog(admin, "edit user", action + " Removed permissions: " + removed, logger);
				}
			}
		} catch (Exception e) {
			logger.error("Cannot save rights for user with ID: " + aForm.getAdminID(), e);
			throw e;
		}
	}

	protected void loadMailinglists(ComAdmin editedAdmin, ComAdminForm form){
		form.setUsername(editedAdmin.getUsername());
		form.setMailinglists(mailinglistService.getAllMailingListsNames(editedAdmin.getCompanyID()));
		form.getDisabledMailinglistsIds().clear();
		form.getDisabledMailinglistsIds().addAll(mailinglistApprovalService.getDisabledMailinglistsForAdmin(editedAdmin.getCompanyID(), form.getAdminID()));
	}

	protected void loadMailinglists(ComAdminForm form, HttpServletRequest request){
		ComAdmin editedAdmin = adminService.getAdmin(form.getAdminID(), AgnUtils.getCompanyID(request));
		loadMailinglists(editedAdmin, form);
	}
	
	@Required
	public final void setPermissionFilter(final PermissionFilter filter) {
		this.permissionFilter = Objects.requireNonNull(filter, "Permission filter is null");
	}
}
