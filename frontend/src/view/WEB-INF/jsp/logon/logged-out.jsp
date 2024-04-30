<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="supportMailAddress" type="java.lang.String"--%>
<%--@elvariable id="layoutdir" type="java.lang.String"--%>

<s:message var="title" code="logon.title"/>

<c:url var="editionLogoSrc" value="/layout/0/edition_logo.png"/>
<c:url var="agnitasEmmLogoSvgSrc" value="/layout/0/logo.svg"/>
<c:url var="agnitasEmmLogoPngSrc" value="/layout/0/logo.png"/>
<c:url var="logonPageLink" value="/logon.action"/>

<c:if test="${not empty layoutdir and layoutdir != 'assets/core'}">
    <%-- Use custom title and edition logo --%>
    <s:message var="title" code="logon.title.${fn:substringAfter(layoutdir, 'assets/')}" text="${title}"/>
</c:if>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="refresh" content="3; URL='${logonPageLink}'">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title>${title}</title>

        <link rel="shortcut icon" href="<c:url value="/favicon.ico"/>">

        <tiles:insert page="/WEB-INF/jsp/assets.jsp"/>
    </head>
    <body class="systempage">
        <div class="system-tile" role="main">
            <div class="system-tile-header">
                <div class="logo">
                    <img class="logo-image" src="${agnitasEmmLogoSvgSrc}" onerror="this.onerror=null; this.src='${agnitasEmmLogoPngSrc}'" alt="Logo">

                    <p class="headline">
                        <mvc:message code="default.EMM"/>
                    </p>
                    <p class="version">
                        <mvc:message code="default.version"/>
                    </p>
                </div>
                <div class="edition-logo">
                    <img class="logo-image" src="${editionLogoSrc}" alt="Edition Logo">
                </div>
            </div>
            <div class="system-tile-content">
                <div class="form-group">
                    <mvc:message code="logout.successful"/>
                </div>
                <div class="form-group">
                    <a href="${logonPageLink}">
                        <mvc:message code="logout.relogin"/>
                    </a>
                </div>
                <div class="form-group">
                    <mvc:message code="logon.security" arguments="${supportMailAddress}"/>
                </div>
            </div>
        </div>

        <div id="notifications-container"></div>

        <%@include file="/WEB-INF/jsp/additional.jsp"%>
    </body>
</html>
