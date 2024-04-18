<%@page import="com.agnitas.emm.common.MailingType"%>
<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ page import="com.agnitas.emm.core.birtreport.dto.BirtReportType" %>
<%@ page import="com.agnitas.emm.core.birtreport.dto.ReportSettingsType" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="birtReportForm" type="com.agnitas.emm.core.birtreport.forms.BirtReportForm"--%>
<%--@elvariable id="dateFormat" type="java.text.SimpleDateFormat"--%>
<%--@elvariable id="datePickerFormatPattern" type="java.lang.String"--%>
<%--@elvariable id="reportDateFormatPattern" type="java.lang.String"--%>
<%--@elvariable id="settingsFilters" type="net.sf.json.JSONObject"--%>
<%--@elvariable id="workflowParameters" type="org.agnitas.web.forms.WorkflowParameters"--%>
<%--@elvariable id="isMailTrackingEnabled" type="java.lang.Boolean"--%>

<c:set var="MAILING_KEY" value="<%= ReportSettingsType.MAILING.getKey()%>"/>
<c:set var="COMPARISON_KEY" value="<%= ReportSettingsType.COMPARISON.getKey()%>"/>
<c:set var="RECIPIENT_KEY" value="<%= ReportSettingsType.RECIPIENT.getKey()%>"/>
<c:set var="TOP_DOMAIN_KEY" value="<%= ReportSettingsType.TOP_DOMAIN.getKey()%>"/>

<c:set var="TYPE_AFTER_MAILING_24HOURS_ID" value="<%= BirtReportType.TYPE_AFTER_MAILING_24HOURS.getKey()%>"/>
<c:set var="NORMAL_MAILING_TYPE" value="<%= MailingType.NORMAL.getCode() %>"/>

