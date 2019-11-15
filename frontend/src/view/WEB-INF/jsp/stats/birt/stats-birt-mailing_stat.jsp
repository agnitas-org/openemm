<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do"%>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common" prefix="emm" %>

<%--@elvariable id="birtStatForm" type="com.agnitas.web.forms.ComBirtStatForm"--%>
<%--@elvariable id="sectorlist" type="java.util.Set<com.agnitas.emm.core.company.enums.Sector>"--%>

<c:set var="isReportCanBeShown" value="true"/>
<c:if test="${birtStatForm.reportName eq 'top_domains.rptdesign' && birtStatForm.everSent eq false}">
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

                <agn:agnForm action="/mailing_stat2" data-form="resource" method="POST">
                    <html:hidden property="reportFormat" value="html"/>
                    <html:hidden property="companyID"/>
                    <html:hidden property="mailingID"/>
                    <html:hidden property="dateSelectMode"/>
                    <html:hidden property="bouncetype" value="BOTH"/>
                    <html:hidden property="showReportOnly" value="0"/>
                    <html:hidden property="urlID"/>
                    <html:hidden property="recipientType" value="ALL_SUBSCRIBERS"/>

                    <div class="tile">
                        <div class="tile-header">
                            <a href="#" class="headline" data-toggle-tile="#tile-statisticsMailing">
                                <i class="tile-toggle icon icon-angle-up"></i>
                                <bean:message key="Targetgroups"/>
                            </a>
                            <ul class="tile-header-actions">
                                <li>
                                    <button class="btn btn-primary btn-regular" type="button" data-form-persist="reportFormat:html,showReportOnly:0" data-form-submit>
                                        <i class="icon icon-refresh"></i>
                                <span class="text">
                                    <bean:message key="button.Refresh"/>
                                </span>
                                    </button>
                                </li>
                            </ul>
                        </div>
                        <div id="tile-statisticsMailing" class="tile-content tile-content-forms">
                            <div class="form-group">
                                <div class="col-sm-4">
                                    <label class="control-label" for="targetGroupSelect">
                                        <bean:message key="Targetgroups"/>
                                    </label>
                                </div>
                                <div class="col-sm-8">
                                    <c:set var="addTargetGroupMessage" scope="page">
                                        <bean:message key="addTargetGroup" />
                                    </c:set>
                                    <agn:agnSelect property="selectedTargets" styleId="targetGroupSelect" styleClass="form-control js-select" multiple="" data-placeholder="${addTargetGroupMessage}">
                                        <c:forEach var="target" items="${targetlist}" varStatus="rowCounter">
                                            <html:option value="${target.id}">${target.targetName}</html:option>
                                        </c:forEach>
                                    </agn:agnSelect >
                                </div>
                            </div>

                            <div class="form-group" data-show-by-select="#selectReportName" data-show-by-select-values="mailing_summary.rptdesign">
                                <div class="col-sm-4">
                                    <label class="control-label" for="showNetto">
                                        <bean:message key="mailing.statistics.show.netto"/>
                                    </label>
                                </div>
                                <div class="col-sm-8">
                                    <label class="toggle">
                                        <agn:agnCheckbox property="showNetto" styleId="showNetto" data-form-persist="reportFormat:html,showReportOnly:0" data-form-submit=""/>
                                        <div class="toggle-control"></div>
                                    </label>
                                </div>
                            </div>

                            <logic:equal name="birtStatForm" property="reportName" value="top_domains.rptdesign">
                                <div class="form-group">
                                    <div class="col-sm-4">
                                        <label class="control-label"><bean:message key="domains.max"/></label>
                                    </div>
                                    <div class="col-sm-8">
                                        <html:select property="maxDomains" styleClass="form-control select2-offscreen">
                                            <html:option value="5">5</html:option>
                                            <html:option value="10">10</html:option>
                                            <html:option value="15">15</html:option>
                                        </html:select>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <div class="col-sm-4">
                                        <label class="control-label"><bean:message key="ToplevelDomains"/></label>
                                    </div>
                                    <div class="col-sm-8">
                                        <html:checkbox property="topLevelDomain"/>
                                    </div>
                                </div>
                            </logic:equal>
                        </div>
                    </div>

                    <div class="tile">

                        <div class="tile-header">
                            <label for="selectReportName" class="headline">
                                <bean:message key="report.mailing.statistics.select"/>
                            </label>
                            <div class="col-md-3 pull-left" style="margin: 0 20px 0 0;">
                                <agn:agnSelect property="reportName" data-form-persist="reportFormat:html,showReportOnly:0"  data-form-submit="" styleClass="form-control" styleId="selectReportName">
                                    <c:forEach items="${reports}" var="report">
                                        <html:option value="${report.key}">${report.value}</html:option>
                                    </c:forEach>
                                </agn:agnSelect>
                            </div>

                            <c:if test="${isReportCanBeShown && (birtStatForm.dateSelectMode ne 'NONE')}">
                                <ul class="tile-header-nav">
                                    <li class="${birtStatForm.dateSelectMode == 'LAST_TENHOURS' ? 'active' : ''}">
                                        <a href="#" data-form-set="dateSelectMode:LAST_TENHOURS,reportName:${birtStatForm.reportName}" data-form-persist="reportFormat:html,showReportOnly:0" data-form-submit>
                                            <bean:message key="TenHours"/>
                                        </a>
                                    </li>
                                    <li class="${birtStatForm.dateSelectMode == 'SELECT_DAY' ? 'active' : ''}">
                                        <a href="#" data-form-set="dateSelectMode:SELECT_DAY,reportName:${birtStatForm.reportName}" data-form-persist="reportFormat:html,showReportOnly:0" data-form-submit>
                                            <bean:message key="Day"/>
                                        </a>
                                    </li>
                                    <li class="${birtStatForm.dateSelectMode == 'SELECT_MONTH' ? 'active' : ''}">
                                        <a href="#" data-form-set="dateSelectMode:SELECT_MONTH,reportName:${birtStatForm.reportName}" data-form-persist="reportFormat:html,showReportOnly:0" data-form-submit>
                                            <bean:message key="Month"/>
                                        </a>
                                    </li>
                                    <li class="${birtStatForm.dateSelectMode == 'SELECT_PERIOD' ? 'active' : ''}">
                                        <a href="#" data-form-set="dateSelectMode:SELECT_PERIOD,reportName:${birtStatForm.reportName}" data-form-persist="reportFormat:html,showReportOnly:0" data-form-submit>
                                            <bean:message key="statistics.dateRange"/>
                                        </a>
                                    </li>
                                </ul>
                            </c:if>

                            <c:if test="${isReportCanBeShown}">
                                <ul class="tile-header-actions">
                                    <li class="dropdown">
                                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                            <i class="icon icon-cloud-download"></i>
                                            <span class="text"><bean:message key="Export"/></span>
                                            <i class="icon icon-caret-down"></i>
                                        </a>
                                        <ul class="dropdown-menu">
                                            <li class="dropdown-header"><bean:message key="statistics.exportFormats"/></li>
                                            <li>
                                                <a href="#" tabindex="-1" data-form-persist="reportFormat:csv,showReportOnly:1" data-prevent-load data-form-submit-static>
                                                    <i class="icon icon-file-excel-o"></i>
                                                    <bean:message key='export.message.csv'/>
                                                </a>
                                                    <%-- Disable an ability to export Top Domains stats as PDF --%>
                                                    <%--<logic:notEqual name="birtStatForm" property="reportName" value="top_domains.rptdesign">--%>
                                                    <%--<a href="#" tabindex="-1" data-form-persist="reportFormat:pdf" data-form-submit-static>--%>
                                                    <%--<i class="icon icon-file-pdf-o"></i>--%>
                                                    <%--<bean:message key='export.message.pdf'/>--%>
                                                    <%--</a>--%>
                                                    <%--</logic:notEqual>--%>
                                            </li>
                                        </ul>
                                    </li>
                                </ul>
                            </c:if>
                        </div>

                        <div class="tile-content" data-form-content>

                            <c:if test="${isReportCanBeShown && (birtStatForm.dateSelectMode ne 'NONE')}">

                                <c:if test="${birtStatForm.dateSelectMode == 'SELECT_DAY'}">

                                    <div class="tile-controls">
                                        <div class="controls">
                                            <div class="control">
                                                <div class="input-group">
                                                    <div class="input-group-controls">
                                                        <input type="text" value="${birtStatForm.selectDay}" class="form-control datepicker-input js-datepicker" name="selectDay"
                                                               data-datepicker-options="format: '${fn:toLowerCase(localDatePattern)}', formatSubmit: '${fn:toLowerCase(localDatePattern)}'"/>
                                                    </div>
                                                    <div class="input-group-btn">
                                                        <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                                            <i class="icon icon-calendar-o"></i>
                                                        </button>
                                                    </div>
                                                    <div class="input-group-btn">
                                                        <button class="btn btn-primary btn-regular pull-right" type="button" data-form-set="reportName:${birtStatForm.reportName}" data-form-persist="reportFormat:html,showReportOnly:0" data-form-submit>
                                                            <i class="icon icon-refresh"></i>
                                                            <span class="text"><bean:message key="button.Refresh"/></span>
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
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
                                                <button class="btn btn-primary btn-regular pull-right" type="button" data-form-set="reportName:${birtStatForm.reportName}" data-form-persist="reportFormat:html,showReportOnly:0" data-form-submit>
                                                    <i class="icon icon-refresh"></i>
                                            <span class="text">
                                                <bean:message key="button.Refresh"/>
                                            </span>
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </c:if>

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
                                                            <input type="text" value="${birtStatForm.startDate_localized}" class="form-control datepicker-input js-datepicker" name="startDate_localized"
                                                                   data-datepicker-options="format: '${fn:toLowerCase(localDatePattern)}', formatSubmit: '${fn:toLowerCase(localDatePattern)}'"/>
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
                                                            <input type="text" value="${birtStatForm.stopDate_localized}" class="form-control datepicker-input js-datepicker" name="stopDate_localized"
                                                                   data-datepicker-options="format: '${fn:toLowerCase(localDatePattern)}', formatSubmit: '${fn:toLowerCase(localDatePattern)}'"/>
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
                                            <button class="btn btn-primary btn-regular pull-right" type="button" data-form-set="reportName:${birtStatForm.reportName}" data-form-persist="reportFormat:html,showReportOnly:0" data-form-submit>
                                                <i class="icon icon-refresh"></i>
                                            <span class="text">
                                                <bean:message key="button.Refresh"/>
                                            </span>
                                            </button>
                                        </div>

                                    </div>

                                </c:if>

                            </c:if>

                            <logic:equal name="birtStatForm" property="reportName" value="top_domains.rptdesign">
                                <c:choose>
                                    <c:when test="${birtStatForm.everSent eq false}">
                                        <c:set var="notificationMessage">
                                            <bean:message key="statistics.topdomains.display.info"/>
                                        </c:set>
                                    </c:when>
                                    <c:when test="${birtStatForm.mailtrackingActive eq false}">
                                        <c:set var="notificationMessage">
                                            <bean:message key="mailtracking.required.topdomains"/>
                                        </c:set>
                                    </c:when>
                                    <c:when test="${birtStatForm.mailtrackingExpired eq false}">
                                        <c:set var="notificationMessage">
                                            <bean:message key="mailtracking.required.topdomains.expired" arg0="${birtStatForm.mailtrackingExpirationDays}"/>
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
                            </logic:equal>

                            <c:if test="${allowBenchmark && birtStatForm.reportName eq 'mailing_benchmark.rptdesign'}">
                                <div class="tile-controls">
                                    <div class="notification notification-info">
                                        <div class="notification-header">
                                            <p class="headline">
                                                <i class="icon icon-state-info"></i>
                                                <span class="text"><bean:message key="statistic.benchmark.performanceBenchmark"/> <bean:message key="statistic.benchmark.available5HoursAfterSend"/></span>
                                            </p>
                                        </div>
                                    </div>
                                </div>

                                <div class="tile-controls">
                                    <div class="form-group">
                                        <div class="col-sm-1">
                                            <label class="control-label" for="sector">
                                                <bean:message key="statistic.benchmark.industry"/>
                                            </label>
                                        </div>
                                        <div class="col-sm-11">
                                            <div class="input-group">
                                                <div class="input-group-controls">
                                                    <html:select property="sector" size="1" styleClass="form-control">
                                                        <c:forEach var="curSector" items="${sectorlist}" begin="1">
                                                            <html:option value="${curSector.id}"><bean:message key="${curSector.messageKey}"/></html:option>
                                                        </c:forEach>
                                                    </html:select>
                                                </div>
                                                <div class="input-group-btn">
                                                    <button class="btn btn-primary btn-regular" type="button" data-form-persist="reportFormat:html,showReportOnly:0" data-form-submit>
                                                        <bean:message key="settings.Update"/>
                                                    </button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>

                            <c:if test="${birtStatForm.reportUrl ne null && isReportCanBeShown}">
                                <iframe src="${birtStatForm.reportUrl}" border="0" scrolling="auto" frameborder="0" style="width: 100%">
                                    Your Browser does not support IFRAMEs, please update!
                                </iframe>
                            </c:if>
                        </div>
                    </div>

                </agn:agnForm>

            </div>
        </div>

    </tiles:put>
</tiles:insert>
