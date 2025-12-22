<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="hidden" type="java.lang.Boolean"--%>
<%--@elvariable id="isLocked" type="java.lang.Boolean"--%>

<c:set var="DISABLED" value="${isLocked or hidden}"/>

<div class="tiles-container" data-editable-view="${agnEditViewKey}">
    <c:if test="${not DISABLED and emm:permissionAllowed('targets.change', pageContext.request)}">
        <%@ include file="target-evaluate.jsp"%>
    </c:if>

    <div class="tile" style="flex: 1 1 60%" data-editable-tile="main">
        <div class="tile-body">
            <%@ include file="target-dependents.jsp"%>
        </div>
    </div>
</div>
