<%@ page errorPage="/errorRedesigned.action" %>
<%@ page contentType="text/html; charset=utf-8"%>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="campaignList" type="com.agnitas.beans.impl.PaginatedListImpl"--%>
<%--@elvariable id="form" type="com.agnitas.web.forms.PaginationForm"--%>

<mvc:message var="deletionTooltip" code="campaign.Delete" />
<c:set var="deleteAllowed" value="${emm:permissionAllowed('campaign.delete', pageContext.request)}" />

<div class="tiles-container">
    <mvc:form id="table-tile" servletRelativeAction="/mailing/archive/list.action" cssClass="tile" modelAttribute="form" method="GET">

        <script type="application/json" data-initializer="web-storage-persist">
            {
                "archive-overview": {
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
                                    <c:url var="bulkDeleteUrl" value="/mailing/archive/deleteRedesigned.action"/>
                                    <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${deletionTooltip}" data-form-url='${bulkDeleteUrl}' data-form-confirm>
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </div>
                            </div>
                        </c:if>
                        <%@include file="../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../common/table/entries-label.jsp">
                            <jsp:param name="totalEntries" value="${campaignList.fullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <emm:table var="campaign" modelAttribute="campaignList" cssClass="table table-hover table--borderless js-table">

                        <%--@elvariable id="campaign" type="com.agnitas.beans.Campaign"--%>

                        <c:if test="${deleteAllowed}">
                            <c:set var="checkboxSelectAll">
                                <input class="form-check-input" type="checkbox" data-bulk-checkboxes />
                            </c:set>

                            <emm:column title="${checkboxSelectAll}" cssClass="mobile-hidden" headerClass="mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="bulkIds" value="${campaign.id}" data-bulk-checkbox />
                            </emm:column>
                        </c:if>

                        <emm:column titleKey="mailing.archive" sortable="true" property="shortname" />
                        <emm:column titleKey="Description"     sortable="true" property="description" />

                        <emm:column>
                            <a href='<c:url value="/mailing/archive/${campaign.id}/view.action"/>' class="hidden" data-view-row></a>

                            <c:if test="${deleteAllowed}">
                                <a href='<c:url value="/mailing/archive/deleteRedesigned.action?bulkIds=${campaign.id}"/>' class="icon-btn icon-btn--danger js-row-delete" data-tooltip="${deletionTooltip}">
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
