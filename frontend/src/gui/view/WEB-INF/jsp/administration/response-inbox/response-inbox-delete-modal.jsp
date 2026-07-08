<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="replies" type="java.util.List<com.agnitas.emm.core.response_inbox.bean.MailloopReplyEntry>"--%>
<%--@elvariable id="reply" type="com.agnitas.emm.core.response_inbox.bean.MailloopReplyEntry"--%>

<c:set var="isBulkDeletion" value="${fn:length(replies) gt 1}"/>
<c:set var="reply" value="${replies.iterator().next()}" />

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <mvc:form cssClass="modal-content" method="DELETE">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="${isBulkDeletion ? 'bulkAction.mailloop.inbox.delete' : 'mailloop.inbox.delete'}" /></h1>
                <button type="button" class="btn-close" data-confirm-negative>
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <p>
                    <c:choose>
                        <c:when test="${isBulkDeletion}">
                            <mvc:message code="bulkAction.mailloop.inbox.delete.question" />
                        </c:when>
                        <c:otherwise>
                            <mvc:message code="mailloop.inbox.delete.question" arguments="${[fn:escapeXml(reply.sender), fn:escapeXml(reply.subject)]}" />
                        </c:otherwise>
                    </c:choose>
                </p>

                <c:if test="${isBulkDeletion}">
                    <div class="table-wrapper mt-3" data-js-table="delete-items">
                        <div class="table-wrapper__header justify-content-end">
                            <div class="table-wrapper__controls">
                                <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                                <jsp:include page="../../common/table/entries-label.jsp" />
                            </div>
                        </div>
                    </div>

                    <script id="delete-items" type="application/json">
                        {
                            "columns": [
                                 {
                                    "field": "subject",
                                    "headerName": "<mvc:message code='mailing.Subject'/>",
                                    "filter": false,
                                    "suppressMovable": true,
                                    "sortable": true,
                                    "sort": "asc",
                                    "resizable": false,
                                    "cellRenderer": "NotEscapedStringCellRenderer"
                                },
                                {
                                    "field": "sender",
                                    "headerName": "<mvc:message code='mailing.SenderEmail'/>",
                                    "filter": false,
                                    "suppressMovable": true,
                                    "sortable": true,
                                    "resizable": false,
                                    "cellRenderer": "MustacheTemplateCellRender",
                                    "cellRendererParams": {"templateName": "mailloop-reply-sender"}
                                }
                            ],
                            "options": {
                                "pagination": false,
                                "showRecordsCount": "simple"
                            },
                            "data": ${emm:toJson(replies)}
                        }
                    </script>
                </c:if>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-danger" data-confirm-positive>
                    <i class="icon icon-trash-alt"></i>
                    <span class="text"><mvc:message code="button.Delete"/></span>
                </button>
            </div>
        </mvc:form>
    </div>
</div>

<script id="mailloop-reply-sender" type="text/x-mustache-template">
    <span class="text-truncate-table">{{- value }}</span>
</script>
