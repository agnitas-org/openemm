<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="mailing.show"/>

<c:set var="agnNavigationKey" 		value="MailingParameter" 	scope="request" />
<c:set var="agnTitleKey" 			value="MailingParameter" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="MailingParameter" 	scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="MailingParameter" 	scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 			scope="request" />
<c:set var="agnHelpKey" 			value="mailingParameter"	scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="MailingParameter"/>
    </emm:instantiate>
</emm:instantiate>

<emm:ShowByPermission token="mailing.parameter.change">
    <c:set var="createNewItemUrl" scope="request">
        <html:rewrite page='/mailingParameter.do?action=newParameter'/>
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <bean:message key="MailingParameter.new"/>
    </c:set>
</emm:ShowByPermission>
