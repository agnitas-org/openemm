<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.forms.MailingBaseForm"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<c:if test="${not mailingBaseForm.isTemplate}">
<emm:Permission token="mailing.delete"/>
</c:if>
<c:if test="${mailingBaseForm.isTemplate}">
<emm:Permission token="template.delete"/>
</c:if>

<logic:equal name="mailingBaseForm" property="isTemplate" value="true">
<!--  template navigation: -->
    <c:set var="agnNavigationKey"		value="templateDelete"							scope="request" />
    <c:set var="agnNavHrefAppend"		value="&mailingID=${mailingBaseForm.mailingID}"	scope="request" />
    <c:set var="agnTitleKey"			value="Template"								scope="request" />
    <c:set var="agnSubtitleKey"			value="Template"								scope="request" />
    <c:set var="agnSubtitleValue"		value="${mailingBaseForm.shortname}"			scope="request" />
    <c:set var="sidemenu_active"		value="Mailings"								scope="request" />
    <c:set var="sidemenu_sub_active"	value="none"									scope="request" />
    <c:set var="agnHighlightKey"		value="Template"								scope="request" />
</logic:equal>

<logic:equal name="mailingBaseForm" property="isTemplate" value="false">
<!-- mailing navigation: -->
    <c:set var="agnNavigationKey"			value="mailingDelete"							scope="request" />
    <c:set var="agnNavHrefAppend"			value="&mailingID=${mailingBaseForm.mailingID}"	scope="request" />
    <c:set var="agnTitleKey"				value="Mailing"									scope="request" />
    <c:set var="agnSubtitleKey"				value="Mailing"									scope="request" />
    <c:set var="agnSubtitleValue"			value="${mailingBaseForm.shortname}"			scope="request" />
    <c:set var="sidemenu_active"			value="Mailings"								scope="request" />
    <c:set var="sidemenu_sub_active"		value="none"									scope="request" />
    <c:set var="agnHighlightKey"			value="Mailing"									scope="request" />
</logic:equal>
