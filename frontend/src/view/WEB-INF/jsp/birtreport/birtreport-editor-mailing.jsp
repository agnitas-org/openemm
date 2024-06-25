<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ page import="com.agnitas.emm.core.birtreport.dto.FilterType" %>
<%@ page import="com.agnitas.emm.core.birtreport.dto.PredefinedType" %>
<%@ page import="com.agnitas.emm.core.birtreport.dto.ReportSettingsType" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="birtReportForm" type="com.agnitas.emm.core.birtreport.forms.BirtReportForm"--%>
<%--@elvariable id="propertiesMap" type="java.util.Map<ReportSettingsType, Map<String, List<BirtReportSettings.Properties>>>"--%>
<%--@elvariable id="actionBasedMailings" type="java.util.List"--%>
<%--@elvariable id="dateBasedMailings" type="java.util.List"--%>
<%--@elvariable id="intervalBasedMailings" type="java.util.List"--%>
<%--@elvariable id="followupMailings" type="java.util.List"--%>
<%--@elvariable id="targetList" type="java.util.List"--%>

<c:set var="PREDEFINED_LAST_ONE" value="<%=PredefinedType.PREDEFINED_LAST_ONE.getValue()%>" scope="request"/>
<c:set var="PREDEFINED_LAST_THREE" value="<%=PredefinedType.PREDEFINED_LAST_THREE.getValue()%>" scope="request"/>
<c:set var="PREDEFINED_LAST_FIVE" value="<%=PredefinedType.PREDEFINED_LAST_FIVE.getValue()%>" scope="request"/>

<c:set var="FILTER_NO_FILTER" value="<%=FilterType.FILTER_NO_FILTER.getKey()%>" scope="request"/>
<c:set var="FILTER_ARCHIVE" value="<%=FilterType.FILTER_ARCHIVE.getKey()%>" scope="request"/>
<c:set var="FILTER_MAILINGLIST" value="<%=FilterType.FILTER_MAILINGLIST.getKey()%>" scope="request"/>
<c:set var="FILTER_TARGET" value="<%=FilterType.FILTER_TARGET.getKey()%>" scope="request"/>

<c:set var="MAILING" value="<%= ReportSettingsType.MAILING%>"/>
<c:set var="MAILING_KEY" value="<%= ReportSettingsType.MAILING.getKey()%>"/>

<c:set var="predefined" value="${birtReportForm.settings[MAILING]['predefineMailing']}"/>
<c:if test="${empty predefined}">
    <c:set var="predefined" value="0"/>
</c:if>

<c:set var="filter" value="${birtReportForm.settings[MAILING]['mailingFilter']}"/>
<c:if test="${empty filter}">
    <c:set var="filter" value="${FILTER_NO_FILTER}"/>
</c:if>

