<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="deleteItemKey" type="java.lang.String"--%>
<%--@elvariable id="items" type="java.lang.Object"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <mvc:form servletRelativeAction="/mailinglist/bulkDelete.action" cssClass="modal-content" modelAttribute="bulkActionForm">
            <c:forEach var="mailingListId" items="${bulkDeleteForm.bulkIds}">
                <input type="hidden" name="bulkIds" value="${mailingListId}"/>
            </c:forEach>

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="bulkAction.delete.mailinglist"/></h1>
                <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <p><mvc:message code="bulkAction.delete.mailinglist.question"/></p>

                <div class="table-wrapper mt-3" data-js-table="delete-items">
                    <div class="table-wrapper__header justify-content-end">
                        <div class="table-wrapper__controls">
                            <%@include file="../common/table/toggle-truncation-btn.jspf" %>
                            <jsp:include page="../common/table/entries-label.jsp" />
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
                        "data": ${emm:toJson(items)}
                    }
                </script>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-danger js-confirm-positive">
                    <i class="icon icon-trash-alt"></i>
                    <span class="text"><mvc:message code="button.Delete"/></span>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
