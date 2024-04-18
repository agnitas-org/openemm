<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="blacklistDeleteForm" type="com.agnitas.emm.core.globalblacklist.forms.BlacklistDeleteForm"--%>
<%--@elvariable id="mailinglists" type="java.util.List"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-lg modal-dialog-centered">
        <mvc:form cssClass="modal-content" servletRelativeAction="/recipients/blacklist/delete.action" modelAttribute="blacklistDeleteForm" id="blacklistDeleteView">
            <mvc:hidden path="email"/>

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="Recipient"/> `${blacklistDeleteForm.email}`</h1>
                <button type="button" class="btn-close shadow-none js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <div class="row g-3">
                    <div class="col-12">
                        <p><mvc:message code="recipient.blacklist.delete"/></p>
                    </div>

                    <c:if test="${not empty mailinglists}">
                        <div class="col-12">
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

                        <div class="col-12">
                            <div class="notification-simple notification-simple--lg notification-simple--info">
                                <span><mvc:message code="blacklist.mailinglists.hint" /></span>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-danger js-confirm-positive flex-grow-1">
                    <i class="icon icon-trash-alt"></i>
                    <span><mvc:message code="button.Delete"/></span>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
