<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.forms.MailingBaseForm"  errorPage="/error.do" %>
<%@ page import="org.agnitas.web.EmmActionAction" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingBaseForm" type="com.agnitas.web.forms.ComMailingBaseForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

<emm:CheckLogon/>

<emm:Permission token="mailing.show"/>

<c:set var="ACTION_LIST" value="<%= EmmActionAction.ACTION_LIST %>" scope="page" />

<c:choose>
    <c:when test="${limitedRecipientOverview}">
        <c:set var="agnNavigationKey" 		value="mailingView_DisabledMailinglist"     scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" 		value="mailingView"                         scope="request" />
    </c:otherwise>
</c:choose>
<c:set var="agnNavHrefAppend"		value="&mailingID=${mailingBaseForm.mailingID}"	scope="request" />
<c:set var="agnTitleKey" 			value="Mailing" 								scope="request" />
<c:set var="agnSubtitleKey" 		value="Mailing" 								scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 								scope="request" />
<c:set var="sidemenu_sub_active"	value="none" 									scope="request" />
<c:set var="agnHighlightKey" 		value="Mailing" 								scope="request" />

<c:set var="agnSubtitleValue" scope="request">
    <ul class="breadcrumbs">
        <li>
            <html:link page="/mailingbase.do?action=${mailingBaseForm.previousAction}">
                <bean:message key="default.Overview"/>
            </html:link>
        </li>
        <li>
            ${mailingBaseForm.shortname}
        </li>
    </ul>
</c:set>
