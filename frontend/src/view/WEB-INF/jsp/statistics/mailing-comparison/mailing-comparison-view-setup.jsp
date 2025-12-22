<%@ page contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.action" %>

<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="agnBreadcrumbsRootKey" 	value="Statistics" 		        scope="request"/>
<c:set var="agnTitleKey" 			value="statistic.comparison" 	scope="request"/>
<c:set var="sidemenu_active" 		value="Statistics" 				scope="request"/>
<c:set var="sidemenu_sub_active"	value="statistic.comparison" 	scope="request"/>
<c:set var="agnHighlightKey" 		value="statistic.comparison" 	scope="request"/>
<c:set var="agnHelpKey" 			value="compareMailings" 		scope="request"/>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="statistic.comparison"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/statistics/mailing/comparison/list.action?restoreSort=true"/>
        </c:set>
    </emm:instantiate>
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${fn:join(mailingNames.toArray(), ', ')}"/>
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

        <%-- Items for dropdown --%>
        <emm:instantiate var="option" type="java.util.LinkedHashMap">
            <c:set target="${optionList}" property="0" value="${option}"/>
            <c:set target="${option}" property="extraAttributes" value="data-prevent-load"/>
            <c:set target="${option}" property="url">${birtExportReportUrl}</c:set>
            <c:set target="${option}" property="name">
                <mvc:message code="user.export.csv"/>
            </c:set>
        </emm:instantiate>
    </emm:instantiate>
</emm:instantiate>
