<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.birtstatistics.enums.StatisticType" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingStatisticForm" type="com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatisticForm"--%>
<%--@elvariable id="isActiveMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="mailinglistDisabled" type="java.lang.Boolean"--%>
<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="downloadBirtUrl" type="java.lang.String"--%>
<%--@elvariable id="workflowName" type="java.lang.String"--%>
<%--@elvariable id="hideStatActions" type="java.lang.Boolean"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="statWorkflowId" type="java.lang.Integer"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>

<c:set var="forCampaign" value="${statWorkflowId > 0}" scope="request"/>

<emm:Permission token="stats.mailing"/>

<c:set var="isReportCanBeShown" value="true" scope="request"/>
<c:if test="${mailingStatisticForm.statisticType eq 'TOP_DOMAINS' && isEverSent eq false}">
    <c:set var="isReportCanBeShown" value="false" scope="request"/>
</c:if>

<c:set var="agnTitleKey" 			value="${forCampaign ? 'Workflow' : 'Mailing'}"  scope="request" />
<c:set var="sidemenu_active" 		value="${forCampaign ? 'Workflow' : 'Mailings'}" scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview"		                 scope="request" />
<c:set var="agnHighlightKey" 		value="Statistics" 				                 scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="${forCampaign ? 'Workflow' : 'Mailings'}" scope="request" />
<c:url var="agnBreadcrumbsRootUrl" 	value="/mailing/list.action"                     scope="request" />
<c:set var="agnEditViewKey"         value="mailing-stat"                             scope="request" />

<c:set var="SUMMARY_TYPE"               value="<%= StatisticType.SUMMARY %>"                          scope="request"/>
<c:set var="CLICK_PER_LINK_TYPE"        value="<%= StatisticType.CLICK_STATISTICS_PER_LINK %>"        scope="request"/>
<c:set var="DELIVERY_PROGRESS_TYPE"     value="<%= StatisticType.PROGRESS_OF_DELIVERY %>"             scope="request"/>
<c:set var="OPENINGS_PROGRESS_TYPE"     value="<%= StatisticType.PROGRESS_OF_OPENINGS %>"             scope="request"/>
<c:set var="CLICK_PROGRESS_TYPE"        value="<%= StatisticType.PROGRESS_OF_CLICKS %>"               scope="request"/>
<c:set var="TOP_DOMAINS_TYPE"           value="<%= StatisticType.TOP_DOMAINS %>"                      scope="request"/>
<c:set var="BOUNCES_TYPE"               value="<%= StatisticType.BOUNCES %>"                          scope="request"/>
<c:set var="BENCHMARK_TYPE"             value="<%= StatisticType.BENCHMARK %>"                        scope="request"/>
<c:set var="TRACKING_POINT_TYPE"        value="<%= StatisticType.TRACKING_POINT_WEEK_OVERVIEW %>"     scope="request"/>
<c:set var="SIMPLE_TRACKING_POINT_TYPE" value="<%= StatisticType.SIMPLE_TRACKING_POINT %>"            scope="request"/>
<c:set var="NUM_TRACKING_POINT_TYPE"    value="<%= StatisticType.NUM_TRACKING_POINT_WEEK_OVERVIEW %>" scope="request"/>
<c:set var="ALPHA_TRACKING_POINT_TYPE"  value="<%= StatisticType.ALPHA_TRACKING_POINT %>"             scope="request"/>
<c:set var="DEVICES_TYPE"               value="<%= StatisticType.DEVICES_OVERVIEW %>"                 scope="request"/>

<c:choose>
    <c:when test="${forCampaign}">
        <c:set var="agnNavigationKey" value="campaign" scope="request" />
        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="workflowId" value="${workflowId}"/>
        </emm:instantiate>
    </c:when>
    <c:when test="${isMailingGrid}">
        <c:set var="agnNavigationKey" value="GridMailingView" scope="request" />

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="templateID" value="${mailingStatisticForm.templateId}"/>
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingStatisticForm.mailingID}"/>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" value="mailingView" scope="request" />

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingStatisticForm.mailingID}"/>
            <c:set target="${agnNavHrefParams}" property="init" value="true"/>
        </emm:instantiate>
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnNavConditionsParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavConditionsParams}" property="isActiveMailing" value="${isActiveMailing}" />
    <c:set target="${agnNavConditionsParams}" property="mailinglistDisabled" value="${mailinglistDisabled}" />
    <c:set target="${agnNavConditionsParams}" property="isPostMailing" value="${not empty isPostMailing and isPostMailing}" />
