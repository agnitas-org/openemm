<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="workflowForm" type="com.agnitas.emm.core.workflow.web.forms.WorkflowForm"--%>
<%--@elvariable id="isTotalStatisticAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="autoOptData" type="com.agnitas.mailing.autooptimization.beans.impl.AutoOptimizationLight"--%>

<c:set var="STATUS_OPEN" 		value="<%= WorkflowStatus.STATUS_OPEN %>" 		scope="page" />
<c:set var="STATUS_ACTIVE" 		value="<%= WorkflowStatus.STATUS_ACTIVE %>" 	scope="page" />
<c:set var="STATUS_INACTIVE" 	value="<%= WorkflowStatus.STATUS_INACTIVE %>" 	scope="page" />
<c:set var="STATUS_COMPLETE" 	value="<%= WorkflowStatus.STATUS_COMPLETE %>" 	scope="page" />
<c:set var="STATUS_TESTING" 	value="<%= WorkflowStatus.STATUS_TESTING %>" 	scope="page" />
<c:set var="STATUS_TESTED" 		value="<%= WorkflowStatus.STATUS_TESTED %>" 	scope="page" />

<emm:CheckLogon/>
<emm:Permission token="workflow.show"/>

<c:url var="workflowsOverviewLink" value="/workflow/list.action"/>

<c:set var="agnNavigationKey" 		value="none" 									scope="request" />
<c:set var="agnNavHrefAppend" 		value="&workflowId=${workflowForm.workflowId}" 	scope="request" />
<c:set var="agnTitleKey" 			value="workflow.single" 						scope="request" />
<c:set var="sidemenu_active" 		value="Workflow" 								scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 									scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Workflow" 								scope="request" />
<c:set var="agnBreadcrumbsRootUrl" 	value="${workflowsOverviewLink}"                scope="request" />
<c:set var="agnHelpKey" 			value="help_workflow_edit" 						scope="request" />

<c:set var="workflowToggleTestingButtonEnabled" value="false" scope="request"/>
<c:set var="workflowToggleTestingButtonState" value="true" scope="request"/> <%-- Start/stop testing --%>

<c:choose>
    <c:when test="${workflowForm.status eq STATUS_OPEN || workflowForm.status eq STATUS_TESTED || workflowForm.status eq STATUS_INACTIVE}">
        <c:set var="workflowToggleTestingButtonEnabled" value="true" scope="request"/>
        <c:set var="workflowToggleTestingButtonState" value="true" scope="request"/> <%-- Start testing button --%>
    </c:when>
    <c:when test="${workflowForm.status eq STATUS_TESTING}">
        <c:set var="workflowToggleTestingButtonEnabled" value="true" scope="request"/>
        <c:set var="workflowToggleTestingButtonState" value="false" scope="request"/> <%-- Stop testing button --%>
    </c:when>
</c:choose>

