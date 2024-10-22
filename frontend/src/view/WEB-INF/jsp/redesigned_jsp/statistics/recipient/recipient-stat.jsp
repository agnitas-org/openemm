<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.birtstatistics.DateMode" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="LAST_WEEK" value="<%= DateMode.LAST_WEEK.toString() %>"/>
<c:set var="SELECT_MONTH" value="<%= DateMode.SELECT_MONTH.toString() %>"/>
<c:set var="SELECT_PERIOD" value="<%= DateMode.SELECT_PERIOD.toString() %>"/>

<%--@elvariable id="yearlist" type="java.util.List"--%>
<%--@elvariable id="monthlist" type="java.util.List"--%>
<%--@elvariable id="targetlist" type="java.util.List"--%>
<%--@elvariable id="mailinglists" type="java.util.List"--%>
<%--@elvariable id="mediatypes" type="java.util.List"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.birtstatistics.recipient.forms.RecipientStatisticForm"--%>
<%--@elvariable id="birtStatisticUrlWithoutFormat" type="java.lang.String"--%>
<%--@elvariable id="localeDatePattern" type="java.lang.String"--%>

<mvc:form cssClass="tiles-container flex-column" id="stat-form" servletRelativeAction="/statistics/recipient/view.action"
          method="GET"
          modelAttribute="form"
          data-controller="recipient-statistics-view"
          data-editable-view="${agnEditViewKey}">
    <div id="filter-tile" class="tile h-auto flex-none" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="report.mailing.filter"/></h1>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col">
                    <label class="form-label"><mvc:message code="Mailinglist"/></label>
                    <mvc:select id="mailinglist-select" path="mailingListId" cssClass="form-control js-select">
                        <c:if test="${form.reportName ne 'recipient_doi.rptdesign'}">
                            <mvc:option value="0"><mvc:message code="statistic.All_Mailinglists"/></mvc:option>
                        </c:if>
                        <mvc:options items="${mailinglists}" itemValue="id" itemLabel="shortname"/>
                    </mvc:select>
                </div>
                <c:if test="${form.reportName ne 'recipient_doi.rptdesign'}">
                    <div class="col">
                        <label class="form-label"><mvc:message code="Target"/></label>
                        <mvc:select path="targetId" cssClass="form-control js-select">
                            <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                            <mvc:options items="${targetlist}" itemValue="id" itemLabel="targetName"/>
                        </mvc:select>
                    </div>
                </c:if>

                <c:if test="${form.reportName eq 'recipient_progress.rptdesign'
                           or form.reportName eq 'recipient_optouts.rptdesign'
                           or form.reportName eq 'recipient_optins.rptdesign'}">
                    <div class="col">
                        <label class="form-label"><mvc:message code="mediatype"/></label>
                        <mvc:select path="mediaType" cssClass="form-control">
                            <c:forEach var="mt" items="${mediatypes}">
                                <mvc:option value="${mt.mediaCode}"><mvc:message code="mailing.MediaType.${mt.mediaCode}"/></mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
    <div id="overview-tile" class="tile" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="report.mailing.statistics.select"/></h1>
            <div class="tile-title-controls gap-3">
                <mvc:select path="reportName" cssClass="form-control" id="selectReportName" data-action="type-change" data-select-options="dropdownAutoWidth: true, width: 'auto'">
                    <mvc:option value="recipient_progress.rptdesign"><mvc:message code="statistics.progress"/></mvc:option>
                    <mvc:option value="recipient_status.rptdesign"><mvc:message code="Status"/></mvc:option>
                    <mvc:option value="recipient_mailtype.rptdesign"><mvc:message code="Mailtype"/></mvc:option>
                    <mvc:option value="remarks"><mvc:message code="recipient.Remark"/></mvc:option>
                    <%@include file="recipient-stat-extended-options.jspf"%>
                </mvc:select>

                <c:if test="${form.reportName eq 'recipient_progress.rptdesign'
                          or form.reportName eq 'recipient_optouts.rptdesign'
                          or form.reportName eq 'recipient_optins.rptdesign'}">
                    <mvc:select path="dateSelectMode" cssClass="form-control"
                                data-action="change-period"
                                data-select-options="dropdownAutoWidth: true, width: 'auto'">
                        <mvc:option value="${LAST_WEEK}"><mvc:message code="Week"/></mvc:option>
                        <mvc:option value="${SELECT_MONTH}"><mvc:message code="Month"/></mvc:option>
                        <mvc:option value="${SELECT_PERIOD}"><mvc:message code="statistics.dateRange"/></mvc:option>
                    </mvc:select>
               </c:if>

                <c:if test="${form.reportName eq 'recipient_progress.rptdesign'
                           or form.reportName eq 'recipient_doi.rptdesign'
                           or form.reportName eq 'recipient_optins.rptdesign'
                           or form.reportName eq 'recipient_optouts.rptdesign'}">

                    <c:if test="${form.dateSelectMode == SELECT_PERIOD}">
                        <div class="d-flex gap-2" data-date-range>
                            <div class="d-flex gap-3 align-items-center">
                                <label class="form-label mb-0" for="startDate"><mvc:message code="From"/></label>
                                <div class="date-picker-container">
                                    <mvc:text path="startDate.date" id="startDate"
                                              data-value="${form.startDate.date}"
                                              data-field-validator="max-length-validator" data-validator-options="max: 10"
                                              cssClass="form-control js-datepicker"/>
                                </div>
                            </div>
                            <div class="d-flex gap-3 align-items-center">
                                <label class="form-label mb-0" for="endDate"><mvc:message code="default.to"/></label>
                                <div class="date-picker-container">
                                    <mvc:text path="endDate.date" id="endDate"
                                              data-value="${form.endDate.date}"
                                              data-field-validator="max-length-validator" data-validator-options="max: 10"
                                              cssClass="form-control js-datepicker"/>
                                </div>
                            </div>
                        </div>
                    </c:if>

                    <c:if test="${form.dateSelectMode == SELECT_MONTH}">
                        <div class="d-flex gap-3">
                            <mvc:select path="month" cssClass="form-control">
                                <c:forEach items="${monthlist}" var="month">
                                    <mvc:option value="${month[0]}"><mvc:message code="${month[1]}"/></mvc:option>
                                </c:forEach>
                            </mvc:select>
                            <mvc:select path="year" cssClass="form-control">
                                <mvc:options items="${yearlist}" />
                            </mvc:select>
                        </div>
                    </c:if>
                </c:if>
            </div>
        </div>

        <div class="tile-body p-2 js-scrollable" style="overflow-y: auto !important;">
            <c:choose>
                <c:when test="${form.reportName eq 'remarks'}">
                    <%@include file="remarks-stat.jspf"%>
                </c:when>
                <c:otherwise>
                    <iframe src="${birtStatisticUrlWithoutFormat}&__format=html" border="0" scrolling="auto" style="width: 100%; height: 500px" frameborder="0">
                        Your Browser does not support IFRAMEs, please update!
                    </iframe>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</mvc:form>
