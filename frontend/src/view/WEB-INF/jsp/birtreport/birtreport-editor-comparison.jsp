<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ page import="com.agnitas.emm.core.birtreport.dto.ReportSettingsType" %>
<%@ page import="com.agnitas.emm.core.birtreport.dto.FilterType" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="birtReportForm" type="com.agnitas.emm.core.birtreport.forms.BirtReportForm"--%>
<%--@elvariable id="propertiesMap" type="java.util.Map<ReportSettingsType, Map<String, List<BirtReportSettings.Properties>>>"--%>
<%--@elvariable id="targetList" type="java.util.List"--%>

<c:set var="FILTER_NO_FILTER" value="<%=FilterType.FILTER_NO_FILTER.getKey()%>" scope="request"/>
<c:set var="FILTER_ARCHIVE" value="<%=FilterType.FILTER_ARCHIVE.getKey()%>" scope="request"/>
<c:set var="FILTER_MAILINGLIST" value="<%=FilterType.FILTER_MAILINGLIST.getKey()%>" scope="request"/>
<c:set var="FILTER_TARGET" value="<%=FilterType.FILTER_TARGET.getKey()%>" scope="request"/>

<c:set var="COMPARISON" value="<%= ReportSettingsType.COMPARISON%>"/>
<c:set var="COMPARISON_KEY" value="<%= ReportSettingsType.COMPARISON.getKey()%>"/>

<c:set var="predefined" value="${birtReportForm.settings[COMPARISON]['predefineMailing']}"/>
<c:if test="${empty predefined}">
    <c:set var="predefined" value="0"/>
</c:if>

<c:set var="filter" value="${birtReportForm.settings[COMPARISON]['mailingFilter']}"/>
<c:if test="${empty filter}">
    <c:set var="filter" value="${FILTER_NO_FILTER}"/>
</c:if>

