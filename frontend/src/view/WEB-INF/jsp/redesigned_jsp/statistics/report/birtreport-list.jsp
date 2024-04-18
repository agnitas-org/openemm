<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="birtReportsForm" type="org.agnitas.web.forms.PaginationForm"--%>
<%--@elvariable id="reports" type="org.agnitas.beans.impl.PaginatedListImpl<com.agnitas.emm.core.birtreport.bean.ReportEntry>"--%>
<%--@elvariable id="dateFormat" type="java.text.SimpleDateFormat"--%>

<mvc:message var="deleteMessage" code="Delete"/>

<c:set var="allowedDeletion" value="false"/>
<emm:ShowByPermission token="report.birt.delete">
    <c:set var="allowedDeletion" value="true"/>
</emm:ShowByPermission>

<div class="filter-overview hidden" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" servletRelativeAction="/statistics/reports.action" modelAttribute="birtReportsForm" method="post" cssClass="tile" data-editable-tile="main">
        <input type="hidden" name="page" value="${reports.pageNumber}"/>
        <input type="hidden" name="sort" value="${reports.sortCriterion}"/>
        <input type="hidden" name="dir" value="${reports.sortDirection}"/>

        <script type="application/json" data-initializer="web-storage-persist">
            {
                "birt-report-overview": {
                    "rows-count": ${birtReportsForm.numberOfRows}
                }
            }
        </script>

        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>
        </div>
        <div class="tile-body">
            <div class="table-box">
                <div class="table-scrollable">
                    <display:table class="table table-hover table-rounded js-table"
                                   id="report" name="reports" requestURI="/statistics/reports.action"
                                   pagesize="${birtReportsForm.numberOfRows}" excludedParams="*">

                        <%@ include file="../../displaytag/displaytag-properties.jspf" %>

                        <display:column titleKey="Name" property="shortname" maxLength="150" sortable="true" sortProperty="shortname" />
                        <display:column titleKey="Description" property="description" sortable="true" sortProperty="description" />

                        <display:column titleKey="default.changeDate" sortable="true" sortProperty="change_date">
                            <emm:formatDate value="${report.changeDate}" format="${dateFormat}"/>
                        </display:column>

                        <display:column titleKey="mailing.LastDelivery" sortable="true" sortProperty="delivery_date">
                            <emm:formatDate value="${report.deliveryDate}" format="${dateFormat}"/>
                        </display:column>

                        <display:column headerClass="fit-content">
                            <a href='<c:url value="/statistics/report/${report.id}/view.action"/>' class="hidden" data-view-row="page"></a>
                            <c:url var="deleteReportLink" value="/statistics/report/${report.id}/delete.action" />

                            <c:if test="${allowedDeletion}">
                                <a href="${deleteReportLink}" class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="${deleteMessage}">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </c:if>
                        </display:column>
                    </display:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/statistics/reports/search.action" modelAttribute="birtReportsForm"
              data-form="resource" data-resource-selector="#table-tile" data-toggle-tile="mobile" data-editable-tile="">
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up desktop-hidden"></i><mvc:message code="report.mailing.filter"/>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-icon-sm btn-inverse" data-form-clear data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-sync"></i></a>
                <a class="btn btn-icon btn-icon-sm btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
                    <mvc:message var="nameMsg" code="Name" />
                    <label for="filter-name" class="form-label">${nameMsg}</label>
                    <mvc:text id="filter-name" path="name" cssClass="form-control" placeholder="${nameMsg}"/>
                </div>

                <div class="col-12" data-date-range>
                    <label class="form-label" for="filter-changeDate-from"><mvc:message code="default.changeDate"/></label>
                    <div class="date-picker-container mb-1">
                        <mvc:message var="fromMsg" code="From" />
                        <mvc:text id="filter-changeDate-from" path="changeDate.from" placeholder="${fromMsg}" cssClass="form-control js-datepicker"/>
                    </div>
                    <div class="date-picker-container">
                        <mvc:message var="toMsg" code="To" />
                        <mvc:text id="filter-changeDate-to" path="changeDate.to" placeholder="${toMsg}" cssClass="form-control js-datepicker"/>
                    </div>
                </div>

                <div class="col-12" data-date-range>
                    <label class="form-label" for="filter-lastDeliveryDate-from"><mvc:message code="mailing.LastDelivery"/></label>
                    <div class="date-picker-container mb-1">
                        <mvc:text id="filter-lastDeliveryDate-from" path="lastDeliveryDate.from" placeholder="${fromMsg}" cssClass="form-control js-datepicker"/>
                    </div>
                    <div class="date-picker-container">
                        <mvc:text id="filter-lastDeliveryDate-to" path="lastDeliveryDate.to" placeholder="${toMsg}" cssClass="form-control js-datepicker"/>
                    </div>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
