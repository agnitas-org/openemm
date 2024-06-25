<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="userFormId" type="java.lang.Integer"--%>
<%--@elvariable id="userFormName" type="java.lang.String"--%>

<c:set var="agnNavigationKey" 		value="formViewWithLinks" 							    scope="request" />
<c:set var="agnTitleKey" 			value="Form" 											scope="request" />
<c:set var="agnSubtitleKey" 		value="Form" 											scope="request" />
<c:set var="sidemenu_active" 		value="Forms" 											scope="request" />
<c:set var="sidemenu_sub_active" 	value="workflow.panel.forms" 							scope="request" />
<c:set var="agnHighlightKey" 		value="Statistics" 										scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 											scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Forms" 											scope="request" />
<c:set var="agnHelpKey" 			value="formStatistic" 									scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="user-form-id" value="${userFormId}"/>
</emm:instantiate>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/webform/list.action"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${userFormId eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="New_Form"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${userFormName}"/>
                <c:set target="${agnBreadcrumb}" property="url">
                    <c:url value="/webform/${userFormId}/view.action"/>
                </c:set>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Statistics"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
        <c:set target="${element}" property="btnCls" value="btn dropdown-toggle"/>
        <c:set target="${element}" property="extraAttributes" value="data-bs-toggle='dropdown'"/>
        <c:set target="${element}" property="iconBefore" value="icon-cloud-download-alt"/>
        <c:set target="${element}" property="name"><mvc:message code="button.Download"/></c:set>

        <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
            <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
        </emm:instantiate>

        <%-- Items for dropdown --%>
        <emm:instantiate var="option" type="java.util.LinkedHashMap">
            <c:set target="${optionList}" property="0" value="${option}"/>
            <c:set target="${option}" property="extraAttributes" value="data-prevent-load"/>
            <c:set target="${option}" property="url" value="${birtStatisticUrlWithoutFormat}&__format=csv"/>
            <c:set target="${option}" property="name"><mvc:message code="export.message.csv"/></c:set>
        </emm:instantiate>
    </emm:instantiate>
</emm:instantiate>
