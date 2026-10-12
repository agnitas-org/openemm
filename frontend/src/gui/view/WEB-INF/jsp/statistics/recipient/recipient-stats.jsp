<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.birtstatistics.DateMode" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core"      %>

<%--@elvariable id="daysPeriodLimit" type="java.lang.Integer"--%>
<%--@elvariable id="years" type="java.util.List"--%>
<%--@elvariable id="monthlist" type="java.util.List"--%>
<%--@elvariable id="targets" type="java.util.List"--%>
<%--@elvariable id="mailinglists" type="java.util.List"--%>
<%--@elvariable id="mediatypes" type="java.util.List"--%>

<mvc:form cssClass="tiles-container flex-column" id="stat-form" servletRelativeAction="/statistics/recipient/view.action"
          method="GET"
          modelAttribute="recipientStatsForm"
          data-form="resource"
          data-controller="recipient-stats"
          data-initializer="recipient-stats"
          data-editable-view="${agnEditViewKey}">

    <script id="config:recipient-stats" type="application/json">
        {
            "daysPeriodLimit": ${daysPeriodLimit}
        }
    </script>

    <div id="filter-tile" class="tile h-auto flex-none" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="report.mailing.filter"/></h1>
        </div>
        <div class="tile-body row g-3">
            <div class="col">
                <label for="mailinglist-select" class="form-label"><mvc:message code="Mailinglist"/></label>
                <select id="mailinglist-select" name="mailinglistId" class="form-control">
                    <option value="0"><mvc:message code="statistic.All_Mailinglists"/></option>
                    <c:forEach var="mailinglist" items="${mailinglists}">
                        <option value="${mailinglist.id}">${mailinglist.shortname}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="col" data-hide-by-select="#stats-type" data-hide-by-select-values="doi">
                <label for="targetId" class="form-label"><mvc:message code="Target"/></label>
                <mvc:select path="targetId" cssClass="form-control">
                    <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                    <mvc:options items="${targets}" itemValue="id" itemLabel="targetName"/>
                </mvc:select>
            </div>

            <div class="col" data-show-by-select="#stats-type" data-show-by-select-values="progress,opt-outs,opt-ins">
                <label for="mediatype" class="form-label"><mvc:message code="mediatype"/></label>
                <select id="mediatype" name="mediaType" class="form-control">
                    <c:forEach var="mt" items="${mediatypes}">
                        <option value="${mt}"><mvc:message code="mailing.MediaType.${mt.mediaCode}"/></option>
                    </c:forEach>
                </select>
            </div>
        </div>
    </div>
    <div id="overview-tile" class="tile" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="report.mailing.statistics.select"/></h1>
            <div class="tile-title-controls gap-3">
                <select class="form-control" id="stats-type" data-action="reload-stats" data-select-options="dropdownAutoWidth: true, width: 'auto'">
                    <option value="progress"><mvc:message code="statistics.progress"/></option>
                    <option value="recipient-status"><mvc:message code="Status"/></option>
                    <option value="mail-format"><mvc:message code="Mailtype"/></option>
                    <option value="status-info"><mvc:message code="recipient.Remark"/></option>
                    <%@include file="fragments/recipient-stats-extended-options.jspf"%>
                </select>

                <div data-show-by-select="#stats-type" data-show-by-select-values="progress,opt-outs,opt-ins">
                    <select id="period-type" name="dateMode" class="form-control"
                                data-action="reload-stats"
                                data-select-options="dropdownAutoWidth: true, width: 'auto'">
                        <option value="${DateMode.LAST_WEEK}"><mvc:message code="Week"/></option>
                        <option value="${DateMode.SELECT_MONTH}"><mvc:message code="Month"/></option>
                        <option value="${DateMode.SELECT_PERIOD}"><mvc:message code="statistics.dateRange"/></option>
                    </select>
                </div>

                <div data-show-by-select="#stats-type" data-show-by-select-values="progress,doi,opt-outs,opt-ins">
                    <div class="hstack gap-2"
                         data-date-range
                         data-show-by-select="#period-type"
                         data-show-by-select-values="SELECT_PERIOD">
                        <div class="hstack gap-3">
                            <label class="form-label mb-0" for="startDate"><mvc:message code="From"/></label>
                            <div class="date-picker-container">
                                <mvc:text path="startDate" id="startDate"
                                          data-field-validator="max-length-validator" data-validator-options="max: 10"
                                          cssClass="form-control js-datepicker"/>
                            </div>
                        </div>
                        <div class="hstack gap-3">
                            <label class="form-label mb-0" for="endDate"><mvc:message code="default.to"/></label>
                            <div class="date-picker-container">
                                <mvc:text path="endDate" id="endDate"
                                          data-field-validator="max-length-validator" data-validator-options="max: 10"
                                          cssClass="form-control js-datepicker"/>
                            </div>
                        </div>
                    </div>

                    <div class="d-flex gap-3"
                         data-show-by-select="#period-type"
                         data-show-by-select-values="SELECT_MONTH">
                        <mvc:select path="month" cssClass="form-control">
                            <c:forEach items="${months}" var="month">
                                <mvc:option value="${month[0] + 1}"><mvc:message code="${month[1]}"/></mvc:option>
                            </c:forEach>
                        </mvc:select>
                        <mvc:select path="year" cssClass="form-control">
                            <mvc:options items="${years}" />
                        </mvc:select>
                    </div>
                </div>
            </div>
        </div>

        <div class="tile-body p-3 d-flex flex-column"></div>
    </div>
</mvc:form>

<%@include file="fragments/recipient-progress-stats.jspf"%>
<%@include file="fragments/recipient-opt-ins-stats.jspf"%>
<%@include file="fragments/recipient-opt-outs-stats.jspf"%>
<%@include file="fragments/recipient-status-info-stats.jspf"%>
<%@include file="fragments/recipient-mail-format-stats.jspf"%>
<%@include file="fragments/recipient-recipient-status-stats.jspf"%>
<%@include file="fragments/recipient-doi-stats.jspf"%>
