<%@ page isErrorPage="true" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<html>

<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><mvc:message code="permission.denied.title"/></title>
    <jsp:include page="/WEB-INF/jsp/assets.jsp"/>
</head>
<body id="csrf-error-page" class="systempage">

<div class="msg-tile msg-tile-error">
    <div class="msg-tile-header" style="height: auto; padding-block: 10px;">
        <h1>
            <i class="icon-fa5 icon-fa5-ban"></i>
            <mvc:message code="permission.denied.title"/>
        </h1>
    </div>
    <div class="msg-tile-content">
        <h3><mvc:message code="error.csrf"/></h3>
    </div>
</div>

</body>
</html>
