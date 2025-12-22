<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="targetGroupList" type="java.util.List"--%>
<%--@elvariable id="selectionMax" type="java.lang.Integer"--%>

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

        <div class="tile-body vstack gap-3" id="available-mailings">
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
                    <emm:table var="mailing" modelAttribute="mailings" cssClass="table table-hover table--borderless js-table">
                        <c:if test="${mailings.fullListSize gt 0}">
                            <emm:column>
                                <input class="form-check-input" type="checkbox" name="bulkIds" value="${mailing.id}" data-select-restrict />
                            </emm:column>
                        </c:if>

                        <emm:column titleKey="Mailing"          sortable="true" property="shortname" />
                        <emm:column titleKey="Description"      sortable="true" property="description" />
                        <emm:column titleKey="mailing.senddate" sortable="true" property="senddate" />

                        <emm:column cssClass="hidden" headerClass="hidden">
                            <c:url var="viewMailingStatisticLink" value="/statistics/mailing/${mailing.id}/view.action"/>
                            <a href="${viewMailingStatisticLink}" class="hidden" data-view-row="page"></a>
                        </emm:column>
                    </emm:table>
                </div>
            </div>
        </div>
    </mvc:form>
    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/statistics/mailing/comparison/search.action"
              modelAttribute="mailingComparisonFilter" data-toggle-tile="" data-form="resource"
              data-resource-selector="#table-tile" data-editable-tile="">
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-secondary" data-form-clear data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body vstack gap-3 js-scrollable">
            <div>
                <mvc:message var="mailingMsg" code="Mailing"/>
                <label class="form-label" for="mailing-filter">${mailingMsg}</label>
                <mvc:text id="mailing-filter" path="mailing" cssClass="form-control" placeholder="${mailingMsg}"/>
            </div>
            <div>
                <mvc:message var="descriptionMsg" code="Description"/>
                <label class="form-label" for="description-filter">${descriptionMsg}</label>
                <mvc:text id="description-filter" path="description" cssClass="form-control" placeholder="${descriptionMsg}"/>
            </div>
            <div>
                <label class="form-label" for="send-date-from"><mvc:message code="mailing.senddate"/></label>
                <mvc:dateRange id="send-date" path="sendDate" options="maxDate: 0" />
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
