<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="reportId" type="java.lang.Integer"--%>
<%--@elvariable id="reportContentEscaped" type="java.lang.String"--%>

<div class="modal modal-adaptive" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered modal-dialog-full-height">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title">
                    <c:choose>
                        <c:when test="${not empty datasourceId}">
                            <mvc:message code="recipient.DatasourceId"/>=${datasourceId}
                        </c:when>
                        <c:otherwise>
                            <mvc:message code="mediapool.file"/>
                        </c:otherwise>
                    </c:choose>
                </h1>
                <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body" data-initializer="recipient-report-initializer" style="width: 50vw">
                <iframe id="report-iframe" style="width: 100%;" srcdoc="${reportContentEscaped}"></iframe>
            </div>
        </div>
    </div>
</div>
