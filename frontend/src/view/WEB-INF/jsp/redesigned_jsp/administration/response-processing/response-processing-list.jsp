<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="agnDisplay" uri="https://emm.agnitas.de/jsp/jsp/displayTag" %>
<%@ taglib prefix="mvc"        uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"        uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"         uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"          uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="bounceFilterListForm" type="com.agnitas.emm.core.bounce.form.BounceFilterListForm"--%>
<%--@elvariable id="bounceFilterList" type="org.agnitas.beans.impl.PaginatedListImpl"--%>

<mvc:message var="deleteMsg" code="Delete"/>
<c:set var="allowedDeletion" value="${emm:permissionAllowed('mailloop.delete', pageContext.request)}" />

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" cssClass="tile" servletRelativeAction="/administration/bounce/list.action" method="GET"
              modelAttribute="bounceFilterListForm" data-editable-tile="main">

        <div class="tile-body">
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "bounce-filter-overview": {
                        "rows-count": ${bounceFilterListForm.numberOfRows}
                    }
               }
            </script>
            <div class="table-wrapper">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <c:if test="${allowedDeletion}">
                            <div class="bulk-actions hidden">
                                <p class="bulk-actions__selected">
                                    <span><%-- Updates by JS --%></span>
                                    <mvc:message code="default.list.entry.select" />
                                </p>
                                <div class="bulk-actions__controls">
                                    <c:url var="bulkDeleteUrl" value="/administration/bounce/deleteRedesigned.action"/>
                                    <a href="#" class="icon-btn text-danger" data-tooltip="${deleteMsg}" data-form-url='${bulkDeleteUrl}' data-form-confirm>
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </div>
                            </div>
                        </c:if>
                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp">
                            <jsp:param name="filteredEntries" value="${bounceFilterList.fullListSize}"/>
                            <jsp:param name="totalEntries" value="${bounceFilterList.notFilteredFullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <agnDisplay:table class="table table-hover table--borderless js-table"
                                   id="bounceFilter"
                                   requestURI="/administration/bounce/list.action"
                                   name="bounceFilterList"
                                   pagesize="${bounceFilterListForm.numberOfRows}"
                                   excludedParams="*">
                        <%@ include file="../../common/displaytag/displaytag-properties.jspf" %>

                        <c:if test="${allowedDeletion}">
                            <agnDisplay:column title="<input class='form-check-input' type='checkbox' data-bulk-checkboxes />" class="mobile-hidden" headerClass="mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="bulkIds" value="${bounceFilter.id}" data-bulk-checkbox />
                            </agnDisplay:column>
                        </c:if>

                        <agnDisplay:column headerClass="js-table-sort" titleKey="Name" sortable="true" sortProperty="shortname">
                            <span>${bounceFilter.shortName}</span>
                            <a href="<c:url value="/administration/bounce/${bounceFilter.id}/view.action"/>" class="hidden" data-view-row></a>
                        </agnDisplay:column>

                        <agnDisplay:column headerClass="js-table-sort" titleKey="Description" sortable="true" sortProperty="description">
                            <span>${bounceFilter.description}</span>
                        </agnDisplay:column>

                        <agnDisplay:column headerClass="js-table-sort" titleKey="mailloop.filter_adr" sortable="true" sortProperty="filter_address">
                            <span>${bounceFilter.filterEmailWithDefault}</span>
                        </agnDisplay:column>

                        <c:if test="${allowedDeletion}">
                            <agnDisplay:column class="table-actions mobile-hidden" headerClass="fit-content mobile-hidden">
                                <a href="<c:url value="/administration/bounce/deleteRedesigned.action?bulkIds=${bounceFilter.id}"/>" class="icon-btn text-danger js-row-delete" data-tooltip="${deleteMsg}">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </agnDisplay:column>
                        </c:if>
                    </agnDisplay:table>
                </div>
            </div>
        </div>
    </mvc:form>
    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/administration/bounce/search.action"
              modelAttribute="bounceFilterListForm"
              data-toggle-tile="mobile"
              data-form="resource"
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
                    <mvc:message var="nameMsg" code="Name"/>
                    <label class="form-label" for="name">${nameMsg}</label>
                    <mvc:text id="name" path="name" cssClass="form-control" placeholder="${nameMsg}"/>
                </div>
                <div class="col-12">
                    <mvc:message var="filterAddressMsg" code="mailloop.filter_adr"/>
                    <label class="form-label" for="filter-address">${filterAddressMsg}</label>
                    <mvc:text id="filter-address" path="filterAddress" cssClass="form-control" placeholder="${filterAddressMsg}"/>
                </div>
                <div class="col-12">
                    <mvc:message var="descriptionMsg" code="Description"/>
                    <label class="form-label" for="description">${descriptionMsg}</label>
                    <mvc:text id="description" path="description" cssClass="form-control" placeholder="${descriptionMsg}"/>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
