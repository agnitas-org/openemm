<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="targetId" type="java.lang.Integer"--%>
<%--@elvariable id="isLocked" type="java.lang.Boolean"--%>
<%--@elvariable id="hidden" type="java.lang.Boolean"--%>

<c:if test="${empty targetShortname}">
    <c:set var="targetShortname" value="${targetEditForm.shortname}"/>
</c:if>

<c:if test="${empty targetId}">
    <c:set var="targetId" value="0"/>
</c:if>

<c:set var="agnTitleKey"            value="Target"                                                  scope="request"/>
<c:set var="sidemenu_active"        value="Targetgroups"                                            scope="request"/>
<c:set var="agnBreadcrumbsRootKey"  value="Targetgroups"                                            scope="request"/>
<c:url var="agnBreadcrumbsRootUrl"  value="/target/list.action?restoreSort=true"                    scope="request"/>
<c:set var="agnHelpKey"             value="targetGroupView"                                         scope="request"/>
<c:set var="agnEditViewKey"         value="target-group-view"                                       scope="request"/>
<c:if test="${targetId gt 0}">
    <c:set var="agnNavigationKey"   value="targetGroup"                                             scope="request"/>
    <c:set var="agnHighlightKey"    value="${isStatsPage ? 'Statistics' : 'default.settings'}"      scope="request"/>
</c:if>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${targetId eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="target.NewTarget"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${targetShortname}"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="targetId" value="${targetId}"/>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <c:if test="${not hidden}">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

            <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
            <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

            <emm:instantiate var="dropDownItems" type="java.util.LinkedHashMap">
                <c:set target="${element}" property="dropDownItems" value="${dropDownItems}"/>
            </emm:instantiate>

            <%--options for dropdown--%>
            <c:if test="${not isLocked}">
                <c:if test="${not empty targetId and targetId != 0}">
                    <emm:ShowByPermission token="targets.change">
                        <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                            <c:set target="${dropDownItems}" property="2" value="${dropDownItem}"/>
                            <c:set target="${dropDownItem}" property="url">
                                <c:url value="/target/${targetId}/copy.action"/>
                            </c:set>
                            <c:set target="${dropDownItem}" property="name"><mvc:message code="button.Copy"/></c:set>
                        </emm:instantiate>
                    </emm:ShowByPermission>

                    <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                        <c:set target="${dropDownItems}" property="7" value="${dropDownItem}"/>
                        <c:set target="${dropDownItem}" property="type" value="href"/>
                        <c:set target="${dropDownItem}" property="url">
                            <c:url value="/statistics/recipient/view.action">
                                <c:param name="mailinglistId" value="0"/>
                                <c:param name="targetId" value="${targetId}"/>
                            </c:url>
                        </c:set>
                        <c:set target="${dropDownItem}" property="name"><mvc:message code="Statistics"/></c:set>
                    </emm:instantiate>

                    <emm:ShowByPermission token="targets.lock">
                        <c:url var="lockUrl" value="/target/${targetId}/lock.action"/>
                        <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                            <c:set target="${dropDownItems}" property="3" value="${dropDownItem}"/>
                            <c:set target="${dropDownItem}" property="url" value="${lockUrl}"/>
                            <c:set target="${dropDownItem}" property="name"><mvc:message code="target.lock"/></c:set>
                        </emm:instantiate>
                    </emm:ShowByPermission>
                </c:if>
            </c:if>

            <c:if test="${isLocked}">
                <emm:ShowByPermission token="targets.lock">
                    <c:url var="unlockUrl" value="/target/${targetId}/unlock.action"/>
                    <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                        <c:set target="${dropDownItems}" property="4" value="${dropDownItem}"/>
                        <c:set target="${dropDownItem}" property="url" value="${unlockUrl}"/>
                        <c:set target="${dropDownItem}" property="name"><mvc:message code="button.Unlock"/></c:set>
                    </emm:instantiate>
                </emm:ShowByPermission>
            </c:if>

            <c:if test="${not empty targetId and targetId != 0}">
                <emm:ShowByPermission token="recipient.delete">
                    <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                        <c:set target="${dropDownItems}" property="6" value="${dropDownItem}"/>
                        <c:set target="${dropDownItem}" property="extraAttributes" value="data-confirm"/>
                        <c:set target="${dropDownItem}" property="type" value="href"/>
                        <c:set target="${dropDownItem}" property="url">
                            <c:url value="/target/${targetId}/confirm/delete/recipients.action"/>
                        </c:set>
                        <c:set target="${dropDownItem}" property="name"><mvc:message code="target.delete.recipients"/></c:set>
                    </emm:instantiate>
                </emm:ShowByPermission>
            </c:if>

            <c:if test="${not isLocked and targetId > 0}">
                <emm:ShowByPermission token="targets.delete">
                    <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                        <c:set target="${dropDownItems}" property="0" value="${dropDownItem}"/>
                        <c:set target="${dropDownItem}" property="url">
                            <c:url value="/target/${targetId}/confirm/delete.action"/>
                        </c:set>
                        <c:set target="${dropDownItem}" property="name"><mvc:message code="button.Delete"/></c:set>
                        <c:set target="${dropDownItem}" property="extraAttributes" value=" data-confirm=''"/>
                    </emm:instantiate>
                </emm:ShowByPermission>
            </c:if>
        </emm:instantiate>
    </c:if>

    <%-- Save btn --%>
    <c:if test="${not isStatsPage and not isLocked and not hidden}">
        <emm:ShowByPermission token="targets.change">
            <c:url var="saveUrl" value="/target/${targetId}/save.action"/>
            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:set target="${itemActionsSettings}" property="3" value="${element}"/>
                <c:set target="${element}" property="extraAttributes"
                       value="data-form-url='${saveUrl}' data-form-target='#settings-tile' data-form-submit-event"/>
                <c:set target="${element}" property="iconBefore" value="icon-save"/>
                <c:set target="${element}" property="name"><mvc:message code="button.Save"/></c:set>
                <c:set target="${element}" property="url" value="${saveUrl}"/>
            </emm:instantiate>
        </emm:ShowByPermission>
    </c:if>
</emm:instantiate>
