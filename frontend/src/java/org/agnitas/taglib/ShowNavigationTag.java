/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.taglib;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.agnitas.emm.core.Permission;
import org.agnitas.emm.core.navigation.ConditionsHandler;
import org.agnitas.emm.extension.ExtensionSystem;
import org.agnitas.emm.extension.util.ExtensionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.java.plugin.registry.Extension;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *  Display the navigation for a page. the navigation is specified by a
 *  properties file in org.agnitas.util.properties.navigation in the format
 *  token_i, href_i, msg_i, where i determines the order in which navigation is
 *  displayed.
 */
public class ShowNavigationTag extends BodyTagSupport {
	private static final long serialVersionUID = 6251408976255583139L;

	private static final transient Logger logger = Logger.getLogger(ShowNavigationTag.class);

	private String navigation;
	private String highlightKey;
	private String prefix;
	private String declaringPlugin;
	private String extension;

	private final List<NavigationData> navigationDataList = new Vector<>();
	private Iterator<NavigationData> navigationDataIterator;
	private int navigationIndex;

	private static class NavigationData {
		private final String message;
		private final String token;
		private final String href;
        private final Boolean hideForMysqlKey;
        private final String iconClass;
		private final String plugin;
		private final String extension;
		private final String subMenu;
		private final String hideForToken;
		private final String upsellingRef;
		private final boolean conditionSatisfied;

		public NavigationData(String message, String token, String href, Boolean hideForMysqlKey, String iconClass,
							  String plugin, String extension, String subMenu, String hideForToken,
							  String upsellingRef, boolean conditionSatisfied) {
            this.message = message;
            this.token = token;
            this.href = href;
            this.hideForMysqlKey = hideForMysqlKey;
            this.iconClass = iconClass;
            this.plugin = plugin;
            this.extension = extension;
			this.subMenu = subMenu;
			this.hideForToken = hideForToken;
			this.upsellingRef = upsellingRef;
			this.conditionSatisfied = conditionSatisfied;
		}

        public String getMessage() { return message; }
        public String getToken() { return token; }
        public String getHref() { return href; }
        public Boolean getHideForMysqlKey() { return hideForMysqlKey; }
        public String getIconClass() { return iconClass; }
        public String getPlugin() { return plugin; }
        public String getExtension() { return extension; }
		public String getSubMenu() { return subMenu; }
		public String getHideForToken() { return hideForToken; }
		public String getUpsellingRef() { return upsellingRef; }
        public boolean isConditionSatisfied() { return conditionSatisfied; }

        @Override
        public String toString() {
            return "message[" + getMessage() + "], token[" + getToken() + "], " +
					"hideForMysqlKey[" + getHideForMysqlKey().toString() + "], iconClass[" + getIconClass()
                    + "], href[" + getHref() + "], plugin[" + (plugin !=  null ? plugin : "")
                    + "], extension[" + (extension != null ? extension : "")
					+ "], subMenu[" + (subMenu != null ? subMenu : "")
					+ "], upsellingRef[" + (upsellingRef != null ? upsellingRef : "")
                    + "], conditionId[" + isConditionSatisfied() + "]";
        }
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setNavigation(String navigation) {
		this.navigation = navigation;
	}

	public void setHighlightKey(String highlightKey) {
		this.highlightKey = highlightKey;
	}

