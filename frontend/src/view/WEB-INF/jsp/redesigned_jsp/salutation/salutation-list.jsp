<%@ page import="com.agnitas.util.importvalues.Gender" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="salutationOverviewFilter" type="com.agnitas.emm.core.salutation.form.SalutationOverviewFilter"--%>
<%--@elvariable id="salutation" type="com.agnitas.beans.Title"--%>
<%--@elvariable id="salutations" type="com.agnitas.beans.impl.PaginatedListImpl<com.agnitas.beans.Title>"--%>
<%--@elvariable id="agnEditViewKey" type="java.lang.String"--%>

<c:set var="MALE_GENDER" value="<%= Gender.MALE.getStorageValue() %>"/>
<c:set var="FEMALE_GENDER" value="<%= Gender.FEMALE.getStorageValue() %>"/>
<c:set var="UNKNOWN_GENDER" value="<%= Gender.UNKNOWN.getStorageValue() %>"/>
<c:set var="PRAXIS_GENDER" value="<%= Gender.PRAXIS.getStorageValue() %>"/>
<c:set var="COMPANY_GENDER" value="<%= Gender.COMPANY.getStorageValue() %>"/>

<mvc:message var="deleteMsg" code="salutation.SalutationDelete"/>
<c:set var="deleteAllowed" value="${emm:permissionAllowed('salutation.delete', pageContext.request)}" />
<c:url var="bulkDeleteUrl" value="/salutation/deleteRedesigned.action"/>

<div class="filter-overview" data-editable-tile="main">
    <mvc:form id="table-tile" cssClass="tile" servletRelativeAction="/salutation/listRedesigned.action" modelAttribute="salutationOverviewFilter" method="GET">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "salutation-overview": {
                    "rows-count": ${salutationOverviewFilter.numberOfRows}
                }
            }
        </script>

        <div class="tile-body">
            <div class="table-wrapper">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <c:if test="${deleteAllowed}">
                            <div class="bulk-actions hidden">
                                <p class="bulk-actions__selected">
                                    <span><%-- Updates by JS --%></span>
                                    <mvc:message code="default.list.entry.select" />
                                </p>
                                <div class="bulk-actions__controls">
                                    <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${deleteMsg}" data-form-url='${bulkDeleteUrl}' data-form-confirm>
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </div>
                            </div>
                        </c:if>
                        <%@include file="../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../common/table/entries-label.jsp">
                            <jsp:param name="totalEntries" value="${salutations.fullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <emm:table var="salutation" modelAttribute="salutations" cssClass="table table-hover table--borderless js-table">

                        <c:set var="checkboxSelectAll">
                            <input class="form-check-input" type="checkbox" data-bulk-checkboxes />
                        </c:set>

                        <c:set var="salutationDeleteAllowed" value="${salutation.companyID ne 0 and deleteAllowed}" />

                        <c:if test="${deleteAllowed}">
                            <emm:column title="${checkboxSelectAll}" cssClass="mobile-hidden" headerClass="mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="bulkIds" value="${salutation.id}" data-bulk-checkbox ${salutationDeleteAllowed ? '' : 'disabled'} />
                            </emm:column>
                        </c:if>

                        <emm:column sortProperty="description" titleKey="Name" sortable="true">
                            <span>${salutation.description} (${salutation.id})</span>
                        </emm:column>

                        <c:forEach var="gender" items="${salutation.titleGender}">
                            <c:set var="genderTitle"><mvc:message code="Gender"/> ${gender.key}</c:set>
                            <emm:column sortProperty="gender${gender.key}" title="${genderTitle}" sortable="true">
                                <span>${gender.value}</span>
                            </emm:column>
                        </c:forEach>

                        <emm:column cssClass="table-actions">
                            <div class="justify-content-start">
                                <a href="#" class="icon-btn icon-btn--info" data-copyable data-copyable-value="[agnTITLE type='${salutation.id}']" data-tooltip="<mvc:message code='button.Copy'/>">
                                    <i class="icon icon-copy"></i>
                                </a>

                                <a href='<c:url value="/salutation/${salutation.id}/view.action"/>' class="hidden" data-view-row></a>

                                <c:if test="${salutationDeleteAllowed}">
                                    <a href='${bulkDeleteUrl}?bulkIds=${salutation.id}' class="icon-btn icon-btn--danger js-row-delete" data-tooltip="${deleteMsg}">
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </c:if>
                            </div>
                        </emm:column>
                    </emm:table>
                </div>
            </div>
        </div>
    </mvc:form>

    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/salutation/search.action" modelAttribute="salutationOverviewFilter"
              data-toggle-tile=""
              data-form="resource"
              data-resource-selector="#table-tile"
              data-editable-tile="">
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-secondary" data-form-clear="#filter-tile" data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body form-column js-scrollable">
            <div>
                <label class="form-label" for="name-filter"><mvc:message code="Name" /></label>
                <mvc:text id="name-filter" path="name" cssClass="form-control" />
            </div>
            <div>
                <label class="form-label" for="id-filter"><mvc:message code="MailinglistID" /></label>
                <mvc:text id="id-filter" path="salutationId" cssClass="form-control" />
            </div>

            <c:forEach var="gender" items="<%= Gender.values() %>">
                <c:set var="genderVal" value="${gender.storageValue}" />
                <div>
                    <label class="form-label" for="gender${genderVal}-filter"><mvc:message code="Gender"/>&nbsp;${genderVal}</label>
                    <mvc:text id="gender${genderVal}-filter" path="gender${genderVal}" cssClass="form-control" />
                </div>
            </c:forEach>
        </div>
    </mvc:form>
</div>
