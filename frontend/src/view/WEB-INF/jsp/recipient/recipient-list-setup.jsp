<%@ page language="java" contentType="text/html; charset=utf-8"  buffer="32kb"  errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComRecipientAction, org.agnitas.web.RecipientAction" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="recipient.show"/>

<c:set var="ACTION_RECIPIENT_LIST" value="<%= RecipientAction.ACTION_LIST %>" scope="request" />
<c:set var="ACTION_CONFIRM_DELETE" 		value="<%= RecipientAction.ACTION_CONFIRM_DELETE %>" 			scope="request" />
<c:set var="ACTION_VIEW" 				value="<%= RecipientAction.ACTION_VIEW %>" 						scope="request" />
<c:set var="ACTION_BULK_CONFIRM_DELETE" value="<%= ComRecipientAction.ACTION_BULK_CONFIRM_DELETE %>"	scope="request" />

<c:set var="agnNavigationKey" 		value="subscriber_list" scope="request" />
<c:set var="agnTitleKey" 			value="Recipients" 		scope="request" />
<c:set var="agnSubtitleKey" 		value="Recipients" 		scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.search" 	scope="request" />
<c:set var="agnHighlightKey" 		value="default.search" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 			scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Recipients" 		scope="request" />
<c:set var="agnHelpKey" 			value="recipientList" 	scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.search"/>
    </emm:instantiate>
</emm:instantiate>

<emm:ShowByPermission token="recipient.create">
    <c:set var="createNewItemUrl" scope="request">
        <html:rewrite page='/recipient.do?action=2&trgt_clear=1'/>
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <bean:message key="recipient.NewRecipient"/>
    </c:set>
</emm:ShowByPermission>
