<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.birtreport.dto.ReportSettingsType" %>
<%@ page import="com.agnitas.emm.core.birtreport.bean.impl.BirtReportDateRangedSettings" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="birtReportForm" type="com.agnitas.emm.core.birtreport.forms.BirtReportForm"--%>
<%--@elvariable id="propertiesMap" type="java.util.Map<ReportSettingsType, Map<String, List<BirtReportSettings.Properties>>>"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="targetList" type="java.util.List"--%>
<%--@elvariable id="mailinglistList" type="java.util.List"--%>
<%--@elvariable id="datePickerFormatPattern" type="java.lang.String"--%>
<%--@elvariable id="reportDateFormatPattern" type="java.lang.String"--%>


<c:set var="TOP_DOMAIN_KEY" value="<%= ReportSettingsType.TOP_DOMAIN.getKey()%>"/>
<c:set var="DATE_RANGE_PREDEFINED" value="<%=BirtReportDateRangedSettings.DATE_RANGE_PREDEFINED%>" scope="request"/>
<c:set var="DATE_RANGE_CUSTOM" value="<%=BirtReportDateRangedSettings.DATE_RANGE_CUSTOM%>" scope="request"/>

<c:set var="DATE_RANGE_PREDEFINED_WEEK" value="<%=BirtReportDateRangedSettings.DATE_RANGE_PREDEFINED_WEEK%>" scope="request"/>
<c:set var="DATE_RANGE_PREDEFINED_30_DAYS" value="<%=BirtReportDateRangedSettings.DATE_RANGE_PREDEFINED_30_DAYS%>" scope="request"/>
<c:set var="DATE_RANGE_PREDEFINED_LAST_MONTH" value="<%=BirtReportDateRangedSettings.DATE_RANGE_PREDEFINED_LAST_MONTH%>" scope="request"/>
<c:set var="DATE_RANGE_PREDEFINED_THREE_MONTHS" value="<%=BirtReportDateRangedSettings.DATE_RANGE_PREDEFINED_THREE_MONTHS%>" scope="request"/>

