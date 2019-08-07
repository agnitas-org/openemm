<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="sidemenu_active" type="java.lang.String"--%>
<%--@elvariable id="sidemenu_sub_active" type="java.lang.String"--%>

<%--@elvariable id="_navigation_switch" type="java.lang.String"--%>
<%--@elvariable id="_navigation_isHighlightKey" type="java.lang.Boolean"--%>
<%--@elvariable id="_navigation_token" type="java.lang.String"--%>
<%--@elvariable id="_navigation_href" type="java.lang.String"--%>
<%--@elvariable id="_navigation_isHideAnyCase" type="java.lang.Boolean"--%>
<%--@elvariable id="_navigation_navMsg" type="java.lang.String"--%>
<%--@elvariable id="_navigation_index" type="java.lang.Integer"--%>
<%--@elvariable id="_navigation_plugin" type="java.lang.String"--%>
<%--@elvariable id="_navigation_extension" type="java.lang.String"--%>
<%--@elvariable id="_navigation_iconClass" type="java.lang.String"--%>
<%--@elvariable id="_navigation_submenu" type="java.lang.String"--%>
<%--@elvariable id="_navigation_hideForToken" type="java.lang.String"--%>
<%--@elvariable id="_navigation_upsellingRef" type="java.lang.String"--%>

<%--@elvariable id="_sub_navigation_switch" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_isHighlightKey" type="java.lang.Boolean"--%>
<%--@elvariable id="_sub_navigation_token" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_href" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_isHideAnyCase" type="java.lang.Boolean"--%>
<%--@elvariable id="_sub_navigation_navMsg" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_index" type="java.lang.Integer"--%>
<%--@elvariable id="_sub_navigation_plugin" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_extension" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_iconClass" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_submenu" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_hideForToken" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_upsellingRef" type="java.lang.String"--%>

<emm:ShowNavigation navigation="sidemenu" highlightKey='${sidemenu_active}'>
    <c:set var="showMenuItem" value="false"/>
    <c:set var="showUpsellingPage" value="false"/>

    <emm:ShowByPermission token="${_navigation_token}">
        <c:set var="showMenuItem" value="true"/>
    </emm:ShowByPermission>

    <c:if test="${not showMenuItem and not empty _navigation_upsellingRef}">
        <c:set var="showUpsellingPage" value="true"/>
    </c:if>

    <c:if test="${not empty _navigation_hideForToken}">
        <emm:ShowByPermission token="${_navigation_hideForToken}">
            <c:set var="showMenuItem" value="false"/>
        </emm:ShowByPermission>
    </c:if>

    <c:if test="${showMenuItem or showUpsellingPage}">
        <c:set var="sideMenuItemStyles" value="${_navigation_isHighlightKey ? 'active open' : ''}"/>
        <li class="${sideMenuItemStyles}">
            <%-- Check whether at least one item is there in submenu dropdown --%>
            <c:set var="isSubmenuAvailable" value="false"/>
            <emm:ShowNavigation navigation='${_navigation_submenu}Sub' highlightKey="${sidemenu_sub_active}" prefix="_sub">
                <emm:ShowByPermission token="${_sub_navigation_token}">
                    <c:set var="isSubmenuAvailable" value="true"/>
                </emm:ShowByPermission>

                <c:if test="${not empty _sub_navigation_hideForToken}">
                    <emm:ShowByPermission token="${_sub_navigation_hideForToken}">
                        <c:set var="isSubmenuAvailable" value="false"/>
                    </emm:ShowByPermission>
                </c:if>
            </emm:ShowNavigation>

            <c:set var="sideMenuItem">
                <i class="menu-item-logo icon icon-${_navigation_iconClass}"></i>
                <c:if test="${empty _navigation_plugin}">
                    <span class="menu-item-text"><bean:message key="${_navigation_navMsg}" /></span>
                </c:if>
                <c:if test="${not empty _navigation_plugin}">
                    <span class="menu-item-text"><emm:message key="${_navigation_navMsg}" plugin="${_navigation_plugin}"/></span>
                </c:if>
                <i class="nav-arrow icon<c:if test="${isSubmenuAvailable}"> icon-caret-down</c:if>"></i>
            </c:set>

            <%-- Show advisory title for menu item shown without permission --%>
            <c:choose>
                <c:when test="${showUpsellingPage}">
                    <c:url var="upsellingLink"  value="/upselling.action" >
                        <c:param name="page" value="${_navigation_upsellingRef}"/>
                        <c:param name="featureNameKey" value="${_navigation_navMsg}"/>
                    </c:url>

                    <html:link href="${upsellingLink}" titleKey="default.forbidden.tab.premium.feature" styleClass="menu-item ${sideMenuItemStyles}">
                        ${sideMenuItem}
                    </html:link>
                </c:when>
                <c:otherwise>
                    <html:link page="${_navigation_href}" styleClass="menu-item ${sideMenuItemStyles}">
                        ${sideMenuItem}
                    </html:link>
                </c:otherwise>
            </c:choose>

            <%-- Hide sub-items for menu item shown without permission --%>
            <c:if test="${isSubmenuAvailable and not showUpsellingPage}">
                <ul class="dropdown-menu">
                    <emm:ShowNavigation navigation='${_navigation_submenu}Sub' highlightKey="${sidemenu_sub_active}" prefix="_sub">
                        <c:set var="showSubMenuItem" value="false"/>
                        <c:url var="subItemPage" value="${_sub_navigation_href}"/>

                        <emm:ShowByPermission token="${_sub_navigation_token}">
                            <c:set var="showSubMenuItem" value="true"/>
                        </emm:ShowByPermission>

                        <c:if test="${not showSubMenuItem and not empty _sub_navigation_upsellingRef}">
                            <c:url var="subItemPage" value="/upselling.action" >
                                <c:param name="page" value="${_sub_navigation_upsellingRef}"/>
                                <c:param name="featureNameKey" value="${_sub_navigation_navMsg}"/>
                                <c:param name="sidemenuActive" value="${_navigation_navMsg}"/>
                                <c:param name="sidemenuSubActive" value="${_sub_navigation_navMsg}"/>
                            </c:url>

                            <c:set var="showSubMenuItem" value="true"/>
                        </c:if>

                        <c:if test="${not empty _sub_navigation_hideForToken}">
                            <emm:ShowByPermission token="${_sub_navigation_hideForToken}">
                                <c:set var="showSubMenuItem" value="false"/>
                            </emm:ShowByPermission>
                        </c:if>

                        <c:if test="${showSubMenuItem}">
                            <li>
                                <html:link href="${subItemPage}" styleClass="menu-item${_sub_navigation_isHighlightKey ? ' active' : ''}">
                                    <span class="text">
                                        <c:choose>
                                            <c:when test="${empty _sub_navigation_plugin}">
                                                <bean:message key="${_sub_navigation_navMsg}" />
                                            </c:when>
                                            <c:otherwise>
                                                <emm:message key="${_sub_navigation_navMsg}" plugin="${_sub_navigation_plugin}"/>
                                            </c:otherwise>
                                        </c:choose>
                                    </span>

                                    <c:if test="${not empty _sub_navigation_iconClass}">
                                        &nbsp;<i class="icon icon-${_sub_navigation_iconClass}"></i>
                                    </c:if>
                                </html:link>
                            </li>
                        </c:if>
                    </emm:ShowNavigation>
                </ul>
            </c:if>
        </li>
    </c:if>
</emm:ShowNavigation>
