<%@ page import="com.agnitas.emm.core.admin.enums.UiLayoutType" %>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" %>

<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="emm"   uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"   uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"    uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="emailPlaceholder" value="info@yourdomain.com" scope="request"/>

<tiles:insertAttribute name="page-setup"/>

<html lang="en">
<tiles:insertAttribute name="head-tag"/>

<body data-bs-theme="${emmLayoutBase.themeType.name}" class="${emm:getLayoutType(pageContext.request) eq UiLayoutType.LEFT_HANDED ? 'left-hand' : ''}">
    <div id="page-wrapper">
        <aside class="sidebar">
            <%@include file="fragments/sidebar/sidebar.jspf" %>
        </aside>

        <header class="header">
            <%@include file="fragments/breadcrumbs.jspf" %>

            <div id="main-loader" class="loader hidden">
                <i class="icon icon-spinner icon-pulse"></i>
                <span><mvc:message code="default.loading" /></span>
            </div>

            <ul class="header__actions">
                <c:if test="${not empty headerActionsHtml}">
                    <li>
                        ${headerActionsHtml}
                    </li>
                </c:if>
                <%@ include file="fragments/edit-view-btn.jspf" %>
                <tiles:insertAttribute name="itemactions"/>
                <tiles:insertAttribute name="newresource"/>
            </ul>
        </header>

        <main id="main-view">
            <tiles:insertAttribute name="tabsmenu"/>
            <tiles:insertAttribute name="body"/>
        </main>
    </div>

    <tiles:insertAttribute name="footer_matomo"/>
    <tiles:insertAttribute name="messages"/>

    <script>
      window.isRedesignedUI = true; // TODO: EMMGUI-714 remove after finish of redesign and remove of old design
    </script>
</body>
</html>
