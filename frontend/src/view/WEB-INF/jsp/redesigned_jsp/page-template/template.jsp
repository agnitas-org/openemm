<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.agnitas.beans.EmmLayoutBase" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="DARK_MODE_THEME_TYPE" value="<%= EmmLayoutBase.ThemeType.DARK_MODE%>" scope="page"/>

<c:set var="isBreadcrumbsShown" value="false"                scope="request"/>
<c:set var="agnHeadLineFirstCrumb" value=""                  scope="request"/>
<c:set var="isTabsMenuShown" value="true"                    scope="request"/>
<c:set var="emailPlaceholder" value="info@yourdomain.com"    scope="request"/>

<c:set var="isDarkTheme" value="${emmLayoutBase.getThemeType() eq DARK_MODE_THEME_TYPE}" />

<tiles:insertAttribute name="page-setup"/>

<html lang="en">
<tiles:insertAttribute name="head-tag"/>

<body class="${isDarkTheme ? 'dark-theme' : ''}">
    <div id="page-wrapper">
        <aside class="sidebar">
            <%@include file="fragments/sidebar/sidebar.jspf" %>
        </aside>

        <header class="header">
            <%@include file="fragments/breadcrumbs/breadcrumbs.jspf" %>

            <ul class="header__actions">
                <li class="loader loader--main hidden">
                    <i class="icon icon-spinner icon-pulse"></i>
                    <span><mvc:message code="default.loading" /></span>
                </li>
                <%@ include file="fragments/edit-view-btn.jspf" %>
                <tiles:insertAttribute name="itemactions"/>
                <tiles:insertAttribute name="newresource"/>
            </ul>
        </header>

        <main id="main-view">
            <c:if test="${isTabsMenuShown}">
                <tiles:insertAttribute name="tabsmenu"/>
            </c:if>

            <tiles:insertAttribute name="body"/>
        </main>
    </div>

    <tiles:insertAttribute name="footer_matomo"/>
    <tiles:insertAttribute name="messages"/>

    <script>
      window.helpKey = '${agnHelpKey}';
      window.isRedesignedUI = true; // TODO: EMMGUI-714 remove after finish of redesign and remove of old design
    </script>
</body>
</html>
