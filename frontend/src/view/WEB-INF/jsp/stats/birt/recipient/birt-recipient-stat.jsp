<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html:form action="/recipient_stats" method="post">
    <html:hidden property="dateSelectMode"/>
    <html:hidden property="reportFormat" value="html"/>
    <html:hidden property="showReportOnly"/>
    <div class="tile">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#tile-statisticsRecipient">
                <i class="tile-toggle icon icon-angle-down"></i>
                <bean:message key="report.mailing.filter"/>
            </a>
            <ul class="tile-header-actions">
                <li>
                    <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                        <i class="icon icon-refresh"></i>
                        <bean:message key="button.Refresh"/>
                    </button>
                </li>
            </ul>
        </div>
        <div id="tile-statisticsRecipient" class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><bean:message key="Mailinglist"/></label>
                </div>
                <div class="col-sm-8">
                    <html:select property="mailingListID" size="1" styleClass="form-control js-select">
                        <html:option value="0"><bean:message key="statistic.All_Mailinglists"/></html:option>
                        <c:forEach var="mailingList" items="${mailinglists}">
                            <html:option value="${mailingList.id}">
                                <c:out value="${mailingList.shortname}"/>
                            </html:option>
                        </c:forEach>
                    </html:select>
                </div>
            </div>
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><bean:message key="Target"/></label>
                </div>
                <div class="col-sm-8">
                    <html:select property="targetID" size="1" styleClass="form-control js-select">
                        <html:option value="0"><bean:message key="statistic.all_subscribers"/></html:option>
                        <c:forEach var="target" items="${targetlist}">
                            <html:option value="${target.id}">
                                <c:out value="${target.targetName}"/>
                            </html:option>
                        </c:forEach>
                    </html:select>
                </div>
            </div>
            <c:if test="${birtStatForm.reportName eq 'recipient_progress.rptdesign'}">
                <c:set var="showLastperiodTabs" value="${birtStatForm.dateSelectMode == 'LAST_MONTH' || birtStatForm.dateSelectMode == 'LAST_FORTNIGHT' || birtStatForm.dateSelectMode == 'LAST_WEEK'}"/>
                <c:set var="showSELECT_MONTH_PERIOD_DATE" value="${birtStatForm.dateSelectMode == 'SELECT_DAY' || birtStatForm.dateSelectMode == 'SELECT_PERIOD' || birtStatForm.dateSelectMode == 'SELECT_MONTH'}"/>
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label"><bean:message key="mediatype"/></label>
                    </div>
                    <div class="col-sm-8">
                        <html:select property="mediaType" size="1" styleClass="form-control select2-offscreen">
                            <c:forEach var="mt" items="${mediatype}">
                                <html:option value="${mt[0]}"><bean:message key="${mt[1]}"/></html:option>
                            </c:forEach>
                        </html:select>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
    <div class="tile">
        <div class="tile-header">
            <label for="selectReportName" class="headline">
                <bean:message key="report.mailing.statistics.select"/>
            </label>
            <div class="col-md-3 pull-left" style="margin: 0 20px 0 0;">
                <agn:agnSelect property="reportName" data-form-submit="" styleClass="form-control" styleId="selectReportName">
                    <html:option value="recipient_progress.rptdesign"><bean:message key="statistics.progress"/></html:option>
                    <html:option value="recipient_status.rptdesign"><bean:message key="Status"/></html:option>
                    <html:option value="recipient_mailtype.rptdesign"><bean:message key="Mailtype"/></html:option>
                </agn:agnSelect>
            </div>

            <c:if test="${birtStatForm.reportName eq 'recipient_progress.rptdesign'}">
                <ul class="tile-header-nav">
                    <li class="${birtStatForm.dateSelectMode == 'LAST_WEEK' ? 'active' : ''}">
                        <a href="#" data-form-set="dateSelectMode:LAST_WEEK" data-form-submit>
                            <bean:message key="Week"/>
                        </a>
                    </li>
                    <li class="${birtStatForm.dateSelectMode == 'SELECT_MONTH' ? 'active' : ''}">
                        <a href="#" data-form-set="dateSelectMode:SELECT_MONTH" data-form-submit>
                            <bean:message key="Month"/>
                        </a>
                    </li>
                    <li class="${birtStatForm.dateSelectMode == 'SELECT_PERIOD' ? 'active' : ''}">
                        <a href="#" data-form-set="dateSelectMode:SELECT_PERIOD" data-form-submit>
                            <bean:message key="statistics.dateRange"/>
                        </a>
                    </li>
                </ul>
            </c:if>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a class="dropdown-toggle" href="#" data-toggle="dropdown">
                        <i class="icon icon-cloud-download"></i>
                        <span class="text"><bean:message key="Export"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><bean:message key="statistics.exportFormats"/></li>
                        <li>
                            <%--<a href="#" data-form-set="reportFormat:pdf, showReportOnly:1" data-form-submit-static>--%>
                                <%--<i class="icon icon-file-pdf-o"></i>--%>
                                <%--<bean:message key='export.message.pdf'/>--%>
                            <%--</a>--%>
                            <a href="#" data-form-set="reportFormat:csv" data-form-submit-static>
                                <i class="icon icon-file-excel-o"></i>
                                <bean:message key='export.message.csv'/>
                            </a>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>


        <div class="tile-content">

            <c:if test="${birtStatForm.reportName eq 'recipient_progress.rptdesign'}">

                <c:if test="${birtStatForm.dateSelectMode == 'SELECT_PERIOD'}">
                    <div class="tile-controls">
                        <div class="controls controls-left">

                            <div class="date-range-controls">
                                <label class="control" for="startDay">
                                    <bean:message key="From"/>
                                </label>

                                <div class="control">
                                    <div class="input-group">
                                        <div class="input-group-controls">
                                            <input id="startDay" type="text" value="${birtStatForm.startDay}" class="form-control datepicker-input js-datepicker" maxlength="10" name="startDay" data-datepicker-options="format: '${fn:toLowerCase(localeDatePattern)}'"/>
                                        </div>
                                        <div class="input-group-btn">
                                            <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                                <i class="icon icon-calendar-o"></i>
                                            </button>
                                        </div>
                                    </div>
                                </div>


                                <label class="control" for="endDay">
                                    <bean:message key="default.to"/>
                                </label>

                                <div class="control">
                                    <div class="input-group">
                                        <div class="input-group-controls">
                                            <input id="endDay" type="text" value="${birtStatForm.endDay}" class="form-control datepicker-input js-datepicker" maxlength="10" name="endDay" data-datepicker-options="format: '${fn:toLowerCase(localeDatePattern)}'"/>
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
                                <bean:message key="button.Refresh"/>
                            </button>
                        </div>

                    </div>
                </c:if>

                <c:if test="${birtStatForm.dateSelectMode == 'SELECT_MONTH'}">
                    <div class="tile-controls">

                        <div class="controls controls-left">
                            <div class="control">
                                <html:select property="month" size="1" styleClass="form-control select2-offscreen">
                                    <c:forEach var="mon" items="${monthlist}">
                                        <html:option value="${mon[0]}"><bean:message key="${mon[1]}"/></html:option>
                                    </c:forEach>
                                </html:select>
                            </div>
                            <div class="control">
                                <html:select property="year" size="1" styleClass="form-control select2-offscreen">
                                    <c:forEach var="yea" items="${yearlist}">
                                        <html:option value="${yea}"><c:out value="${yea}"/></html:option>
                                    </c:forEach>
                                </html:select>
                            </div>
                        </div>

                        <div class="controls controls-right">
                            <div class="control">
                                <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                                    <i class="icon icon-refresh"></i>
                                    <bean:message key="button.Refresh"/>
                                </button>
                            </div>
                        </div>
                    </div>

                </c:if>

            </c:if>

            <c:if test="${empty noDateAvailable}">
                <iframe src="${birtStatForm.reportUrl}" border="0" scrolling="auto" style="width: 100%; height: 500px" frameborder="0">
                    Your Browser does not support IFRAMEs, please update!
                </iframe>
            </c:if>
        </div>
    </div>
</html:form>
