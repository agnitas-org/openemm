<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="mailingShortname" type="java.lang.String"--%>

<emm:CheckLogon/>
<emm:Permission token="mailing.show"/>

<c:choose>
    <c:when test="${limitedRecipientOverview}">
        <c:set var="agnNavigationKey" value="mailingView_DisabledMailinglist" scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" value="mailingView"                     scope="request" />
    </c:otherwise>
</c:choose>
<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
    <c:set target="${agnNavHrefParams}" property="init" value="false"/>
</emm:instantiate>
<c:set var="agnTitleKey" 		 value="Mailing"  scope="request" />
<c:set var="agnSubtitleKey" 	 value="Mailing"  scope="request" />
<c:set var="sidemenu_active" 	 value="Mailings" scope="request" />
<c:set var="sidemenu_sub_active" value="none" 	  scope="request" />
<c:set var="agnHighlightKey" 	 value="Mailing"  scope="request" />

<c:set var="agnSubtitleValue" scope="request">
    <ul class="breadcrumbs">
        <li>
            <a href="<c:url value='/mailing/list.action'/>">
                <mvc:message code="default.Overview"/>
            </a>
        </li>
        <li>${mailingShortname}</li>
    </ul>
</c:set>