<div class="tile vspace-bottom-0" data-initializer="report-settings">
    <script id="config:report-settings" type="application/json">
        {
            "settingsType" : ${COMPARISON_KEY},
            "data" : {
                "mailingType": ${birtReportForm.settings[COMPARISON]['mailingType']},
                "mailingFilter": ${filter},
                "predefineMailing": ${predefined},
                "selectedMailings": ${emm:toJson(birtReportForm.settings[COMPARISON]['selectedMailings'])}
            },

            "selectors" : {
                "filterBlockId" : "#field-filter-comparison select",
                "normalMailing" : "#normal_mailings_comparison select",
                "mailingType" : "[name='settings[COMPARISON][mailingType]']",
                "mailingFilter" : "[name='settings[COMPARISON][mailingFilter]']",
                "predefineMailing" : "[name='settings[COMPARISON][selectedMailings]']"
            }
        }
    </script>
    <div class="tile-header">
        <h2 class="headline"><mvc:message code="mailing.comparison"/></h2>
    </div>
    <div class="tile-content tile-content-forms" data-field="toggle-vis">

        <div class="form-group">
            <div class="col-sm-offset-4 col-sm-8">
                <div class="checkbox">
                    <label for="comparison_settings_enable">
                        <mvc:checkbox path="settings[COMPARISON][enabled]" id="comparison_settings_enable" cssClass="birtreport_enable_checkbox" value="true"/>
                        <strong><mvc:message code="report.activateDelivery"/></strong>
                    </label>
                </div>
            </div>
        </div>

        <div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="report.mailings"/></label>
                </div>
                <div class="col-sm-8">
                    <label class="radio-inline">
                        <mvc:radiobutton path="settings[COMPARISON][mailingType]" value="1"
                                         data-field-vis=""
                                         data-field-vis-hide="#normal_mailings_comparison" data-field-vis-show="#last-mailings_comparison"/>
                        <mvc:message code="birt.report.predefinedMailing"/>
                    </label>

                    <label class="radio-inline">
                        <mvc:radiobutton path="settings[COMPARISON][mailingType]" value="2"
                                         data-field-vis=""
                                         data-field-vis-hide="#last-mailings_comparison" data-field-vis-show="#normal_mailings_comparison"/>

                        <mvc:message code="report.mailing.select"/>
                    </label>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="report.mailing.filter"/></label>
                </div>

                <div class="col-sm-4">
                    <mvc:select path="settings[COMPARISON][mailingFilter]" cssClass="form-control js-select" data-field-vis="" size="1">
                        <mvc:option value="${FILTER_NO_FILTER}" data-field-vis-hide="#field-filter-comparison">
                            <mvc:message code="report.mailing.filter.default"/>
                        </mvc:option>
                        <mvc:option value="${FILTER_ARCHIVE}" data-field-vis-show="#field-filter-comparison">
                            <mvc:message code="mailing.archive"/>
                        </mvc:option>
                        <mvc:option value="${FILTER_MAILINGLIST}" data-field-vis-show="#field-filter-comparison">
                            <mvc:message code="Mailinglist"/>
                        </mvc:option>
                        <mvc:option value="${FILTER_TARGET}" data-field-vis-show="#field-filter-comparison">
                            <mvc:message code="target.Target"/>
                        </mvc:option>
                    </mvc:select>
                </div>
                <div id="field-filter-comparison" class="col-sm-4">
                    <mvc:select path="settings[COMPARISON][predefineMailing]" cssClass="form-control js-select" size="1">
                    </mvc:select>
                </div>
            </div>

            <div id="last-mailings_comparison" class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="report.mailings"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="settings[COMPARISON][predefinedMailings]" data-field-vis="" size="1" cssClass="form-control js-select">
                        <mvc:option value="2"  data-field-vis-hide="#period" ><mvc:message code="report.mailing.last.two"/></mvc:option>
                        <mvc:option value="3"  data-field-vis-hide="#period"><mvc:message code="report.mailing.last.three"/></mvc:option>
                        <mvc:option value="5"  data-field-vis-hide="#period"><mvc:message code="report.mailing.last.five"/></mvc:option>
                        <mvc:option value="10" data-field-vis-hide="#period"><mvc:message code="report.mailing.last.ten"/></mvc:option>
                        <mvc:option value="0"  data-field-vis-show="#period,#mailing-statistic-period" ><mvc:message code="report.period.select"/></mvc:option>
                    </mvc:select>
                </div>

                <div id="period">
                    <jsp:include page="birtreport-editor-period.jsp">
                        <jsp:param name="periodPath" value="settings[COMPARISON][periodType]"/>
                        <jsp:param name="startDatePath" value="settings[COMPARISON][startDate]"/>
                        <jsp:param name="stopDatePath" value="settings[COMPARISON][stopDate]"/>
                        <jsp:param name="startDateValue" value="${birtReportForm.settings.COMPARISON.startDate}"/>
                        <jsp:param name="stopDateValue" value="${birtReportForm.settings.COMPARISON.stopDate}"/>
                    </jsp:include>
                </div>
            </div>

            <div id="normal_mailings_comparison" class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="normal_mailings"><mvc:message code="report.mailing.select"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="settings[COMPARISON][selectedMailings]" id="normal_mailings" cssClass="form-control js-select-tags" multiple="true">
                    </mvc:select>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="availableTargets_comparison"><mvc:message code="Target-Groups"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:message var="addTargetGroupMessage" code="addTargetGroup" />
                    <mvc:select path="settings[COMPARISON][selectedTargets]" id="availableTargets_comparison" cssClass="form-control js-select-tags" multiple="true" data-placeholder="${addTargetGroupMessage}">
                        <mvc:options items="${targetList}" itemValue="id" itemLabel="targetName"/>
                    </mvc:select>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="tile">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#comparison-figures">
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

    <div id="comparison-figures" class="tile-content tile-content-forms">
        <div class="form-group">

            <c:set var="allowedStatsRevenue" value="false"/>
            <emm:ShowByPermission token="stats.revenue">
                <c:set var="allowedStatsRevenue" value="true"/>
            </emm:ShowByPermission>


            <c:forEach items="${propertiesMap}" var="settings">
                <c:if test="${settings.key.key == COMPARISON_KEY}">
                    <c:forEach items="${settings.value}" var="group">
                        <div class="col-sm-3">
                            <ul class="list-group">
                                <li class="list-group-item">
                                    <h5 class="list-group-item-heading">
                                        <mvc:message code="${group.key}"/>
                                    </h5>
                                </li>

                                <c:if test="${group.key == 'General'}">
                                    <li class="list-group-item">
                                        <label class="checkbox-inline" for="sentEmails_comparison">
                                            <input id="sentEmails_comparison" type="checkbox" checked="checked" disabled="disabled"/>
                                            <mvc:message code="report.deliveredMails"/>
                                        </label>
                                    </li>
                                    <li class="list-group-item">
                                        <label class="checkbox-inline" for="sentEmails_comparison">
                                            <input id="sentEmails_comparison" type="checkbox" checked="checked" disabled="disabled"/>
                                            <mvc:message code="statistic.mails.delivered"/>
                                        </label>
                                    </li>
                                    <li class="list-group-item">
                                        <label class="checkbox-inline" for="sentDate_comparison">
                                            <input id="sentDate_comparison" type="checkbox" checked="checked" disabled="disabled"/>
                                            <mvc:message code="mailing.senddate"/>
                                        </label>
                                    </li>
                                </c:if>

                                <c:forEach items="${group.value}" var="prop">
                                    <c:choose>
                                        <c:when test="${prop.propName eq 'conversionRate' && not allowedStatsRevenue}">
                                            <mvc:hidden path="settings[COMPARISON][${prop.propName}]" value="false"/>
                                        </c:when>

                                        <c:otherwise>
                                            <li class="list-group-item">
                                                <label class="checkbox-inline" for="property_comparison_${prop.propName}">
                                                    <mvc:checkbox path="settings[COMPARISON][${prop.propName}]" id="property_comparison_${prop.propName}" value="true"/>
                                                    <mvc:message code="${prop.labelCode}"/>
                                                </label>
                                            </li>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:forEach>
                </c:if>
            </c:forEach>
        </div>
    </div>
</div>
