<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb"  errorPage="/error.do" %>
<%@ page import="org.agnitas.dao.MailingStatus" %>
<%@ page import="org.agnitas.web.ExportWizardAction" %>
<%@ page import="org.agnitas.web.ProfileImportAction" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles" %>

<%--@elvariable id="dashboardForm" type="com.agnitas.emm.core.dashboard.form.DashboardForm"--%>
<%--@elvariable id="mailinglist" type="org.agnitas.beans.impl.PaginatedListImpl<java.util.Map<java.lang.String, java.lang.Object>"--%>
<%--@elvariable id="worldmailinglist" type="java.utils.List<java.util.Map<java.lang.String, java.lang.Object>"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<c:set var="ACTION_IMPORT_RECIPIENT" value="<%= ProfileImportAction.ACTION_START %>" scope="request"/>
<c:set var="ACTION_EXPORT_RECIPIENT" value="<%= ExportWizardAction.ACTION_LIST %>" scope="request"/>

<div data-controller="dashboard">
    <div class="row" data-equalizer="max" data-equalizer-max="400">

        <emm:ShowByPermission token="mailing.show">

            <%-- if stats are displayed column should be half-width by default and respond to view settings --%>
            <emm:ShowByPermission token="stats.mailing">
                <div class="col-md-6" data-view-split="col-md-6" data-view-block="col-xs-12" data-view-hidden="col-xs-12">
            </emm:ShowByPermission>

            <%-- if stats are hidden column should be full-width--%>
            <emm:HideByPermission token="stats.mailing">
                <div class="col-md-12">
            </emm:HideByPermission>

                <div class="tile">
                    <div class="tile-header">
                        <h2 class="headline">
                            <mvc:message code="dashboard.mailings.header"/>
                        </h2>

                        <ul class="tile-header-nav">
                            <li class="active">
                                <a href="#" data-toggle-tab="#dashboard-mailingsList"><mvc:message code="default.list"/></a>
                            </li>
                            <li>
                                <a href="#" data-toggle-tab="#dashboard-mailingsPreview"><mvc:message code="default.Preview"/></a>
                            </li>
                        </ul>

                        <emm:ShowByPermission token="mailing.change">
                                <c:url var="mailingCreateLink" value="/mailing/create.action"/>
                            <ul class="tile-header-actions">
                                <li>
                                    <a href="${mailingCreateLink}" class="btn btn-primary btn-regular">
                                        <i class="icon icon-plus"></i>
                                        <span class="text">
                                            <mvc:message code="New"/>
                                        </span>
                                    </a>
                                </li>
                            </ul>
                        </emm:ShowByPermission>
                    </div>

                    <div class="tile-content" data-equalizer-watch>
                        <div id="dashboard-mailingsList">
                            <ul class="link-list">
                                <c:forEach var="mailing" items="${mailinglist.list}" begin="0" step="1">
                                    <li>
                                        <c:url var="mailingLink" value="/mailing/${mailing.mailingid}/settings.action"/>
                                        <c:if test="${mailing.workstatus == 'mailing.status.sent' or mailing.workstatus == 'mailing.status.norecipients'}">
                                            <emm:ShowByPermission token="stats.mailing">
                                                <c:url var="mailingLink" value="/statistics/mailing/${mailing.mailingid}/view.action"/>
                                            </emm:ShowByPermission>
                                        </c:if>
                                        <a href="${mailingLink}" class="link-list-item recently-used-mailing">
                                            <div class="recently-used-mailing-info">
                                                <p class="headline">
                                                    <c:set var="mailingWorkStatus">
                                                        <c:if test="${mailing.workstatus ne null}">
                                                            <mvc:message code="${mailing.workstatus}"/>
                                                        </c:if>
                                                    </c:set>
                                                    <span class="mailing-badge ${mailing.workstatus}" data-tooltip="${mailingWorkStatus}"></span>
                                                    ${mailing.shortname}
                                                </p>
                                                <p class="description">
                                                    <span data-tooltip="<mvc:message code='birt.mailinglist'/>">
                                                        <i class="icon icon-list-ul"></i>
                                                        <span class="text">${mailing.mailinglist}</span>
                                                    </span>

                                                    <span data-tooltip="<mvc:message code='default.changeDate'/>">
                                                        <i class="icon icon-pencil"></i>
                                                        <span class="text">
                                                            <emm:formatDate value="${mailing.changedate}" pattern="${adminDateFormat}"/>
                                                        </span>
                                                    </span>

                                                    <c:if test="${mailing.senddate ne null}">
                                                        <span data-tooltip="<mvc:message code='mailing.senddate'/>">
                                                            <i class="icon icon-calendar-o"></i>
                                                            <strong>
                                                                <emm:formatDate value="${mailing.senddate}" pattern="${adminDateFormat}"/>
                                                            </strong>
                                                        </span>
                                                    </c:if>

                                                    <c:if test="${mailing.usedInCM}">
                                                        <button class="text" data-help="help_${helplanguage}/mailing/overview/WorkflowEditorMailingOverviewMsg.xml" tabindex="-1" type="button">
                                                            <i class="icon icon-linkage-campaignmanager"></i>&nbsp;<strong><mvc:message code="campaign.manager.icon"/></strong>
                                                        </button>
                                                    </c:if>
                                                </p>
                                            </div>
                                            <div class="btn-group recently-used-mailing-btn-group" data-mailing-id="${mailing.mailingid}">
                                                <emm:ShowByPermission token="mailing.change">
                                                    <button class="btn btn-regular" data-action="copy-recent-mailing" data-tooltip="<mvc:message code="button.Copy"/>">
                                                        <i class="icon icon-copy"></i>
                                                    </button>
                                                </emm:ShowByPermission>
                                                <button class="btn btn-regular" data-action="edit-recent-mailing" data-tooltip="<mvc:message code="mailing.MailingEdit"/>">
                                                    <i class="icon icon-pencil"></i>
                                                </button>
                                                <emm:ShowByPermission token="stats.mailing">
                                                    <button class="btn btn-regular" data-action="open-recent-mailing-statistic" data-tooltip="<mvc:message code="mailing.statistics"/>">
                                                        <i class="icon icon-bar-chart-o"></i>
                                                    </button>
                                                </emm:ShowByPermission>
                                                <emm:ShowByPermission token="mailing.delete">
                                                    <button class="btn btn-regular" data-action="delete-recent-mailing" data-tooltip="<mvc:message code="mailing.MailingDelete"/>">
                                                        <i class="icon icon-trash-o"></i>
                                                    </button>
                                                </emm:ShowByPermission>
                                            </div>
                                        </a>
                                    </li>
                                </c:forEach>
                            </ul>
                        </div>
                        <div class="hidden dashboard-mailing-preview" id="dashboard-mailingsPreview">
                            <div class="row">
                                <c:forEach var="mailing" items="${mailinglist.list}" begin="0" step="1">
                                    <div class="col-xs-4" data-view-split="col-xs-4" data-view-block="col-xs-3" data-view-hidden="col-xs-3">
                                        <c:url var="mailingLink" value="/mailing/${mailing.mailingid}/settings.action"/>
                                        <c:if test="${mailing.workstatus == 'mailing.status.sent' or mailing.workstatus == 'mailing.status.norecipients'}">
                                            <emm:ShowByPermission token="stats.mailing">
                                                <c:url var="mailingStatLink" value="/statistics/mailing/${mailing.mailingid}/view.action"/>
                                            </emm:ShowByPermission>
                                        </c:if>
                                        <a class="mailing-preview-card" href="${mailingLink}">
                                            <c:choose>
                                                <c:when test="${mailing.isOnlyPostType}">
                                                    <c:url var="previewUrl" value="assets/core/images/facelift/post_thumbnail.jpg"/>
                                                </c:when>
                                                <c:when test="${not empty mailing.component and mailing.component ne 0}">
                                                    <c:url var="previewUrl" value="/sc">
                                                        <c:param name="compID" value="${mailing.component}"/>
                                                        <c:param name="cacheKiller" value="${emm:milliseconds()}"/>
                                                    </c:url>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:url var="previewUrl" value="assets/core/images/facelift/no_preview.svg"/>
                                                </c:otherwise>
                                            </c:choose>

                                            <img src="${previewUrl}" class="mailing-preview-card-image" />
                                            <div class="mailing-preview-card-body">
                                                <strong class="headline">
                                                    <c:set var="mailingWorkStatus">
                                                        <c:if test="${mailing.workstatus ne null}">
                                                            <mvc:message code="${mailing.workstatus}"/>
                                                        </c:if>
                                                    </c:set>
                                                    <span class="mailing-badge ${mailing.workstatus}" data-tooltip="${mailingWorkStatus}"></span>
                                                        ${mailing.shortname}
                                                </strong>

                                                <p class="description">
                                                        <span data-tooltip="<mvc:message code='birt.mailinglist'/>">
                                                            <i class="icon icon-list"></i>
                                                            ${mailing.mailinglist}
                                                        </span>

                                                    <c:if test="${mailing.usedInCM}">
                                                        <button class="text" data-help="help_${helplanguage}/mailing/overview/WorkflowEditorMailingOverviewMsg.xml" tabindex="-1" type="button">
                                                            <i class="icon icon-linkage-campaignmanager"></i>&nbsp;<strong><mvc:message code="campaign.manager.icon"/></strong>
                                                        </button>
                                                    </c:if>
                                                </p>
                                            </div>
                                        </a>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
        </emm:ShowByPermission>

        <emm:ShowByPermission token="stats.mailing">

            <%-- if mailings are displayed column should be half-width by default and respond to view settings --%>
            <emm:ShowByPermission token="mailing.show">
                <div class="hidden" data-view-split="col-md-6" data-view-block="col-xs-12" data-view-hidden="hidden">
            </emm:ShowByPermission>

            <%-- if mailings are hidden column should be full-width--%>
            <emm:HideByPermission token="mailing.show">
                <div class="col-md-12">
            </emm:HideByPermission>

                <div class="tile">
                    <div class="tile-header">
                        <h2 class="headline">
                            <mvc:message code="Statistics"/>
                        </h2>

                        <ul class="tile-header-nav">
                            <li>
                                <a href="#" data-toggle-tab="#dashboard-stats-overview"><mvc:message code="default.Overview"/></a>
                            </li>
                            <emm:include page="fragments/dashboard-mia-tab.jsp"/>
                        </ul>
                    </div>

                    <div class="tile-content" data-equalizer-watch>
                        <div id="dashboard-stats-overview">
                            <div class="chart-controls">
                                <c:choose>
                                    <c:when test="${dashboardForm.lastSentMailingId ne 0}">
                                        <c:url var="mailingStatLink" value="/statistics/mailing/:mailing-id:/view.action"/>

                                        <div class="form-group" data-initializer="dashboard-statistics">
                                            <div class="col-sm-8">
                                                <select class="js-select" data-action="statistics-select-mailing">
                                                    <option selected value="${dashboardForm.lastSentMailingId}">
                                                        <mvc:message code="dashboard.statistics.mailing.last_sent"/>
                                                    </option>
                                                    <c:forEach var="mailing" items="${worldmailinglist}" begin="0" step="1">
                                                        <option title="${mailing.shortname}" value="${mailing.mailingid}">${mailing.shortname}</option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <script id="config:dashboard-statistics" type="application/json">
                                                {
                                                  "mailingId": ${dashboardForm.lastSentMailingId},
                                                  "mailingStatisticsLinkPattern": "${mailingStatLink}",
                                                  "urls": {
                                                      "STATISTICS": "<c:url value="/dashboard/statistics.action"/>"
                                                  }
                                                }
                                            </script>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="empty-list well">
                                            <i class="icon icon-info-circle"></i><strong><mvc:message code="dashboard.mailing.noMailings"/></strong>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <c:if test="${dashboardForm.lastSentMailingId != 0}">
                                <div class="js-stat-chart clickable" data-action="open-mailing-statistics"></div>

                                <div class="pie-charts clickable" data-action="open-mailing-statistics">
                                    <div class="pie-chart">
                                        <div class="pie-chart-header"><mvc:message code="birt.report.opens"/></div>
                                        <div class="pie-chart-content">
                                            <div class="js-view-chart"></div>
                                            <div><span class="text">* <mvc:message code="dashboard.statistics.ofSent"/></span></div>
                                        </div>
                                    </div>
                                    <div class="pie-chart">
                                        <div class="pie-chart-header">
                                            <mvc:message code="statistic.clicker"/>
                                        </div>
                                        <div class="pie-chart-content">
                                            <div class="js-click-chart"></div>
                                            <div><span class="text">* <mvc:message code="dashboard.statistics.ofOpened"/></span></div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                        </div>
                        <emm:include page="fragments/dashboard-mia-content.jsp"/>
                    </div>
                </div>
            </div>
        </emm:ShowByPermission>
    </div>

</div>

<div style="width: 100%; clear: both;"></div>

<emm:ShowByPermission token="calendar.show">
    <jsp:include page="../calendar/calendar-view.jsp" />
</emm:ShowByPermission>

<emm:include page="fragments/dashboard-news.jsp"/>
