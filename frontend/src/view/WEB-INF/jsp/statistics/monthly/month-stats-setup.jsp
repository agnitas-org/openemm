<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="agnTitleKey" 			value="MonthStats" 		    scope="request" />
<c:set var="sidemenu_active" 		value="Statistics" 		    scope="request" />
<c:set var="sidemenu_sub_active" 	value="MonthStats" 		    scope="request" />
<c:set var="agnHighlightKey" 		value="MonthStats" 		    scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Statistics" 		    scope="request" />
<c:set var="agnHelpKey" 			value="monthlyOverview"     scope="request" />
<c:set var="agnEditViewKey" 	    value="monthly-overview" 	scope="request" />

<%--@elvariable id="birtStatisticUrlCsvReport" type="java.lang.String"--%>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="MonthStats"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

        <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
        <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

        <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
            <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
        </emm:instantiate>

        <c:set var="csvUrl" value="${birtStatisticUrlCsvReport}&__format=csv"/>

        <%-- Items for dropdown --%>
        <emm:instantiate var="option" type="java.util.LinkedHashMap">
            <c:set target="${optionList}" property="0" value="${option}"/>
            <c:set target="${option}" property="url">${csvUrl}</c:set>
            <c:set target="${option}" property="name"><mvc:message code="export.message.csv"/></c:set>
        </emm:instantiate>
    </emm:instantiate>

    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

        <c:set target="${element}" property="extraAttributes" value="data-form-target='#stat-form' data-form-submit"/>
        <c:set target="${element}" property="iconBefore" value="icon icon-sync"/>
        <c:set target="${element}" property="name">
            <mvc:message code="button.Refresh" />
        </c:set>
    </emm:instantiate>
</emm:instantiate>
