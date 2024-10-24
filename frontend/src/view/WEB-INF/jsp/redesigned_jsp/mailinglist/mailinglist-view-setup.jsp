<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="mailinglistForm" type="com.agnitas.emm.core.mailinglist.form.MailinglistForm"--%>

<c:set var="isNew" value="${mailinglistForm.id eq 0}"/>

<c:set var="agnTitleKey" 			     value="Mailinglist"		          scope="request" />
<c:set var="sidemenu_active" 		     value="Recipients"	 		          scope="request" />
<c:set var="sidemenu_sub_active" 	     value="Mailinglists"	 	          scope="request" />
<c:set var="agnBreadcrumbsRootKey"	     value="Mailinglists" 			      scope="request" />
<c:set var="agnHelpKey" 			     value="mailinglistCreate" 	          scope="request" />
<c:set var="agnEditViewKey" 	         value="mailinglist-view" 	          scope="request" />
<c:url var="agnBreadcrumbsRootUrl"       value="/mailinglist/list.action"     scope="request" />

<c:choose>
    <c:when test="${isNew}">
        <c:set var="agnHighlightKey" value="settings.NewMailinglist" scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnHighlightKey" value="settings.EditMailinglist" scope="request" />
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="mailinglistId" value="${mailinglistForm.id}"/>
</emm:instantiate>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
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
    <c:if test="${not isNew}">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="cls" value="mobile-hidden"/>
            <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
            <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>
            <emm:instantiate var="dropDownItems" type="java.util.LinkedHashMap">
                <c:set target="${element}" property="dropDownItems" value="${dropDownItems}"/>
            </emm:instantiate>
        </emm:instantiate>

        <%@include file="fragments/mailinglist-manage-approval-option.jspf" %>

        <emm:instantiate var="option" type="java.util.LinkedHashMap">
            <c:set target="${dropDownItems}" property="1" value="${option}"/>

            <c:set target="${option}" property="url">
                <c:url value="${birtStatisticUrlWithoutFormat}&__format=csv"/>
            </c:set>
            <c:set target="${option}" property="extraAttributes" value="data-prevent-load=''"/>
            <c:set target="${option}" property="name">
                <mvc:message code="report.download.csv"/>
            </c:set>
        </emm:instantiate>

        <emm:ShowByPermission token="mailinglist.delete">
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${dropDownItems}" property="2" value="${option}"/>

                <c:set target="${option}" property="url">
                    <c:url value="/mailinglist/${mailinglistForm.id}/confirmDelete.action"/>
                </c:set>
                <c:set target="${option}" property="extraAttributes" value=" data-confirm=''"/>
                <c:set target="${option}" property="name">
                    <mvc:message code="settings.mailinglist.delete"/>
                </c:set>
            </emm:instantiate>
        </emm:ShowByPermission>

        <emm:ShowByPermission token="mailinglist.recipients.delete">
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${dropDownItems}" property="3" value="${option}"/>

                <c:set target="${option}" property="url">
                    <c:url value="/mailinglist/${mailinglistForm.id}/recipientsDeleteSettings.action"/>
                </c:set>
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
            <c:set target="${element}" property="extraAttributes" value="data-form-target='#mailinglist-form' data-form-submit"/>
            <c:set target="${element}" property="iconBefore" value="icon-save"/>
            <c:set target="${element}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </emm:instantiate>
    </emm:ShowByPermission>
</emm:instantiate>
