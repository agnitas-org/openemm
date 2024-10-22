<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="filename" type="java.lang.String"--%>
<%--@elvariable id="isFailed" type="java.lang.Boolean"--%>

<div id="ref-table-export-result-modal" class="modal ${isFailed ? 'modal-alert' : ''}" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="export.finish"/></h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body text-dark">
                <p><mvc:message code="${isFailed ? 'Error' : 'report.file.download'}"/></p>
            </div>
            <c:if test="${not isFailed}">
                <div class="modal-footer">
                    <c:url var="downloadCsvLink" value="/administration/table/downloadExportResult.action">
                        <c:param name="filename" value="${filename}"/>
                    </c:url>

                    <a href="${downloadCsvLink}" id="download-btn" class="btn btn-primary overflow-hidden"
                       onclick="AGN.Lib.Modal.getInstance($('#ref-table-export-result-modal')).hide()">
                        <i class="icon icon-file-download"></i>
                        <span class="text text-truncate"><mvc:message code="button.Download"/> ${filename}</span>
                    </a>
                </div>
            </c:if>
        </div>
    </div>
</div>
