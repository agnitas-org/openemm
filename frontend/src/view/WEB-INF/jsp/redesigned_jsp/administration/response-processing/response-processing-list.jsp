<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="bounceFilterListForm" type="com.agnitas.emm.core.bounce.form.BounceFilterListForm"--%>
<%--@elvariable id="bounceFilterList" type="org.agnitas.beans.impl.PaginatedListImpl"--%>

<c:set var="allowedDeletion" value="false"/>
<emm:ShowByPermission token="mailloop.delete">
    <c:set var="allowedDeletion" value="true"/>
</emm:ShowByPermission>

<div class="filter-overview hidden" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" cssClass="tile" servletRelativeAction="/administration/bounce/list.action" method="GET"
              modelAttribute="bounceFilterListForm" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview"/></h1>
        </div>

        <div class="tile-body">
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "bounce-filter-overview": {
                        "rows-count": ${bounceFilterListForm.numberOfRows}
                    }
               }
            </script>
            <div class="table-box">
                <div class="table-scrollable">
                    <display:table class="table table-hover table-rounded js-table"
                                   id="bounceFilter"
                                   requestURI="/administration/bounce/list.action"
                                   name="bounceFilterList"
                                   pagesize="${bounceFilterListForm.numberOfRows}"
                                   excludedParams="*">
                        <%@ include file="../../displaytag/displaytag-properties.jspf" %>

                        <display:column title="<input class='form-check-input' type='checkbox' data-form-bulk='bulkIds'/>" sortable="false" class="js-checkable mobile-hidden" headerClass="bulk-ids-column js-table-sort mobile-hidden">
                            <input class="form-check-input" type="checkbox" name="bulkIds" value="${bounceFilter.id}"/>
                        </display:column>

                        <display:column headerClass="js-table-sort" property="shortName" titleKey="Name" sortable="true" sortProperty="shortname"/>

                        <display:column headerClass="js-table-sort" property="description" titleKey="Description" sortable="true" sortProperty="description"/>

                        <display:column headerClass="js-table-sort" property="filterEmailWithDefault" titleKey="mailloop.filter_adr" sortable="true" sortProperty="filter_address"/>

                        <display:column class="table-actions mobile-hidden ${allowedDeletion ? '' : 'hidden'}" headerClass="fit-content mobile-hidden ${allowedDeletion ? '' : 'hidden'}" sortable="false">
                            <a href="<c:url value="/administration/bounce/${bounceFilter.id}/view.action"/>" class="hidden" data-view-row></a>

                            <c:if test="${allowedDeletion}">
                                <a href="<c:url value="/administration/bounce/deleteRedesigned.action?bulkIds=${bounceFilter.id}"/>" class="btn btn-icon-sm btn-danger js-row-delete" data-tooltip="<mvc:message code="Delete"/>">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </c:if>
                        </display:column>
                    </display:table>
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