<c:choose>
    <c:when test="${workflowForm.workflowId > 0 && not empty workflowForm.shortname}">
        <c:set var="agnSubtitleKey" 		value="workflow.single" scope="request" />
        <c:set var="sidemenu_sub_active" 	value="none" 			scope="request" />
        <c:set var="agnHighlightKey" 		value="workflow.single" scope="request" />

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${workflowForm.shortname}"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:set var="agnSubtitleKey" 		value="workflow.new" 		scope="request" />
        <c:set var="sidemenu_sub_active" 	value="workflow.new" 		scope="request" />
        <c:set var="agnHighlightKey" 		value="workflow.new" 		scope="request" />
        <c:set var="agnHelpKey" 			value="help_workflow_new" 	scope="request" />

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="workflow.new"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:otherwise>
</c:choose>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap"  scope="request">
    <c:if test="${workflowForm.workflowId != 0 && workflowForm.shortname != ''}">
        <%--Actions dropdown menu --%>
        <emm:instantiate var="itemAction" type="java.util.LinkedHashMap"  scope="request">
            <c:set target="${itemActionsSettings}" property="2" value="${itemAction}"/>

            <c:set target="${itemAction}" property="btnCls" value="btn btn-secondary btn-regular dropdown-toggle"/>
            <c:set target="${itemAction}" property="extraAttributes" value="data-toggle='dropdown'"/>
            <c:set target="${itemAction}" property="iconBefore" value="icon-wrench"/>
            <c:set target="${itemAction}" property="name"><mvc:message code="action.Action"/></c:set>
            <c:set target="${itemAction}" property="iconAfter" value="icon-caret-down"/>

            <%--Dropdown items --%>
            <emm:instantiate var="options" type="java.util.LinkedHashMap"  scope="request">
                <c:set target="${itemAction}" property="dropDownItems" value="${options}"/>

                <emm:ShowByPermission token="workflow.change">
                    <%--Copy button --%>
                    <emm:instantiate var="option" type="java.util.LinkedHashMap"  scope="request">
                        <c:set target="${options}" property="3" value="${option}"/>

                        <c:set target="${option}" property="url" value="#"/>
                        <c:set target="${option}" property="extraAttributes" value="data-action='workflow-copy'"/>
                        <c:set target="${option}" property="icon" value="icon-copy"/>
                        <c:set target="${option}" property="name">
                            <mvc:message code="button.Copy"/>
                        </c:set>
                    </emm:instantiate>


	                <%--Start test button --%>
	                <c:if test="${workflowToggleTestingButtonEnabled}">
	                    <c:choose>
	                        <c:when test="${workflowToggleTestingButtonState}">
	                            <c:set var="buttonText">
	                                <mvc:message code="button.workflow.testrun.start"/>
	                            </c:set>
	                        </c:when>
	                        <c:otherwise>
	                            <c:set var="buttonText">
	                                <mvc:message code="button.workflow.testrun.stop"/>
	                            </c:set>
	                        </c:otherwise>
	                    </c:choose>
	                    <c:set var="helperText">
	                        <mvc:message code="button.workflow.testrun.help"/>
	                    </c:set>
	
	                    <emm:instantiate var="option" type="java.util.LinkedHashMap"  scope="request">
	                        <c:set target="${options}" property="0" value="${option}"/>
	                        <c:set target="${option}" property="url" value="#"/>
	                        <c:set target="${option}" property="extraAttributes" value="data-action='workflow-dry-run'  data-tooltip-help='${buttonText}' data-tooltip-help-text='${helperText}' "/>
	                        <c:set target="${option}" property="icon" value="icon-fa5-project-diagram icon-fa5"/>
	                        <c:set target="${option}" property="name">${buttonText}</c:set>
	                    </emm:instantiate>
	                </c:if>
                
                </emm:ShowByPermission>

                <%-- Auto Opt Total Statistics button --%>
                <c:if test="${isTotalStatisticAvailable}">
                    <emm:instantiate var="option" type="java.util.LinkedHashMap"  scope="request">
                        <c:set target="${options}" property="2" value="${option}"/>
                        <c:set target="${option}" property="url">
                            <c:url value="/workflow/${workflowForm.workflowId}/getTotalStatistics.action"/>
                        </c:set>
                        <c:set target="${option}" property="icon" value="icon-fa5 icon-fa5-chart-bar far"/>
                        <c:set target="${option}" property="name">
                            <mvc:message code="statistic.total"/>
                        </c:set>
                    </emm:instantiate>
                </c:if>

                <emm:ShowByPermission token="workflow.delete">
                    <%--Delete button --%>
                    <emm:instantiate var="option" type="java.util.LinkedHashMap"  scope="request">
                        <c:set target="${options}" property="4" value="${option}"/>
                        <c:set target="${option}" property="url">
                            <c:url value="/workflow/${workflowForm.workflowId}/confirmDelete.action"/>
                        </c:set>
                        <c:set target="${option}" property="extraAttributes" value="data-confirm=''"/>
                        <c:set target="${option}" property="icon" value="icon-fa5 icon-fa5-trash-alt far"/>
                        <c:set target="${option}" property="name">
                            <mvc:message code="button.Delete"/>
                        </c:set>
                    </emm:instantiate>
                </emm:ShowByPermission>
            </emm:instantiate>
        </emm:instantiate>
    </c:if>

    <emm:ShowByPermission token="workflow.change">
        <emm:instantiate var="itemAction" type="java.util.LinkedHashMap"  scope="request">
            <c:set target="${itemActionsSettings}" property="3" value="${itemAction}"/>

            <c:set target="${itemAction}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${itemAction}" property="extraAttributes" value="data-form-target='#workflowForm' data-action='workflow-save'"/>
            <c:set target="${itemAction}" property="iconBefore" value="icon-save"/>
            <c:set target="${itemAction}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </emm:instantiate>
    </emm:ShowByPermission>
</emm:instantiate>
