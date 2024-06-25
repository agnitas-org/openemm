<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="contactHistoryJson" type="net.sf.json.JSONArray"--%>
<%--@elvariable id="deliveryHistoryEnabled" type="java.lang.Boolean"--%>
<%--@elvariable id="recipient" type="org.agnitas.emm.core.recipient.dto.RecipientLightDto"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>

<mvc:message var="minMsg" code="default.min"/>
<mvc:message var="maxMsg" code="default.max"/>
<mvc:message var="senddateMsg" code='mailing.senddate'/>
<mvc:message var="typeMsg" code='default.Type'/>
<mvc:message var="nameMsg" code='default.Name'/>
<mvc:message var="subjectMsg" code='mailing.Subject'/>
<mvc:message var="deliveryDateMsg" code='DeliveryDate'/>
<mvc:message var="deiveriesMsg" code='recipient.Mailing.deliveries'/>
<mvc:message var="openingsMsg" code='statistic.openings'/>
<mvc:message var="clicksMsg" code='statistic.Clicks'/>

<div class="filter-overview hidden" data-editable-view="${agnEditViewKey}">
    <div id="table-tile" class="tile js-data-table" data-table="recipient-mailing-history-overview" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.search"/></h1>
        </div>
        <div class="tile-body d-flex flex-column gap-3 js-scrollable">
            <div class="notification-simple notification-simple--info notification-simple--lg">
                <span id="remaining-generations-text"><mvc:message code="info.recipient.data.retention" arguments="${expireSuccess},${expireRecipient}"/></span>
            </div>
            <div class="js-data-table-body" data-web-storage="recipient-mailing-history-overview"></div>
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
                        "headerName": "${senddateMsg}",
                        "editable": false,
                        "field": "sendDate",
                        "type": "dateColumn"
                    },
                    {
                        "headerName": "${typeMsg}",
                        "editable": false,
                        "field": "typeTitle"
                    },
                    {
                        "headerName": "${nameMsg}",
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
                        "headerName": "${subjectMsg}",
                        "editable": false,
                        "field": "subject"
                    },
                    {
                        "headerName": "${deliveryDateMsg}",
                        "editable": false,
                        "field": "deliveryDate",
                        "type": "customColumn",
                        "filter": "DeliveryDateFilter",
                        "comparator": "recipient-mailings-delivery-date",
                        "cellRenderer": "MustacheTemplateCellRender",
                        "cellRendererParams": {"templateName": "delivery-date-cell"}
                    },
                    {
                        "headerName": "${deiveriesMsg}",
                        "editable": false,
                        "type": "numberRangeColumn",
                        "field": "numberOfDeliveries",
                        "cellRenderer": "RecipientSuccessDeliveryInfoRenderer",
                        "cellRendererParams": {"deliveryHistoryEnabled": true, "recipientId": ${recipient.customerId}}
                    },
                    {
                        "headerName": "${openingsMsg}",
                        "type": "numberRangeColumn",
                        "editable": false,
                        "field": "openings"
                    },
                    {
                        "headerName": "${clicksMsg}",
                        "type": "numberRangeColumn",
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
    <div id="filter-tile" class="tile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up desktop-hidden"></i><mvc:message code="report.mailing.filter"/>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-icon-sm btn-inverse" id="reset-filter" data-form-clear="#filter-tile" data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-sync"></i></a>
                <a class="btn btn-icon btn-icon-sm btn-primary" id="apply-filter" data-tooltip="<mvc:message code="button.filter.apply"/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
                    <label class="form-label" for="sendDate-from-filter">${senddateMsg}</label>
                    <div class="inline-input-range" data-date-range>
                        <div class="date-picker-container">
                            <input type="text" id="sendDate-from-filter" placeholder="<mvc:message code='From'/>" class="form-control js-datepicker" />
                        </div>
                        <div class="date-picker-container">
                            <input type="text" id="sendDate-to-filter" placeholder="<mvc:message code='To'/>" class="form-control js-datepicker" />
                        </div>
                    </div>
                </div>
                <div class="col-12">
                    <label class="form-label" for="typeTitle-filter">${typeMsg}</label>
                    <input type="text" id="typeTitle-filter" class="form-control" placeholder="${typeMsg}"/>
                </div>
                <div class="col-12">
                    <label class="form-label" for="mailingName-filter">${nameMsg}</label>
                    <input type="text" id="mailingName-filter" class="form-control" placeholder="${nameMsg}"/>
                </div>
                <div class="col-12">
                    <label class="form-label" for="subject-filter">${subjectMsg}</label>
                    <input type="text" id="subject-filter" class="form-control" placeholder="${nameMsg}"/>
                </div>
                <div class="col-12">
                    <label class="form-label" for="deliveryDate-filter">${deliveryDateMsg}</label>
                    <select id="deliveryDate-filter" multiple class="form-control">
                        <option value="date"><mvc:message code="Date"/></option>
                        <option value="soft-bounce"><mvc:message code="bounces.softbounce"/></option>
                        <option value="hard-bounce"><mvc:message code="statistic.bounces.hardbounce"/></option>
                        <option value="no-feedback"><mvc:message code="recipient.history.mailing.feedback.no"/></option>
                    </select>
                    <div class="mt-1" data-date-range data-show-by-select="#deliveryDate-filter" data-show-by-select-values="date">
                        <div class="date-picker-container mb-1">
                            <input type="text" id="deliveryDate-from-filter" placeholder="<mvc:message code='From'/>" class="form-control js-datepicker"/>
                        </div>
                        <div class="date-picker-container mb-1">
                            <input type="text" id="deliveryDate-to-filter" placeholder="<mvc:message code='To'/>" class="form-control js-datepicker"/>
                        </div>
                    </div>
                </div>
                <div class="col-12">
                    <label class="form-label" for="numberOfDeliveries-min-filter">${deiveriesMsg}</label>
                    <div class="inline-input-range">
                        <input type="number" id="numberOfDeliveries-min-filter" class="form-control" placeholder="${minMsg}" min="1" step="1" pattern="\d+"/>
                        <input type="number" id="numberOfDeliveries-max-filter" class="form-control" placeholder="${maxMsg}" min="1" step="1" pattern="\d+"/>
                    </div>
                </div>
                <div class="col-12">
                    <label class="form-label" for="openings-min-filter">${openingsMsg}</label>
                    <div class="inline-input-range">
                        <input type="number" id="openings-min-filter" class="form-control" placeholder="${minMsg}" min="1" step="1" pattern="\d+"/>
                        <input type="number" id="openings-max-filter" class="form-control" placeholder="${maxMsg}" min="1" step="1" pattern="\d+"/>
                    </div>
                </div>
                <div class="col-12">
                    <label class="form-label" for="clicks-min-filter">${clicksMsg}</label>
                    <div class="inline-input-range">
                        <input type="number" id="clicks-min-filter" class="form-control" placeholder="${minMsg}" min="1" step="1" pattern="\d+"/>
                        <input type="number" id="clicks-max-filter" class="form-control" placeholder="${maxMsg}" min="1" step="1" pattern="\d+"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script id="delivery-date-cell" type="text/x-mustache-template">
    {{ if (value === 'soft-bounce') { }}
        <mvc:message code="bounces.softbounce"/>
    {{ } else if (value === 'hard-bounce') { }}
        <mvc:message code="statistic.bounces.hardbounce"/>
    {{ } else if (!value) { }}
        <mvc:message code="recipient.history.mailing.feedback.no"/>
    {{ } else { }}
        {{- AGN.Lib.DateFormat.formatAdminDate(value) }}
    {{ } }}
</script>

<script id="clicks-cell" type="text/x-mustache-template">
    {{ var clicksHistoryUrl = AGN.url('/recipient/' + ${recipient.customerId} + '/mailing/' + entry.mailingId + '/clicksHistory.action') }}
    <a href="{{- clicksHistoryUrl  }}" class="btn btn-pill" data-confirm><i class="icon icon-external-link-alt"></i><span>{{- value }}</span></a>
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
