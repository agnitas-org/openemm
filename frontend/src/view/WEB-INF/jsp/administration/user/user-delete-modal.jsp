<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="usernames" type="java.util.List<java.lang.String>"--%>

<c:set var="isBulkDeletion" value="${fn:length(usernames) gt 1}"/>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <mvc:form cssClass="modal-content" method="DELETE">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="${isBulkDeletion ? 'bulkAction.delete.admin' : 'settings.admin.delete'}"/></h1>
                <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <p><mvc:message code="${isBulkDeletion ? 'bulkAction.delete.admin.question' : 'settings.admin.delete.question'}" arguments="${usernames}" /></p>

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
                                    "field": "name",
                                    "headerName": "<mvc:message code='Name'/>",
                                    "filter": false,
                                    "suppressMovable": true,
                                    "sortable": true,
                                    "sort": "asc",
                                    "resizable": false,
                                    "cellRenderer": "NotEscapedStringCellRenderer"
                                }
                            ],
                            "options": {
                                "pagination": false,
                                "singleNameTable": true,
                                "showRecordsCount": "simple"
                            },
                            "data": ${emm:toJson(usernames)}
                        }
                    </script>
                </c:if>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-danger" data-confirm-positive data-form-set="includingActivity: true">
                    <i class="icon icon-trash-alt"></i>
                    <span><mvc:message code="settings.admin.delete.activity.remove"/></span>
                </button>
                <button type="button" class="btn btn-danger" data-confirm-positive>
                    <i class="icon icon-trash-alt"></i>
                    <span><mvc:message code="settings.admin.delete.activity.keep"/></span>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