	public void setPlugin(String declaringPlugin) {
		this.declaringPlugin = declaringPlugin;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	@Override
	public int doStartTag() throws JspException {
		prepareNavigationData();

		navigationDataIterator = navigationDataList.iterator();
		navigationIndex = 0;

		if (prefix == null) {
			prefix = "";
		}

		if (navigationDataIterator.hasNext()) {
			setBodyAttributes();
			return EVAL_BODY_BUFFERED;
		} else {
			return SKIP_BODY;
		}
	}

	@Override
	public int doAfterBody() throws JspException {
		if (navigationDataIterator.hasNext()) {
			setBodyAttributes();
			return EVAL_BODY_BUFFERED; // EVAL_BODY_AGAIN;
		} else {
			return SKIP_BODY;
		}
	}

	@Override
	public int doEndTag() throws JspException {
		// Reset optional attribute value
		declaringPlugin = null;
		extension = null;

		// Emit body content
		try {
			BodyContent currentBodyContent = getBodyContent();

			if (currentBodyContent != null) {
				if (!navigationDataList.isEmpty()) {
					currentBodyContent.writeOut(getPreviousOut());
				}
				currentBodyContent.clearBody();
			}
		} catch (IOException e) {
			logger.error("Error in ShowNavigationTag.doEndTag: " + e.getMessage(), e);
		}
		return EVAL_PAGE;
	}

	private void prepareNavigationData() {
		navigationDataList.clear();

		try {
			ResourceBundle bundle;

    		if (StringUtils.isEmpty(declaringPlugin)) {
    			if (StringUtils.isEmpty(navigation)) {
    				return;
				}

    			try {
					bundle = ResourceBundle.getBundle("navigation." + navigation);
				} catch (Exception e) {
					if (navigation.endsWith("Sub")) {
						// no such submenu properties file found => no submenu
						return;
					} else {
						throw e;
					}
				}

    			prepareNavigationDataFromResourceBundle(bundle, null, null);
    		} else {
				bundle = getExtensionResourceBundle(declaringPlugin, navigation);
	    		prepareNavigationDataFromResourceBundle(bundle, declaringPlugin, extension);
			}

    		prepareNavigationDataFromExtensionPoints(bundle);
		} catch (Exception e) {
			logger.error("Error preparing navigation data from extension: " + e.getMessage(), e);
		}
	}

	private ResourceBundle getExtensionResourceBundle(String extensionItem, String resourceName) throws Exception {
		ExtensionSystem extensionSystem = ExtensionUtils.getExtensionSystem(pageContext.getServletContext());

		return extensionSystem.getPluginResourceBundle(extensionItem, resourceName);
	}

	private void prepareNavigationDataFromResourceBundle(ResourceBundle resourceBundle, String plugin, String extensionItem) {
		if (logger.isDebugEnabled()) {
			logger.debug("Processing navigation resource bundle for plugin: " + (plugin != null ? plugin : "core system"));
		}

        for (int i = 1;; i++) {
            String msgKey = "msg_" + i;
            if (!resourceBundle.containsKey(msgKey)) {
            	break;
            }

            String hrefKey = "href_" + i;
            String hideForMysqlKey = "hideForMysql_" + i;
            String iconClass = "iconClass_" + i;
            String subMenu = "submenu_" + i;
            String upsellingRef = "upsellingRef_" + i;

			String securityToken;
        	String tokenKey = "token_" + i;
			if (resourceBundle.containsKey(tokenKey)) {
				securityToken = resourceBundle.getString(tokenKey);
			} else {
				// default token => no permission needed
				securityToken = Permission.ALWAYS_ALLOWED.toString();
			}

			//Remove after finishing migration
			String hideForToken = "hideForToken_" + i;

			String conditionId = "conditionId_" + i;

			if (logger.isInfoEnabled()) {
				logger.info("extension '" + extensionItem + "' in plugin '" + plugin + "' added menu item. Label key is: " + msgKey);
			}

            NavigationData navigationData = new NavigationData(resourceBundle.getString(msgKey), securityToken,
                resourceBundle.getString(hrefKey),
				getDataQuietly(resourceBundle, hideForMysqlKey).equals("true"), getDataQuietly(resourceBundle, iconClass),
                plugin, extensionItem, getDataQuietly(resourceBundle, subMenu), getDataQuietly(resourceBundle, hideForToken),
				getDataQuietly(resourceBundle, upsellingRef), isConditionSatisfied(getDataQuietly(resourceBundle, conditionId)));

            navigationDataList.add(navigationData);
        }
    }

    private String getDataQuietly(ResourceBundle resourceBundle, String key) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return "";
        }
    }

