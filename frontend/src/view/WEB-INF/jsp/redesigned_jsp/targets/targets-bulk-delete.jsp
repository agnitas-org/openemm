<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/errorRedesigned.action" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="form" type="org.agnitas.web.forms.BulkActionForm"--%>
<%--@elvariable id="names" type="java.util.List<java.lang.String>"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-lg modal-dialog-centered">
        <mvc:form cssClass="modal-content" servletRelativeAction="/target/bulk/delete.action" modelAttribute="form">
            <c:forEach var="targetId" items="${form.bulkIds}">
                <input type="hidden" name="bulkIds" value="${targetId}" />
            </c:forEach>

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="bulkAction.delete.target"/></h1>
                <button type="button" class="btn-close shadow-none js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <mvc:message code="bulkAction.delete.target.question"/>

                <%-- rows + header + table-controls--%>
                <c:set var="tableHeight" value="${(names.size() + 2) * 40}"/>
                <div class="js-data-table mt-3" data-table="delete-items" style="height: ${tableHeight}px; max-height: 500px">
                    <div class="js-data-table-body h-100"></div>

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
                                    "resizable": false
                                }
                            ],
                            "options": {
                                "pagination": false,
                                "singleNameTable": true,
                                "showRecordsCount": "simple"
                            },
                            "data": ${emm:toJson(names)}
                        }
                    </script>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-danger js-confirm-positive flex-grow-1">
                    <i class="icon icon-trash-alt"></i>
                    <span><mvc:message code="button.Delete"/></span>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
