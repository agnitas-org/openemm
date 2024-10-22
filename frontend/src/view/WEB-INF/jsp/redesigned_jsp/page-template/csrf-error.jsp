<%@ page isErrorPage="true" pageEncoding="UTF-8" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<html>

<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><mvc:message code="permission.denied.title"/></title>
    <jsp:include page="/WEB-INF/jsp/redesigned_jsp/page-template/assets.jsp"/>
</head>
<body id="csrf-error-page" class="systempage">

<div class="tile tile--msg tile--alert">
    <div class="tile-header">
        <h1>
            <i class="icon icon-state-alert"></i>
            <mvc:message code="permission.denied.title"/>
        </h1>
    </div>
    <div class="tile-body">
        <p><mvc:message code="error.csrf" /></p>
    </div>
    <div class="tile-footer tile-footer--buttons">
        <a href="#" class="btn btn-primary" onclick="window.history.back(); return false;">
            <i class="icon icon-reply"></i>
            <span class="text"> <mvc:message code="button.Back"/></span>
        </a>
    </div>
</div>

<script id="csrf-error-message" type="text/x-mustache-template">
    <div class="modal modal-alert" tabindex="-1">
        <div class="modal-dialog modal-fullscreen-lg-down">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="d-flex align-items-center justify-content-center gap-2">
                        <i class="icon icon-state-alert"></i>
                        <mvc:message code="permission.denied.title"/>
                    </h1>

                    <button type="button" class="btn-close" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>

                <div class="modal-body">
                    <p><mvc:message code="error.csrf" /></p>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-primary" data-bs-dismiss="modal" onclick="location.reload();">
                        <i class="icon icon-redo"></i>
                        <span class="text"><mvc:message code="error.reload"/></span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</script>
</body>
</html>
