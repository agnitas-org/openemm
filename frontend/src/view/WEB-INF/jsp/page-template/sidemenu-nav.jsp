<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="sidemenu_active" type="java.lang.String"--%>
<%--@elvariable id="sidemenu_sub_active" type="java.lang.String"--%>

<%--@elvariable id="_navigation_switch" type="java.lang.String"--%>
<%--@elvariable id="_navigation_isHighlightKey" type="java.lang.Boolean"--%>
<%--@elvariable id="_navigation_token" type="java.lang.String"--%>
<%--@elvariable id="_navigation_href" type="java.lang.String"--%>
<%--@elvariable id="_navigation_isHideAnyCase" type="java.lang.Boolean"--%>
<%--@elvariable id="_navigation_navMsg" type="java.lang.String"--%>
<%--@elvariable id="_navigation_index" type="java.lang.Integer"--%>
<%--@elvariable id="_navigation_iconClass" type="java.lang.String"--%>
<%--@elvariable id="_navigation_itemClass" type="java.lang.String"--%>
<%--@elvariable id="_navigation_submenu" type="java.lang.String"--%>
<%--@elvariable id="_navigation_hideForToken" type="java.lang.String"--%>
<%--@elvariable id="_navigation_upsellingRef" type="java.lang.String"--%>
<%--@elvariable id="_navigation_conditionSatisfied" type="java.lang.Boolean"--%>

<%--@elvariable id="_sub_navigation_switch" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_isHighlightKey" type="java.lang.Boolean"--%>
<%--@elvariable id="_sub_navigation_token" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_href" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_isHideAnyCase" type="java.lang.Boolean"--%>
<%--@elvariable id="_sub_navigation_navMsg" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_index" type="java.lang.Integer"--%>
<%--@elvariable id="_sub_navigation_iconClass" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_itemClass" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_submenu" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_hideForToken" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_upsellingRef" type="java.lang.String"--%>
<%--@elvariable id="_sub_navigation_conditionSatisfied" type="java.lang.Boolean"--%>

<%--@elvariable id="sidemenu_active_additional_params" type="java.util.LinkedHashMap"--%>
<%--@elvariable id="additional_param" type="com.agnitas.taglib.SideMenuAdditionalParamTag.MenuAdditionalParams"--%>

