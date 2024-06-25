<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="birtStatisticUrlWithoutFormat" type="java.lang.String"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.birtstatistics.recipient.forms.RecipientStatisticForm"--%>

<c:set var="agnNavigationKey" 		value="none" 	                    scope="request" />
<c:set var="agnTitleKey" 			value="Recipient" 			        scope="request" />
<c:set var="agnSubtitleKey" 		value="Statistics" 			        scope="request" />
<c:set var="sidemenu_active" 		value="Statistics" 			        scope="request" />
<c:set var="sidemenu_sub_active" 	value="Recipients" 			        scope="request" />
<c:set var="agnHighlightKey" 		value="statistic.Recipient"         scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				        scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Statistics" 			        scope="request" />
<c:set var="agnHelpKey" 			value="recipientStatistic" 	        scope="request" />
<c:set var="agnEditViewKey" 	    value="recipients-stat-overview" 	scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Recipients"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
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
        <c:set var="csvUrl" value="${birtStatisticUrlWithoutFormat}&__format=csv"/>
        <c:if test="${form.reportName eq 'remarks'}">
            <c:url var="csvUrl" value="/statistics/recipient/remarks-csv.action?mailingListId=${form.mailingListId}&targetId=${form.targetId}"/>
        </c:if>
        <emm:instantiate var="option" type="java.util.LinkedHashMap">
            <c:set target="${optionList}" property="0" value="${option}"/>
            <c:set target="${option}" property="url">${csvUrl}</c:set>
            <c:set target="${option}" property="name"><mvc:message code="export.message.csv"/></c:set>
        </emm:instantiate>
    </emm:instantiate>

    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

        <c:set target="${element}" property="btnCls" value="btn"/>
        <c:set target="${element}" property="extraAttributes" value="data-form-target='#stat-form' data-form-submit"/>
        <c:set target="${element}" property="iconBefore" value="icon icon-sync"/>
        <c:set target="${element}" property="name">
            <mvc:message code="button.Refresh" />
        </c:set>
    </emm:instantiate>
</emm:instantiate>
