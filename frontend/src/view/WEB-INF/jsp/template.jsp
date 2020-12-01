<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.agnitas.emm.core.commons.util.ConfigValue" %>
<%@ page import="org.agnitas.emm.core.commons.util.ConfigService" %>
<%@ page import="com.agnitas.util.ComHelpUtil" %>
<%@ page import="org.agnitas.beans.EmmLayoutBase" %>
<%@ page import="com.agnitas.beans.ComAdminPreferences" %>
<%@ page import="org.agnitas.service.WebStorage" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="MENU_POSITION_LEFT" value="<%= EmmLayoutBase.MENU_POSITION_LEFT%>" scope="page"/>
<c:set var="MENU_POSITION_TOP" value="<%= EmmLayoutBase.MENU_POSITION_TOP%>" scope="page"/>
<c:set var="START_PAGE_DASHBOARD" value="<%= ComAdminPreferences.START_PAGE_DASHBOARD%>" scope="page"/>
<c:set var="START_PAGE_CALENDAR" value="<%= ComAdminPreferences.START_PAGE_CALENDAR%>" scope="page"/>
<c:set var="isWideSidebarWebStorageBundleKey" value="<%= WebStorage.IS_WIDE_SIDEBAR%>" scope="page"/>
<c:set var="DARK_MODE_THEME_TYPE" value="<%= EmmLayoutBase.ThemeType.DARK_MODE%>" scope="page"/>

<c:set var="isTabsMenuShown" value="true" scope="request"/>
<c:set var="isBreadcrumbsShown" value="false" scope="request"/>

<c:url var="LOGOUT" value="/logout.action"/>

<tiles:insert attribute="page-setup"/>

<!--[if IE 9 ]>    <html class="ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html class=""> <!--<![endif]-->
<tiles:insert attribute="head-tag"/>

<emm:webStorage var="isWideSidebarBundle" key="${isWideSidebarWebStorageBundleKey}"/>
<body class="${isWideSidebarBundle.isTrue() ? 'wide-sidebar' : ''} ${emmLayoutBase.getThemeType() eq DARK_MODE_THEME_TYPE ? 'dark-theme' : ''}">

    <div class="loader">
        <i class="icon icon-refresh icon-spin"></i> <bean:message key="default.loading" />
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
                                <bean:message key="${_navigation_navMsg}" />
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
	                                            <bean:message key="${agnBreadcrumb.textKey}"/>
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
                <c:set var="helpPageUrl" value="<%= ComHelpUtil.getHelpPageUrl(request) %>" />
                <button class="btn btn-secondary btn-regular" type="button" data-popup="${helpPageUrl}">
                    <i class="icon icon-question-circle"></i>
                    <span class="text"><bean:message key="help"/></span>
                </button>
            </li>
            <%@ include file="itemactions-support.jspf" %>
            <tiles:insert attribute="newresource"/>
            <tiles:insert attribute="itemactions"/>
        </ul>
    </header>
    <!-- Header END -->

    <!-- Nav BEGIN -->
    <aside class="l-sidebar">
        <%@ include file="logo.jspf" %>

        <ul class="l-menu scrollable js-scrollable" data-controller="sidemenu" data-initializer="sidemenu">
            <%-- init data for transfering on frontend side --%>
            <c:set var="creationTime" value="${pageContext.session.creationTime}"/>
            <c:set var="lastAccessedTime" value="${pageContext.session.lastAccessedTime}"/>
            <c:set var="maxInactiveInterval" value="${pageContext.session.maxInactiveInterval}"/>
            <c:url var="sessionInfoUrl" value="/session/info.action" />
            <c:set var="sessionTimeOnMouseOverMessage">
                <bean:message key="session.timer.mouseover"/>
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

            <tiles:insert attribute="sidemenu"/>
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
            <li>
                <form action="${LOGOUT}" method="POST" class="logout-form">
                    <button type="submit" data-tooltip="<bean:message key='default.Logout'/>">
                        <i class="logout-logo icon-fa5 icon-power-off"></i>
                        <span class="logout-text"><bean:message key='default.Logout'/></span>
                    </button>
                </form>
            </li>
            <li id="account-data" data-initializer="account-data">
                <html:link page="/selfservice.do?action=showChangeForm">
                    <div class="account-initials">
                        <span><c:if test="${not empty firstName}">${fn:substring(firstName, 0, 1)}</c:if>${fn:substring(fullName, 0, 1)}</span>
                    </div>
                    <div class="account-data-infobox">
                        <p><strong><c:if test="${not empty firstName}">${firstName} </c:if>${fullName}</strong></p>
                        <p>${companyShortName}</p>
                        <p><bean:message key="default.CompanyID"/>: ${companyID}</p>
                        <br/>

                        <%
                        // Show Version on non-live servers only (and on all OpenEMM)
                        if (!ConfigService.getInstance().getBooleanValue(ConfigValue.IsLiveInstance) || "Inhouse".equalsIgnoreCase(ConfigService.getInstance().getValue(ConfigValue.System_License_Type)) || ConfigService.getInstance().getIntegerValue(ConfigValue.System_Licence) == 0) {
                        %>
                            <p class="version-sign">
                                <strong><%= ConfigService.getInstance().getValue(ConfigValue.ApplicationVersion) %></strong>
                            </p>
                        <%
                        }
                        %>
                    </div>
                </html:link>
            </li>
        </ul>
    </aside>

    <!-- Nav END -->

    <c:if test="${isTabsMenuShown}">
        <!-- Tabs BEGIN -->
        <tiles:insert attribute="tabsmenu"/>
        <!-- Tabs END -->
    </c:if>


    <!-- Main Content BEGIN -->
    <main class="l-main-view">
        <tiles:insert attribute="body"/>
    </main>
    <!-- Main Content END -->


    <tiles:insert attribute="footer_matomo"/>

    <tiles:insert attribute="messages"/>

</body>
</html>
