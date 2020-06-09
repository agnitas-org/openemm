<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.birtstatistics.DateMode" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc"%>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags"   prefix="agn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>

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

<mvc:form servletRelativeAction="/statistics/recipient/view.action" method="post" modelAttribute="form">
    <mvc:hidden path="dateSelectMode"/>
    <div class="tile">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#tile-statisticsRecipient">
                <i class="tile-toggle icon icon-angle-down"></i>
                <mvc:message code="report.mailing.filter"/>
            </a>
            <ul class="tile-header-actions">
                <li>
                    <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                        <i class="icon icon-refresh"></i>
                        <mvc:message code="button.Refresh"/>
                    </button>
                </li>
            </ul>
        </div>
        <div id="tile-statisticsRecipient" class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="Mailinglist"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="mailingListId" cssClass="form-control js-select">
                        <mvc:option value="0"><mvc:message code="statistic.All_Mailinglists"/></mvc:option>
                        <mvc:options items="${mailinglists}" itemValue="id" itemLabel="shortname"/>
                    </mvc:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="Target"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="targetId" cssClass="form-control js-select">
                        <mvc:option value="0"><mvc:message code="statistic.all_subscribers"/></mvc:option>
                        <mvc:options items="${targetlist}" itemValue="id" itemLabel="targetName"/>
                    </mvc:select>
                </div>
            </div>
            <c:if test="${form.reportName eq 'recipient_progress.rptdesign'}">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><mvc:message code="mediatype"/></label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:select path="mediaType" cssClass="form-control select2-offscreen">
                            <c:forEach var="mt" items="${mediatypes}">
                                <mvc:option value="${mt.mediaCode}"><mvc:message code="mailing.MediaType.${mt.mediaCode}"/></mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
    <div class="tile">
        <div class="tile-header">
            <label for="selectReportName" class="headline">
                <mvc:message code="report.mailing.statistics.select"/>
            </label>
            <div class="col-md-3 pull-left" style="margin: 0 20px 0 0;">
                <mvc:select path="reportName" data-form-submit="" cssClass="form-control" id="selectReportName">
                    <mvc:option value="recipient_progress.rptdesign"><mvc:message code="statistics.progress"/></mvc:option>
                    <mvc:option value="recipient_status.rptdesign"><mvc:message code="Status"/></mvc:option>
                    <mvc:option value="recipient_mailtype.rptdesign"><mvc:message code="Mailtype"/></mvc:option>
                </mvc:select>
            </div>

            <c:if test="${form.reportName eq 'recipient_progress.rptdesign'}">
                <ul class="tile-header-nav">
                    <li class="${form.dateSelectMode == LAST_WEEK ? 'active' : ''}">
                        <a href="#" data-form-set="dateSelectMode:LAST_WEEK" data-form-submit>
                            <mvc:message code="Week"/>
                        </a>
                    </li>
                    <li class="${form.dateSelectMode == SELECT_MONTH ? 'active' : ''}">
                        <a href="#" data-form-set="dateSelectMode:SELECT_MONTH" data-form-submit>
                            <mvc:message code="Month"/>
                        </a>
                    </li>
                    <li class="${form.dateSelectMode == SELECT_PERIOD ? 'active' : ''}">
                        <a href="#" data-form-set="dateSelectMode:SELECT_PERIOD" data-form-submit>
                            <mvc:message code="statistics.dateRange"/>
                        </a>
                    </li>
                </ul>
            </c:if>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a class="dropdown-toggle" href="#" data-toggle="dropdown">
                        <i class="icon icon-cloud-download"></i>
                        <span class="text"><mvc:message code="Export"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><mvc:message code="statistics.exportFormats"/></li>
                        <li>
                            <a href="${birtStatisticUrlWithoutFormat}&__format=csv" tabindex="-1" data-prevent-load="">
                                <i class="icon icon-file-excel-o"></i> <mvc:message code='export.message.csv'/>
                            </a>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>


        <div class="tile-content">

            <c:if test="${form.reportName eq 'recipient_progress.rptdesign'}">

                <c:if test="${form.dateSelectMode == SELECT_PERIOD}">
                    <div class="tile-controls">
                        <div class="controls controls-left">

                            <div class="date-range-controls">
                                <label class="control" for="startDate">
                                    <mvc:message code="From"/>
                                </label>

                                <div class="control">
                                    <div class="input-group">
                                        <div class="input-group-controls">

                                            <mvc:text path="startDate.date" id="startDate"
                                              data-value="${form.startDate.date}"
                                              data-field-validator="max-length-validator" data-validator-options="max: 10"
                                              data-datepicker-options="format: '${fn:toLowerCase(localeDatePattern)}',
                                                                        formatSubmit: '${fn:toLowerCase(localeDatePattern)}'"
                                              cssClass="form-control datepicker-input js-datepicker js-datepicker-period-start"/>
                                        </div>
                                        <div class="input-group-btn">
                                            <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                                <i class="icon icon-calendar-o"></i>
                                            </button>
                                        </div>
                                    </div>
                                </div>


                                <label class="control" for="endDate">
                                    <mvc:message code="default.to"/>
                                </label>

                                <div class="control">
                                    <div class="input-group">
                                        <div class="input-group-controls">
                                            <mvc:text path="endDate.date" id="endDate"
                                              data-value="${form.endDate.date}"
                                              data-field-validator="max-length-validator" data-validator-options="max: 10"
                                              data-datepicker-options="format: '${fn:toLowerCase(localeDatePattern)}',
                                                                        formatSubmit: '${fn:toLowerCase(localeDatePattern)}'"
                                              cssClass="form-control datepicker-input js-datepicker js-datepicker-period-end"/>
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
                            <button class="btn btn-primary btn-regular pull-right" type="button" data-form-submit>
                                <i class="icon icon-refresh"></i>
                                <mvc:message code="button.Refresh"/>
                            </button>
                        </div>

                    </div>
                </c:if>

                <c:if test="${form.dateSelectMode == SELECT_MONTH}">
                    <div class="tile-controls">

                        <div class="controls controls-left">
                            <div class="control">
                                <mvc:select path="month" cssClass="form-control select2-offscreen">
                                    <c:forEach items="${monthlist}" var="month">
                                        <mvc:option value="${month[0]}"><mvc:message code="${month[1]}"/></mvc:option>
                                    </c:forEach>
                                </mvc:select>
                            </div>
                            <div class="control">
                                <mvc:select path="year" cssClass="form-control select2-offscreen">
                                    <mvc:options items="${yearlist}" />
                                </mvc:select>
                            </div>
                        </div>

                        <div class="controls controls-right">
                            <div class="control">
                                <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                                    <i class="icon icon-refresh"></i>
                                    <mvc:message code="button.Refresh"/>
                                </button>
                            </div>
                        </div>
                    </div>

                </c:if>

            </c:if>

            <iframe src="${birtStatisticUrlWithoutFormat}&__format=html" border="0" scrolling="auto" style="width: 100%; height: 500px" frameborder="0">
                Your Browser does not support IFRAMEs, please update!
            </iframe>
        </div>
    </div>
</mvc:form>
