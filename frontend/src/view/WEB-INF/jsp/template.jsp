<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.agnitas.emm.core.commons.util.ConfigValue" %>
<%@ page import="org.agnitas.emm.core.commons.util.ConfigService" %>
<%@ page import="com.agnitas.util.ComHelpUtil" %>
<%@ page import="org.agnitas.beans.EmmLayoutBase" %>
<%@ page import="com.agnitas.beans.ComAdminPreferences" %>
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

<c:set var="isTabsMenuShown" value="true" scope="request"/>
<c:set var="isBreadcrumbsShown" value="false" scope="request"/>

<tiles:insert attribute="page-setup"/>

<!--[if IE 9 ]>    <html class="ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html class=""> <!--<![endif]-->
<tiles:insert attribute="head-tag"/>
<body>

    <div class="loader">
        <i class="icon icon-refresh icon-spin"></i> <bean:message key="default.loading" />
    </div>

    <!-- Header BEGIN -->
    <header class="l-header">
        <ul class="header-menu">
            <li>
                <a class="menu-open" href="#">
                    <i class="icon icon-bars"></i>
                </a>
            </li>
        </ul>
        <ul class="header-nav">
            <li>
                <h1 class="headline">
                    <c:set var="isHeadLineShown" value="false"/>

                    <emm:ShowNavigation navigation="sidemenu" highlightKey='${sidemenu_active}'>
                        <c:if test="${_navigation_navMsg == agnTitleKey or (isBreadcrumbsShown and _navigation_navMsg == agnBreadcrumbsRootKey)}">
                            <c:set var="isHeadLineShown" value="true" />

                            <c:set var="agnHeadLineIconClass" value="${_navigation_iconClass}"/>

                            <c:set var="agnHeadLineFirstCrumb">
                                <c:choose>
                                    <c:when test="${not empty _navigation_plugin}">
                                        <emm:message key="${_navigation_navMsg}" plugin="${_navigation_plugin}"/>
                                    </c:when>
                                    <c:otherwise>
                                        <bean:message key="${_navigation_navMsg}" />
                                    </c:otherwise>
                                </c:choose>
                            </c:set>
                        </c:if>
                    </emm:ShowNavigation>

                    <c:choose>
                        <c:when test="${isHeadLineShown and isBreadcrumbsShown}">
                            <c:set var="crumbs" value=""/>

                            <c:set var="crumbs">${crumbs}<li><i class="icon icon-${agnHeadLineIconClass}"></i></li></c:set>
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
                                                <c:choose>
                                                    <c:when test="${empty agnPluginId}">
                                                        <bean:message key="${agnBreadcrumb.textKey}"/>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <emm:message key="${agnBreadcrumb.textKey}" plugin="${agnPluginId}"/>
                                                    </c:otherwise>
                                                </c:choose>
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
                            <i class="icon icon-${agnHeadLineIconClass}"></i>
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
        <c:url var="agnitasEmmLogoSvgLink" value="/assets/core/images/facelift/agnitas-emm-logo.svg"/>
        <c:url var="agnitasEmmLogoPngLink" value="/assets/core/images/facelift/agnitas-emm-logo.png"/>
        <div class="l-logo">
            <c:if test="${sessionScope['emm.adminPreferences'].startPage == START_PAGE_DASHBOARD}">
                <html:link page="/dashboard.action" styleClass="logo">
                    <img class="logo-image" src="${agnitasEmmLogoSvgLink}" onerror="this.onerror=null; this.src='${agnitasEmmLogoPngLink}'">

                    <p class="headline"><bean:message key="default.EMM" /></p>
                    <p class="version"><bean:message key="default.version" /></p>
                </html:link>
            </c:if>
            <c:if test="${sessionScope['emm.adminPreferences'].startPage == START_PAGE_CALENDAR}">
                <html:link page="/calendar.action" styleClass="logo">
                    <img class="logo-image" src="${agnitasEmmLogoSvgLink}" onerror="this.onerror=null; this.src='${agnitasEmmLogoPngLink}'">

                    <p class="headline"><bean:message key="default.EMM" /></p>
                    <p class="version"><bean:message key="default.version" /></p>
                </html:link>
            </c:if>
            <a href="#" class="menu-close">
                <i class="icon icon-close"></i>
            </a>
        </div>

        <ul class="l-menu scrollable js-scrollable">
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

                <%-- timer layout --%>
                <li id="session-time-layout"
                    class="session-timer"
                    style="display: none;"
                    data-tooltip="${sessionTimeOnMouseOverMessage}">
                    <i class="menu-item-logo icon icon-hourglass-end" id="session-time-icon" aria-hidden="true"></i>
                    <span class="menu-item-text" id="session-time-field"></span>
                </li>
            <tiles:insert attribute="sidemenu"/>
            <%@include file="information-link.jspf"%>
            <li class="account-data">
                <html:link page="/selfservice.do?action=showChangeForm" styleClass="menu-item">
                    <p class="small"><bean:message key="username.message"/></p>
                    <p><strong><c:if test="${not empty firstName}">${firstName} </c:if>${fullName}</strong></p>
                    <p>${companyShortName}</p>
                    <p><bean:message key="default.CompanyID"/>: ${companyID}</p>
                </html:link>

                <c:set var="messageDefaultLogout"><bean:message key="default.Logout"/></c:set>

                <form action="<c:url value="/logout.action"/>" method="POST">
                    <button type="submit" class="btn btn-regular" data-tooltip="${messageDefaultLogout}">
                        <i class="icon icon-power-off"></i>
                        <span>${messageDefaultLogout}</span>
                    </button>
                </form>
             </li>
            <%
            // Show Version on non-live servers only
            if (!ConfigService.getInstance().getBooleanValue(ConfigValue.IsLiveInstance)) {
            %>
            <li class="version-sign">
                <strong><%= ConfigService.getInstance().getValue(ConfigValue.ApplicationVersion) %></strong>
            </li>
            <%
            }
            %>
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


    <tiles:insert attribute="footer_piwik"/>

    <tiles:insert attribute="messages"/>

</body>
</html>