<div class="tile vspace-bottom-0">
    <div class="tile-header">
        <h2 class="headline"><mvc:message code="statistic.TopDomains"/></h2>
    </div>
    <div class="tile-content tile-content-forms" data-field="toggle-vis">
        <div class="form-group">
            <div class="col-sm-offset-4 col-sm-8">
                <div class="checkbox">
                    <label for="top_domains_settings_enable">
                        <mvc:checkbox path="settings[TOP_DOMAIN][enabled]" id="top_domains_settings_enable" value="true" cssClass="birtreport_enable_checkbox"/>
                        <strong><mvc:message code="report.activateDelivery"/></strong>
                    </label>
                </div>
            </div>
        </div>
        <div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="report.recipient.period"/></label>
                </div>
                <div class="col-sm-8">
                    <label class="radio-inline">
                        <mvc:radiobutton path="settings[TOP_DOMAIN][dateRangeType]" value="${DATE_RANGE_PREDEFINED}"
                                         data-field-vis=""
                                         data-field-vis-hide="#customDateRange_from,#customDateRange_to"
                                         data-field-vis-show="#predefinedDateRange"/>
                        <mvc:message code="birt.report.period.predefined"/>
                    </label>
                    <label class="radio-inline">
                        <mvc:radiobutton path="settings[TOP_DOMAIN][dateRangeType]" value="${DATE_RANGE_CUSTOM}"
                                         data-field-vis=""
                                         data-field-vis-hide="#predefinedDateRange"
                                         data-field-vis-show="#customDateRange_from,#customDateRange_to"/>

                        <mvc:message code="birt.report.period.custom"/>
                    </label>
                </div>
            </div>
            <div id="predefinedDateRange" class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="topDomains_predefinedDateRange"><mvc:message code="statistics.dateRange"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="settings[TOP_DOMAIN][predefinedDateRange]" id="topDomains_predefinedDateRange" size="1" cssClass="form-control js-select">
                        <mvc:option value="${DATE_RANGE_PREDEFINED_WEEK}"><mvc:message code="report.recipient.last.week"/></mvc:option>
                        <mvc:option value="${DATE_RANGE_PREDEFINED_30_DAYS}"><mvc:message code="report.last.30days"/></mvc:option>
                        <mvc:option value="${DATE_RANGE_PREDEFINED_LAST_MONTH}"><mvc:message code="report.last.month"/></mvc:option>
                        <mvc:option value="${DATE_RANGE_PREDEFINED_THREE_MONTHS}"><mvc:message code="report.recipient.last.three.months"/></mvc:option>
                    </mvc:select>
                </div>
            </div>
            <div id="customDateRange_from" class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="customStartDate"><mvc:message code="report.recipient.from"/></label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <mvc:text path="settings[TOP_DOMAIN][startDate]" id="customStartDate"
                                      data-value="${birtReportForm.settings.TOP_DOMAIN.startDate}"
                                      cssClass="form-control datepicker-input js-datepicker"
                                      data-datepicker-options="format: '${fn:toLowerCase(datePickerFormatPattern)}',
                                      formatSubmit: '${fn:toLowerCase(reportDateFormatPattern)}'"/>
                        </div>
                        <div class="input-group-btn">
                            <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1" >
                                <i class="icon icon-calendar-o"></i>
                            </button>
                        </div>
                    </div>

                </div>
            </div>
            <div id="customDateRange_to" class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="customStartDate"><mvc:message code="default.to"/></label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <mvc:text path="settings[TOP_DOMAIN][stopDate]" id="customStopDate"
                                      data-value="${birtReportForm.settings.TOP_DOMAIN.stopDate}"
                                      cssClass="form-control datepicker-input js-datepicker"
                                      data-datepicker-options="format: '${fn:toLowerCase(datePickerFormatPattern)}',
                                      formatSubmit: '${fn:toLowerCase(reportDateFormatPattern)}'"/>
                        </div>
                        <div class="input-group-btn">
                            <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1" >
                                <i class="icon icon-calendar-o"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="selectedMaialinglists_topDomains"><mvc:message code="report.mailinglists"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="settings[TOP_DOMAIN][selectedMailinglists]" id="selectedMaialinglists_topDomains" multiple="true" cssClass="form-control js-select-tags">
                        <mvc:options items="${mailinglistList}" itemValue="id" itemLabel="shortname"/>
                    </mvc:select>

                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="availableTargets_topDomains"><mvc:message code="Target-Groups"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:message var="addTargetGroupMessage" code="addTargetGroup" />
                    <mvc:select path="settings[TOP_DOMAIN][selectedTargets]" id="availableTargets_topDomains" cssClass="form-control js-select-tags" multiple="true" data-placeholder="${addTargetGroupMessage}">
                        <mvc:options items="${targetList}" itemValue="id" itemLabel="targetName"/>
                    </mvc:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for=""><mvc:message code="domains.max"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="settings[TOP_DOMAIN][maxDomains]" cssClass="form-control select2-offscreen">
                        <mvc:option value="5">5</mvc:option>
                        <mvc:option value="10">10</mvc:option>
                        <mvc:option value="15">15</mvc:option>
                    </mvc:select>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="tile">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#top-domains-figures">
            <i class="icon tile-toggle icon-angle-up"></i>
            <mvc:message code="report.figures"/>
        </a>
        <ul class="tile-header-actions">
            <!-- dropdown for toggle all on/off -->
            <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                    <i class="icon icon-pencil"></i>
                    <span class="text"><mvc:message code="ToggleOnOff"/></span>
                    <i class="icon icon-caret-down"></i>
                </a>
                <ul class="dropdown-menu">
                    <li>
                        <a href="#" data-toggle-checkboxes="on">
                            <i class="icon icon-check-square-o"></i>
                            <span class="text"><mvc:message code="toggle.allOn"/></span>
                        </a>
                    </li>
                    <li>
                        <a href="#" data-toggle-checkboxes="off">
                            <i class="icon icon-square-o"></i>
                            <span class="text"><mvc:message code="toggle.allOff"/></span>
                        </a>
                    </li>
                </ul>
            </li>
        </ul>
    </div>

    <div id="top-domains-figures" class="tile-content tile-content-forms">
        <c:forEach items="${propertiesMap}" var="settings">
            <c:if test="${settings.key.key == TOP_DOMAIN_KEY}">
                <c:forEach items="${settings.value}" var="group">
                    <c:choose>
                        <c:when test="${empty group.key}">
                            <c:forEach items="${group.value}" var="prop">
                                <li class="list-group-item">
                                    <label class="checkbox-inline" for="property_topdomain_${prop.propName}">
                                        <mvc:checkbox path="settings[TOP_DOMAIN][${prop.propName}]" id="property_topdomain_${prop.propName}" value="true"/>
                                        <mvc:message code="${prop.labelCode}"/>
                                    </label>
                                </li>

                                <c:if test="${settings.value.containsKey(prop.propName)}">
                                    <c:forEach items="${settings.value.get(prop.propName)}" var="subProp">
                                        <li class="list-group-item list-group-item-nested">
                                            <label class="checkbox-inline" for="property_topdomain_${subProp.propName}">
                                                <mvc:checkbox path="settings[TOP_DOMAIN][${subProp.propName}]" id="property_topdomain_${subProp.propName}" value="true"/>
                                                <mvc:message code="${subProp.labelCode}"/>
                                            </label>
                                        </li>
                                    </c:forEach>

                                </c:if>
                            </c:forEach>
                        </c:when>
                    </c:choose>

                </c:forEach>
            </c:if>
        </c:forEach>
    </div>
</div>
