<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="mailingName" type="java.lang.String"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="clicksHistoryJson" type="net.sf.json.JSONArray"--%>

<div class="modal modal-extra-wide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                    <i aria-hidden="true" class="icon icon-times-circle"></i>
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
                <h4 class="modal-title">
                    <mvc:message code="recipient.history.mailing.link" arguments="${mailingName}"/>
                </h4>
            </div>

            <div class="modal-body">
                <div class="js-data-table" data-table="delivery-info">
                    <div class="js-data-table-body" style="height: 30vh"></div>
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
                                    "type": "numberColumn"
                                },
                                {
                                    "headerName": "<mvc:message code='recipient.history.mailing.link.timestamp'/>",
                                    "field": "last_time",
                                    "type": "dateColumn",
                                    "width": 100,
                                    "cellRenderer": "DateCellRenderer",
                                    "cellRendererParams": { "optionDateFormat": "${fn:replace(fn:replace(adminDateTimeFormat, "d", "D"), "y", "Y")}" }
                                }
                            ],
                            "data": ${clicksHistoryJson}
                        }
                    </script>
                </div>
            </div>
            <div class="modal-footer">
                <div class="btn-group">
                    <button type="button" class="btn btn-primary btn-large" data-dismiss="modal">
                        <i class="icon icon-check"></i>
                        <span class="text"><mvc:message code="button.OK"/></span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
