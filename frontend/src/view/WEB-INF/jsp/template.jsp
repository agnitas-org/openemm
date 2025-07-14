<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.agnitas.emm.common.LicenseType"%>
<%@ page import="org.agnitas.emm.core.commons.util.ConfigValue" %>
<%@ page import="org.agnitas.emm.core.commons.util.ConfigService" %>
<%@ page import="com.agnitas.util.HelpUtil" %>
<%@ page import="com.agnitas.beans.EmmLayoutBase" %>
<%@ page import="com.agnitas.beans.AdminPreferences" %>
<%@ page import="com.agnitas.service.WebStorage" %>
<%@ page import="com.agnitas.util.AgnUtils" %>
<%@ page import="com.agnitas.emm.core.commons.web.ConnectionSpeedTestController" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="MENU_POSITION_LEFT" value="<%= EmmLayoutBase.MENU_POSITION_LEFT%>" scope="page"/>
<c:set var="MENU_POSITION_TOP" value="<%= EmmLayoutBase.MENU_POSITION_TOP%>" scope="page"/>
<c:set var="isWideSidebarWebStorageBundleKey" value="<%= WebStorage.IS_WIDE_SIDEBAR%>" scope="page"/>
<c:set var="DARK_MODE_THEME_TYPE" value="<%= EmmLayoutBase.ThemeType.DARK%>" scope="page"/>
<c:set var="DARK_MODE_THEME_TYPE_2" value="<%= EmmLayoutBase.ThemeType.DARK_CONTRAST%>" scope="page"/>
<c:set var="SPEED_TEST_RESOURCE_SIZE" value="<%= ConnectionSpeedTestController.SPEED_TEST_RESOURCE_SIZE%>" scope="page"/>
<c:set var="SLOW_CONNECTION_THRESHOLD_KBPS" value="<%= ConfigService.getInstance().getIntegerValue(ConfigValue.SlowConnectionThresholdKbps, AgnUtils.getCompanyID(request)) %>" scope="page"/>

<c:set var="isTabsMenuShown" value="true" scope="request"/>
<c:set var="isBreadcrumbsShown" value="false" scope="request"/>
<c:set var="hideDesignSwitch" value="false" scope="request" />

<c:url var="LOGOUT" value="/logout.action"/>

<tiles:insertAttribute name="page-setup"/>

<!--[if IE 9 ]>    <html class="ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html class=""> <!--<![endif]-->
<tiles:insertAttribute name="head-tag"/>

