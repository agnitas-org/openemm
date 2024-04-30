<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="mailingShortname" type="java.lang.String"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="deliveryHistoryJson" type="net.sf.json.JSONArray"--%>

<div class="modal modal-wide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                    <i aria-hidden="true" class="icon icon-times-circle"></i>
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
                <h4 class="modal-title">
                    <mvc:message code="recipient.history.mailing.delivery" arguments="${mailingShortname}"/>
                </h4>
            </div>

            <div class="modal-body">
                <div class="js-data-table" data-table="delivery-info">
                    <div class="js-data-table-body" style="height: 30vh"></div>
                    <script id="delivery-info" type="application/json">
                        {
                            "columns": [
                                {
                                    "headerName": "<mvc:message code='recipient.Timestamp'/>",
                                    "editable": false,
                                    "suppressMenu": true,
                                    "width": 60,
                                    "field": "timestamp",
                                    "type": "dateColumn",
                                    "cellRenderer": "DateCellRenderer",
                                    "cellRendererParams": { "optionDateFormat": "${fn:replace(fn:replace(adminDateTimeFormat, "d", "D"), "y", "Y")}" }
                                },
                                {
                                    "headerName": "<mvc:message code='Mailing'/>",
                                    "editable": false,
                                    "suppressMenu": true,
                                    "width": 100,
                                    "field": "mailing",
                                    "textInPopoverIfTruncated": true
                                }
                            ],
                            "data": ${deliveryHistoryJson},
                            "options": {
                                "pagination": false,
                                "showRecordsCount": false
                            }
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
