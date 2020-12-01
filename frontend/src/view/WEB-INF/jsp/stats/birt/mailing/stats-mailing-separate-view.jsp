<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do"%>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags"   prefix="agn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles"    prefix="tiles" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<%--@elvariable id="mailingStatisticForm" type="com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatisticForm"--%>
<%--@elvariable id="sectorlist" type="java.util.Set<com.agnitas.emm.core.company.enums.Sector>"--%>
<%--@elvariable id="targetlist" type="java.util.List"--%>
<%--@elvariable id="isMailtrackingActive" type="java.lang.Boolean"--%>
<%--@elvariable id="mailtrackingExpired" type="java.lang.Boolean"--%>
<%--@elvariable id="isEverSent" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingGrid" type="java.lang.Boolean"--%>
<%--@elvariable id="allowBenchmark" type="java.lang.Boolean"--%>
<%--@elvariable id="birtUrl" type="java.lang.String"--%>
<%--@elvariable id="downloadBirtUrl" type="java.lang.String"--%>
<%--@elvariable id="localDatePattern" type="java.lang.String"--%>
<%--@elvariable id="reports" type="java.util.Map"--%>
<%--@elvariable id="monthlist" type="java.util.List<java.lang.String[]>"--%>
<%--@elvariable id="yearlist" type="java.util.List<java.lang.Integer>"--%>
<%--@elvariable id="mailtrackingExpirationDays" type="java.lang.Integer"--%>
<%--@elvariable id="isTotalAutoOpt" type="java.lang.Boolean"--%>

<c:set var="isReportCanBeShown" value="true"/>
<c:if test="${mailingStatisticForm.statisticType eq 'TOP_DOMAINS' && isEverSent eq false}">
    <c:set var="isReportCanBeShown" value="false"/>
</c:if>

