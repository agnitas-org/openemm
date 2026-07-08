<%@ page import="java.net.URLEncoder" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="blacklists" type="java.util.List"--%>
<%--@elvariable id="blacklistDto" type="com.agnitas.emm.core.globalblacklist.beans.BlacklistDto"--%>
<%--@elvariable id="blacklistListForm" type="com.agnitas.emm.core.globalblacklist.forms.BlacklistOverviewFilter"--%>

<mvc:message var="deleteMsg" code="blacklist.BlacklistDelete" />

<c:url var="saveActionUrl" value="/recipients/blacklist/save.action" />

<c:set var="changeAllowed" value="${emm:permissionAllowed('recipient.change', pageContext.request)}" />
<c:set var="deleteAllowed" value="${emm:permissionAllowed('recipient.delete', pageContext.request)}" />

<mvc:form id="blacklist-overview" cssClass="tiles-container" servletRelativeAction="/recipients/blacklist/list.action"
     modelAttribute="blacklistListForm" data-controller="blacklist-list" data-editable-view="${agnEditViewKey}">

    <script type="application/json" data-initializer="web-storage-persist">
        {
            "blacklist-overview": {
                "rows-count": ${blacklistListForm.numberOfRows}
            }
        }
    </script>

    <div class="tiles-block flex-column" style="flex: 3">
        <div id="creation-tile" class="tile h-auto flex-shrink-0" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="recipient.Blacklist"/></h1>
            </div>

            <div class="tile-body">
                <div class="row">
                    <div class="col">
                        <label for="new-entry-email" class="form-label"><mvc:message code="mailing.MediaType.0" /></label>
                        <input id="new-entry-email" class="form-control" placeholder="${emailPlaceholder}" />
                    </div>

                    <div class="col">
                        <label for="new-entry-reason" class="form-label"><mvc:message code="blacklist.reason" /></label>
                        <input id="new-entry-reason" class="form-control" />
                    </div>

                    <div class="col-auto d-flex align-items-end">
                        <button class="btn btn-primary" type="button" data-action="save" data-url="${saveActionUrl}">
                            <i class="icon icon-plus"></i>
                            <mvc:message code="button.Add" />
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div id="table-tile" class="tile" data-editable-tile="main">
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

                                        <a href="#" class="icon-btn icon-btn--danger"
                                           data-tooltip="${deleteMsg}"
                                           data-form-url="<c:url value='/recipients/blacklist/delete.action'/>"
                                           data-form-method="GET"
                                           data-form-confirm>
                                            <i class="icon icon-trash-alt"></i>
                                        </a>
                                    </div>
                                </div>
                            </c:if>

                            <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                            <jsp:include page="../../common/table/entries-label.jsp">
                                <jsp:param name="filteredEntries" value="${blacklists.fullListSize}"/>
                                <jsp:param name="totalEntries" value="${blacklists.notFilteredFullListSize}"/>
                            </jsp:include>
                        </div>
                    </div>

                    <div class="table-wrapper__body">
                        <emm:table var="blacklistDto" modelAttribute="blacklists" cssClass="table table-hover table--borderless js-table">

                            <c:if test="${deleteAllowed}">
                                <c:set var="checkboxSelectAll">
                                    <input class="form-check-input" type="checkbox" data-bulk-checkboxes autocomplete="off" />
                                </c:set>

                                <emm:column title="${checkboxSelectAll}" cssClass="mobile-hidden" headerClass="mobile-hidden">
                                    <input class="form-check-input" type="checkbox" name="emails" value="${blacklistDto.email}" autocomplete="off" data-bulk-checkbox />
                                </emm:column>
                            </c:if>

                            <emm:column titleKey="mailing.MediaType.0" sortable="true" property="email" />

                            <emm:column titleKey="blacklist.reason" sortable="true" sortProperty="reason">
                                <span>${fn:escapeXml(blacklistDto.reason)}</span>
                                <c:if test="${changeAllowed}">
                                    <a href="#"
                                       data-modal="modal-edit-blacklisted-recipient"
                                       data-modal-set="email: '${blacklistDto.email}', reason: '${blacklistDto.reason}'" data-view-row></a>
                                </c:if>
                            </emm:column>

                            <emm:column property="date" headerClass="fit-content" sortProperty="timestamp" titleKey="CreationDate" sortable="true" />

                            <c:if test="${deleteAllowed}">
                                <emm:column>
                                    <c:url var="deleteUrl" value="/recipients/blacklist/delete.action">
                                        <c:param name="emails" value="${blacklistDto.email}"/>
                                    </c:url>
                                    <a href="${deleteUrl}" class="icon-btn icon-btn--danger js-row-delete" data-tooltip="${deleteMsg}">
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </emm:column>
                            </c:if>
                        </emm:table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div id="filter-tile" class="tile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-secondary"  data-form-clear="#filter-tile" data-form-submit data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" data-form-submit data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>

        <div class="tile-body vstack gap-3 js-scrollable">
            <div>
                <label class="form-label" for="filter-email"><mvc:message code="mailing.MediaType.0" /></label>
                <mvc:text id="filter-email" path="email" cssClass="form-control" placeholder="${emailPlaceholder}"/>
            </div>

            <div>
                <label class="form-label" for="filter-reason"><mvc:message code="blacklist.reason" /></label>
                <mvc:text id="filter-reason" path="reason" cssClass="form-control"/>
            </div>

            <div>
                <label class="form-label" for="creation-date-from"><mvc:message code="CreationDate"/></label>
                <mvc:dateRange id="creation-date" path="creationDate" options="maxDate: 0" />
            </div>
        </div>
    </div>
</mvc:form>

<c:if test="${changeAllowed}">
    <%@ include file="fragments/modal-edit-blacklisted-recipient.jspf" %>
</c:if>
