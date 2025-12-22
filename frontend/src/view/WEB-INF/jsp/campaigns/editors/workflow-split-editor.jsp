<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div id="split-editor" data-initializer="split-editor-initializer">
    <mvc:form action="" id="splitNodeForm" name="splitNodeForm">
        <div class="w-100">
            <label for="split-type" class="form-label"><mvc:message code="mailing.listsplit"/></label>
            <select id="split-type" class="form-control" name="splitType">
                <option value="">--</option>
                <c:forEach var="type" items="${splitTypes}">
                    <option value="${type}"><mvc:message code="listsplit.${type}"/></option>
                </c:forEach>
        </div>
    </mvc:form>
</div>
