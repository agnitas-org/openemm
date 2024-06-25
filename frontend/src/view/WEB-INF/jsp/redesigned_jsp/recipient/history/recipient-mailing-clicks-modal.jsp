<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="mailingName" type="java.lang.String"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="clicksHistoryJson" type="net.sf.json.JSONArray"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-xl modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="recipient.history.mailing.link" arguments="${mailingName}"/></h1>
                <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body">
                <div class="js-data-table" data-table="delivery-info">
                    <div class="js-data-table-body" style="height: 60vh"></div>
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
                                    "suppressSizeToFit": true
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
</div>
