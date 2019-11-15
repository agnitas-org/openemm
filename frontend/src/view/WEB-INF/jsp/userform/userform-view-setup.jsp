<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComUserFormEditAction" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" 			value="<%= ComUserFormEditAction.ACTION_LIST %>" />
<c:set var="ACTION_CONFIRM_DELETE" 	value="<%= ComUserFormEditAction.ACTION_CONFIRM_DELETE %>" />
<c:set var="ACTION_CLONE_FORM" 		value="<%= ComUserFormEditAction.ACTION_CLONE_FORM %>" />
<c:set var="ACTION_EXPORT" 			value="<%= ComUserFormEditAction.ACTION_EXPORT %>" />

<emm:CheckLogon/>
<emm:Permission token="forms.show"/>

<c:set var="tmpFormID" value="${userFormEditForm.formID}" scope="request"/>

<c:set var="agnNavHrefAppend" 		value="&formID=${tmpFormID}"	scope="request" />
<c:set var="agnTitleKey" 			value="Form" 					scope="request" />
<c:set var="agnSubtitleKey" 		value="Form" 					scope="request" />
<c:set var="sidemenu_active" 		value="SiteActions" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="workflow.panel.forms" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="SiteActions" 			scope="request" />
<c:set var="agnHelpKey" 			value="formView" 				scope="request" />

<c:if test="${tmpFormID eq 0}">
    <c:set var="agnNavigationKey" 	value="formView" scope="request" />
    <c:set var="agnHighlightKey" 	value="New_Form" scope="request" />
</c:if>
<c:if test="${tmpFormID ne 0}">
    <c:set var="agnNavigationKey" 	value="formViewWithLinks" 	scope="request" />
    <c:set var="agnHighlightKey" 	value="default.Content" 	scope="request" />
</c:if>

<c:set var="submitType" value="data-form-submit"/>
<c:if test="${workflowForwardParams != null && workflowForwardParams != ''}">
    <c:set var="submitType" value="data-form-submit-static"/>
</c:if>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="workflow.panel.forms"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/userform.do">
                <c:param name="action" value="${ACTION_LIST}"/>
            </c:url>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${userFormEditForm.formID eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="New_Form"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${userFormEditForm.formName}"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">

    <c:if test="${not empty workflowForwardParams}">
        <jsp:useBean id="element0" class="java.util.LinkedHashMap" scope="request">
            <c:set target="${itemActionsSettings}" property="0" value="${element0}"/>
            <c:set target="${element0}" property="btnCls" value="btn btn-secondary btn-regular"/>
            <c:set target="${element0}" property="iconBefore" value="icon-angle-left"/>
            <c:set target="${element0}" property="type" value="href"/>
            <c:set target="${element0}" property="url">
                <%--todo: GWUA-4271: change after test sucessfully--%>
                <%--<c:url value="/workflow/${workflowId}/view.action">--%>
                    <%--<c:param name="forwardParams" value="${workflowForwardParams};elementValue=${userFormEditForm.formID}"/>--%>
                <%--</c:url>--%>
                <html:rewrite page="/workflow.do?method=view&forwardParams=${workflowForwardParams};elementValue=${userFormEditForm.formID}&workflowId=${workflowId}"/>
            </c:set>
            <c:set target="${element0}" property="name">
                <bean:message key="button.Back"/>
            </c:set>
        </jsp:useBean>
    </c:if>

    <jsp:useBean id="element1" class="java.util.LinkedHashMap" scope="request">
        <c:set target="${itemActionsSettings}" property="1" value="${element1}"/>
        <c:set target="${element1}" property="btnCls" value="btn btn-secondary btn-regular dropdown-toggle"/>
        <c:set target="${element1}" property="extraAttributes" value="data-toggle='dropdown'"/>
        <c:set target="${element1}" property="iconBefore" value="icon-wrench"/>
        <c:set target="${element1}" property="name"><bean:message key="action.Action"/></c:set>
        <c:set target="${element1}" property="iconAfter" value="icon-caret-down"/>
        <jsp:useBean id="optionList" class="java.util.LinkedHashMap" scope="request">
            <c:set target="${element1}" property="dropDownItems" value="${optionList}"/>
        </jsp:useBean>

        <c:if test="${tmpFormID ne 0}">
            <emm:ShowByPermission token="forms.change">
                <jsp:useBean id="option1" class="java.util.LinkedHashMap" scope="request">
                    <c:set target="${optionList}" property="1" value="${option1}"/>
                    <emm:ShowByPermission token="mailing.change">
                        <c:set target="${option1}" property="url">
                            <html:rewrite page="/userform.do?action=${ACTION_CLONE_FORM}&formID=${tmpFormID}"/>
                        </c:set>
                        <c:set target="${option1}" property="icon" value="icon-copy"/>
                        <c:set target="${option1}" property="name">
                             <bean:message key="button.Copy"/>
                        </c:set>
                    </emm:ShowByPermission>
                </jsp:useBean>
            </emm:ShowByPermission>    
		
			<%@include file="userform-view-setup-export.jspf" %>
        
            <emm:ShowByPermission token="forms.delete">
                <jsp:useBean id="option0" class="java.util.LinkedHashMap" scope="request">
                    <c:set target="${optionList}" property="0" value="${option0}"/>
                    <c:set target="${option0}" property="url">
                        <html:rewrite page="/userform.do?action=${ACTION_CONFIRM_DELETE}&formID=${tmpFormID}"/>
                    </c:set>
                    <c:set target="${option0}" property="extraAttributes" value="data-confirm=''"/>
                    <c:set target="${option0}" property="icon" value="icon-trash-o"/>
                    <c:set target="${option0}" property="name">
                        <bean:message key="button.Delete"/>
                    </c:set>
                </jsp:useBean>
            </emm:ShowByPermission>
        </c:if>
    </jsp:useBean>

    <emm:ShowByPermission token="forms.change">
        <jsp:useBean id="element2" class="java.util.LinkedHashMap" scope="request">
            <c:set target="${itemActionsSettings}" property="2" value="${element2}"/>
            <c:set target="${element2}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element2}" property="extraAttributes" value="data-form-target='#userFormEditForm' data-form-set='save:save' ${submitType}"/>
            <c:set target="${element2}" property="iconBefore" value="icon-save"/>
            <c:set target="${element2}" property="name">
                <bean:message key="button.Save"/>
            </c:set>
        </jsp:useBean>
    </emm:ShowByPermission>

</jsp:useBean>
