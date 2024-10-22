<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<%--@elvariable id="contactHistoryJson" type="net.sf.json.JSONArray"--%>
<%--@elvariable id="deliveryHistoryEnabled" type="java.lang.Boolean"--%>
<%--@elvariable id="recipient" type="org.agnitas.emm.core.recipient.dto.RecipientLightDto"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="expireSuccess" type="java.lang.Integer"--%>
<%--@elvariable id="expireRecipient" type="java.lang.Integer"--%>

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
                        <label class="label js-data-table-paginate" data-page-size="200" data-table-body=".js-data-table-body" data-web-storage="recipient-mailing-history-overview">
                            <span class="label-text">200</span>
                        </label>
                    </li>
                </ul>
            </li>
        </ul>
    </div>
    
    <div class="form-group align-center">
        <div class="col-sm-offset-3 col-sm-6">
            <div class="notification-simple notification-info">
                <mvc:message code="info.recipient.data.retention" arguments="${expireSuccess},${expireRecipient}"/>
            </div>
        </div>
    </div>
    
    <div class="tile-content" data-sizing="scroll">
        <div class="js-data-table-body" data-web-storage="recipient-mailing-history-overview" style="height: 100%;"></div>
    </div>

    <c:forEach var="entry" items="${contactHistoryJson}">
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
                    "comparator": "recipient-mailings-delivery-date",
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "delivery-date-cell"}
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
                    "field": "clicks",
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "clicks-cell"}
                }
            ],
            "data": ${contactHistoryJson}
        }
    </script>
</div>

<script id="delivery-date-cell" type="text/x-mustache-template">
    {{ if (value === 'soft-bounce') { }}
        <mvc:message code="bounces.softbounce"/>
    {{ } else if (value === 'hard-bounce') { }}
        <mvc:message code="statistic.bounces.hardbounce"/>
    {{ } else if (!value) { }}
        <mvc:message code="recipient.history.mailing.feedback.no"/>
    {{ } else { }}
        {{- moment(value).format('${fn:replace(fn:replace(adminDateFormat, "d", "D"), "y", "Y")}') }}
    {{ } }}
</script>

<script id="clicks-cell" type="text/x-mustache-template">
    {{ var clicksHistoryUrl = AGN.url('/recipient/' + ${recipient.customerId} + '/mailing/' + entry.mailingId + '/clicksHistory.action') }}
    <a href="{{- clicksHistoryUrl  }}" class="btn btn-regular" data-confirm style="padding: 4px 6px; line-height: 13px;"><i class="icon icon-share-square-o"></i><span>{{- value }}</span></a>
</script>

<script type="text/javascript">
  function localStrValue(value) {
    if (value === 'soft-bounce') {
      return '<mvc:message code="bounces.softbounce"/>';
    }
    if (value === 'hard-bounce') {
      return '<mvc:message code="statistic.bounces.hardbounce"/>';
    }
    if (!value) {
      return '<mvc:message code="recipient.history.mailing.feedback.no"/>';
    }
    return value;
  }

  function customComparator(value1, value2) {
      const v1 = localStrValue(value1);
      const v2 = localStrValue(value2);

      if (typeof v1 === "string" && typeof v2 === "string") {
          return v1.toLowerCase().localeCompare(v2);
      }
      if (typeof v1 === "string") {
          return -1;
      }
      if (typeof v2 === "string") {
          return 1;
      }
      return v1 - v2;
  }
  AGN.Opt.Table['comparators']['recipient-mailings-delivery-date'] = customComparator;
</script>
