<%@ page language="java" import="org.agnitas.web.*" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

<emm:CheckLogon/>

<emm:Permission token="mailing.send.show"/>

<logic:equal name="mailingSendForm" property="isTemplate" value="true">
<!--  template navigation: -->
    <c:set var="agnNavigationKey"		value="templateView"										scope="request" />
    <c:set var="agnNavHrefAppend"		value="&mailingID=${mailingSendForm.mailingID}&init=true"	scope="request" />
    <c:set var="agnTitleKey"			value="Template"											scope="request" />
    <c:set var="agnSubtitleKey"			value="Template"											scope="request" />
    <c:set var="agnSubtitleValue"		value="${mailingSendForm.shortname}"						scope="request" />
    <c:set var="sidemenu_active"		value="Mailings"											scope="request" />
    <c:set var="sidemenu_sub_active"	value="Templates"											scope="request" />
    <c:set var="agnHighlightKey"		value="Send_Mailing"										scope="request" />
    <c:set var="agnHelpKey" 			value="sendMailing" 										scope="request" />
</logic:equal>

<logic:equal name="mailingSendForm" property="isTemplate" value="false">
<!-- mailing navigation: -->
    <c:choose>
        <c:when test="${limitedRecipientOverview}">
            <c:set var="agnNavigationKey" 		value="mailingView_DisabledMailinglist"     scope="request" />
        </c:when>
        <c:otherwise>
            <c:set var="agnNavigationKey" 		value="mailingView"                         scope="request" />
        </c:otherwise>
    </c:choose>
    <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
        <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingSendForm.mailingID}"/>
        <c:set target="${agnNavHrefParams}" property="init" value="true"/>
    </emm:instantiate>
    <c:set var="agnNavigationKey"			value="mailingView"											scope="request" />
    <c:set var="agnTitleKey"				value="Mailing"												scope="request" />
    <c:set var="agnSubtitleKey"				value="Mailing"												scope="request" />
    <c:set var="agnSubtitleValue"			value="${mailingSendForm.shortname}"						scope="request" />
    <c:set var="sidemenu_active"			value="Mailings"											scope="request" />
    <c:set var="sidemenu_sub_active"		value="none"												scope="request" />
    <c:set var="agnHighlightKey"			value="Send_Mailing"										scope="request" />
	<c:set var="agnHelpKey" 				value="sendMailing" 										scope="request" />
</logic:equal>
