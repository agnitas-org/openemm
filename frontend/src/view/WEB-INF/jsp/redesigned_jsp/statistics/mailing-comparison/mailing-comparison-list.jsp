<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="targetGroupList" type="java.util.List"--%>
<%--@elvariable id="selectionMax" type="java.lang.Integer"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>

<div class="filter-overview hidden" data-editable-view="${agnEditViewKey}">
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

        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview"/></h1>
        </div>

        <div class="tile-body d-flex flex-column gap-3" id="available-mailings">
            <div class="notification-simple notification-simple--lg notification-simple--info">
                <span><mvc:message code="error.NrOfMailings" /></span>
            </div>

            <div class="table-box">
                <div class="table-scrollable">
                    <display:table class="table table-hover table-rounded js-table"
                                   id="mailing"
                                   pagesize="${mailings.pageSize}"
                                   sort="external"
                                   name="mailings"
                                   requestURI="/statistics/mailing/comparison/list.action"
                                   partialList="true"
                                   size="${mailings.fullListSize}"
                                   excludedParams="*">
                        <%@ include file="../../displaytag/displaytag-properties.jspf" %>

                        <display:column class="js-checkable" sortable="false" headerClass="fit-content">
                            <input class="form-check-input" type="checkbox" name="bulkIds" value="${mailing.id}" data-select-restrict/>
                        </display:column>

                        <display:column property="shortname" titleKey="Mailing"
                                        sortable="true" sortProperty="shortname" headerClass="js-table-sort"/>

                        <display:column property="description" titleKey="Description"
                                        sortable="true" sortProperty="description" headerClass="js-table-sort"/>

                        <display:column property="senddate" titleKey="mailing.senddate" format="{0, date, ${adminDateFormat}}"
                                        sortable="true" sortProperty="senddate" headerClass="js-table-sort"/>

                        <display:column class="table-actions hidden" headerClass="hidden">
                            <c:url var="viewMailingStatisticLink" value="/statistics/mailing/${mailing.id}/view.action"/>
                            <a href="${viewMailingStatisticLink}" class="hidden" data-view-row="page"></a>
                        </display:column>
                    </display:table>
                </div>
            </div>
        </div>
    </mvc:form>
    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/statistics/mailing/comparison/search.action"
              modelAttribute="mailingComparisonFilter" data-toggle-tile="mobile" data-form="resource"
              data-resource-selector="#table-tile" data-editable-tile="">
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
