<%@ page import="java.util.Enumeration" %>
<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"    uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mvc"   uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"   uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="sec"   uri="http://www.springframework.org/security/tags" %>

<c:set var="autoRedirectToLogin">
    <tiles:insertAttribute name="autoRedirectToLogin" />
</c:set>

<mvc:message var="title" code="logon.title"/>
<c:if test="${not empty layoutdir and layoutdir != 'assets/core'}">
    <%-- Use custom title and edition logo --%>
    <mvc:message var="title" code="logon.title.${fn:substringAfter(layoutdir, 'assets/')}" text="${title}"/>
</c:if>

<c:url var="agnitasEmmLogoSvgSrc" value="/layout/0/logo.svg"/>
<c:url var="agnitasEmmLogoPngSrc" value="/layout/0/logo.png"/>
<c:url var="loginUrl" value="/logon.action" />

<!doctype html>
<html lang="en">
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <sec:csrfMetaTags />

    <c:if test="${autoRedirectToLogin}">
        <meta http-equiv="refresh" content="3; URL='${loginUrl}'">
    </c:if>

    <title>${title}</title>
    <link rel="shortcut icon" href="<c:url value="/favicon.ico"/>">

    <tiles:insertTemplate template="/WEB-INF/jsp/redesigned_jsp/page-template/assets.jsp" />
</head>
<body class="login-page">

<div class="login-form" data-controller="login">
    <div class="login-form__header">
        <img src="${agnitasEmmLogoSvgSrc}" onerror="this.onerror=null; this.src='${agnitasEmmLogoPngSrc}'" alt="Logo">

        <div class="login-form__version">
            <h1><mvc:message code="default.EMM"/></h1>
            <h3><mvc:message code="default.version.redesigned"/></h3>
        </div>
    </div>

    <div class="login-form__body">
        <c:set var="formContent">
            <tiles:insertAttribute name="formContent" />
        </c:set>

        <c:if test="${not empty formContent}">
            <div class="login-form__content">
                <c:if test="${backToLogin}">
                    <div class="login-form__nav">
                        <c:if test="${backToLogin}">
                            <a href="${loginUrl}" class="d-flex gap-1 align-items-center">
                                <i class="icon icon-caret-left"></i>
                                <mvc:message code="logon.back" />
                            </a>
                        </c:if>
                    </div>
                </c:if>

                ${formContent}
            </div>
        </c:if>

        <div id="popups" data-popups-options="useTabs: false, collapse: false, removeEmptyContainer: false">
            <tiles:insertTemplate template="/WEB-INF/jsp/messages.jsp" />
        </div>
    </div>

    <p class="text-center"><mvc:message code="default.Copyright"/></p>
</div>

<c:set var="extraContent">
    <tiles:insertAttribute name="extra-body" />
</c:set>

<c:if test="${not empty extraContent}">
    <div class="login-page__extra-content">
        ${extraContent}
    </div>
</c:if>

<c:if test="${not empty iframeUrl}">
    <iframe src="${iframeUrl}" class="js-fixed-iframe w-100 h-100" border="0" frameborder="0" scrolling="auto">
        Your Browser does not support IFRAMEs, please update!
    </iframe>
</c:if>

</body>
</html>
