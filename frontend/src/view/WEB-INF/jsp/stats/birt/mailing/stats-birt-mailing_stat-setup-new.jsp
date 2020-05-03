<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.MailingBaseAction" %>
<%@ page import="org.agnitas.web.forms.WorkflowParametersHelper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<%--@elvariable id="birtStatForm" type="com.agnitas.emm.core.birtstatistics.mailing.forms.BirtStatForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

<c:set var="BASE_ACTION_LIST" 					value="<%= MailingBaseAction.ACTION_LIST %>"						scope="request" />
<c:set var="BASE_ACTION_VIEW" 					value="<%= MailingBaseAction.ACTION_VIEW %>"						scope="request" />
<c:set var="BASE_ACTION_CONFIRM_DELETE" 		value="<%= MailingBaseAction.ACTION_CONFIRM_DELETE %>" 				scope="request" />
<c:set var="BASE_ACTION_CLONE_AS_MAILING" 		value="<%= MailingBaseAction.ACTION_CLONE_AS_MAILING %>"			scope="request" />
<c:set var="WORKFLOW_ID"                        value="<%= WorkflowParametersHelper.WORKFLOW_ID %>" scope="request" />
<c:set var="WORKFLOW_FORWARD_PARAMS" 			value="<%= WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS %>"			scope="request" />
<c:set var="WORKFLOW_FORWARD_TARGET_ITEM_ID" 	value="<%= WorkflowParametersHelper.WORKFLOW_FORWARD_TARGET_ITEM_ID %>"	scope="request" />

<emm:CheckLogon/>
<emm:Permission token="stats.mailing"/>

<c:set var="agnTitleKey" 			value="Mailing" 				scope="request" />
<c:set var="agnSubtitleKey" 		value="Mailing" 				scope="request" />
<c:set var="sidemenu_active" 		value="Statistics" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="MailStat" 				scope="request" />
<c:set var="agnHighlightKey" 		value="Statistics" 				scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Mailings" 				scope="request" />
<c:set var="agnBreadcrumbsRootUrl" 	value="${mailingsOverviewLink}" scope="request" />

<c:set var="agnSubtitleValue" scope="request">
    <i class="icon icon-envelope"></i>&nbsp;${birtStatForm.shortname}
</c:set>

<c:choose>
    <c:when test="${isMailingGrid}">
        <c:set var="isTabsMenuShown" value="false" scope="request" />

        <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${birtStatForm.mailingID}"/>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${limitedRecipientOverview}">
                <c:set var="agnNavigationKey"   value="mailingView_DisabledMailinglist"     scope="request" />
            </c:when>
            <c:otherwise>
                <c:set var="agnNavigationKey"   value="mailingView"                         scope="request" />
            </c:otherwise>
        </c:choose>
        <c:set var="agnNavHrefAppend" value="?mailingID=${birtStatForm.mailingID}&init=true" scope="request" />
    </c:otherwise>
</c:choose>

<emm:instantiate var="reportNameToHelpKeyMap" type="java.util.HashMap">
    <c:set target="${reportNameToHelpKeyMap}" property="mailing_summary.rptdesign" value="summary"/>
    <c:set target="${reportNameToHelpKeyMap}" property="mailing_linkclicks.rptdesign" value="clickstatistic_per_link"/>
    <c:set target="${reportNameToHelpKeyMap}" property="mailing_delivery_progress.rptdesign" value="deliveryProgressStatistic"/>
    <c:set target="${reportNameToHelpKeyMap}" property="mailing_net_and_gross_openings_progress.rptdesign" value="Progress_of_openings"/>
    <c:set target="${reportNameToHelpKeyMap}" property="mailing_linkclicks_progress.rptdesign" value="Progress_of_clicks"/>
    <c:set target="${reportNameToHelpKeyMap}" property="top_domains.rptdesign" value="top_domains"/>
    <c:set target="${reportNameToHelpKeyMap}" property="mailing_bounces.rptdesign" value="bounceStatistic"/>
    <%@ include file="../stats-birt-mailing_stat-setup-extended.jspf" %>
</emm:instantiate>

<c:set var="agnHelpKey" value="${reportNameToHelpKeyMap[birtStatForm.reportName]}" scope="request"/>

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
    <c:param name="mailingID" value="${birtStatForm.mailingID}"/>
    <c:param name="keepForward" value="true"/>
    <c:param name="init" value="true"/>
</c:url>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${birtStatForm.shortname}"/>
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
    <c:set target="${reports}" property="mailing_summary.rptdesign">
        <mvc:message code="Summary"/>
    </c:set>
    <c:set target="${reports}" property="mailing_linkclicks.rptdesign">
        <mvc:message code="Clickstats_Linkclicks"/>
    </c:set>
    <c:set target="${reports}" property="mailing_delivery_progress.rptdesign">
        <mvc:message code="statistic.delivery_progress"/>
    </c:set>
    <c:set target="${reports}" property="mailing_net_and_gross_openings_progress.rptdesign">
        <mvc:message code="Clickstats_Openings"/>
    </c:set>
    <c:set target="${reports}" property="mailing_linkclicks_progress.rptdesign">
        <mvc:message code="statistic.clicks_progress"/>
    </c:set>
    <c:set target="${reports}" property="top_domains.rptdesign">
    	<mvc:message code="statistic.TopDomains"/>
    </c:set>
    <c:set target="${reports}" property="mailing_bounces.rptdesign">
        <mvc:message code="statistic.Bounces"/>
    </c:set>
    <%@ include file="../stats-birt-mailing_stat-setup-extended2.jspf" %>
</emm:instantiate>

<jsp:include page="/jsp/mailing/actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0"/>
    <jsp:param name="mailingId" value="${birtStatForm.mailingID}"/>
    <jsp:param name="isTemplate" value="false"/>
    <jsp:param name="workflowId" value="${workflowId}"/>
</jsp:include>
