<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.web.forms.WorkflowParametersHelper" %>
<%@ page import="com.agnitas.emm.core.birtstatistics.enums.StatisticType" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<%--@elvariable id="mailingStatisticForm" type="com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatisticForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>
<%--@elvariable id="downloadBirtUrl" type="java.lang.String"--%>

<emm:CheckLogon/>
<emm:Permission token="stats.mailing"/>


<c:set var="isReportCanBeShown" value="true" scope="request"/>
<c:if test="${mailingStatisticForm.statisticType eq 'TOP_DOMAINS' && isEverSent eq false}">
    <c:set var="isReportCanBeShown" value="false" scope="request"/>
</c:if>
<c:url var="mailingsOverviewLink" value="/mailing/list.action"/>

<c:set var="agnTitleKey" 			value="Mailing" 				scope="request" />
<c:set var="agnSubtitleKey" 		value="Mailing" 				scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview"		scope="request" />
<c:set var="agnHighlightKey" 		value="Statistics" 				scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Mailings" 				scope="request" />
<c:set var="agnBreadcrumbsRootUrl" 	value="${mailingsOverviewLink}" scope="request" />
<c:set var="agnEditViewKey" value="mailing-stat" scope="request"/>

<c:set var="SUMMARY_TYPE" value="<%= StatisticType.SUMMARY %>" scope="request"/>
<c:set var="CLICK_PER_LINK_TYPE" value="<%= StatisticType.CLICK_STATISTICS_PER_LINK %>" scope="request"/>
<c:set var="DELIVERY_PROGRESS_TYPE" value="<%= StatisticType.PROGRESS_OF_DELIVERY %>" scope="request"/>
<c:set var="OPENINGS_PROGRESS_TYPE" value="<%= StatisticType.PROGRESS_OF_OPENINGS %>" scope="request"/>
<c:set var="CLICK_PROGRESS_TYPE" value="<%= StatisticType.PROGRESS_OF_CLICKS %>" scope="request"/>
<c:set var="TOP_DOMAINS_TYPE" value="<%= StatisticType.TOP_DOMAINS %>" scope="request"/>
<c:set var="BOUNCES_TYPE" value="<%= StatisticType.BOUNCES %>" scope="request"/>
<c:set var="BENCHMARK_TYPE" value="<%= StatisticType.BENCHMARK %>" scope="request"/>
<c:set var="TRACKING_POINT_TYPE" value="<%= StatisticType.TRACKING_POINT_WEEK_OVERVIEW %>" scope="request"/>
<c:set var="SIMPLE_TRACKING_POINT_TYPE" value="<%= StatisticType.SIMPLE_TRACKING_POINT %>" scope="request"/>
<c:set var="NUM_TRACKING_POINT_TYPE" value="<%= StatisticType.NUM_TRACKING_POINT_WEEK_OVERVIEW %>" scope="request"/>
<c:set var="ALPHA_TRACKING_POINT_TYPE" value="<%= StatisticType.ALPHA_TRACKING_POINT %>" scope="request"/>
<c:set var="DEVICES_TYPE" value="<%= StatisticType.DEVICES_OVERVIEW %>" scope="request"/>

<c:set var="agnSubtitleValue" scope="request">
    <i class="icon-fa5 icon-fa5-envelope"></i>&nbsp;${mailingStatisticForm.shortname}
</c:set>

<c:choose>
    <c:when test="${isMailingGrid}">
        <%@ include file="../fragments/mailing-grid-navigation.jspf" %>

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="templateID" value="${mailingStatisticForm.templateId}"/>
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingStatisticForm.mailingID}"/>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${isPostMailing}">
                <c:set var="agnNavigationKey" value="mailingView_post" scope="request" />
            </c:when>
            <c:when test="${limitedRecipientOverview}">
                <c:set var="agnNavigationKey"   value="mailingView_DisabledMailinglist"     scope="request" />
            </c:when>
            <c:otherwise>
                <c:set var="agnNavigationKey"   value="mailingView"                         scope="request" />
            </c:otherwise>
        </c:choose>
        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingStatisticForm.mailingID}"/>
            <c:set target="${agnNavHrefParams}" property="init" value="true"/>
        </emm:instantiate>
    </c:otherwise>
</c:choose>

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

<c:set var="agnHelpKey" value="${reportNameToHelpKeyMap[mailingStatisticForm.statisticType]}" scope="request"/>

<c:if test="${empty agnHelpKey}">
    <c:set var="agnHelpKey" value="Statistics" scope="request"/>
</c:if>

<c:url var="mailingViewLink" value="/mailing/${mailingStatisticForm.mailingID}/settings.action">
    <c:param name="keepForward" value="true"/>
</c:url>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${mailingStatisticForm.shortname}"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
    </emm:instantiate>
</emm:instantiate>

<jsp:include page="../mailing-actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0"/>
    <jsp:param name="mailingId" value="${mailingStatisticForm.mailingID}"/>
    <jsp:param name="isTemplate" value="false"/>
    <jsp:param name="workflowId" value="${workflowId}"/>
</jsp:include>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:if test="${isReportCanBeShown && not empty downloadBirtUrl}">
        <%-- Actions dropdown --%>
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
    
            <c:set target="${element}" property="btnCls" value="btn dropdown-toggle"/>
            <c:set target="${element}" property="extraAttributes" value="data-bs-toggle='dropdown'"/>
            <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
            <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>
    
            <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
                <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
            </emm:instantiate>
    
            <%-- Items for dropdown --%>
            <emm:instantiate var="option" type="java.util.LinkedHashMap">
                <c:set target="${optionList}" property="0" value="${option}"/>
                <c:set target="${option}" property="url">${downloadBirtUrl}</c:set>
                <c:set target="${option}" property="extraAttributes" value="data-prevent-load"/>
                <c:set target="${option}" property="name"><mvc:message code="export.message.csv"/></c:set>
            </emm:instantiate>
            <%@ include file="fragments/create-single-mailing-stats-report.jspf" %>
        </emm:instantiate>
    </c:if>
    
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

        <c:set target="${element}" property="btnCls" value="btn"/>
        <c:set target="${element}" property="extraAttributes" value="data-form-target='#stat-form' data-form-submit"/>
        <c:set target="${element}" property="iconBefore" value="icon icon-sync"/>
        <c:set target="${element}" property="name">
            <mvc:message code="button.Refresh"/>
        </c:set>
    </emm:instantiate>
</emm:instantiate>