<emm:ShowNavigation navigation="sidemenu" highlightKey="${sidemenu_active}">
    <c:set var="showMenuItem" value="${emm:permissionAllowed(_navigation_token, pageContext.request)}"/>
    <c:set var="showUpsellingPage" value="false"/>

    <c:if test="${not _navigation_conditionSatisfied}">
        <c:set var="showMenuItem" value="false"/>
    </c:if>

    <c:set var="isHiddenLBMenuItem" value="${_navigation_submenu eq 'Grid' and _navigation_conditionSatisfied eq false}"/>

    <c:if test="${not showMenuItem and not empty _navigation_upsellingRef and not isHiddenLBMenuItem}">
        <c:set var="showUpsellingPage" value="true"/>
    </c:if>

    <c:if test="${not empty _navigation_hideForToken and emm:permissionAllowed(_navigation_hideForToken, pageContext.request)}">
        <c:set var="showMenuItem" value="false"/>
    </c:if>

    <c:if test="${showMenuItem or showUpsellingPage}">
        <c:set var="sideMenuItemStyles" value="${_navigation_isHighlightKey ? 'active open' : ''}"/>
        <li class="sidebar__item ${sideMenuItemStyles}" data-action="activateSubmenu">
            <%-- Check whether at least one item is there in submenu dropdown --%>
            <c:set var="isSubmenuAvailable" value="false"/>
            <emm:ShowNavigation navigation='sidemenu_items.${_navigation_submenu}Sub' highlightKey="${_navigation_isHighlightKey ? sidemenu_sub_active : ''}" prefix="_sub">
                <c:if test="${not isSubmenuAvailable}">
                    <emm:ShowByPermission token="${_sub_navigation_token}">
                        <c:set var="isSubmenuAvailable" value="true"/>
                    </emm:ShowByPermission>

                    <c:if test="${not _sub_navigation_conditionSatisfied}">
                        <c:set var="isSubmenuAvailable" value="false"/>
                    </c:if>

                    <c:if test="${not empty _sub_navigation_hideForToken}">
                        <emm:ShowByPermission token="${_sub_navigation_hideForToken}">
                            <c:set var="isSubmenuAvailable" value="false"/>
                        </emm:ShowByPermission>
                    </c:if>
                </c:if>
            </emm:ShowNavigation>

            <%-- Show advisory title for menu item shown without permission --%>
            <c:choose>
                <c:when test="${showUpsellingPage}">
                    <c:url var="upsellingLink"  value="/upselling.action" >
                        <c:param name="page" value="${_navigation_upsellingRef}"/>
                    </c:url>

                    <a href="${upsellingLink}" data-tooltip="<mvc:message code="default.forbidden.tab.premium.feature" />" class="sidebar__link ${sideMenuItemStyles}" data-confirm>
                        <span class="sidebar__icon">
                            <i class="icon icon-${_navigation_iconClass}"></i>
                        </span>
                        <span class="sidebar__link-text"><mvc:message code="${_navigation_navMsg}" /></span>
                    </a>
                </c:when>
                <c:otherwise>
                    <c:url var="menuItemLink" value="${_navigation_href}">
                        <c:if test="${_navigation_isHighlightKey}">
                            <c:forEach var="additional_param" items="${sidemenu_active_additional_params}">
                                <c:if test="${not additional_param.forSubmenuOnly}">
                                    <c:param name="${additional_param.paramName}" value="${additional_param.paramValue}"/>
                                </c:if>
                            </c:forEach>
                        </c:if>
                    </c:url>
                    <a href="${menuItemLink}" class="sidebar__link ${sideMenuItemStyles}">
                        <span class="sidebar__icon">
                            <i class="icon icon-${_navigation_iconClass}"></i>
                        </span>
                        <span class="sidebar__link-text"><mvc:message code="${_navigation_navMsg}" /></span>
                    </a>
                </c:otherwise>
            </c:choose>

            <div class="sidebar__submenu">
                <h1 class="sidebar__submenu-title"><mvc:message code="${_navigation_navMsg}" /></h1>
                    <%-- Hide sub-items for menu item shown without permission --%>
                <c:if test="${isSubmenuAvailable and not showUpsellingPage}">

                    <ul class="sidebar__submenu-list">
                        <emm:ShowNavigation navigation='sidemenu_items.${_navigation_submenu}Sub' highlightKey="${_navigation_isHighlightKey ? sidemenu_sub_active : ''}" prefix="_sub">
                            <c:url var="subItemPage" value="${_sub_navigation_href}">
                                <c:if test="${_navigation_isHighlightKey and _sub_navigation_isHighlightKey}">
                                    <c:forEach var="additional_param" items="${sidemenu_active_additional_params}">
                                        <c:param name="${additional_param.paramName}" value="${additional_param.paramValue}"/>
                                    </c:forEach>
                                </c:if>
                            </c:url>

                            <c:set var="showSubMenuItem" value="${emm:permissionAllowed(_sub_navigation_token, pageContext.request)}" />
                            <c:if test="${not _sub_navigation_conditionSatisfied}">
                                <c:set var="showSubMenuItem" value="false"/>
                            </c:if>

                            <c:set var="showUpsellingPage" value="false" />
                            <c:if test="${not showSubMenuItem and not empty _sub_navigation_upsellingRef}">
                                <c:url var="subItemPage" value="/upselling.action" >
                                    <c:param name="page" value="${_sub_navigation_upsellingRef}"/>
                                </c:url>

                                <c:set var="showSubMenuItem" value="true"/>
                                <c:set var="showUpsellingPage" value="true" />
                            </c:if>

                            <c:if test="${not empty _sub_navigation_hideForToken and emm:permissionAllowed(_sub_navigation_hideForToken, pageContext.request)}">
                                <c:set var="showSubMenuItem" value="false"/>
                            </c:if>

                            <c:if test="${showSubMenuItem}">
                                <li class="sidebar__submenu-item ${_sub_navigation_itemClass}">
                                    <a href="${subItemPage}" class="sidebar__submenu-link ${_sub_navigation_isHighlightKey ? 'active' : ''}" ${showUpsellingPage ? 'data-confirm' : ''}>
                                        <mvc:message code="${_sub_navigation_navMsg}" />
                                    </a>
                                </li>
                            </c:if>
                        </emm:ShowNavigation>
                    </ul>
                </c:if>
            </div>
        </li>
    </c:if>
</emm:ShowNavigation>