</emm:instantiate>

<emm:instantiate var="reportNameToHelpKeyMap" type="java.util.HashMap">
    <c:set target="${reportNameToHelpKeyMap}" property="${SUMMARY_TYPE}" value="summary"/>
    <c:set target="${reportNameToHelpKeyMap}" property="${CLICK_PER_LINK_TYPE}" value="clickstatistic_per_link"/>
    <c:set target="${reportNameToHelpKeyMap}" property="${DELIVERY_PROGRESS_TYPE}" value="deliveryProgressStatistic"/>
    <c:set target="${reportNameToHelpKeyMap}" property="${OPENINGS_PROGRESS_TYPE}" value="Progress_of_openings"/>
    <c:set target="${reportNameToHelpKeyMap}" property="${CLICK_PROGRESS_TYPE}" value="Progress_of_clicks"/>
    <c:set target="${reportNameToHelpKeyMap}" property="${TOP_DOMAINS_TYPE}" value="top_domains"/>
    <c:set target="${reportNameToHelpKeyMap}" property="${BOUNCES_TYPE}" value="bounceStatistic"/>
    <%@ include file="fragments/help-keys-extended.jspf" %>
</emm:instantiate>

<c:set var="agnHelpKey" value="${forCampaign ? 'workflow' : reportNameToHelpKeyMap[mailingStatisticForm.statisticType]}" scope="request"/>

<c:if test="${empty agnHelpKey}">
    <c:set var="agnHelpKey" value="Statistics" scope="request"/>
</c:if>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <c:choose>
        <c:when test="${forCampaign}">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${workflowName}"/>
                <c:set target="${agnBreadcrumb}" property="url">
                    <c:url value="/workflow/${statWorkflowId}/view.action" />
                </c:set>
            </emm:instantiate>
        </c:when>
        <c:otherwise>
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingStatisticForm.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url">
                    <c:url value="/mailing/${mailingStatisticForm.mailingID}/settings.action">
                        <c:param name="keepForward" value="true"/>
                    </c:url>
                </c:set>
            </emm:instantiate>
        </c:otherwise>
    </c:choose>
</emm:instantiate>

<c:if test="${isReportCanBeShown && not empty downloadBirtUrl and not hideStatActions}">
    <emm:instantiate var="agnMailingExtraOptions" type="java.util.LinkedHashMap" scope="request">
        <emm:instantiate var="option" type="java.util.LinkedHashMap">
            <c:set target="${agnMailingExtraOptions}" property="0" value="${option}"/>
            <c:set target="${option}" property="url">${downloadBirtUrl}</c:set>
            <c:set target="${option}" property="extraAttributes" value="data-prevent-load"/>
            <c:set target="${option}" property="name"><mvc:message code="export.message.csv"/></c:set>
        </emm:instantiate>

        <c:url var="singleReportCreationLink" value="/statistics/singleMailingStatistics/create.action">
            <c:param name="mailingId" value="${mailingStatisticForm.mailingID}"/>
        </c:url>

        <emm:instantiate var="option" type="java.util.LinkedHashMap">
            <c:set target="${agnMailingExtraOptions}" property="1" value="${option}"/>
            <c:set target="${option}" property="url">${singleReportCreationLink}</c:set>
            <c:set target="${option}" property="extraAttributes" value="id='reportEvaluateBtn' data-evaluate-loader"/>
            <c:set target="${option}" property="name"><mvc:message code="button.generate.pdf"/></c:set>
        </emm:instantiate>
    </emm:instantiate>
</c:if>

<jsp:include page="../mailing-actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0" />
    <jsp:param name="mailingId" value="${mailingStatisticForm.mailingID}" />
    <jsp:param name="isTemplate" value="false" />
    <jsp:param name="workflowId" value="${workflowId}" />
</jsp:include>

<emm:instantiate var="element" type="java.util.LinkedHashMap">
    <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

    <c:url var="workflowTotalStatUrl" value="/workflow/${statWorkflowId}/statistic.action"/>
    <c:set var="workflowTotalStatUrlAttr" value="data-form-url='${workflowTotalStatUrl}'"/>

    <c:set target="${element}" property="extraAttributes" value="${forCampaign and (empty mailingId or mailingId <= 0) ? workflowTotalStatUrlAttr : ''}  data-form-target='#stat-form' data-form-submit"/>
    <c:set target="${element}" property="iconBefore" value="icon icon-sync"/>
    <c:set target="${element}" property="name">
        <mvc:message code="button.Refresh"/>
    </c:set>
</emm:instantiate>
