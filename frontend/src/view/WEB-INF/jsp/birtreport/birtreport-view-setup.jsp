<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="birtReportForm" type="com.agnitas.emm.core.birtreport.forms.BirtReportForm"--%>
<%--@elvariable id="workflowParameters" type="com.agnitas.emm.core.workflow.beans.parameters.WorkflowParameters"--%>
<%--@elvariable id="hasActiveDelivery" type="java.lang.Boolean"--%>

<c:if test="${empty workflowParameters}">
    <c:set var="workflowParameters" value="${emm:getWorkflowParams(pageContext.request)}"/>
</c:if>

<c:set var="isTabsMenuShown" 		value="true" 									scope="request"/>
<c:set var="agnTitleKey" 			value="Reports" 								scope="request"/>
<c:set var="agnSubtitleKey" 		value="Reports" 								scope="request"/>
<c:set var="sidemenu_active" 		value="Statistics" 								scope="request"/>
<c:set var="sidemenu_sub_active" 	value="Reports" 								scope="request"/>
<c:set var="isBreadcrumbsShown" 	value="true" 									scope="request"/>
<c:set var="agnBreadcrumbsRootKey"	value="Statistics" 								scope="request"/>
<c:set var="agnHelpKey" 			value="reports" 								scope="request"/>

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="reportId" value="${birtReportForm.reportId}"/>
</emm:instantiate>

<c:choose>
    <c:when test="${birtReportForm.reportId != 0}">
        <c:set var="agnNavigationKey"	value="birtreportView" scope="request"/>
        <c:set var="agnHighlightKey" 	value="report.edit"	scope="request"/>
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey"	value="birtreportNew"	scope="request"/>
        <c:set var="agnHighlightKey"	value="report.new" 	scope="request"/>
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Reports"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/statistics/reports.action?restoreSort=true"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${birtReportForm.reportId eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="report.new"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${birtReportForm.shortname}"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<c:set var="workflowID" value="${workflowParameters.workflowId}"/>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">

    <c:if test="${birtReportForm.reportId ne 0 or workflowID ne 0}">
        <emm:instantiate var="action" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${action}"/>

            <emm:instantiate var="dropDownItems" type="java.util.LinkedHashMap"/>
            <c:set target="${action}" property="dropDownItems" value="${dropDownItems}"/>
            <c:set target="${action}" property="btnCls" value="btn btn-secondary btn-regular dropdown-toggle"/>
            <c:set target="${action}" property="extraAttributes" value="data-toggle='dropdown'"/>
            <c:set target="${action}" property="iconBefore" value="icon-wrench"/>
            <c:set target="${action}" property="name"><mvc:message code="action.Action"/></c:set>
            <c:set target="${action}" property="iconAfter" value="icon-caret-down"/>


            <c:if test="${birtReportForm.reportId ne 0}">
                <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                    <c:set target="${dropDownItems}" property="0" value="${dropDownItem}"/>
                    <c:set target="${dropDownItem}" property="url">
                        <c:url value="/statistics/report/evaluate.action"/>
                    </c:set>

                    <c:set target="${dropDownItem}" property="extraAttributes" value="id='reportEvaluateBtn'
                                data-form-target='#birtreportForm'"/>
                    <c:set target="${dropDownItem}" property="icon" value="icon-eye"/>
                    <c:set target="${dropDownItem}" property="name"><mvc:message code="Evaluate"/></c:set>
                </emm:instantiate>

                <c:if test="${hasActiveDelivery}">
                    <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                        <c:set target="${dropDownItems}" property="1" value="${dropDownItem}"/>
                        <c:set target="${dropDownItem}" property="icon" value="icon-times"/>
                        <c:set target="${dropDownItem}" property="url" value="#"/>
                        <c:set target="${dropDownItem}" property="extraAttributes" value="data-action='confirm-deactivate-deliveries'"/>
                        <c:set target="${dropDownItem}" property="name"><mvc:message code="report.deactivate.all"/></c:set>
                    </emm:instantiate>
                </c:if>

                <emm:ShowByPermission token="report.birt.delete">
                    <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                        <c:set target="${dropDownItems}" property="2" value="${dropDownItem}"/>
                        <c:set target="${dropDownItem}" property="url">
                            <c:url value="/statistics/report/${birtReportForm.reportId}/confirmDelete.action"/>
                        </c:set>
                        <c:set target="${dropDownItem}" property="icon" value="icon-trash-o"/>
                        <c:set target="${dropDownItem}" property="extraAttributes" value=" data-confirm=''"/>
                        <c:set target="${dropDownItem}" property="name"><mvc:message code="button.Delete"/></c:set>
                    </emm:instantiate>
                </emm:ShowByPermission>
            </c:if>

            <c:if test="${not empty workflowID and workflowID ne 0}">
                <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                    <c:set target="${dropDownItems}" property="2" value="${dropDownItem}"/>

                    <c:set target="${dropDownItem}" property="url">
                        <c:url value="/workflow/${workflowID}/view.action">
                            <c:if test="${not empty workflowParameters.workflowForwardParams}">
                                <c:param name="forwardParams" value="${workflowParameters.workflowForwardParams};elementValue=${birtReportForm.reportId}"/>
                            </c:if>
                        </c:url>
                    </c:set>
                    <c:set target="${dropDownItem}" property="icon" value="icon-reply"/>
                    <c:set target="${dropDownItem}" property="name"><mvc:message code="mailing.button.toCampaign"/></c:set>
                </emm:instantiate>
            </c:if>

        </emm:instantiate>
    </c:if>

    <emm:ShowByPermission token="report.birt.change">
        <emm:instantiate var="action" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="2" value="${action}"/>
            <c:set target="${action}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${action}" property="extraAttributes" value="id='birtReportSaveBtn' data-form-target='#birtreportForm' data-form-submit"/>
            <c:set target="${action}" property="iconBefore" value="icon-save"/>
            <c:set target="${action}" property="name"><mvc:message code="button.Save"/></c:set>
        </emm:instantiate>
    </emm:ShowByPermission>
</emm:instantiate>
