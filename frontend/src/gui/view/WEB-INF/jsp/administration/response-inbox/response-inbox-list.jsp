<%@ page import="com.agnitas.emm.core.response_inbox.enums.MailloopReplyStatus" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="filter" type="com.agnitas.emm.core.response_inbox.forms.ResponseInboxOverviewFilter"--%>
<%--@elvariable id="replies" type="com.agnitas.beans.PaginatedList"--%>

<mvc:message var="deleteMsg" code="Delete" />
<c:set var="allowedDeletion" value="${emm:permissionAllowed('inbox.delete', pageContext.request)}" />
<c:url var="deleteUrl" value="/response-inbox/delete.action" />

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
    <mvc:form id="table-tile" cssClass="tile" servletRelativeAction="/response-inbox.action" modelAttribute="filter" method="GET" data-editable-tile="main">

        <script type="application/json" data-initializer="web-storage-persist">
            {
                "response-inbox-overview": {
                    "rows-count": ${filter.numberOfRows}
                }
            }
        </script>

        <div class="tile-body">
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
                                    <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${deleteMsg}" data-form-url="${deleteUrl}" data-form-confirm>
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </div>
                            </div>
                        </c:if>
                        <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../common/table/entries-label.jsp">
                            <jsp:param name="filteredEntries" value="${replies.fullListSize}"/>
                            <jsp:param name="totalEntries" value="${replies.notFilteredFullListSize}"/>
                        </jsp:include>
                    </div>
                </div>

                <div class="table-wrapper__body">
                    <emm:table var="reply" modelAttribute="replies" cssClass="table table-hover table--borderless js-table">

                        <%--@elvariable id="reply" type="com.agnitas.emm.core.response_inbox.bean.MailloopReplyEntry"--%>

                        <c:if test="${allowedDeletion}">
                            <emm:column title="<input class='form-check-input' type='checkbox' data-bulk-checkboxes autocomplete='off' />" cssClass="mobile-hidden" headerClass="mobile-hidden">
                                <input class="form-check-input" type="checkbox" name="ids" value="${reply.id}" autocomplete="off" data-bulk-checkbox />
                            </emm:column>
                        </c:if>

                        <emm:column titleKey="mailing.Subject" sortable="true" sortProperty="subject">
                            <div class="hstack gap-2 overflow-wrap-anywhere">
                                <span class="status-badge mailloop.reply.status.${fn:toLowerCase(reply.status)}"
                                      data-reply-status-badge="${reply.id}" data-tooltip="<mvc:message code="${reply.status.messageKey}" />"></span>
                                <span class="text-truncate-table">${reply.subject}</span>
                            </div>
                        </emm:column>

                        <emm:column titleKey="mailing.SenderEmail" sortable="true" sortProperty="sender_full_name">
                            <a href="<c:url value="/response-inbox/${reply.id}/view.action"/>" class="hidden" data-view-row></a>
                            <span>${fn:escapeXml(reply.sender)}</span>
                        </emm:column>

                        <emm:column titleKey="mailloop.filter_adr" sortable="true" sortProperty="response_email" property="responseEmail" />
                        <emm:column titleKey="recipient.Timestamp" sortable="true" property="timestamp" headerClass="fit-content" />

                        <c:if test="${allowedDeletion}">
                            <emm:column>
                                <a href="${deleteUrl}?ids=${reply.id}" class="icon-btn icon-btn--danger js-row-delete" data-tooltip="${deleteMsg}">
                                    <i class="icon icon-trash-alt"></i>
                                </a>
                            </emm:column>
                        </c:if>
                    </emm:table>
                </div>
            </div>
        </div>
    </mvc:form>
    <mvc:form id="filter-tile" cssClass="tile" method="GET" servletRelativeAction="/response-inbox/search.action" modelAttribute="filter"
              data-toggle-tile="" data-form="resource" data-resource-selector="#table-tile" data-editable-tile="">
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
        <div class="tile-body form-column js-scrollable">
            <div>
                <mvc:message var="subjectMsg" code="mailing.Subject" />
                <label for="filter-subject" class="form-label">${subjectMsg}</label>
                <mvc:text id="filter-subject" path="subject" cssClass="form-control" placeholder="${subjectMsg}" />
            </div>

            <div>
                <mvc:message var="senderMsg" code="mailing.SenderEmail" />
                <label for="filter-sender" class="form-label">${senderMsg}</label>
                <mvc:text id="filter-sender" path="sender" cssClass="form-control" placeholder="${senderMsg}" />
            </div>

            <div>
                <mvc:message var="responseAddressMsg" code="mailloop.filter_adr" />
                <label for="filter-response-address" class="form-label">${responseAddressMsg}</label>
                <mvc:text id="filter-response-address" path="responseAddress" cssClass="form-control" placeholder="${responseAddressMsg}" />
            </div>

            <div>
                <label class="form-label" for="filter-timestamp-from"><mvc:message code="recipient.Timestamp" /></label>
                <mvc:dateRange id="filter-timestamp" inline="true" path="timestamp" options="maxDate: 0" />
            </div>

            <div>
                <label for="filter-status" class="form-label"><mvc:message code="autoImport.status" /></label>

                <mvc:select id="filter-status" path="status" cssClass="form-control" data-result-template="select2-badge-option" data-selection-template="select2-badge-option">
                    <mvc:option value=""><mvc:message code="default.All" /></mvc:option>
                    <c:forEach var="status" items="${MailloopReplyStatus.values()}">
                        <mvc:option value="${status}" data-badge-class="mailloop.reply.status.${fn:toLowerCase(status)}"><mvc:message code="${status.messageKey}" /></mvc:option>
                    </c:forEach>
                </mvc:select>
            </div>
        </div>
    </mvc:form>
</div>