<tiles:insert page="/WEB-INF/jsp/mailing/template.jsp">
    <tiles:put name="header" type="string">
        <ul class="tile-header-nav">
            <tiles:insert page="/WEB-INF/jsp/tabsmenu-mailing.jsp" flush="false"/>
        </ul>
    </tiles:put>

    <tiles:put name="content" type="string">
        <div class="${isMailingGrid ? "tile-content-padded" : "row"}">
            <div class="col-xs-12">
                <mvc:form servletRelativeAction="/statistics/mailing/${mailingStatisticForm.mailingID}/view.action"
                          method="post" data-form="resource"
                          modelAttribute="mailingStatisticForm">
                    <mvc:hidden path="dateSelectMode"/>
                    <mvc:hidden path="mailingID"/>


                    <div class="tile">
                        <div class="tile-header">
                            <a href="#" class="headline" data-toggle-tile="#tile-statisticsMailing">
                                <i class="tile-toggle icon icon-angle-up"></i>
                                <mvc:message code="Targetgroups"/>
                            </a>
                            <ul class="tile-header-actions">
                                <li>
                                    <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                                        <i class="icon icon-refresh"></i>
                                <span class="text">
                                    <mvc:message code="button.Refresh"/>
                                </span>
                                    </button>
                                </li>
                            </ul>
                        </div>
                        <div id="tile-statisticsMailing" class="tile-content tile-content-forms">
                            <c:if test="${isTotalAutoOpt}">
                                <mvc:hidden path="selectedTargets"/>
                            </c:if>

                            <c:if test="${not isTotalAutoOpt}">
                                <div class="form-group">
                                    <div class="col-sm-4">
                                        <label class="control-label" for="targetGroupSelect">
                                            <mvc:message code="Targetgroups"/>
                                        </label>
                                    </div>
                                    <div class="col-sm-8">
                                        <c:set var="addTargetGroupMessage" scope="page">
                                            <mvc:message code="addTargetGroup" />
                                        </c:set>
                                        <mvc:select path="selectedTargets" id="targetGroupSelect" cssClass="form-control js-select"  multiple="multiple" data-placeholder="${addTargetGroupMessage}">
                                            <mvc:options items="${targetlist}" itemValue="id" itemLabel="targetName"/>
                                        </mvc:select >
                                    </div>
                                </div>
                            </c:if>

                            <div class="form-group" data-show-by-select="#statisticType" data-show-by-select-values="SUMMARY">
                                <div class="col-sm-4">
                                    <label class="control-label" for="showNetto">
                                        <mvc:message code="mailing.statistics.show.netto"/>
                                    </label>
                                </div>
                                <div class="col-sm-8">
                                    <label class="toggle">
                                        <mvc:checkbox path="showNetto" id="showNetto" data-form-submit=""/>
                                        <div class="toggle-control"></div>
                                    </label>
                                </div>
                            </div>

                            <c:if test="${mailingStatisticForm.statisticType eq 'TOP_DOMAINS'}">
                                <div class="form-group">
                                    <div class="col-sm-4">
                                        <label class="control-label"><mvc:message code="domains.max"/></label>
                                    </div>
                                    <div class="col-sm-8">
                                        <mvc:select path="maxDomains" cssClass="form-control select2-offscreen">
                                            <mvc:option value="5">5</mvc:option>
                                            <mvc:option value="10">10</mvc:option>
                                            <mvc:option value="15">15</mvc:option>
                                        </mvc:select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <div class="col-sm-4">
                                        <label class="control-label"><mvc:message code="ToplevelDomains"/></label>
                                    </div>
                                    <div class="col-sm-8">
                                        <mvc:checkbox path="topLevelDomain"/>
                                    </div>
                                </div>
                            </c:if>
                        </div>
                    </div>

                    <div class="tile">

                        <div class="tile-header">
                            <label for="statisticType" class="headline">
                                <mvc:message code="report.mailing.statistics.select"/>
                            </label>
                            <div class="col-md-3 pull-left" style="margin: 0 20px 0 0;">
                                <mvc:select path="statisticType" data-form-submit="" cssClass="form-control" id="statisticType">
                                    <c:forEach items="${reports}" var="report">
                                        <mvc:option value="${report.key}">${report.value}</mvc:option>
                                    </c:forEach>
                                </mvc:select>
                            </div>

                            <c:if test="${isReportCanBeShown && (mailingStatisticForm.dateSelectMode ne 'NONE')}">
                                <ul class="tile-header-nav">
                                    <li class="${mailingStatisticForm.dateSelectMode == 'LAST_TENHOURS' ? 'active' : ''}">
                                        <a href="#" data-form-set="dateSelectMode:LAST_TENHOURS, statisticType:${mailingStatisticForm.statisticType}, startDate.date: '', endDate.date: ''" data-form-submit>
                                            <mvc:message code="TenHours"/>
                                        </a>
                                    </li>
                                    <li class="${mailingStatisticForm.dateSelectMode == 'SELECT_DAY' ? 'active' : ''}">
                                        <a href="#" data-form-set="dateSelectMode:SELECT_DAY, statisticType:${mailingStatisticForm.statisticType}, startDate.date: '', endDate.date: ''" data-form-submit>
                                            <mvc:message code="Day"/>
                                        </a>
                                    </li>
                                    <li class="${mailingStatisticForm.dateSelectMode == 'SELECT_MONTH' ? 'active' : ''}">
                                        <a href="#" data-form-set="dateSelectMode:SELECT_MONTH, statisticType:${mailingStatisticForm.statisticType}, startDate.date: '', endDate.date: ''" data-form-submit>
                                            <mvc:message code="Month"/>
                                        </a>
                                    </li>
                                    <li class="${mailingStatisticForm.dateSelectMode == 'SELECT_PERIOD' ? 'active' : ''}">
                                        <a href="#" data-form-set="dateSelectMode:SELECT_PERIOD, statisticType:${mailingStatisticForm.statisticType}, startDate.date: '', endDate.date: ''" data-form-submit>
                                            <mvc:message code="statistics.dateRange"/>
                                        </a>
                                    </li>
                                </ul>
                            </c:if>

                            <c:if test="${isReportCanBeShown && not empty downloadBirtUrl}">
                                <ul class="tile-header-actions">
                                    <li class="dropdown">
                                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                            <i class="icon icon-cloud-download"></i>
                                            <span class="text"><mvc:message code="Export"/></span>
                                            <i class="icon icon-caret-down"></i>
                                        </a>
                                        <ul class="dropdown-menu">
                                            <li class="dropdown-header"><mvc:message code="statistics.exportFormats"/></li>
                                            <li>
                                                <a href="${downloadBirtUrl}" tabindex="-1" data-prevent-load="">
                                                    <i class="icon icon-file-excel-o"></i>
                                                    <mvc:message code='export.message.csv'/>
                                                </a>
                                            </li>
                                        </ul>
                                    </li>
                                </ul>
                            </c:if>
                        </div>

                        <div class="tile-content" data-form-content>

                            <c:if test="${isReportCanBeShown && (mailingStatisticForm.dateSelectMode ne 'NONE')}">

                                <c:if test="${mailingStatisticForm.dateSelectMode == 'SELECT_DAY'}">

                                    <div class="tile-controls">
                                        <div class="controls">
                                            <div class="control">
                                                <div class="input-group">
                                                    <div class="input-group-controls">
                                                        <mvc:text path="startDate.date" data-value="${mailingStatisticForm.startDate.date}"
                                                                  cssClass="form-control datepicker-input js-datepicker"
                                                                  data-datepicker-options="format: '${fn:toLowerCase(localDatePattern)}'"/>
                                                    </div>
                                                    <div class="input-group-btn">
                                                        <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                                            <i class="icon icon-calendar-o"></i>
                                                        </button>
                                                    </div>
                                                    <div class="input-group-btn">
                                                        <button class="btn btn-primary btn-regular pull-right" type="button" data-form-set="statisticType: ${mailingStatisticForm.statisticType}" data-form-submit>
                                                            <i class="icon icon-refresh"></i>
                                                            <span class="text"><mvc:message code="button.Refresh"/></span>
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                </c:if>

                                <c:if test="${mailingStatisticForm.dateSelectMode == 'SELECT_MONTH'}">
                                    <div class="tile-controls">
                                        <div class="controls controls-left">
                                            <div class="control">
                                                <mvc:select path="month" size="1" cssClass="form-control select2-offscreen">
                                                    <c:forEach var="mon" items="${monthlist}">
                                                        <mvc:option value="${mon[0]}"><mvc:message code="${mon[1]}"/></mvc:option>
                                                    </c:forEach>
                                                </mvc:select>
                                            </div>
                                            <div class="control">
                                                <mvc:select path="year" size="1" cssClass="form-control select2-offscreen">
                                                    <c:forEach var="yea" items="${yearlist}">
                                                        <mvc:option value="${yea}"><c:out value="${yea}"/></mvc:option>
                                                    </c:forEach>
                                                </mvc:select>
                                            </div>
                                        </div>

                                        <div class="controls controls-right">
                                            <div class="control">
                                                <button class="btn btn-primary btn-regular pull-right" type="button" data-form-set="statisticType:${mailingStatisticForm.statisticType}" data-form-submit>
                                                    <i class="icon icon-refresh"></i>
                                            <span class="text">
                                                <mvc:message code="button.Refresh"/>
                                            </span>
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </c:if>

                                <c:if test="${mailingStatisticForm.dateSelectMode == 'SELECT_PERIOD'}">
                                    <div class="tile-controls">
                                        <div class="controls controls-left">

                                            <div class="date-range-controls">
                                                <label class="control" for="startDay">
                                                    <mvc:message code="From"/>
                                                </label>

                                                <div class="control">
                                                    <div class="input-group">
                                                        <div class="input-group-controls">
                                                            <input id="startDay" type="text" value="${mailingStatisticForm.startDate.date}" class="form-control datepicker-input js-datepicker" name="startDate.date"
                                                                   data-datepicker-options="format: '${fn:toLowerCase(localDatePattern)}'"/>
                                                        </div>
                                                        <div class="input-group-btn">
                                                            <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                                                <i class="icon icon-calendar-o"></i>
                                                            </button>
                                                        </div>
                                                    </div>
                                                </div>


                                                <label class="control" for="endDay">
                                                    <mvc:message code="default.to"/>
                                                </label>

                                                <div class="control">
                                                    <div class="input-group">
                                                        <div class="input-group-controls">
                                                            <input id="endDay" type="text" value="${mailingStatisticForm.endDate.date}" class="form-control datepicker-input js-datepicker" name="endDate.date"
                                                                   data-datepicker-options="format: '${fn:toLowerCase(localDatePattern)}'"/>
                                                        </div>
                                                        <div class="input-group-btn">
                                                            <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                                                <i class="icon icon-calendar-o"></i>
                                                            </button>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>

                                        </div>

                                        <div class="controls controls-right">
                                            <button class="btn btn-primary btn-regular pull-right" type="button" data-form-set="statisticType:${mailingStatisticForm.statisticType}" data-form-submit>
                                                <i class="icon icon-refresh"></i>
                                            <span class="text">
                                                <mvc:message code="button.Refresh"/>
                                            </span>
                                            </button>
                                        </div>

                                    </div>

                                </c:if>

                            </c:if>

                            <c:if test="${mailingStatisticForm.statisticType eq 'TOP_DOMAINS'}">
                                <c:choose>
                                    <c:when test="${isEverSent eq false}">
                                        <c:set var="notificationMessage">
                                            <mvc:message code="statistics.topdomains.display.info"/>
                                        </c:set>
                                    </c:when>
                                    <c:when test="${isMailtrackingActive eq false}">
                                        <c:set var="notificationMessage">
                                            <mvc:message code="mailtracking.required.topdomains"/>
                                        </c:set>
                                    </c:when>
                                    <c:when test="${mailtrackingExpired eq false}">
                                        <c:set var="notificationMessage">
                                            <mvc:message code="mailtracking.required.topdomains.expired" arguments="${mailtrackingExpirationDays}"/>
                                        </c:set>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="notificationMessage" value=""/>
                                    </c:otherwise>
                                </c:choose>

                                <c:if test="${notificationMessage ne ''}">
                                    <div class="tile-controls">
                                        <div class="notification notification-info">
                                            <div class="notification-header">
                                                <p class="headline">
                                                    <i class="icon icon-state-info"></i>
                                                    <span class="text">${notificationMessage}</span>
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                </c:if>
                            </c:if>

                            <c:if test="${allowBenchmark && mailingStatisticForm.statisticType eq 'BENCHMARK'}">
                                <div class="tile-controls">
                                    <div class="notification notification-info">
                                        <div class="notification-header">
                                            <p class="headline">
                                                <i class="icon icon-state-info"></i>
                                                <span class="text"><mvc:message code="statistic.benchmark.performanceBenchmark"/> <mvc:message code="statistic.benchmark.available5HoursAfterSend"/></span>
                                            </p>
                                        </div>
                                    </div>
                                </div>

                                <div class="tile-controls">
                                    <div class="form-group">
                                        <div class="col-sm-1">
                                            <label class="control-label" for="sector">
                                                <mvc:message code="statistic.benchmark.industry"/>
                                            </label>
                                        </div>
                                        <div class="col-sm-11">
                                            <div class="input-group">
                                                <div class="input-group-controls">
                                                    <mvc:select path="sector" size="1" cssClass="form-control" id="sector">
                                                        <c:forEach var="curSector" items="${sectorlist}" begin="1">
                                                            <mvc:option value="${curSector.id}"><mvc:message code="${curSector.messageKey}"/></mvc:option>
                                                        </c:forEach>
                                                    </mvc:select>
                                                </div>
                                                <div class="input-group-btn">
                                                    <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                                                        <mvc:message code="settings.Update"/>
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>

                            <c:if test="${birtUrl ne null && isReportCanBeShown}">
                                <iframe src="${birtUrl}" border="0" scrolling="auto" frameborder="0" style="width: 100%">
                                    Your Browser does not support IFRAMEs, please update!
                                </iframe>
                            </c:if>
                        </div>
                    </div>

                </mvc:form>

            </div>
        </div>

    </tiles:put>
</tiles:insert>
