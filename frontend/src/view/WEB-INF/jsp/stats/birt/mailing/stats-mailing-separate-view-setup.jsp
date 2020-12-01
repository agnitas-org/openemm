<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.MailingBaseAction" %>
<%@ page import="org.agnitas.web.forms.WorkflowParametersHelper" %>
<%@ page import="com.agnitas.emm.core.birtstatistics.enums.StatisticType" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<%--@elvariable id="mailingStatisticForm" type="com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatisticForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

<c:set var="BASE_ACTION_LIST" 					value="<%= MailingBaseAction.ACTION_LIST %>"						scope="request" />
<c:set var="BASE_ACTION_VIEW" 					value="<%= MailingBaseAction.ACTION_VIEW %>"						scope="request" />
<c:set var="BASE_ACTION_CONFIRM_DELETE" 		value="<%= MailingBaseAction.ACTION_CONFIRM_DELETE %>" 				scope="request" />
<c:set var="BASE_ACTION_CLONE_AS_MAILING" 		value="<%= MailingBaseAction.ACTION_CLONE_AS_MAILING %>"			scope="request" />

<emm:CheckLogon/>
<emm:Permission token="stats.mailing"/>

<c:set var="agnTitleKey" 			value="Mailing" 				scope="request" />
<c:set var="agnSubtitleKey" 		value="Mailing" 				scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview"		scope="request" />
<c:set var="agnHighlightKey" 		value="Statistics" 				scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Mailings" 				scope="request" />
<c:set var="agnBreadcrumbsRootUrl" 	value="${mailingsOverviewLink}" scope="request" />

<c:set var="SUMMARY_TYPE" value="<%= StatisticType.SUMMARY %>"/>
<c:set var="CLICK_PER_LINK_TYPE" value="<%= StatisticType.CLICK_STATISTICS_PER_LINK %>"/>
<c:set var="DELIVERY_PROGRESS_TYPE" value="<%= StatisticType.PROGRESS_OF_DELIVERY %>"/>
<c:set var="OPENINGS_PROGRESS_TYPE" value="<%= StatisticType.PROGRESS_OF_OPENINGS %>"/>
<c:set var="CLICK_PROGRESS_TYPE" value="<%= StatisticType.PROGRESS_OF_CLICKS %>"/>
<c:set var="TOP_DOMAINS_TYPE" value="<%= StatisticType.TOP_DOMAINS %>"/>

<c:set var="BOUNCES_TYPE" value="<%= StatisticType.BOUNCES %>"/>
<c:set var="BENCHMARK_TYPE" value="<%= StatisticType.BENCHMARK %>"/>
<c:set var="TRACKING_POINT_TYPE" value="<%= StatisticType.TRACKING_POINT_WEEK_OVERVIEW %>"/>
<c:set var="SOCIAL_NETWORKS_TYPE" value="<%= StatisticType.SOCIAL_NETWORKS %>"/>
<c:set var="SIMPLE_TRACKING_POINT_TYPE" value="<%= StatisticType.SIMPLE_TRACKING_POINT %>"/>
<c:set var="NUM_TRACKING_POINT_TYPE" value="<%= StatisticType.NUM_TRACKING_POINT_WEEK_OVERVIEW %>"/>
<c:set var="ALPHA_TRACKING_POINT_TYPE" value="<%= StatisticType.ALPHA_TRACKING_POINT %>"/>
<c:set var="DEVICES_TYPE" value="<%= StatisticType.DEVICES_OVERVIEW %>"/>

<c:set var="agnSubtitleValue" scope="request">
    <i class="icon-fa5 icon-fa5-envelope"></i>&nbsp;${mailingStatisticForm.shortname}
</c:set>

<c:choose>
    <c:when test="${isMailingGrid}">
        <c:set var="isTabsMenuShown" value="false" scope="request" />

        <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

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
    <%@ include file="stats-mailing-separate-view-setup-extended.jspf" %>
</emm:instantiate>

<c:set var="agnHelpKey" value="${reportNameToHelpKeyMap[mailingStatisticForm.statisticType]}" scope="request"/>

<c:if test="${empty agnHelpKey}">
    <c:set var="agnHelpKey" value="Statistics" scope="request"/>
</c:if>

<c:url var="mailingsOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_LIST}"/>
    <c:param name="isTemplate" value="false"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="mailingViewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_VIEW}"/>
    <c:param name="mailingID" value="${mailingStatisticForm.mailingID}"/>
    <c:param name="keepForward" value="true"/>
    <c:param name="init" value="true"/>
</c:url>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${mailingStatisticForm.shortname}"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Statistics"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="reports" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${reports}" property="">
    	 <mvc:message code="statistic.load.specific"/>
    </c:set>
    <c:set target="${reports}" property="${SUMMARY_TYPE}">
        <mvc:message code="Summary"/>
    </c:set>
    <c:set target="${reports}" property="${CLICK_PER_LINK_TYPE}">
        <mvc:message code="Clickstats_Linkclicks"/>
    </c:set>
    <c:set target="${reports}" property="${DELIVERY_PROGRESS_TYPE}">
        <mvc:message code="statistic.delivery_progress"/>
    </c:set>
    <c:set target="${reports}" property="${OPENINGS_PROGRESS_TYPE}">
        <mvc:message code="Clickstats_Openings"/>
    </c:set>
    <c:set target="${reports}" property="${CLICK_PROGRESS_TYPE}">
        <mvc:message code="statistic.clicks_progress"/>
    </c:set>
    <c:set target="${reports}" property="${TOP_DOMAINS_TYPE}">
    	<mvc:message code="statistic.TopDomains"/>
    </c:set>
    <c:set target="${reports}" property="${BOUNCES_TYPE}">
        <mvc:message code="statistic.Bounces"/>
    </c:set>
    <%@ include file="stats-mailing-separate-view-setup-extended2.jspf" %>
</emm:instantiate>

<jsp:include page="/WEB-INF/jsp/mailing/actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0"/>
    <jsp:param name="mailingId" value="${mailingStatisticForm.mailingID}"/>
    <jsp:param name="isTemplate" value="false"/>
    <jsp:param name="workflowId" value="${workflowId}"/>
</jsp:include>
