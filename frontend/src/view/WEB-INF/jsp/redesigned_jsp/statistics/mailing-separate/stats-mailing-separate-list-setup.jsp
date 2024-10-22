<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnTitleKey" 			       value="Statistics" 			           scope="request" />
<c:set var="sidemenu_active" 		       value="Statistics" 			           scope="request" />
<c:set var="sidemenu_sub_active" 	       value="MailStat" 			           scope="request" />
<c:set var="agnHighlightKey" 		       value="statistic.MailStat" 	           scope="request" />
<c:set var="agnBreadcrumbsRootKey"	       value="MailStat" 		               scope="request" />
<c:set var="agnHelpKey" 			       value="statisticMailing" 	           scope="request" />
<c:set var="agnEditViewKey" 	           value="mailing-separate-overview"       scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
    </emm:instantiate>
</emm:instantiate>
