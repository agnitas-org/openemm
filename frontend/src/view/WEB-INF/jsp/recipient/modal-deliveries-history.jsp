<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean"     prefix="bean" %>
<%@ taglib uri="http://displaytag.sf.net"               prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<div class="modal modal-extra-wide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                    <i aria-hidden="true" class="icon icon-times-circle"></i>
                    <span class="sr-only"><bean:message key="button.Cancel"/></span>
                </button>
                <h4 class="modal-title">
                    <bean:message key="recipient.history.mailing.delivery" arg0="${mailingName}"/>
                </h4>
            </div>

            <div class="modal-body">
                <div class="js-data-table" data-table="delivery-info">
                    <div class="js-data-table-body" style="height: 60vh"></div>
                    <script id="delivery-info" type="application/json">
                        {
                            "columns": [
                                {
                                    "headerName": "<bean:message key='recipient.Timestamp'/>",
                                    "editable": false,
                                    "suppressMenu": true,
                                    "width": 60,
                                    "field": "timestamp",
                                    "type": "dateColumn",
                                    "cellRenderer": "DateCellRenderer",
                                    "cellRendererParams": { "optionDateFormat": "${fn:replace(fn:replace(dateTimeFormatPattern, "d", "D"), "y", "Y")}" }
                                },
                                {
                                    "headerName": "<bean:message key='recipient.delivery.mailer'/>",
                                    "editable": false,
                                    "suppressMenu": true,
                                    "width": 100,
                                    "field": "mailer",
                                    "textInPopoverIfTruncated": true
                                },
                                {
                                    "headerName": "<bean:message key='recipient.delivery.relay'/>",
                                    "editable": false,
                                    "suppressMenu": true,
                                    "width": 100,
                                    "field": "relay",
                                    "textInPopoverIfTruncated": true
                                },
                                {
                                    "headerName": "<bean:message key='recipient.delivery.dsn'/>",
                                    "editable": false,
                                    "suppressMenu": true,
                                    "width": 30,
                                    "field": "dsn"
                                },
                                {
                                    "headerName": "<bean:message key='default.status'/>",
                                    "editable": false,
                                    "suppressMenu": true,
                                    "field": "status",
                                    "textInPopoverIfTruncated": true
                                }
                            ],
                            "data": ${deliveriesInfo}
                        }
                    </script>
                </div>
            </div>

            <div class="modal-footer">
                <div class="btn-group">
                    <button type="button" class="btn btn-primary btn-large" data-dismiss="modal">
                        <i class="icon icon-check"></i>
                        <span class="text"><bean:message key="button.OK"/></span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