<div class="tile vspace-bottom-0" data-initializer="report-settings">
    <script id="config:report-settings" type="application/json">
        {
            "settingsType" : ${MAILING_KEY},
            "data" : {
                "mailingType": ${birtReportForm.settings[MAILING]['mailingType']},
                "mailingFilter": ${filter},
                "predefineMailing": ${predefined},
                "selectedMailings": ${emm:toJson(birtReportForm.settings[MAILING]['selectedMailings'])}
            },
            "selectors" : {
                "filterBlockId" : "#field-filter-mailing select",
                "normalMailing" : "#normal-mailings-mailing-settings select",
                "mailingType" : "[name$='settings[MAILING][mailingType]']",
                "mailingFilter" : "[name='settings[MAILING][mailingFilter]']",
                "predefineMailing" : "[name='settings[MAILING][predefineMailing]']"
            }

        }
    </script>
    <div class="tile-header">
        <h2 class="headline"><mvc:message code="mailing.statistics"/></h2>
    </div>
    <div class="tile-content tile-content-forms" data-field="toggle-vis">

        <div class="form-group">
            <div class="col-sm-offset-4 col-sm-8">
                <div class="checkbox">
                    <label for="mailing_settings_enable">
                        <mvc:checkbox path="settings[MAILING][enabled]" id="mailing_settings_enable" cssClass="birtreport_enable_checkbox" value="true"/>
                        <strong><mvc:message code="report.activateDelivery"/></strong>
                    </label>
                </div>
            </div>
        </div>

        <div>
            <div id="type-mailings-stats" class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="mailing.Mailing_Type"/></label>
                </div>
                <div class="col-sm-8">
                    <label class="radio-inline">
                        <mvc:radiobutton path="settings[MAILING][mailingGeneralType]" value="3"
                                         data-field-vis=""
                                         data-field-vis-hide="#actionbased-mailings-stats,
                                                            #intervalbased-mailings-stats,
                                                            #followup-mailings-stats,
                                                            #datebased-mailings-stats,
                                                            #mailing-statistic-period,
                                                            #custom-date-range-from,
                                                            #custom-date-range-to"
                                         data-field-vis-show="#normal-type-mailings-stats, #filter-normal-mailing"/>

                        <mvc:message code="Normal_Mailing"/>
                    </label>
                    <label class="radio-inline">
                        <mvc:radiobutton path="settings[MAILING][mailingGeneralType]" value="4"
                                         data-field-vis=""
                                         data-field-vis-hide="#normal-type-mailings-stats,
                                                            #report-mailings-stats,
                                                            #normal-mailings-mailing-settings,
                                                            #intervalbased-mailings-stats,
                                                            #followup-mailings-stats,
                                                            #datebased-mailings-stats,
                                                            #filter-normal-mailing"
                                         data-field-vis-show="#actionbased-mailings-stats, #mailing-statistic-period"/>

                        <mvc:message code="mailing.action.based.mailing"/>
                    </label>
                    <label class="radio-inline">
                        <mvc:radiobutton path="settings[MAILING][mailingGeneralType]" value="5"
                                         data-field-vis=""
                                         data-field-vis-hide="#normal-type-mailings-stats,
                                                            #report-mailings-stats,
                                                            #normal-mailings-mailing-settings,
                                                            #actionbased-mailings-stats,
                                                            #intervalbased-mailings-stats,
                                                            #followup-mailings-stats,
                                                            #filter-normal-mailing"
                                         data-field-vis-show="#datebased-mailings-stats, #mailing-statistic-period"/>

                        <mvc:message code="mailing.Rulebased_Mailing"/>
                    </label>
                    <label class="radio-inline">
                        <mvc:radiobutton path="settings[MAILING][mailingGeneralType]" value="6"
                                         data-field-vis=""
                                         data-field-vis-hide="#normal-type-mailings-stats,
                                                            #report-mailings-stats,
                                                            #normal-mailings-mailing-settings,
                                                            #actionbased-mailings-stats,
                                                            #followup-mailings-stats,
                                                            #datebased-mailings-stats,
                                                            #filter-normal-mailing"
                                         data-field-vis-show="#intervalbased-mailings-stats, #mailing-statistic-period"/>

                        <mvc:message code="mailing.Interval_Mailing"/>
                    </label>
                    <label class="radio-inline">
                        <mvc:radiobutton path="settings[MAILING][mailingGeneralType]" value="7"
                                         data-field-vis=""
                                         data-field-vis-hide="#normal-type-mailings-stats,
                                                            #report-mailings-stats,
                                                            #normal-mailings-mailing-settings,
                                                            #actionbased-mailings-stats,
                                                            #intervalbased-mailings-stats,
                                                            #datebased-mailings-stats,
                                                            #filter-normal-mailing"
                                         data-field-vis-show="#followup-mailings-stats, #mailing-statistic-period"/>

                        <mvc:message code="mailing.Followup_Mailing"/>
                    </label>
                </div>

            </div>
            <div id="normal-type-mailings-stats" class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="report.mailings"/></label>
                </div>
                <div class="col-sm-8">
                    <label class="radio-inline">
                        <mvc:radiobutton path="settings[MAILING][mailingType]" value="1"
                                         data-field-vis=""
                                         data-field-vis-hide="#normal-mailings-mailing-settings"
                                         data-field-vis-show="#report-mailings-stats"/>

                        <mvc:message code="birt.report.predefinedMailing"/>
                    </label>
                    <label class="radio-inline">
                        <mvc:radiobutton path="settings[MAILING][mailingType]" value="3"
                                         data-field-vis=""
                                         data-field-vis-hide="#report-mailings-stats"
                                         data-field-vis-show="#normal-mailings-mailing-settings"/>

                        <mvc:message code="report.mailing.select"/>
                    </label>
                </div>
            </div>
        </div>

        <div id="filter-normal-mailing" class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><mvc:message code="report.mailing.filter"/></label>
            </div>
             <div class="col-sm-4">
                <mvc:select path="settings[MAILING][mailingFilter]" cssClass="form-control js-select" data-field-vis="" size="1">
                    <mvc:option value="${FILTER_NO_FILTER}" data-field-vis-hide="#field-filter-mailing">
                        <mvc:message code="report.mailing.filter.default"/>
                    </mvc:option>
                    <mvc:option value="${FILTER_ARCHIVE}" data-field-vis-show="#field-filter-mailing">
                        <mvc:message code="mailing.archive"/>
                    </mvc:option>
                    <mvc:option value="${FILTER_MAILINGLIST}" data-field-vis-show="#field-filter-mailing">
                        <mvc:message code="Mailinglist"/>
                    </mvc:option>
                    <mvc:option value="${FILTER_TARGET}" data-field-vis-show="#field-filter-mailing">
                        <mvc:message code="target.Target"/>
                    </mvc:option>
                </mvc:select>
            </div>
            <div id="field-filter-mailing" class="col-sm-4">
                <mvc:select path="settings[MAILING][predefineMailing]" cssClass="form-control js-select" size="1">
                </mvc:select>
            </div>
        </div>

        <div id="report-mailings-stats" class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><mvc:message code="report.mailings"/></label>
            </div>
            <div class="col-sm-8">
                <mvc:select path="settings[MAILING][predefinedMailings]" cssClass="form-control js-select" size="1">
                    <mvc:option value="${PREDEFINED_LAST_ONE}"><mvc:message code="report.mailing.last"/></mvc:option>
                    <mvc:option value="${PREDEFINED_LAST_THREE}"><mvc:message code="report.mailing.last.three"/></mvc:option>
                    <mvc:option value="${PREDEFINED_LAST_FIVE}"><mvc:message code="report.mailing.last.five"/></mvc:option>
                </mvc:select>
            </div>
        </div>

        <div id="normal-mailings-mailing-settings" class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><mvc:message code="report.mailing.select"/></label>
            </div>
            <div class="col-sm-8">
                <mvc:select path="settings[MAILING][selectedMailings]" multiple="true" cssClass="form-control js-select-tags">
                </mvc:select>
            </div>
        </div>

        <div id="actionbased-mailings-stats" class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><mvc:message code="report.mailing.select"/></label>
            </div>
            <div class="col-sm-8">
                <mvc:select path="settings[MAILING][selectedMailings]" multiple="true" cssClass="form-control js-select-tags">
                    <mvc:options items="${actionBasedMailings}" itemValue="id" itemLabel="shortname"/>
                </mvc:select>
            </div>
        </div>

        <div id="datebased-mailings-stats" class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><mvc:message code="report.mailing.select"/></label>
            </div>
            <div class="col-sm-8">
                <mvc:select path="settings[MAILING][selectedMailings]" multiple="true" cssClass="form-control js-select-tags">
                    <mvc:options items="${dateBasedMailings}" itemValue="id" itemLabel="shortname"/>
                </mvc:select>
            </div>
        </div>

        <div id="intervalbased-mailings-stats" class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><mvc:message code="report.mailing.select"/></label>
            </div>
            <div class="col-sm-8">
                <mvc:select path="settings[MAILING][selectedMailings]" multiple="true" cssClass="form-control js-select-tags">
                    <mvc:options items="${intervalBasedMailings}" itemValue="id" itemLabel="shortname"/>
                </mvc:select>
            </div>
        </div>

        <div id="followup-mailings-stats" class="form-group">
            <div class="col-sm-4">
                <label class="control-label"><mvc:message code="report.mailing.select"/></label>
            </div>
            <div class="col-sm-8">
                <mvc:select path="settings[MAILING][selectedMailings]" multiple="true" cssClass="form-control js-select-tags">
                    <mvc:options items="${followupMailings}" itemValue="id" itemLabel="shortname"/>
                </mvc:select>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="availableTargets_comparison"><mvc:message code="Target-Groups"/></label>
            </div>
            <div class="col-sm-8">
                <mvc:message var="addTargetGroupMessage" code="addTargetGroup" />
                <mvc:select path="settings[MAILING][selectedTargets]" id="availableTargets_comparison" cssClass="form-control js-select-tags" multiple="true" data-placeholder="${addTargetGroupMessage}">
                    <mvc:options items="${targetList}" itemValue="id" itemLabel="targetName"/>
                </mvc:select>
            </div>
        </div>

        <div id="period">
            <jsp:include page="birtreport-editor-period.jsp">
                <jsp:param name="periodPath" value="settings[MAILING][periodType]"/>
                <jsp:param name="startDatePath" value="settings[MAILING][startDate]"/>
                <jsp:param name="stopDatePath" value="settings[MAILING][stopDate]"/>
            </jsp:include>
        </div>
    </div>
