/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.userform.trackablelinks.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.web.BaseTrackableLinkForm;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.LinkProperty.PropertyType;
import com.agnitas.dao.UserFormDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.userform.bean.UserForm;
import com.agnitas.userform.trackablelinks.bean.ComTrackableUserFormLink;

/**
 * Implementation of <strong>Action</strong> for user form trackable links
 */
public class ComTrackableUserFormLinkAction extends DispatchAction {
	private static final transient Logger logger = Logger.getLogger(ComTrackableUserFormLinkAction.class);

	private UserFormDao userFormDao = null;

	protected ConfigService configService;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	/**
	 * For obtaining trackable user form list
	 *
	 * @param mapping
	 *            - action mapping
	 * @param form
	 *            - action form
	 * @param request
	 *            - HTTP request object
	 * @param response
	 *            - HTTP response object
	 * @return
	 * @throws Exception
	 */
	public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting action method list");
		}

		// Validate the request parameters specified by the user
		ComTrackableUserFormLinkForm aForm = null;
		ActionForward destination = mapping.findForward("list");

		ComAdmin admin = AgnUtils.getAdmin(request);

		if (admin == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Finished action method list - not logged in");
			}

			return mapping.findForward("logon");
		} else {
			ActionMessages errors = new ActionMessages();
			ActionMessages actionMessages = new ActionMessages();

			aForm = (ComTrackableUserFormLinkForm) form;


			if (logger.isInfoEnabled()) {
				logger.info("Action: " + aForm.getAction());
			}

			try {
				loadUserFormData(admin, aForm, request);
			} catch (Exception e) {
				logger.error("execute: " + e, e);
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			}

			// Report any errors we have discovered back to the original form
			if (!errors.isEmpty()) {
				saveErrors(request, errors);
				logger.error("saving errors: " + destination);
			}

			// Report any message (non-errors) we have discovered
			if (!actionMessages.isEmpty()) {
				saveMessages(request, actionMessages);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Finished action method list");
			}

			

			return destination;
		}
	}
    
    private void loadLinkItemsData(ComTrackableUserFormLinkForm aForm, Collection<ComTrackableUserFormLink> links) {
	    links.forEach(link -> {
	        int id = link.getId();
	        aForm.setLinkItemName(id, link.getShortname());
	        aForm.setLinkItemRelevance(id, link.getRelevance());
	        aForm.setLinkItemUsage(id, link.getUsage());
        });
    }
    
    /**
	 * For obtaining trackable user link view
	 *
	 * @param mapping
	 *            - action mapping
	 * @param form
	 *            - action form
	 * @param request
	 *            - HTTP request object
	 * @param response
	 *            - HTTP response object
	 * @return
	 * @throws Exception
	 */
	public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting action method view");
		}

		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			return mapping.findForward("logon");
		} else {
			ComTrackableUserFormLinkForm aForm = (ComTrackableUserFormLinkForm) form;
			ComTrackableUserFormLink aLink = userFormDao.getUserFormTrackableLink(aForm.getLinkID());

			if (aLink != null) {
				aForm.setLinkToView(aLink);
				aForm.setLinkName(aLink.getShortname());
				aForm.setTrackable(aLink.getUsage());
				aForm.setLinkUrl(aLink.getFullUrl());
				aForm.setLinkAction(aLink.getActionID());
				aForm.setRelevance(aLink.getRelevance());
				aForm.setDeepTracking(aLink.getDeepTracking());
				aForm.setRelevance(aLink.getRelevance());
				// only if parameter is provided in form
				if (request.getParameter("deepTracking") != null) {
					aForm.setDeepTracking(aLink.getDeepTracking());
				}
			} else {
				logger.error("could not load link: " + aForm.getLinkID());
			}

            UserForm userForm = userFormDao.getUserForm(aForm.getFormID(), AgnUtils.getCompanyID(request));
            if (userForm != null) {
                aForm.setShortname(userForm.getFormName());
            }

			ActionForward destination = mapping.findForward("view");

			if (logger.isDebugEnabled()) {
				logger.debug("Finished action method view");
			}

			return destination;
		}
	}

	/**
	 * For saving a trackable user link
	 *
	 * @param mapping
	 *            - action mapping
	 * @param form
	 *            - action form
	 * @param request
	 *            - HTTP request object
	 * @param response
	 *            - HTTP response object
	 * @return
	 * @throws Exception
	 */
	public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting action method save");
		}

		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			return mapping.findForward("logon");
		} else {
			ActionMessages errors = new ActionMessages();
			ActionMessages actionMessages = new ActionMessages();

			ComTrackableUserFormLinkForm comTrackableUserFormLinkForm = (ComTrackableUserFormLinkForm) form;
			ActionForward destination = mapping.findForward("list");

			try {
				saveLink(comTrackableUserFormLinkForm, request);
				loadUserFormData(admin, comTrackableUserFormLinkForm, request);
				actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
			} catch (Exception e) {
				logger.error("execute: " + e, e);
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			}

			// Report any errors we have discovered back to the original form
			if (!errors.isEmpty()) {
				saveErrors(request, errors);
				logger.error("saving errors: " + destination);
			}
			
			if (!actionMessages.isEmpty()) {
				saveMessages(request, actionMessages);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Finished action method save");
			}

			return destination;
		}
	}

	public ActionForward saveLinks(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting action method save");
		}

		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			return mapping.findForward("logon");
		} else {
			ActionMessages errors = new ActionMessages();
			ActionMessages actionMessages = new ActionMessages();

			ComTrackableUserFormLinkForm comTrackableUserFormLinkForm = (ComTrackableUserFormLinkForm) form;
			ActionForward destination = mapping.findForward("list");

			try {
				saveLinks(comTrackableUserFormLinkForm, request);
				loadUserFormData(admin, comTrackableUserFormLinkForm, request);
				actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
			} catch (Exception e) {
				logger.error("execute: " + e, e);
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			}

			// Report any errors we have discovered back to the original form
			if (!errors.isEmpty()) {
				saveErrors(request, errors);
				logger.error("saving errors: " + destination);
			}
			
			if (!actionMessages.isEmpty()) {
				saveMessages(request, actionMessages);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Finished action method save");
			}

			return destination;
		}
	}
	
	public void saveLinks(ComTrackableUserFormLinkForm form, HttpServletRequest request) throws Exception {
		for (int linkId: form.getBulkIDs()) {
			ComTrackableUserFormLink userFormLink = userFormDao.getUserFormTrackableLink(linkId);
			userFormLink.setShortname(form.getLinkItemName(linkId));
			userFormLink.setRelevance(form.getLinkItemRelevance(linkId));

			//set default link tracking option
			//set common usage if it's enabled
			int usage = form.getTrackable();
			if(usage < 0) {
				usage = form.getLinkItemUsage(linkId);
			}
			userFormLink.setUsage(usage);

			userFormDao.storeUserFormTrackableLink(userFormLink);
		}

		saveLinkExtensionToFormular(form, request);
		replaceCommonLinkProperties(form, request);
	}

	/**
	 * For setting the standard usage
	 *
	 * @param mapping
	 *            - action mapping
	 * @param actionForm
	 *            - action form
	 * @param request
	 *            - HTTP request object
	 * @param response
	 *            - HTTP response object
	 * @return
	 * @throws Exception
	 */
	public ActionForward setStandardUsage(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting action method setStandardMeasurement");
		}

		ActionForward destination = mapping.findForward("list");
		ComTrackableUserFormLinkForm form = (ComTrackableUserFormLinkForm) actionForm;

		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Finished action method setStandardMeasurement");
			}
			return mapping.findForward("logon");
		} else {
			ActionMessages errors = new ActionMessages();

			try {
				UserForm userForm = userFormDao.getUserForm(form.getFormID(), AgnUtils.getCompanyID(request));
				try {
					for (ComTrackableUserFormLink aLink : userForm.getTrackableLinks().values()) {
						aLink.setUsage(form.getTrackable());
						userFormDao.storeUserFormTrackableLink(aLink);
					}
				} catch (Exception e) {
					errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
					logger.error("execute: " + e, e);
				}
				loadUserFormData(admin, form, request);
			} catch (Exception e) {
				logger.error("execute: " + e, e);
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			}

			// Report any errors we have discovered back to the original form
			if (!errors.isEmpty()) {
				saveErrors(request, errors);
				logger.error("saving errors: " + destination);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Finished action method setStandardMeasurement");
			}

			return destination;
		}
	}

	/**
	 * NOT CURRENTLY USED IN FRONT END For setting the standard action
	 *
	 * @param mapping
	 *            - action mapping
	 * @param form
	 *            - action form
	 * @param request
	 *            - HTTP request object
	 * @param response
	 *            - HTTP response object
	 * @return
	 * @throws Exception
	 */
	public ActionForward setStandardAction(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting action method setStandardAction");
		}

		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			return mapping.findForward("logon");
		} else {
			ActionForward destination = mapping.findForward("list");
			ActionMessages errors = new ActionMessages();
			ComTrackableUserFormLinkForm comTrackableUserFormLinkForm = (ComTrackableUserFormLinkForm) form;
			try {
				setStandardAction(comTrackableUserFormLinkForm, request);
				loadUserFormData(admin, comTrackableUserFormLinkForm, request);

			} catch (Exception e) {
				logger.error("execute: " + e, e);
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			}

			// Report any errors we have discovered back to the original form
			if (!errors.isEmpty()) {
				saveErrors(request, errors);
				logger.error("saving errors: " + destination);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Finished action method setStandardAction");
			}

			return destination;
		}
	}

	/**
	 * NOT CURRENTLY USED IN FRONT END For setting the standard deeptracking
	 *
	 * @param mapping
	 *            - action mapping
	 * @param form
	 *            - action form
	 * @param request
	 *            - HTTP request object
	 * @param response
	 *            - HTTP response object
	 * @return
	 * @throws Exception
	 */
	public ActionForward setStandardDeeptracking(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting action method setStandardDeeptracking");
		}

		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			return mapping.findForward("logon");
		} else {
			ActionForward destination = mapping.findForward("list");
			ActionMessages errors = new ActionMessages();
			ComTrackableUserFormLinkForm comTrackableUserFormLinkForm = (ComTrackableUserFormLinkForm) form;
			try {
				setStandardDeeptracking(comTrackableUserFormLinkForm, request);
				loadUserFormData(admin, comTrackableUserFormLinkForm, request);

			} catch (Exception e) {
				logger.error("execute: " + e, e);
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			}

			// Report any errors we have discovered back to the original form
			if (!errors.isEmpty()) {
				saveErrors(request, errors);
				logger.error("saving errors: " + destination);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Finished action method setStandardDeeptracking");
			}

			return destination;
		}
	}

	/**
	 * For setting the standard extended link values
	 *
	 * @param mapping
	 *            - action mapping
	 * @param form
	 *            - action form
	 * @param request
	 *            - HTTP request object
	 * @param response
	 *            - HTTP response object
	 * @return
	 * @throws Exception
	 */
	public ActionForward setExtendLinks(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting action method extendLinks");
		}

		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			return mapping.findForward("logon");
		} else {
			ActionForward destination = mapping.findForward("list");
			ActionMessages errors = new ActionMessages();
			ComTrackableUserFormLinkForm comTrackableUserFormLinkForm = (ComTrackableUserFormLinkForm) form;
			try {
				saveExtendLinks(comTrackableUserFormLinkForm, request);
				saveLinkExtensionToFormular(comTrackableUserFormLinkForm, request);
				loadUserFormData(admin, comTrackableUserFormLinkForm, request);

			} catch (Exception e) {
				logger.error("execute: " + e, e);
				errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			}

			// Report any errors we have discovered back to the original form
			if (!errors.isEmpty()) {
				saveErrors(request, errors);
				logger.error("saving errors: " + destination);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Finished action method extendLinks");
			}

			return destination;
		}
	}

	public ActionForward addExtensions(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting action method addDefaultExtensions");
		}

		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			return mapping.findForward("logon");
		} else if (admin.permissionAllowed(Permission.MAILING_EXTEND_TRACKABLE_LINKS)) {
			// Only change link properties if adminuser is allowed to
			UserForm userForm = userFormDao.getUserForm(((ComTrackableUserFormLinkForm) form).getFormID(), admin.getCompanyID());
			if (userForm != null) {
				String extensionString = ((ComTrackableUserFormLinkForm) form).getLinkExtension();
				if (StringUtils.isNotBlank(extensionString)) {
					if (extensionString.startsWith("?")) {
						extensionString = extensionString.substring(1);
					}
					String[] extensionProperties = extensionString.split("&");
					for (String extensionProperty : extensionProperties) {
						String[] extensionPropertyData = extensionProperty.split("=");
						String extensionPropertyName = URLDecoder.decode(extensionPropertyData[0], "UTF-8");
						String extensionPropertyValue = "";
						if (extensionPropertyData.length > 1) {
							extensionPropertyValue = URLDecoder.decode(extensionPropertyData[1], "UTF-8");
						}

						for (ComTrackableUserFormLink link : userForm.getTrackableLinks().values()) {
							// Change link properties
							Set<LinkProperty> properties = new HashSet<>(link.getProperties());
							boolean changedProperty = false;
							for (LinkProperty property : properties) {
								if (property.getPropertyType() == PropertyType.LinkExtension && property.getPropertyName().equals(extensionPropertyName)) {
									property.setPropertyValue(extensionPropertyValue);
									changedProperty = true;
								}
							}
							if (!changedProperty) {
								LinkProperty newProperty = new LinkProperty(PropertyType.LinkExtension, extensionPropertyName, extensionPropertyValue);
								properties.add(newProperty);
							}
						}
					}

					// Store properties of all links in db
					for (ComTrackableUserFormLink link : userForm.getTrackableLinks().values()) {
						userFormDao.storeUserFormTrackableLinkProperties(link);
					}
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Finished action method addDefaultExtensions");
		}

		return list(mapping, form, request, response);
	}

	public ActionForward addDefaultExtensions(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting action method addDefaultExtensions");
		}

		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			return mapping.findForward("logon");
		} else if (admin.permissionAllowed(Permission.MAILING_EXTEND_TRACKABLE_LINKS)) {
			// Only change link properties if adminuser is allowed to
			UserForm userForm = userFormDao.getUserForm(((ComTrackableUserFormLinkForm) form).getFormID(), admin.getCompanyID());
			if (userForm != null) {
				String defaultExtensionString = configService.getValue(ConfigValue.DefaultLinkExtension, admin.getCompanyID());
				if (StringUtils.isNotBlank(defaultExtensionString)) {
					if (defaultExtensionString.startsWith("?")) {
						defaultExtensionString = defaultExtensionString.substring(1);
					}
					String[] extensionProperties = defaultExtensionString.split("&");
					for (String extensionProperty : extensionProperties) {
						final int eqIndex = extensionProperty.indexOf('=');
						final String[] extensionPropertyData = (eqIndex == -1) ? new String[] { extensionProperty, "" } : new String[] { extensionProperty.substring(0, eqIndex), extensionProperty.substring(eqIndex + 1) };

						String extensionPropertyName = URLDecoder.decode(extensionPropertyData[0], "UTF-8");
						String extensionPropertyValue = "";
						if (extensionPropertyData.length > 1) {
							extensionPropertyValue = URLDecoder.decode(extensionPropertyData[1], "UTF-8");
						}

						for (ComTrackableUserFormLink link : userForm.getTrackableLinks().values()) {
							// Change link properties
							Set<LinkProperty> properties = new HashSet<>(link.getProperties());
							boolean changedProperty = false;
							for (LinkProperty property : properties) {
								if (property.getPropertyType() == PropertyType.LinkExtension && property.getPropertyName().equals(extensionPropertyName)) {
									property.setPropertyValue(extensionPropertyValue);
									changedProperty = true;
								}
							}
							if (!changedProperty) {
								LinkProperty newProperty = new LinkProperty(PropertyType.LinkExtension, extensionPropertyName, extensionPropertyValue);
								properties.add(newProperty);
							}
						}
					}

					// Store properties of all links in db
					for (ComTrackableUserFormLink link : userForm.getTrackableLinks().values()) {
						userFormDao.storeUserFormTrackableLinkProperties(link);
					}
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Finished action method addDefaultExtensions");
		}

		return list(mapping, form, request, response);
	}

	private Map<String, String> getDefaultExtension(ComAdmin admin, UserForm userForm) throws UnsupportedEncodingException {
		String defaultExtensionString = configService.getValue(ConfigValue.DefaultLinkExtension, admin.getCompanyID());
		Map<String, String> defaultExtensions = new HashMap<>();
		if (StringUtils.isNotBlank(defaultExtensionString)) {
			if (defaultExtensionString.startsWith("?")) {
				defaultExtensionString = defaultExtensionString.substring(1);
			}

			String[] extensionProperties = defaultExtensionString.split("&");
			for (String extensionProperty : extensionProperties) {

				final int eqIndex = extensionProperty.indexOf('=');
				final String[] extensionPropertyData = (eqIndex == -1) ? new String[]{extensionProperty, ""} : new String[]{extensionProperty.substring(0, eqIndex), extensionProperty.substring(eqIndex + 1)};

				String extensionPropertyName = URLDecoder.decode(extensionPropertyData[0], "UTF-8");
				String extensionPropertyValue = "";

				if (extensionPropertyData.length > 1) {
					extensionPropertyValue = URLDecoder.decode(extensionPropertyData[1], "UTF-8");
				}
				defaultExtensions.put(extensionPropertyName, extensionPropertyValue);

				for (ComTrackableUserFormLink link : userForm.getTrackableLinks().values()) {
					// Change link properties
					Set<LinkProperty> properties = new HashSet<>(link.getProperties());
					boolean changedProperty = false;
					for (LinkProperty property : properties) {
						if (property.getPropertyType() == PropertyType.LinkExtension && property.getPropertyName().equals(extensionPropertyName)) {
							property.setPropertyValue(extensionPropertyValue);
							changedProperty = true;
						}
					}
					if (!changedProperty) {
						LinkProperty newProperty = new LinkProperty(PropertyType.LinkExtension, extensionPropertyName, extensionPropertyValue);
						properties.add(newProperty);
					}
				}
			}
		}

		return defaultExtensions;
	}

	public ActionForward removeAllExtensions(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting action method removeAllExtensions");
		}

		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin == null) {
			return mapping.findForward("logon");
		} else if (admin.permissionAllowed(Permission.MAILING_EXTEND_TRACKABLE_LINKS)) {
			// Only clear properties of all links in db if adminuser is allowed to
			UserForm userForm = userFormDao.getUserForm(((ComTrackableUserFormLinkForm) form).getFormID(), admin.getCompanyID());
			if (userForm != null) {
				for (ComTrackableUserFormLink link : userForm.getTrackableLinks().values()) {
					Set<LinkProperty> newProperties = new HashSet<>();
					for (LinkProperty property : link.getProperties()) {
						if (property.getPropertyType() != PropertyType.LinkExtension) {
							newProperties.add(property);
						}
					}
					link.setProperties(new ArrayList<>(newProperties));
					userFormDao.storeUserFormTrackableLinkProperties(link);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Finished action method removeAllExtensions");
		}

		return list(mapping, form, request, response);
	}

	private void replaceCommonLinkProperties(ComTrackableUserFormLinkForm form, HttpServletRequest request) throws Exception {
		ComAdmin admin = AgnUtils.getAdmin(request);
		if (admin.permissionAllowed(Permission.MAILING_EXTEND_TRACKABLE_LINKS)) {
			// Only clear properties of all links in db if adminuser is allowed to
			UserForm userForm = userFormDao.getUserForm(form.getFormID(), admin.getCompanyID());
			if (userForm != null) {
				for (ComTrackableUserFormLink link : userForm.getTrackableLinks().values()) {
					Set<LinkProperty> linkProperties = new HashSet<>(link.getProperties());

					// Remove all old commonLinkProperties
                    List<LinkProperty> common = new ArrayList<>(userForm.getCommonLinkExtensions());
                    common.retainAll(linkProperties);
                    linkProperties.removeAll(common);

                    updateLinkPropertiesParameters(request, linkProperties);
					link.setProperties(new ArrayList<>(linkProperties));
					userFormDao.storeUserFormTrackableLinkProperties(link);
				}
			}
		}
	}
	
	private void updateLinkPropertiesParameters(HttpServletRequest request, Set<LinkProperty> linkProperties) {
		Enumeration<String> parameterNamesEnum = request.getParameterNames();
		while (parameterNamesEnum.hasMoreElements()) {
			String parameterName = parameterNamesEnum.nextElement();
			if (parameterName.startsWith(BaseTrackableLinkForm.PROPERTY_NAME_PREFIX)) {
				int propertyID = Integer.parseInt(parameterName.substring(BaseTrackableLinkForm.PROPERTY_NAME_PREFIX.length()));
				String[] extensionNames = request.getParameterValues(parameterName);
				String[] extensionValues = request.getParameterValues(BaseTrackableLinkForm.PROPERTY_VALUE_PREFIX + propertyID);
				if (extensionNames != null && extensionNames.length > 0 && StringUtils.isNotBlank(extensionNames[0])) {
					LinkProperty newProperty;
					if (extensionValues != null && extensionValues.length > 0 && StringUtils.isNotBlank(extensionValues[0])) {
						newProperty = new LinkProperty(PropertyType.LinkExtension, extensionNames[0], extensionValues[0]);
					} else {
						newProperty = new LinkProperty(PropertyType.LinkExtension, extensionNames[0], "");
					}
					linkProperties.add(newProperty);
				}
			}
		}
	}

	/**
	 * helper function for setting extended links
	 *
	 * @param aForm
	 *            - action form
	 * @param req
	 *            - HTTP request object
	 * @throws Exception
	 */
	private void saveExtendLinks(ComTrackableUserFormLinkForm aForm, HttpServletRequest req) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting helper method saveExtendedLinks");
		}

		Set<Integer> indices = aForm.getLinkIndices();
		// int companyId = aForm.getCompanyID(req);
		int linkId;
		ComTrackableUserFormLink link;

		for (int index : indices) {
			linkId = aForm.getExtendLinkId(index);
			link = userFormDao.getUserFormTrackableLink(linkId);
			linkId = userFormDao.storeUserFormTrackableLink(link);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Finished helper method saveExtendedLinks");
		}
	}

	/**
	 * helper function for setting the link extension to the formula
	 *
	 * @param aForm
	 *            - action form
	 * @param req
	 *            - HTTP request object
	 * @throws Exception
	 */
	private void saveLinkExtensionToFormular(ComTrackableUserFormLinkForm aForm, HttpServletRequest req) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting helper method saveLinkExtensionToFormular");
		}

		int formId = aForm.getFormID();
		int companyId = AgnUtils.getCompanyID(req);
		UserForm userform = userFormDao.getUserForm(formId, companyId);
		userFormDao.storeUserForm(userform);

		if (logger.isDebugEnabled()) {
			logger.debug("Finished helper method saveLinkExtensionToFormular");
		}
	}

	/**
	 * Helper function for loading all links into the action form for a
	 * particular formula
	 *
	 * @param aForm
	 *            - action form
	 * @param req
	 *            - HTTP request object
	 * @throws Exception
	 */
	private void loadUserFormData(ComAdmin admin, ComTrackableUserFormLinkForm aForm, HttpServletRequest req) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting helper method loadUserFormData");
		}

		UserForm userForm = userFormDao.getUserForm(aForm.getFormID(), AgnUtils.getCompanyID(req));
		if (userForm != null) {
			aForm.setLinks(userForm.getTrackableLinks().values());
			loadLinkItemsData(aForm, userForm.getTrackableLinks().values());

			aForm.setShortname(userForm.getFormName());
			aForm.setCompanyHasDefaultLinkExtension(StringUtils.isNotBlank(configService.getValue(ConfigValue.DefaultLinkExtension, admin.getCompanyID())));

			aForm.setCommonLinkExtensions(userForm.getCommonLinkExtensions());

			// Fill textfield for simple changes
			StringBuilder commonLinkExtensionsText = new StringBuilder();
			for (LinkProperty linkProperty : userForm.getCommonLinkExtensions()) {
				if (commonLinkExtensionsText.length() > 0) {
					commonLinkExtensionsText.append("&");
				}
				commonLinkExtensionsText.append(linkProperty.getPropertyName());
				commonLinkExtensionsText.append("=");
				commonLinkExtensionsText.append(linkProperty.getPropertyValue() == null ? "" : linkProperty.getPropertyValue());
			}
			aForm.setLinkExtension(commonLinkExtensionsText.toString());
			
			Map<String, String> defaultExtensions = getDefaultExtension(admin, userForm);
			ObjectMapper mapper = new ObjectMapper();
			String JSON = mapper.writeValueAsString(defaultExtensions);
			req.setAttribute("defaultExtensions", JSON);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Finished helper method loadUserFormData");
		}
	}

	/**
	 * Saves link based on a link id found in the action form
	 *
	 * @param aForm
	 *            - action form
	 * @param req
	 *            - HTTP Request
	 * @throws Exception
	 */
	private void saveLink(ComTrackableUserFormLinkForm aForm, HttpServletRequest req) throws Exception {
		ComTrackableUserFormLink aLink = null;

		if (logger.isDebugEnabled()) {
			logger.debug("Starting helper method saveLink");
		}

		aLink = userFormDao.getUserFormTrackableLink(aForm.getLinkID());

		if (aLink != null) {
			aLink.setShortname(aForm.getLinkName());
			aLink.setUsage(aForm.getTrackable());
			aLink.setActionID(aForm.getLinkAction());
			aLink.setRelevance(aForm.getRelevance());

			// only if parameter is provided in form
			if (req.getParameter("deepTracking") != null) {
				aLink.setDeepTracking(aForm.getDeepTracking());
			}

			ComAdmin admin = AgnUtils.getAdmin(req);
			// Only change link properties if adminuser is allowed to
			if (admin != null && admin.permissionAllowed(Permission.MAILING_EXTEND_TRACKABLE_LINKS)) {
				// search for link properties
				Set<LinkProperty> linkProperties = new HashSet<>();
				updateLinkPropertiesParameters(req, linkProperties);
				aLink.setProperties(new ArrayList<>(linkProperties));
			}

			userFormDao.storeUserFormTrackableLink(aLink);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Finished helper method saveLink");
		}
	}

	/**
	 * Helper function for setting the standard action
	 *
	 * @param aForm
	 *            - action form
	 * @param req
	 *            - action request
	 * @throws Exception
	 */
	private void setStandardAction(ComTrackableUserFormLinkForm aForm, HttpServletRequest req) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting helper method setStandardAction");
		}

		ActionMessages errors = new ActionMessages();
		UserForm userForm = userFormDao.getUserForm(aForm.getFormID(), AgnUtils.getCompanyID(req));

		try {
			for (ComTrackableUserFormLink aLink : userForm.getTrackableLinks().values()) {
				aLink.setActionID(aForm.getLinkAction());
				userFormDao.storeUserFormTrackableLink(aLink);
			}
		} catch (Exception e) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
			logger.error("execute: " + e, e);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Finished helper method setStandardAction");
		}
	}

	/**
	 * Helper function for setting the standard deeptracking
	 *
	 * @param aForm
	 *            - action form
	 * @param req
	 *            - action request
	 * @throws Exception
	 */
	private void setStandardDeeptracking(ComTrackableUserFormLinkForm aForm, HttpServletRequest req) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting helper method setStandardDeeptracking");
		}

		ActionMessages errors = new ActionMessages();
		UserForm userForm = userFormDao.getUserForm(aForm.getFormID(), AgnUtils.getCompanyID(req));

		try {
			for (ComTrackableUserFormLink aLink : userForm.getTrackableLinks().values()) {
				aLink.setDeepTracking(aForm.getDeepTracking());
				userFormDao.storeUserFormTrackableLink(aLink);
			}
		} catch (Exception e) {
			logger.error("execute: " + e, e);
			errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.exception", configService.getValue(ConfigValue.SupportEmergencyUrl)));
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Finished helper method setStandardDeeptracking");
		}
	}

	public void setUserFormDao(UserFormDao userFormDao) {
		this.userFormDao = userFormDao;
	}
}
