<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="reportEntries" type="java.util.List<com.agnitas.util.ImportReportEntry>"--%>
<%--@elvariable id="assignedMailinglists" type="java.util.List<com.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="mailinglistAssignStats" type="java.util.Map<com.agnitas.emm.core.mediatypes.common.MediaTypes, java.util.Map<java.lang.Integer, java.lang.Integer>>"--%>
<%--@elvariable id="mailinglistMessage" type="java.lang.String"--%>
<%--@elvariable id="fixedRecipientsFile" type="java.lang.String"--%>
<%--@elvariable id="duplicateRecipientsFile" type="java.lang.String"--%>
<%--@elvariable id="invalidRecipientsFile" type="java.lang.String"--%>
<%--@elvariable id="validRecipientsFile" type="java.lang.String"--%>
<%--@elvariable id="resultFile" type="java.lang.String"--%>

<c:set var="isSuccess" value="${true}" />
<c:forEach var="reportEntry" items="${reportEntries}">
	<c:if test="${reportEntry.value.matches('[0-9]+')}">
	    <c:if test="${reportEntry.value gt 0 && fn:contains(reportEntry.key, 'error')}">
            <c:set var="isSuccess" value="${false}" />
	    </c:if>
	</c:if>
</c:forEach>

<div class="tiles-container">
    <div class="tile">
        <div class="tile-header">
            <h1 class="tile-title text-truncate">
                <mvc:message code="import.standard"/>
            </h1>
            <div class="tile-title-controls">
                <span class="status-badge ${isSuccess ? 'status.success' : 'status.warning'}"></span>
            </div>
        </div>
        <div class="tile-body vstack gap-3 js-scrollable">
            <c:forEach var="reportEntry" items="${reportWarnings}">
                <div class="notification-simple notification-simple--lg notification-simple--info">
                    <span>${reportEntry}</span>
                </div>
            </c:forEach>

            <div class="input-groups">
                <c:forEach var="reportEntry" items="${reportEntries}">
                    <c:if test="${reportEntry.value.matches('.*[A-Za-z]+.*')}">
                        <div class="input-group">
                            <c:set var="isErrorEntry" value="${fn:contains(reportEntry.key, 'error') or fn:contains(reportEntry.key, 'fatal')}" />
                            <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="${reportEntry.key}"/></span>
                            <input type="text" value="${reportEntry.value}" class="form-control ${isErrorEntry ? 'text-danger' : ''}" readonly>
                        </div>
                    </c:if>

                    <c:if test="${reportEntry.value.matches('[0-9]+')}">
                        <div class="input-group">
                            <c:set var="isSuccessEntry" value="${reportEntry.value eq 0 && fn:contains(reportEntry.key, 'error')}" />
                            <c:set var="isWarningEntry" value="${reportEntry.value gt 0 && fn:contains(reportEntry.key, 'error')}" />

                            <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="${reportEntry.key}"/></span>
                            <input type="text" value="${reportEntry.value}" class="form-control ${isWarningEntry ? 'text-warning' : ''} ${isSuccessEntry ? 'text-success' : ''}" readonly>
                        </div>
                    </c:if>
                </c:forEach>
            </div>

            <c:if test="${assignedMailinglists ne null}">
                <c:forEach var="mailinglistAssignStatByMediaType" items="${mailinglistAssignStats}">
                    <div>
                        <label class="form-label"><mvc:message code="mediatype"/> ${mailinglistAssignStatByMediaType.key}:</label>
                        <div class="input-groups">
                            <c:forEach var="mailinglist" items="${assignedMailinglists}">
                                <div class="input-group">
                                    <span class="input-group-text input-group-text--disabled input-group-text--left-aligned"><mvc:message code="${mailinglistMessage}"/> ${mailinglist.shortname}</span>
                                    <input type="text" value="${mailinglistAssignStatByMediaType.value[mailinglist.id]}" class="form-control" readonly>
                                </div>
                            </c:forEach>
                        </div>
                    </div>
                </c:forEach>
            </c:if>
        </div>
    </div>
</div>
