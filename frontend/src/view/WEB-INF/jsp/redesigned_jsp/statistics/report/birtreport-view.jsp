<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@page import="com.agnitas.emm.common.MailingType" %>

<%@ page import="com.agnitas.emm.core.birtreport.dto.BirtReportType" %>
<%@ page import="com.agnitas.emm.core.birtreport.dto.ReportSettingsType" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="birtReportForm" type="com.agnitas.emm.core.birtreport.forms.BirtReportForm"--%>
<%--@elvariable id="activeTabType" type="com.agnitas.emm.core.birtreport.dto.ReportSettingsType"--%>
<%--@elvariable id="dateFormat" type="java.text.SimpleDateFormat"--%>
<%--@elvariable id="datePickerFormatPattern" type="java.lang.String"--%>
<%--@elvariable id="reportDateFormatPattern" type="java.lang.String"--%>
<%--@elvariable id="settingsFilters" type="net.sf.json.JSONObject"--%>
<%--@elvariable id="isMailTrackingEnabled" type="java.lang.Boolean"--%>

<c:set var="MAILING_KEY" value="<%= ReportSettingsType.MAILING.getKey()%>"/>
<c:set var="COMPARISON_KEY" value="<%= ReportSettingsType.COMPARISON.getKey()%>"/>
<c:set var="RECIPIENT_KEY" value="<%= ReportSettingsType.RECIPIENT.getKey()%>"/>
<c:set var="TOP_DOMAIN_KEY" value="<%= ReportSettingsType.TOP_DOMAIN.getKey()%>"/>

<c:set var="TYPE_AFTER_MAILING_24HOURS_ID" value="<%= BirtReportType.TYPE_AFTER_MAILING_24HOURS.getKey()%>"/>
<c:set var="NORMAL_MAILING_TYPE" value="<%= MailingType.NORMAL.getCode() %>"/>

