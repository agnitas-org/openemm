<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="statData" type="List<com.agnitas.emm.core.birtstatistics.dto.TargetGroupEventProgressDTO>"--%>
<%--@elvariable id="selectedTargets" type="List<com.agnitas.beans.TargetLight>"--%>
<%--@elvariable id="fromDate" type="java.util.Date"--%>
<%--@elvariable id="toDate" type="java.util.Date"--%>
<%--@elvariable id="linkUrl" type="java.lang.String"--%>

<div class="modal modal-adaptive" tabindex="-1" data-controller="progress-stat">
    <div class="modal-dialog modal-dialog-full-height">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title">${linkUrl}</h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body d-flex flex-column gap-3 js-scrollable" data-initializer="link-clicks-progress-stat">
                <%-- Loads by JS --%>
            </div>
        </div>
    </div>
</div>

<script id="config:link-clicks-progress-stat" type="application/json">
    {
        "data": ${emm:toJson(statData)},
        "from": ${emm:toJson(fromDate)},
        "to": ${emm:toJson(toDate)},
        "selectedTargets": ${emm:toJson(selectedTargets)}
    }
</script>

<%@include file="fragments/progress-stat-templates.jspf"%>
