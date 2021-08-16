<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.action.form.EmmActionForm"--%>

<c:choose>
    <c:when test="${form.id eq 0}">
        <c:set var="isTabsMenuShown" 	value="false" 				scope="request"/>
        <c:set var="agnNavigationKey"	value="Action" 				scope="request"/>
        <c:set var="agnHighlightKey" 	value="action.New_Action"	scope="request"/>
    </c:when>
    <c:otherwise>
        <c:set var="isTabsMenuShown" 	value="true" 				scope="request"/>
        <c:set var="agnNavigationKey"	value="ActionEdit" 			scope="request"/>
        <c:set var="agnHighlightKey" 	value="action.Edit_Action"	scope="request"/>
    </c:otherwise>
</c:choose>

<c:set var="agnTitleKey" 			value="Actions" 							scope="request" />
<c:set var="agnSubtitleKey" 		value="Actions" 							scope="request" />
<c:set var="sidemenu_active" 		value="TriggerManagement" 					scope="request" />
<c:set var="sidemenu_sub_active"	value="Actions"								scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 								scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="TriggerManagement" 					scope="request" />
<c:set var="agnHelpKey" 			value="actionList" 							scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="action-id" value="${form.id}"/>
</emm:instantiate>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/action/list.action"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${form.id eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="action.New_Action"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${form.shortname}"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:if test="${form.id ne 0}">
        <%-- Actions dropdown --%>

        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${element}" property="btnCls" value="btn btn-secondary btn-regular dropdown-toggle"/>
            <c:set target="${element}" property="extraAttributes" value="data-toggle='dropdown'"/>
            <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
            <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>
            <c:set target="${element}" property="iconAfter" value="icon-caret-down"/>

            <emm:instantiate var="dropDownItems" type="java.util.LinkedHashMap">
                <c:set target="${element}" property="dropDownItems" value="${dropDownItems}"/>
            </emm:instantiate>

            <emm:ShowByPermission token="actions.change">
                <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                    <c:set target="${dropDownItems}" property="1" value="${dropDownItem}"/>
                    <c:set target="${dropDownItem}" property="url">
                        <c:url value="/action/${form.id}/clone.action"/>
                    </c:set>
                    <c:set target="${dropDownItem}" property="icon" value="icon-copy"/>
                    <c:set target="${dropDownItem}" property="name">
                         <mvc:message code="button.Copy"/>
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>

            <emm:ShowByPermission token="actions.delete">
                <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                    <c:set target="${dropDownItems}" property="0" value="${dropDownItem}"/>
                    <c:set target="${dropDownItem}" property="url">
                        <c:url value="/action/${form.id}/confirmDelete.action"/>
                    </c:set>
                    <c:set target="${dropDownItem}" property="extraAttributes" value="data-confirm=''"/>
                    <c:set target="${dropDownItem}" property="icon" value="icon-trash-o"/>
                    <c:set target="${dropDownItem}" property="name">
                        <mvc:message code="button.Delete"/>
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>


            <%-- Hide dropdown when it doesn't contain at least one item --%>

            <c:if test="${not empty dropDownItems}">
                <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
            </c:if>
        </emm:instantiate>
    </c:if>

    <emm:ShowByPermission token="actions.change">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
            <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element}" property="extraAttributes" value="data-form-target='#emmActionForm' data-form-submit-event=''"/>
            <c:set target="${element}" property="iconBefore" value="icon-save"/>
            <c:set target="${element}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </emm:instantiate>
    </emm:ShowByPermission>
</emm:instantiate>