<mvc:form id="birt-report-view" cssClass="tiles-container" servletRelativeAction="/statistics/report/save.action" modelAttribute="birtReportForm"
          data-form="resource" data-controller="birt-reports" data-initializer="birt-reports" data-form-dirty-checking="" data-editable-view="${agnEditViewKey}">

    <script id="config:birt-reports" type="application/json">
      {
        "reportId" : ${birtReportForm.reportId},
        "activeTab" : ${birtReportForm.activeTab},
        "reportType" : ${birtReportForm.type},
        "constant" : {
          "MAILING_ID" : ${MAILING_KEY},
          "AFTER_MAILING_TYPE_ID" : ${TYPE_AFTER_MAILING_24HOURS_ID},
          "MAILING_SETTINGS" : ${MAILING_KEY},
          "COMPARISON_SETTINGS" : ${COMPARISON_KEY},
          "RECIPIENT_SETTINGS" : ${RECIPIENT_KEY},
          "TOP_DOMAIN_SETTINGS" : ${TOP_DOMAIN_KEY},
          "NORMAL_MAILING_TYPE" : ${NORMAL_MAILING_TYPE}
        },
        "filtered" : ${emm:toJson(settingsFilters)}
      }
    </script>

    <mvc:hidden path="reportId"/>
    <mvc:hidden path="activeTab"/>

    <div class="tiles-block flex-column">
        <div id="general-settings-tile" class="tile h-auto flex-shrink-0" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="mailing.generalSettings" /></h1>
            </div>
            <div class="tile-body js-scrollable">
                <div class="row g-3">
                    <div class="col-12">
                        <label class="form-label" for="shortname">
                            <mvc:message var="nameMsg" code="default.Name"/>
                            ${nameMsg} *
                        </label>

                        <mvc:text path="shortname" maxlength="89" size="52" id="shortname" cssClass="form-control" placeholder="${nameMsg}"/>
                    </div>

                    <div class="col-12">
                        <label class="form-label" for="description">
                            <mvc:message var="descriptionMsg" code="Description"/>
                            ${descriptionMsg}
                        </label>

                        <mvc:textarea id="description" path="description" cssClass="form-control" placeholder="${descriptionMsg}" rows="1"/>
                    </div>
                </div>
            </div>
        </div>

        <div id="delivery-settings-tile" class="tile" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="workflow.mailing.DeliverySettings" /></h1>
            </div>
            <div class="tile-body js-scrollable">
                <div class="row g-3" data-field="toggle-vis">
                    <div class="col-12">
                        <label for="emailAddresses" class="form-label"><mvc:message code="report.autosend.email"/> *</label>

                        <select name="emailAddresses" id="emailAddresses" class="form-control dynamic-tags" multiple placeholder="${emailPlaceholder}">
                            <c:forEach var="emailAddress" items="${emm:splitString(birtReportForm.emailAddresses)}">
                                <c:if test="${not empty emailAddress}">
                                    <option value="${emailAddress}" selected>${emailAddress}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="col-12">
                        <label for="emailSubject" class="form-label"><mvc:message code="mailing.Subject"/> *</label>
                        <mvc:text path="emailSubject" maxlength="99" id="emailSubject" cssClass="form-control"/>
                    </div>

                    <div class="col-12">
                        <label for="emailDescription" class="form-label">${descriptionMsg}</label>
                        <mvc:textarea id="emailDescription" path="emailDescription" cssClass="form-control" placeholder="${descriptionMsg}" rows="1" />
                    </div>

                    <div class="col-12">
                        <label for="interval" class="form-label"><mvc:message code="mailing.interval" /></label>

                        <div class="row g-1">
                            <div class="col-12">
                                <mvc:select id="interval" path="type" cssClass="form-control js-select" data-field-vis="" data-action="interval-change">
                                    <mvc:option value="0" data-field-vis-hide="#interval-weekdays-block, #interval-month-block, #interval-dispatch-block"
                                                data-field-vis-show="#interval-time-block">
                                        <mvc:message code="report.daily"/>
                                    </mvc:option>
                                    <mvc:option value="1" data-field-vis-hide="#interval-month-block, #interval-dispatch-block"
                                                data-field-vis-show="#interval-weekdays-block, #interval-time-block">
                                        <mvc:message code="report.weekly"/>
                                    </mvc:option>
                                    <mvc:option value="2" data-field-vis-hide="#interval-month-block, #interval-dispatch-block"
                                                data-field-vis-show="#interval-weekdays-block, #interval-time-block">
                                        <mvc:message code="Interval.2weekly"/>
                                    </mvc:option>
                                    <mvc:option value="3" data-field-vis-hide="#interval-weekdays-block, #interval-dispatch-block"
                                                data-field-vis-show="#interval-month-block, #interval-time-block">
                                        <mvc:message code="report.monthly"/>
                                    </mvc:option>
                                    <mvc:option value="6" data-field-vis-hide="#interval-weekdays-block, #interval-month-block, #interval-time-block"
                                                data-field-vis-show="#interval-dispatch-block">
                                        <mvc:message code="report.after.mailing"/>
                                    </mvc:option>
                                </mvc:select>
                            </div>

                            <div id="interval-month-block" class="col-12">
                                <mvc:select path="reportMonthly" cssClass="form-control js-select">
                                    <mvc:option value="3"><mvc:message code="report.monthly.first.day"/></mvc:option>
                                    <mvc:option value="4"><mvc:message code="report.monthly.15th.day"/></mvc:option>
                                    <mvc:option value="5"><mvc:message code="report.monthly.last.day"/></mvc:option>
                                </mvc:select>
                            </div>

                            <div id="interval-dispatch-block" class="col-12">
                                <mvc:select path="afterMailing" id="afterMailing" cssClass="form-control js-select" size="1">
                                    <mvc:option value="6"><mvc:message code="report.after.mailing.24hours"/></mvc:option>
                                    <mvc:option value="7"><mvc:message code="report.after.mailing.48hours"/></mvc:option>
                                    <mvc:option value="8"><mvc:message code="report.after.mailing.week"/></mvc:option>
                                </mvc:select>
                            </div>
                        </div>
                    </div>

                    <div id="interval-weekdays-block" class="col-12">
                        <label class="form-label"><mvc:message code="report.autosend.days"/></label>

                        <div class="row g-1">
                            <div class="col-12">
                                <div class="form-check form-switch">
                                    <mvc:checkbox id="interval[monday]_id" path="sendWeekDays[2]" cssClass="form-check-input" role="switch" value="true"/>
                                    <label class="form-label form-check-label fw-normal" for="interval[monday]_id"><mvc:message code="calendar.dayOfWeek.2"/></label>
                                </div>
                            </div>

                            <div class="col-12">
                                <div class="form-check form-switch">
                                    <mvc:checkbox id="interval[tuesday]_id" path="sendWeekDays[3]" cssClass="form-check-input" role="switch" value="true"/>
                                    <label class="form-label form-check-label fw-normal" for="interval[tuesday]_id"><mvc:message code="calendar.dayOfWeek.3"/></label>
                                </div>
                            </div>

                            <div class="col-12">
                                <div class="form-check form-switch">
                                    <mvc:checkbox id="interval[wednesday]_id" path="sendWeekDays[4]" cssClass="form-check-input" role="switch" value="true"/>
                                    <label class="form-label form-check-label fw-normal" for="interval[wednesday]_id"><mvc:message code="calendar.dayOfWeek.4"/></label>
                                </div>
                            </div>

                            <div class="col-12">
                                <div class="form-check form-switch">
                                    <mvc:checkbox id="interval[thursday]_id" path="sendWeekDays[5]" cssClass="form-check-input" role="switch" value="true"/>
                                    <label class="form-label form-check-label fw-normal" for="interval[thursday]_id"><mvc:message code="calendar.dayOfWeek.5"/></label>
                                </div>
                            </div>

                            <div class="col-12">
                                <div class="form-check form-switch">
                                    <mvc:checkbox id="interval[friday]_id" path="sendWeekDays[6]" cssClass="form-check-input" role="switch" value="true"/>
                                    <label class="form-label form-check-label fw-normal" for="interval[friday]_id"><mvc:message code="calendar.dayOfWeek.6"/></label>
                                </div>
                            </div>

                            <div class="col-12">
                                <div class="form-check form-switch">
                                    <mvc:checkbox id="interval[saturday]_id" path="sendWeekDays[7]" cssClass="form-check-input" role="switch" value="true"/>
                                    <label class="form-label form-check-label fw-normal" for="interval[saturday]_id"><mvc:message code="calendar.dayOfWeek.7"/></label>
                                </div>
                            </div>

                            <div class="col-12">
                                <div class="form-check form-switch">
                                    <mvc:checkbox id="interval[sunday]_id" path="sendWeekDays[1]" cssClass="form-check-input" role="switch" value="true"/>
                                    <label class="form-label form-check-label fw-normal" for="interval[sunday]_id"><mvc:message code="calendar.dayOfWeek.1"/></label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div id="interval-time-block" class="col-12 has-feedback">
                        <label class="form-label" for="sendDate"><mvc:message code="report.autosend.time"/> *</label>

                        <div class="time-picker-container">
                            <mvc:text path="sendDate.time" id="sendDate" data-value="${birtReportForm.sendDate.time}"
                                      cssClass="form-control js-timepicker"
                                      data-timepicker-options="mask: 'h:quarts'"/>

                            <div class="form-control-feedback-message">
                                <mvc:message code="default.interval"/>: <mvc:message code="default.minutes.15"/>
                            </div>
                        </div>
                    </div>

                    <div class="col-12">
                        <label for="format" class="form-label"><mvc:message code="report.data.type"/></label>
                        <mvc:select path="format" id="format" cssClass="form-control js-select" size="1">
                            <mvc:option value="0"><mvc:message code="report.data.type.pdf"/></mvc:option>
                            <mvc:option value="1"><mvc:message code="report.data.type.csv"/></mvc:option>
                        </mvc:select>
                    </div>

                    <div class="col-12">
                        <label for="endDate" class="form-label"><mvc:message code="workflow.stop.EndDate"/></label>
                        <div class="date-picker-container">
                            <mvc:text id="endDate" path="endDate.date" cssClass="form-control js-datepicker"
                                      data-datepicker-options="minDate: 0, formatSubmit: '${fn:toLowerCase(reportDateFormatPattern)}'" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="extended-settings-tile" class="tile" data-editable-tile>
        <div class="tile-header p-2">
            <nav class="navbar navbar-expand-lg">
                <a class="chosen-tab btn btn-primary w-0" href="#">
                    <span class="text text-truncate"></span>
                    <div class="form-check form-switch">
                        <input type="checkbox" class="form-check-input" ${birtReportForm.settings[activeTabType]['enabled'] eq 'true' ? 'checked' : ''}
                               role="switch" data-action="activate-delivery-for-active-tab">
                    </div>
                </a>
                <button class="navbar-toggler btn-icon" type="button" data-bs-toggle="offcanvas" data-bs-target="#report-settings-tabs" aria-controls="report-settings-tabs" aria-expanded="false">
                    <i class="icon icon-bars"></i>
                </button>
                <div class="collapse navbar-collapse offcanvas" tabindex="-1" id="report-settings-tabs">
                    <ul class="navbar-nav offcanvas-body">
                        <li class="nav-item">
                            <a class="btn btn-outline-primary" href="#" data-toggle-tab="#settings_${COMPARISON_KEY}" data-form-set="activeTab: ${COMPARISON_KEY}" data-bs-dismiss="offcanvas">
                                <span class="text-truncate"><mvc:message code="mailing.comparison"/></span>
                                <div class="form-check form-switch">
                                    <mvc:checkbox path="settings[COMPARISON][enabled]" cssClass="form-check-input" value="true" role="switch" data-action="activate-delivery" />
                                </div>
                            </a>
                        </li>

                        <li class="nav-item">
                            <a class="btn btn-outline-primary" href="#" data-toggle-tab="#settings_${MAILING_KEY}" data-form-set="activeTab: ${MAILING_KEY}" data-bs-dismiss="offcanvas">
                                <span class="text-truncate"><mvc:message code="mailing.statistics"/></span>
                                <div class="form-check form-switch">
                                    <mvc:checkbox path="settings[MAILING][enabled]" cssClass="form-check-input" value="true" role="switch" data-action="activate-delivery" />
                                </div>
                            </a>
                        </li>

                        <li class="nav-item">
                            <a class="btn btn-outline-primary" href="#" data-toggle-tab="#settings_${RECIPIENT_KEY}" data-form-set="activeTab: ${RECIPIENT_KEY}" data-bs-dismiss="offcanvas">
                                <span class="text-truncate"><mvc:message code="statistic.Recipient"/></span>
                                <div class="form-check form-switch">
                                    <mvc:checkbox path="settings[RECIPIENT][enabled]" cssClass="form-check-input" value="true" role="switch" data-action="activate-delivery" />
                                </div>
                            </a>
                        </li>

                        <c:if test="${isMailTrackingEnabled}">
                            <li class="nav-item">
                                <a class="btn btn-outline-primary" href="#" data-toggle-tab="#settings_${TOP_DOMAIN_KEY}" data-form-set="activeTab: ${TOP_DOMAIN_KEY}" data-bs-dismiss="offcanvas">
                                    <span class="text-truncate"><mvc:message code="statistic.TopDomains"/></span>
                                    <div class="form-check form-switch">
                                        <mvc:checkbox path="settings[TOP_DOMAIN][enabled]" cssClass="form-check-input" value="true" role="switch" data-action="activate-delivery" />
                                    </div>
                                </a>
                            </li>
                        </c:if>
                    </ul>
                </div>
            </nav>
        </div>
        <div id="settings_${COMPARISON_KEY}" class="tile-body js-scrollable" ${birtReportForm.activeTab eq COMPARISON_KEY and not isReportTypeAfterMailing ? 'data-tab-show="true"' : 'data-tab-hide="true"'}>
            <%@ include file="fragments/mailing-comparison-settings.jspf" %>
        </div>

        <div id="settings_${MAILING_KEY}" class="tile-body js-scrollable" ${birtReportForm.activeTab eq MAILING_KEY or isReportTypeAfterMailing? 'data-tab-show="true"' : 'data-tab-hide="true"'}>
            <%@ include file="fragments/mailing-statistics-settings.jspf" %>
        </div>

        <div id="settings_${RECIPIENT_KEY}" class="tile-body js-scrollable" ${birtReportForm.activeTab eq RECIPIENT_KEY and not isReportTypeAfterMailing ? 'data-tab-show="true"' : 'data-tab-hide="true"'}>
            <%@ include file="fragments/recipient-statistics-settings.jspf" %>
        </div>

        <c:if test="${isMailTrackingEnabled}">
            <div id="settings_${TOP_DOMAIN_KEY}" class="tile-body js-scrollable" ${birtReportForm.activeTab eq TOP_DOMAIN_KEY and not isReportTypeAfterMailing ? 'data-tab-show="true"' : 'data-tab-hide="true"'}>
                <%@ include file="fragments/top-domain-settings.jspf" %>
            </div>
        </c:if>
    </div>
</mvc:form>

<c:if test="${hasActiveDelivery}">
    <script id="report-deactivate-deliveries" type="text/x-mustache-template">
        <mvc:form cssClass="modal modal-adaptive" servletRelativeAction="/statistics/report/${birtReportForm.reportId}/deactivateAllDeliveries.action" tabindex="-1" data-form="resource">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h1 class="modal-title"><mvc:message code="report.deactivate.all"/></h1>
                        <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                            <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                        </button>
                    </div>

                    <div class="modal-body">
                        <p><mvc:message code="report.deactivate.question" /></p>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-danger js-confirm-negative" data-bs-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="default.No"/></span>
                        </button>

                        <button type="button" class="btn btn-primary js-confirm-positive" data-bs-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="default.Yes"/></span>
                        </button>
                    </div>
                </div>
            </div>
        </mvc:form>
    </script>
</c:if>

<%@ include file="../../common/modal/evaluation-loader-modal.jspf" %>
