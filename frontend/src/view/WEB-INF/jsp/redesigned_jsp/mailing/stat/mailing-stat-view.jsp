<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action"%>
<%@ page import="com.agnitas.emm.core.birtstatistics.enums.StatisticType" %>
<%@ page import="com.agnitas.emm.core.birtstatistics.DateMode" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<%--@elvariable id="mailingStatisticForm" type="com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatisticForm"--%>
<%--@elvariable id="sectorlist" type="java.util.Set<com.agnitas.emm.core.company.enums.Sector>"--%>
<%--@elvariable id="targetlist" type="java.util.List"--%>
<%--@elvariable id="isMailtrackingActive" type="java.lang.Boolean"--%>
<%--@elvariable id="mailtrackingExpired" type="java.lang.Boolean"--%>
<%--@elvariable id="isWorkflowStatistics" type="java.lang.Boolean"--%>
<%--@elvariable id="isEverSent" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingGrid" type="java.lang.Boolean"--%>
<%--@elvariable id="allowBenchmark" type="java.lang.Boolean"--%>
<%--@elvariable id="birtUrl" type="java.lang.String"--%>
<%--@elvariable id="localDatePattern" type="java.lang.String"--%>
<%--@elvariable id="reports" type="java.util.Map"--%>
<%--@elvariable id="monthlist" type="java.util.List<java.lang.String[]>"--%>
<%--@elvariable id="yearlist" type="java.util.List<java.lang.Integer>"--%>
<%--@elvariable id="mailtrackingExpirationDays" type="java.lang.Integer"--%>
<%--@elvariable id="isTotalAutoOpt" type="java.lang.Boolean"--%>
<%--@elvariable id="forCampaign" type="java.lang.Boolean"--%>
<%--@elvariable id="statWorkflowId" type="java.lang.Integer"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="workflowStatMailings" type="java.util.Map<java.lang.Integer, java.lang.String>"--%>

<c:set var="SUMMARY" value="<%= StatisticType.SUMMARY %>" />
<c:set var="CLICK_STATISTICS_PER_LINK" value="<%= StatisticType.CLICK_STATISTICS_PER_LINK %>" />
<c:set var="PROGRESS_OF_OPENINGS" value="<%= StatisticType.PROGRESS_OF_OPENINGS %>" />
<c:set var="PROGRESS_OF_CLICKS" value="<%= StatisticType.PROGRESS_OF_CLICKS %>" />
<c:set var="LAST_TENHOURS" value="<%= DateMode.LAST_TENHOURS %>" />
<c:set var="SELECT_DAY" value="<%= DateMode.SELECT_DAY %>" />
<c:set var="SELECT_MONTH" value="<%= DateMode.SELECT_MONTH %>" />
<c:set var="SELECT_PERIOD" value="<%= DateMode.SELECT_PERIOD %>" />

