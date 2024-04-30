<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

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

<emm:ShowNavigation navigation="${agnNavigationKey}" highlightKey="${agnHighlightKey}">
    <emm:ShowByPermission token="${_navigation_token}">
        <c:set var="hideForToken" value="false"/>

        <c:if test="${not empty _navigation_hideForToken}">
            <emm:ShowByPermission token="${_navigation_hideForToken}">
                <c:set var="hideForToken" value="true"/>
            </emm:ShowByPermission>
        </c:if>

        <c:if test="${not hideForToken}">
            <c:set var="shownTabsCount" value="${shownTabsCount + 1}" scope="page"/>
        </c:if>
    </emm:ShowByPermission>
</emm:ShowNavigation>

<c:if test="${shownTabsCount > 1}">
    <nav class="sub-navigation">
        <ul class="sub-nav">
            <emm:ShowNavigation navigation='${agnNavigationKey}'
                                highlightKey='${agnHighlightKey}'>
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

                <c:set var="linkMsg">
                    <bean:message key="${_navigation_navMsg}"/>
                </c:set>

                <c:set var="navigationLink" value="${_navigation_href.concat(agnNavHrefAppend)}"/>
                <c:if test="${empty agnNavHrefAppend or not empty agnNavHrefParams}">
                    <c:set var="navigationLink" value="${_navigation_href}"/>
                    <c:forEach var="hrefParam" items="${agnNavHrefParams}">
                        <c:set var="key" value="{${hrefParam.key}}"/>
                        <c:set var="navigationLink" value="${fn:replace(navigationLink, key, hrefParam.value)}"/>
                    </c:forEach>
                </c:if>

                <c:if test="${showTabsItem}">
                    <li class="${_navigation_isHighlightKey ? 'active' : ''}">
                        <c:if test="${_navigation_isHighlightKey}">
                            <html:link page="${navigationLink}">${linkMsg}</html:link>
                        </c:if>
                        <c:if test="${not _navigation_isHighlightKey}">
                            <html:link styleClass="" page="${navigationLink}">${linkMsg}</html:link>
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

                    <li class="${_navigation_isHighlightKey ? 'active' : ''}">
                        <html:link  styleClass="" href="${upsellingLink}" titleKey="default.forbidden.tab.premium.feature">
                            ${linkMsg}
                        </html:link>
                    </li>
                </c:if>
            </emm:ShowNavigation>
        </ul>
        <div class="dropdown" data-dropdown-expand=".sub-nav">
          <button id="dLabel" class="dropdown-toggle" type="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            <span class="icon icon-ellipsis-v"></span>&nbsp;
            <span class="icon icon-caret-down"></span>
          </button>
          <ul class="dropdown-menu" aria-labelledby="dLabel" >

          </ul>
        </div>
    </nav>
</c:if>
