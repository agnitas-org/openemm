<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<%--@elvariable id="contactHistoryJson" type="net.sf.json.JSONArray"--%>
<%--@elvariable id="deliveryHistoryEnabled" type="java.lang.Boolean"--%>
<%--@elvariable id="recipient" type="org.agnitas.emm.core.recipient.dto.RecipientLightDto"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>

<div class="tile js-data-table" data-sizing="container" data-table="recipient-mailing-history-overview">
    <div class="tile-header" data-sizing="top">
        <h2 class="headline">
            <mvc:message code="default.search"/>
        </h2>

        <ul class="tile-header-actions">
            <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                    <i class="icon icon-eye"></i>
                    <span class="text"><mvc:message code="button.Show"/></span>
                    <i class="icon icon-caret-down"></i>
                </a>
                <ul class="dropdown-menu">
                    <li class="dropdown-header"><mvc:message code="listSize"/></li>
                    <li>
                        <label class="label js-data-table-paginate" data-page-size="20" data-table-body=".js-data-table-body" data-web-storage="recipient-mailing-history-overview">
                            <span class="label-text">20</span>
                        </label>
                        <label class="label js-data-table-paginate" data-page-size="50" data-table-body=".js-data-table-body" data-web-storage="recipient-mailing-history-overview">
                            <span class="label-text">50</span>
                        </label>
                        <label class="label js-data-table-paginate" data-page-size="100" data-table-body=".js-data-table-body" data-web-storage="recipient-mailing-history-overview">
                            <span class="label-text">100</span>
                        </label>
                    </li>
                </ul>
            </li>
        </ul>
    </div>

    <div class="tile-content" data-sizing="scroll">
        <div class="js-data-table-body" data-web-storage="recipient-mailing-history-overview" style="height: 100%;"></div>
    </div>

    <c:set var="deliveryDateAvailable" value="false"/>
    <c:forEach var="entry" items="${contactHistoryJson}">
        <c:if test="${not empty entry['deliveryDate']}">
            <c:set var="deliveryDateAvailable" value="true"/>
        </c:if>

        <c:set target="${entry}" property="typeTitle" value=""/>
        <c:if test="${not empty entry['typeMessageKey']}">
            <mvc:message var="typeTitle" code="${entry['typeMessageKey']}"/>
            <c:set target="${entry}" property="typeTitle" value="${typeTitle}"/>
        </c:if>
    </c:forEach>

    <script id="recipient-mailing-history-overview" type="application/json">
        {
            "columns": [
                {
                    "headerName": "<mvc:message code='mailing.senddate'/>",
                    "editable": false,
                    "field": "sendDate",
                    "type": "dateColumn",
                    "cellRenderer": "DateCellRenderer",
                    "cellRendererParams": { "optionDateFormat": "${fn:replace(fn:replace(adminDateFormat, "d", "D"), "y", "Y")}" }
                },
                {
                    "headerName": "<mvc:message code='default.Type'/>",
                    "editable": false,
                    "field": "typeTitle"
                },
                {
                    "headerName": "<mvc:message code='default.Name'/>",
                    "editable": false,
                    "suppressResize": true,
                    "suppressMenu": true,
                    "suppressSorting": true,
                    "cellAction": null,
                    "suppressSizeToFit": true,
                    "field": "mailingName",
                    "cellRenderer": "RecipientDeliveryInfoRenderer",
                    "cellRendererParams": {"deliveryHistoryEnabled": ${deliveryHistoryEnabled}, "recipientId": ${recipient.customerId}}
                },
                {
                    "headerName": "<mvc:message code='mailing.Subject'/>",
                    "editable": false,
                    "field": "subject"
                },
                {
                    "headerName": "<mvc:message code='recipient.Mailings.deliverydate'/>",
                    "editable": false,
                    "field": "deliveryDate",
                    "type": "dateColumn",
                    "cellRenderer": "DateCellRenderer",
                    "cellRendererParams": { "optionDateFormat": "${fn:replace(fn:replace(adminDateFormat, "d", "D"), "y", "Y")}" },
                    "hide": ${not deliveryDateAvailable}
                },

                {
                    "headerName": "<mvc:message code='recipient.Mailing.deliveries'/>",
                    "editable": false,
                    "field": "numberOfDeliveries",
                    "cellRenderer": "RecipientSuccessDeliveryInfoRenderer",
                    "cellRendererParams": {"deliveryHistoryEnabled": true, "recipientId": ${recipient.customerId}}
                },
                {
                    "headerName": "<mvc:message code='recipient.Mailings.openings'/>",
                    "editable": false,
                    "field": "openings"
                },
                {
                    "headerName": "<mvc:message code='recipient.Mailings.clicks'/>",
                    "editable": false,
                    "field": "clicks"
                }
            ],
            "data": ${contactHistoryJson}
        }
    </script>
</div>
