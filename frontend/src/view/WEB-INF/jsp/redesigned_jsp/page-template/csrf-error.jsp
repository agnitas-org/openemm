<%@ page isErrorPage="true" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

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
        <p class="w-100"><mvc:message code="error.csrf" /></p>

        <a href="#" class="btn btn-regular btn-primary w-100 flex-center gap-1" onclick="window.history.back(); return false;">
            <i class="icon icon-reply"></i>
            <span class="text"> <mvc:message code="button.Back"/></span>
        </a>
    </div>
</div>

<script id="csrf-error-message" type="text/x-mustache-template">
    <div class="modal modal-alert" tabindex="-1">
        <div class="modal-dialog modal-fullscreen-lg-down modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="d-flex align-items-center justify-content-center gap-2">
                        <i class="icon icon-state-alert"></i>
                        <mvc:message code="permission.denied.title"/>
                    </h1>

                    <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>

                <div class="modal-body pt-3">
                    <p><mvc:message code="error.csrf" /></p>

                    <div class="row mt-3">
                        <div class="col d-flex">
                            <button class="btn btn-primary flex-grow-1" data-bs-dismiss="modal" onclick="location.reload();">
                                <i class="icon icon-redo"></i>
                                <span class="text"><mvc:message code="error.reload"/></span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>
</body>
</html>
