<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="export" type="com.agnitas.beans.ExportPredef"--%>
<%--@elvariable id="exports" type="java.util.List<com.agnitas.beans.ExportPredef>"--%>
<%--@elvariable id="form" type="com.agnitas.web.forms.PaginationForm"--%>

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
                                    <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${exportDeleteMessage}" data-form-url='${bulkDeleteUrl}' data-form-confirm>
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
                    <emm:table var="export" cssClass="table table-hover table--borderless js-table" modelAttribute="exports">
                        <c:if test="${deleteAllowed}">
                            <c:set var="checkboxSelectAll">
                                <input class="form-check-input" type="checkbox" data-bulk-checkboxes autocomplete="off" />
                            </c:set>

                            <emm:column title="${checkboxSelectAll}" cssClass="mobile-hidden" headerClass="mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="bulkIds" value="${export.id}" autocomplete="off" data-bulk-checkbox />
                            </emm:column>
                        </c:if>

                        <emm:column sortable="true" titleKey="default.Name" property="shortname" />
                        <emm:column sortable="true" titleKey="Description" property="description" />

                        <emm:column headerClass="${deleteAllowed ? '' : 'hidden'}" cssClass="${deleteAllowed ? '' : 'hidden'}">
                            <a href='<c:url value="/export/${export.id}/view.action"/>' class="hidden" data-view-row="page"></a>

                            <c:if test="${deleteAllowed}">
                                <a href='<c:url value="/export/delete.action?bulkIds=${export.id}"/>' class="icon-btn icon-btn--danger js-row-delete" data-tooltip="${exportDeleteMessage}">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </c:if>
                        </emm:column>
                    </emm:table>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
