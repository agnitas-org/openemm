<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="supportMailAddress" type="java.lang.String"--%>

<c:url var="editionLogoSrc" value="/layout/0/edition_logo.png"/>
<c:url var="agnitasEmmLogoSvgSrc" value="/layout/0/logo.svg"/>
<c:url var="agnitasEmmLogoPngSrc" value="/layout/0/logo.png"/>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title><s:message code="logon.title"/></title>

        <link rel="shortcut icon" href="<c:url value="/favicon.ico"/>">

        <tiles:insert page="/WEB-INF/jsp/assets.jsp"/>
    </head>
    <body class="systempage">
        <div class="system-tile" role="main">
            <div class="system-tile-header">
                <div class="logo">
                    <img class="logo-image" src="${agnitasEmmLogoSvgSrc}" onerror="this.onerror=null; this.src='${agnitasEmmLogoPngSrc}'">

                    <p class="headline">
                        <s:message code="default.EMM"/>
                    </p>
                    <p class="version">
                        <s:message code="default.version"/>
                    </p>
                </div>
                <div class="edition-logo">
                    <img class="logo-image" src="${editionLogoSrc}">
                </div>
            </div>
            <div class="system-tile-content align-center">
                <div class="form-group">
                    <s:message code="AreYouSure"/>
                </div>
                <div class="form-group">
                    <mvc:form servletRelativeAction="/logout.action" method="POST">
                        <c:set var="messageDefaultLogout"><s:message code="default.Logout"/></c:set>
                        <button type="submit" class="btn btn-regular" data-tooltip="${messageDefaultLogout}">
                            <i class="icon icon-power-off"></i>
                            <span>${messageDefaultLogout}</span>
                        </button>
                    </mvc:form>
                </div>
            </div>
        </div>

        <div id="notifications-container"></div>

        <%@include file="/WEB-INF/jsp/additional.jsp"%>
    </body>
</html>
