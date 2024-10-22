<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="agnDisplay" uri="https://emm.agnitas.de/jsp/jsp/displayTag" %>
<%@ taglib prefix="emm"        uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"        uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"          uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="export" type="org.agnitas.beans.ExportPredef"--%>
<%--@elvariable id="exports" type="java.util.List<org.agnitas.beans.ExportPredef>"--%>
<%--@elvariable id="form" type="org.agnitas.web.forms.PaginationForm"--%>

<mvc:message var="exportDeleteMessage" code="export.ExportDelete" />
<c:set var="deleteAllowed" value="${emm:permissionAllowed('export.delete', pageContext.request)}" />

<div class="tiles-container">
    <mvc:form servletRelativeAction="/export/list.action" modelAttribute="form" cssClass="tile" method="GET">
        <script type="application/json" data-initializer="web-storage-persist">
            {
                "export-profile-overview": {
                    "rows-count": ${form.numberOfRows}
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
                                    <c:url var="bulkDeleteUrl" value="/export/delete.action"/>
                                    <a href="#" class="icon-btn text-danger" data-tooltip="${exportDeleteMessage}" data-form-url='${bulkDeleteUrl}' data-form-confirm>
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </div>
                            </div>
                        </c:if>
                        <%@include file="../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../common/table/entries-label.jsp">
                            <jsp:param name="totalEntries" value="${exports.fullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <agnDisplay:table id="export" class="table table-hover table--borderless js-table" name="exports"
                                   requestURI="/export/list.action" excludedParams="*" pagesize="${form.numberOfRows}">

                        <%@ include file="../common/displaytag/displaytag-properties.jspf" %>

                        <c:if test="${deleteAllowed}">
                            <c:set var="checkboxSelectAll">
                                <input class="form-check-input" type="checkbox" data-bulk-checkboxes />
                            </c:set>

                            <agnDisplay:column title="${checkboxSelectAll}" class="mobile-hidden" headerClass="mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="bulkIds" value="${export.id}" data-bulk-checkbox />
                            </agnDisplay:column>
                        </c:if>

                        <agnDisplay:column headerClass="js-table-sort" sortable="true" titleKey="default.Name" sortProperty="shortname">
                            <span>${export.shortname}</span>
                        </agnDisplay:column>
                        <agnDisplay:column headerClass="js-table-sort" sortable="true" titleKey="Description" sortProperty="description">
                            <span>${export.description}</span>
                        </agnDisplay:column>

                        <agnDisplay:column headerClass="fit-content">
                            <a href='<c:url value="/export/${export.id}/view.action"/>' class="hidden" data-view-row="page"></a>

                            <c:if test="${deleteAllowed}">
                                <a href='<c:url value="/export/delete.action?bulkIds=${export.id}"/>' class="icon-btn text-danger js-row-delete" data-tooltip="${exportDeleteMessage}">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </c:if>
                        </agnDisplay:column>
                    </agnDisplay:table>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
