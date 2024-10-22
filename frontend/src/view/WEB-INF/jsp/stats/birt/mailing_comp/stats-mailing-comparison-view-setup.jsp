<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.action" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="agnNavigationKey" 		value="statsCompare"            scope="request" />
<c:set var="isTabsMenuShown" 		value="false" 			        scope="request" />
<c:set var="agnTitleKey" 			value="statistic.comparison" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="Statistics" 				scope="request" />
<c:set var="sidemenu_active" 		value="Statistics" 				scope="request" />
<c:set var="sidemenu_sub_active"	value="statistic.comparison" 	scope="request" />
<c:set var="agnHighlightKey" 		value="statistic.comparison" 	scope="request" />
<c:set var="agnHelpKey" 			value="compareMailings" 		scope="request" />

<c:set var="agnSubtitleValue" scope="request">
    <ul class="breadcrumbs">
        <li>
            <c:url var="link" value="/statistics/mailing/comparison/list.action?restoreSort=true"/>
            <a href="${link}"> <mvc:message code="default.Overview"/></a>
        </li>
        <li>
            <%--@elvariable id="mailingNames" type="java.util.List<java.lang.String>"--%>
            ${fn:join(mailingNames.toArray(), ', ')}
        </li>
    </ul>
</c:set>
