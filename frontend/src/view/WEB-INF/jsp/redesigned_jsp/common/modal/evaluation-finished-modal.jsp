<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="fileName" type="java.lang.String"--%>
<%--@elvariable id="tmpFileName" type="java.lang.String"--%>
<%--@elvariable id="title" type="java.lang.String"--%>
<%--@elvariable id="message" type="java.lang.String"--%>
<%--@elvariable id="success" type="java.lang.Boolean"--%>

<div id="evaluate-file-download-modal" class="modal" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title">
                    <mvc:message code="${not empty title ? title : 'GWUA.report.calculation.finished'}"/>
                </h1>
                <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body text-dark">
                <c:if test="${success}">
                    <mvc:message code="${not empty message ? message : 'GWUA.file.download.ready'}"/>
                </c:if>
                <c:if test="${not success}">
                    <mvc:message code="error.report.evaluation"/>
                    <mvc:message code="error.file.missingOrEmpty"/>
                </c:if>
            </div>
            <c:if test="${success}">
                <div class="modal-footer">
                    <c:url var="download" value="/statistics/report/download.action">
                        <c:param name="fileName" value="${fileName}"/>
                        <c:param name="tmpFileName" value="${tmpFileName}"/>
                    </c:url>
                    <a id="download-btn" href="${download}" class="btn btn-primary flex-grow-1"
                       onclick="AGN.Lib.Modal.getInstance($('#evaluate-file-download-modal')).hide()">
                        <i class="icon icon-file-download"></i>
                        <mvc:message code="button.Download"/> ${fileName}
                    </a>
                </div>
            </c:if>
        </div>
    </div>
</div>