    private boolean isConditionSatisfied(String conditionId){
	    if(StringUtils.isBlank(conditionId)){
	        return true;
        }
        final ConditionsHandler conditionsHandler = WebApplicationContextUtils.getRequiredWebApplicationContext(pageContext.getServletContext()).getBean(ConditionsHandler.class);
        if(conditionsHandler == null){
            logger.error("Conditions handler is not allowed!!!");
            return false;
        }
        return conditionsHandler.checkCondition(conditionId);
    }

	private void prepareNavigationDataFromExtensionPoints(ResourceBundle resourceBundle) throws Exception {
		if (!resourceBundle.containsKey("navigation.plugin") || !resourceBundle.containsKey("navigation.extensionpoint")) {
			return;
		}

		//getting data from navigation.property file for the extension point
		String plugin = resourceBundle.getString("navigation.plugin");
		String extensionPoint = resourceBundle.getString("navigation.extensionpoint");

		ExtensionSystem extensionSystem = ExtensionUtils.getExtensionSystem(pageContext.getServletContext());
		if(extensionSystem != null) {
			Collection<Extension> extensions = extensionSystem.getActiveExtensions(plugin, extensionPoint);
			for (Extension extensionItem : extensions) {
				String resourceName = extensionItem.getParameter("navigation-bundle").valueAsString();

				ResourceBundle extensionBundle = extensionSystem.getPluginResourceBundle(extensionItem.getDeclaringPluginDescriptor().getId(), resourceName);
				prepareNavigationDataFromResourceBundle(extensionBundle, extensionItem.getDeclaringPluginDescriptor().getId(), extensionItem.getId());
			}
		} else {
			logger.warn("No active Navigation extensions for plugin '" + plugin + "' defined");
		}
	}

	private void setBodyAttributes() {
		NavigationData navigationData = navigationDataIterator.next();
		navigationIndex++;

		logger.info("setting navigation attributes " + prefix + "_navigation_href = " + navigationData.getHref());

		if (StringUtils.isNotBlank(highlightKey) && StringUtils.equals(navigationData.getMessage(), highlightKey)) {
            pageContext.setAttribute(prefix + "_navigation_switch", "on");
            pageContext.setAttribute(prefix + "_navigation_isHighlightKey", Boolean.TRUE);
        } else {
            pageContext.setAttribute(prefix + "_navigation_switch", "off");
            pageContext.setAttribute(prefix + "_navigation_isHighlightKey", Boolean.FALSE);
        }

        pageContext.setAttribute(prefix + "_navigation_token", StringUtils.trimToEmpty(navigationData.getToken()));
        pageContext.setAttribute(prefix + "_navigation_href", StringUtils.trimToEmpty(navigationData.getHref()));
        pageContext.setAttribute(prefix + "_navigation_navMsg", StringUtils.trimToEmpty(navigationData.getMessage()));
        pageContext.setAttribute(prefix + "_navigation_index", navigationIndex);
        pageContext.setAttribute(prefix + "_navigation_plugin", StringUtils.trimToEmpty(navigationData.getPlugin()));
        pageContext.setAttribute(prefix + "_navigation_extension", StringUtils.trimToEmpty(navigationData.getExtension()));
        pageContext.setAttribute(prefix + "_navigation_iconClass", StringUtils.trimToEmpty(navigationData.getIconClass()));
		pageContext.setAttribute(prefix + "_navigation_submenu", StringUtils.trimToEmpty(navigationData.getSubMenu()));
		pageContext.setAttribute(prefix + "_navigation_hideForToken", StringUtils.trimToEmpty(navigationData.getHideForToken()));
		pageContext.setAttribute(prefix + "_navigation_upsellingRef", StringUtils.trimToEmpty(navigationData.getUpsellingRef()));
        pageContext.setAttribute(prefix + "_navigation_conditionSatisfied", navigationData.isConditionSatisfied());
	}
}
