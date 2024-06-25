<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="_navigation_isHighlightKey" type="java.lang.Boolean"--%>
<%--@elvariable id="_navigation_token" type="java.lang.String"--%>
<%--@elvariable id="_navigation_href" type="java.lang.String"--%>
<%--@elvariable id="_navigation_navMsg" type="java.lang.String"--%>
<%--@elvariable id="_navigation_hideForToken" type="java.lang.String"--%>
<%--@elvariable id="_navigation_upsellingRef" type="java.lang.String"--%>
<%--@elvariable id="_navigation_conditionSatisfied" type="java.lang.Boolean"--%>

<%--@elvariable id="agnNavigationKey" type="java.lang.String"--%>
<%--@elvariable id="agnHighlightKey" type="java.lang.String"--%>
<%--@elvariable id="agnExtensionId" type="java.lang.String"--%>
<%--@elvariable id="agnNavHrefAppend" type="java.lang.String"--%>
<%--@elvariable id="agnNavHrefParams" type="java.util.LinkedHashMap"--%>

<c:set var="shownTabsCount" value="0" scope="page"/>
<c:set var="activeTab" value="" />

<c:set var="agnHeaderNavKey" value="${agnNavigationKey}"/>
<c:if test="${not empty agnHeaderNavKey}">
    <c:set var="agnHeaderNavKey" value="header_tabs.${agnHeaderNavKey}"/>
</c:if>

<emm:ShowNavigation navigation="${agnHeaderNavKey}" highlightKey="${agnHighlightKey}" redesigned="true">
    <emm:ShowByPermission token="${_navigation_token}">
        <c:set var="hideForToken" value="false"/>

        <c:if test="${not empty _navigation_hideForToken}">
            <emm:ShowByPermission token="${_navigation_hideForToken}">
                <c:set var="hideForToken" value="true"/>
            </emm:ShowByPermission>
        </c:if>

        <c:if test="${not hideForToken}">
            <c:set var="shownTabsCount" value="${shownTabsCount + 1}" scope="page"/>

            <c:if test="${_navigation_isHighlightKey}">
                <mvc:message var="activeTab" code="${_navigation_navMsg}" />
            </c:if>
        </c:if>
    </emm:ShowByPermission>
</emm:ShowNavigation>

<c:if test="${shownTabsCount > 1}">
    <div id="navbar_wrapper" data-controller="navbar">
        <nav class="navbar navbar-expand-lg">
            <a class="chosen-tab btn btn-primary" href="#">${activeTab}</a>
            <button class="navbar-toggler btn-icon-sm" type="button" data-bs-toggle="offcanvas" data-bs-target="#main-navbar" aria-controls="main-navbar" aria-expanded="false">
                <i class="icon icon-bars"></i>
            </button>

            <div class="collapse navbar-collapse offcanvas" tabindex="-1" id="main-navbar">
                <ul class="navbar-nav offcanvas-body">
                    <emm:ShowNavigation navigation='${agnHeaderNavKey}'
                                        highlightKey='${agnHighlightKey}' redesigned="true">
                        <c:set var="showTabsItem" value="false"/>
                        <c:set var="showUpsellingPage" value="false"/>

                        <emm:ShowByPermission token="${_navigation_token}">
                            <c:set var="showTabsItem" value="true"/>
                        </emm:ShowByPermission>

                        <c:if test="${not _navigation_conditionSatisfied}">
                            <c:set var="showTabsItem" value="false"/>
                        </c:if>

                        <c:if test="${not showTabsItem and not empty _navigation_upsellingRef}">
                            <c:set var="showUpsellingPage" value="true"/>
                        </c:if>

                        <c:if test="${not empty _navigation_hideForToken}">
                            <emm:ShowByPermission token="${_navigation_hideForToken}">
                                <c:set var="showTabsItem" value="false"/>
                                <c:set var="showUpsellingPage" value="false"/>
                            </emm:ShowByPermission>
                        </c:if>

                        <mvc:message var="linkMsg" code="${_navigation_navMsg}"/>

                        <c:set var="navigationLink" value="${_navigation_href.concat(agnNavHrefAppend)}"/>
                        <c:if test="${empty agnNavHrefAppend or not empty agnNavHrefParams}">
                            <c:set var="navigationLink" value="${_navigation_href}"/>
                            <c:forEach var="hrefParam" items="${agnNavHrefParams}">
                                <c:set var="key" value="{${hrefParam.key}}"/>
                                <c:set var="navigationLink" value="${fn:replace(navigationLink, key, hrefParam.value)}"/>
                            </c:forEach>
                        </c:if>

                        <c:if test="${showTabsItem}">
                            <li data-action="expand-navbar-tab" class="nav-item">
                                <c:if test="${_navigation_isHighlightKey}">
                                    <a href="<c:url value="${navigationLink}" />" class="btn btn-outline-primary ${_navigation_isHighlightKey ? 'active' : ''}">
                                        <span class="text-truncate">${linkMsg}</span>
                                    </a>
                                </c:if>
                                <c:if test="${not _navigation_isHighlightKey}">
                                    <a href="<c:url value="${navigationLink}" />" class="btn btn-outline-primary ">
                                        <span class="text-truncate">${linkMsg}</span>
                                    </a>
                                </c:if>
                            </li>
                        </c:if>

                        <c:if test="${not showTabsItem and showUpsellingPage}">
                            <c:set var="forwardedParams" value="${fn:substringAfter(navigationLink, '?')}"/>
                            <c:url var="upsellingLink"  value="/upselling.action" >
                                <c:param name="page" value="${_navigation_upsellingRef}"/>
                                <c:param name="featureNameKey" value="${_navigation_navMsg}"/>
                                <c:param name="navigationKey" value="${agnNavigationKey}"/>
                                <c:param name="extraParams" value="${forwardedParams}"/>
                                <c:param name="navigationLink" value="${navigationLink}"/>
                            </c:url>

                            <li data-action="expand-navbar-tab" class="nav-item ${_navigation_isHighlightKey ? 'active' : ''}">
                                <a href="${upsellingLink}" title="<mvc:message code="default.forbidden.tab.premium.feature" />" class="btn btn-outline-primary ${_navigation_isHighlightKey ? 'active' : ''}">
                                    <span class="text-truncate">${linkMsg}</span>
                                </a>
                            </li>
                        </c:if>
                    </emm:ShowNavigation>
                </ul>
            </div>
        </nav>
    </div>
</c:if>