<div id="main-content">
<mvc:form servletRelativeAction="/statistics/report/save.action" modelAttribute="birtReportForm" data-form="resource"
      data-controller="birt-reports"
      data-initializer="birt-reports"
      data-form-dirty-checking=""
      id="birtreportForm">

    <script id="config:birt-reports" type="application/json">
      {
        "reportId" : ${birtReportForm.reportId},
        "activeTab" : ${birtReportForm.activeTab},
        "reportSettingsId" : "#reportSettings",
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
        "filtered" : ${emm:toJson(settingsFilters)},
        "urls" : {
          "FILTERED_MAILING_URL": "<c:url value="/statistics/report/getFilteredMailing.action"/>"
        }
      }

    </script>

    <mvc:hidden path="reportId"/>
    <mvc:hidden path="activeTab"/>

    <emm:workflowParameters />

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <mvc:message code="report.edit"/>
            </h2>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="shortname">
                        <mvc:message var="nameMsg" code="default.Name"/>
                        ${nameMsg}*
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="shortname" maxlength="89" size="52" id="shortname" cssClass="form-control" placeholder="${nameMsg}"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="description">
                        <mvc:message var="descriptionMsg" code="default.description"/>
                        ${descriptionMsg}
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:textarea path="description" rows="5" cols="32" id="description" cssClass="form-control" placeholder="${descriptionMsg}"/>
                </div>
            </div>
        </div>
    </div>
    <div class="tile">
        <div class="tile-header">
            <h2 class="headline"><mvc:message code="report.autosend"/></h2>
        </div>
        <div class="tile-content tile-content-forms" data-field="toggle-vis">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="emailAddresses" class="control-label"><mvc:message code="report.autosend.email"/>*</label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="emailAddresses" maxlength="199" id="emailAddresses" cssClass="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="emailSubject" class="control-label"><mvc:message code="mailing.Subject"/>*</label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="emailSubject" maxlength="99" id="emailSubject" cssClass="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="emailDescription" class="control-label"><mvc:message code="Description"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:textarea path="emailDescription" rows="5" cols="72" id="emailDescription" cssClass="form-control"/>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="mailing.interval" /></label>
                </div>
                <div class="col-sm-8">
                    <label class="radio-inline" for="type_none">
                        <mvc:radiobutton path="type" id="type_none" value="0"
                                         data-field-vis="" data-field-vis-hide="#field-interval-weekdays, #field-interval-months, #field-interval-dispatch"
                                         data-field-vis-show="#field-interval-time"/>

                        <mvc:message code="report.daily"/>
                    </label>
                    <label class="radio-inline" for="type_weekly">
                        <mvc:radiobutton path="type" id="type_weekly" value="1"
                                         data-field-vis="" data-field-vis-hide="#field-interval-months, #field-interval-dispatch"
                                         data-field-vis-show="#field-interval-weekdays, #field-interval-time"/>

                        <mvc:message code="report.weekly"/>
                    </label>
                    <label class="radio-inline" for="type_2weekly">
                        <mvc:radiobutton path="type" id="type_2weekly" value="2"
                                         data-field-vis="" data-field-vis-hide="#field-interval-months, #field-interval-dispatch"
                                         data-field-vis-show="#field-interval-weekdays, #field-interval-time"/>

                        <mvc:message code="Interval.2weekly"/>
                    </label>
                    <label class="radio-inline" for="type_monthly">
                        <mvc:radiobutton path="type" id="type_monthly" value="3"
                                         data-field-vis="" data-field-vis-hide="#field-interval-weekdays, #field-interval-dispatch"
                                         data-field-vis-show="#field-interval-months, #field-interval-time"/>

                        <mvc:message code="report.monthly"/>
                    </label>
                    <label class="radio-inline" for="type_afterMailing">
                        <mvc:radiobutton path="type" id="type_afterMailing" value="6"
                                         data-field-vis="" data-field-vis-hide="#field-interval-weekdays, #field-interval-months, #field-interval-time"
                                         data-field-vis-show="#field-interval-dispatch"/>

                        <mvc:message code="report.after.mailing"/>
                    </label>
                </div>
            </div>
            <div id="field-interval-weekdays" class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="report.autosend.days"/></label>
                </div>
                <disv class="col-sm-8">
                    <label class="checkbox-inline">
                        <mvc:checkbox path="sendWeekDays[2]" value="true"/>
                        <mvc:message code="calendar.dayOfWeek.2"/>
                    </label>
                    <label class="checkbox-inline">
                        <mvc:checkbox path="sendWeekDays[3]" value="true"/>
                        <mvc:message code="calendar.dayOfWeek.3"/>
                    </label>
                    <label class="checkbox-inline">
                        <mvc:checkbox path="sendWeekDays[4]" value="true"/>
                        <mvc:message code="calendar.dayOfWeek.4"/>
                    </label>
                    <label class="checkbox-inline">
                        <mvc:checkbox path="sendWeekDays[5]" value="true"/>
                        <mvc:message code="calendar.dayOfWeek.5"/>
                    </label>
                    <label class="checkbox-inline">
                        <mvc:checkbox path="sendWeekDays[6]" value="true"/>
                        <mvc:message code="calendar.dayOfWeek.6"/>
                    </label>
                    <label class="checkbox-inline">
                        <mvc:checkbox path="sendWeekDays[7]" value="true"/>
                        <mvc:message code="calendar.dayOfWeek.7"/>
                    </label>
                    <label class="checkbox-inline">
                        <mvc:checkbox path="sendWeekDays[1]" value="true"/>
                        <mvc:message code="calendar.dayOfWeek.1"/>
                    </label>
                </disv>
            </div>

            <div id="field-interval-months" class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="report.monthly"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="reportMonthly" cssClass="form-control js-select">
                        <mvc:option value="3"><mvc:message code="report.monthly.first.day"/></mvc:option>
                        <mvc:option value="4"><mvc:message code="report.monthly.15th.day"/></mvc:option>
                        <mvc:option value="5"><mvc:message code="report.monthly.last.day"/></mvc:option>
                    </mvc:select>
                </div>
            </div>

            <div id="field-interval-time" class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="report.autosend.time"/>*</label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <mvc:text path="sendDate.time" id="sendDate" data-value="${birtReportForm.sendDate.time}"
                                                   cssClass="form-control js-timepicker"
                                                   data-timepicker-options="mask: 'h:quarts'"/>
                        </div>
                        <div class="input-group-addon">
                            <span class="addon">
                                <i class="icon icon-clock-o"></i>
                            </span>
                        </div>
                    </div>
                    <p class="help-block"><mvc:message code="default.interval"/>: <mvc:message code="default.minutes.15"/></p>
                </div>
            </div>
            <div id="field-interval-dispatch" class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="afterMailing"><mvc:message code="report.after.mailing"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="afterMailing" id="afterMailing" cssClass="form-control js-select" size="1">
                        <mvc:option value="6"><mvc:message code="report.after.mailing.24hours"/></mvc:option>
                        <mvc:option value="7"><mvc:message code="report.after.mailing.48hours"/></mvc:option>
                        <mvc:option value="8"><mvc:message code="report.after.mailing.week"/></mvc:option>
                    </mvc:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="format" class="control-label"><mvc:message code="report.data.type"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="format" id="format" cssClass="form-control js-select" size="1">
                        <mvc:option value="0"><mvc:message code="report.data.type.pdf"/></mvc:option>
                        <mvc:option value="1"><mvc:message code="report.data.type.csv"/></mvc:option>
                    </mvc:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="endDate" class="control-label"><mvc:message code="workflow.stop.EndDate"/></label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <mvc:text path="endDate.date" id="endDate"
                                               cssClass="form-control datepicker-input js-datepicker"
                                               data-value="${birtReportForm.endDate.date}"
                                               data-datepicker-options="format: '${fn:toLowerCase(datePickerFormatPattern)}',
                                               formatSubmit: ${fn:toLowerCase(reportDateFormatPattern)}, min: true"/>
                        </div>
                        <div class="input-group-btn">
                            <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1" >
                                <i class="icon icon-calendar-o"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <c:set var="isReportTypeAfterMailing" value="${birtReportForm.type eq TYPE_AFTER_MAILING_24HOURS_ID}"/>
    <div id="reportSettings" class="tile">
        <div class="tile-header">
            <ul class="tile-header-nav">
                <li class="${isReportTypeAfterMailing ? 'disabled' : ''}">
                    <a href="#" data-toggle-tab="#settings_${COMPARISON_KEY}" data-form-set="activeTab: ${COMPARISON_KEY}"><mvc:message code="mailing.comparison"/></a>
                </li>
                <li >
                    <a href="#" data-toggle-tab="#settings_${MAILING_KEY}" data-form-set="activeTab: ${MAILING_KEY}"><mvc:message code="mailing.statistics"/></a>
                </li>
                <li class="${isReportTypeAfterMailing ? 'disabled' : ''}">
                    <a href="#" data-toggle-tab="#settings_${RECIPIENT_KEY}" data-form-set="activeTab: ${RECIPIENT_KEY}"><mvc:message code="statistic.Recipient"/></a>
                </li>
                <c:if test="${isMailTrackingEnabled}">
                    <li class="${isReportTypeAfterMailing ? 'disabled' : ''}">
                        <a href="#" data-toggle-tab="#settings_${TOP_DOMAIN_KEY}" data-form-set="activeTab: ${TOP_DOMAIN_KEY}"><mvc:message code="statistic.TopDomains"/></a>
                    </li>
                </c:if>
            </ul>
        </div>

        <div class="tile-content">
            <div id="settings_${COMPARISON_KEY}" ${birtReportForm.activeTab eq COMPARISON_KEY and not isReportTypeAfterMailing ? 'data-tab-show="true"' : 'data-tab-hide="true"'}>
                <jsp:include page="/WEB-INF/jsp/birtreport/birtreport-editor-comparison.jsp"/>
            </div>

            <div id="settings_${MAILING_KEY}" ${birtReportForm.activeTab eq MAILING_KEY or isReportTypeAfterMailing? 'data-tab-show="true"' : 'data-tab-hide="true"'}>
                <jsp:include page="/WEB-INF/jsp/birtreport/birtreport-editor-mailing.jsp"/>
            </div>

            <div id="settings_${RECIPIENT_KEY}" ${birtReportForm.activeTab eq RECIPIENT_KEY and not isReportTypeAfterMailing ? 'data-tab-show="true"' : 'data-tab-hide="true"'}>
                <jsp:include page="/WEB-INF/jsp/birtreport/birtreport-editor-recipient.jsp"/>
            </div>

            <c:if test="${isMailTrackingEnabled}">
                <div id="settings_${TOP_DOMAIN_KEY}" ${birtReportForm.activeTab eq TOP_DOMAIN_KEY and not isReportTypeAfterMailing ? 'data-tab-show="true"' : 'data-tab-hide="true"'}>
                    <jsp:include page="/WEB-INF/jsp/birtreport/birtreport-editor-top-domain.jsp"/>
                </div>
            </c:if>

        </div>
    </div>
</mvc:form>
</div>

<%@ include file="fragments/birtreport-evaluate-loader.jspf" %>
