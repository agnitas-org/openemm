/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.taglib;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.navigation.ConditionsHandler;
import com.agnitas.emm.core.navigation.condition.NavItemCondition;
import com.agnitas.util.AgnUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *  Display the navigation for a page. the navigation is specified by a
 *  properties file in com.agnitas.util.properties.navigation in the format
 *  token_i, href_i, msg_i, where i determines the order in which navigation is
 *  displayed.
 */
public class ShowNavigationTag extends BodyTagSupport {

	private static final long serialVersionUID = 6251408976255583139L;

	private static final Logger logger = LogManager.getLogger(ShowNavigationTag.class);

	private String navigation;
	private String highlightKey;
	private String prefix;

	private final List<NavigationData> navigationDataList = new Vector<>();
	private transient Iterator<NavigationData> navigationDataIterator;
	private int navigationIndex;
	private Integer companyId;
	private transient ConfigService configService;

	private record NavigationData(
			String message,
			String token,
			String href,
			Boolean hideForMysqlKey,
			String iconClass,
			String subMenu,
			String hideForToken,
			String itemClass,
			String upsellingRef,
			boolean conditionSatisfied,
			String conditionMessage
	) {
		@Override
		public String toString() {
			return "message[" + message() + "], token[" + token() + "], " +
					"hideForMysqlKey[" + hideForMysqlKey().toString() + "], iconClass[" + iconClass()
					+ "], href[" + href()
					+ "], subMenu[" + (subMenu != null ? subMenu : "")
					+ "], upsellingRef[" + (upsellingRef != null ? upsellingRef : "")
					+ "], conditionId[" + conditionSatisfied()
					+ "], conditionMessage[" + conditionMessage()
					+ "], itemClass[" + itemClass() + "]";
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

	@Override
	public int doStartTag() {
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
	public int doAfterBody() {
		if (navigationDataIterator.hasNext()) {
			setBodyAttributes();
			return EVAL_BODY_BUFFERED; // EVAL_BODY_AGAIN
		} else {
			return SKIP_BODY;
		}
	}

	@Override
	public int doEndTag() {
		// Emit body content
		try {
			BodyContent currentBodyContent = getBodyContent();

			if (currentBodyContent != null && !navigationDataList.isEmpty()) {
				currentBodyContent.writeOut(getPreviousOut());
			}

		} catch (IOException e) {
            logger.error("Error in ShowNavigationTag.doEndTag: {}", e.getMessage(), e);
		}
		return EVAL_PAGE;
	}

	private void prepareNavigationData() {
		navigationDataList.clear();

		try {
			ResourceBundle bundle;

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

			prepareNavigationDataFromResourceBundle(bundle);
		} catch (Exception e) {
            logger.error("Error preparing navigation data from extension: {}", e.getMessage(), e);
		}
	}

	private void prepareNavigationDataFromResourceBundle(ResourceBundle resourceBundle) {
        for (int i = 1;; i++) {
            String msgKey = "msg_" + i;
            if (!resourceBundle.containsKey(msgKey)) {
            	break;
            }

            String hrefKey = "href_" + i;
            String hideForMysqlKey = "hideForMysql_" + i;
            String iconClass = "iconClass_" + i;
            String itemClass = "itemClass_" + i;
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

			NavItemCondition.ConditionResult conditionResult = checkCondition(getDataQuietly(resourceBundle, conditionId));

			String upsellingValue = getUpsellingValue(resourceBundle, upsellingRef);

			NavigationData navigationData = new NavigationData(resourceBundle.getString(msgKey), securityToken,
                resourceBundle.getString(hrefKey),
				getDataQuietly(resourceBundle, hideForMysqlKey).equals("true"), getDataQuietly(resourceBundle, iconClass),
                getDataQuietly(resourceBundle, subMenu), getDataQuietly(resourceBundle, hideForToken), getDataQuietly(resourceBundle, itemClass),
				upsellingValue, conditionResult.isSatisfied(), conditionResult.getMessage());

            navigationDataList.add(navigationData);
        }
    }

	private String getUpsellingValue(ResourceBundle resourceBundle, String upsellingRef) {
		if (!getConfigValue().getBooleanValue(ConfigValue.UpsellingShow, getCompanyId())) {
			return "";
		}
		return getDataQuietly(resourceBundle, upsellingRef);
	}

	private int getCompanyId() {
		if (companyId == null) {
			companyId = AgnUtils.getCompanyID((HttpServletRequest) pageContext.getRequest());
		}
		return companyId;
	}

	private ConfigService getConfigValue() {
		if (configService == null) {
			configService = WebApplicationContextUtils.getRequiredWebApplicationContext(pageContext.getServletContext())
				.getBean(ConfigService.class);
		}
		return configService;
	}

	private String getDataQuietly(ResourceBundle resourceBundle, String key) {
        try {
            return resourceBundle.getString(key);
        } catch (MissingResourceException e) {
            return "";
        }
    }

    private NavItemCondition.ConditionResult checkCondition(String conditionId){
	    if (StringUtils.isBlank(conditionId)){
	        return new NavItemCondition.ConditionResult(true);
        }
        final ConditionsHandler conditionsHandler = WebApplicationContextUtils.getRequiredWebApplicationContext(pageContext.getServletContext()).getBean(ConditionsHandler.class);
		return conditionsHandler.checkCondition(conditionId, (HttpServletRequest) pageContext.getRequest());
    }

	private void setBodyAttributes() {
		NavigationData navigationData = navigationDataIterator.next();
		navigationIndex++;

		logger.trace("setting navigation attributes {}_navigation_href = {}", prefix, navigationData.href());

		if (StringUtils.isNotBlank(highlightKey) && Strings.CS.equals(navigationData.message(), highlightKey)) {
            pageContext.setAttribute(prefix + "_navigation_isHighlightKey", Boolean.TRUE);
        } else {
            pageContext.setAttribute(prefix + "_navigation_isHighlightKey", Boolean.FALSE);
        }

        pageContext.setAttribute(prefix + "_navigation_token", StringUtils.trimToEmpty(navigationData.token()));
        pageContext.setAttribute(prefix + "_navigation_href", StringUtils.trimToEmpty(navigationData.href()));
        pageContext.setAttribute(prefix + "_navigation_navMsg", StringUtils.trimToEmpty(navigationData.message()));
        pageContext.setAttribute(prefix + "_navigation_index", navigationIndex);
        if (navigationData.conditionSatisfied) {
            pageContext.setAttribute(prefix + "_navigation_iconClass", StringUtils.trimToEmpty(navigationData.iconClass()));
            pageContext.setAttribute(prefix + "_navigation_itemClass", StringUtils.trimToEmpty(navigationData.itemClass()));
        }
		pageContext.setAttribute(prefix + "_navigation_submenu", StringUtils.trimToEmpty(navigationData.subMenu()));
		pageContext.setAttribute(prefix + "_navigation_hideForToken", StringUtils.trimToEmpty(navigationData.hideForToken()));
		pageContext.setAttribute(prefix + "_navigation_upsellingRef", StringUtils.trimToEmpty(navigationData.upsellingRef()));
        pageContext.setAttribute(prefix + "_navigation_conditionSatisfied", navigationData.conditionSatisfied());
        pageContext.setAttribute(prefix + "_navigation_conditionMsg", navigationData.conditionMessage());
	}
}
