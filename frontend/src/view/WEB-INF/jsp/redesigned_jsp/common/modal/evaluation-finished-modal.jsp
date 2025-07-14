<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="downloadUrl" type="java.lang.String"--%>
<%--@elvariable id="fileName" type="java.lang.String"--%>
<%--@elvariable id="tmpFileName" type="java.lang.String"--%>
<%--@elvariable id="title" type="java.lang.String"--%>
<%--@elvariable id="message" type="java.lang.String"--%>
<%--@elvariable id="success" type="java.lang.Boolean"--%>

<div id="evaluate-file-download-modal" class="modal" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title">
                    <mvc:message code="${not empty titleKey ? titleKey : 'report.calculation.finished'}"/>
                </h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body text-secondary">
                <c:choose>
                    <c:when test="${success}">
                        <c:if test="${not empty message}">
                            ${message}
                        </c:if>
                        <mvc:message code="report.file.download" />
                    </c:when>
                    <c:otherwise>
                        <mvc:message code="error.report.evaluation"/>
                        <mvc:message code="error.file.missingOrEmpty"/>
                    </c:otherwise>
                </c:choose>
            </div>
            <c:if test="${success}">
                <div class="modal-footer">
                    <c:url var="downloadUrl" value="${downloadUrl}">
                        <c:param name="fileName" value="${fileName}"/>
                        <c:param name="tmpFileName" value="${tmpFileName}"/>
                    </c:url>

                    <a href="${downloadUrl}" id="download-btn" class="btn btn-primary"
                       onclick="AGN.Lib.Modal.getInstance($('#evaluate-file-download-modal')).hide()" data-prevent-load>
                        <i class="icon icon-file-download"></i>
                        <mvc:message code="button.Download"/> ${fileName}
                    </a>
                </div>
            </c:if>
        </div>
    </div>
</div>
