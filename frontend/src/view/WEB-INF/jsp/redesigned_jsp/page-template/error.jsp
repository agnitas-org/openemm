<%@ page isErrorPage="true" pageEncoding="UTF-8" %>

<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<html>

<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <sec:csrfMetaTags />

    <title><mvc:message code="error.global.title"/></title>
    <jsp:include page="/WEB-INF/jsp/redesigned_jsp/page-template/assets.jsp"/>
</head>
<body id="error-page" class="systempage">

    <c:url var="errorSvgSrc" value="/assets/core/images/facelift/errors_error-500.svg"/>
    <c:url var="errorPngSrc" value="/assets/core/images/facelift/errors_error-500.png"/>

    <div class="tile tile--msg tile--alert">
        <div class="tile-header">
            <img alt="" src="${errorSvgSrc}" onerror="this.onerror=null; this.src='${errorPngSrc}'">
            <h1>500 - <mvc:message code="error.global.title"/></h1>
        </div>
        <div class="tile-body">
            <h2><mvc:message code="error.global.headline"/></h2>
            <p class="w-100"><mvc:message code="error.global.message" /></p>
        </div>
        <div class="tile-footer tile-footer--buttons">
            <a href="#" class="btn btn-primary" onclick="window.history.back(); return false;">
                <i class="icon icon-reply"></i>
                <span class="text"> <mvc:message code="button.Back"/></span>
            </a>
        </div>
    </div>
</body>
</html>
