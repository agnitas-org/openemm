<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.userform.form.UserFormForm"--%>

<%--@elvariable id="workflowParameters" type="org.agnitas.web.forms.WorkflowParameters"--%>

<c:if test="${empty workflowParameters}">
    <c:set var="workflowParameters" value="${emm:getWorkflowParams(pageContext.request)}"/>
</c:if>

<c:set var="agnTitleKey" 			value="Form" 					scope="request" />
<c:set var="agnSubtitleKey" 		value="Form" 					scope="request" />
<c:set var="sidemenu_active" 		value="Forms" 					scope="request" />
<c:set var="sidemenu_sub_active" 	value="workflow.panel.forms" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Forms" 					scope="request" />
<c:set var="agnHelpKey" 			value="formViewNew" 				scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="user-form-id" value="${form.formId}"/>
</emm:instantiate>

<c:if test="${form.formId eq 0}">
    <c:set var="agnNavigationKey" 	value="formViewNew" scope="request" />
    <c:set var="agnHighlightKey" 	value="New_Form" scope="request" />
</c:if>
<c:if test="${form.formId gt 0}">
    <c:set var="agnNavigationKey" 	value="formViewWithLinksNew" 	scope="request" />
    <c:set var="agnHighlightKey" 	value="default.Content" 	scope="request" />
</c:if>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="workflow.panel.forms"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/webform/list.action"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${form.formId eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="New_Form"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${form.formName}"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<c:set var="workflowID" value="${workflowParameters.workflowId}"/>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">

    <c:if test="${not empty workflowID and workflowID ne 0}">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="btnCls" value="btn btn-secondary btn-regular"/>
            <c:set target="${element}" property="iconBefore" value="icon-angle-left"/>
            <c:set target="${element}" property="type" value="href"/>
            <c:set target="${element}" property="url">
                <c:url value="/workflow/${workflowID}/view.action">
                    <c:if test="${not empty workflowParameters.workflowForwardParams}">
                        <c:param name="forwardParams" value="${workflowParameters.workflowForwardParams};elementValue=${form.formId}"/>
                    </c:if>
                </c:url>
            </c:set>
            <c:set target="${element}" property="name">
                <mvc:message code="button.Back"/>
            </c:set>
        </emm:instantiate>
    </c:if>

    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <emm:instantiate var="dropDownItems" type="java.util.LinkedHashMap"/>
        <c:set target="${element}" property="btnCls" value="btn btn-secondary btn-regular dropdown-toggle"/>
        <c:set target="${element}" property="extraAttributes" value="data-toggle='dropdown'"/>
        <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
        <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>
        <c:set target="${element}" property="iconAfter" value="icon-caret-down"/>
        <c:set target="${element}" property="dropDownItems" value="${dropDownItems}"/>

        <c:if test="${form.formId gt 0}">
            <emm:ShowByPermission token="forms.change|mailing.change">
                <emm:instantiate var="option" type="java.util.LinkedHashMap">
                    <c:set target="${dropDownItems}" property="1" value="${option}"/>
                    <c:set target="${option}" property="url">
                        <c:url value="/webform/${form.formId}/clone.action"/>
                    </c:set>
                    <c:set target="${option}" property="icon" value="icon-copy"/>
                    <c:set target="${option}" property="name">
                         <mvc:message code="button.Copy"/>
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>

			<%@include file="userform-view-setup-export-new.jspf" %>

            <emm:ShowByPermission token="forms.delete">
                <emm:instantiate var="option" type="java.util.LinkedHashMap">
                    <c:set target="${dropDownItems}" property="0" value="${option}"/>
                    <c:set target="${option}" property="url">
                        <c:url value="/webform/${form.formId}/confirmDelete.action"/>
                    </c:set>
                    <c:set target="${option}" property="extraAttributes" value="data-confirm=''"/>
                    <c:set target="${option}" property="icon" value="icon-trash-o"/>
                    <c:set target="${option}" property="name">
                         <mvc:message code="button.Delete"/>
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>
        </c:if>
        <%-- Do not show a dropdown when it's empty --%>

        <c:if test="${not empty dropDownItems}">
            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
        </c:if>
    </emm:instantiate>

     <emm:ShowByPermission token="forms.change">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="2" value="${element}"/>
            <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element}" property="extraAttributes" value="data-form-target='#userFormForm'"/>
            <c:set target="${element}" property="iconBefore" value="icon-save"/>
            <c:set target="${element}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </emm:instantiate>
    </emm:ShowByPermission>

</emm:instantiate>
