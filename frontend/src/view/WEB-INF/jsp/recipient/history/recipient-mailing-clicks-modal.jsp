<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="mailingName" type="java.lang.String"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="clicksHistoryJson" type="org.json.JSONArray"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="recipient.history.mailing.link" arguments="${mailingName}"/></h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body">
                <div class="table-wrapper" data-js-table="delivery-info" style="height: 60vh">
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
                                "headerName": "<mvc:message code='htmled.link'/>",
                                "field": "full_url",
                                "textInPopoverIfTruncated": true
                            },
                            {
                                "headerName": "<mvc:message code='Numbers'/>",
                                "field": "count",
                                "type": "numberColumn",
                                "suppressSizeToFit": true,
                                "cellRenderer": "NotEscapedStringCellRenderer"
                            },
                            {
                                "headerName": "<mvc:message code='recipient.history.mailing.link.timestamp'/>",
                                "field": "last_time",
                                "type": "dateTimeColumn",
                                "suppressSizeToFit": true
                            }
                        ],
                        "data": ${clicksHistoryJson}
                    }
                </script>
            </div>
        </div>
    </div>
</div>
