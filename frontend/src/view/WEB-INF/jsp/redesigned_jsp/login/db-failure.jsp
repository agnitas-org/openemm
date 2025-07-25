<%@ page isErrorPage="true" pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
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
<div class="tile tile--msg tile--alert">
    <div class="tile-header">
        <svg><use href="<c:url value="/assets/core/images/facelift/sprite.svg"/>#errors_error-500"></use></svg>
        <h1>500 - DataBase Failure</h1>
    </div>
    <div class="tile-body">
        <h2><mvc:message code="error.global.headline"/></h2>
        <div class="w-100">
            <p><mvc:message code="error.global.message" arguments="${supportEmergencyUrl}"/></p>
        </div>

        <emm:messages var="msg" type="error">
            <p class="w-100 text-danger">${msg}</p>
        </emm:messages>
    </div>
</div>
</body>
</html>
