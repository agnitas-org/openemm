<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComCompareMailingAction" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" value="<%= ComCompareMailingAction.ACTION_LIST %>"/>

<emm:CheckLogon/>

<emm:Permission token="stats.mailing"/>

<c:set var="agnNavigationKey" 		value="statsCompare" 			scope="request" />
<c:set var="agnTitleKey" 			value="statistic.comparison" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="Statistics" 				scope="request" />
<c:set var="sidemenu_active" 		value="Statistics" 				scope="request" />
<c:set var="sidemenu_sub_active"	value="statistic.comparison" 	scope="request" />
<c:set var="agnHighlightKey" 		value="statistic.comparison" 	scope="request" />
<c:set var="agnHelpKey" 			value="compareMailings" 		scope="request" />

<c:set var="agnSubtitleValue" scope="request">
    <ul class="breadcrumbs">
        <li>
            <html:link page="/mailing_compare.do?action=${ACTION_LIST}">
                <bean:message key="default.Overview"/>
            </html:link>
        </li>
        <li>
            <c:forEach items="${mailingNames}" var="mailingName" varStatus="loop">
                ${mailingName}${!loop.last ? ', ' : ''}
            </c:forEach>
        </li>
    </ul>
</c:set>
