<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="agnDisplay" uri="https://emm.agnitas.de/jsp/jsp/displayTag" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="targetGroupList" type="java.util.List"--%>
<%--@elvariable id="selectionMax" type="java.lang.Integer"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
    <mvc:form id='table-tile' cssClass="tile" servletRelativeAction="/statistics/mailing/comparison/list.action"
              modelAttribute="mailingComparisonFilter"
              data-resource-selector="#available-mailings" data-editable-tile="main">

        <script type="application/json" data-initializer="web-storage-persist">
            {
                "mailing-comparison-overview": {
                    "rows-count": ${mailingComparisonFilter.numberOfRows}
                }
            }
        </script>

        <div class="tile-body d-flex flex-column gap-3" id="available-mailings">
            <div class="notification-simple notification-simple--lg notification-simple--info">
                <span><mvc:message code="error.NrOfMailings" /></span>
            </div>

            <div class="table-wrapper">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp">
                            <jsp:param name="filteredEntries" value="${mailings.fullListSize}"/>
                            <jsp:param name="totalEntries" value="${mailings.notFilteredFullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <agnDisplay:table class="table table-hover table--borderless js-table"
                                   id="mailing"
                                   pagesize="${mailings.pageSize}"
                                   sort="external"
                                   name="mailings"
                                   requestURI="/statistics/mailing/comparison/list.action"
                                   partialList="true"
                                   size="${mailings.fullListSize}"
                                   excludedParams="*">
                        <%@ include file="../../common/displaytag/displaytag-properties.jspf" %>

                        <agnDisplay:column headerClass="fit-content">
                            <input class="form-check-input" type="checkbox" name="bulkIds" value="${mailing.id}" data-select-restrict/>
                        </agnDisplay:column>

                        <agnDisplay:column titleKey="Mailing" sortable="true" sortProperty="shortname" headerClass="js-table-sort">
                            <span>${mailing.shortname}</span>
                        </agnDisplay:column>

                        <agnDisplay:column titleKey="Description" sortable="true" sortProperty="description" headerClass="js-table-sort">
                            <span>${mailing.description}</span>
                        </agnDisplay:column>

                        <agnDisplay:column property="senddate" titleKey="mailing.senddate" format="{0, date, ${adminDateFormat}}"
                                        sortable="true" sortProperty="senddate" headerClass="js-table-sort"/>

                        <agnDisplay:column class="table-actions hidden" headerClass="hidden">
                            <c:url var="viewMailingStatisticLink" value="/statistics/mailing/${mailing.id}/view.action"/>
                            <a href="${viewMailingStatisticLink}" class="hidden" data-view-row="page"></a>
                        </agnDisplay:column>
                    </agnDisplay:table>
                </div>
            </div>
        </div>
    </mvc:form>
    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/statistics/mailing/comparison/search.action"
              modelAttribute="mailingComparisonFilter" data-toggle-tile="mobile" data-form="resource"
              data-resource-selector="#table-tile" data-editable-tile="">
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-inverse" data-form-clear data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
                    <mvc:message var="mailingMsg" code="Mailing"/>
                    <label class="form-label" for="mailing-filter">${mailingMsg}</label>
                    <mvc:text id="mailing-filter" path="mailing" cssClass="form-control" placeholder="${mailingMsg}"/>
                </div>
                <div class="col-12">
                    <mvc:message var="descriptionMsg" code="Description"/>
                    <label class="form-label" for="description-filter">${descriptionMsg}</label>
                    <mvc:text id="description-filter" path="description" cssClass="form-control" placeholder="${descriptionMsg}"/>
                </div>
                <div class="col-12" data-date-range>
                    <label class="form-label" for="send-date-from-filter"><mvc:message code="mailing.senddate"/></label>
                    <div class="date-picker-container mb-1">
                        <mvc:message var="fromMsg" code="From" />
                        <mvc:text id="send-date-from-filter" path="sendDate.from" placeholder="${fromMsg}" cssClass="form-control js-datepicker" />
                    </div>
                    <div class="date-picker-container">
                        <mvc:message var="toMsg" code="To" />
                        <mvc:text id="send-date-to-filter" path="sendDate.to" placeholder="${toMsg}" cssClass="form-control js-datepicker" />
                    </div>
                </div>
            </div>
        </div>
    </mvc:form>
</div>

<script>
    (function(){
      var MAX = ${selectionMax};
      var SELECTOR = '[data-select-restrict]';
      AGN.Lib.Action.new({'change': SELECTOR}, function() {
		  var selected = $(SELECTOR + ':checked').length;
		  if (selected >= MAX) {
		    $(SELECTOR + ':not(:checked)').attr('disabled', true);
          } else {
		    $(SELECTOR).removeAttr('disabled');
          }
		});
  })();
</script>
