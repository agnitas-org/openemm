<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.userform.form.UserFormForm"--%>
<%--@elvariable id="isActive" type="java.lang.Boolean"--%>
<%--@elvariable id="workflowParameters" type="com.agnitas.emm.core.workflow.beans.parameters.WorkflowParameters"--%>

<c:if test="${empty workflowParameters}">
    <c:set var="workflowParameters" value="${emm:getWorkflowParams(pageContext.request)}"/>
</c:if>

<c:set var="agnTitleKey" 			value="Form" 					scope="request" />
<c:set var="sidemenu_active" 		value="Forms" 					scope="request" />
<c:set var="sidemenu_sub_active" 	value="Forms" 	                scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Forms" 					scope="request" />
<c:url var="agnBreadcrumbsRootUrl" 	value="/webform/list.action"	scope="request" />
<c:set var="agnHelpKey" 			value="createNewForm" 			scope="request" />
<c:set var="agnEditViewKey" 	    value="userform-view"           scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="user-form-id" value="${form.formId}"/>
</emm:instantiate>

<c:if test="${form.formId gt 0}">
    <c:set var="agnNavigationKey" 	value="formViewWithLinks" 	scope="request" />
    <c:set var="agnHighlightKey" 	value="default.Content" 	scope="request" />
</c:if>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
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
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

        <emm:instantiate var="dropDownItems" type="java.util.LinkedHashMap"/>
        <c:set target="${element}" property="cls" value="mobile-hidden"/>
        <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
        <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

        <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
            <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
        </emm:instantiate>

        <c:if test="${form.formId gt 0}">
            <emm:ShowByPermission token="forms.change|mailing.change">
                <emm:instantiate var="option" type="java.util.LinkedHashMap">
                    <c:set target="${optionList}" property="0" value="${option}"/>
                    <c:set target="${option}" property="url">
                        <c:url value="/webform/${form.formId}/clone.action"/>
                    </c:set>
                    <c:set target="${option}" property="name">
                         <mvc:message code="button.Copy"/>
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>

            <emm:ShowByPermission token="forms.change">
                <emm:instantiate var="option" type="java.util.LinkedHashMap">
                    <c:set target="${optionList}" property="1" value="${option}"/>
                    <c:set target="${option}" property="url">
                        <c:url value="/webform/changeActiveness.action">
                            <c:param name="ids" value="${form.formId}" />
                            <c:param name="activate" value="${not isActive}" />
                        </c:url>
                    </c:set>
                    <c:set target="${option}" property="name">
                        <mvc:message code="${isActive ? 'btndeactivate' : 'button.Activate'}"/>
                    </c:set>
                    <c:set target="${option}" property="extraAttributes" value="data-ajax='post'"/>
                </emm:instantiate>
            </emm:ShowByPermission>

			<emm:ShowByPermission token="forms.export">
			    <emm:instantiate var="option" type="java.util.LinkedHashMap">
			        <c:set target="${optionList}" property="2" value="${option}"/>
			        <c:set target="${option}" property="url">
			            <c:url value="/webform/${form.formId}/export.action"/>
			        </c:set>
			        <c:set target="${option}" property="extraAttributes" value="data-prevent-load=''"/>
			        <c:set target="${option}" property="name">
			             <mvc:message code="forms.export"/>
			        </c:set>
			    </emm:instantiate>
			</emm:ShowByPermission>

            <emm:ShowByPermission token="forms.delete">
                <emm:instantiate var="option" type="java.util.LinkedHashMap">
                    <c:set target="${optionList}" property="3" value="${option}"/>
                    <c:set target="${option}" property="url">
                        <c:url value="/webform/delete.action?bulkIds=${form.formId}"/>
                    </c:set>
                    <c:set target="${option}" property="extraAttributes" value="data-confirm=''"/>
                    <c:set target="${option}" property="name">
                         <mvc:message code="button.Delete"/>
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>
        </c:if>
    </emm:instantiate>

     <emm:ShowByPermission token="forms.change">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="2" value="${element}"/>
            <c:set target="${element}" property="extraAttributes" value="data-form-target='#userFormForm' data-form-submit-event"/>
            <c:set target="${element}" property="iconBefore" value="icon-save"/>
            <c:set target="${element}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </emm:instantiate>
    </emm:ShowByPermission>
</emm:instantiate>