<mvc:form cssClass="tiles-container flex-column" id="stat-form" servletRelativeAction="/statistics/mailing/${mailingStatisticForm.mailingID}/view.action?statWorkflowId=${statWorkflowId}"
          method="GET" data-form="resource"
          modelAttribute="mailingStatisticForm"
          data-controller="mailing-stat"
          data-initializer="mailing-stat"
          data-editable-view="${agnEditViewKey}">

    <script id="config:mailing-stat" type="application/json">
        {
            "statWorkflowId" : "${statWorkflowId}"
        }
    </script>

    <c:if test="${mailingId > 0}">
        <div id="filter-tile" class="tile h-auto flex-none" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="report.mailing.filter"/></h1>
            </div>
            <div class="tile-body js-scrollable">
                <c:if test="${isTotalAutoOpt}">
                    <mvc:hidden path="selectedTargets"/>
                </c:if>

                <div class="row g-3">
                    <c:if test="${not isTotalAutoOpt}">
                        <div class="col">
                            <label class="form-label" for="targetGroupSelect"><mvc:message code="Targetgroups"/></label>
                            <c:set var="addTargetGroupMessage" scope="page">
                                <mvc:message code="addTargetGroup" />
                            </c:set>
                            <mvc:select path="selectedTargets" id="targetGroupSelect" cssClass="form-control js-select-tags"  multiple="multiple" data-placeholder="${addTargetGroupMessage}">
                                <mvc:options items="${targetlist}" itemValue="id" itemLabel="targetName"/>
                            </mvc:select >
                        </div>
                    </c:if>

                    <div class="col" data-show-by-select="#statisticType" data-show-by-select-values="SUMMARY">
                        <div class="form-check form-switch" style="margin-top: 2rem">
                            <mvc:checkbox id="showNetto" path="showNetto" cssClass="form-check-input" role="switch" data-form-submit=""/>
                            <label class="form-label form-check-label" for="showNetto"><mvc:message code="mailing.statistics.show.netto"/></label>
                        </div>
                    </div>

                    <c:if test="${mailingStatisticForm.statisticType eq 'TOP_DOMAINS'}">
                        <div class="col">
                            <label class="form-label"><mvc:message code="domains.max"/></label>
                            <mvc:select path="maxDomains" cssClass="form-control js-select">
                                <mvc:option value="5">5</mvc:option>
                                <mvc:option value="10">10</mvc:option>
                                <mvc:option value="15">15</mvc:option>
                            </mvc:select>
                        </div>

                        <div class="col">
                            <div class="form-check form-switch" style="margin-top: 2rem">
                                <mvc:checkbox id="top-level-domain-switch" path="topLevelDomain" cssClass="form-check-input" role="switch"/>
                                <label class="form-label form-check-label" for="top-level-domain-switch"><mvc:message code="ToplevelDomains"/></label>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </c:if>

    <div id="overview-tile" class="tile" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="${forCampaign ? 'statistic.workflow' : 'report.mailing.statistics.select'}"/></h1>
            <div class="tile-title-controls gap-3">

                <c:if test="${forCampaign}">
                    <mvc:select path="mailingID" cssClass="form-control js-select" data-select-options="dropdownAutoWidth: true, width: 'auto'" data-action="change-mailing">
                        <c:forEach var="workflowStatMailing" items="${workflowStatMailings}">
                            <mvc:option value="${workflowStatMailing.key}">${workflowStatMailing.value}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </c:if>

                <c:if test="${not forCampaign or mailingId > 0}">
                    <mvc:select path="statisticType" cssClass="form-control" id="statisticType" data-select-options="dropdownAutoWidth: true, width: 'auto'" data-form-submit="">
                        <mvc:option value=""><mvc:message code="statistic.load.specific"/></mvc:option>
                        <mvc:option value="${SUMMARY_TYPE}"><mvc:message code="Summary"/></mvc:option>
                        <mvc:option value="${CLICK_PER_LINK_TYPE}"><mvc:message code="Clickstats_Linkclicks"/></mvc:option>
                        <mvc:option value="${DELIVERY_PROGRESS_TYPE}"><mvc:message code="statistic.delivery_progress"/></mvc:option>
                        <mvc:option value="${OPENINGS_PROGRESS_TYPE}"><mvc:message code="Clickstats_Openings"/></mvc:option>
                        <mvc:option value="${CLICK_PROGRESS_TYPE}"><mvc:message code="statistic.clicks_progress"/></mvc:option>
                        <mvc:option value="${TOP_DOMAINS_TYPE}"><mvc:message code="statistic.TopDomains"/></mvc:option>
                        <mvc:option value="${BOUNCES_TYPE}"><mvc:message code="statistic.Bounces"/></mvc:option>
                        <%@ include file="fragments/statistic-type-extended-options.jspf" %>
                    </mvc:select>
                </c:if>

                <c:if test="${isTotalAutoOpt and mailingStatisticForm.statisticType eq SUMMARY_TYPE}">
                    <mvc:select path="ignoreAutoOptSummary" data-form-submit="" cssClass="form-control js-select" data-select-options="dropdownAutoWidth: true, width: 'auto'">
                        <mvc:option value="false"><mvc:message code="statistic.workflow"/></mvc:option>
                        <mvc:option value="true"><mvc:message code="mailing.statistics"/></mvc:option>
                    </mvc:select>
                </c:if>

                <c:if test="${isReportCanBeShown && (mailingStatisticForm.dateSelectMode ne 'NONE')}">
                    <c:set var="workflowStatUrl" value="" />
                    <c:if test="${isWorkflowStatistics}">
                        <c:url var="workflowStatUrl" value="/workflow/${statWorkflowId}/statistic.action" />
                    </c:if>
                    <mvc:select id="period-select" path="dateSelectMode" cssClass="form-control" data-select-options="dropdownAutoWidth: true, width: 'auto'"
                                data-form-submit="" data-form-url="${workflowStatUrl}">
                        <c:if test="${mailingStatisticForm.show10HoursTab}">
                            <mvc:option value="${LAST_TENHOURS}"><mvc:message code="TenHours"/></mvc:option>
                        </c:if>
                        <mvc:option value="${SELECT_DAY}"><mvc:message code="Day"/></mvc:option>
                        <mvc:option value="${SELECT_MONTH}"><mvc:message code="Month"/></mvc:option>
                        <mvc:option value="${SELECT_PERIOD}"><mvc:message code="statistics.dateRange"/></mvc:option>
                    </mvc:select>

                    <c:if test="${mailingStatisticForm.dateSelectMode eq SELECT_DAY}">
                        <div class="date-picker-container">
                            <mvc:text path="startDate.date" data-value="${mailingStatisticForm.startDate.date}" cssClass="form-control js-datepicker"/>
                        </div>
                    </c:if>

                    <c:if test="${mailingStatisticForm.dateSelectMode eq SELECT_MONTH}">
                        <div class="d-flex gap-inherit">
                            <mvc:select path="month" size="1" cssClass="form-control">
                                <c:forEach var="mon" items="${monthlist}">
                                    <mvc:option value="${mon[0]}"><mvc:message code="${mon[1]}"/></mvc:option>
                                </c:forEach>
                            </mvc:select>
                            
                            <mvc:select path="year" size="1" cssClass="form-control">
                                <c:forEach var="yea" items="${yearlist}">
                                    <mvc:option value="${yea}"><c:out value="${yea}"/></mvc:option>
                                </c:forEach>
                            </mvc:select>
                        </div>
                    </c:if>

                    <c:if test="${mailingStatisticForm.dateSelectMode eq SELECT_PERIOD}">
                        <div class="d-flex gap-2" data-date-range>
                            <div class="hstack gap-3">
                                <label class="form-label mb-0" for="startDay"><mvc:message code="From"/></label>
                                <div class="date-picker-container">
                                    <mvc:text id="startDay" path="startDate.date" cssClass="form-control js-datepicker"/>
                                </div>
                            </div>
                            <div class="hstack gap-3">
                                <label class="form-label mb-0" for="endDay"><mvc:message code="default.to"/></label>
                                <div class="date-picker-container">
                                    <mvc:text id="endDay" path="endDate.date" cssClass="form-control js-datepicker"/>
                                </div>
                            </div>
                        </div>
                    </c:if>
                </c:if>

                <c:if test="${allowBenchmark && mailingStatisticForm.statisticType eq BENCHMARK_TYPE}">
                    <div class="hstack gap-inherit">
                        <label class="form-label mb-0" for="sector"><mvc:message code="statistic.benchmark.industry"/></label>
                        <mvc:select path="sector" size="1" cssClass="form-control" id="sector">
                            <c:forEach var="curSector" items="${sectorlist}" begin="1">
                                <mvc:option value="${curSector.id}"><mvc:message code="${curSector.messageKey}"/></mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </c:if>
            </div>
        </div>

        <div class="tile-body p-2 d-flex flex-column gap-2">
            <c:if test="${fn:contains([SUMMARY, CLICK_STATISTICS_PER_LINK, PROGRESS_OF_OPENINGS, PROGRESS_OF_CLICKS], mailingStatisticForm.statisticType) }">
                <c:set var="endDeviceAndModelsNotification">
                    <div class="notification-simple notification-simple--info mx-2 w-auto">
                        <i class="icon icon-state-info"></i>
                        <p><mvc:message code="info.statistics.endDeviceAndModels"/></p>
                    </div>
                </c:set>
                <%@ include file="fragments/end-device-and-models-notification-extended.jspf" %>
                ${endDeviceAndModelsNotification}
            </c:if>

            <c:if test="${mailingStatisticForm.statisticType eq 'TOP_DOMAINS'}">
                <c:choose>
                    <c:when test="${not isEverSent}">
                        <mvc:message var="notificationMessage" code="statistics.topdomains.display.info"/>
                    </c:when>
                    <c:when test="${not isMailtrackingActive}">
                        <mvc:message var="notificationMessage" code="mailtracking.required.topdomains"/>
                    </c:when>
                    <c:when test="${not mailtrackingExpired}">
                        <mvc:message var="notificationMessage" code="mailtracking.required.topdomains.expired" arguments="${mailtrackingExpirationDays}"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="notificationMessage" value=""/>
                    </c:otherwise>
                </c:choose>

                <c:if test="${not empty notificationMessage}">
                    <div class="notification-simple notification-simple--info mx-2 w-auto">
                        <i class="icon icon-state-info"></i>
                        <p>${notificationMessage}</p>
                    </div>
                </c:if>
            </c:if>

            <c:if test="${allowBenchmark && mailingStatisticForm.statisticType eq 'BENCHMARK'}">
                <div class="notification-simple notification-simple--info mx-2 w-auto">
                    <i class="icon icon-state-info"></i>
                    <p><mvc:message code="statistic.benchmark.performanceBenchmark"/></p>
                </div>
            </c:if>

            <c:if test="${birtUrl ne null && isReportCanBeShown}">
                <div class="position-relative js-scrollable overflow-auto hide-scroll">
                    <iframe src="${birtUrl}" style="width: 100%">
                        Your Browser does not support IFRAMEs, please update!
                    </iframe>
                </div>
            </c:if>
        </div>
    </div>
</mvc:form>

<%@ include file="../../common/modal/evaluation-loader-modal.jspf" %>
