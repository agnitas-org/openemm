<%@ page contentType="text/html; charset=utf-8" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<form class="row g-3" method="post" action="<c:url value="/oauth/logon.action"/>">
    <sec:csrfInput/>

    <div class="col-12">
        <label for="username" class="form-label">
            <i class="icon icon-user"></i>
            <mvc:message code="logon.username"/>
        </label>
        <input type="text" id="username" name="username" class="form-control" autocomplete="username" autofocus>
    </div>

    <div class="col-12">
        <label for="password" class="form-label">
            <i class="icon icon-key"></i>
            <mvc:message code="logon.password"/>
        </label>
        <input type="password" id="password" name="password" class="form-control" autocomplete="current-password">
    </div>

    <div class="col-12">
        <button type="submit" class="btn btn-light btn-lg w-100">
            <span class="text"><mvc:message code="logon.login"/></span>
            <i class="icon icon-caret-right"></i>
        </button>
    </div>
</form>
