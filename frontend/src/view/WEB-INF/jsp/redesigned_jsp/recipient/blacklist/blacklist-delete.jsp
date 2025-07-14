<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="blacklistDeleteForm" type="com.agnitas.emm.core.globalblacklist.forms.BlacklistDeleteForm"--%>
<%--@elvariable id="mailinglists" type="java.util.List"--%>

<c:set var="isBulkDeletion" value="${fn:length(blacklistDeleteForm.emails) gt 1}"/>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <mvc:form cssClass="modal-content" method="POST" modelAttribute="blacklistDeleteForm">

            <c:forEach var="email" items="${blacklistDeleteForm.emails}">
                <input type="hidden" name="emails" value="${email}">
            </c:forEach>

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="${isBulkDeletion ? 'bulkAction.delete.recipients' : 'recipient.RecipientDelete'}"/></h1>
                <button type="button" class="btn-close js-confirm-negative">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body vstack gap-3 js-scrollable">
                <p>
                    <c:choose>
                        <c:when test="${isBulkDeletion}">
                            <mvc:message code="bulkAction.delete.blacklist.question" />
                        </c:when>
                        <c:otherwise>
                            <mvc:message code="recipient.blacklist.delete.question" arguments="${[blacklistDeleteForm.email]}" />
                        </c:otherwise>
                    </c:choose>
                </p>

                <c:if test="${isBulkDeletion}">
                    <div class="table-wrapper" data-js-table="blacklisted-emails">
                        <div class="table-wrapper__header justify-content-end">
                            <div class="table-wrapper__controls">
                                <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                                <jsp:include page="../../common/table/entries-label.jsp" />
                            </div>
                        </div>
                    </div>

                    <script id="blacklisted-emails" type="application/json">
                        {
                            "columns": [
                                 {
                                    "field": "name",
                                    "headerName": "<mvc:message code='mailing.MediaType.0'/>",
                                    "filter": false,
                                    "suppressMovable": true,
                                    "sortable": true,
                                    "sort": "asc",
                                    "resizable": false,
                                    "cellRenderer": "NotEscapedStringCellRenderer"
                                }
                            ],
                            "options": {
                                "pagination": false,
                                "singleNameTable": true,
                                "showRecordsCount": "simple"
                            },
                            "data": ${emm:toJson(blacklistDeleteForm.emails)}
                        }
                    </script>
                </c:if>

                <c:if test="${not empty mailinglists}">
                    <div>
                        <label class="form-label"><mvc:message code="blacklist.mailinglists"/></label>

                        <div class="d-flex flex-column gap-1">
                            <c:forEach var="mailinglist" items="${mailinglists}">
                                <div class="form-check form-switch">
                                    <mvc:checkbox path="mailingListIds" id="mailinglist-${mailinglist.id}" cssClass="form-check-input" role="switch" value="${mailinglist.id}" />
                                    <label class="form-label form-check-label fw-normal" for="mailinglist-${mailinglist.id}">${mailinglist.shortname}</label>
                                </div>
                            </c:forEach>
                        </div>
                    </div>

                    <div class="notification-simple notification-simple--lg notification-simple--info">
                        <span><mvc:message code="blacklist.mailinglists.hint" /></span>
                    </div>
                </c:if>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-danger js-confirm-positive">
                    <i class="icon icon-trash-alt"></i>
                    <span><mvc:message code="button.Delete"/></span>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
