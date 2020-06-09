<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="mailinglistForm" type="com.agnitas.emm.core.mailinglist.form.MailinglistForm"--%>

<c:set var="isNew" value="${mailinglistForm.id eq 0}"/>

<c:set var="agnTitleKey" 			value="NewMailinglist"		scope="request" />
<c:set var="sidemenu_active" 		value="Recipients"	 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="Mailinglists"	 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Recipients" 			scope="request" />
<c:set var="agnHelpKey" 			value="mailinglistCreate" 	scope="request" />

<c:choose>
    <c:when test="${isNew}">
        <c:set var="agnHighlightKey"    value="settings.NewMailinglist"     scope="request" />
        <c:set var="isTabsMenuShown"    value="false" 				        scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnHighlightKey" 	value="settings.EditMailinglist" 	scope="request" />
        <c:set var="isTabsMenuShown" 	value="true" 			            scope="request" />
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="mailinglistId" value="${mailinglistForm.id}"/>
</emm:instantiate>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
	<emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Mailinglists"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/mailinglist/list.action"/>
        </c:set>
    </emm:instantiate>
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${isNew}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="settings.NewMailinglist"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailinglistForm.shortname}"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">

    <%-- Dropdown actions menu button --%>
    <c:if test="${!isNew}">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <emm:instantiate var="dropDownItems" type="java.util.LinkedHashMap">
                <c:set target="${element}" property="dropDownItems" value="${dropDownItems}"/>
            </emm:instantiate>
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="btnCls" value="btn btn-secondary btn-regular dropdown-toggle"/>
            <c:set target="${element}" property="extraAttributes" value="data-toggle='dropdown'"/>
            <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
            <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>
            <c:set target="${element}" property="iconAfter" value="icon-caret-down"/>
        </emm:instantiate>

        <emm:ShowByPermission token="mailinglist.delete">
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${dropDownItems}" property="0" value="${option}"/>

                <c:set target="${option}" property="url">
                    <c:url value="/mailinglist/${mailinglistForm.id}/confirmDelete.action"/>
                </c:set>
                <c:set target="${option}" property="icon" value="icon-trash-o"/>
                <c:set target="${option}" property="extraAttributes" value=" data-confirm=''"/>
                <c:set target="${option}" property="name">
                    <mvc:message code="button.Delete"/>
                </c:set>
            </emm:instantiate>
        </emm:ShowByPermission>

        <emm:ShowByPermission token="mailinglist.recipients.delete">
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${dropDownItems}" property="1" value="${option}"/>

                <c:set target="${option}" property="url">
                    <c:url value="/mailinglist/${mailinglistForm.id}/usersDeleteSettings.action"/>
                </c:set>
                <c:set target="${option}" property="icon" value="icon-trash-o"/>
                <c:set target="${option}" property="extraAttributes" value=" data-confirm=''"/>
                <c:set target="${option}" property="name">
                    <mvc:message code="mailinglist.delete.recipients"/>
                </c:set>
            </emm:instantiate>
        </emm:ShowByPermission>
    </c:if>

    <emm:ShowByPermission token="mailinglist.change">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

            <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element}" property="extraAttributes" value="data-form-target='#mailinglistForm' data-form-submit"/>
            <c:set target="${element}" property="iconBefore" value="icon-save"/>
            <c:set target="${element}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </emm:instantiate>

    </emm:ShowByPermission>
</emm:instantiate>