<emm:webStorage var="isWideSidebarBundle" key="${isWideSidebarWebStorageBundleKey}"/>
<body class="${isWideSidebarBundle.isTrue() ? 'wide-sidebar' : ''} ${emmLayoutBase.getThemeType() eq DARK_MODE_THEME_TYPE or  emmLayoutBase.getThemeType() eq DARK_MODE_THEME_TYPE_2 ? 'dark-theme' : ''}">

    <div class="loader">
        <i class="icon icon-refresh icon-spin"></i> <mvc:message code="default.loading" />
    </div>
    
    <!-- Header BEGIN -->
    <header class="l-header">
        <div id="wide-menu-toggle" data-action="expandMenu">
            <i class="icon icon-caret-right"></i>
            <i class="icon icon-caret-left"></i>
        </div>

        <ul class="header-nav">
            <li>
                <h1 class="headline">
                    <c:set var="isHeadLineShown" value="false"/>

                    <emm:ShowNavigation navigation="sidemenu" highlightKey='${sidemenu_active}'>
                        <c:if test="${_navigation_navMsg == agnTitleKey or (isBreadcrumbsShown and _navigation_navMsg == agnBreadcrumbsRootKey)}">
                            <c:set var="isHeadLineShown" value="true" />

                            <c:set var="agnHeadLineIconClass" value="${_navigation_iconClass}"/>

                            <c:set var="agnHeadLineFirstCrumb">
                                <mvc:message code="${_navigation_navMsg}" />
                            </c:set>
                        </c:if>
                    </emm:ShowNavigation>

                    <c:choose>
                        <c:when test="${isHeadLineShown and isBreadcrumbsShown}">
                            <c:set var="crumbs" value=""/>

                            <c:set var="crumbs">${crumbs}<li><i class="icon-fa5 icon-fa5-${agnHeadLineIconClass}"></i></li></c:set>
                            <c:choose>
                                <c:when test="${not empty agnBreadcrumbsRootUrl}">
                                    <c:set var="crumbs">${crumbs}<li class="js-ellipsis"><a href="${agnBreadcrumbsRootUrl}">${fn:trim(agnHeadLineFirstCrumb)}</a></li></c:set>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="crumbs">${crumbs}<li class="js-ellipsis">${fn:trim(agnHeadLineFirstCrumb)}</li></c:set>
                                </c:otherwise>
                            </c:choose>

                            <c:if test="${not empty agnBreadcrumbs}">
                                <c:forEach var="agnBreadcrumb" items="${agnBreadcrumbs}">
                                    <c:set var="agnBreadcrumb" value="${agnBreadcrumb.value}"/>

                                    <c:set var="crumb">
                                        <c:choose>
                                            <c:when test="${not empty agnBreadcrumb.textKey}">
	                                            <mvc:message code="${agnBreadcrumb.textKey}"/>
                                            </c:when>
                                            <c:when test="${not empty agnBreadcrumb.text}">
                                                ${fn:escapeXml(agnBreadcrumb.text)}
                                            </c:when>
                                        </c:choose>
                                    </c:set>

                                    <c:if test="${not empty agnBreadcrumb.url}">
                                        <c:set var="crumb"><a href="${fn:trim(agnBreadcrumb.url)}">${fn:trim(crumb)}</a></c:set>
                                    </c:if>

                                    <c:set var="crumbs">${crumbs}<li class="js-ellipsis">${fn:trim(crumb)}</li></c:set>
                                </c:forEach>
                            </c:if>

                            <ul class="breadcrumbs js-ellipsis">${crumbs}</ul>
                        </c:when>
                        <c:when test="${isHeadLineShown}">
                            <i class="icon-fa5 icon-fa5-${agnHeadLineIconClass}"></i>
                            ${agnHeadLineFirstCrumb}
                        </c:when>
                        <c:otherwise>
                            ${agnSubtitleValue}
                        </c:otherwise>
                    </c:choose>
                </h1>
            </li>
        </ul>
        <ul class="header-actions">
            <li>
                <c:set var="helpPageUrl" value="<%= HelpUtil.getHelpPageUrl(request) %>" />
                <button class="btn btn-secondary btn-regular" type="button" data-popup="${helpPageUrl}">
                    <i class="icon icon-question-circle"></i>
                    <span class="text"><mvc:message code="help"/></span>
                </button>
            </li>
            <%@ include file="itemactions-support.jspf" %>
            <tiles:insertAttribute name="newresource"/>
            <tiles:insertAttribute name="itemactions"/>
        </ul>
    </header>
    <!-- Header END -->

    <!-- Nav BEGIN -->
    <aside class="l-sidebar">
        <%@ include file="logo.jspf" %>

        <ul class="l-menu scrollable js-scrollable" data-controller="sidemenu">
            <%-- init data for transfering on frontend side --%>
            <c:set var="creationTime" value="${pageContext.session.creationTime}"/>
            <c:set var="lastAccessedTime" value="${pageContext.session.lastAccessedTime}"/>
            <c:set var="maxInactiveInterval" value="${pageContext.session.maxInactiveInterval}"/>
            <c:url var="sessionInfoUrl" value="/session/info.action" />
            <c:set var="sessionTimeOnMouseOverMessage">
                <mvc:message code="session.timer.mouseover"/>
            </c:set>

            <%-- data injection into client --%>
            <script data-initializer="session-timer" type="application/json">
                {
                    "creationTime": "${creationTime}",
                    "lastAccessedTime": "${lastAccessedTime}",
                    "maxInactiveInterval": "${maxInactiveInterval}",
                    "sessionInfoUrl": "${sessionInfoUrl}"
                }
            </script>

            <tiles:insertAttribute name="sidemenu"/>
        </ul>

        <ul class="l-info-section">
            <%-- timer layout --%>
            <li>
                <div id="session-time-layout"
                     class="session-timer"
                     style="display: none;"
                     data-tooltip="${sessionTimeOnMouseOverMessage}">
                    <span class="item" id="session-time-field"></span>
                </div>
            </li>
            <c:if test="${canSwitchDesign and not hideDesignSwitch}">
                <li style="margin-bottom: 10px">
                    <div id="design-switch-block">
                        <label class="toggle" style="margin-bottom: 0" data-tooltip="<mvc:message code="redesign.switch.new" />">
                            <input type="checkbox" id="switch-to-new-design" data-action="switch-to-new-design" data-switch-url="${switchDesignUrl}">
                            <div class="toggle-control"></div>
                        </label>

                        <label id="switch-to-new-design-label" for="switch-to-new-design">
                            <mvc:message code="redesign.switch.new" />
                        </label>

                    </div>
                </li>
            </c:if>

            <li class="logout-area">
                <mvc:form action="${LOGOUT}" method="POST" cssClass="logout-form">
                    <button type="submit" data-tooltip="<mvc:message code='default.Logout'/>">
                        <i class="logout-logo icon-fa5 icon-power-off"></i>
                        <span class="logout-text"><mvc:message code='default.Logout'/></span>
                    </button>
                </mvc:form>
                <c:if test="${SLOW_CONNECTION_THRESHOLD_KBPS > 0}">
                    <span id="internet-indicator" class="icon-stack icon-lg" data-tooltip="<mvc:message code='OK'/>">
                      <i class="fas icon-wifi icon-stack-1x icon-inverse"></i>
                    </span>
                </c:if>
            </li>
            <li id="account-data" data-initializer="account-data">
                <a href="<c:url value="/user/self/view.action"/>">
                    <div class="account-initials">
                        <span><c:if test="${not empty firstName}">${fn:substring(firstName, 0, 1)}</c:if>${fn:substring(fullName, 0, 1)}</span>
                    </div>
                    <div class="account-data-infobox">
                        <p><strong><c:if test="${not empty firstName}">${firstName} </c:if>${fullName}</strong></p>
                        <c:if test="${not empty supervisorName}"><p><strong>${supervisorName}</strong></p></c:if>
                        <p>${companyShortName}</p>
                        <p><mvc:message code="default.CompanyID"/>: ${companyID}</p>
                        <br/>

                        <%
                        // Show Version on non-live servers only (and on all OpenEMM)
                        LicenseType licenseType = LicenseType.getLicenseTypeByID(ConfigService.getInstance().getValue(ConfigValue.System_License_Type));
                        if (!ConfigService.getInstance().getBooleanValue(ConfigValue.IsLiveInstance)
                        		|| licenseType == LicenseType.Inhouse
                                || licenseType == LicenseType.OpenEMM
                                || licenseType == LicenseType.OpenEMM_Plus
                        		|| ConfigService.getInstance().getIntegerValue(ConfigValue.System_Licence) == 0) {
                        %>
                            <p class="version-sign">
                                <strong>
                                <%= ConfigService.getInstance().getValue(ConfigValue.ApplicationVersion) %>
                                </strong>
                                <% if (ConfigService.getInstance().getBooleanValue(ConfigValue.ShowApplicationVersionWithDate)) { %>
                                	<br />
                                	(<%= ConfigService.getApplicationVersionInstallationTimeString(request) %>)
                                <% } %>
                            </p>
                        <%
                        }
                        %>
                    </div>
                </a>
            </li>
        </ul>
    </aside>

    <!-- Nav END -->

    <c:if test="${isTabsMenuShown}">
        <!-- Tabs BEGIN -->
        <tiles:insertAttribute name="tabsmenu"/>
        <!-- Tabs END -->
    </c:if>


    <!-- Main Content BEGIN -->
    <main class="l-main-view">
        <tiles:insertAttribute name="body"/>
    </main>
    <!-- Main Content END -->


    <tiles:insertAttribute name="footer_matomo"/>

    <tiles:insertAttribute name="messages"/>
    
    <c:if test="${SLOW_CONNECTION_THRESHOLD_KBPS > 0}">
        <script data-initializer="connection-speed-test" type="application/json">
            {
                "SLOW_CONNECTION_THRESHOLD_KBPS": ${SLOW_CONNECTION_THRESHOLD_KBPS},
                "SPEED_TEST_RESOURCE_SIZE": ${SPEED_TEST_RESOURCE_SIZE}
            }
        </script>
    </c:if>

</body>
</html>
