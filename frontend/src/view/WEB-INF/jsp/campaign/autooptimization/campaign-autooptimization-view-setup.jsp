<%@ page contentType="text/html; charset=utf-8" import="com.agnitas.mailing.autooptimization.beans.ComOptimization" buffer="64kb" errorPage="/error.do" %>
<%@ page import="com.agnitas.mailing.autooptimization.beans.impl.AutoOptimizationStatus" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="optimizationForm" type="com.agnitas.mailing.autooptimization.form.OptimizationForm"--%>

<c:set var="STATUS_NOT_STARTED" value="<%=AutoOptimizationStatus.NOT_STARTED.getCode()%>" scope="request" />
<c:set var="STATUS_TEST_SEND" value="<%=AutoOptimizationStatus.TEST_SEND.getCode()%>" scope="request" />
<c:set var="STATUS_EVAL_IN_PROGRESS" value="<%=AutoOptimizationStatus.EVAL_IN_PROGRESS.getCode()%>" scope="request" />
<c:set var="STATUS_FINISHED" value="<%=AutoOptimizationStatus.FINISHED.getCode()%>" scope="request" />
<c:set var="STATUS_SCHEDULED" value="<%=AutoOptimizationStatus.SCHEDULED.getCode()%>" scope="page" />

<c:set var="admin" value="${emm.admin}" scope="request" />

<c:set var="agnNavigationKey" 		value="Archive" 					scope="request" />
<c:set var="agnTitleKey" 			value="mailing.archive" 			scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 					scope="request" />
<c:set var="sidemenu_sub_active" 	value="mailing.archive" 			scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.autooptimization"	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 						scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 					scope="request" />
<c:set var="agnHelpKey" 			value="autooptimization" 			scope="request" />

<c:choose>
	<c:when test="${optimizationForm.optimizationID != 0}">
        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="campaignID" value="${optimizationForm.campaignID}"/>
            <c:set target="${agnNavHrefParams}" property="campaignName" value="${optimizationForm.campaignName}"/>
        </emm:instantiate>

		<c:set var="agnSubtitleKey" value="mailing.autooptimization" scope="request" />
	</c:when>
	<c:otherwise>
        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="campaignID" value="${optimizationForm.campaignID}"/>
            <c:set target="${agnNavHrefParams}" property="campaignName" value="${optimizationForm.campaignName}"/>
        </emm:instantiate>

        <c:set var="agnSubtitleKey" value="mailing.autooptimization.new" scope="request" />
	</c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.archive"/>

        <c:url var="campaignOverviewLink" value="/mailing/archive/list.action"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${campaignOverviewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>

        <c:set target="${agnBreadcrumb}" property="text" value="${optimizationForm.campaignName}"/>
        <c:url var="campaignLink" value="/mailing/archive/${optimizationForm.campaignID}/view.action"/>

        <c:set target="${agnBreadcrumb}" property="url" value="${campaignLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.autooptimization"/>
        <c:url var="campaignOptimizationsLink" value="/optimization/list.action">
            <c:param name="campaignID" value="${optimizationForm.campaignID}"/>
            <c:param name="campaignName" value="${optimizationForm.campaignName}"/>
        </c:url>
        <c:set target="${agnBreadcrumb}" property="url" value="${campaignOptimizationsLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="3" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${optimizationForm.optimizationID != 0}">
                <c:set target="${agnBreadcrumb}" property="text" value="${optimizationForm.shortname}"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.autooptimization.new"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:if test="${optimizationForm.optimizationID ne 0}">
        <emm:ShowByPermission token="mailing.send.world">
            <emm:instantiate var="element2" type="java.util.LinkedHashMap">
                <c:set target="${itemActionsSettings}" property="2" value="${element2}"/>
                <c:set target="${element2}" property="btnCls" value="btn btn-regular btn-secondary"/>

                <c:url var="unScheduleLink" value="/optimization/unSchedule.action">
                    <c:param name="campaignID" value="${optimizationForm.campaignID}"/>
                    <c:param name="campaignName" value="${optimizationForm.campaignName}"/>
                </c:url>
                <c:set target="${element2}" property="extraAttributes" value="data-form-url='${unScheduleLink}' data-form-target='#optimizationForm' data-form-submit"/>

                <c:set target="${element2}" property="iconBefore" value="icon-tasks"/>
                <c:set target="${element2}" property="name"><mvc:message code="btndeactivate" /></c:set>
            </emm:instantiate>

            <emm:instantiate var="element3" type="java.util.LinkedHashMap">
                <c:set target="${itemActionsSettings}" property="3" value="${element3}"/>
                <c:set target="${element3}" property="btnCls" value="btn btn-regular btn-secondary"/>

                <c:url var="scheduleLink" value="/optimization/schedule.action">
                    <c:param name="campaignID" value="${optimizationForm.campaignID}"/>
                    <c:param name="campaignName" value="${optimizationForm.campaignName}"/>
                </c:url>
                <c:set target="${element3}" property="extraAttributes" value="data-form-url='${scheduleLink}' data-form-target='#optimizationForm' data-form-submit"/>

                <c:set target="${element3}" property="iconBefore" value="icon-tasks"/>
                <c:set target="${element3}" property="name" />
                <c:set target="${element3}" property="name"><mvc:message code="button.Activate" /></c:set>
            </emm:instantiate>
        </emm:ShowByPermission>
    </c:if>

    <c:if test="${optimizationForm.optimizationID ne 0}">
        <emm:ShowByPermission token="campaign.delete">
            <c:if test="${optimizationForm.status == STATUS_NOT_STARTED || optimizationForm.status == STATUS_TEST_SEND || optimizationForm.status == STATUS_EVAL_IN_PROGRESS }">
                <emm:instantiate var="element0" type="java.util.LinkedHashMap">
                    <c:set target="${itemActionsSettings}" property="0" value="${element0}"/>
                    <c:set target="${element0}" property="btnCls" value="btn btn-regular btn-alert"/>
                    <c:set target="${element0}" property="type" value="href"/>
                    <c:set target="${element0}" property="url">
                        <c:url value="/optimization/${optimizationForm.optimizationID}/confirmDelete.action">
                            <c:param name="campaignID" value="${optimizationForm.campaignID}"/>
                            <c:param name="campaignName" value="${optimizationForm.campaignName}"/>
                        </c:url>
                    </c:set>
                    <c:set target="${element0}" property="extraAttributes" value="data-confirm"/>
                    <c:set target="${element0}" property="iconBefore" value="icon-trash-o"/>
                    <c:set target="${element0}" property="name">
                        <mvc:message code="button.Delete"/>
                    </c:set>
                </emm:instantiate>
            </c:if>
        </emm:ShowByPermission>
    </c:if>

    <c:if test="${optimizationForm.status == STATUS_NOT_STARTED }">
        <emm:instantiate var="element1" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="1" value="${element1}"/>
            <c:set target="${element1}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element1}" property="extraAttributes" value="data-form-target='#optimizationForm' data-form-submit"/>
            <c:set target="${element1}" property="iconBefore" value="icon-save"/>
            <c:set target="${element1}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </emm:instantiate>
    </c:if>
</emm:instantiate>
