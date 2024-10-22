<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="_navigation_isHighlightKey" type="java.lang.Boolean"--%>
<%--@elvariable id="_navigation_token" type="java.lang.String"--%>
<%--@elvariable id="_navigation_href" type="java.lang.String"--%>
<%--@elvariable id="_navigation_navMsg" type="java.lang.String"--%>
<%--@elvariable id="_navigation_hideForToken" type="java.lang.String"--%>
<%--@elvariable id="_navigation_upsellingRef" type="java.lang.String"--%>
<%--@elvariable id="_navigation_conditionSatisfied" type="java.lang.Boolean"--%>
<%--@elvariable id="_navigation_conditionMsg" type="java.lang.String"--%>

<%--@elvariable id="agnNavigationKey" type="java.lang.String"--%>
<%--@elvariable id="agnHighlightKey" type="java.lang.String"--%>
<%--@elvariable id="agnExtensionId" type="java.lang.String"--%>
<%--@elvariable id="agnNavHrefAppend" type="java.lang.String"--%>
<%--@elvariable id="agnNavHrefParams" type="java.util.LinkedHashMap"--%>

<c:set var="agnShownHeaderTabs" value="0" />

<c:if test="${not empty agnNavigationKey}">
    <c:set var="agnHeaderNavigationTabs">
        <emm:ShowNavigation navigation="header_tabs.${agnNavigationKey}" highlightKey="${agnHighlightKey}" redesigned="true">
            <c:if test="${empty _navigation_hideForToken or not emm:permissionAllowed(_navigation_hideForToken, pageContext.request)}">

                <mvc:message var="agnHeaderTabMsg" code="${_navigation_navMsg}" />

                <c:if test="${_navigation_isHighlightKey}">
                    <mvc:message var="agnActiveHeaderTab" code="${_navigation_navMsg}" />
                </c:if>

                <c:choose>
                    <c:when test="${_navigation_conditionSatisfied and emm:permissionAllowed(_navigation_token, pageContext.request)}">
                        <c:set var="agnShownHeaderTabs" value="${agnShownHeaderTabs + 1}" />

                        <c:set var="navigationLink" value="${_navigation_href.concat(agnNavHrefAppend)}"/>
                        <c:if test="${empty agnNavHrefAppend or not empty agnNavHrefParams}">
                            <c:set var="navigationLink" value="${_navigation_href}"/>
                            <c:forEach var="hrefParam" items="${agnNavHrefParams}">
                                <c:set var="key" value="{${hrefParam.key}}"/>
                                <c:set var="navigationLink" value="${fn:replace(navigationLink, key, hrefParam.value)}"/>
                            </c:forEach>
                        </c:if>

                        <li data-action="expand-navbar-tab" class="nav-item">
                            <a href="<c:url value="${navigationLink}" />" class="btn btn-outline-primary ${_navigation_isHighlightKey ? 'active' : ''}">
                                <span class="text-truncate">${agnHeaderTabMsg}</span>
                            </a>
                        </li>
                    </c:when>
                    <c:when test="${not empty _navigation_conditionMsg}">
                        <c:set var="agnShownHeaderTabs" value="${agnShownHeaderTabs + 1}" />

                        <li data-action="expand-navbar-tab" class="nav-item" data-tooltip="${fn:escapeXml(_navigation_conditionMsg)}">
                            <a href="#" class="btn btn-outline-primary disabled">
                                <span class="text-truncate">${agnHeaderTabMsg}</span>
                            </a>
                        </li>
                    </c:when>
                    <c:when test="${not empty _navigation_upsellingRef and _navigation_conditionSatisfied}">
                        <c:set var="agnShownHeaderTabs" value="${agnShownHeaderTabs + 1}" />

                        <c:url var="upsellingLink" value="/upsellingRedesigned.action">
                            <c:param name="page" value="${_navigation_upsellingRef}"/>
                        </c:url>

                        <li data-action="expand-navbar-tab" class="nav-item">
                            <a href="${upsellingLink}" class="btn btn-outline-primary ${_navigation_isHighlightKey ? 'active' : ''}"
                               data-confirm data-tooltip="<mvc:message code="default.forbidden.tab.premium.feature" />">
                                <span class="text-truncate">${agnHeaderTabMsg}</span>
                            </a>
                        </li>
                    </c:when>
                </c:choose>
            </c:if>
        </emm:ShowNavigation>
    </c:set>
</c:if>

<c:if test="${agnShownHeaderTabs gt 1}">
    <div id="navbar_wrapper" data-controller="navbar">
        <nav class="navbar navbar-expand-lg">
            <a class="chosen-tab btn btn-primary" href="#">${agnActiveHeaderTab}</a>
            <button class="navbar-toggler btn-icon" type="button" data-bs-toggle="offcanvas" data-bs-target="#main-navbar" aria-controls="main-navbar" aria-expanded="false">
                <i class="icon icon-bars"></i>
            </button>

            <div class="collapse navbar-collapse offcanvas" tabindex="-1" id="main-navbar">
                <ul class="navbar-nav offcanvas-body">${agnHeaderNavigationTabs}</ul>
            </div>
        </nav>
    </div>
</c:if>