</div>

<div class="tile">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#mailing-figures">
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


    <div id="mailing-figures" class="tile-content tile-content-forms">
        <div class="form-group">
            <c:forEach items="${propertiesMap}" var="settings">
                <c:if test="${settings.key.key == MAILING_KEY}">
                    <c:forEach items="${settings.value}" var="group">
                        <div class="col-sm-3">
                            <ul class="list-group">
                                <li class="list-group-item">
                                    <h5 class="list-group-item-heading">
                                        <mvc:message code="${group.key}"/>
                                    </h5>
                                </li>

                                <c:if test="${group.key == 'statistic.sending.opener'}">
                                    <li class="list-group-item">
                                        <label class="checkbox-inline" for="sentEmails_mailing">
                                            <input id="sentEmails_mailing" type="checkbox" checked="checked" disabled="disabled"/>
                                            <mvc:message code="statistic.mails.delivered"/>
                                        </label>
                                    </li>
                                    <li class="list-group-item">
                                        <label class="checkbox-inline" for="sentDate_mailing">
                                            <input id="sentDate_mailing" type="checkbox" checked="checked" disabled="disabled"/>
                                            <mvc:message code="mailing.senddate"/>
                                        </label>
                                    </li>
                                </c:if>

                                <c:forEach items="${group.value}" var="prop">
                                    <li class="list-group-item">
                                        <label class="checkbox-inline" for="property_mailing_${prop.propName}">
                                            <mvc:checkbox path="settings[MAILING][${prop.propName}]" id="property_mailing_${prop.propName}" value="true"/>
                                            <mvc:message code="${prop.labelCode}"/>
                                        </label>
                                    </li>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:forEach>
                </c:if>
            </c:forEach>
        </div>
    </div>
</div>
