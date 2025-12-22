<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="mailingShortname" type="java.lang.String"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="deliveryHistoryJson" type="org.json.JSONArray"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="recipient.history.mailing.delivery" arguments="${mailingShortname}"/></h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body">
                <div class="table-wrapper" data-js-table="delivery-info" style="height: 30vh">
                    <div class="table-wrapper__header justify-content-end">
                        <div class="table-wrapper__controls">
                            <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                            <jsp:include page="../../common/table/entries-label.jsp" />
                        </div>
                    </div>
                </div>
                <script id="delivery-info" type="application/json">
                    {
                        "columns": [
                            {
                                "headerName": "<mvc:message code='recipient.Timestamp'/>",
                                "editable": false,
                                "suppressMenu": true,
                                "suppressSizeToFit": true,
                                "field": "timestamp",
                                "type": "dateColumn"
                            },
                            {
                                "headerName": "<mvc:message code='Mailing'/>",
                                "editable": false,
                                "suppressMenu": true,
                                "field": "mailing",
                                "textInPopoverIfTruncated": true,
                                "cellRenderer": "NotEscapedStringCellRenderer"
                            }
                        ],
                        "data": ${deliveryHistoryJson}
                    }
                </script>
            </div>
        </div>
    </div>
</div>
